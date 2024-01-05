
package com.wlzq.activity.virtualfin.service.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wlzq.activity.ActivityBizException;
import com.wlzq.activity.ActivityConstant;
import com.wlzq.activity.virtualfin.biz.*;
import com.wlzq.activity.virtualfin.dto.ActGoodsFlowDto;
import com.wlzq.activity.virtualfin.dto.ActRedEnvelopeDto;
import com.wlzq.activity.virtualfin.dto.ExpGoldOverviewDto;
import com.wlzq.activity.virtualfin.dto.LastAmountFlowResDto;
import com.wlzq.activity.virtualfin.model.*;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.common.utils.BeanUtils;
import com.wlzq.core.BaseService;
import com.wlzq.core.Page;
import com.wlzq.core.RequestParams;
import com.wlzq.core.annotation.ApiServiceType;
import com.wlzq.core.annotation.ApiServiceTypeEnum;
import com.wlzq.core.annotation.MustLogin;
import com.wlzq.core.annotation.Signature;
import com.wlzq.core.dto.ResultDto;
import com.wlzq.core.dto.StatusObjDto;
import com.wlzq.remote.service.utils.RemoteUtils;
import com.wlzq.service.base.sys.utils.AppConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.wlzq.activity.ActivityBizException.ACT_EXCEPTION;

/**
 * 体验金服务类
 * @author 
 * @version 1.0
 */
@Service("activity.virtualfin")
@ApiServiceType({ApiServiceTypeEnum.APP,ApiServiceTypeEnum.COOPERATION})
public class VirtualFinService extends BaseService{
	
	@Autowired
	private ExpGoldBiz expGoldBiz;
	@Autowired
	private ActGoodsFlowBiz goodsFlowBiz;
	@Autowired
	private ActFinOrderBiz finOrderBiz;
	@Autowired
	private ActRedEnvelopeBiz redEnvelopeBiz;
	@Autowired
	private ActTaskExpGoldBiz actTaskExpGoldBiz;
	@Autowired
	private ActFinProductBiz productBiz;
 
