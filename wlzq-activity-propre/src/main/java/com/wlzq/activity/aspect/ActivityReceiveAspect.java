package com.wlzq.activity.aspect;

import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.base.dto.AcReceivePriceVO;
import com.wlzq.activity.utils.ActivityRedis;
import com.wlzq.common.model.account.Customer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Random;


/**
 * 订单支付拦截
 *
 * @author jjw
 * @version 2021-11-26
 */
@Aspect
@Component
public class ActivityReceiveAspect {
    @Pointcut("execution(public * com.wlzq.activity.base.biz.impl.CouponCommonReceiveBizImpl.receiveGiftBag(..))")
    public void pointCutMethod() {
    }

    @Before("pointCutMethod()")
    public void interceptorBefore(JoinPoint joinPoint) throws Throwable {

        Thread.sleep(new Random().nextInt(600));

        String key = getKey(joinPoint);

        boolean setNXEX = ActivityRedis.ACT_ACTIVITY_INFO.setNXEX(key, Thread.currentThread().getName());
        if (!setNXEX) {
            throw ActivityBizException.ACT_CLICK_FAST;
        }
    }

    @After("pointCutMethod()")
    public void interceptorAfter(JoinPoint joinPoint) {
        String key = getKey(joinPoint);

        String threadName = Thread.currentThread().getName();
        if (threadName.equals(ActivityRedis.ACT_ACTIVITY_INFO.get(key))) {
            ActivityRedis.ACT_ACTIVITY_INFO.del(key);
        }
    }

    private String getKey(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        AcReceivePriceVO acReceivePriceVO = (AcReceivePriceVO) args[0];
        Customer customer = (Customer) args[1];
        return acReceivePriceVO.getActivityCode() + ":" + customer.getCustomerId() + ":" + acReceivePriceVO.getMobile();
    }


}
