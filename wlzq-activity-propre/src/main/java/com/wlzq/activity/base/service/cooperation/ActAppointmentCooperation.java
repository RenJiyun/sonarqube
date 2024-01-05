package com.wlzq.activity.base.service.cooperation;

import com.wlzq.activity.base.dao.ActAppointmentDao;
import com.wlzq.activity.base.dao.ActivityDao;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author renjiyun
 */
@Service("activity.appointmentcooperation")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
@Slf4j
public class ActAppointmentCooperation extends BaseService {

    public static final String GROUP_CODE = "ACTIVITY.MIAOSHAXQY.GROUP";

    @Autowired
    private ActivityDao activityDao;

    @Autowired
    private ActAppointmentDao actAppointmentDao;

    /**
     * 发送预约提醒
     */
    @Signature(false)
    public ResultDto sendreminder(RequestParams params) {
        List<Activity> activityList = activityDao.findListByGroupCode(GROUP_CODE);
        Date now = new Date();

        // 过滤出即将开始的活动
        Optional<Activity> activityOptional = activityList.stream().filter(activity -> {
            Date startTime = activity.getDateFrom();
            return startTime.after(now);
        }).min((a1, a2) -> {
            Date startTime1 = a1.getDateFrom();
            Date startTime2 = a2.getDateFrom();
            return startTime1.compareTo(startTime2);
        });

        if (activityOptional.isPresent()) {
            List<String> mobileList = actAppointmentDao.getAllUserMobileNeedSendReminder(activityOptional.get().getCode());
            int size = 500;
            int total = mobileList.size();
            if (total == 0) {
                return new ResultDto(0, "");
            }
            int page = total / size + 1;
            for (int i = 0; i < page; i++) {
                int fromIndex = i * size;
                int toIndex = (i + 1) * size;
                toIndex = Math.min(toIndex, total);
                List<String> subList = mobileList.subList(fromIndex, toIndex);
                doSend(activityOptional.get(), subList);
            }
        }
        return new ResultDto(0, "");
    }


    private static final String SCENE_NO_KEY = "activity.appointment.MIAOSHAXQY";

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private void doSend(Activity activity, List<String> subList) {
        // 参数格式: {"sceneNo":"1032308030001", "users":[{"mobile":"18170970067"},{"mobile":"15521128034"}]}
        String sceneNo = AppConfigUtils.get(SCENE_NO_KEY, "");
        if (ObjectUtils.isEmptyOrNull(sceneNo)) {
            log.error("发送预约提醒失败, 未配置场景号");
            return;
        }
        Map<String, Object> params = new HashMap<>();
        params.put("sceneNo", sceneNo);
        List<Map<String, Object>> users = new ArrayList<>();
        subList.forEach(mobile -> {
            Map<String, Object> user = new HashMap<>();
            user.put("mobile", mobile);
            users.add(user);
        });
        params.put("users", users);
        try {
            RemoteUtils.call("upush.scenecooperation.send", ApiServiceTypeEnum.COOPERATION, params, false);
        } catch (Exception e) {
            log.error("发送预约提醒失败", e);
            throw e;
        }
    }
}