    @Signature(true)
	public ResultDto expgoldoverview(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String goodsCode = params.getString("goodsCode");
    	String taskCodes = params.getString("taskCodes");
    	String productCodes = params.getString("productCodes");
    	String mobile = user != null ? user.getMobile() : null;
   		StatusObjDto<ExpGoldOverviewDto> result = expGoldBiz.overview(activityCode, mobile, goodsCode, taskCodes, productCodes);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0,BeanUtils.beanToMap(result.getObj()),"");
	}
  
    /**
     * 订单列表
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
	public ResultDto orders(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String mobile = user != null ? user.getMobile() : null;
   		StatusObjDto<List<ActFinOrder>> result = finOrderBiz.orders(activityCode, mobile);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
	}
    
    /**
     * 体验金流水
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
	public ResultDto goldflow(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String goodsCode = params.getString("goodsCode");
    	String mobile = user != null ? user.getMobile() : null;
    	Page page = buildPageNew(params);
   		StatusObjDto<ActGoodsFlowDto> result = goodsFlowBiz.goodsFlow(activityCode, mobile, goodsCode, page);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}
    
    /**
     * 红包流水
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
	public ResultDto redenvelopeflow(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String mobile = user != null ? user.getMobile() : null;
    	Page page = buildPageNew(params);
   		StatusObjDto<ActRedEnvelopeDto> result = redEnvelopeBiz.redEnvelopeFlow(activityCode, mobile, page);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}
    
    /**
     * 完成任务
     * @param params
     * @param user
     * @return
     */
    @Signature(true)
    @MustLogin
	public ResultDto dotask(RequestParams params,AccTokenUser user,Customer customer) throws InterruptedException {
    	String activityCode = params.getString("activityCode");
    	String taskCode = params.getString("taskCode");
		if (ActivityConstant.TASK_ACT_21818_TJD.equals(taskCode)
				|| ActivityConstant.TASK_ACT_21818_LEVEL2_BUY.equals(taskCode)
				|| ActivityConstant.TASK_ACT_21818_LOOK_ARTICLE.equals(taskCode)) {
			Map<String,Object> bizMap = new HashMap<>();
			bizMap.put("userId",user.getUserId());
			bizMap.put("mobile",user.getMobile());
			bizMap.put("source",10001);
			bizMap.put("remark","dotask异常");
			RemoteUtils.call("account.blacklistcooperation.create", ApiServiceTypeEnum.COOPERATION, bizMap, true);
		}
    	String userId = user != null ? user.getUserId() : null;
    	String openId = user != null ? user.getOpenid() : null;
    	String customerId = customer != null ? customer.getCustomerId() : null;
    	String mobile = user != null ? user.getMobile() : null;
   		StatusObjDto<ActTask> result = actTaskExpGoldBiz.doTask(activityCode, mobile, taskCode, userId, openId, customerId,"",null);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
    	return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}
    
    /**
     * 购买产品
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
    @MustLogin
	public ResultDto buyproduct(RequestParams params,AccTokenUser user,Customer customer) {
		checkDeviceType(params, user);

		String activityCode = params.getString("activityCode");
    	String productCode = params.getString("productCode");
    	Double price = params.getDouble("price");
    	String mobile = user != null ? user.getMobile() : null;
    	String userId = user != null ? user.getUserId() : null;
    	String openId = user != null ? user.getOpenid() : null;
    	String customerId = customer != null ? customer.getCustomerId() : null;
   		StatusObjDto<ActFinOrder> result = finOrderBiz.buy(activityCode, mobile, productCode, price, userId, openId, customerId);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
   		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}

	/**
     * 提现红包
     */
    @Signature(true)
    @MustLogin
	public ResultDto withdraw(RequestParams params,AccTokenUser user,Customer customer) {
		checkDeviceType(params, user);
		String withDrawFlag = AppConfigUtils.get(ActivityConstant.ACT_API_WITH_DRAW_FLAG);
		if (StringUtils.equals(withDrawFlag,"0")) {
			throw ACT_EXCEPTION;
		}else {
			String activityCode = params.getString("activityCode");
			Double quantity = params.getDouble("quantity");
			String mobile = user != null ? user.getMobile() : null;
			String userId = user != null ? user.getUserId() : null;
			String openId = user != null ? user.getOpenid() : null;
			String customerId = customer != null ? customer.getCustomerId() : null;
			StatusObjDto<ActRedEnvelope> result = redEnvelopeBiz.withdraw(activityCode, mobile, userId, openId, customerId, quantity);
			if(!result.isOk()) {
				return new ResultDto(result.getCode(),result.getMsg());
			}
			return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
		}
	}

    /**
     * 产品列表
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
	public ResultDto products(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String productCodes = params.getString("productCodes");
   		StatusObjDto<List<ActFinProduct>> result = productBiz.products(activityCode, productCodes);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		Map<String,Object> data = new HashMap<String,Object>();
		data.put("total", result.getObj().size());
		data.put("info", result.getObj());
    	return new ResultDto(0,data,"");
	}

    /**
     * 首次登录
     * @param params
     * @param user
     * @param customer
     * @return
     */
    @Signature(true)
	public ResultDto firstlogin(RequestParams params,AccTokenUser user,Customer customer) {
    	String activityCode = params.getString("activityCode");
    	String mobile = user != null ? user.getMobile() : null;
   		StatusObjDto<ActFirstLogin> result = expGoldBiz.loginStatus(activityCode, mobile);
   		if(!result.isOk()) {
			return new ResultDto(result.getCode(),result.getMsg());
		}
		return new ResultDto(0, BeanUtils.beanToMap(result.getObj()),"");
	}


	/**
	 * 查询最近的红包/体验金流水列表，默认查最近10条
	 * @param params
	 * @param user
	 * @return
	 */
	@Signature(true)
	public ResultDto getlastamountflow(RequestParams params,AccTokenUser user,Customer customer) {
		String activityCode = params.getString("activityCode");
		Page page = buildPageNew(params);
		List<LastAmountFlowResDto> lastAmountFlow = redEnvelopeBiz.getLastAmountFlow(activityCode);
		JSONArray jsonObject = (JSONArray) JSON.toJSON(lastAmountFlow);
		Map<String,Object> data = new HashMap<>();
		data.put("list",jsonObject);
		return new ResultDto(0,data,"");
	}

	/**
	 * 刷新活动任务状态
	 */
	@Signature(true)
	@MustLogin
	public ResultDto flushacttaskstatus(RequestParams params,AccTokenUser user,Customer customer) {
		String activityCode = params.getString("activityCode");
		String mobile = user.getMobile();

		List<ActTask> actTasks = actTaskExpGoldBiz.flushActTaskStatus(activityCode, mobile, null,user, customer);
		JSONArray jsonObject = (JSONArray) JSON.toJSON(actTasks);
		Map<String,Object> data = new HashMap<>();
		data.put("list",jsonObject);
		return new ResultDto(0,data,"");
	}

	private void checkDeviceType(RequestParams params, AccTokenUser user) {
		if(!Objects.equals(params.getDeviceType(),1) && !Objects.equals(params.getDeviceType(),2)){
			Map<String,Object> bizMap = new HashMap<>();
			bizMap.put("userId", user.getUserId());
			bizMap.put("mobile", user.getMobile());
			bizMap.put("source",10001);
			bizMap.put("remark","dt异常");
			RemoteUtils.call("account.blacklistcooperation.create", ApiServiceTypeEnum.COOPERATION, bizMap, true);
			throw ActivityBizException.ACT_ACCOUNT_EXCEP;
		}
	}




}
