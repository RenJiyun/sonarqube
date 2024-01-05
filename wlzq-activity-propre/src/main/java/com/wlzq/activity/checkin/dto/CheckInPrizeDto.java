package com.wlzq.activity.checkin.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 签到奖品dto
 * @author 
 * @version 1.0
 */
public class CheckInPrizeDto implements Serializable{
	private static final long serialVersionUID = 1003434767L;
	/**获奖状态，0：未获取，1：获奖*/
	private Integer status;	
	/**描述*/
	private String description;	
	/**兑换码*/
	private String redeem;	
	/**兑换码到期时间*/
	private Date ExpireDate;
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getRedeem() {
		return redeem;
	}
	public void setRedeem(String redeem) {
		this.redeem = redeem;
	}
	public Date getExpireDate() {
		return ExpireDate;
	}
	public void setExpireDate(Date expireDate) {
		ExpireDate = expireDate;
	}
	
}

