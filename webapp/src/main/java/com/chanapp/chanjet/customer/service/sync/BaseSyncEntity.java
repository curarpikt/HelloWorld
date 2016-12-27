package com.chanapp.chanjet.customer.service.sync;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.chanapp.chanjet.customer.cache.CspEntityRestObject;
import com.chanapp.chanjet.customer.cache.CustomerMetaData;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.constant.SQ;
import com.chanapp.chanjet.customer.constant.metadata.UserMetaData;
import com.chanapp.chanjet.customer.service.grant.GrantServiceItf;
import com.chanapp.chanjet.customer.service.idmove.IdMoveService;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.ReflectionUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.usertype.EmailAccount;
import com.chanjet.csp.common.base.usertype.GeoPoint;
import com.chanjet.csp.common.base.usertype.MobilePhone;
import com.chanjet.csp.common.base.usertype.Phone;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public abstract class BaseSyncEntity implements ISyncEntity {
    protected List<IBusinessObjectRow> entitySet;
    public Set<String> filedName;
    public static final List<String> sysFields = new ArrayList<String>(Arrays.asList(SC.createdBy, SC.createdDate,
            SC.id, SC.lastModifiedBy, SC.lastModifiedDate, SC.owner, SC.version, SC.ownerDepartment));
    private static final Log logger = LogFactory.getLog(BaseSyncEntity.class);

    public Set<Long> getDeletedData() {
        return this.delIds;
    }

    public List<Long> getAddIds() {
        if (addIds.size() > 0) {
            return new ArrayList<Long>(addIds);
        }
        return new ArrayList<Long>();
    }

    public Set<Long> getDelIds() {
        return delIds;
    }

    public Set<String> getFieldSet() {
        return this.filedName;
    }

    protected Set<Long> addIds = new TreeSet<Long>();
    protected boolean isAll = false;
    protected Set<Long> delIds = new TreeSet<Long>();
    protected Map<Long, List<Long>> noDeleteIds = new HashMap<Long, List<Long>>();
    protected Set<Long> sharedIds = new TreeSet<Long>();

    public List<Long> getSharedIds() {
        if (sharedIds.size() > 0) {
            return new ArrayList<Long>(sharedIds);
        }
        return new ArrayList<Long>();
    }

    public Map<Long, List<Long>> getNoDeletedIds() {
        return this.noDeleteIds;
    }

    protected IBusinessObjectHome boHome;
    protected String entityName;

    public String getEntityName() {
        return this.entityName;
    }

    private List<Long> getEntityIdsBySubUsers(List<Long> hierarchyOwners) {
        List<Long> idList = new ArrayList<Long>();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(SC.owner, hierarchyOwners.toArray());
        // criteria.ne(SysColumns.isdeleted, false);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IBusinessObjectRowSet rowset = ServiceLocator.getInstance().lookup(this.boHome.getDefinition().getName())
                .queryAll(jsonQueryBuilder.toJsonQuerySpec());
        if (rowset != null && rowset.getRows() != null) {
            for (IBusinessObjectRow row : rowset.getRows()) {
                Long objectId = (Long) row.getFieldValue(SC.id);
                idList.add(objectId);
            }
        }
        return idList;
    }

    /**
     * <p>
     * 新增下属的
     * </p>
     * 
     * @param hierarchyOwners
     * @param boHome
     * @return
     * 
     * @author : lf
     * @date : 2015年9月1日
     */
    private List<Long> getSubEntityId(List<Long> hierarchyOwners) {
        List<Long> subObjIds = new ArrayList<Long>();
        String entityName = this.boHome.getDefinition().getPrimaryEO().getName();
        // 新增下属的客户
        List<Long> subCustomerIds = getEntityIdsBySubUsers(hierarchyOwners);
        if (subCustomerIds != null)
            subObjIds.addAll(subCustomerIds);
        // 新增下属参与的客户
        List<Long> subgrantIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                .finGrantObjectIdByUserIds(hierarchyOwners, entityName);
        if (subgrantIds != null)
            subObjIds.addAll(subgrantIds);
        return subObjIds;
    }

    protected List<IBusinessObjectRow> getEntityByIds(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        }
		List<List<Long>> queryIds= new ArrayList<List<Long>>();
		Integer Max=SQ.MAX_QUERY_RESULT;
		Integer index=0;
		for(Long id:ids){
			if(index%Max==0){
				List<Long> tempList = new ArrayList<Long>();
				queryIds.add(tempList);
			}
			List<Long> idList =	queryIds.get(queryIds.size()-1);
			idList.add(id);
			index++;
		}
		List<IBusinessObjectRow> rows = new ArrayList<IBusinessObjectRow>();
		for(List<Long> innerIds:queryIds){
	        Criteria criteria = Criteria.AND();
/*	        if (withDel != true) {
	            criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
	        }*/
	        criteria.in(SC.id, innerIds.toArray());
	        String queryStr = JsonQueryBuilder.getInstance().addCriteria(criteria).addOrderDesc(SC.lastModifiedDate)
	                .toJsonQuerySpec();
	        //保证在500条之内，ID查询
	       IBusinessObjectRowSet rowSet = this.boHome.query(queryStr);
	       rows.addAll(rowSet.getRows());
		}
        return rows;
        //return ServiceLocator.getInstance().lookup(this.boHome.getDefinition().getName()).queryAll(queryStr);
    }

    @Override
    public List<Map<String, Object>> getEntityData() {
        List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        if (this.isAll == false) {
            entitySet = getEntityByIds(new ArrayList<Long>(addIds));
        }
        if (entitySet == null)
            return null;
        List<IBusinessObjectRow> rows = entitySet;
        for (IBusinessObjectRow row : rows) {
            Map<String, Object> dataMap = cloneMap(row.getAllFieldValues());
            retList.add(dataMap);
        }
        ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).setRowPrivilegeField(new ArrayList<Long>(addIds),
                entityName, retList);
        return retList;
    }

    private List<IBusinessObjectRow> getAllWithOutDelete() {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
       // List<Long> ids = new ArrayList<Long>();
        // criteria.ne(SysColumns.isdeleted, false);
        // String queryStr =
        // JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        //jsonQueryBuilder.addFields(SC.id);
        List<IBusinessObjectRow> rows = QueryLimitUtil.queryList(jsonQueryBuilder.toJsonQuerySpec(), this.boHome);
        return rows;
/*        if (rowset != null && rowset.getRows() != null) {
            for (IBusinessObjectRow row : rowset.getRows()) {
                ids.add((Long) row.getFieldValue(SC.id));
            }
        }
        return getEntityByIds(ids, false);*/
    }

    public void setAll() {
        this.isAll = true;
        entitySet = getAllWithOutDelete();
        if (entitySet != null) {
            for (IBusinessObjectRow row : entitySet) {
                Long id = (Long) row.getFieldValue(SC.id);
                this.addIds.add(id);
            }
        }
		//IDMOVE
		IdMoveService idmoveService = new IdMoveService();
		Set<Long> movedIds = idmoveService.getMovedIds(this.entityName);
		if(movedIds!=null&&movedIds.size()>0){
			delIds.addAll(movedIds);
		}
    }

    /**
     * @param simpleName 实体名称
     * @return
     * 
     * @author : lf
     * @date : 2015年10月23日
     */
    protected static TreeSet<String> getFieldsName(String simpleName) {
        TreeSet<String> filedsName = new TreeSet<String>();
        try {
            IEntity metaEntity = AppWorkManager.getAppMetadataManager().getEntityByName(simpleName);
            if (metaEntity == null) {
                throw new AppException("app.attribute.entityName.invalid");
            }
            Map<String, IField> fldMetas = metaEntity.getFields();

            CustomerMetaData customerMetaData = MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
            CspEntityRestObject entity = customerMetaData.getEntites().get(simpleName);
            Map<String, FieldMetaData> fileds = entity.getFields();
            for (String field : fileds.keySet()) {
                FieldMetaData iField = fileds.get(field);
                IField metaField = fldMetas.get(field);
                if (metaField != null) {
                    if (FieldTypeEnum.SHADOW_FOREIGN_KEY.equals(metaField.getType())) {
                        continue;
                    }
                    if (metaField.isSystemField() && !sysFields.contains(field)) {
                        continue;
                    }
                }
                FieldTypeEnum fieldTypeEnum = iField.type;
                // 坐标属性字段特殊处理，拆分成2个字段
                switch (fieldTypeEnum) {
                    case GEOPOINT:
                        filedsName.add(field + ".longitude");
                        filedsName.add(field + ".latitude");
                        break;
                    default:
                        filedsName.add(field);
                        break;
                }
            }
            // 用户加入停用状态属性
            if (UserMetaData.EOName.equals(simpleName)) {
                filedsName.add("status");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filedsName;
    }

    private Map<String, Object> cloneMap(Map<String, Object> Map) {
        Map<String, Object> retMap = new HashMap<String, Object>();

        for (Map.Entry<String, Object> entry : Map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Object realValue = null;

            // 自定字段
            if (value != null && value.getClass().getSimpleName().equals("DynAttrValue")) {
                realValue = ReflectionUtil.getFieldValue(value, "dynAttrValue");
                if (realValue instanceof Timestamp) {
                    Timestamp innertemp = (Timestamp) realValue;
                    Date innerDate = new Date(innertemp.getTime());
                    retMap.put(key, DateUtil.getDateString(innerDate));
                    continue;
                }
            } else {
                realValue = value;
            }

            // 坐标类型
            if (realValue instanceof GeoPoint) {
                GeoPoint temp = (GeoPoint) realValue;
                double latitude = temp.getLatitude();
                double longitude = temp.getLongitude();
                String key1 = key + ".latitude";
                String key2 = key + ".longitude";
                retMap.put(key1, latitude);
                retMap.put(key2, longitude);
                continue;
            } // 枚举类型
            else if (realValue instanceof DynamicEnum) {
                DynamicEnum temp = (DynamicEnum) realValue;
                retMap.put(key, temp.getValue());
                continue;
            } else if (realValue instanceof HashMap) {
                HashMap<?, ?> temp = (HashMap<?, ?>) realValue;
                retMap.put(key, temp.get(SC.id));
                continue;
            }
            // 时间戳
            else if (realValue instanceof Timestamp) {
                Timestamp temp = (Timestamp) realValue;
                retMap.put(key, temp.getTime());
                continue;
            } else if (realValue instanceof Phone) {
                Phone temp = (Phone) realValue;
                retMap.put(key, temp.getPhoneNumber());
                continue;
            } else if (realValue instanceof EmailAccount) {
                EmailAccount temp = (EmailAccount) realValue;
                retMap.put(key, temp.getAccountId());
                continue;
            } else if (realValue instanceof MobilePhone) {
                MobilePhone temp = (MobilePhone) realValue;
                retMap.put(key, temp.getPhoneNumber());
                continue;
            } else if (realValue instanceof Date) {
                Date temp = (Date) realValue;
                retMap.put(key, DateUtil.getDateString(temp));
                continue;
            }
            retMap.put(key, realValue);
        }
        return retMap;
    }

    protected void setIdList(List<Long> hierarchyOwners, final SyncOprationLog log) {
        List<Long> addList = log.getAddIdsByBOName(this.entityName);

        Map<String, List<Long>> delMap = log.getDeleteMap();
        List<Long> delCustomers = delMap.get(entityName);
        if (hierarchyOwners != null && hierarchyOwners.size() > 0) {
            logger.info("hierarchyOwners:" + hierarchyOwners.toString());
            // 新增下属的,包括共享的
            List<Long> subIds = getSubEntityId(hierarchyOwners);
            if (subIds != null) {
                logger.info("getSubEntityId:" + subIds.toString());
                addIds.addAll(subIds);
            }

        }
        // 新增
        if (addList != null && addList.size() > 0)
            addIds.addAll(addList);
        // 去掉新增后又删除的。
        if (addIds != null && delCustomers != null && delCustomers.size() > 0)
            addIds.removeAll(delCustomers);

        // 删除
        if (delCustomers != null)
            delIds.addAll(delCustomers);

    }

    private List<Long> findIdListByTs(Long ts, String orderBy, String orderType) {
        List<Long> idList = new ArrayList<Long>();
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.gt(SC.lastModifiedDate, ts);
        JsonQueryBuilder builder = JsonQueryBuilder.getInstance();
        builder.addCriteria(criteria).addFields(SC.id);
        if (orderType != null) {
            if (orderType.equals("desc")) {
                builder.addOrderDesc(orderBy);
            }
            if (orderType.equals("asc")) {
                builder.addOrderAsc(orderBy);
            }
        }
       List<IBusinessObjectRow> rows = QueryLimitUtil.queryList(builder.toJsonQuerySpec(), this.boHome);       
/*        IBusinessObjectRowSet rowset = ServiceLocator.getInstance().lookup(this.boHome.getDefinition().getName())
                .queryAll(builder.toJsonQuerySpec());*/
        for (IBusinessObjectRow row : rows) {
            Long id = (Long) row.getFieldValue(SC.id);
            idList.add(id);
        }
        return idList;
    }

    protected void addByTs(Long syncVersion, String orderType, String orderField) {
        List<Long> ids = findIdListByTs(syncVersion, orderField, orderType);
        if (ids != null)
            addIds.addAll(ids);
    }

    public abstract void initPara();
}
