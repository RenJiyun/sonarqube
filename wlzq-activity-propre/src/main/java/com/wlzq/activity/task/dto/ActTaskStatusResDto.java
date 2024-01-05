package com.wlzq.activity.task.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author luohc
 * @date 2021/7/14 13:32
 */
@Data
@Accessors(chain = true)
public class ActTaskStatusResDto implements Serializable {

    public static final Integer STATUS_NOT_FINISH_TASK = 0;
    public static final Integer STATUS_WAITING_RECEIVE = 1 ;
    public static final Integer STATUS_RECEIVED = 2 ;
    public static final Integer FINISH_COURSE_COUNTER = 9 ;


    /**
     * 购买课程状态，0：未购买，1：已购买
     */
    private Integer status;

    /**
     * 学习课程数量
     */
    private Integer counter;

    /**
     * 0未完成任务 1：已完成任务待领取，2：已领取
     */
    private Integer redPackStatus;

    /**
     * 已获取的券码
     */
    private List<String> receiveCouponCodes;

    /**
     * 购买时间
     */
    @JsonSerialize(using= Date2LongSerializer.class)
    private Date time;


}
