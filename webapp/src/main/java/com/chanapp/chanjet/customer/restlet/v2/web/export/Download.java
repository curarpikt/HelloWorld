package com.chanapp.chanjet.customer.restlet.v2.web.export;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.customer.util.Context;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.customer.util.UserAgentUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.rest.restlet.RestletUtils;

/**
 * 备份-下载
 * 
 * @author tds
 *
 */
public class Download extends BaseRestlet {
    @Override
    public Object run() {
        Long id = this.getParamAsLong("id");
    	String datestr = DateUtil.getDateStringByFormat(new Date(),"yyyyMMdd");
		String fileName = "客户管家备份数据-" + datestr ;//+ ".xls";
		Map<String, Object> retMap = ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class).download(id);
		if(retMap.containsKey("url")){
			String url =retMap.get("url").toString();
			return download(url,fileName);
		}
		return null;
    }
    
    private Object download(String url,String fileName){
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
