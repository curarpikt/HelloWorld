package com.chanapp.chanjet.customer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.rest.restlet.RestletFilter;
import com.chanjet.csp.rest.restlet.RestletRequestContext;
import com.chanjet.csp.rest.restlet.RestletUtils;

public class CustomRestletFilter  implements RestletFilter{
    private final static Logger logger = LoggerFactory.getLogger(CustomRestletFilter.class);
	@Override
	public Object filter(RestletRequestContext requestContext) {		
		Object obj = null;
		String payload = requestContext.getPayload();
//		requestContext.getQueryParameters();
//		System.out.println("PayloadType:"+requestContext.getPayloadType());
//		System.out.println("methodName:"+requestContext.getClassMethodName());
//		System.out.println("payload:"+payload);
//		System.out.println("className:"+requestContext.getRestletClassName());
		logger.info("methodName:"+requestContext.getClassMethodName());
		logger.info("payload:"+payload);
		logger.info("className:"+requestContext.getRestletClassName());
		obj = RestletUtils.invokeRestletMethod(requestContext);
		String retInfo = AppWorkManager.getDataManager().toJSONString(obj);
		//System.out.println("return :"+retInfo);
		logger.info("return:"+retInfo);
		// TODO Auto-generated method stub
		return obj;
	}

}
