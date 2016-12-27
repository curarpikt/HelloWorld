package com.chanapp.chanjet.customer.service.privilege;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.service.BaseServiceItf;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.json.JSONObject;

public interface PrivilegeServiceItf extends BaseServiceItf {
    boolean checkSelectDataAuth(String entityName, Long objectId, Long userId);

    boolean checkInsertDataAuth(String entityName, Long objectId, Long userId);

    boolean checkUpdateDataAuth(String entityName, Long objectId, Long userId);

    boolean checkDeleteDataAuth(String entityName, Long objectId, Long userId);

    List<Long> checkSelectDataAuthList(String entityName, List<Long> objIds, Long userId);

    List<Long> checkInsertDataAuthList(String entityName, List<Long> objIds, Long userId);

    List<Long> checkUpdateDataAuthList(String entityName, List<Long> objIds, Long userId);

    List<Long> checkDeleteDataAuthList(String entityName, List<Long> objIds, Long userId);

    void setRowPrivilegeField(List<Long> ids, String entityName, List<Map<String, Object>> rows);

    void removeBossById(Long userId,BoSession session);
    
    void addBoss(Long userId, Long bossId,BoSession session);

    List<Long> saveHierarchyUsers(List<UserValue> userValues,BoSession session);

    void changeBoss(Long bossId,BoSession session);

    Map<String, String> diableTransData(Long userId, Long transId,BoSession session);

    Map<String, String> disableUser(Long userId,BoSession session);

    Map<String, Object> transCustomer(Long fromUserId, Long toUserId, Map<String, Object> para);

	//void handerAppUserChange(BoSession session, String entityId, Long userId, boolean origFlag);

	List<Long> removeSubsById(Long userId,BoSession session);
}
