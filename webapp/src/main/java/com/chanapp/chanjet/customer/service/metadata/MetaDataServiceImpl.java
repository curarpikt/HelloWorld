package com.chanapp.chanjet.customer.service.metadata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.CspEntityRestObject;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.DataHelper;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.cache.VersionInfo;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.base.field.FieldBuilder;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.customization.ICSPEnumForCustomization;
import com.chanjet.csp.cmr.api.metadata.customization.ICustomizationSession;
import com.chanjet.csp.cmr.api.metadata.customization.IEntityForCustomization;
import com.chanjet.csp.cmr.api.metadata.userschema.type.appCSPEnums.ICSPEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.appCSPEnums.IEnumValue;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.graphql.GqlManager;

public class MetaDataServiceImpl implements MetaDataServiceItf {
    public final static String SHADOW_FOREIGN_KEY = "SHADOW_FOREIGN_KEY";
    private final String[] EONAMES = { EO.Customer, EO.Contact, EO.WorkRecord, EO.Comment, EO.User,
            EO.TodoTips, EO.TodoWork, EO.Checkin, EO.ExportTask };
    private final static String FIELDPATTERN = "fieldPattern";

    @Override
    public CustomerMetaData getMetaData(Long version) {
        CustomerMetaData customerMetaData = new CustomerMetaData();
        if (version != null && version >= customerMetaData.getVersion()) {
            return customerMetaData;
        }
        IAppMetadataManager metadataManager = AppWorkManager.getAppMetadataManager();
        Map<String, CspEntityRestObject> entites = new HashMap<String, CspEntityRestObject>();
        for (String eoName : EONAMES) {
            buildMetaDataByEntity(entites, eoName, metadataManager);
        }
        // 组装实体
        customerMetaData.setEntites(entites);
        // 组装全部枚举项
        customerMetaData.setEnums(getMetaDataEnum(metadataManager));
   
        // 组装布局
      // LayoutServiceItf layoutService = ServiceLocator.getInstance().lookup(LayoutServiceItf.class);
     // customerMetaData.setLayout(layoutService.getFieldDatas());
        customerMetaData.setLayout(LayoutManager.getLayout());
        // 组装停用字段
        Map<String, List<String>> disableFields = new HashMap<String, List<String>>();
   //     FieldDataServiceItf fieldDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        disableFields.put(EO.Customer, LayoutManager.getDisableFields(EO.Customer));
        disableFields.put(EO.Contact, LayoutManager.getDisableFields(EO.Contact));
        disableFields.put(EO.WorkRecord, LayoutManager.getDisableFields(EO.WorkRecord));
        customerMetaData.setDisableFields(disableFields);
        return customerMetaData;
    }

    private Map<String, CSPEnum> getMetaDataEnum(IAppMetadataManager metadataManager) {
        Map<String, CSPEnum> enumMap = new LinkedHashMap<String, CSPEnum>();
        for (ICSPEnum iCSPEnum : metadataManager.getCSPEnums()) {
            CSPEnum cspEnum = new CSPEnum(iCSPEnum.getName());
            for (IEnumValue enumValue : iCSPEnum.getAllEnumValuesOrderedByOrdinal()) {
                CSPEnumValue cspEnumValue = new CSPEnumValue(enumValue.getName(), enumValue.getLabel(),
                        enumValue.isActive());
                cspEnum.addEnumValue(cspEnumValue);
            }
            enumMap.put(iCSPEnum.getName(), cspEnum);
        }
        return enumMap;
    }

    private void buildMetaDataByEntity(Map<String, CspEntityRestObject> entites, String eoName,
            IAppMetadataManager metadataManager) {
        IEntity metaEntity = metadataManager.getEntityByName(eoName);
        if (metaEntity == null) {
            throw new AppException("app.attribute.entityName.invalid");
        }
        Map<String, IField> fldMetas = metaEntity.getFields();
        Map<String, IField> customFields = metaEntity.getCustomFields();
        List<String> customCustomerFields = new ArrayList<String>();
        CspEntityRestObject customerRestObject = new CspEntityRestObject();
        // 去掉了字段长度设置，待确认
        for (String key : fldMetas.keySet()) {
            IField meta = fldMetas.get(key);
            if (SHADOW_FOREIGN_KEY.equalsIgnoreCase(meta.getType().name())) {
                continue;
            }
            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
            // 系统字段LABLE设置
            if (key.equals("owner")) {
                if (fieldMeta.label.equals("Owner")) {
                    fieldMeta.label = "业务员";
                }
            } else if (key.equals("createdBy")) {
                fieldMeta.label = "创建人";
            } else if (key.equals("createdDate")) {
                fieldMeta.label = "创建时间";
            } else if (key.equals("lastModifiedDate")) {
                fieldMeta.label = "上次修改时间";
            }
            if (customFields.get(key) != null) {
                customCustomerFields.add(key);
            }
            customerRestObject.addField(fieldMeta);
        }
        customerRestObject.setCustomFields(customCustomerFields);
        customerRestObject.sortField();
        entites.put(eoName, customerRestObject);
    }

