package cn.offway.hades.controller;

import cn.offway.hades.domain.*;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.service.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiniu.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping
public class MerchantController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private QiniuProperties qiniuProperties;
    @Autowired
    private PhMerchantService merchantService;
    @Autowired
    private PhMerchantBrandService merchantBrandService;
    @Autowired
    private PhMerchantFileService merchantFileService;
    @Autowired
    private PhBrandService brandService;
    @Autowired
    private PhAddressService addressService;

    @RequestMapping("/merchant.html")
    public String index(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "merchant_index";
    }

    @RequestMapping("/merchant_add.html")
    public String merchant_add(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "merchant_add";
    }

    @RequestMapping("/merchant_edit.html")
    public String merchant_edit(ModelMap map, Long id) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        map.addAttribute("theId", id);
        return "merchant_add";
    }

    @ResponseBody
    @RequestMapping("/merchant_list")
    public Map<String, Object> getList(HttpServletRequest request) {
        int sEcho = Integer.parseInt(request.getParameter("sEcho"));
        int iDisplayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
        String name = request.getParameter("name");
        Sort sort = new Sort("id");
        Page<PhMerchant> pages = merchantService.findAll(name, new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength, sort));
        List<PhMerchant> list = pages.getContent();
        List<Object> data = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for (PhMerchant item : list) {
            Map<String, Object> row = mapper.convertValue(item, Map.class);
            long mid = item.getId();
            List<PhMerchantBrand> brandList = merchantBrandService.findByPid(mid);
            row.put("brandList", brandList);
            data.add(row);
        }
        int initEcho = sEcho + 1;
        Map<String, Object> map = new HashMap<>();
        map.put("sEcho", initEcho);
        map.put("iTotalRecords", pages.getTotalElements());//数据总条数
        map.put("iTotalDisplayRecords", pages.getTotalElements());//显示的条数
        map.put("aData", data);//数据集合
        return map;
    }

    @ResponseBody
    @RequestMapping("/merchant_del")
    public boolean delete(@RequestParam("ids[]") Long[] ids) {
        for (Long id : ids) {
            PhMerchant tmpObj = merchantService.findOne(id);
            merchantService.del(id);
            merchantBrandService.delByPid(id);
            merchantFileService.delByPid(id);
            addressService.del(tmpObj.getAddrId());
        }
        return true;
    }

    @ResponseBody
    @PostMapping("/merchant_save")
    public boolean save(PhMerchant merchant, String brandIDStr, String imagesJSONStr, String address_send_jsonStr) {
        merchant.setCreateTime(new Date());
        merchant.setStatus("0");
        PhMerchant merchantObj = merchantService.save(merchant);
        //purge first
        merchantBrandService.delByPid(merchantObj.getId());
        //rebuild
        String[] brandList = brandIDStr.split(",");
        for (String bid : brandList) {
            PhBrand brand = brandService.findOne(Long.valueOf(bid));
            if (brand != null) {
                PhMerchantBrand merchantBrand = new PhMerchantBrand();
                merchantBrand.setMerchantId(merchantObj.getId());
                merchantBrand.setMerchantName(merchantObj.getName());
                merchantBrand.setMerchantLogo(merchantObj.getLogo());
                merchantBrand.setBrandId(brand.getId());
                merchantBrand.setBrandLogo(brand.getLogo());
                merchantBrand.setBrandName(brand.getName());
                merchantBrand.setRemark(brand.getRemark());
                merchantBrand.setCreateTime(new Date());
                merchantBrandService.save(merchantBrand);
            } else {
                logger.error("brand Id 非法");
            }
        }
        //purge first
        merchantFileService.delByPid(merchantObj.getId());
        //rebuild
        PhAddress address = new PhAddress();
        JSONObject addrObj = JSON.parseObject(address_send_jsonStr);
        address.setProvince(addrObj.getString("province"));
        address.setCity(addrObj.getString("city"));
        address.setCounty(addrObj.getString("county"));
        address.setContent(addrObj.getString("content"));
        address.setRealName(addrObj.getString("realName"));
        address.setPhone(addrObj.getString("phone"));
        address.setRemark(address_send_jsonStr);
        if (merchantObj.getAddrId() != null) {
            address.setId(merchantObj.getAddrId());
        } else {
            address.setCreateTime(new Date());
        }
        PhAddress addressObj = addressService.save(address);
        merchantObj.setAddrId(addressObj.getId());
        merchantService.save(merchantObj);
        String text = new String(com.qiniu.util.Base64.decode(imagesJSONStr.getBytes(), Base64.DEFAULT));
        JSONArray jsonArray = JSON.parseArray(text);
        if (jsonArray != null) {
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                PhMerchantFile merchantFile = new PhMerchantFile();
                merchantFile.setMerchantId(merchantObj.getId());
                merchantFile.setLogo(merchantObj.getLogo());
                merchantFile.setName(merchantObj.getName());
                merchantFile.setRemark(merchantObj.getRemark());
                merchantFile.setFileUrl(jsonObject.getString("url"));
                merchantFile.setCreateTime(new Date());
                merchantFileService.save(merchantFile);
            }
        } else {
            logger.error("file json 非法");
        }
        return true;
    }

    @ResponseBody
    @RequestMapping("/merchant_find")
    public Map<String, Object> find(Long id) {
        PhMerchant merchant = merchantService.findOne(id);
        Map<String, Object> map = new HashMap<>();
        if (map != null) {
            List<PhMerchantBrand> brandList = merchantBrandService.findByPid(merchant.getId());
            List<PhMerchantFile> fileList = merchantFileService.findByPid(merchant.getId());
            PhAddress address = addressService.findOne(merchant.getAddrId());
            map.put("main", merchant);
            map.put("brandList", brandList);
            map.put("fileList", fileList);
            map.put("address", address);
        }
        return map;
    }
}
