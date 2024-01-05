package com.wlzq.activity.bill.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.wlzq.common.serializer.Date2LongSerializer;

import lombok.Data;

/**
 * 2022年度账单
 * @author jjw
 *
 */
@Data
public class ActBill {
	private String clientId;		// 客户号
	private String organFlag;		// 0个人,1机构,2自营,6机构（机构端）,3产品,5产品（机构端）
	private Long loginDays;		// 客户开户手机号-2022年e万通登录天数
	private String loginEarliest;		// 客户开户手机号-2022年最早登录e万通时间格式：yyyy-MM-dd HH:mm:ss
	private String loginLatest;		// 客户开户手机号-2022年最晚登录e万通时间格式：yyyy-MM-dd HH:mm:ss
	private Long stockEtfOrderCnt;		// 非交易时间下单股票和ETF基金次数
	private Long ofundOrderCnt;		// 在理财商城下委托单买场外基金次数
	private Long condStockOrderCnt;	//条件单委托股票次数
	private Long selfStkUp100Cnt;		// 自选股中包含市场全年涨幅前100的股票数量
	private String selfStkUp100List;		// 自选股中包含市场全年涨幅前100的股票列表格式:code:name,code:name...
	private Long selfStkDr100Cnt;		// 自选股中包含市场全年分红前100的股票数量
	private String selfStkDr100List;		// 自选股中包含市场全年分红前100的股票列表格式:code:name,code:name...
	private Long selfStkSurged100Cnt;		// 自选股中包含市场全年涨停次数前100的股票数量
	private String selfStkSurged100List;		// 自选股中包含市场全年涨停次数前100的股票列表格式:code:name,code:name...
	private Long selfStockCnt;	//自选股数量
	private String selfMaxUpStockCode;	//全年涨幅最大自选股股票代码
	private String selfMaxUpStockName;	//全年涨幅最大自选股股票名称
	private Double selfMaxUpStockChangePct;	//全年涨幅最大自选股的年涨幅
	private Long useLv2Day;		// 客户2022年使用增强lv2的月份数
	private Long useCyjjDay;		// 客户2022年使用产业掘金的月份数
	private Long useZtzsDay;		// 客户2022年使用涨停助手的月份数
	private String useAuxiliarytoolsList;		// 客户2022年使用辅助工具列表
	private String invProductCnt;		// 客户2022年使用投顾产品数
	private Long visitIntPageDay;		// 客户2022年访问投顾社区天数
	private Double avgHoldPct;		// 客户2022年日均持仓仓位
	private Double topHoldPct;		// 客户2022年日均持仓仓位排名百分比
	private Long optCrdtPerm;	//客户是否满足融资融券条件
	private Long notuseFundDay;		// 客户2022闲置资金天数
	private Double notuseFundAvgbalance;		// 客户2022年闲置资金日均金额
	private Double notuseFundGzhgprofit;	//客户2022年闲置资金投资国债逆回购收益
	private Long notuseFundDayif30;	//客户2022年闲置资金天数排名（天数最大排第一）
	private Double profitYtd;		// 客户2022年年度收益
	private Long upTop500StkCnt;		// 客户2022年持仓股进入AB股涨幅排名前500名的股票数
	private Double actProfitYtdRankPct;	//客户2022年年度收益排名百分比(不含收益为0客户)
	private Long maxProfitMonth;		// 客户2022年收益最大月份
	private Double maxProfitM;		// 客户2022年收益最大月份的月收益
	private String maxProfitSecucode;		// 客户2022年持有个股收益最大的股票代码
	private String maxProfitSecuname;		// 客户2022年持有个股收益最大的股票名称
	private Double maxProfitP;		// 客户2022年持有个股收益最大的股票收益
	private Long holdStockSurgedDays;		// 客户2022年持仓股的涨停天数
	private String surgedStockList;		// 客户2022年持仓股涨停的股票描述格式:code:name,code:name...
	private Long ipoEntrustTimes;		// 客户2022年参与打新次数
	private Long ipoStockPTimes;		// 客户2022年新股中签股票次数
	private Long ipoBondPTimes;		// 客户2022年新股中签债权次数
	private Double ipoHoldProfitYtd;		// 客户2022年新股中签的累计收益
	private Long fundTrdCnt;		// 客户2022年购买场外基金的数量
	private Double fundTrdProfit;		// 客户2022年购买场外基金的收益（含持仓和赎回）
	private Long finProdTrdCntYtd;		// 客户2022年购买定期理财订单次数
	private Double finProdProfitYtd;		// 客户2022年购买定期理财收益
	private Long hasActiveXklcCoupon;		// 客户是否有可用的新客理财券0:是，1：否
	private Integer isShare;		// 是否分享 1：是 0：否
	@JsonSerialize(using=Date2LongSerializer.class)
	private Date updateTime;		// 更新时间
}