    @Override
    public void saveCustomField(FiledSave field) {
        IEntity entity = preSaveValidate(field);
        preSaveValidate(field);
        //FieldDataServiceItf metaDataService = ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
        // 更新编辑界面布局
/*        Long editSortId = Long.parseLong(entity.getProperty(LayoutServiceImpl.sortEdit));
        Long managerSortId = Long.parseLong(entity.getProperty(LayoutServiceImpl.sortManager));
        List<Long> sortIds = new ArrayList<Long>();
        sortIds.add(editSortId);
        sortIds.add(managerSortId);*/
        String fieldName = "Field" + new Date().getTime();  
        saveEntityField(entity, fieldName, field);
        if(field.isHidden()==false){
            LayoutManager.updateSortByAddField(entity.getName(),fieldName);
        }
        MetadataCacheBuilder.newBuilder().buildCache().clear();
        VersionInfo.getInstance().setLastModifiedDate(new Date());
        GqlManager.refreshBOSchema(field.getEntityName());

    }

    private void saveEntityField(IEntity entity, String fieldName, FiledSave fieldinfo) {
        IAppMetadataManager metaDataManager = AppWorkManager.getAppMetadataManager();
        ICustomizationSession customSession = metaDataManager.startCustomizationSession();
        try {
            IEntityForCustomization custEntity = customSession.getEntityForCustomizationById(entity.getId());
            if (fieldinfo.getFieldType().equals("Enum")) {
                createEnumEntity(custEntity, fieldinfo, customSession, fieldName);
            } else {
                createTextEntity(custEntity, fieldinfo, customSession, fieldName);
            }
            customSession.commit();
        } catch (Exception e) {
            customSession.rollBack();
            throw e;
        }
    }

    private void updateEntityField(IEntity entity, FiledSave fieldinfo) {
        IAppMetadataManager metaDataManager = AppWorkManager.getAppMetadataManager();
        ICustomizationSession customSession = metaDataManager.startCustomizationSession();
        try {
            IEntityForCustomization custEntity = customSession.getEntityForCustomizationById(entity.getId());
            if (fieldinfo.getFieldType().equals("Enum")) {
                updateEnumEntity(custEntity, fieldinfo, customSession);
            } else {
                customSession.setLabel(custEntity.getField(fieldinfo.getFieldName()), fieldinfo.getFieldLabel());
            }
            
   /* 		String BOName =entity.getName() + "BO";
    		IBusinessObject bo = metaDataManager.getBusinessObjectByName(BOName);
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());	
			IBOFieldForCustomization custField = custBO.getField(fieldinfo.getFieldName());
			customSession.setLabel(custField, fieldinfo.getFieldLabel());	*/
            customSession.commit();
        } catch (Exception e) {
            customSession.rollBack();
            throw e;
        }
    }

    private static void updateEnumEntity(IEntity custEntity, FiledSave param, ICustomizationSession customSession) {
        // 更新label
        IField field = custEntity.getField(param.getFieldName());
        String enumName = param.getEnumName();
        customSession.setLabel(field, param.getFieldLabel());
        ICSPEnumForCustomization cspEnum = customSession.getCSPEnumForCustomizationByName(enumName,
                AppWorkManager.getCurrentAppId());
        int index = 0;
        for (CSPEnumValue eenumValueVo : param.getEnumValues()) {
            String enumLabel = eenumValueVo.getEnumLabel();
            if (StringUtils.isNotEmpty(enumLabel)) {
                if (StringUtils.isEmpty(eenumValueVo.getEnumValue())) {
                    // new
                    String enumValueName = "EnumValue" + System.currentTimeMillis() + index;
                    index++;
                    IEnumValue enumValue = customSession.createEnumValueForCustomization(cspEnum, enumValueName);
                    // enumValue.setLabel(enumLabel);
                    enumValue.setOrdinal(param.getEnumValues().indexOf(eenumValueVo));
                    String delflag = eenumValueVo.getDelflag();
                    if (null != delflag && "y".equalsIgnoreCase(delflag)) {
                        enumValue.setActive(false);
                    } else {
                        enumValue.setActive(true);
                    }
                    customSession.setLabel(enumValue, enumLabel);
                    if (null != delflag && "y".equalsIgnoreCase(delflag)) {
                        enumValue.setActive(false);
                    } else {
                        enumValue.setActive(true);
                        index++;
                    }
                } else { // update enumvalue

                    IEnumValue enumValue = cspEnum.getEnumValue(eenumValueVo.getEnumValue());
                    customSession.setLabel(enumValue, enumLabel);
                    enumValue.setOrdinal(param.getEnumValues().indexOf(eenumValueVo));
                    // enumValue.setOrdinal(index);
                    String delflag = eenumValueVo.getDelflag();
                    if (null != delflag && "y".equalsIgnoreCase(delflag)) {
                        enumValue.setActive(false);
                    } else {
                        enumValue.setActive(true);
                        index++;
                    }
                }
            }
        }
    }

