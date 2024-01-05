package com.wlzq.activity.base.biz.impl;

import com.wlzq.activity.base.biz.ActCacheBiz;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.service.base.sys.RedisFacadeAbstract;
import com.wlzq.service.base.sys.service.impl.CacheServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: qiaofeng
 * @date: 2022/5/17 11:06
 * @description:
 */
@Service
public class ActCacheBizImpl extends CacheServiceImpl implements ActCacheBiz {
    /*RedisFacadeAbstract子类的包路径*/
    private static final String BASEPACKAGE = "com.wlzq.activity.*";

    @Override
    public StatusDto deleteCache(String prefix, String key) {
        if (ObjectUtils.isNotEmptyOrNull(key)) {
            // 如果指定了要删除的缓存的key，就只删除这个缓存
            return super.deleteCache(BASEPACKAGE, prefix, key);
        }

        /*取出支持删除key的对象*/
        StatusObjDto<RedisFacadeAbstract> objDto = super.getRedisFacadeAbstract(BASEPACKAGE, prefix);
        RedisFacadeAbstract redisFacadeAbstract = objDto.getObj();

        /*拿到指定前缀的全部key*/
        Set<String> keys = redisFacadeAbstract.keys();
        final List<String> keyList = new ArrayList<>(keys).stream()
                .map(item -> {
                    String[] split = item.split(prefix);
                    return split[split.length - 1];
                }).collect(Collectors.toList());

        // 如果没指定要删除的key，就删除指定前缀的全部缓存
        keyList.forEach(redisFacadeAbstract::del);

        return new StatusDto(true);
    }

    @Override
    public StatusObjDto<List<String>> findPrefix() {
        return super.prefixList(BASEPACKAGE);
    }
}
