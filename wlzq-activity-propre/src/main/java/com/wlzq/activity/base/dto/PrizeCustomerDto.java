package com.wlzq.activity.base.dto;


import org.apache.commons.lang.StringUtils;

import lombok.Data;

/**
 * 
 * @author zhaozx
 * @version 2019-08-19
 */
@Data
public class PrizeCustomerDto {
	
	private String userName;
	private String mobile;
	private String prizeName;
	private String customerId;
	
    public static String replaceStarAction(String string) {
    	if (string == null) return null;
        String userNameAfterReplaced = "";
        int nameLength = string.length();
        if(nameLength<3 && nameLength>0){
            if(nameLength==1){
                userNameAfterReplaced = "*";
            }else{
                userNameAfterReplaced = string.substring(0, 1) + "*";
            }
        }else{
            Integer num1,num2,num3;
            num2=(new Double(Math.ceil(new Double(nameLength)/3))).intValue();
            num1=(new Double(Math.floor(new Double(nameLength)/3))).intValue();
            num3=nameLength-num1-num2;
            String star= StringUtils.repeat("*",num2);
            userNameAfterReplaced = string.replaceAll("(.{"+num1+"})(.{"+num2+"})(.{"+num3+"})","$1"+star+"$3");
        }
        return userNameAfterReplaced;
    }
    
    public static PrizeCustomerDto replaceStar(PrizeCustomerDto dto) {
    	if (dto == null) return null;
    	String customerId = replaceStarAction(dto.getCustomerId());
    	String userName = replaceStarAction(dto.getUserName());
    	String mobile = replaceStarAction(dto.getMobile());
    	PrizeCustomerDto newDto = new PrizeCustomerDto();
    	newDto.setCustomerId(customerId);
    	newDto.setUserName(userName);
    	newDto.setMobile(mobile);
    	newDto.setPrizeName(dto.getPrizeName());
    	return newDto;
    }
}
