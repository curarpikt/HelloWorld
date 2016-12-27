package com.chanapp.chanjet.customer.service.layout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.cache.CustomerLayout;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.FieldDataMetaData;

import com.chanapp.chanjet.customer.util.MetaDataUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.reader.PropertiesReader;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.customization.IBusinessObjectForCustomization;
import com.chanjet.csp.cmr.api.metadata.customization.ICustomizationSession;
import com.chanjet.csp.cmr.api.metadata.customization.IEntityForCustomization;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;

public class LayoutServiceImpl extends BaseServiceImpl implements LayoutServiceItf {/*

    public final static String _initFlag = "inited";
    public final static String sortEdit = "sortEdit";
    public final static String sortManager = "sortManager";
    private final static String _customerEditFields = "customerEditFields";
    private final static String _customerMangerFields = "customerMangerFields";
    private final static String _contactFields = "contactFields";
    private final static String _cantEdit = "cantEdit";
    private final static String _noBlank = "noBlank";
    private final static String _cantDisable = "cantDisable";

    private final static String _fieldPattern = "fieldPattern";
    final static PropertiesReader reader = PropertiesReader.getInstance("customer/layout.properties");
    private final static Long SORTTYPE = 5L;
    // 字段类型
    public final static Long FIELD_STATUS_ENABLE = 1L;
    public final static Long FIELD_STATUS_DISABLE = 0L;

    private void _saveAppLayoutAsProperty(Map<String, Long> sortMap, String entityName) {
        String appId = AppWorkManager.getCurrentAppId();
        IAppMetadataManager metaDataManager = AppWorkManager.getCmrAppMetadataManagerFactory()
                .getAppMetadataManager(appId);
        ICustomizationSession customSession = metaDataManager.startCustomizationSession();
        try {
            IEntity customerEntity = metaDataManager.getEntityByName(entityName);
            IEntityForCustomization custEntity = customSession.getEntityForCustomizationById(customerEntity.getId());
            for (Map.Entry<String, Long> entry : sortMap.entrySet()) {
                if (entry.getValue() != null)
                    customSession.setProperty(custEntity, entry.getKey(), entry.getValue().toString());
            }
            // 更新布局初始化标志
            customSession.setProperty(custEntity, "initLayout", _initFlag);
            customSession.commit();
        } catch (Exception e) {
            customSession.rollBack();
            throw e;
        }
    }

    @Override
    public void initLayout(String entityName) {
        String appId = AppWorkManager.getCurrentAppId();
        IAppMetadataManager metaDataManager = AppWorkManager.getCmrAppMetadataManagerFactory()
                .getAppMetadataManager(appId);
        IEntity customerEntity = metaDataManager.getEntityByName(entityName);
        String initCustomer = customerEntity.getProperty("initLayout");
        Map<String, Long> layoutMap = new HashMap<String, Long>();
        if (initCustomer == null || !initCustomer.equals(_initFlag)) {
            Map<String, IField> customFields = customerEntity.getCustomFields();
            Set<String> customSets = customFields.keySet();
            // 初始化编辑界面
            Long editLayoutId = _saveFielddate(entityName, sortEdit, customSets);
            layoutMap.put(sortEdit, editLayoutId);
            // 初始化管理界面
            Long managerLayoutId = _saveFielddate(entityName, sortManager, customSets);
            layoutMap.put(sortManager, managerLayoutId);
            _saveAppLayoutAsProperty(layoutMap, entityName);
        }
    }

    *//**
     * 保存字段顺序布局
     * 
     * @param table
     * @param view
     * @param customFields
     * @return
     *//*
    private Long _saveFielddate(String table, String view, Set<String> customFields) {
        StringBuffer sortBuffer = new StringBuffer();
        String[] baseFields = _getDefalutFields(table, view);
        for (String basefield : baseFields) {
            sortBuffer.append(basefield.trim() + ",");
        }
        for (String customField : customFields) {
            sortBuffer.append(customField.trim() + ",");
        }
        String sortStr = sortBuffer.toString();
        if (sortStr.lastIndexOf(",") != -1) {
            sortStr = sortStr.substring(0, sortStr.lastIndexOf(","));
        }
        IBusinessObjectHome boHome = AppWorkManager.getBusinessObjectManager()
                .getPrimaryBusinessObjectHome(FieldDataMetaData.EOName);
        IBusinessObjectRow row = boHome.createRow();
        row.setFieldValue(FieldDataMetaData.entityTable, table);
        row.setFieldValue(FieldDataMetaData.type, SORTTYPE);
        row.setFieldValue(FieldDataMetaData.extend, sortStr);
        boHome.upsert(row);
        Long id = (Long) row.getFieldValue(SC.id);
        return id;
    }

    *//**
     * 获取默认字段布局
     * 
     * @param table
     * @param view
     * @return
     *//*
    private String[] _getDefalutFields(String table, String view) {
        if (CustomerMetaData.EOName.equals(table)) {
            if (sortEdit.equals(view)) {
                String fields = reader.getString(_customerEditFields);
                return fields.split(",");
            } else if (sortManager.equals(view)) {
                String fields = reader.getString(_customerMangerFields);
                return fields.split(",");
            }
        } else if (ContactMetaData.EOName.equals(table)) {
            String fields = reader.getString(_contactFields);
            return fields.split(",");
        }
        return null;
    }

    private List<String> _getCantEdits() {
        List<String> retList = new ArrayList<String>();
        String fieldstr = reader.getString(_cantEdit);
        String[] fields = fieldstr.split(",");
        Collections.addAll(retList, fields);
        return retList;
    }

    private List<String> _getCantDisable() {
        List<String> retList = new ArrayList<String>();
        String fieldstr = reader.getString(_cantDisable);
        String[] fields = fieldstr.split(",");
        Collections.addAll(retList, fields);
        return retList;
    }

    private List<String> _getNoblank() {
        List<String> retList = new ArrayList<String>();
        String fieldstr = reader.getString(_noBlank);
        String[] fields = fieldstr.split(",");
        Collections.addAll(retList, fields);
        return retList;
    }

    private List<String> _getSortFields(IEntity entity, String sortType, List<String> apendFields) {
        List<String> sortFields = new ArrayList<String>();
        String fielddataId = entity.getProperty(sortType);
        if (fielddataId == null) {
            return sortFields;
        }
        FieldDataServiceItf fieldDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        // 获取客户定义字段顺序
        IFieldDataRow row = fieldDataService.findByIdWithAuth(Long.parseLong(fielddataId));
        String innerSort = row.getExtend();
        String[] innersorts = innerSort.split(",");
        Collections.addAll(sortFields, innersorts);
        // 附加的字段
        if (apendFields != null) {
            for (String addField : apendFields) {
                if (!sortFields.contains(addField)) {
                    sortFields.add(addField);
                }
            }
        }
        return sortFields;
    }

    private ArrayList<Map<String, Object>> _getManagerFieldAttr(IEntity entity, List<String> sortFields) {
        FieldDataServiceItf fieldDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        String entityTable = entity.getTable();
        Map<String, IField> customFields = entity.getCustomFields();
        Set<String> self = customFields.keySet();
        List<String> disableList = fieldDataService.getDisableFields(entityTable);
        ArrayList<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        for (String fieldName : sortFields) {
            fieldName = fieldName.trim();
            Map<String, Object> layout = new HashMap<String, Object>();
            String key = entityTable + "." + fieldName;
            if ("remark".equals(fieldName) || "address".equals(fieldName)) {
                layout.put("fieldType", "LongText");
            }
            if (fieldName.equals("createdBy") || fieldName.equals("lastModifiedBy") || fieldName.equals("owner")) {
                layout.put("displayField", "name");
            }
            if (_getCantEdits().contains(key)) {
                layout.put("canEdit", "false");
            } else {
                layout.put("canEdit", "true");
            }
            if (_getCantDisable().contains(key)) {
                layout.put("canDisable", "false");
            } else {
                layout.put("canDisable", "true");
            }
            if (self.contains(fieldName)) {
                layout.put("self", "true");
            } else {
                layout.put("self", "false");
            }
            layout.put("name", fieldName);
            if (disableList.contains(fieldName)) {
                layout.put("status", FIELD_STATUS_DISABLE);
            } else {
                layout.put("status", FIELD_STATUS_ENABLE);
            }
            String fieldPatttern = _getFieldPatttern(entity, fieldName, key);
            if (fieldPatttern != null) {
                layout.put("extend", fieldPatttern);
            }
            retList.add(layout);
        }
        return null;
    }

    private ArrayList<Map<String, Object>> _getEditFieldAttr(IEntity entity, List<String> sortFields) {
        FieldDataServiceItf fieldDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        String entityTable = entity.getTable();
        Map<String, IField> customFields = entity.getCustomFields();
        Set<String> self = customFields.keySet();
        List<String> disableList = fieldDataService.getDisableFields(entityTable);
        ArrayList<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        for (String field : sortFields) {
            if (disableList.contains(field)) {
                continue;
            }
            Map<String, Object> layout = new HashMap<String, Object>();
            String key = entityTable + "." + field;
            String fieldName = field;
            if ("remark".equals(fieldName) || "address".equals(fieldName)) {
                layout.put("fieldType", "LongText");
            }
            if (fieldName.equals("createdBy") || fieldName.equals("lastModifiedBy") || fieldName.equals("owner")
                    || fieldName.equals("customer")) {
                layout.put("displayField", "name");
            }
           if (_getCantEdits().contains(key)) {
                layout.put("canEdit", "false");
            } else {
                layout.put("canEdit", "true");
            }
            if (self.contains(fieldName)) {
                layout.put("self", "true");
            } else {
                layout.put("self", "false");
            }
            String fieldPatttern = _getFieldPatttern(entity, field, key);
            if (fieldPatttern != null) {
                layout.put("extend", fieldPatttern);
            }
            layout.put("name", field);
            retList.add(layout);
        }
        return retList;
    }

    private String _getFieldPatttern(IEntity entity, String field, String key) {
        field = field.trim();
        String fieldPatttern = entity.getField(field).getProperty(_fieldPattern);
        FieldVO pattternVO = null;
        if (fieldPatttern != null) {
            pattternVO = AppWorkManager.getDataManager().fromJSONString(fieldPatttern, FieldVO.class);
        } else {
            pattternVO = new FieldVO();
        }
        if (_getNoblank().contains(key)) {
            pattternVO.setNoBlank(true);
            return dataManager.toJSONString(pattternVO);
        } else if (fieldPatttern != null) {
            return dataManager.toJSONString(pattternVO);
        }
        return null;
    }

    @Override
    public CustomerLayout getFieldDatas() {
        CustomerLayout layout = new CustomerLayout();
        IEntity customerEntity = MetaDataUtil.getEntityByEOName(CustomerMetaData.EOName);
        IEntity contactEntity = MetaDataUtil.getEntityByEOName(ContactMetaData.EOName);
        List<String> customerEditFields = _getSortFields(customerEntity, sortEdit, null);
        layout.setCustomerEdit(_getEditFieldAttr(customerEntity, customerEditFields));
        // 特殊字段，不参加排序
        String[] customerAddFields = reader.getString("customterAddFields").split(",");
        List<String> addFields = new ArrayList<String>(Arrays.asList(customerAddFields));
        // 查看页面布局=新增界面布局+特殊字段
        List<String> customerViewFields = _getSortFields(customerEntity, sortEdit, addFields);
        layout.setCustomerView(_getEditFieldAttr(customerEntity, customerViewFields));
        List<String> contactEditFields = _getSortFields(contactEntity, sortEdit, null);
        layout.setContactView(_getEditFieldAttr(contactEntity, contactEditFields));
        List<String> customerManagerFields = _getSortFields(customerEntity, sortManager, null);
        layout.setCustomerManager(_getManagerFieldAttr(customerEntity, customerManagerFields));
        List<String> contactManagerFields = _getSortFields(contactEntity, sortManager, null);
        layout.setContactManager(_getManagerFieldAttr(contactEntity, contactManagerFields));
        return layout;
    }

    
    

    

*/}
