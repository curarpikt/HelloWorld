package com.chanapp.chanjet.customer.util;

import com.chanjet.csp.ccs.api.common.DeviceInfo;
import com.chanjet.csp.ccs.api.common.DeviceType;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class DeviceUtil {
	public static final String ANDROIDTYPE = "Android";
	public static final String APPLETYPE = "Apple";
	public static final String APPLEINFO = "ios";
	public static final String WEBINFO = "web";
	public static final String WEBCODE = "2";
	public static final String ANDROIDCODE = "21";
	public static final String APPLECODE = "22";
	
	public static String getDeviceType(){
		DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
		String deviceType = "-";
		if(null != device){
			DeviceType type = device.getDeviceType();
			deviceType = type.name();
		}
		return deviceType;
	}
	
	public static String getClientType(){
		String clientType = WEBCODE;
		String deviceType = DeviceUtil.getDeviceType();
		if (ANDROIDTYPE.equals(deviceType)) {
			clientType = ANDROIDCODE;
		} else if (APPLETYPE.equals(deviceType)) {
			clientType = APPLECODE;
		}
		return clientType;
	}
	
	public static String getEndpointInfo(){
		String endpointInfo = WEBINFO;
		String deviceType = DeviceUtil.getDeviceType();
		if (ANDROIDTYPE.equals(deviceType)) {
			endpointInfo = ANDROIDCODE;
		} else if (APPLETYPE.equals(deviceType)) {
			endpointInfo = APPLEINFO;
		}
		return endpointInfo;
	}
}
