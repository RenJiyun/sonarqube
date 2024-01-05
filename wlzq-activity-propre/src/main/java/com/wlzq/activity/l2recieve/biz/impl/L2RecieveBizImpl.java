package com.wlzq.activity.l2recieve.biz.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wlzq.activity.base.biz.ActivityBaseBiz;
import com.wlzq.activity.base.biz.Level2Biz;
import com.wlzq.activity.base.dto.Level2OpenDto;
import com.wlzq.activity.base.model.Activity;
import com.wlzq.activity.l2recieve.biz.L2RecieveBiz;
import com.wlzq.activity.l2recieve.dao.Level2RecieveDao;
import com.wlzq.activity.l2recieve.dao.Level2RecieveInviteDao;
import com.wlzq.activity.l2recieve.dao.Level2RecievePrizeDao;
import com.wlzq.activity.l2recieve.dao.Level2RecieveUserDao;
import com.wlzq.activity.l2recieve.dto.InviteDto;
import com.wlzq.activity.l2recieve.dto.OpenDto;
import com.wlzq.activity.l2recieve.model.AppUser;
import com.wlzq.activity.l2recieve.model.Level2Recieve;
import com.wlzq.activity.l2recieve.model.Level2RecieveInvite;
import com.wlzq.activity.l2recieve.model.Level2RecievePrize;
import com.wlzq.activity.l2recieve.model.Level2RecieveUser;
import com.wlzq.activity.l2recieve.redis.L2RecieveRedis;
import com.wlzq.common.constant.CheckcodeTypeE;
import com.wlzq.common.constant.CodeConstant;
import com.wlzq.common.model.account.AccUser;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.common.utils.DateUtils;
import com.wlzq.common.utils.JsonUtils;
import com.wlzq.common.utils.ObjectUtils;
import com.wlzq.common.utils.RegxUtils;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.core.exception.BizException;
import com.wlzq.remote.service.common.account.AccountUserBiz;
import com.wlzq.remote.service.common.base.CheckcodeBiz;
import com.wlzq.remote.service.utils.RemoteUtils;

/**
 * L2领取业务接口实现
 * @author louie
 *
 */
@Service
public class L2RecieveBizImpl extends ActivityBaseBiz implements L2RecieveBiz {
	private Logger logger = LoggerFactory.getLogger(L2RecieveBizImpl.class);
	/** 活动编码 */
	public static final String ACTIVITY_CODE = "ACTIVITY.L2RECIEVE";
	/** 账户生效后允许领取时间 */
	public static final Integer RECIEVE_ALLOW_DAYS = 30;
	/** Level支付方式 */
	public static final Integer LEVEL2_PAY_MODE = 7;
	//验证码发送模板
	private static final String SMS_CHECKCODE_TEMPLATE = "ACTIVITY.L2RECIEVEINVITE.CHECKCODE";
	
	@Autowired
    private  Level2RecievePrizeDao prizeDao;
	
	@Autowired
    private  Level2RecieveInviteDao inviteDao;
	
	@Autowired
    private  Level2RecieveDao recieveDao;
	
	@Autowired
    private  Level2RecieveUserDao customerDao;
	
    @Autowired
    private  AccountUserBiz userBiz;
    @Autowired
    private  Level2Biz level2Biz;
    @Autowired
    private  CheckcodeBiz checkCodeBiz;
    
    @Override
    public StatusObjDto<List<OpenDto>> recieveStatus(String mobile){
    	List<Level2Recieve> recieves = recieveDao.findStatus(mobile);
    	Map<Integer,OpenDto> recievesMap = new HashMap<Integer,OpenDto>();
    	for(Level2Recieve recieve:recieves) {
    		OpenDto openDto = new OpenDto();
    		openDto.setRecieveStatus(OpenDto.RECIEVE_SUCCESS);
    		Integer activeStatus = recieve.getStatus().equals(Level2Recieve.STATUS_OPEN_SUCCESS)?OpenDto.STATUS_ACTIVE:
    			OpenDto.STATUS_NOT_ACTIVE;
    		if(activeStatus.equals(Level2Recieve.STATUS_OPEN_SUCCESS)) {
    			openDto.setRecieveStatus(OpenDto.RECIEVE_ALREADY);
    		}
    		openDto.setActiveStatus(activeStatus);
    		openDto.setAmount(0);
    		openDto.setMobile(recieve.getMobile());
    		openDto.setType(recieve.getType());
    		recievesMap.put(recieve.getType(), openDto);
    	}
    	Integer[] types = new Integer[] {Level2RecievePrize.TYPE_NEW_CREDIT,
    			Level2RecievePrize.TYPE_NEW_EFFECTIVE,Level2RecievePrize.TYPE_NEW_OPEN};
    	List<OpenDto> opens = new ArrayList<OpenDto>();
    	for(Integer type:types) {
    		if(recievesMap.containsKey(type)) {//已领取添加
    			opens.add(recievesMap.get(type));
    		}else{//未领取添加
    			OpenDto openDto = new OpenDto();
        		openDto.setRecieveStatus(OpenDto.RECIEVE_NOT);
        		Integer activeStatus = OpenDto.STATUS_NOT_ACTIVE;
        		openDto.setActiveStatus(activeStatus);
        		openDto.setAmount(0);
        		openDto.setMobile(mobile);
        		openDto.setType(type);
        		opens.add(openDto);
    		}
    	}
    	
    	return new StatusObjDto<List<OpenDto>>(true,opens,StatusDto.SUCCESS,"");
    }
    
