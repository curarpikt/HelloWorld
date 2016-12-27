package com.chanapp.chanjet.customer.service.operationlog;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogHome;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRow;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRowSet;
import com.chanapp.chanjet.customer.constant.OP;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.OperationLogMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.http.HttpResponse;
import com.chanapp.chanjet.customer.reader.BigDataReader;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.HttpUtil;
import com.chanapp.chanjet.customer.util.ShortIdUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.ccs.api.common.DeviceInfo;
import com.chanjet.csp.ccs.api.common.DeviceType;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class OperationLogServiceImpl extends BoBaseServiceImpl<IOperationLogHome, IOperationLogRow, IOperationLogRowSet>
        implements OperationLogServiceItf {
    private static Logger logger = LoggerFactory.getLogger(OperationLogServiceImpl.class);

    /**
     * 获取操作类型
     * 
     * @param key
     * @return Long
     */
    private String getOperateType(IBusinessObjectRow row, boolean preOperation) {
        Long id = (Long) row.getFieldValue(SC.id);
        if (id == 0L) {
            return OP.SAVE;
        } else {
            return OP.UPDATE;
        }
    }

    @Override
    public void generate(IBusinessObjectRow row) {
        generate(row, getOperateType(row, false));

    }

    /**
     * 指定 变更类型 记录变更日志
     * 
     * @param row
     * @param operateType
     */
    @Override
    public void generate(IBusinessObjectRow row, String operationType) {
        IOperationLogRow logRow = this.createRow();
        // 设置实体主键
        logRow.setEntityID((Long) row.getFieldValue("id"));
        // 设置实体名称
        logRow.setEntityType(row.getDefinition().getPrimaryEO().getName());
        // 设置操作类型
        logRow.setOperateType(operationType);
        // 设置操作时间
        logRow.setOperateTime(DateUtil.getNowDateTime());
        this.upsert(logRow);
    }

    /**
     * 指定 变更类型 记录变更日志
     * 
     * @param session
     * @param row
     * @param operateType
     */
    @Override
    public void generate(Long id, String entityName, String operateType) {
        IOperationLogRow logRow = this.createRow();
        // 设置实体主键
        logRow.setEntityID(id);
        // 设置实体名称
        logRow.setEntityType(entityName);
        // 设置操作类型
        logRow.setOperateType(operateType);
        // 设置操作时间
        logRow.setOperateTime(DateUtil.getNowDateTime());
        this.upsert(logRow);
    }

    @Override
    public void grantLogbatch(List<Long> customerIds, Long userId, String operTag, String operType) {
        IOperationLogRowSet rowSet = this.createRowSet();
        for (Long customerId : customerIds) {
            IOperationLogRow operationLog = this.createRow();
            operationLog.setEntityID(customerId);
            operationLog.setEntityType(CustomerMetaData.EOName);
            operationLog.setOperateType(operType);
            operationLog.setOperator(userId);
            operationLog.setOperateTime(DateUtil.getNowDateTime());
            operationLog.setOperTag(operTag);
            rowSet.addRow(operationLog);
            IOperationLogRow addLog = this.createRow();
            addLog.setEntityID(customerId);
            addLog.setEntityType(CustomerMetaData.EOName);
            addLog.setOperateType(OP.UPDATE);
            addLog.setOperateTime(DateUtil.getNowDateTime());
            rowSet.addRow(addLog);
        }
        batchInsert(rowSet, false);
    }

    /**
     * 大数据打点
     */
    @Override
    public void writeMsg2BigData(Long entityId, String entityType, int i) {
        String orgId = EnterpriseContext.getOrgId();
        String appId = EnterpriseContext.getAppId();
        String userId = EnterpriseContext.getCurrentUser().getUserId();
        String topic = "customer";
        String interfaceUrl = BigDataReader.getUpitemDetailDataUrl() + "?topic=" + topic + "&id=" + userId;

        Map<String, Object> map = new HashMap<String, Object>();
        // 数据接口类型 (现在此都是 1)
        map.put("ift", "4");
        String clientType = "2";
        DeviceInfo device = EnterpriseContext.getRequestDeviceInfo();
        String deviceType = "-";
        if (null != device) {
            DeviceType type = device.getDeviceType();
            deviceType = type.name();
        }
        logger.info("deviceType = {}", deviceType);
        if ("Android".equals(deviceType)) {
            clientType = "21";
        } else if ("Apple".equals(deviceType)) {
            clientType = "22";
        }
        // 客户端类型
        map.put("ct", clientType);
        // 产品ID
        map.put("pd", appId);
        // 用户id
        map.put("id", userId);
        // 企业ID
        map.put("cp", orgId);
        // 渠道标识
        map.put("tg", "");
        // 服务端版本
        String appVersion = AppWorkManager.getWebAppVersion();// "1.0.0";
        map.put("cv", appVersion);

        Map<String, String> iMap = new HashMap<String, String>();
        // 留给开发者使用的特殊变动值/默认为空值
        iMap.put("d", entityId + "");
        // 上报项id
        String str = "0000000000";

        String fmtAppId = str.substring(0, 4 - appId.length()) + appId;
        // String fmtClientType = str.substring(0, 3 - clientType.length()) +
        // clientType;
        String fmtClientType = "000";
        String msgId = fmtAppId + "" + fmtClientType + "" + entityType;
        iMap.put("i", msgId);
        // 前导上报项id
        iMap.put("p", "");
        // 上报项值或动态变更值或扩展值
        iMap.put("v", "1");
        // 上报项类型（共分：1,2,3,4,5,6,7,8）
        iMap.put("t", "1");
        // 上报时时间
        Date date = new Date();
        iMap.put("ti", DateUtil.getDateStringByFormat(date, "yyyyMMddHHmmss"));
        // 一次性行为下的带有结构化的数据值
        iMap.put("n", "");
        List<Map<String, String>> ilist = new ArrayList<Map<String, String>>();
        ilist.add(iMap);

        map.put("l", ilist);
        Boolean result = false;
        try {
            int timeout = 1000;
            String data = dataManager.toJSONString(map);
            String url = interfaceUrl + "&data=" + HttpUtil.urlEncode(data);
            Map<String, String> header = new HashMap<String, String>();
            header.put("Content-Type", "application/json");
            HttpResponse response = HttpUtil.doGet(url, header, null, timeout);

            logger.info("url = {} ", interfaceUrl);
            logger.info("params = {} ", data);
            logger.info("response = {} ", response.getString());

            if (200 == response.getStatusCode()) {
                Map<String, Object> tokenMap = dataManager.jsonStringToMap(response.getString());
                String accepted = (String) tokenMap.get("accepted");
                if ("OK".equals(accepted)) {
                    result = true;
                } else {
                    String error = (String) tokenMap.get("error");
                    //System.out.println("writeMsg2BigData error = " + error);
                }
                logger.info("accepted = {} ", accepted);
                //System.out.println("accepted = " + accepted);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("writeMsg2BigData", e);
        }
        try {
            if (!result) {
                i++;
                if (i < 2) {
                    logger.info("writeMsg2BigData entityType = {} repeat = {} times", entityType, i);
                    writeMsg2BigData(entityId, entityType, i);
                } else {
                    logger.info("writeMsg2BigData faild : appId = " + appId + ", orgId = " + orgId + ", userId" + userId
                            + ", createdDate" + new Date());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("writeMsg2BigData", e);
        }
    }

    @Override
    public void grantLog(Long workRecordId, Long userId) {
        IOperationLogRow operationLog = this.createRow();
        operationLog.setEntityID(workRecordId);
        operationLog.setEntityType(WorkRecordMetaData.EOName);
        operationLog.setOperateType(OP.GRANT);
        operationLog.setOperator(userId);
        operationLog.setOperateTime(DateUtil.getNowDateTime());
        this.upsert(operationLog);
    }

    @Override
    public void saveBatchDeleteLog(String reason, List<Long> ids) {
        Map<String, Object> contentMap = new HashMap<String, Object>();
        contentMap.put("reason", reason);
        contentMap.put("ids", ids);
        IOperationLogRow operationLog = createRow();
        operationLog.setContent(dataManager.toJSONString(contentMap));
        operationLog.setEntityType(CustomerMetaData.EOName);
        operationLog.setOperateType(OP.MUTIDELETE);
        operationLog.setOperateTime(DateUtil.getNowDateTime());
        upsert(operationLog);
    }

    @Override
    public void generateBatch(List<Long> ids, String entityName, String operateType) {
        IOperationLogRowSet rowSet = createRowSet();
        for (Long id : ids) {
            IOperationLogRow logRow = createRow();
            // 设置实体主键
            logRow.setEntityID(id);
            // 设置实体名称
            logRow.setEntityType(entityName);
            // 设置操作类型
            logRow.setOperateType(operateType);
            // 设置操作时间
            logRow.setOperateTime(DateUtil.getNowDateTime());
            rowSet.addRow(logRow);
        }
        batchInsert(rowSet, false);
    }

    @Override
    public void grantLog(Long customerId, Long userId, String operTag, String operType) {
        IOperationLogRow operationLog = createRow();
        operationLog.setEntityID(customerId);
        operationLog.setEntityType(CustomerMetaData.EOName);
        operationLog.setOperateType(operType);
        operationLog.setOperator(userId);
        operationLog.setOperateTime(DateUtil.getNowDateTime());
        operationLog.setOperTag(operTag);
        upsert(operationLog);
        IOperationLogRow addLog = createRow();
        addLog.setEntityID(customerId);
        addLog.setEntityType(CustomerMetaData.EOName);
        addLog.setOperateType(OP.UPDATE);
        addLog.setOperateTime(DateUtil.getNowDateTime());
        upsert(addLog);
    }

    @Override
    public IOperationLogRowSet getHistoryMultiDelete() {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq(OperationLogMetaData.operateType, OP.MUTIDELETE);
        jsonQueryBuilder.addCriteria(criteria);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public List<Map<String, Object>> findAllLastLog(Long version) {
        List<Map<String, Object>> result = null;
        if (version == null) {
            String hql = "select distinct entityID, entityType , operateType,operator from " + getBusinessObjectId()
                    + " group by entityID, entityType, operateType,operator";
            result = runCQLQuery(hql);
        } else {
            HashMap<String, Object> paraMap = new HashMap<String, Object>();
            String hql = "select distinct entityID, entityType, operateType,operator from " + getBusinessObjectId()
                    + " where operateTime > :operateTime group by entityID, entityType, operateType,operator";
            Timestamp lastDate = new Timestamp(version);
            paraMap.put("operateTime", lastDate);
            result = runCQLQuery(hql, paraMap);
        }

        return result;
    }
    
    @Override
    public String transLog(List<Long> customerIds,Long userId,String content,String entityName){
		String operTag = ShortIdUtil.generateShortUuid();
		IOperationLogRowSet rowSet = this.createRowSet();
		for (Long id : customerIds) {
			IOperationLogRow operationLog =this.createRow();
			operationLog.setEntityID( id);
			operationLog.setEntityType( entityName);
			operationLog.setOperateType(OP.TRANS);
			operationLog.setOperTag(operTag);
			operationLog.setOperator(userId);
			operationLog.setOperateTime(DateUtil.getNowDateTime());
			operationLog.setContent( content);
			rowSet.addRow(operationLog);
			IOperationLogRow addLog =this.createRow();
			addLog.setEntityID( id);
			addLog.setEntityType( CustomerMetaData.EOName);
			addLog.setOperateType( OP.UPDATE);
			addLog.setOperateTime( DateUtil.getNowDateTime());
			rowSet.addRow(addLog);
		}
		this.batchInsert(rowSet);
		return operTag;	
    }

}
