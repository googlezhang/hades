package cn.offway.hades.controller;

import cn.offway.hades.domain.PhGoods;
import cn.offway.hades.domain.PhStarsame;
import cn.offway.hades.domain.PhStarsameGoods;
import cn.offway.hades.domain.PhStarsameImage;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.service.PhGoodsService;
import cn.offway.hades.service.PhStarsameGoodsService;
import cn.offway.hades.service.PhStarsameImageService;
import cn.offway.hades.service.PhStarsameService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping
public class StarSameController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PhStarsameService starsameService;
    @Autowired
    private PhStarsameGoodsService starsameGoodsService;
    @Autowired
    private PhStarsameImageService starsameImageService;
    @Autowired
    private PhGoodsService goodsService;
    @Autowired
    private QiniuProperties qiniuProperties;

    @RequestMapping("/starSame.html")
    public String index(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "starSame_index";
    }

    @ResponseBody
    @RequestMapping("/starSame_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        int sEcho = Integer.parseInt(request.getParameter("sEcho"));
        int iDisplayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
        Page<PhStarsame> pages = starsameService.findAll(new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength));
        int initEcho = sEcho + 1;
        Map<String, Object> map = new HashMap<>();
        map.put("sEcho", initEcho);
        map.put("iTotalRecords", pages.getTotalElements());//数据总条数
        map.put("iTotalDisplayRecords", pages.getTotalElements());//显示的条数
        map.put("aData", pages.getContent());//数据集合
        return map;
    }

    @ResponseBody
    @RequestMapping("/starSame_del")
    public boolean delete(@RequestParam("ids[]") Long[] ids) {
        for (Long id : ids) {
            starsameService.delete(id);
            starsameGoodsService.deleteByPid(id);
            starsameImageService.deleteByPid(id);
        }
        return true;
    }

    @ResponseBody
    @PostMapping("/starSame_save")
    public boolean save(PhStarsame starsame, String goodsID, String imagesJSONStr) {
        PhStarsame starsameObj = starsameService.save(starsame);
        String[] goodsList = goodsID.split(",");
        for (String gid : goodsList) {
            PhGoods goods = goodsService.findOne(Long.valueOf(gid));
            if (goods != null) {
                PhStarsameGoods starsameGoods = new PhStarsameGoods();
                starsameGoods.setStarsameId(starsameObj.getId());
                starsameGoods.setStarsameTitle(starsameObj.getTitle());
                starsameGoods.setGoodsId(goods.getId());
                starsameGoods.setGoodsName(goods.getName());
                starsameGoods.setGoodsImage(goods.getImage());
                starsameGoods.setBrandId(goods.getBrandId());
                starsameGoods.setBrandName(goods.getBrandName());
                starsameGoods.setBrandLogo(goods.getBrandLogo());
                starsameGoods.setRemark(goods.getRemark());
                starsameGoods.setCreateTime(new Date());
                starsameGoodsService.save(starsameGoods);
            } else {
                logger.error("goods Id 非法");
            }
        }
        String text = new String(Base64.decode(imagesJSONStr.getBytes(), Base64.DEFAULT));
        JSONArray jsonArray = JSON.parseArray(text);
        if (jsonArray != null) {
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                PhStarsameImage starsameImage = new PhStarsameImage();
                starsameImage.setStarsameId(starsameObj.getId());
                starsameImage.setStarsameTitle(starsameObj.getTitle());
                starsameImage.setImageUrl(jsonObject.getString("url"));
                starsameImage.setRemark(jsonObject.getString("remark"));
                starsameImage.setSort(jsonObject.getLong("sort"));
                starsameImage.setCreateTime(new Date());
                starsameImageService.save(starsameImage);
            }
        } else {
            logger.error("images json 非法");
        }
        return true;
    }
}
