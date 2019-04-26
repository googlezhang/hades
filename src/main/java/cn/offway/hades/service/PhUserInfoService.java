package cn.offway.hades.service;

import cn.offway.hades.domain.PhUserInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 用户信息Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
public interface PhUserInfoService {

    PhUserInfo save(PhUserInfo phUserInfo);

    PhUserInfo findOne(Long id);

    Page<PhUserInfo> list(String phone, String nickname, Pageable pageable);
}