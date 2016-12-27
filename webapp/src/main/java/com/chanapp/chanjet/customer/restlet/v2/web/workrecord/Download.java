package com.chanapp.chanjet.customer.restlet.v2.web.workrecord;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;

import com.chanapp.chanjet.customer.util.Context;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.customer.util.UserAgentUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.rest.restlet.RestletUtils;

/**
 * 工作记录下载
 * 
 * @author tds
 *
 */
public class Download extends BaseRestlet {
    @Override
    public Object run() {
        String url = this.getParam("url");
        // Long workrecrodId = this.getParamAsLong("workrecrodId");
        String fileName = this.getParam("fileName");

        File file = null;
        try {
            URL resourceUrl = new URL(url);
            String suffix = url.substring(url.lastIndexOf(".") + 1);
            file = File.createTempFile("download", "." + suffix);
            String filename = file.getName();
            if (fileName != null) {
                filename = fileName + "." + suffix;
            }
            if (UserAgentUtil.isIE((RestRequest) Context.get(Context.request))) {
                filename = URLEncoder.encode(filename, "UTF-8");
            } else {
                filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
            }
            RestletUtils.setResponseType("application/octet-stream");

            FileUtil.copyURLToFile(resourceUrl, file);

            Object download = RestletUtils.createDownload(filename, file, "application/octet-stream");
            return download;
        } catch (Exception e) {
            throw new AppException(e.getMessage());
        } finally {
            if (file != null && file.exists()) {
                file.deleteOnExit();
            }
        }
    }

}
