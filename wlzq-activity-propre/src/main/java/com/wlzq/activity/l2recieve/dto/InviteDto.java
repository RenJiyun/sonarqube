package com.wlzq.activity.l2recieve.dto;

/**
 * InviteDto
 * @author 
 * @version 1.0
 */
public class InviteDto {
	/**状态，0：成功获取level2，其它：失败*/
	private java.lang.Integer status;	
	/**提示语*/
	private java.lang.String message;
	
	public java.lang.Integer getStatus() {
		return status;
	}
	public void setStatus(java.lang.Integer status) {
		this.status = status;
	}
	public java.lang.String getMessage() {
		return message;
	}
	public void setMessage(java.lang.String message) {
		this.message = message;
	}	

}

