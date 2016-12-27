package com.chanapp.chanjet.customer.service.importrecordnew;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewHome;
import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRow;
import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

/**
 * @author tds
 *
 */
public interface ImportRecordNewServiceItf
        extends BoBaseServiceItf<IImportRecordNewHome, IImportRecordNewRow, IImportRecordNewRowSet> {
    Map<String, Object> excelTemplate();

    Map<String, Object> records();

    Map<String, Object> task(Long id);

    Map<String, Object> text(String text, Long sheetType);

    Map<String, Object> uploadSuf(String url, String filename);

    Map<String, Object> recordDetail(Long parentId, Long sheetType, Long pageno, Long pagesize);

    Map<String, Object> getRecordHead(Long id, Long sheetType);

    Map<String, Object> recordRemove(Long id);

    Map<String, Object> recordDetailRemove(Long id);

    Map<String, Object> recordsClean();

    Map<String, Object> recordsStatus(String ids);

    Map<String, Object> getErrorMsg(String name);

	Long importRecordFromGZQ(List<String> headers1, List<String> headers2, List<String> headers3);

	Map<String, Object> task(Long id, Map<String, List<String>> rsMap);

	IImportRecordNewRow getLastestImportReocrd();

	File downExcel(long id);
}
