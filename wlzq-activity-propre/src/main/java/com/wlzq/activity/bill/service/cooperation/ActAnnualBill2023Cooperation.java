package com.wlzq.activity.bill.service.cooperation;

import com.wlzq.activity.bill.biz.ActBill2023Biz;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author renjiyun
 */
@Service("activity.annualbill2023cooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class ActAnnualBill2023Cooperation {
    @Autowired
    private ActBill2023Biz actBill2023Biz;

    @Signature(false)
    public ResultDto receiveprize(RequestParams params) {
        actBill2023Biz.batchReceivePrize();
        return new ResultDto(0, "");
    }
}
