package com.wlzq.activity.push.executor;

import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;

public abstract class BaseExecutorTask implements BaseExecutor {
	
	/**
	 * 创建一个推送批次
	 * @return
	 */
	protected String newBatch() {
		String a = "" + (Math.random() * 9000);
		a = a.substring(1, 4);
		a  = a.replace(".", "0");
		return DateUtils.formate(new Date(System.currentTimeMillis()), "yyyyMMddHHmmssSSS") + a;
	}
	
	protected Map<String, Object> gettask(String msgThemeCode, String taskName, String taskTime, String batch, Integer isTemp, String taskKey, Integer num) {
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("msgThemeCode", msgThemeCode);
		params.put("taskName", taskName);
		params.put("taskTime", taskTime);
		params.put("batch", batch);
		params.put("isTemp", isTemp);
		params.put("taskKey", taskKey);
		params.put("planPushNum", num);
		ResultDto result = RemoteUtils.call("push.msgthemepushcooperation.gettask", ApiServiceTypeEnum.COOPERATION, params, true);
		if (result != null && result.getCode() != null && result.getCode().intValue() == 0) {
			if (result.getData() != null) {
				Map<String, Object> m = result.getData();
				@SuppressWarnings("unchecked")
				Map<String, Object> taskInfos = (Map<String, Object>) m.get("taskInfos");
				return taskInfos;
			}
		}
		return null;		
	}
	
	protected ResultDto genSingle(String msgThemeCode, Integer taskId, String batch, int size, int order, Map<String, Object> busParams) {
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("msgThemeCode", msgThemeCode);
		params.put("taskId", taskId);
		params.put("batch", batch);
		params.put("number", size);
		params.put("order", order);
		params.put("busParams", JsonUtils.object2JSON(busParams));
		ResultDto result = RemoteUtils.call("push.msgthemepushcooperation.gensinglemsg", ApiServiceTypeEnum.COOPERATION, params, true);
		return result;		
	}
	
	/**
	 * 发送单个推送消息
	 * @param msgThemeCode
	 * @param batch
	 * @param size
	 * @param order
	 * @param busParams
	 * @return
	 */
	protected ResultDto sendSingleMsg(String msgThemeCode, String batch, int size, int order, Map<String, Object> busParams) {
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("msgThemeCode", msgThemeCode);
		params.put("batch", batch);
		params.put("number", size);
		params.put("order", order);
		params.put("busParams", JsonUtils.object2JSON(busParams));
		ResultDto result = RemoteUtils.call("push.msgthemepushcooperation.sendsinglemsg", ApiServiceTypeEnum.COOPERATION, params, true);
		return result;
	}
	
	/**
	 * 推送单个消息
	 * @param msgThemeCode 消息主题编码
	 * @param batch 批次
	 * @param size 总数量
	 * @param order 总数量中的序号
	 * @param pushTargetType 对象类别,1:客户号,2:手机号,3:资金账号,4:openid
	 * @param targetCode 根据推送类别变化，1则填入客户号，2则填手机号，3则填资金账号，4，则填openid
	 * @param uniKey 每个推送的唯一码，重复的唯一码将不作推送，可自行根据业务拟定。假如每个客户号只能收到一次，则填入客户号，假如每个客户每天只能收到一次，则填入日期+客户号，为空则无限制
	 * @param busParams 其他业务参数
	 * @param linkUrl 微信模板消息中的链接，可为空
	 * @return
	 */
	protected ResultDto sendSingleMsg(String msgThemeCode, String batch, int size, int order, 
			PushTargetType pushTargetType, String targetCode, String uniKey,
			Map<String, Object> busParams, String linkUrl) {
		if (busParams == null) {
			busParams = Maps.newHashMap();
		}
		busParams.put("targetType", pushTargetType.getTargetType());
		busParams.put("targetCode", targetCode);
		busParams.put("sendToKey", uniKey);
		busParams.put("linkUrl", linkUrl);
		
		Map<String, Object> params = Maps.newHashMap();
		params.put("msgThemeCode", msgThemeCode);
		params.put("batch", batch);
		params.put("number", size);
		params.put("order", order);
		params.put("busParams", JsonUtils.object2JSON(busParams));
		ResultDto result = RemoteUtils.call("push.msgthemepushcooperation.gensinglemsg", ApiServiceTypeEnum.COOPERATION, params, true);
//		ResultDto result = RemoteUtils.call("push.msgthemepushcooperation.sendsinglemsg", ApiServiceTypeEnum.COOPERATION, params, true);
		return result;
	}
	
}
