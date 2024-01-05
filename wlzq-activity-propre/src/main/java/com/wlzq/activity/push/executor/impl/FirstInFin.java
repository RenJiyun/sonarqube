package com.wlzq.activity.push.executor.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.wlzq.activity.l2recieve.dao.Level2RecieveUserDao;
import com.wlzq.activity.l2recieve.model.Level2RecieveUser;
import com.wlzq.activity.push.dao.PushBusinessRecordDao;
import com.wlzq.activity.push.executor.BaseExecutorTask;
import com.wlzq.activity.push.executor.PushTargetType;
import com.wlzq.activity.push.model.PushBusinessRecord;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.core.dto.StatusDto;

/**
 * 用户完成首次入金任务
 * 
 * @author cjz
 *
 */
@Slf4j
@Service("FirstInFin")
public class FirstInFin extends BaseExecutorTask {
	
	@Autowired
	private Level2RecieveUserDao level2RecieveUserDao;
	@Autowired
	private PushBusinessRecordDao pushBusinessRecordDao;
	
	private static final String MSGTHEMECODE = "FirstInFin";
	
	private static final String LOG_TAG = "用户完成首次入金任务";
	
	private static final String DATE_FORMATE = "yyyy-MM-dd";
	
	private static final Integer USER_TYPE = 2;
	private static final String SMS_LINK = "m.wlzq.cn/c?d4";
	private static final String SMS_CONTENT = "恭喜您获得炒股利器Level-2增强行情1个月免费体验资格，详情点 m.wlzq.cn/c?d4 拒收回T";
	private static final String SEC_TYPE = "T-1";

	@Override
	public StatusDto run() {
		Date today = new Date();
		Date yesterday = DateUtils.addDay(today, -1);
		String yesterdayStr = DateUtils.formate(yesterday, DATE_FORMATE);
		yesterday = DateUtils.parseDate(yesterdayStr, DATE_FORMATE);
				
		List<Level2RecieveUser> users = this.getLevel2RecieveUser(yesterday);
		log.info(new StringBuilder().append(LOG_TAG).append("<|>").append("查询符合条件订单").append("<|>").append("查询日期:").append(yesterdayStr).append("<|>")
				.append("<|>").append("符合条件客户数:").append(users.size()).toString());
		
		this.handleUsers(users);
		
		return new StatusDto(true);
	}
	
	private List<Level2RecieveUser> getLevel2RecieveUser(Date date) {
		Level2RecieveUser qry = new Level2RecieveUser();
		qry.setEffectiveDateBegin(date);
		qry.setEffectiveDateEnd(date);
		qry.setType(USER_TYPE);
		List<Level2RecieveUser> list = level2RecieveUserDao.findList(qry);
		return list;
	}
	
	private void handleUsers(List<Level2RecieveUser> users) {
		if (users != null && users.size() > 0) {
			Date date = new Date();
			String batch = super.newBatch();
			String taskName = LOG_TAG +  DateUtils.formate(date, "yyyyMMdd");
			String exeDate = DateUtils.formate(date, DATE_FORMATE);
			String taskTime = exeDate + " 00:00:00";
			String taskKey = "T|" + exeDate;
			Integer taskId = null;
			
			
			int size = users.size();
			Map<String, Object> taskInfos = super.gettask(MSGTHEMECODE, taskName, taskTime, batch, 1, taskKey, size);
			boolean isFail = false;
			if (taskInfos != null) {
				if (taskInfos.get("isOk") != null) {
					if (taskInfos.get("isOk").equals(1)) {
//						batch = (String) taskInfos.get("batch");
						taskId = (Integer) taskInfos.get("taskId");
//						if (!(batch != null && batch.trim().length() > 0)) {
//							isFail = true;
//						}
						if (!(taskId != null && taskId.intValue() > 0)) {
							isFail = true;
						}
					} else if (taskInfos.get("isOk").equals(2)) {
						log.info(new StringBuilder().append(LOG_TAG).append("<|>").append("生成获取推送任务").append("<|>")
								.append("batch:").append(batch).append("<|>")
								.append("taskName:").append(taskName).append("<|>")
								.append("生成任务状态:当日无需生成推送任务").toString());
						return;
					}
				} else {
					isFail = true;
				}
			} else {
				isFail = true;
			}
			if (isFail) {
				log.info(new StringBuilder().append(LOG_TAG).append("<|>").append("生成获取推送任务").append("<|>")
						.append("batch:").append(batch).append("<|>")
						.append("taskName:").append(taskName).append("<|>")
						.append("生成任务状态:失败").toString());
				return;
			}
			
			int orderSort = 0;
			
			int count = 0;
			
			
			String link = SMS_LINK.trim();
			String content = SMS_CONTENT.trim();
			for (Level2RecieveUser user : users) {
				orderSort++;
				String customerId = user.getCustmerId();
				String mobile = user.getMobile();
				String uniKey = null;
				uniKey = customerId.trim();
				
				Map<String, Object> busParams = Maps.newHashMap();
				busParams.put("mobile", mobile);
				busParams.put("targetType", PushTargetType.CUSTOMER_ID.getTargetType());
				busParams.put("targetCode", customerId);
				busParams.put("sendToKey", uniKey);
//				super.sendSingleMsg(MSGTHEMECODE, batch, size, ++count,
//						PushTargetType.CUSTOMER_ID, customerId, uniKey,
//						busParams, null);
				
				super.genSingle(MSGTHEMECODE, taskId, batch, size, orderSort, busParams);
				
				PushBusinessRecord record = new PushBusinessRecord();
				record.setBusinessType(MSGTHEMECODE);
				record.setSecType(SEC_TYPE);
				record.setBatch(batch);
				record.setCustomerId(customerId);
				record.setMobile(mobile);
//				record.setCustomerName(user.get);
				record.setLink(link);
				record.setContent(content);
				record.setCreatedTime(new Date());
				pushBusinessRecordDao.insert(record);
				
				log.info(new StringBuilder().append(LOG_TAG).append("<|>").append("生成单条推送数据").append("<|>")
						.append("batch:").append(batch).append("<|>")
						.append("size:").append(size).append("<|>")
						.append("orderSort:").append(count).append("<|>")
						.append("targetType:").append(1).append("<|>")
						.append("targetCode:").append(customerId).append("<|>")
						.append("mobile:").append(mobile)
						.toString());
			}
		}
			
	}
	
}
