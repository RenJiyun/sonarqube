package com.wlzq.activity.task.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.task.biz.SpecificTaskBiz;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.activity.virtualfin.model.ActGoodsFlow;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.base.CouponInfo;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.base.CouponBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 体验类任务校验
 *
 * @author renjiyun
 */
@Service
public class ExperienceTaskBizImpl implements SpecificTaskBiz {

    private final String[] supportTaskCodes = {
            "TASK.2023DOUBLE11.26", // 领取并使用产业掘金7天体验券, 活动前使用也算
            "TASK.2023DOUBLE11.27", // 领取并使用盘中宝7天体验券, 活动前使用也算
            "TASK.2023DOUBLE11.28", // 领取并使用公告全知道7天体验券, 活动前使用也算
            "TASK.2023DOUBLE11.29", // 解锁一篇盘中宝文章
            "TASK.2023DOUBLE11.30", // 解锁一篇公告全知道文章
    };

    private final String[] COUPON_CODES = {
            "PRIZE.COUPON.CYJJ.FREE.7.RECEIVE",
            "PRIZE.COUPON.PZB.FREE.7.RECEIVE",
            "PRIZE.COUPON.GGQZD.FREE.7.RECEIVE"
    };

    @Autowired
    private CouponBiz couponBiz;

    @Autowired
    private ActPrizeDao actPrizeDao;
    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;

    @Override
    public String[] supportTaskCodes() {
        return supportTaskCodes;
    }

    @Override
    public Tuple checkTask(Activity activity, ActTask actTask, List<ActGoodsFlow> actGoodsFlowList, String bizCode,
                           String mobile, AccTokenUser user, Customer customer, String recommendMobile, boolean isBatch) {
        String taskCode = actTask.getCode();
        if (taskCode.equals("TASK.2023DOUBLE11.29") || taskCode.equals("TASK.2023DOUBLE11.30")) {
            String goodsCode = taskCode.equals("TASK.2023DOUBLE11.29") ? "PZB" : "GGQZD";
            return checkVasArticleOrder(actGoodsFlowList, activity, user, bizCode, goodsCode);
        } else if (taskCode.equals("TASK.2023DOUBLE11.26") || taskCode.equals("TASK.2023DOUBLE11.27") || taskCode.equals("TASK.2023DOUBLE11.28")) {
            // 获取对应的券模板编码
            String couponCode = COUPON_CODES[Integer.parseInt(taskCode.substring(taskCode.length() - 2)) - 26];
            return checkCouponUsed(activity, mobile, customer, couponCode);
        }

        return new Tuple(false, "");
    }

    private Tuple checkCouponUsed(Activity activity, String mobile, Customer customer, String couponCode) {
        ActPrize qryActPrize = new ActPrize()
                .setMobile(mobile)
                .setCode(couponCode);
        List<ActPrize> actPrizeList = actPrizeDao.findList(qryActPrize);

        // 只有产业掘金的7天体验券才走客户号查询的逻辑
        if (CollectionUtil.isEmpty(actPrizeList) && "PRIZE.COUPON.CYJJ.FREE.7.RECEIVE".equals(couponCode)) {
            // 若按手机号无法查到该券, 则按照客户号查询
            if (customer != null) {
                qryActPrize.setMobile(null).setCustomerId(customer.getCustomerId());
                actPrizeList = actPrizeDao.findList(qryActPrize);
                if (CollectionUtil.isEmpty(actPrizeList)) {
                    return new Tuple(false, "");
                }
            } else {
                return new Tuple(false, "");
            }
        }

        actPrizeList = actPrizeList.stream().filter(e -> !e.getStatus().equals(1)).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(actPrizeList)) {
            return new Tuple(false, "");
        }

        ActPrize prize = actPrizeList.get(0);

