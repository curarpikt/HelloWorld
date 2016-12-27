package com.chanapp.chanjet.customer.service.customer;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerHome;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRowSet;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.vo.PageRestObject;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.annotation.NotSingleton;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

@NotSingleton
public interface CustomerServiceItf extends BoBaseServiceItf<ICustomerHome, ICustomerRow, ICustomerRowSet> {
    ICustomerRow addCustomer(LinkedHashMap<String, Object> customerParam);

    ICustomerRow updateCustomer(LinkedHashMap<String, Object> customerParam);

    void deleteCustomer(Long id);

    Map<String, Object> getExistsCustomer(Long customerId, String name, String phone);

    void updateCustomerStatus(ICustomerRow customerRow, IWorkRecordRow workRecordRow, boolean isInsert);

    Integer countCustomers(String params);

    List<Row> customerForCombox(String param, Integer pageNo, Integer pageSize);

    List<Long> getCustomerIdsByCondtion(String criteria);

    Object deleteByIds(List<Long> ids, String reason);

    /**
     * 通过客户id获取客户信息；如果isdelete==true 返回null
     * 
     * @param id
     * @return
     */
    ICustomerRow getCustomerById(Long id);

    Map<Long, Long> getCustomerCountByUser();

    RowSet customerList(String criteria, Integer pageNo, Integer pageSize);

    void shareCustomer(Map<String, Object> value);

    void unShareCustomer(Map<String, Object> value);

/*    List<Long> getDelCustomerIds();*/

    List<ICustomerRow> getCustomerByIds(List<Long> ids);

    ICustomerRow findByIdWithOutAuth(Long id);

    ICustomerRowSet getCustomerDeleted();

    List<ICustomerRow> getDeletedCustomerByIds(ICustomerRowSet customerRowSet, List<Long> customerIds);

    List<Map<String, Object>> getAddCustomerAnalysisData(String startdate, String enddate);

    RowSet getCompositionAnalysisData(String enumName, Long userId, String condtion);

    List<Map<String, Object>> customerProgressCount(Long userId, Date startDate, Date endDate);

    List<Map<String, Object>> progressCountCustomer();

    Integer countAll();

    ICustomerRowSet findAllWithPage(PageRestObject page);

    ICustomerRow getCustomerByName(String name);

    List<UserValue> unsharedUsers(Long customerId);

    List<UserValue> getSharedUsers(Long customerId);

    Map<String, Object> getCustomersByNameAndPhone(String name, String phone);

    Map<Long, Set<Long>> customersRefGrants(List<Long> customerIds);

    Map<String, Object> addCustomerWithMerge(LinkedHashMap<String, Object> customerValue);

    void updateCustomerCoordinate(Long customerId, Map<String, Double> coordinate, String coordinateNote);

	Map<String, Object> queryCustomerListByKeyword(int first, int max, String keyword);

	Map<String, Object> getCustomerDetail(Long customerId);

	String getCheckInCustomer(Double latitude, Double longitude, Integer first, Integer max);

	List<Long> getCustomerIdsByKeyWord(String searchtext);

}