    private static String createTextEntity(IEntityForCustomization custEntity, FiledSave param,
            ICustomizationSession customSession, String fieldName) {
        FieldTypeEnum fieldTypeEnum = FieldTypeEnum.fromValue(param.getFieldType());
        FieldBuilder textfield = new FieldBuilder();
        switch (fieldTypeEnum) {
            case TEXT:
                textfield.fieldName = fieldName;
                textfield.labelStr = param.getFieldLabel();
                textfield.fieldType = FieldTypeEnum.TEXT;
                textfield.setProperty("length", "255");
                customSession.createFieldForCustomization(custEntity, textfield);
                // custEntity.createCustomField(textfield);
                break;
            case INTEGER:
                textfield.fieldName = fieldName;
                textfield.labelStr = param.getFieldLabel();
                textfield.fieldType = FieldTypeEnum.INTEGER;
                customSession.createFieldForCustomization(custEntity, textfield);
                // custEntity.createCustomField(textfield);
                break;
            case DATE:
                textfield.fieldName = fieldName;
                textfield.labelStr = param.getFieldLabel();
                textfield.fieldType = FieldTypeEnum.DATE;
                customSession.createFieldForCustomization(custEntity, textfield);
                break;
            default:
                textfield.fieldName = fieldName;
                textfield.labelStr = param.getFieldLabel();
                customSession.createFieldForCustomization(custEntity, textfield);
                break;
        }
        // customSession.upsert(custEntity);
        return fieldName;
    }

    private String createEnumEntity(IEntityForCustomization custEntity, FiledSave param,
            ICustomizationSession customSession, String fieldName) {
        Date nowDate = new Date();
        String enumName = "Enum" + nowDate.getTime();
        // ICustomCSPEnumDescriptor customCspEnumDes = new
        // CustomCSPEnumDescriptor(enumName);
        // 创建枚举对象
        customSession.createCSPEnumForCustomization(enumName);
        ICSPEnumForCustomization cspEnum = customSession.getCSPEnumForCustomizationByName(enumName,
                AppWorkManager.getCurrentAppId());

        int index = 0;
        for (CSPEnumValue eenumValueVo : param.getEnumValues()) {
            String enumLabel = eenumValueVo.getEnumLabel();
            if (StringUtils.isNotEmpty(enumLabel)) {
                String enumValueName = "EnumValue" + System.currentTimeMillis() + index;
                index++;
                IEnumValue enumValue = customSession.createEnumValueForCustomization(cspEnum, enumValueName);
                // enumValue.setLabel(enumLabel);
                enumValue.setOrdinal(param.getEnumValues().indexOf(eenumValueVo));
                String delflag = eenumValueVo.getDelflag();
                if (null != delflag && "y".equalsIgnoreCase(delflag)) {
                    enumValue.setActive(false);
                } else {
                    enumValue.setActive(true);
                }
                customSession.setLabel(enumValue, enumLabel);
            }
        }
        // 创建枚举字段
        FieldBuilder textfield = new FieldBuilder();
        textfield.fieldName = fieldName;
        textfield.labelStr = param.getFieldLabel();
        textfield.fieldType = FieldTypeEnum.CSP_ENUM;
        textfield.cspEnumName = enumName;
        customSession.createFieldForCustomization(custEntity, textfield);
        return fieldName;

    }

