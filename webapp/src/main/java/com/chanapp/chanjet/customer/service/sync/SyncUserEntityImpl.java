package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.SysRelUserMetaData;
import com.chanapp.chanjet.customer.constant.metadata.UserMetaData;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;

public class SyncUserEntityImpl extends BaseSyncEntity {
    private Long syncVersion;

    public SyncUserEntityImpl(Long syncVersion) {
        this.entityName = UserMetaData.EOName;
        this.boHome = AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome("CSPUser");
        this.syncVersion = syncVersion;
        filedName = getFieldsName(entityName);
        filedName.add(VF.privilege);
        filedName.add("fullSpell");
        filedName.add("shortSpell");
        initPara();
    }

    private static Map<Long, String> getUserStatus(List<Long> ids) {
        Map<Long, String> statusMap = new HashMap<Long, String>();
        UserQuery query= new UserQuery();
        query.setUserIds(ids);
        VORowSet<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
        for (UserValue row : users.getItems()) {
            Long userId = (Long) row.getUserId();        
            statusMap.put(userId, row.getStatus());	
       }
        return statusMap;
    }

    @Override
    public List<Map<String, Object>> getEntityData() {
        List<Map<String, Object>> userItems = super.getEntityData();
        if (userItems == null || userItems.size() == 0) {
            return userItems;
        }
        Map<Long, String> statusMap = getUserStatus(new ArrayList<Long>(this.addIds));
        for (Map<String, Object> row : userItems) {
            Long userId = (Long) row.get(SysRelUserMetaData.userId);
            row.put(SysRelUserMetaData.status, statusMap.get(userId));
            if (row.get("name") != null) {
                String name = row.get("name").toString();
                row.put("fullSpell", PinyinUtil.hanziToPinyinFull(name, true));
                row.put("shortSpell", PinyinUtil.hanziToPinyinSimple(name, false));
            } else {
                row.put("fullSpell", "");
                row.put("shortSpell", "");
            }
        }
        return userItems;
    }

    @Override
    public void initPara() {
        /*
         * row.put("fullSpell", PinyinUtil.hanziToPinyinFull(name, true));
         * row.put("shortSpell", PinyinUtil.hanziToPinyinSimple(name,false));
         */
        // 是否先调用CIA用户同步方法
        // 增加SYSUSER表状态变化的用户
        List<Long> ids = ServiceLocator.getInstance().lookup(UserServiceItf.class).findUserIds(null,
                syncVersion);
        this.addIds.addAll(ids);
        // 增加USER表CIA变化的用户
        addByTs(syncVersion, SC.lastModifiedBy, "desc");
    }

}
