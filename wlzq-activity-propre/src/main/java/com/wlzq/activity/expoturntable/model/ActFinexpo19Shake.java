package com.wlzq.activity.expoturntable.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * 金交会2019摇一摇Entity
 * @author cjz
 * @version 2019-06-13
 */
public class ActFinexpo19Shake {
	
	@JsonIgnore
	private Integer id;
	@JsonIgnore
	private Integer scene;		// 场次
	@JsonIgnore
	private String userId;		// user_Id
	private Integer totalCount;		// 摇次数
	@JsonIgnore
	private Integer status;		// 状态，1有效，0无效
	private Integer lastCount;		// 最后一次摇次数
	private String counts;		// 次数列表
	@JsonIgnore
	private Integer signinId;		// 签到id
	
	public static final Integer STATUS_VALID = 1;
	public static final Integer STATUS_INVALID = 0;
	
	@JsonIgnore
	private String nickName;
	@JsonIgnore
	private String portrait;
	
	private Integer playerId;
	@JsonIgnore
	private List<Integer> playerIds;
	@JsonIgnore
	private Integer sizeCount;
	
	public ActFinexpo19Shake() {
		super();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getScene() {
		return scene;
	}

	public void setScene(Integer scene) {
		this.scene = scene;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}
	
	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public Integer getLastCount() {
		return lastCount;
	}

	public void setLastCount(Integer lastCount) {
		this.lastCount = lastCount;
	}
	
	public String getCounts() {
		return counts;
	}

	public void setCounts(String counts) {
		this.counts = counts;
	}
	
	public Integer getSigninId() {
		return signinId;
	}

	public void setSigninId(Integer signinId) {
		this.signinId = signinId;
	}

	public Integer getSizeCount() {
		return sizeCount;
	}

	public void setSizeCount(Integer sizeCount) {
		this.sizeCount = sizeCount;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public Integer getPlayerId() {
		return playerId;
	}

	public void setPlayerId(Integer playerId) {
		this.playerId = playerId;
	}

	public List<Integer> getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(List<Integer> playerIds) {
		this.playerIds = playerIds;
	}

}