    @Transactional
	@Override
	public StatusObjDto<OpenDto> recieve(Integer type,String userId, String mobile,String ip) {
		if(ObjectUtils.isEmptyOrNull(type)) {
			//throw BizException.COMMON_PARAMS_NOT_NULL.format("type");
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_PARAMS_NOT_NULL.getCode(),"type参数不能为空");
		}
		if(type < Level2RecievePrize.TYPE_NEW_OPEN || type > Level2RecievePrize.TYPE_NEW_CREDIT)
		if(ObjectUtils.isEmptyOrNull(userId)) {
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_PARAMS_NOT_NULL.getCode(),"userId参数不能为空");
		}
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_PARAMS_NOT_NULL.getCode(),"mobile参数不能为空");
		}
		if(!RegxUtils.isMobile(mobile)) {
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_PARAMS_IS_ILLICIT.getCode(),"mobile参数不合法");
		}
        
		//活动有效性检查
		Activity activity = findActivity(ACTIVITY_CODE);
		if(activity == null) {
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_CUSTOMIZE_ERROR.getCode(),"活动不存在");
		}
		Activity copyAct = new Activity();
		BeanUtils.copyToBean(activity, copyAct);
		Date recieveEndDate = DateUtils.addDay(copyAct.getDateTo(), RECIEVE_ALLOW_DAYS);
		recieveEndDate = DateUtils.getDayEnd(recieveEndDate);
		copyAct.setDateTo(recieveEndDate);
		StatusDto validStatus = isValid(copyAct);
		if(!validStatus.isOk()) {
			return new StatusObjDto<OpenDto>(true,validStatus.getCode(),validStatus.getMsg());
		}
		
		//查询有无领取过奖品
		OpenDto openDto = new OpenDto();
		openDto.setType(type);
		openDto.setMobile(mobile);
		openDto.setAmount(0);
		Level2Recieve recieve = new Level2Recieve();
		recieve.setType(type);
		recieve.setMobile(mobile);
		List<Level2Recieve> recieves = recieveDao.findList(recieve);
		if(recieves.size() > 0) {
			openDto.setRecieveStatus(OpenDto.RECIEVE_ALREADY);
			Integer status = recieves.get(0).getStatus();
			Integer activeStatus = status.equals(Level2Recieve.STATUS_OPEN_SUCCESS)?OpenDto.STATUS_ACTIVE:OpenDto.STATUS_NOT_ACTIVE;
			openDto.setActiveStatus(activeStatus);
			return new StatusObjDto<OpenDto>(true,openDto,StatusDto.SUCCESS,"");
		}
		
		//获取Level2商品
		Level2RecievePrize prize = prizeDao.findPrize(type);
		if(prize == null) {
			return new StatusObjDto<OpenDto>(false,BizException.COMMON_CUSTOMIZE_ERROR.getCode(),"奖品未配置");
		}
		
		Level2RecieveUser customer = customerDao.findCustomer(type, mobile);
