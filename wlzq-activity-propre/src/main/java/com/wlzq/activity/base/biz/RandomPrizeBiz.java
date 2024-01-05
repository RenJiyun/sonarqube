package com.wlzq.activity.base.biz;

import com.wlzq.activity.base.model.ActLotteryEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author renjiyun
 */
public interface RandomPrizeBiz {

    /**
     * 随机奖品
     *
     * @param context
     * @return
     */
    RandomResult randomPrize(Map<String, Object> context);

    @Data
    @AllArgsConstructor
    public static class RandomResult {
        private String prizeTypeCode;
        private List<Long> prizeIds;
        private ActLotteryEnum actLotteryEnum;
    }
}
