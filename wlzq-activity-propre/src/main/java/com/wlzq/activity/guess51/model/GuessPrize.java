/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.guess51.model;

import java.util.Date;

import lombok.Data;

/**
 * 竞猜活动奖品Entity
 * @author louie
 * @version 2018-05-25
 */
@Data
public class GuessPrize {
	public static final Integer STATUS_NOT_USED = 0;
	public static final Integer STATUS_USED = 1;
	public static final Integer HAS_POPUP_NO = 0;
	public static final Integer HAS_POPUP_YES = 1;
	
	private Long id;		// id
	private String userId;		// user_id
	private String nickName;		// 昵称
	private Long winId;		// 连胜ID
	private Long prizeId;		// 奖品ID
	private String prizeCode;		// 奖品编码
	private String redeemCode;		// 兑换码
	private String cardNo;		// 卡编码
	private String cardPassword;		// 卡密码
	private Date createTime;		// 创建时间
	private Integer type;		// 类型
	private Integer status;		// 状态，1：未发出，2：已发出, 3:已使用
	private Integer hasPopup;		// 是否已经提示，0：否，1：是
	private Integer winCount;		// 获奖时连胜次数
	
}