package cn.offway.hades.controller;

import cn.offway.hades.domain.PhAdmin;
import cn.offway.hades.domain.PhUserInfo;
import cn.offway.hades.service.PhOrderInfoService;
import cn.offway.hades.service.PhRoleadminService;
import cn.offway.hades.service.PhUserInfoService;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.event.WriteHandler;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping
public class PurchaseRankController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private PhRoleadminService roleadminService;
    @Autowired
    private PhOrderInfoService orderInfoService;
    @Autowired
    private PhUserInfoService userInfoService;

    @RequestMapping("/purchase_rank.html")
    public String index() {
        return "purchase_rank_index";
    }

    @ResponseBody
    @RequestMapping("/purchase_rank_list")
    public Map<String, Object> getList(@AuthenticationPrincipal PhAdmin admin, String sTime, String eTime, String sPrice, String ePrice) {
        List<Long> roles = roleadminService.findRoleIdByAdminId(admin.getId());
        if (roles.contains(BigInteger.valueOf(8L))) {
            return null;
        } else {
            List<Map<String, Object>> data = new ArrayList<>();
            List<Object[]> list = orderInfoService.stat(sTime, eTime);
            for (Object[] l : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("amount", l[0]);
                map.put("userId", l[1]);
                map.put("orderCount", l[2]);
                PhUserInfo userInfo = userInfoService.findOne(Long.valueOf(String.valueOf(l[1])));
                if (userInfo != null) {
                    map.put("phone", userInfo.getPhone());
                    map.put("nickname", userInfo.getNickname());
                    map.put("createTime", userInfo.getCreateTime());
                } else {
                    map.put("phone", "");
                    map.put("nickname", "");
                    map.put("createTime", "");
                }
                double amt = Double.valueOf(String.valueOf(l[0]));
                if (StringUtils.isNotBlank(sPrice) && StringUtils.isNotBlank(ePrice)) {
                    if ((Integer.valueOf(sPrice) <= amt) && (Integer.valueOf(ePrice) >= amt)) {
                        data.add(map);
                    }
                } else if (StringUtils.isNotBlank(sPrice)) {
                    if ((Integer.valueOf(sPrice) <= amt)) {
                        data.add(map);
                    }
                } else if (StringUtils.isNotBlank(ePrice)) {
                    if ((Integer.valueOf(ePrice) >= amt)) {
                        data.add(map);
                    }
                } else {
                    data.add(map);
                }
            }
            Map<String, Object> res = new HashMap<>();
            res.put("data", data);
            return res;
        }
    }

    private List<List<String>> head() {
        List<List<String>> list = new ArrayList<List<String>>();
        String[] cols = new String[]{"用户id", "用户手机号", "用户昵称", "用户注册时间", "总下单金额", "总下单数"};
        for (String s : cols) {
            List<String> head0 = new ArrayList<String>();
            head0.add(s);
            list.add(head0);
        }
        return list;
    }

    @RequestMapping("/purchase_rank_list_export.html")
    public void export(HttpServletResponse response, String sTime, String eTime, String sPrice, String ePrice) {
        List<List<String>> dataList = new ArrayList<List<String>>();
        List<Object[]> list = orderInfoService.stat(sTime, eTime);
        for (Object[] l : list) {
            List<String> data = new ArrayList<String>(6);
            data.add(0, String.valueOf(l[1]));
            PhUserInfo userInfo = userInfoService.findOne(Long.valueOf(String.valueOf(l[1])));
            if (userInfo != null) {
                data.add(1, userInfo.getPhone());
                data.add(2, userInfo.getNickname());
                data.add(3, userInfo.getCreateTime().toString());
            } else {
                data.add(1, "");
                data.add(2, "");
                data.add(3, "");
            }
            data.add(4, String.format("%.2f", l[0]));
            data.add(5, String.valueOf(l[2]));
            //price range filter
            double amt = Double.valueOf(String.valueOf(l[0]));
            if (StringUtils.isNotBlank(sPrice) && StringUtils.isNotBlank(ePrice)) {
                if ((Integer.valueOf(sPrice) <= amt) && (Integer.valueOf(ePrice) >= amt)) {
                    dataList.add(data);
                }
            } else if (StringUtils.isNotBlank(sPrice)) {
                if ((Integer.valueOf(sPrice) <= amt)) {
                    dataList.add(data);
                }
            } else if (StringUtils.isNotBlank(ePrice)) {
                if ((Integer.valueOf(ePrice) >= amt)) {
                    dataList.add(data);
                }
            } else {
                dataList.add(data);
            }
        }
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            response.setContentType("multipart/form-data");
            response.setCharacterEncoding("utf-8");
            String fileName = new String(("BuyRankList_" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
                    .getBytes(), StandardCharsets.UTF_8);
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");
            ExcelWriter writer = new ExcelWriter(null, outputStream, ExcelTypeEnum.XLSX, true, new WriteHandler() {
                @Override
                public void sheet(int i, org.apache.poi.ss.usermodel.Sheet sheet) {
                    //nothing
                }

                @Override
                public void row(int i, Row row) {
                    //nothing
                }

                @Override
                public void cell(int i, Cell cell) {
                    //nothing
                }
            });
            Sheet sheet = new Sheet(1, 0);
            sheet.setHead(head());
            sheet.setSheetName("BuyRankList");
            sheet.setAutoWidth(true);
            writer.write0(dataList, sheet);
            writer.finish();
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            logger.error("IO ERROR", e);
        }
    }
}
