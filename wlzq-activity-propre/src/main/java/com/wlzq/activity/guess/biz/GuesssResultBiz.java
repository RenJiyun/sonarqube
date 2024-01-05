package com.wlzq.activity.guess.biz;

import com.wlzq.activity.guess.model.Guesss;

/**
 * 猜涨跌结果处理接口
 * @author louie
 *
 */
public interface GuesssResultBiz {

	/**
	 * 处理竞猜者竞猜结果
	 * @param guess
	 * @param resultDirection
	 * @param upRatio
	 * @param downRatio
	 */
	public void handle(Guesss guess,Integer resultDirection,Double upRatio,Double downRatio);

}
