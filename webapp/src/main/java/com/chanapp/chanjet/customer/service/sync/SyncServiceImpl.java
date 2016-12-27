
package com.chanapp.chanjet.customer.service.sync;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.cache.Cache;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.TodoWorkMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.idmove.IDMoveUtil;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

/**
 * @author tds
 *
 */
public class SyncServiceImpl extends BaseServiceImpl implements SyncServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(SyncServiceImpl.class);

    // private static int SELECT_PRVILEGE = Integer.valueOf("001", 2);
    private static int UPDATE_PRVILEGE = Integer.valueOf("100", 2);
    private static int ALL_PRVILEGE = Integer.valueOf("111", 2);

    private static final int QUERY_INTERVAL = 15;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> loadCsvData(String SyncData) {
        Map<String, Object> paramsMap = dataManager.jsonStringToMap(SyncData);
        
        
        SyncData syncArgs = new SyncData();
        Object syncVersion = paramsMap.get("syncVersion");
        if (syncVersion != null && syncVersion instanceof Integer) {
            syncArgs.setSyncVersion(new Long((Integer) syncVersion));
        } else if (syncVersion != null && syncVersion instanceof Long) {
            syncArgs.setSyncVersion((Long) syncVersion);
        }

        List<Map<String, Object>> hierarchyUsers = (List<Map<String, Object>>) paramsMap.get("hierarchyUsers");
        syncArgs.setHierarchyUsers(hierarchyUsers);
        Assert.notNull(syncArgs.getSyncVersion(), "app.sync.syncVersion.required");
    	//检查是否有IDMOVE
		checkIDMoved(syncArgs);
        Map<String, Object> ret = loadCsvData(syncArgs, SyncCache.getSyncCache());
        return ret;
    }
    
	private static void checkIDMoved(SyncData syncData){
		Long movedTs = IDMoveUtil.getInstance().getLastMovedTS();
			//	AppWorkManager.getPriorSetting().getApplicationValue(IdMoveService.ID_MOVEDTS);		
		Long lastSyncTs = syncData.getSyncVersion();
		if(movedTs!=null&&movedTs>lastSyncTs){
			syncData.setSyncVersion(0l);
		}					
	}

    private boolean checkLastSync(SyncData syncArgs, Cache<String, SyncUserInfo> syncCache) {
        try {
            String token = EnterpriseContext.getToken();
            SyncUserInfo syncInfo = syncCache.get(token);
            Long syncVersion = syncArgs.getSyncVersion();
            if (syncArgs.getSyncVersion() == 0)
                return false;
            if (syncInfo == null) {
                syncInfo = new SyncUserInfo();
                syncInfo.setLastSyncVersion(syncVersion);
                syncCache.put(token, syncInfo);
            } else {
                // 如果和上次同步时间相同，判断为手机端上次同步失败，无需验证MD5文件。
                if (syncVersion.equals(syncInfo.getLastSyncVersion())) {
                    return true;
                }
                // 如果小于15秒。MD5文件时间戳比较标准用上次同步时间，减少差异几率
                Long timeDiff = syncVersion - syncInfo.getLastSyncVersion();
                if (timeDiff < QUERY_INTERVAL * 1000 && timeDiff > 0) {
                    syncArgs.setSyncVersion(syncInfo.getLastSyncVersion());
                } else {
                    syncInfo.setLastSyncVersion(syncVersion);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private Boolean chkHierarchyChange(Long syncVersion) {
        if (syncVersion == 0) {
            return true;
        } else {
        	return true;
/*            if (ServiceLocator.getInstance().lookup(UserServiceItf.class).hasHierarchyChange(syncVersion)) {
                return true;
            } else {
                return ServiceLocator.getInstance().lookup(UserServiceItf.class).chkHierarchyChange(syncVersion);
            }*/
        }
    }

    private List<Map<String, Object>> reloadHierarchyUsers() {
        List<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getHierarchyUsers(null);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (UserValue user : users) {
            Map<String, Object> map = new HashMap<String, Object>();
            // 测试注意ID端上是否用到

            map.put("id", user.getId());
            map.put("userId", user.getId());
            map.put("parentId", user.getParentId());
            map.put("userRole", user.getUserRole());
            map.put("userLevel", user.getUserLevel());
            map.put("status", user.getStatus());	
            result.add(map);
        }
        return result;
    }

    private List<Long> getHierarchyOwners(List<Map<String, Object>> oldHierarchy,
            List<Map<String, Object>> newHierarchy) {
        Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        // String oldUserRole = null;
        Long oldUserLevel = null;
        List<Long> oldOwners = new ArrayList<Long>();
        List<Long> oldUserIds = new ArrayList<Long>();
        for (Map<String, Object> oldMap : oldHierarchy) {
            Long userId = Long.valueOf(oldMap.get("userId").toString());
            oldUserIds.add(userId);
            if (currentUserId.equals(oldMap.get("parentId"))) {
                oldOwners.add(userId);
            }
            if (currentUserId.equals(userId)) {
                if (null != oldMap.get("userLevel") && !"".equals(oldMap.get("userLevel"))) {
                    oldUserLevel = Long.valueOf(oldMap.get("userLevel").toString());
                }
                // oldUserRole = (String)oldMap.get("userRole");
            }
        }
        String newUserRole = null;
        Long newUserLevel = null;
        List<Long> newOwners = new ArrayList<Long>();
        List<Long> newUserIds = new ArrayList<Long>();
        for (Map<String, Object> newMap : newHierarchy) {
            Long userId = Long.valueOf(newMap.get("userId").toString());
            newUserIds.add(userId);
            if (currentUserId.equals(newMap.get("parentId"))) {
                newOwners.add(userId);
            }
            if (currentUserId.equals(userId)) {
                if (null != newMap.get("userLevel") && !"".equals(newMap.get("userLevel"))) {
                    newUserLevel = Long.valueOf(newMap.get("userLevel").toString());
                }
                newUserRole = (String) newMap.get("userRole");
            }
        }
        if (null != newUserRole) {
            if (SRU.LEVEL_BOSS.equals(newUserLevel) && !SRU.LEVEL_BOSS.equals(oldUserLevel)) {// 新老板，或者新管理员
                // 除去老下属后所有员工
                newUserIds.removeAll(oldOwners);
                return newUserIds;
            } else if (SRU.LEVEL_BOSS.equals(newUserLevel) && SRU.LEVEL_BOSS.equals(oldUserLevel)) {// 老板和管理员角色没变化
                // 新员工
                newUserIds.removeAll(oldUserIds);
                return newUserIds;
            } else {
                // 除去老下属后新增下属
                newOwners.removeAll(oldOwners);
                return newOwners;
            }
        }
        return null;
    }

    private List<Long> reloadSyncArgs(SyncData syncArgs, SyncData syncData) {
        syncData.setSyncVersion(new Date().getTime());
        Long syncVersion = syncArgs.getSyncVersion();
        Assert.notNull(syncVersion, "app.sync.syncVersion.required");
        List<Map<String, Object>> hierarchyUsers = syncArgs.getHierarchyUsers();
        Assert.notNull(hierarchyUsers, "app.sync.hierarchyUsers.required");
        boolean hierarchyChange = chkHierarchyChange(syncVersion);
        syncData.setHierarchyChange(hierarchyChange);
        List<Long> hierarchyOwners = null;
        if (hierarchyChange) {
            List<Map<String, Object>> newHieratchyUsers = reloadHierarchyUsers();
            syncData.setHierarchyUsers(newHieratchyUsers);
            // 首次如何处理
            if (null != hierarchyUsers && hierarchyUsers.size() > 0) {
                hierarchyOwners = getHierarchyOwners(hierarchyUsers, newHieratchyUsers);
            }
        }
        return hierarchyOwners;
    }

    private Map<String, Object> loadCsvData(SyncData syncArgs, Cache<String, SyncUserInfo> syncCache) {
        Assert.notNull(syncArgs.getSyncVersion(), "app.sync.syncVersion.required");
        // 验证是和上次同步时间戳相同
        boolean sameTs = checkLastSync(syncArgs, syncCache);
        SyncData syncData = new SyncData();
        if (syncArgs.getSyncVersion() != 0) {
            syncArgs.setSyncVersion(syncArgs.getSyncVersion() - QUERY_INTERVAL * 1000);
        }
        Long start = System.currentTimeMillis();
        List<Long> hierarchyOwners = reloadSyncArgs(syncArgs, syncData);
        logger.info("loadUserHierarchy use time = " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();
        SyncQueryDataImpl syncQuery = new SyncQueryDataImpl(syncArgs.getSyncVersion(), hierarchyOwners);
        logger.info("loadAllData use time = " + (System.currentTimeMillis() - start) + " ms");
        List<ISyncEntity> syncEntitys = syncQuery.getSyncEntityList();
        start = System.currentTimeMillis();
        Map<String, Object> result = SyncTransCSV.getCsvResult(syncEntitys);
        logger.info("TranCsvData use time = " + (System.currentTimeMillis() - start) + " ms");
        // 时间戳相同 或首次同步，不用MD5校验直接下发
        if (sameTs || syncArgs.getSyncVersion() == 0) {
            result.put("hierarchyUsers", syncData.getHierarchyUsers());
        }
        // 下发报文,MD5校验
        else {
            result.put("hierarchyUsers", syncData.getHierarchyUsers());
        }
        result.put("hierarchyChange", syncData.getHierarchyChange());
        result.put("syncVersion", syncData.getSyncVersion());
        return result;
    }

    private Map<String, Object> saveCustomer(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .deleteCustomer(Long.valueOf(idObject.toString()));
                result.put("success", true);
            }
        } else {
            boolean mergeFlag = false;
            ICustomerRow customer = null;
            LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) entityMap;
            if (OP.UPDATE.equals(operationType)) {
                // 客户名称重复性检查
            	String name = customerParam.get(CustomerMetaData.name) ==null?null:customerParam.get(CustomerMetaData.name).toString();
            	String phone = customerParam.get(CustomerMetaData.phone) ==null?null:customerParam.get(CustomerMetaData.phone).toString().toString();
                Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .getExistsCustomer(ConvertUtil.toLong(customerParam.get(SC.id).toString()),name,phone);
                if (sameCustomer != null) {
                    Assert.customerRepeat(sameCustomer);
                }
                customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).updateCustomer(customerParam);
            } else if (OP.ADD.equals(operationType)) {
             	String name = customerParam.get(CustomerMetaData.name) ==null?null:customerParam.get(CustomerMetaData.name).toString();
            	String phone = customerParam.get(CustomerMetaData.phone) ==null?null:customerParam.get(CustomerMetaData.phone).toString().toString();
            	// 客户名称重复性检查
                Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .getExistsCustomer(null, name,phone);
                if (sameCustomer != null) {
                    Assert.customerRepeat(sameCustomer);
                }
                Map<String, Object> cusotmerMap = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .addCustomerWithMerge(customerParam);
                customer = (ICustomerRow) cusotmerMap.get("entity");
                mergeFlag = (Boolean) cusotmerMap.get("mergeFlag");
            }
            if (customer != null) {
                Row row = BoRowConvertUtil.toRow(customer);
                Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                boolean hasDel = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                        .checkDeleteDataAuth(CustomerMetaData.EOName, customer.getId(), currentUserId);
                if (hasDel) {
                    row.put("privilege", ALL_PRVILEGE);
                } else {
                    row.put("privilege", UPDATE_PRVILEGE);
                }
                result.put("mergeFlag", mergeFlag);
                result.put("entity", row);
            }
        }
        return result;
    }

    private Map<String, Object> saveContact(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                        .deleteContact(Long.valueOf(idObject.toString()));
                result.put("success", true);
            }
        } else {
            boolean mergeFlag = false;
            IContactRow contact = null;
            LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) entityMap;
            if (OP.UPDATE.equals(operationType)) {
                contact = ServiceLocator.getInstance().lookup(ContactServiceItf.class).upsertContact(contactParam);
            } else if (OP.ADD.equals(operationType)) {
                Map<String, Object> contactMap = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                        .addContactWithMerge(contactParam);
                contact = (IContactRow) contactMap.get("entity");
                mergeFlag = (Boolean) contactMap.get("mergeFlag");
            }
            if (contact != null) {
                Row row = BoRowConvertUtil.toRow(contact);
                Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                boolean hasDel = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                        .checkDeleteDataAuth(ContactMetaData.EOName, contact.getId(), currentUserId);
                if (hasDel) {
                    row.put("privilege", ALL_PRVILEGE);
                } else {
                    row.put("privilege", UPDATE_PRVILEGE);
                }
                result.put("mergeFlag", mergeFlag);
                result.put("entity", row);
            }
        }
        return result;
    }

    /**
     * 
     * 保存工作记录
     */

    private Map<String, Object> saveWorkRecord(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        boolean hasDelPri = true;
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                        .deleteWorkRecord(Long.valueOf(idObject.toString()));
                result.put("success", true);
            }
        } else {
            Row row = null;
            if (entityMap.get(WorkRecordMetaData.customer) != null) {
                String customerId = entityMap.get(WorkRecordMetaData.customer).toString();
                if (StringUtils.isEmpty(customerId)) {
                    entityMap.remove(WorkRecordMetaData.customer);
                }
            }
            String workRecordValue = dataManager.toJSONString(entityMap);
            // 工作记录修改
            if (OP.UPDATE.equals(operationType)) {
                Row tempRow = new Row();
                IWorkRecordRow record = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                        .updateWorkRecord(workRecordValue, tempRow);
                row = BoRowConvertUtil.toRow(record);
                if (tempRow.get("attachments") != null) {
                    row.put("attachments", tempRow.get("attachments"));
                } else {
                    row.put("attachments", new ArrayList<Object>());
                }

                Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                hasDelPri = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                        .checkDeleteDataAuth(WorkRecordMetaData.EOName, record.getId(), currentUserId);
                // 工作记录新增
            } else if (OP.ADD.equals(operationType)) {
                row = new Row();
                ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).addWorkRecord(
                        (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(workRecordValue), row);
            }
            if (row != null) {
                if (hasDelPri) {
                    row.put("privilege", ALL_PRVILEGE);
                } else {
                    row.put("privilege", UPDATE_PRVILEGE);
                }
                result.put("entity", row);
            }
        }
        return result;
    }

    /**
     * 保存评论
     * 
     * @param operationType
     * @param entityMap
     * @return @
     */
    private Map<String, Object> saveComment(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                        .deleteComment(Long.valueOf(idObject.toString()));
                result.put("success", true);
            }
        } else {
            Row row = null;
            if (OP.ADD.equals(operationType)) {
                row = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                        .addComment((LinkedHashMap<String, Object>) entityMap);
            }
            result.put("entity", row);
        }
        return result;
    }

    /**
     * 保存待办
     * 
     * @param operationType
     * @param entityMap
     * @return @
     */
    private Map<String, Object> saveTodoWork(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class)
                        .deleteTodoWork(Long.valueOf(idObject.toString()));
                result.put("success", true);
            }
        } else {
            Row todoWork = null;
            LinkedHashMap<String, Object> todoWorkParam = (LinkedHashMap<String, Object>) entityMap;
            if (OP.UPDATE.equals(operationType)) {
                todoWork = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).updateTodoWork(todoWorkParam);
            } else if (OP.ADD.equals(operationType)) {
                todoWork = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).addTodoWork(todoWorkParam);
            }

            if (todoWork != null) {
                Row row = todoWork;
                Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                boolean hasDel = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkDeleteDataAuth(
                        TodoWorkMetaData.EOName, ConvertUtil.toLong(todoWork.get("id").toString()), currentUserId);
                if (hasDel) {
                    row.put("privilege", ALL_PRVILEGE);
                } else {
                    row.put("privilege", UPDATE_PRVILEGE);
                }
                result.put("entity", row);
            }
        }
        return result;
    }

    /**
     * 保存签到
     * 
     * @param operationType
     * @param entityMap
     * @return @
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> saveCheckin(String operationType, Map<String, Object> entityMap) {
        Map<String, Object> result = new HashMap<String, Object>();
        logger.info("operationType=" + operationType);
        if (OP.DELETE.equals(operationType)) {
            Object idObject = entityMap.get("id");
            if (idObject != null) {
                logger.info("saveCheckin  delete id =" + idObject.toString());
                ServiceLocator.getInstance().lookup(CheckinServiceItf.class)
                        .deleteCheckin(Long.valueOf(idObject.toString()));
                result.put("success", true);
            } else {
                logger.info("saveCheckin  delete id is null!!!");
            }
        } else {
            Row checkin = null;
            LinkedHashMap<String, Object> checkinParam = (LinkedHashMap<String, Object>) entityMap;
            if (OP.UPDATE.equals(operationType)) {
                checkin = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).updateCheckin(checkinParam);
            } else if (OP.ADD.equals(operationType)) {
                checkin = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).addCheckin(checkinParam);
                Long customerId = checkinParam.containsKey("customerId")
                        ? ConvertUtil.toLong(checkinParam.get("customerId").toString()) : null;
                String coordinateNote =   checkinParam.get("coordinateNote")==null ? null:  checkinParam.get("coordinateNote").toString();    
                if (customerId != null && customerId > 0) {
                    ServiceLocator.getInstance().lookup(CustomerServiceItf.class).updateCustomerCoordinate(customerId,
                            (Map<String, Double>) checkinParam.get("coordinate"),
                            coordinateNote);
                }
            }
            if (checkin != null) {
                Long currentUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                boolean hasDel = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                        .checkDeleteDataAuth(TodoWorkMetaData.EOName, (Long) checkin.get(SC.id), currentUserId);
                if (hasDel) {
                    checkin.put("privilege", ALL_PRVILEGE);
                } else {
                    checkin.put("privilege", UPDATE_PRVILEGE);
                }
                result.put("entity", checkin);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> syncSave(String SyncData) {
        logger.info("syncSave start");
        Map<String, Object> map = dataManager.jsonStringToMap(SyncData);
        logger.info("syncSave para toObject over");
        Map<String, Object> result = new HashMap<String, Object>();
        if (map != null) {
            String entityType = (String) map.get("entityType");
            String operationType = (String) map.get("operationType");
            Map<String, Object> entityMap = (Map<String, Object>) map.get("entity");
            if (EO.Customer.equalsIgnoreCase(entityType)) {
                result = saveCustomer(operationType, entityMap);
            } else if (EO.Contact.equalsIgnoreCase(entityType)) {
                result = saveContact(operationType, entityMap);
            } else if (EO.WorkRecord.equalsIgnoreCase(entityType)) {
                result = saveWorkRecord(operationType, entityMap);
            } else if (EO.Comment.equalsIgnoreCase(entityType)) {
                result = saveComment(operationType, entityMap);
            } else if (EO.TodoWork.equalsIgnoreCase(entityType)) {
                result = saveTodoWork(operationType, entityMap);
            } else if (EO.Checkin.equalsIgnoreCase(entityType)) {
                result = saveCheckin(operationType, entityMap);
            } else {
                Object[] paraMap = new Object[1];
                paraMap[0] = entityType;
                throw new AppException("app.sync.entityType.error", paraMap);
            }
        }
        return result;
    }

}
