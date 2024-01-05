package com.wlzq.activity.double11.biz.impl;

import com.google.common.collect.Maps;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.biz.ActPrizeBiz;
import com.wlzq.activity.double11.biz.Double11InternalPromotionBiz;
import com.wlzq.activity.double11.dto.BranchRankingDto;
import com.wlzq.activity.double11.dto.MyRankingInfoDto;
import com.wlzq.activity.double11.dto.TaskIntegralInfo;
import com.wlzq.activity.double11.dto.SaleRankingDto;
import com.wlzq.activity.virtualfin.dao.ActGoodsFlowDao;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.CollectionUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.Page;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.dtasource.dynamic.annotation.DataSourceAnnotation;
import com.wlzq.dtasource.dynamic.enums.DataSourceEnum;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 双11活动内部推动业务接口实现
 *
 * @author zhujt
 */
@Service
@Slf4j
public class Double11InternalPromotionBizImpl implements Double11InternalPromotionBiz {
    /**
     * 攒积分活动编码
     */
    private static final String ACTIVITY_CODE = "ACTIVITY.2023DOUBLE11.ZJF";
    private static final String START_TIME_STR = "2023-11-08 00:00:00";
    private static final String END_TIME_STR = "2023-11-17 23:59:59";
    public static final String ACTIVITY_2023_DOUBLE_11_ZJF_ENDTIME = "ACTIVITY.2023DOUBLE11.ZJF.ENDTIME";
    public static final String ACTIVITY_2023_DOUBLE_11_ZJF_STARTTIME = "ACTIVITY.2023DOUBLE11.ZJF.STARTTIME";
    @Autowired
    private ActPrizeBiz actPrizeBiz;
    @Autowired
    private ActGoodsFlowDao actGoodsFlowDao;

    @DataSourceAnnotation(DataSourceEnum.SLAVE)
    @Override
    public StatusObjDto<List<SaleRankingDto>> saleRanking(String staffNo) {
        if (ObjectUtils.isEmptyOrNull(staffNo)) {
            throw BizException.STAFF_NOT_LOGIN_ERROR;
        }
        Date startTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_STARTTIME,START_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        Date endTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_ENDTIME,END_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        List<SaleRankingDto> saleRankingList = actGoodsFlowDao.saleRanking(startTime,endTime,ACTIVITY_CODE);
        return new StatusObjDto<List<SaleRankingDto>>(true, saleRankingList, 0, "");
    }

    @DataSourceAnnotation(DataSourceEnum.SLAVE)
    @Override
    public StatusObjDto<List<BranchRankingDto>> branchRanking(String staffNo) {
        if (ObjectUtils.isEmptyOrNull(staffNo)) {
            throw BizException.STAFF_NOT_LOGIN_ERROR;
        }
        Date startTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_STARTTIME,START_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        Date endTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_ENDTIME,END_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        List<BranchRankingDto> branchRankingList = actGoodsFlowDao.branchRanking(startTime,endTime,ACTIVITY_CODE);
        return new StatusObjDto<List<BranchRankingDto>>(true, branchRankingList, 0, "");
    }

    @DataSourceAnnotation(DataSourceEnum.SLAVE)
    @Override
    public StatusObjDto<MyRankingInfoDto> myRankingInfo(AccTokenUser user, Page page) {
        if (ObjectUtils.isEmptyOrNull(user) || ObjectUtils.isEmptyOrNull(user.getStaffNo())) {
            throw BizException.STAFF_NOT_LOGIN_ERROR;
        }
        String staffNo = user.getStaffNo();
        Map<String, Object> salerParam = Maps.newHashMap();
        salerParam.put("staffno", staffNo);
        ResultDto salerDto = RemoteUtils.call("sale.staffcooperation.findbycrmno", ApiServiceTypeEnum.COOPERATION, salerParam, true);
        //调用失败
        if (ResultDto.FAIL_COMMON.equals(salerDto.getCode())) {
            return new StatusObjDto<>(false, salerDto.getCode(), salerDto.getMsg());
        }
        //返回为空
        if (null == salerDto.getData() && salerDto.getData().isEmpty()) {
            throw ActivityBizException.STAFFNO_NOT_EXIT;
        }
        log.info("myRankingInfo:staff:{}", JsonUtils.object2JSON(salerDto.getData()));
        String mobile = String.valueOf(salerDto.getData().get("mobile"));
        Date startTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_STARTTIME,START_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        Date endTime = DateUtils.parseDate(AppConfigUtils.get(ACTIVITY_2023_DOUBLE_11_ZJF_ENDTIME,END_TIME_STR),"yyyy-MM-dd HH:mm:ss");
        MyRankingInfoDto myRankingInfoDto = actGoodsFlowDao.myRankingInfo(startTime,endTime,ACTIVITY_CODE,mobile);
        if(ObjectUtils.isNotEmptyOrNull(myRankingInfoDto)&&ObjectUtils.isNotEmptyOrNull(myRankingInfoDto.getSort())){
           List<TaskIntegralInfo>  taskInfoList = actGoodsFlowDao.myTaskIntegralInfo(startTime,endTime,ACTIVITY_CODE,mobile,page);
           myRankingInfoDto.setInfo(taskInfoList);
           myRankingInfoDto.setTotal(CollectionUtils.isEmpty(taskInfoList)?0:taskInfoList.size());
        }
        return new StatusObjDto<MyRankingInfoDto>(true, myRankingInfoDto, 0, "");
    }
}
