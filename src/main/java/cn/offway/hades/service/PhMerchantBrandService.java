package cn.offway.hades.service;

import cn.offway.hades.domain.PhBrand;
import cn.offway.hades.domain.PhMerchant;
import cn.offway.hades.domain.PhMerchantBrand;

import java.util.List;

/**
 * 商户品牌关系Service接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
public interface PhMerchantBrandService {

    PhMerchantBrand save(PhMerchantBrand phMerchantBrand);

    PhMerchantBrand findOne(Long id);

    List<PhMerchantBrand> findByPid(Long pid);

    List<PhMerchantBrand> findByBid(Long bid);

    void del(Long id);

    void delByPid(Long pid);

    void updateMerchantInfo(PhMerchant merchant);

    void updateBrandInfo(PhBrand brand);
}
