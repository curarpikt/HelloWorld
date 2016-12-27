package com.chanapp.chanjet.customer.service.buriedpoint;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.reader.CiaReader;
import com.chanapp.chanjet.customer.util.Context;
import com.chanapp.chanjet.customer.util.DeviceUtil;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.impl.common.HttpUtil;

public class BuriedPointServiceImpl extends BaseServiceImpl implements BuriedPointServiceItf {
    private final Logger log = LoggerFactory.getLogger(BuriedPointServiceImpl.class);

    @Override
    public void firstLoginPoint() {
    	Boolean result = false;
        Map<String, String> parameter = new HashMap<String, String>();
        parameter.put("access_token", EnterpriseContext.getToken());
        parameter.put("orgId", EnterpriseContext.getOrgId());
        parameter.put("appId", EnterpriseContext.getAppId());
        parameter.put("clientType", DeviceUtil.getClientType());
        parameter.put("userIp", getClientIpAddress());
        parameter.put("extendInfo", "");
        String resultJson = HttpUtil.HttpPostAction(CiaReader.getFirstLoginApp() + "?appKey="
                + EnterpriseContext.getAppKey() + "&appSecret=" + EnterpriseContext.getAppSecret(), parameter);
        Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
        result = (Boolean) tokenMap.get("result");
        if (result==false) {
            _buriedPointErrorLog("firstLoginApp");
        }
    }

    @Override
    public void everyLoginPoint() {
        Map<String, String> parameter = new HashMap<String, String>();
        String accessTocken = EnterpriseContext.getToken();
        String appId = EnterpriseContext.getAppId();
        String appKey = EnterpriseContext.getAppKey();
        String orgId = EnterpriseContext.getOrgId();
        String userId = EnterpriseContext.getCurrentUser().getUserId();
        String clientType = DeviceUtil.getClientType();
        parameter.put("access_token", accessTocken);
        parameter.put("appKey", appKey);
        parameter.put("endpointInfo", DeviceUtil.getEndpointInfo());
        // 确认一下含义
        parameter.put("rpt",
                "\27{\"ct\":\"" + clientType + "\",\"pd\":\"" + appId + "\",\"id\":\"" + userId + "\",\"cp\":\"" + orgId
                        + "\",\"tg\":\"\",\"cv\":\"\",\"l\":[{\"d\":\"\"},{\"p\":\"" + getClientIpAddress()
                        + "\"}]}\27");
        // 直接CATCH.不抛
        String resultJson;
        resultJson = HttpUtil.HttpPostAction(CiaReader.getEveryLoginApp(), parameter);
        Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
        Boolean result = (Boolean) tokenMap.get("result");
        if (result==false) {
            _buriedPointErrorLog("firstLoginApp");
        }
 
    }

    private void _buriedPointErrorLog(String actionName) {
        log.info(actionName + "faild : appKey = " + EnterpriseContext.getAppKey() + ", appId = "
                + EnterpriseContext.getAppId() + ", token = " + EnterpriseContext.getToken() + ", orgId = "
                + EnterpriseContext.getOrgId() + ", userId" + EnterpriseContext.getCurrentUser().getUserLongId());
    }

    @Override
    public String getClientIpAddress() {
        return (String) Context.get(Context.clientIpAddress);
    }

}