        StatusObjDto<CouponInfo> statusObjDto = couponBiz.couponInfo(null, prize.getRedeemCode());
        CouponInfo couponInfo = statusObjDto.getObj();
        if (couponInfo == null) {
            return new Tuple(false, "");
        }
        Integer couponStatus = couponInfo.getStatus();
        if (couponStatus.equals(3)) {
            Map<String, Object> vasOrder = getVasOrderByCouponCode(couponInfo.getCode());
            if (vasOrder == null) {
                return new Tuple(true, "");
            } else {
                String recommendMobile = (String) vasOrder.get("recommendMobile");
                String outTradeNo = (String) vasOrder.get("outTradeNo");
                return new Tuple(true, recommendMobile, "", outTradeNo);
            }
        }
        return new Tuple(false, "");
    }

    Tuple checkVasArticleOrder(List<ActGoodsFlow> actGoodsFlowList, Activity activity, AccTokenUser user,
                               String bizCode, String goodsCode) {
        if (ObjectUtils.isEmptyOrNull(user)
                || ObjectUtils.isEmptyOrNull(user.getMobile())
                || ObjectUtils.isEmptyOrNull(bizCode)) {
            return new Tuple(false, "");
        }

        // 对于以下情况, 将只是校验对应的产品是否已经订阅
        if (("PZBIsSubDoTask".equals(bizCode) && "PZB".equals(goodsCode)) ||
                ("GGQZDIsSubDoTask".equals(bizCode) && "GGQZD".equals(goodsCode))) {
            if (checkVasGoodsSubscribed(user, goodsCode)) {
                return new Tuple(true, "");
            }
        }

        // 校验该订单号是否已经用于完成任务
        boolean bizCodeUsed = CollectionUtil.isNotEmpty(actGoodsFlowList) &&
                actGoodsFlowList.stream().anyMatch((e -> bizCode.equals(e.getBizCode())));

        if (bizCodeUsed) {
            return new Tuple(false, "");
        }

        Map<String, Object> order = getVasOrder(user.getMobile(), bizCode);
        if (ObjectUtils.isEmptyOrNull(order)) {
            return new Tuple(false, "");
        }

        Integer status = (Integer) order.get("status");
        String orderGoodsCode = (String) order.get("goodsCode");
        Date createTime = new Date(Long.parseLong(String.valueOf(order.get("createTime"))));
        Integer priceType = (Integer) order.get("priceType");
        String orderMobile = (String) order.get("mobile");

        if (!user.getMobile().equals(orderMobile)) {
            return new Tuple(false, "");
        }

        if (status != 1 || !goodsCode.equals(orderGoodsCode) || priceType != 4) {
            return new Tuple(false, "");
        }

        // 校验是否在活动期间内使用
        if (createTime.after(activity.getDateTo()) || createTime.before(activity.getDateFrom())) {
            return new Tuple(false, "");
        }

        String recommendMobile = (String) order.get("recommendMobile");
        return new Tuple(true, recommendMobile);
    }

    @Override
    public void excludeTaskCodes(Set<String> candidateTaskCodes, AccTokenUser user, Customer customer) {
        if (user == null) {
            throw ActivityBizException.ACTIVITY_NOT_LOGIN;
        }

        if (CollectionUtil.isEmpty(candidateTaskCodes)) {
            return;
        }

        // 若已订阅盘中宝, 则不显示盘中宝文章解锁任务
        if (candidateTaskCodes.contains("TASK.2023DOUBLE11.29") && checkVasGoodsSubscribed(user, "PZB")) {
            candidateTaskCodes.remove("TASK.2023DOUBLE11.29");
        }

        // 若已订阅公告全知道, 则不显示公告全知道文章解锁任务
        if (candidateTaskCodes.contains("TASK.2023DOUBLE11.30") && checkVasGoodsSubscribed(user, "GGQZD")) {
            candidateTaskCodes.remove("TASK.2023DOUBLE11.30");
        }
    }

    private boolean checkVasGoodsSubscribed(AccTokenUser user, String goodsCode) {
        Map<String, Object> params = RemoteUtils.newParamsBuilder()
                .put("userId", user.getUserId())
                .put("mobile", user.getMobile())
                .put("code", goodsCode)
                .build();

        ResultDto resultDto = RemoteUtils.call("vas.decisioncooperation.goodsdetail",
                ApiServiceTypeEnum.COOPERATION, params, true);

        // 调用报错的情况下, 将默认认为该用户已经订阅过, 因此将不会显示该任务
        if (resultDto == null || ResultDto.FAIL_COMMON.equals(resultDto.getCode())) {
            return true;
        }

        if (resultDto.getData() == null) {
            return false;
        }
        Map<String, Object> goodsDetailMap = resultDto.getData();
        String isSub = goodsDetailMap.get("isSub").toString();
        if ("1".equals(isSub)) {
            return true;
        }
        return false;
    }
}
