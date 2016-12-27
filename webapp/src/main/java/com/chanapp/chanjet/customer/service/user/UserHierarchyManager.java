package com.chanapp.chanjet.customer.service.user;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.role.RoleServiceItf;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.customer.vo.system.UserRole;
import com.chanapp.chanjet.web.context.AppContext;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.dataauth.api.UserAffiliate;

public class UserHierarchyManager {
	private AppUser appUser;
	private UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
	private User user ;
	private BoSession session = AppContext.session();
	private String NOCHANGE = "noChange";
	private Long userId ;
	private final static String SALESMANTOMANAGER = "salesmanToManager";
	private final static String MANAGERTOSALESMAN = "managerTosalesman";
	private final static String BOSSTOSALESMAN = "bossTosalesman";
	private PrivilegeServiceItf privilegeService =  ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
	  
	public UserHierarchyManager(Long userId,BoSession session){
		this.userId = userId;
		this.appUser = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());		
	    this.user = EnterpriseUtil.getUserById(userId);
	    this.session = session;
	}
	
	private String checkAppUserChange(boolean origisSuperUser){
		if(!appUser.getIsAppSuperUser().equals(origisSuperUser)){
			//业务员变应用管理员
			if(origisSuperUser == false){
				if(!userService.isBoss(userId)&&!isManager(userId)){
					return SALESMANTOMANAGER;
				}
			
			}//应用管理员变业务员
			else{
				if(userService.isBoss(userId))
					return BOSSTOSALESMAN;
				if(isManager(userId))
					return MANAGERTOSALESMAN;
			}			
		}
		return NOCHANGE;
	}
	
	public void salesmanToManager(){
        // 删除老角色
        for (UserRole role : user.getUserRoles()) {
            EnterpriseUtil.deleteUserRole(role,session);
        }
        //删除上下级关系
        List<Long> allSubs = privilegeService.removeSubsById(userId,session);
        privilegeService.removeBossById(userId,session);      
        //下属移交给BOSS
        for(Long subId:allSubs){
        	privilegeService.addBoss(subId,userService.getSuperAppUserId(),session);
        }	
	}
	
	public void magangerToSalesman(){
        RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
        roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, user,session);
        privilegeService.addBoss(userId, userService.getSuperAppUserId(),session);
	}
	
	public void bossToSalesman(){
		//获取所有的应用管理员
		List<AppUser> appUsers = EnterpriseUtil.findSuperAppUserByAppId(AppWorkManager.getCurrentAppId());
		if(appUsers!=null&&appUsers.size()>0){
			AppUser newBoss = appUsers.get(0);
			privilegeService.changeBoss(newBoss.getUser().getId(),session);
		}
	}
	
	public void handerAppUserChange(boolean origisSuperUser){
		String changeAction = checkAppUserChange(origisSuperUser);
		switch (changeAction) {
		case SALESMANTOMANAGER:
			salesmanToManager();
			break;
		case BOSSTOSALESMAN:
			bossToSalesman();
			break;
		case MANAGERTOSALESMAN:
			magangerToSalesman();
			break;	
		}
	}
			
	private boolean isManager(Long userId){
		UserAffiliate user = AppWorkManager.getDataAuthManager().getUserAffiliate();
		List<Map<String, Object>> bossList =user.getDirectBoss(session, userId, null);
		if(bossList==null||bossList.size()==0)
			return true;
		return false;
	}
}
