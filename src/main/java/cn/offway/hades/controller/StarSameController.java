package cn.offway.hades.controller;

import cn.offway.hades.domain.*;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.service.*;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qiniu.util.Base64;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private PhStarsameCommentsService starsameCommentsService;
    @Autowired
    private PhGoodsService goodsService;
    @Autowired
    private PhBrandService brandService;
    @Autowired
    private QiniuProperties qiniuProperties;
    @Autowired
    private PhNoticeService noticeService;
    @Autowired
    private PhUserInfoService userInfoService;

    @RequestMapping("/starSame.html")
    public String index(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        return "starSame_index";
    }

    @RequestMapping("/starSame_Mini.html")
    public String who(ModelMap map, String who) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        map.addAttribute("who", who);
        return "starSame_index";
    }

    @RequestMapping("/starSameComment.html")
    public String index(ModelMap map, Long id) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        map.addAttribute("theId", id);
        PhStarsame starsame = starsameService.findOne(id);
        if (starsame != null) {
            map.addAttribute("theName", starsame.getTitle());
        }
        return "startSameComment_index";
    }

    @ResponseBody
    @RequestMapping("/starSame_list")
    public Map<String, Object> getList(HttpServletRequest request, String id, String name, String starName) {
        int sEcho = Integer.parseInt(request.getParameter("sEcho"));
        int iDisplayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "sort"), new Sort.Order(Sort.Direction.DESC, "createTime"));
        Page<PhStarsame> pages = starsameService.findAll(id, name, starName, new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength, sort));
        int initEcho = sEcho + 1;
        Map<String, Object> map = new HashMap<>();
        map.put("sEcho", initEcho);
        map.put("iTotalRecords", pages.getTotalElements());//数据总条数
        map.put("iTotalDisplayRecords", pages.getTotalElements());//显示的条数
        map.put("aData", pages.getContent());//数据集合
        return map;
    }

    @ResponseBody
    @RequestMapping("/starSameComment_list")
    public Map<String, Object> getCommentList(int sEcho, int iDisplayStart, int iDisplayLength, String pid, String id, String userId, String content) {
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "createTime"));
        PageRequest pr = new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength, sort);
        Page<PhStarsameComments> pages = starsameCommentsService.findAll(pid, id, userId, content, pr);
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
    @RequestMapping("/starSameComment_del")
    @Transactional
    public boolean deleteComment(@RequestParam("ids[]") Long[] ids) {
        for (Long id : ids) {
            PhStarsameComments comments = starsameCommentsService.findOne(id);
            if (comments != null) {
                PhUserInfo userInfo = userInfoService.findOne(comments.getUserId());
                if (userInfo != null) {
                    //删除评论时，发一条系统通知
                    PhNotice notice = new PhNotice();
                    notice.setUserId(userInfo.getId());
                    notice.setCreateTime(new Date());
                    notice.setIsRead("0");/* 是否已读[0-否,1-是] **/
                    notice.setType("0");/* 类型[0-系统消息，1-订单通知，2-活动通知] **/
                    notice.setContent(MessageFormat.format("用户您好，您发送的评论“{0}”涉嫌违规，已被系统删除。", comments.getContent()));
                    noticeService.save(notice);
                }
                starsameCommentsService.delete(id);
            }
        }
        return true;
    }

    @ResponseBody
    @RequestMapping("/starSame_find")
    public Map<String, Object> find(Long id) {
        PhStarsame starsame = starsameService.findOne(id);
        Map<String, Object> map = new HashMap<>();
        if (starsame != null) {
            List<PhStarsameGoods> goodsList = starsameGoodsService.findAllByPid(starsame.getId(), "0");
            List<PhStarsameGoods> goodsListSame = starsameGoodsService.findAllByPid(starsame.getId(), "1");
            List<PhStarsameImage> imageList = starsameImageService.findAllByPid(starsame.getId());
            map.put("main", starsame);
            map.put("goodsList", goodsList);
            map.put("goodsListSame", goodsListSame);
            map.put("imageList", imageList);
        }
        return map;
    }

    @ResponseBody
    @RequestMapping("/starSame_top")
    public boolean top(Long id, Long sort) {
        List<PhStarsame> top6 = starsameService.getLimitList();
        long i = 0;
        boolean included = false;
        for (int index = 0; index < 6; index++) {
            if (index < top6.size()) {
                PhStarsame obj = top6.get(index);
                if (Long.compare(id, obj.getId()) == 0) {
                    included = true;
                    obj.setSort(sort);
                    starsameService.save(obj);
                    continue;
                }
                if (Long.compare(i, sort) == 0) {
                    //skip specific position
                    i++;
                }
                obj.setSort(i);
                starsameService.save(obj);
                i++;
            }
        }
        if (!included) {
            PhStarsame newOne = starsameService.findOne(id);
            newOne.setSort(sort);
            starsameService.save(newOne);
        }
        starsameService.resetSort();
        return true;
    }


    @ResponseBody
    @RequestMapping("/starSame_topMini")
    public boolean topMini(Long id, Long sort) {
        List<PhStarsame> top6 = starsameService.getLimitListSortMini();
        long i = 0;
        boolean included = false;
        for (int index = 0; index < 6; index++) {
            if (index < top6.size()) {
                PhStarsame obj = top6.get(index);
                if (Long.compare(id, obj.getId()) == 0) {
                    included = true;
                    obj.setSortMini(sort);
                    starsameService.save(obj);
                    continue;
                }
                if (Long.compare(i, sort) == 0) {
                    //skip specific position
                    i++;
                }
                obj.setSortMini(i);
                starsameService.save(obj);
                i++;
            }
        }
        if (!included) {
            PhStarsame newOne = starsameService.findOne(id);
            newOne.setSortMini(sort);
            starsameService.save(newOne);
        }
        starsameService.resetSort();
        return true;
    }

    @ResponseBody
    @PostMapping("/starSame_save")
    public boolean save(PhStarsame starsame, String goodsIDStr, String goodsIDStrSame, String imagesJSONStr) {
        starsame.setCreateTime(new Date());
        if (starsame.getId() == null) {
            starsame.setSort(999L);
            starsame.setSortMini(999L);
        }
        if (null == starsame.getCallCount()) {
            starsame.setCallCount(0L);
        }
        PhStarsame starsameObj = starsameService.save(starsame);
        //purge first
        starsameGoodsService.deleteByPid(starsameObj.getId());
        String[] goodsList = goodsIDStr.split(",");
        String[] goodsSameList = goodsIDStrSame.split(",");
        for (String Sameid : goodsSameList) {
            if (NumberUtils.isNumber(Sameid)) {
                PhBrand phBrand = brandService.findOne(Long.valueOf(Sameid));
                if ((phBrand != null)) {
                    PhStarsameGoods starsameGoods0 = new PhStarsameGoods();
                    starsameGoods0.setStarsameId(starsameObj.getId());
                    starsameGoods0.setStarsameTitle(starsameObj.getTitle());
                    starsameGoods0.setBrandId(phBrand.getId());
                    starsameGoods0.setBrandName(phBrand.getName());
                    starsameGoods0.setBrandLogo(phBrand.getLogo());
                    starsameGoods0.setRemark(phBrand.getRemark());
                    starsameGoods0.setType("1");
                    starsameGoods0.setCreateTime(new Date());
                    starsameGoodsService.save(starsameGoods0);
                } else {
                    logger.error("goods Same Id 非法");
                }
            } else {
                logger.error("goods Same Id 非法");
            }
        }
        for (String gid : goodsList) {
            if (NumberUtils.isNumber(gid)) {
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
                    starsameGoods.setType("0");
                    starsameGoods.setCreateTime(new Date());
                    starsameGoodsService.save(starsameGoods);
                } else {
                    logger.error("goods Id 非法");
                }
            } else {
                logger.error("goods Id 非法");
            }
        }
        //purge first
        starsameImageService.deleteByPid(starsameObj.getId());
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

    @ResponseBody
    @PostMapping("/starSame_sameSrot")
    public boolean sameSort() {
        starsameService.sameSort();
        return true;
    }
}
