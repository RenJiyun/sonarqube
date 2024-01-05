package com.wlzq.activity.base.biz.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeTypeBiz;
import com.wlzq.activity.base.cons.PriceTimesTypeEnum;
import com.wlzq.activity.base.cons.PrizeReceiveStatusEnum;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dao.ActPrizeTypeDao;
import com.wlzq.activity.base.model.ActPrize;
import com.wlzq.activity.base.model.ActPrizeType;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.base.redis.BaseRedis;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.wlzq.activity.double11.biz.Double11Biz.ACTIVITY_2023DOUBLE11_XTJJDS;

/**
 * @author zhaozx
 */
@Service
public class ActPrizeTypeBizImpl implements ActPrizeTypeBiz {

    private Logger logger = LoggerFactory.getLogger(ActPrizeTypeBizImpl.class);

    @Autowired
    private ActPrizeTypeDao actPrizeTypeDao;
    @Autowired
    private ActPrizeDao actPrizeDao;

    @Override
    @Transactional
    public StatusDto createPrizeType(List<Map<String, Object>> prizeTypeMap) {
        Date now = new Date();
        int count = 0;
        for (Map<String, Object> map : prizeTypeMap) {
            ActPrizeType each = BeanUtils.mapToBean(map, ActPrizeType.class);
            if (ObjectUtils.isEmptyOrNull(each.getCreateTime())) {
                each.setCreateTime(now);
            }
            // 若奖品类型不存在, 新增
            if (actPrizeTypeDao.findByCode(each.getCode()) == null) {
                actPrizeTypeDao.insert(each);
                count++;
            }
        }
        String msg = "成功生成" + count + "个奖品";
        logger.info(msg);
        return new StatusDto(true, StatusDto.SUCCESS, msg);
    }

    @Override
    public void initPrizes(String activityCode, ActPrizeType prizeType) {
        String redisKey = activityCode + ":" + prizeType.getCode();
        boolean initialized = ActivityRedis.ACT_ACTVITY_PRIZE.exists(redisKey);
        if (initialized) {
            return;
        }

        // todo
        // 1. 后续代码需做并发控制
        // 2. 如果该奖品类型设置了每日发放次数, 需要额外的控制机制
        // 3. 发送奖品 id 到 redis 时, 需要批处理, 否则会造成过多的网络请求
        // 4. 日志打印

        // 获取当前尚未发放(可用)的奖品 id 列表, 若为空, 则抛出异常: 奖品已领完
        List<Long> prizeIds = actPrizeDao.findAvailablePrizesByType(activityCode, prizeType.getCode());
        if (CollectionUtil.isEmpty(prizeIds)) {
            throw ActivityBizException.ACT_EMPTY_PRIZE;
        }

        prizeIds.forEach(prizeId -> ActivityRedis.ACT_ACTVITY_PRIZE.sadd(redisKey, prizeId));
    }

    @Override
    public ActPrizeType getPrizeType(String prizeTypeCode) {
        ActPrizeType actPrizeType = (ActPrizeType) BaseRedis.ACT_PRIZETYPE_INFO.get(prizeTypeCode);

        if (actPrizeType == null) {
            actPrizeType = actPrizeTypeDao.findByCode(prizeTypeCode);
            if (actPrizeType == null) {
                return null;
            }
        }
        BaseRedis.ACT_PRIZETYPE_INFO.set(prizeTypeCode, actPrizeType);
        return actPrizeType;
    }

    @Override
    public ActPrize getOneAvailablePrize(String activityCode, ActPrizeType prizeType) {
        String redisKey = activityCode + ":" + prizeType.getCode();
        String id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sPop(redisKey));
        if (ObjectUtils.isEmptyOrNull(id)) {
            // 若缓存中取不到奖品 id, 则尝试重新初始化奖池
            ActivityRedis.ACT_ACTVITY_PRIZE.del(redisKey);
            initPrizes(activityCode, prizeType);
            id = String.valueOf(ActivityRedis.ACT_ACTVITY_PRIZE.sPop(redisKey));
            if (ObjectUtils.isEmptyOrNull(id)) {
                return null;
            }
        }
        return actPrizeDao.get(id);
    }


    @Override
    public PriceTimesTypeEnum getPrizeTimesType(ActPrizeType prizeType, PriceTimesTypeEnum defaultType) {
        return PriceTimesTypeEnum.fromCode(prizeType.getLimitTimesType()).orElse(defaultType);
    }

    @Override
    public PrizeReceiveStatusEnum getReceiveStatus(ActPrizeType prizeType, List<ActPrize> prizeList,
                                                   Activity activity, boolean queryStatus) {
        return canReceive(prizeType, prizeList, activity, queryStatus) ? PrizeReceiveStatusEnum.NOT_RECEIVED : PrizeReceiveStatusEnum.RECEIVED;
    }

    @Override
    public boolean canReceive(ActPrizeType prizeType, List<ActPrize> receivedPrizeList, Activity activity, boolean queryStatus) {
        if (ACTIVITY_2023DOUBLE11_XTJJDS.equals(activity.getCode())) {
            return true;
        }

        if (CollectionUtil.isEmpty(receivedPrizeList)) {
            return true;
        } else {
            PriceTimesTypeEnum prizeTimesType = getPrizeTimesType(prizeType, PriceTimesTypeEnum.ONCE);

            // 校验是否超出了用户可领取数限量
            if (prizeType.getLimitPerUser() != null) {
                boolean result = receivedPrizeList.size() < prizeType.getLimitPerUser();
                if (!result && activity.getCode().equals("ACTIVITY.2023DOUBLE11.DHL") && !queryStatus) {
                    throw ActivityBizException.PRIZE_PER_USER_LIMIT;
                } else if (!result) {
                    return false;
                }
            }

            if (prizeTimesType == PriceTimesTypeEnum.ONCE) {
                // 仅能领取一次
                return receivedPrizeList.isEmpty();
            } else if (prizeTimesType == PriceTimesTypeEnum.DAILY_ONCE) {
                // 每天仅一次
                Date today = new Date();
                Date beginOfDay = DateUtil.beginOfDay(today);
                Date endOfDay = DateUtil.endOfDay(today);

                // 过滤出今天领取的奖品
                List<ActPrize> todayReceived = receivedPrizeList.stream()
                        .filter(e -> e.getUpdateTime().getTime() >= beginOfDay.getTime() && e.getUpdateTime().getTime() <= endOfDay.getTime())
                        .collect(Collectors.toList());
                return CollectionUtil.isEmpty(todayReceived);
            } else if (prizeTimesType == PriceTimesTypeEnum.MULTI_TIMES) {
                return true;
            } else {
                return true;
            }
        }
    }
}
