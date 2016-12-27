package com.chanapp.chanjet.customer.service.regist;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.http.HttpResponse;
import com.chanapp.chanjet.customer.reader.CiaReader;
import com.chanapp.chanjet.customer.util.HttpUtil;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class RegistServiceImpl extends BaseServiceImpl implements RegistServiceItf {
    private static Logger logger = LoggerFactory.getLogger(RegistServiceImpl.class);

    @Override
    public Map<String, Object> getOrganizationInfoByOrgId(String orgId) {
        try {
            String url = CiaReader.getOrganizationUrl();
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/plain");

            Map<String, String> authMap = new HashMap<String, String>();
            authMap.put("appKey", EnterpriseContext.getAppKey());
            authMap.put("appSecret", EnterpriseContext.getAppSecret());
            authMap.put("orgIdentify", orgId);

            HttpResponse response = HttpUtil.doGet(url, headers, authMap, 3000);

            String retStr = response.getString();
            Map<String, Object> tokenMap = dataManager.jsonStringToMap(retStr);
            if (tokenMap.containsKey("errorCode")) {
                tokenMap.put("param", MsgUtil.msgMap.get(tokenMap.get("errorCode")));
            }
            return tokenMap;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("getOrganizationInfoByOrgId exception : ", e);
        }
        return null;
    }

}