    private IEntity preSaveValidate(FiledSave filedSave) {
        String entityName = filedSave.getEntityName();
        if (StringUtils.isEmpty(entityName)) {
            throw new AppException("app.attribute.entityName.invalid");
        }
        IEntity entity = AppWorkManager.getAppMetadataManager().getEntityByName(filedSave.getEntityName());
        if (null == entity) {
            throw new AppException("app.attribute.entityName.invalid");
        }
        String fieldLabel = filedSave.getFieldLabel();
        if (StringUtils.isEmpty(fieldLabel)) {
            throw new AppException("app.attribute.label.required");
        }
        String fieldName = filedSave.getFieldName();
        Map<String, IField> dynattrList = entity.getFields();
        for (IField field : dynattrList.values()) {
            if (FieldTypeEnum.SHADOW_FOREIGN_KEY.equals(field.getType())) {
                continue;
            }
            if (field.getName().equals(fieldName)) {
                if (!(field.getType().value()).equals(filedSave.getFieldType())) {
                    throw new AppException("app.attribute.dont.changetype");
                }
            }
            if (field.getLabel().equals(fieldLabel) && (null == fieldName || !field.getName().equals(fieldName))) {
                throw new AppException("app.attribute.label.duplicated");
            }
        }
        String fieldType = filedSave.getFieldType();
        if (FieldTypeEnum.CSP_ENUM.value().equals(fieldType)) {
            List<CSPEnumValue> enumValues = filedSave.getEnumValues();
            if (null != enumValues) {
                for (int i = enumValues.size() - 1; i > -1; i--) {
                    String enumLabel = enumValues.get(i).getEnumLabel();
                    if (StringUtils.isEmpty(enumLabel)) {
                        enumValues.remove(i);
                    }
                }
            }
            if (null == enumValues || enumValues.size() < 1) {
                throw new AppException("app.attribute.enumValues.required");
            }
            Map<String, String> isRepeatMap = new HashMap<String, String>();
            for (CSPEnumValue enumVo : enumValues) {
                String enumLabel = enumVo.getEnumLabel();
                if (StringUtils.isNotEmpty(enumLabel)) {
                    String enumValue = enumVo.getEnumValue();
                    if (!isRepeatMap.containsKey(enumLabel)) {
                        isRepeatMap.put(enumLabel, enumValue);
                    } else {
                        throw new AppException("app.attribute.enumLabel.duplicated");
                    }
                }
            }
        }
        return entity;
    }

    @Override
    public void updateCustomField(FiledSave field) {
        if (null != field) {
            IEntity entity = preSaveValidate(field);
            updateEntityField(entity, field);
            MetadataCacheBuilder.newBuilder().buildCache().clear();
            VersionInfo.getInstance().setLastModifiedDate(new Date());
        }
    }

    @Override
    public List<CSPEnum> findENumList() {
        IAppMetadataManager metaDataManager = AppWorkManager.getCmrAppMetadataManagerFactory()
                .getAppMetadataManager(AppWorkManager.getCurrentAppId());
        List<CSPEnum> cspEnums = new ArrayList<CSPEnum>();
        for (ICSPEnum icspEnum : metaDataManager.getCSPEnums()) {
            CSPEnum cspEnum = new CSPEnum(icspEnum.getName());
            for (IEnumValue enumValue : icspEnum.getActiveEnumValuesOrderedByOrdinal()) {
                CSPEnumValue cspEnumValue = new CSPEnumValue(enumValue.getName(), enumValue.getLabel(),
                        enumValue.isActive());
                cspEnum.addEnumValue(cspEnumValue);
            }
            cspEnums.add(cspEnum);
        }
        return cspEnums;
    }

    @Override
    public Map<String, Object> setFieldPattern(String entityTable, String fieldName, Map<String, Object> fieldMap) {
        Assert.notNull(entityTable);
        Assert.notNull(fieldName);
        Assert.notNull(fieldMap.get("noBlank"));
        Assert.notEmpty(fieldMap);        
        Map<String, String> reslut = new HashMap<String, String>();
        String noBlank=fieldMap.get("noBlank").toString();
        if("true".equals(noBlank)){
        	   LayoutManager.setFieldPattern(entityTable, fieldName, "false");
        }else if("false".equals(noBlank)){
        	   LayoutManager.setFieldPattern(entityTable, fieldName, "true"); 	
        }     
        MetadataCacheBuilder.newBuilder().buildCache().clear();
        VersionInfo.getInstance().setLastModifiedDate(new Date());
        Map<String, Object> retobj = new HashMap<String, Object>();
   	 	reslut.put("success", "true");
   	 	retobj.put("resultObj", reslut);
        return retobj;
/*        String appId = AppWorkManager.getCurrentAppId();
        IAppMetadataManager metaDataManager = AppWorkManager.getCmrAppMetadataManagerFactory()
                .getAppMetadataManager(appId);
        ICustomizationSession customSession = metaDataManager.startCustomizationSession();
        try {
            IEntity Entity = metaDataManager.getEntityByName(entityTable);
            IFieldForCustomization field = customSession.getEntityForCustomizationById(Entity.getId())
                    .getField(fieldName);
     
            FieldVO vo = null;
            String oldPattern = field.getProperty(FIELDPATTERN);
            if (oldPattern != null) {
                vo = AppWorkManager.getDataManager().fromJSONString(oldPattern, FieldVO.class);
            } else {
                vo = new FieldVO();
            }
            BeanUtils.populate(vo, fieldMap);
            String patternJson = AppWorkManager.getDataManager().toJSONString(vo);
            customSession.setProperty(field, FIELDPATTERN, patternJson);
            customSession.commit();
            return reslut;
        } catch (Exception e) {
            customSession.rollBack();
            e.printStackTrace();
            throw new AppException(e.getMessage());
        }
*/
    }

}
