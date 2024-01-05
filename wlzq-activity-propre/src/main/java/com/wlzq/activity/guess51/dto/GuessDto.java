package com.wlzq.activity.guess51.dto;

import lombok.Data;

/**
 * 竞猜情况dto
 * @author 
 * @version 1.0
 */
@Data
public class GuessDto {
	/**状态,0：竞猜成功，1：竞猜失败*/
	private Integer status;
	/**下场是否为下个交易日,0：否，1：是*/
	private Integer nextIsNextTradeDate;
	/**场次*/
	private Integer nextGuessNo;	
}

