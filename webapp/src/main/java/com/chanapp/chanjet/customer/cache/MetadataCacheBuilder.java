package com.chanapp.chanjet.customer.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.chanapp.chanjet.customer.constant.EO;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.userschema.type.appCSPEnums.ICSPEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.appCSPEnums.IEnumValue;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;

public class MetadataCacheBuilder {

    private static MetadataCacheBuilder cacheBuilder = new MetadataCacheBuilder();

    public final static String SHADOW_FOREIGN_KEY = "SHADOW_FOREIGN_KEY";

    private Cache<String, CustomerMetaData> metaDataCache;

    private MetadataCacheBuilder() {
    }

    private final ReentrantReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private final Lock writeLock = cacheLock.writeLock();

    public static MetadataCacheBuilder newBuilder() {
        return cacheBuilder;
    }

    /**
     * 构建metadata元数据缓存
     */
    public Cache<String, CustomerMetaData> buildCache() {
        writeLock.lock();
        try {
            if (metaDataCache == null || metaDataCache.isEmpty()) {
                CacheLoad<String, CustomerMetaData> cacheLoad = new CacheLoad<String, CustomerMetaData>() {
                    @Override
                    public Cache<String, CustomerMetaData> loadCache() {
                        IAppMetadataManager metadataManager = AppWorkManager.getAppMetadataManager();
                        Cache<String, CustomerMetaData> cache = new BaseCache<String, CustomerMetaData>();
                        CustomerMetaData customerMetaData = new CustomerMetaData();
                        IEntity metaEntity = metadataManager.getEntityByName(EO.Customer);
                        if (metaEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        Map<String, CspEntityRestObject> entites = new HashMap<String, CspEntityRestObject>();
                        CspEntityRestObject customerRestObject = new CspEntityRestObject();
                        List<String> customCustomerFields = new ArrayList<String>();

                        Map<String, IField> customFields = metaEntity.getCustomFields();
                        Map<String, IField> fldMetas = metaEntity.getFields();
                        for (String key : fldMetas.keySet()) {
                            IField meta = fldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            if (key.equals("name")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("address")) {
                                fieldMeta.length = 128;
                            } else if (key.equals("remark")) {
                                fieldMeta.length = 255;
                            } else if (key.equals("url")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("phone")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("url")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("fax")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("owner")) {
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
                        entites.put(EO.Customer, customerRestObject);

                        IEntity contactEntity = metadataManager.getEntityByName(EO.Contact);
                        if (contactEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject contactRestObject = new CspEntityRestObject();
                        List<String> customContactFields = new ArrayList<String>();

                        customFields = contactEntity.getCustomFields();
                        Map<String, IField> contactFldMetas = contactEntity.getFields();
                        for (String key : contactFldMetas.keySet()) {
                            IField meta = contactFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);

                            if (key.equals("name")) {
                                fieldMeta.length = 16;
                            } else if (key.equals("department")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("position")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("appellation")) {
                                fieldMeta.length = 16;
                            } else if (key.equals("mobile")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("phone")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("email")) {
                                fieldMeta.length = 64;
                            } else if (key.equals("qq")) {
                                fieldMeta.length = 64;
                            }
                            if (customFields.get(key) != null) {
                                customContactFields.add(key);
                            }
                            contactRestObject.addField(fieldMeta);
                        }
                        contactRestObject.setCustomFields(customContactFields);
                        contactRestObject.sortField();
                        entites.put(EO.Contact, contactRestObject);

                        // WorkRecord
                        IEntity workRecordEntity = metadataManager.getEntityByName(EO.WorkRecord);
                        if (workRecordEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject workRecordRestObject = new CspEntityRestObject();
                        List<String> customRecordFields = new ArrayList<String>();

                        customFields = workRecordEntity.getCustomFields();
                        Map<String, IField> recordFldMetas = workRecordEntity.getFields();
                        for (String key : recordFldMetas.keySet()) {
                            IField meta = recordFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            if (customFields.get(key) != null) {
                                customRecordFields.add(key);
                            }
                            workRecordRestObject.addField(fieldMeta);
                        }
                        workRecordRestObject.setCustomFields(customRecordFields);
                        workRecordRestObject.sortField();
                        entites.put(EO.WorkRecord, workRecordRestObject);

                        // Comment
                        IEntity commentEntity = metadataManager.getEntityByName(EO.Comment);
                        if (commentEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject commentRestObject = new CspEntityRestObject();
                        List<String> commentFields = new ArrayList<String>();

                        customFields = commentEntity.getCustomFields();
                        Map<String, IField> commentFldMetas = commentEntity.getFields();
                        for (String key : commentFldMetas.keySet()) {
                            IField meta = commentFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            if (customFields.get(key) != null) {
                                commentFields.add(key);
                            }
                            commentRestObject.addField(fieldMeta);
                        }
                        commentRestObject.setCustomFields(commentFields);
                        commentRestObject.sortField();
                        entites.put(EO.Comment, commentRestObject);

                        // Attament
                        IEntity attamentEntity = metadataManager.getEntityByName(EO.Attachment);
                        if (attamentEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject attamentRestObject = new CspEntityRestObject();
                        List<String> customAttamentFields = new ArrayList<String>();

                        customFields = attamentEntity.getCustomFields();
                        Map<String, IField> attamentFldMetas = attamentEntity.getFields();
                        for (String key : attamentFldMetas.keySet()) {
                            IField meta = attamentFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            if (customFields.get(key) != null) {
                                customAttamentFields.add(key);
                            }
                            attamentRestObject.addField(fieldMeta);
                        }
                        attamentRestObject.setCustomFields(customAttamentFields);
                        entites.put(EO.Attachment, attamentRestObject);

                        // User
                        IEntity userEntity = metadataManager.getEntityByName(EO.User);
                        if (userEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject userRestObject = new CspEntityRestObject();
                        List<String> customUserFields = new ArrayList<String>();
                        customFields = userEntity.getCustomFields();
                        Map<String, IField> userFldMetas = userEntity.getFields();
                        for (String key : userFldMetas.keySet()) {
                            IField meta = userFldMetas.get(key);
                            if (SHADOW_FOREIGN_KEY.equalsIgnoreCase(meta.getType().name())) {
                                continue;
                            }
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            if (customFields.get(key) != null) {
                                customUserFields.add(key);
                            }
                            userRestObject.addField(fieldMeta);
                        }
                        userRestObject.setCustomFields(customUserFields);
                        entites.put(EO.User, userRestObject);
                        // SysRelUser
                        IEntity sysRelUserEntity = metadataManager.getEntityByName(EO.SysRelUser);
                        if (sysRelUserEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject sysRelUserRestObject = new CspEntityRestObject();
                        customFields = sysRelUserEntity.getCustomFields();
                        Map<String, IField> sysRelUserFldMetas = sysRelUserEntity.getFields();
                        for (String key : sysRelUserFldMetas.keySet()) {
                            IField meta = sysRelUserFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            sysRelUserRestObject.addField(fieldMeta);
                        }
                        entites.put(EO.SysRelUser, sysRelUserRestObject);

                        // importRecord
/*                        IEntity importRecordEntity = metadataManager.getEntityByName(EO.ImportRecord);
                        if (importRecordEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject importRecordRestObject = new CspEntityRestObject();
                        List<String> importRecordFields = new ArrayList<String>();
                        Map<String, IField> importRecordFldMetas = importRecordEntity.getFields();
                        for (String key : importRecordFldMetas.keySet()) {
                            IField meta = importRecordFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);

                            importRecordRestObject.addField(fieldMeta);
                        }
                        importRecordRestObject.setCustomFields(importRecordFields);
                        importRecordRestObject.sortField();
                        entites.put(EO.ImportRecord, importRecordRestObject);*/

                        // todoTips
                        IEntity todoTipsEntity = metadataManager.getEntityByName(EO.TodoTips);
                        if (todoTipsEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject todoTipsRestObject = new CspEntityRestObject();
                        customFields = todoTipsEntity.getCustomFields();
                        Map<String, IField> todoTipsFldMetas = todoTipsEntity.getFields();
                        for (String key : todoTipsFldMetas.keySet()) {
                            IField meta = todoTipsFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            todoTipsRestObject.addField(fieldMeta);
                        }
                        entites.put(EO.TodoTips, todoTipsRestObject);

                        // todoWork
                        IEntity todoWorkEntity = metadataManager.getEntityByName(EO.TodoWork);
                        if (todoWorkEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject todoWorkRestObject = new CspEntityRestObject();
                        customFields = todoWorkEntity.getCustomFields();
                        Map<String, IField> todoWorkFldMetas = todoWorkEntity.getFields();
                        for (String key : todoWorkFldMetas.keySet()) {
                            IField meta = todoWorkFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            todoWorkRestObject.addField(fieldMeta);
                        }
                        entites.put(EO.TodoWork, todoWorkRestObject);

                        // checkin
                        IEntity checkinEntity = metadataManager.getEntityByName(EO.Checkin);
                        if (checkinEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject checkinRestObject = new CspEntityRestObject();
                        customFields = checkinEntity.getCustomFields();
                        Map<String, IField> checkinFldMetas = checkinEntity.getFields();
                        for (String key : checkinFldMetas.keySet()) {
                            IField meta = checkinFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            checkinRestObject.addField(fieldMeta);
                        }
                        entites.put(EO.Checkin, checkinRestObject);

                        // exportTask
                        IEntity exportTaskEntity = metadataManager.getEntityByName(EO.ExportTask);
                        if (exportTaskEntity == null) {
                            throw new AppException("app.attribute.entityName.invalid");
                        }
                        CspEntityRestObject exportTaskRestObject = new CspEntityRestObject();
                        customFields = exportTaskEntity.getCustomFields();
                        Map<String, IField> exportTaskFldMetas = exportTaskEntity.getFields();
                        for (String key : exportTaskFldMetas.keySet()) {
                            IField meta = exportTaskFldMetas.get(key);
                            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                            exportTaskRestObject.addField(fieldMeta);
                        }
                        entites.put(EO.ExportTask, exportTaskRestObject);

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
                        customerMetaData.setEntites(entites);
                        customerMetaData.setEnums(enumMap);

                        cache.put("metadata", customerMetaData);
                        return cache;
                    }
                };
                metaDataCache = cacheLoad.loadCache();
            }
        } finally {
            writeLock.unlock();
        }
        return metaDataCache;
    }
}
