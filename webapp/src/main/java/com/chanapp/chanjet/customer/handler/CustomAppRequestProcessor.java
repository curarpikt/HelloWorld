package com.chanapp.chanjet.customer.handler;

import java.io.UnsupportedEncodingException;

import com.chanapp.chanjet.customer.util.Context;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.common.base.rest.RestResponse;
import com.chanjet.csp.enterprise.ext.AppRequestHandler;

public class CustomAppRequestProcessor implements AppRequestHandler {

  @Override
  public void init() {

  }

  @Override
  public boolean process(RestRequest request, RestResponse response) {
    initContext(request);

    if (request.getCharacterEncoding() == null) {
      try {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
    }

    /*
     * ContextUtil.getInstance().setServerName(request.getServerName());
     * ContextUtil.getInstance().initOrgInfo();
     */

    return true;
  }

  /**
   * 初始化当前线程容器
   */
  private void initContext(RestRequest request) {
    Context.clear();
    String ip = getClientIpAddress(request);
    Context.put(Context.clientIpAddress, ip);
    Context.put(Context.request, request);
    AppContext.initSession();
  }

  /**
   * add by wangab
   * @param request
   * @return
   */
  public static String getClientIpAddress(RestRequest request) {
    String ip = request.getHeader("dian-remote");
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("x-forwarded-for");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
      ip = request.getRemoteAddr();
    }
    if (ip.indexOf(",") > 0) ip = ip.substring(0, ip.indexOf(","));
    return ip;
  }

  @Override
  public boolean finish(RestRequest arg0, RestResponse arg1) {
    Context.clear();
    AppContext.clearSession();
    return false;
  }

}
