package com.chanapp.chanjet.customer.service.role;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.expandauth.CreateUserRoleWithExpandAuth;
import com.chanapp.chanjet.customer.expandauth.DeleteUserRoleWithExpandAuth;
import com.chanapp.chanjet.customer.expandauth.RemovePrivilegeExpandAuth;
import com.chanapp.chanjet.customer.service.sysreluser.SysRelUserServiceItf;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.vo.system.Role;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.dataauth.Assignment;
import com.chanjet.csp.util.ExpandAuthorizationDataAccess;

public class RoleServiceImpl extends BaseServiceImpl implements RoleServiceItf {
    public final static String[] PRIVI_ENTITYS = new String[] { "Customer", "Contact", "WorkRecord", "Attachment",
            "TodoWork", "Checkin" };

    @Override
    public String createUserRoleByName(String roleName, User user,BoSession session) {
        List<Role> roleList = getUserRole(user);
        // 如果用户没有角色，绑定角色
        if (roleList == null || roleList.size() == 0) {
            Role targetRole = EnterpriseUtil.getRoleByName(roleName);
            UserRole userRole = new UserRole();
            userRole.setRole(targetRole);
            userRole.setUser(user);
            CreateUserRoleWithExpandAuth createUserRole = new CreateUserRoleWithExpandAuth();
            Object[] paras = { userRole,session };
            ExpandAuthorizationDataAccess.processData(createUserRole, paras);
            // BoEnterpriseUserRole.createUserRole(session, userRole);
            // TODO 删除历史数据，现在查询有问题
             _removeUserAssignment(user.getUserId());
             return roleName;
        }else{
        	return roleList.get(0).getName();
        }
    }

    @Override
    public void deleteUserRoleByName(String roleName, User user,BoSession session) {
        List<Role> roleList = getUserRole(user);
        // 如果用户没有角色，绑定角色
        if (roleList == null || roleList.size() == 0) {
            Set<UserRole> userRoles = user.getUserRoles();
            for (UserRole userRole : userRoles) {
                if (roleName.equals(userRole.getRole().getName())) {
                    DeleteUserRoleWithExpandAuth deleteUserRole = new DeleteUserRoleWithExpandAuth();
                    Object[] paras = { userRole,session};
                    ExpandAuthorizationDataAccess.processData(deleteUserRole, paras);
                    break;
                }
            }
        }
    }

    @Override
    public List<Role> getUserRole(User user) {
        List<Role> roleList = EnterpriseUtil.getRoleList();
        List<Role> retList = new ArrayList<Role>();
        for (Role role : roleList) {
            if (EnterpriseUtil.hasRole(user, role)) {
                retList.add(role);
            }
        }
        return retList;
    }

    /**
     * 删除老数据中的个人数据权限
     */
    private void _removeUserAssignment(Long userId) {
        // TODO 用userSetting记录，只做一次。
        for (String key : PRIVI_ENTITYS) {
            removeEntityPrivilege(EnterpriseContext.getAppId(), key, userId);
        }
    }

    @Override
    public void removeEntityPrivilege(String appId, String entityName, Long userId) {
        Set<Assignment> assignments = boDataAccessManager.getDataAuthManagement().listAssignments(entityName, userId,
                null, null, session());
        RemovePrivilegeExpandAuth removePrivileges = new RemovePrivilegeExpandAuth();
        Object[] paras = { assignments, session() };
        // TODO 多次删除，效率问题。是否可以提供按USERID删除
        ExpandAuthorizationDataAccess.processData(removePrivileges, paras);
        /*
         * for(Assignment assignment:assignments){
         * boDataAccessManager.getDataAuthManagement().
         * removeAssignment(assignment.getId(), session); }
         */
    }

	@Override
	public void initUserRole(BoSession session) {
		Map<Long,String> roleMap = ServiceLocator.getInstance().lookup(SysRelUserServiceItf.class).getAllUserRoleMap();
		for (Map.Entry<Long, String> entry : roleMap.entrySet()){
			if(ROLE.SYSRELUSER_ROLE_BOSS.equals(entry.getValue()))
				continue;
			User user = EnterpriseUtil.getUserById(entry.getKey());
	        RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
	        roleService.createUserRoleByName(entry.getValue(), user,session);
		}
		// TODO Auto-generated method stub
		
	}
}
