package com.chanapp.chanjet.customer.eventhandler.handler;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.EO;

public class SysHandlerRegister {
	  final static Map<String,SystemEntityHandlerItf> handlers = new HashMap<String,SystemEntityHandlerItf>();
	  public static void register(String... entityNames) {	
		    for (String entityName : entityNames) {
		    	if(EO.AppUser.equals(entityName)){		    		
		    		SystemEntityHandlerItf appUserHandler = new AppUserHandler();
		    		handlers.put("com.chanjet.system.systemapp.entity.AppUser", appUserHandler);
		    	}
		    }
	 }
	  
	  public static SystemEntityHandlerItf getSystemHandler(String entityName){
				 return handlers.get(entityName);
	  }
}
