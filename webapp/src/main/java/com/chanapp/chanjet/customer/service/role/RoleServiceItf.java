package com.chanapp.chanjet.customer.service.role;

import java.util.List;

import com.chanapp.chanjet.customer.vo.system.Role;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.web.service.BaseServiceItf;
import com.chanjet.csp.bo.api.BoSession;

public interface RoleServiceItf extends BaseServiceItf {

	String createUserRoleByName(String roleName, User user,BoSession session);

    List<Role> getUserRole(User user);

    void removeEntityPrivilege(String appId, String entityName, Long userId);

    void deleteUserRoleByName(String roleName, User user,BoSession session);
    
    void initUserRole(BoSession session);

}
