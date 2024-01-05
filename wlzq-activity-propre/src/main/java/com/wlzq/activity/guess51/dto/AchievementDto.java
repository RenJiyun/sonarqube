package com.wlzq.activity.guess51.dto;

import lombok.Data;

/**
 * 战绩dto
 * @author 
 * @version 1.0
 */
@Data
public class AchievementDto {	
	/**昵称*/
	private String nickName;	
	/**手机*/
	private String mobile;	
	/**积分*/
	private Long point;	
	/**头像*/
	private String portrait;
	/**连胜次数*/
	private Long total;
	/**最高连胜次数*/
	private Long winCount;
	/**排名*/
	private Long order;
	/**打败用户比例*/
	private String beatRatio;
	
}

