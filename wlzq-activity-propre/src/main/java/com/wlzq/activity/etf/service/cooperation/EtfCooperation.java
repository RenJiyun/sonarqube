package com.wlzq.activity.etf.service.cooperation;

import com.wlzq.activity.etf.biz.EtfBiz;
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
 * @Description: ETF课程及投顾产品联合推广活动
 */
@Service("activity.etfcooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION, ApiServiceTypeEnum.APP})
public class EtfCooperation {

    @Autowired
    private EtfBiz etfBiz;

    /**
     * 定时任务批量发券
     */
    @Signature(false)
    public ResultDto batchreceive(RequestParams params) {
        String activityCode = params.getString("activityCode");
        String prizeType = params.getString("prizeType");

        StatusDto result = etfBiz.batchReceive(activityCode, prizeType);

        return new ResultDto(ResultDto.SUCCESS);
    }
}
