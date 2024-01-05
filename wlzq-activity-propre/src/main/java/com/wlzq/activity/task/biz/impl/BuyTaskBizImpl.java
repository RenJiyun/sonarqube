package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.task.dao.VasPayAgreementDao;
import com.wlzq.activity.task.model.VasPayAgreement;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 购买类任务校验
 *
 * @author renjiyun
 */
@Service
public class BuyTaskBizImpl implements SpecificTaskBiz {
    @Autowired
    private VasPayAgreementDao vasPayAgreementDao;

    /** 购买投顾产品(14天产品规格以外) */
    private static final String TASK_CODE_2023DOUBLE11_19 = "TASK.2023DOUBLE11.19";
    /** 购买形态掘金大师选股工具 */
    private static final String TASK_CODE_2023DOUBLE11_20 = "TASK.2023DOUBLE11.20";
    /** 开通A股L2连续包月服务 */
    private static final String TASK_CODE_2023DOUBLE11_21 = "TASK.2023DOUBLE11.21";
    /** 购买风口研报决策锦囊 */
    private static final String TASK_CODE_2023DOUBLE11_22 = "TASK.2023DOUBLE11.22";
    /** 1元购掘金周期股课 */
    private static final String TASK_CODE_2023DOUBLE11_23 = "TASK.2023DOUBLE11.23";
    /** 购买涨停助手决策工具 */
    private static final String TASK_CODE_2023DOUBLE11_24 = "TASK.2023DOUBLE11.24";
    /** 体验投顾产品14天规格 */
    private static final String TASK_CODE_2023DOUBLE11_25 = "TASK.2023DOUBLE11.25";

    private final String[] supportTaskCodes = {
            TASK_CODE_2023DOUBLE11_19,
            TASK_CODE_2023DOUBLE11_20,
            TASK_CODE_2023DOUBLE11_21,
            TASK_CODE_2023DOUBLE11_22,
            TASK_CODE_2023DOUBLE11_23,
            TASK_CODE_2023DOUBLE11_24,
            TASK_CODE_2023DOUBLE11_25
    };

    @Override
    public String[] supportTaskCodes() {
        return supportTaskCodes;
    }

