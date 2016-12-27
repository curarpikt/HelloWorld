package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.rest.RestAttachment;

/**
 * 附件文件上传
 * 
 * @author tds
 *
 */
public class AttamentUploadOnly extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(AttamentUploadOnly.class);

    @Override
    public Object run() {
        List<RestAttachment> attachments = this.getAtts();
        Assert.notNull(attachments);

        Map<String, Object> retMap = new HashMap<String, Object>();

        for (RestAttachment attach : attachments) {
            DataHandler dh = attach.getDataHandler();
            String ct = attach.getHeader("Content-Type");
            if (ct!=null&&(ct.equals("application/octet-stream") || ct.equals("image/jpeg"))) {
                String fileName = dh.getName();

                try {
                    if (fileName == null) {
                        fileName = attach.getContentDispositionParameter("filename");
                    }

                    fileName = new String(fileName.getBytes("ISO8859-1"), "UTF-8");

                    retMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).uploadFile(fileName,
                            dh.getInputStream());
                    retMap.put("result", true);
                    break;
                } catch (Exception e) {
                    logger.error("error", e);
                    retMap.put("result", false);
                    retMap.put("message", e.getMessage());
                }

            }
        }
        return retMap;
    }

}