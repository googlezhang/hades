package cn.offway.hades.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import cn.offway.hades.domain.PhProductInfo;

/**
 * 活动产品表Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2018-10-15 16:49:00 Exp $
 */
public interface PhProductInfoService{

	PhProductInfo save(PhProductInfo phProductInfo);
	
	PhProductInfo findOne(Long id);

	Page<PhProductInfo> findByPage(String name, Pageable page);
}
