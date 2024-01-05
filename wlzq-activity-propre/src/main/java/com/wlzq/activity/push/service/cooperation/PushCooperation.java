package com.wlzq.activity.push.service.cooperation;

import com.google.common.collect.Maps;
import com.wlzq.activity.push.biz.PushBaseBiz;
import com.wlzq.activity.push.dto.NoticeDto;
import com.wlzq.activity.push.dto.RenewedReceivedNoticeDto;
import com.wlzq.activity.push.dto.StaffPushDto;
import com.wlzq.activity.task.biz.impl.FreeCourseBizImpl;
import com.wlzq.activity.task.dto.PushTimeDto;
import com.wlzq.activity.task.dto.TaskPushDto;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 产品协作服务类
 *
 * @author
 * @version 1.0
 */
@Service("activity.pushcooperation")
@ApiServiceType({ ApiServiceTypeEnum.COOPERATION })
public class PushCooperation extends BaseService {

	@Autowired
	private PushBaseBiz pushBiz;
	@Autowired
	private FreeCourseBizImpl freeCourseBiz;

	@Signature(true)
	public ResultDto genpushdata(RequestParams params) {
		String exeTask = params.getString("exeTask");
		StatusDto result = pushBiz.genPushData(exeTask);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, "");
	}

	/**
	 * 查找续费领券成功的提醒数据
	 */
	@Signature(true)
	public ResultDto renewedreceivednotice(RequestParams params) {
		/*活动编码*/
		String activityCode = params.getString("activityCode");

		StatusObjDto<List<RenewedReceivedNoticeDto>> result = pushBiz.renewedReceivedNotice(activityCode);

		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());

		return new ResultDto(0, data, "");
	}

	/**
	 * 发券成功提醒
	 */
	@Signature(true)
	public ResultDto receivednotice(RequestParams params) {
		/*活动编码*/
		String activityCode = params.getString("activityCode");

		StatusObjDto<List<NoticeDto>> result = pushBiz.receivedNotice(activityCode);

		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());

		return new ResultDto(0, data, "");
	}

	/**
	 * 推送2022双11活动销售榜单微信消息，给非总部的所有员工
	 * @param params
	 * @return
	 */
	@Signature(false)
	public ResultDto push2022doublesalelist(RequestParams params) {
		String sceneNo = params.getString("sceneNo");
		StatusObjDto result = pushBiz.push2022DoubleActivitySaleListToStaff(sceneNo);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, "");
	}

	/**
	 * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
	 */
	@Signature(false)
	public ResultDto pushinvestadviserOrdertostaff(RequestParams params) {
		String sceneNo = params.getString("sceneNo");
		StatusObjDto result = pushBiz.pushInvestAdviserOrderToStaff(sceneNo);
		if (!result.isOk()) {
			return new ResultDto(result.getCode(), result.getMsg());
		}
		return new ResultDto(0, "");
	}




	/**
	 * 推送2022双11活动销售榜单微信消息，给非总部的所有员工
	 */
	@Signature(false)
	public ResultDto push2022doublesalelist2(RequestParams params) {
		StatusObjDto<List<StaffPushDto>> result = pushBiz.push2022DoubleActivitySaleListToStaff2();
		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());
		return new ResultDto(0, data, "");
	}
	/**
	 * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
	 */
	@Signature(false)
	public ResultDto pushinvestadviserOrdertostaff2(RequestParams params) {
		String startTime = params.getString("startTime");
		String endTime = params.getString("endTime");
		StatusObjDto<List<StaffPushDto>> result = pushBiz.pushInvestAdviserOrderToStaff2(startTime,endTime);
		Map<String, Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());
		return new ResultDto(0, data, "");
	}


	@Signature(false)
	public ResultDto waitingreceiveeedpack(RequestParams params) {
		Long beginTime = params.getLong("startTimeStamp");
		Long endTime = params.getLong("endTimeStamp");

		PushTimeDto timeDto = new PushTimeDto();
		timeDto.setStartTime(new Date(beginTime)).setEndTime(new Date(endTime));

		StatusObjDto<List<TaskPushDto>> result = freeCourseBiz.getWaitingReceiveRedPack(timeDto);
		if(!result.isOk()) {
			return new ResultDto(ResultDto.FAIL_COMMON,result.getMsg());
		}
		Map<String,Object> data = Maps.newHashMap();
		data.put("list", result.getObj());
		data.put("total", result.getObj().size());
		return  new ResultDto(0,data,"");
	}

}
