package com.chanapp.chanjet.customer.service.operationlog;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogHome;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRow;
import com.chanapp.chanjet.customer.businessobject.api.operationlog.IOperationLogRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public interface OperationLogServiceItf
        extends BoBaseServiceItf<IOperationLogHome, IOperationLogRow, IOperationLogRowSet> {
    void generate(IBusinessObjectRow row);

    void generate(IBusinessObjectRow row, String operationType);

    void writeMsg2BigData(Long entityId, String entityType, int i);

    void grantLog(Long workRecordId, Long userId);

    void saveBatchDeleteLog(String reason, List<Long> ids);

    void generateBatch(List<Long> ids, String entityName, String operateType);

    void generate(Long id, String entityName, String operateType);

    void grantLogbatch(List<Long> customerIds, Long userId, String operTag, String operType);

    void grantLog(Long customerId, Long userId, String operTag, String operType);

    IOperationLogRowSet getHistoryMultiDelete();

    List<Map<String, Object>> findAllLastLog(Long version);

	String transLog(List<Long> customerIds, Long userId, String content, String entityName);
    
    
}
