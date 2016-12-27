package com.chanapp.chanjet.customer.service.workrecord;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRow;
import com.chanapp.chanjet.customer.businessobject.api.usersetting.IUserSettingRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordHome;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRowSet;
import com.chanapp.chanjet.customer.constant.BI;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.CT;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.ROLE;
import com.chanapp.chanjet.customer.constant.SQ;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.US;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.customerfollow.CustomerFollowServiceItf;
import com.chanapp.chanjet.customer.service.message.MessageServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.usersetting.UserSettingServiceItf;
import com.chanapp.chanjet.customer.service.workrecordhis.WorkRecordHisServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.PortalUtil;
import com.chanapp.chanjet.customer.util.PushMsg;
import com.chanapp.chanjet.customer.vo.LoadMoreList;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.reader.PropertiesReader;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.BoDataAuthManagement;
import com.chanjet.csp.bo.api.IBusinessObjectManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.dataauth.Grant;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class WorkRecordServiceImpl extends BoBaseServiceImpl<IWorkRecordHome, IWorkRecordRow, IWorkRecordRowSet>
        implements WorkRecordServiceItf {
    private static Logger logger = LoggerFactory.getLogger(WorkRecordServiceImpl.class);

    private static final String HISTORY_CUSTOMER_OWNER = "h_owner";
    private static final String HISTORY_CUSTOMER_SUPERISOR = "h_superisor";
    private static final String HISTORY_CUSTOMER_SHARETO = "h_shareTo";

    private final static String ORDER_ASC = "asc";
    private final static String ORDER_DESC = "desc";

	public static final String NEW = "new";
	public static final String DELETE = "delete";
	public static final String UNCHANGE = "unchange";
    /**
     * 根据客户ID逻辑删除工作记录
     * 
     * @param customerId
     */
    @Override
    public void delByCustomerId(Long customerId) {
/*        // 根据客户ID查询该客户下的未删除的工作记录信息
        String jsonQuerySpec = JsonQuery.getInstance().setCriteriaStr(WorkRecordMetaData.customer + "=" + customerId
                ).toString();

        this.batchUpdate(jsonQuerySpec, new String[] { SC.isDeleted }, new Object[] { Boolean.valueOf(true) });*/
    }

    @Override
    public void preUpsert(IWorkRecordRow row, IWorkRecordRow origRow) {
        ICustomerRow customerRow = null;

        if (row != null && row.getVisitStart() != null) {
            row.setContactTime(row.getVisitStart());
        }
        // 新增
        if (row != null && this.isInsert(row, true)) {
            if (row.getContactTime() == null) {
                row.setContactTime(new Timestamp(new Date().getTime()));
            }
            if(row.getVisitEnd()==null||row.getVisitEnd().getTime()==0){
            	Timestamp contactTime = row.getContactTime();
            	Long oneHouerAfter = contactTime.getTime()+1000*60*60;     
            	Timestamp visitEnd =new Timestamp(oneHouerAfter);            	
            	row.setVisitEnd(visitEnd);           
            	row.setVisitStart(contactTime);
            }
            // 设定一些通用字段
            row.setRecordType("note");
            row.setCommentCount(0L);
            if (row.getRemindTime() != null && row.getRemindTime().equals(new Timestamp(-1L))) {
                row.setRemindTime(null);
            }
            Long customerId = row.getCustomer();
            Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
            boolean hasPri = false;
            if (customerId != null) {
                customerRow = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .findByIdWithAuth(customerId);         
                if (customerRow == null) {
                    Assert.notNull(customerRow, "app.workrecord.customer.notexist");
                }
                row.setCustomerCopy(customerRow.getName());
                if (customerRow.getOwner().equals(currUserId)) {
                    row.setHistoryCustomerRole(HISTORY_CUSTOMER_OWNER);
                } else {
                    hasPri = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                            .checkUpdateDataAuth(BO.Customer, customerId, currUserId);
                    if (hasPri) {
                        row.setHistoryCustomerRole(HISTORY_CUSTOMER_SUPERISOR);
                    } else {
                        row.setHistoryCustomerRole(HISTORY_CUSTOMER_SHARETO);
                    }
                }
            }
        } else {
            Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
            if (origRow != null && !origRow.getOwner().equals(row.getOwner())) {
                throw new AppException("app.privilege.user.invalid.invalidoper");// 传入的ownerId被变了
            }
            if (origRow != null && !origRow.getOwner().equals(userId)) {
                throw new AppException("app.privilege.user.invalid.invalidoper");// 不是自己的工作记录提示无权修改
            }
            Long customerId = row.getCustomer();
            ServiceLocator.getInstance().lookup(WorkRecordHisServiceItf.class).addWorkRecordHis(row.getId());
            boolean allowNullStatus = false;
            customerRow = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .findByIdWithAuth(row.getCustomer());
            if (customerRow != null) {
                // 有客户ID的情况下客户ID不允许修改
                if (origRow.getCustomer() != null && !origRow.getCustomer().equals(customerId)) {
                    throw new AppException("app.workrecord.customer.modifyError");
                }
            }
            // 客户为空或客户状态为空，可以修改客户为空
            if (customerRow == null || customerRow.getStatus() == null) {
                allowNullStatus = true;
            }
            if (allowNullStatus == false && row.getStatus() == null) {
                throw new AppException("app.workRecord.status.required");
            }
        }
    }

    @Override
    public void postUpsert(IWorkRecordRow row, IWorkRecordRow origRow) {
        ICustomerRow customerRow = null;
        boolean isInsert = false;
        if (this.isInsert(row, false)) {
            OperationLogServiceItf operationLogService = ServiceLocator.getInstance()
                    .lookup(OperationLogServiceItf.class);
            operationLogService.writeMsg2BigData(row.getId(), BI.WORKRECORD_ADD, 0);
            isInsert = true;
        }
        // 回写客户状态
        if (row.getCustomer() != null) {
            CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
            customerRow = customerService.findByIdWithAuth(row.getCustomer());
            customerService.updateCustomerStatus(customerRow, row, isInsert);
        }
        // 如果有@的人， 消息推送
        sendWorkRecordMsg(row, customerRow);
        shareByContent(row.getContent(), row.getId());
    }

    private void sendWorkRecordMsg(IWorkRecordRow workRecordRow, ICustomerRow customerRow) {
        List<Long> grantUsers = PushMsg.getPushUserByContent(workRecordRow.getContent());
        if (grantUsers != null && grantUsers.size() > 0) {
            try {
                String json = getWorkRecordMsg(workRecordRow, customerRow);
                if (json != null) {
                    List<Long> userlist = grantUsers;
                    ArrayList<Long> ids = new ArrayList<Long>(userlist);
                    String token = EnterpriseContext.getToken();
                    long userid = EnterpriseContext.getCurrentUser().getUserLongId();
                    StringBuffer text = new StringBuffer();
                    if (null != userlist) {
                        for (Long id : userlist) {
                            text.append(id.toString() + ",");
                        }
                    }
                    logger.info("pushIds={}", text);
                    String from = PushMsg.getFrom(IM.WORK_RECORD_AT);
                    String msgType = PushMsg.getMsgType(from);
                    ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(null, msgType, ids, json);
                    PushMsg.asynPush(from, null, ids, json, userid, token);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getWorkRecordMsg(IWorkRecordRow record, ICustomerRow customerRow) {
        String orgId = PortalUtil.getOrgId();
        String orgName = PortalUtil.getOrgNameById(orgId);
        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        Map<String, Object> Msgrow = new HashMap<String, Object>();
        Map<String, Object> apsrow = new HashMap<String, Object>();
        Map<String, Object> extras = new HashMap<String, Object>();
        Map<String, Object> row = new HashMap<String, Object>();

        String customerName = "";
        if (record.getCustomer() != null) {
            customerName = customerRow.getName();
        }
        if (customerRow != null && customerRow.getStatus() != null) {
            String customerStatus = customerRow.getStatus().getLabel();
            row.put("workrecordStatus", customerStatus);
        } else {
            row.put("workrecordStatus", "");
        }
        String workrecordContent = record.getContent();
        // 语音评论模板
        String alter = EnterpriseContext.getCurrentUser().getName() + "在工作中@了你:"
                + PushMsg.cutString(PushMsg.getAtContent(workrecordContent), 20);
        String type = IM.WORK_RECORD_AT;
        String from = PushMsg.getFrom(type);

        row.put("customerName", customerName);
        row.put("workrecordContent", PushMsg.cutString(PushMsg.getAtContent(workrecordContent), 20));
        row.put("workrecordId", record.getId());
        Map<String, Object> operator = new HashMap<String, Object>();
        operator.put("username", EnterpriseContext.getCurrentUser().getName());
        operator.put("userid", EnterpriseContext.getCurrentUser().getUserLongId());
        operator.put("headpictrue", EnterpriseContext.getCurrentUser().getHeadPicture());
        row.put("operator", operator);
        row.put("type", type);
        row.put("from", from);
        row.put("orgId", orgId);
        row.put("orgName", orgName);
        apsrow.put("alert", alter);
        apsrow.put("sound", "default");
        apsrow.put("badge", 1);

        extras.put("workrecordId", record.getId());
        extras.put("from", from);
        extras.put("type", type);
        extras.put("orgId", orgId);
        extras.put("orgName", orgName);

        Msgrow.put("aps", apsrow);
        Msgrow.put("x", row);
        Msgrow.put("extras", extras);
        return dataManager.toJSONString(Msgrow);
    }

    /**
     * @param content
     * @param workRecordId
     */
    @Override
    public void shareByContent(String content, Long workRecordId) {
        if (content != null) {
            List<Long> grantUsers = PushMsg.getPushUserByContent(content);
            if (grantUsers != null && grantUsers.size() > 0) {
                for (Long userId : grantUsers) {
                    shareWorkRecord(workRecordId, userId);
                }
            }
        }
    }

    /**
     * @author 王安邦
     * @param workRecordId
     * @param shareUserId
     * @throws BaseException
     */
    private void shareWorkRecord(Long workRecordId, Long shareUserId) {
        Assert.notNull(workRecordId, "app.shareworkrecord.para.error");
        Assert.notNull(shareUserId, "app.shareworkrecord.para.error");
        Long granter = EnterpriseContext.getCurrentUser().getUserLongId();
        try {
            BoDataAuthManagement dataAuthMgmt = boDataAccessManager.getDataAuthManagement();
            Collection<Grant> grants = dataAuthMgmt.listEffectiveGrants(AppWorkManager.getCurrentAppId(),
                    WorkRecordMetaData.EOName, shareUserId, DataAuthPrivilege.SELECT, workRecordId, true, session());
            if (grants == null || grants.size() == 0) {
                dataAuthMgmt.createGrant(WorkRecordMetaData.EOName, shareUserId, DataAuthPrivilege.SELECT, workRecordId,
                        granter, session());
                ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).grantLog(workRecordId, shareUserId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.shareworkrecord.share.error");
        }
    }

    /**
     * 检查数据权限并查询数据
     * 
     * @param id 工作记录ID
     * @return
     */
    @Override
    public IBusinessObjectRow findByIdAndCusWithAuth(Long id) {
        checkSelectAuthById(id);

        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(SC.id, id);
        criteria.addChild(Criteria.OR().empty(WorkRecordMetaData.customer).addChild(Criteria.OR()
                .empty(WorkRecordMetaData.customerIsDeleted).eq(WorkRecordMetaData.customerIsDeleted, false)));
        String queryStr = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        IBusinessObjectRowSet rowset = ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery).query(queryStr);
        if (rowset != null && rowset.size() > 0) {
            return rowset.getRow(0);
        }
        return null;
    }

    @Override
    public Map<Long, Object> getDelNumByCustomerId(List<Long> customerIds) {
        Map<Long, Object> result = new HashMap<Long, Object>();
        if (customerIds != null && customerIds.size() > 0) {
            String cqlQueryString = " select count(c.id) as num,c.customer.id as cid from " + this.getBusinessObjectId()
                    + " c ";
            cqlQueryString += " where (c.isDeleted is null or c.isDeleted = :isDeleted) ";
            cqlQueryString += " and c.customer.id in :customerIds group by c.customer.id ";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("isDeleted", false);
            paraMap.put("customerIds", customerIds);
            List<Map<String, Object>> list = runCQLQuery(cqlQueryString, paraMap);
            if (list != null) {
                for (Map<String, Object> obj : list) {
                    result.put((Long) obj.get("cid"), obj.get("num"));
                }
            }
        }
        return result;
    }

    @Override
    public List<CustomerProcess> getCustomerProgress(Long customerId) {
        List<CustomerProcess> customerProgress = new ArrayList<CustomerProcess>();
        IWorkRecordRowSet workRecords = queryByCustomer(customerId, null, 1, Integer.MAX_VALUE, ORDER_ASC);

        DynamicEnum status = null;
        DynamicEnum lastStatus = null;
        for (IWorkRecordRow workRecord : workRecords.getWorkRecordRows()) {
            if (status == null || workRecord.getStatus() != null) {
                status = (DynamicEnum) workRecord.getFieldValue(WorkRecordMetaData.status);
                if (status == null || status.equals(lastStatus)) {
                    continue;
                }
                CustomerProcess customerProcessVo = new CustomerProcess();
                lastStatus = status;
                customerProcessVo.setEnumName(status.getValue());
                customerProcessVo.setEnumLabel(status.getLabel());
                Timestamp contactTime = workRecord.getContactTime();
                customerProcessVo.setDate(contactTime.getTime());
                customerProgress.add(customerProcessVo);
            }
        }
        return customerProgress;
    }

    private JsonQueryBuilder getJsonQueryBuilder(Long customerId, String status) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(WorkRecordMetaData.customer, customerId);
        if (StringUtils.isNotEmpty(status)) {
            criteria.eq(WorkRecordMetaData.status, status);
        }
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);

        return jsonQueryBuilder;
    }

    @Override
    public IWorkRecordRowSet queryByCustomer(Long customerId, String status, int pageNo, int pageSize, String order) {
        JsonQueryBuilder jsonQueryBuilder = getJsonQueryBuilder(customerId, status);
        if (order.equals(ORDER_DESC)) {
            jsonQueryBuilder.addOrderDesc(WorkRecordMetaData.contactTime);
        }
        if (order.equals(ORDER_ASC)) {
            jsonQueryBuilder.addOrderAsc(WorkRecordMetaData.contactTime);
        }

        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    /**
     * 根据客户ID和工作记录的状态，返回满足要求的非逻辑删除的工作记录的件数。
     * 
     * @param customerId 客户ID
     * @param status 工作记录的状态
     * @return count 符合条件的记录数
     */
    private Integer countCustomerWorkRecords(Long customerId, String status) {
        JsonQueryBuilder jsonQueryBuilder = getJsonQueryBuilder(customerId, status);
        return ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .getRowCount(jsonQueryBuilder.toJsonQuerySpec());
    }

    @SuppressWarnings("unchecked")
    @Override
    public IWorkRecordRow addWorkRecord(LinkedHashMap<String, Object> workRecordParam, Row retRow) {
    	Long customer = workRecordParam.get("customer")==null?null:ConvertUtil.toLong(workRecordParam.get("customer").toString());
        if (customer != null && customer > 0) {
            ServiceLocator.getInstance().lookup(CustomerServiceItf.class).findByIdWithAuth(customer);
        }
        String localId = null;
        if(workRecordParam.get("localId")!=null){
        	localId = workRecordParam.get("localId").toString();
        }
        IWorkRecordRow workRecordRow = getRowByLocalId(localId);
        populateBORow(workRecordParam, workRecordRow);

        // 保存工作记录
        upsert(workRecordRow);
        // 保存附件
        IAttachmentRowSet attSet = saveAttachment((List<Map<String, Object>>) workRecordParam.get("attachments"),
                workRecordRow);
        if (retRow != null) {
            Row convertRow = BoRowConvertUtil.toRow(workRecordRow);
            if (attSet != null && attSet.getAttachmentRows().size() > 0) {
                retRow.put("attachments", BoRowConvertUtil.toRowSet(attSet).getItems());
            }
            retRow.putAll(convertRow);
        }
        return workRecordRow;
    }

    private IAttachmentRowSet saveAttachment(List<Map<String, Object>> attachments, IWorkRecordRow workRecordRow) {
        if (attachments == null || attachments.size() == 0)
            return null;
        String entityName = getBusinessObjectHome().getDefinition().getPrimaryEO().getName();
        return ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).save(workRecordRow.getId(), entityName,
                attachments);

    }
    
    private IAttachmentRowSet saveAttachmentForH5(List<Map<String, Object>> attachments, IWorkRecordRow workRecordRow) {
        if (attachments == null || attachments.size() == 0)
            return null;
        AttachmentServiceItf attService =  ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);
        List<Map<String, Object>> addList = new ArrayList<Map<String, Object>>();
        for(Map<String, Object> attMap : attachments){
        	String operate = (String)attMap.get("status");	
			Assert.notNull(operate, "app.attachment.update.statusMiss");	
			if(NEW.equals(operate)){				
				addList.add(attMap);
			}else if(DELETE.equals(operate)){
				Assert.notNull(attMap.get("id"), "app.attachment.update.idMiss");	
				Long attId =Long.parseLong(attMap.get("id").toString());
				attService.delete(attId);
			}
        }
        String entityName = getBusinessObjectHome().getDefinition().getPrimaryEO().getName();
        return ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).save(workRecordRow.getId(), entityName,
        		addList);

    }


    @SuppressWarnings("unchecked")
    @Override
    public LoadMoreList queryWordRecrod(String queryValue, String queryType, Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 0) {
            pageNo = 0;
        }
        if (queryType == null)
            queryType = "";
        LoadMoreList timeLine = new LoadMoreList();
        Assert.largePage(pageSize, "app.common.pagesize.toolarge");
        String[] types = queryType.split(",");
        Set<String> typeSet = new HashSet<String>();
        for (String type : types) {
            typeSet.add(type);
        }

        Map<String, Object> paraMap = null;
        if (queryValue == null || queryValue.equals("")) {
            paraMap = new HashMap<String, Object>();
        } else {
            paraMap = dataManager.jsonStringToMap(queryValue);
        }
        if (typeSet.contains("owner")) {
            Assert.notNull(paraMap.get("ownerId"), "app.customer.queryValue.ownerIdMiss");
        }
        if (typeSet.contains("status")) {
            Assert.notNull(paraMap.get("statusEnum"), "app.customer.queryValue.statusEnumMiss");
        }
        if (typeSet.contains("timeZone")) {
            Assert.notNull(paraMap.get("timeZone"), "app.customer.queryValue.timeTypeMiss");
        }
        IBusinessObjectRowSet workRecordSet = _queryByCondition(typeSet, paraMap, pageNo, pageSize);
        List<IBusinessObjectRow> entites = new ArrayList<IBusinessObjectRow>();
        if (workRecordSet != null) {
            entites = workRecordSet.getRows();
        }
        timeLine.setHasMore(false);
        if (entites.size() == (pageSize + 1)) {
            timeLine.setHasMore(true);
            entites.remove(entites.size() - 1);
        }
        if (entites.size() == 0) {
            return timeLine;
        }
        List<Long> ids = new ArrayList<Long>();
        List<Long> customer_ids = new ArrayList<Long>();

        for (IBusinessObjectRow record : entites) {
            Object idO = record.getFieldValue(SC.id);
            if (idO != null) {
                ids.add((Long) idO);
            }
            Object customerO = record.getFieldValue(WorkRecordMetaData.customer);
            if (null != customerO) {
                HashMap<String, Long> customerM = (HashMap<String, Long>) customerO;
                Long customerId = customerM.get(SC.id);
                if (customerId != null) {
                    customer_ids.add(customerId);
                }
            }
        }

        RowSet rowSet = BoRowConvertUtil.toRowSet(workRecordSet);
        Map<Long, List<IAttachmentRow>> attachMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findWorkRecordAttachments(ids);
        List<Long> customerPriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                .checkSelectDataAuthList(CustomerMetaData.EOName, customer_ids,
                        EnterpriseContext.getCurrentUser().getUserLongId());
        List<Long> followlist = ServiceLocator.getInstance().lookup(CustomerFollowServiceItf.class)
                .findFollowByCustomerIds(customer_ids);
        // List<Long> customerPriv = CustomerService.getAllCutomerIds();
        Map<Long, List<ITodoWorkRow>> todoWorkMap = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class)
                .findTodoWorkByWorkRecordId(ids);
        for (int i = 0; i < rowSet.getItems().size(); i++) {
            Row row = rowSet.getItems().get(i);
            Map<String, Object> customer = (Map<String, Object>) row.get("customer");
            boolean follow = false;
            boolean customerExits = false;
            if (customer != null) {
                Long customerId = (Long) customer.get("id");
                if (followlist.contains(customerId)) {
                    follow = true;
                }
                if (customerPriv.contains(customerId)) {
                    customerExits = true;
                }
            }
            Long commentCount = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                    .countCommentByWorkrecordId(row.getLong("id"));
            List<IAttachmentRow> attachments = attachMap.get(row.getLong("id"));
            if(attachments!=null&&attachments.size()>0){
            	row.put("attachments", BoRowConvertUtil.toRowList(attachments));
            }            
            row.put("follow", follow);
            row.put("customerExits", customerExits);
            row.put("commentCount", commentCount);
            List<ITodoWorkRow> todoWorks = todoWorkMap.get(row.getLong("id"));
            row.put("todoWorks", BoRowConvertUtil.toRowList(todoWorks));
        }
        timeLine.setItems(rowSet.getItems());
        return timeLine;
    }

    private IBusinessObjectRowSet _queryByCondition(Set<String> condtionType, Map<String, Object> queryValue,
            Integer pageNo, Integer pageSize) {
        Criteria criteria = getQueryDao(condtionType, queryValue);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(WorkRecordMetaData.contactTime);
        if (pageNo != null) {
            jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
            jsonQueryBuilder.setMaxResult(pageSize + 1);
        }

        IBusinessObjectRowSet rowSet = ServiceLocator.getInstance().lookup(BO.WorkRecordForList)
                .query(jsonQueryBuilder.toJsonQuerySpec());

        return rowSet;
    }

    private Criteria getQueryDao(Set<String> condtionType, Map<String, Object> paraMap) {
        // 加入删除标记
        if (paraMap == null) {
            throw new AppException("app.common.params.invalid");
        }
        paraMap.put("isDeleted", false);
        condtionType.add("isDeleted");
        // 加入启用用户过滤
        paraMap.put("status", SRU.STATUS_ENABLE);
        condtionType.add("enableUser");
        // 搜索框模糊查询转换
        if (paraMap != null && paraMap.get("searchValue") != null) {
            String searchtext = paraMap.get("searchValue").toString();
            try {
                searchtext = java.net.URLDecoder.decode(searchtext, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            paraMap.put("searchValue", "%" + searchtext + "%");
            condtionType.add("queryName");
        }

/*        List<Long> delIds = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getDelCustomerIds();
        if (delIds != null && delIds.size() > 0) {
            paraMap.put("customerIds", delIds);
            condtionType.add("customerIds");
        }*/
        // 工作记录列表人员权限处理
        if (paraMap.get("ownerId") == null) {
            Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
            UserValue relUser = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                    .getUserValueByUserId(currUserId);
            if (SRU.ROLE_SALESMAN.equals(relUser.getUserRole()) || SRU.ROLE_SUPERISOR.equals(relUser.getUserRole())) {
                List<Long> users = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                        .getAllEnableSubordinate(currUserId);
                condtionType.add("ownerList");
                paraMap.put("ownerIds", users);
            }
        }
        // 穿透查询角色处理
        if (paraMap != null && paraMap.get("userRole") != null) {
            String userRole = paraMap.get("userRole").toString();
            if ("supervisor".equals(userRole)) {
                Assert.notNull(paraMap.get("ownerId"), "app.customer.queryValue.ownerIdMiss");
                String ownerId = paraMap.get("ownerId").toString();
                List<Long> users = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                        .getAllEnableSubordinate(Long.parseLong(ownerId));
                condtionType.add("ownerList");
                condtionType.remove("owner");
                paraMap.put("ownerIds", users);
            }
        }
        // 时间枚举项转换
        if (paraMap != null && paraMap.get("timeZone") != null) {
            String timeZone = paraMap.get("timeZone").toString();
            Date start = null, end = null;
            if (CT.COUNTTYPE_LASTWEEK.equals(timeZone)) {
                start = DateUtil.getLastWeekStart();
                end = DateUtil.getLastWeekEnd();
            } else if (CT.COUNTTYPE_WEEK.equals(timeZone)) {
                start = DateUtil.getCurrentWeekStart();
                end = DateUtil.getCurrentWeekEnd();
            } else if (CT.COUNTTYPE_MONTH.equals(timeZone)) {
                start = DateUtil.getCurrentMonthStart();
                end = DateUtil.getCurrentMonthEnd();
            } else if (CT.LAST_MONTH.equals(timeZone)) {
                start = DateUtil.getLastMonthStart();
                end = DateUtil.getLastMonthEnd();
            } else if (CT.TODAY.equals(timeZone)) {
                start = new Date();
                end = DateUtil.getNowDateTime();
            } else if (CT.YESTODAY.equals(timeZone)) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                start = cal.getTime();
                end = cal.getTime();
            } else if (CT.COUNTTYPE_QUARTER.equals(timeZone)) {
                start = DateUtil.getCurrentQuarterStart();
                end = DateUtil.getCurrentQuarterEnd();
            } else if (CT.COUNTTYPE_YEAR.equals(timeZone)) {
                start = DateUtil.getCurrentYearStart();
                end = DateUtil.getCurrentYearEnd();
            }
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Timestamp startTS = Timestamp.valueOf(df.format(start) + " 00:00:00.001");
            Timestamp endTS = Timestamp.valueOf(df.format(end) + " 23:59:59.999");
            paraMap.put("startDate", startTS);
            paraMap.put("endDate", endTS);
        }
        Criteria criteria = Criteria.AND();
        if (paraMap.get("startTime") != null) {
            criteria.ge(WorkRecordMetaData.contactTime, Long.parseLong(paraMap.get("startTime").toString()));
        }
        if (paraMap.get("endTime") != null) {
            criteria.le(WorkRecordMetaData.contactTime, Long.parseLong(paraMap.get("endTime").toString()));
        }
        for (String type : condtionType) {
            if ("owner".equals(type)) {
                criteria.eq(WorkRecordMetaData.ownerUserId, paraMap.get("ownerId"));

            } else if ("ownerList".equals(type)) {
                criteria.in(WorkRecordMetaData.ownerUserId, ((List<?>) paraMap.get("ownerIds")).toArray());

            } else if ("status".equals(type)) {
                if (paraMap.get("statusEnum") != null && StringUtils.isEmpty(paraMap.get("statusEnum").toString())) {
                    criteria.empty(WorkRecordMetaData.status);
                } else {
                    criteria.eq(WorkRecordMetaData.status, paraMap.get("statusEnum"));
                }
            } else if ("queryName".equals(type)) {
                Object searchValue = paraMap.get("searchValue");
                criteria.addChild(Criteria.OR().like(WorkRecordMetaData.content, searchValue)
                        .like(WorkRecordMetaData.customerName, searchValue));

            } /*else if ("isDeleted".equals(type)) {
                criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, paraMap.get("isDeleted")));

            }*/ else if ("enableUser".equals(type)) {
                criteria.in("ownerUserId", ServiceLocator.getInstance().lookup(UserServiceItf.class)
                        .findUserIds(String.valueOf(paraMap.get("status")), null).toArray());

            } else if ("timeZone".equals(type)) {
                criteria.between(WorkRecordMetaData.contactTime, paraMap.get("startDate"), paraMap.get("endDate"));

            } /*else if ("customerIds".equals(type)) {
                criteria.addChild(
                        Criteria.OR()
                                .addChild(Criteria.NOT().in(WorkRecordMetaData.customer,
                                        ((List<?>) paraMap.get("customerIds")).toArray()))
                        .empty(WorkRecordMetaData.customer));

            }*/
        }

        return criteria;
    }

    @Override
    public void deleteWorkRecord(Long workRecordId) {
        IWorkRecordRow row = query(workRecordId);
        Assert.notNull(row, "app.workrecord.object.notexist");
     //   String entityName = WorkRecordMetaData.EOName;

        // 删除附件
/*        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).deleteAttachmentByRelate(entityName,
                workRecordId);*/
        updateCustomerByDeleteRecord(row);
        // 记录日志
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(row, OP.DELETE);
/*        ServiceLocator.getInstance().lookup(RecycleServiceItf.class).addRecovery(WorkRecordMetaData.EOName,
                workRecordId);*/
        // 逻辑删除
        logicDeleteByIdWithAuth(workRecordId);
        // 回写客户状态和最后一条工作记录


    }

    private void updateCustomerByDeleteRecord(IWorkRecordRow row) {
        Long customerId = row.getCustomer();
        // 没有客户，返回
        if (customerId == null || customerId < 1L)
            return;

        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        ICustomerRow customer = customerService.getCustomerById(customerId);
        if (customer == null)
            return;
        Long lastRecordId = customer.getLastRecord();
        // 客户没有最后的工作记录，直接返回
        if (lastRecordId == null)
            return;
        // 删除的工作记录是客户的最新一条工作记录
        if (lastRecordId.equals(row.getId())) {
            IWorkRecordRowSet recordSet = getWorkRecordByCustomerId(customerId);
            List<IWorkRecordRow> records = recordSet.getWorkRecordRows();
            boolean isSetStatus = false;
            for (IWorkRecordRow recordRow : records) {
                boolean hasPri = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                        .checkUpdateDataAuth(CustomerMetaData.EOName, customerId, recordRow.getOwner());
                if (recordRow.getId().equals(lastRecordId))
                    continue;
                if (hasPri) {
                    DynamicEnum status = recordRow.getStatus();
                    customer.setStatus(status);
                    customer.setLastRecord(recordRow.getId());
                    isSetStatus = true;
                    break;
                }
            }
            // 没有其他可回写的工作记录，更新状态为空
            if (isSetStatus == false) {
                customer.setStatus(null);
                customer.setLastRecord(null);
            }
            customerService.upsert(customer);
        }

    }

    private IWorkRecordRowSet getWorkRecordByCustomerId(Long customerId) {
        Criteria criteria = Criteria.AND().eq(WorkRecordMetaData.customer, customerId);
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        String jsonQuerySpec = JsonQueryBuilder.getInstance().setFirstResult(0).setMaxResult(1).addCriteria(criteria)
                .toJsonQuerySpec();
        IWorkRecordRowSet workRecordRowSet = query(jsonQuerySpec);
        return workRecordRowSet;
    }

    private void logicDeleteByIdWithAuth(Long id) {
        this.checkDeleteAuthById(id);
        this.deleteRowWithRecycle(id);
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(id, EO.WorkRecord, OP.DELETE);
    }

    @Override
    public LoadMoreList findCustomerWorkRecords(Long id, Integer pageNo, Integer pageSize, String status) {
        Integer total = countCustomerWorkRecords(id, status);
        IBusinessObjectRowSet items = queryByCustomer(id, status, pageNo, pageSize, ORDER_DESC);
        RowSet rowset = BoRowConvertUtil.toRowSet(items);
        List<Long> ids = new ArrayList<Long>();
        List<IBusinessObjectRow> rowList = new ArrayList<IBusinessObjectRow>();
        if (items != null && items.getRows() != null) {
            rowList = items.getRows();
        }
        for (IBusinessObjectRow workRecord : rowList) {
            ids.add((Long) workRecord.getFieldValue(SC.id));
        }
        Map<Long, List<IAttachmentRow>> attachMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findWorkRecordAttachments(ids);
        Map<Long, List<ITodoWorkRow>> todoWorkMap = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class)
                .findTodoWorkByWorkRecordId(ids);
        for (int i = 0; i < rowset.getItems().size(); i++) {
            Row row = rowset.getItems().get(i);
            Long count = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                    .countCommentByWorkrecordId(row.getLong("id"));
            row.put("commentCount", count);
            List<IAttachmentRow> attachments = attachMap.get(row.getLong("id"));
            row.put("attachments", BoRowConvertUtil.toRowList(attachments));
            List<ITodoWorkRow> todoWorks = todoWorkMap.get(row.getLong("id"));
            row.put("todoWorks", BoRowConvertUtil.toRowList(todoWorks));
        }

        LoadMoreList timeLine = new LoadMoreList();
        timeLine.setItems(rowset.getItems());
        timeLine.setHasMore(true);
        if (total <= (pageNo * pageSize)) {
            timeLine.setHasMore(false);
        }

        return timeLine;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Row findWorkRecord(Long id) {
/*    	ICustomerRow customerRow = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).query(100026L);
    	;*/
    	//IWorkRecordRow temprow = this.query(id);
        IBusinessObjectRow item = findByIdAndCusWithAuth(id);
        if (item == null)
            throw new AppException("app.workrecord.object.notexist");
        Row row = BoRowConvertUtil.toRow(item);
        Long count = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                .countCommentByWorkrecordId(row.getLong("id"));
        row.put("commentCount", count);
        Object customer = item.getFieldValue(WorkRecordMetaData.customer);
        if (customer != null) {
            Map<String, Object> customerMap = (Map<String, Object>) customer;
            row.put("customerName", customerMap.get(CustomerMetaData.name));
        } else {
            row.put("customerName", "");
        }
        List<IAttachmentRow> attachments = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findAttachmentListByRelate(EO.WorkRecord, id);
        row.put("attachments", BoRowConvertUtil.toRowList(attachments) == null ? new ArrayList<Row>()
                : BoRowConvertUtil.toRowList(attachments));
        return row;
    }

    @Override
    public int latestRecordForWeb() {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        UserSettingServiceItf userSettingService = ServiceLocator.getInstance().lookup(UserSettingServiceItf.class);
        IUserSettingRow userSettingRow = userSettingService.getByKeyUserIdAndType(userId, US.LAST_GET_WORKRECORD_TIME,
                null);
        if (userSettingRow == null) {
            userSettingService.generate(US.LAST_GET_WORKRECORD_TIME, String.valueOf(new Date().getTime()), userId,
                    null);
            return 0;
        }
        Long version = 0L;
        if (null != userSettingRow.getValue()) {
            version = Long.parseLong(userSettingRow.getValue());
        }
        return countLatestWorkRecordNum(userId, version);
    }

    private int countLatestWorkRecordNum(Long userId, Long version) {
        try {
            List<Long> ids = new ArrayList<Long>();
            UserValue user = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUserValueByUserId(userId);
            EnterpriseUtil.getUserById(userId);
            if (user == null) {
                return 0;
            }
            if (SRU.LEVEL_BOSS.equals(user.getUserLevel())) {
                List<UserValue> users = ServiceLocator.getInstance().lookup(UserServiceItf.class).getAllEnableUse(null);
                for (UserValue userValue : users) {
                    ids.add(userValue.getId());
                }
            } else {
                List<Map<String, Object>> subUsers = AppWorkManager.getDataAuthManager().getUserAffiliate()
                        .getAllSubUser(session(), userId, null);

                if (subUsers != null) {
                    for (Map<String, Object> userMap : subUsers) {
                        ids.add(Long.valueOf(userMap.get("id").toString()));
                    }
                }
            }
            if (ids == null || ids.size() == 0)
                return 0;
            int number = countLatestWorkRecordNum(version, ids);
            return number;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int countLatestWorkRecordNum(Long version, List<Long> ids) {
        String _ids = "";
        for (Long id : ids) {
            if (!_ids.isEmpty()) {
                _ids += ",";
            }
            _ids += id;
        }

        String jsonQuerySpec = JsonQuery.getInstance()
                .setCriteriaStr(
                        SC.createdDate + "> '" + DateUtil.getDateTimeString(new Date(version)) + "' AND " + SC.createdBy
                                + " <> " + EnterpriseContext.getCurrentUser().getUserLongId() + " AND " + SC.owner
                                + " in (" + _ids + ")")
                .toString();
        return getRowCount(jsonQuerySpec);
    }

    @Override
    public void read() {
        UserSettingServiceItf userSettingService = ServiceLocator.getInstance().lookup(UserSettingServiceItf.class);
        IUserSettingRow userSettingRow = userSettingService.getByKeyUserIdAndType(
                EnterpriseContext.getCurrentUser().getUserLongId(), US.LAST_GET_WORKRECORD_TIME, null);
        if (userSettingRow != null) {
            long lastTime = Long.parseLong(userSettingRow.getValue());
            Long time = new Date().getTime();
            if (time - lastTime > 1000) { // 大于一秒才更新
                userSettingRow.setValue(String.valueOf(new Date().getTime()));
                userSettingService.upsert(userSettingRow);
            }
        }

    }

    @Override
    public LoadMoreList getFollows(Integer pageno, Integer pagesize) {
        if (pageno == null || pageno < 0) {
            pageno = 0;
        }
        if (pagesize >= 1000) {
            throw new AppException("app.common.pagesize.toolarge");
        }
        LoadMoreList timeLine = new LoadMoreList();
        List<Long> workRecorIds = ServiceLocator.getInstance().lookup(CustomerFollowServiceItf.class)
                .findFollowCustomers(EnterpriseContext.getCurrentUser().getUserLongId(), pageno, pagesize + 1);
        timeLine.setHasMore(false);
        if (workRecorIds.size() == 0)
            return timeLine;
        if (workRecorIds.size() == (pagesize + 1)) {
            timeLine.setHasMore(true);
            workRecorIds.remove(workRecorIds.size() - 1);
        }
        RowSet rowset = BoRowConvertUtil.toRowSet(findWorkRecordByIds(workRecorIds));
        Map<Long, List<IAttachmentRow>> attachMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findWorkRecordAttachments(workRecorIds);
        for (int i = 0; i < rowset.getItems().size(); i++) {
            Row row = rowset.getItems().get(i);
            Long count = ServiceLocator.getInstance().lookup(CommentServiceItf.class)
                    .countCommentByWorkrecordId(row.getLong("id"));
            row.put("commentCount", count);
            List<IAttachmentRow> attachments = attachMap.get(row.getLong("id"));
            row.put("attachments", attachments);
        }
        timeLine.setItems(rowset.getItems());
        return timeLine;
    }

    private IBusinessObjectRowSet findWorkRecordByIds(List<Long> ids) {
        Criteria criteria = Criteria.AND();
        criteria.in(SC.id, ids.toArray());
        String queryStr = JsonQueryBuilder.getInstance().addCriteria(criteria).addOrderDesc(SC.createdDate)
                .toJsonQuerySpec();
        return ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery).query(queryStr);
    }

    @Override
    public IWorkRecordRow findByIdWithOutAuth(Long id) {
        IWorkRecordRow row = query(id);
        if (row != null) {
/*            Boolean isDelete = (Boolean) row.getFieldValue(SC.isDeleted);
            if (Boolean.TRUE.equals(isDelete)) {
                return null;
            }*/
            return row;
        }
        return null;
    }

    /* 保留给回收站初始化使用
     * @see com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf#getWorkRecordDeleted()
     */
    @Override
    public IWorkRecordRowSet getWorkRecordDeleted() {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq(SC.isDeleted, true);
        jsonQueryBuilder.addCriteria(criteria);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public List<Map<String, Object>> progressCountWorkrecord(Date startDate, Date endDate) {
        String workrecordHql = " select count(w.id) as num ,w.status as status ,w.owner.userId as userId from "
                + getBusinessObjectId() + " w "
                + " where (w.isDeleted is null or w.isDeleted = 'N') and (w.customer.id is null or w.customer.id in (select c.id from "
                + getBusinessObjectId(BO.Customer) + " c where c.id >"+SQ.HSY_SEED_DATA+"))"
               ;
        if (startDate != null && endDate != null) {
            workrecordHql += " and w.contactTime between :startDate and :endDate ";
        }
        workrecordHql += " group by w.status,w.owner.userId ";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        if (startDate != null && endDate != null) {
            Timestamp start = Timestamp.valueOf(DateUtil.getDateTimeString(startDate));
            Timestamp end = Timestamp.valueOf(DateUtil.getDateTimeString(endDate));
            paraMap.put("startDate", start);
            paraMap.put("endDate", end);
            // paraMap.put("isDeleted", false);
        }
        List<Map<String, Object>> result = runCQLQuery(workrecordHql, paraMap);
        return result;
    }

    @Override
    public Integer countByTime(Long version) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
    	Integer count=0;
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.gt(SC.createdDate, new Timestamp(version));
      //  criteria.addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false));
        jsonQueryBuilder.addCriteria(criteria);
        Integer hasCustomer = ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .getRowCount(jsonQueryBuilder.toJsonQuerySpec());
        Integer noCustomer =getNoCustomerCount(version);
        count =hasCustomer+noCustomer;
        return count;
    }
    
    private Integer getNoCustomerCount(Long version){
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria1 = Criteria.AND();
      //  criteria1.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria1.gt(SC.createdDate, new Timestamp(version));
        criteria1.empty("customer");
        jsonQueryBuilder.addCriteria(criteria1);
        return ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .getRowCount(jsonQueryBuilder.toJsonQuerySpec());

    }

    @Override
    public List<List<String>> getWorkRecordExcelRow(Long version, PageRestObject page) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.addChild(
                Criteria.OR().addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false))
                        .empty(WorkRecordMetaData.customer));
        Timestamp timestamp = new Timestamp(version);
        criteria.gt(SC.createdDate, timestamp);

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(SC.createdDate);
        jsonQueryBuilder.addOrderDesc(SC.id);

        Integer pageNo = page.getPageno();
        Integer pageSize = page.getPagesize();
        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);

        String jsonQuery = jsonQueryBuilder.toJsonQuerySpec();
        IBusinessObjectRowSet rowSet =  AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome(BO.WorkRecordForExport).query(jsonQuery);
        		//ServiceLocator.getInstance().lookup(BO.WorkRecordForExport).query(jsonQuery);
        List<IBusinessObjectRow> workRecordList = null;
        List<List<String>> retList = new ArrayList<List<String>>();
        if (rowSet != null && rowSet.getRows() != null) {
            workRecordList = rowSet.getRows();
            for (IBusinessObjectRow workRecord : workRecordList) {
                List<String> rowList = new ArrayList<String>();
                Object nameO = workRecord.getFieldValue("customerName") == null ? ""
                        : workRecord.getFieldValue("customerName");
                String name = String.valueOf(nameO);
                Object contentO = workRecord.getFieldValue(WorkRecordMetaData.content) == null ? ""
                        : workRecord.getFieldValue(WorkRecordMetaData.content);
                String content = String.valueOf(contentO);
                content = PushMsg.getAtContent(content);
                String owner = String.valueOf(workRecord.getFieldValue("ownerName"));
                String createdBy = String.valueOf(workRecord.getFieldValue("createdByName"));
                Timestamp createdDate = (Timestamp) workRecord.getFieldValue(SC.createdDate);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                rowList.add(name);
                rowList.add(content);
                rowList.add(owner);
                rowList.add(createdBy);
                rowList.add(sdf.format(createdDate));
                retList.add(rowList);
            }
        }

        return retList;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<Long, List<Long>> getIdMapByCustomer(List<Long> ids) {
        Map<Long, List<Long>> idMap = new HashMap<Long, List<Long>>();
        if (ids == null || ids.size() == 0) {
            return idMap;
        }
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(WorkRecordMetaData.customer, ids.toArray());
        criteria.addChild(Criteria.OR().empty("customer")
                .addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false)));
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IBusinessObjectRowSet  rowset = ServiceLocator.getInstance()
                .lookup(BO.WorkRecordForQuery).queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (IBusinessObjectRow row : rowset.getRows()) {
            Long id = (Long) row.getFieldValue(SC.id);
            Map<?, ?> customerMap = (Map<?, ?>) row.getFieldValue(WorkRecordMetaData.customer);
            if (customerMap != null) {
                Long customerId = (Long) customerMap.get(SC.id);
                if (idMap.containsKey(customerId)) {
                    idMap.get(customerMap.get(SC.id)).add(id);
                } else {
                    List<Long> idList = new ArrayList<Long>();
                    idList.add(id);
                    idMap.put(customerId, idList);
                }
            }

        }
        return idMap;
    }

    @Override
    public List<Long> getIdListByCustomerIdList(List<Long> ids) {
        List<Long> idList = new ArrayList<Long>();
        if (ids == null || ids.size() == 0) {
            return idList;
        }
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(WorkRecordMetaData.customer, ids.toArray());
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IWorkRecordRowSet rowset = this.queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (IWorkRecordRow row : rowset.getWorkRecordRows()) {
            Long id = (Long) row.getFieldValue(SC.id);
            idList.add(id);
        }
        return idList;
    }

    @Override
    public List<Long> getAllWorkReocrdIds() {
        List<Long> ids = new ArrayList<Long>();
        Criteria criteria = Criteria.AND();
/*        criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.addChild(Criteria.OR().empty("customer")
                .addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false)));*/
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        List<IBusinessObjectRow> rowset = ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .queryAll(jsonQueryBuilder.toJsonQuerySpec()).getRows();
        for (IBusinessObjectRow row : rowset) {
            Long id = (Long) row.getFieldValue(SC.id);
            ids.add(id);
        }
        return ids;
    }
    
    @Override
    public List<IBusinessObjectRow> getAllWorkReocrdSet() {
        Criteria criteria = Criteria.AND();
/*        criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.addChild(Criteria.OR().empty("customer")
                .addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false)));*/
    	String enterpiseId = AppWorkManager.getCloudEnterpriseId();
    	Long userId = AppWorkManager.getCurrAppUserId();
    	boolean isSuperUser = ServiceLocator.getInstance().lookup(UserServiceItf.class).isBoss(userId);
    	String targetID = PropertiesReader.getInstance("customer/enterpriseID.properties").getString("specialID");
    	if(enterpiseId.equals(targetID)&&isSuperUser){
    		Date startDate = DateUtil.getLastThreeMonthStart();
            criteria.gt(SC.lastModifiedDate, startDate.getTime());    		
    	}
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IBusinessObjectManager boManager = AppWorkManager.getBusinessObjectManager();        
        List<IBusinessObjectRow> rowset  = QueryLimitUtil.queryList(jsonQueryBuilder.toJsonQuerySpec(), boManager.getPrimaryBusinessObjectHome(BO.WorkRecordForQuery));
       
        /*List<IBusinessObjectRow> rowset = ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .queryAll(jsonQueryBuilder.toJsonQuerySpec()).getRows();*/
        return rowset;
    }


    @Override
    public IWorkRecordRow updateWorkRecord(String workRecordValue, Row retRow) {
        LinkedHashMap<String, Object> workRecord = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(workRecordValue);
        Long id = ConvertUtil.toLong(workRecord.get("id").toString());       
        return  updateWorkRecord( workRecordValue,  retRow,id) ;
    }
        
    @Override
    public IWorkRecordRow updateWorkRecord(String workRecordValue, Row retRow,Long id) {
        LinkedHashMap<String, Object> workRecord = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(workRecordValue);
        IWorkRecordRow workRecordRow = query(id);
        String entityName = workRecordRow.getBusinessObjectHome().getDefinition().getPrimaryEO().getName();
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).deleteAttachmentByRelate(entityName,
                workRecordRow.getId());
        if (workRecord.get("attachments") != null) {
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) workRecord.get("attachments");
            IAttachmentRowSet attSet = saveAttachment(attachments, workRecordRow);
            if (retRow != null) {
                Row convertRow = BoRowConvertUtil.toRow(workRecordRow);
                if (attSet != null && attSet.getAttachmentRows().size() > 0) {
                    retRow.put("attachments", BoRowConvertUtil.toRowSet(attSet).getItems());
                }
                retRow.putAll(convertRow);
            }
        }
        this.populateBORow(workRecord, workRecordRow);
        upsert(workRecordRow);
        return workRecordRow;
    }
    
    @Override
    public  String getWorkRecordList(Integer first,Integer max,Map<String,Object> para){
		Map<String,Object> retData = new HashMap<String,Object>();
		Criteria criteria = Criteria.AND();
		//criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
	    criteria.addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false));
		if(para.get("keyWord")!=null&&StringUtils.isNotEmpty(para.get("keyWord").toString())){
			String searchtext =(String)para.get("keyWord");
			try {
				//+号 在decode后会变成空格
				searchtext = searchtext.replace("+", "%2B"); 
				//searchtext =  new String(searchtext.getBytes("ISO-8859-1"), "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
			List<Long> customerIds =  ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCustomerIdsByKeyWord(searchtext);
			Criteria childCriteria=Criteria.OR();
			if(customerIds!=null&&customerIds.size()>0)
				childCriteria.in(WorkRecordMetaData.customer, customerIds.toArray());
			childCriteria.like(WorkRecordMetaData.content, searchtext);
			criteria.addChild(childCriteria);
		}
		if(para.get("customerId")!=null){
			criteria.eq(WorkRecordMetaData.customer, para.get("customerId"));
		}
		if(para.get("owner")!=null&&StringUtils.isNotEmpty(para.get("owner").toString())){
			String ownerStr	= (String)para.get("owner");
			List<Long> ownerIds = new ArrayList<Long>();
			String[] ids =ownerStr.split(",");
			for(String owner:ids){
				if(StringUtils.isEmpty(owner)){
					continue;
				}
				ownerIds.add(Long.parseLong(owner));
			}
			if(ownerIds.size()>0)
				criteria.in(SC.owner, ownerIds.toArray());										
		}
		else if(para.get("customerId")==null||StringUtils.isEmpty(para.get("customerId").toString())){
			Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
			UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
			String role =userService.getUserRoleName(currUserId);
			if(ROLE.SYSRELUSER_ROLE_SALESMAN.equals(role)||ROLE.SYSRELUSER_ROLE_SUPERISOR.equals(role)){
				List<Long> users = userService.getAllEnableSubordinate(currUserId);	
				if(users.size()>0)
					criteria.in(SC.owner, users.toArray());
			}
		}
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		String jsonStr = jsonQueryBuilder.addFields("customerIsDeleted",SC.owner,SC.id,SC.lastModifiedDate,WorkRecordMetaData.content,WorkRecordMetaData.contactTime,WorkRecordMetaData.content,WorkRecordMetaData.status,WorkRecordMetaData.customer)
		.setFirstResult(first).setMaxResult(max+1).addCriteria(criteria).addOrderDesc(WorkRecordMetaData.contactTime).toJsonQuerySpec();	
		//IBusinessObjectRowSet workRecordSet =this.query(jsonStr);
        IBusinessObjectRowSet workRecordSet = ServiceLocator.getInstance().lookup(BO.WorkRecordForQuery)
                .query(jsonStr);
		List<Map<String,Object>> items = new ArrayList<Map<String,Object>>();
		boolean hasMore = false;
		int size = 0;
		PrivilegeServiceItf privilegeService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
		CommentServiceItf commentService = ServiceLocator.getInstance().lookup(CommentServiceItf.class);
		AttachmentServiceItf attachementService = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);

		for(IBusinessObjectRow workRecordRow :workRecordSet.getRows()){
			size++;
			if(size==max+1){
				hasMore = true;
				break;
			}
/*			Boolean test = (Boolean)workRecordRow.getFieldValue(session, "customerIsDelete");
			if(test){
				continue;
			}*/
			Row retRow = BoRowConvertUtil.toRow(workRecordRow);			
			HashMap userMap = (HashMap)workRecordRow.getFieldValue(SC.owner);
			Long ownerId = (Long)userMap.get(SC.id);
			Long workRecordId = (Long)workRecordRow.getFieldValue(SC.id);
			boolean hasPri=privilegeService.checkDeleteDataAuth(WorkRecordMetaData.EOName,workRecordId, EnterpriseContext.getCurrentUser().getUserLongId());	    
				
			if(EnterpriseContext.getCurrentUser().getUserLongId().equals(ownerId)){
				retRow.put("privilege", Integer.valueOf("111", 2));
			}else if(hasPri){				
				retRow.put("privilege", Integer.valueOf("101", 2));	
			}else{
				retRow.put("privilege", Integer.valueOf("001", 2));	
			}
			Long count = commentService.countCommentByWorkrecordId(workRecordId);
			retRow.put("commentCount", count);	
			items.add(retRow);
		}
		//组装附件信息
		attachementService.getAttachmentsByRelateItem(items, WorkRecordMetaData.EOName);
		retData.put("hasMore", hasMore);
		retData.put("items", items);
		return AppWorkManager.getDataManager().toJSONString(retData);
	}
    
    @Override
    public IWorkRecordRow updateWorkRecordForH5(String workRecordValue, Row retRow,Long id) {
        LinkedHashMap<String, Object> workRecord = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(workRecordValue);
        IWorkRecordRow workRecordRow = query(id);
        if (workRecord.get("attachments") != null) {
            List<Map<String, Object>> attachments = (List<Map<String, Object>>) workRecord.get("attachments");
            saveAttachmentForH5(attachments, workRecordRow);
            AttachmentServiceItf attachementService = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class);
            List<IAttachmentRow> atts =attachementService.findAttachmentListByRelate(WorkRecordMetaData.EOName, id);
            if (retRow != null) {
                //Row convertRow = BoRowConvertUtil.toRow(workRecordRow);
                if (atts != null && atts.size() > 0) {
                    retRow.put("attachments", BoRowConvertUtil.toRowList(atts));
                }
               // retRow.putAll(convertRow);
            }
        }
        this.populateBORow(workRecord, workRecordRow);
        upsert(workRecordRow);
        return workRecordRow;
    }
}
