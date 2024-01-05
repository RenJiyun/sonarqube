
package com.wlzq.activity.double11.service.app;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.double11.dao.ActCustomerUnionIdDao;
import com.wlzq.activity.double11.dto.QrcodeDto;
import com.wlzq.activity.double11.model.ActCustomerUnionId;
import com.wlzq.activity.task.biz.HengShengBiz;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Double11Service服务类
 *
 * @author
 * @version 1.0
 */
@Service("activity.double11")
public class Double11Service {

    @Autowired
    private Double11Biz double11Biz;
    @Autowired
    private HengShengBiz hengShengBiz;
    @Autowired
    private ActCustomerUnionIdDao actCustomerUnionIdDao;

    @Signature(true)
    @MustLogin(true)
    public ResultDto recieve(RequestParams params, AccTokenUser user, Customer customer) {
        String customerId = customer != null ? customer.getCustomerId() : null;
        String fundAccount = customer != null ? customer.getFundAccount() : null;
        StatusDto result = double11Biz.l2receive(user.getUserId(), user.getOpenid(), user.getMobile(), customerId, fundAccount);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, "");
    }

    /**
     * 员工活动与奖励-排行榜查询
     */
    @Signature(true)
    public ResultDto achievement(RequestParams params, AccTokenUser user, Customer customer) {
        /*榜单日期*/
        Long achievementDate = params.getLong("achievementDate");
        /*榜单类型*/
        Integer achievementType = params.getInt("achievementType");

        StatusObjDto<Map<String, Object>> result = double11Biz.achievement(achievementDate, achievementType);

        return new ResultDto(0, result.getObj(), result.getMsg());
    }


    /**
     * 保存 unionid
     *
     * @param customerId | 客户id |  | required
     * @param mobile     | 手机号 |  | required
     * @param unionId    | unionId |  | required
     * @param openId     | openId |  | required
     * @cate 2023年双十一
     */
    @Signature(true)
    public ResultDto saveunionid(RequestParams params, AccTokenUser user, Customer customer) {
        String unionId = params.getString("unionId");
        if (ObjectUtils.isEmptyOrNull(unionId)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("unionId");
        }
        String openId = params.getString("openId");
        if (ObjectUtils.isEmptyOrNull(openId)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("openId");
        }
        String customerId = params.getString("customerId");
        if (ObjectUtils.isEmptyOrNull(customerId)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("customerId");
        }
        String mobile = params.getString("mobile");
        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        StatusDto result = double11Biz.saveUnionId(unionId, openId, customerId, mobile);
        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, "");
    }


    /**
     * 获取二维码
     *
     * @return com.wlzq.activity.double11.dto.QrcodeDto
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto qiweiqrcode(RequestParams params, AccTokenUser user, Customer customer) {
        QrcodeDto qrcodeDto = double11Biz.getQrcode(customer.getCustomerId());
        if (qrcodeDto == null) {
            return new ResultDto(StatusDto.FAIL_COMMON, "获取二维码失败");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", qrcodeDto);
        return new ResultDto(0, data, "");
    }

    /**
     * 查询添加结果
     *
     * @param mobile | 手机号 |  | required
     * @cate 2023年双十一
     */
    @Signature(true)
    public ResultDto chkaddresult(RequestParams params, AccTokenUser user, Customer customer) {
        String mobile = params.getString("mobile");
        if (ObjectUtils.isEmptyOrNull(mobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
        }
        ActCustomerUnionId qryActCustomerUnionId = new ActCustomerUnionId()
                .setMobile(mobile);
        List<ActCustomerUnionId> actCustomerUnionIdList = actCustomerUnionIdDao.findList(qryActCustomerUnionId);
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtil.isEmpty(actCustomerUnionIdList)) {
            result.put("result", 0);
        } else {
            ActCustomerUnionId customerUnionId = actCustomerUnionIdList.get(0);
            Tuple addResult = double11Biz.checkQiWeiAddResult(customerUnionId.getCustomerId(), customerUnionId.getUnionId());
            if (addResult.get(0)) {
                result.put("result", 1);
            } else {
                result.put("result", 0);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", result);
        return new ResultDto(0, data, "");
    }

    /**
     * 查询港股通权限
     *
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto chkggperm(RequestParams params, AccTokenUser user, Customer customer) {
        boolean checkResult = hengShengBiz.checkGgPerm(user, customer);
        Map<String, Object> resultMap = new HashMap<>();
        if (checkResult) {
            resultMap.put("result", 1);
        } else {
            resultMap.put("result", 0);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", resultMap);
        return new ResultDto(0, data, "");
    }

    /**
     * 查询是否开通北交所权限
     *
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto chkbjsperm(RequestParams params, AccTokenUser user, Customer customer) {
        boolean checkResult = hengShengBiz.checkBjsPerm(user, customer);
        Map<String, Object> resultMap = new HashMap<>();
        if (checkResult) {
            resultMap.put("result", 1);
        } else {
            resultMap.put("result", 0);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", resultMap);
        return new ResultDto(0, data, "");
    }


    /**
     * 查询是否开通科创板权限
     *
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto chkkcbperm(RequestParams params, AccTokenUser user, Customer customer) {
        boolean checkResult = hengShengBiz.checkKcbPerm(user, customer);
        Map<String, Object> resultMap = new HashMap<>();
        if (checkResult) {
            resultMap.put("result", 1);
        } else {
            resultMap.put("result", 0);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", resultMap);
        return new ResultDto(0, data, "");
    }


    /**
     * 补填订单推荐人
     *
     * @param outTradeNo      | outTradeNo |  | required
     * @param recommendMobile | 推荐人手机号 |  | required
     * @cate 2023年双十一
     */
    @Signature(true)
    @MustLogin(true)
    public ResultDto supplementrecommend(RequestParams params, AccTokenUser user, Customer customer) {
        String outTradeNo = params.getString("outTradeNo");
        if (ObjectUtils.isEmptyOrNull(outTradeNo)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("outTradeNo");
        }
        String recommendMobile = params.getString("recommendMobile");
        if (ObjectUtils.isEmptyOrNull(recommendMobile)) {
            throw BizException.COMMON_PARAMS_NOT_NULL.format("recommendMobile");
        }

        double11Biz.supplementRecommend(user, outTradeNo, recommendMobile);
        return new ResultDto(0, null, "");
    }


    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(true)
    public ResultDto chknoact(RequestParams params, AccTokenUser user, Customer customer) {
        boolean checkResult = double11Biz.checkNonAccount(customer);
        Map<String, Object> resultMap = new HashMap<>();
        if (checkResult) {
            resultMap.put("result", 1);
        } else {
            resultMap.put("result", 0);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("info", resultMap);
        return new ResultDto(0, data, "");
    }
}
