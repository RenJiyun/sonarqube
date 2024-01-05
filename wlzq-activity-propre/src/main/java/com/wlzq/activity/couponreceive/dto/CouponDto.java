/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.couponreceive.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 优惠券dto
 * @author louie
 * @version 2018-11-23
 */
@Data
public class CouponDto  implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 178324783278L;
	private String id;		// id
	private Integer status;		// status
	private String name;		// 名称
	private String description;		// 描述
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateFrom;		// 有效期开始时间
	@JsonSerialize(using = Date2LongSerializer.class)
	private Date validityDateTo;		// 有效期结束时间
	private String productCode;		// 产品代码
}