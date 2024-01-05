package com.wlzq.activity.base.service.biz;

import com.wlzq.activity.base.biz.ActPrizeMonitoringBiz;
import com.wlzq.activity.base.dao.ActPrizeMonitorDao;
import com.wlzq.activity.base.model.ActPrizeMonitor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ActPrizeMonitoringBizTest {
    @Autowired
    private ActPrizeMonitorDao actPrizeMonitorDao;
    @Autowired
    private ActPrizeMonitoringBiz actPrizeMonitoringBiz;
    @Test
    public void selectAll(){
        List<ActPrizeMonitor> actPrizeMonitors = actPrizeMonitorDao.selectAll();
        actPrizeMonitors.forEach(actPrizeMonitor -> {
            System.out.println(actPrizeMonitor);
        });
    }
    @Test
    public void selectNameByCode(){
        List<String> strings = actPrizeMonitorDao.selectNameByCode("2021040114369418624127");
        strings.forEach(string ->{
            System.out.println(string);
        });
    }
    @Test
    public void setStatus(){
        actPrizeMonitorDao.setStatus("2021040114369418624127");
    }
    @Test
    public void selectPrizeCount(){
        actPrizeMonitoringBiz.selectPrizeCount();
    }
}
