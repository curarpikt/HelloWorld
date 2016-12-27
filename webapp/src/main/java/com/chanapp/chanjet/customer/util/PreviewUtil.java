package com.chanapp.chanjet.customer.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PreviewUtil {

    private static final Logger logger = LoggerFactory.getLogger(PreviewUtil.class);

    /**
     * 
     * 获得文件的后缀
     */
    private static String getSuffix(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf(".");
        if (index < 0) {
            return null;
        }
        return filename.substring(index + 1);
    }

    public static boolean convertToJpg(String path, String type) {
        String suffix = getSuffix(path);
        if (isConver(suffix)) {
            try {
            	CspCssUtil.getOssService().getConvertInfoForJpg(path);
            	//TODO PreviewService
/*                PreviewService preview = new PreviewServiceImpl();
                boolean bool = preview.createConvertJob(path, type);
                logger.info("bool={}", bool);
                if (bool) {
                    return true;
                }*/
            	return true;
            } catch (Exception e) {
                logger.error("convert error", e);
            }
        }
        return false;
    }
    
    
    public static String getConvertInfoForMp3(String path) {  
            try {
            	String url = CspCssUtil.getOssService().getConvertInfoForMp3(path);
            	return url;
            } catch (Exception e) {
                logger.error("convert error", e);
            }        
        return null;
    }

    private static boolean isConver(String suffix) {
        if (suffix == null) {
            return false;
        }
        suffix = suffix.toLowerCase();
        String types = ",doc,docx,ppt,pot,xls,xlsx,txt,pdf,pptx,";
        return types.contains("," + suffix + ",");
    }

}