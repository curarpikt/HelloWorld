package com.chanapp.chanjet.customer.util;

import java.util.Map;

import com.chanapp.chanjet.customer.http.HttpCommon;
import com.chanapp.chanjet.customer.http.HttpMethodEnum;
import com.chanapp.chanjet.customer.http.HttpResponse;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.data.api.DataManager;

public class HttpUtil {
    private static final DataManager dataManager = AppWorkManager.getDataManager();

    public static HttpResponse doGet(String url, Map<String, String> header, Map<String, String> getParams, int timeout)
            throws Exception {
        return HttpCommon.doHttp(url, HttpMethodEnum.GET, header, getParams, null, timeout);
    }

    public static HttpResponse doDelete(String url, Map<String, String> header, Map<String, String> getParams,
            int timeout) throws Exception {
        return HttpCommon.doHttp(url, HttpMethodEnum.DELETE, header, getParams, null, timeout);
    }

    public static HttpResponse doPost(String url, Map<String, String> header, Object body, int timeout)
            throws Exception {
        return doPutOrPost(url, header, body, HttpMethodEnum.POST, timeout);
    }

    public static HttpResponse doPut(String url, Map<String, String> header, Object body, int timeout)
            throws Exception {
        return doPutOrPost(url, header, body, HttpMethodEnum.PUT, timeout);
    }

    private static HttpResponse doPutOrPost(String url, Map<String, String> header, Object body, HttpMethodEnum method,
            int timeout) throws Exception {
        byte[] bodyString = null;
        if (body != null) {
            if (body instanceof byte[]) {
                bodyString = (byte[]) body;
            } else if (body instanceof String) {
                bodyString = body.toString().getBytes();
            } else {
                bodyString = dataManager.toJSONString(body).getBytes();
            }
        }
        //System.out.println("bodyString:"+bodyString);
        return HttpCommon.doHttp(url, method, header, null, bodyString, timeout);
    }

    /**
     * 对get参数进行urlencoder
     * 
     * @param params
     * @return
     */
    public static String urlEncode(String params) {
        return HttpCommon.urlEncode(params);
    }

}
