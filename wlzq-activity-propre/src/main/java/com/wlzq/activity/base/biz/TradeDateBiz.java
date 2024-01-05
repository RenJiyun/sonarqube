package com.wlzq.activity.base.biz;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.service.base.sys.holiday.biz.HolidayBiz;
import com.wlzq.service.base.sys.holiday.model.Holiday;
/**
 * 活动审核名单业务类
 * @author 
 * @version 1.0
 */
@Service
public class TradeDateBiz{
	
	@Autowired
	private	HolidayBiz holidayBiz;

	/**
	 * 是否为连续的交易日
	 * @param firstDate
	 * @param secondDate
	 * @return
	 */
	public boolean isContinuousTradeDate(Date firstDate,Date secondDate) {
		if(firstDate == null || secondDate == null) {
			return false;
		}
		//是否为交易日
		if(!isTradeDate(firstDate) || !isTradeDate(secondDate)) {
			return false;
		}
		Date comSecondDate = getNextTradeDate(firstDate);
		String comSecondDateStr = DateUtils.formate(comSecondDate);
		String secondDateStr = DateUtils.formate(secondDate);
		if(comSecondDateStr.equals(secondDateStr)) {//若下个交易日与对比第二个交易日相同，则为连续交易日
			return true;
		}
		return false;
	}

	/**
	 * 是否为交易日
	 * @param date
	 * @return
	 */
	public boolean isTradeDate(Date date) {
		if(ObjectUtils.isEmptyOrNull(date)) {
			return false;
		}
		if(DateUtils.isWeekend(date)) return false;
		//查询是否在假期
		date = DateUtils.getDayStart(date);
		Holiday holiday = holidayBiz.getHolidayFromDate(date);
		if(holiday != null) return false;
		return true;
	}

	/**
	 * 获取下一个交易日
	 * @param date
	 * @return
	 */
	public Date getNextTradeDate(Date date) {
		if(ObjectUtils.isEmptyOrNull(date)) {
			return null;
		}
		int numberOfWeek = DateUtils.getIntWeekOfDate(date);
		int addDays = numberOfWeek == 5 ? 3:numberOfWeek == 6 ?2:1;
		Date nextTradeDate = DateUtils.addDay(date, addDays);
		Date nextTradeDateStart = DateUtils.getDayStart(nextTradeDate);
		//查询是否在假期
		Holiday holiday = holidayBiz.getHolidayFromDate(nextTradeDateStart);
		if(holiday != null) {
			return getNextTradeDate(holiday.getDateTo());
		}
		return nextTradeDate;
	}

}
