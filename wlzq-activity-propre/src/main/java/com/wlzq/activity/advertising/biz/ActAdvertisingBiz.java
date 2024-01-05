package com.wlzq.activity.advertising.biz;

import com.wlzq.activity.advertising.model.ActAdvertising;
import com.wlzq.activity.base.model.ActActivityVoucher;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;
import java.util.Map;

public interface ActAdvertisingBiz {

    /**
     * 收集手机号接口
     * @param phoneNumber 手机号码
     * @param pageEncoding 页面编码
     * @return
     */
    StatusDto saveActAdvertising(String phoneNumber, String pageEncoding,String wlzqstatId,String imageCheckCode,String checkCode,String deviceId,
                                 String pgm,String kw,String grp,String pln,String chn);


    /**
     * 发送短信验证码
     * @param mobile
     * @return
     */
    public StatusDto sendCheckCode(String mobile);
}
