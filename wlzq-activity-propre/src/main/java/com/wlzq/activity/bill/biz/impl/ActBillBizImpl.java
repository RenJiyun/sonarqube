package com.wlzq.activity.bill.biz.impl;

import java.util.Date;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wlzq.activity.bill.biz.ActBillBiz;
import com.wlzq.activity.bill.dao.ActBillDao;
import com.wlzq.activity.bill.dto.ActBillDto;
import com.wlzq.activity.bill.model.ActBill;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;

@Service
public class ActBillBizImpl implements ActBillBiz {
	//@Autowired
	//private SysNDictDao sysNDictDao;
	@Autowired
	private ActBillDao actBillDao;
	
	@Override
	public StatusObjDto<ActBillDto> view(AccTokenUser user, Customer customer) {
		if(ObjectUtils.isEmptyOrNull(customer)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("customer");
		}
		
		if(!Customer.USER_TYPE_PERSON.equals(customer.getUserType())) {
			throw BizException.CUSTOMER_NOT_PERSON;
		}
		
		ActBill bill = actBillDao.findBillByCustomerId(customer.getCustomerId());
		if(ObjectUtils.isEmptyOrNull(bill)) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("没有查到您的账单信息");
		}
		
		ActBillDto dto = new ActBillDto();
		BeanUtils.copyProperties(bill, dto);
		
		/*SysNDict dict = new SysNDict();
		dict.setType("wish_label");
		List<SysNDict> findList = sysNDictDao.findList(dict);
		List<String> wishs = findList.stream().map(SysNDict::getLabel).collect(Collectors.toList());
		dto.setWishs(wishs);
		
		//开户天数
		Date openDate = DateUtils.getDayStart(bill.getOpenDate());
		Date now = DateUtils.getDayStart(new Date());
		dto.setOpenDateDays(DateUtils.daysBetween(openDate, now));*/

		return new StatusObjDto<ActBillDto>(true, dto, StatusDto.SUCCESS, "");
	}

	@Override
	public ResultDto wish(AccTokenUser user, Customer customer, String wish) {
		/*if(ObjectUtils.isEmptyOrNull(customer)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("customer");
		}
		if(ObjectUtils.isEmptyOrNull(wish)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("wish");
		}
		
		if(!Customer.USER_TYPE_PERSON.equals(customer.getUserType())) {
			throw BizException.CUSTOMER_NOT_PERSON;
		}
		
		ActBill actBill = new ActBill();
		actBill.setCustomerId(customer.getCustomerId());
		actBill.setMyWish(wish);
		actBill.setUpdateTime(new Date());
		actBillDao.updateBillByCustomerId(actBill);*/
		
		return new ResultDto(0, "");
	}

	@Override
	public ResultDto share(AccTokenUser user, Customer customer) {
		if(ObjectUtils.isEmptyOrNull(customer)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("customer");
		}
		
		if(!Customer.USER_TYPE_PERSON.equals(customer.getUserType())) {
			throw BizException.CUSTOMER_NOT_PERSON;
		}
		
		ActBill actBill = new ActBill();
		actBill.setClientId(customer.getCustomerId());
		actBill.setIsShare(CodeConstant.CODE_YES);
		actBill.setUpdateTime(new Date());
		actBillDao.updateBillByCustomerId(actBill);
		
		return new ResultDto(0, "");
	}

}
