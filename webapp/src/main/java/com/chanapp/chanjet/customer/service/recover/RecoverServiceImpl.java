package com.chanapp.chanjet.customer.service.recover;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclerelation.IRecycleRelationRow;
import com.chanapp.chanjet.customer.businessobject.api.recyclerelation.IRecycleRelationRowSet;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.ShortIdUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class RecoverServiceImpl extends BaseServiceImpl implements RecoverServiceItf {
    @Override
    public void recoveryEntity(String entityName, List<Long> ids) {
        Criteria criteria = Criteria.AND();
        criteria.eq(SC.isDeleted, true);
        criteria.in(SC.id, ids.toArray());
        String json = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        ServiceLocator.getInstance().lookup(entityName).batchSetIsDeleted(json, false);
    }

    @Override
    public void preRec(String entityName, List<Long> ids) {
        switch (entityName) {
            case EO.Customer:
                preRec_Customer(ids);
                break;
            case EO.Contact:
                preRec_Contact(ids);
                break;
            case EO.WorkRecord:
                preRec_WorkRecord(ids);
                break;
        }
    }

    @Override
    public void postRec(String entityName, List<Long> ids) {
        switch (entityName) {
            case EO.Customer:
                postRec_Customer(ids);
                break;
            case EO.Contact:
                postRec_Contact(ids);
                break;
            case EO.WorkRecord:
                postRec_WorkRecord(ids);
                break;
        }
    }

    @Override
    public void addRecovery(String entityName, Long id) {
    	if(EO.Customer.equals(entityName)){
    		addRecovery_Customer(entityName,id);
    	}else{
            Long operUser = EnterpriseContext.getCurrentUser().getUserLongId();
            Timestamp operTime = new Timestamp(new Date().getTime());
            RecycleServiceItf recycleService = ServiceLocator.getInstance().lookup(RecycleServiceItf.class);
            IRecycleRow recycleRow = recycleService.saveRecycleRow(operUser, operTime, null, entityName, null);
            recycleService.saveRecycleRelation(id, entityName, recycleRow, null, null);
            recycleService.upsert(recycleRow);
    	}
    }

    public void postRec_Contact(List<Long> ids) {
        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        for (Long id : ids) {
            operationLogService.generate(id, EO.Contact, OP.UPDATE);
        }
    }

    public void preRec_Contact(List<Long> ids) {
        ContactServiceItf contactService = ServiceLocator.getInstance().lookup(ContactServiceItf.class);
        StringBuilder _ids = new StringBuilder();
        for (Long id : ids) {
            if (_ids.length() > 0) {
                _ids.append(",");
            }
            _ids.append(id);
        }
        Criteria criteria = Criteria.AND();
        criteria.in(SC.id, ids.toArray());
        JsonQueryBuilder queryBuilder = JsonQueryBuilder.getInstance().addCriteria(criteria);
        //JsonQuery jq = JsonQuery.getInstance().setCriteriaStr("id in(" + ids.toString() + ")");
        IContactRowSet contactSet = contactService.queryAll(queryBuilder.toJsonQuerySpec(), true);

        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        for (IContactRow contactRow : contactSet.getContactRows()) {
            Long customerId = contactRow.getCustomer();
            if (customerId > 0) {
                Long id = customerId;
                IBusinessObjectRow customer = customerService.findByIdWithAuth(id);
                if (customer == null) {
                    // app.sharecustomer.customer.deleted,21004,该客户已经被删除
                    throw new AppException("app.sharecustomer.customer.deleted");
                }
            }
        }
    }

    public void preRec_Customer(List<Long> ids) {
        // do nothing
    }

    public void postRec_Customer(List<Long> ids) {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        String operTag = ShortIdUtil.generateShortUuid();
        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        operationLogService.grantLogbatch(ids, userId, operTag, OP.GRANT);
    }

    public void addRecoveryByBatchDel_Customer(String entityName, List<Long> customerIds, String reason) {
        RecycleServiceItf recycleService = ServiceLocator.getInstance().lookup(RecycleServiceItf.class);
        BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> recycleRelationService = ServiceLocator
                .getInstance().lookup(BO.RecycleRelation);
        Map<Long, Object> delContacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .getDelNumByCustomerId(customerIds);
        Map<Long, Object> delWorkRecords = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .getDelNumByCustomerId(customerIds);

        Long operUser = EnterpriseContext.getCurrentUser().getUserLongId();
        Timestamp operTime = new Timestamp(new Date().getTime());
        IRecycleRow recycleRow = recycleService.saveRecycleRow(operUser, operTime, reason, CustomerMetaData.EOName,
                null);
        recycleService.upsert(recycleRow);
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
        recycleRelationService.batchInsert(recycleRelationRowSet, false);
    }

    public void addRecovery_Customer(String entityName, Long id) {
        List<Long> customerIds = new ArrayList<Long>();
        customerIds.add(id);
        Map<Long, Object> delContacts = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .getDelNumByCustomerId(customerIds);
        Map<Long, Object> delWorkRecords = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .getDelNumByCustomerId(customerIds);
        RecycleServiceItf recycleService = ServiceLocator.getInstance().lookup(RecycleServiceItf.class);
        Long operUser = EnterpriseContext.getCurrentUser().getUserLongId();
        Timestamp operTime = new Timestamp(new Date().getTime());
        IRecycleRow recycleRow = recycleService.saveRecycleRow(operUser, operTime, null, CustomerMetaData.EOName, null);
        Map<String, Object> children = new HashMap<String, Object>();
        children.put("contactNum", delContacts.get(id));
        children.put("workRecordNum", delWorkRecords.get(id));
        recycleService.saveRecycleRelation(id, entityName, recycleRow, dataManager.toJSONString(children), null);
        recycleService.upsert(recycleRow);

    }

    public void preRec_WorkRecord(List<Long> ids) {
        WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
        StringBuilder _ids = new StringBuilder();
        for (Long id : ids) {
            if (_ids.length() > 0) {
                _ids.append(",");
            }
            _ids.append(id);
        }
        //JsonQuery jq = JsonQuery.getInstance().setCriteriaStr("id in(" + ids.toString() + ")");
        Criteria criteria = Criteria.AND();
        criteria.in(SC.id, ids.toArray());
        JsonQueryBuilder queryBuilder = JsonQueryBuilder.getInstance().addCriteria(criteria);
        IWorkRecordRowSet workRecordSet = workRecordService.queryAll(queryBuilder.toJsonQuerySpec(), true);
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        for (IWorkRecordRow workRecordRow : workRecordSet.getWorkRecordRows()) {
            Long customerId = workRecordRow.getCustomer();
            if (customerId != null && customerId > 0) {
                Long id = customerId;
                IBusinessObjectRow customer = customerService.findByIdWithAuth(id);
                if (customer == null) {
                    // app.sharecustomer.customer.deleted,21004,该客户已经被删除
                    throw new AppException("app.sharecustomer.customer.deleted");
                }
            }
        }
    }

    public void postRec_WorkRecord(List<Long> ids) {
        WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
        StringBuilder _ids = new StringBuilder();
        for (Long id : ids) {
            if (_ids.length() > 0) {
                _ids.append(",");
            }
            _ids.append(id);
        }
        Criteria criteria = Criteria.AND();
        criteria.in(SC.id, ids.toArray());
        JsonQueryBuilder queryBuilder = JsonQueryBuilder.getInstance().addCriteria(criteria);
        IWorkRecordRowSet workRecordSet = workRecordService.queryAll(queryBuilder.toJsonQuerySpec(), true);
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        PrivilegeServiceItf privilegeService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        AttachmentServiceItf attachmentService = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);
        for (IWorkRecordRow workRecordRow : workRecordSet.getWorkRecordRows()) {
            List<String> fileDirs = new ArrayList<String>();
            operationLogService.grantLog(workRecordRow.getId(), workRecordRow.getOwner());
            IAttachmentRowSet attSet = attachmentService.findAttachmentByRelate(WorkRecordMetaData.EOName,
                    workRecordRow.getId(), true);
            List<IAttachmentRow> attachements = attSet.getAttachmentRows();
            for (IAttachmentRow attRow : attachements) {
                String fileDir = attRow.getFileDir();
                if (fileDirs != null && fileDirs.contains(fileDir)) {
                    continue;
                } else {
                    fileDirs.add(fileDir);
                    attachmentService.recoverAttachmentById(attRow.getId());
                }
            }

            if (workRecordRow.getCustomer() != null) {
                // 工作记录onwer有权限才能回写客户
                boolean hasPri = privilegeService.checkUpdateDataAuth(CustomerMetaData.EOName,
                        workRecordRow.getCustomer(), workRecordRow.getOwner());
                if (hasPri) {
                    ICustomerRow customerRow = customerService.findByIdWithAuth(workRecordRow.getCustomer());
                    customerService.updateCustomerStatus(customerRow, workRecordRow, false);
                }

            }
        }
    }

    @Override
    public void reCoverEntity(String entityName, List<Long> ids) {
        Assert.notNull(ids, "app.common.params.invalid");
        Assert.notNull(entityName, "app.common.params.invalid");

        preRec(entityName, ids);
        recoveryEntity(entityName, ids);
        postRec(entityName, ids);
    }

}
