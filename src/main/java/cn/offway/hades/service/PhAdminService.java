package cn.offway.hades.service;

import cn.offway.hades.domain.PhAdmin;

/**
 * Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2018-10-15 16:49:00 Exp $
 */
public interface PhAdminService{

	PhAdmin save(PhAdmin phAdmin);
	
	PhAdmin findOne(Long id);
}
