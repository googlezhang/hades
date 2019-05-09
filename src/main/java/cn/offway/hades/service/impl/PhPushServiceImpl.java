package cn.offway.hades.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import cn.offway.hades.service.PhPushService;

import cn.offway.hades.domain.PhPush;
import cn.offway.hades.repository.PhPushRepository;


/**
 * 推送记录Service接口实现
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
@Service
public class PhPushServiceImpl implements PhPushService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PhPushRepository phPushRepository;
	
	@Override
	public PhPush save(PhPush phPush){
		return phPushRepository.save(phPush);
	}
	
	@Override
	public PhPush findOne(Long id){
		return phPushRepository.findOne(id);
	}
}
