package cn.offway.hades.controller;

import cn.offway.hades.domain.PhAdmin;
import cn.offway.hades.domain.PhWithdrawInfo;
import cn.offway.hades.properties.QiniuProperties;
import cn.offway.hades.service.PhWithdrawInfoService;
import cn.offway.hades.service.QiniuService;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class TransferController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PhWithdrawInfoService withdrawInfoService;
    @Autowired
    private QiniuProperties qiniuProperties;
    @Autowired
    private QiniuService qiniuService;


    @Value("${ph.url}")
    private String url;

    @Value("${alipay.appid}")
    private String appid;
    @Value("${alipay.privatekey}")
    private String privatekey;
    @Value("${alipay.publickey}")
    private String publickey;

    @RequestMapping("/transfer.html")
    public String index(ModelMap map) {
        map.addAttribute("qiniuUrl", qiniuProperties.getUrl());
        map.addAttribute("url", url);
        return "transfer_index";
    }

    @ResponseBody
    @RequestMapping("/transfer_list")
    public Map<String, Object> getList(HttpServletRequest request, String name, String tag, String status, String title, String type) {
        int sEcho = Integer.parseInt(request.getParameter("sEcho"));
        int iDisplayStart = Integer.parseInt(request.getParameter("iDisplayStart"));
        int iDisplayLength = Integer.parseInt(request.getParameter("iDisplayLength"));
        Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "id"), new Sort.Order(Sort.Direction.ASC, "sort"));
        Page<PhWithdrawInfo> pages = withdrawInfoService.findAll(new PageRequest(iDisplayStart == 0 ? 0 : iDisplayStart / iDisplayLength, iDisplayLength < 0 ? 9999999 : iDisplayLength));
        int initEcho = sEcho + 1;
        return echoBody(initEcho, pages.getTotalElements(), pages.getTotalElements(), pages.getContent());
    }

    private Map<String, Object> echoBody(int initEcho, long iTotalRecords, long iTotalDisplayRecords, List<?> data) {
        Map<String, Object> map = new HashMap<>();
        map.put("sEcho", initEcho);
        map.put("iTotalRecords", iTotalRecords);//数据总条数
        map.put("iTotalDisplayRecords", iTotalDisplayRecords);//显示的条数
        map.put("aData", data);//数据集合
        return map;
    }

    @ResponseBody
    @RequestMapping("/transfer_confirm")
    public boolean confirm(Long id, @AuthenticationPrincipal PhAdmin admin) throws AlipayApiException {
        PhWithdrawInfo phWithdrawInfo = withdrawInfoService.findOne(id);
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appid, privatekey, "json", "UTF-8", publickey, "RSA2");
        AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
        request.setBizContent("{" +
                "\"out_biz_no\":\"" + phWithdrawInfo.getOrderNo() + "\"," +
                "\"payee_type\":\"ALIPAY_USERID\"," +
                "\"payee_account\":\"" + phWithdrawInfo.getAlipayUserId() + "\"," +
                "\"amount\":\""+phWithdrawInfo.getAmount()+"\"," +
                "\"payer_show_name\":\"很潮app\"," +
                "\"remark\":\"很潮App金额提现\"" +
                "  }");
        AlipayFundTransToaccountTransferResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            phWithdrawInfo.setStatus("1");
            phWithdrawInfo.setCheckName(admin.getNickname());
            phWithdrawInfo.setCheckTime(new Date());

        } else {
            phWithdrawInfo.setStatus("2");
            phWithdrawInfo.setCheckName(admin.getNickname());
            phWithdrawInfo.setCheckTime(new Date());
            phWithdrawInfo.setRemark(response.getSubMsg());
        }
        withdrawInfoService.save(phWithdrawInfo);
        return true;
    }

    @ResponseBody
    @RequestMapping("/transfer_refuse")
    public boolean refuse(Long id,String reason, @AuthenticationPrincipal PhAdmin admin){
        PhWithdrawInfo phWithdrawInfo = withdrawInfoService.findOne(id);
        phWithdrawInfo.setStatus("2");
        phWithdrawInfo.setCheckTime(new Date());
        phWithdrawInfo.setCheckName(admin.getNickname());
        phWithdrawInfo.setCheckReason(reason);
        withdrawInfoService.save(phWithdrawInfo);
        return true;
    }
}
