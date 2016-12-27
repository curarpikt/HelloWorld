package com.chanapp.chanjet.customer.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.http.HttpCommon;
import com.chanapp.chanjet.customer.http.HttpResponse;
import com.chanapp.chanjet.customer.reader.PortalReader;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.data.api.DataManager;

/**
 * @Description: 官网接口
 * 
 */
public class PortalUtil {
    private static final Logger logger = LoggerFactory.getLogger(PortalUtil.class);
    protected static final DataManager dataManager = AppWorkManager.getDataManager();

    /**
     * 
     * 根据token获得企业列表
     */
    public static Map<String, Object> getOrgListByToken(String token) {
        Map<String, Object> data = new HashMap<String, Object>();
        try {
            Map<String, String> params = new HashMap<String, String>();
            String url = PortalReader.getOrgListUrl();
            params.put("token", token);
            logger.info("switch token={}", token);
    		HttpClientUtils http = HttpClientUtils.getInstance(3000, 5000);
			PostMethod postMethod = http.post(url, params);
			String text = http.resultMethod2String(postMethod);
            if (text != null && StringUtils.isNotEmpty(text)) {
                data = dataManager.jsonStringToMap(text);
            }
        } catch (Exception e) {
            logger.error("switch org error", e);
        }
        return data;
    }

    /**
     * 
     * 切换企业
     */
    public static Map<String, Object> switchOrg(String token, String orgId) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {

            Map<String, String> params = new HashMap<String, String>();
            String url = PortalReader.getChangeOrgUrl();
            params.put("token", token);
            params.put("orgid", orgId);
            logger.info("switch token={},orgid={}", token, orgId);

            logger.info("switch url={}", url);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/plain");
            //byte[] body = HttpCommon.generatorHttpParams(params).getBytes();
            String text = com.chanjet.csp.ccs.impl.common.HttpUtil.HttpPostAction(url, params);
           // HttpResponse response = HttpUtil.doPost(url, headers, body, 3000);
            logger.info("switch postMethod");
           // String text = response.getString();

            logger.info("switch text={}", text);
            if (text != null) {
                try {
                    Map<String, Object> obj = dataManager.jsonStringToMap(text);
                    if (obj != null && obj.containsKey("result")) {
                        boolean result = ConvertUtil.toBoolean(obj.get("result").toString(), false);
                        if (result) {
                            map.put("result", result);
                            // return true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("switch exception:", e);
                }
            }
            logger.error("switch result={}", text);
            return map;
        } catch (Exception e) {
            logger.error("switch org error", e);
        }
        map.put("result", false);
        return map;
    }

    private static Map<String, Object> getOrgInfo(String url, Map<String, String> params) {
        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/plain");
            byte[] body = HttpCommon.generatorHttpParams(params).getBytes();
            HttpResponse response = HttpUtil.doPost(url, headers, body, 3000);
            logger.info("switch postMethod");
            String text = response.getString();

            if (text != null && StringUtils.isNotEmpty(text)) {
                Map<String, Object> obj = dataManager.jsonStringToMap(text);
                if (obj != null && ConvertUtil.toBoolean(obj.get("result").toString(), false)
                        && obj.containsKey("body")) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> _body = (Map<String, Object>) obj.get("body");
                    if (_body != null && _body.containsKey("exists") && "1".equals(_body.get("exists").toString())) {
                        return _body;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("switch {} : {} error:{}", url, params, e.getMessage());
        }
        return null;
    }

 

    /**
     * 
     * 测试本地获得企业ID
     */
    public static Map<String, Object> getOrgInfoByAccount(String orgAccount) {
        Map<String, String> params = new HashMap<String, String>();
        String url = PortalReader.getFindOrgInfoByOrgAccountUrl();
        params.put("orgAccount", orgAccount);
        logger.info("switch orgAccount={}", orgAccount);
        return getOrgInfo(url, params);
    }

    /**
     * 
     * 封装获得当前企业ID
     */
    public static String getOrgId() {
        String orgId = ContextUtil.getInstance().getOrgId();
        logger.info("orgId={}", orgId);
        return orgId;
    }

    /**
     * 
     * 根据企业ID获得企业名称
     */
    public static String getOrgNameById(String orgId) {
        String orgName = ContextUtil.getInstance().getOrgName();
        logger.info("orgName={}", orgName);
        return orgName;
    }

  

    

}
