package com.chanapp.chanjet.customer.restlet.v2.rest;

import java.util.Map;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;


/**
 *    
 * <p>
 * 初始化用户
 *</p>
 * @author lf </br>
 * @Email lufane@chanjet.com 
 * @date 2016年3月17日 下午2:05:06
 *  
 *     
 */
public class Init extends BaseRestlet {

	@Override
	public Object run() { 
	    UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
		ProcessResult result = new ProcessResult(true);
		Map<String, Object> retMap = userService.initUser(boDataAccessManager.getBoSession());
		result.setData(retMap);
		result.setSuccess(true);
	    return result;
	}

}
