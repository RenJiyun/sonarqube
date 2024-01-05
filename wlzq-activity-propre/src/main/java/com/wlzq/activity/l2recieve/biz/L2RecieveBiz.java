package com.wlzq.activity.l2recieve.biz;

import java.util.List;

import com.wlzq.activity.l2recieve.dto.OpenDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * L2领取业务接口
 * @author louie
 *
 */
public interface L2RecieveBiz {
	/**
	 * 领取情况
	 * @param mobile
	 * @return
	 */
	public StatusObjDto<List<OpenDto>> recieveStatus(String mobile);
	/**
	 * 领取Level2
	 * @param type 类型：1：新开户，2：新增有效户，3：新开信用账户
	 * @param userId 用户ID
	 * @param mobile 开通手机号
	 * @param ip 领取ip
	 * @return
	 */
	public StatusObjDto<OpenDto> recieve(Integer type,String userId,String mobile,String ip);
	/**
	 * 发送短信验证码
	 * @param mobile
	 * @return
	 */
	public StatusDto sendCheckCode(String mobile);
	/**
	 * 接受邀请
	 * @param mobile
	 * @param checkCode
	 * @param shareCode
	 * @return
	 */
	public StatusDto acceptInvitation(String mobile,String checkCode,String shareCode);
	
	/**
	 * 批量开通邀请获取的Level2
	 */
	public void openInvite();
	
	/**
	 * 批量开通未开户获取的Level2
	 */
	public void openNotActive();

	/**
	 * 是否已领取
	 * @param mobile
	 * @return
	 */
	public StatusObjDto<Integer> hasRecieve(String userId,String mobile);
	
	/**
	 * 缓存领取状态
	 * @return
	 */
	public StatusDto cacheRecieveStatus() ;
}
