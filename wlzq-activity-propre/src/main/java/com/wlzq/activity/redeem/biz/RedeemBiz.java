package com.wlzq.activity.redeem.biz;

import com.wlzq.activity.redeem.dto.RedeemDto;
import com.wlzq.activity.redeem.dto.RedeemGoodsDto;
import com.wlzq.activity.redeem.model.Redeem;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 兑换码业务接口
 * @author louie
 *
 */
public interface RedeemBiz {

	/**
	 * 推送兑换码到微信用户
	 * @param activityCode
	 * @param openId
	 * @param funcAccount
	 * @param nickname
	 * @return
	 */
	public StatusDto sendCodeToWechat(String activityCode,String openId,String funcAccount,String nickname);
	
	/**
	 * openid查询未兑换的兑换码
	 * @param openId
	 * @return
	 */
	public StatusObjDto<RedeemDto> findRedeemByOpenId(String openId);
	
	/**
	 * code查询未兑换的兑换码
	 * @param code
	 * @return
	 */
	public StatusObjDto<Redeem> findRedeemByCode(String code);
	
	/**
	 *  兑换level2权限
	 * @param activityCode 活动编码
	 * @param code 兑换码
	 * @param mobile 手机号
	 * @param userId 
	 * @param recommendMobile 推荐人手机号
	 * @return
	 */
	public StatusObjDto<RedeemGoodsDto>  recieve(String activityCode,String code,String mobile,String userId,String recommendMobile);

	/**
	 * 查询可用的兑换码
	 * @param typeCode 类型编码
	 * @return
	 */
	public StatusObjDto<Redeem> findAvailable(String typeCode);
	

//	/**
//	 * 发出兑换码
//	 * @param code
//	 * @return
//	 */
//	public StatusDto out(String code);
}
