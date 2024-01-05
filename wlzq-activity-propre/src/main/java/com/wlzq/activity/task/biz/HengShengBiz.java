package com.wlzq.activity.task.biz;

import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;

/**
 * @author renjiyun
 */
public interface HengShengBiz {
    /**
     * 校验客户是否有港股通权限
     *
     * @param user
     * @param customer
     * @return
     */
    boolean checkGgPerm(AccTokenUser user, Customer customer);

    /**
     * 校验客户是否已开通北交所权限
     *
     * @param user
     * @param customer
     * @return
     */
    boolean checkBjsPerm(AccTokenUser user, Customer customer);

    /**
     * 校验客户是否已开通科创板权限
     *
     * @param user
     * @param customer
     * @return
     */
    boolean checkKcbPerm(AccTokenUser user, Customer customer);

    /**
     * 校验客户是否已开通科创板权限, 且未开通北交所权限
     *
     * @param user
     * @param customer
     * @return
     */
    boolean checkKcbNotBjsPerm(AccTokenUser user, Customer customer);
}
