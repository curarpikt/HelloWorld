package com.chanapp.chanjet.customer.service.sync;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVWriter;

public class SyncTransCSV {
    private static final Logger logger = LoggerFactory.getLogger(SyncTransCSV.class);

    private static final String entityId = "entityId";
    private static final String entityType = "entityType";
    private static final String noDeleteIds = "noDeleteIds";
    private static final String deleteEntity = "deleteEntity";

    public static Map<String, Object> getCsvResult(List<ISyncEntity> entitys) {
        Map<String, Object> result = new TreeMap<String, Object>();
        List<Map<String, Object>> deleteDate = new ArrayList<Map<String, Object>>();
        Set<String> deleteField = new LinkedHashSet<String>();
        deleteField.add(entityId);
        deleteField.add(entityType);
        deleteField.add(noDeleteIds);
        for (ISyncEntity entity : entitys) {
            getDeleteData(entity, deleteDate);
            List<Map<String, Object>> data = entity.getEntityData();
            String entityName = entity.getEntityName();
            Set<String> entityFields = entity.getFieldSet();
            if (data == null || data.size() == 0)
                continue;
            result.put(entityName, getCsvStr(data, entityFields));
        }
        result.put(deleteEntity, getCsvStr(deleteDate, deleteField));
        return result;
    }

    private static void getDeleteData(ISyncEntity entity, List<Map<String, Object>> data) {
        Set<Long> ids = entity.getDeletedData();
        String entityName = entity.getEntityName();
        if (ids == null || ids.size() == 0)
            return;
        for (Long id : ids) {
            Map<String, Object> deleteMap = new HashMap<String, Object>();
            deleteMap.put(entityId, id);
            deleteMap.put(noDeleteIds, entity.getNoDeletedIds().get(id));
            deleteMap.put(entityType, entityName);
            data.add(deleteMap);
        }
    }

    public static String getCsvStr(List<Map<String, Object>> data, Set<String> fields) {
        List<List<String>> csvList = new ArrayList<List<String>>();
        for (Map<String, Object> line : data) {
            csvList.add(getCvsLineList(line, fields));
        }
        return csvData2String(csvList, fields);
    }

    private static List<String> getCvsLineList(Map<String, Object> data, Set<String> fields) {
        List<String> csvLine = new ArrayList<String>();
        for (String fieldname : fields) {
            if (data.get(fieldname) != null) {
                csvLine.add(data.get(fieldname).toString());
            } else {
                csvLine.add("");
            }
        }
        return csvLine;
    }

    private static String csvData2String(List<List<String>> data, Set<String> filedsName) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter streamWriter = null;
        // ICsvListWriter writer = null;
        CSVWriter csvWriter = null;
        try {
            streamWriter = new OutputStreamWriter(baos, "UTF-8");
            csvWriter = new CSVWriter(streamWriter, CSVWriter.DEFAULT_SEPARATOR, '\"');
            String[] filedname = new String[filedsName.size()];
            filedsName.toArray(filedname);
            csvWriter.writeNext(filedname);
            for (List<String> values : data) {
                String[] filedValues = null;
                if (values != null) {
                    filedValues = new String[values.size()];
                    for (int i = 0; i < values.size(); i++) {
                        String v = values.get(i);
                        filedValues[i] = toStr(toBytes(v));
                    }
                }
                csvWriter.writeNext(filedValues);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("csvData2String exception :", e);
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
                logger.error("csvData2String close exception :", e);
            }
        }
        String result = "";
        try {
            result = baos.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            result = baos.toString();
            logger.error("baos.toStrin() exception :", e);
        }
        return result;
    }

    public static byte[] toBytes(String s) {
        try {
            return s != null ? s.getBytes("UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            // throw new RuntimeException(e);
            logger.error("toBytes Exception : ", e);
        }
        return null;
    }

    public static String toStr(byte[] b) {
        try {
            return b != null ? new String(b, "UTF-8") : null;
        } catch (UnsupportedEncodingException e) {
            // throw new RuntimeException(e);
            logger.error("toStr Exception : ", e);
        }
        return null;
    }
}
