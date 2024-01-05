package com.wlzq.activity.bill.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.google.gson.Gson;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.bill.biz.ActBill2023Biz;
import com.wlzq.activity.bill.dao.ActBill2023Dao;
import com.wlzq.activity.bill.dao.ActReceiveRegDao;
import com.wlzq.activity.bill.dto.ActBill2023Dto;
import com.wlzq.activity.bill.model.ActBill2023;
import com.wlzq.activity.bill.model.ActReceiveReg;
import com.wlzq.activity.double11.biz.Double11Biz;
import com.wlzq.activity.double11.dao.ActCustomerUnionIdDao;
import com.wlzq.activity.double11.dto.QrcodeDto;
import com.wlzq.activity.double11.model.ActCustomerUnionId;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.dao.ActTaskDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.config.AopConfigUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author renjiyun
 */
@Service
@Slf4j
public class ActBill2023BizImpl implements ActBill2023Biz {
    /**
     * 添加企微任务编码
     */
    private static final String QIWEI_TASK_CODE = "TASK.2023DOUBLE11.31";

    /**
     * 2023年度账单活动编码
     */
    private static final String ANNUAL_BILL_ACTIVITY_CODE = "ACTIVITY.ANNUAL.BILL.2023";

    /**
     * 2023年度账单活动企微添加奖品优惠券编码
     */
    private static final String ANNUAL_BILL_PRIZE_CODE = "PRIZE.ANNUAL.BILL.2023.AL2";

    private static final Map<String, Integer> NATIONAL_DEBT = new HashMap<>();
    static {
        NATIONAL_DEBT.put("204001", 1);
        NATIONAL_DEBT.put("204002", 2);
        NATIONAL_DEBT.put("204003", 3);
        NATIONAL_DEBT.put("204004", 4);
        NATIONAL_DEBT.put("204007", 7);
        NATIONAL_DEBT.put("204014", 14);
        NATIONAL_DEBT.put("204028", 28);
        NATIONAL_DEBT.put("204091", 91);
        NATIONAL_DEBT.put("204182", 182);
        NATIONAL_DEBT.put("131810", 1);
        NATIONAL_DEBT.put("131811", 2);
        NATIONAL_DEBT.put("131800", 3);
        NATIONAL_DEBT.put("131809", 4);
        NATIONAL_DEBT.put("131801", 7);
        NATIONAL_DEBT.put("131802", 14);
        NATIONAL_DEBT.put("131803", 28);
        NATIONAL_DEBT.put("131805", 91);
        NATIONAL_DEBT.put("131806", 182);
    }

    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;
    @Autowired
    private ActReceiveRegDao actReceiveRegDao;
    @Autowired
    private ActCustomerUnionIdDao actCustomerUnionIdDao;
    @Autowired
    private Double11Biz double11Biz;
    @Autowired
    private ActTaskDao actTaskDao;
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActivityBaseBiz activityBaseBiz;
    @Autowired
    private ActBill2023Dao actBill2023Dao;
    @Autowired
    private ActBill2023Biz self;

    @Override
    public boolean chkAddResult(String customerId) {
        ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
                .setCustomerId(customerId)
                .setTaskCode(QIWEI_TASK_CODE);

        List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.list(qryActGoodsFlow);
        return CollectionUtil.isNotEmpty(actGoodsFlows);

    }

    @Override
    @Transactional
    public boolean receiveReg(String userId, String mobile, String customerId) {
        ActReceiveReg qryActReceiveReg = new ActReceiveReg()
                .setCustomerId(customerId)
                .setActivityCode(ANNUAL_BILL_ACTIVITY_CODE);
        List<ActReceiveReg> actReceiveRegs = actReceiveRegDao.findList(qryActReceiveReg);

        Date now = new Date();
        if (CollectionUtil.isNotEmpty(actReceiveRegs)) {
            ActReceiveReg actReceiveReg = actReceiveRegs.get(0);

            // 若已经领取(通过批处理), 则不再进行更新
            if (Objects.equals(actReceiveReg.getStatus(), ActReceiveReg.STATUS_RECEIVED)) {
                return true;
            }

            actReceiveReg.setUserId(userId)
                    .setMobile(mobile)
                    .setUpdateTime(now);
            actReceiveRegDao.update(actReceiveReg);
        } else {
            ActReceiveReg actReceiveReg = new ActReceiveReg()
                    .setUserId(userId)
                    .setCustomerId(customerId)
                    .setMobile(mobile)
                    .setActivityCode(ANNUAL_BILL_ACTIVITY_CODE)
                    .setStatus(ActReceiveReg.STATUS_REG)
                    .setCreateTime(now);
            actReceiveRegDao.insert(actReceiveReg);
        }
        return true;
    }

