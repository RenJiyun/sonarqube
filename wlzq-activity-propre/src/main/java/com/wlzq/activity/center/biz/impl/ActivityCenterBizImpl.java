package com.wlzq.activity.center.biz.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wlzq.activity.center.biz.ActivityCenterBiz;
import com.wlzq.activity.center.cons.ActVisibilityEnum;
import com.wlzq.activity.center.dao.ActCenterDao;
import com.wlzq.activity.center.dto.ActCenterDto;
import com.wlzq.activity.center.model.ActCenter;
import com.wlzq.activity.center.redis.ActCenterRedis;
import com.wlzq.common.constant.ClientTypeConstant;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.service.base.sys.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: qiaofeng
 * @date: 2022/3/18 10:41
 * @description:
 */
@Service
public class ActivityCenterBizImpl implements ActivityCenterBiz {
    /*活动最大展示时间为：活动结束时间 + 90天*/
    private static final Integer DAYS_AFTER_ACTIVITY_END = 90;
    /*区块位置：已结束活动*/
    private static final Integer POSITION_ACTIVITY_ENDED = 5;

    @Autowired
    private ActCenterDao actCenterDao;

    @Override
    public StatusObjDto<List<ActCenterDto>> listActivity(Integer cientType) {
        // 查缓存
        List<ActCenter> actlist = (List<ActCenter>) ActCenterRedis.ACT_CENTER_INFO.get(String.valueOf(cientType));

        if (actlist == null) {
            // 未命中缓存，查数据库
            List<Integer> visibility = Lists.newArrayList();
            /*查全部客户端可见的活动*/
            visibility.add(ActVisibilityEnum.ALL_VISIBLE.getCode());
            /*查仅当前客户端可见的活动*/
            visibility.add(visibilityOf(cientType).getCode());
            actlist = actCenterDao.getActlist(visibility, DAYS_AFTER_ACTIVITY_END);

            // 拼接图片地址
            for (ActCenter act : actlist) {
                act.setImage(ImageUtils.getImageUrl(act.getImage()));
            }

            // 放缓存
            ActCenterRedis.ACT_CENTER_INFO.set(String.valueOf(cientType), actlist);
        }

        // 去掉超过活动最大展示时间的活动
        // actlist.removeIf(act -> act.getEndTime().toInstant().plus(DAYS_AFTER_ACTIVITY_END, ChronoUnit.DAYS).toEpochMilli() < Instant.now().toEpochMilli());
        actlist.removeIf(act -> DateUtils.daysBetween(act.getEndTime(), new Date()) > DAYS_AFTER_ACTIVITY_END);

        // 单独处理已结束的活动
        for (ActCenter act : actlist) {
            /*已结束: 活动结束时间 < 当前时间*/
            if (act.getEndTime().getTime() < Instant.now().toEpochMilli()) {
                /*放到已结束的位置*/
                act.setPosition(POSITION_ACTIVITY_ENDED);
            }
        }

        // 按区块位置分组
        Map<Integer, List<ActCenter>> actGroup = actlist.stream().collect(Collectors.groupingBy(ActCenter::getPosition));
        // 组装返回的数据结构
        List<ActCenterDto> actCenterDtoList = Lists.newArrayList();
        actGroup.forEach((position, activityList) -> {
            ActCenterDto dto = new ActCenterDto();
            dto.setPosition(position);
            dto.setActivityList(activityList);

            actCenterDtoList.add(dto);
        });

        return new StatusObjDto<>(true, actCenterDtoList, 0, "");
    }

    @Override
    public StatusDto delCache() {
        /*把App和WeChat的都删掉*/
        for (Integer clientType : VisibilityCache.cache.keySet()) {
            ActCenterRedis.ACT_CENTER_INFO.del(String.valueOf(clientType));
        }
        return new StatusDto(true);
    }

    private static ActVisibilityEnum visibilityOf(Integer clientType) {
        return VisibilityCache.cache.get(clientType);
    }

    private static class VisibilityCache {
        static final Map<Integer, ActVisibilityEnum> cache;

        static {
            cache = Maps.newHashMap();

            cache.put(ClientTypeConstant.APP, ActVisibilityEnum.APP_VISIBLE);
            cache.put(ClientTypeConstant.WECHAT, ActVisibilityEnum.WECHAT_VISIBLE);
            /*其它客户端类型的默认只查全部可见的活动*/
            cache.put(ClientTypeConstant.H5, ActVisibilityEnum.ALL_VISIBLE);
            cache.put(ClientTypeConstant.OTHER, ActVisibilityEnum.ALL_VISIBLE);
        }

        private VisibilityCache() {
        }
    }
}