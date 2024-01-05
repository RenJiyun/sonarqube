package com.wlzq.activity.bill.biz;

import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.bill.dto.ActBill2023Dto;
import com.wlzq.activity.bill.model.ActReceiveReg;
import com.wlzq.activity.virtualfin.model.ActTask;
import com.wlzq.common.model.account.Customer;

import java.util.Date;

/**
 * @author renjiyun
 */
public interface ActBill2023Biz {

    /**
     * 查询指定客户号是否已经添加过企微
     *
     * @param customerId
     * @return
     */
    boolean chkAddResult(String customerId);

    /**
     * 领取登记
     *
     * @param userId
     * @param mobile
     * @param customerId
     * @return
     */
    boolean receiveReg(String userId, String mobile, String customerId);

    /**
     * 批量完成企微添加任务并领取奖品
     */
    void batchReceivePrize();

    /**
     * 领取奖品
     *
     * @param activity
     * @param actTask
     * @param actReceiveReg
     * @param now
     * @return
     */
    boolean receivePrize(Activity activity, ActTask actTask, ActReceiveReg actReceiveReg, Date now);

    /**
     * 获取年度账单
     *
     * @param customer
     * @return
     */
    ActBill2023Dto view(Customer customer);
}
