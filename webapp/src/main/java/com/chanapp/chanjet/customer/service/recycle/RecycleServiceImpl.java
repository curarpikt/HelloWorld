package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRowSet;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRow;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRowSet;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleHome;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleRow;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleRowSet;
import com.chanapp.chanjet.customer.businessobject.api.recyclerelation.IRecycleRelationHome;
import com.chanapp.chanjet.customer.businessobject.api.recyclerelation.IRecycleRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclerelation.IRecycleRelationRowSet;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.RC;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.RecycleRelationMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.recover.RecoverServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.MobilePhone;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class RecycleServiceImpl extends BoBaseServiceImpl<IRecycleHome, IRecycleRow, IRecycleRowSet>
        implements RecycleServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(RecycleServiceImpl.class);

    @Override
    public void addRecoveryByBatchDel(String entityName, List<Long> customerIds, String reason) {
        Assert.notNull(customerIds, "app.common.params.invalid");
        Assert.notNull(entityName, "app.common.params.invalid");
        _addRecoveryByBatchDel(entityName, customerIds, reason);
    }

    private void _addRecoveryByBatchDel(String entityName, List<Long> customerIds, String reason) {
        BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> recycleRelationService = ServiceLocator
                .getInstance().lookup(BO.RecycleRelation);
        IRecycleRelationHome recycleRelationHome = (IRecycleRelationHome) recycleRelationService
                .getBusinessObjectHome();
        Map<Long, Object> delContacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .getDelNumByCustomerId(customerIds);
        Map<Long, Object> delWorkRecords = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .getDelNumByCustomerId(customerIds);

        Long operUser = EnterpriseContext.getCurrentUser().getUserLongId();
        Timestamp operTime = new Timestamp(new Date().getTime());
        IRecycleRow recycleRow = saveRecycleRow(operUser, operTime, reason, CustomerMetaData.EOName, null);
        upsert(recycleRow);
        Long recycleId = recycleRow.getId();
        IRecycleRelationRowSet recycleRelationRowSet = (IRecycleRelationRowSet) recycleRelationService.createRowSet();
        for (Long id : customerIds) {
            Map<String, Object> children = new HashMap<String, Object>();
            children.put("contactNum", delContacts.get(id));
            children.put("workRecordNum", delWorkRecords.get(id));
            IRecycleRelationRow recycleRelationRow = (IRecycleRelationRow) recycleRelationService.createRow();
            recycleRelationRow.setEntityId(id);
            recycleRelationRow.setEntityName(entityName);
            recycleRelationRow.setContent(dataManager.toJSONString(children));
            recycleRelationRow.setRecycle(recycleId);
            recycleRelationRow.setStatus(1l);
            recycleRelationRowSet.addRow(recycleRelationRow);
        }
        recycleRelationHome.batchInsert(recycleRelationRowSet, false);

    }

    @Override
    public IRecycleRow saveRecycleRow(Long operUser, Timestamp operTime, String reason, String entityName,
            List<IRecycleRow> recycleList) {
        IRecycleRow recycleRow = createRow();
        recycleRow.setOperUser(operUser);
        recycleRow.setOperTime(operTime);
        recycleRow.setReason(reason);
        recycleRow.setEntityName(entityName);
        recycleRow.setStatus(1l);
        if (recycleList != null) {
            recycleList.add(recycleRow);
        }
        //System.out.println("saveRecycleRow entityName = " + entityName);
        return recycleRow;
    }

    @Override
    public void saveRecycleRelation(Long entityId, String entityName, IRecycleRow recycleRow, String content,
            Map<String, List<Long>> recyledIdList) {
        if (recyledIdList != null && recyledIdList.get(entityName) != null
                && recyledIdList.get(entityName).contains(entityId)) {
            return;
        }
        IRecycleRelationRow recycleRelationRow = (IRecycleRelationRow) recycleRow
                .createChildRow(RecycleRelationMetaData.EOName);
        recycleRelationRow.setEntityId(entityId);
        recycleRelationRow.setEntityName(entityName);
        recycleRelationRow.setContent(content);
        recycleRelationRow.setStatus(1l);
    }

    @Override
    public void addRecovery(String entityName, Long id) {
        Assert.notNull(id, "app.common.params.invalid");
        Assert.notNull(entityName, "app.common.params.invalid");

        ServiceLocator.getInstance().lookup(RecoverServiceItf.class).addRecovery(entityName, id);
    }

    private Long checkLong(Object o) {
        if (o == null) {
            throw new AppException("app.common.para.format.error");
        }
        try {
            Long r = Long.parseLong(o.toString());
            return r;
        } catch (Exception e) {
            throw new AppException("app.common.para.format.error");
        }
    }

    @Override
    public int delRecycles(String ids) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        HashMap<String, Object> para = new HashMap<String, Object>();
        String[] idArr = ids.split(",");
        List<Long> idList = new ArrayList<Long>();
        for (int i = 0; i < idArr.length; i++) {
            idList.add(checkLong(idArr[i].toString()));
        }
        para.put("ids", idList);
        para.put("status", RC.Status_CLEAN);
        updateRecycleRelation(para);
        return updateRecycle(para);

    }

    @SuppressWarnings("unchecked")
    private int updateRecycleRelation(HashMap<String, Object> para) {
        Long status = (Long) para.get("status");
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        // 只更新是new状态的
        criteria.eq("status", RC.Status_NEW);
        List<Long> ids = (List<Long>) para.get("ids");
        if (ids != null && ids.size() > 0) {
            criteria.in("recycle", ids.toArray());
        }
        List<Long> entityIds = (List<Long>) para.get("entityIds");
        if (entityIds != null && entityIds.size() > 0) {
            criteria.in("entityId", entityIds.toArray());
        }
        jsonQueryBuilder.addCriteria(criteria);

        int r = 0;
        IRecycleRelationRowSet relations = (IRecycleRelationRowSet) ServiceLocator.getInstance()
                .lookup(BO.RecycleRelation).queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (IRecycleRelationRow relation : relations.getRecycleRelationRows()) {
            relation.setStatus(status);
            ServiceLocator.getInstance().lookup(BO.RecycleRelation).upsert(relation);
            r++;
        }

        // int r =
        // ServiceLocator.getInstance().lookup(BO.RecycleRelation).batchUpdate(jsonQueryBuilder.toJsonQuerySpec(),
        // new String[] { "status" }, new Object[] { status });
        return r;
    }

    private Integer updateRecycle(HashMap<String, Object> para) {
        Long status = (Long) para.get("status");
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        // 只更新是new状态的
        criteria.eq("status", RC.Status_NEW);
        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) para.get("ids");
        if (ids != null && ids.size() > 0) {
            criteria.in(SC.id, ids.toArray());
        }
        jsonQueryBuilder.addCriteria(criteria);
        int r = batchUpdate(jsonQueryBuilder.toJsonQuerySpec(), new String[] { "status" }, new Object[] { status });
        return r;
    }

    @Override
    public int cleanRecycle() {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_CLEAN);
        updateRecycleRelation(para);
        return updateRecycle(para);
    }

    private List<Long> queryOperUserIds(HashMap<String, Object> para) {
        String cqlQueryString = " select distinct u.id as uid from " + this.getBusinessObjectId() + " c "
                + " left join c.operUser u " + " where  1=1 ";
        if (para.get("entityName") != null) {
            cqlQueryString = cqlQueryString + "and c.entityName = :entityName ";
        }
        if (para.get("status") != null) {
            cqlQueryString = cqlQueryString + "and c.status = :status ";
        }
        List<Map<String, Object>> opers = runCQLQuery(cqlQueryString, para);
        List<Long> operUserIds = new ArrayList<Long>();
        for (int i = 0; i < opers.size(); i++) {
            operUserIds.add((Long) opers.get(i).get("uid"));
        }
        return operUserIds;
    }

    @Override
    public List<UserValue> getOperUsers(String entityName) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        if (entityName == null || (!entityName.equals(CustomerMetaData.EOName) && !entityName.equals(EO.Contact)
                && !entityName.equals(EO.WorkRecord))) {
            throw new AppException("app.common.para.invalid");
        }

        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("entityName", entityName);
        para.put("status", RC.Status_NEW);
        //para.put("isDeleted", false);
        List<Long> ids = queryOperUserIds(para);
        ids.add(60003828757l);
        if (ids == null || ids.size() < 1) {
            return new ArrayList<UserValue>();
        }
        UserQuery query = new UserQuery();

        query.setUserIds(ids);
        VORowSet<UserValue> rowSet = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
        List<UserValue> userList = rowSet.getItems();
        return userList;
    }

    private List<Map<String, Object>> getRecycleRelationIds(Long recycleId) {
        String cqlQueryString = "select rr.entityId as entityId from " + this.getBusinessObjectId(BO.RecycleRelation)
                + " rr left join rr.recycle r" + " where" + " (rr.isDeleted is null or rr.isDeleted = :isDeleted) "
                + " and rr.status = :status " + " and r.id = :recycleId";
        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_NEW);
        para.put("recycleId", recycleId);
        para.put("isDeleted", false);
        List<Map<String, Object>> infos = runCQLQuery(cqlQueryString, para);
        return infos == null ? new ArrayList<Map<String, Object>>() : infos;
    }

    private int restoreDo(Long recycleId, String entityName, List<Long> entityIdList) {
        // RecoveryService. entityName entityIdList
        ServiceLocator.getInstance().lookup(RecoverServiceItf.class).reCoverEntity(entityName, entityIdList);

        HashMap<String, Object> para = new HashMap<String, Object>();
        List<Long> idList = new ArrayList<Long>();
        idList.add(recycleId);
        para.put("ids", idList);
        para.put("status", RC.Status_RESRORE);

        // 更新回收站关系表
        para.put("entityIds", entityIdList);
        updateRecycleRelation(para);

        List<Map<String, Object>> entityIdsDb = getRecycleRelationIds(recycleId);
        int r = 0;
        if (entityIdsDb == null || entityIdsDb.size() < 1) {
            // 更新回收站
            r = updateRecycle(para);
        }

        return r;
    }

    @Override
    public int restore(Long recycleId, String entityIds) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(SC.id, recycleId);
        jsonQueryBuilder.addCriteria(criteria);

        IRecycleRowSet recycleRowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        if (recycleRowSet == null || recycleRowSet.size() < 1) {
            return 0;
        }

        IRecycleRow row = recycleRowSet.getRow(0);
        String entityName = row.getEntityName();

        List<Long> entityIdList = new ArrayList<Long>();
        List<Map<String, Object>> entityIdsDb = getRecycleRelationIds(recycleId);
        for (int i = 0; i < entityIdsDb.size(); i++) {
            entityIdList.add(Long.parseLong(entityIdsDb.get(i).get("entityId").toString()));// 数据库里的entityId
        }

        if (entityIds != null && !entityIds.equals("")) {// 如果前端传了entityId
            String[] entityIdArr = entityIds.split(",");
            List<Long> entityIdPara = new ArrayList<Long>();
            for (int i = 0; i < entityIdArr.length; i++) {
                entityIdPara.add(checkLong(entityIdArr[i]));
            }
            if (entityIdPara.size() > 0) {
                entityIdList.retainAll(entityIdPara);// entityIdList=取数据库里与传参的交集entityId
            }
        }

        if (entityIdList == null || entityIdList.size() < 1) {
            return 0;
        }

        List<Long> _entityIdList = new ArrayList<Long>();
        int r = 0;
        for (int i = 0; i < entityIdList.size(); i++) {
            _entityIdList.add(entityIdList.get(i));
            if (i % 500 == 499) {
                r = r + restoreDo(recycleId, entityName, _entityIdList);
                _entityIdList = new ArrayList<Long>();
            }
        }
        if (_entityIdList.size() > 0) {
            r = r + restoreDo(recycleId, entityName, _entityIdList);
        }

        return r;
    }

    @SuppressWarnings("unchecked")
    private Criteria getCriteria(HashMap<String, Object> para) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        if (para.get("entityName") != null) {
            criteria.eq("entityName", para.get("entityName").toString());
        }
        if (para.get("status") != null) {
            criteria.eq("status", Long.parseLong(para.get("status").toString()));
        }
        if (para.get("operUserIds") != null && ((List<Long>) para.get("operUserIds")).size() > 0) {
            criteria.in("operUser", ((List<Long>) para.get("operUserIds")).toArray());
        }
        if (para.get("startTime") != null) {
            criteria.ge("operTime", Long.parseLong(para.get("startTime").toString()));
        }
        if (para.get("endTime") != null) {
            criteria.le("operTime", Long.parseLong(para.get("endTime").toString()));
        }

        return criteria;
    }

    private IRecycleRowSet _getRecycles(HashMap<String, Object> para) {
        Integer pageno = (Integer) para.get("pageno");
        Integer pagesize = (Integer) para.get("pagesize");

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(getCriteria(para));
        jsonQueryBuilder.addOrderDesc("operTime");
        jsonQueryBuilder.setFirstResult((pageno - 1) * pagesize);
        jsonQueryBuilder.setMaxResult(pagesize);

        IRecycleRowSet rowSet = query(jsonQueryBuilder.toJsonQuerySpec());
        return rowSet;
    }

    private Integer getRecyclesCount(HashMap<String, Object> para) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(getCriteria(para));

        Integer total = getRowCount(jsonQueryBuilder.toJsonQuerySpec());
        return total;
    }

    private List<Map<String, Object>> getRecycleRelationCustomer(Long recycleId) {
        String cqlQueryString = "select rr.id as id,rr.entityId as entityId,c.name as name,rr.content as content,rr.entityName as entityName from "
                + this.getBusinessObjectId(BO.RecycleRelation) + " rr left join rr.recycle r" + ","
                + this.getBusinessObjectId(BO.Customer) + " c" + " where rr.entityId=c.id"
                + " and (rr.isDeleted is null or rr.isDeleted = :isDeleted) " + " and rr.status = :status "
                + " and r.id = :recycleId" + " order by rr.id";
        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_NEW);
        para.put("recycleId", recycleId);
        para.put("isDeleted", false);
        List<Map<String, Object>> infos = runCQLQuery(cqlQueryString, para);
        return infos == null ? new ArrayList<Map<String, Object>>() : infos;
    }

    private List<Map<String, Object>> getRecycleRelationContact(Long recycleId) {
        String cqlQueryString = "select rr.id as id,rr.entityId as entityId,con.name as name,con.mobile as mobile,rr.content as content,rr.entityName as entityName,c.name as cname from "
                + this.getBusinessObjectId(BO.RecycleRelation) + " rr left join rr.recycle r" + ","
                + this.getBusinessObjectId(BO.Contact) + " con left join con.customer c" + " where rr.entityId=con.id"
                + " and (rr.isDeleted is null or rr.isDeleted = :isDeleted) " + " and rr.status = :status "
                + " and r.id = :recycleId" + " order by rr.id";
        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_NEW);
        para.put("recycleId", recycleId);
        para.put("isDeleted", false);
        List<Map<String, Object>> infos = runCQLQuery(cqlQueryString, para);
        return infos == null ? new ArrayList<Map<String, Object>>() : infos;
    }

    private List<Map<String, Object>> getRecycleRelationWorkRecord(Long recycleId) {
        String cqlQueryString = "select rr.id as id,rr.entityId as entityId,w.content as content,rr.content as rcontent,rr.entityName as entityName,c.name as name from "
                + this.getBusinessObjectId(BO.RecycleRelation) + "  rr left join rr.recycle r" + ","
                + this.getBusinessObjectId(BO.WorkRecord) + " w left join w.customer c" + " where rr.entityId=w.id"
                + " and (rr.isDeleted is null or rr.isDeleted = :isDeleted) " + " and rr.status = :status "
                + " and r.id = :recycleId" + " order by rr.id";
        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_NEW);
        para.put("recycleId", recycleId);
        para.put("isDeleted", false);
        List<Map<String, Object>> infos = runCQLQuery(cqlQueryString, para);

        if (infos != null && infos.size() > 0) {
            for (int i = 0; i < infos.size(); i++) {
                Map<String, Object> os = infos.get(i);
                String content = os.get("content") == null ? "" : os.get("content").toString();
                os.put("content", content);
            }
        }
        return infos == null ? new ArrayList<Map<String, Object>>() : infos;
    }

    private void genRelationCustomer(List<Map<String, Object>> relationInfos, List<Map<String, Object>> relationRows) {
        for (int i = 0; i < relationInfos.size(); i++) {
            // rr.id as id,rr.entityId as entityId,c.name as name,rr.content as
            // content,rr.entityName as entityName
            Map<String, Object> relationInfo = relationInfos.get(i);
            Map<String, Object> relationRow = new HashMap<String, Object>();
            Long relationId = (Long) relationInfo.get("id");
            Long entityId = (Long) relationInfo.get("entityId");
            String customerName = (String) relationInfo.get("name");
            String content = (String) relationInfo.get("content");
            String entityName = (String) relationInfo.get("entityName");
            relationRow.put("relationId", relationId);
            relationRow.put("entityId", entityId);
            relationRow.put("customerName", customerName);
            relationRow.put("content", content);
            relationRow.put("entityName", entityName);
            relationRows.add(relationRow);
        }
    }

    private void genRelationContact(List<Map<String, Object>> relationInfos, List<Map<String, Object>> relationRows) {
        for (int i = 0; i < relationInfos.size(); i++) {
            // rr.id as id,rr.entityId as entityId,con.name as name,con.mobile
            // as mobile,rr.content as content,rr.entityName as
            // entityName,c.name as cname
            Map<String, Object> relationInfo = relationInfos.get(i);
            Map<String, Object> relationRow = new HashMap<String, Object>();
            Long relationId = (Long) relationInfo.get("id");
            Long entityId = (Long) relationInfo.get("entityId");
            String contactName = (String) relationInfo.get("name");
            String mobile = "";
            MobilePhone mobilePhone = (MobilePhone) relationInfo.get("mobile");
            if (mobilePhone != null) {
                mobile = mobilePhone.getPhoneNumber();
            }
            String content = (String) relationInfo.get("content");
            String entityName = (String) relationInfo.get("entityName");
            String refCustomerName = (String) relationInfo.get("cname");
            relationRow.put("relationId", relationId);
            relationRow.put("entityId", entityId);
            relationRow.put("contactName", contactName);
            relationRow.put("mobile", mobile);
            relationRow.put("content", content);
            relationRow.put("entityName", entityName);
            relationRow.put("refCustomerName", refCustomerName);
            relationRows.add(relationRow);
        }
    }

    private void genRelationWorkRecord(List<Map<String, Object>> relationInfos,
            List<Map<String, Object>> relationRows) {
        for (int i = 0; i < relationInfos.size(); i++) {
            // rr.id as id,rr.entityId as entityId,w.content as
            // content,rr.content as rcontent,rr.entityName as entityName,c.name
            // as name
            Map<String, Object> relationInfo = relationInfos.get(i);
            Map<String, Object> relationRow = new HashMap<String, Object>();
            Long relationId = (Long) relationInfo.get("id");
            Long entityId = (Long) relationInfo.get("entityId");
            String workContent = (String) relationInfo.get("content");
            String content = (String) relationInfo.get("rcontent");
            String entityName = (String) relationInfo.get("entityName");
            String refCustomerName = (String) relationInfo.get("name");

            relationRow.put("relationId", relationId);
            relationRow.put("entityId", entityId);
            relationRow.put("workContent", workContent);
            relationRow.put("content", content);
            relationRow.put("entityName", entityName);
            relationRow.put("refCustomerName", refCustomerName);
            relationRows.add(relationRow);
        }
    }

    private RowSet getRecycles(HashMap<String, Object> para) {
        try {

            RowSet rs = new RowSet();

            IRecycleRowSet rowSet = _getRecycles(para);
            Integer total = getRecyclesCount(para);

            List<IRecycleRow> recycleRows = rowSet.getRecycleRows();

            List<Row> rows = new ArrayList<>();
            for (IRecycleRow recycleRow : recycleRows) {
                Row row = new Row();
                row.put("recycle", recycleRow);
                Long recycleId = recycleRow.getId();
                String entityName = recycleRow.getEntityName();
                List<Map<String, Object>> relationInfos = null;
                List<Map<String, Object>> relationRows = new ArrayList<Map<String, Object>>();
                if (EO.Customer.equals(entityName)) {
                    relationInfos = getRecycleRelationCustomer(recycleId);
                    genRelationCustomer(relationInfos, relationRows);
                }
                if (EO.Contact.equals(entityName)) {
                    relationInfos = getRecycleRelationContact(recycleId);
                    genRelationContact(relationInfos, relationRows);
                }
                if (EO.WorkRecord.equals(entityName)) {
                    relationInfos = getRecycleRelationWorkRecord(recycleId);
                    genRelationWorkRecord(relationInfos, relationRows);
                }
                row.put("operSize", relationRows.size());
                row.put("recycleRelations", relationRows);
                rows.add(row);
            }

            rs.setItems(rows);
            rs.setTotal(total);

            return rs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RowSet getRecycles(String entityName, String operUserIds, Long startTime, Long endTime, Integer pageno,
            Integer pagesize) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        RowSet rowSet = new RowSet();

        Assert.largePage(pagesize, "app.common.pagesize.toolarge");
        if (entityName == null || (!entityName.equals(CustomerMetaData.EOName) && !entityName.equals(EO.Contact)
                && !entityName.equals(EO.WorkRecord))) {
            throw new AppException("app.common.para.invalid");
        }
        HashMap<String, Object> para = new HashMap<String, Object>();
        para.put("status", RC.Status_NEW);
        para.put("entityName", entityName);
        if (operUserIds != null && !operUserIds.equals("")) {
            List<Long> opers = new ArrayList<Long>();
            String[] operArr = operUserIds.split(",");
            for (int i = 0; i < operArr.length; i++) {
                opers.add(checkLong(operArr[i]));
            }
            para.put("operUserIds", opers);
        }
        if (startTime != null && startTime > 0) {
            checkLong(startTime);
            para.put("startTime", startTime);
        }
        if (endTime != null && endTime > 0) {
            checkLong(endTime);
            para.put("endTime", endTime);
        }
        if (pageno != null && pageno > 0) {
            para.put("pageno", pageno);
        } else {
            para.put("pageno", 1);
        }
        if (pagesize > 0) {
            para.put("pagesize", pagesize);
        } else {
            para.put("pagesize", 20);
        }

        rowSet = getRecycles(para);

        return rowSet;
    }

    @Override
    public Map<String, Object> hisRecycleInfo() {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        Map<String, Object> result = new HashMap<String, Object>();
        String key = "CUSTOMER_HISDELDATA_INRECYCLE";
        String upgraded = AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                .getApplicationValue(key);
        if (upgraded == null || upgraded.equals("failed")) {
            ICustomerRowSet customerList = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .getCustomerDeleted();
            if (customerList != null && customerList.size() > 0) {
                result.put("needRecycle", "true");
                return result;
            }
            IContactRowSet contactList = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .getContactDeleted();
            if (contactList != null && contactList.size() > 0) {
                result.put("needRecycle", "true");
                return result;
            }
            IWorkRecordRowSet workrecordList = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                    .getWorkRecordDeleted();
            if (workrecordList != null && workrecordList.size() > 0) {
                result.put("needRecycle", "true");
                return result;
            }
            result.put("needRecycle", "false");
        } else {
            result.put("needRecycle", "false");
        }
        return result;
    }

    private Map<String, List<Long>> getRecEntityMap() {
        Map<String, List<Long>> retMap = new HashMap<String, List<Long>>();
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance().addFields(RecycleRelationMetaData.entityName,
                RecycleRelationMetaData.entityId);
        IRecycleRelationRowSet rowSet = (IRecycleRelationRowSet) ServiceLocator.getInstance().lookup(BO.RecycleRelation)
                .query(jsonQueryBuilder.toJsonQuerySpec());
        for (IRecycleRelationRow row : rowSet.getRecycleRelationRows()) {
            String entityName = row.getEntityName();
            Long entityId = row.getEntityId();
            if (retMap.containsKey(entityName)) {
                retMap.get(entityName).add(entityId);
            } else {
                List<Long> idList = new ArrayList<Long>();
                idList.add(entityId);
                retMap.put(entityName, idList);
            }
        }
        return retMap;
    }

    @SuppressWarnings("unchecked")
    private void historyDelDataInRecycle() {
        // 历史删除的数据（包括客户、联系人、工作记录）放入回收站 的key
        String key = "CUSTOMER_HISDELDATA_INRECYCLE";

        Map<String, List<Long>> recyledIdList = getRecEntityMap();
        try {
            List<IRecycleRow> recycleList = new ArrayList<IRecycleRow>();
            String upgraded = AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                    .getApplicationValue(key);

            // System.out.println(key + " upgraded = " + upgraded);
            logger.info(key + " upgraded={}", upgraded);
            if (upgraded != null && upgraded.equals("start")) {
                Long start = System.currentTimeMillis();
                ICustomerRowSet customerList = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .getCustomerDeleted();
                List<Long> customerIds = new ArrayList<Long>();
                if (customerList != null) {
                    int num = customerList.size();
                    for (int i = 0; i < num; i++) {
                        ICustomerRow customerRow = customerList.getRow(i);
                        customerIds.add(customerRow.getId());
                    }
                }

                IOperationLogRowSet operationLogList = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class)
                        .getHistoryMultiDelete();
                IContactRowSet contactList = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                        .getContactDeleted();
                Map<Long, Object> delContacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                        .getDelNumByCustomerId(customerIds);
                IWorkRecordRowSet workrecordList = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                        .getWorkRecordDeleted();
                Map<Long, Object> delWorkRecords = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                        .getDelNumByCustomerId(customerIds);
                logger.info("hisInRecycle query time = {} ", (System.currentTimeMillis() - start));
                // session.beginTransaction();
                Long time1 = System.currentTimeMillis();
                if (operationLogList != null) {
                    int num = operationLogList.size();
                    logger.info("hisInRecycle operationLog size = {} ", num);

                    for (int i = 0; i < num; i++) {
                        IOperationLogRow operationLogRow = operationLogList.getRow(i);
                        String content = operationLogRow.getContent();
                        Map<String, Object> contentObject = dataManager.jsonStringToMap(content);
                        String reason = contentObject.get("reason").toString();
                        Long createdBy = operationLogRow.getCreatedBy();
                        Timestamp operatetime = operationLogRow.getCreatedDate();
                        // multiCustomerInRecycle
                        IRecycleRow recycleRow = saveRecycleRow(createdBy, operatetime, reason, EO.Customer,
                                recycleList);
                        List<Long> ids = (List<Long>) contentObject.get("ids");
                        List<ICustomerRow> customerRows = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                                .getDeletedCustomerByIds(customerList, ids);
                        for (ICustomerRow customerRow : customerRows) {
                            Long customerId = customerRow.getId();
                            Map<String, Object> children = new HashMap<String, Object>();
                            children.put("contactNum", delContacts.get(customerId));
                            children.put("workRecordNum", delWorkRecords.get(customerId));
                            // multiCustomerInRecycleRelation
                            saveRecycleRelation(customerId, EO.Customer, recycleRow, dataManager.toJSONString(children),
                                    recyledIdList);
                        }
                    }
                }
                Long time2 = System.currentTimeMillis();
                logger.info("hisInRecycle multiCustomer time = {} ", (time2 - time1));
                if (customerList != null) {
                    int num = customerList.size();
                    logger.info("hisInRecycle customer size = {} ", num);
                    for (int i = 0; i < num; i++) {
                        ICustomerRow customerRow = customerList.getRow(i);
                        // CustomerInRecycle
                        Long operUser = customerRow.getLastModifiedBy();
                        Timestamp operTime = customerRow.getLastModifiedDate();
                        IRecycleRow recycleRow = saveRecycleRow(operUser, operTime, null, EO.Customer, recycleList);
                        // CustomerInRecycleRelation
                        Long customerId = customerRow.getId();
                        Map<String, Object> children = new HashMap<String, Object>();
                        children.put("contactNum", delContacts.get(customerId));
                        children.put("workRecordNum", delWorkRecords.get(customerId));
                        saveRecycleRelation(customerId, EO.Customer, recycleRow, dataManager.toJSONString(children),
                                recyledIdList);
                    }
                }
                Long time3 = System.currentTimeMillis();
                logger.info("hisInRecycle Customer time = {} ", (time3 - time2));
                if (contactList != null) {
                    int num = contactList.size();
                    logger.info("hisInRecycle contact size = {} ", num);

                    for (int i = 0; i < num; i++) {
                        IContactRow contactRow = contactList.getRow(i);
                        // ContactInRecycle
                        Long operUser = contactRow.getLastModifiedBy();
                        Timestamp operTime = contactRow.getLastModifiedDate();
                        IRecycleRow recycleRow = saveRecycleRow(operUser, operTime, null, EO.Contact, recycleList);
                        // ContactInRecycleRelation

                        Long contentId = contactRow.getId();
                        saveRecycleRelation(contentId, EO.Contact, recycleRow, null, recyledIdList);
                    }
                }
                Long time4 = System.currentTimeMillis();
                logger.info("hisInRecycle Contact time = {} ", (time4 - time3));
                if (workrecordList != null) {
                    int num = workrecordList.size();
                    logger.info("hisInRecycle workrecord size = {} ", num);
                    for (int i = 0; i < num; i++) {
                        IWorkRecordRow workRecordRow = workrecordList.getRow(i);
                        // WorkRecordInRecycle
                        Long operUser = workRecordRow.getLastModifiedBy();
                        Timestamp operTime = workRecordRow.getLastModifiedDate();
                        IRecycleRow recycleRow = saveRecycleRow(operUser, operTime, null, EO.WorkRecord, recycleList);
                        // WorkRecordInRecycleRelation

                        Long workRecordId = workRecordRow.getId();
                        saveRecycleRelation(workRecordId, EO.WorkRecord, recycleRow, null, recyledIdList);
                    }
                }
                Long time5 = System.currentTimeMillis();
                logger.info("hisInRecycle WorkRecord time = {} ", (time5 - time4));
                List<IRecycleRow> insertRows = new ArrayList<IRecycleRow>();
                for (IRecycleRow row : recycleList) {
                    IBusinessObjectRowSet childSet = row.getChildRowSet(RecycleRelationMetaData.EOName);
                    if (childSet != null && childSet.getRows() != null && childSet.getRows().size() > 0) {
                        insertRows.add(row);
                    }
                }
                IRecycleRowSet recycleSet = this.createRowSet();
                for (IRecycleRow insertRow : insertRows) {
                    recycleSet.addRow(insertRow);
                }
                batchInsert(recycleSet);
                for (IRecycleRow row : recycleSet.getRecycleRows()) {
                    IBusinessObjectRowSet childSet = row.getChildRowSet(RecycleRelationMetaData.EOName);
                    Long fkId = row.getId();
                    for (IBusinessObjectRow childRow : childSet.getRows()) {
                        childRow.setFieldValue(RecycleRelationMetaData.Recycle, fkId);
                    }
                    ServiceLocator.getInstance().lookup(BO.RecycleRelation).batchInsert(childSet);

                }
                // 修复成功后，设置标识

                AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                        .setApplicationValue(key, "done");
                // session.getTransaction().commit();
                logger.info("historyDelDataInRecycle ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                    .setApplicationValue(key, "failed");
            logger.error("historyDelDataInRecycle fail", e);
        }
    }

    @Override
    public Map<String, Object> recHisReocrds(String tag) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        Map<String, Object> result = new HashMap<String, Object>();
        String key = "CUSTOMER_HISDELDATA_INRECYCLE";
        String upgraded = AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                .getApplicationValue(key);
        if ("start".equalsIgnoreCase(tag)) {
            if (upgraded == null || upgraded.equals("failed")) {
                AppWorkManager.getDataAuthManager().getPriorSetting(AppWorkManager.getCurrentAppId())
                        .setApplicationValue(key, "start");
            } else {
                throw new AppException("app.recycle.para.status.error");
            }
        } else if ("run".equalsIgnoreCase(tag)) {
            historyDelDataInRecycle();
        } else {
            throw new AppException("app.common.para.format.error");
        }

        result.put("result", "true");
        return result;
    }
}
