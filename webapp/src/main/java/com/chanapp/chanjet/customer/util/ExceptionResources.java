package com.chanapp.chanjet.customer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

/**
 * 异常处理信息类，用于读取和查找异常资源。 异常资源文件要求：
 * 
 * <pre>
 * 异常信息资源文件放在应用的resource目录。
 * 平台异常信息资源文件默认为csp.exception.csv，应用也可以加入自己的资源文件，要求名称为app.exception.csv。
 * 为了支持多语，可以在文件名中加入“_<locale>”作为文件名，如 csp.exception_zh.csv表示中文。
 * 查询资源时，会根据当前locale优先搜索对应locale的资源，没有时，再使用默认的资源。app资源优先于system资源优先搜索。
 * </pre>
 * 
 * @author wolf
 *
 */
public class ExceptionResources {
    private static Logger logger = LoggerFactory.getLogger(ExceptionResources.class);

    private final static String APP_FILE = "app.exception.csv";

    private static Map<String, Map<String, Map<String, String>>> fileResoures = new LinkedHashMap<String, Map<String, Map<String, String>>>();

    /**
     * 返回指定消息的资源信息
     * 
     * @param message 消息字符串名称
     * @return 如果没有找到，返回null, 如果找到，返回包含code,和info的资源Map对象
     */
    public static Map<String, String> getResources(String message) {
        Map<String, Map<String, String>> map = ExceptionResources.fileResoures.get(APP_FILE);
        if (map == null) {
            map = ExceptionResources.loadResources(APP_FILE);
            ExceptionResources.fileResoures.put(APP_FILE, map);
        }
        Map<String, String> item = map.get(message);
        if (item != null) {
            return item;
        }

        return null;
    }

    /**
     * 根据指定的文件资源，装入资源文件，如果资源文件不存在，返回空值Map
     * 
     * @param filename 资源文件名
     * @return Map对象,Key为资源名称，值为Map(code,info)
     */
    private static Map<String, Map<String, String>> loadResources(String filename) {
        Map<String, Map<String, String>> map = new LinkedHashMap<String, Map<String, String>>();
        InputStream in = ExceptionResources.class.getClassLoader().getResourceAsStream(filename);
        if (in == null) {
            logger.warn("Cannot find the resource file:" + filename);
            return map;
        }

        ICsvMapReader mapReader = null;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF8"));
            mapReader = new CsvMapReader(br, CsvPreference.STANDARD_PREFERENCE);

            // the header columns are used as the keys to the Map
            final String[] header = mapReader.getHeader(true);

            Map<String, Object> valueMap;
            CellProcessor[] processors = new CellProcessor[header.length];
            while ((valueMap = mapReader.read(header, processors)) != null) {
                String message = (String) valueMap.get("message");
                if (message == null) {
                    continue;
                }
                String code = (String) valueMap.get("code");
                String info = (String) valueMap.get("info");
                if (code == null) {
                    code = "-1";
                }

                Map<String, String> item = new LinkedHashMap<String, String>();
                item.put("code", code);
                item.put("info", info);
                map.put(message, item);
            }
        } catch (IOException e) {
            logger.error("Invalid resource file:" + filename);
        } finally {
            if (mapReader != null) {
                try {
                    mapReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}