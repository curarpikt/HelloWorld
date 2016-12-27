package com.chanapp.chanjet.customer.service.exporttask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRowSet;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskHome;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskRow;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskRowSet;
import com.chanapp.chanjet.customer.cache.DataHelper;
import com.chanapp.chanjet.customer.cache.FieldMetaData;
import com.chanapp.chanjet.customer.constant.TASK;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;

import com.chanapp.chanjet.customer.layout.LayoutManager;
import com.chanapp.chanjet.customer.service.checkin.CheckinServiceItf;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.report.ReportServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.customer.util.ReportUtil;
import com.chanapp.chanjet.customer.vo.LoadMoreList;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.annotation.NotSingleton;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;
import com.chanjet.csp.ui.util.OSSUtil;

@NotSingleton
public class ExportTaskServiceImpl extends BoBaseServiceImpl<IExportTaskHome, IExportTaskRow, IExportTaskRowSet>
        implements ExportTaskServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(ExportTaskServiceImpl.class);

    // 特殊业务字段顺序
    private List<HashMap<Integer, String>> businessSeqList;

    // 元数据字段类型
    private Map<String, Map<String, String>> fieldTypeMap;

    // 元数据字段名称
    private Map<String, Map<String, String>> fieldNameMap;

    private Map<String, Integer> sheetEntityMap = new HashMap<String, Integer>();

    private String customerEditFields[] = null;
    private String contactFields[] = null;
    // add by jiayuep owner
    private String workRecordFields[] = new String[] { "customer", "content", "owner", "createdBy", "createdDate" };

    /**
     * 返回数据备份任务列表
     * 
     * @param para
     * @param exportTaskHome
     * @param session
     * @return
     */
    private List<IExportTaskRow> getExportTasks(Map<String, Object> para) {

        Long id = (Long) para.get("id");
        Long userId = (Long) para.get("userId");
        String creTimeStartStr = (String) para.get("creTimeStartStr");
        String creTimeEndStr = (String) para.get("creTimeEndStr");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Criteria criteria = Criteria.AND();
        if (StringUtils.isNotEmpty(creTimeStartStr)) {
            Timestamp creTimeStart = null;
            try {
                creTimeStart = Timestamp.valueOf(df.format(df.parse(creTimeStartStr)));
            } catch (ParseException e) {
                throw new AppException("app.common.params.invalid");
            }
            criteria.ge("creTime", creTimeStart);
        }
        if (StringUtils.isNotEmpty(creTimeEndStr)) {
            Timestamp creTimeEnd = null;
            try {
                creTimeEnd = Timestamp.valueOf(df.format(df.parse(creTimeEndStr)));
            } catch (ParseException e) {
                throw new AppException("app.common.params.invalid");
            }
            criteria.le("creTime", creTimeEnd);
        }
        if (userId != null) {
            criteria.eq("userId", userId);
        }
        if (id != null) {
            criteria.eq("id", id);
        }

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc("creTime");
        jsonQueryBuilder.addOrderDesc("id");
        IExportTaskRowSet exportTaskRowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        List<IExportTaskRow> exportTaskRowList = null;
        if (exportTaskRowSet != null) {
            exportTaskRowList = exportTaskRowSet.getExportTaskRows();
        }
        return exportTaskRowList;

    }

    @Override
    public Map<String, Object> getExportTaskById(Long id) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> para = new HashMap<String, Object>();
        para.put("id", id);
        List<IExportTaskRow> data = getExportTasks(para);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);

        if (data == null || data.size() == 0) {
            result.put("taskStatus", TASK.STATUS_0);
            return result;
        }

        IExportTaskRow exportTask = data.get(0);
        Long taskStatus = exportTask.getTaskStatus();
        result.put("id", id);
        result.put("taskStatus", taskStatus);
        result.put("result", true);

        return result;
    }

    private Integer taskListCount(long userId, List<Long> status) {
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in("taskStatus", status.toArray());

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);

        return getRowCount(jsonQueryBuilder.toJsonQuerySpec());
    }

    private IExportTaskRowSet taskList(long userId, List<Long> status, Integer pageNo, Integer pageSize) {
        Criteria criteria = Criteria.AND();
      //  criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in("taskStatus", status.toArray());

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc("creTime");
        jsonQueryBuilder.addOrderDesc(SC.id);

        jsonQueryBuilder.setFirstResult((pageNo - 1) * pageSize);
        jsonQueryBuilder.setMaxResult(pageSize);

        return query(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public LoadMoreList taskList(Integer pageNo, Integer pageSize) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();

        List<Long> status = new ArrayList<Long>();
        status.add(TASK.STATUS_3);
        status.add(TASK.STATUS_9);
        Integer total = taskListCount(userId, status);

        // para 验证
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 20;
        }

        IExportTaskRowSet items = taskList(userId, status, pageNo, pageSize);

        RowSet rowset = BoRowConvertUtil.toRowSet(items);
        LoadMoreList loadMoreList = new LoadMoreList();
        loadMoreList.setItems(rowset.getItems());
        loadMoreList.setHasMore(true);
        if (total <= (pageNo * pageSize)) {
            loadMoreList.setHasMore(false);
        }

        return loadMoreList;
    }

    @Override
    public Map<String, Object> getExportTasksToday() {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> para = new HashMap<String, Object>();
        String baseDate = DateUtil.getDateString(new Date());
        para.put("creTimeStartStr", baseDate + " 00:00:00");
        para.put("creTimeEnd", baseDate + " 23:59:59");

        List<IExportTaskRow> data = getExportTasks(para);

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        Timestamp currentTime = DateUtil.getNowDateTime();
        result.put("currentTime", currentTime);

        if (data == null || data.size() == 0) {
            result.put("taskStatus", TASK.STATUS_0);
            result.put("result", true);
            return result;
        }

        // 按时间 id 倒叙排列 第一个一定是最新的状态
        IExportTaskRow exportTask = data.get(0);

        Long id = exportTask.getId();
        Long taskStatus = exportTask.getTaskStatus();
        result.put("id", id);
        result.put("taskStatus", taskStatus);
        result.put("result", true);
        return result;
    }

    @Override
    public Map<String, Object> saveTask() {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();

        Map<String, Object> para = new HashMap<String, Object>();
        String baseDate = DateUtil.getDateString(new Date());
        para.put("creTimeStartStr", baseDate + " 00:00:00");
        para.put("creTimeEnd", baseDate + " 23:59:59");

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);

        List<IExportTaskRow> data = getExportTasks(para);

        if (data != null && data.size() > 0) {// 只有 执行失败才允许创建
            for (int i = 0; i < data.size(); i++) {
                Long ts = data.get(i).getTaskStatus();
                if (TASK.STATUS_1.equals(ts) || TASK.STATUS_2.equals(ts) || TASK.STATUS_3.equals(ts)) {
                    result.put("result", false);
                    result.put("taskStatus", data.get(0).getTaskStatus());
                    return result;
                }
            }
        }

        IExportTaskRow task = createRow();
        task.setCreTime(new Timestamp(new java.util.Date().getTime()));
        task.setTaskName("备份任务");
        task.setTaskStatus(TASK.STATUS_1);
        task.setUserId(userId);
        upsert(task);

        Long id = task.getId();
        Long taskStatus = task.getTaskStatus();
        result.put("id", id);
        result.put("taskStatus", taskStatus);
        result.put("result", true);

        return result;
    }

    private ExportCountValue _getExportCount() {
        ExportCountValue value = new ExportCountValue();
        Integer customerCount = ServiceLocator.getInstance().lookup(CustomerServiceItf.class).countAll();
        Date endDate = new Date();
        Calendar cl = Calendar.getInstance();
        cl.setTime(endDate);
        cl.add(Calendar.MONTH, -3);
        Integer workRecordCount = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .countByTime(cl.getTime().getTime());
        Integer contactCount = ServiceLocator.getInstance().lookup(ContactServiceItf.class).countAll();
        value.setCustomerCount(customerCount);
        value.setContactCount(contactCount);
        value.setWorkRecordCount(workRecordCount);
        return value;
    }

    private void initSheetMap() {
        sheetEntityMap.put(CustomerMetaData.EOName, 0);
        sheetEntityMap.put(ContactMetaData.EOName, 1);
        sheetEntityMap.put(WorkRecordMetaData.EOName, 2);
    }

    private void initFieldSeq() {
        businessSeqList = new ArrayList<HashMap<Integer, String>>();
        HashMap<Integer, String> customerSheet = new HashMap<Integer, String>();
        customerSheet.put(2, "status");
        customerSheet.put(3, "owner");
        // customerSheet.put(4, "granter");
        HashMap<Integer, String> contactSheet = new HashMap<Integer, String>();
        contactSheet.put(1, "customer");
        HashMap<Integer, String> workRecordSheet = new HashMap<Integer, String>();
        businessSeqList.add(customerSheet);
        businessSeqList.add(contactSheet);
        businessSeqList.add(workRecordSheet);
    }

    private void initFieldType(List<String> entitys) {
        fieldTypeMap = new HashMap<String, Map<String, String>>();
        fieldNameMap = new HashMap<String, Map<String, String>>();
        // [[客户名称, 跟进进度, Owner, null, 行业, 地区, 客户来源, 客户分类, 客户级别, 传真, 网址], [所属公司,
        // 手机, 邮箱, QQ, 电话, 职务, 备注, 性别, 称谓, 微博, 传真, 所属部门, 地址], [客户, 沟通内容, Created
        // By, Created Date]]
        for (String entity : entitys) {
            Map<String, String> typeMap = new HashMap<String, String>();
            Map<String, String> nameMap = new HashMap<String, String>();
            IEntity metaEntity = metaDataManager.getEntityByName(entity);
            Map<String, IField> fldMetas = metaEntity.getFields();
            // 参与人特殊处理
            nameMap.put("granter", "参与人");
            for (String key : fldMetas.keySet()) {
                IField meta = fldMetas.get(key);
                FieldMetaData fieldMeta = DataHelper.convertFieldMetadata(meta);
                typeMap.put(key, fieldMeta.type.name());
                if (key.equals("owner")) {
                    nameMap.put(key, "业务员");
                } else if (key.equals("customer")) {// add by jiayuep
                    nameMap.put(key, "客户名称");
                } else if (key.equals("createdBy")) {
                    nameMap.put(key, "创建人");
                } else if (key.equals("createdDate")) {
                    nameMap.put(key, "创建时间");
                } else if (key.equals("lastModifiedDate")) {
                    nameMap.put(key, "上次修改时间");
                } else {
                    nameMap.put(key, fieldMeta.label);
                }
            }
            fieldTypeMap.put(entity, typeMap);
            fieldNameMap.put(entity, nameMap);
        }
    }

    public ExportDataValue getExportData(List<PageRestObject> sheetPageList) {
        logger.info("ExportDataValue begin:");
        ExportDataValue exportValue = new ExportDataValue();
        // 数据列表
        List<List<Map<String, Object>>> sheetList = new ArrayList<List<Map<String, Object>>>();
        // TITEL列表
        List<List<ExcelColunmValue>> fieldNameList = new ArrayList<List<ExcelColunmValue>>();
        if (customerEditFields == null) {
            customerEditFields = LayoutManager.getCustomerEditFields();
        }
        if (contactFields == null) {
            contactFields = LayoutManager.getContactEditFields();
        }
        if (sheetEntityMap == null || sheetEntityMap.size() == 0) {
            initSheetMap();
        }
        if (businessSeqList == null) {
            initFieldSeq();
        }
        // 初始化字段与名称、类型对应关系fieldTypeMap，fieldNameMap
        logger.info("initFieldType begin:");
        if (fieldTypeMap == null && fieldNameMap == null) {
            // 初始化业务固有顺序
            List<String> entityList = new ArrayList<String>();
            entityList.add(CustomerMetaData.EOName);
            entityList.add(ContactMetaData.EOName);
            entityList.add(WorkRecordMetaData.EOName);
            initFieldType(entityList);
        }

        Integer customerSheetNum = 0;
        Integer contactSheetNum = 1;
        Integer workRecordSheetNum = 2;
        if (sheetEntityMap != null) {
            customerSheetNum = sheetEntityMap.get(CustomerMetaData.EOName);
            contactSheetNum = sheetEntityMap.get(ContactMetaData.EOName);
            workRecordSheetNum = sheetEntityMap.get(WorkRecordMetaData.EOName);
        }
        // 获取所有客户
        PageRestObject customerPage = sheetPageList.get(customerSheetNum);
    
            ICustomerRowSet customerSet = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .findAllWithPage(customerPage);              
            setCustomerSheet(customerSet, sheetList, fieldNameList);
            logger.info("setCustomerSheet end:");
        
    
        logger.info("getCustomers end:");
        
        PageRestObject contactPage = sheetPageList.get(contactSheetNum);
      
            // 获取联系人
        IBusinessObjectRowSet contactSet = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .findAllWithPage(contactPage);
        logger.info("getcontacts end:");
        setContactSheet(contactSet, sheetList, fieldNameList);
        logger.info("setContactSheet end:");
        
        
        // 获取3个月内的工作经记录
        Date endDate = new Date();
        Calendar cl = Calendar.getInstance();
        cl.setTime(endDate);
        cl.add(Calendar.MONTH, -3);
        PageRestObject workRecordPage = sheetPageList.get(workRecordSheetNum);
       
        List<List<String>> workrecordList = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .getWorkRecordExcelRow(cl.getTime().getTime(),  workRecordPage);
        logger.info("getWorkRecords end:");
        setWorkRowSheet(workrecordList, sheetList, fieldNameList);
        logger.info("setWorkRowSheet end:");

        logger.info("begin setSheet");
        exportValue.setSheetDatas(sheetList);
        exportValue.setSheetFieldNames(fieldNameList);
        exportValue.setStartDate(new Timestamp(cl.getTime().getTime()));
        exportValue.setEndDate(new Timestamp(endDate.getTime()));
        logger.info("end setSheet");
        return exportValue;
    }

    private List<ExcelColunmValue> getFieldNames(String entityName, List<String> seqList) {
        List<ExcelColunmValue> lables = new ArrayList<ExcelColunmValue>();
        Map<String, String> nameMap = fieldNameMap.get(entityName);
        for (String field : seqList) {
            ExcelColunmValue value = new ExcelColunmValue();
            value.setFiledLaebl(nameMap.get(field));
            value.setFiledName(field);
            lables.add(value);
        }
        return lables;
    }

    private Map<String, Object> getExcelRow(Row row, List<String> fields, String entityClass) {
        // String entityClass = checkRowEntity(row);
        Map<String, String> typeMap = fieldTypeMap.get(entityClass);
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        for (String field : fields) {
            Object value = row.get(field);
            String type = typeMap.get(field);
            if (value != null) {
                data.put(field, getValueByType(type, value));
            } else {
                data.put(field, "");
            }
        }
        return data;
    }

    private String getValueByType(String type, Object value) {
        if (type.equals(FieldTypeEnum.CSP_ENUM.name())) {
            Map row = (Map) value;
            return row.get("label").toString();
        }
        if (type.equals(FieldTypeEnum.FOREIGN_KEY.name())) {
        	Map row = (Map) value;
            // 其他没有NAME属性的对象需特殊处理
            if (row.get("name") != null) {
                return row.get("name").toString();
            }
            return "";
        }
        if (type.equals(FieldTypeEnum.TIMESTAMP.name())) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long timeStart = (Long) value;
            Date date = new Date(timeStart);
            return sdf.format(date);
        }
        return value.toString();
    }

    private List<String> getSeqList(HashMap<Integer, String> customerSeq, String[] fields) {
        List<String> seqList = new ArrayList<String>();
        int seq = 1;
        for (String field : fields) {
            if (seqList.contains(field)) {
                continue;
            }
            if (customerSeq != null && customerSeq.containsKey(seq)) {
                seqList.add(seq - 1, customerSeq.get(seq));
            }
            seqList.add(field);
            seq++;
        }
        return seqList;
    }

    private void setCustomerSheet(ICustomerRowSet customerSet, List<List<Map<String, Object>>> sheetList,
            List<List<ExcelColunmValue>> fieldNameList) {
        RowSet rows = BoRowConvertUtil.toRowSet(customerSet);
        customerSet = null;
        HashMap<Integer, String> customerSeq = businessSeqList.get(0);
        // 获取客户字段顺序
        List<String> seqList = getSeqList(customerSeq, customerEditFields);
        seqList.add("createdDate");
        fieldNameList.add(getFieldNames(CustomerMetaData.EOName, seqList));
        List<Map<String, Object>> customerSheet = new ArrayList<Map<String, Object>>();
        for (Row row : rows.getItems()) {
            Map<String, Object> rowValue = getExcelRow(row, seqList, CustomerMetaData.EOName);
            customerSheet.add(rowValue);
        }

        sheetList.add(customerSheet);
    }

    private void setContactSheet(IBusinessObjectRowSet contacts, List<List<Map<String, Object>>> sheetList,
            List<List<ExcelColunmValue>> fieldNameList) {
        RowSet rows = BoRowConvertUtil.toRowSet(contacts);
        contacts = null;
        HashMap<Integer, String> customerSeq = businessSeqList.get(1);
        List<String> seqList = getSeqList(customerSeq, contactFields);
        seqList.add("owner");
        seqList.add("createdDate");
        List<String> trimedSeqList = new ArrayList<String>();
        for (String seq : seqList) {
        	trimedSeqList.add(seq.trim());
        }
        fieldNameList.add(getFieldNames(ContactMetaData.EOName, trimedSeqList));
        List<Map<String, Object>> contactSheet = new ArrayList<Map<String, Object>>();
        for (Row row : rows.getItems()) {
            contactSheet.add(getExcelRow(row, seqList, ContactMetaData.EOName));
        }
        sheetList.add(contactSheet);
    }

    private void setWorkRowSheet(List<List<String>> workRecordList, List<List<Map<String, Object>>> sheetList,
            List<List<ExcelColunmValue>> fieldNameList) {
        List<Map<String, Object>> workRecordSheet = new ArrayList<Map<String, Object>>();
        HashMap<Integer, String> customerSeq = businessSeqList.get(2);
        List<String> seqList = getSeqList(customerSeq, workRecordFields);
        fieldNameList.add(getFieldNames(WorkRecordMetaData.EOName, seqList));
        for (List<String> work : workRecordList) {
            Map<String, Object> data = new LinkedHashMap<String, Object>();
            data.put("customer", work.get(0));
            data.put("content", work.get(1));
            data.put("owner", work.get(2));
            data.put("createdBy", work.get(3));
            data.put("createdDate", work.get(4));
            workRecordSheet.add(data);
        }
        sheetList.add(workRecordSheet);
    }

    private ExportDataValue getBizData(ExportDataValue bizData) {
        List<PageRestObject> sheetPageList = new ArrayList<PageRestObject>();

        ExportCountValue exportCount = _getExportCount();

        Integer customerCountAll = 0;
        Integer contactCountAll = 0;
        Integer workRecordCountAll = 0;

        if (exportCount != null) {
            customerCountAll = exportCount.getCustomerCount();
            contactCountAll = exportCount.getContactCount();
            workRecordCountAll = exportCount.getWorkRecordCount();
        }
        if (customerCountAll > 65500) {
            customerCountAll = 65500;
        }
        if (contactCountAll > 65500) {
            contactCountAll = 65500;
        }
        if (workRecordCountAll > 65500) {
            workRecordCountAll = 65500;
        }

        Integer pageno = 0;
        Integer pagesize = MAX_QUERY_RESULT;

        int totalpageCustomer = (customerCountAll + pagesize - 1) / pagesize;
        int totalpageContact = (contactCountAll + pagesize - 1) / pagesize;
        int totalpageWorkRecord = (workRecordCountAll + pagesize - 1) / pagesize;

        int totalpage = totalpageCustomer;
        if (totalpageContact > totalpage) {
            totalpage = totalpageContact;
        }
        if (totalpageWorkRecord > totalpage) {
            totalpage = totalpageWorkRecord;
        }

        PageRestObject customerSheet = new PageRestObject();
        PageRestObject contactSheet = new PageRestObject();
        PageRestObject workRecordSheet = new PageRestObject();
        ExportDataValue bizDataTmp;
        logger.info("=============executeTaskById  biz start===================");
        if (totalpage < 1) {
            totalpage = 1;
        }
        for (int i = 0; i < totalpage; i++) {
            pageno = pageno + 1;
            customerSheet.setPageno(pageno);
            customerSheet.setPagesize(pagesize);
            contactSheet.setPageno(pageno);
            contactSheet.setPagesize(pagesize);
            workRecordSheet.setPageno(pageno);
            workRecordSheet.setPagesize(pagesize);
            sheetPageList.add(customerSheet);
            sheetPageList.add(contactSheet);
            sheetPageList.add(workRecordSheet);
            if (pagesize * pageno > (customerCountAll + pagesize)) {
                customerSheet.setPagesize(0);
            }
            if (pagesize * pageno > (contactCountAll + pagesize)) {
                contactSheet.setPagesize(0);
            }
            if (pagesize * pageno > (workRecordCountAll + pagesize)) {
                workRecordSheet.setPagesize(0);
            }
            logger.info("-----executeTaskById  biz i=" + i + "------start------");
            if (i == 0) {
                bizData = getExportData(sheetPageList);
            } else {
                bizDataTmp = getExportData(sheetPageList);
                if (bizDataTmp.getSheetDatas().get(0) != null) {// 累加第一个sheet数据
                    bizData.getSheetDatas().get(0).addAll(bizDataTmp.getSheetDatas().get(0));
                }
                if (bizDataTmp.getSheetDatas().get(1) != null) {// 累加第一个sheet数据
                    bizData.getSheetDatas().get(1).addAll(bizDataTmp.getSheetDatas().get(1));
                }
                if (bizDataTmp.getSheetDatas().get(2) != null) {// 累加第一个sheet数据
                    bizData.getSheetDatas().get(2).addAll(bizDataTmp.getSheetDatas().get(2));
                }
            }
            logger.info("-----executeTaskById  biz i=" + i + "-----end-----");
        }
        logger.info("=============executeTaskById  biz end===================");
        return bizData;
    }

    private Map<String, Integer> getWorkBook(IExportTaskRow exportTask, ExportDataValue bizData,
            HSSFWorkbook workbook) {
        Map<String, Integer> wbMap = new HashMap<String, Integer>();
        String sheet1Name = "客户信息";
        String sheet2Name = "联系人";
        String sheet3Name = "工作记录(" + DateUtil.formatTimeStamp(bizData.getStartDate(), "yyyy-MM-dd") + "至"
                + DateUtil.formatTimeStamp(bizData.getEndDate(), "yyyy-MM-dd") + ")";

        // 枚举下拉框初始化 暂不用
        Map<Integer, List<String>> enumDatas = null;
        // 预留参数 如控制邮箱 URL超链接样显示等
        Map<String, Object> paras = null;

        List<List<ExcelColunmValue>> titlesObjectList = bizData.getSheetFieldNames();

        List<ExcelColunmValue> sheet1TitleObjectList = titlesObjectList.get(0) == null
                ? new ArrayList<ExcelColunmValue>() : titlesObjectList.get(0);
        List<ExcelColunmValue> sheet2TitleObjectList = titlesObjectList.get(1) == null
                ? new ArrayList<ExcelColunmValue>() : titlesObjectList.get(1);
        List<ExcelColunmValue> sheet3TitleObjectList = titlesObjectList.get(2) == null
                ? new ArrayList<ExcelColunmValue>() : titlesObjectList.get(2);

        List<String> sheet1TitleValueList = new ArrayList<String>();
        List<String> sheet1TitleKeyList = new ArrayList<String>();
        ExcelColunmValue excelColunmValue;
        for (int i = 0; i < sheet1TitleObjectList.size(); i++) {
            excelColunmValue = sheet1TitleObjectList.get(i);
            sheet1TitleValueList.add(excelColunmValue.getFiledLaebl());
            sheet1TitleKeyList.add(excelColunmValue.getFiledName());
        }
        List<String> sheet2TitleValueList = new ArrayList<String>();
        List<String> sheet2TitleKeyList = new ArrayList<String>();
        for (int i = 0; i < sheet2TitleObjectList.size(); i++) {
            excelColunmValue = sheet2TitleObjectList.get(i);
            sheet2TitleValueList.add(excelColunmValue.getFiledLaebl());
            sheet2TitleKeyList.add(excelColunmValue.getFiledName());
        }
        List<String> sheet3TitleValueList = new ArrayList<String>();
        List<String> sheet3TitleKeyList = new ArrayList<String>();
        for (int i = 0; i < sheet3TitleObjectList.size(); i++) {
            excelColunmValue = sheet3TitleObjectList.get(i);
            sheet3TitleValueList.add(excelColunmValue.getFiledLaebl());
            sheet3TitleKeyList.add(excelColunmValue.getFiledName());
        }

        List<List<Map<String, Object>>> datasList = bizData.getSheetDatas();

        List<Map<String, Object>> sheet1DataList = datasList.get(0) == null ? new ArrayList<Map<String, Object>>()
                : datasList.get(0);
        List<Map<String, Object>> sheet2DataList = datasList.get(1) == null ? new ArrayList<Map<String, Object>>()
                : datasList.get(1);
        List<Map<String, Object>> sheet3DataList = datasList.get(2) == null ? new ArrayList<Map<String, Object>>()
                : datasList.get(2);

        int customerCount = sheet1DataList.size();
        int contactCount = sheet2DataList.size();
        int workRecordCount = sheet3DataList.size();

        wbMap.put("customerCount", customerCount);
        wbMap.put("contactCount", contactCount);
        wbMap.put("workRecordCount", workRecordCount);

        // try {
        logger.info("=============executeTaskById  workbook===================");

        HSSFCellStyle commStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.WHITE,
                POIColorConstants.BLACK);
        HSSFCellStyle firstColStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.BLUEH,
                POIColorConstants.BLACK);

        // 蓝底 黑字
        HSSFCellStyle titleStyle = ExportExcel.generateTitleCellStyle(workbook, POIColorConstants.BLUEH,
                POIColorConstants.BLACK);

        POIStylePara commStylePara = new POIStylePara();
        commStylePara.setCellStyle(commStyle);
        POIStylePara firstColStylePara = new POIStylePara();
        firstColStylePara.setCellStyle(firstColStyle);

        // sheet1 title style start
        POIStylePara POIStylePara1 = new POIStylePara();
        POIStylePara1.setCellStyle(titleStyle);
        POIStylePara1.setColStart(0);
        POIStylePara1.setColEnd(sheet1TitleValueList.size() - 1);

        List<POIStylePara> sheet1TitleStyleParas = new ArrayList<POIStylePara>();
        sheet1TitleStyleParas.add(POIStylePara1);
        // sheet1 title style end
        // sheet1 cell width
        List<Integer> sheet1ColWidths = new ArrayList<Integer>();
        for (int i = 0; i < sheet1TitleKeyList.size(); i++) {
            if ("address".equals(sheet1TitleKeyList.get(i))) {// 地址 定长
                sheet1ColWidths.add(8000);// 定长
            } else if ("remark".equals(sheet1TitleKeyList.get(i))) {// 备注 定长
                sheet1ColWidths.add(8000);// 定长
            } else {
                sheet1ColWidths.add(0);
            }
        }

        logger.info("=============executeTaskById  generateSheet1  start===================");
        // 生成sheet1
        ExportExcel.generateSheet(workbook, sheet1TitleValueList, sheet1ColWidths, sheet1Name, sheet1DataList,
                enumDatas, sheet1TitleStyleParas, firstColStylePara, commStylePara, paras);
        logger.info("=============executeTaskById  generateSheet1  end===================");
        // sheet2 title style start
        POIStylePara POIStylePara21 = new POIStylePara();
        POIStylePara21.setCellStyle(titleStyle);
        POIStylePara21.setColStart(0);
        POIStylePara21.setColEnd(sheet2TitleKeyList.size() - 1);

        List<POIStylePara> sheet2TitleStyleParas = new ArrayList<POIStylePara>();
        sheet2TitleStyleParas.add(POIStylePara21);
        // sheet2 title style end

        // sheet2 cell width
        List<Integer> sheet2ColWidths = new ArrayList<Integer>();
        for (int i = 0; i < sheet2TitleKeyList.size(); i++) {
            if ("address".equals(sheet2TitleKeyList.get(i))) {// 地址 定长
                sheet2ColWidths.add(8000);// 定长
            } else if ("remark".equals(sheet2TitleKeyList.get(i))) {// 备注 定长
                sheet2ColWidths.add(8000);// 定长
            } else {
                sheet2ColWidths.add(0);
            }
        }

        // 生成sheet2
        ExportExcel.generateSheet(workbook, sheet2TitleValueList, sheet2ColWidths, sheet2Name, sheet2DataList,
                enumDatas, sheet2TitleStyleParas, firstColStylePara, commStylePara, paras);

        // sheet3 title style start
        POIStylePara POIStylePara31 = new POIStylePara();
        POIStylePara31.setCellStyle(titleStyle);
        POIStylePara31.setColStart(0);
        POIStylePara31.setColEnd(sheet3TitleValueList.size() - 1);

        List<POIStylePara> sheet3TitleStyleParas = new ArrayList<POIStylePara>();
        sheet3TitleStyleParas.add(POIStylePara31);

        // sheet3 title style end

        // sheet3 cell width
        List<Integer> sheet3ColWidths = new ArrayList<Integer>();
        for (int i = 0; i < sheet3TitleKeyList.size(); i++) {
            if ("content".equals(sheet3TitleKeyList.get(i))) {// 沟通内容 定长
                sheet3ColWidths.add(12000);// 定长
            } else {
                sheet3ColWidths.add(0);
            }
        }

        // 生成sheet3
        ExportExcel.generateSheet(workbook, sheet3TitleValueList, sheet3ColWidths, sheet3Name, sheet3DataList,
                enumDatas, sheet3TitleStyleParas, firstColStylePara, commStylePara, paras);

        logger.info("=============executeTaskById  generateSheet3  end===================");
        return wbMap;

    }

    @Override
    public Map<String, Object> executeTaskById(Long id) {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        IExportTaskRow exportTask = query(id);

        ByteArrayOutputStream os = null;
        InputStream is = null;
        File tmpFile = null;

        Integer customerCount = 0;
        Integer contactCount = 0;
        Integer workRecordCount = 0;

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);

        try {
            Map<String, Object> para = new HashMap<String, Object>();
            para.put("id", id);

            List<IExportTaskRow> data = getExportTasks(para);

            if (data == null || data.size() == 0) {
                result.put("taskStatus", TASK.STATUS_0);
                return result;
            }

            exportTask = data.get(0);

            Long ts = exportTask.getTaskStatus();
            if (!TASK.STATUS_1.equals(ts)) {
                result.put("taskStatus", ts);// 不是新建状态下 不允许启动任务
                return result;
            }

            exportTask.setRunTime(DateUtil.getNowDateTime());
            exportTask.setTaskStatus(TASK.STATUS_2);
            // 更新任务状态为 正在执行 初始化执行开始时间
            upsert(exportTask);

            // 分页调取
            ExportDataValue bizData = new ExportDataValue();
            bizData = getBizData(bizData);
            exportTask.setStartDate(bizData.getStartDate());
            exportTask.setEndDate(bizData.getEndDate());
            HSSFWorkbook workbook = ExportExcel.generateWorkbook();
            // 组装excel
            Map<String, Integer> wbMap = getWorkBook(exportTask, bizData, workbook);
            customerCount = wbMap.get("customerCount");
            contactCount = wbMap.get("contactCount");
            workRecordCount = wbMap.get("workRecordCount");
            String backupCount = "{\"customerCount\":" + customerCount + ",\"contactCount\":" + contactCount
                    + ",\"workRecordCount\":" + workRecordCount + "}";
            exportTask.setBackupCount(backupCount);

            Map<String, Object> fileUploadMap = null;
            os = new ByteArrayOutputStream();
            workbook.write(os);

            is = new ByteArrayInputStream(os.toByteArray());

            tmpFile = File.createTempFile("export_info", ".xls");
            FileUtil.copyInputStreamToFile(is, tmpFile);
            Long now = DateUtil.getNowDateTime().getTime();
            String fileName = now.toString() + ".xls";
            fileUploadMap = OSSUtil.uploadFile(fileName, tmpFile);

            if (fileUploadMap != null) {
                Object backupFileUrl = fileUploadMap.get("url");
                Object fileSize = fileUploadMap.get("size");
                if (backupFileUrl != null && fileSize != null) {
                    uploadRsSave(exportTask, customerCount, contactCount, workRecordCount, backupFileUrl.toString(),
                            fileSize.toString(), TASK.STATUS_3, null);
                } else {
                    uploadRsSave(exportTask, customerCount, contactCount, workRecordCount,
                            backupFileUrl == null ? "" : backupFileUrl.toString(),
                            fileSize == null ? "" : fileSize.toString(), TASK.STATUS_9, "云端无返回url或size");
                }
            } else {
                uploadRsSave(exportTask, customerCount, contactCount, workRecordCount, null, null, TASK.STATUS_9,
                        "云端无返回");
            }

        } catch (IOException e) {
            e.printStackTrace();
            logger.error("IO E =" + e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = "e.getMessage() is null";
            } else {
                if (errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
            }
            uploadRsSave(exportTask, customerCount, contactCount, workRecordCount, null, null, TASK.STATUS_9,
                    errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(" E =" + e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = "e.getMessage() is null";
            } else {
                if (errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
            }
            uploadRsSave(exportTask, customerCount, contactCount, workRecordCount, null, null, TASK.STATUS_9,
                    errorMessage);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }

        result.put("id", id);
        result.put("taskStatus", exportTask.getTaskStatus());
        result.put("result", true);
        return result;
    }

    private IExportTaskRow uploadRsSave(IExportTaskRow exportTask, Integer customerCount, Integer contactCount,
            Integer workRecordCount, String backupFileUrl, String fileSize, Long taskStatus, String errorMessage) {
        // 备份成功或失败
        String backupCount = "{\"customerCount\":" + customerCount + ",\"contactCount\":" + contactCount
                + ",\"workRecordCount\":" + workRecordCount + "}";

        exportTask.setBackupCount(backupCount);
        exportTask.setBackupFileUrl(backupFileUrl);
        exportTask.setFinishTime(new Timestamp(new Date().getTime()));
        exportTask.setTaskStatus(taskStatus);// 或执行失败
        exportTask.setErrorMessage(errorMessage);

        Timestamp creTime = exportTask.getCreTime() == null ? new Timestamp(new Date().getTime())
                : exportTask.getCreTime();
        Timestamp runTime = exportTask.getRunTime() == null ? new Timestamp(new Date().getTime())
                : exportTask.getRunTime();// 如果查询组装备份数据异常 则 创建时间及runtime 为null
        Timestamp finishTime = exportTask.getFinishTime();

        Long backupTimeCost = finishTime.getTime() - creTime.getTime();
        Long taskTimeCost = finishTime.getTime() - runTime.getTime();
        exportTask.setBackupTimeCost(backupTimeCost);// 备份全程耗时
        exportTask.setTaskTimeCost(taskTimeCost);// 备份任务单独耗时
        exportTask.setFileSize(fileSize);

        upsert(exportTask);

        return exportTask;
    }

    @Override
    public Map<String, Object> getExportCount() {
        ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        ExportCountValue data = _getExportCount();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        if (data == null) {
            return result;
        }

        result.put("result", true);
        result.put("customerCount", data.getCustomerCount());
        result.put("contactCount", data.getContactCount());
        result.put("workRecordCount", data.getWorkRecordCount());

        Timestamp startDate = DateUtil.getNowDateTime();
        result.put("startDate", startDate);

        Timestamp endDate = null;
        Calendar cl = Calendar.getInstance();
        cl.setTime(new Date());
        cl.add(Calendar.MONTH, -3);
        endDate = new Timestamp(cl.getTime().getTime());
        result.put("endDate", endDate);
        return result;
    }

    @Override
    public Map<String, Object> download(Long id) {
        try {
            ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
            boolean result = true;
            String message = "";
            if (id == null) {
                // throw new BOApplicationException("id不能为空！");
                logger.error("请求参数id不能为空");
                message = "请求参数不能为空";
                result = false;
            }
            Map<String, Object> para = new HashMap<String, Object>();
            para.put("id", id);
            List<IExportTaskRow> data = getExportTasks(para);
            if (data == null || data.size() == 0) {
                // throw new BOApplicationException("id参数不合法！");
                logger.error("根据id不能为空" + id + "获得不到数据");
                message = "文件备份记录不存在！";
                result = false;
            }

            IExportTaskRow task = null;
            if (data != null) {
                task = data.get(0);
            }

            if (task == null) {
                // throw new BOApplicationException("文件备份记录不存在！");
                logger.error("根据id不能为空" + id + "获得不到数据");
                message = "文件备份记录不存在！";
                result = false;
            }
            String url = null;
            if (task != null) {
                url = task.getBackupFileUrl();
            }

            if (url == null || "".equals(url)) {
                // throw new BOApplicationException("文件下载地址不存在！");
                logger.error("备份记录的url为空！");
                message = "文件下载地址不存在！";
                result = false;
            }
            Map<String, Object> rs = new HashMap<String, Object>();
            rs.put("url", url);
            rs.put("result", result);
            rs.put("message", message);
            return rs;
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> rs = new HashMap<String, Object>();
            rs.put("url", "");
            rs.put("result", "false");
            rs.put("message", e.getMessage());
            return rs;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private Row visitCount(Long groupId, Long userId, String countDate) {
        if (StringUtils.isBlank(countDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            countDate = sdf.format(new Date());
        }
        Assert.checkCountDate(countDate, "app.report.visitCount.countDate.illege");

        List<Long> userIds = new ArrayList<Long>();
        Map<Long, Map<String, Object>> userInfo = new HashMap<Long, Map<String, Object>>();
        List<Map<String, Object>> groups = ReportUtil.getUserGroupInfo(groupId, userId, userInfo, userIds, countDate,
                "KQ", true);

        Row row = new Row();
        List<VisitExportData> datas = null;
        if (userIds != null && userIds.size() > 0) {
            datas = ServiceLocator.getInstance().lookup(CheckinServiceItf.class).getVisitCountExport(userIds,
                    countDate);
        }

        Iterator<Map<String, Object>> groupIt = groups.iterator();
        while (groupIt.hasNext()) {

            Map<String, Object> entry = groupIt.next();
            List<Long> tmpUserIds = (List<Long>) entry.get("childIds");
            List<Object> visitInfo = new ArrayList<Object>();

            for (Long tmpUserId : tmpUserIds) {// for 1
                Map<String, Object> m = userInfo.get(tmpUserId);
                if (datas != null) {
                    List<VisitExportData> visits = new ArrayList<VisitExportData>();
                    for (int i = 0; i < datas.size(); i++) {// for2
                        if (datas.get(i).getUserId().equals(tmpUserId)) {
                            visits.add(datas.get(i));
                        }
                    } // end for 2
                    if (visits != null && visits.size() > 0) {
                        m.put("visit", visits);
                    } else {
                        m.put("visit", null);
                    }
                    if ("disable".equals(m.get("status")) && visits.size() < 1) {
                        // XXXX如果是停用 并且无签到数据 则不添加
                        // 停用的无数据也放
                        visitInfo.add(m);
                    } else {
                        visitInfo.add(m);
                    }
                } else {// no visit data == null
                    if ("disable".equals(m.get("status"))) {
                        // XXXX如果是停用 并且无签到数据 则不添加
                        // 停用的无数据也放
                        visitInfo.add(m);
                    } else {
                        visitInfo.add(m);
                    }
                }
            } // end for 1
            entry.put("children", visitInfo);
        } // end while
        row.put("data", groups);
        return row;
    }

    private Map<String, Object> _visitCountExport(Long groupId, Long userId, String countDate) {
        // 获取外勤签到统计数据
        Map<String, Object> rsMap = visitCount(groupId, userId, countDate);
        // 组装EXCEL
        List<String> titles = new ArrayList<String>();
        titles.add("部门");
        titles.add("姓名");
        titles.add("客户");
        titles.add("时间");

        List<Integer> colWidths = new ArrayList<Integer>();
        colWidths.add(256 * 18);
        colWidths.add(256 * 18);
        colWidths.add(256 * 32);
        colWidths.add(256 * 16);
        String sheetName = "外勤签到";

        Map<Integer, List<String>> enumDatas = null;

        HSSFWorkbook workbook = ExportExcel.generateWorkbook();
        // 蓝底 黑字
        HSSFCellStyle titleStyle = ExportExcel.generateTitleCellStyle(workbook, POIColorConstants.BLUEH,
                POIColorConstants.BLACK);
        POIStylePara POIStylePara1 = new POIStylePara();
        POIStylePara1.setCellStyle(titleStyle);
        POIStylePara1.setColStart(0);
        POIStylePara1.setColEnd(titles.size() - 1);

        List<POIStylePara> titleStyleParas = new ArrayList<POIStylePara>();
        titleStyleParas.add(POIStylePara1);

        HSSFCellStyle commStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.WHITE,
                POIColorConstants.BLACK);
        POIStylePara commStylePara = new POIStylePara();
        commStylePara.setCellStyle(commStyle);

        Map<String, Object> paras = null;
        ExportExcel.generateVisitSheet(workbook, titles, colWidths, sheetName, rsMap, enumDatas, titleStyleParas,
                commStylePara, commStylePara, paras);

        ByteArrayOutputStream os = null;
        ByteArrayInputStream is = null;
        try {
            os = new ByteArrayOutputStream();
            workbook.write(os);
            is = new ByteArrayInputStream(os.toByteArray());
            String fileName = DateUtil.getNowDateTime().getTime() + ".xls";
            Map<String, Object> fileUploadMap = OSSUtil.uploadFile(fileName, is);
            return fileUploadMap;
        } catch (Exception e) {
            logger.error("attendanceCountExport Exception:{}", e);
            throw new AppException("app.common.server.error");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public Map<String, Object> visitCountExport(Long groupId, Long userId, String countDate) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        try {
            Map<String, Object> fileUploadMap = _visitCountExport(groupId, userId, countDate);
            if (fileUploadMap != null) {
                Object url = fileUploadMap.get("url");
                Object suffix = fileUploadMap.get("suffix");
                if (url != null) {
                    result.put("url", url);
                    result.put("suffix", suffix);
                    result.put("result", true);
                }
            }
        } catch (Exception e) {
            logger.error("visitCountExport exception:", e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getAttendanceData(Long groupId, Long userId, String countDate) {
        Map<String, Object> rsMap = new HashMap<String, Object>();
        Row row = ServiceLocator.getInstance().lookup(ReportServiceItf.class).attendanceCount(groupId, userId,
                countDate);

        List<Map<String, Object>> headers = (List<Map<String, Object>>) row.get("header");

        List<AttendanceExportHeader> titles = new ArrayList<AttendanceExportHeader>();
        AttendanceExportHeader header = null;
        for (int i = 0; i < headers.size(); i++) {
            header = new AttendanceExportHeader();
            header.setColumnLabel((String) headers.get(i).get("columnLabel"));
            header.setColumnName((String) headers.get(i).get("columnName"));
            titles.add(header);
        }
        rsMap.put("titles", titles);

        List<Map<String, Object>> datas = (List<Map<String, Object>>) row.get("data");

        List<AttendanceExportMergeDownRow> mergeDownRows = new ArrayList<AttendanceExportMergeDownRow>();
        AttendanceExportMergeDownRow mergeDownRow = null;
        for (int i = 0; i < datas.size(); i++) {
            mergeDownRow = new AttendanceExportMergeDownRow();
            mergeDownRow.setParentName((String) datas.get(i).get("name"));
            mergeDownRow.setParentuserId(datas.get(i).get("userId").toString());
            mergeDownRow.setUserRole("userRole");
            List<Map<String, Object>> childrens = (List<Map<String, Object>>) datas.get(i).get("children");
            AttendanceExportData exportData = null;
            for (int j = 0; j < childrens.size(); j++) {
                exportData = new AttendanceExportData();
                exportData.setActualTimes(childrens.get(j).get("actualTimes") == null ? "--"
                        : childrens.get(j).get("actualTimes").toString());
                exportData.setEmail((String) childrens.get(j).get("email"));
                exportData.setHeadPicture((String) childrens.get(j).get("headPicture"));
                exportData.setLateTimes(childrens.get(j).get("lateTimes") == null ? "--"
                        : childrens.get(j).get("lateTimes").toString());
                exportData.setLeaveEarlyTimes(childrens.get(j).get("leaveEarlyTimes") == null ? "--"
                        : childrens.get(j).get("leaveEarlyTimes").toString());
                exportData.setMobile((String) childrens.get(j).get("mobile"));
                exportData.setName((String) childrens.get(j).get("name"));
                exportData.setParentId(
                        childrens.get(j).get("parentId") == null ? "" : childrens.get(j).get("parentId").toString());
                exportData.setUserId(
                        childrens.get(j).get("userId") == null ? "" : childrens.get(j).get("userId").toString());
                exportData.setUserLevel(
                        childrens.get(j).get("userLevel") == null ? "" : childrens.get(j).get("userLevel").toString());
                exportData.setUserRole((String) childrens.get(j).get("userRole"));
                // 部门 姓名 实际 迟到 早退 是固定的 不要按序循环
                AttendanceExportDayData dayData = null;
                for (int k = 5; k < titles.size(); k++) {
                    dayData = new AttendanceExportDayData();
                    // 去取 01 02 03 04号 的header
                    Map<String, Object> dayMap = (Map<String, Object>) childrens.get(j)
                            .get(titles.get(k).getColumnName());
                    if (dayMap == null) {
                        dayData.setAbsence("0");
                    } else {
                        dayData.setAbsence("1");
                        // "04":{"onTime":"09:02:03","onAbnormalTime":0,"offTime":"17:02:03","offStatus":0,"onStatus":0,"offAbnormalTime":0},
                        dayData.setOffAbnormalTime(
                                dayMap.get("offAbnormalTime") == null ? "" : dayMap.get("offAbnormalTime").toString());
                        dayData.setOffStatus(dayMap.get("offStatus") == null ? "" : dayMap.get("offStatus").toString());
                        dayData.setOffTime(dayMap.get("offTime") == null ? "未打卡" : dayMap.get("offTime").toString());
                        dayData.setOnAbnormalTime(
                                dayMap.get("onAbnormalTime") == null ? "" : dayMap.get("onAbnormalTime").toString());
                        dayData.setOnStatus(dayMap.get("onStatus") == null ? "" : dayMap.get("onStatus").toString());
                        dayData.setOnTime(dayMap.get("onTime") == null ? "未打卡" : dayMap.get("onTime").toString());
                    }
                    exportData.getDaysData().add(dayData);
                }
                mergeDownRow.getExportDataList().add(exportData);
            }
            mergeDownRows.add(mergeDownRow);
        }
        rsMap.put("mergeDownRows", mergeDownRows);
        return rsMap;

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> _attendanceCountExport(Long groupId, Long userId, String countDate) {
        Map<String, Object> rsMap = getAttendanceData(groupId, userId, countDate);

        logger.info("attendanceCountExport getAttendanceData end");
        List<AttendanceExportHeader> titles = (List<AttendanceExportHeader>) rsMap.get("titles");
        List<AttendanceExportMergeDownRow> mergeDownRows = (List<AttendanceExportMergeDownRow>) rsMap
                .get("mergeDownRows");
        logger.info("attendanceCountExport getAttendanceData GOT end");
        HSSFWorkbook workbook = ExportExcel.generateWorkbook();
        String sheetName = "考勤统计";
        HSSFCellStyle commStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.WHITE,
                POIColorConstants.BLACK);
        HSSFCellStyle titleStyle = ExportExcel.generateTitleCellStyle(workbook, POIColorConstants.BLUEH,
                POIColorConstants.BLACK);
        ExportExcel.generateAttendanceSheet(workbook, titles, mergeDownRows, sheetName, titleStyle, commStyle);
        logger.info("attendanceCountExport generateAttendanceSheet end");

        ByteArrayOutputStream os = null;
        ByteArrayInputStream is = null;
        try {
            os = new ByteArrayOutputStream();
            workbook.write(os);
            is = new ByteArrayInputStream(os.toByteArray());
            String fileName = DateUtil.getNowDateTime().getTime() + ".xls";
            Map<String, Object> fileUploadMap = OSSUtil.uploadFile(fileName, is);
            return fileUploadMap;
        } catch (Exception e) {
            logger.error("attendanceCountExport Exception:", e);
            throw new AppException("app.common.server.error");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    @Override
    public Map<String, Object> attendanceCountExport(Long groupId, Long userId, String countDate) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        try {
            Map<String, Object> fileUploadMap = _attendanceCountExport(groupId, userId, countDate);
            if (fileUploadMap != null) {
                Object url = fileUploadMap.get("url");
                Object suffix = fileUploadMap.get("suffix");
                if (url != null) {
                    result.put("url", url);
                    result.put("suffix", suffix);
                    result.put("result", true);
                }
            }
        } catch (Exception e) {
            logger.error("attendanceCountExport exception:", e);
        }
        return result;
    }
}
