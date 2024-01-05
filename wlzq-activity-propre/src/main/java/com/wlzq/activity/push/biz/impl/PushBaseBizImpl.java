package com.wlzq.activity.push.biz.impl;

import com.google.common.collect.Maps;
import com.wlzq.activity.base.dao.ActPrizeDao;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.push.biz.PushBaseBiz;
import com.wlzq.activity.push.dto.NoticeDto;
import com.wlzq.activity.push.dto.RenewedReceivedNoticeDto;
import com.wlzq.activity.push.dto.StaffPushDto;
import com.wlzq.activity.push.executor.BaseExecutor;
import com.wlzq.activity.push.redis.PushRedis;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.SpringApplicationContext;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.common.base.PushBiz;
import com.wlzq.remote.service.common.push.dto.PushUserDto;
import com.wlzq.remote.service.common.push.dto.SceneSendDto;
import com.wlzq.remote.service.common.push.dto.WxParaDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

@Service
public class PushBaseBizImpl implements PushBaseBiz {
    /*已经发过短信的用户*/
    private static final String NOTICED = "noticed";

    @Autowired
    private ActPrizeDao prizeDao;
    @Autowired
    private ActivityDao activityDao;
    @Autowired
    private PushBiz pushBiz;


    @Override
    public StatusDto genPushData(String exeTask) {
        BaseExecutor exe = (BaseExecutor) SpringApplicationContext.getBean(exeTask);
        return exe.run();
    }

