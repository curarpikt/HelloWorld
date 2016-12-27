package com.chanapp.chanjet.customer.service.importrecord;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IPT;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.ImportRecordNewMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.BoSession;
import com.chanjet.csp.common.base.util.StringUtils;

public class ImportCustomerImpl extends ImportServiceImpl {

	private boolean importError = false;
	private List<String> customerNameList = new ArrayList<String>();

	@Override
	public void importData(List<ImportData> data, BoSession localSession) {
		LinkedHashMap<String, Object> rowMap = new LinkedHashMap<String, Object>();
		ImportData nameData = null;
		try {
			for (ImportData row : data) {
				if (CustomerMetaData.name.equals(row.getField())) {
					nameData = row;
				}
				Object value = row.getInsertValue() == null?row.getValue():row.getInsertValue();
				rowMap.put(row.getField(), value);
			}
			rowMap.put(CustomerMetaData.localId, new Date().getTime());
			ServiceLocator.getInstance().lookup(CustomerServiceItf.class).addCustomer(rowMap);
		} catch (Exception e) {
			e.printStackTrace();
			if (e.toString().indexOf("app.customer.name.duplicated") != -1) {
				nameData.setMsg(IPT.MSG_CEXISTS);
				nameData.setErrorType(IPT.ERRORTYPE_CEXISTS);
			}
			this.importError = true;
		}

	}

	@Override
	public Long getSheetNo() {
		return 0l;
	}

	@Override
	public boolean isImportError() {
		return importError;
	}

	/*
	 * @Override public boolean commonVaild(List<ImportData> dataRows, BoSession
	 * localSession) { boolean error = false; if
	 * (super.vaildImportData(dataRows, localSession)) { return true; } else {
	 * for (ImportData dataRow : dataRows) { String fieldName =
	 * dataRow.getField(); switch (fieldName) { case SC.owner: List<Long>
	 * ownerIdList = ServiceLocator.getInstance().lookup(UserServiceItf.class)
	 * .getUserIdByName(dataRow.getValue()); if (ownerIdList == null ||
	 * ownerIdList.size() == 0) { dataRow.setMsg(IPT.MSG_NON);
	 * dataRow.setErrorType(IPT.ERRORTYPE_NON); error = true; } else if
	 * (ownerIdList.size() != 1) { dataRow.setMsg(IPT.MSG_MULTI);
	 * dataRow.setErrorType(IPT.ERRORTYPE_MULTI); error = true; } else { String
	 * ownerId = ownerIdList.get(0).toString(); dataRow.setValue(ownerId); }
	 * break; case CustomerMetaData.name: if
	 * (StringUtils.isEmpty(dataRow.getValue())) { dataRow.setMsg(IPT.MSG_NULL);
	 * dataRow.setErrorType(IPT.ERRORTYPE_NULL); error = true; } String
	 * customerName = dataRow.getValue().trim(); if
	 * (customerNameList.contains(customerName)) {
	 * dataRow.setMsg(IPT.MSG_CEXISTS);
	 * dataRow.setErrorType(IPT.ERRORTYPE_CEXISTS); error = true; }else {
	 * customerNameList.add(customerName); } break; default: break; } } return
	 * error; } }
	 */
	@Override
	public boolean specialFiledVaild(Map<String, ImportData> dataMap, BoSession localSession) {
		ImportData dataRow = dataMap.get(CustomerMetaData.name);
		if (StringUtils.isEmpty(dataRow.getValue())) {
			dataRow.setMsg(IPT.MSG_NULL);
			dataRow.setErrorType(IPT.ERRORTYPE_NULL);
			return true;
		}
		String customerName = dataRow.getValue().trim();
		if (customerNameList.contains(customerName)) {
			dataRow.setMsg(IPT.MSG_CEXISTS);
			dataRow.setErrorType(IPT.ERRORTYPE_CEXISTS);
			return true;
		} else {
			customerNameList.add(customerName);
		}
		return false;

	}

	@Override
	public void updateImportNumField(Long taskId, Long total, Long error) {
        String json = JsonQuery.getInstance().setCriteriaStr(SC.id + "=" + taskId).toString();
        ServiceLocator.getInstance().lookup(BO.ImportRecordNew).batchUpdate(json,
                new String[] { ImportRecordNewMetaData.totalCustomer,ImportRecordNewMetaData.errorCustomerCount }, new Long[] { total,error});
		
	}


}
