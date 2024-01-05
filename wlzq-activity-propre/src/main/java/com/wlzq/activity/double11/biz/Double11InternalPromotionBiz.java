package com.wlzq.activity.double11.biz;

import com.wlzq.activity.double11.dto.BranchRankingDto;
import com.wlzq.activity.double11.dto.MyRankingInfoDto;
import com.wlzq.activity.double11.dto.SaleRankingDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.core.Page;
import com.wlzq.core.dto.StatusObjDto;

import java.util.List;

/**
 * 双11活动内部推动业务接口
 *
 * @author zhujt
 */
public interface Double11InternalPromotionBiz {

    /**
     * 一马当先奖
     *
     * @param staffNo
     * @return
     */
    StatusObjDto<List<SaleRankingDto>> saleRanking(String staffNo);

    /**
     * 巅峰登顶奖
     *
     * @param staffNo
     * @return
     */
    StatusObjDto<List<BranchRankingDto>> branchRanking(String staffNo);

    /**
     * 我的排名情况
     *
     * @param user
     * @param page
     * @return
     */
    StatusObjDto<MyRankingInfoDto> myRankingInfo(AccTokenUser user, Page page);
}
