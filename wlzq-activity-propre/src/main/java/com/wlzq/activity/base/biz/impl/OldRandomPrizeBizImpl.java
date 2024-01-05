package com.wlzq.activity.base.biz.impl;

import com.wlzq.activity.base.biz.RandomPrizeBiz;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.model.ActLotteryEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class OldRandomPrizeBizImpl implements RandomPrizeBiz {

    @Autowired
    private ActPrizeDao actPrizeDao;

    @Override
    public RandomResult randomPrize(Map<String, Object> context) {
        String activityCode = (String) context.get("activityCode");
        String userId = (String) context.get("userId");
        String customerId = (String) context.get("customerId");

        /*随机数抽取奖品**/
        Double random = Math.random();
        List<ActLotteryEnum> lotteryEnums = ActLotteryEnum.getActLotteryEnumList(activityCode, userId, customerId);
        ActLotteryEnum lotteryEnum = ActLotteryEnum.getActLottery(lotteryEnums, random);

        String prizeTypeCode = lotteryEnum.getPrizeTypeCode();
        log.info("{}本次随机数为：{}抽奖中奖奖品：{}", userId, random, prizeTypeCode);

        /* 查询剩余可用奖品 **/
        List<Long> allPrizeList = new ArrayList<>();
        if (StringUtils.isNotBlank(prizeTypeCode)) {
            allPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
        }

        // 尝试从第二个奖品池中抽取奖品
        if (allPrizeList.isEmpty()) {
            /*随机数抽取奖品**/
            Double random1 = Math.random();
            lotteryEnums = ActLotteryEnum.getActLotteryEnumListSecond(activityCode, userId, customerId);
            lotteryEnum = ActLotteryEnum.getActLottery(lotteryEnums, random1);
            prizeTypeCode = lotteryEnum.getPrizeTypeCode();
            log.info("{}本次随机数为：{}抽奖中奖奖品：{}", userId, random, prizeTypeCode);
            /* 查询剩余可用奖品 **/
            allPrizeList = actPrizeDao.findAvailablePrizesByType(activityCode, prizeTypeCode);
        }

        return new RandomResult(prizeTypeCode, allPrizeList, lotteryEnum);
    }
}
