package com.chanapp.chanjet.customer.service.importrecord;

import java.util.List;

import com.chanjet.csp.bo.api.BoSession;

public interface ImportServiceItf {
	void importTask(List<List<ImportData>> dataList, Long taskId,BoSession session);
}
