package com.chanapp.chanjet.customer.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.chanapp.chanjet.customer.util.PinyinUtil;

public class CspEntityRestObject {

    private List<String> customFields = new ArrayList<String>();

    private Map<String, FieldMetaData> fields = new LinkedHashMap<String, FieldMetaData>();

    public List<String> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<String> customFields) {
        this.customFields = customFields;
    }

    public Map<String, FieldMetaData> getFields() {
        return fields;
    }

    public void setFields(Map<String, FieldMetaData> fields) {
        this.fields = fields;
    }

    public void addField(FieldMetaData fieldMetaData) {
        this.fields.put(fieldMetaData.name, fieldMetaData);
    }

    public void sortField() {
        List<Map.Entry<String, FieldMetaData>> list = new LinkedList<Map.Entry<String, FieldMetaData>>();
        Map<String, FieldMetaData> localFields = this.getFields();
        Iterator<Entry<String, FieldMetaData>> it = localFields.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, FieldMetaData> entry = it.next();
            list.add(entry);
        }

        Collections.sort(list, new FieldMetaDataComparator());
        Map<String, FieldMetaData> newFields = new LinkedHashMap<String, FieldMetaData>();
        for (Iterator<Map.Entry<String, FieldMetaData>> ite = list.iterator(); ite.hasNext();) {
            Map.Entry<String, FieldMetaData> map = ite.next();
            newFields.put(map.getKey(), map.getValue());
        }
        this.fields = newFields;
    }

    public static class FieldMetaDataComparator implements Comparator<Map.Entry<String, FieldMetaData>> {

        @Override
        public int compare(Map.Entry<String, FieldMetaData> o1, Map.Entry<String, FieldMetaData> o2) {
            String fieldName1 = PinyinUtil.hanziToPinyinSimple(o1.getValue().label, false);
            String fieldName2 = PinyinUtil.hanziToPinyinSimple(o2.getValue().label, false);
            return fieldName1.compareTo(fieldName2);
        }

    }

}
