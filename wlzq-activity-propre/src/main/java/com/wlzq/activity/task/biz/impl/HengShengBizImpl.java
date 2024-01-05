package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.task.biz.HengShengBiz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.T2Utils;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.core.global.ThreadGlobal;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.redis.UserRedis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author renjiyun
 */
@Service
@Slf4j
public class HengShengBizImpl implements HengShengBiz {

    // 证券股东信息查询的功能号
    private static final String FUNC_NO = "331300";
    private static final Integer MAX_REQUEST_NUM = 1000;
    private static final String INIT_POSITION_STR = " ";

    @Override
    public boolean checkGgPerm(AccTokenUser user, Customer customer) {
        if (user == null || customer == null) {
            return false;
        }
        return haveGgPerm(getAllHs331300Dto(user, customer));
    }

    private boolean haveGgPerm(List<Hs331300Dto> hs331300DtoList) {
        if (CollectionUtil.isEmpty(hs331300DtoList)) {
            return false;
        }
        return hs331300DtoList.stream().anyMatch(e -> e.getExchangeType().equals("G") || e.getExchangeType().equals("S"));
    }


    @Override
    public boolean checkBjsPerm(AccTokenUser user, Customer customer) {
        return haveBjsPerm(getAllHs331300Dto(user, customer));
    }

    @Override
    public boolean checkKcbPerm(AccTokenUser user, Customer customer) {
        return haveKcbPerm(getAllHs331300Dto(user, customer));
    }

    @Override
    public boolean checkKcbNotBjsPerm(AccTokenUser user, Customer customer) {
        List<Hs331300Dto> hs331300DtoList = getAllHs331300Dto(user, customer);
        return haveKcbPerm(hs331300DtoList) && !haveBjsPerm(hs331300DtoList);
    }

    private boolean haveBjsPerm(List<Hs331300Dto> hs331300DtoList) {
        if (CollectionUtil.isEmpty(hs331300DtoList)) {
            return false;
        }
        return hs331300DtoList.stream().anyMatch(e -> ObjectUtils.isNotEmptyOrNull(e.getHolderRights())
                && (e.getHolderRights().contains("$") || e.getHolderRights().contains("7") || e.getHolderRights().contains("8")));
    }

    private boolean haveKcbPerm(List<Hs331300Dto> hs331300DtoList) {
        if (CollectionUtil.isEmpty(hs331300DtoList)) {
            return false;
        }
        return hs331300DtoList.stream().anyMatch(e -> ObjectUtils.isNotEmptyOrNull(e.getHolderRights())
                && e.getHolderRights().contains("O"));
    }

    private List<Hs331300Dto> getAllHs331300Dto(AccTokenUser user, Customer customer) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("op_branch_no", customer.getBranchNo())
                .put("op_entrust_way", "D")
                .put("op_station", getOpStation(user))
                .put("branch_no", customer.getBranchNo())
                .put("client_id", customer.getCustomerId())
                .put("user_token", customer.getUserToken())
                .put("password", getCustomerPassword(customer))
                .build();

