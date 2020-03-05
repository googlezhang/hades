package cn.offway.hades.service.impl;

import cn.offway.hades.domain.PhBrandRecommend;
import cn.offway.hades.repository.PhBrandRecommendRepository;
import cn.offway.hades.service.PhBrandRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 品牌推荐表Service接口实现
 *
 * @author tbw
 * @version $v: 1.0.0, $time:2020-03-02 14:23:35 Exp $
 */
@Service
public class PhBrandRecommendServiceImpl implements PhBrandRecommendService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PhBrandRecommendRepository phBrandRecommendRepository;

    @Override
    public PhBrandRecommend save(PhBrandRecommend phBrandRecommend) {
        return phBrandRecommendRepository.save(phBrandRecommend);
    }

    @Override
    public PhBrandRecommend findOne(Long id) {
        return phBrandRecommendRepository.findOne(id);
    }

    @Override
    public void delete(Long id) {
        phBrandRecommendRepository.delete(id);
    }

    @Override
    public Page<PhBrandRecommend> list(Pageable pageable) {
        return phBrandRecommendRepository.findAll(pageable);
    }

    @Override
    public List<PhBrandRecommend> save(List<PhBrandRecommend> entities) {
        return phBrandRecommendRepository.save(entities);
    }
}