    @Override
    public void batchReceivePrize() {
        ActReceiveReg qryActReceiveReg = new ActReceiveReg();
        qryActReceiveReg.setActivityCode(ANNUAL_BILL_ACTIVITY_CODE);

        qryActReceiveReg.setStatus(ActReceiveReg.STATUS_REG);
        List<ActReceiveReg> actReceiveRegs = actReceiveRegDao.findList(qryActReceiveReg);

        qryActReceiveReg.setStatus(ActReceiveReg.STATUS_RECEIVE_FAIL);
        List<ActReceiveReg> preFailed = actReceiveRegDao.findList(qryActReceiveReg);

        List<ActReceiveReg> allReceiveRegs = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(actReceiveRegs)) {
            allReceiveRegs.addAll(actReceiveRegs);
        }
        if (CollectionUtil.isNotEmpty(preFailed)) {
            allReceiveRegs.addAll(preFailed);
        }

        if (CollectionUtil.isEmpty(allReceiveRegs)) {
            return;
        }

        Activity activity = activityBaseBiz.findActivity(ANNUAL_BILL_ACTIVITY_CODE);
        ActTask qryActTask = new ActTask()
                .setActivityCode(ANNUAL_BILL_ACTIVITY_CODE)
                .setCode(QIWEI_TASK_CODE);

        ActTask actTask = actTaskDao.get(qryActTask);
        Date now = new Date();

        log.info("批量领取企微添加奖励, 数量: {}", allReceiveRegs.size());
        List<ActReceiveReg> failed = new ArrayList<>();
        for (ActReceiveReg actReceiveReg : allReceiveRegs) {
            try {
                boolean receiveResult = self.receivePrize(activity, actTask, actReceiveReg, now);
                if (!receiveResult) {
                    failed.add(actReceiveReg);
                }
            } catch (Exception e) {
                log.error("领取企微添加奖励异常, customerId: " + actReceiveReg.getCustomerId(), e);
                failed.add(actReceiveReg);
            }
        }

