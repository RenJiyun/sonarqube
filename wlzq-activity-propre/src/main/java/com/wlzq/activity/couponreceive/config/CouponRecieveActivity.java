package com.wlzq.activity.couponreceive.config;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.google.common.collect.Lists;
import com.wlzq.common.utils.DateUtils;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix="couponrecieve")
@PropertySource("classpath:couponrecieve.properties")
public class CouponRecieveActivity {
	private List<String> activityInfos;
	List<RecieveInfo> recieveInfos;
	
    @PostConstruct
    public void initIt() throws Exception {
       if(activityInfos == null) return;
       recieveInfos = Lists.newArrayList();
       for(String info:activityInfos) {
    	   String[] times = info.split(";");
    	   for(String time:times) {
    		   String[] timeInfo = time.trim().split(",");
    		   Date timeStart = DateUtils.parseDate(timeInfo[0].trim(), "yyyy-MM-dd HH:mm:ss");
    		   Date timeEnd = DateUtils.parseDate(timeInfo[1].trim(), "yyyy-MM-dd HH:mm:ss");
    		   String[] ids = timeInfo[2].split("\\|");
    		   String[] couponTemplates = timeInfo[3].split("\\|");
    		   String[] counts = timeInfo[4].split("\\|");
    		   List<RecieveCoupon> coupons = Lists.newArrayList();
    		   for(int i = 0;i < ids.length;i++) {
    			   RecieveCoupon coupon = new RecieveCoupon();
    			   coupon.setId(ids[i].trim());
    			   coupon.setCouponTemplateCode(couponTemplates[i].trim());
    			   coupon.setLimit(Integer.valueOf(counts[i].trim()));
    			   coupons.add(coupon);
    		   }
    		   RecieveInfo recieveInfo = new RecieveInfo();
    		   recieveInfo.setTimeStart(timeStart);
    		   recieveInfo.setTimeEnd(timeEnd);
    		   recieveInfo.setCoupons(coupons);
    		   recieveInfos.add(recieveInfo);
    	   }
       }
    }
}
