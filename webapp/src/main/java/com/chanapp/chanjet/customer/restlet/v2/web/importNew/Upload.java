package com.chanapp.chanjet.customer.restlet.v2.web.importNew;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.rest.RestAttachment;
import com.chanjet.csp.ui.util.OSSUtil;

/**
 * @author tds
 *
 */
public class Upload extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(Upload.class);

    @Override
    public Object run() {
        List<RestAttachment> attachments = this.getAtts();
        Assert.notNull(attachments);
        
        RestAttachment fileDataAttachment = null;
        String filename = null;
        for (RestAttachment attachment : attachments) {
        	String contentName = attachment.getContentDispositionParameter("name");
        	if ("filedata".equals(contentName.toLowerCase())) {
        		fileDataAttachment = attachment;
        		filename = fileDataAttachment.getContentDispositionParameter("filename");
        		break;
        	}
        }        
        Assert.notNull(fileDataAttachment);
        
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        Map<String, Object> data = new HashMap<String, Object>();
        DataHandler fileDataHandler = fileDataAttachment.getDataHandler();
        String mediaType = fileDataAttachment.getHeader("Content-Type");
        try {   
            filename = new String(filename.getBytes("ISO8859-1"), "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 检查类型
        String suffix = OSSUtil.getSuffix(filename);
        if (suffix == null) {
            suffix = "";
        }
        suffix = suffix.toLowerCase();
        if ((!mediaType.toString().equals("application/octet-stream")) && suffix.equals("xls")
                && suffix.equals("xlsx")) {
            return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class)
                    .getErrorMsg("app.import.excel.format");
        }
        // 创建临时文件并解析excel head        
        File tmpFile = null;
        InputStream is = null;
        try {        	    	
            is = fileDataHandler.getInputStream();
            tmpFile = File.createTempFile("customer_excel", "." + suffix);
            FileUtil.copyInputStreamToFile(is, tmpFile);
            data = OSSUtil.uploadFile(filename, tmpFile);
            if (data != null) {
                String url = (String) data.get("url");
                Map<String, Object> rs = ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class)
                        .uploadSuf(url, filename);
                Object error = rs.get("error");
                if (error != null) {
                    return error;
                } else {
                    result.put("id", (Long) rs.get("id"));
                    result.put("result", true);
                    return result;
                }
            } else {
                logger.error("oss upload no back  on importNewController");
                return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class)
                        .getErrorMsg("app.common.server.error");
            }
        } catch (Exception e) {
            if (tmpFile != null) {
                tmpFile.delete();
            }
            return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class)
                    .getErrorMsg("app.common.server.error");
        }
    }

}
