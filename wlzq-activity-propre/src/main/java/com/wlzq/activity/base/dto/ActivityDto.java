package com.wlzq.activity.base.dto;


import java.util.Date;

public class ActivityDto {
    private Integer id;
    /** 活动编码 */
    private String code;
    /** 活动组编码 */
    private String groupCode;
    /** 活动名称 */
    private String name;
    /** 活动开始时间 */
    private Date dateFrom;
    /** 活动结束时间 */
    private Date dateTo;
    /** 每天活动开始时间 */
    private String timeFrom;
    /** 每天活动结束时间 */
    private String timeTo;
    /** 活动状态: 0-不正常 1-正常 */
    private Integer status;
    /** 备注 */
    private String remark;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public java.lang.String getCode() {
        return code;
    }

    public void setCode(java.lang.String code) {
        this.code = code;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String value) {
        this.name = value;
    }

    public java.util.Date getDateFrom() {
        return this.dateFrom;
    }

    public void setDateFrom(java.util.Date value) {
        this.dateFrom = value;
    }

    public java.util.Date getDateTo() {
        return this.dateTo;
    }

    public void setDateTo(java.util.Date value) {
        this.dateTo = value;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer value) {
        this.status = value;
    }

    public String getTimeFrom() {
        return timeFrom;
    }

    public void setTimeFrom(String timeFrom) {
        this.timeFrom = timeFrom;
    }

    public String getTimeTo() {
        return timeTo;
    }

    public void setTimeTo(String timeTo) {
        this.timeTo = timeTo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}

