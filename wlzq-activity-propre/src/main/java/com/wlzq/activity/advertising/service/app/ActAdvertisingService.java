/**
 * Copyright &copy; 2001-2018 <a href="http://www.wlzq.cn">wlzq</a> All rights reserved.
 */
package com.wlzq.activity.advertising.service.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;
import com.wlzq.activity.advertising.biz.ActAdvertisingBiz;
import com.wlzq.activity.advertising.dao.ActAdvertisingDao;
import com.wlzq.activity.advertising.model.ActAdvertising;
import com.wlzq.common.constant.CheckcodeTypeE;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.account.FortuneBiz;
import com.wlzq.remote.service.common.base.CheckcodeBiz;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收集投放广告手机号记录Service
 * @author pjw
 * @version 2021-07-16
 */
@Service("activity.actadvertising")
public class ActAdvertisingService extends BaseService {

   @Autowired
   private ActAdvertisingBiz actAdvertisingBiz;



    @Signature(true)
    public ResultDto create(RequestParams params) {
        String pageEncoding = params.getString("pageEncoding");
        String phoneNumber = params.getString("mobile");
        String deviceId = params.getString("deviceId");
        String imageCheckCode = params.getString("imageCheckCode");
        String checkCode = params.getString("checkCode");
        String wlzqstatId = params.getCookie("wlzqstat_id");
        String pgm = params.getString("pgm");
        String kw = params.getString("kw");
        String grp = params.getString("grp");
        String pln = params.getString("pln");
        String chn = params.getString("chn");

        StatusDto  result = actAdvertisingBiz.saveActAdvertising(phoneNumber,pageEncoding,wlzqstatId,imageCheckCode,checkCode,deviceId,
                pgm,kw,grp,pln,chn);
         return new ResultDto(result.getCode(), result.getMsg());
    }

    @Signature(true)
    public ResultDto sendcheckcode(RequestParams params) {
        String mobile = (String)params.get("mobile");
        StatusDto result = actAdvertisingBiz.sendCheckCode(mobile);
        if(!result.isOk()) {
            return new ResultDto(result.getCode(),result.getMsg());
        }

        return new ResultDto(0,"");
    }
	
}