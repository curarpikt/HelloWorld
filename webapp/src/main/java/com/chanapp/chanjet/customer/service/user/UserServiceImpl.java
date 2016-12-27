package com.chanapp.chanjet.customer.service.user;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.US;
import com.chanapp.chanjet.customer.constant.metadata.UserMetaData;
import com.chanapp.chanjet.customer.constant.metadata.UserSettingMetaData;
import com.chanapp.chanjet.customer.reader.CiaReader;
import com.chanapp.chanjet.customer.service.buriedpoint.BuriedPointServiceItf;
import com.chanapp.chanjet.customer.service.cia.CiaServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.role.RoleServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.DeviceUtil;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanapp.chanjet.customer.util.PortalUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.cia.UserInfo;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.impl.common.HttpUtil;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;
import com.chanjet.csp.util.SyncUserUtils;
import com.chanjet.csp.web.security.impl.AuthenticationService;

public class UserServiceImpl extends BaseServiceImpl implements UserServiceItf {

    private final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> paraMap) {
    	 
        return QueryLimitUtil.runCQLQuery(getBusinessObjectHome(UserMetaData.BOname), session(), cqlQueryString,
                paraMap);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runCQLQuery(String cqlQueryString) {
        return QueryLimitUtil.runCQLQuery(getBusinessObjectHome(UserMetaData.BOname), session(), cqlQueryString);
    }

    @SuppressWarnings({ "unchecked", "unused" })
    private List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> paraMap, int start,
            int pageSize) {
        return boDataAccessManager.runCQLQuery(getBusinessObjectHome(UserMetaData.BOname), session(), cqlQueryString,
                paraMap, start, pageSize);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runCQLQuery(String cqlQueryString, int start, int pageSize) {
        return boDataAccessManager.runCQLQuery(getBusinessObjectHome(UserMetaData.BOname), session(), cqlQueryString,
                start, pageSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized Row initUser(BoSession session) {
        UserInfo userInfo = EnterpriseContext.getCurrentUser();
        // TODO 确认USER是否有用
        User user = EnterpriseUtil.getUserById(userInfo.getUserLongId());
        SyncUserUtils.syncUserFromCIA(userInfo.getUserLongId(), false);
        AppUser appUser = EnterpriseUtil.findAppUserByUserId(userInfo.getUserLongId(),
                AppWorkManager.getCurrentAppId());
        // 设置用户埋点信息
        boolean isFisrt = _upsertMonitorUserSet(UserSettingMetaData.LOGINAPP_MONITOR_POINT);
        // 登录埋点，放在最后
        _loginAppMonitorPoint(isFisrt);
        String userRole = ROLE.SYSRELUSER_ROLE_SALESMAN;
        boolean supserUser = false;
        // 非应用管理员才需要初始化数据权限
        if (!appUser.getIsAppSuperUser()) {
            // 绑定业务员角色
            RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
            userRole = roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, user,session);
            // 设置用户上级
            PrivilegeServiceItf privService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);           
            // 设置用户上级
            List<Map<String,Object>> bosslist=AppWorkManager.getDataAuthManager().getUserAffiliate().getAllBoss(session(), userInfo.getUserLongId(), null);
           if(bosslist==null||bosslist.size()==0){
               privService.addBoss(EnterpriseContext.getCurrentUser().getUserLongId(), getSuperAppUserId(),session);  
           }

        } else {
        	if(ServiceLocator.getInstance().lookup(UserServiceItf.class).isBoss(userInfo.getUserLongId())){
                userRole = ROLE.SYSRELUSER_ROLE_BOSS;
                supserUser = true;
        	}else{
        		userRole = ROLE.SYSRELUSER_ROLE_MANAGER;
        	}

        }
        // 初始化首次引导
        boolean loginGuide = _upsertMonitorUserSet(UserSettingMetaData.LOGIN_GUIDE);
      //  Map<String, Object> userMap = dataManager.objectToLinkedHashMap(userInfo);
        Map<String, Object> userMap  = new HashMap<String, Object>();
        try {
        	userMap = BeanUtils.describe(userInfo);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        userMap.put(UserMetaData.userRole, userRole);
        userMap.put(UserMetaData.headPicture, userInfo.getHeadPicture());
        // 兼容错误历史数据
        userMap.put("headPictrue", userInfo.getHeadPicture());
        // 从CIA重新获取企业全称
        CiaServiceItf ciaService = ServiceLocator.getInstance().lookup(CiaServiceItf.class);
        String orgFullName = ciaService.getOrgFullName(userInfo.getOrgId());
        userMap.put(UserMetaData.orgFullName, orgFullName);
        userMap.put("loginGuide", loginGuide);
        userMap.put("superUser", supserUser);
        userMap.put("userRole", userRole);
        userMap.put("name", userInfo.getName());
        userMap.put("headPicture", userInfo.getHeadPicture());
        userMap.put("headPictrue", userInfo.getHeadPicture());
		Row row = new Row();
		row.put("user",  userMap);	
        return row;
    }
    
    

    /*
     * @Override public void setUserBoss(Long bossId) { // 没有传上级ID，默认应用管理员为上级 if
     * (bossId == null) { bossId = getSuperAppUserId(); if (bossId == null) { //
     * TODO 应用管理员可以有多个，后续改成绑定第一个BOSS throw new
     * AppException("app.appuser.superuser.noexits"); } }
     * setUserHierarchy(EnterpriseContext.getCurrentUser().getUserLongId(),
     * bossId); }
     */

    @Override
    public Long getSuperAppUserId() {
     	String value = AppWorkManager.getPriorSetting().getApplicationValue(US.BOSS_VALUE);
     	if(value==null||StringUtils.isEmpty(value)){
     	      throw new AppException("app.appuser.superuser.miss");
     	}
     	return Long.parseLong(value);
    }
    
    public Long getSuperAppUserIdByAppUser() {
        AppUser superApp = null;
        List<AppUser> users = EnterpriseUtil.findAppUserByAppId(AppWorkManager.getCurrentAppId());
        for (AppUser user : users) {
            if (user.getIsAppSuperUser() && superApp != null) {
            	//throw new AppException("app.appuser.superuser.ununique");
            } else if (user.getIsAppSuperUser()) {
                superApp = user;
            }
        }
        if (superApp != null)
            return superApp.getUser().getId();
        return null;
    }

    /**
     * 记录首次登陆记录
     */
    private boolean _upsertMonitorUserSet(String key) {
        IBusinessObjectHome boHome = AppWorkManager.getBusinessObjectManager()
                .getPrimaryBusinessObjectHome(UserSettingMetaData.EOName);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        String deviceType = DeviceUtil.getDeviceType();
        Criteria criteria = Criteria.AND();
        criteria.eq(UserSettingMetaData.deviceType, deviceType);
        criteria.eq(UserSettingMetaData.key, key);
        criteria.eq(UserSettingMetaData.userId, currUserId);
        String queryStr = jsonQueryBuilder.addCriteria(criteria).toJsonQuerySpec();
        Integer count = boHome.getRowCount(queryStr);
        // 首次登陆
        if (count == 0) {
            BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> service = ServiceLocator
                    .getInstance().lookup(UserSettingMetaData.EOName);
            IBusinessObjectRow row = service.createRow();
            row.setFieldValue(UserSettingMetaData.key, key);
            row.setFieldValue(UserSettingMetaData.value, String.valueOf(false));
            row.setFieldValue(UserSettingMetaData.userId, currUserId);
            row.setFieldValue(UserSettingMetaData.deviceType, deviceType);
            service.upsert(row);
            return true;
        } else {
            IBusinessObjectRow row = boHome.query(queryStr).getRow(0);
            row.setFieldValue(UserSettingMetaData.key, String.valueOf(false));
            boHome.upsert(row);
            return false;
        }
    }

    private void _loginAppMonitorPoint(boolean firstLogin) {
        try {
            BuriedPointServiceItf bPointService = ServiceLocator.getInstance().lookup(BuriedPointServiceItf.class);
            if (firstLogin) {
                bPointService.firstLoginPoint();
            }
            bPointService.everyLoginPoint();
        } catch (Exception e) {
            log.error("init writeMsg2BigData Exception ", e);
        }
    }

    /*
     * @Override public void setUserHierarchy(Long userId, Long parentId) {
     * 
     * // TODO 是否每次都需要打开应用和用户的数据权限
     * 
     * QueryLimitUtil.getDataAuthManagement(). setEnable(true, session);
     * UserEnableWithExpandAuth userEnable = new UserEnableWithExpandAuth();
     * //为当前用户打开数据权限 Object[] paras = {userId};
     * ExpandAuthorizationDataAccess.processData(userEnable, paras);
     * 
     * 
     * // 跨权限设置用户上级 SetUserBossWithExpandAuth setUserBoss = new
     * SetUserBossWithExpandAuth(); Object[] paras = { userId, parentId };
     * ExpandAuthorizationDataAccess.processData(setUserBoss, paras); }
     */
    @Override
    public VORowSet<UserValue> queryUserWithCustomerCount(Integer pageNo, Integer pageCount, String status) {
        Integer first = (pageNo - 1) * pageCount;
        UserQuery query = new UserQuery();
        query.setFirst(first);
        query.setMax(pageCount);
        query.setCount(true);
        query.setStatus(status);
        VORowSet<UserValue> retUserSet = getUsersByParam(query);
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        Map<Long, Long> countMap = customerService.getCustomerCountByUser();
        for (UserValue userValue : retUserSet.getItems()) {
            Long count = countMap.get(userValue.getUserId());
            userValue.setCustomercount(count);
        }
        return retUserSet;
    }

    /**
     * <p>
     * 判断是否应用管理员
     * </p>
     * 
     * @param userId
     * @return
     *
     * @author : lf
     * @date : 2016年3月16日
     */
    @Override
    public boolean isBoss(Long userId) {
    	Long bossId = getSuperAppUserId();
    	if(userId.equals(bossId))
    		return true;
    	return false;
    	/*
        AppUser user = EnterpriseUtil.findAppUserByUserId(userId, AppWorkManager.getCurrentAppId());
        if (user.getIsAppSuperUser())
            return true;
        return false;
    */}

    @Override
    public List<UserValue> getAllEnableUse(Long excludeUserId) {
        List<UserValue> retUsers = new ArrayList<UserValue>();
        List<User> users = EnterpriseUtil.getLoginUserList();
        for (User user : users) {
            if (user.isActive() == false || user.getId().equals(excludeUserId))
                continue;
            UserValue userValue = new UserValue();
            userValue.setName(user.getName());
            userValue.setEmail(user.getEmail());
            userValue.setMobile(user.getMobile());
            userValue.setHeadPicture(user.getHeadPicture());
            userValue.setId(user.getId());
            userValue.setUserId(user.getId());
            if(user.isActive()){
                userValue.setStatus(SRU.STATUS_ENABLE);
            }else{
            	userValue.setStatus(SRU.STATUS_DISABLE);	
            }

            userValue.setHeadPic(user.getHeadPicture());
            Set<UserRole> userRoles = user.getUserRoles();
            String roleName = null;
            for (UserRole userRole : userRoles) {
                roleName = userRole.getRole().getName();
                break;
            }
            userValue.setUserRole(roleName);
            retUsers.add(userValue);
        }
        return retUsers;
    }

    /*
     * @Override public IBusinessObjectRowSet getUsersByIds(List<Long> ids) {
     * String _ids = ""; for (Long id : ids) { if (!_ids.isEmpty()) { _ids +=
     * ","; } _ids += id; } String queryStr =
     * JsonQuery.getInstance().setCriteriaStr("userId in (" + _ids +
     * ")").toString(); IBusinessObjectRowSet userRowSet =
     * this.getBusinessObjectHome("CSPUser").query(queryStr); return userRowSet;
     * }
     */

    @Override
    public Boolean isOrgBoss(UserInfo userInfo) {
      //  Boolean isBoss = false;
        Long userId = userInfo.getUserLongId();
        return isBoss(userId);
      //  return isBoss;
    }

    @Override
    public void authBoss() {
        boolean isBoss = isOrgBoss(EnterpriseContext.getCurrentUser());
        if (!isBoss) {
            throw new AppException("app.sysreluser.isnot.boss");
        }
    }

    @Override
    public List<UserValue> getHierarchyUsers(Long userId) {
        return getHierarchyUsers(userId, false);
    }

    /**
     * 获取上下级关系。 containsDisable:true 包含停用用户；false 不包含停用用户
     * 
     * @param userId
     * @param containsDisable
     * @return
     * @throws Exception
     */
    @Override
    public List<UserValue> getHierarchyUsers(Long userId, boolean containsDisable) {
        return getHierarchyUsers(userId, containsDisable, false);
    }

    /**
     * 获取上下级关系。 containsDisable:true 包含停用用户；false 不包含停用用户
     * 
     * @param userId
     * @param containsDisable
     * @return
     * @throws Exception
     */
    @Override
    public List<UserValue> getHierarchyUsers(Long userId, boolean containsDisable, boolean managerHander) {
        Long bossUserId = getSuperAppUserId();
        Assert.notNull(bossUserId, "app.privilege.user.invalid");
        if (userId == null || userId == 0L) {
            userId = bossUserId;
        }
        UserQuery query = new UserQuery();
        if (!userId.equals(bossUserId) && !ROLE.SYSRELUSER_ROLE_MANAGER.equals(getUserRoleName(userId))) {
            List<Map<String, Object>> subUsers = AppWorkManager.getDataAuthManager().getUserAffiliate()
                    .getAllSubUser(session(), userId, null);
            List<Long> ids = new ArrayList<Long>();
            ids.add(userId);
            for (Map<String, Object> userMap : subUsers) {
                Long id = (Long) userMap.get("id");
                ids.add(id);
            }
            query.setUserIds(ids);
        }
        if (!containsDisable) {
            query.setStatus(SRU.STATUS_ENABLE);
        }
        VORowSet<UserValue> rowSet = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
        List<UserValue> userList = rowSet.getItems();
        Map<Long, Long> bossMap = batchLoadDirectBoss();
        for (UserValue user : userList) {
            if (bossMap.containsKey(user.getId())) {
                user.setParentId(bossMap.get(user.getId()));
            }
            // 副总
            if (managerHander && SRU.ROLE_MANAGER.equals(user.getUserRole())) {
                user.setParentId(bossUserId);
            }
            if(user.getHeadPic()!=null)
            	user.setHeadPicture(user.getHeadPic());
        }
        sort(userList, "desc");
        return userList;
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Long> batchLoadDirectBoss() {
        Map<Long, Long> directBoss = new HashMap<Long, Long>();
        String cqlQueryString = "select a.boss.id as bid ,a.user.id as uid from  com.chanjet.system.systemapp.businessobject.CSPAffiliate a where a.directBoss = :directBoss";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("directBoss", true);
        List<Map<String, Object>> result = QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPAffiliate"),
                session(), cqlQueryString, paraMap);
        for (Map<String, Object> affiliate : result) {
            Long userId = (Long) affiliate.get("uid");
            Long bossId = (Long) affiliate.get("bid");
            if (!userId.equals(bossId)) {
                directBoss.put(userId, bossId);
            }
        }
        return directBoss;
    }

    private void sort(List<UserValue> list, final String sort) {
        Collections.sort(list, new Comparator<UserValue>() {
            public int compare(UserValue a, UserValue b) {
                int ret = 0;
                String aFullSpell = a.getFullSpell();
                String bFullSpell = b.getFullSpell();
                if (null == aFullSpell) {
                    aFullSpell = "";
                }
                if (null == bFullSpell) {
                    bFullSpell = "";
                }
                aFullSpell = aFullSpell.toLowerCase();
                bFullSpell = bFullSpell.toLowerCase();
                if ("desc".equals(sort)) {// 倒序
                    ret = aFullSpell.compareTo(bFullSpell);
                } else {
                    ret = bFullSpell.compareTo(aFullSpell);
                }
                return ret;
            }
        });
    }

    /**
     * 根据用户ID获取当前用户的直接上级的用户ID。 查询不到返回NULL。
     * 
     * @param userId 用户ID
     * @return 直接上级的用户ID
     */
    private Long getDirectBoss(Long userId) {
        List<Map<String, Object>> directBossList = AppWorkManager.getDataAuthManager().getUserAffiliate()
                .getDirectBoss(session(), userId, null);
        if (directBossList != null && directBossList.size() > 0) {
            return (Long) directBossList.get(0).get(UserMetaData.userId);
        }
        return null;
    }

    @Override
    public UserValue getUserValueByUserId(Long userId) {
        List<Long> ids = new ArrayList<Long>();
        ids.add(userId);
        UserQuery query = new UserQuery();
        query.setUserIds(ids);
        query.setFirst(0);
        query.setMax(1);
        VORowSet<UserValue> rowSet = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);

        if (rowSet.getItems() == null || rowSet.getItems().size() < 1) {
            return null;
        }
        UserValue userValue = (UserValue) rowSet.getItems().get(0);
        // 设置ParentId
        if (SRU.ROLE_MANAGER.equals(userValue.getUserRole())) {
            Long bossId = getSuperAppUserId();
            if (null != bossId) {
                userValue.setParentId(bossId);
            }
        } else {
            Long directBoss = getDirectBoss(userId);
            if (directBoss != null) {
                userValue.setParentId(directBoss);
            }
        }
        return userValue;
    }

    @Override
    public synchronized UserInfo initOtherUser(UserInfo userInfo,BoSession session) {
        // 获取用户信息
        if (userInfo != null) {
            Long userId = userInfo.getUserLongId();
            log.info("initOtherUser userId:" + userId);
            SyncUserUtils.syncUserFromCIA(userId, false);
            log.info("after syncUserFromCIA  userId:" + userInfo.getUserLongId());
            // 获取用户设置信息,并初始化用户设置信息
            PrivilegeServiceItf privService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
            // 设置用户上级
            privService.addBoss(userId, getSuperAppUserId(),session);

            // 初始化角色为业务员
            User user = EnterpriseUtil.getUserById(userInfo.getUserLongId());
            RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
            roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, user,session);

        }
        return userInfo;
    }

    /*
     * @Override public Map<String, String> disableUser(Long userId) {
     * authBoss(); Map<String, String> result = new
     * com.chanjet.csp.ccs.services.cia.CiaServiceImpl().unAuthUser(userId.
     * toString(), EnterpriseContext.getAppId()); if
     * (result.get("result").equals("true")) { // 删除数据权限
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * removeDataAuth(userId); // 修改上下级的关系
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * buildAffiliateByDisableUser(userId); // 修改用户身份和状态
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * updateSysUserByDisableUser(userId); } else { if
     * ("20310,20312,20121".contains(result.get("errorcode"))) { User user =
     * EnterpriseUtil.getUserById(userId); if (user != null) { // 删除数据权限
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * removeDataAuth(userId); // 修改上下级的关系
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * buildAffiliateByDisableUser(userId); // 修改用户身份和状态
     * ServiceLocator.getInstance().lookup(DataAuthServiceItf.class).
     * updateSysUserByDisableUser(userId); } } else { result.put("errorMsg",
     * MsgUtil.getMsg(result.get("errorcode"))); } } return result; }
     */

    private String validate(String name, String headPic) {
        String result = "";
        try {
            Map<String, String> userInfo = new HashMap<String, String>();
            if (null != name) {
                if (name.length() < 2) {
                    throw new AppException("app.userinfo.username.minlength");
                }
                if (name.length() > 20) {
                    throw new AppException("app.userinfo.username.maxlength");
                }
                Pattern pattern = Pattern.compile("^[a-zA-Z0-9_\u4e00-\u9fa5\\-]{2,20}$");
                Matcher matcher = pattern.matcher(name);
                if (!matcher.matches()) {
                    throw new AppException("app.userinfo.username.illege");
                }
                userInfo.put("name", name);
            }
            if (null != headPic)
                userInfo.put("headPicture", headPic);
            Map<String, String> authMap = new HashMap<String, String>();
            authMap.put("userInfo", dataManager.toJSONString(userInfo));
            String resultJson = HttpUtil.HttpPostAction(CiaReader.getUserInfoUrl() + "?appKey="
                    + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), authMap);
            if (resultJson.contains("\"errorCode\"")) {
                Map<String, Object> tokenMap = dataManager.jsonStringToMap(resultJson);
                result = (String) tokenMap.get("errorCode");
            } else {
                // Map<String, Object> tokenMap =
                // dataManager.jsonStringToMap(resultJson);
            }
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException("app.privilege.userupdate.error");
        }
        return result;
    }

    @Override
    public Map<String, Object> modify(String name, String headPic) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("success", true);
        String tokenResult = validate(name, headPic);
        if (StringUtils.isNotEmpty(tokenResult)) {
            resultMap.put("success", false);
            resultMap.put("code", tokenResult);
            resultMap.put("info", MsgUtil.getMsg(tokenResult));
            return resultMap;
        }

        return resultMap;
    }

    @Override
    public Map<String, Object> switchOrganization(Long orgId) {
        Map<String, Object> map = new HashMap<String, Object>();
        if (orgId > 0) {
            String token = EnterpriseContext.getToken();
            map = PortalUtil.switchOrg(token, String.valueOf(orgId));
        }
        return map;
    }

    @Override
    public Map<String, Object> getAtList(String keyWord) {
        List<UserValue> users = getAllEnableUse(null);
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> data = new HashMap<String, Object>();
        Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Map<String, String>> userList = new ArrayList<Map<String, String>>();
        for (UserValue row : users) {
            String name = row.getName();
            Long id = row.getId();
            if (currUserId.equals(id)) {
                continue;
            }
            String fullSpell = PinyinUtil.hanziToPinyinFull(name, true);
            String shortSpell = PinyinUtil.hanziToPinyinSimple(name, false);
            if (fullSpell == null)
                fullSpell = "";
            if (shortSpell == null)
                shortSpell = "";
            if (name == null)
                name = "";
            if (keyWord==null||StringUtils.isEmpty(keyWord)||name.indexOf(keyWord) != -1 || fullSpell.indexOf(keyWord) != -1 || shortSpell.indexOf(keyWord) != -1) {
                Map<String, String> user = new HashMap<String, String>();
                user.put("name", name);
                user.put("userId", row.getId() + "");
                user.put("headPicture", row.getHeadPicture());
                user.put("email", row.getEmail());
                user.put("mobile", row.getMobile());
                userList.add(user);
            }
        }
        result.put("result", userList);
        data.put("data", result);
        data.put("code", 0);
        data.put("msg", "成功");
        return data;
    }

    @Override
    public Map<String, Object> belongOrganization() {
        String token = EnterpriseContext.getToken();
        Map<String, Object> data = PortalUtil.getOrgListByToken(token);
        return data;
    }

    public VORowSet<UserValue> getUsersByParam(UserQuery query) {
        VORowSet<UserValue> userSet = new VORowSet<UserValue>();
        List<UserValue> retUsers = new ArrayList<UserValue>();
        String cql = query.getCql();
        List<Map<String, Object>> users = null;
        if (query.getFirst() != null && query.getMax() != null) {
            users = runCQLQuery(cql, query.getFirst(), query.getMax());
        } else {
            users = runCQLQuery(cql);
        }

        for (Map<String, Object> userMap : users) {
            UserValue userValue = new UserValue();
            String name = userMap.get("user.name") == null ? null : (String) userMap.get("user.name");
            userValue.setName(name);
            Long userId = userMap.get("user.userId") == null ? null : (Long) userMap.get("user.userId");
            userValue.setId(userId);
            userValue.setUserId(userId);
            String headPic = userMap.get("user.headPicture") == null ? null : (String) userMap.get("user.headPicture");
            userValue.setHeadPic(headPic);
            String userRoleName = getUserRoleName(userId);
            userValue.setUserRole(userRoleName);
            Boolean status = userMap.get("appUser.isActive") == null ? null : (Boolean) userMap.get("appUser.isActive");
            if(status!=null&&status){
            	userValue.setStatus(SRU.STATUS_ENABLE);
            }else{
            	userValue.setStatus(SRU.STATUS_DISABLE);
            }          
            String mobile = userMap.get("user.mobile") == null ? null : (String) userMap.get("user.mobile");
            userValue.setMobile(mobile);
            String email = userMap.get("user.email") == null ? null : (String) userMap.get("user.email");
            userValue.setEmail(email);
            retUsers.add(userValue);

            userSet.setItems(retUsers);
        }
        if (query.isCount()) {
            String countCql = query.getCountCql();
            List<Map<String, Object>> countList = runCQLQuery(countCql);
            Long count = (Long) countList.get(0).get("total");
            userSet.setTotal(count);
        }
        return userSet;
    }

    /*
     * @Override public VORowSet<UserValue> getUsersByParamsPage(HashMap<String,
     * Object> params, String orderPart, int start, int pageSize) {
     * VORowSet<UserValue> userSet = new VORowSet<UserValue>(); List<UserValue>
     * retUsers = new ArrayList<UserValue>(); String cql =
     * "select user.name,user.headPicture,userRole.role.name as rolename,appUser.isActive,user.mobile,user.email "
     * + "from com.chanjet.system.systemapp.businessobject.CSPUser user" +
     * " ,com.chanjet.system.systemapp.businessobject.CSPUserRole userRole " +
     * " ,com.chanjet.system.systemapp.businessobject.CSPAppUser appUser " +
     * " where user.userId =userRole.user.userId and user.userId = appUser.user.userId "
     * ; if (params.containsKey("user.userId")) { String _ids = "0"; List<Long>
     * ids = (List<Long>) params.get("user.userId"); for (Long id : ids) { if
     * (!_ids.isEmpty()) { _ids += ","; } _ids += id; } cql = cql +
     * " and user.userId in(" + _ids + ") "; params.remove("user.userId"); } if
     * (orderPart != null) { cql += orderPart; } else { // 默认按创建时间排序 cql +=
     * " order by user.createdDate desc "; } // 获取总条数 Integer count = null;
     * List<Map<String, Object>> users = null; // 组装UserValue if (start < 0 ||
     * pageSize < 0) { users = runCQLQuery(cql, params); } else { String
     * countCql = "select count(user.userId) as total " +
     * "from com.chanjet.system.systemapp.businessobject.CSPUser user" +
     * " ,com.chanjet.system.systemapp.businessobject.CSPUserRole userRole " +
     * " ,com.chanjet.system.systemapp.businessobject.CSPAppUser appUser " +
     * " where user.userId =userRole.user.userId and user.userId = appUser.user.userId "
     * ; List<Map<String, Object>> countList = runCQLQuery(countCql, params);
     * count = (Integer) countList.get(0).get("total"); users = runCQLQuery(cql,
     * params, start, pageSize); } for (Map<String, Object> userMap : users) {
     * UserValue userValue = new UserValue(); String name =
     * userMap.get("user.name") == null ? null : (String)
     * userMap.get("user.name"); userValue.setName(name); String headPic =
     * userMap.get("user.headPicture") == null ? null : (String)
     * userMap.get("user.headPicture"); userValue.setHeadPic(headPic); String
     * roleName = userMap.get("rolename") == null ? null : (String)
     * userMap.get("rolename"); userValue.setUserRole(roleName); Boolean status
     * = userMap.get("appUser.isActive") == null ? null : (Boolean)
     * userMap.get("appUser.isActive"); userValue.setStatus(status == null ?
     * null : status.toString()); String mobile = userMap.get("user.mobile") ==
     * null ? null : (String) userMap.get("user.mobile");
     * userValue.setMobile(mobile); String email = userMap.get("user.email") ==
     * null ? null : (String) userMap.get("user.email");
     * userValue.setEmail(email); retUsers.add(userValue); }
     * 
     * userSet.setItems(retUsers); userSet.setTotal(count); return userSet; }
     */

    @Override
    public List<UserValue> getUserGroups() {
        List<UserValue> retUser = new ArrayList<UserValue>();
        List<UserValue> users = getAllEnableUse(null);
        for (UserValue user : users) {
            if (ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(user.getUserRole())) {
                retUser.add(user);
            }
        }
        sort(retUser, "desc");
        return retUser;
    }

    @Override
    public UserValue getOrgBoss() {
        Long bossId =EnterpriseUtil.getAppUserId();
        return ServiceLocator.getInstance().lookup(UserServiceItf.class).getUserValueByUserId(bossId);
    }

    @Override
    public Map<String, Object> getHierarchyUsers2Tree(String monthStart, String monthEnd, String bizType) {
        String[] bizTypes = { "KQ", "BF" };
        if (monthStart == null || "".equals(monthStart)) {
            monthStart = DateUtil.formatDate(new Date(), "yyyy-MM");
        }
        if (monthEnd == null || "".equals(monthEnd)) {
            monthEnd = DateUtil.formatDate(new Date(), "yyyy-MM");
        }
        Assert.checkCountDate(monthStart, "app.report.attendanceCount.countDate.illege");
        Assert.checkCountDate(monthEnd, "app.report.attendanceCount.countDate.illege");
        // 教研结束时间小于系统当前时间
        if (bizType == null || "".equals(bizType)) {// KQ：考勤、 BF：拜访
            throw new AppException("app.sysreluser.args.error");
        }

        Boolean bizTypeCheck = false;
        for (int i = 0; i < bizTypes.length; i++) {
            if (bizTypes[i].equals(bizType)) {
                bizTypeCheck = true;
            }
        }
        if (bizTypeCheck == false) {
            throw new AppException("app.sysreluser.args.error");
        }
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();

        Map<String, Object> para = new HashMap<String, Object>();
        para.put("monthStart", monthStart);
        para.put("monthEnd", monthEnd);
        para.put("bizType", bizType);

        Map<String, Object> rsMap = new HashMap<String, Object>();
        List<UserValue> validUsers = new ArrayList<UserValue>();
        rsMap.put("result", true);
        List<UserValue> users = getHierarchyUsers(userId, true);
        if (users == null || users.size() < 1) {
            return rsMap;
        }
        // 过滤考勤无效用户
        if (bizType.equals("KQ") || bizType.equals("BF")) {
            bizFilterKQ(validUsers, users, para);
        }
        if (validUsers == null || validUsers.size() < 1) {
            return rsMap;
        }
        List<Map<String, Object>> groups = treeDatas(validUsers);
        rsMap.put("data", groups);
        return rsMap;
    }

    private void bizFilterKQ(List<UserValue> validUsers, List<UserValue> users, Map<String, Object> para) {
        UserValue user = null;
        String monthEnd = (String) para.get("monthEnd");

        for (int i = 0; i < users.size(); i++) {
            user = users.get(i);
            validUsers.add(user);
//            if (user.getStatus().equals("false")) {// 如果是停用的
//                // 停用的时间
//                Long lastModifiedDateL = user.getLastModifiedDateOut();
//                String lastModifiedDateStr = DateUtil.formatDate(new Date(lastModifiedDateL), "yyyy-MM");
//                if (lastModifiedDateStr.compareTo(monthEnd) >= 0) {// 如果停用时间>=查询时间或之前
//                    validUsers.add(user);
//                }
//            } else {// 如果是正常的
//                validUsers.add(user);
//            }
        }
    }

    @Override
    public String getUserRoleName(Long userId) {
        if (userId == null) {
            return null;
        }       
     //   User testuser =(User)AppWorkManager.getBoDataAccessManager().getDataAuthManagement().getUser(userId, session());        
       
        User user = EnterpriseUtil.getUserById(userId);
        Set<UserRole> roleSet = user.getUserRoles();
        // TODO 现在平台取不到BOSS角色。
    	if(isBoss(user.getUserId())){
    		 return ROLE.SYSRELUSER_ROLE_BOSS;
    	}
        for (UserRole userRole : roleSet) {   
        	if(userRole.getRole()!=null){
            	Long roleId =userRole.getRole().getId();
            	if(roleId==10010||roleId==10011||roleId==10012){
            		   return userRole.getRole().getName();
            	}
        	}             
        }
        AppUser appUser = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());
        if(appUser.getIsAppSuperUser())
        	return ROLE.SYSRELUSER_ROLE_MANAGER;
        return null;
    }

    private List<Map<String, Object>> treeDatas(List<UserValue> validUsers) {
        List<Map<String, Object>> groups = new ArrayList<Map<String, Object>>();
        Map<Long, List<Long>> children = new HashMap<Long, List<Long>>();
        Map<Long, List<Map<String, Object>>> childrenInfo = new HashMap<Long, List<Map<String, Object>>>();

        for (UserValue value : validUsers) {
            // userIds.add(value.getId());
            // add
            Map<Long, Map<String, Object>> userInfo = new HashMap<Long, Map<String, Object>>();
            this.userValue2Map(userInfo, value);
            Long parentId = value.getParentId();
            Long dbUserId = value.getId();
            // 处理副总的parentId 为空的情况
            if ((parentId == null || parentId < 1) && !ROLE.SYSRELUSER_ROLE_MANAGER.equals(value.getUserRole())
                    && !ROLE.SYSRELUSER_ROLE_BOSS.equals(value.getUserRole())) {
                UserValue userValue = getOrgBoss();
                if (userValue != null) {
                    parentId = userValue.getUserId();
                }
            }

            if (parentId != null && parentId > 1) {
                if (!ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(value.getUserRole())) { // 主管不加到老版組內
                    if (children.containsKey(parentId)) {
                        children.get(parentId).add(dbUserId);
                        // add
                        // childrenInfo.get(parentId).add(userInfo);
                        childrenInfo.get(parentId).add(userInfo.get(dbUserId));
                    } else {
                        List<Long> userIdList = new ArrayList<Long>();
                        userIdList.add(dbUserId);
                        children.put(parentId, userIdList);

                        // add
                        // List<Map<Long,Map<String,Object>>> childrenInfoList =
                        // new ArrayList<Map<Long,Map<String,Object>>>();
                        List<Map<String, Object>> childrenInfoList = new ArrayList<Map<String, Object>>();
                        // childrenInfoList.add(userInfo);
                        childrenInfoList.add(userInfo.get(dbUserId));
                        childrenInfo.put(parentId, childrenInfoList);
                    }
                }
            }
            if (ROLE.SYSRELUSER_ROLE_BOSS.equals(value.getUserRole())
                    || ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(value.getUserRole())) {
                Map<String, Object> group = new HashMap<String, Object>();
                group.put("name", value.getName());
                group.put("userId", dbUserId);
                group.put("userRole", value.getUserRole());
                group.put("fullSpell", value.getFullSpell());
                group.put("simpleSpell", value.getShortSpell());
                if (children.containsKey(dbUserId)) {
                    children.get(dbUserId).add(dbUserId);
                    // add
                    childrenInfo.get(dbUserId).add(userInfo.get(dbUserId));
                } else {
                    List<Long> userIdList = new ArrayList<Long>();
                    userIdList.add(dbUserId);
                    children.put(dbUserId, userIdList);
                    // add
                    List<Map<String, Object>> childrenInfoList = new ArrayList<Map<String, Object>>();
                    childrenInfoList.add(userInfo.get(dbUserId));
                    childrenInfo.put(dbUserId, childrenInfoList);
                }
                group.put("childIds", children.get(dbUserId));

                // add
                group.put("childInfos", childrenInfo.get(dbUserId));
                groups.add(group);
            }
        }
        return groups;
    }

    private void userValue2Map(Map<Long, Map<String, Object>> userInfo, UserValue value) {
        Map<String, Object> tmp = new HashMap<String, Object>();
        tmp.put("fullSpell", value.getFullSpell());
        tmp.put("simpleSpell", value.getShortSpell());
        tmp.put("name", value.getName());
        tmp.put("parentId", value.getParentId());
        tmp.put("userId", value.getId());
        userInfo.put(value.getId(), tmp);
    }

    @Override
    public Map<String, Object> uploadHeadPicture(String name, File headPic) {
        Map<String, Object> retValue = null;
        if (null != headPic) {
            retValue = ServiceLocator.getInstance().lookup(CiaServiceItf.class).updateHeadPicture(headPic);
            log.info("picture:retValue:" + retValue);
            if (retValue.get("errorCode") != null)
                return retValue;
        }
        Map<String, String> userInfo = new HashMap<String, String>();
        if (null != name)
            userInfo.put("name", name);
        if (null != headPic && retValue.get("headPicture") != null)
            userInfo.put("headPicture", retValue.get("headPicture").toString());
        Map<String, String> authMap = new HashMap<String, String>();
        authMap.put("userInfo", dataManager.toJSONString(userInfo));
        if (retValue == null) {
            try {
/*            	
            	HttpClientUtils http = HttpClientUtils.getInstance(3000, 5000);
    			PostMethod postMethod = http.post(CiaReader.getUserInfoUrl() + "?appKey="
                        + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), authMap);
    			String retStr = http.resultMethod2String(postMethod);
    	        retValue = dataManager.jsonStringToMap(retStr);*/
                String retStr = HttpUtil.HttpPostAction(CiaReader.getUserInfoUrl() + "?appKey="
                        + EnterpriseContext.getAppKey() + "&access_token=" + EnterpriseContext.getToken(), authMap);
                retValue = dataManager.jsonStringToMap(retStr);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("CiaException:" + CiaReader.getUserInfoUrl() + "?appKey=" + EnterpriseContext.getAppKey()
                        + "&access_token=" + EnterpriseContext.getToken(), e);
            }
        }
        log.info("user:retValue:" + retValue);
        return retValue;
    }

    @Override
    public void logOut() {
        String loginKey = EnterpriseContext.getLoginKey();
        // CIA退出登录
       new com.chanjet.csp.ccs.impl.cia.CiaServiceImpl().logout();
        // 清空session
        if (loginKey != null) {
            AuthenticationService.logoutKey(loginKey);
        }

    }

    @Override
    public List<Long> getAllEnableSubordinate(Long userId) {
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        UserValue userValue = userService.getUserValueByUserId(userId);
        Assert.notNull(userValue, "app.privilege.user.invalid");
        if (ROLE.SYSRELUSER_ROLE_BOSS.equals(userValue.getUserRole())
                || ROLE.SYSRELUSER_ROLE_MANAGER.equals(userValue.getUserRole())) {
            return findUserIds("T", null);
        } else {
            return findAllEnableSubordinate(userId);
        }
    }

    @Override
    public List<Long> findUserIds(String status, Long syncVersion) {

        String cql = "select user.id " + "from com.chanjet.system.systemapp.businessobject.CSPUser user"
                + " ,com.chanjet.system.systemapp.businessobject.CSPAppUser appUser "
                + " where user.userId = appUser.user.userId ";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        if (status != null) {
            if (SRU.STATUS_ENABLE.equals(status)) {
                status = "T";
            } else if (SRU.STATUS_DISABLE.equals(status)) {
                status = "F";
            }
            cql = cql + " and appUser.isActive ='" + status + "' ";
        }
        if (syncVersion != null && syncVersion > 0) {
            cql = cql + " and user." + SC.lastModifiedDate + "> :operateTime" ;
            Timestamp lastDate = new Timestamp(syncVersion);
            paraMap.put("operateTime", lastDate);
        }
        List<Map<String, Object>> resultList = QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPAffiliate"),
                session(), cql,paraMap);
        List<Long> userIds = new ArrayList<Long>();
        if (resultList != null) {
            for (Map<String, Object> result : resultList) {
                if (result != null) {
                    userIds.add((Long) result.get("user.id"));
                }
            }
        }
        return userIds;
    }

    /**
     * 查询用户下属-只包括启用的用户
     */
    @SuppressWarnings("unchecked")
    private List<Long> findAllEnableSubordinate(Long userId) {
        String cqlQueryString = " select a.user.userId as userId from com.chanjet.system.systemapp.businessobject.CSPAffiliate a "
                + "where  a.user.userId <> :userId and a.boss.userId = :userId "
                + "and a.user.userId in (select user.id from com.chanjet.system.systemapp.businessobject.CSPAppUser appUser"
                + " where appUser.isActive ='T' ) " + "order by a.user.name ";

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("userId", userId);
        List<Map<String, Object>> resultList = QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPAffiliate"),
                session(), cqlQueryString, params);
        // 根据result 返回
        List<Long> subordinates = new ArrayList<Long>();
        if (resultList != null) {
            for (Map<String, Object> result : resultList) {
                if (result != null) {
                    subordinates.add((Long) result.get("userId"));
                }
            }
        }
        subordinates.add(userId);
        return subordinates;
    }

    /*
     * @Override public UserValue getOrgBossUserValue() { Long bossId =
     * EnterpriseUtil.getAppUser().getUser().getUserId(); HashMap<String,
     * Object> params = new HashMap<String, Object>(); List<Long> userIds = new
     * ArrayList<Long>(); userIds.add(bossId); params.put("user.userId",
     * userIds); VORowSet<UserValue> userSet =
     * ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam
     * (params, null); if (userSet.getItems() != null &&
     * userSet.getItems().size() > 0) { return (UserValue)
     * userSet.getItems().get(0); } return null; }
     */

    @Override
    public List<Long> getUserIdByName(String name) {
        String cqlQueryString = "select userId as userId from " + getBusinessObjectId("CSPUser")
                + " b where b.name = :name";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("name", name);
        List<Long> list = new ArrayList<Long>();
        List<Map<String, Object>> rs = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> obj : rs) {
            list.add((Long) obj.get("userId"));
        }
        return list;
    }

    @Override
    public boolean chkHierarchyChange(Long syncVersion) {
        String hql = "select count(a.id) as cnt from  " + getBusinessObjectId("CSPUser")
                + " a where a.lastModifiedDate > :syncVersion";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("syncVersion", new Date(syncVersion));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> result = QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPUser"), session(),
                hql, paraMap);
        if (result != null && result.size() > 0) {
            Long cnt = (Long) result.get(0).get("cnt");
            if (cnt > 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean hasHierarchyChange(Long syncVersion) {
        String hql = "select count(a.id) as cnt from  " + getBusinessObjectId("CSPAffiliate")
                + " a where a.lastModifiedDate > :syncVersion";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("syncVersion", new Date(syncVersion));
        List<Map<String, Object>> result = QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPGrant"), session(),
                hql, paraMap);
        if (result != null && result.size() > 0) {
            Long cnt = (Long) result.get(0).get("cnt");
            if (cnt > 0) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void initBossSettingValue(){
    	String value = AppWorkManager.getPriorSetting().getApplicationValue(US.BOSS_VALUE);
    	if(value==null||StringUtils.isEmpty(value)){
    		Long userId = getSuperAppUserIdByAppUser();
    		if(userId!=null&&userId>0)
    			AppWorkManager.getPriorSetting().setApplicationValue(US.BOSS_VALUE, userId.toString());
    	}
    }
    
 
}
