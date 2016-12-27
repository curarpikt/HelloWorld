package com.chanapp.chanjet.customer.service.checkin;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinHome;
import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinRow;
import com.chanapp.chanjet.customer.businessobject.api.checkin.ICheckinRowSet;
import com.chanapp.chanjet.customer.service.exporttask.VisitExportData;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface CheckinServiceItf extends BoBaseServiceItf<ICheckinHome, ICheckinRow, ICheckinRowSet> {

    Map<Long, Object> getCheckinUsers(List<Long> userIds, String countDate);

    Map<Long, Object> getCheckinVisit(List<Long> userIds, String countDate, String countType);

    Integer getWeekOfYear(String date);

    List<Map<String, Object>> getCheckinVisitDetail(Long userId, Long startDate, Long endDate);

    List<VisitExportData> getVisitCountExport(List<Long> userIds, String countDate);

    void deleteCheckin(Long id);

    Row updateCheckin(LinkedHashMap<String, Object> value);

    Row addCheckin(LinkedHashMap<String, Object> value);

	String addCheckInForH5(String payload);

	String getCheckinDetail(Long id);

	String getCheckinList(Integer first, Integer max, Map<String, Object> para);

	boolean checkCheckinByCustomerId(Long customerId);
	
	

}
