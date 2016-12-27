package com.chanapp.chanjet.customer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.US;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.customer.vo.system.Role;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanapp.chanjet.web.constant.SBO;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAuthManagement;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.bo.api.CqlQuery;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.common.base.dataauth.UserRoleObject;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

/**
 * 
 * 封装对系统EO的操作
 * 
 * @author tds
 *
 */
public class EnterpriseUtil {
    private static final IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();
    private static final BoDataAuthManagement dataAuthManagement = AppWorkManager.getBoDataAccessManager()
            .getDataAuthManagement();

    private static BoSession session() {
        return AppContext.session();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getById(Long id, String boName) {
        String boFullName = getSystemBoFullName(boName);
        IBusinessObjectHome boHome = boManager.getPrimaryBusinessObjectHome(boName);
        String cql = "from " + boFullName + " where id = :id";
        CqlQuery query = session().createCqlQuery(cql, boHome);
        query.setLong("id", id);
        Map<String, Object> result = (Map<String, Object>) query.uniqueResult();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> getList(String cql, String boName, Map<String, Object> params) {
        IBusinessObjectHome boHome = boManager.getPrimaryBusinessObjectHome(boName);
        CqlQuery query = session().createCqlQuery(cql, boHome);
        if (params != null) {
            for (String s : params.keySet()) {
                query.setParameter(s, params.get(s));
            }
        }
        
        List<Map<String, Object>> results = (List<Map<String, Object>>) query.list();
        return results;
    }

    public static String getSystemBoFullName(String boName) {
        return "com.chanjet.system.systemapp.businessobject." + boName;
    }

    public static User getUserById(Long id) {
        Map<String, Object> userObj = getById(id, SBO.CSPUser);       
        return getUserFromMap(userObj);
    }
    
    public static User getUserNameAndHeadPicById(Long id){
    	  User user = new User();
    	  String cql = "select id,name,headPicture from " + getSystemBoFullName(SBO.CSPUser)+ " where id = :id";
    	  IBusinessObjectHome boHome = boManager.getPrimaryBusinessObjectHome(SBO.CSPUser);
    	  CqlQuery query = session().createCqlQuery(cql, boHome);
          query.setLong("id", id);
          Map<String, Object> result = (Map<String, Object>) query.uniqueResult();
          user.setId((Long)result.get("id"));
          user.setName((String)result.get("name"));
          user.setHeadPicture((String)result.get("headPicture"));
    	  return user;
    }

    public static List<Role> getRoleList() {
        String cql = "from " + getSystemBoFullName(SBO.CSPRole)+" where id in(10010,10011,10012)";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> results = (List<Map<String, Object>>) getList(cql, SBO.CSPRole, params);
        if (results == null || results.size() == 0) {
            return null;
        }
        List<Role> roles = new ArrayList<Role>();
        for (Map<String, Object> roleObj : results) {
            Role role = new Role();
            role.setId((Long) roleObj.get(SBO.CSPRole + ".id"));
            role.setName((String) roleObj.get(SBO.CSPRole + ".name"));
            roles.add(role);
        }
        return roles;
    }

    public static boolean hasRole(User user, Role role) {
        if (user == null || role == null) {
            return false;
        }
        for (UserRole userRole : user.getUserRoles()) {
        	String roleName = userRole.getRole().getName();
            if (role.getName().equals(roleName)) {
                return true;
            }
        }
        return false;
    }

    public static Role getRoleByName(String roleName) {
        String cql = "from " + getSystemBoFullName(SBO.CSPRole) + " where name = '" + roleName + "'";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPRole, params);
        if (roleList != null && roleList.size() > 0) {
            Role role = new Role();
            role.setId((Long) roleList.get(0).get(SBO.CSPRole + ".id"));
            role.setName((String) roleList.get(0).get(SBO.CSPRole + ".name"));
            return role;
        } else {
            return null;
        }
    }

    public static AppUser findAppUserByUserId(Long userId, String appId) {
        String cql = "from " + getSystemBoFullName(SBO.CSPAppUser) + " where user =" + userId + " and appId='" + appId
                + "'";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPAppUser, params);
        if (roleList != null && roleList.size() > 0) {
            AppUser appUser = new AppUser();
            appUser.setId((Long) roleList.get(0).get(SBO.CSPAppUser + ".id"));
            appUser.setAppId((String) roleList.get(0).get(SBO.CSPAppUser + ".appId"));
            appUser.setIsActive((Boolean) roleList.get(0).get(SBO.CSPAppUser + ".isActive"));
            appUser.setIsAppSuperUser((Boolean) roleList.get(0).get(SBO.CSPAppUser + ".isAppSuperUser"));
            User user = new User();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) roleList.get(0).get(SBO.CSPAppUser + ".user");
            user.setId((Long) map.get("id"));

            appUser.setUser(user);
            return appUser;
        } else {
            return null;
        }
    }

    public static List<AppUser> findAppUserByAppId(String appId) {
        String cql = "from " + getSystemBoFullName(SBO.CSPAppUser) + " where appId='" + appId + "'";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPAppUser, params);
        List<AppUser> appUsers = new ArrayList<AppUser>();
        for(Map<String, Object> appUserMap : roleList){
            AppUser appUser = new AppUser();
            appUser.setId((Long) appUserMap.get(SBO.CSPAppUser + ".id"));
            appUser.setAppId((String) appUserMap.get(SBO.CSPAppUser + ".appId"));
            Boolean isActive = (Boolean) appUserMap.get(SBO.CSPAppUser + ".isActive");
            if(isActive==false)
            	continue;
            appUser.setIsActive((Boolean) appUserMap.get(SBO.CSPAppUser + ".isActive"));
            appUser.setIsAppSuperUser((Boolean) appUserMap.get(SBO.CSPAppUser + ".isAppSuperUser"));
            User user = new User();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) appUserMap.get(SBO.CSPAppUser + ".user");
            user.setId((Long) map.get("id"));
            appUser.setUser(user);
            appUsers.add(appUser);
        }
        return appUsers;
    }
    
    public static List<AppUser> findSuperAppUserByAppId(String appId) {
        String cql = "from " + getSystemBoFullName(SBO.CSPAppUser) + " where appId='" + appId + "'  and isAppSuperUser='t' ";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPAppUser, params);
        List<AppUser> appUsers = new ArrayList<AppUser>();
        for(Map<String, Object> appUserMap : roleList){
            AppUser appUser = new AppUser();
            appUser.setId((Long) appUserMap.get(SBO.CSPAppUser + ".id"));
            appUser.setAppId((String) appUserMap.get(SBO.CSPAppUser + ".appId"));
            Boolean isActive = (Boolean) appUserMap.get(SBO.CSPAppUser + ".isActive");
            if(isActive==false)
            	continue;
            appUser.setIsActive((Boolean) appUserMap.get(SBO.CSPAppUser + ".isActive"));
            appUser.setIsAppSuperUser((Boolean) appUserMap.get(SBO.CSPAppUser + ".isAppSuperUser"));
            User user = new User();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) appUserMap.get(SBO.CSPAppUser + ".user");
            user.setId((Long) map.get("id"));
            appUser.setUser(user);
            appUsers.add(appUser);
        }
        return appUsers;
    }

    public static List<User> getLoginUserList() {
        String cql = "select u1.id,u1.username,u1.name,u.isAppSuperUser,u.isActive,u1.headPicture,u1.email,u1.mobile from " + getSystemBoFullName(SBO.CSPAppUser) +" u join u.user u1 "
        		+ " where u.appId = :appId" ;
        Map<String, Object> params = new HashMap<String, Object>();  
        params.put("appId",AppWorkManager.getCurrentAppId());      
/*        user.setId((Long) userObj.get(SBO.CSPUser + ".id"));
        user.setUserId((Long) userObj.get(SBO.CSPUser + ".userId"));
        user.setUsername((String) userObj.get(SBO.CSPUser + ".username"));
        user.setSuperUser((Boolean) userObj.get(SBO.CSPUser + ".superUser"));
        user.setName((String) userObj.get(SBO.CSPUser + ".name"));
        user.setIsActive((Boolean) userObj.get(SBO.CSPUser + ".isActive"));
        user.setHeadPicture((String) userObj.get(SBO.CSPUser + ".headPicture"));
        user.setUserRoles(new HashSet<UserRole>(findUserRoleByUser(user.getId() + "")));*/
        
        List<Map<String, Object>> results = (List<Map<String, Object>>) getList(cql, SBO.CSPAppUser, params);
        if (results == null || results.size() == 0) {
            return null;
        }
        List<User> users = new ArrayList<User>();
        for (Map<String, Object> userObj : results) {
            User user = new User();
            user.setId((Long) userObj.get("0"));
            user.setUserId((Long) userObj.get("0"));
            user.setUserName((String) userObj.get("1"));
            user.setSuperUser((Boolean) userObj.get("3"));
            user.setName((String) userObj.get("2"));
            user.setActive((Boolean) userObj.get("4"));
            user.setHeadPicture((String) userObj.get("5"));
            user.setUserRoles(new HashSet<UserRole>(findUserRoleByUser(user.getId() + "")));
            if(userObj.get("6")!=null){
                user.setEmail(userObj.get("6").toString());
            }
            if(userObj.get("7")!=null){
                user.setMobile(userObj.get("7").toString());
            }
            users.add(user);
        }
        return users;
    }

    private static User getUserFromMap(Map<String, Object> userObj) {
        User user = new User();
        user.setId((Long) userObj.get(SBO.CSPUser + ".id"));
        user.setUserId((Long) userObj.get(SBO.CSPUser + ".userId"));
        user.setUserName((String) userObj.get(SBO.CSPUser + ".username"));
        user.setSuperUser((Boolean) userObj.get(SBO.CSPUser + ".superUser"));
        user.setName((String) userObj.get(SBO.CSPUser + ".name"));
        user.setActive((Boolean) userObj.get(SBO.CSPUser + ".isActive"));
        user.setHeadPicture((String) userObj.get(SBO.CSPUser + ".headPicture"));
        user.setUserRoles(new HashSet<UserRole>(findUserRoleByUser(user.getId() + "")));
        if(userObj.get(SBO.CSPUser + ".email")!=null){
            user.setEmail(userObj.get(SBO.CSPUser + ".email").toString());
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    public static List<UserRole> findUserRoleByUser(String userId) {
    	
    	//AppWorkManager.getBoDataAccessManager().getDataAuthManagement().getUser(Long.parseLong(userId),session());
    	//String appId = EnterpriseContext.getAppId();
    	//String appTag = appId+"#";
        String cql = "from " + getSystemBoFullName(SBO.CSPUserRole) + " where user=" + userId+" and role in(10010,10011,10012)";
        Map<String, Object> params = new HashMap<String, Object>();
    //    params.put("roles", rolelist);
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPUserRole, params);
        List<UserRole> userRoles = new ArrayList<UserRole>();
        if (roleList != null && roleList.size() > 0) {
            for(Map<String, Object> singleRole:roleList){       	
                UserRole userRole = new UserRole();
                userRole.setId((Long) singleRole.get(SBO.CSPUserRole + ".id"));
                User user = new User();
                Map<String, Object> map = (Map<String, Object>) singleRole.get(SBO.CSPUserRole + ".user");
                user.setId((Long) map.get("id"));
                userRole.setUser(user);       
                map = (Map<String, Object>) singleRole.get(SBO.CSPUserRole + ".role");
                Role role = getRoleById((Long) map.get("id"));       
                userRole.setRole(role);
                userRoles.add(userRole);        
            }
        }
        return userRoles;
    }

    public static UserRoleObject getUserRoleById(Long userRoleId) {
        return dataAuthManagement.getUserRoleById(session(), userRoleId);
    }

    public static List<UserRoleObject> getUserRoleList(Long userRoleId) {
        return dataAuthManagement.getUserRoleList(session());
    }

    public static void createUserRole(UserRole userRole,BoSession session) {
        if (userRole == null || userRole.getUser() == null || userRole.getRole() == null) {
            return;
        }
        dataAuthManagement.createUserRole(session, userRole.getUser().getId(), userRole.getRole().getId());
    }
   
    public static void deleteUserRole(UserRole userRole,BoSession session) {
        if (userRole == null) {
            return;
        } 
        dataAuthManagement.deleteUserRole(session(), userRole.getId());
    }

    public static Long getAppUserId() {
     	String value = AppWorkManager.getPriorSetting().getApplicationValue(US.BOSS_VALUE);
     	if(value==null||StringUtils.isEmpty(value)){
     	      throw new AppException("app.appuser.superuser.miss");
     	}
     	return Long.parseLong(value);
    }
    
    
    public static Role getRoleById(Long Id) {
        String cql = "from " + getSystemBoFullName(SBO.CSPRole) + " where id = '" + Id + "'";
        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> roleList = (List<Map<String, Object>>) getList(cql, SBO.CSPRole, params);
        if (roleList != null && roleList.size() > 0) {
            Role role = new Role();
            role.setId((Long) roleList.get(0).get(SBO.CSPRole + ".id"));
            role.setName((String) roleList.get(0).get(SBO.CSPRole + ".name"));
            return role;
        } else {
            return null;
        }
    }


}