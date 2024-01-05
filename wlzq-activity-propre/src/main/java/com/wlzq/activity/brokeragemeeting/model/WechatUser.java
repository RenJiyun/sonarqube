package com.wlzq.activity.brokeragemeeting.model;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 微信用户Entity
 * @author louie
 * @version 2017-09-12
 */
public class WechatUser implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private String openid;		// OPENID
	private String unionid;		// UNIONID
	@Deprecated
	private String userId;		// 用户ID(已废弃)
	private String nickname;		// 昵称
	private String headimgurl;		// 头像
	private Integer isBind;		// is_bind
	private Date createTime;		// 创建时间
	private Integer isDeleted;		// 是否删除,0:否，1：是
	private Integer subscribe;		// subscribe
	private Integer sex;		// sex
	private Date subscribe_time;		// subscribe_time
	private String province;		// province
	private String city;		// city
	private Integer groupid;		// groupid
	private String tagid_list;		// tagid_list
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Length(min=0, max=50, message="OPENID长度必须介于 0 和 50 之间")
	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
	@Length(min=1, max=255, message="UNIONID长度必须介于 1 和 255 之间")
	public String getUnionid() {
		return unionid;
	}

	public void setUnionid(String unionid) {
		this.unionid = unionid;
	}
	
	@Length(min=1, max=50, message="用户ID长度必须介于 1 和 50 之间")
	@Deprecated
	public String getUserId() {
		return userId;
	}
	@Deprecated
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	@Length(min=1, max=255, message="昵称长度必须介于 1 和 255 之间")
	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public String getHeadimgurl() {
		return headimgurl;
	}

	public void setHeadimgurl(String headimgurl) {
		this.headimgurl = headimgurl;
	}

	@NotNull(message="is_bind不能为空")
	public Integer getIsBind() {
		return isBind;
	}

	public void setIsBind(Integer isBind) {
		this.isBind = isBind;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="创建时间不能为空")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	@NotNull(message="是否删除,0:否，1：是不能为空")
	public Integer getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Integer isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	@NotNull(message="subscribe不能为空")
	public Integer getSubscribe() {
		return subscribe;
	}

	public void setSubscribe(Integer subscribe) {
		this.subscribe = subscribe;
	}
	
	@NotNull(message="sex不能为空")
	public Integer getSex() {
		return sex;
	}

	public void setSex(Integer sex) {
		this.sex = sex;
	}
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	@NotNull(message="subscribe_time不能为空")
	public Date getSubscribe_time() {
		return subscribe_time;
	}

	public void setSubscribe_time(Date subscribe_time) {
		this.subscribe_time = subscribe_time;
	}
	
	@Length(min=1, max=50, message="province长度必须介于 1 和 50 之间")
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
	
	@Length(min=1, max=50, message="city长度必须介于 1 和 50 之间")
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}
	
	@NotNull(message="groupid不能为空")
	public Integer getGroupid() {
		return groupid;
	}

	public void setGroupid(Integer groupid) {
		this.groupid = groupid;
	}
	
	@Length(min=1, max=255, message="tagid_list长度必须介于 1 和 255 之间")
	public String getTagid_list() {
		return tagid_list;
	}

	public void setTagid_list(String tagid_list) {
		this.tagid_list = tagid_list;
	}
	
}