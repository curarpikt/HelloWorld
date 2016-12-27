package com.chanapp.chanjet.customer.service.customer;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerHome;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customerforquerylist.ICustomerForQueryListRow;
import com.chanapp.chanjet.customer.businessobject.api.customerforquerylist.ICustomerForQueryListRowSet;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.cache.CSPEnum;
import com.chanapp.chanjet.customer.cache.CSPEnumValue;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.cache.MetadataCacheBuilder;
import com.chanapp.chanjet.customer.constant.BI;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.PV;
import com.chanapp.chanjet.customer.constant.SQ;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.UserMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customerfollow.CustomerFollowServiceItf;
import com.chanapp.chanjet.customer.service.grant.GrantServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.RecycleServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.MsgUtil;
import com.chanapp.chanjet.customer.util.PeriodUtil;
import com.chanapp.chanjet.customer.util.PhoneNumUtil;
import com.chanapp.chanjet.customer.util.PinyinUtil;
import com.chanapp.chanjet.customer.util.ReflectionUtil;
import com.chanapp.chanjet.customer.util.ShortIdUtil;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserQuery;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.bo.api.ReportingResult;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.usertype.DynamicEnum;
import com.chanjet.csp.common.base.usertype.GeoPoint;
import com.chanjet.csp.common.base.usertype.Phone;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class CustomerServiceImpl extends BoBaseServiceImpl<ICustomerHome, ICustomerRow, ICustomerRowSet>
        implements CustomerServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    public final static String SPLITFLAG = "||";

    /**
     * 新增客户
     * 
     * @param customerParam 客户信息（端参数）
     * @return CustomerRow 新增后客户信息（DB）
     */
    @Override
    public ICustomerRow addCustomer(LinkedHashMap<String, Object> customerParam) {
        return upsertCustomer(customerParam, null);
    }

    /**
     * 变更客户信息
     * 
     * @param customerParam 客户信息（端参数）
     * @return CustomerRow 变更后客户信息（DB）
     */
    public ICustomerRow updateCustomer(LinkedHashMap<String, Object> customerParam) {
        return upsertCustomer(customerParam, null);
    }

    @Override
    public void preUpsert(ICustomerRow row, ICustomerRow origRow) {
        if (this.isInsert(row, true)) {       
            // 设置客户ROW对象的通用字段
            setCustomerRowCommonItem(row);
            // 检查客户对象
            checkCustomerRow(row);
        } else {
                // 设置客户ROW对象的通用字段
                setCustomerRowCommonItem(row);
                // 检查客户对象
                checkCustomerRow(row);
           // }
        }
    }

    @Override
    public void postUpsert(ICustomerRow row, ICustomerRow origRow) {
        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);

        if (this.isInsert(row, false)) {
            if (StringUtils.isEmpty(row.getLocalId())) {
                Long id = row.getId();
                String json = JsonQuery.getInstance().setCriteriaStr(SC.id + "=" + id).toString();
                ServiceLocator.getInstance().lookup(BO.CustomerWithOutBusiness).batchUpdate(json,
                        new String[] { CustomerMetaData.localId }, new String[] { String.valueOf(id) });
            }
            operationLogService.generate(row);
            operationLogService.writeMsg2BigData(row.getId(), BI.CUSTOMER_ADD, 0);
        } else {
                if (StringUtils.isEmpty(row.getLocalId())) {
                    Long id = row.getId();
                    String json = JsonQuery.getInstance().setCriteriaStr(SC.id + "=" + id).toString();
                    ServiceLocator.getInstance().lookup(BO.CustomerWithOutBusiness).batchUpdate(json,
                            new String[] { CustomerMetaData.localId }, new String[] { String.valueOf(id) });
                
                operationLogService.generate(row);
            }
        }
    }

    /**
     * 新增或变更客户信息
     * 
     * @param customer 客户信息（端参数）
     * @param toOrig 源（scanned:名片扫描）
     * @return CustomerRow 新增或变更后客户信息（DB）
     */
    private ICustomerRow upsertCustomer(LinkedHashMap<String, Object> customer, String toOrig) {
        ICustomerRow customerRow = getFinalCustomerRow(customer, toOrig);

        this.upsert(customerRow);

        customerRow = this.query(customerRow.getId());
        return customerRow;
    }

    /**
     * 获取根据参数设置后的联系人ROW对象。
     * 
     * @param contact 联系人信息（端参数）
     * @param boHome
     * @return ContactRow 联系人ROW对象
     */
    private ICustomerRow getFinalCustomerRow(LinkedHashMap<String, Object> customer, String toOrgin) {
        ICustomerRow customerRow = getCustomerRow(customer, toOrgin);

        // 将【客户信息（端参数）】设置到【客户ROW对象】里
        this.populateBORow(customer, customerRow);

        // 设置客户ROW对象的通用字段
        setCustomerRowCommonItem(customerRow);
        // 设置OWNER  
        Long ownerId = customer.get(CustomerMetaData.owner)==null?null:Long.parseLong(customer.get(CustomerMetaData.owner).toString());
        setOwner(customerRow, ownerId);
        // 检查客户对象
        checkCustomerRow(customerRow);
        return customerRow;
    }

    /**
     * 根据端参数中id，返回变更客户ROW对象或新增客户ROW对象。
     * 
     * @param customer 客户信息（端参数）
     * @param toOrgin 源（scanned:名片扫描）
     * @param boHome
     * @return CustomerRow 客户对象（数据库中客户对象或空白数据对象）
     */
    private ICustomerRow getCustomerRow(LinkedHashMap<String, Object> customer, String toOrgin) {
        Long id = customer.containsKey(SC.id) ? ConvertUtil.toLong(customer.get(SC.id).toString()) : 0L;
        String name = (String) customer.get(CustomerMetaData.name);
        String phone = (String) customer.get(CustomerMetaData.phone);
        if (id != null && id > 0) { // 编辑
            return getCustomerRowForUpdate(id, name, phone);
        } else { // 新增
        	String localId = null;
        	if(customer.get(CustomerMetaData.localId)!=null){
        		localId = customer.get(CustomerMetaData.localId).toString();
        	}
        	return getCustomerRowForAdd(localId, name, phone,toOrgin);

        }
    }

    /**
     * 根据ID获取用来变更的CustomerRow对象 如果没有该客户的数据权限抛异常 如果数据库里没有该客户信息抛异常
     * 
     * @param id 客户ID
     * @param customerBoHome
     * @param session
     * @return customer CustomerRow对象
     */
    private ICustomerRow getCustomerRowForUpdate(Long id, String name, String phone) {
        ICustomerRow customer = this.findByIdWithAuth(id);
        Assert.notNull(customer, "app.customer.object.notexist");
        // 如果客户名称和数据库中重复则抛异常。
        Map<String, Object> sameCustomer = getExistsCustomer(id, name, phone);
        Assert.customerRepeat(sameCustomer);
        return customer;
    }

    /**
     * 根据localId来获取新增用的客户ROW对象。 如果数据库中已经存在则返回数据库中对象。 如果数据库中不存在则返回空白客户对象
     * 
     * @param localId 本地ID
     * @param name 客户名称
     * @param toOrgin 源（scanned:名片扫描）
     * @param customerBoHome
     * @return customer CustomerRow对象
     */
    private ICustomerRow getCustomerRowForAdd(String localId, String name, String phone, String toOrgin) {
        ICustomerRow customer = null;
        if (StringUtils.isEmpty(localId)) {
            return this.createRow();
        }
        customer = getCustomerByLocalId(localId);
        if (customer == null) {
            if (StringUtils.isNotEmpty(name)) {
                Map<String, Object> sameCustomer = getExistsCustomer(0L, name, phone);
                if ("scanned".equals(toOrgin)) {// 如果是名片扫描
                    if (sameCustomer != null) {// 如果是空 继续往下走
                        try {
                            // 如果不空 代表全局有记录 按id可查询代表有权限 如果异常 代表无权限 抛出扫描名片客户重复的移仓
                            customer = this.findByIdWithAuth((Long) sameCustomer.get("id"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Assert.customerRepeat(sameCustomer);
                        }
                    }
                } else {
                    Assert.customerRepeat(sameCustomer);
                }
            }
            customer = this.createRow();
        }
        return customer;
    }

    /**
     * 获取客户ROW对象
     */
    private ICustomerRow getCustomerByLocalId(String localId) {
        return _getCustomerByLocalId(localId);
    }

    /**
     * 根据入参：本地ID 获取客户信息，如果获取不到则返回NULL
     * 
     * @param localId 本地ID
     * @return IBusinessObjectRow 第一条查询结果或NULL
     */
    private ICustomerRow _getCustomerByLocalId(String localId) {
        String queryStr = JsonQuery.getInstance().setCriteriaStr(CustomerMetaData.localId + " ='" + localId + "' and "+SC.id+ ">"+SQ.HSY_SEED_DATA).toString();
        IBusinessObjectRowSet customerRowSet = this.query(queryStr);
        if (customerRowSet != null && customerRowSet.getRows() != null && customerRowSet.getRows().size() > 0) {
            return (ICustomerRow) customerRowSet.getRow(0);
        }

        return null;
    }

    /**
     * 检查客户对象
     * 
     * @param row 客户ROW对象
     */
    private void checkCustomerRow(ICustomerRow row) {
        // 检查客户名称和OWNER
        Assert.hasLength(row.getName(), "app.customer.name.required");
        Assert.notNull(row.getOwner(), "app.customer.owner.notexist");
    }

    /**
     * 设置客户ROW对象的通用字段
     * 
     * @param row 客户ROW对象
     */
    private void setCustomerRowCommonItem(ICustomerRow row) {
        // 设置更新时间
        row.setModifiedTime(DateUtil.getNowDateTime());
        // 设置全拼、简拼
        String name = row.getName();
        if (StringUtils.isNotEmpty(name)) {
            row.setFullSpell(PinyinUtil.hanziToPinyinFull(row.getName(), true));
            row.setSimpleSpell(PinyinUtil.hanziToPinyinSimple(row.getName(), false));
        }
        Phone phone = row.getPhone();
        if (phone != null) {
            String phoneNum = phone.getPhoneNumber();
            if (StringUtils.isNotEmpty(phoneNum)) {
                row.setEffectivePhone(PhoneNumUtil.getEffectivePhoneNumString(phoneNum, true, false));
            }
        }
    }

    @Override
    public Map<String, Object> getExistsCustomer(Long customerId, String name, String phone) {
        Map<String, Object> info = new HashMap<String, Object>();

        if (StringUtils.isNotEmpty(name) || phone != null) {
        	if(customerId!=null){
            	ICustomerRow oldRow= this.query(customerId);
            	if(oldRow!=null&&StringUtils.isEmpty(name)){
            		name = oldRow.getName();
            	}
            	if(oldRow!=null&&StringUtils.isEmpty(phone)&&oldRow.getPhone()!=null){
            		phone = oldRow.getPhone().getPhoneNumber();
            	}
        	}

        	List<Object[]> result = _getExistsCustomer(customerId, name, phone);
          //  List<Object[]> result = data.getRawData();
            if (result != null && result.size() > 0) {
                Object[] customerObj = result.get(0);
                Long createdBy = (Long) customerObj[1]; // createdby
                User user = EnterpriseUtil.getUserById(createdBy);
                String userName = null;
                if (user != null) {
                    userName = user.getName();
                }
                info.put("createdByName", userName);
                info.put("createdDate", customerObj[3]);
                info.put("id", customerObj[0]);// id
                String customerName = (String) customerObj[4];
                Phone phoneObject = (Phone) customerObj[5];
                String phoneNum = "";
                if (phoneObject != null) {
                    phoneNum = phoneObject.getPhoneNumber();
                }
                boolean repeatPhone = false;
                if (StringUtils.isNotEmpty(phone) && phone.length() > 10) {
                    repeatPhone = phoneNum.contains(phone);
                }
                boolean repeatName = false;
                if (StringUtils.isNotEmpty(name) && name.equals(customerName)) {
                    repeatName = true;
                }
                if (repeatName && repeatPhone) {
                    info.put("repeat", "all");
                } else if (repeatName) {
                    info.put("repeat", "name");
                } else if (repeatPhone) {
                    info.put("repeat", "phone");
                }
                return info;
            }
        }
        return null;
    }

    private List<Object[]> _getExistsCustomer(Long customerId, String name, String phone) {
        String cond1 = "";
        if (StringUtils.isNotEmpty(name)) {
        	name = name.replaceAll("'", "''");
            cond1 += "(" + CustomerMetaData.name + "='" + name + "')";
        }
        if (phone != null) {
            phone = phone.replace("，", ",");
            String[] phoneArray = phone.split(",");
            for (int i = 0; i < phoneArray.length; i++) {
                if (phoneArray[i] != null) {
                    String phoneNum = phoneArray[i].trim();
                    if (phoneNum.length() > 10) {
                        if (StringUtils.isNotEmpty(cond1)) {
                            cond1 += " OR ";
                        }
                        cond1 += "(" + CustomerMetaData.phone + " like '%" + phoneNum + "%')";
                    }
                }
            }
        }

        if (StringUtils.isNotEmpty(cond1)) {
            cond1 = " AND (" + cond1 + ")";
        }

        String cond2 = "";
        if (null != customerId && customerId > 0) {
            cond2 = " AND (" + SC.id + "!=" + customerId + ")";
        }

        String jsonQuerySpec = JsonQuery.getInstance()
                .setCriteriaStr(SC.id+ ">"+SQ.HSY_SEED_DATA + cond1 + cond2)
                .addFields(SC.id, SC.createdBy, SC.owner, SC.createdDate, CustomerMetaData.name, CustomerMetaData.phone)
                .setMaxResult(1).toString();
		/*Criteria criteria = Criteria.AND();
		criteria.addChild(Criteria.OR().emptysdf(SC.isDeleted).eq(SC.isDeleted, false));
		Criteria condition = Criteria.OR()ds;
		
		if(StringUtils.isNotEmpty(name)){
			condition.eq(CustomerMetaData.name, name);
		}
		if(phone != null){
			phone = phone.replace("，", ",");
			String[] phoneArray = phone.split(",");
			for(int i = 0 ;i<phoneArray.length;i++){
			//for(String effectivePhone : effectivePhones){
				if(phoneArray[i] != null){
					String phoneNum = phoneArray[i].trim();
					if(phoneNum.length() > 10){
						condition.like(CustomerMetaData.phone,phoneNum);
					}
				}
			}
		}
		criteria.addChild(condition);
		if (null != customerId && customerId > 0) {
			criteria.ne(SC.id, customerId);
		}
        String jsonQuerySpecTemp =JsonQueryBuilder.getInstance().addCriteria(criteria)
        		.addFields(SC.id, SC.createdBy, SC.owner, SC.createdDate, CustomerMetaData.name, CustomerMetaData.phone)
        		.setFirstResult(0)
        		.setMaxResult(1).toJsonQuerySpec();*/
        List<Object[]> data = privilegedQueryNoTransform(jsonQuerySpec);
        return data;
    }

    /**
     * <p>
     * 按用户统计客户数量
     * </p>
     */
    @Override
    public Map<Long, Long> getCustomerCountByUser() {
        Map<Long, Long> countMap = new HashMap<Long, Long>();
        String jsonReportSpec = JsonQueryBuilder.getInstance().addFields(UserMetaData.customerCount, SC.owner)
                .addCount(SC.id, UserMetaData.customerCount).addGroup(SC.owner).toJsonReportSpec();
        Criteria criteria = Criteria.OR();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        //criteria.empty(SC.isDeleted).eq(SC.isDeleted, false);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        ReportingResult result = this.getBusinessObjectHome().getReportData(jsonQuerySpec, jsonReportSpec);
        for (Object[] row : result.getDataRows()) {
            Long userId = (Long) row[0];
            Long count = (Long) row[1];
            countMap.put(userId, count);
        }
        return countMap;
    }

    /**
     * <p>
     * 回写客户状态
     * </p>
     */
    @Override
    public void updateCustomerStatus(ICustomerRow customerRow, IWorkRecordRow workRecordRow, boolean isInsert) {
        if (customerRow == null)
            return;
        // 按contactTime倒排序
        Long lastId = customerRow.getLastRecord();
        IWorkRecordRow lastRecord = null;
        if (lastId != null) {
            lastRecord = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).query(lastId);
        }
        // 新增，修改最后一条，修改
        if (isInsert == true || lastId == null || lastId.equals(workRecordRow.getId())
                || lastRecord.getContactTime().before(workRecordRow.getContactTime())) {
            customerRow.setLastRecord(workRecordRow.getId());
            if (workRecordRow.getStatus() != null) {
                customerRow.setStatus(workRecordRow.getStatus());
            } else if (customerRow.getStatus() != null) {
                Assert.notNull(workRecordRow.getStatus(), "app.workRecord.status.required");
            }
            this.upsert(customerRow);
        }
    }

    @Override
    public Integer countCustomers(String params) {
        String cqlQueryString = "select count(id) as cnt from " + getBusinessObjectHome().getDefinition().getId()
                + " c ";
        cqlQueryString += " where " +SC.id+ ">"+SQ.HSY_SEED_DATA;
        if (StringUtils.isNotEmpty(params)) {
            cqlQueryString += " and c.name like :params or c.phone like :params ";
        }
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(params)) {
            paraMap.put("params", "%" + params + "%");
        }

        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        if (result != null && result.size() > 0) {
            return new Integer(result.get(0).get("cnt").toString());
        }
        return 0;
    }

    @Override
    public List<Row> customerForCombox(String param, Integer pageNo, Integer pageSize) {
        List<Row> customers = new ArrayList<Row>();
        List<Map<String, Object>> objects = findCustomerNames(param, pageNo, pageSize);
        for (Map<String, Object> object : objects) {
            Row customer = new Row();
            customer.put(SC.id, object.get("id"));
            customer.put(CustomerMetaData.name, object.get("name"));
            Map<String, Object> statusMap = new HashMap<String, Object>();
            statusMap.put("value", object.get("status"));
            customer.put(CustomerMetaData.status, statusMap);
            customers.add(customer);
        }
        return customers;
    }

    private List<Map<String, Object>> findCustomerNames(String params, Integer pageNo, Integer pageSize) {
        String cqlQueryString = "select id, name,status from " + getBusinessObjectHome().getDefinition().getId()
                + " c where  "+SC.id+ ">"+SQ.HSY_SEED_DATA;
        if (StringUtils.isNotEmpty(params)) {
            cqlQueryString += " and (c.name like :params or c.phone like :params or lower(c.fullSpell) like :params or lower(c.simpleSpell) like :params) ";
        }
        cqlQueryString += "order by c.lastModifiedDate Desc";

        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        if (StringUtils.isNotEmpty(params)) {
            params = params.toLowerCase();
            paraMap.put("params", "%" + params + "%");
        }
        List<Map<String, Object>> customers = runCQLQuery(cqlQueryString, paraMap, (pageNo - 1) * pageSize, pageSize);

        return customers;
    }

    @Override
    public List<Long> getCustomerIdsByCondtion(String criteria) {
        Map<String, Object> paraMap;
        if (criteria == null || criteria.equals("") || criteria.equals("\"{}\"")) {
            paraMap = new HashMap<String, Object>();
        } else {
            paraMap = dataManager.jsonStringToMap(criteria);
        }
        Long start = System.currentTimeMillis();
        List<Long> ids = _getCustomerIdsByCondtion(paraMap);
        logger.info("queryCustomerIds use time = " + (System.currentTimeMillis() - start) + " ms");
        return ids;
    }

    private void getEntityFieldValue(Criteria criteria, List<Object> fieldValues, String fieldname, IField field) {
        FieldTypeEnum type = field.getType();
        // 空字符串处理
        if (fieldValues.get(0) != null && fieldValues.get(0).toString().equals("none")) {
            criteria.empty(fieldname);
        } else if (fieldValues.size() == 1 && StringUtils.isEmpty(fieldValues.get(0).toString())) {
            criteria.empty(fieldname);
        } // 枚举类型。默认多选
        else if (FieldTypeEnum.CSP_ENUM.value().equals(type.value())) {
            criteria.in(fieldname, fieldValues.toArray());
        } // 日期类型
        else if (FieldTypeEnum.DATE.value().equals(type.value())) {
            String dateStr = fieldValues.get(0).toString();
            List<Timestamp> ts = PeriodUtil.getTimePeriod(dateStr);
            Timestamp startTS = ts.get(0);
            Timestamp endTS = ts.get(1);
            criteria.ge(fieldname, startTS);
            criteria.le(fieldname, endTS);
        } else if (FieldTypeEnum.INTEGER.value().equals(type.value())) {
            String dateStr = fieldValues.get(0).toString();
            Long minNum = null;
            Long maxNum = null;
            if (dateStr.indexOf(SPLITFLAG) != -1) {
                try {
                    if (dateStr.indexOf(SPLITFLAG) + 2 == dateStr.length()) {
                        minNum = Long.parseLong(dateStr.substring(0, dateStr.indexOf(SPLITFLAG)));
                        maxNum = Long.MAX_VALUE;
                    } else if (dateStr.indexOf(SPLITFLAG) == 0) {
                        minNum = Long.MIN_VALUE;
                        maxNum = Long.parseLong(
                                dateStr.substring(dateStr.indexOf(SPLITFLAG) + SPLITFLAG.length(), dateStr.length()));
                    } else {
                        minNum = Long.parseLong(dateStr.substring(0, dateStr.indexOf(SPLITFLAG)));
                        maxNum = Long.parseLong(
                                dateStr.substring(dateStr.indexOf(SPLITFLAG) + SPLITFLAG.length(), dateStr.length()));
                    }
                    criteria.ge(fieldname, minNum);
                    criteria.le(fieldname, maxNum);
                } catch (Exception e) {
                    throw new AppException("app.common.params.invalid");
                }
            } else {
                throw new AppException("app.common.params.invalid");
            }
        }
    }

    private Map<String, Timestamp> getFollowTime(String timeType) {
        Map<String, Timestamp> timeMap = new HashMap<String, Timestamp>();
        Date followstart = null;
        Date followend = null;
        if ("today".equalsIgnoreCase(timeType)) {
            followstart = new Date();
        }
        if ("currentWeek".equalsIgnoreCase(timeType)) {
            followstart = DateUtil.getCurrentWeekStart();
        } else if ("currentMonth".equalsIgnoreCase(timeType)) {
            followstart = DateUtil.getCurrentMonthStart();
        } else if ("lastWeek".equalsIgnoreCase(timeType)) {
            followstart = DateUtil.getLastWeekStart();
            followend = DateUtil.getLastWeekEnd();
        } else if ("lastMonth".equalsIgnoreCase(timeType)) {
            followstart = DateUtil.getLastMonthStart();
            followend = DateUtil.getLastMonthEnd();
        } else if ("threeMonths".equalsIgnoreCase(timeType)) {
            followstart = DateUtil.getLastThreeMonthStart();
        } else if (timeType.indexOf(SPLITFLAG) != -1) {
            String startDate = null;
            String endDate = null;
            timeType = timeType.trim();
            if (timeType.indexOf(SPLITFLAG) + 2 == timeType.length()) {
                startDate = timeType.substring(0, timeType.indexOf(SPLITFLAG));
                endDate = DateUtil.getDateString(new Date());
            } else if (timeType.indexOf(SPLITFLAG) == 0) {
                startDate = "1900-01-01";
                endDate = timeType.substring(timeType.indexOf(SPLITFLAG) + SPLITFLAG.length(), timeType.length());
            } else {
                startDate = timeType.substring(0, timeType.indexOf(SPLITFLAG));
                endDate = timeType.substring(timeType.indexOf(SPLITFLAG) + SPLITFLAG.length(), timeType.length());
            }
            followstart = DateUtil.getDateByString(startDate);
            followend = DateUtil.getDateByString(endDate);
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (followend == null)
            followend = new Date();
        Timestamp startTS = Timestamp.valueOf(df.format(followstart) + " 00:00:00.001");
        Timestamp endTS = Timestamp.valueOf(df.format(followend) + " 23:59:59.999");

        timeMap.put("start", startTS);
        timeMap.put("end", endTS);
        return timeMap;
    }

    @Override
    public Object deleteByIds(List<Long> ids, String reason) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        String customerName = CustomerMetaData.EOName;
        Long start = System.currentTimeMillis();
        //logicDeleteByIds(ids);
        logger.info("logicDeleteByIds use time = " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();

        OperationLogServiceItf operationLogService = ServiceLocator.getInstance().lookup(OperationLogServiceItf.class);
        operationLogService.saveBatchDeleteLog(reason, ids);
        logger.info("saveBatchDeleteLog use time = " + (System.currentTimeMillis() - start) + " ms");
        start = System.currentTimeMillis();
        operationLogService.generateBatch(ids, customerName, OP.DELETE);
        logger.info("generateBatch use time = " + (System.currentTimeMillis() - start) + " ms");

        start = System.currentTimeMillis();
        RecyclableBinManager.putBatch(customerName, ids, reason);
        logger.info("generateBatch use time = " + (System.currentTimeMillis() - start) + " ms");
        result.put("success", ids.size());
        return result;
    }

    /**
     * 逻辑删除entity（需要检查数据权限）
     * 
     * @param id entityID
     * @param session
     */
    private void logicDeleteByIds(List<Long> ids) {
    	for(Long id:ids){
    		deleteRowWithRecycle(id);
    	}
    }

    @Override
    public ICustomerRow getCustomerById(Long id) {
        ICustomerRow row = query(id);
        return row;
    }

    @Override
    public void deleteCustomer(Long id) {
        checkAuthById(id, DataAuthPrivilege.DELETE);
        ICustomerRow row = this.findByIdWithAuth(id);  
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(row, OP.DELETE);
        deleteRowWithRecycle(id);
    }

    @Override
    public RowSet customerList(String criteria, Integer pageNo, Integer pageSize) {
        Map<String, Object> paraMap;
        if (criteria == null || criteria.equals("")) {
            paraMap = new HashMap<String, Object>();
        } else {
            paraMap = dataManager.jsonStringToMap(criteria);
        }
        Long start = System.currentTimeMillis();
        RowSet rowSet = _customerList(paraMap, pageNo, pageSize);
        logger.info("customerList use time = " + (System.currentTimeMillis() - start) + " ms ");
        start = System.currentTimeMillis();
        getCustomrListRowSet(rowSet);
        logger.info("getCustomrListRowSet use time = " + (System.currentTimeMillis() - start) + " ms ");
        return rowSet;
    }

    @Override
    public Map<Long, Set<Long>> customersRefGrants(List<Long> customerIds) {
        Map<Long, Set<Long>> grantsMap = new HashMap<Long, Set<Long>>();
        if (customerIds == null || customerIds.size() == 0) {
            return grantsMap;
        }
        if(customerIds.size()>2000){
        	return ServiceLocator.getInstance().lookup(GrantServiceItf.class).getAllGrantsByCustomerIdS(customerIds);
        }
        return ServiceLocator.getInstance().lookup(GrantServiceItf.class).getGrantsByCustomerIdS(customerIds);
    }

    private void getCustomrListRowSet(RowSet rowSet) {
        List<Row> rowlist = rowSet.getItems();
        HashMap<Long, Row> rowMap = new HashMap<Long, Row>();
        if (rowlist == null || rowlist.size() == 0)
            return;
        List<Long> ids = new ArrayList<Long>();
        for (Row row : rowlist) {
            Long customerId = (Long) row.get("id");
            rowMap.put(customerId, row);
            ids.add(customerId);
        }
        if (ids.size() > 0) {
            Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
            List<Long> followIds = ServiceLocator.getInstance().lookup(CustomerFollowServiceItf.class).isFollow(userId,
                    ids);
            Long start = System.currentTimeMillis();
            HashMap<Long, ArrayList<Row>> contactMap = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .findContactByCustomerIds(ids);
            logger.info("findContactByCustomerIds use time = " + (System.currentTimeMillis() - start) + " ms ");
            start = System.currentTimeMillis();
            Map<Long, Set<Long>> shareMap = customersRefGrants(ids);
            logger.info("customersRefGrants use time = " + (System.currentTimeMillis() - start) + " ms ");
            Set<Long> shareCustomerIds = shareMap.keySet();
            Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
            for (Long customerId : ids) {
                List<Row> contacts = contactMap.get(customerId);
                Row row = rowMap.get(customerId);
                row.put("contacts", contacts);
                // 加入关注标志
                if (followIds.contains(customerId)) {
                    row.put("follow", true);
                } else {
                    row.put("follow", false);
                }
                if (shareCustomerIds.contains(customerId)) {
                    Set<Long> userIds = shareMap.get(customerId);
                    if (userIds.contains(currUserId)) {
                        row.put("customerType", "grant");
                    } else {
                        row.put("customerType", "othergrant");
                    }
                } else {
                    Row owner = (Row) row.get("owner");
                    Long ownerId = owner.getLong("id");
                    if (currUserId.equals(ownerId)) {
                        row.put("customerType", "mine");
                    } else {
                        row.put("customerType", "mine");
                    }

                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Criteria _criteria(Map<String, Object> paraMap, Long currUserId) {
        if (paraMap == null)
            return null;
        Criteria criteria = Criteria.AND();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA); 
        // 未删除的
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        // 查询框搜索
        if (paraMap.containsKey("searchValue") && StringUtils.isNotEmpty(paraMap.get("searchValue").toString())) {
            String searchtext = paraMap.get("searchValue").toString();
            try {
                // +号 在decode后会变成空格
                searchtext = searchtext.replace("+", "%2B");
                searchtext = java.net.URLDecoder.decode(searchtext, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Criteria earchTextCriteria = Criteria.OR().like(CustomerMetaData.address, searchtext)
                    .like(CustomerMetaData.name, searchtext).like(CustomerMetaData.phone, searchtext)
                    .like(CustomerMetaData.fullSpell, searchtext).like(CustomerMetaData.simpleSpell, searchtext);
            // 关联联系人条件过滤
            List<Long> cusotmerIds = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .getCustomerIdByContactSearch(searchtext);
            if (cusotmerIds != null && cusotmerIds.size() > 0) {
                earchTextCriteria.in(SC.id, cusotmerIds.toArray());
            }
            criteria.addChild(earchTextCriteria);
        }
        if (paraMap.containsKey("userRole")) {
            List<Object> userRole = (List<Object>) paraMap.get("userRole");
            String roleStr = (String) userRole.get(0);
            if ("supervisor".equals(roleStr)) {
                List<Object> userIds = (List<Object>) paraMap.get("owner");
                Long supervisorId = ConvertUtil.toLong(userIds.get(0).toString());
                List<Long> users = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                        .getAllEnableSubordinate(supervisorId);
                ArrayList<Long> userList = new ArrayList<Long>();
                userList.addAll(users);
                paraMap.put("owner", userList);
            }
        }

        // 关注的客户
        if (paraMap.containsKey("followed")) {
            List<Long> customerIds = ServiceLocator.getInstance().lookup(CustomerFollowServiceItf.class)
                    .getCurrUserFollowCustomerIds();
            if (customerIds.size() == 0) {
                customerIds.add(new Long(-1));
            }
            List<Object> groups = (List<Object>) paraMap.get("followed");
            String followed = (String) groups.get(0);
            if ("followed".equals(followed)) {
                criteria.in(SC.id, customerIds.toArray());
            } else if ("unfollow".equals(followed)) {
                Criteria followCriteria = Criteria.NOT().in(SC.id, customerIds.toArray());
                criteria.addChild(followCriteria);
            }
        }
        // 跟进时间
        if (paraMap.containsKey("followTime")) {
            List<Object> groups = (List<Object>) paraMap.get("followTime");
            String timeType = (String) groups.get(0);
            Map<String, Timestamp> timeMap = getFollowTime(timeType);
            criteria.between("lastRecordContactTime", timeMap.get("start"), timeMap.get("end"));
            // lastRecordContactTime
        }
        // 跟进时长
        if (paraMap.containsKey("noFollowTime")) {
            List<Object> groups = (List<Object>) paraMap.get("noFollowTime");
            String timeType = (String) groups.get(0);
            Date endDate = new Date();
            Calendar cl = Calendar.getInstance();
            cl.setTime(endDate);
            // 距离今天，一个月内数据
            if (timeType.equals("aMonth")) {
                cl.add(Calendar.MONTH, -1);
            }
            if (timeType.equals("threeMonth")) {
                cl.add(Calendar.MONTH, -3);
            }
            if (timeType.equals("helfYear")) {
                cl.add(Calendar.MONTH, -6);
            }
            if (timeType.equals("oneYear")) {
                cl.add(Calendar.YEAR, -1);
            }
            Date startDate = cl.getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Timestamp endTS = Timestamp.valueOf(df.format(startDate) + " 00:00:00.001");
            // 工作记录联系时间为空，客户创建时间小于endTS
            Criteria unfollowCriteria = Criteria.OR()
                    .addChild(Criteria.AND().empty("lastRecordContactTime").lt(SC.createdDate, endTS))
                    // 工作记录联系时间不为空，工作记录联系时间小于TS
                    .addChild(Criteria.AND().addChild(Criteria.NOT().empty("lastRecordContactTime"))
                            .lt("lastRecordContactTime", endTS));
            criteria.addChild(unfollowCriteria);
        }
        // 客户类型
        if (paraMap.containsKey("customerType")) {
            List<Object> groups = (List<Object>) paraMap.get("customerType");
            String customerType = (String) groups.get(0);
            Long chooseId = 0L;
            if (currUserId == null) {
                List<Object> chooseUser = (List<Object>) paraMap.get("userId");
                Assert.notNull(chooseUser, "app.customer.query.chooseUser");
                chooseId = ConvertUtil.toLong(chooseUser.get(0).toString());
            } else {
                chooseId = currUserId;
            }
            if ("mine".equals(customerType)) {
                criteria.eq(SC.owner, chooseId);
            } else if ("grant".equals(customerType)) {
                List<Long> customerIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                        .getCustomerIdsByUserId(chooseId);
                if (customerIds.size() == 0) {
                    customerIds.add(new Long(-1));
                }
                criteria.in(SC.id, customerIds.toArray());
            }
        } else if (currUserId == null && paraMap.containsKey("userId")) {
            List<Object> chooseUser = (List<Object>) paraMap.get("userId");
            Assert.notNull(chooseUser, "app.customer.query.chooseUser");
            Long chooseId = ConvertUtil.toLong(chooseUser.get(0).toString());
            List<Long> customerIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                    .getCustomerIdsByUserId(chooseId);
            if (customerIds.size() == 0) {
                customerIds.add(new Long(-1));
            }
            criteria.addChild(Criteria.OR().eq(SC.owner, chooseId).in(SC.id, customerIds.toArray()));
        }

        if (paraMap.containsKey("timetype")) {
            Date start = null, end = null;
            Map<String, Object> map = (Map<String, Object>) paraMap.get("timetype");
            String startDate = map.get("start").toString();
            String endDate = map.get("end").toString();
            start = DateUtil.getDateByString(startDate);
            end = DateUtil.getDateByString(endDate);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            Timestamp startTS = Timestamp.valueOf(df.format(start) + " 00:00:00.001");
            Timestamp endTS = Timestamp.valueOf(df.format(end) + " 23:59:59.999");
            criteria.between(SC.createdDate, startTS, endTS);

        }
        // 业务员
        if (paraMap.containsKey("owner")) {
            // 兼容按客户ID移交
            if (paraMap.get("owner") instanceof java.util.ArrayList) {
                List<Object> userIds = (List<Object>) paraMap.get("owner");
                if (userIds != null && userIds.size() == 1 && userIds.get(0).equals("-1")) {
                    paraMap.remove("owner");
                } else {
                    if (userIds != null) {
                        criteria.in(SC.owner, userIds.toArray());
                    }
                }
            } else {
                List<Object> userIds = (List<Object>) paraMap.get("owner");
                if (userIds != null && userIds.size() == 1 && userIds.get(0).equals("-1")) {
                    paraMap.remove("owner");
                } else if (userIds != null) {
                    criteria.in(SC.owner, userIds.toArray());
                }
            }

        }
        // 自定义字段查询
        Set<String> fields = paraMap.keySet();
        IEntity entityDefine = this.getBusinessObjectHome().getDefinition().getPrimaryEntity();
        for (String fieldname : fields) {
            if ("searchValue".equals(fieldname))
                continue;
            if ("owner".equals(fieldname))
                continue;
            IField field = entityDefine.getField(fieldname);
            if (field == null) {
                continue;
            } else {
                List<Object> fieldValues = new ArrayList<Object>();
                if (paraMap.get(fieldname) instanceof Map) {
                    fieldValues.add(paraMap.get(fieldname));
                } else {
                    fieldValues = (List<Object>) paraMap.get(fieldname);
                }
                getEntityFieldValue(criteria, fieldValues, fieldname, field);
            }
        }

        return criteria;
    }

    private List<Long> _getCustomerIdsByCondtion(Map<String, Object> paraMap) {
        Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        Criteria criteria = _criteria(paraMap, currUserId);
        if (criteria == null)
            return null;

        JsonQueryBuilder queryBuilder = JsonQueryBuilder.getInstance().addFields(SC.id, SC.lastModifiedDate)
                .addCriteria(criteria).addOrderDesc(SC.lastModifiedDate).addOrderAsc(SC.id);

        IBusinessObjectRowSet customerIdSet = ServiceLocator.getInstance().lookup(BO.CustomerForQueryList)
                .queryAll(queryBuilder.toJsonQuerySpec());

        List<Long> ids = new ArrayList<Long>();
        if (customerIdSet != null) {
            for (IBusinessObjectRow row : customerIdSet.getRows()) {
                ids.add((Long) row.getFieldValue(SC.id));
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    private RowSet _customerList(Map<String, Object> paraMap, Integer pageNo, Integer pageSize) {
        Criteria criteria = _criteria(paraMap, null);
        if (criteria == null)
            return null;

        RowSet rowSet = new RowSet();

        BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> customerForQueryListService = ServiceLocator
                .getInstance().lookup(BO.CustomerForQueryList);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addFields(SC.id, SC.lastModifiedDate)
                .addCriteria(criteria).setFirstResult((pageNo - 1) * pageSize).setMaxResult(pageSize)
                .addOrderDesc(SC.lastModifiedDate).addOrderAsc(SC.id).toJsonQuerySpec();
        Long start = System.currentTimeMillis();
        Integer count = customerForQueryListService
                .getRowCount(JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec());
        logger.info("CustomerForCount use time = " + (System.currentTimeMillis() - start) + " ms ");
        rowSet.setTotal(count);
        start = System.currentTimeMillis();
        ICustomerForQueryListRowSet customerIdSet = (ICustomerForQueryListRowSet) customerForQueryListService
                .query(jsonQuerySpec);
        logger.info("CustomerForQueryListRowSet use time = " + (System.currentTimeMillis() - start) + " ms ");
        List<Long> ids = new ArrayList<Long>();
        if (customerIdSet != null && customerIdSet.getRows() != null) {
            for (IBusinessObjectRow row : customerIdSet.getRows()) {
                ids.add((Long) row.getFieldValue(SC.id));
            }
        }
        if (ids.size() == 0)
            return rowSet;
        Criteria criteriaIds = Criteria.AND();
        criteriaIds.in(SC.id, ids.toArray());
        String jsonQueryByIds = JsonQueryBuilder.getInstance()
                .addFields(CustomerMetaData.name, CustomerMetaData.status, CustomerMetaData.lastRecord, SC.id,
                        SC.lastModifiedDate, SC.owner, "userName", "userUserId")
                .addCriteria(criteriaIds).addOrderDesc(SC.lastModifiedDate).addOrderAsc(SC.id).toJsonQuerySpec();
        ICustomerForQueryListRowSet customerRowSet = (ICustomerForQueryListRowSet) customerForQueryListService
                .query(jsonQueryByIds);
        try {
            List<Row> rowList = new ArrayList<Row>();
            start = System.currentTimeMillis();
            for (ICustomerForQueryListRow customerQueryRow : customerRowSet.getCustomerForQueryListRows()) {
                Row queryRow = new Row();
                queryRow.put(CustomerMetaData.name, customerQueryRow.getName());
                if (customerQueryRow.getStatus() != null) {
                    Row status = new Row();
                    status.put("value", customerQueryRow.getStatus().getValue());
                    status.put("label", customerQueryRow.getStatus().getLabel());
                    queryRow.put(CustomerMetaData.status, status);
                }
                if (customerQueryRow.getLastRecord() != null && customerQueryRow.getFieldValue("lastRecord") != null) {
                    Map<String, Object> lastRecordValue = (Map<String, Object>) customerQueryRow
                            .getFieldValue("lastRecord");
                    if (lastRecordValue != null) {
                        Object contactTime = lastRecordValue.get(WorkRecordMetaData.contactTime);
                        if (contactTime != null) {
                            lastRecordValue.put(SC.lastModifiedDate, contactTime);
                        }

                        IWorkRecordRow row = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                                .createRow();
                        row.setContent(lastRecordValue.get(WorkRecordMetaData.content) == null ? ""
                                : lastRecordValue.get(WorkRecordMetaData.content).toString());
                        lastRecordValue.put(WorkRecordMetaData.content, row.getContent());
                        Map<String, Object> ownerMap = new HashMap<String, Object>();
                        ownerMap.put(UserMetaData.name, customerQueryRow.getUserName());
                        ownerMap.put(SC.id, customerQueryRow.getUserUserId());
                        lastRecordValue.put(SC.owner, ownerMap);
                    }
                    queryRow.put(CustomerMetaData.lastRecord, lastRecordValue);
                }

                queryRow.put(SC.id, customerQueryRow.getId());
                Row onwerRow = new Row();
                onwerRow.put(SC.owner, customerQueryRow.getOwner());
                queryRow.put(SC.owner, onwerRow);
                rowList.add(queryRow);
            }

            logger.info("toRowList use time = " + (System.currentTimeMillis() - start) + " ms ");
            if (rowList != null)
                rowSet.setItems(rowList);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return rowSet;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void shareCustomer(Map<String, Object> value) {
        List<Object> _sharedUserIds = (List<Object>) value.get("sharedUserIds");
        List<Object> _customerIds = (List<Object>) value.get("customerIds");

        List<DataAuthPrivilege> privileges = new ArrayList<DataAuthPrivilege>();
        privileges.add(DataAuthPrivilege.UPDATE);
        privileges.add(DataAuthPrivilege.SELECT);

        if (privileges == null || privileges.isEmpty()) {
            throw new AppException("app.sharecustomer.para.error");
        }

        if (_sharedUserIds == null || _sharedUserIds.isEmpty()) {
            throw new AppException("app.sharecustomer.para.error");
        }

        if (_customerIds == null || _customerIds.isEmpty()) {
            throw new AppException("app.sharecustomer.para.error");
        }

        List<Long> sharedUserIds = new ArrayList<Long>();
        for (Object userId : _sharedUserIds) {
            sharedUserIds.add(ConvertUtil.toLong(userId.toString()));
        }

        List<Long> customerIds = new ArrayList<Long>();
        for (Object customerId : _customerIds) {
            customerIds.add(ConvertUtil.toLong(customerId.toString()));
        }

        checkDeleteCustomers(customerIds);
        List<String[]> msgs = new ArrayList<String[]>();

        try {
            for (Long userId : sharedUserIds) {
                msgs.add(grantAuth(userId, customerIds, privileges));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException("app.sharecustomer.share.error");
        }
        // 给被共享人发送消息推送
        for (String[] msg : msgs) {
            if (msg.length >= 3) {
                String operTag = msg[0];
                String alert = msg[1];
                String userId = msg[2];
                String targetId = null;
                if(msg.length==4){
                	targetId = msg[3];
                }
                sengMsg(operTag, alert, Long.valueOf(userId), IM.CUSTOMER_SHARE,targetId);
            }
        }
        // 检查客户所有人，非本人共享，推送消息通知
        checkCustomerOnwer(customerIds, sharedUserIds, true);

    }

    @Override
    public List<ICustomerRow> getCustomerByIds(List<Long> ids) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(SC.id, ids.toArray());
        criteria.ge(SC.id, SQ.HSY_SEED_DATA); 
        jsonQueryBuilder.addCriteria(criteria);
        ICustomerRowSet customerRowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        List<ICustomerRow> customerList = null;
        if (customerRowSet != null && customerRowSet.getRows() != null) {
            customerList = customerRowSet.getCustomerRows();
        }
        return customerList;
    }

    private void checkCustomerOnwer(List<Long> customerIds, List<Long> userIds, boolean grant) {
        List<ICustomerRow> customers = getCustomerByIds(customerIds);
        UserQuery query = new UserQuery();
        query.setUserIds(userIds);
        VORowSet<UserValue> voset = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);

        List<UserValue> users = voset.getItems();
        if (users.isEmpty()) {
            return;
        }
        StringBuffer names = new StringBuffer();
        int nameMax = 3;
        for (UserValue user : users) {
            if (nameMax > 0) {
                names.append((String) user.getName() + ",");
            }
            nameMax--;
        }
        String nameStr = names.toString();
        nameStr = nameStr.substring(0, nameStr.lastIndexOf(","));
        if (users.size() > 3) {
            nameStr = nameStr + "等 ";
        }
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        for (ICustomerRow customer : customers) {
            Long OwnerId = customer.getOwner();
            if (userId.equals(OwnerId)) {
                continue;
            }
            if (grant) {
                String alert = EnterpriseContext.getCurrentUser().getName() + "已将客户【" + customer.getName() + "】共享给了"
                        + nameStr;
                sengMsg("", alert, OwnerId, IM.CUSTOMER_SHARE);
            } else {
                String alert = EnterpriseContext.getCurrentUser().getName() + "取消了" + nameStr + "对客户【"
                        + customer.getName() + "】的共享";
                sengMsg("", alert, OwnerId, IM.CUSTOMER_CANCEL_SHARE);
            }

        }
    }

    private void sengMsg(String operTag, String alert, Long userId, String type) {
        sengMsg(operTag, alert, userId, type,null);
    }
    private void sengMsg(String operTag, String alert, Long userId, String type,String targetId) {
        MsgUtil.sengMsg(operTag, alert, userId, type,targetId);
    }

    private void checkDeleteCustomers(List<Long> customerIds) {
        List<ICustomerRow> customers = findDeleteByIds(customerIds);
        if (customers != null && customers.size() == 0) {
            throw new AppException("app.sharecustomer.customer.deleted");
        }
    }

    private List<ICustomerRow> findDeleteByIds(List<Long> customerIds) {

        Criteria criteria = Criteria.AND();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA); 
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(SC.id, customerIds.toArray());
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance().addCriteria(criteria);

        ICustomerRowSet customerRowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());

        List<ICustomerRow> customerList = null;
        if (customerRowSet != null && customerRowSet.getRows() != null) {
            customerList = customerRowSet.getCustomerRows();
        }
        return customerList;
    }

    private String[] grantAuth(Long userId, List<Long> customerIds, List<DataAuthPrivilege> privileges) {
        String operTag = ShortIdUtil.generateShortUuid();
        String[] msg = new String[4];
        int count = 0;
        String customerName = null;
        for (Long customerId : customerIds) {
            if (customerName == null) {
                ICustomerRow customer = getCustomerById(customerId);
                if (customer != null) {
                    customerName = customer.getName();
                }
            }
            // 客户共享
            if (createGrant(userId, customerId, privileges)) {
                ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).grantLog(customerId, userId, operTag,
                        OP.GRANT);
                count++;
            }
        }
        if (count > 1) {
            customerName = "【" + customerName + "】等客户";
        } else {
            customerName = "客户【" + customerName + "】";
        }
        String alert = EnterpriseContext.getCurrentUser().getName() + "已将" + customerName + "共享给您，请您一起跟进！";
        msg[0] = operTag;
        msg[1] = alert;
        msg[2] = userId.toString();
        if(customerIds.size()==1){
        	   msg[3] = customerIds.get(0).toString();
        }
        return msg;
    }

    private boolean createGrant(Long userId, Long customerId, List<DataAuthPrivilege> privileges) {
        Long granter = EnterpriseContext.getCurrentUser().getUserLongId();

        String entityName = "Customer";
        Boolean doShare = false;// 是否做了有效的分享 即是否入库了
        List<Map<String, Object>> grants = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                .getPrivilegeIds(customerId, userId);
        for (DataAuthPrivilege privilege : privileges) {// start for1
            String privilegeName = privilege.name();
            // 库里是否已存在权限
            Boolean existsPrivilege = false;
            if (grants == null || grants.size() == 0) {
                boDataAccessManager.getDataAuthManagement().createGrant(entityName, userId, privilege, customerId,
                        granter, session());
                doShare = true;
            } else {
                for (int i = 0; i < grants.size(); i++) {
                    Map<String, Object> ge = grants.get(i);
                    if (privilegeName.equals(ge.get("privilege").toString())) {
                        existsPrivilege = true;
                    }
                }
                if (!existsPrivilege) {// 如果库里不存在该权限 则入库
                    boDataAccessManager.getDataAuthManagement().createGrant(entityName, userId, privilege, customerId,
                            granter, session());
                    doShare = true;
                }
            }
        }
        return doShare;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unShareCustomer(Map<String, Object> value) {
        List<Long> _sharedUserIds = (List<Long>) value.get("sharedUserIds");
        List<Long> _customerIds = (List<Long>) value.get("customerIds");

        if (_customerIds == null || _customerIds.isEmpty()) {
            throw new AppException("app.sharecustomer.para.error");
        }

        List<Long> sharedUserIds = new ArrayList<Long>();
        if (_sharedUserIds != null) {
            for (Object userId : _sharedUserIds) {
                sharedUserIds.add(ConvertUtil.toLong(userId.toString()));
            }
        }

        List<Long> customerIds = new ArrayList<Long>();
        for (Object customerId : _customerIds) {
            customerIds.add(ConvertUtil.toLong(customerId.toString()));
        }

        checkDeleteCustomers(customerIds);
        List<String[]> msgList = new ArrayList<String[]>();
        for (Long customerId : customerIds) {
            try {
                msgList.addAll(ungrantAuth(customerId, sharedUserIds));
            } catch (Exception e) {
                e.printStackTrace();
                throw new AppException("app.sharecustomer.unshare.error");
            }
        }
        for (String[] msg : msgList) {
            if (msg.length == 3) {
                String operTag = msg[0];
                String alert = msg[1];
                String userId = msg[2];
                sengMsg(operTag, alert, Long.valueOf(userId), IM.CUSTOMER_CANCEL_SHARE);
            }
        }
    }

    private List<String[]> ungrantAuth(Long customerId, List<Long> userids) {
        String operTag = ShortIdUtil.generateShortUuid();
        Map<Long, List<Long>> clearWorkMap = new HashMap<Long, List<Long>>();

        List<String[]> retList = new ArrayList<String[]>();

        if (userids == null) {
            userids = ServiceLocator.getInstance().lookup(GrantServiceItf.class).getUserIdsByCustomerId(customerId);
        }
        String customerName = null;
        ICustomerRow customer = getCustomerById(customerId);
        if (customer != null) {
            customerName = customer.getName();
        }
        int count = 0;
        for (Long userId : userids) {
            String[] retStr = new String[3];
            List<Long> grants = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                    .getGrantsByCustomerIdAndUserId(customerId, userId);
            addClearMap(clearWorkMap, customerId, userId);
            for (Long grantid : grants) {
                boDataAccessManager.getDataAuthManagement().removeGrant(grantid, session());
                ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).grantLog(customerId, userId, operTag,
                        OP.UNGRANT);
                count++;
            }
            if (count > 1) {
                customerName = "【" + customerName + "】等客户";
            } else {
                customerName = "客户【" + customerName + "】";
            }
            String alert = "抱歉，" + EnterpriseContext.getCurrentUser().getName() + "取消了您对" + customerName + "的共享！";

            retStr[0] = operTag;
            retStr[1] = alert;
            retStr[2] = userId.toString();
            retList.add(retStr);
        }

        List<Long> customerids = new ArrayList<Long>();
        customerids.add(customerId);
        // 检查客户所有人，非本人共享，推送消息通知
        checkCustomerOnwer(customerids, userids, false);

        return retList;
    }

    private void addClearMap(Map<Long, List<Long>> clearWorkMap, Long customerId, Long userId) {
        if (clearWorkMap.containsKey(userId)) {
            clearWorkMap.get(userId).add(customerId);
        } else {
            List<Long> customerList = new ArrayList<Long>();
            customerList.add(customerId);
            clearWorkMap.put(userId, customerList);
        }
    }

/*    @Override
    public List<Long> getDelCustomerIds() {
        List<Long> ids = new ArrayList<Long>();

        Criteria criteria = Criteria.AND();
        criteria.eq(SC.isDeleted, true);

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addFields(SC.id);
        ICustomerRowSet customerSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        if (customerSet != null && customerSet.getCustomerRows() != null) {
            for (ICustomerRow customer : customerSet.getCustomerRows()) {
                ids.add(customer.getId());
            }
        }
        return ids;
    }*/

    @Override
    public ICustomerRow findByIdWithOutAuth(Long id) {
        ICustomerRow row = query(id);
        if (row != null) {
/*            Boolean isDelete = (Boolean) row.getFieldValue(SC.isDeleted);
            if (Boolean.TRUE.equals(isDelete)) {
                return null;
            }*/
            return row;
        }
        return null;
    }

    /* 
     * 保留给回收站初始化使用
     * @see com.chanapp.chanjet.customer.service.customer.CustomerServiceItf#getCustomerDeleted()
     */
    @Override
    public ICustomerRowSet getCustomerDeleted() {
    	//TODO 老回收站功能使用，暂时保留
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA); 
        criteria.eq(SC.isDeleted, true);
        jsonQueryBuilder.addCriteria(criteria);
        //AppWorkManager.getBoDataAccessMan
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public List<ICustomerRow> getDeletedCustomerByIds(ICustomerRowSet customerRowSet, List<Long> customerIds) {
        List<ICustomerRow> rows = new ArrayList<ICustomerRow>();
        if (customerRowSet != null) {
            List<ICustomerRow> customerRows = customerRowSet.getCustomerRows();
            int num = customerRows.size();
            int i = num - 1;
            for (; i > -1; i--) {
                ICustomerRow customerRow = customerRows.get(i);
                Long customerId = customerRow.getId();
                Integer compareId = new Integer(customerId.toString());
                if (customerIds.contains(compareId.longValue())) {
                    rows.add(customerRows.remove(i));
                }
            }
        }
        return rows;
    }

    @Override
    public List<Map<String, Object>> getAddCustomerAnalysisData(String startdate, String enddate) {
        String cqlQueryString = "select count(id) as cnt, c.owner.name as name,c.owner.userId as userId from "
                + getBusinessObjectId() + " c";
        String sqlWhere = " where c.id>"+SQ.HSY_SEED_DATA+" and  c.createdDate >=:startDate and c.createdDate <=:endDate ";
        String hqlGroup = " group by c.owner.name,c.owner.userId";
        cqlQueryString = cqlQueryString + sqlWhere + hqlGroup;
        Timestamp end = null;
        Timestamp start = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if (startdate == null) {
            start = DateUtil.getWeekFirstDay();
        } else {
            try {
                start = Timestamp.valueOf(df.format(df.parse(startdate)));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (enddate == null) {
            end = DateUtil.getNowDateTime();
        } else {
            try {
                end = Timestamp.valueOf(df.format(df.parse(enddate)));
                if (DateUtil.isTheSameDate(end, DateUtil.getNowDateTime())) {
                    end = DateUtil.getNowDateTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        HashMap<String, Object> params = new HashMap<String, Object>();
        // params.put("isDeleted", false);
        params.put("startDate", start);
        params.put("endDate", end);

        return runCQLQuery(cqlQueryString, params);
    }

    private List<Map<String, Object>> getAnalysisData(String enumName, Long userId, String condition) {
        IEntity entityDefine = metaDataManager.getEntityByName("Customer");
        IField field = entityDefine.getField(enumName);
        boolean isCustom = entityDefine.getCustomFields().containsKey(field.getName());
        String sqlWhere = " where "+SC.id+ ">"+SQ.HSY_SEED_DATA;
        if (StringUtils.isNotEmpty(condition)) {
            sqlWhere += " and name like :name";
        }
        if (userId != null) {
            sqlWhere += " and c.createdBy.userId = :userId";
        }
        String cqlQueryString = "";
/*        if (isCustom) {
            String hqlGroup = " group by csp_get_dynattrval(c, 'Customer', '" + enumName + "')";
            cqlQueryString = "select count(id)  as cnt , csp_get_dynattrval(c, 'Customer', '" + enumName + "') as enumName from "
                    + getBusinessObjectId() + " as c" + sqlWhere + hqlGroup;
        } else {*/
            String hqlGroup = " group by c." + enumName + " ";
            cqlQueryString = "select count(id) as cnt , c." + enumName + " as enumName  from Customer as c" + sqlWhere
                    + hqlGroup;
      //  }

        HashMap<String, Object> params = new HashMap<String, Object>();
        //params.put("isDeleted", false);
        if (StringUtils.isNotEmpty(condition)) {
            params.put("name", "%" + condition + "%");
        }
        if (userId != null) {
            params.put("userId", userId);
        }

        return runCQLQuery(cqlQueryString, params);
    }

    @Override
    public RowSet getCompositionAnalysisData(String enumName, Long userId, String condtion) {
        List<Map<String, Object>> list = getAnalysisData(enumName, userId, condtion);
        RowSet rowSet = new RowSet();
        HashMap<String, Row> rowMap = new HashMap<String, Row>();
        int total = 0;
        Row emptyRow = null;
        for (Map<String, Object> objects : list) {
            boolean flag = false;
            Row row = new Row();
            row.put("y", objects.get("cnt").toString());
            row.put("tooltip", objects.get("cnt").toString() + "个");
            if (objects.get("enumName") == null || StringUtils.isEmpty(objects.get("enumName").toString())) {
                row.put("text", "无");
                flag = true;
            } else {
                if (objects.get("enumName") != null
                        && objects.get("enumName").getClass().getSimpleName().equals("DynAttrValue")) {
                    Object daAttrValue = ReflectionUtil.getFieldValue(objects.get("enumName"), "dynAttrValue");
                    DynamicEnum dynamicEnum = (DynamicEnum) daAttrValue;
                    if (dynamicEnum != null) {
                        row.put("text", dynamicEnum.getLabel());
                        row.put("value", dynamicEnum.getValue());
                    } else {
                        row.put("text", "无");
                        flag = true;
                    }
                } else if (objects.get("enumName") instanceof DynamicEnum) {
                    DynamicEnum oppoStatus = (DynamicEnum) objects.get("enumName");
                    row.put("text", oppoStatus.getLabel());
                    row.put("value", oppoStatus.getValue());

                }
            }
            total += Integer.parseInt(objects.get("cnt").toString());
            if (!flag) {
                String enumValue = row.get("text").toString().trim();
                if (rowMap.containsKey(enumValue)) {
                    Row oldRow = rowMap.get(row.get("text"));
                    Integer oldY = Integer.parseInt(oldRow.get("y").toString());
                    Integer Y = Integer.parseInt(row.get("y").toString());
                    String sum = oldY + Y + "";
                    oldRow.put("y", sum);
                    oldRow.put("tooltip", sum + "个");
                } else {
                    rowSet.add(row);
                    rowMap.put(enumValue, row);
                }

            } else {
                emptyRow = row;
            }
        }
        if (emptyRow != null) {
            rowSet.add(emptyRow);
        }
        rowSet.setTotal(total);

        RowSet rs = null;

        char[] arr = enumName.toCharArray();
        if (arr[0] >= 'a' && arr[0] <= 'z') {
            arr[0] -= 'a' - 'A';
        }

        FieldMetaData cspEnum = getMeta().getEntites().get("Customer").getFields().get(enumName);
        String realEnumName = cspEnum.enumName;
        List<CSPEnumValue> enumValueList = getEnum(realEnumName);
        rs = compositionAnalysisDataSort(rowSet, enumValueList);
        return rs;
    }

    /**
     * 按枚举值排序统计数据
     * 
     * @param rowSet
     * @param enumValueList
     */
    private RowSet compositionAnalysisDataSort(RowSet rowSet, List<CSPEnumValue> enumValueList) {

        if (rowSet == null) {
            return null;
        }
        if (enumValueList == null) {
            return rowSet;
        }
        List<Row> rows = rowSet.getItems();
        if (rows == null || rows.size() < 1) {
            return rowSet;
        }
        RowSet rs = new RowSet();

        for (int i = 0; i < enumValueList.size(); i++) {
            String value = enumValueList.get(i).getEnumValue();
            for (int j = 0; j < rows.size(); j++) {
                String old_value = (String) rows.get(j).get("value");
                if (old_value == null || "".equals(old_value)) {// "无" 的那种情况
                    // 此处不处理
                } else if (value.equals(old_value)) {
                    rs.add(rows.get(j));
                }
            }
        }

        for (int j = 0; j < rows.size(); j++) {// 处理无的那种情况
            String old_value = (String) rows.get(j).get("value");
            if (old_value == null || "".equals(old_value)) {// "无" 的那种情况
                rs.add(rows.get(j));// "无" 放在最后
            }
        }

        rs.setTotal(rowSet.getTotal());

        return rs;

    }

    private com.chanapp.chanjet.customer.cache.CustomerMetaData getMeta() {
        return MetadataCacheBuilder.newBuilder().buildCache().get("metadata");
    }

    private List<CSPEnumValue> getEnum(String enumName) {
        List<CSPEnumValue> enums = new ArrayList<CSPEnumValue>();
        if (enumName == null) {
            return enums;
        }
        CSPEnum cspEnum = getMeta().getEnums().get(enumName);
        if (cspEnum == null) {
            return enums;
        }
        enums = cspEnum.getEnumValues();
        return enums;
    }

    @Override
    public List<Map<String, Object>> customerProgressCount(Long userId, Date startDate, Date endDate) {
        List<Object> params = new ArrayList<Object>();
        String cqlQueryString = " select count(t.id) as num ,t.status as status from " + getBusinessObjectId() + " t "
                + " where "+SC.id+ ">"+SQ.HSY_SEED_DATA;
      //  params.add(false);
        if (startDate != null && endDate != null) {
            cqlQueryString += " and t.lastModifiedDate between ? and ? ";
            Timestamp start = Timestamp.valueOf(DateUtil.getDateTimeString(startDate));
            Timestamp end = Timestamp.valueOf(DateUtil.getDateTimeString(endDate));
            params.add(start);
            params.add(end);
        }
        if (userId != null) {
            cqlQueryString += "and t.owner.id = ?";
            params.add(Long.valueOf(userId));
        }
        cqlQueryString += " group by t.status ";
        return runCQLQuery(cqlQueryString, params);
    }

    @Override
    public List<Map<String, Object>> progressCountCustomer() {
        String cqlQueryString = " select count(t.id) as num ,t.status as status,t.owner.userId as userId from "
                + getBusinessObjectId() + " t "
                + " where t.id >"+SQ.HSY_SEED_DATA+" group by t.status,t.owner.userId";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
       // paraMap.put("isDeleted", false);
        return runCQLQuery(cqlQueryString, paraMap);
    }

    @Override
    public Integer countAll() {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        jsonQueryBuilder.addCriteria(criteria);
        return getRowCount(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public ICustomerRowSet findAllWithPage(PageRestObject page) {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(SC.createdDate);
        jsonQueryBuilder.addOrderDesc(SC.id);

        Integer pageNo = page.getPageno();
        Integer pageSize = page.getPagesize();
        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);
        String jsonQuery = jsonQueryBuilder.toJsonQuerySpec();
        return this.getBusinessObjectHome().query(jsonQuery);
       // return query(jsonQuery);
    }

    @Override
    public ICustomerRow getCustomerByName(String name) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, "false"));
        criteria.eq(CustomerMetaData.name, name);
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        jsonQueryBuilder.addCriteria(criteria);
        String jsonQuerySpec = jsonQueryBuilder.toJsonQuerySpec();

        ICustomerRowSet rowset = query(jsonQuerySpec);
        if (rowset != null && rowset.size() > 0) {
            return rowset.getRow(0);
        }
        return null;
    }

    @Override
    public List<UserValue> unsharedUsers(Long customerId) {
        if (0L == customerId) {
            throw new AppException("app.common.params.invalid");
        }
        List<UserValue> retUsers = new ArrayList<UserValue>();
        UserQuery query = new UserQuery();
        query.setStatus(SRU.STATUS_ENABLE);
        VORowSet<UserValue> rowSet = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
        ICustomerRow customerRow = findByIdWithAuth(customerId);
        if (customerRow == null)
            return retUsers;
        Long userId = customerRow.getOwner();
        List<Long> userIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                .getUserIdsByCustomerId(customerId);
        if (userIds != null) {
            userIds.add(userId);
        }
        for (Object row : rowSet.getItems()) {
            UserValue user = (UserValue) row;
            Long id = user.getId();
            if (userIds == null || userIds.isEmpty() || !userIds.contains(id)) {
                retUsers.add(user);
            }
        }
        return retUsers;
    }

    private List<Map<String, Object>> findGlobalExactCustomers(String customerName, String phone) {
        List<Object[]> list = _getExistsCustomer(null,customerName,phone);
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
       // List<Object[]> list = data.getRawData();
        if (list != null) {
            for (Object[] o : list) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", o[0]);
                map.put("name", o[4]);
                map.put("owner", o[2]);
                map.put("createdDate", o[3]);
                result.add(map);
            }
        }
        return result;
    }

    private int countGlobalCustomers(String customerName, String fullSpell) {
        Criteria criteria = Criteria.AND();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        Criteria condition = Criteria.OR();
        String[] fullSpellArray = fullSpell.split("\\,");
        for (String fullSpellStr : fullSpellArray) {
            condition.like("fullSpell", fullSpellStr);
        }
        criteria.addChild(condition);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        Integer total = privilegedGetRowCount(jsonQuerySpec);
        return total;
    }

    private List<Map<String, Object>> findGlobalCustomers(String customerName, String fullSpell, int maxResults) {
        Criteria criteria = Criteria.AND();
        criteria.ge(SC.id, SQ.HSY_SEED_DATA);
        Criteria condition = Criteria.OR();
        condition.like("name", customerName);
        String[] fullSpellArray = fullSpell.split("\\,");
        for (String fullSpellStr : fullSpellArray) {
            condition.like("fullSpell", fullSpellStr);
        }
        criteria.addChild(condition);
        String jsonQuerySpec = JsonQueryBuilder.getInstance().addFields("id", "name", "owner", "createdDate")
                .addCriteria(criteria).setMaxResult(maxResults).toJsonQuerySpec();

        List<Object[]> list = privilegedQueryNoTransform(jsonQuerySpec);
       // List<Object[]> list = data.getRawData();

        List<Map<String, Object>> customers = new ArrayList<Map<String, Object>>();

        for (Object[] o : list) {
            Map<String, Object> customer = new HashMap<String, Object>();
            customer.put("id", o[0]);
            customer.put("name", o[1]);
            customer.put("owner", o[2]);
            customer.put("createdDate", o[3]);
            customers.add(customer);
        }
        return customers;
    }

    @Override
    public Map<String, Object> getCustomersByNameAndPhone(String name, String phone) {
        Assert.hasLength(name, "app.customer.name.required");
        boolean exactMatch = true;
        Map<String, Object> result = new HashMap<String, Object>();

        //name = ConvertUtil.toUTF8(name);

        List<Map<String, Object>> customers = findGlobalExactCustomers(name, phone);
        if (customers == null || customers.size() < 1) {
            // 公司后缀，去除
            String comSuffix = "有限|公司|集团";
            String[] suffixArray = comSuffix.split("\\|");
            for (String suffix : suffixArray) {
                name = name.replace(suffix, "");
            }
            String fullSpell = PinyinUtil.hanziToPinyinFull(name, true);
            int maxResults = 30;
            // 全公司同名客户总数
            int total = countGlobalCustomers(name, fullSpell);
            // 全公司最多30个同名客户
            customers = findGlobalCustomers(name, fullSpell, maxResults);
            result.put("total", total);
            exactMatch = false;
        }
        // 修改权限范围内的客户
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Long> customerIds = new ArrayList<Long>();
        if (customers != null) {
            for (Map<String, Object> customer : customers) {
                customerIds.add((Long) customer.get("id"));
            }
        }
        List<Long> customerUpdatePriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                .checkUpdateDataAuthList(CustomerMetaData.EOName, customerIds, userId);
        List<Long> customerSelectPriv = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class)
                .checkSelectDataAuthList(CustomerMetaData.EOName, customerIds, userId);
        List<Map<String, Object>> customerList = new ArrayList<Map<String, Object>>();
        if (customers != null) {
            for (Map<String, Object> customer : customers) {
                Map<String, Object> customerMap = new HashMap<String, Object>();
                Long customerId = (Long) customer.get("id");
                customerMap.put("id", customerId);
                customerMap.put("name", customer.get("name"));
                customerMap.put("owner", customer.get("owner"));
                Long ownerId =(Long) customer.get("owner");
                User owner = EnterpriseUtil.getUserById(ownerId);
                customerMap.put("ownerName", owner.getName());
                customerMap.put("createdDate", customer.get("createdDate"));
                if (customerUpdatePriv.contains(customerId)) {
                    customerMap.put("privilege", PV.UPDATE_PRVILEGE);
                } else if (customerSelectPriv.contains(customerId)) {
                    customerMap.put("privilege", PV.SELECT_PRVILEGE);
                } else {
                    customerMap.put("privilege", PV.NO_PRVILEGE);
                }
                customerList.add(customerMap);
            }
        }
        result.put("result", true);
        result.put("items", customerList);
        result.put("exactMatch", exactMatch);
        return result;
    }

    @Override
    public Map<String, Object> addCustomerWithMerge(LinkedHashMap<String, Object> customerValue) {
        ICustomerRow customerRow = getFinalCustomerRow(customerValue, null);
        Map<String, Object> result = new HashMap<String, Object>();
        boolean mergeFlag = customerRow.getId() != null && customerRow.getId() > 0;
        result.put("mergeFlag", mergeFlag);

        upsert(customerRow);
        result.put("entity", customerRow);

        return result;
    }

    @Override
    public void updateCustomerCoordinate(Long customerId, Map<String, Double> coordinate, String coordinateNote) {
        try {
            ICustomerRow customer = findByIdWithAuth(customerId);
            if (customer != null) {
                Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
                if (ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkUpdateDataAuth("Customer",
                        customer.getId(), userId)) {
                    if (coordinate != null) {
                        GeoPoint geoPoint = boDataAccessManager.createGeoPoint();
                        
                        geoPoint.setLatitude(coordinate.get("latitude"));
                        geoPoint.setLongitude(coordinate.get("longitude"));
                        customer.setCoordinate(geoPoint);
                    } else {
                        customer.setCoordinate(null);
                    }
                    customer.setCoordinateNote(coordinateNote);
                    upsert(customer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<UserValue> getSharedUsers(Long customerId) {
        if (0L == customerId) {
            throw new AppException("app.common.params.invalid");
        }
        List<Long> userIds = ServiceLocator.getInstance().lookup(GrantServiceItf.class)
                .getUserIdsByCustomerId(customerId);
        if (userIds == null || userIds.size() == 0) {
            return null;
        }
        UserQuery query = new UserQuery();
        query.setUserIds(userIds);
        VORowSet<UserValue> rowSet = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUsersByParam(query);
        return rowSet.getItems();
    }
    @Override
	public  Map<String,Object> queryCustomerListByKeyword(int first,int max,String keyword){
		Map<String,Object> retList = new HashMap<String,Object>();
		retList.put("hasMore", false);   
		Criteria criteria = Criteria.AND();
		criteria.ge(SC.id, SQ.HSY_SEED_DATA);
		//criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));		
		if(StringUtils.isNotEmpty(keyword)){			
			String searchtext = keyword;
			try {
				//+号 在decode后会变成空格
				searchtext = searchtext.replace("+", "%2B"); 
				//searchtext =  new String(searchtext.getBytes("ISO-8859-1"), "UTF-8");

			} catch (Exception e) {
				e.printStackTrace();
			}
            List<Long> cusotmerIds = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                    .getCustomerIdByContactSearch(searchtext);
			Criteria childCriteria=Criteria.OR();
			if(cusotmerIds!=null&&cusotmerIds.size()>0){
				childCriteria.in(SC.id, cusotmerIds.toArray());
			}
			childCriteria.like(CustomerMetaData.name,searchtext).like(CustomerMetaData.phone, searchtext).like(CustomerMetaData.fullSpell, searchtext).like(CustomerMetaData.simpleSpell, searchtext);			
			criteria.addChild(childCriteria);
		}
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		if(first>-1){
			jsonQueryBuilder.setFirstResult(first);
			jsonQueryBuilder.setMaxResult(max+1);
		}
		jsonQueryBuilder.addCriteria(criteria).addOrderDesc(SC.lastModifiedDate);
		IBusinessObjectRowSet rowSet = this.query(jsonQueryBuilder.toJsonQuerySpec());
		if(rowSet!=null&&rowSet.getRows()!=null&&rowSet.getRows().size()>0){
			List<IBusinessObjectRow> rows = rowSet.getRows();
			if(rows.size()==(max+1)){
				retList.put("hasMore", true);
				rows.remove(rows.size() - 1);
			}else{
				retList.put("hasMore", false);
			}					
			retList.put("items", getCustomerListData(rows));
			
		}
		return retList;		
	}
	
	
	private List<Map<String,Object>> getCustomerListData(List<IBusinessObjectRow> rows){
		Map<Long,Map<String,Object>> customerIdMap = new HashMap<Long,Map<String,Object>>();
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		List<Long> ids =new ArrayList<Long>();
		for(IBusinessObjectRow row : rows){
			ICustomerRow orginRow = (ICustomerRow)row;
			Map<String,Object> customerMap = new HashMap<String,Object>();
			customerMap.put(SC.id, orginRow.getId());
			customerMap.put(CustomerMetaData.name, orginRow.getName());		
			if(orginRow.getStatus()!=null){
				Map enumMap = new HashMap();
				enumMap.put("label", orginRow.getStatus().getLabel());
				enumMap.put("value", orginRow.getStatus().getValue());
				customerMap.put(CustomerMetaData.status, enumMap);
			}
			customerMap.put(CustomerMetaData.address, orginRow.getAddress());
			if(orginRow.getPhone()!=null){
				customerMap.put(CustomerMetaData.phone, orginRow.getPhone().getPhoneNumber());	
			}
			customerIdMap.put(orginRow.getId(), customerMap);
			ids.add(orginRow.getId());
		}
		HashMap<Long,ArrayList<Row>> contactMap= ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .findContactByCustomerIds(ids);				
		for(Long id:ids){
			Map<String,Object> customerMap = customerIdMap.get(id);
			customerMap.put("contacts", contactMap.get(id));
			retList.add(customerMap);
		}		
		return retList;
	}
	
	@Override
	public Map<String,Object> getCustomerDetail( Long customerId ){
		Map<String,Object> retMap = new HashMap<String,Object>();
        Assert.notNull(customerId, "客户ID不能为空");
        ICustomerRow customer = findByIdWithAuth(customerId);
        if (customer == null) {
            throw new AppException("app.customer.object.notexist");
        }
        boolean hasPri = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkUpdateDataAuth(
                CustomerMetaData.EOName, customerId, EnterpriseContext.getCurrentUser().getUserLongId());
        Map<String, Object> customerRow = BoRowConvertUtil.toRow(customer);
	    if(hasPri){
	    	customerRow.put("privilege", Integer.valueOf("111", 2));
	    }else{
	    	customerRow.put("privilege", Integer.valueOf("011", 2));
	    }
	    IContactRowSet contactSet = ServiceLocator.getInstance().lookup(ContactServiceItf.class).findContactsByCustomer(customerId);
		retMap.put("customer", customerRow);
		retMap.put("contacts", getContactViewList(contactSet));
	    return retMap;
	}
	
	private List<Map<String,Object>> getContactViewList(IContactRowSet contacts){
		List<Map<String,Object>> retList =  new ArrayList<Map<String,Object>>();
		if(contacts!=null&&contacts.getRows()!=null){
			for(IContactRow row:contacts.getContactRows()){
				Map<String,Object> contactMap = new HashMap<String,Object>();
				contactMap.put(SC.id, row.getId());
				contactMap.put(ContactMetaData.name, row.getName());
				contactMap.put(ContactMetaData.position, row.getPosition());
				if(row.getEmail()!=null){
					contactMap.put(ContactMetaData.email, row.getEmail().getAccountId());	
				}		
				if(row.getMobile()!=null){
					contactMap.put(ContactMetaData.mobile, row.getMobile().getPhoneNumber());
				}
				retList.add(contactMap);
			}
		}
		return retList;
	}
	
	@Override
	public String getCheckInCustomer(Double latitude, Double longitude, Integer first, Integer max) {
		// ST_GeographyFromText
		// String geographyTarget = "(select
		// st_geographyfromtext('SRID="+com.chanapp.chanjet.customer.frame.util.Constants.SRID+";point(-"+latitude+"
		// "+longitude+")'))";
		String hql = " select id,name,coordinate,coordinateNote,"
				+ " st_distance(coordinate,st_point(:lat, :lng)) as distance from "+ getBusinessObjectId()+"  where id>"+SQ.HSY_SEED_DATA
				+ " order by st_distance(coordinate,st_point(:lat, :lng)) ";
		HashMap<String, Object> paraMap = new HashMap<String, Object>();
		paraMap.put("lat", longitude);
		//paraMap.put("isDeleted", false);
		paraMap.put("lng", latitude);
		List<Map<String, Object>> result = runCQLQuery(hql, paraMap);
		List<Map<String, Object>> retCustomers = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> customer : result) {
			Map<String, Object> customerMap = new HashMap<String, Object>();
			customerMap.put(SC.id, customer.get("id"));
			customerMap.put(CustomerMetaData.name, customer.get("name"));
			customerMap.put(CustomerMetaData.coordinateNote, customer.get("coordinateNote"));
			customerMap.put("distance", customer.get("distance"));
			if (customer.get("coordinate") != null) {
				GeoPoint coordinate = (GeoPoint) customer.get("coordinate");
				customerMap.put(CustomerMetaData.coordinate, AppWorkManager.getDataManager().toJSONString(coordinate));
			}
			retCustomers.add(customerMap);
		}
		Map<String,Object> retMap = new HashMap<String,Object>();
		retMap.put("items", retCustomers);		
		return JSON.toJSONString(retMap);
	}

	@Override
	public  List<Long> getCustomerIdsByKeyWord(String searchtext){
    	List<Long> customerIds = new ArrayList<Long>();
		JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
		Criteria criteria = Criteria.AND();
		criteria.ge(SC.id, SQ.HSY_SEED_DATA);
		//criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));	
		Criteria childCriteria=Criteria.OR();
		childCriteria.like(CustomerMetaData.name,searchtext).like(CustomerMetaData.phone, searchtext).like(CustomerMetaData.fullSpell, searchtext).like(CustomerMetaData.simpleSpell, searchtext);
		criteria.addChild(childCriteria);
		String queryStr = jsonQueryBuilder.addFields(SC.id).addCriteria(criteria).toJsonQuerySpec();
		IBusinessObjectRowSet customerSet = this.query(queryStr);
		
		for(IBusinessObjectRow customerRow :customerSet.getRows()){
			Long id = (Long)customerRow.getFieldValue(SC.id);
			customerIds.add(id);
		}
		return customerIds;
	}
}
