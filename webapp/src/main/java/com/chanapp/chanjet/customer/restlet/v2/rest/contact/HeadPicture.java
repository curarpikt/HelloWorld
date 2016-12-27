package com.chanapp.chanjet.customer.restlet.v2.rest.contact;

import java.io.IOException;
import java.io.InputStream;
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
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestAttachment;
import com.chanjet.csp.ui.util.OSSUtil;

/**
 * 联系人头像上传
 * 
 * @author tds
 *
 */
public class HeadPicture extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(HeadPicture.class);

    @Override
    public Object run() {
        List<RestAttachment> attachments = this.getAtts();
        Assert.notNull(attachments);

        Map<String, String> retMap = new HashMap<String, String>();

        RestAttachment attach = attachments.get(0);

        DataHandler dh = attach.getDataHandler();
        String ct = attach.getHeader("Content-Type");
        if (ct.equals("application/octet-stream") || ct.equals("image/jpeg")) {
            String fileName = dh.getName();
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).isImageSuffix(suffix)) {
                Map<String, Object> result = null;
                InputStream is;
                try {
                    is = dh.getInputStream();
                    result = OSSUtil.uploadImage(fileName, is);
                } catch (IOException e1) {
                    logger.error("OSSUtil.uploadFile({}) error:{}", fileName, e1.getMessage());
                }

                if (result == null || result.get("url") == null || result.get("width") == null
                        || result.get("height") == null) {
                    throw new AppException("app.upload.uploadfailed");
                }
                String url = result.get("url").toString();
                int width = Integer.parseInt(result.get("width").toString());
                int height = Integer.parseInt(result.get("height").toString());
                try {
                    retMap = OSSUtil.cropImage(url, 0, 0, width, height);
                    if (retMap != null) {
                        retMap.put("result", "true");
                    }
                } catch (Exception e) {
                    retMap.put("result", "false");
                    retMap.put("info", e.getMessage());
                }
            }
        }

        return retMap;
    }

}