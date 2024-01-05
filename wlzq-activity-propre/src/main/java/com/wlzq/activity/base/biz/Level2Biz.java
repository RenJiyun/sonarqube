package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.dto.Level2OpenDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * Level2业务接口
 * @author louie
 *
 */
public interface Level2Biz {

	/**
	 * level2 开通
	 * @param goodsId 商品ID
	 * @param mobile 手机号
	 * @param payMode 支付方式 6：对换码，7：新客户免费体验
	 * @param reason reason
	 * @param recommendMobile 推荐人手机
	 * @return
	 */
	public StatusObjDto<Level2OpenDto> openLevel2(Integer goodsId,String mobile,Integer payMode,  String reason, String recommendMobile);
	
}
