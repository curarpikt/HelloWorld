package com.chanapp.chanjet.customer.service.dataauth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.PV;
import com.chanapp.chanjet.customer.service.message.MessageServiceItf;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.PortalUtil;
import com.chanapp.chanjet.customer.util.PushMsg;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAuthManagement;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.dataauth.Assignment;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.util.StringUtils;

public class DataAuthServiceImpl extends BaseServiceImpl implements DataAuthServiceItf {
    public final static String[] PRIVI_ENTITYS = new String[] { "Customer", "Contact", "WorkRecord", "Attachment",
            "TodoWork", "Checkin" };

    private static final BoDataAuthManagement dataAuthManager = AppWorkManager.getBoDataAccessManager()
            .getDataAuthManagement();

    @Override
    public void removeDataAuth(Long userId) {
        for (String key : PRIVI_ENTITYS) {
            removeEntityPrivilege(key, userId);
        }
    }

    private void removeEntityPrivilege(String entityName, Long userId) {
        Set<Assignment> assignmentList = dataAuthManager.listAssignments(entityName, userId, null, null, session());
        if (assignmentList != null) {
            for (Iterator<Assignment> iterator = assignmentList.iterator(); iterator.hasNext();) {
                Assignment assignment = (Assignment) iterator.next();
                dataAuthManager.removeAssignment(assignment.getId(), session());
            }
        }

    }

    @Override
    public void checkAndUpdateDataAuth(Long userId, String privi) {
        String dataPrivi = PV.SELF_PRIVI;
        if (PV.PRIVI_NORMAL.equals(privi)) {
            dataPrivi = PV.SELF_PRIVI;
        } else if (PV.PRIVI_SETUP.equals(privi)) {
            dataPrivi = PV.ALL_PRIVI;
        }
        setDataAuth(userId, dataPrivi);
    }

