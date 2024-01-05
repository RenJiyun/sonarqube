package com.wlzq.activity.renewed.service;

import com.wlzq.activity.renewed.biz.RenewedReceiveBiz;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author: 乔峰
 * @Description: 连续包月续费成功领券活动
 */
@Service("activity.renewedreceivecooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION, ApiServiceTypeEnum.APP})
public class RenewedReceiveCooperation {

    @Autowired
    private RenewedReceiveBiz renewedReceiveBiz;

    /**
     * 定时任务批量发券
     */
    @Signature(false)
    public ResultDto batchreceive(RequestParams params) {
        StatusDto result = renewedReceiveBiz.batchReceive();

        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }

        return new ResultDto(ResultDto.SUCCESS);
    }
}