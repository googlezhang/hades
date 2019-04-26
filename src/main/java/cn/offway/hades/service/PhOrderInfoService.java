package cn.offway.hades.service;

import cn.offway.hades.domain.PhOrderInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;
import java.util.List;

/**
 * 订单Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
public interface PhOrderInfoService {

    PhOrderInfo save(PhOrderInfo phOrderInfo);

    PhOrderInfo findOne(Long id);

    Page<PhOrderInfo> findAll(Long mid, String orderNo, String sTime, String eTime, String userId, String payMethod, String status, Pageable pageable);

    List<PhOrderInfo> findToCheck(Date start, Date stop);

    List<PhOrderInfo> findToProcess(Date start, Date stop);
}