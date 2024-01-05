package com.wlzq.activity.actWL20.biz.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.actWL20.biz.FundinGoBiz;
import com.wlzq.activity.actWL20.dao.FundinGoDao;
import com.wlzq.activity.actWL20.model.ActFundinGo;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;

@Service
public class FundinGoBizImp implements FundinGoBiz {

	private Logger logger = LoggerFactory.getLogger(FundinGoBizImp.class);
	
	private static String ACTIVITY_CODE = "ACTIVITY.818.2021818";
	
	@Autowired
	private	ActPrizeBiz actPrizeBiz;
	
	@Autowired
	private FundinGoDao fundinGoDao;
	
	@Autowired
	private ActivityDao activityDao;

	@Override
	public ResultDto updateFundinGo() {
		Integer fundStatus = ActFundinGo.CUS_NOT_VALID;
		
		//查找不满足条件的入金记录
		List<ActFundinGo> fundinGoList = fundinGoDao.findNotAvailableFundinGo();
		
		if(fundinGoList != null && fundinGoList.size()>0) {
			for(ActFundinGo fundinGo : fundinGoList) {
				ResultDto resultDto = historyRJ(fundinGo.getCustomerId());
				if (!ResultDto.SUCCESS.equals(resultDto.getCode()) || resultDto.getData() == null || resultDto.getData().isEmpty() || CollectionUtils.isEmpty((List<Map<String, Object>>)resultDto.getData().get("O_RESULT"))) {
					resultDto = todayRJ(fundinGo.getCustomerId()); // 查不到历史入金，查当天
					if (!ResultDto.SUCCESS.equals(resultDto.getCode()) || resultDto.getData() == null || resultDto.getData().isEmpty() || CollectionUtils.isEmpty((List<Map<String, Object>>)resultDto.getData().get("O_RESULT"))) {
						continue;
						//throw ActivityBizException.ACT_CUS_NOT_FOUND;
					}
				}
				List<Map<String, Object>> result = (List<Map<String, Object>>) resultDto.getData().get("O_RESULT");
				
				//判断是否满足活动条件
				ActFundinGo actFundinGo = new ActFundinGo();
				for(Map<String, Object> map : result) {
					Date openDate = fundinGo.getOpenDate();
					Date validDate =  DateUtils.parseDate(AppConfigUtils.get("ACT818_OPEN_DATE", "2019-01-01") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
					String firstFundinDt = (String) map.get("FIRST_FUNDIN_DT") + " 00:00:00";
					Date firstDate =  DateUtils.parseDate(firstFundinDt, "yyyyMMdd HH:mm:ss");
					BigDecimal amount = new BigDecimal(map.get("TOTAL_FUNDIN_AMT").toString());
					String lastFundinDt = (String) map.get("LAST_FUNDIN_DT") + " 00:00:00";
					Date lastDate =  DateUtils.parseDate(lastFundinDt, "yyyyMMdd HH:mm:ss"); 
					
					actFundinGo.setId(fundinGo.getId());
					actFundinGo.setCustomerId(fundinGo.getCustomerId());
					actFundinGo.setFirstFundingDate(firstDate);
					actFundinGo.setAmount(amount);
					actFundinGo.setLastFundingDate(lastDate);
					actFundinGo.setUpdateTime(new Date());
					
					
					//判断是否2019年1月1日之后开户
				    if(openDate.getTime() < validDate.getTime()) {
				    	fundStatus = ActFundinGo.CUS_NOT_VALID;
						break;
				    }
					
					//首次入金日期<20210809则不符合条件
					Date checkDate =  DateUtils.parseDate(AppConfigUtils.get("ACT818_FIRST_FUNDIN_TIME", "2021-08-09") + " 00:00:00", "yyyy-MM-dd HH:mm:ss");
					if(actFundinGo.getFirstFundingDate().getTime() < checkDate.getTime()) {
						fundStatus = ActFundinGo.CUS_NOT_VALID;
						break;
					}
					
					//累计入金金额<1000则不符合条件
					if(actFundinGo.getAmount().doubleValue() - 1000 < 0) {
						fundStatus = ActFundinGo.CUS_NOT_VALID;
						break;
					}
					
					//最近入金日期必须再活动日期内
					Activity act = findActivity(ACTIVITY_CODE);
					if(actFundinGo.getLastFundingDate().getTime() < act.getDateFrom().getTime() || actFundinGo.getLastFundingDate().getTime() > act.getDateTo().getTime()) {
						fundStatus = ActFundinGo.CUS_NOT_VALID;
						break;
					}
					
					//通过条件判断则更改状态为满足条件状态
					fundStatus = ActFundinGo.CUS_VALID;
				}
				
				//判断是否已经领取奖品
				List<ActPrize> prizes = actPrizeBiz.findPrize(ACTIVITY_CODE, actFundinGo.getCustomerId(), null, null, null, null, null);
				if(!CollectionUtils.isEmpty(prizes)) {
					fundStatus = ActFundinGo.CUS_RECIEVE;
				}
				
				actFundinGo.setStatus(fundStatus);
				fundinGoDao.update(actFundinGo);
			}
			
		}
		
		return new ResultDto(0, "");
	}
	
	/**
	 * 查询客户历史入金信息
	 * 
	 * @param customerId
	 *            客户号
	 * @return
	 */
	@Override
	public ResultDto historyRJ(String customerId) {
		Map<String, Object> busparams = Maps.newHashMap();
		String serviceId = new String("ext.sjzx.khscrj");
		Map<String, Object> params = Maps.newHashMap();
		params.put("I_KHH", customerId);
		busparams.put("serviceId", serviceId);
		busparams.put("isNeedLogin", 1);
		busparams.put("params", JsonUtils.map2Json(params));
		ResultDto resultDto = RemoteUtils.call("base.fsdpcoopration.callservice", ApiServiceTypeEnum.COOPERATION, busparams, false);
		logger.info("818活动客户历史入金信息return:" + resultDto.getData());
		return resultDto;
	}

	/**
	 * 查询客户当日入金信息
	 * 
	 * @param customerId
	 *            客户号
	 * @return
	 */
	@Override
	public ResultDto todayRJ(String customerId) {
		Map<String, Object> busparams = Maps.newHashMap();
		String serviceId = new String("ext.crm.dspt.khrjcx");
		Map<String, Object> params = Maps.newHashMap();
		params.put("I_KHH", customerId);
		params.put("I_INIT_DATE", DateUtils.formate(new Date(), "yyyyMMdd"));
		busparams.put("serviceId", serviceId);
		busparams.put("isNeedLogin", 1);
		busparams.put("params", JsonUtils.map2Json(params));
		ResultDto resultDto = RemoteUtils.call("base.fsdpcoopration.callservice", ApiServiceTypeEnum.COOPERATION, busparams, false);
		logger.info("818活动客户当日入金信息return:" + resultDto.getData());
		return resultDto;
	}
	
	public Activity findActivity(String activityCode) {
		if(ObjectUtils.isEmptyOrNull(activityCode)) {
			return null; 
		}
		
		Activity act = (Activity) BaseRedis.ACT_ACTIVITY_INFO.get(activityCode);
		if(act != null) {
			return act;
		}
		
		return activityDao.findActivityByCode(activityCode);
	}

}
