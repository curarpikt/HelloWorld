package com.chanapp.chanjet.customer.service.importrecordnew;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.DataHelper;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.exception.BOApplicationException;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;

public class ImportUtil {
    private final static Logger logger = LoggerFactory.getLogger(ImportUtil.class);

    protected static final IAppMetadataManager metaDataManager = AppWorkManager.getAppMetadataManager();

	private static List<Long> importIdList = new Vector<Long>();
		
	public static boolean importInProgess(Long recordId){
		return importIdList.contains(recordId);
	}
	
	public static void setInProgess(Long recordId){
		if(!importIdList.contains(recordId)){
			importIdList.add(recordId);
		}	
	}
	
	public static void setDone(Long recordId){
		importIdList.remove(recordId);
	}
	
    public ImportUtil() {

    }

    public static Timestamp getTimestampByString(String str) {
        Timestamp ts = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date d = sdf.parse(str);
            ts = new Timestamp(d.getTime());
        } catch (Exception e) {
            logger.error("getTimestampByString error", e);
        }
        return ts;
    }

    public static boolean isLong(String str) {
        boolean bool = false;
        try {
            Long.valueOf(str);
            bool = true;
        } catch (NumberFormatException e) {
            logger.error("isLong error", e);
        }
        return bool;
    }

    public static boolean isDate(String str) {
        if (StringUtils.isEmpty(str)) {
            return true;
        }
        String[] dd = str.split("-");
        if (dd.length != 3) {
            return false;
        }
        String year = dd[0];
        String month = dd[1];
        String day = dd[2];
        if (!isNumeric(year) || !isNumeric(month) || !isNumeric(day)) {
            return false;
        }
        if (year.length() != 4) {
            return false;
        }
        if (month.length() != 1 && month.length() != 2) {
            return false;
        }
        if (day.length() != 1 && day.length() != 2) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            sdf.setLenient(false);
            sdf.parse(str);
            return true;
        } catch (Exception e) {
            logger.error("isDate error", e);
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        if (StringUtils.isNumeric(str)) {
            return true;
        }
        return false;
    }

    public static Object parseValue(String type, String str) {
        Object text = str;
        type = type.toLowerCase();
        switch (type) {
            case "primarykey":
                break;
            case "foreignkey":
                break;
            case "timestamp":
                text = getTimestampByString(str);
                break;
            case "integer":
                if (!isNumeric(str)) {
                    text = null;
                }
                break;
            case "text":
                break;
            case "phone":
                break;
            case "long":
                if (!isLong(str)) {
                    text = null;
                }
                break;
            case "mobile_phone":
                break;
            case "email":
                break;
            case "date":
                if (!isDate(str)) {
                    text = null;
                }
                break;
            case "uri":
                break;
        }
        return text;
    }

    /**
     * 
     * 获得元数据
     */
    public static CustomerMetaData getMeta() {
        CustomerMetaData cacheMetaData = new CustomerMetaData();
        try {
            cacheMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
        } catch (Exception e) {

        }
        return cacheMetaData;
    }

    /**
     * 
     * 根据枚举值名称获得对应的values
     */
    public static List<CSPEnumValue> getEnum(String enumName) {
        List<CSPEnumValue> enums = new ArrayList<CSPEnumValue>();
        List<CSPEnumValue> enumsActive = new ArrayList<CSPEnumValue>();
        if (enumName == null) {
            return enums;
        }
        CSPEnum cspEnum = getMeta().getEnums().get(enumName);
        if (cspEnum == null) {
            return enums;
        }
        enums = cspEnum.getEnumValues();
        int i = 0;
        int size = enums.size();
        for (i = 0; i < size; i++) {
            if (enums.get(i) != null && enums.get(i).getIsActive()) {
                enumsActive.add(enums.get(i));
            }
        }
        return enumsActive;
    }

    /**
     * 
     * 获得所有枚举值包含启动和停用的
     */
    public static List<CSPEnumValue> getAllEnum(String enumName) {
        List<CSPEnumValue> enums = new ArrayList<CSPEnumValue>();
        if (enumName == null) {
            return enums;
        }
        CSPEnum cspEnum = getMeta().getEnums().get(enumName);
        if (cspEnum == null) {
            return enums;
        }
        enums = cspEnum.getEnumValues();
        return enums;
    }

    /**
     * 
     * 枚举值转换成字符串数组
     */
    public static String[] CSPEnumValue2StringArr(List<CSPEnumValue> list) {
        int i = 0;
        int size = list.size();
        String str[] = new String[size];
        String label = "";
        for (i = 0; i < size; i++) {
            label = list.get(i).getEnumLabel();
            if (label == null) {
                label = "";
            }
            str[i] = label;
        }
        return str;
    }

    /**
     * 
     * 根据枚举值label获得对应的枚举值
     */
    public static String getEnumValue(String enumName, String label) {
        if (StringUtils.isEmpty(enumName)) {
            return null;
        }
        List<CSPEnumValue> cspEnumValues = getEnum(enumName);
        int i = 0;
        int size = cspEnumValues.size();
        String enumValue = null;
        for (i = 0; i < size; i++) {
            if (cspEnumValues.get(i).getEnumLabel().equals(label)) {
                enumValue = cspEnumValues.get(i).getEnumValue();
                break;
            }
        }
        return enumValue;
    }

    private static void setMetadataLength(FieldMetaData metaData, String key) {
        if (key.equals("name")) {
            metaData.length = 64;
        } else if (key.equals("address")) {
            metaData.length = 128;
        } else if (key.equals("remark")) {
            metaData.length = 255;
        } else if (key.equals("url")) {
            metaData.length = 64;
        } else if (key.equals("phone")) {
            metaData.length = 64;
        } else if (key.equals("url")) {
            metaData.length = 64;
        } else if (key.equals("fax")) {
            metaData.length = 64;
        }
    }

    private static void setMetadataLength2(FieldMetaData metaData, String key) {
        if (key.equals("name")) {
            metaData.length = 16;
        } else if (key.equals("department")) {
            metaData.length = 64;
        } else if (key.equals("position")) {
            metaData.length = 64;
        } else if (key.equals("appellation")) {
            metaData.length = 16;
        } else if (key.equals("mobile")) {
            metaData.length = 64;
        } else if (key.equals("phone")) {
            metaData.length = 64;
        } else if (key.equals("email")) {
            metaData.length = 64;
        } else if (key.equals("qq")) {
            metaData.length = 64;
        }

    }

    public ArrayList<Map<String, Object>> getHead1(Map<String, Object> data) throws BOApplicationException {
        String customerIngore = "modifiedTime";
        IEntity metaCustomerEntity = metaDataManager.getEntityByName("Customer");
        Map<String, IField> customerMetaData = metaCustomerEntity.getFields();
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String[] customerEditFields = LayoutManager.getCustomerEditFields();
        List<String> editCustomer = Arrays.asList(customerEditFields);
        boolean isHiddenField = false;
        for (String key : customerMetaData.keySet()) {
            if(data!=null&&data.containsKey(key)){
            	isHiddenField = true;
            }
            if (customerIngore.contains(key)) {
                continue;
            }
       
            if (!editCustomer.contains(key) && !"owner".equals(key)&&isHiddenField==false) {            
                continue;
            }
            IField customerMeta = customerMetaData.get(key);
            FieldMetaData metaData = DataHelper.convertFieldMetadata(customerMeta);
            String[] enumList = {};
            if (metaData.type.equals(FieldTypeEnum.CSP_ENUM)) {
                enumList = CSPEnumValue2StringArr(ImportUtil.getEnum(metaData.enumName));
            }
            Map<String, Object> tmp = new HashMap<String, Object>();
            if (key.equals("owner")) {
                tmp.put("label", "业务员");
            } else {
                tmp.put("label", metaData.label);
            }

            setMetadataLength(metaData, key);

            tmp.put("field", key);
            tmp.put("enum", enumList);
            tmp.put("enumName", metaData.enumName);
            tmp.put("editable", metaData.editable);
            tmp.put("length", metaData.length);
            tmp.put("type", metaData.type.value());
            list.add(tmp);
        }
        return sortLabel(list, "Customer");
    
    }
    /**
     * 获得导出sheet1的head信息
     */
    public ArrayList<Map<String, Object>> getHead1() throws BOApplicationException {
    	return getHead1(null);
    }

    /**
     * 获得导出sheet1的head信息
     */
    public ArrayList<Map<String, Object>> getHead2() throws BOApplicationException {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        String contactIngore = "logo,modifiedTime,customer";
        IEntity metaContactEntity = metaDataManager.getEntityByName("Contact");

        Map<String, IField> contactMetaData = metaContactEntity.getFields();
        String[] contactEditFields = LayoutManager.getContactEditFields();
        List<String> trimedEditContactFields = new ArrayList<String>();
        for (String aField : contactEditFields) {
        	trimedEditContactFields.add(aField.trim());
        }

        // 取客户姓名
        IEntity metaCustomerEntity = metaDataManager.getEntityByName("Customer");
        Map<String, IField> customerMetaData = metaCustomerEntity.getFields();
        addCustomerFields(list, customerMetaData);

        for (String key : contactMetaData.keySet()) {
            if (contactIngore.contains(key)) {
                continue;
            }
            if (!trimedEditContactFields.contains(key) && !"owner".equals(key)) {
                continue;
            }
            IField contactMeta = contactMetaData.get(key);
            FieldMetaData metaData = DataHelper.convertFieldMetadata(contactMeta);
            // FieldMetaData metaData = contactMetaData.get(key);
            String[] enumList = {};
            if (metaData.type.equals(FieldTypeEnum.CSP_ENUM)) {
                enumList = CSPEnumValue2StringArr(ImportUtil.getEnum(metaData.enumName));
            }

            setMetadataLength2(metaData, key);

            Map<String, Object> tmp = new HashMap<String, Object>();

            if (key.equals("owner")) {
                tmp.put("label", "业务员");
            } else {
                tmp.put("label", metaData.label);
            }
            tmp.put("field", key);
            tmp.put("enum", enumList);
            tmp.put("enumName", metaData.enumName);
            tmp.put("editable", metaData.editable);
            tmp.put("length", metaData.length);
            tmp.put("type", metaData.type);
            list.add(tmp);
        }
        return sortLabel(list, "Contact");
    }

    /**
     * 获得导出sheet3的head信息
     */
    public ArrayList<Map<String, Object>> getHead3() throws BOApplicationException {

        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        // 工作记录
        IEntity metaWorkRecordEntity = metaDataManager.getEntityByName("WorkRecord");
        Map<String, IField> fldWorkRecordMetas = metaWorkRecordEntity.getFields();
        // 取客户姓名
        IEntity metaCustomerEntity = metaDataManager.getEntityByName("Customer");
        Map<String, IField> customerMetaData = metaCustomerEntity.getFields();
        addCustomerFields(list, customerMetaData);

        IField meta = null;

        for (String key : fldWorkRecordMetas.keySet()) {
            if (key.equals("content") || key.equals("owner")) {// ||key.equals("createddate")
                                                               // 不需要时间的
                meta = fldWorkRecordMetas.get(key);
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                Map<String, Object> tmp = new HashMap<String, Object>();
                if (key.equals("owner")) {
                    tmp.put("label", "业务员");
                } else {
                    tmp.put("label", fieldMeta.label);
                }

                tmp.put("field", key);
                tmp.put("enum", new String[0]);
                tmp.put("enumName", fieldMeta.enumName);
                tmp.put("editable", fieldMeta.editable);
                tmp.put("length", fieldMeta.length);
                tmp.put("type", fieldMeta.type);
                list.add(tmp);
            }
        }
        return sortLabel(list, "WorkRecord");
    }

    /**
     * 
     * excel head重新处理成map
     */
    private static Map<String, Map<String, Object>> getLabel(ArrayList<Map<String, Object>> label) {
        Map<String, Map<String, Object>> list = new HashMap<String, Map<String, Object>>();
        int size = label.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            Map<String, Object> map = label.get(i);
            String key = map.get("field").toString();
            list.put(key, map);
        }
        return list;
    }

    /**
     * 
     * 根据指定字段重新排序
     */
    public static ArrayList<Map<String, Object>> sortLabel(ArrayList<Map<String, Object>> label, String entityName) {
        ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String fields = "";
        if ("Customer".equals(entityName)) {
            fields = "name,owner,type,level,derive,industry,area,address,phone,fax,url,remark";
        }
        if ("Contact".equals(entityName)) {
            fields = "Customer_name,name,owner,phone,mobile,email,qq,department,position,gender,appellation,remark";
        }
        if ("WorkRecord".equals(entityName)) {
            fields = "Customer_name,content,owner";
        }
        Map<String, Map<String, Object>> labelMap = getLabel(label);
        String fieldsArr[] = fields.split(",");
        for (int i = 0; i < fieldsArr.length; i++) {
            String f = fieldsArr[i];
            if (labelMap.containsKey(f)) {
                list.add(labelMap.get(f));
                labelMap.remove(f);
            }
        }
        if (!labelMap.isEmpty()) {
            for (String key : labelMap.keySet()) {
                list.add(labelMap.get(key));
            }
        }
        return list;
    }

    /**
     * 
     * 根据字段获得label名称
     */
    public static String getLabelByField(String type, String field) {
        String label = "";
        Map<String, IField> fields = null;
        if (type.equals("Customer")) {
            // 客户字段
            IEntity customerEntity = metaDataManager.getEntityByName("Customer");
            fields = customerEntity.getFields();
        } else if (type.equals("Contact")) {
            // 联系人字段
            IEntity contactEntity = metaDataManager.getEntityByName("Contact");
            fields = contactEntity.getFields();
        } else if (type.equals("WorkRecord")) {
            // 工作记录字段
            IEntity workRecordEntity = metaDataManager.getEntityByName("WorkRecord");
            fields = workRecordEntity.getFields();
        }
        if (fields == null) {
            return label;
        }
        for (String key : fields.keySet()) {
            IField meta = fields.get(key);
            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
            if (key.equals(field)) {
                label = fieldMeta.label == null ? "" : fieldMeta.label.trim();
                if (key.equals("owner")) {
                    label = "业务员";
                }
                break;
            }
        }
        return label;

    }

    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> getMetadata(Map<String, Object> data, long sheetType)
            throws BOApplicationException {
        List<Map<String, Object>> head = null;
        if (sheetType == 0) {
            head = getHead1(data);
        }
        if (sheetType == 1) {
            head = getHead2();
        }
        if (sheetType == 2) {
            head = getHead3();
        }
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int size = head.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            Map<String, Object> field = head.get(i);
            String key = field.get("field").toString();
            if (data.containsKey(key)) {
                Map tmp = (Map) data.get(key);
                field.put("value", tmp.get("value"));
                list.add(field);
            }
        }
        return list;
    }

    /**
     * 计算字符串单字节长度 利用正则表达式将每个中文字符转换为"**" 匹配中文字符的正则表达式： [\u4e00-\u9fa5]
     * 匹配双字节字符(包括汉字在内)：[^\x00-\xff]
     * 
     * @param validateStr
     * @return
     */
    public static int getRegExpLength(String validateStr) {
        if (validateStr == null) {
            return 0;
        }
        // String temp = validateStr.replaceAll("[\u4e00-\u9fa5]", "**");
        String temp = validateStr.replaceAll("[^\\x00-\\xff]", "**");
        return temp.length();
    }

    private static Map<String, Object> getCustomerNameField(Map<String, IField> customerMetaData) {
        for (String key : customerMetaData.keySet()) {
            IField customerMeta = customerMetaData.get(key);
            FieldMetaData metaData = DataHelper.convertFieldMetadata(customerMeta);
            String[] enumList = {};
            Map<String, Object> tmp = new HashMap<String, Object>();
            if (key.equals("name")) {
                tmp.put("label", metaData.label);
                metaData.length = 64;
                tmp.put("field", "Customer_" + key);
                tmp.put("enum", enumList);
                tmp.put("enumName", metaData.enumName);
                tmp.put("editable", metaData.editable);
                tmp.put("length", metaData.length);
                tmp.put("type", metaData.type.value());
                return tmp;
            }
        }

        return null;
    }

    private static void addCustomerFields(ArrayList<Map<String, Object>> list, Map<String, IField> customerMetaData) {
        Map<String, Object> field = getCustomerNameField(customerMetaData);
        if (field != null) {
            list.add(field);
        }
    }

    private static void addCustomerFields(Map<String, Map<String, Object>> fields, Map<String, IField> customerFields) {
        Map<String, Object> field = getCustomerNameField(customerFields);
        if (field != null) {
            fields.put((String) field.get("label"), field);
        }
    }

    /**
     * 
     * 根据label获得对应sheet字段信息(自动判断是那个sheet 暂不用
     */
    public static Map<String, Map<String, Object>> getFieldsNew(List<String> headers, int sheetNo) {
        String ownerLabel = "业务员";
        // 客户head
        List<String> customerHeader = new ArrayList<String>();
        // 联系人head
        List<String> contactHeader = new ArrayList<String>();
        // 工作记录 head
        List<String> workrecordHeader = new ArrayList<String>();
        // 客户字段
        int i = 0;
        int size = headers.size();

        int currSheet = 0; // 看是获得那个sheet的 title
        for (i = 0; i < size; i++) {
            String label = headers.get(i);
            label = label.trim();
            if (sheetNo == 0) {
                customerHeader.add(label);
                currSheet = 0;
            } else if (sheetNo == 1) {
                currSheet = 1;
                contactHeader.add(label);
            } else if (sheetNo == 2) {
                currSheet = 2;
                workrecordHeader.add(label);
            }
        }

        Map<String, Map<String, Object>> fields = new LinkedHashMap<String, Map<String, Object>>();

        IEntity customerEntity = metaDataManager.getEntityByName("Customer");
        Map<String, IField> customerFields = customerEntity.getFields();
        if (currSheet == 0) {

            for (String key : customerFields.keySet()) {
                IField meta = customerFields.get(key);
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                String label = fieldMeta.label;
                if (label == null) {
                    continue;
                }
                label = label.trim();
                if (key.equals("owner")) {
                    label = ownerLabel;
                }
                if (customerHeader.contains(label)) {

                    setMetadataLength(fieldMeta, key);

                    Map<String, Object> customerMap = new HashMap<String, Object>();
                    customerMap.put("label", label);
                    customerMap.put("type", fieldMeta.type.value());
                    customerMap.put("field", key);
                    customerMap.put("length", fieldMeta.length);
                    customerMap.put("enumName", fieldMeta.enumName);
                    fields.put(label, customerMap);
                }
            }
        }

        if (currSheet == 1) {
            // 联系人字段
            IEntity contactEntity = metaDataManager.getEntityByName("Contact");
            Map<String, IField> contactFields = contactEntity.getFields();

            // 把客户名称加进来
            addCustomerFields(fields, customerFields);

            for (String key : contactFields.keySet()) {
                IField meta = contactFields.get(key);
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                String label = fieldMeta.label;
                if (label == null) {
                    continue;
                }
                label = label.trim();
                if (key.equals("owner")) {
                    label = ownerLabel;
                }
                if (contactHeader.contains(label)) {
                    setMetadataLength2(fieldMeta, key);

                    Map<String, Object> contactMap = new HashMap<String, Object>();
                    contactMap.put("label", label);
                    contactMap.put("type", fieldMeta.type.value());
                    contactMap.put("field", key);
                    contactMap.put("length", fieldMeta.length);
                    contactMap.put("enumName", fieldMeta.enumName);
                    fields.put(label, contactMap);
                }
            }
        }

        if (currSheet == 2) {
            // 工作记录字段
            IEntity workRecordEntity = metaDataManager.getEntityByName("WorkRecord");
            Map<String, IField> workRecordFields = workRecordEntity.getFields();

            // 把客户名称加进来
            addCustomerFields(fields, customerFields);

            for (String key : workRecordFields.keySet()) {
                IField meta = workRecordFields.get(key);

                if (meta.isSystemField()) {
                    // continue; 系统字段也要处理
                }
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                String label = fieldMeta.label;
                if (label == null) {
                    continue;
                }
                label = label.trim();
                if (key.equals("owner")) {
                    label = ownerLabel;
                }

                if (workrecordHeader.contains(label) && (key.equals("content") || key.equals("owner"))) {
                    Map<String, Object> workRecordMap = new HashMap<String, Object>();
                    if (key.equals("owner")) {
                        workRecordMap.put("label", "业务员");
                    } else {
                        workRecordMap.put("label", label);
                    }
                    workRecordMap.put("type", fieldMeta.type.value());
                    workRecordMap.put("field", key);
                    workRecordMap.put("length", fieldMeta.length);
                    workRecordMap.put("enumName", fieldMeta.enumName);
                    fields.put(label, workRecordMap);
                }
            }
        }

        Map<String, Map<String, Object>> rows = new LinkedHashMap<String, Map<String, Object>>();
        i = 0;
        for (i = 0; i < size; i++) {
            String columnName = headers.get(i);
            if (fields.containsKey(columnName)) {
                Map<String, Object> row = fields.get(columnName);
                rows.put(columnName, row);
            }
        }
        return rows;
    }

    /**
     * 
     * 重写是否包含
     */
    public static boolean listContains(List<String> list, String value) {
        int i = 0;
        int size = list.size();
        for (i = 0; i < size; i++) {
            if (list.get(i).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 
     * 检查必须字段（客户名称，客户业务员，联系人）
     */
    public static String checkHeadFields(List<String> headers) {
        String needFileds = "";
        if (headers == null) {
            headers = new ArrayList<String>();
        }
        String customerNameLabel = getLabelByField("Customer", "name");
        String customerOwnerLabel = getLabelByField("Customer", "owner");
        String contactNameLabel = getLabelByField("Contact", "name");
        if (!listContains(headers, customerNameLabel)) {
            needFileds += "," + customerNameLabel;
        }
        if (!listContains(headers, customerOwnerLabel)) {
            needFileds += "," + customerOwnerLabel;
        }
        if (!listContains(headers, contactNameLabel)) {
            needFileds += "," + contactNameLabel;
        }
        return needFileds;
    }

    /**
     * 
     * 检查必须字段（客户名称）
     */
    public static String checkHeadFieldsCustomer(List<String> headers) {
        String needFileds = "";
        if (headers == null) {
            headers = new ArrayList<String>();
        }
        String customerNameLabel = getLabelByField("Customer", "name");
        String customerOwnerLabel = getLabelByField("Customer", "owner");
        if (!listContains(headers, customerNameLabel)) {
            needFileds += "," + customerNameLabel;
        }
        if (!listContains(headers, customerOwnerLabel)) {
            needFileds += "," + customerOwnerLabel;
        }
        return needFileds;
    }

    /**
     * 
     * 检查必须字段（联系人）
     */
    public static String checkHeadFieldsContact(List<String> headers) {
        String needFileds = "";
        if (headers == null) {
            headers = new ArrayList<String>();
        }
        String customerNameLabel = getLabelByField("Customer", "name");
        String contactNameLabel = getLabelByField("Contact", "name");
        if (!listContains(headers, customerNameLabel)) {
            needFileds += "," + customerNameLabel;
        }
        if (!listContains(headers, contactNameLabel)) {
            needFileds += "," + contactNameLabel;
        }
        return needFileds;
    }

    /**
     * 
     * 检查必须字段（工作记录的业务员）
     */
    public static String checkHeadFieldsWorkRecord(List<String> headers) {
        String needFileds = "";
        if (headers == null) {
            headers = new ArrayList<String>();
        }
        String workRecordOwnerLabel = getLabelByField("WorkRecord", "owner");
        if (!listContains(headers, workRecordOwnerLabel)) {
            needFileds += "," + workRecordOwnerLabel;
        }
        return needFileds;
    }
    
    public static void transMap2Bean(Map<String, Object> map, Object obj) {  
    	  
        try {  
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());  
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();  
  
            for (PropertyDescriptor property : propertyDescriptors) {  
                String key = property.getName();  
  
                if (map.containsKey(key)) {  
                    Object value = map.get(key);  
                    // 得到property对应的setter方法  
                    Method setter = property.getWriteMethod();  
                    setter.invoke(obj, value);  
                }  
  
            }   
        } catch (Exception e) {  
            System.out.println("transMap2Bean Error " + e);  
        }  
  
        return;  
  
    }  

}
