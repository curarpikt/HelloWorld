package com.chanapp.chanjet.customer.service.user;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.service.BaseServiceItf;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.ccs.api.cia.UserInfo;

public interface UserServiceItf extends BaseServiceItf {

    Long getSuperAppUserId();

    /**
     * <p>
     * 初始化用户
     * </p>
     * 
     * @return
     *
     * @author : lf
     * @date : 2016年3月18日
     */
    Map<String, Object> initUser(BoSession session);

    VORowSet<UserValue> queryUserWithCustomerCount(Integer pageNo, Integer pageCount, String status);

    boolean isBoss(Long userId);

    List<UserValue> getAllEnableUse(Long excludeUserId);

    /* IBusinessObjectRowSet getUsersByIds(List<Long> ids); */

    /* List<UserValue> getAllEnableUserValue(); */

    Boolean isOrgBoss(UserInfo userInfo);

    void authBoss();

    /**
     * 获取上下级关系。（不包含停用用户）
     * 
     * @param userId
     * @return
     */
    List<UserValue> getHierarchyUsers(Long userId);

    UserValue getUserValueByUserId(Long userId);

    List<UserValue> getHierarchyUsers(Long userId, boolean containsDisable);

    List<UserValue> getHierarchyUsers(Long userId, boolean containsDisable, boolean managerHander);

    UserInfo initOtherUser(UserInfo userInfo,BoSession session);

    /* Map<String, String> disableUser(Long userId); */

    Map<String, Object> modify(String name, String headPic);

    Map<String, Object> switchOrganization(Long orgId);

    Map<String, Object> getAtList(String keyWord);

    Map<String, Object> belongOrganization();

    Map<String, Object> uploadHeadPicture(String name, File headPic);

    VORowSet<UserValue> getUsersByParam(UserQuery query);

    List<UserValue> getUserGroups();

    UserValue getOrgBoss();

    void logOut();
    // SYSRELUSER迁移

    List<Long> getAllEnableSubordinate(Long userId);

    List<Long> findUserIds(String status, Long syncVersion);

    /* UserValue getOrgBossUserValue(); */

    List<Long> getUserIdByName(String name);

    boolean chkHierarchyChange(Long syncVersion);

    boolean hasHierarchyChange(Long syncVersion);

    Map<String, Object> getHierarchyUsers2Tree(String monthStart, String monthEnd, String bizType);

	String getUserRoleName(Long userId);

	void initBossSettingValue();

}
