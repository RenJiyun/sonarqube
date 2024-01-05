package com.wlzq.activity.base.dao;

import com.wlzq.activity.base.model.ActAppointment;
import com.wlzq.common.persist.CrudDao;
import com.wlzq.core.annotation.MybatisScan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author renjiyun
 */
@MybatisScan
public interface ActAppointmentDao extends CrudDao<ActAppointment> {
    /**
     * 根据活动编码获取所有预约用户的手机号
     *
     * @param activityCode
     * @return
     */
    List<String> getAllUserMobileNeedSendReminder(@Param("activityCode") String activityCode);
}
