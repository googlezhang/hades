package cn.offway.hades.service;


import cn.offway.hades.domain.PhRefund;
import cn.offway.hades.domain.PhRefundOrderGoods;

import java.util.List;

/**
 * 退换货后的订单商品Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-08-06 15:46:21 Exp $
 */
public interface PhRefundOrderGoodsService {

    PhRefundOrderGoods save(PhRefundOrderGoods phRefundOrderGoods);

    PhRefundOrderGoods findOne(Long id);

    void delete(Long id);

    List<PhRefundOrderGoods> save(List<PhRefundOrderGoods> entities);

    void updateByRefund(PhRefund phRefund);
}
