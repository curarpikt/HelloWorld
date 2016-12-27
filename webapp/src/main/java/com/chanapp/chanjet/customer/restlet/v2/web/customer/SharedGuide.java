package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.US;
import com.chanapp.chanjet.customer.service.grant.GrantServiceItf;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 首次共享引导。判断是否给客户提示首次共享引导框。
 * 
 * @author tds
 *
 */
public class SharedGuide extends BaseRestlet {
    @Override
    public Object run() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("result", false);
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        UserSettingServiceItf userSettingService = ServiceLocator.getInstance().lookup(UserSettingServiceItf.class);
        String settingValue = userSettingService.getUserSettingValue(userId, US.SHARED_GUIDE);
        if (null == settingValue) {
            List<Long> customerIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                    .getCustomerIdsByUserId(userId);
            if (customerIds != null && customerIds.size() > 0) {
                resultMap.put("result", true);
                userSettingService.insertUserSetting(userId, US.SHARED_GUIDE, "true");
            }
        } else if ("true".equals(settingValue)) {
            resultMap.put("result", false);
            userSettingService.updateUserSetting(userId, US.SHARED_GUIDE, "false");
        }
        return resultMap;
    }

}