    /**
     * 查当天发券成功的记录
     */
    @Override
    public StatusObjDto<List<RenewedReceivedNoticeDto>> renewedReceivedNotice(String activityCode) {
        /*今天*/
        LocalDate today = LocalDate.now();
        /*今天00:00:00*/
        long updateTimeFrom = LocalDateTime.of(today, LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        /*今天23:59:59 */
        long updateTimeTo = LocalDateTime.of(today, LocalTime.MAX).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        /*当天全部发券成功的*/
        final List<RenewedReceivedNoticeDto> allReceivedList =
                prizeDao.findNoticeList(activityCode, new Date(updateTimeFrom), new Date(updateTimeTo));

        /*已经发过短信的*/
        final Set<String> noticedMobiles = Optional.ofNullable((Set<String>) PushRedis.RECEIVED_NOTICED.get(NOTICED))
                .orElse(new HashSet<>());

        /*需要短信通知的*/
        List<RenewedReceivedNoticeDto> noticeList = allReceivedList.stream()
                .filter(dto -> !noticedMobiles.contains(dto.getMobile())).collect(Collectors.toList());

        /*已经发送过短信的用户信息放缓存里*/
        Set<String> allNoticedMobiles = allReceivedList.stream().map(RenewedReceivedNoticeDto::getMobile)
                .collect(Collectors.toSet());
        PushRedis.RECEIVED_NOTICED.set(NOTICED, allNoticedMobiles);

        return new StatusObjDto<>(true, noticeList);
    }

    /**
     * 查发券成功的记录
     */
    @Override
    public StatusObjDto<List<NoticeDto>> receivedNotice(String activityCode) {

        /*今天*/
        LocalDate today = LocalDate.now();
        /*今天00:00:00*/
        long updateTimeFrom = LocalDateTime.of(today, LocalTime.MIN).toInstant(ZoneOffset.of("+8")).toEpochMilli();
        /*今天23:59:59 */
        long updateTimeTo = LocalDateTime.of(today, LocalTime.MAX).toInstant(ZoneOffset.of("+8")).toEpochMilli();

        /*当天全部发券成功的*/
        final List<NoticeDto> allReceivedList =
                prizeDao.findNotice(activityCode, new Date(updateTimeFrom), new Date(updateTimeTo));

        /*已经发过短信的*/
        final Set<String> noticedMobiles = Optional.ofNullable((Set<String>) PushRedis.NOTICED.get(activityCode))
                .orElse(new HashSet<>());

        /*需要短信通知的*/
        List<NoticeDto> noticeList = allReceivedList.stream()
                .filter(dto -> !noticedMobiles.contains(dto.getMobile())).collect(Collectors.toList());

        /*已经发送过短信的用户信息放缓存里*/
        Set<String> allNoticedMobiles = allReceivedList.stream().map(NoticeDto::getMobile)
                .collect(Collectors.toSet());
        PushRedis.NOTICED.set(activityCode, allNoticedMobiles);

        return new StatusObjDto<>(true, noticeList);
    }


    /**
     * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
     */
    @Override
    public StatusObjDto<List<StaffPushDto>> pushInvestAdviserOrderToStaff(String sceneNo) {
        Date now = new Date();
        Date sDate = DateUtils.addMinute(now, -12);
        Date eDate = DateUtils.addMinute(now, -2);
        String startTime = DateUtils.formate(sDate,"yyyy-MM-dd HH:mm:ss");
        String endTime = DateUtils.formate(eDate,"yyyy-MM-dd HH:mm:ss");

        List<StaffPushDto> staffPushList = activityDao.findStaffPushList(startTime, endTime);
        for (StaffPushDto staffPushDto : staffPushList) {
            Map<String, Object> openidParams = Maps.newHashMap();
            String recommendMobile = staffPushDto.getRecommendMobile();
            if (StringUtils.isBlank(recommendMobile)) {
                continue;
            }

            openidParams.put("mobile", recommendMobile);
            ResultDto openidDto = RemoteUtils.call("account.staffcooperation.getopenidbymobile", ApiServiceTypeEnum.COOPERATION, openidParams, false);
            if (ResultDto.SUCCESS.equals(openidDto.getCode()) && openidDto.getData() != null
                    && ObjectUtils.isNotEmptyOrNull(openidDto.getData().get("openid"))) {
                String openid = String.valueOf(openidDto.getData().get("openid"));
                List<PushUserDto> users = new ArrayList<>();
                users.add(new PushUserDto().setWxOpenid(openid));
                SceneSendDto sendDto = new SceneSendDto();
                sendDto.setUsers(users);
                sendDto.setSceneNo(sceneNo);
                sendDto.setCid(staffPushDto.getOrderNo() + staffPushDto.getRecommendMobile() + "weixin");

                Map<String,Object> templatePara = new HashMap<>();
                sendDto.setTemplatePara(templatePara);

                templatePara.put("first",new WxParaDto().setValue("决策工具代金券转化营销提醒"));
                templatePara.put("EventSystem",new WxParaDto().setValue("【双11财富狂欢节活动】"));
                templatePara.put("EventContent", new WxParaDto().setValue("【客户下单投顾产品获赠决策工具代金券】"));
                templatePara.put("Total",new WxParaDto().setValue("1"));
                templatePara.put("remark",new WxParaDto().setValue("备注：请查收销售手册。").setColor("#FF0000"));
                templatePara.put("url","https://mp.weixin.qq.com/s/fj9ri84Ljz7pyW0ruXDx3w?come=wxmsg");

                pushBiz.sendSceneMessage(sendDto);
            }
        }
        return new StatusObjDto<>(true);
    }

    /**
     * 员工推荐客户购买投顾产品成功下单后，推送决策工具推广消息
     */
    @Override
    public StatusObjDto<List<StaffPushDto>> pushInvestAdviserOrderToStaff2(String startTime, String endTime) {
        List<StaffPushDto> staffPushList = activityDao.findStaffPushList(startTime, endTime);
        for (StaffPushDto staffPushDto : staffPushList) {
            Map<String, Object> openidParams = Maps.newHashMap();
            String recommendMobile = staffPushDto.getRecommendMobile();
            if (StringUtils.isBlank(recommendMobile)) {
                continue;
            }

            openidParams.put("mobile", recommendMobile);
            ResultDto openidDto = RemoteUtils.call("account.staffcooperation.getopenidbymobile", ApiServiceTypeEnum.COOPERATION, openidParams, false);
            boolean noOpenId = !ResultDto.SUCCESS.equals(openidDto.getCode())
                    || openidDto.getData() == null
                    || ObjectUtils.isEmptyOrNull(openidDto.getData().get("openid"));
            if (noOpenId) {
                continue;
            }
            String openid = String.valueOf(openidDto.getData().get("openid"));
            staffPushDto.setOpenId(openid);
        }
        return new StatusObjDto<>(true,staffPushList);
    }




    /**
     * 推送2022双11活动销售榜单微信消息，给非总部的所有员工
     */
    @Override
    public StatusObjDto push2022DoubleActivitySaleListToStaff(String sceneNo) {
        //找出非总部的员工列表
        List<StaffPushDto> staffPushDtos = findBranchStaff();

        List<PushUserDto> pushUserDtos = staffPushDtos.stream()
                .map(e -> new PushUserDto().setWxOpenid(e.getOpenId()))
                .collect(collectingAndThen(
                                toCollection(() -> new TreeSet<>(Comparator.comparing(PushUserDto::getWxOpenid))), ArrayList::new
                        )
                );
//
        //发送微信消息
        batchPush2022DoubleActivitySaleList(pushUserDtos,200,sceneNo);
        return new StatusObjDto<>(true);
    }

    /**
     * 找出非总部的员工列表
     */
    public List<StaffPushDto> findBranchStaff(){
        return activityDao.findBranchStaff();
    }

    public void batchPush2022DoubleActivitySaleList(List<PushUserDto> datas,Integer perNum,String sceneNo){
        int size = datas.size();
        int segmentStart = 0;
        int perSegmentCounter = 0;
        ArrayList<PushUserDto> perSegmentDto = new ArrayList<>(perNum);
        for (int i = 0; i < size; i++) {
            perSegmentCounter++;
            perSegmentDto.add(datas.get(i));

            if (perSegmentCounter==perNum) {
                int segmentEnd = segmentStart + perNum;
                push2022DoubleActivitySaleList(perSegmentDto,sceneNo);
                segmentStart = segmentEnd;
                perSegmentCounter = 0;
                perSegmentDto = new ArrayList<>(perNum);
            }else if (i==size-1) {
                push2022DoubleActivitySaleList(perSegmentDto,sceneNo);
            }
        }
    }

    public void push2022DoubleActivitySaleList(List<PushUserDto> users,String sceneNo){
        SceneSendDto sendDto = new SceneSendDto();
        sendDto.setUsers(users);
        sendDto.setSceneNo(sceneNo);

        Map<String,Object> templatePara = new HashMap<>();
        sendDto.setTemplatePara(templatePara);

        templatePara.put("first",new WxParaDto().setValue("双11销售奖励排行榜单"));
        templatePara.put("EventSystem",new WxParaDto().setValue("11.11财富狂欢节"));
        templatePara.put("EventContent", new WxParaDto().setValue("销售奖励排行榜单查看任务"));
        templatePara.put("Total",new WxParaDto().setValue("1"));
        templatePara.put("remark",new WxParaDto().setValue("备注：马上查看榜单").setColor("#FF0000"));
        templatePara.put("url","https://m.wlzq.cn/s/doubleeleven/rankguide?come=wxmsg");
        pushBiz.sendSceneMessage(sendDto);
    }


    @Override
    public StatusObjDto<List<StaffPushDto>> push2022DoubleActivitySaleListToStaff2() {
        //找出非总部的员工列表
        List<StaffPushDto> staffPushDtos = findBranchStaff();

        List<StaffPushDto> pushUserDtos = staffPushDtos.stream()
                .collect(collectingAndThen(
                            toCollection(() -> new TreeSet<>(Comparator.comparing(StaffPushDto::getOpenId))), ArrayList::new
                        )
                );
        return new StatusObjDto<>(true,pushUserDtos);
    }



}
