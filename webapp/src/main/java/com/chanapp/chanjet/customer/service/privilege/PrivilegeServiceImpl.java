package com.chanapp.chanjet.customer.service.privilege;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.constant.US;
import com.chanapp.chanjet.customer.constant.VF;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.expandauth.SetUserBossWithExpandAuth;
import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.message.MessageServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.role.RoleServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.customer.util.PortalUtil;
import com.chanapp.chanjet.customer.util.PushMsg;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.customer.vo.system.Role;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.cia.CiaService;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.api.common.Result;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.dataauth.api.UserAffiliate;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;
import com.chanjet.csp.util.ExpandAuthorizationDataAccess;
import com.chanjet.csp.util.SyncUserUtils;

public class PrivilegeServiceImpl extends BaseServiceImpl implements PrivilegeServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(PrivilegeServiceImpl.class);

    private boolean checkDataAuth(String entityName, Long objectId, Long userId, DataAuthPrivilege privilege) {
        try {
        	
        	if(ServiceLocator.getInstance().lookup(UserServiceItf.class).isBoss(userId))
        		return true;
           return boDataAccessManager.getDataAuthorization().isEoAuthorized(session(), entityName, privilege, objectId);
        } catch (Exception e) {
            throw new AppException("app.privilege.checkauth.error");
        }
    }

    @Override
    public boolean checkUpdateDataAuth(String entityName, Long objectId, Long userId) {
        return checkDataAuth(entityName, objectId, userId, DataAuthPrivilege.UPDATE);
    }

    @Override
    public boolean checkSelectDataAuth(String entityName, Long objectId, Long userId) {
        return checkDataAuth(entityName, objectId, userId, DataAuthPrivilege.SELECT);
    }

    @Override
    public boolean checkDeleteDataAuth(String entityName, Long objectId, Long userId) {
        return checkDataAuth(entityName, objectId, userId, DataAuthPrivilege.DELETE);
    }

    @Override
    public boolean checkInsertDataAuth(String entityName, Long objectId, Long userId) {
        return checkDataAuth(entityName, objectId, userId, DataAuthPrivilege.INSERT);
    }

    public List<Long> checkDataAuthList(String entityName, List<Long> objIds, Long userId,
            DataAuthPrivilege privilege) {
        try {
            return boDataAccessManager.getDataAuthorization().isEoAuthorizedList(session(), entityName, privilege,
                    objIds);
        } catch (Exception e) {
            throw new AppException("app.privilege.checkauth.error");
        }
    }

    @Override
    public List<Long> checkSelectDataAuthList(String entityName, List<Long> objIds, Long userId) {
        return checkDataAuthList(entityName, objIds, userId, DataAuthPrivilege.SELECT);
    }

    @Override
    public List<Long> checkInsertDataAuthList(String entityName, List<Long> objIds, Long userId) {
        return checkDataAuthList(entityName, objIds, userId, DataAuthPrivilege.INSERT);
    }

    @Override
    public List<Long> checkUpdateDataAuthList(String entityName, List<Long> objIds, Long userId) {
        return checkDataAuthList(entityName, objIds, userId, DataAuthPrivilege.UPDATE);
    }

    @Override
    public List<Long> checkDeleteDataAuthList(String entityName, List<Long> objIds, Long userId) {
        return checkDataAuthList(entityName, objIds, userId, DataAuthPrivilege.DELETE);
    }

    private final static int SELECT_PRVILEGE = Integer.valueOf("001", 2);
    private final static int UPDATE_PRVILEGE = Integer.valueOf("100", 2);// 暂时用4以后强制升级更新会                                                                         // 3：011
    private final static int ALL_PRVILEGE = Integer.valueOf("111", 2);

    @Override
    public void setRowPrivilegeField(List<Long> ids, String entityName, List<Map<String, Object>> rows) {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Long> updateIds = checkUpdateDataAuthList(entityName, ids, userId);
        List<Long> delIds = checkDeleteDataAuthList(entityName, ids, userId);
        for (Map<String, Object> row : rows) {
            Long id = (Long) row.get(SC.id);
            if (delIds != null && delIds.contains(id)) {
                row.put(VF.privilege, ALL_PRVILEGE);
            } else if (updateIds != null && updateIds.contains(id)) {
                row.put(VF.privilege, UPDATE_PRVILEGE);
            } else {
                row.put(VF.privilege, SELECT_PRVILEGE);
            }
        }
    }

    @Override
    public List<Long> saveHierarchyUsers(List<UserValue> userValues,BoSession session) {
    	//ServiceLocator.getInstance().lookup(UserSettingServiceItf.class).lockAppUserChange(session);
        Assert.authBoss(EnterpriseContext.getCurrentUser().getUserLongId());
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        List<UserValue> dbUsers = userService.getHierarchyUsers(null);
        Map<Long, UserValue> sysRelUserVosMap = new HashMap<Long, UserValue>();
        for (UserValue userValue : userValues) {
            sysRelUserVosMap.put(userValue.getId(), userValue);
        }

        Long start = System.currentTimeMillis();
        // 验证前台参数
        _checkHierarchyUsers(userValues, sysRelUserVosMap);
        logger.info("checkHierarchyUsers use time = " + (System.currentTimeMillis() - start) + " ms");
        // 保存权限
        List<Long> syncUsers = _saveHierarchyUsers(userValues,session);
        logger.info("_saveHierarchyUsers use time = " + (System.currentTimeMillis() - start) + " ms");
        // 消息推送
        _checkChangeAndPushMsg(dbUsers, userValues);
        return syncUsers;
    }

    private static void _checkChangeAndPushMsg(List<UserValue> dbSysRelUsers, List<UserValue> newSysRelUsers) {
        String orgId = PortalUtil.getOrgId();
        String orgName = PortalUtil.getOrgNameById(orgId);
        if (StringUtils.isNotEmpty(orgId)) {
            try {
                Map<Long, String> newMap = _parseSysRelUsers(newSysRelUsers);
                Map<Long, String> oldMap = _parseSysRelUsers(dbSysRelUsers);
                Iterator<Entry<Long, String>> it = newMap.entrySet().iterator();
                String userName = EnterpriseContext.getCurrentUser().getName();
                // 推送消息
                String content = "变更了你的权限，系统将执行最新的权限控制";
                String alert = "【" + userName + "】变更了你的权限，系统将执行最新的权限控制";
                String type = IM.PERMISSIONS_CHANGE;
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
                while (it.hasNext()) {
                    Entry<Long, String> entry = it.next();
                    Long userId = entry.getKey();
                    String newProps = entry.getValue();
                    String oldProps = oldMap.get(userId);
                    if (!newProps.equals(oldProps)) {
                        // push(null, userId, body,
                        // EnterpriseContext.getToken());
                        if (!userIds.contains(userId)) {
                            userIds.add(userId);
                        }
                    }
                }
                if (userIds.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (Long userId : userIds) {
                        sb.append(userId).append(",");
                    }
                    List<Long> userIdsTmp = new ArrayList<Long>();
                    for (Long userId : userIds) {
                        userIdsTmp.add(Long.valueOf(userId));
                    }
                    String msgType = PushMsg.getMsgType(from);
                    ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(null, msgType, userIdsTmp,
                            dataManager.toJSONString(r));
                    // 剔除老板
                    Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
                    if(userId!=null){
                    	userIds.remove(userId);
                    }
                    logger.info("ChangeAuthAndPushMsg pushIds={}", sb);
                    PushMsg.asynPush(from, null, userIds, dataManager.toJSONString(r),
                            EnterpriseContext.getCurrentUser().getUserLongId(), EnterpriseContext.getToken());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<Long, String> _parseSysRelUsers(List<UserValue> sysRelUsers) {
        Map<Long, String> sysRelUsersMap = new HashMap<Long, String>();
        for (UserValue outterValue : sysRelUsers) {
            Long parentId = outterValue.getParentId();
            if (parentId == null) {
                parentId = 0l;
            }
            Long userLevel = outterValue.getUserLevel();
            if (userLevel == null) {
                userLevel = 0l;
            }
            String userRole = outterValue.getUserRole();
            if (userRole == null) {
                userRole = "";
            }

            String newUserProp = parentId + "|" + userLevel + "|" + userRole + "|";
            Long userId = outterValue.getId();
            // 所有下级ID
            Set<Long> children = new HashSet<Long>();
            for (UserValue innerValue : sysRelUsers) {
                if (userId.equals(innerValue.getParentId())) {
                    children.add(innerValue.getId());
                }
            }
            Iterator<Long> it = children.iterator();
            while (it.hasNext()) {
                newUserProp = newUserProp + it.next();
            }
            sysRelUsersMap.put(userId, newUserProp);
        }
        return sysRelUsersMap;
    }

    private static UserValue _checkHierarchyUsers(List<UserValue> sysRelUserVos, Map<Long, UserValue> sysRelUserVosMap)
            throws AppException {
        if (null == sysRelUserVos) {
            logger.info("checkHierarchyUsers exception: null == sysRelUserVos");
            throw new AppException("app.sysreluser.args.error");
        }
        List<UserValue> boss = new ArrayList<UserValue>();
        List<Long> parentIds = new ArrayList<Long>();
        List<Long> userIds = new ArrayList<Long>();
        for (UserValue userValue : sysRelUserVos) {
            Long userId = userValue.getId();
            User user = EnterpriseUtil.getUserById(userId);
            if (user == null)
                throw new AppException("app.privilege.user.invalid");

            if (user.isActive() == false) {
                logger.info("user:"+user.getName()+" isactive:"+user.isActive());
                throw new AppException("app.sysreluser.args.error");
            }
            String userRole = userValue.getUserRole();
            Long parentId = userValue.getParentId();
            if (parentId != null) {
                parentIds.add(parentId);
            }
            // 业务员和管理员不能作为别人的上级
            if (ROLE.SYSRELUSER_ROLE_SALESMAN.equals(userRole) || ROLE.SYSRELUSER_ROLE_MANAGER.equals(userRole)) {
                userIds.add(userValue.getId());
            }
            if (ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(userRole) && parentId == null) {
                logger.info(
                        "checkHierarchyUsers exception: Constants.SYSRELUSER_ROLE_SUPERISOR.equals(userRole) && parentId == null,userRole="
                                + userRole);
                throw new AppException("app.sysreluser.args.error");
            }
            if (ROLE.SYSRELUSER_ROLE_BOSS.equals(userRole)) {
                boss.add(userValue);
            }
        }
        for (Long uid : userIds) {
            if (parentIds.contains(uid)) {
                logger.info("checkHierarchyUsers exception: parentIds.contains(uid),uid=" + uid);
                throw new AppException("app.sysreluser.args.error");
            }
        }
        if (boss.size() != 1) {// 只有一个老板
            logger.info("checkHierarchyUsers exception: boss.size() != 1");
            throw new AppException("app.sysreluser.args.error");
        }
        return boss.get(0);
    }

    private List<Long> _saveHierarchyUsers(List<UserValue> userValues,BoSession session) {
        Map<Long, UserValue> sysRelUserVosMap = new HashMap<Long, UserValue>();
        for (UserValue userValue : userValues) {
            sysRelUserVosMap.put(userValue.getId(), userValue);
        }
        Long start = System.currentTimeMillis();
    	List<Long> syncUsers =_saveDifferUserRole(userValues,session);
        logger.info("saveDiffUserRole use time = " + (System.currentTimeMillis() - start) + " ms");
        _saveAffiliate(userValues,session);
        logger.info("saveAffiliate use time = " + (System.currentTimeMillis() - start) + " ms");
        return syncUsers;
    }

    private void _saveAffiliate(List<UserValue> userValues,BoSession session) {
        Long superUserId = ServiceLocator.getInstance().lookup(UserServiceItf.class).getSuperAppUserId();
        for (UserValue useValue : userValues) {
            // 主管和业务员
            if (ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(useValue.getUserRole())
                    || ROLE.SYSRELUSER_ROLE_SALESMAN.equals(useValue.getUserRole())) {
                if (useValue.getParentId() == null) {
                    useValue.setParentId(superUserId);
                }
                if (!isDirectBoss(useValue.getId(), useValue.getParentId())) {
                    // 删除原来的上下级关系
                    removeBossById(useValue.getUserId(),session);
                    // 增加新BOSS
                    addBoss(useValue.getId(), useValue.getParentId(),session);
                }
            } else {
                // 删除原来的上下级关系
                removeBossById(useValue.getUserId(),session);
            }
        }
    }

    @Override
    public void addBoss(Long userId, Long bossId,BoSession session) {
        if (bossId == 0L) {
            throw new AppException("app.sysreluser.args.error");
        }
        List<Long> bossList = new ArrayList<Long>();
        bossList.add(bossId);
        if (!bossList.contains(userId)) {
            SetUserBossWithExpandAuth setUserBoss = new SetUserBossWithExpandAuth();
            Object[] paras = { userId, bossList,session};
            ExpandAuthorizationDataAccess.processData(setUserBoss, paras);
        }
    }



    @Override
    public List<Long> removeSubsById(Long userId,BoSession session) {
        List<Long> userList = new ArrayList<Long>();
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        List<Map<String, Object>> subUsers = user.getDirectSubUser(session, userId, null);
        List<Long> bossList = new ArrayList<Long>();
        bossList.add(userId);
        for (Map<String, Object> subUser : subUsers) {
            Long subId = (Long) subUser.get("userId");
            userList.add(subId);
            user.removeBoss(session(), subId, bossList);
        }
        return userList;
    }

    private List<Long> _saveDifferUserRole(List<UserValue> userValues,BoSession session) {
    	List<Long> syncUsers = new ArrayList<Long>();
        for (UserValue value : userValues) {
            String roleName = value.getUserRole();
            if (ROLE.SYSRELUSER_ROLE_BOSS.equals(roleName))
                continue;
            Role newRole = EnterpriseUtil.getRoleByName(roleName);
            User user = EnterpriseUtil.getUserById(value.getId());
            AppUser  appUser = EnterpriseUtil.findAppUserByUserId(value.getId(),AppWorkManager.getCurrentAppId());
            // 前台角色和数据角色不一样
            if (!EnterpriseUtil.hasRole(user, newRole)) {
                // 删除老角色
                for (UserRole role : user.getUserRoles()) {
                    EnterpriseUtil.deleteUserRole(role,session);
                }
                //副总不需要设置ROLE。直接用调用CIA设置应用管理员
                if (ROLE.SYSRELUSER_ROLE_MANAGER.equals(roleName)){
                    // 通知CIA增加管理员权限
                    ServiceLocator.getInstance().lookup(CiaServiceItf.class).addAppManager(value.getId());
                    syncUsers.add(value.getId());
                   // SyncUserUtils.syncUserFromCIA(value.getId(), false);
                    continue;
                }else if(appUser.getIsAppSuperUser()){
                    ServiceLocator.getInstance().lookup(CiaServiceItf.class).cancelAppManager(value.getId());
                    syncUsers.add(value.getId());
                    //SyncUserUtils.syncUserFromCIA(value.getId(), false);
                }
                // 绑定新角色
                UserRole newUserRole = new UserRole();
                newUserRole.setRole(newRole);
                newUserRole.setUser(user);
                EnterpriseUtil.createUserRole(newUserRole,session);
            }
        }
        return syncUsers;
    }

    @Override
    public void changeBoss(Long bossId,BoSession session) {
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        // Assert.authBoss(EnterpriseContext.getCurrentUser().getUserLongId());
        User user = EnterpriseUtil.getUserById(bossId);
        if (user == null || user.isActive() == false) {
            throw new AppException("app.privilege.user.invalid");
        }
        Long oldBossId = userService.getSuperAppUserId();
        if (bossId.equals(oldBossId)) {
            return;
        }
        // 更新上下级关系
        _updateAffilitaByChangeBoss(bossId, oldBossId,session);
        // 更新用户角色
        _updateRoleByChangeBoss(bossId, oldBossId,session);
        // 通知CIA增加管理员权限
        ServiceLocator.getInstance().lookup(CiaServiceItf.class).addAppManager(bossId);
        // 取消CIA的管理员权限
        ServiceLocator.getInstance().lookup(CiaServiceItf.class).cancelAppManager(oldBossId);
        // TODO 集测 环境使用，线上可以去掉
        SyncUserUtils.syncUserFromCIA(bossId, false);
        SyncUserUtils.syncUserFromCIA(oldBossId, false);
        //设置应用管理员标示
		AppWorkManager.getPriorSetting().setApplicationValue(US.BOSS_VALUE, bossId.toString());
        // 给新BOSS推送消息
         _changeBossPushMsg(bossId.toString());
    }

    private void _updateRoleByChangeBoss(Long bossId, Long oldBossId,BoSession session) {
        // TODO 需要平台提供变化BOSS角色的方法
        User oldBoss = EnterpriseUtil.getUserById(oldBossId);
        RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
        // 给原BOSS创建业务员角色
        roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, oldBoss,session);
        User newBoss = EnterpriseUtil.getUserById(bossId);
        roleService.deleteUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, newBoss,session);
    }

    private void _updateAffilitaByChangeBoss(Long bossId, Long oldBossId,BoSession session) {
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        try {
            // 去掉新BOSS与原来上级的关系
            removeBossById(bossId,session);
            // 获取老BOSS的下属
            List<Long> newSubList = new ArrayList<Long>();
            // 删除老BOSS的所有下级
            List<Long> subs = removeSubsById(oldBossId,session);
            if (subs != null && subs.size() > 0) {
                newSubList.addAll(subs);
            }
            // 添加给新BOSS，append=false代表不追加,包括原BOSS和原BOSS直属下属
            newSubList.add(oldBossId);
            List<Long> newBossList = new ArrayList<Long>();
            newBossList.add(bossId);
            for (Long newSub : newSubList) {
                user.addBoss(session(), newSub, newBossList, false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private static void _changeBossPushMsg(String userId) {
        try {
            String orgId = PortalUtil.getOrgId();
            String orgName = PortalUtil.getOrgNameById(orgId);
            if (StringUtils.isNotEmpty(orgId)) {
                // 推送消息
                String userName = EnterpriseContext.getCurrentUser().getName();
                String content = "变更了你的权限，系统将执行最新的权限控制";
                String alert = "【" + userName + "】变更了你的权限，系统将执行最新的权限控制";
                String type = IM.PERMISSIONS_CHANGE;
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
                operator.put("username", userName);
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
                if(userId!=null)
                	userIds.add(Long.parseLong(userId));
                logger.info("changeBossPushMsg pushIds={}", userIds);
                PushMsg.asynPush(from, null, userIds, dataManager.toJSONString(r),
                        EnterpriseContext.getCurrentUser().getUserLongId(), EnterpriseContext.getToken());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<String, String> disableUser(Long userId,BoSession session) {
    	Map<String, String> retMap = new HashMap<String, String>();
        CiaService ciaService = new com.chanjet.csp.ccs.impl.cia.CiaServiceImpl();
        Result<Boolean> result= ciaService.unAuthUser(userId.toString(), EnterpriseContext.getAppId());
        String code = result.getCode();
        if (code==null||("20310,20312,20121".contains(code))) {
            // AppWorkManager.get
            User user = EnterpriseUtil.getUserById(userId);
            SyncUserUtils.syncUserFromCIA(userId, false);
            // Long userRoleId = null ;
            UserRole _userRole = null;
            for (UserRole userRole : user.getUserRoles()) {
                _userRole = userRole;
                break;
            }
            // UserRole _userRole
            // =(UserRole)EnterpriseUtil.getUserRoleById(userRoleId);
            if (_userRole != null) {
                // user.getUserRoles().clear();
                EnterpriseUtil.deleteUserRole(_userRole,session);
            }
            // 修改上下级的关系
            updateAffilitaByDisableUser(userId,session);
            // 消息推送
            _accountStopPushMsg(userId.toString());
            retMap.put("result", "true");
        } else {
        	retMap.put("message", MsgUtil.getMsg(result.getCode()));
        }

        return retMap;
    }

    private void updateAffilitaByDisableUser(Long userId,BoSession session) {
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        // 删除上级
        removeBossById(userId,session);
        // 删除下级
        List<Long> userIdList = removeSubsById(userId,session);
        // 原有下级移交给BOSS,包含自己
        userIdList.add(userId);   
        Long bossId = EnterpriseUtil.getAppUserId();
        List<Long> bossList = new ArrayList<Long>();
        bossList.add(bossId);
        for (Long subId : userIdList) {
            user.addBoss(session(), subId, bossList, false);
        }
    }

    private static void _accountStopPushMsg(String userId) {
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
            if(userId!=null)
            	userIds.add(Long.parseLong(userId));
            if (userIds.size() > 0) {   
                String msgType = PushMsg.getMsgType(from);
                ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(null, msgType, userIds,
                        dataManager.toJSONString(r));
                PushMsg.asynPushNoAuthUser(from, null, userIds, dataManager.toJSONString(r),
                        EnterpriseContext.getCurrentUser().getUserLongId(), EnterpriseContext.getToken());
            }
        }
    }

    @Override
    public Map<String, String> diableTransData(Long userId, Long transId,BoSession session) {
        Assert.notNull(userId, "app.privilege.user.tran.paraerror");
        Assert.notNull(transId, "app.privilege.user.tran.paraerror");
        Assert.authBoss(EnterpriseContext.getCurrentUser().getUserLongId());
        return disableUser(userId,session);
    }

    /**
     * <p>
     * 移交客户
     * </p>
     * 
     * @param fromUserId
     * @param toUserId
     * @param para
     * @return
     *
     * @author : lf
     * @date : 2016年4月12日
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> transCustomer(Long fromUserId, Long toUserId, Map<String, Object> para) {
        Map<String, Object> result = new HashMap<String, Object>();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> customerService = ServiceLocator
                .getInstance().lookup(BO.Customer);
        // 按客户移交
        if (para.containsKey(CustomerMetaData.owner)) {
            criteria.eq(CustomerMetaData.owner, para.get(CustomerMetaData.owner));
        }

        // 按客户ID移交
        if (para.containsKey(SC.id)) {
            List<Long> ids = (List<Long>) para.get(SC.id);
            criteria.in(SC.id, ids.toArray());
        }
        // 按条件移交
        if (para.containsKey(CustomerMetaData.conditions)) {
            String conditions = (String) para.get(CustomerMetaData.conditions);
            CustomerServiceItf customerItf = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
            List<Long> ids = customerItf.getCustomerIdsByCondtion(conditions);
            criteria.in(SC.id, ids.toArray());
        }

        String jsonQuery = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        ICustomerRowSet IcustomerSet = (ICustomerRowSet) customerService.query(jsonQuery);
        List<ICustomerRow> customers = IcustomerSet.getCustomerRows();
        int count = customerService.batchUpdate(jsonQuery, new String[] { CustomerMetaData.owner },
                new Object[] { toUserId });
        if (count > 0) {
            _afterTransJobs(toUserId, fromUserId, count, customers);
        }
        result.put("result", true);
        return result;
    }

    private void _afterTransJobs(Long toUserId, Long fromUserId, int count, List<ICustomerRow> customers) {
        User toUser = EnterpriseUtil.getUserById(toUserId);  
        User fromUser = EnterpriseUtil.getUserById(fromUserId);
        String content = "";
        if (fromUser != null && toUser != null) {
            content = "将" + fromUser.getName() + "的" + count + "个客户移交给了" + toUser.getName();
        }
        List<Long> ids = new ArrayList<Long>();
        for (ICustomerRow customer : customers) {
            ids.add(customer.getId());
        }
        // 记录移交日志
        String operTag = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).transLog(ids, toUserId,
                content, CustomerMetaData.EOName);
        // 移交人消息推送
        _sendMsg(fromUser, toUser, count, operTag, customers);
    }

    private static void _sendMsg(User fromUser, User touser, int count, String operTag, List<ICustomerRow> customers) {
        Assert.notNull(touser);
        Long userId = touser.getId();
        String userName = EnterpriseContext.getCurrentUser().getName();
        Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        String customerName = "";
        for (ICustomerRow customer : customers) {
            customerName = customer.getName();
            break;
        }
        if (count > 1) {
            customerName = "【" + customerName + "】等客户";
        } else {
            customerName = "客户【" + customerName + "】";
        }
        String alert = userName + "已将" + customerName + "移交给您,请查收";
        MsgUtil.sengMsg(operTag, alert, userId, IM.CUSTOMER_TRANSFER);
        if (!currUserId.equals(fromUser.getUserId())) {
            String toName = touser.getName();
            alert = userName + "将您的" + customerName + "移交给" + toName + ",请获悉";
            MsgUtil.sengMsg(operTag, alert, fromUser.getUserId(), IM.CUSTOMER_TRANSFER);
        }
    }

    public boolean isDirectBoss(Long userId, Long parentId) {
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        List<Map<String, Object>> bossList = user.getDirectBoss(session(), userId, null);
        for (Map<String, Object> bossMap : bossList) {
            Long bossId = (Long) bossMap.get("userId");
            if (bossId != null && bossId.longValue() == parentId) {
                return true;
            }
        }
        return false;
    }

	@Override
	public void removeBossById(Long userId, BoSession session) {
        UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
        List<Map<String, Object>> bossList = user.getDirectBoss(session, userId, null);
        List<Long> bossIds = new ArrayList<Long>();
        for (Map<String, Object> bossMap : bossList) {
            Long bossId = (Long) bossMap.get("userId");
            bossIds.add(bossId);
        }
        if (bossIds != null && bossIds.size() > 0) {
            user.removeBoss(session, userId, bossIds);
        }
    
	}
    
/*    @Override
    public void handerAppUserChange(BoSession session, String entityId,Long userId,boolean origisSuperUser){
		//AppUser appUser = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());
		UserHierarchyManager hierarchyMa =new UserHierarchyManager(userId);
		hierarchyMa.handerAppUserChange(session, entityId, userId, origisSuperUser);
		if(hierarchyMa.checkAppUserChange(origisSuperUser)){	
			 UserServiceItf userService =  ServiceLocator.getInstance().lookup(UserServiceItf.class);
	         Long appUserId = userService.getSuperAppUserId();
	         User user = EnterpriseUtil.getUserById(userId);
			//非管理员变成应用管理员
			if(appUser.getIsAppSuperUser()){			
                // 删除老角色
                for (UserRole role : user.getUserRoles()) {
                    EnterpriseUtil.deleteUserRole(role);
                }
                //删除上下级关系
                List<Long> allSubs = removeSubsById(userId);
                removeBossById(userId);      
                //下属移交给BOSS
                for(Long subId:allSubs){
                	addBoss(subId,appUserId);
                }
			}
			//应用管理员变成非应用管理员
			else{
				//取消了BOSS的应用管理员
				if(userService.isAppSuperUser(userId)){
					List<AppUser> appUsers = EnterpriseUtil.findAppUserByAppId(AppWorkManager.getCurrentAppId());
					if(appUsers!=null&&appUsers.size()>0){
						AppUser newBoss = appUsers.get(0);
						changeBoss(newBoss.getUser().getId());
					}
				}
				//取消了副总的应用管理员
				else{
		            // 绑定业务员角色
		            RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
		            roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, user);
		            addBoss(userId, appUserId);
				}				
			}
		}	
    }*/
    
}
