package com.chanapp.chanjet.customer.service.contactremindset;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetHome;
import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetRow;
import com.chanapp.chanjet.customer.businessobject.api.contactremindset.IContactRemindSetRowSet;
import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.constant.RE;
import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class ContactRemindSetServiceImpl
        extends BoBaseServiceImpl<IContactRemindSetHome, IContactRemindSetRow, IContactRemindSetRowSet>
        implements ContactRemindSetServiceItf {

    private static final Logger logger = LoggerFactory.getLogger(ContactRemindSetServiceImpl.class);

    @Override
    public Map<String, Object> saveSets(String sets) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        ContactRemindSetsValue setsObject = null;
        try {
            setsObject = JSON.parseObject(sets, ContactRemindSetsValue.class);
            //setsObject = dataManager.fromJSONString(sets, ContactRemindSetsValue.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("参数不是合法的json格式！");
        }
        checkSetsValue(setsObject);

        Map<String, Object> result = new HashMap<String, Object>();
        try {
            _save(sets);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.common.server.error");
            // throw new BaseException("app.common.server.error");

        }
        result.put("message", "保存成功。");
        result.put("result", true);
        // result.put("row", row);
        return result;
    }

    private IContactRemindSetRow _save(String sets) {
        // 1 更新
        IContactRemindSetRowSet contactRemindSetRowSet = getAllRecords(0);
        IContactRemindSetRow contactRemindSetRow = null;
        if (contactRemindSetRowSet != null && contactRemindSetRowSet.getRows() != null
                && contactRemindSetRowSet.getRows().size() > 0) {
            contactRemindSetRow = contactRemindSetRowSet.getRow(0);
            contactRemindSetRow.setSets(sets);
        } else {
            // 2 第一次保存提交的
            contactRemindSetRow = createRow();
            contactRemindSetRow.setSets(sets);
        }
        upsert(contactRemindSetRow);
        return contactRemindSetRow;
    }

    private IContactRemindSetRowSet getAllRecords(long modifyTime) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        String queryStr = jsonQueryBuilder.addCriteria(criteria).addOrderDesc("id").setFirstResult(0).setMaxResult(1)
                .toJsonQuerySpec();
        return query(queryStr);
    }

    private void checkSetsValue(ContactRemindSetsValue setsObject) {
        if (setsObject == null) {
            throw new AppException("参数为不能为空！");
            // throw new BaseException("参数为不能为空！");
        }
        if (setsObject.getEnumName() == null || "".equals(setsObject.getEnumName())) {
            throw new AppException("参数枚举项名称不能为空！");
            // throw new BaseException("参数枚举项名称不能为空！");
        }
        List<Map<String, Object>> values = setsObject.getValues();
        if (values == null || values.size() < 1) {
            throw new AppException("参数枚举值列表不能为空！");
            // throw new BaseException("参数枚举值列表不能为空！");
        }
        for (int i = 0; i < values.size(); i++) {
            Map<String, Object> setMap = values.get(i);
            Object enumValue = setMap.get("value");
            Object setProperty = setMap.get("setProperty");
            Object isActive = setMap.get("isActive");
            if (enumValue == null || !String.class.isInstance(enumValue)) {
                throw new AppException("参数枚举值不合法！");
                // throw new BaseException("参数枚举值不合法！");

            }
            if (setProperty == null) {
                // System.out.println("-----------------setProperty-------- is
                // null");
                throw new AppException("参数枚举值设定值不合法！");
                // throw new BaseException("参数枚举值设定值不合法！");

            } else {
                try {
                    Integer.parseInt(setProperty.toString());
                } catch (Exception e) {
                    // System.out.println("-----------------setProperty--------"+setProperty);
                    throw new AppException("参数枚举值设定值不合法！");
                    // throw new BaseException("参数枚举值设定值不合法！");

                }
            }
            if (isActive == null || !Boolean.class.isInstance(isActive)) {
                throw new AppException("参数枚举值是否停用不合法！");
                // throw new BaseException("参数枚举值是否停用不合法！");
            }
        }
    }

    @Override
    public ContactRemindSetsValue queryByModifyTime(Long modifyTime) {
        if (modifyTime == null) {
            modifyTime = 0L;
        }
        IContactRemindSetRow contactRemindSetRow = _queryByModifyTime(modifyTime);
        ContactRemindSetsValue rsMap = new ContactRemindSetsValue();
        if (contactRemindSetRow != null) {
            String sets = contactRemindSetRow.getSets();
            ContactRemindSetsValue setsMap = JSON.parseObject(sets, ContactRemindSetsValue.class);
          //  ContactRemindSetsValue setsMap = dataManager.fromJSONString(sets, ContactRemindSetsValue.class);
            rsMap.setChange(true);
            rsMap.setModifyTime(new Date().getTime());
            rsMap.setEnumName(setsMap.getEnumName());
            // rsMap.setErrorMessage(setsMap.getErrorMessage());
            rsMap.setErrorCode(0);
            rsMap.setValues(setsMap.getValues());
        } else {
            rsMap.setChange(false);
            rsMap.setModifyTime(new Date().getTime());
            rsMap.setErrorCode(0);
        }

        return rsMap;
    }

    private ContactRemindSetsValue getDefaultSet(String defaultEnumName) {
        ContactRemindSetsValue setsMap = new ContactRemindSetsValue();
        setsMap.setEnumName(defaultEnumName);
        setsMap.setChange(true);
        setsMap.setModifyTime(new Date().getTime());
        // CSPEnum cspEnum = getEnum(defaultEnumName);
        // String enumName = cspEnum.getEnumName();
        FieldMetaData cspEnum = getMeta().getEntites().get("Customer").getFields().get(defaultEnumName);
        String realEnumName = cspEnum.enumName;
        List<CSPEnumValue> enumValueList = getEnumValues(realEnumName);
        for (int i = 0; i < enumValueList.size(); i++) {
            CSPEnumValue cspEnumValue = enumValueList.get(i);
            String metaEnumValue = cspEnumValue.getEnumValue();
            boolean metaIsActive = cspEnumValue.getIsActive();
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("value", metaEnumValue);
            m.put("setProperty", RE.CONTACT_REMIND_SET_DEFAULT_VALUE);
            m.put("isActive", metaIsActive);
            setsMap.getValues().add(m);
        }
        return setsMap;

    }

    private CustomerMetaData getMeta() {
        CustomerMetaData cacheMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");

        return cacheMetaData;
    }

    private List<CSPEnumValue> getEnumValues(String enumName) {
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

    private IContactRemindSetRow _queryByModifyTime(Long modifyTime) {
        IContactRemindSetRowSet contactRemindSetRowSet = getAllRecords(modifyTime);
        if (contactRemindSetRowSet == null || contactRemindSetRowSet.size() < 1) {
            logger.info("联络提醒设置未设置");
            ContactRemindSetsValue defaultSetsMap = getDefaultSet(RE.CONTACT_REMIND_SET_DEFAULT_ENUMNAME);
            IContactRemindSetRow deraultRow = _save(dataManager.toJSONString(defaultSetsMap));
            return deraultRow;
        }
        IContactRemindSetRow contactRemindSetRow = contactRemindSetRowSet.getRow(0);
        Date lastModifiedDate = contactRemindSetRow.getLastModifiedDate();
        Long lastModifiedTime = lastModifiedDate.getTime();

        String sets = contactRemindSetRow.getSets();
       // ContactRemindSetsValue setsMap = dataManager.fromJSONString(sets, ContactRemindSetsValue.class);
        ContactRemindSetsValue setsMap = JSON.parseObject(sets, ContactRemindSetsValue.class);

        
        String enumName = setsMap.getEnumName();

        List<String> disableFields =LayoutManager.getDisableFields("Customer");
        if (disableFields.contains(enumName)) {
            ContactRemindSetsValue defaultSetsMap = getDefaultSet(RE.CONTACT_REMIND_SET_DEFAULT_ENUMNAME);
            IContactRemindSetRow deraultRow = _save(dataManager.toJSONString(defaultSetsMap));
            return deraultRow;
        }

        FieldMetaData cspEnum = getMeta().getEntites().get("Customer").getFields().get(enumName);
        String realEnumName = cspEnum.enumName;

        List<CSPEnumValue> enumValueList = getEnumValues(realEnumName);
        List<Map<String, Object>> setValues = setsMap.getValues();
        List<Map<String, Object>> setValuesAdd = new ArrayList<Map<String, Object>>();// 新增枚举值的list
        boolean isChange = false;
        int compareSize = 0;// 元数据中存在设置过的数目

        Map<String, String> enumValueMap = new HashMap<String, String>();

        for (int j = 0; j < enumValueList.size(); j++) {// 循环元数据的
            CSPEnumValue cspEnumValue = enumValueList.get(j);
            String metaEnumValue = cspEnumValue.getEnumValue();
            boolean metaIsActive = cspEnumValue.getIsActive();
            boolean match = false; // 元数据找在设置中是都找到匹配
            for (int i = 0; i < setValues.size(); i++) {// 循环设置的
                                                        // map.get("value")
                                                        // map.get("setProperty");
                Map<String, Object> setMap = setValues.get(i);
                String enumValue = (String) setMap.get("value");
                enumValueMap.put(enumValue, enumValue);
                // String setProperty = (String) setMap.get("setProperty");
                boolean isActive = (boolean) setMap.get("isActive");
                if (metaEnumValue.equals(enumValue)) {
                    compareSize = compareSize + 1;
                    match = true;
                    if (isActive != metaIsActive) {
                        isChange = true;// 发生停用启用也算修改
                    }
                    setMap.put("isActive", metaIsActive);// 用元数据的覆盖设置过的
                }
            }
            if (!match) {
                isChange = true;// 只要有一次不匹配的 那么就代表元数据发生过新增修改
                // 把新增的加过来
                Map<String, Object> setMapAdd = new HashMap<String, Object>();
                setMapAdd.put("setProperty", RE.CONTACT_REMIND_SET_DEFAULT_VALUE);
                setMapAdd.put("value", metaEnumValue);
                setMapAdd.put("isActive", metaIsActive);
                setValuesAdd.add(setMapAdd);
            }
        }

        for (int i = 0; i < setValuesAdd.size(); i++) {
            setValues.add(setValuesAdd.get(i));
        }

        if (isChange) {
            setsMap.setChange(isChange);
            setsMap.setEnumName(enumName);
            setsMap.setValues(setValues);
            setsMap.setModifyTime(new Date().getTime());
            contactRemindSetRow.setSets(dataManager.toJSONString(setsMap));
            upsert(contactRemindSetRow);
        } else {
            if (modifyTime.compareTo(lastModifiedTime) > 0) {
                logger.info("联络提醒设置无变化且时间戳大于最后修改时间数据");
                return null;
            }
            // 时间戳小于最后修改时间 要返回设计数据
        }

        return contactRemindSetRow;
    }

    @Override
    public Map<String, Object> getDefaultSetValue() {
        Map<String, Object> result = new HashMap<String, Object>();
        Integer defaultSetValue = RE.CONTACT_REMIND_SET_DEFAULT_VALUE;
        result.put("defaultSetValue", defaultSetValue);
        return result;
    }
}
