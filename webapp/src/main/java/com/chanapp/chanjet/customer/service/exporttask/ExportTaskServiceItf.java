package com.chanapp.chanjet.customer.service.exporttask;

import java.util.Map;

import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskHome;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskRow;
import com.chanapp.chanjet.customer.businessobject.api.exporttask.IExportTaskRowSet;
import com.chanapp.chanjet.customer.vo.LoadMoreList;

public interface ExportTaskServiceItf extends BoBaseServiceItf<IExportTaskHome, IExportTaskRow, IExportTaskRowSet> {
    Map<String, Object> getExportTaskById(Long id);

    LoadMoreList taskList(Integer pageNo, Integer pageSize);

    Map<String, Object> getExportTasksToday();

    Map<String, Object> saveTask();

    Map<String, Object> executeTaskById(Long id);

    Map<String, Object> getExportCount();

    Map<String, Object> download(Long id);

    Map<String, Object> visitCountExport(Long groupId, Long userId, String countDate);

    Map<String, Object> attendanceCountExport(Long groupId, Long userId, String countDate);
}
