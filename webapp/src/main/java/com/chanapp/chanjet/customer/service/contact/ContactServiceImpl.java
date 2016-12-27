package com.chanapp.chanjet.customer.service.contact;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactHome;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.constant.BI;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.PV;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.PhoneNumUtil;
import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.bo.api.IDataTransformer;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class ContactServiceImpl extends BoBaseServiceImpl<IContactHome, IContactRow, IContactRowSet>
        implements ContactServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(ContactServiceImpl.class);

    @Override
    public IContactRow addContactForCustomer(LinkedHashMap<String, Object> contactParam, Long customerId) {
        LinkedHashMap<String, Object> customer = new LinkedHashMap<String, Object>();
        customer.put("id", customerId);
        contactParam.put("customer", customer); // 设置联系人的客户
        contactParam.put("ingoreCustomer", true); // 忽略客户的检查及设置
        return upsertContact(contactParam);
    }

    /**
     * 新增或变更联系人
     * 
     * @param contact 联系人信息（端参数）
     * @return ContactRow 新增或更新后的联系人ROW对象
     */
    @Override
    public IContactRow upsertContact(LinkedHashMap<String, Object> contact) {
        IContactRow contactRow = getFinalContactRow(contact); // 获取设置后的联系人ROW对象。
        try {
            this.upsert(contactRow);
        } catch (AppException e) {
            if (e.getErrorMsgList() != null) {
                Object[] args = e.getErrorMsgList().get(0).getArguments();
                if (args != null && args.length > 2) {
                    String field = (String) args[1];
                    if (ContactMetaData.email.equals(field)) {
                        throw new AppException("app.contact.email.invalid");
                    }
                }
            }
            throw e;
        }
        contactRow = this.query(contactRow.getId());
        return contactRow;
    }

    /**
     * 获取根据参数设置后的联系人ROW对象。
     * 
     * @param contact 联系人信息（端参数）
     * @param boHome
     * @return ContactRow 联系人ROW对象
     */
    private IContactRow getFinalContactRow(LinkedHashMap<String, Object> contact) {
        // 获取联系人对象
        IContactRow contactRow = getContactRow(contact);
        // 设置联系人的客户
        if (contact.containsKey("ingoreCustomer") && !(Boolean) contact.get("ingoreCustomer")) {
            // 客户ID不能为空
            Assert.notNull(contact.get("customer"), "app.contact.customer.required");
            // 客户ID必须有SELECT权限
            @SuppressWarnings("unchecked")
            ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .query((Long) ((LinkedHashMap<String, Object>) contact.get("customer")).get("id"));
            Assert.notNull(customer, "app.customer.object.notexist");
            contactRow.setCustomer(customer.getId());
        }
        this.populateBORow(contact, contactRow);
        if (contact.containsKey("ingoreCustomer") && !(Boolean) contact.get("ingoreCustomer")) {
            // 客户ID对应的客户在数据库中存在
            Assert.notNull(contactRow.getCustomer(), "app.customer.object.notexist");
        }
        return contactRow;
    }

    /**
     * 获取联系人ROW对象
     * 
     * @param contact 联系人信息（端参数）
     * @param boHome
     */
    private IContactRow getContactRow(LinkedHashMap<String, Object> contact) {
    
        Long id = contact.get("id")==null?null:ConvertUtil.toLong(contact.get("id").toString());
        if (id != null && id > 0) {
            return getContactForUpdate(contact);
        } else {
            return getContactForAdd(contact);
        }
    }

    /**
     * 返回用来新增的联系人ROW对象。 根据localid查找联系人对象，如果存在则返回。
     * 根据入参的客户ID、手机、联系人姓名查找联系人，如果存在则返回。 创建空白的联系人ROW对象。
     * 
     * @param contactValue 入参：联系人信息
     * @param contactBoHome
     * @param session
     * @return contact 联系人ROW对象
     */
    @SuppressWarnings("unchecked")
    private IContactRow getContactForAdd(LinkedHashMap<String, Object> contactValue) {
        IContactRow contact = null;

        if (contactValue.get(ContactMetaData.localId)!=null) {
            if (!(contactValue.get(ContactMetaData.localId)).toString().equals("0")) {
                contact = getContactByLocalIdWithBo(contactValue.get(ContactMetaData.localId).toString());
            } else {
                int i = (int) (Math.random() * 900) + 100;
                Long localId = new Date().getTime();
                contactValue.put(ContactMetaData.localId, "-" + localId.toString() + i);
            }
        } else {
            int i = (int) (Math.random() * 900) + 100;
            Long localId = new Date().getTime();
            contactValue.put(ContactMetaData.localId, "-" + localId.toString() + i);
        }
        if (contact == null) {
            Long customerId = 0L;
            Object customer = contactValue.get(ContactMetaData.customer);
            if (customer != null) {
                if (customer instanceof Map) {
                	Object tempId = ((LinkedHashMap<String, Object>) customer).get(SC.id);
                    customerId = Long.parseLong(tempId.toString());
                } else {
                    customerId = ConvertUtil.toLong(customer.toString());
                }
            }
            Long contactId = contactValue.get(SC.id)==null?null:Long.parseLong(contactValue.get(SC.id).toString());
            String contactMobile = contactValue.get(ContactMetaData.mobile)==null?null:contactValue.get(ContactMetaData.mobile).toString();
            String contactName = contactValue.get(ContactMetaData.name)==null?null:contactValue.get(ContactMetaData.name).toString();
            contact = findExistContactWithBo(contactId, customerId,
            		contactMobile, contactName);
        }
        if (contact == null) {
            contact = this.createRow();
        }
        return contact;
    }

    /**
     * 返回已经存在的联系人BO对象。（本地ID）
     * 
     * @param contactId
     * @param contactBoHome
     * @return
     */
    private IContactRow getContactByLocalIdWithBo(String localId) {
        return _getContactByLocalId(localId);
    }

    /**
     * 根据入参：本地ID 获取客户信息，如果获取不到则返回NULL
     * 
     * @param localId 本地ID
     * @return IBusinessObjectRow 第一条查询结果或NULL
     */
    private IContactRow _getContactByLocalId(String localId) {
        String queryStr = JsonQuery.getInstance().setCriteriaStr(ContactMetaData.localId + "='" + localId + "'").toString();
        IContactRowSet contactRowSet = this.query(queryStr);
        if (contactRowSet != null && contactRowSet.getRows() != null && contactRowSet.getRows().size() > 0) {
            return contactRowSet.getRow(0);
        }
        return null;
    }

    /**
     * 返回已经存在的联系人BO对象。（用来操作数据库）
     * 
     * @param contactId
     * @param customerId
     * @param mobile
     * @param contactName
     * @param contactBoHome
     * @return
     */
    private IContactRow findExistContactWithBo(Long contactId, Long customerId, String mobile, String contactName) {
        return _findExistContact(contactId, customerId, mobile, contactName);
    }

    /**
     * 获取联系人对象。
     * 
     * @param contactId 联系人ID
     * @param customerId 客户ID
     * @param mobile 联系人电话
     * @param contactName 联系人名称
     * @param contactBoHome 可以为空
     */
    private IContactRow _findExistContact(Long contactId, Long customerId, String mobile, String contactName) {
        if (null != customerId && StringUtils.isNotEmpty(contactName)) {
            if (StringUtils.isNotEmpty(contactName)) {
            	contactName = contactName.replaceAll("'", "''");
            }
            String criteriaStr = ContactMetaData.customer + "=" + customerId + " AND " + ContactMetaData.name + "='"
                    + contactName + "' ";
            if (StringUtils.isNotEmpty(mobile)) {
                criteriaStr += " AND " + ContactMetaData.mobile + "='" + mobile + "'";
            } else {
                criteriaStr += " AND " + ContactMetaData.mobile + " is null";
            }
            if (null != contactId) {
                criteriaStr += " AND " + SC.id + "!=" + contactId;
            }
            String queryStr = JsonQuery.getInstance().setCriteriaStr(criteriaStr).toString();
            IContactRowSet contactRowSet = this.query(queryStr);
            if (contactRowSet != null && contactRowSet.getRows() != null && contactRowSet.getRows().size() > 0) {
                return contactRowSet.getRow(0);
            }
        }
        return null;
    }

    /**
     * 获取用来更新的联系人ROW对象。
     * 
     * @param contactValue 联系人信息（端参数）
     * @param contactBoHome
     * @return ContactRow
     */
    private IContactRow getContactForUpdate(LinkedHashMap<String, Object> contactValue) {
        // 变更时、联系人ID不能为空
        IContactRow contact = this.query(ConvertUtil.toLong(contactValue.get("id").toString()));
        Assert.notNull(contact, "app.contact.object.notexist");
        return contact;
    }

    /**
     * 根据客户ID逻辑删除联系人。
     * 
     * @param customerId
     */
    @Override
    public void delByCustomerId(Long customerId) {
        // 删除客户下的联系人
        _delByCustomerId(customerId);
    }

    /**
     * 根据客户ID删除联系人信息。
     * 
     * @param customerId 客户ID
     */
    private void _delByCustomerId(Long customerId) {
  
        // 根据客户ID查询该客户下的未删除的联系人信息
/*        String jsonQuerySpec = JsonQuery.getInstance().setCriteriaStr(ContactMetaData.customer + "=" + customerId
                + " AND (" + SC.isDeleted + " is null OR " + SC.isDeleted + "='N')").toString();

        this.batchUpdate(jsonQuerySpec, new String[] { SC.isDeleted }, new Object[] { Boolean.valueOf(true) });*/
    }

    @Override
    public void preUpsert(IContactRow row, IContactRow origRow) {
        upsertSet(row, origRow);
        upsertCheck(row, origRow);
    }

    @Override
    public void postUpsert(IContactRow row, IContactRow origRow) {
        if (StringUtils.isEmpty(row.getLocalId())) {
            Long id = row.getId();
            String json = JsonQuery.getInstance().setCriteriaStr(SC.id + "=" + id).toString();
            ServiceLocator.getInstance().lookup(BO.Contact).batchUpdate(json,
                    new String[] { ContactMetaData.localId }, new String[] { String.valueOf(id) });
        }

        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        operationLogService.generate(row);
        if (this.isInsert(row, false)) {
            operationLogService.writeMsg2BigData(row.getId(), BI.CONTACT_ADD, 0);
        }
    }

    /**
     * 新增联系人和变更联系人的通用设置。
     * 
     * @param row
     * @param origRow
     */
    private void upsertSet(IContactRow row, IContactRow origRow) {
        // 客户ID不能为空
        Assert.notNull(row.getCustomer(), "app.contact.customer.required");
        // 客户ID必须有SELECT权限

        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).query(row.getCustomer());
        row.setCustomer(customer.getId());
        row.setCustomerCopy(customer.getName());
        // 设置全拼和简拼
        if (null != row.getName()) {
            row.setFullSpell(PinyinUtil.hanziToPinyinFull(row.getName(), true));
            row.setSimpleSpell(PinyinUtil.hanziToPinyinSimple(row.getName(), false));
        }
        // 设置手机
        if (null != row.getMobile()) {
            row.setEffectiveMobile(getMobilePhone(row.getMobile().getPhoneNumber()));
        }
        // 设置电话
        if (null != row.getPhone()) {
            row.setEffectivePhone(getMobilePhone(row.getPhone().getPhoneNumber()));
        }
        // 设置OWNER
        row.setModifiedTime(DateUtil.getNowDateTime());
        // 设置OWNER
        if (row.getOwner() == null) {
            row.setOwner(EnterpriseContext.getCurrentUser().getUserLongId());
        }
    }

    /**
     * 设置手机和电话
     * 
     * @param mobilePhone
     * @return mobilePhone
     */
    private String getMobilePhone(String mobilePhone) {
    	if(mobilePhone==null||StringUtils.isEmpty(mobilePhone))
    		return null;
        List<String> listMobile = PhoneNumUtil.getEffectivePhoneNum(mobilePhone);
        String effectiveMobile = ",";
        for (String m : listMobile) {
            effectiveMobile += m + ",";
        }
        return effectiveMobile;
    }

    /**
     * 新增联系人和变更联系人的通用检查。
     * 
     * @param row
     * @param origRow
     */
    private void upsertCheck(IContactRow row, IContactRow origRow) {
        Assert.notNull(row.getCustomer(), "app.contact.customer.required");

        // 检查名称和OWNER
        Assert.hasLength(row.getName(), "app.contact.name.required");
        Assert.notNull(row.getOwner(), "app.contact.owner.notexist");
        // 检查联系人是否重复。
        checkContactDuplicated(row);
    }

    /**
     * 检查联系人是否重复
     * 
     * @param row 客户对象
     */
    private void checkContactDuplicated(IContactRow contactRow) {
        // 入参的联系人ID、客户ID、手机和联系人姓名能在数据中查到，则返回数据库中该联系人的ROW对象。
        String phoneNumber = "";
        if (contactRow.getMobile() != null) {
            phoneNumber = contactRow.getMobile().getPhoneNumber();
        }
        IContactRow sameNameContactNewCustomer = _findExistContact(contactRow.getId(), contactRow.getCustomer(),
                phoneNumber, contactRow.getName());
        if (sameNameContactNewCustomer != null) {
            Assert.isNull(sameNameContactNewCustomer, "app.contact.duplicated");
        }
    }

    @Override
    public List<Long> getCustomerIdByContactSearch(String searchValue) {
        List<Long> ids = new ArrayList<Long>();
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.addChild(
                Criteria.OR().like(ContactMetaData.name, searchValue).like(ContactMetaData.fullSpell, searchValue)
                        .like(ContactMetaData.simpleSpell, searchValue).like(ContactMetaData.email, searchValue)
                        .like(ContactMetaData.mobile, searchValue).like(ContactMetaData.position, searchValue));
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance().addFields(ContactMetaData.customer);
        jsonQueryBuilder.addCriteria(criteria);
        IContactRowSet rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        if (rowSet != null && rowSet.getContactRows() != null) {
            for (IContactRow row : rowSet.getContactRows()) {
                if (row.getCustomer() != null) {
                    ids.add(row.getCustomer());
                }
            }
        }
        return ids;
    }

    @Override
    public Map<Long, Object> getDelNumByCustomerId(List<Long> customerIds) {
        Map<Long, Object> result = new HashMap<Long, Object>();
        if (customerIds != null && customerIds.size() > 0) {
            String cqlQueryString = " select count(c.id) as num,c.customer.id  as cid from "
                    + getBusinessObjectHome().getDefinition().getId() + " c ";
            cqlQueryString += " where (c.isDeleted is null or c.isDeleted = :isDeleted) ";
            cqlQueryString += " and c.customer.id in :customerIds group by c.customer.id ";
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            paraMap.put("isDeleted", false);
            paraMap.put("customerIds", customerIds);
            List<Map<String, Object>> list = runCQLQuery(cqlQueryString, paraMap);
            if (list != null) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    result.put((Long) list.get(i).get("cid"), list.get(i).get("num"));
                }
            }
        }
        return result;
    }

    @Override
    public HashMap<Long, ArrayList<Row>> findContactByCustomerIds(List<Long> ids) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(ContactMetaData.customer, ids.toArray());
        jsonQueryBuilder.addCriteria(criteria).addOrderAsc(SC.createdDate);
        IContactRowSet contactSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        HashMap<Long, ArrayList<Row>> retMap = new HashMap<Long, ArrayList<Row>>();
        if (contactSet.getContactRows() == null || contactSet.getContactRows().size() == 0) {
            return retMap;
        }
        for (IContactRow row : contactSet.getContactRows()) {
            Row contactRow = new Row();
            if (row.getEmail() != null) {
                contactRow.put(ContactMetaData.email, row.getEmail().getAccountId());
            }
            if (row.getMobile() != null) {
                contactRow.put(ContactMetaData.mobile, row.getMobile().getPhoneNumber());
            }
            contactRow.put(ContactMetaData.name, row.getName());
            contactRow.put(SC.id, row.getId());
            // Row contactRow = BeanConverter.getInstanse().toRow(row);
            Long customerId = row.getCustomer();
            if (retMap.containsKey(customerId)) {
                ArrayList<Row> contactlist = retMap.get(customerId);
                contactlist.add(contactRow);
            } else {
                ArrayList<Row> contactlist = new ArrayList<Row>();
                contactlist.add(contactRow);
                retMap.put(customerId, contactlist);
            }
        }
        return retMap;
    }

    @Override
    public Map<String, Object> addContact(LinkedHashMap<String, Object> contactParam) {
        Map<String, Object> addResult = addContactWithMerge(contactParam);
        IContactRow contactRow = (IContactRow) addResult.get("entity");
        Row row = BoRowConvertUtil.toRow(contactRow);

        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .query(contactRow.getCustomer());
        if (customer != null) {
            Row customerRow = BoRowConvertUtil.toRow(customer);
            row.put("customer", customerRow);
        }

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("mergeFlag", addResult.get("mergeFlag"));
        result.put("entity", row);
        return result;
    }

    /**
     * 新增联系人。（返回结果带mergeFlag）
     *
     * @param contactValue 联系人信息
     * @return contactRow 新增的联系人ROW对象
     */
    @Override
    public Map<String, Object> addContactWithMerge(LinkedHashMap<String, Object> contactParam) {
        IContactRow contactRow = getFinalContactRow(contactParam);
        Map<String, Object> result = new HashMap<String, Object>();
        boolean mergeFlag = contactRow.getId() != null && contactRow.getId() > 0;
        result.put("mergeFlag", mergeFlag);
        try {
            upsert(contactRow);
        } catch (AppException e) {
            if (e.getErrorMsgList() != null) {
                Object[] args = e.getErrorMsgList().get(0).getArguments();
                if (args != null && args.length > 2) {
                    String field = (String) args[1];
                    if (ContactMetaData.email.equals(field)) {
                        throw new AppException("app.contact.email.invalid");
                    }
                }
            }
            throw e;
        }
        contactRow = query(contactRow.getId());
        result.put("entity", contactRow);
        return result;
    }

    @Override
    public Row updateContact(LinkedHashMap<String, Object> contactParam) {
        IContactRow contactRow = upsertContact(contactParam);
        return BoRowConvertUtil.toRow(contactRow);
    }

    @Override
    public void deleteContact(Long contactId) {
        logicDeleteByIdWithAuth(contactId);
      //  ServiceLocator.getInstance().lookup(RecycleServiceItf.class).addRecovery(ContactMetaData.EOName, contactId);
    }

    /**
     * 逻辑删除entity（需要检查数据权限）
     * 
     * @param id entityID
     * @param session
     */
    private void logicDeleteByIdWithAuth(Long id) {
        this.checkDeleteAuthById(id);
        this.deleteRowWithRecycle(id);
/*
        String json = "{\"Criteria\" : {\"FieldName\" : \"id\", \"Operator\" : \"eq\", \"Values\" : [" + id + "]}}";
        batchSetIsDeleted(json, true);*/

        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(id, ContactMetaData.EOName,
                "DELETE");
    }

    @Override
    public IContactRowSet findContactsByCustomer(Long customerId) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(ContactMetaData.customer, customerId);
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderAsc(SC.createdDate);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public Row getContact(Long contactId) {
        IContactRow contactRow = query(contactId);
        if (contactRow == null) {
            throw new AppException("app.contact.object.notexist");
        }
        return BoRowConvertUtil.toRow(contactRow);
    }

    @Override
    public IContactRowSet findByCustomrIds(List<Long> customerids) {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(ContactMetaData.customer, customerids.toArray());
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderAsc(SC.createdDate);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public IContactRowSet getContactDeleted() {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.eq(SC.isDeleted, true);
        jsonQueryBuilder.addCriteria(criteria);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public Integer countAll() {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
      //  criteria.addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false));
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        return ServiceLocator.getInstance().lookup(BO.ContactWithCustomer)
                .getRowCount(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public IBusinessObjectRowSet findAllWithPage(PageRestObject page) {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
       // criteria.addChild(Criteria.OR().empty("customerIsDeleted").eq("customerIsDeleted", false));

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(SC.createdDate);
        jsonQueryBuilder.addOrderDesc(SC.id);

        Integer pageNo = page.getPageno();
        Integer pageSize = page.getPagesize();
        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);
        return AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome(BO.ContactWithCustomer).query(jsonQueryBuilder.toJsonQuerySpec());
      //  return ServiceLocator.getInstance().lookup(BO.ContactWithCustomer).query(jsonQueryBuilder.toJsonQuerySpec());
    }

    /**
     * 全公司同名联系人总数
     * 
     * @param mobile 手机号
     * @param phone 电话
     */
    private int countGlobalContacts(List<String> mobileList, List<String> phoneList) {
        Criteria criteria = Criteria.AND().addChild(Criteria.OR().empty("isDeleted").eq("isDeleted", "false"));
        Criteria condition = Criteria.OR();
        if (mobileList != null) {
            for (String mobile : mobileList) {
                condition.like("effectiveMobile", "," + mobile + ",");
            }
        }
        if (phoneList != null) {
            for (String phone : phoneList) {
                condition.like("effectivePhone", "," + phone + ",");
            }
        }
        criteria.addChild(condition);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        Integer total = privilegedGetRowCount(jsonQuerySpec);
        return total;
    }

    /**
     * 全公司相同手机号和电话的联系人
     * 
     * @param mobile
     * @param phone
     * @param maxResults
     */
    private List<Map<String, Object>> findGlobalContacts(List<String> mobileList, List<String> phoneList,
            int maxResults) {
        Criteria criteria = Criteria.AND().addChild(Criteria.OR().empty("isDeleted").eq("isDeleted", "false"));
        Criteria condition = Criteria.OR();
        if (mobileList != null) {
            for (String mobile : mobileList) {
                condition.like("effectiveMobile", "," + mobile + ",");
            }
        }
        if (phoneList != null) {
            for (String phone : phoneList) {
                condition.like("effectivePhone", "," + phone + ",");
            }
        }
        criteria.addChild(condition);

        String jsonQuerySpec = JsonQueryBuilder.getInstance()
                .addFields("id", "name", "owner", "customer", "customerName", "mobile", "phone", "email", "qq", "logo",
                        "position", "address", "remark", "createdDate", "customerOwner")
                .addCriteria(criteria).setMaxResult(maxResults).toJsonQuerySpec();

        List<Object[]> list = ServiceLocator.getInstance().lookup(BO.ContactWithCustomer)
                .privilegedQueryNoTransform(jsonQuerySpec);
       // List<Object[]> list = data.getRawData();

        List<Map<String, Object>> contacts = new ArrayList<Map<String, Object>>();

        for (Object[] o : list) {
            Map<String, Object> contact = new HashMap<String, Object>();
            contact.put("id", o[0]);
            contact.put("name", o[1]);
            contact.put("owner", o[2]);
            contact.put("customerId", o[3]);
            contact.put("customerName", o[4]);
            contact.put("mobile", o[5]);
            contact.put("phone", o[6]);
            contact.put("email", o[7]);
            contact.put("qq", o[8]);
            contact.put("logo", o[9]);
            contact.put("position", o[10]);
            contact.put("address", o[11]);
            contact.put("remark", o[12]);
            contact.put("createdDate", o[13]);
            contact.put("customerOwnerId", o[14]);
            contacts.add(contact);
        }
        return contacts;
    }

    @Override
    public Map<String, Object> getContactsByMobileAndPhone(String mobile, String phone) {
        int maxResults = 50;
        // 全公司同名联系人总数
        List<String> mobileList = null;
        if (StringUtils.isNotEmpty(mobile)) {
            mobileList = PhoneNumUtil.getEffectivePhoneNum(mobile);
        }
        List<String> phoneList = null;
        if (StringUtils.isNotEmpty(phone)) {
            phoneList = PhoneNumUtil.getEffectivePhoneNum(phone);
        }
        if ((mobileList == null || mobileList.size() < 1) && (phoneList == null || phoneList.size() < 1)) {
            throw new AppException("app.contact.phone.invalid");
        }

        int total = countGlobalContacts(mobileList, phoneList);
        // 全公司最多50个同名联系人
        List<Map<String, Object>> contacts = findGlobalContacts(mobileList, phoneList, maxResults);
        // 修改权限范围内的联系人
        List<Long> contactIds = new ArrayList<Long>();
        if (contacts != null) {
            for (Map<String, Object> contact : contacts) {
                Long contactId = (Long) contact.get("id");
                if (contactId != null && contactId > 0) {
                    contactIds.add(contactId);
                }
            }
        }
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Long> contactUpdatePriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                .checkUpdateDataAuthList(ContactMetaData.EOName, contactIds, userId);
        List<Long> contactSelectPriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                .checkSelectDataAuthList(ContactMetaData.EOName, contactIds, userId);
        List<Map<String, Object>> contactList = new ArrayList<Map<String, Object>>();

        List<Long> customerUpdatePriv;
        List<Long> customerSelectPriv;
        Long customerId = null;
        List<Long> customerIds = null;

        if (contacts != null) {
            for (Map<String, Object> contact : contacts) {
                Map<String, Object> contactMap = new HashMap<String, Object>();
                Long contactId = (Long) contact.get("id");
                contactMap.put("id", contactId);
                contactMap.put("name", contact.get("name"));
                contactMap.put("owner", contact.get("owner"));

                if (contactUpdatePriv.contains(contactId)) {
                    contactMap.put("privilege", PV.UPDATE_PRVILEGE);
                } else if (contactSelectPriv.contains(contactId)) {
                    contactMap.put("privilege", PV.SELECT_PRVILEGE);
                } else {
                    contactMap.put("privilege", PV.NO_PRVILEGE);
                }

                contactMap.put("customerId", contact.get("customerId"));
                contactMap.put("customerName", contact.get("customerName"));
                contactMap.put("customerOwnerId", contact.get("customerOwnerId"));

                customerId = (Long) contact.get("customerId");
                if (customerId != null && customerId > 0) {
                    customerIds = new ArrayList<Long>();
                    customerIds.add(customerId);
                    customerUpdatePriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                            .checkUpdateDataAuthList(CustomerMetaData.EOName, customerIds, userId);
                    customerSelectPriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                            .checkSelectDataAuthList(CustomerMetaData.EOName, customerIds, userId);
                    if (customerUpdatePriv.contains(customerId)) {
                        contactMap.put("customerPrivilege", PV.UPDATE_PRVILEGE);
                    } else if (customerSelectPriv.contains(customerId)) {
                        contactMap.put("customerPrivilege", PV.SELECT_PRVILEGE);
                    } else {
                        contactMap.put("customerPrivilege", PV.NO_PRVILEGE);
                    }
                } else {
                    contactMap.put("customerPrivilege", PV.NO_PRVILEGE);
                }
                contactMap.put("mobile", contact.get("mobile"));
                contactMap.put("phone", contact.get("phone"));
                contactMap.put("email", contact.get("email"));
                contactMap.put("qq", contact.get("qq"));
                contactMap.put("logo", contact.get("logo"));
                contactMap.put("position", contact.get("position"));
                contactMap.put("address", contact.get("address"));
                contactMap.put("remark", contact.get("remark"));
                contactMap.put("createdDate", contact.get("createdDate"));
                contactList.add(contactMap);
            }
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("items", contactList);
        result.put("total", total);
        result.put("result", true);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> scanCard(LinkedHashMap<String, Object> contactParam) {
        LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) contactParam
                .get("scannedCustomer");
        Assert.notNull(customerParam, "app.customer.object.notexist");
        String customerName = customerParam.get("name")==null ? null:customerParam.get("name").toString();
        String phone = null;
		if(customerParam.get("phone")!=null)
			phone = customerParam.get("phone").toString();
        Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .getExistsCustomer(null, customerName, phone);
        if (sameCustomer != null) {
            Long customerId = (Long) sameCustomer.get("id");
            ICustomerRow row = null;
            try {
                row = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).findByIdWithAuth(customerId);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("CustomerService.findByIdWithAuth exception:", e);
            }
            if (row == null)
                Assert.customerRepeat(sameCustomer);
            else {
                customerParam.put(SC.id, customerId);
            }
        }
        // 新增客户
        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .addCustomer(customerParam);
        // 返回结果设置
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("mergeFlag", false);
        IContactRow contactRow = addContactForCustomer(contactParam, customer.getId());
        Row customerRow = BoRowConvertUtil.toRow(customer);
        Row retRow = BoRowConvertUtil.toRow(contactRow);
        retRow.put("customer", customerRow);
        result.put("entity", retRow);
        return result;
    }

    @Override
    public List<Long> getIdListByCustomerIdList(List<Long> ids) {
        List<Long> idList = new ArrayList<Long>();
        if (ids == null || ids.size() == 0) {
            return idList;
        }
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(ContactMetaData.customer, ids.toArray());
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        IContactRowSet rowset = this.queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (IContactRow row : rowset.getContactRows()) {
            Long id = (Long) row.getFieldValue(SC.id);
            idList.add(id);
        }
        return idList;
    }

}
