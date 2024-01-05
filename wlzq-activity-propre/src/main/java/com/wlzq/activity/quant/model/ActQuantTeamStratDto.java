package com.wlzq.activity.quant.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

@Data
public class ActQuantTeamStratDto {

	public final static Integer ORDER_BY_SCORE = 1;
	public final static Integer ORDER_BY_VOTE = 2;
	
	private String id;
	private String teamId;		// 队伍id
	private String leader;		// 队长名
	@JsonIgnore
	private String leaderMobile;		// 队长手机号
	@JsonIgnore
	private String leaderEmail;		// 队长邮箱
	private String teammates;		// 队员
	private List<String> teammateList;
	private String professor;		// 教授名
	@JsonIgnore
	private String professorMobile;		// 教授手机号
	@JsonIgnore
	private String professorEmail;		// 教授邮箱
	private String university;		// 大学名
	private String department;		// 学院
	private String major;		// 专业
	private String education;		// 学历
	@JsonIgnore
	private String thsAccount;		// 同花顺账户
	@JsonIgnore
	private String dkAccount;		// 点宽账号
	private String strategyName;		// 策略名
	private String evaluation;			// 评估字符串
	private String score;				// 分数
	private String rate;
	private String withdraw;
	private String sharp;
	private Date runTime;			// 策略更新时间
	private String dataUpdTime;			// 更新时间
	private Integer totalVoteCount;			// 总票数
	private Integer dayVoteCount;			// 当天票数
	private Integer order;					// 排序
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date statTime;			// 统计时间
	
	@JsonIgnore
	private Date createTime;		// create_time
	@JsonIgnore
	private Date updateTime;		// update_time
	@JsonIgnore
	private Integer isDeleted;		// is_deleted
	
	private String backTraceDateStart;			// 回测区间左值
//	@JsonSerialize(using=Date2LongSerializer.class)
	private String backTraceDateEnd;			// 回测区间右值
	
}