        if (CollectionUtil.isNotEmpty(failed)) {
            log.warn("批量领取企微添加奖励失败, 数量: {}", failed.size());
            for (ActReceiveReg actReceiveReg : failed) {
                actReceiveReg.setStatus(ActReceiveReg.STATUS_RECEIVE_FAIL);
                actReceiveReg.setUpdateTime(now);
                actReceiveRegDao.update(actReceiveReg);
            }
        }
    }

    @Override
    @Transactional
    public boolean receivePrize(Activity activity, ActTask actTask, ActReceiveReg actReceiveReg, Date now) {
        // 校验是否已经完成过任务, 若已完成, 则不再进行领取
        ActGoodsFlow qryActGoodsFlow = new ActGoodsFlow()
                .setCustomerId(actReceiveReg.getCustomerId())
                .setTaskCode(QIWEI_TASK_CODE);
        List<ActGoodsFlow> actGoodsFlows = actGoodsFlowDao.list(qryActGoodsFlow);
        if (CollectionUtil.isNotEmpty(actGoodsFlows)) {
            log.warn("已经完成过任务, customerId: {}", actReceiveReg.getCustomerId());
            actReceiveReg.setStatus(ActReceiveReg.STATUS_RECEIVED);
            actReceiveReg.setUpdateTime(new Date());
            actReceiveRegDao.update(actReceiveReg);
            return true;
        }

        ActCustomerUnionId qryActCustomerUnionId = new ActCustomerUnionId()
                .setCustomerId(actReceiveReg.getCustomerId());
        List<ActCustomerUnionId> actCustomerUnionIdList = actCustomerUnionIdDao.findList(qryActCustomerUnionId);
        if (CollectionUtil.isEmpty(actCustomerUnionIdList)) {
            log.warn("未找到 unionId, customerId: {}", actReceiveReg.getCustomerId());
            return false;
        }
        String unionId = actCustomerUnionIdList.get(0).getUnionId();

        // 校验是否已经添加企微
        Tuple addResult = double11Biz.checkQiWeiAddResult(actReceiveReg.getCustomerId(), unionId);
        if (addResult.get(0)) {
            actReceiveReg.setStatus(ActReceiveReg.STATUS_RECEIVED);
            actReceiveReg.setUpdateTime(new Date());
            actReceiveRegDao.update(actReceiveReg);
        } else {
            log.info("未添加企微, customerId: {}, unionId: {}", actReceiveReg.getCustomerId(), unionId);
            return false;
        }

        String source = addResult.get(1);
        QrcodeDto qrcodeDto = double11Biz.getQrcode(actReceiveReg.getCustomerId());
        String qiweiMobile = "";
        if (qrcodeDto != null) {
            qiweiMobile = qrcodeDto.getMobile();
        }
        ActGoodsFlow newGoodsFlow = new ActGoodsFlow()
                .setMobile(actReceiveReg.getMobile())
                .setCustomerId(actReceiveReg.getCustomerId())
                .setUserId(actReceiveReg.getUserId())
                .setActivityCode(actTask.getActivityCode())
                .setTaskCode(actTask.getCode())
                .setBizCode(qiweiMobile)
                .setCreateTime(now)
                .setUpdateTime(now)
                .setFlag(ActGoodsFlow.FLOW_FLAG_GET)
                .setGoodsCode(actTask.getGoodsCode())
                .setGoodsQuantity(actTask.getGoodsQuantity())
                .setSource(source);
        actGoodsFlowDao.insert(newGoodsFlow);

        AccTokenUser accTokenUser = new AccTokenUser();
        accTokenUser.setUserId(actReceiveReg.getUserId());
        accTokenUser.setMobile(actReceiveReg.getMobile());

        Customer customer = new Customer();
        customer.setCustomerId(actReceiveReg.getCustomerId());
        actPrizeBiz.giveOutCoupon(activity, ANNUAL_BILL_PRIZE_CODE, accTokenUser, customer, "");
        return true;
    }

    @Override
    public ActBill2023Dto view(Customer customer) {
        ActBill2023Dto actBill2023Dto = new ActBill2023Dto();
        String visitorData = AppConfigUtils.get("activity.bill.2023.visitor.data");
        Map visitorDataMap = new Gson().fromJson(visitorData, Map.class);
        actBill2023Dto.setCommon(visitorDataMap);

        if (customer == null) {
            return actBill2023Dto;
        }
        ActBill2023 actBill2023 = actBill2023Dao.findBillByCustomerId(customer.getCustomerId());
        populateNationalDebt(actBill2023);
        actBill2023Dto.setClientBill(actBill2023);
        return actBill2023Dto;
    }

    private void populateNationalDebt(ActBill2023 actBill2023) {
        String maxProfitFinName = actBill2023.getMaxProfitFinName();
        if (ObjectUtils.isEmptyOrNull(maxProfitFinName)) {
            return;
        }

        String[] split = maxProfitFinName.split(",");
        String code = split[0].trim();
        String name = split[1].trim();
        if (NATIONAL_DEBT.containsKey(code)) {
            Integer term = NATIONAL_DEBT.get(code);
            String marketName = code.startsWith("2") ? "沪市" : "深市";
            actBill2023.setMaxProfitFinName("国债逆回购" + marketName + term + "天期");
        } else {
            actBill2023.setMaxProfitFinName(name + code);
        }
    }
}
