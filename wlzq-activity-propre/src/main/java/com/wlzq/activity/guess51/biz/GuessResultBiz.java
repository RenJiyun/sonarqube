package com.wlzq.activity.guess51.biz;

import com.wlzq.activity.guess51.model.Guess;

/**
 * 猜涨跌结果处理接口
 * @author louie
 *
 */
public interface GuessResultBiz {

	/**
	 * 处理竞猜者竞猜结果
	 * @param guess
	 * @param resultDirection
	 * @param upRatio
	 * @param downRatio
	 */
	public void handle(Guess guess,Integer resultDirection,Double upRatio,Double downRatio);

}
