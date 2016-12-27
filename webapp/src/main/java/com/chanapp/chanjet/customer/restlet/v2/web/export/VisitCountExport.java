package com.chanapp.chanjet.customer.restlet.v2.web.export;

import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.chanapp.chanjet.customer.service.exporttask.ExportTaskServiceItf;
import com.chanapp.chanjet.customer.util.Context;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.customer.util.UserAgentUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.rest.RestRequest;
import com.chanjet.csp.rest.restlet.RestletUtils;

/**
 * 外勤签到统计-导出
 * 
 * @author tds
 *
 */
public class VisitCountExport extends BaseRestlet {
    @Override
    public Object run() {
        Long groupId = this.getParamAsLong("groupId");
        Long userId = this.getParamAsLong("userId");
        String countDate = this.getParam("countDate");

        Map<String, Object> map = ServiceLocator.getInstance().lookup(ExportTaskServiceItf.class)
                .visitCountExport(groupId, userId, countDate);

        if (map != null) {
            String url = (String) map.get("url");
            if (url != null && !"".equals(url)) {
                File file = null;
                try {
                    URL resourceUrl = new URL(url);
                    String suffix = url.substring(url.lastIndexOf(".") + 1);
                    file = File.createTempFile("download", "." + suffix);

                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                    String datestr = df.format(new Date());
                    String filename = "外勤签到统计" + datestr + ".xls";

                    if (UserAgentUtil.isIE((RestRequest) Context.get(Context.request))) {
                        filename = URLEncoder.encode(filename, "UTF-8");
                    } else {
                        filename = new String(filename.getBytes("UTF-8"), "ISO8859-1");
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

        return map;

    }

}
