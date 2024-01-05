package com.wlzq.activity.base.biz;

import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.service.base.sys.service.ICacheService;

import java.util.List;

public interface ActCacheBiz extends ICacheService {

    /**
     * 删除缓存
     *
     * @param prefix      缓存的前缀
     * @param key         缓存的key
     * @return StatusDto
     */
    StatusDto deleteCache(String prefix, String key);

    StatusObjDto<List<String>> findPrefix();
}