//		if(customer == null) { //兼容旧带*号手机（旧数据）
//			String mobileLike = mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
//			customer = customerDao.findCustomer(type, mobileLike);
//		}
		if(customer != null && ObjectUtils.isNotEmptyOrNull(customer.getEffectiveDate())) { //已有客户处理
			Date effectiveDate = customer.getEffectiveDate();
			Date now = new Date();
			if(effectiveDate.getTime() > now.getTime()) {//有效时间大于当前时间返回（有效户数据有此情况）
				return new StatusObjDto<OpenDto>(false,BizException.COMMON_CUSTOMIZE_ERROR.getCode(),"生效时间大于当前时间");
			}
			Date yearStartDate = DateUtils.getYearStart(new Date());
			//生效时间本年且小于活动时间,设置生效时间等于活动时间
			if(effectiveDate.getTime() > yearStartDate.getTime() && effectiveDate.getTime() < activity.getDateFrom().getTime()) {
				effectiveDate = activity.getDateFrom();
			}
			if(effectiveDate.getTime() > activity.getDateTo().getTime()) {
				return new StatusObjDto<OpenDto>(false,BizException.COMMON_CUSTOMIZE_ERROR.getCode(),"生效时间不在活动时间");
			}
			Date recieveEndTime = DateUtils.addDay(effectiveDate, RECIEVE_ALLOW_DAYS);
			recieveEndTime = DateUtils.getDayEnd(recieveEndTime);
			if(effectiveDate.getTime() < activity.getDateFrom().getTime() || now.getTime() > recieveEndTime.getTime()) {
				openDto.setRecieveStatus(OpenDto.RECIEVE_EXPIRE);
				openDto.setActiveStatus(OpenDto.STATUS_NOT_ACTIVE);
				return new StatusObjDto<OpenDto>(true,openDto,StatusDto.SUCCESS,"");
			}
			//开通Level2
			Long id = addRecieve(userId,mobile,Level2Recieve.STATUS_NOT_OPEN,type,prize.getGoodsId(),null,null);
			String reason = type.equals(Level2RecievePrize.TYPE_NEW_OPEN)?"Level2领取活动新开户":
				type.equals(Level2RecievePrize.TYPE_NEW_EFFECTIVE)?"Level2领取活动新增有效户":
				type.equals(Level2RecievePrize.TYPE_NEW_CREDIT)?"Level2领取活动两融新开户":"";
			StatusObjDto<Level2OpenDto> openStatus = level2Biz.openLevel2(prize.getGoodsId(), mobile, LEVEL2_PAY_MODE,reason, null);
			logger.error(mobile+" level2 open result ："+ JsonUtils.object2JSON(openStatus));
			if(!openStatus.isOk()) {
				return new StatusObjDto<OpenDto>(false,BizException.COMMON_CUSTOMIZE_ERROR.getCode(),"权限开通失败");
			}
			//邀请人更新开通信息
			updateRecieve(id,Level2Recieve.STATUS_OPEN_SUCCESS,openStatus.getObj(),null,"");
			
			openDto.setRecieveStatus(OpenDto.RECIEVE_SUCCESS);
			openDto.setActiveStatus(OpenDto.STATUS_ACTIVE);
			
			//熔断处理
//			Thread breakerT = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					saveParticipate(ACTIVITY_CODE,userId,null,"activity.l2recieve.recieve",ip,now);
//					breakerHandle(ACTIVITY_CODE);
//				}
//			});
//			breakerT.start();
			
			return new StatusObjDto<OpenDto>(true,openDto,StatusDto.SUCCESS,"");
		}else {//客户不存在处理
			addRecieve(userId,mobile,Level2Recieve.STATUS_NOT_ACTIVE,type,prize.getGoodsId(),null,null);
			openDto.setRecieveStatus(OpenDto.RECIEVE_SUCCESS);
			openDto.setActiveStatus(OpenDto.STATUS_NOT_ACTIVE);
			return  new StatusObjDto<OpenDto>(true,openDto,StatusDto.SUCCESS,"");
		}
	}
	
	@Override
	public StatusDto sendCheckCode(String mobile) {
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		if(!RegxUtils.isMobile(mobile)) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("mobile");
		}
		return checkCodeBiz.sendCheckcode(SMS_CHECKCODE_TEMPLATE, CheckcodeTypeE.ACTIVITY_LEVEL2_RECIEVE_INVITE, mobile, "");
	}
	
	@Transactional
	@Override
	public StatusDto acceptInvitation(String mobile, String checkCode, String shareCode) {
		StatusDto isValid =  isValid(ACTIVITY_CODE);
		if(!isValid.isOk()) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format(isValid.getMsg());
		}
		if(ObjectUtils.isEmptyOrNull(mobile)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("mobile");
		}
		if(!RegxUtils.isMobile(mobile)) {
			throw BizException.COMMON_PARAMS_IS_ILLICIT.format("mobile");
		}
		if(ObjectUtils.isEmptyOrNull(checkCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("checkCode");
		}
		if(ObjectUtils.isEmptyOrNull(shareCode)) {
			throw BizException.COMMON_PARAMS_NOT_NULL.format("shareCode");
		}
		
		//查询邀请用户
		AccUser user = userBiz.findByShareCode(shareCode);
		if(user == null) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("邀请用户不存在");
		}
		
		StatusDto verifyStatus = checkCodeBiz.verifyCheckcode(CheckcodeTypeE.ACTIVITY_LEVEL2_RECIEVE_INVITE, mobile, checkCode, "");
		if(!verifyStatus.isOk()) {
			return new StatusObjDto<InviteDto>(false,verifyStatus.getCode(),verifyStatus.getMsg());
		}
		
		//检查邀请是否有效
		checkInvite(user.getUserId(),user.getMobile(),mobile);
		
		//添加邀请记录
		Level2RecieveInvite invite = new Level2RecieveInvite();
		invite.setUserId(user.getUserId());
		invite.setUserMobile(user.getMobile());
		invite.setInviteMobile(mobile);
		invite.setCreateTime(new Date());
		invite.setStatus(Level2RecieveInvite.STATUS_NO_USER);
		inviteDao.insert(invite);
		//缓存领取状态
		L2RecieveRedis.INVITE_RECIEVE_STATUS.set(invite.getInviteMobile(), CodeConstant.CODE_NO);
		L2RecieveRedis.INVITE_RECIEVE_STATUS.set(invite.getUserMobile(), CodeConstant.CODE_NO);
		return new StatusDto(true,StatusDto.SUCCESS,"");
	}
	
	public void openInvite() {
		Thread openT = new Thread(new Runnable() {
			@Override
			public void run() {
				openLevel2ForInvite();
			}
		});
		openT.start();
	}
	

	@Override
	public void openNotActive() {
		Thread openT = new Thread(new Runnable() {
			@Override
			public void run() {
				openLevel2ForNotActive();
			}
		});
		openT.start();
	}

	@Override
	public StatusObjDto<Integer> hasRecieve(String userId,String mobile) {
		Integer reSuccess = CodeConstant.CODE_NO;
		Integer recieveStatus = (Integer) L2RecieveRedis.RECIEVE_STATUS.get(mobile);
		if(ObjectUtils.isNotEmptyOrNull(recieveStatus) &&  recieveStatus.equals((CodeConstant.CODE_NO))) {
			//开通Level2
		    for(int type = 1; type < 4 ;type++) {
				//新开户，新增有效户，新开信用账户领取Level2
				StatusObjDto<OpenDto> rStatus = recieve(type, userId,  mobile, "");
				if(rStatus.isOk() && rStatus.getObj().getRecieveStatus().equals(OpenDto.RECIEVE_SUCCESS) && rStatus.getObj().getActiveStatus().equals(CodeConstant.CODE_YES)) {
					reSuccess = CodeConstant.CODE_YES;
					L2RecieveRedis.RECIEVE_STATUS.del(mobile);
				}
			}
		}
		//邀请用户开通Level2
		Integer inviteRecieveStatus = (Integer) L2RecieveRedis.INVITE_RECIEVE_STATUS.get(mobile);
		if(ObjectUtils.isNotEmptyOrNull(inviteRecieveStatus) &&  inviteRecieveStatus.equals(CodeConstant.CODE_NO)) {
			//开通Level2
			reSuccess = openLevel2ForInvite(mobile);
			if(reSuccess.equals(CodeConstant.CODE_YES)) {
				L2RecieveRedis.INVITE_RECIEVE_STATUS.del(mobile);
			}
		}
	    reSuccess =CodeConstant.CODE_YES.equals(inviteRecieveStatus) ?CodeConstant.CODE_YES:reSuccess;
		return new StatusObjDto<Integer>(true, reSuccess,StatusObjDto.SUCCESS, "");
	}

	@Override
	public StatusDto cacheRecieveStatus() {
		//查询生效时间在30天内的可领取Level2的手机号进行缓存
		Level2RecieveUser user = new Level2RecieveUser();
		Date endDate = DateUtils.getDayEnd(new Date());
		Date beginDate =DateUtils.getDayStart(DateUtils.addDay(endDate, -RECIEVE_ALLOW_DAYS));
		user.setEffectiveDateBegin(beginDate);
		user.setEffectiveDateEnd(endDate);
		List<Level2RecieveUser>  users = customerDao.findValidList(user);
		for(Level2RecieveUser u:users) {
			L2RecieveRedis.RECIEVE_STATUS.set(u.getMobile(), CodeConstant.CODE_NO);
		}
		//查询邀请领取Level2的手机号进行缓存
		int pageSize = 10,pageIndex=1;
		while(true) {
			int[] page = buildPage(pageIndex,pageSize);
			List<Level2RecieveInvite> invites = inviteDao.findNotOpen(null,page[0],page[1]); 
			for(Level2RecieveInvite invite:invites) {
				L2RecieveRedis.INVITE_RECIEVE_STATUS.set(invite.getInviteMobile(), CodeConstant.CODE_NO);
				L2RecieveRedis.INVITE_RECIEVE_STATUS.set(invite.getUserMobile(), CodeConstant.CODE_NO);
			}
			if(invites.size() < pageSize) {
				break;
			}
			pageIndex++;
		}
		return new StatusDto(true);
	}

	private void openLevel2ForInvite() {
		logger.info("领取活动邀请好友Level2批量开通开始处理...........................");
		Activity activity = findActivity(ACTIVITY_CODE);
		Activity copyAct = new Activity();
		BeanUtils.copyToBean(activity, copyAct);
		Date recieveEndDate = DateUtils.addDay(copyAct.getDateTo(), 1);
		recieveEndDate = DateUtils.getDayEnd(recieveEndDate);
		copyAct.setDateTo(recieveEndDate); //批量开通处理可延期一天
		StatusDto isValid =  isValid(copyAct);
		if(!isValid.isOk()) {
			logger.info(isValid.getMsg()+".........................................");
			return;
		}
		//获取邀请好友Level2商品
		Level2RecievePrize prize = prizeDao.findPrize(Level2RecievePrize.TYPE_INVITE);
		if(prize == null) {
			logger.info("奖品未配置................................................");
		}
		int pageSize = 10,pageIndex=1;
		while(true) {
			int[] page = buildPage(pageIndex,pageSize);
			List<Level2RecieveInvite> invites = inviteDao.findNotOpen(null,page[0],page[1]); 
			for(Level2RecieveInvite invite:invites) {
				AppUser appUser = getAppUser(invite.getInviteMobile());
				if(appUser == null) { //添加备注
					invite.setStatus(Level2RecieveInvite.STATUS_NO_USER);
					invite.setRemark("app新用户不存在");
					inviteDao.update(invite);
					continue;
				}
				if(appUser.getRegistTime().getTime() < invite.getCreateTime().getTime()) {
					invite.setStatus(Level2RecieveInvite.STATUS_REGIST_BEFORE);
					invite.setRemark("注册时间"+DateUtils.formate(appUser.getRegistTime(),"yyyy-MM-dd HH:mm:ss")+"早于邀请时间");
					inviteDao.update(invite);
					continue;
				}
				//添加邀请人领取记录
				Long id = addRecieve(invite.getUserId(),invite.getUserMobile(),Level2Recieve.STATUS_NOT_OPEN,Level2RecievePrize.TYPE_INVITE,prize.getGoodsId(),
						invite.getId(),"1");
				//添加被邀请人领取记录
				Long beId = addRecieve(null,invite.getInviteMobile(),Level2Recieve.STATUS_NOT_OPEN,Level2RecievePrize.TYPE_INVITE,prize.getGoodsId(),
						invite.getId(),"2");
				//邀请人开通level2
				StatusObjDto<Level2OpenDto> openStatus = level2Biz.openLevel2(prize.getGoodsId(), invite.getUserMobile(), LEVEL2_PAY_MODE, "Level2领取活动邀请好友", null);
				if(!openStatus.isOk()) {
					throw BizException.COMMON_CUSTOMIZE_ERROR.format("权限开通失败");
				}
				//邀请人更新开通信息
				updateRecieve(id,1,openStatus.getObj(),null,"");
				//被邀请人开通level2
				StatusObjDto<Level2OpenDto> beOpenStatus = level2Biz.openLevel2(prize.getGoodsId(), invite.getInviteMobile(), LEVEL2_PAY_MODE, "Level2领取活动被邀请好友", null);
				if(!beOpenStatus.isOk()) {
					throw BizException.COMMON_CUSTOMIZE_ERROR.format("权限开通失败");
				}
				//被邀请人更新开通信息
				updateRecieve(beId,1,beOpenStatus.getObj(),null,"");
				
				invite.setStatus(Level2RecieveInvite.STATUS_OPEN_SUCCESS);
				invite.setRemark("开通成功");
				inviteDao.update(invite);
			}
			if(invites.size() < pageSize) {
				break;
			}
			pageIndex++;
		}
	}

	/**
	 * 被邀请人开通
	 * @param inviteMobile 被邀请人开通Level2，同时邀请人开通Level2
	 */
	private Integer openLevel2ForInvite(String inviteMobile) {
		logger.info("领取活动邀请好友Level2开通开始处理..........................."+inviteMobile);
		Activity activity = findActivity(ACTIVITY_CODE);
		Activity copyAct = new Activity();
		BeanUtils.copyToBean(activity, copyAct);
		Date recieveEndDate = DateUtils.addDay(copyAct.getDateTo(), 1);
		recieveEndDate = DateUtils.getDayEnd(recieveEndDate);
		copyAct.setDateTo(recieveEndDate); //批量开通处理可延期一天
		StatusDto isValid =  isValid(copyAct);
		if(!isValid.isOk()) {
			logger.info(isValid.getMsg()+".........................................");
			return CodeConstant.CODE_NO;
		}
		//获取邀请好友Level2商品
		Level2RecievePrize prize = prizeDao.findPrize(Level2RecievePrize.TYPE_INVITE);
		if(prize == null) {
			logger.info("奖品未配置................................................");
			return CodeConstant.CODE_NO; 
		}
		List<Level2RecieveInvite> invites = inviteDao.findNotOpen(inviteMobile,0,10); 
		if(invites.size() == 0) return  CodeConstant.CODE_NO;
		Level2RecieveInvite invite = invites.get(0);
		AppUser appUser = getAppUser(invite.getInviteMobile());
		if(appUser == null) { //添加备注
			invite.setStatus(Level2RecieveInvite.STATUS_NO_USER);
			invite.setRemark("app新用户不存在");
			inviteDao.update(invite);
			return CodeConstant.CODE_NO;
		}
		if(appUser.getRegistTime().getTime() < invite.getCreateTime().getTime()) {
			invite.setStatus(Level2RecieveInvite.STATUS_REGIST_BEFORE);
			invite.setRemark("注册时间"+DateUtils.formate(appUser.getRegistTime(),"yyyy-MM-dd HH:mm:ss")+"早于邀请时间");
			inviteDao.update(invite);
			return CodeConstant.CODE_NO;
		}
		//添加邀请人领取记录
		Long id = addRecieve(invite.getUserId(),invite.getUserMobile(),Level2Recieve.STATUS_NOT_OPEN,Level2RecievePrize.TYPE_INVITE,prize.getGoodsId(),
				invite.getId(),"1");
		//添加被邀请人领取记录
		Long beId = addRecieve(null,invite.getInviteMobile(),Level2Recieve.STATUS_NOT_OPEN,Level2RecievePrize.TYPE_INVITE,prize.getGoodsId(),
				invite.getId(),"2");
		//邀请人开通level2
		StatusObjDto<Level2OpenDto> openStatus = level2Biz.openLevel2(prize.getGoodsId(), invite.getUserMobile(), LEVEL2_PAY_MODE, "Level2领取活动邀请好友", null);
		if(!openStatus.isOk()) {
			logger.error("权限开通失败");
		}
		//邀请人更新开通信息
		updateRecieve(id,1,openStatus.getObj(),null,"");
		//被邀请人开通level2
		StatusObjDto<Level2OpenDto> beOpenStatus = level2Biz.openLevel2(prize.getGoodsId(), invite.getInviteMobile(), LEVEL2_PAY_MODE, "Level2领取活动被邀请好友", null);
		if(!beOpenStatus.isOk()) {
			logger.error("权限开通失败");
			return CodeConstant.CODE_NO;
		}
		//被邀请人更新开通信息
		updateRecieve(beId,1,beOpenStatus.getObj(),null,"");
		
		invite.setStatus(Level2RecieveInvite.STATUS_OPEN_SUCCESS);
		invite.setRemark("开通成功");
		inviteDao.update(invite);
		
		return CodeConstant.CODE_YES;
	}
	
	private void openLevel2ForNotActive() {
		logger.info("领取活动未开户状态Level2批量开通开始处理...........................");
		Activity activity = findActivity(ACTIVITY_CODE);
		Activity copyAct = new Activity();
		BeanUtils.copyToBean(activity, copyAct);
		Date recieveEndDate = DateUtils.addDay(copyAct.getDateTo(), 1);
		recieveEndDate = DateUtils.getDayEnd(recieveEndDate);
		copyAct.setDateTo(recieveEndDate); //批量开通处理可延期一天
		StatusDto isValid =  isValid(copyAct);
		if(!isValid.isOk()) {
			logger.info(isValid.getMsg()+".........................................");
			return;
		}
		//获取Level2商品
		List<Level2RecievePrize> prizes = prizeDao.findList(null);
		if(prizes.size() == 0) {
			logger.info("奖品未配置................................................");
		}
		Map<Integer,Integer> level2Goods = new HashMap<Integer,Integer>();
		for(Level2RecievePrize prize:prizes) {
			level2Goods.put(prize.getType(), prize.getGoodsId());
		}
		int pageSize = 10,pageIndex=1;
		while(true) {
			int[] page = buildPage(pageIndex,pageSize);
			List<Level2Recieve> recieves = recieveDao.findNotActive(page[0],page[1]); 
			for(Level2Recieve recieve:recieves) {
				Integer type = recieve.getType();
				if(!level2Goods.containsKey(type)){
					logger.info("类型为"+recieve.getType()+"的Level2商品未配置--------------");
					continue;
				}
				String mobile = recieve.getMobile();
				Level2RecieveUser customer = customerDao.findCustomer(type, mobile);
//				if(customer == null) { //兼容旧带*号手机（旧数据）
//					String mobileLike = mobile.replaceAll("(\\d{3})\\d{4}(\\d{4})","$1****$2");
//					customer = customerDao.findCustomer(type, mobileLike);
//				}
				if(customer == null) {
					recieve.setRemark("自动生效客户不存在");
					recieve.setActiveTime(new Date());
					recieveDao.update(recieve);
					continue;
				}
				
				Date effectiveDate = customer.getEffectiveDate();
				Date yearStartDate = DateUtils.getYearStart(new Date());
				//生效时间本年且小于活动时间,设置生效时间等于活动时间
				if(effectiveDate.getTime() > yearStartDate.getTime() && effectiveDate.getTime() < activity.getDateFrom().getTime()) {
					effectiveDate = activity.getDateFrom();
				}
				if(effectiveDate.getTime() < activity.getDateFrom().getTime() || 
						effectiveDate.getTime() > activity.getDateTo().getTime()) {
					recieve.setRemark("账户生效时间"+DateUtils.formate(effectiveDate, "yyyy-MM-dd HH:mm:ss")+"不在活动时间");
					recieve.setActiveTime(new Date());
					recieve.setStatus(Level2Recieve.STATUS_NOT_VALID);
					recieveDao.update(recieve);
					continue;
				}
				
				Date recieveEndTime = DateUtils.addDay(effectiveDate, RECIEVE_ALLOW_DAYS);
				recieveEndTime = DateUtils.getDayEnd(recieveEndTime);
				Date now = new Date();
				if(now.getTime() > recieveEndTime.getTime()) {
					recieve.setRemark("账户生效时间为"+DateUtils.formate(effectiveDate, "yyyy-MM-dd HH:mm:ss")+"，超过领取有效期");
					recieve.setActiveTime(new Date());
					recieve.setStatus(Level2Recieve.STATUS_NOT_VALID);
					recieveDao.update(recieve);
					continue;
				}
				
				//开通level2
				String reason = type.equals(Level2RecievePrize.TYPE_NEW_OPEN)?"Level2领取活动新开户":
					type.equals(Level2RecievePrize.TYPE_NEW_EFFECTIVE)?"Level2领取活动新增有效户":
					type.equals(Level2RecievePrize.TYPE_NEW_CREDIT)?"Level2领取活动两融新开户":"";
				StatusObjDto<Level2OpenDto> openStatus = level2Biz.openLevel2(level2Goods.get(type), mobile, LEVEL2_PAY_MODE, reason, null);
				
				if(!openStatus.isOk()) {
					updateRecieve(recieve.getId(),Level2Recieve.STATUS_NOT_ACTIVE,openStatus.getObj(),new Date(),"自动生效失败");
					continue;
				}
				//邀请人更新开通信息
				updateRecieve(recieve.getId(),Level2Recieve.STATUS_OPEN_SUCCESS,openStatus.getObj(),new Date(),"自动生效");
			}
			if(recieves.size() < pageSize) {
				break;
			}
			pageIndex++;
		}
	}
	
	private AppUser getAppUser(String mobile) {
		Map<String,Object> busparams = new HashMap<String,Object>();
		busparams.put("mobile", mobile);
		ResultDto rd = RemoteUtils.call("wechatsd.appuser.getbymobile",ApiServiceTypeEnum.COOPERATION, busparams,true);
		if(!rd.getCode().equals(ResultDto.SUCCESS)) {
			return null;
		}
		
		AppUser appUser = BeanUtils.mapToBean(rd.getData(), AppUser.class);
		return appUser;
	}

	private AppUser getAppUser(String userId,String mobile) {
		if(ObjectUtils.isEmptyOrNull(mobile)) return null;
		AccUser user = userBiz.findByMobile(mobile);
		AppUser appUser = new AppUser();
		appUser.setMobile(user.getMobile());
		appUser.setRegistTime(user.getCreateTime());
		return appUser;
	}
	
	/**
	 * 添加领取记录
	 * @param userId
	 * @param mobile
	 * @param status
	 * @param type
	 * @param goodsId
	 * @param inviteId
	 * @param openDto
	 */
	private Long addRecieve(String userId,String mobile,Integer status,Integer type,Integer goodsId,
			Long inviteId,String remark) {
		Level2Recieve recieve = new Level2Recieve();
		recieve.setUserId(userId);
		recieve.setMobile(mobile);
		recieve.setGoodsId(goodsId);
		recieve.setType(type);
		recieve.setInviteId(inviteId);
		recieve.setStatus(status);
		recieve.setCreateTime(new Date());
		recieve.setRemark(remark);
		recieveDao.insert(recieve);
		
		return recieve.getId();
	}

	/**
	 * 更新领取记录
	 * @param id
	 * @param status
	 * @param openDto
	 * @param activeTime
	 * @param remark
	 */
	private void updateRecieve(Long id,Integer status,Level2OpenDto openDto,Date activeTime,String remark) {
		Level2Recieve recieve = new Level2Recieve();
		if(openDto != null) {
			recieve.setOrderNo(openDto.getOrderNo());
			recieve.setNotityMessage(openDto.getMessage());
			recieve.setNotityStatus(openDto.getStatus());
			recieve.setTakeTime(new Date());
		}
		recieve.setId(id);
		recieve.setStatus(status);
		recieve.setActiveTime(activeTime);
		recieve.setRemark(remark);
		recieveDao.update(recieve);
	}
	
	/**
	 * 检查邀请是否有效
	 * @param userId 邀请人userid
	 * @param mobile 邀请人手机号
	 * @param inviteMobile 被邀请人手机号
	 */
	private void checkInvite(String userId,String mobile,String inviteMobile) {
		if(mobile.equals(inviteMobile)) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("不能邀请自己");
		}
		//查询是否有邀请记录
		Level2RecieveInvite invite = new Level2RecieveInvite();
		invite.setInviteMobile(inviteMobile);
		List<Level2RecieveInvite> invitesExist = inviteDao.findList(invite);
		if(invitesExist.size() > 0) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("您已被邀请过");
		}
		//查询是否重复邀请
		invite.setUserId(userId);
		invite.setInviteMobile(inviteMobile);
		List<Level2RecieveInvite> invitesDup = inviteDao.findList(invite);
		if(invitesDup.size() > 0) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("重复邀请");
		}
		
		invite.setUserId(null);
		invite.setUserMobile(inviteMobile);
		invite.setInviteMobile(mobile);
		List<Level2RecieveInvite> invitesEach = inviteDao.findList(invite);
		if(invitesEach.size() > 0) {
			throw BizException.COMMON_CUSTOMIZE_ERROR.format("不能相互邀请");
		}
	}

	private  int[] buildPage(Integer pageIndex, Integer pageSize) {
		pageIndex = pageIndex == null || pageIndex <= 0 ? 1 : pageIndex;
		pageSize = pageSize == null || pageSize <= 0 ? 5 : pageSize;

		int rowStart = (pageIndex - 1) * pageSize;

		int rowEnd = 1;
		if (rowStart == 0) {
			if (pageSize - Integer.MAX_VALUE == 0) {
				rowEnd = pageSize;
			} else {
				rowEnd = pageSize;
			}

		} else {
			rowEnd = rowStart + pageSize;
		}

		return new int[] { rowStart, rowEnd };
	}

}