    @Override
    public Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                           String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch) {
        // bizCode 为订单号
        String taskCode = actTask.getCode();
        if (TASK_CODE_2023DOUBLE11_19.equals(taskCode)) {
            return checkServiceOrderFor14Days(activity, bizCode, customer, false);
        } else if (TASK_CODE_2023DOUBLE11_20.equals(taskCode)) {
            return checkXtjjds(activity, bizCode, mobile);
        } else if (TASK_CODE_2023DOUBLE11_21.equals(taskCode)) {
            return checkOpenAlv2(activity, bizCode, mobile);
        } else if (TASK_CODE_2023DOUBLE11_22.equals(taskCode)) {
            return checkFkybBag(activity, bizCode, mobile);
        } else if (TASK_CODE_2023DOUBLE11_23.equals(taskCode)) {
            return checkServiceOrderForKccp006(activity, bizCode, customer);
        } else if (TASK_CODE_2023DOUBLE11_24.equals(taskCode)) {
            return checkZtzs(activity, bizCode, mobile);
        } else if (TASK_CODE_2023DOUBLE11_25.equals(taskCode)) {
            return checkServiceOrderFor14Days(activity, bizCode, customer, true);
        }

        return new Tuple(false, "");
    }

    private Tuple checkXtjjds(Activity activity, String bizCode, String mobile) {
        String requiredGoodsCode = "XTJJDS";
        if (ObjectUtils.isEmptyOrNull(mobile) || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }

        Map<String, Object> order = getVasOrder(mobile, bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer status = (Integer) order.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        String goodsCode = (String) order.get("goodsCode");

        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        if (!requiredGoodsCode.equals(goodsCode)) {
            return new Tuple(false, "");
        }

        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkZtzs(Activity activity, String bizCode, String mobile) {
        String requiredGoodsCode = "ZTZS";
        if (ObjectUtils.isEmptyOrNull(mobile) || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }
        Map<String, Object> order = getVasOrder(mobile, bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer status = (Integer) order.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        String goodsCode = (String) order.get("goodsCode");

        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        if (!requiredGoodsCode.equals(goodsCode)) {
            return new Tuple(false, "");
        }

        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkFkybBag(Activity activity, String bizCode, String mobile) {
        String requiredGoodsCode = "FKYB";
        Integer requiredPriceType = 5;
        Integer requiredSpecification = 9;

        if (ObjectUtils.isEmptyOrNull(mobile) || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }
        Map<String, Object> order = getVasOrder(mobile, bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer priceType = (Integer) order.get("priceType");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        String goodsCode = (String) order.get("goodsCode");
        Integer specification = (Integer) order.get("specification");
        Integer status = (Integer) order.get("status");
        Integer activityPrice = (Integer) order.get("activityPrice");

        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        // 校验活动时间
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        // 校验产品编码
        if (!requiredGoodsCode.equals(goodsCode)) {
            return new Tuple(false, "");
        }

        // 校验 priceType == 5
        if (!requiredPriceType.equals(priceType)) {
            return new Tuple(false, "");
        }

        // 校验规格为9篇
        if (!requiredSpecification.equals(specification)) {
            return new Tuple(false, "");
        }

        // TODO
        // 校验活动价格为99元
        //        if (!activityPrice.equals(99 * 100)) {
        //            return new Tuple(false, "");
        //        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkOpenAlv2(Activity activity, String bizCode, String mobile) {
        String requiredGoodsCode = "ALv2";
        Integer requiredPriceType = 3;
        Integer requiredTimeType = 1;

        if (ObjectUtils.isEmptyOrNull(mobile)) {
            return new Tuple(false, "");
        }

        if (ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }

        Map<String, Object> order = getVasOrder(mobile, bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer priceType = (Integer) order.get("priceType");
        Integer timeType = (Integer) order.get("timeType");
        Integer status = (Integer) order.get("status");
        String orderMobile = (String) order.get("mobile");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        String goodsCode = (String) order.get("goodsCode");

        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        if (!requiredGoodsCode.equals(goodsCode)) {
            return new Tuple(false, "");
        }

        if (!mobile.equals(orderMobile)) {
            return new Tuple(false, "");
        }

        // 校验是否为连续包月
        if (!requiredPriceType.equals(priceType) || !requiredTimeType.equals(timeType)) {
            return new Tuple(false, "");
        }

        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkServiceOrderFor14Days(Activity activity, String bizCode, Customer customer, boolean include14Days) {
        Integer requiredTimeUnit = 1;
        Integer requiredTime = 14;

        if (ObjectUtils.isEmptyOrNull(customer) || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }

        Map<String, Object> order = getServiceOrder(customer.getCustomerId(), bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer status = (Integer) order.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        Integer timeUnit = (Integer) order.get("timeUnit");
        Integer time = (Integer) order.get("time");

        // 校验订单状态
        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        // 校验订单时间
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        // 若产品规格为14天
        if (requiredTimeUnit.equals(timeUnit) && time <= requiredTime) {
            if (!include14Days) {
                return new Tuple(false, "");
            }
        } else {
            if (include14Days) {
                return new Tuple(false, "");
            }
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    private Tuple checkServiceOrderForKccp006(Activity activity, String bizCode, Customer customer) {
        String requiredGoodsCode = "KCCP006";
        if (ObjectUtils.isEmptyOrNull(customer) || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }

        Map<String, Object> order = getServiceOrder(customer.getCustomerId(), bizCode);
        if (order == null) {
            return new Tuple(false, "");
        }

        Integer status = (Integer) order.get("status");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        String productCode = (String) order.get("productCode");

        // 校验订单状态
        if (!CodeConstant.CODE_YES.equals(status)) {
            return new Tuple(false, "");
        }

        // 校验订单时间
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        // 校验产品编码
        if (!requiredGoodsCode.equals(productCode)) {
            return new Tuple(false, "");
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    @Override
    public void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer) {

        // 若已经开通A股L2连续包月服务，则不再展示开通A股L2连续包月服务任务
        if (candidateTaskCodes.contains(TASK_CODE_2023DOUBLE11_21) && checkIsInAlv2(user)) {
            candidateTaskCodes.remove(TASK_CODE_2023DOUBLE11_21);
        }

        // 若已经购买了掘金周期股课, 则不再展示该任务
        if (candidateTaskCodes.contains(TASK_CODE_2023DOUBLE11_23)) {
            boolean needToBeRemoved = customer == null || checkHaveBuyKccp006(customer);
            if (needToBeRemoved) {
                candidateTaskCodes.remove(TASK_CODE_2023DOUBLE11_23);
            }
        }
    }

    private boolean checkHaveBuyKccp006(Customer customer) {
        List<Map<String, Object>> orderList = getServiceOrderList(customer.getCustomerId(), "KCCP006");
        if (CollectionUtils.isEmpty(orderList)) {
            return false;
        }

        for (Map<String, Object> order : orderList) {
            Integer status = (Integer) order.get("status");
            if (CodeConstant.CODE_YES.equals(status)) {
                return true;
            }
        }

        return false;
    }

    private boolean checkIsInAlv2(AccTokenUser user) {
        List<VasPayAgreement> vasPayAgreementList = vasPayAgreementDao.findList(new VasPayAgreement()
                .setMobile(user.getMobile())
                .setProductCode("ALv2"));

        if (CollectionUtils.isEmpty(vasPayAgreementList)) {
            return false;
        }
        return true;
    }
}
