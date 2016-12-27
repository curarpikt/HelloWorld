package com.chanapp.chanjet.customer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.reader.OssReader;
import com.chanjet.ccs.oss.service.OssService;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class CspCssUtil {
    private static final Logger logger = LoggerFactory.getLogger(CspCssUtil.class);
	public static OssService getOssService() {
		try{
			String appKey=EnterpriseContext.getAppKey();
			String appSecret=EnterpriseContext.getAppSecret();
			String orgId=EnterpriseContext.getOrgId(); 
			String userId=EnterpriseContext.getCurrentUser().getUserId();
			String token=EnterpriseContext.getToken();
			String appId=EnterpriseContext.getAppId();
			OssService ossService = new OssService(appKey,appSecret,orgId,userId,token,appId,OssReader.getDomain("oss.upload.domain"));
			logger.info("new ossService begin:appKey:"+appKey+",appSecret:"+appSecret+",orgId"+orgId+",userId:"+userId+",token:"+token+",appId:"+appId);	
			//System.out.println("new ossService begin:appKey:"+appKey+",appSecret:"+appSecret+",orgId"+orgId+",userId:"+userId+",token:"+token+",appId:"+appId+",url:"+OssReader.getDomain("oss.upload.domain"));
			return ossService;
		}catch(Exception e){
			logger.error("new ossService eorror:"+e);
			throw e;
		}
		
	}
}
