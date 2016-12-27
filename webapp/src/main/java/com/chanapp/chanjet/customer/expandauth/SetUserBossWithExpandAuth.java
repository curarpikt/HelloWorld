package com.chanapp.chanjet.customer.expandauth;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.util.Assert;
import com.chanjet.csp.dataauth.api.UserAffiliate;
import com.chanjet.csp.enterprise.ext.ExpandAuthorization;

public class SetUserBossWithExpandAuth implements ExpandAuthorization {

    @SuppressWarnings("unchecked")
    @Override
    public void process(Object[] arg0) {
        List<Long> bossList = null;
        Long userId = null;
     	BoSession  session = null;
        if (arg0 != null) {
            bossList = (List<Long>) arg0[1]; // 预定传入参数为BOSSID
            userId = (Long) arg0[0]; // 预定传入参数为USERID
            session = (BoSession) arg0[2];
            
        }
        Assert.notNull(bossList);
        Assert.notNull(userId);
       // Long appUserId = AppWorkManager.getCurrAppUserId();
       // SyncUserUtils.syncUserFromCIA(appUserId, false);
       // appUserId = AppWorkManager.getCurrAppUserId();
      //  BoSession session = AppContext.session();
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        List<Map<String, Object>> oldBossList = user.getDirectBoss(session, userId, null);
        for (Map<String, Object> bossMap : oldBossList) {
            Long oldBossId = (Long) bossMap.get("userId");
            if (bossList.contains(oldBossId)) {
                bossList.remove(oldBossId);
            }
        }
        // 设置上级
        if (bossList != null && bossList.size() > 0) {
            user.addBoss(session, userId, bossList, false);
        }
    }

}
