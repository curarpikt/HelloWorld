package com.chanapp.chanjet.customer.restlet.v2.web.importNew.excel;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
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
 * 下载excel模板
 * 
 * @author tds
 *
 */
public class Template extends BaseRestlet {
    @Override
    public Object run() {

        Map<String, Object> rs = ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).excelTemplate();

        boolean result = (boolean) rs.get("result");
        String url = (String) rs.get("url");
        String message = (String) rs.get("message");
        if (!result) {
            throw new AppException(message);
        }
        String datestr = DateUtil.getDateStringByFormat(new Date(), "yyyyMMdd");
        File file = null;
        try {

            URL resourceUrl = new URL(url);
            String suffix = url.substring(url.lastIndexOf(".") + 1);
            file = File.createTempFile("download", "." + suffix);

            String _filename = "客户管家数据准备模板-" + datestr + ".xls";
            String filename = "";
            // 兼容IE下文件名乱码问题
            if (UserAgentUtil.isIE((RestRequest) Context.get(Context.request))) {
                filename = URLEncoder.encode(_filename, "UTF-8");
            } else {
                filename = new String(_filename.getBytes("UTF-8"), "ISO-8859-1");
            }
            RestletUtils.setResponseType("application/vnd.ms-excel");

            FileUtil.copyURLToFile(resourceUrl, file);

            Object download = RestletUtils.createDownload(filename, file, "application/vnd.ms-excel");
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
