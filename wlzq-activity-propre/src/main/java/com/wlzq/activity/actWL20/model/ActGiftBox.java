package com.wlzq.activity.actWL20.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.AccessType;

/**
 * 
 * @author jjw
 *
 */
@Data
@Accessors(chain = true)
public class ActGiftBox {	
	public static Integer USER_TYPE_1 = 1;	//第一类用户
	public static Integer USER_TYPE_2 = 2;	//第二类用户
	public static Integer USER_TYPE_3 = 3;	//第三类用户
	public static Integer USER_TYPE_4 = 4;	//第四类用户
	
	@JsonIgnore
	private Integer id;
	private String mobile;		//手机号
	private Integer userType;	//用户类别 1：第一类、2：第二类、3：其他
	private String activityCode;	//活动编码
}
