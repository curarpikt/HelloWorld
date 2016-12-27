package com.chanapp.chanjet.customer.restlet.v2.rest.workrecord;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestAttachment;

/**
 * 工作记录文件上传
 * 
 * @author tds
 *
 */
public class AttamentUpload extends BaseRestlet {
	@Override
	public Object run() {
		List<RestAttachment> attachments = this.getAtts();
		Assert.notNull(attachments);
		String category = null;
		RestAttachment uploadData = null;
		for (RestAttachment attach : attachments) {
			String ct = attach.getHeader("Content-Type");
			if (ct != null && (ct.equals("application/octet-stream") || ct.equals("image/jpeg")||ct.equals("multipart/form-data"))) {
				uploadData = attach;
			}
			if (attach.getContentDispositionParameter("name") != null
					&& attach.getContentDispositionParameter("name").equals("category")) {
				DataHandler dh = attach.getDataHandler();
				try {
					BufferedReader reader = new BufferedReader(new InputStreamReader(dh.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line = null;
					while ((line = reader.readLine()) != null) {
						sb.append(line);
					}
					category =sb.toString();
					//System.out.println(sb.toString());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return updateFile(uploadData, category);
	}

	private Map<String, Object> updateFile(RestAttachment attach, String category) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		DataHandler dh = attach.getDataHandler();
		String fileName = dh.getName();
		if (fileName == null) {
			fileName = attach.getContentDispositionParameter("filename");
		}
		try {
			fileName = new String(fileName.getBytes("ISO8859-1"), "UTF-8");

			if ("audio".equals(category)) {
				String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
				if (!"spx".equalsIgnoreCase(suffix)) {
					throw new AppException("app.upload.audio.transformerror");
				}
				retMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).uploadTransFileToMp3(fileName,
						dh.getInputStream());
			} else {
				retMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).uploadFile(fileName,
						dh.getInputStream());
				if ("image".equals(category)) {
					ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).cutImage(retMap);
				}
			}
			retMap.put("result", true);
			retMap.put("category", category);
		} catch (Exception e) {
			throw new AppException(e.getMessage());
		}
		return retMap;
	}

}