package com.wlzq.activity.redenvelope.biz;

import com.wlzq.activity.redenvelope.dto.RedEnvelopeDto;
import com.wlzq.activity.redenvelope.model.RedEnvelope;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

/**
 * 微信红包业务接口
 * @author louie
 *
 */
public interface RedEnvelopeBiz {

	/**
	 * 创建红包
	 * @param redEnvelope
	 * @return
	 */
	public StatusObjDto<RedEnvelopeDto> create(RedEnvelope redEnvelope);
	
	/**
	 * 创建红包
	 * @param redEnvelope
	 * @return
	 */
	public StatusObjDto<RedEnvelopeDto> create(RedEnvelope redEnvelope,boolean isNeedCheck);
	
	/**
	 * 红包回调处理
	 * @param result 红包回调加密信息
	 * @return
	 */
	public StatusDto notify(String result);
}
