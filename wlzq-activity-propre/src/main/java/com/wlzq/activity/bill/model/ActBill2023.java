package com.wlzq.activity.bill.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 2023年度账单
 *
 * @author renjiyun
 */
@Data
@Accessors(chain = true)
public class ActBill2023 {
    /** 客户号 */
    private String clientId;
    /** 0个人, 1机构, 2自营, 6机构（机构端）, 3产品, 5产品（机构端） */
    private String organFlag;


    /** 客户2023年登录万联e万通自然天数 */
    private String loginDays;
    /** 客户2023年登录天数打败其他客户百分比 */
    private String loginOutperformRatio;


    /** 客户2023年资产峰值 */
    private String maxAsset;
    /** 客户2023年日均资产 */
    private String avgAsset;
    /** 客户2023年日均股票市值 */
    private String avgGpMkt;
    /** 客户2023年日均理财市值 */
    private String avgFinMkt;
    /** 客户2023年日均债券市值 */
    private String avgZqMkt;
    /** 客户2023年日均现金资产 */
    private String avgFund;
    /** 客户2023年日均其他市值 */
    private String avgQtMkt;


    /** 客户2023年总收益 */
    private String sumProfit;
    /** 客户2023年收益跑赢其他客户百分比 */
    private String profitOutperformRatio;


    /** 客户2023年最大收益类别 */
    private String maxProfitScrType;
    /** 客户2023年收益最高日期 */
    private String maxProfitDt;
    /** 客户2023年最高收益额 */
    private String maxProfit;


    /** 客户2023年股票总收益 */
    private String sumProfitGp;
    /** 客户2023年股票收益最高月份 */
    private String maxProfitYmDt;
    /** 客户2023年月收益最高额 */
    private String maxProfitYm;


    /** 客户2023年持有股票数 */
    private String holderGpQ;
    /** 客户2023年收益最高股票名称 */
    private String maxProfitGpName;
    /** 客户2023年单只股票最高收益额 */
    private String maxProfitGp;
    /** 客户2023年收益最低股票名称 */
    private String minProfitGpName;
    /** 客户2023年单只股票收益最低额 */
    private String minProfitGp;

    /** 客户2023年证券交易次数 */
    private String trdCnt;
    /** 2023年万联客户证券平均交易次数 */
    private String trdCntAvgWl;
    /** 客户2023年交易次数前三的证券名称 */
    private String topThreeTrdCntScrName;

    /** 客户2023年参加打新次数 */
    private String ipoEntrustTimes;
    /** 客户2023年中签次数 */
    private String ipoScrpTimes;
    /** 客户2023年中签率领先其他客户百分比 */
    private String ipoOutperformRatio;


    /** 客户2023年理财收益总额 */
    private String sumFinProfit;
    /** 客户2023年持有理财产品数 */
    private String holderFinQ;
    /** 客户2023年最高收益理财产品名称 */
    private String maxProfitFinName;
    /** 客户2023年单只理财产品最高收益额 */
    private String maxProfitFin;
}
