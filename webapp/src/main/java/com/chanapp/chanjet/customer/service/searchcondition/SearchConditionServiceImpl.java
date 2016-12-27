package com.chanapp.chanjet.customer.service.searchcondition;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionHome;
import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionRow;
import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionRowSet;
import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.DataHelper;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.PeriodUtil;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class SearchConditionServiceImpl
        extends BoBaseServiceImpl<ISearchConditionHome, ISearchConditionRow, ISearchConditionRowSet>
        implements SearchConditionServiceItf {

    private static final String TYPE_CONFIG = "Config";
    private static final String TYPE_FAVORITE = "Favorite";

    @Override
    public void deleteFavoriteCondition(Long userId, Long versionId) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq("versionId", versionId).eq("userId", userId);
        jsonQueryBuilder.addCriteria(criteria);
        String queryStr = jsonQueryBuilder.toJsonQuerySpec();
        batchDelete(queryStr);
    }

    private List<ISearchConditionRow> findSearchConditions(String type, String entityType, Long userId) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq("type", type).eq("entityType", entityType).eq("userId", userId);
        jsonQueryBuilder.addCriteria(criteria);

        ISearchConditionRowSet conditionSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        return conditionSet.getSearchConditionRows();
    }

    private void getFixedCondition(List<Map<String, Object>> result, List<String> exists) {
        // 关注状态
        Map<String, Object> colMap = new HashMap<String, Object>();
        colMap.put("id", "followed");
        colMap.put("name", "关注状态");
        colMap.put("multiSelect", false);
        List<Map<String, Object>> colItem = new ArrayList<Map<String, Object>>();
        colMap.put("list", colItem);
        Map<String, Object> itemMap = new HashMap<String, Object>();
        /*
         * itemMap.put("id","all"); itemMap.put("name","全部");
         * colItem.add(itemMap);
         */
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "followed");
        itemMap.put("name", "已关注的");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "unfollow");
        itemMap.put("name", "未关注的");
        colItem.add(itemMap);
        if (exists.size() > 0 && !exists.contains("followed")) {
            colMap.put("visible", "0");// 已存在的不包括在隐藏
            result.add(colMap);
        } else {
            colMap.put("visible", "1");// 默认和包含的显示
            result.add(colMap);
        }
        colMap = new HashMap<String, Object>();
        colMap.put("id", "userId");
        colMap.put("name", "业务员");
        colMap.put("multiSelect", false);
        colItem = new ArrayList<Map<String, Object>>();
        colMap.put("list", colItem);
        try {
            List<UserValue> invitedUsers = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                    .getHierarchyUsers(EnterpriseContext.getCurrentUser().getUserLongId());
            for (UserValue user : invitedUsers) {
                itemMap = new HashMap<String, Object>();
                itemMap.put("id", user.getId());
                itemMap.put("name", user.getName());
                colItem.add(itemMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (exists.size() > 0 && (exists.contains("userId") || exists.contains("owner"))) {
            colMap.put("visible", "1");
            result.add(colMap);
        } else if (exists.size() == 0) {
            colMap.put("visible", "1");
            result.add(colMap);
        } else {
            colMap.put("visible", "0");
            result.add(colMap);
        }
        // }
        // 跟进时间-本周、本月、上周、上月、最近3个月
        colMap = new HashMap<String, Object>();
        colMap.put("id", "followTime");
        colMap.put("name", "跟进时间");
        colMap.put("type", "DATE");
        colMap.put("multiSelect", false);
        colItem = new ArrayList<Map<String, Object>>();
        colMap.put("list", colItem);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "today");
        itemMap.put("name", "今天");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "currentWeek");
        itemMap.put("name", "本周");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "currentMonth");
        itemMap.put("name", "本月");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "lastWeek");
        itemMap.put("name", "上周");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "lastMonth");
        itemMap.put("name", "上月");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "threeMonths");
        itemMap.put("name", "最近三个月");
        colItem.add(itemMap);
        if (exists.size() > 0 && !exists.contains("followTime")) {
            colMap.put("visible", "0");
            result.add(colMap);
        } else {
            colMap.put("visible", "1");
            result.add(colMap);
        }
        // 多久未联系-超1个月未跟进、超3个月未跟进、超半年未跟进、超1年未跟进
        colMap = new HashMap<String, Object>();
        colMap.put("id", "noFollowTime");
        colMap.put("name", "多久未跟进");
        colMap.put("multiSelect", false);
        colItem = new ArrayList<Map<String, Object>>();
        colMap.put("list", colItem);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "aMonth");
        itemMap.put("name", "超1个月未跟进");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "threeMonth");
        itemMap.put("name", "超3个月未跟进");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "helfYear");
        itemMap.put("name", "超半年未跟进");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "oneYear");
        itemMap.put("name", "超1年未跟进");
        colItem.add(itemMap);

        if (exists.size() > 0 && !exists.contains("noFollowTime")) {
            colMap.put("visible", "0");
            result.add(colMap);
        } else {
            colMap.put("visible", "1");
            result.add(colMap);
        }

        // 客户类型
        colMap = new HashMap<String, Object>();
        colMap.put("id", "customerType");
        colMap.put("name", "客户类型");
        colMap.put("multiSelect", false);
        colItem = new ArrayList<Map<String, Object>>();
        colMap.put("list", colItem);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "mine");
        itemMap.put("name", "负责的");
        colItem.add(itemMap);
        itemMap = new HashMap<String, Object>();
        itemMap.put("id", "grant");
        itemMap.put("name", "参与的");
        colItem.add(itemMap);
        if (exists.size() > 0 && !exists.contains("customerType")) {
            colMap.put("visible", "0");
            result.add(colMap);
        } else {
            colMap.put("visible", "1");
            result.add(colMap);
        }
    }

    @Override
    public List<Map<String, Object>> findConfigConditions(String entityType, Long userId) {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        List<ISearchConditionRow> list = findSearchConditions(TYPE_CONFIG, entityType, userId);
        List<String> disableFields = LayoutManager.getDisableFields(entityType);
        Map<String, IField> fields = getMetaDataByEntityType(entityType);
        List<String> exists = new ArrayList<String>();
        if (list != null && list.size() > 0) {
            for (ISearchConditionRow searchCondition : list) {
                exists.add(searchCondition.getFieldName());
            }
        }
        getFixedCondition(result, exists);
        for (String key : fields.keySet()) {
            // 去掉停用字段
            if (disableFields.contains(key)) {
                continue;
            }
            IField meta = fields.get(key);
            getMetaDataFields(meta, result, exists);
        }
        return result;
    }

    private Map<String, Object> getEnumMap(FieldMetaData fieldMeta, List<String> exists) {
        Map<String, Object> enumMap = new HashMap<String, Object>();
        enumMap.put("id", fieldMeta.name);
        enumMap.put("name", fieldMeta.label);
        enumMap.put("type", fieldMeta.type.name());
        if (!exists.contains(fieldMeta.name)) {
            enumMap.put("visible", "0");
        } else {
            enumMap.put("visible", "1");
        }
        return enumMap;
    }

    private void getDateSelectList(List<Map<String, String>> enumItem) {
        Map<String, String> dateMap = PeriodUtil.getDatePeriod();
        for (Map.Entry<String, String> entry : dateMap.entrySet()) {
            Map<String, String> itemMap = new HashMap<String, String>();
            itemMap.put("id", entry.getKey());
            itemMap.put("name", entry.getValue());
            enumItem.add(itemMap);
        }
    }

    private void getMetaDataFields(IField meta, List<Map<String, Object>> result, List<String> exists) {
        if (null != meta && null != meta.getType() && FieldTypeEnum.CSP_ENUM.value().equals(meta.getType().value())) {
            FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
            Map<String, Object> enumMap = getEnumMap(fieldMeta, exists);
            enumMap.put("multiSelect", true);
            List<Map<String, String>> enumItem = new ArrayList<Map<String, String>>();
            enumMap.put("list", enumItem);

            CustomerMetaData customerMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
            CSPEnum cspEnum = customerMetaData.getEnums().get(fieldMeta.enumName);
            if (null != cspEnum) {
                List<CSPEnumValue> list = cspEnum.getEnumValues();
                if (null != list) {
                    for (CSPEnumValue enumValue : list) {
                        if (enumValue.getIsActive()) {
                            Map<String, String> itemMap = new HashMap<String, String>();
                            itemMap.put("id", enumValue.getEnumValue());
                            itemMap.put("name", enumValue.getEnumLabel());
                            enumItem.add(itemMap);
                        }
                    }
                }
            }

            result.add(enumMap);
        } else if (null != meta && null != meta.getType()
                && FieldTypeEnum.DATE.value().equals(meta.getType().value())) {
            if (!meta.isSystemField()) {
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                // ICSPEnum iCSPEnum =
                // AppMetadataAgent.getCSPEnumByName(fieldMeta.enumName);
                Map<String, Object> enumMap = getEnumMap(fieldMeta, exists);
                enumMap.put("multiSelect", false);
                List<Map<String, String>> enumItem = new ArrayList<Map<String, String>>();
                enumMap.put("list", enumItem);
                try {
                    getDateSelectList(enumItem);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                result.add(enumMap);
            }
        } else if (null != meta && null != meta.getType()
                && FieldTypeEnum.INTEGER.value().equals(meta.getType().value())) {
            if (!meta.isSystemField()) {
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                Map<String, Object> enumMap = getEnumMap(fieldMeta, exists);
                enumMap.put("multiSelect", false);
                result.add(enumMap);
            }

        }
    }

    private Map<String, IField> getMetaDataByEntityType(String entityType) {
        Map<String, IField> fieldMap = new HashMap<String, IField>();
        IEntity metaEntity = AppWorkManager.getAppMetadataManager().getEntityByName(entityType);
        if (metaEntity == null) {
            throw new AppException("app.attribute.entityName.invalid");
        }
        Map<String, IField> fldMetas = metaEntity.getFields();
        Map<String, IField> customFields = metaEntity.getCustomFields();
        fieldMap.putAll(fldMetas);
        if (customFields != null && customFields.size() > 0) {
            fieldMap.putAll(customFields);
        }
        return fldMetas;
    }

    private void deleteBy(Long userId, String type, String entityType) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq("type", type).eq("entityType", entityType).eq("userId", userId);
        jsonQueryBuilder.addCriteria(criteria);
        String queryStr = jsonQueryBuilder.toJsonQuerySpec();
        batchDelete(queryStr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void batchAddConfigCondition(Map<String, Object> map, Long userId) {
        Assert.notNull(map, "app.searchCondition.object.required");
        String entityType = (String) map.get("entityType");
        Assert.inConditions(entityType, "app.searchCondition.entityType.invalid");
        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("items");
        Assert.notNull(list, "app.searchCondition.items.required");
        deleteBy(userId, TYPE_CONFIG, entityType);
        if (null != list) {
            for (Map<String, Object> item : list) {
                ISearchConditionRow searchCondition = createRow();
                try {
                	LinkedHashMap<String, Object> convertItem = (LinkedHashMap<String, Object>)item;
                	this.populateBORow(convertItem, searchCondition);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if ("1".equals(searchCondition.getVisible())) {
                    searchCondition.setType(TYPE_CONFIG);
                    searchCondition.setUserId(userId);
                    searchCondition.setEntityType(entityType);
                    searchCondition.setOrdinal((long) list.indexOf(item));
                    upsert(searchCondition);
                }
            }
        }
    }

    @Override
    public void deleteFavoriteCondition(Long userId, String entityType) {
        deleteBy(userId, TYPE_FAVORITE, entityType);
    }

    @Override
    public void deleteConfigCondition(Long userId, String entityType) {
        deleteBy(userId, TYPE_CONFIG, entityType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void batchAddFavoriteCondition(Map<String, Object> map, Long userId) {
        Assert.notNull(map, "app.searchCondition.object.required");
        String entityType = (String) map.get("entityType");
        Assert.inConditions(entityType, "app.searchCondition.entityType.invalid");
        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("items");
        Assert.notNull(list, "app.searchCondition.items.required");
        Date date = new Date();
        for (Map<String, Object> item : list) {
            ISearchConditionRow searchCondition = createRow();
            try {
            	LinkedHashMap<String, Object> itemMap = (LinkedHashMap<String, Object>)item;
                this.populateBORow(itemMap, searchCondition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            searchCondition.setType(TYPE_FAVORITE);
            searchCondition.setUserId(userId);
            searchCondition.setEntityType(entityType);
            searchCondition.setVersionId(date.getTime());
            upsert(searchCondition);
        }
    }

    @Override
    public List<ISearchConditionRow> findFavoriteConditions(String entityType, Long userId) {
        List<String> disableFields = LayoutManager.getDisableFields(entityType);
        List<ISearchConditionRow> conditions = findSearchConditions(TYPE_FAVORITE, entityType, userId);
        List<ISearchConditionRow> retList = new ArrayList<ISearchConditionRow>();
        for (ISearchConditionRow condition : conditions) {
            String fieldName = condition.getFieldName();
            if (disableFields.contains(fieldName)) {
                continue;
            } else {
                retList.add(condition);
            }
        }
        return retList;
    }

}