        List<Hs331300Dto> hs331300DtoList = all(params, customer);
        return hs331300DtoList;
    }

    private String getOpStation(AccTokenUser user) {
        String opStation = "ds|" + ThreadGlobal.reqestId.get().getClientIp();
        if (user != null) {
            String uid = ObjectUtils.isNotEmptyOrNull(user.getMobile()) ?
                    user.getMobile() : ObjectUtils.isNotEmptyOrNull(user.getOpenid()) ? user.getOpenid() : "";
            String mac = ObjectUtils.isEmptyOrNull(user.getLoginMac()) ? "" : user.getLoginMac();
            uid = Objects.equals(mac, "") ? uid : uid + "," + mac;
            opStation += "," + uid;
        }
        return opStation;
    }

    private String getCustomerPassword(Customer customer) {
        String password = (String) UserRedis.CUSTOMER_PASS.get(customer.getCustomerId());
        if (ObjectUtils.isEmptyOrNull(password)) {
            throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
        }
        password = password.replace("encrypt_rsa:", "");

        if (ObjectUtils.isEmptyOrNull(password)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("password");
        }

        return password;
    }


    private List<Hs331300Dto> all(Map<String, Object> params, Customer customer) {
        params.put("funcNo", HengShengBizImpl.FUNC_NO);
        List<Hs331300Dto> dtoList = new ArrayList<>();
        params.put("position_str", INIT_POSITION_STR);
        params.put("request_num", MAX_REQUEST_NUM.toString());

        while (true) {
            ResultDto result = null;
            try {
                result = RemoteUtils.t2call(params);
                assert result != null;
                log.info("{} result: {} | {}", HengShengBizImpl.FUNC_NO, customer.getCustomerId(), JsonUtils.object2JSON(result));
            } catch (Exception e) {
                log.error("{} error: {} | {}", HengShengBizImpl.FUNC_NO, customer.getCustomerId(), e.getMessage());
                throw e;
            }

            checkResult(result);

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) result.getData().get("0");
            if (ObjectUtils.isNotEmptyOrNull(dataList)) {
                for (Map<String, Object> data : dataList) {
                    Hs331300Dto dto = BeanUtil.mapToBean(data, Hs331300Dto.class, true, null);
                    dtoList.add(dto);
                }
            }

            if (dataList.size() < MAX_REQUEST_NUM) {
                break;
            }

            Map<String, Object> lastData = dataList.get(dataList.size() - 1);
            // 更新定位串
            params.put("position_str", lastData.get("position_str"));
        }

        return dtoList;
    }

    private void checkResult(ResultDto result) {
        if (!result.getCode().equals(StatusDto.SUCCESS)) {
            List<String> errors = T2Utils.getErrors(result.getMsg());
            if (errors.size() == 3 && "330030".equals(errors.get(0))) {
                throw BizException.CUSTOMER_NOT_LOGIN_ERROR;
            }
            throw BizException.COMMON_CUSTOMIZE_ERROR.format(result.getMsg());
        }
    }

    private static class Hs331300Dto {
        private String exchangeType;
        private String stockAccount;
        private String mainFlag;
        private String holderKind;
        private String holderStatus;
        /** 股东权限 */
        private String holderRights;
        private String register;
        private String seatNo;
        private String acodeAccount;
        private String holderName;
        private String fundAccount;
        private String assetProp;

        public String getExchangeType() {
            return exchangeType;
        }

        public void setExchangeType(String exchangeType) {
            this.exchangeType = exchangeType;
        }

        public String getStockAccount() {
            return stockAccount;
        }

        public void setStockAccount(String stockAccount) {
            this.stockAccount = stockAccount;
        }

        public String getMainFlag() {
            return mainFlag;
        }

        public void setMainFlag(String mainFlag) {
            this.mainFlag = mainFlag;
        }

        public String getHolderKind() {
            return holderKind;
        }

        public void setHolderKind(String holderKind) {
            this.holderKind = holderKind;
        }

        public String getHolderStatus() {
            return holderStatus;
        }

        public void setHolderStatus(String holderStatus) {
            this.holderStatus = holderStatus;
        }

        public String getHolderRights() {
            return holderRights;
        }

        public void setHolderRights(String holderRights) {
            this.holderRights = holderRights;
        }

        public String getRegister() {
            return register;
        }

        public void setRegister(String register) {
            this.register = register;
        }

        public String getSeatNo() {
            return seatNo;
        }

        public void setSeatNo(String seatNo) {
            this.seatNo = seatNo;
        }

        public String getAcodeAccount() {
            return acodeAccount;
        }

        public void setAcodeAccount(String acodeAccount) {
            this.acodeAccount = acodeAccount;
        }

        public String getHolderName() {
            return holderName;
        }

        public void setHolderName(String holderName) {
            this.holderName = holderName;
        }

        public String getFundAccount() {
            return fundAccount;
        }

        public void setFundAccount(String fundAccount) {
            this.fundAccount = fundAccount;
        }

        public String getAssetProp() {
            return assetProp;
        }

        public void setAssetProp(String assetProp) {
            this.assetProp = assetProp;
        }
    }
}
