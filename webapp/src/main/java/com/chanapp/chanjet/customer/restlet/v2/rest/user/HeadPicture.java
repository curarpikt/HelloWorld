package com.chanapp.chanjet.customer.restlet.v2.rest.user;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import javax.activation.DataHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestAttachment;
public class HeadPicture extends BaseRestlet {
	
    private static final Logger logger = LoggerFactory.getLogger(HeadPicture.class);
    @Override
    public Object run() {
        List<RestAttachment> attachments = this.getAtts();
        Assert.notNull(attachments);
        try {
            String name = null;
            File file = null;
            String fileName = null;
            for(RestAttachment attach:attachments){
                String ct = attach.getHeader("Content-Type");
                if (ct!=null&&(ct.equals("application/octet-stream") || ct.equals("image/jpeg")||ct.equals("multipart/form-data"))) {
                    DataHandler dh = attach.getDataHandler();
                    fileName = dh.getName();
                    String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
                    if (ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).isImageSuffix(suffix)) {
                        InputStream is = dh.getInputStream();
                        file = File.createTempFile("csp", ".jpg");
                        FileUtil.copyFile(is, file);
                    }
                } 
                return ServiceLocator.getInstance().lookup(UserServiceItf.class).uploadHeadPicture(name, file);
            }
   
        } catch (Exception e) {
            logger.error("user.HeadPicture.error", e);
            throw new AppException(e.getMessage());
        }
        return null;
    }

}
