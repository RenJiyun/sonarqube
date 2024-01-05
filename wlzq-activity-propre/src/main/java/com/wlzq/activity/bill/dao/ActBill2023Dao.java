package com.wlzq.activity.bill.dao;

import com.wlzq.activity.bill.model.ActBill2023;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActBill2023Dao extends CrudDao<ActBill2023> {
    /**
     * 根据客户号查询账单
     *
     * @param customerId
     * @return
     */
    ActBill2023 findBillByCustomerId(@Param("customerId") String customerId);
}
