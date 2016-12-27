package com.chanapp.chanjet.customer.service.usersetting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingHome;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRow;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRowSet;
import com.chanapp.chanjet.customer.constant.BI;
import com.chanapp.chanjet.customer.constant.LK;
import com.chanapp.chanjet.customer.constant.metadata.UserSettingMetaData;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.ccs.api.common.DeviceInfo;
import com.chanjet.csp.ccs.api.common.DeviceType;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class UserSettingServiceImpl extends BoBaseServiceImpl<IUserSettingHome, IUserSettingRow, IUserSettingRowSet>
        implements UserSettingServiceItf {

    @Override
    public String getUserSettingValue(Long userId, String settingKey) {
        DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
        String deviceType = "-";
        if (null != device) {
            DeviceType type = device.getDeviceType();
            deviceType = type.name();
        }
        IUserSettingRow userSettingDb = getByKeyUserIdAndType(userId, settingKey, deviceType);
        if (userSettingDb != null) {
            return userSettingDb.getValue();
        }
        return null;
    }

    @Override
    public IUserSettingRow getByKeyUserIdAndType(Long userId, String key, String deviceType) {
        Criteria criteria = Criteria.AND();
        criteria.eq(UserSettingMetaData.key, key);
        if (userId != null && userId > 2) {
            criteria.eq(UserSettingMetaData.userId, userId.toString());
        }
        if (StringUtils.isNotEmpty(deviceType)) {
            criteria.eq(UserSettingMetaData.deviceType, deviceType);
        }
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IUserSettingRowSet rowset = query(jsonQueryBuilder.toJsonQuerySpec());
        if (rowset.size() < 1) {
            return null;
        } else {
            return rowset.getRow(0);
        }
    }

    @Override
    public IUserSettingRow insertUserSetting(Long userId, String settingKey, String value) {
        DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
        String deviceType = "-";
        if (null != device) {
            DeviceType type = device.getDeviceType();
            deviceType = type.name();
        }

        IUserSettingRow userSetting = getByKeyUserIdAndType(userId, settingKey, deviceType);
        if (userSetting == null) {
            userSetting = createRow();
        }
        userSetting.setUserId(userId);
        userSetting.setKey(settingKey);
        userSetting.setValue(value);
        userSetting.setDeviceType(deviceType);
        upsert(userSetting);
        return userSetting;
    }

    @Override
    public IUserSettingRow updateUserSetting(Long userId, String settingKey, String value) {
        DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
        String deviceType = "-";
        if (null != device) {
            DeviceType type = device.getDeviceType();
            deviceType = type.name();
        }
        IUserSettingRow userSetting = getByKeyUserIdAndType(userId, settingKey, deviceType);
        userSetting.setValue(value);
        upsert(userSetting);
        return userSetting;
    }

    @Override
    public void generate(String key, String value, Long userId, String deviceType) {
        IUserSettingRow row = createRow();
        row.setKey(key);
        row.setValue(value);
        row.setUserId(userId);
        row.setDeviceType(deviceType);
        upsert(row);
    }

    @Override
    public Map<String, String> getUserSetting() {
        Map<String, String> map = new HashMap<String, String>();
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<IUserSettingRow> userSettings = getUserSetting(userId);
        for (IUserSettingRow userSetting : userSettings) {
            if (userSetting.getKey() != null) {
                map.put(userSetting.getKey(), userSetting.getValue());
            }
        }
        return map;
    }

    private List<IUserSettingRow> getUserSetting(Long userId) {
        DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
        String deviceType = "-";
        if (null != device) {
            DeviceType type = device.getDeviceType();
            deviceType = type.name();
        }
        List<IUserSettingRow> userSettingDb = getUserSetting(userId, deviceType);
        return userSettingDb;
    }

    private List<IUserSettingRow> getUserSetting(Long userId, String deviceType) {
        Criteria criteria = Criteria.AND();
        if (userId != null && userId > 2) {
            criteria.eq(UserSettingMetaData.userId, userId.toString());
        }
        if (StringUtils.isNotEmpty(deviceType)) {
            criteria.eq(UserSettingMetaData.deviceType, deviceType);
        }
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IUserSettingRowSet rowset = query(jsonQueryBuilder.toJsonQuerySpec());
        if (rowset == null)
            return null;
        return rowset.getUserSettingRows();
    }

    @Override
    public Map<String, Object> insertUserSetting(String map) {
        Map<String, Object> userSettingMap = dataManager.jsonStringToMap(map);
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        String settingKey = (String) userSettingMap.get("key");
        String value =  userSettingMap.get("value").toString();
        if (StringUtils.isEmpty(settingKey)) {
            throw new AppException("app.userSetting.key.isnull");
        }
        IUserSettingRow userSetting = insertUserSetting(userId, settingKey, value);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", userSetting.getId());
        result.put("userId", userSetting.getUserId());
        result.put("value", userSetting.getValue());
        result.put("key", userSetting.getKey());
        return result;
    }

    @Override
    public Map<String, Object> updateUserSetting(String map) {
        Map<String, Object> userSettingMap = dataManager.jsonStringToMap(map);
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        String settingKey = (String) userSettingMap.get("key");
        String value = (String) userSettingMap.get("value");
        if (StringUtils.isEmpty(settingKey)) {
            throw new AppException("app.userSetting.key.isnull");
        }
        IUserSettingRow userSetting = updateUserSetting(userId, settingKey, value);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("id", userSetting.getId());
        result.put("userId", userSetting.getUserId());
        result.put("value", userSetting.getValue());
        result.put("key", userSetting.getKey());
        return result;
    }

    @Override
    public Map<String, Object> workingHours() {
        Map<String, Object> map = new HashMap<String, Object>();
        IUserSettingRow userSettingRow = getByKeyUserIdAndType(null, "APP_CONFIG_WORKING_HOURS", null);
        if (userSettingRow != null) {
            String value = userSettingRow.getValue();
            Map<String, Object> valueMap = dataManager.jsonStringToMap(value);
            map.put("startTime", valueMap.get("startTime"));
            map.put("endTime", valueMap.get("endTime"));
            map.put("status", userSettingRow.getStatus());
        }
        return map;
    }

    @Override
    public Map<String, Object> setWorkingHours(String workingHoursMap) {
        Map<String, Object> value = dataManager.jsonStringToMap(workingHoursMap);
        Map<String, Object> map = new HashMap<String, Object>();
        IUserSettingRow workingHours = setWorkingHours(value);
        if (workingHours != null) {
            String str = workingHours.getValue();
            Map<String, Object> valueMap = dataManager.jsonStringToMap(str);
            map.put("startTime", valueMap.get("startTime"));
            map.put("endTime", valueMap.get("endTime"));
            map.put("status", workingHours.getStatus());
        }
        return map;
    }

    private synchronized IUserSettingRow setWorkingHours(Map<String, Object> workingHoursMap) {
        String settingKey = "APP_CONFIG_WORKING_HOURS";
        IUserSettingRow userSetting = getByKeyUserIdAndType(null, settingKey, null);
        if (userSetting == null) {
            userSetting = createRow();
        }
        userSetting.setKey(settingKey);
        String startTimeStr = (String) workingHoursMap.get("startTime");
        String endTimeStr = (String) workingHoursMap.get("endTime");
        if (StringUtils.isEmpty(startTimeStr) || StringUtils.isEmpty(endTimeStr)) {
            throw new AppException("appconfig.workinghours.time.required");
        }

        boolean startIsDate = Pattern.matches("(0[0-9]|1[0-2]):[0-5]{1}[0-9]{1}", startTimeStr);
        boolean endIsDate = Pattern.matches("(1[3-9]|2[0-3]):[0-5]{1}[0-9]{1}", endTimeStr);
        if (!startIsDate || !endIsDate) {
            throw new AppException("appconfig.workinghours.time.illege");
        }
        userSetting.setValue("{\"startTime\": \"" + startTimeStr + "\",\"endTime\": \"" + endTimeStr + "\"}");
        if (workingHoursMap.containsKey("status")) {
            Boolean status = ConvertUtil.toBoolean(workingHoursMap.get("status").toString());
            userSetting.setStatus(status);
        } else {
            userSetting.setStatus(null);
        }

        if (userSetting.getStatus() == null) {
            throw new AppException("appconfig.workinghours.status.required");
        }
        upsert(userSetting);
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).writeMsg2BigData(userSetting.getId(),
                BI.CUSTOMER_ADD, 0);
        return userSetting;
    }

    @Override
    public IUserSettingRow workingHoursRow() {
        String settingKey = "APP_CONFIG_WORKING_HOURS";
        return getByKeyUserIdAndType(null, settingKey, null);
    }

	@Override
	public void initUseSettingLock() {
		IUserSettingRow row = getByKeyUserIdAndType(null, LK.lockAppUserChange, null);
		if(row==null){
			IUserSettingRow userSetting = createRow();
			userSetting.setKey(LK.lockAppUserChange);
			upsert(userSetting);
		}
	}
	
	@Override
	public void lockAppUserChange(BoSession session) {
        Criteria criteria = Criteria.AND();
        criteria.eq(UserSettingMetaData.key, LK.lockAppUserChange);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IUserSettingRowSet rowSet = this.getBusinessObjectHome().query(session, jsonQueryBuilder.toJsonQuerySpec());
		if(rowSet!=null&&rowSet.getRows()!=null&&rowSet.getRows().size()>0){
			IUserSettingRow row = rowSet.getRow(0);
	        Long version = row.getVersion();	
			row.setFieldValue(session,SC.version, version);
			try{
				this.getBusinessObjectHome().upsert(session, row);
			}
			catch(Exception e){
			//	e.printStackTrace();
			}
			//upsert(row);
		}
	}

}
