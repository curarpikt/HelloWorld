package com.chanapp.chanjet.customer.eventhandler;

import com.chanapp.chanjet.customer.eventhandler.handler.SysHandlerRegister;
import com.chanapp.chanjet.customer.eventhandler.handler.SystemEntityHandlerItf;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.json.JSONObject;
import com.chanjet.csp.event.SystemCloudServiceEventHandler;

public class CustomCloudServiceEventHandler extends SystemCloudServiceEventHandler {


	@Override
	protected void handleUpdate(BoSession session, String entityId, JSONObject entity) {
		SystemEntityHandlerItf handler = SysHandlerRegister.getSystemHandler(entityId);
		if(handler!=null){
			handler.handleUpdate(session, entityId, entity);
		}
	}
	
/*	 private void handerAppUserChange(BoSession session, String entityId,Long userId,boolean origFlag){	
			AppUser appUser = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());
			 PrivilegeServiceItf privilegeService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
			if(!appUser.getIsAppSuperUser().equals(origFlag)){	
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
	                List<Long> allSubs = privilegeService.removeSubsById(userId);
	                privilegeService.removeBossById(userId);      
	                //下属移交给BOSS
	                for(Long subId:allSubs){
	                	privilegeService.addBoss(subId,appUserId);
	                }
				}
				//应用管理员变成非应用管理员
				else{
					//取消了BOSS的应用管理员
					if(userService.isAppSuperUser(userId)){
						List<AppUser> appUsers = EnterpriseUtil.findAppUserByAppId(AppWorkManager.getCurrentAppId());
						if(appUsers!=null&&appUsers.size()>0){
							AppUser newBoss = appUsers.get(0);
							privilegeService.changeBoss(newBoss.getUser().getId());
						}
					}
					//取消了副总的应用管理员
					else{
			            // 绑定业务员角色
			            RoleServiceItf roleService = ServiceLocator.getInstance().lookup(RoleServiceItf.class);
			            roleService.createUserRoleByName(ROLE.SYSRELUSER_ROLE_SALESMAN, user);
			            privilegeService.addBoss(userId, appUserId);
					}
					
				}
			}
		
	    }*/
}
