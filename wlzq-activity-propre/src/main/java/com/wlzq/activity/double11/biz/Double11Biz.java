package com.wlzq.activity.double11.biz;

import cn.hutool.core.lang.Tuple;
import com.wlzq.activity.double11.dto.QrcodeDto;
import com.wlzq.common.model.account.AccTokenUser;
import com.wlzq.common.model.account.Customer;
import com.wlzq.core.dto.StatusDto;
import com.wlzq.core.dto.StatusObjDto;

import java.util.Map;

/**
 * 双11活动业务接口
 *
 * @author louie
 */
public interface Double11Biz {
    /** 2023年双十一攒积分活动 */
    public static final String ACTIVITY_2023DOUBLE11_ZJF = "ACTIVITY.2023DOUBLE11.ZJF";

    public static final String ACTIVITY_2023DOUBLE11_XTJJDS = "ACTIVITY.2023DOUBLE11.XTJJDS";

    /**
     * 领取level2
     *
     * @param userId
     * @param openId
     * @param mobile
     * @param customerId
     * @param fundAccount
     * @return
     */
    public StatusDto l2receive(String userId, String openId, String mobile, String customerId, String fundAccount);

    /**
     * 获取成就榜单
     *
     * @param achievementDate 榜单日期
     * @param achievementType 榜单类型 1-e万通个人奖励-冲刺奖、2-e万通个人奖励-爆发奖、3-同花顺投顾团队奖励-冲刺奖、4-同花顺投顾团队奖励-爆发奖、
     *                        5-决策销售奖励榜单-推动奖、6-决策销售奖励榜单-传播奖
     * @return
     */
    StatusObjDto<Map<String, Object>> achievement(Long achievementDate, Integer achievementType);

    /**
     * 更新个人奖励榜单
     *
     * @return
     */
    StatusDto updatePersonalAwardRanking();

    /**
     * 更新投顾团队奖励榜单
     *
     * @return
     */
    StatusDto updateThsAdviserTeamAwardRanking();

    /**
     * 更新决策工具销售奖励榜单
     *
     * @return
     */
    StatusDto updateDecisionSaleAwardRanking();

    /**
     * 保存 unionid
     *
     * @param unionId
     * @param openId
     * @param customerId
     * @param mobile
     * @return
     */
    StatusDto saveUnionId(String unionId, String openId, String customerId, String mobile);

    /**
     * 获取二维码
     *
     * @param customerId
     * @return
     */
    QrcodeDto getQrcode(String customerId);

    /**
     * 校验是否添加了企微
     *
     * @param customerId
     * @param unionId
     * @return
     */
    Tuple checkQiWeiAddResult(String customerId, String unionId);

    /**
     * 校验是否为非有效户
     *
     * @param customer
     * @return true-非有效户, false-有效户
     */
    boolean checkNonAccount(Customer customer);

    /**
     * 补填订单推荐人
     *
     * @param user
     * @param outTradeNo
     * @param recommendMobile
     */
    void supplementRecommend(AccTokenUser user, String outTradeNo, String recommendMobile);
}
