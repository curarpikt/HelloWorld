package com.chanapp.chanjet.customer.service.usersetting;

import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingHome;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRow;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanjet.csp.bo.api.BoSession;

public interface UserSettingServiceItf extends BoBaseServiceItf<IUserSettingHome, IUserSettingRow, IUserSettingRowSet> {

    String getUserSettingValue(Long userId, String settingKey);

    IUserSettingRow getByKeyUserIdAndType(Long userId, String key, String deviceType);

    IUserSettingRow insertUserSetting(Long userId, String settingKey, String value);

    IUserSettingRow updateUserSetting(Long userId, String settingKey, String value);

    void generate(String key, String value, Long userId, String deviceType);

    Map<String, String> getUserSetting();

    Map<String, Object> insertUserSetting(String map);

    Map<String, Object> updateUserSetting(String map);

    Map<String, Object> workingHours();

    Map<String, Object> setWorkingHours(String workingHoursMap);

    IUserSettingRow workingHoursRow();
    
    void initUseSettingLock();

	void lockAppUserChange(BoSession session);
}
