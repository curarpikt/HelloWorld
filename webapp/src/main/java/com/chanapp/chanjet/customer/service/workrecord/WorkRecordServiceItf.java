package com.chanapp.chanjet.customer.service.workrecord;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordHome;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRowSet;
import com.chanapp.chanjet.customer.vo.LoadMoreList;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public interface WorkRecordServiceItf extends BoBaseServiceItf<IWorkRecordHome, IWorkRecordRow, IWorkRecordRowSet> {
    void delByCustomerId(Long customerId);

    void shareByContent(String content, Long workRecordId);

    IBusinessObjectRow findByIdAndCusWithAuth(Long id);

    Map<Long, Object> getDelNumByCustomerId(List<Long> customerIds);

    List<CustomerProcess> getCustomerProgress(Long customerId);

    /**
     * 根据客户ID和工作记录的状态，返回满足要求的非逻辑删除的一页的工作记录信息。
     * 
     * @param customerId 客户ID
     * @param status 工作记录的状态
     * @param pageNo 页号
     * @param pageSize 页面记录数
     * @return workRecords 满足条件的工作记录
     */
    IWorkRecordRowSet queryByCustomer(Long customerId, String status, int pageNo, int pageSize, String order);

    IWorkRecordRow addWorkRecord(LinkedHashMap<String, Object> workRecordParam, Row retRow);

    LoadMoreList queryWordRecrod(String queryValue, String queryType, Integer pageNo, Integer pageSize);

    void deleteWorkRecord(Long workRecordId);

    LoadMoreList findCustomerWorkRecords(Long id, Integer pageNo, Integer pageSize, String status);

    Row findWorkRecord(Long id);

    int latestRecordForWeb();

    void read();

    LoadMoreList getFollows(Integer pageno, Integer pagesize);

    IWorkRecordRow findByIdWithOutAuth(Long id);

    IWorkRecordRowSet getWorkRecordDeleted();

    List<Map<String, Object>> progressCountWorkrecord(Date startDate, Date endDate);

    Integer countByTime(Long version);

    List<List<String>> getWorkRecordExcelRow(Long version, PageRestObject page);

    Map<Long, List<Long>> getIdMapByCustomer(List<Long> ids);

    List<Long> getIdListByCustomerIdList(List<Long> ids);

    List<Long> getAllWorkReocrdIds();

    IWorkRecordRow updateWorkRecord(String workRecordValue, Row retRow);

	IWorkRecordRow updateWorkRecord(String workRecordValue, Row retRow, Long id);

	List<IBusinessObjectRow> getAllWorkReocrdSet();

	String getWorkRecordList(Integer first, Integer max, Map<String, Object> para);

	IWorkRecordRow updateWorkRecordForH5(String workRecordValue, Row retRow, Long id);

}
