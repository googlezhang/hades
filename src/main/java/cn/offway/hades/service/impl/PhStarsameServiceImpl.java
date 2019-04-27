package cn.offway.hades.service.impl;

import cn.offway.hades.domain.PhStarsame;
import cn.offway.hades.repository.PhStarsameRepository;
import cn.offway.hades.service.PhStarsameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;


/**
 * 明星同款Service接口实现
 *
 * @author wn
 * @version $v: 1.0.0, $time:2019-04-04 15:18:00 Exp $
 */
@Service
public class PhStarsameServiceImpl implements PhStarsameService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PhStarsameRepository phStarsameRepository;

    @Override
    public PhStarsame save(PhStarsame phStarsame) {
        return phStarsameRepository.save(phStarsame);
    }

    @Override
    public void delete(Long id) {
        phStarsameRepository.delete(id);
    }

    @Override
    public Page<PhStarsame> findAll(Pageable pageable) {
        return phStarsameRepository.findAll(pageable);
    }

    @Override
    public void resort(Long sort) {
        phStarsameRepository.resort(sort);
    }

    @Override
    public void resetSort() {
        phStarsameRepository.resetSort();
    }

    @Override
    public List<PhStarsame> getLimitList() {
        return phStarsameRepository.findAll(new Specification<PhStarsame>() {
            @Override
            public Predicate toPredicate(Root<PhStarsame> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> params = new ArrayList<Predicate>();
                params.add(criteriaBuilder.notEqual(root.get("sort"), 999));
                Predicate[] predicates = new Predicate[params.size()];
                criteriaQuery.where(params.toArray(predicates));
                return null;
            }
        }, new PageRequest(0, 6, Sort.Direction.ASC, "sort")).getContent();
    }

    @Override
    public PhStarsame findOne(Long id) {
        return phStarsameRepository.findOne(id);
    }
}
