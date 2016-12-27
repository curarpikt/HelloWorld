package com.chanapp.chanjet.customer.util;

import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import com.chanjet.csp.common.base.rest.RestAttachment;

public class AttachmentUtil {
	public static  Map<String,RestAttachment>  getCategory(List<RestAttachment> attachments){
		String category = null;
		for(RestAttachment attach:attachments){
	           category = attach.getContentDispositionParameter("category");
	           if(category!=null)
	        	   continue;
	           DataHandler dh = attach.getDataHandler();
	           String ct = attach.getHeader("Content-Type");
	           if (ct.equals("application/octet-stream") || ct.equals("image/jpeg")) {
	        	   
	           }
		}
		return null;
	}
}
