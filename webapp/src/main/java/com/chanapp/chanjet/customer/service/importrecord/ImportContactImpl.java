package com.chanapp.chanjet.customer.service.importrecord;

import java.util.LinkedHashMap;
import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IPT;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.ImportRecordNewMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.bo.api.BoSession;

public class ImportContactImpl extends ImportServiceImpl {
	private boolean importError = false;

	@Override
	public void importData(List<ImportData> data, BoSession localSession) {

		LinkedHashMap<String, Object> rowMap = new LinkedHashMap<String, Object>();
		try {
			for (ImportData row : data) {
				if (row.getField().startsWith("Customer_")) {
					ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
							.getCustomerByName(row.getValue());
					if (customer == null) {
						row.setErrorType(IPT.ERRORTYPE_NON);
						row.setMsg(IPT.MSG_NON);
						this.importError = true;
					} else {
						rowMap.put(ContactMetaData.customer, customer.getId());
					}
				} else {
					Object value = row.getInsertValue() == null?row.getValue():row.getInsertValue();
					rowMap.put(row.getField(), value);
				}
			}
			ServiceLocator.getInstance().lookup(ContactServiceItf.class).addContact(rowMap);
		} catch (Exception e) {
			this.importError = true;
			e.printStackTrace();
		}

	}

	@Override
	public Long getSheetNo() {
		return 1l;
	}

	@Override
	public boolean isImportError() {
		return importError;
	}

	@Override
	public void updateImportNumField(Long taskId, Long total, Long error) {
        String json = JsonQuery.getInstance().setCriteriaStr(SC.id + "=" + taskId).toString();
        ServiceLocator.getInstance().lookup(BO.ImportRecordNew).batchUpdate(json,
                new String[] { ImportRecordNewMetaData.totalContact,ImportRecordNewMetaData.errorContactCount},  new Long[] { total,error});
	}






}
