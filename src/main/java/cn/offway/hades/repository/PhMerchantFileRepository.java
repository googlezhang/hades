package cn.offway.hades.repository;

import cn.offway.hades.domain.PhMerchantFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商户附件表Repository接口
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
public interface PhMerchantFileRepository extends JpaRepository<PhMerchantFile, Long>, JpaSpecificationExecutor<PhMerchantFile> {
    @Transactional
    @Modifying
    @Query(nativeQuery = true, value = "DELETE FROM `ph_merchant_file` WHERE (`merchant_id` = ?1)")
    void deleteByPid(Long pid);
}
