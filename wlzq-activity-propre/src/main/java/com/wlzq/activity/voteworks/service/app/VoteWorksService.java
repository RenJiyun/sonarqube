
package com.wlzq.activity.voteworks.service.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wlzq.activity.base.dto.MyPrizeDto;
import com.wlzq.activity.base.dto.WinDto;
import com.wlzq.activity.voteworks.biz.VoteWorksBiz;
import com.wlzq.activity.voteworks.dto.LeaveMessageDto;
import com.wlzq.activity.voteworks.dto.LikeInfoDto;
import com.wlzq.activity.voteworks.dto.LikeResultDto;
import com.wlzq.activity.voteworks.dto.MessagesDto;
import com.wlzq.activity.voteworks.dto.VoteLotteryDto;
import com.wlzq.activity.voteworks.dto.VoteOverviewDto;
import com.wlzq.activity.voteworks.model.VoteWorks;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
/**
 * 作品投票服务类
 * @author 
 * @version 1.0
 */
@Service("activity.worksvote")
public class VoteWorksService extends BaseService{
	
    @Autowired
    private VoteWorksBiz voteWorksBiz;
 
    @Signature(true)
    @MustLogin(true)
	public ResultDto overview(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}

    	Long timestamp = params.getLong("timestamp");
   		StatusObjDto<VoteOverviewDto> result = voteWorksBiz.overview(user.getUserId(), thirdUid, timestamp);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
   		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto works(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}  		
   		String name = params.getString("name");
   		Integer tag = params.getInt("tag");
    	int[] page = buildPage(params);
   		StatusObjDto<List<VoteWorks>> result = voteWorksBiz.works(tag, name, page[0], page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto ranking(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
    	int[] page = buildPage(params);
    	Integer tag = params.getInt("tag");
   		StatusObjDto<List<VoteWorks>> result = voteWorksBiz.ranking(tag, page[0], page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto worksdetail(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
    	String no = params.getString("no");
    	Integer tag = params.getInt("tag");
    	Long timestamp = params.getLong("timestamp");
   		StatusObjDto<VoteWorks> result = voteWorksBiz.worksDetail(no,tag,user.getUserId(), thirdUid, timestamp);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
   		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
   	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto popset(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		
   		Integer popStatus = params.getInt("popStatus");
   		StatusObjDto<Integer> result = voteWorksBiz.popSet(thirdUid, popStatus);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
   		
    	return new ResultDto(0,"");
   	}
    
    @Signature(true)
    @MustLogin(true)
	public ResultDto canlike(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
   		Integer tag = params.getInt("tag");
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
		StatusObjDto<Map<String,Object>> result = voteWorksBiz.canLike(user.getUserId(), tag);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		/*Integer count = result.getObj();
		Integer status = count > 0?1:0;
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("status", status);*/
		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto like(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   	2021-08-25 去掉微信点赞限制	
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		if(ObjectUtils.isEmptyOrNull(user.getEkpAccount())) {
   			throw BizException.COMMON_CUSTOMIZE_ERROR.format("未绑定EKP账户");
   		}
    	String no = params.getString("no");
    	Integer tag = params.getInt("tag");
    	Long timestamp = params.getLong("timestamp");
		StatusObjDto<LikeResultDto> result = voteWorksBiz.like(no, tag, user.getUserId(), thirdUid, timestamp);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto likerecords(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		
    	String no = params.getString("no");
    	Integer tag = params.getInt("tag");
    	int[] page = buildPage(params);
		StatusObjDto<LikeInfoDto> result = voteWorksBiz.likeRecord(no, tag, page[0], page[1]);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto leavemessage(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		
    	String no = params.getString("no");
    	Integer tag = params.getInt("tag");
    	String content = params.getString("content");
		StatusObjDto<String> result = voteWorksBiz.leaveMessage(no, tag, user.getUserId(), thirdUid, content);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		LeaveMessageDto leaveDto = new LeaveMessageDto();
		leaveDto.setNickname(user.getNickName());
		leaveDto.setPortrait(user.getPortrait());
    	return new ResultDto(0,BeanUtils.beanToMap(leaveDto),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto messages(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		
    	String no = params.getString("no");
    	Integer isReturnPortrait = params.getInt("isReturnPortrait");
    	Long minId = params.getLong("minId");
    	int[] page = buildPage(params);
    	Integer tag = params.getInt("tag");
		StatusObjDto<MessagesDto> result = voteWorksBiz.messages(no,tag,isReturnPortrait,minId, page[0], page[1]);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto lottery(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) || ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}

    	String lotteryCode = params.getString("lotteryCode");
		StatusObjDto<VoteLotteryDto> result = voteWorksBiz.lottery(user.getUserId(), thirdUid,lotteryCode);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}

    @Signature(true)
    @MustLogin(true)
	public ResultDto leavemobile(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}

   		String lotteryCode = params.getString("lotteryCode");
    	String mobile = params.getString("mobile");
		StatusDto result =  voteWorksBiz.leaveMobile(user.getUserId(), thirdUid, lotteryCode, mobile);
		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		
    	return new ResultDto(0,"");
	}

    @Signature(true)
    @MustLogin(true)
   	public ResultDto prizes(RequestParams params,AccTokenUser user) {
    	int[] page = buildPage(params);
   		StatusObjDto<List<WinDto>> result = voteWorksBiz.prizes(page[0], page[1]);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}
    
    @Signature(true)
    @MustLogin(true)
   	public ResultDto myprizes(RequestParams params,AccTokenUser user) {
    	Integer thirdType = user.getThirdType();
   		String thirdUid = user.getThirdUid();
//   		if(thirdType != null && !thirdType.equals(1) && ObjectUtils.isEmptyOrNull(thirdUid)) {
//   			throw BizException.NOT_LOGIN_ERROR.format("微信未登录");
//   		}
   		
   		StatusObjDto<List<MyPrizeDto>> result = voteWorksBiz.myPrizes(user.getUserId(), thirdUid);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
   	}

}
