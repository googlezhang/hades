package cn.offway.hades.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import cn.offway.hades.domain.PhProductInfo;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.repository.PhProductInfoRepository;
import cn.offway.hades.service.PhProductInfoService;
import cn.offway.hades.service.QiniuService;


/**
 * 活动产品表Service接口实现
 *
 * @author wn
 * @version $v: 1.0.0, $time:2018-10-15 16:49:00 Exp $
 */
@Service
public class PhProductInfoServiceImpl implements PhProductInfoService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PhProductInfoRepository phProductInfoRepository;
	
	@Autowired
	private QiniuService qiniuService;
	
	@Autowired
	private QiniuProperties qiniuProperties;
	
	@Value("${ph.file.path}")
	private String FILE_PATH;
	
	@Override
	public PhProductInfo save(PhProductInfo phProductInfo){
		return phProductInfoRepository.save(phProductInfo);
	}
	
	@Override
	public PhProductInfo saveProduct(PhProductInfo phProductInfo){
		Long productId = phProductInfo.getId();
		boolean isAdd = true;
		if(null!=productId){
			isAdd = false;
			PhProductInfo productInfo = findOne(productId);
			String image = productInfo.getImage();
			if(!image.equals(phProductInfo.getImage())){
				//如果资源变动则删除七牛资源
				qiniuService.qiniuDelete(image.replace(qiniuProperties.getUrl()+"/", ""));
			}
			String banner = productInfo.getBanner();
			if(!banner.equals(phProductInfo.getBanner())){
				//如果资源变动则删除七牛资源
				qiniuService.qiniuDelete(banner.replace(qiniuProperties.getUrl()+"/", ""));
			}
			String shareImage = productInfo.getShareImage();
			if(!shareImage.equals(phProductInfo.getShareImage())){
				//如果资源变动则删除七牛资源
				qiniuService.qiniuDelete(shareImage.replace(qiniuProperties.getUrl()+"/", ""));
			}
			String thumbnail = productInfo.getThumbnail();
			if(!thumbnail.equals(phProductInfo.getThumbnail())){
				//如果资源变动则删除七牛资源
				qiniuService.qiniuDelete(thumbnail.replace(qiniuProperties.getUrl()+"/", ""));
			}
			String saveImage = productInfo.getSaveImage();
			if(!saveImage.equals(phProductInfo.getSaveImage())){
				//如果资源变动则删除七牛资源
				qiniuService.qiniuDelete(saveImage.replace(qiniuProperties.getUrl()+"/", ""));
			}
			
			String background = productInfo.getBackground();
			if(!background.equals(phProductInfo.getBackground())){
				//如果资源变动则删除本地资源
				File file=new File(FILE_PATH+"/"+StringUtils.substringAfterLast(background, "/"));
		        if(file.exists()&&file.isFile()){
		        	file.delete();
		        }
			}
			phProductInfo.setCreateTime(productInfo.getCreateTime());
			phProductInfo.setRuleContent(productInfo.getRuleContent());
		}
		phProductInfo.setImage(phProductInfo.getImage());
		phProductInfo.setBanner(phProductInfo.getBanner());
		phProductInfo.setShareImage(phProductInfo.getShareImage());
		phProductInfo.setThumbnail(phProductInfo.getThumbnail());
		if(isAdd){
			phProductInfo.setCreateTime(new Date());
		}
		
		phProductInfo = save(phProductInfo);
		
		if(isAdd){
			//设置抽奖码自增1
			phProductInfoRepository.sequence(phProductInfo.getId());
		}
		
		return phProductInfo;
	}
	
	@Override
	public PhProductInfo findOne(Long id){
		return phProductInfoRepository.findOne(id);
	}
	
	@Override
	public List<PhProductInfo> findByEndTime(Date endTime){
		return phProductInfoRepository.findByEndTime(endTime);
	}
	
	@Override
	public Page<PhProductInfo> findByPage(final String name,Pageable page){
		return phProductInfoRepository.findAll(new Specification<PhProductInfo>() {
			
			@Override
			public Predicate toPredicate(Root<PhProductInfo> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
				List<Predicate> params = new ArrayList<Predicate>();
				
				if(StringUtils.isNotBlank(name)){
					params.add(criteriaBuilder.like(root.get("name"), "%"+name+"%"));
				}
				
                Predicate[] predicates = new Predicate[params.size()];
                criteriaQuery.where(params.toArray(predicates));
				
				return null;
			}
		}, page);
	}
}
