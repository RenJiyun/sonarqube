package com.wlzq.activity.finsection.service.cooperation;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActTeam;
import com.wlzq.activity.finsection.biz.FinanceBiz;
import com.wlzq.activity.finsection.biz.impl.FinanceBizImpl;
import com.wlzq.activity.finsection.service.app.FinanceService;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;

@Service("activity.financecooperation")
@ApiServiceType({ApiServiceTypeEnum.APP})
public class FinanceCooperation extends BaseService {
	
	@Autowired
	private FinanceBiz financeBiz;
	
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    
	private Logger logger = LoggerFactory.getLogger(FinanceCooperation.class);

	@Signature(true)
	@Transactional
	public ResultDto autoformteam(RequestParams params, AccTokenUser user,Customer customer) {
		logger.info(">>>>>>>>>>> autoformteam begin ");
		List<Long> prizes = actPrizeBiz.findAvailablePrizes(FinanceService.ACTIVITY_CODE_818_2019_1_2, FinanceService.ACT_FINSECTION_STEP_1_PRIZE_CODE);
		
		logger.info(">>>>>>>>>>> autoformteam prize left: " + prizes.size());
		if (prizes != null && prizes.size() < FinanceBizImpl.FINSECTION_PRIZE_LEFT) {
			return new ResultDto(0, null, "");
		}
		List<ActTeam> teamList = financeBiz.autoFormTeam(prizes.size());
		//沒有獎品，则发券
		for (ActTeam team : teamList) {
			List<ActPrize> prizeList = actPrizeBiz.findPrize(FinanceService.ACTIVITY_CODE_818_2019_1_2, team.getCreateCustomerId(), null, null, FinanceService.ACT_FINSECTION_STEP_1_PRIZE_CODE, null, null);
			if (prizeList == null || prizeList.size() == 0) {
				actPrizeBiz.giveOutPrize(FinanceService.ACTIVITY_CODE_818_2019_1_2, "", null, FinanceService.ACT_FINSECTION_STEP_1_PRIZE_CODE, team.getCreateUserId(), null, team.getCreateCustomerId(), null);
			}
		}
		logger.info(">>>>>>>>>>> autoformteam form team count: " + teamList.size());
		logger.info(">>>>>>>>>>> autoformteam end ");
		return new ResultDto(0, null, "");
	}
}
