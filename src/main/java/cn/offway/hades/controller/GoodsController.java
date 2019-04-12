package cn.offway.hades.controller;

import cn.offway.hades.domain.PhBrand;
import cn.offway.hades.domain.PhGoods;
import cn.offway.hades.domain.PhGoodsCategory;
import cn.offway.hades.domain.PhGoodsType;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class GoodsController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private QiniuProperties qiniuProperties;
    @Autowired
    private PhGoodsService goodsService;
    @Autowired
    private PhGoodsTypeService goodsTypeService;
    @Autowired
    private PhGoodsCategoryService goodsCategoryService;
    @Autowired
    private PhGoodsImageService goodsImageService;
    @Autowired
    private PhGoodsPropertyService goodsPropertyService;
    @Autowired
    private PhGoodsStockService goodsStockService;
    @Autowired
    private PhBrandService brandService;

    @RequestMapping("/goods.html")
    public String index(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "goods_index";
    }

    @RequestMapping("/goods_add.html")
    public String add(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "goods_add";
    }

    @ResponseBody
    @RequestMapping("/type_and_category_list")
    public List<Object> getTypeAndCategory() {
        List<Object> ret = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<PhGoodsType> typeList = goodsTypeService.findAll();
        for (PhGoodsType type : typeList) {
            Map container = objectMapper.convertValue(type, Map.class);
            List<PhGoodsCategory> categoryList = goodsCategoryService.findByPid(type.getId());
            container.put("sub", categoryList);
            ret.add(container);
        }
        return ret;
    }

    @ResponseBody
    @RequestMapping("/brand_list_all")
    public List<PhBrand> getBrand() {
        return brandService.findAll();
    }

    @ResponseBody
    @RequestMapping("/goods_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        int sEcho = Integer.parseInt(request.getParameter("sEcho"));
        int iDisplayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
        String name = request.getParameter("name");
        Sort sort = new Sort("id");
        Page<PhGoods> pages = goodsService.findAll(name, new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength, sort));
        int initEcho = sEcho + 1;
        Map<String, Object> map = new HashMap<>();
        map.put("sEcho", initEcho);
        map.put("iTotalRecords", pages.getTotalElements());//数据总条数
        map.put("iTotalDisplayRecords", pages.getTotalElements());//显示的条数
        map.put("aData", pages.getContent());//数据集合
        return map;
    }

    @ResponseBody
    @RequestMapping("/goods_del")
    public boolean delete(@RequestParam("ids[]") Long[] ids) {
        for (Long id : ids) {
            goodsService.del(id);
            goodsImageService.delByPid(id);
            goodsPropertyService.delByPid(id);
            goodsStockService.delByPid(id);
        }
        return true;
    }
}
