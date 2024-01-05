package com.wlzq.activity.base.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.wlzq.activity.base.biz.RandomPrizeBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActLotteryEnum;
import com.wlzq.activity.base.model.ActPrize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.wlzq.activity.base.model.ActLotteryEnum.*;

@Service
public class GgqzdRandomPrizeBizImpl implements RandomPrizeBiz {

    @Autowired
    private ActPrizeDao actPrizeDao;

    private static final List<ActLotteryEnum> pool1 = new ArrayList<>();
    private static final List<ActLotteryEnum> pool2 = new ArrayList<>();
    private static final List<ActLotteryEnum> pool3 = new ArrayList<>();

    static {
        pool1.add(LOTTERY_GGQZD_L2_FREE_30);
        pool1.add(LOTTERY_GGQZD_RED_PACKET_288);
        pool1.add(LOTTERY_GGQZD_RED_PACKET_188);
        pool1.add(LOTTERY_GGQZD_IQIYI);
        pool1.add(LOTTERY_GGQZD_XTJJDS1);

        pool2.add(LOTTERY_GGQZD_RED_PACKET_088);
        pool2.add(LOTTERY_GGQZD_RED_PACKET_688);
        pool2.add(LOTTERY_GGQZD_RED_PACKET_888);
        pool2.add(LOTTERY_GGQZD_JCZX_100);

        pool3.add(LOTTERY_GGQZD_JCZX_50);
    }

    @Override
    public RandomResult randomPrize(Map<String, Object> context) {
        String activityCode = (String) context.get("activityCode");
        Double random = Math.random();
        ActLotteryEnum lotteryEnum = getActLottery(pool1, random);
        List<Long> actPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode, lotteryEnum.getPrizeTypeCode());
        if (CollectionUtil.isEmpty(actPrizeList)) {
            random = Math.random();
            lotteryEnum = getActLottery(pool2, random);
            actPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode, lotteryEnum.getPrizeTypeCode());

            if (CollectionUtil.isEmpty(actPrizeList)) {
                random = Math.random();
                lotteryEnum = getActLottery(pool3, random);
                actPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode, lotteryEnum.getPrizeTypeCode());
            }
        }
        return new RandomResult(lotteryEnum.getPrizeTypeCode(), actPrizeList, lotteryEnum);
    }
}
