package com.chanapp.chanjet.customer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class CsvUtil {

    private static final Log log = LogFactory.getLog(CsvUtil.class);

    public static String parseList2CsvData(String[] filedsName, List<Map<String, Object>> list) {
        List<String[]> csvData = new ArrayList<String[]>();
        if (list != null && list.size() > 0 && filedsName != null && filedsName.length > 0) {
            Iterator<Map<String, Object>> it = list.iterator();
            while (it.hasNext()) {
                List<String> values = parseMap2CsvData(it.next(), filedsName);
                String[] filedValues = new String[values.size()];
                values.toArray(filedValues);
                csvData.add(filedValues);
            }
        }
        return CsvUtil.csvData2String(filedsName, csvData);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static List<String> parseMap2CsvData(Map<String, Object> map, String[] filedsName) {
        List<String> csvList = new ArrayList<String>();
        if (filedsName != null && map != null) {
            for (int i = 0; i < filedsName.length; i++) {
                String field = filedsName[i];
                String key = field;
                Boolean containsChildren = false;
                if (key.indexOf(".") != -1) {
                    key = key.substring(0, key.indexOf("."));
                    containsChildren = true;
                }
                if (map.containsKey(key)) {
                    Object value = map.get(key);
                    if (value == null) {
                        csvList.add(null);
                    } else if (value instanceof Map) {
                        Map<String, Object> childrenMap = (Map) value;
                        if (containsChildren) {
                            String childProps = field.substring(field.indexOf(".") + 1);
                            Object childValue = childrenMap.get(childProps);
                            if (childValue == null) {
                                csvList.add(null);
                            } else {
                                csvList.add(toStr(toBytes(childValue + "")));
                            }
                        } else {
                            Set<String> childPropsFileds = childrenMap.keySet();
                            String[] filedname = new String[childPropsFileds.size()];
                            childPropsFileds.toArray(filedname);
                            csvList.addAll(parseMap2CsvData(childrenMap, filedname));
                        }
                    } else {
                        csvList.add(toStr(toBytes(value.toString())));
                    }
                } else {
                    csvList.add(null);
                }
            }
        }
        return csvList;
    }

    private static String csvData2String(String[] filedName, List<String[]> filedsValues) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = null;
        CSVWriter csvWriter = null;
        try {
            streamWriter = new OutputStreamWriter(baos, "UTF-8");
            csvWriter = new CSVWriter(streamWriter, CSVWriter.DEFAULT_SEPARATOR);
            csvWriter.writeNext(filedName);
            Iterator<String[]> it = filedsValues.iterator();
            while (it.hasNext()) {
                String[] filedsValue = it.next();
                csvWriter.writeNext(filedsValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("csvData2String exception :", e);
        } finally {
            try {
                if (null != csvWriter) {
                    csvWriter.flush();
                }
                if (null != streamWriter) {
                    streamWriter.flush();
                }
                if (null != csvWriter) {
                    csvWriter.close();
                }
                if (null != streamWriter) {
                    streamWriter.close();
                }
                if (baos != null) {
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                log.error("csvData2String close exception :", e);
            }
        }
        String result = "";
        try {
            result = baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = baos.toString();
            log.error("baos.toStrin() exception :", e);
        }
        return result;
    }

    private static byte[] toBytes(String s) {
        try {
            return s != null ? s.getBytes("UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            log.error("toBytes Exception : ", e);
        }
        return null;
    }

    private static String toStr(byte[] b) {
        try {
            return b != null ? new String(b, "UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            log.error("toStr Exception : ", e);
        }
        return null;
    }
}