    private void setEntityPrivilege(String appId, String entityName, Long userId, String condition, String privilege,
            List<String> relationshipNames) {
        DataAuthPrivilege prvlge = null;
        if ("SELECT".equals(privilege))
            prvlge = DataAuthPrivilege.SELECT;
        else if ("UPDATE".equals(privilege))
            prvlge = DataAuthPrivilege.UPDATE;
        else if ("DELETE".equals(privilege))
            prvlge = DataAuthPrivilege.DELETE;
        else if ("INSERT".equals(privilege))
            prvlge = DataAuthPrivilege.INSERT;

        if (prvlge == null) {
            dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.SELECT, condition, session());
            dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.UPDATE, condition, session());
            dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.DELETE, condition, session());
            dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.INSERT, condition, session());
        } else {
            dataAuthManager.createAssignment(entityName, userId, null, prvlge, condition, session());
        }

    }

    private void setEntityDependency(String appId, String entityName, Long userId, String referenceFieldName) {
        String condition = referenceFieldName + " = select_dataauth()";

        dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.SELECT, condition, session());
        dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.UPDATE, condition, session());
        dataAuthManager.createAssignment(entityName, userId, null, DataAuthPrivilege.DELETE, condition, session());

    }

    private void setDataAuth(Long userId, String custDataAuth) {
        removeDataAuth(userId);
        String appId = AppWorkManager.getCurrentAppId();
        if (PV.SELF_PRIVI.equals(custDataAuth)) {
            setEntityDependency(appId, "Contact", userId, "customer");
            setEntityDependency(appId, "WorkRecord", userId, "customer");
            setEntityDependency(appId, "TodoWork", userId, "workRecord");
            for (String key : PRIVI_ENTITYS) {
                setEntityPrivilege(appId, key, userId, "id > 0", "INSERT", null);
                setEntityPrivilege(appId, key, userId, "id > 0 and owner.userId = " + userId, "SELECT", null);
                setEntityPrivilege(appId, key, userId, "id > 0 and owner.userId = " + userId, "UPDATE", null);
                setEntityPrivilege(appId, key, userId, "id > 0 and owner.userId = " + userId, "DELETE", null);
            }
        } else if (PV.ALL_PRIVI.equals(custDataAuth)) {
            for (String key : PRIVI_ENTITYS) {
                setEntityPrivilege(appId, key, userId, "id > 0", null, null);
            }
        }
    }

    private void accountStopPushMsg(String userId) {
        String orgId = PortalUtil.getOrgId();
        String orgName = PortalUtil.getOrgNameById(orgId);
        if (StringUtils.isNotEmpty(orgId)) {
            // 推送消息
            String name = EnterpriseContext.getCurrentUser().getName();
            String content = "抱歉，你的账号已被【" + name + "】停用，不能继续访问";
            String alert = "抱歉，你的账号已被【" + name + "】停用，不能继续访问";
            String type = IM.ACCOUNT_STOP;
            String from = PushMsg.getFrom(type);
            Map<String, Object> aps = new HashMap<String, Object>();
            aps.put("alert", alert);
            aps.put("sound", "default");
            aps.put("badge", 0);

            Map<String, Object> x = new HashMap<String, Object>();
            x.put("content", content);
            x.put("from", from);
            x.put("type", type);
            x.put("orgId", orgId);
            x.put("orgName", orgName);
            Map<String, Object> operator = new HashMap<String, Object>();
            operator.put("username", EnterpriseContext.getCurrentUser().getName());
            operator.put("userid", EnterpriseContext.getCurrentUser().getUserLongId());
            operator.put("headpictrue", EnterpriseContext.getCurrentUser().getHeadPicture());
            x.put("operator", operator);

            Map<String, Object> extras = new HashMap<String, Object>();
            extras.put("from", from);
            extras.put("type", type);
            extras.put("orgId", orgId);
            extras.put("orgName", orgName);

            Map<String, Object> r = new HashMap<String, Object>();
            r.put("aps", aps);
            r.put("x", x);
            r.put("extras", extras);

            ArrayList<Long> userIds = new ArrayList<Long>();
            userIds.add(Long.valueOf(userId));
            if (userIds.size() > 0) {
                String msgType = PushMsg.getMsgType(from);
                ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(null, msgType, userIds,
                        dataManager.toJSONString(r));
                PushMsg.asynPushNoAuthUser(from, null, userIds, dataManager.toJSONString(r),
                        EnterpriseContext.getCurrentUser().getUserLongId(), EnterpriseContext.getToken());
            }
        }
    }

    private void removeBoss(Long userId, boolean direct) {
        List<Map<String, Object>> bossList = AppWorkManager.getDataAuthManager().getUserAffiliate().getDirectBoss(session(), userId,
                null);
        if (bossList != null && bossList.size() > 0) {
            removeInnerBoss(bossList, userId);
        }
    }

    private List<Long> getOldBossList(Long subUserId) {
        List<Long> oldBossIds = new ArrayList<Long>();
        List<Map<String, Object>> oldBossList = AppWorkManager.getDataAuthManager().getUserAffiliate().getDirectBoss(session(),
                subUserId, null);
        if (oldBossList == null || oldBossList.size() == 0)
            return oldBossIds;
        for (Map<String, Object> oldBoss : oldBossList) {
            Long userId = (Long) oldBoss.get("userId");
            oldBossIds.add(userId);
        }
        return oldBossIds;
    }

    private void removeInnerBoss(List<Map<String, Object>> bossList, Long subUserId) {
        if (bossList == null || bossList.size() == 0)
            return;
        List<Long> bossIdList = new ArrayList<Long>();
        List<Long> oldBoss = getOldBossList(subUserId);
        for (Map<String, Object> userMap : bossList) {
            Long bossId = (Long) userMap.get("userId");
            if (oldBoss.contains(bossId)) {
                bossIdList.add(bossId);
            }
        }
        if (bossIdList.size() > 0) {
            AppWorkManager.getDataAuthManager().getUserAffiliate().removeBoss(session(), subUserId, bossIdList);
        }
    }

    private List<Long> removeBossSubs(Long bossId) {
        List<Long> userList = new ArrayList<Long>();
        List<Long> bossList = new ArrayList<Long>();
        bossList.add(bossId);
        List<Map<String, Object>> subUsers = AppWorkManager.getDataAuthManager().getUserAffiliate().getDirectSubUser(session(),
                bossId, null);
        for (Map<String, Object> subUser : subUsers) {
            Long userId = (Long) subUser.get("userId");
            AppWorkManager.getDataAuthManager().getUserAffiliate().removeBoss(session(), userId, bossList);
            userList.add(userId);
        }
        return userList;
    }

    private void addBossSubs(List<Long> subs, Long bossId, boolean append) {
        List<Long> bossList = new ArrayList<Long>();
        bossList.add(bossId);
        for (Long userId : subs) {
            AppWorkManager.getDataAuthManager().getUserAffiliate().addBoss(session(), userId, bossList, append);
        }
    }

    @Override
    public void buildAffiliateByDisableUser(Long userId) {
        // 删除与上级的关系
        removeBoss(userId, false);
        // 删除与下级的关系
        List<Long> userIdList = removeBossSubs(userId);
        // 原有下级移交给BOSS,包含自己
        userIdList.add(userId);
        Long bossId = EnterpriseUtil.getAppUserId();
        addBossSubs(userIdList, bossId, false);
        accountStopPushMsg(userId.toString());
    }


}
