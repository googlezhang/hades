package cn.offway.hades.service;


import java.util.List;

import cn.offway.hades.domain.PhChannelUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户推广渠道表Service接口
 *
 * @author tbw
 * @version $v: 1.0.0, $time:2020-01-10 11:33:13 Exp $
 */
public interface PhChannelUserService{

    PhChannelUser save(PhChannelUser phChannelUser);
	
    PhChannelUser findOne(Long id);

    void delete(Long id);

    List<PhChannelUser> save(List<PhChannelUser> entities);

    PhChannelUser findByUserId(Long id);

    Page<PhChannelUser> list(String channelName, String channel, String userId, String userPhone, Pageable pageable);
}
