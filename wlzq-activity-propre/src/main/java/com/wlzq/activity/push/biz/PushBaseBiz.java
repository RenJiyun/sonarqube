package com.wlzq.activity.push.biz;

import com.wlzq.activity.push.dto.NoticeDto;
import com.wlzq.activity.push.dto.RenewedReceivedNoticeDto;
import com.wlzq.activity.push.dto.StaffPushDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

public interface PushBaseBiz {

	public StatusDto genPushData(String exeTask);

    /**
     * 查找续费领券成功的提醒数据
     * @param activityCode
     */
    StatusObjDto<List<RenewedReceivedNoticeDto>> renewedReceivedNotice(String activityCode);

    /**
     * ETF优惠券领取通知
     */
    StatusObjDto<List<NoticeDto>> receivedNotice(String activityCode);

    /**
     * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
     */
    StatusObjDto<List<StaffPushDto>> pushInvestAdviserOrderToStaff(String sceneNo);

    /**
     * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
     */
    StatusObjDto<List<StaffPushDto>> pushInvestAdviserOrderToStaff2(String startTime, String endTime);

    /**
     * 推送2022双11活动销售榜单微信消息，给非总部的所有员工
     */
    StatusObjDto push2022DoubleActivitySaleListToStaff(String sceneNo);
    /**
     * 推送2022双11活动销售榜单微信消息，给非总部的所有员工
     */
    StatusObjDto<List<StaffPushDto>> push2022DoubleActivitySaleListToStaff2();
}
