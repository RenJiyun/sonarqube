
package com.wlzq.activity.turntable51.service.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.turntable51.biz.TurntableBiz;
import com.wlzq.activity.turntable51.dto.TurntableHitDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.CustomerMustLogin;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
/**
 * 大转盘服务类
 * @author 
 * @version 1.0
 */
@Service("activity.turntable51")
public class TurntableService extends BaseService{
	
    @Autowired
    private TurntableBiz turntableBiz;
	
    @Signature(true)
    @MustLogin(true)
    @CustomerMustLogin(false)
	public ResultDto turn(RequestParams params,AccTokenUser user,Customer customer) {
    	Long timestamp = params.getLong("timestamp");
    	String customerId = ObjectUtils.isEmptyOrNull(customer)?null:customer.getCustomerId();
		StatusObjDto<TurntableHitDto> result = turntableBiz.turn(user.getUserId(),user.getOpenid(), customerId, timestamp);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}

    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

}
