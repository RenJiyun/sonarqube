package com.wlzq.activity.task.service.cooperation;

import com.wlzq.activity.task.biz.TaskBatchChkBiz;
import com.wlzq.activity.task.biz.TaskBiz;
import com.wlzq.activity.task.biz.UserTaskBiz;
import com.wlzq.activity.task.dto.ActTaskReqDto;
import com.wlzq.activity.task.dto.ActTaskResDto;
import com.wlzq.activity.task.dto.ActTaskStatusDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.model.account.User;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.annotation.TokenIgnoreSignature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 活动任务
 *
 * @author wlzq
 */
@Service("activity.task")
@ApiServiceType({ApiServiceTypeEnum.COOPERATION})
public class TaskCooperation {

    @Autowired
    private TaskBiz taskBiz;
    @Autowired
    private TaskBatchChkBiz taskBatchChkBiz;
    @Autowired
    private UserTaskBiz userTaskBiz;


    @Signature(true)
    @TokenIgnoreSignature(true)
    public ResultDto status(RequestParams params) {
        String key = params.getKey();
        String accessToken = params.getString("accessToken");

        String userId = params.getString("userId");
        String customerId = params.getString("customerId");

        String task = params.getString("task");
        Long beginDate = params.getLong("beginDate");
        Long endDate = params.getLong("endDate");

        ActTaskReqDto req = new ActTaskReqDto()
                .setTask(task).setBeginDate(beginDate).setEndDate(endDate)
                .setKey(key).setAccessToken(accessToken)
                .setUserId(userId).setCustomerId(customerId);

        ActTaskResDto resDto = taskBiz.queryTaskStatus(req);
        return new ResultDto(0, BeanUtils.beanToMap(resDto), "");
    }


    /**
     * 批量检查北交所权限, 触发客户开通北交所权限任务完成
     *
     * @param params
     * @return
     */
    @Signature(false)
    public ResultDto batchchkbjsperm(RequestParams params) {
        String checkDate = params.getString("checkDate");
        taskBatchChkBiz.checkBjsPerm(checkDate);
        return new ResultDto(0, null, "");
    }

    /**
     * 批量检查入金流水, 触发非有效户入金一万元任务完成
     *
     * @param params
     * @return
     */
    @Signature(false)
    public ResultDto batchchkrjflow(RequestParams params) {
        String checkDate = params.getString("checkDate");
        taskBatchChkBiz.checkRjFlow(checkDate);
        return new ResultDto(0, null, "");
    }


    @Signature(false)
    public ResultDto checkUserTask(RequestParams params) {
        String customerId = params.getString("customerId");
        String mobile = params.getString("mobile");
        String activityCode = params.getString("activityCode");
        String taskCode = params.getString("taskCode");
        String bizCode = params.getString("bizCode");
        String recommendMobile = params.getString("recommendMobile");
        String isBatch = params.getString("isBatch");

        AccTokenUser user = new AccTokenUser();
        user.setMobile(mobile);

        Customer customer = new Customer();
        customer.setCustomerId(customerId);

        StatusObjDto<ActTaskStatusDto> result =  userTaskBiz.doUserTask(activityCode, taskCode, bizCode,
                recommendMobile, mobile, user, customer,
                Boolean.parseBoolean(isBatch));

        if (!result.isOk()) {
            return new ResultDto(result.getCode(), result.getMsg());
        }
        return new ResultDto(0, BeanUtils.beanToMap(result.getObj()), "");

    }
}
