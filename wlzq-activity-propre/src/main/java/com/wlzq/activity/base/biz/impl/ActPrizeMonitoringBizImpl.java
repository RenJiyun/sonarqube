package com.wlzq.activity.base.biz.impl;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeMonitoringBiz;
import com.wlzq.activity.base.dao.ActPrizeMonitorDao;
import com.wlzq.activity.base.model.ActPrizeMonitor;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.remote.service.common.base.PushBiz;
import com.wlzq.remote.service.common.base.dto.SmsSendDto;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.List;

/**
 * ActPrizeMonitoringBiz实现类
 */

@Service
public class ActPrizeMonitoringBizImpl implements ActPrizeMonitoringBiz {
    @Autowired
    private ActPrizeMonitorDao actPrizeMonitorDao;
    @Autowired
    private PushBiz pushBiz;

    private Logger logger = LoggerFactory.getLogger(ActPrizeMonitoringBizImpl.class);
    public static final String ACT_PRIZE_QUANTITY_MONITORING = "ACT.PRIZE.QUANTITY.MONITORING"; //奖品数量监控短信
    @Override
    @Transactional
    public StatusDto selectPrizeCount() {
        // 查询奖品监控数据
        List<ActPrizeMonitor> actPrizeMonitors = actPrizeMonitorDao.selectAll();
        actPrizeMonitors.forEach(actPrizeMonitor -> {
            if(actPrizeMonitor.getRemainingQuantity() == null){
                actPrizeMonitor.setRemainingQuantity(0);
            }
            // 剩余数量达到监控警戒值发送短信并修改状态
            if (actPrizeMonitor.getSumQuantity() != null && actPrizeMonitor.getRemainingQuantity() <= actPrizeMonitor.getRemainingQuantityWarning()){
                Integer remainingQuantityWarning = actPrizeMonitor.getRemainingQuantityWarning();
                SmsSendDto smsSendDto = new SmsSendDto();
                smsSendDto.setTemplateCode(ACT_PRIZE_QUANTITY_MONITORING);
                smsSendDto.setAsync(0);
                String smsAlertPhoneNumber = actPrizeMonitor.getSmsAlertPhoneNumber();
                String[] phoneNumbers = smsAlertPhoneNumber.split(",|，");
                smsSendDto.setMsgCategory(SmsSendDto.MSG_CATEGORY_PRODUCT);
                String prizeCode = actPrizeMonitor.getCode();
                List<String> activityNames = actPrizeMonitorDao.selectNameByCode(prizeCode);
                StringBuffer activityName = new StringBuffer();
                String prizeName = actPrizeMonitor.getPrizeName();
                activityNames.forEach(name ->{
                     activityName.append(name).append("+");
                });
                activityName.deleteCharAt(activityName.length()-1);
                String jsonParam = new String("{\"activityName\":\""+activityName+"\",\"prizeCode\":\""+prizeCode+"\",\"prizeName\":\""+prizeName+"\",\"remainingQuantity\":\""+remainingQuantityWarning+"\"}");
                for (String phoneNumber: phoneNumbers) {
                    smsSendDto.setMobile(phoneNumber);
                    smsSendDto.setJsonParam(jsonParam);
                    //调用第三方接口发短信
                    StatusDto statusDto = pushBiz.sendSmsWithTmpl(smsSendDto);
                    if (statusDto.getCode() != 0){
                        logger.error("发送短信失败:"+statusDto.getMsg());
                        throw ActivityBizException.SEND_SMS_MESSAGE_FAILED;
                    }
                }
                //修改奖品监控状态为已发送并添加监控触发时间
                Integer count = actPrizeMonitorDao.setStatus(actPrizeMonitor.getCode());
                if (count == 0){
                    logger.error("修改状态失败");
                    throw ActivityBizException.CHANGE_STATUS_FAILED;
                }
            }
        });
        return new StatusDto(true,0);
    }
}
