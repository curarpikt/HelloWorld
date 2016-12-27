package com.chanapp.chanjet.customer.service.importrecordnew;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewHome;
import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRow;
import com.chanapp.chanjet.customer.businessobject.api.importrecordnew.IImportRecordNewRowSet;
import com.chanapp.chanjet.customer.businessobject.api.importrecordrelationnew.IImportRecordRelationNewRow;
import com.chanapp.chanjet.customer.businessobject.api.importrecordrelationnew.IImportRecordRelationNewRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IPT;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.ImportRecordNewMetaData;
import com.chanapp.chanjet.customer.constant.metadata.ImportRecordRelationNewMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.ExceptionResources;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.cmr.api.metadata.base.field.generated.FieldTypeEnum;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;
import com.chanjet.csp.ui.util.OSSUtil;

/**
 * @author tds
 *
 */
public class ImportRecordNewServiceImpl
        extends BoBaseServiceImpl<IImportRecordNewHome, IImportRecordNewRow, IImportRecordNewRowSet>
        implements ImportRecordNewServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(ImportRecordNewServiceImpl.class);

    @Override
    public Map<String, Object> excelTemplate() {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        String sheetTitle1 = "导入客户";
        String sheetTitle2 = "导入联系人";
        String sheetTitle3 = "导入工作记录";

        ImportUtil imUtil = new ImportUtil();

        ArrayList<Map<String, Object>> list1 = imUtil.getHead1();
        ArrayList<Map<String, Object>> list2 = imUtil.getHead2();
        ArrayList<Map<String, Object>> list3 = imUtil.getHead3();

        int listLen1 = list1.size();
        int listLen2 = list2.size();
        int listLen3 = list3.size();
        int i = 0;
        HSSFWorkbook workbook = POIUtils.getWrokbook("xls", null);

        HSSFSheet sheet1 = workbook.createSheet(sheetTitle1);
        HSSFSheet sheet2 = workbook.createSheet(sheetTitle2);
        HSSFSheet sheet3 = workbook.createSheet(sheetTitle3);

        HSSFSheet sheet4 = workbook.createSheet("内容填写说明");

        HSSFSheet hiddenSheet1 = workbook.createSheet("hiddenSheet1");
        HSSFSheet hiddenSheet2 = workbook.createSheet("hiddenSheet2");
        HSSFSheet hiddenSheet3 = workbook.createSheet("hiddenSheet3");
        // 放到最后一个sheet（第5个）
        workbook.setSheetHidden(4, true);
        workbook.setSheetHidden(5, true);
        workbook.setSheetHidden(6, true);

        Font font = workbook.createFont();
        font.setColor(Font.COLOR_RED);
        HSSFRow row = sheet1.createRow((short) 0);
        row.setHeight((short) 300);
        DataFormat format = workbook.createDataFormat();
        // 必须字段
        CellStyle fontStyle = workbook.createCellStyle();
        fontStyle.setFont(font);
        fontStyle.setDataFormat(format.getFormat("@"));
        // 常规字段
        CellStyle headStyle = workbook.createCellStyle();
        headStyle.setDataFormat(format.getFormat("@"));

        int rownum = 0;
        for (rownum = 1; rownum < 2000; rownum++) {
            HSSFRow _row = sheet1.createRow((short) rownum);
            _row.setHeight((short) 300);
            for (i = 0; i < listLen1; i++) {
                HSSFCell cell = _row.createCell(i);
                cell.setCellStyle(headStyle);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            }
        }

        for (i = 0; i < listLen1; i++) {
            HSSFCell cell = row.createCell(i);
            String field = list1.get(i).get("field").toString();
            if (field.equals("Customer_name") || field.equals("name") || field.equals("owner")) {// 如果
                                                                                                 // title
                                                                                                 // 是
                                                                                                 // 客户名称
                                                                                                 // 或
                                                                                                 // 业务员
                                                                                                 // 则变红
                cell.setCellStyle(fontStyle);
            } else {
                cell.setCellStyle(headStyle);
            }
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            String _label = list1.get(i).get("label").toString();
            String[] textlist = (String[]) list1.get(i).get("enum");
            cell.setCellValue(_label);
            int charaecterLength = ImportUtil.getRegExpLength(_label);
            int cellWidth = charaecterLength > 200 ? 200 : (charaecterLength + 1);
            sheet1.setColumnWidth(i, cellWidth * 256);
            if (textlist.length > 0) {
                sheet1 = POIUtils.setHSSFValidation(sheet1, hiddenSheet1, textlist, 1, 1000, i, i);
            }
        }

        HSSFRow row2 = sheet2.createRow((short) 0);
        row2.setHeight((short) 300);

        rownum = 0;
        for (rownum = 1; rownum < 2000; rownum++) {
            HSSFRow _row = sheet2.createRow((short) rownum);
            _row.setHeight((short) 300);
            for (i = 0; i < listLen2; i++) {
                HSSFCell cell = _row.createCell(i);
                cell.setCellStyle(headStyle);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            }
        }

        for (i = 0; i < listLen2; i++) {
            HSSFCell cell = row2.createCell(i);
            String field = list2.get(i).get("field").toString();
            if (field.equals("Customer_name") || field.equals("name") || field.equals("owner")) {// 如果是客户名称
                                                                                                 // 或
                                                                                                 // 联系人名称
                                                                                                 // 或
                                                                                                 // 业务员则变红
                cell.setCellStyle(fontStyle);
            } else {
                cell.setCellStyle(headStyle);
            }
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            String _label = list2.get(i).get("label").toString();
            String[] textlist = (String[]) list2.get(i).get("enum");
            cell.setCellValue(_label);

            // RETODO 20150819 width
            int charaecterLength = ImportUtil.getRegExpLength(_label);
            int cellWidth = charaecterLength > 200 ? 200 : (charaecterLength + 1);
            sheet2.setColumnWidth(i, cellWidth * 256);
            if (textlist.length > 0) {
                sheet2 = POIUtils.setHSSFValidation(sheet2, hiddenSheet2, textlist, 1, 1000, i, i);
            }
        }

        // Sheet sheet3 = workbook.createSheet(sheetTitle3);

        HSSFRow row3 = sheet3.createRow((short) 0);
        row3.setHeight((short) 300);

        rownum = 0;
        for (rownum = 1; rownum < 2000; rownum++) {
            HSSFRow _row = sheet3.createRow((short) rownum);
            _row.setHeight((short) 300);
            for (i = 0; i < listLen3; i++) {
                HSSFCell cell = _row.createCell(i);
                cell.setCellStyle(headStyle);
                cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            }
        }

        for (i = 0; i < listLen3; i++) {
            HSSFCell cell = row3.createCell(i);
            String field = list3.get(i).get("field").toString();
            if (field.equals("owner")) {// 如果是业务员 则变红
                cell.setCellStyle(fontStyle);
            } else {
                cell.setCellStyle(headStyle);
            }
            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            String _label = list3.get(i).get("label").toString();
            String[] textlist = (String[]) list3.get(i).get("enum");
            cell.setCellValue(_label);
            // RETODO 20150819 width
            int charaecterLength = ImportUtil.getRegExpLength(_label);
            int cellWidth = charaecterLength > 200 ? 200 : (charaecterLength + 1);
            sheet3.setColumnWidth(i, cellWidth * 256);
            if (textlist.length > 0) {
                sheet3 = POIUtils.setHSSFValidation(sheet3, hiddenSheet3, textlist, 1, 1000, i, i);
            }
        }

        genSheetInstruction(sheet4, workbook);
        ByteArrayOutputStream os = null;
        InputStream is = null;
        File tmpFile = null;
        String url = "";
        String message = "";
        boolean result = true;
        Map<String, Object> rs = new HashMap<String, Object>();

        try {
            Map<String, Object> fileUploadMap = null;
            os = new ByteArrayOutputStream();
            workbook.write(os);

            is = new ByteArrayInputStream(os.toByteArray());

            tmpFile = File.createTempFile("excelTemplate", ".xls");
            FileUtil.copyInputStreamToFile(is, tmpFile);
            Long now = DateUtil.getNowDateTime().getTime();
            String fileName = now.toString() + ".xls";
            fileUploadMap = OSSUtil.uploadFile(fileName, tmpFile);

            if (fileUploadMap != null) {
                Object backupFileUrl = fileUploadMap.get("url");
                Object fileSize = fileUploadMap.get("size");
                if (backupFileUrl != null && fileSize != null) {
                    url = backupFileUrl.toString();
                } else {
                    message = "云端无返回url或size";
                    result = false;
                }
            } else {
                message = "云端无返回";
                result = false;
            }
            rs.put("url", url);
            rs.put("result", result);
            rs.put("message", message);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("IO E =" + e.getMessage());
            String errorMessage = e.getMessage();
            if (errorMessage == null || "".equals(errorMessage)) {
                errorMessage = "e.getMessage() is null";
            } else {
                if (errorMessage.length() > 500) {
                    errorMessage = errorMessage.substring(0, 500);
                }
            }
            result = false;
            message = errorMessage;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error(e.getMessage());
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    e.printStackTrace();
                }
            }
            if (tmpFile != null && tmpFile.exists()) {
                tmpFile.delete();
            }
        }
        rs.put("url", url);
        rs.put("result", result);
        rs.put("message", message);
        return rs;
    }

    private static void genSheetInstruction(HSSFSheet sheet4, HSSFWorkbook workbook) {
        String title = "内容填写说明:";

        String instruction1_pre = "（1）表头中";
        String instruction1_red = "红色字体";
        String instruction1_end = "标注的，是必填内容。";

        String instruction2_pre = "（2）表头中有一些字段是枚举型数据，需要从";
        String instruction2_red = "下拉选择框中选择，";
        String instruction2_end = "例如客户分类、客户级别、客户来源、行业等。";

        String instruction3 = "（3）表头中日期格式数据，格式请填写成YYYY-MM-DD(如:2014-12-08)。";

        String instruction4 = "（4）客户名称不能多于64个字，联系人姓名不能多于16个字，业务员必须是企业内成员。";

        String instruction5 = "（5）想导入一个新客户及联系人，请先填写《导入客户》表。";

        String instruction6_pre = "（6）";
        String instruction6_red = "同一个客户下导入多个联系人";
        String instruction6_end = "或工作记录，请这样填写：";

        String instruction7 = "      客户名称       联系人姓名    业务员      手机号";
        String instruction8 = "      天天商贸公司      张宇       张丽      18938782378  ";
        String instruction9 = "      天天商贸公司      李林       张丽      18538789871";
        String instruction10 = "      天天商贸公司     王晓昀      张丽      15109897836";
        // String instruction11 =" ";

        String instruction12_up = "提示：导入数据后，可能会处于“正在导入...”状态，此时您可以切换到其他界面继续使用。导入完成后，会在客户";
        String instruction12_down = "管家网页内弹出完成提示。";

        Font fontTitle = workbook.createFont();
        fontTitle.setFontHeightInPoints((short) 24); // 字体高度
        fontTitle.setFontName("宋体"); // 字体
        fontTitle.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度

        Font fontRed = workbook.createFont();
        fontRed.setFontHeightInPoints((short) 12); // 字体高度
        fontRed.setFontName("宋体"); // 字体
        fontRed.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
        fontRed.setColor(Font.COLOR_RED);

        Font fontNormal = workbook.createFont();
        fontNormal.setFontHeightInPoints((short) 12); // 字体高度
        fontNormal.setFontName("宋体"); // 字体
        fontNormal.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
        fontNormal.setColor(Font.COLOR_NORMAL);

        Font fontBlue = workbook.createFont();
        fontBlue.setFontHeightInPoints((short) 12); // 字体高度
        fontBlue.setFontName("宋体"); // 字体
        fontBlue.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度
        fontBlue.setColor(HSSFColor.LIGHT_BLUE.index);

        HSSFRow row0 = sheet4.createRow((short) 0);
        row0.setHeight((short) 300);
        HSSFRow row1 = sheet4.createRow((short) 1);
        row1.setHeight((short) 300);
        HSSFRow row2 = sheet4.createRow((short) 2);
        row2.setHeight((short) 300);

        HSSFRow row3 = sheet4.createRow((short) 3);
        row3.setHeight((short) 600);
        HSSFCell cellTitle = row3.createCell(3);
        HSSFRichTextString rsTitle = new HSSFRichTextString(title);
        rsTitle.applyFont(fontTitle);
        cellTitle.setCellValue(rsTitle);

        HSSFRow row4 = sheet4.createRow((short) 4);
        row4.setHeight((short) 300);
        HSSFCell cellInstruction1 = row4.createCell(3);
        // instruction1_pre HSSFRichTextString ts= new HSSFRichTextString
        HSSFRichTextString rsInstruction1 = new HSSFRichTextString(
                instruction1_pre + instruction1_red + instruction1_end);
        rsInstruction1.applyFont(0, instruction1_pre.length(), fontNormal);
        rsInstruction1.applyFont(instruction1_pre.length(), instruction1_pre.length() + instruction1_red.length(),
                fontRed);
        rsInstruction1.applyFont(instruction1_pre.length() + instruction1_red.length(),
                instruction1_pre.length() + instruction1_red.length() + instruction1_end.length(), fontNormal);
        cellInstruction1.setCellValue(rsInstruction1);

        HSSFRow row5 = sheet4.createRow((short) 5);
        row5.setHeight((short) 300);
        HSSFCell cellInstruction2 = row5.createCell(3);
        // instruction1_pre HSSFRichTextString ts= new HSSFRichTextString
        HSSFRichTextString rsInstruction2 = new HSSFRichTextString(
                instruction2_pre + instruction2_red + instruction2_end);
        rsInstruction2.applyFont(0, instruction2_pre.length(), fontNormal);
        rsInstruction2.applyFont(instruction2_pre.length(), instruction2_pre.length() + instruction2_red.length(),
                fontRed);
        rsInstruction2.applyFont(instruction2_pre.length() + instruction2_red.length(),
                instruction2_pre.length() + instruction2_red.length() + instruction2_end.length(), fontNormal);
        cellInstruction2.setCellValue(rsInstruction2);

        HSSFRow row6 = sheet4.createRow((short) 6);
        row6.setHeight((short) 300);
        HSSFCell cellInstruction3 = row6.createCell(3);
        HSSFRichTextString rsInstruction3 = new HSSFRichTextString(instruction3);
        rsInstruction3.applyFont(fontNormal);
        cellInstruction3.setCellValue(rsInstruction3);

        HSSFRow row7 = sheet4.createRow((short) 7);
        row7.setHeight((short) 300);
        HSSFCell cellInstruction4 = row7.createCell(3);
        HSSFRichTextString rsInstruction4 = new HSSFRichTextString(instruction4);
        rsInstruction4.applyFont(fontNormal);
        cellInstruction4.setCellValue(rsInstruction4);

        HSSFRow row8 = sheet4.createRow((short) 8);
        row8.setHeight((short) 300);
        HSSFCell cellInstruction5 = row8.createCell(3);
        HSSFRichTextString rsInstruction5 = new HSSFRichTextString(instruction5);
        rsInstruction5.applyFont(fontNormal);
        cellInstruction5.setCellValue(rsInstruction5);

        HSSFRow row9 = sheet4.createRow((short) 9);
        row9.setHeight((short) 300);
        HSSFCell cellInstruction6 = row9.createCell(3);
        // instruction1_pre HSSFRichTextString ts= new HSSFRichTextString
        HSSFRichTextString rsInstruction6 = new HSSFRichTextString(
                instruction6_pre + instruction6_red + instruction6_end);
        rsInstruction6.applyFont(0, instruction6_pre.length(), fontNormal);
        rsInstruction6.applyFont(instruction6_pre.length(), instruction6_pre.length() + instruction6_red.length(),
                fontRed);
        rsInstruction6.applyFont(instruction6_pre.length() + instruction6_red.length(),
                instruction6_pre.length() + instruction6_red.length() + instruction6_end.length(), fontNormal);
        cellInstruction6.setCellValue(rsInstruction6);

        HSSFRow row10 = sheet4.createRow((short) 10);
        row10.setHeight((short) 300);
        HSSFCell cellInstruction7 = row10.createCell(3);
        HSSFRichTextString rsInstruction7 = new HSSFRichTextString(instruction7);
        rsInstruction7.applyFont(fontBlue);
        cellInstruction7.setCellValue(rsInstruction7);

        HSSFRow row11 = sheet4.createRow((short) 11);
        row11.setHeight((short) 300);
        HSSFCell cellInstruction8 = row11.createCell(3);
        HSSFRichTextString rsInstruction8 = new HSSFRichTextString(instruction8);
        rsInstruction8.applyFont(fontBlue);
        cellInstruction8.setCellValue(rsInstruction8);

        HSSFRow row12 = sheet4.createRow((short) 12);
        row12.setHeight((short) 300);
        HSSFCell cellInstruction9 = row12.createCell(3);
        HSSFRichTextString rsInstruction9 = new HSSFRichTextString(instruction9);
        rsInstruction9.applyFont(fontBlue);
        cellInstruction9.setCellValue(rsInstruction9);

        HSSFRow row13 = sheet4.createRow((short) 13);
        row13.setHeight((short) 300);
        HSSFCell cellInstruction10 = row13.createCell(3);
        HSSFRichTextString rsInstruction10 = new HSSFRichTextString(instruction10);
        rsInstruction10.applyFont(fontBlue);
        cellInstruction10.setCellValue(rsInstruction10);

        HSSFRow row14 = sheet4.createRow((short) 14);
        row14.setHeight((short) 300);

        // 换行
        HSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        HSSFRow row15 = sheet4.createRow((short) 15);
        row15.setHeight((short) 600);
        HSSFCell cellInstruction12 = row15.createCell(3);
        cellInstruction12.setCellStyle(cellStyle);
        HSSFRichTextString rsInstruction12 = new HSSFRichTextString(instruction12_up + instruction12_down);
        rsInstruction12.applyFont(fontNormal);
        cellInstruction12.setCellValue(rsInstruction12);

        sheet4.setColumnWidth(3, 120 * 255);
    }

    private IImportRecordNewRowSet getAllRecords(long userid) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        //Criteria criteria = Criteria.AND().eq("userId", userid);
        // jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderDesc(SC.id);

        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public Map<String, Object> records() {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        long userid = EnterpriseContext.getCurrentUser().getUserLongId();
        IImportRecordNewRowSet data = getAllRecords(userid);
        Map<String, Object> result = new HashMap<String, Object>();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        int size = data.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            IImportRecordNewRow rec = data.getRow(i);
            Map<String, Object> recMap = new HashMap<String, Object>();
            recMap.put("id", rec.getId());
            recMap.put("status", rec.getStatus());
            recMap.put("fileName", rec.getFileName());
            recMap.put("total", rec.getTotal());
            recMap.put("totalCustomer", rec.getTotalCustomer());
            recMap.put("totalContact", rec.getTotalContact());
            recMap.put("totalWorkRecord", rec.getTotalWorkRecord());
            recMap.put("errorCount", rec.getErrorCount());
            recMap.put("errorCustomerCount", rec.getErrorCustomerCount());
            recMap.put("errorContactCount", rec.getErrorContactCount());
            recMap.put("errorWorkRecordCount", rec.getErrorWorkRecordCount());
            recMap.put("removeErrorCount", rec.getRemoveCount());
            recMap.put("removeCustomerCount", rec.getRemoveCustomerCount());
            recMap.put("removeContactCount", rec.getRemoveContactCount());
            recMap.put("removeWorkRecordCount", rec.getRemoveWorkRecordCount());
            recMap.put("createdDate", rec.getCreatedDate());
            list.add(recMap);
        }
        result.put("total", size);
        result.put("items", list);
        return result;
    }

    private synchronized boolean startTask(long id) {
        IImportRecordNewRow rec = query(id);
        if (rec == null) {
            return false;
        }
        long status = rec.getStatus();

        if (status != 0) {
            return false;
        }
        rec.setRemoveCount(0L);
        rec.setRemoveCustomerCount(0L);
        rec.setRemoveContactCount(0L);
        rec.setRemoveWorkRecordCount(0L);
        rec.setErrorCount(0L);
        rec.setErrorCustomerCount(0L);
        rec.setErrorContactCount(0L);
        rec.setErrorWorkRecordCount(0L);
        rec.setTotal(0L);
        rec.setTotalCustomer(0L);
        rec.setTotalContact(0L);
        rec.setTotalWorkRecord(0L);

        rec.setStatus(1L);
        upsert(rec);
        return true;
    }

    @Override
    public File downExcel(long id) {
        try {
            IImportRecordNewRow rec = query(id);
            if (rec != null) {
                String url = rec.getFileURL();
                URL resourceUrl = new URL(url);
                String suffix = url.substring(url.lastIndexOf(".") + 1);
                File file = File.createTempFile("download", "." + suffix);
                FileUtil.copyURLToFile(resourceUrl, file);
                return file;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private  Map<String, List<String>> getExcelContent(long id) {
        Map<String, List<String>> rsMap;
        File file = null;
        try {
            IImportRecordNewRow rec = query(id);
            if (rec == null) {
                return null;
            }
            long status = rec.getStatus();
            if (status == 2) {
                return null;
            }
            file = downExcel(id);
            rsMap = POIUtils.excel(file);
        } finally {
            if (file != null) {
                file.delete();
            }
        }
        return rsMap;
    }

    private String trim(String str) {
        return str == null ? null : str.trim();
    }

    @SuppressWarnings("unchecked")
    private synchronized boolean startTaskDo(long id, List<String> list, String sheetType, Boolean initHeadFlag) {
        /**
         * 0L-客户sheet 1L-联系人sheet 2L-工作记录sheet
         */
        Long st = 0L;// 是那个sheet的数据
        if (sheetType.equals("Customer_")) {
            st = 0L;
        }
        if (sheetType.equals("Contact_")) {
            st = 1L;
        }
        if (sheetType.equals("WorkRecord_")) {
            st = 2L;
        }

        List<IImportRecordRelationNewRow> recList = new ArrayList<IImportRecordRelationNewRow>();
        int size = list.size();
        long total = size;
        //ArrayList<Map<String, Object>> head = new ArrayList<Map<String, Object>>();
        List<String> customerNameList = new ArrayList<String>();
        int i = 0;
        for (i = 0; i < size; i++) {
            String mapString = list.get(i).toString();
            Map<String, Map<String, Object>> dataMap = dataManager.fromJSONString(mapString, LinkedHashMap.class);
            Map<String, Object> customerMap = new HashMap<String, Object>();
            Map<String, Object> contactMap = new HashMap<String, Object>();
            Map<String, Object> workrecordMap = new HashMap<String, Object>();

            // 对于每一行进行数据校验
            boolean error = false;
            String ownerValue = "";
            Map<String, Object> customerValues = new HashMap<String, Object>();
            Map<String, Object> contactValues = new HashMap<String, Object>();

            for (String key : dataMap.keySet()) {
                Map<String, Object> column = dataMap.get(key);
                String _value = "";
                Object value = column.get("value");
                if (value != null) {
                    _value = value.toString();
                }
                String field = column.get("field").toString();
                String label = column.get("label").toString();
                int length = Integer.valueOf(column.get("length").toString());
                String type = column.get("type").toString();
                String enumName = column.get("enumName").toString();

                // 设置head数据
/*                if (i == 0) {
                    Map<String, Object> firstColumnHead = new HashMap<String, Object>();
                    firstColumnHead.put("label", label);
                    firstColumnHead.put("field", field);
                    firstColumnHead.put("length", length);
                    firstColumnHead.put("type", type);
                    firstColumnHead.put("enumName", enumName);
                    head.add(firstColumnHead);
                }*/
                // value为空跳过
                if (StringUtils.isEmpty(_value)) {
                    continue;
                }
                // 验证长度
                if (StringUtils.isNotEmpty(_value)) {
                    int _length = _value.length();
                    if (length > 0 && _length > length) {
                        column.put("msg", "字符不得超过" + length + "个字符");
                        column.put("errorType", IPT.ERRORTYPE_TOLONG);
                        error = true;
                    }
                }
                if (StringUtils.isNotEmpty(_value) && enumName != null && StringUtils.isNotEmpty(enumName)) {
                    // 验证必要字段：枚举值
                    String enumVal = ImportUtil.getEnumValue(enumName, _value);
                    if (enumVal == null) {
                        column.put("msg", IPT.MSG_ENUM);
                        column.put("errorType", IPT.ERRORTYPE_ENUM);
                        error = true;
                    } else {
                        _value = enumVal;
                    }
                }
                value = ImportUtil.parseValue(type, _value);
                if (value == null) {
                    column.put("msg", IPT.MSG_FORMAT);
                    column.put("errorType", IPT.ERRORTYPE_FORMAT);
                    error = true;
                }
                if (field.equals("owner")) {
                    ownerValue = _value;
                }

                String mapkey = "";
                if (sheetType.equals("Customer_")) {
                    mapkey = field;
                    if (mapkey.startsWith("Field")) {
                        if (type.equals(FieldTypeEnum.CSP_ENUM.toString())) {
                            Map<String, Object> enumValue = new HashMap<String, Object>();
                            enumValue.put("value", value);
                            customerValues.put(mapkey, enumValue);
                        } else {
                            customerValues.put(mapkey, value);
                        }
                    } else {
                        customerMap.put(mapkey, value);
                    }
                } else if (sheetType.equals("Contact_")) {
                    if (field.startsWith("Customer_")) {// 联系人的 客户名称要特殊处理
                        mapkey = "customerName";
                    } else {
                        mapkey = field;
                    }
                    if (mapkey.startsWith("Field")) {
                        if (type.equals(FieldTypeEnum.CSP_ENUM.toString())) {
                            Map<String, Object> enumValue = new HashMap<String, Object>();
                            enumValue.put("value", value);
                            contactValues.put(mapkey, enumValue);
                        } else {
                            contactValues.put(mapkey, value);
                        }
                    } else {
                        contactMap.put(mapkey, value);
                    }
                } else if (sheetType.equals("WorkRecord_")) {
                    if (field.startsWith("Customer_")) {// 工作记录的 客户名称要特殊处理
                        mapkey = "customerName";
                    } else {
                        mapkey = field;
                    }
                    if (mapkey.equals("content") || mapkey.equals("customerName")) {
                        workrecordMap.put(mapkey, value);
                    }
                    if (mapkey.equals("owner")) {
                        workrecordMap.put(mapkey, ownerValue);
                    }
                }
            } // for key end
            long ownerId = 0;
            if (StringUtils.isEmpty(ownerValue)) {
                dataMap.get("owner").put("msg", IPT.MSG_NULL);
                dataMap.get("owner").put("errorType", IPT.ERRORTYPE_NULL);
                error = true;
            } else {
                List<Long> ownerIdList = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                        .getUserIdByName(ownerValue);
                if (ownerIdList == null || ownerIdList.size() == 0) {
                    dataMap.get("owner").put("msg", IPT.MSG_NON);
                    dataMap.get("owner").put("errorType", IPT.ERRORTYPE_NON);
                    error = true;
                } else if (ownerIdList.size() != 1) {
                    dataMap.get("owner").put("msg", IPT.MSG_MULTI);
                    dataMap.get("owner").put("errorType", IPT.ERRORTYPE_MULTI);
                    error = true;
                } else {
                    ownerId = Long.valueOf(ownerIdList.get(0).toString());
                }
            }

            Long customerId = null;
            String customerName = "";
            if (sheetType.equals("Contact_")) {
                customerName = trim((String) contactMap.get("customerName"));
            } else if (sheetType.equals("WorkRecord_")) {
                customerName = trim((String) workrecordMap.get("customerName"));
            }
            if (customerName != null && !"".equals(customerName)) {
                ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                        .getCustomerByName(customerName);
                if (customer == null) {
                    dataMap.get("Customer_name").put("msg", IPT.MSG_NON);
                    dataMap.get("Customer_name").put("errorType", IPT.ERRORTYPE_NON);
                    error = true;
                } else {
                    customerId = customer.getId();
                }
            }

            if (sheetType.equals("Customer_")) {
                if (!customerMap.containsKey("name") || customerMap.get("name") == null
                        || StringUtils.isEmpty(customerMap.get("name").toString())) {
                    dataMap.get("name").put("msg", IPT.MSG_NULL);
                    dataMap.get("name").put("errorType", IPT.ERRORTYPE_NULL);
                    error = true;
                }
                customerName = trim((String) customerMap.get("name"));
                if (customerNameList.contains(customerName)) {
                    dataMap.get("name").put("msg", IPT.MSG_CEXISTS);
                    dataMap.get("name").put("errorType", IPT.ERRORTYPE_CEXISTS);
                    error = true;
                } else {
                    customerNameList.add(customerName);
                }

                if (!customerValues.isEmpty()) {
                    customerMap.put("customValues", customerValues);
                }
                customerMap.put("owner", ownerId);
            } else if (sheetType.equals("Contact_")) {
                if (!contactMap.containsKey("name") || contactMap.get("name") == null
                        || StringUtils.isEmpty(contactMap.get("name").toString())) {
                    dataMap.get("name").put("msg", IPT.MSG_NULL);
                    dataMap.get("name").put("errorType", IPT.ERRORTYPE_NULL);
                    error = true;
                }
                if (!contactMap.containsKey("customerName") || contactMap.get("customerName") == null
                        || StringUtils.isEmpty(contactMap.get("customerName").toString())) {
                    dataMap.get("Customer_name").put("msg", IPT.MSG_NULL);
                    dataMap.get("Customer_name").put("errorType", IPT.ERRORTYPE_NULL);
                    error = true;
                }
                if (customerId != null) {
                    contactMap.put("customer", customerId);
                }

                if (!contactValues.isEmpty()) {
                    contactMap.put("customValues", contactValues);
                }
                contactMap.put("owner", ownerId);
            } else if (sheetType.equals("WorkRecord_")) {
                if (customerId != null) {
                    workrecordMap.put("customer", customerId);
                }

                workrecordMap.put("owner", ownerId);
            }

            if (error) {
                IImportRecordRelationNewRow instanceRel = (IImportRecordRelationNewRow) ServiceLocator.getInstance()
                        .lookup(BO.ImportRecordRelationNew).createRow();
                instanceRel.setCont(dataManager.toJSONString(dataMap));
                instanceRel.setUserId(EnterpriseContext.getCurrentUser().getUserLongId());
                instanceRel.setParentId(id);
                instanceRel.setSheetType(st);
                recList.add(instanceRel);
                continue;
            }

            try {
                if (sheetType.equals("Customer_")) {
                    LinkedHashMap<String, Object> customerVal = dataManager
                            .fromJSONString(dataManager.toJSONString(customerMap), LinkedHashMap.class);

                    Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                            .getExistsCustomer(null, (String) customerVal.get("name"), null);
                    if (sameCustomer != null) {
                        Assert.customerRepeat(sameCustomer);
                    }
                    // 添加客户
                    ServiceLocator.getInstance().lookup(CustomerServiceItf.class).addCustomer(customerVal);
                } else if (sheetType.equals("Contact_")) {
                    setContactPhone(contactMap);
                    LinkedHashMap<String, Object> contactVal = dataManager
                            .fromJSONString(dataManager.toJSONString(contactMap), LinkedHashMap.class);
                    // 添加联系人
                    contactVal.put(ContactMetaData.customer, customerId);
                    contactVal.put(SC.owner, ownerId);
                    ServiceLocator.getInstance().lookup(ContactServiceItf.class).addContact(contactVal);
                } else if (sheetType.equals("WorkRecord_")) {
                    LinkedHashMap<String, Object> workRecordVal = dataManager
                            .fromJSONString(dataManager.toJSONString(workrecordMap), LinkedHashMap.class);
                    if (StringUtils.isNotEmpty((String) workRecordVal.get(WorkRecordMetaData.content))) {
                        workRecordVal.put(WorkRecordMetaData.customer, customerId);

                        if (customerId != null && customerId > 0) {
                            workRecordVal.put(WorkRecordMetaData.customer, customerId);
                        }
                        workRecordVal.put(SC.owner, ownerId);
                        ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).addWorkRecord(workRecordVal,
                                null);
                    }
                }
            } catch (Exception e) {
                if (e.toString().indexOf("app.customer.name.duplicated") != -1) {
                    dataMap.get("name").put("msg", IPT.MSG_CEXISTS);
                    dataMap.get("name").put("errorType", IPT.ERRORTYPE_CEXISTS);
                }

                IImportRecordRelationNewRow instanceRel = (IImportRecordRelationNewRow) ServiceLocator.getInstance()
                        .lookup(BO.ImportRecordRelationNew).createRow();
                instanceRel.setCont(dataManager.toJSONString(dataMap));
                instanceRel.setUserId(EnterpriseContext.getCurrentUser().getUserLongId());
                instanceRel.setParentId(id);
                instanceRel.setSheetType(st);
                recList.add(instanceRel);

            }

        } // for i end

        try {
            IImportRecordNewRow instance = query(id);
            long errorCount = Long.valueOf(recList.size());
            if (instance != null) {
                Long sheetErrorCount = errorCount;
                Long sheetTotal = total;// list.size()
                instance.setTotal(sheetTotal + instance.getTotal());
                instance.setErrorCount(sheetErrorCount + instance.getErrorCount());
                if (st.compareTo(0L) == 0) {
                    instance.setTotalCustomer(sheetTotal + instance.getTotalCustomer());
                    instance.setErrorCustomerCount(sheetErrorCount + instance.getErrorCustomerCount());
                } else if (st.compareTo(1L) == 0) {
                    instance.setTotalContact(sheetTotal + instance.getTotalContact());
                    instance.setErrorContactCount(sheetErrorCount + instance.getErrorContactCount());
                } else if (st.compareTo(2L) == 0) {
                    instance.setTotalWorkRecord(sheetTotal + instance.getTotalWorkRecord());
                    instance.setErrorWorkRecordCount(sheetErrorCount + instance.getErrorWorkRecordCount());
                }

                instance.setRemoveCount(0L);
                instance.setStatus(1L);

                upsert(instance);
            }
            if (recList.size() > 0) {
                for (IImportRecordRelationNewRow row : recList) {
                    ServiceLocator.getInstance().lookup(BO.ImportRecordRelationNew).upsert(row);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void setContactPhone(Map<String, Object> contactMap) {
        String phone = (String) contactMap.get("phone");
        if (phone == null || "".equals(phone)) {
            phone = (String) contactMap.get("effectivePhone");
            if (phone != null && !"".equals(phone)) {
                contactMap.put("phone", phone);
            }
        }
        String mobile = (String) contactMap.get("mobile");
        if (mobile == null || "".equals(mobile)) {
            mobile = (String) contactMap.get("effectiveMobile");
            if (mobile != null && !"".equals(mobile)) {
                contactMap.put("mobile", phone);
            }
        }
    }
    
    @Override
    public Map<String, Object> task(Long id,Map<String, List<String>> rsMap){
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        long startTime = System.currentTimeMillis();
        boolean status = false;
        try {
            boolean bool = startTask(id);
            if (bool) {
            	if(rsMap==null){
            		rsMap = getExcelContent(id);        
            		JSON.toJSONString(rsMap);
            	}
                List<String> customerList = rsMap.get("Customer_");
                List<String> contactList = rsMap.get("Contact_");  
                List<String> workRecordList = rsMap.get("WorkRecord_");    
                if (customerList != null) {
                    List<String> stepList = new ArrayList<String>();
                    int size = customerList.size();
                    int i = 0;
                    Boolean initHeadFlag = false;
                    for (i = 0; i < size; i++) {
                        stepList.add(customerList.get(i));
                        if (i < 100) {
                            initHeadFlag = true;// 只第一次掉startTaskDo 拼装head
                        } else {
                            initHeadFlag = false;
                        }
                        if (i % 100 == 99) {
                            startTaskDo(id, stepList, "Customer_", initHeadFlag);
                            stepList = new ArrayList<String>();
                        }
                    }
                    if (stepList.size() > 0) {
                        startTaskDo(id, stepList, "Customer_", initHeadFlag);
                    }
                    status = true;
                }
                if (contactList != null) {
                    List<String> stepList = new ArrayList<String>();
                    int size = contactList.size();
                    int i = 0;
                    Boolean initHeadFlag = false;
                    for (i = 0; i < size; i++) {
                        stepList.add(contactList.get(i));
                        if (i < 100) {
                            initHeadFlag = true;// 只第一次掉startTaskDo 拼装head
                        } else {
                            initHeadFlag = false;
                        }
                        if (i % 100 == 99) {
                            startTaskDo(id, stepList, "Contact_", initHeadFlag);
                            stepList = new ArrayList<String>();
                        }
                    }
                    if (stepList.size() > 0) {
                        startTaskDo(id, stepList, "Contact_", initHeadFlag);
                    }
                    status = true;
                }
                if (workRecordList != null) {
                    List<String> stepList = new ArrayList<String>();
                    int size = workRecordList.size();
                    int i = 0;
                    Boolean initHeadFlag = false;
                    for (i = 0; i < size; i++) {
                        stepList.add(workRecordList.get(i));
                        if (i < 100) {
                            initHeadFlag = true;// 只第一次掉startTaskDo 拼装head
                        } else {
                            initHeadFlag = false;
                        }
                        if (i % 100 == 99) {
                            startTaskDo(id, stepList, "WorkRecord_", initHeadFlag);
                            stepList = new ArrayList<String>();
                        }
                    }
                    if (stepList.size() > 0) {
                        startTaskDo(id, stepList, "WorkRecord_", initHeadFlag);
                    }
                    status = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            setStatus(id, 2);
            logger.info("[excel] task id={},times={}", id, System.currentTimeMillis() - startTime);
        }
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", status);
        return result;    
    }

    @Override
    public Map<String, Object> task(Long id) {
    	  return task(id,null);
    }

    private boolean setStatus(long id, long status) {
        IImportRecordNewRow instance = query(id);
        if (instance == null) {
            return false;
        }
        instance.setStatus(status);
        upsert(instance);
        return false;
    }

    private IImportRecordRelationNewRow findByIdWithOutAuth(Long id) {
        IImportRecordRelationNewRow row = (IImportRecordRelationNewRow) ServiceLocator.getInstance()
                .lookup(BO.ImportRecordRelationNew).query(id);
        if (row != null) {
/*            Boolean isDelete = (Boolean) row.getFieldValue(SC.isDeleted);
            if (Boolean.TRUE.equals(isDelete)) {
                return null;
            }*/
            return row;
        }
        return null;
    }

    private void resetErrorCount(long id, boolean isZero, long sheetType) {
        IImportRecordNewRow rec = query(id);
        if (rec != null) {
            long errorCount = 0;
            if (isZero) {
                errorCount = 0;
            } else {
                errorCount = rec.getErrorCount() - 1;
                if (errorCount < 0) {
                    errorCount = 0;
                }
            }
            long errorCustomerCount = rec.getErrorCustomerCount();
            if (rec.getErrorCustomerCount() != null && sheetType == 0) {
                errorCustomerCount = errorCustomerCount - 1;
                if (errorCustomerCount < 0) {
                    errorCustomerCount = 0;
                }
            }
            long errorContactCount = rec.getErrorContactCount();
            if (rec.getErrorCustomerCount() != null && sheetType == 1) {
                errorContactCount = rec.getErrorContactCount() - 1;
                if (errorContactCount < 0) {
                    errorContactCount = 0;
                }
            }
            long errorWorkRecordCount = rec.getErrorWorkRecordCount();
            if (rec.getErrorWorkRecordCount() != null && sheetType == 2) {
                errorWorkRecordCount = rec.getErrorWorkRecordCount() - 1;
                if (errorWorkRecordCount < 0) {
                    errorWorkRecordCount = 0;
                }
            }
            rec.setErrorCount(errorCount);
            rec.setErrorCustomerCount(errorCustomerCount);
            rec.setErrorContactCount(errorContactCount);
            rec.setErrorWorkRecordCount(errorWorkRecordCount);
            upsert(rec);
        }
    }

    private boolean recordDetailRemove(long id, long userId) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.eq(ImportRecordRelationNewMetaData.userId, userId);
        criteria.eq(SC.id, id);
        String queryStr = jsonQueryBuilder.addCriteria(criteria).toJsonQuerySpec();
        int count = ServiceLocator.getInstance().lookup(BO.ImportRecordRelationNew).batchDelete(queryStr);
        if (count > 0) {
            return true;
        }
        return false;
    }

    private boolean recordDetailRemoveAndResetErrorCount(long id, long sheetType) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();

        IImportRecordRelationNewRow row = findByIdWithOutAuth(id);
        if (row == null) {
            return false;
        }
        long parentId = row.getParentId();
        boolean bool = recordDetailRemove(id, userId);
        if (bool) {
            resetErrorCount(parentId, false, sheetType);
        }
        return bool;

    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> importRow(Map<String, Object> data, long sheetType) {
        long id = 0;
        if (data.get("id") != null) {
            id = Long.valueOf(data.get("id").toString());
        }
        if (id == 0) {
            throw new AppException("app.common.params.invalid");
        }
        data.remove("id");
        boolean error = false;
        ImportUtil imUtil = new ImportUtil();
        List<Map<String, Object>> list = null;
        list = imUtil.getMetadata(data, sheetType);
        Map<String, Object> customerMap = new HashMap<String, Object>();
        Map<String, Object> contactMap = new HashMap<String, Object>();
        Map<String, Object> workrecordMap = new HashMap<String, Object>();
        Map<String, Object> customerValues = new HashMap<String, Object>();
        Map<String, Object> contactValues = new HashMap<String, Object>();
        String ownerValue = "";
        int size = list.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            Map<String, Object> row = list.get(i);
            String _value = "";
            Object value = row.get("value");
            if (value != null) {
                _value = value.toString();
            }
            String field = row.get("field").toString();
            // String label = row.get("label").toString();
            int length = Integer.valueOf(row.get("length").toString());
            String type = row.get("type").toString();
            String enumName = row.get("enumName").toString();

            // value为空跳过
            if (StringUtils.isEmpty(_value)) {
                continue;
            }
            // 验证长度
            if (StringUtils.isNotEmpty(_value)) {
                int _length = _value.length();
                if (length > 0 && _length > length) {
                    Map<String, Object> f = (Map<String, Object>) data.get(field);
                    f.put("errorType", IPT.ERRORTYPE_TOLONG);
                    f.put("msg", "字符不得超过" + length + "个字符");
                    data.put(field, f);
                    error = true;
                }
            }
            if (StringUtils.isNotEmpty(_value) && enumName != null && StringUtils.isNotEmpty(enumName)) {
                // 验证必要字段：枚举值
                String enumVal = ImportUtil.getEnumValue(enumName, _value);
                if (enumVal == null) {
                    Map<String, Object> f = (Map<String, Object>) data.get(field);
                    f.put("errorType", IPT.ERRORTYPE_ENUM);
                    f.put("msg", IPT.MSG_ENUM);
                    data.put(field, f);
                    error = true;
                } else {
                    _value = enumVal;
                }
            }
            value = ImportUtil.parseValue(type, _value);
            if (value == null) {
                Map<String, Object> f = (Map<String, Object>) data.get(field);
                f.put("errorType", IPT.ERRORTYPE_FORMAT);
                f.put("msg", IPT.MSG_FORMAT);
                data.put(field, f);
                error = true;
            }
            if (field.equals("owner")) {
                ownerValue = _value;
            }

            String mapkey = "";
            if (sheetType == 0) {
                mapkey = field;
                if (mapkey.startsWith("Field")) {
                    if (type.equals(FieldTypeEnum.CSP_ENUM.toString())) {
                        Map<String, Object> enumValue = new HashMap<String, Object>();
                        enumValue.put("value", value);
                        customerValues.put(mapkey, enumValue);
                    } else {
                        customerValues.put(mapkey, value);
                    }
                } else {
                    customerMap.put(mapkey, value);
                }
            } else if (sheetType == 1) {
                if (field.startsWith("Customer_")) {// 联系人的 客户名称要特殊处理
                    mapkey = "customerName";
                } else {
                    mapkey = field;
                }
                if (mapkey.startsWith("Field")) {
                    if (type.equals(FieldTypeEnum.CSP_ENUM.toString())) {
                        Map<String, Object> enumValue = new HashMap<String, Object>();
                        enumValue.put("value", value);
                        contactValues.put(mapkey, enumValue);
                    } else {
                        contactValues.put(mapkey, value);
                    }
                } else {
                    contactMap.put(mapkey, value);
                }
            } else if (sheetType == 2) {
                if (field.startsWith("Customer_")) {// 联系人的 客户名称要特殊处理
                    mapkey = "customerName";
                } else {
                    mapkey = field;
                }
                if (mapkey.equals("content") || mapkey.equals("customerName")) {
                    workrecordMap.put(mapkey, value);
                }
                if (mapkey.equals("owner")) {
                    workrecordMap.put(mapkey, ownerValue);
                }
            }
        } // end for

        long ownerId = 0;
        if (StringUtils.isEmpty(ownerValue)) {
            Map<String, Object> f = (Map<String, Object>) data.get("owner");
            f.put("msg", IPT.MSG_NULL);
            f.put("errorType", IPT.ERRORTYPE_NULL);
            data.put("owner", f);
            error = true;
        } else {
            List<Long> ownerIdList = ServiceLocator.getInstance().lookup(UserServiceItf.class)
                    .getUserIdByName(ownerValue);
            if (ownerIdList == null || ownerIdList.size() == 0) {
                Map<String, Object> f = (Map<String, Object>) data.get("owner");
                f.put("msg", IPT.MSG_NON);
                f.put("errorType", IPT.ERRORTYPE_NON);
                data.put("owner", f);
                error = true;
            } else if (ownerIdList.size() != 1) {
                Map<String, Object> f = (Map<String, Object>) data.get(sheetType == 1 ? "Contact_owner" : "owner");
                f.put("msg", IPT.MSG_MULTI);
                f.put("errorType", IPT.ERRORTYPE_MULTI);
                data.put("owner", f);
                error = true;
            } else {
                ownerId = Long.valueOf(ownerIdList.get(0).toString());
            }
        }

        Long customerId = null;
        String customerName = "";
        if (sheetType == 1) {
            customerName = trim((String) contactMap.get("customerName"));
        } else if (sheetType == 2) {
            customerName = trim((String) workrecordMap.get("customerName"));
        }
        if (customerName != null && !"".equals(customerName)) {
            ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .getCustomerByName(customerName);
            if (customer == null) {
                Map<String, Object> f = (Map<String, Object>) data.get("Customer_name");
                f.put("msg", IPT.MSG_NON);
                f.put("errorType", IPT.ERRORTYPE_NON);
                data.put("Customer_name", f);
                error = true;
            } else {
                customerId = (Long) customer.getFieldValue(SC.id);
            }
        }

        if (sheetType == 0) {
            if (!customerMap.containsKey("name") || customerMap.get("name") == null
                    || StringUtils.isEmpty(customerMap.get("name").toString())) {
                Map<String, Object> f = (Map<String, Object>) data.get("name");
                f.put("msg", IPT.MSG_NULL);
                f.put("errorType", IPT.ERRORTYPE_NULL);
                data.put("name", f);
                error = true;
            }

            Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .getExistsCustomer(customerId, (String) customerMap.get("name"), null);
            if (sameCustomer != null) {
                String repeat = (String) sameCustomer.get("repeat");
                if ("name".equals(repeat)) {
                    Map<String, Object> f = (Map<String, Object>) data.get("name");
                    f.put("msg", IPT.MSG_CEXISTS);
                    f.put("errorType", IPT.ERRORTYPE_CEXISTS);
                    data.put("id", id);
                    data.put("name", f);
                }
                error = true;
            }

            if (!customerValues.isEmpty()) {
                customerMap.put("customValues", customerValues);
            }
            customerMap.put("owner", ownerId);
        } else if (sheetType == 1) {
            if (!contactMap.containsKey("name") || contactMap.get("name") == null
                    || StringUtils.isEmpty(contactMap.get("name").toString())) {
                Map<String, Object> f = (Map<String, Object>) data.get("name");
                f.put("msg", IPT.MSG_NULL);
                f.put("errorType", IPT.ERRORTYPE_NULL);
                data.put("name", f);
                error = true;
            }
            if (!contactMap.containsKey("customerName") || contactMap.get("customerName") == null
                    || StringUtils.isEmpty(contactMap.get("customerName").toString())) {
                Map<String, Object> f = (Map<String, Object>) data.get("Customer_name");
                f.put("msg", IPT.MSG_NULL);
                f.put("errorType", IPT.ERRORTYPE_NULL);
                data.put("Customer_name", f);
                error = true;
            }

            if (customerId != null) {
                contactMap.put("customer", customerId);
            }

            if (!contactValues.isEmpty()) {
                contactMap.put("customValues", contactValues);
            }
            contactMap.put("owner", ownerId);

        } else if (sheetType == 2) {
            if (customerId != null) {
                workrecordMap.put("customer", customerId);
            }

            workrecordMap.put("owner", ownerId);
        }

        if (error) {
            data.put("id", id);
            return data;
        }

        Boolean succFlag = false;
        succFlag = recordDetailRemoveAndResetErrorCount(id, sheetType);
        if (!succFlag) {
            // id 数据不存在
            throw new AppException("app.common.params.invalid");
        }
        if (sheetType == 0) {
            LinkedHashMap<String, Object> customerVal = dataManager
                    .fromJSONString(dataManager.toJSONString(customerMap), LinkedHashMap.class);
            ServiceLocator.getInstance().lookup(CustomerServiceItf.class).addCustomer(customerVal);
        } else if (sheetType == 1) {
            setContactPhone(contactMap);

            LinkedHashMap<String, Object> contactVal = dataManager.fromJSONString(dataManager.toJSONString(contactMap),
                    LinkedHashMap.class);
            // 添加联系人
            contactVal.put(ContactMetaData.customer, customerId);
            contactVal.put(SC.owner, ownerId);
            ServiceLocator.getInstance().lookup(ContactServiceItf.class).addContact(contactVal);
        } else if (sheetType == 2) {
            LinkedHashMap<String, Object> workRecordVal = dataManager
                    .fromJSONString(dataManager.toJSONString(workrecordMap), LinkedHashMap.class);
            if (StringUtils.isNotEmpty((String) workRecordVal.get(WorkRecordMetaData.content))) {
                if (customerId != null && customerId > 0) {
                    workRecordVal.put(WorkRecordMetaData.customer, customerId);
                }
                workRecordVal.put(SC.owner, ownerId);
                ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).addWorkRecord(workRecordVal, null);
            }
        }

        return null;
    }

    @Override
    public Map<String, Object> text(String text, Long sheetType) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> data = null;
        data = dataManager.jsonStringToMap(text);
        if (data == null) {
            throw new AppException("app.common.params.invalid");
        }
        if (sheetType == 0 && !data.containsKey("name") && !data.containsKey("owner")) {
            String needFileds = ImportUtil.checkHeadFieldsCustomer(null);
            Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
            error.put("message", String.format(error.get("message").toString(), needFileds.substring(1)));
            return error;
        }
        if (sheetType == 1 && !data.containsKey("name") && !data.containsKey("owner")
                && !data.containsKey("Customer_name")) {
            String needFileds = ImportUtil.checkHeadFieldsContact(null);
            Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
            error.put("message", String.format(error.get("message").toString(), needFileds.substring(1)));
            return error;
        }
        if (sheetType == 2 && !data.containsKey("owner")) {
            String needFileds = ImportUtil.checkHeadFieldsWorkRecord(null);
            Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
            error.put("message", String.format(error.get("message").toString(), needFileds.substring(1)));
            return error;
        }
        Map<String, Object> errorRow = importRow(data, sheetType);
        Map<String, Object> result = new HashMap<String, Object>();
        if (errorRow != null) {
            result.put("result", false);
            result.put("data", errorRow);
        } else {
            result.put("result", true);
        }
        return result;
    }

    @Override
    public Map<String, Object> uploadSuf(String url, String filename) {
        File tmpFile = null;
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
            URL resourceUrl = new URL(url);
            String suffix = url.substring(url.lastIndexOf(".") + 1);
            tmpFile = File.createTempFile("download", "." + suffix);
            FileUtil.copyURLToFile(resourceUrl, tmpFile);

            result.put("result", false);
            String fileName = filename;
            if (fileName == null || "".equals(fileName)) {
                fileName = url.substring(url.lastIndexOf("/") + 1);
            }
            ArrayList<Map<String, Object>> headCustomer = new ArrayList<Map<String, Object>>();
            ArrayList<Map<String, Object>> headContact = new ArrayList<Map<String, Object>>();
            ArrayList<Map<String, Object>> headWorkRecord = new ArrayList<Map<String, Object>>();

            // sheet1 Customer
            List<String> headers1 = POIUtils.readExcelTitle(suffix, new FileInputStream(tmpFile), 0);
            if (headers1 == null || headers1.size() < 1) {
                result.put("error", getErrorMsg("app.import.excel.sheeterror"));
                return result;
            }
            String needFileds1 = ImportUtil.checkHeadFieldsCustomer(headers1);
            if (!needFileds1.equals("")) {
                Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
                error.put("message", String.format(error.get("message").toString(), needFileds1.substring(1)));
                result.put("error", error);
                return result;
            }

            // sheet2 Contact
            List<String> headers2 = POIUtils.readExcelTitle(suffix, new FileInputStream(tmpFile), 1);
            if (headers2 == null || headers2.size() < 1) {
                result.put("error", getErrorMsg("app.import.excel.sheeterror"));
                return result;
            }
            String needFileds2 = ImportUtil.checkHeadFieldsContact(headers2);
            if (!needFileds2.equals("")) {
                Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
                error.put("message", String.format(error.get("message").toString(), needFileds2.substring(1)));
                result.put("error", error);
                return result;
            }

            // sheet3 WorkRecord
            List<String> headers3 = POIUtils.readExcelTitle(suffix, new FileInputStream(tmpFile), 2);
            if (headers3 == null || headers3.size() < 1) {
                result.put("error", getErrorMsg("app.import.excel.sheeterror"));
                return result;
            }
            String needFileds3 = ImportUtil.checkHeadFieldsWorkRecord(headers3);
            if (!needFileds3.equals("")) {
                Map<String, Object> error = getErrorMsg("app.import.excel.check.head");
                error.put("message", String.format(error.get("message").toString(), needFileds3.substring(1)));
                result.put("error", error);
                return result;
            }
            // 计算 导入记录中的head 并初始化
            Map<String, Map<String, Object>> fieldsHead1 = ImportUtil.getFieldsNew(headers1, 0);
            Map<String, Map<String, Object>> fieldsHead2 = ImportUtil.getFieldsNew(headers2, 1);
            Map<String, Map<String, Object>> fieldsHead3 = ImportUtil.getFieldsNew(headers3, 2);
            for (Map.Entry<String, Map<String, Object>> entry : fieldsHead1.entrySet()) {
                headCustomer.add(entry.getValue());
            }
            for (Map.Entry<String, Map<String, Object>> entry : fieldsHead2.entrySet()) {
                headContact.add(entry.getValue());
            }
            for (Map.Entry<String, Map<String, Object>> entry : fieldsHead3.entrySet()) {
                headWorkRecord.add(entry.getValue());
            }

            IImportRecordNewRow rec = createRow();
            rec.setUserId(EnterpriseContext.getCurrentUser().getUserLongId());
            rec.setStatus(0L);
            rec.setTotal(0L);
            rec.setTotalContact(0L);
            rec.setTotalCustomer(0L);
            rec.setTotalWorkRecord(0L);
            rec.setRemoveCount(0L);
            rec.setRemoveContactCount(0L);
            rec.setRemoveCustomerCount(0L);
            rec.setRemoveWorkRecordCount(0L);
            rec.setErrorCount(0L);
            rec.setErrorContactCount(0L);
            rec.setErrorCustomerCount(0L);
            rec.setErrorWorkRecordCount(0L);
            rec.setHead(dataManager.toJSONString(headCustomer) + "&" + dataManager.toJSONString(headContact) + "&"
                    + dataManager.toJSONString(headWorkRecord));
            rec.setFileURL(url);
            rec.setFileSize(tmpFile.length());
            rec.setFileName(fileName);
            upsert(rec);
            result.put("id", rec.getId());
            result.put("result", true);
        } catch (Exception e) {
            result.put("error", getErrorMsg("app.common.server.error"));
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
        return result;
    }

    private int recordDetailsCount(long userId, Long parentId, Long sheetType) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.eq("userId", userId);
        criteria.eq("sheetType", sheetType);
        criteria.eq("parentId", parentId);
        String queryStr = jsonQueryBuilder.addCriteria(criteria).toJsonQuerySpec();
        return ServiceLocator.getInstance().lookup(BO.ImportRecordRelationNew).getRowCount(queryStr);

    }

    public static IImportRecordRelationNewRowSet recordDetails(long userId, Long parentId, Long sheetType, Long pageno,
            Long pagesize) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        //criteria.eq("userId", userId);
        criteria.eq("sheetType", sheetType);
        criteria.eq("parentId", parentId);
        jsonQueryBuilder.addCriteria(criteria).setFirstResult((int) ((pageno - 1) * pagesize))
                .setMaxResult(pagesize.intValue()).addOrderAsc(SC.id);
        String queryStr = jsonQueryBuilder.toJsonQuerySpec();
        return (IImportRecordRelationNewRowSet) ServiceLocator.getInstance().lookup(BO.ImportRecordRelationNew)
                .query(queryStr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> recordDetail(Long parentId, Long sheetType, Long pageno, Long pagesize) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();
        Map<String, Object> data = new HashMap<String, Object>();
        List<Object> list = new ArrayList<Object>();
        boolean hasMore = false;
        data.put("items", list);
        data.put("hasMore", hasMore);
        if (pageno <= 0 || pagesize <= 0) {
            return data;
        }
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        int total = recordDetailsCount(userId, parentId, sheetType);
        if (pageno * pagesize < total) {
            hasMore = true;
        }
        IImportRecordRelationNewRowSet rec = recordDetails(userId, parentId, sheetType, pageno, pagesize);
        int size = rec.size();
        int i = 0;
        for (i = 0; i < size; i++) {
            try {
                IImportRecordRelationNewRow recRel = rec.getRow(i);
                LinkedHashMap<String, Object> row = dataManager.fromJSONString(recRel.getCont(), LinkedHashMap.class);
                row.put("id", recRel.getId());
                list.add(row);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        data.put("items", list);
        data.put("hasMore", hasMore);
        return data;
    }

    private IImportRecordNewRow getRecordHead(Long id) {
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        IImportRecordNewRow row = query(id);
        if (row != null) {
            Long _id = row.getUserId();
            if (_id == null) {
                if (userId == null) {
                    return row;
                }
            } else {
              //  if (_id.equals(userId)) {
                    return row;
            //    }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getRecordHead(Long id, Long sheetType) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        try {
            IImportRecordNewRow rec = getRecordHead(id);
            Map<String, Object> info = new HashMap<String, Object>();
            Timestamp createdTime = null;
            if (rec != null) {
                String head = rec.getHead();
                String[] headArr = head.split("&");

                Boolean initInfo = false;

                if (sheetType == 0 && headArr != null) {
                    head = headArr[0];
                    initInfo = true;
                }
                if (sheetType == 1 && headArr != null && headArr.length > 0) {
                    head = headArr[1];
                    initInfo = true;
                } else if (sheetType == 1) {
                    head = "[]";
                }
                if (sheetType == 2 && headArr != null && headArr.length > 1) {
                    head = headArr[2];
                    initInfo = true;
                } else if (sheetType == 2) {
                    head = "[]";
                }
                info.put("id", rec.getId());
                info.put("status", rec.getStatus());
                info.put("fileName", rec.getFileName());

                info.put("total", rec.getTotal());
                info.put("totalCustomer", rec.getTotalCustomer());
                info.put("totalContact", rec.getTotalContact());
                info.put("totalWorkRecord", rec.getTotalWorkRecord());

                info.put("errorCount", rec.getErrorCount());
                info.put("errorCustomerCount", rec.getErrorCustomerCount());
                info.put("errorContactCount", rec.getErrorContactCount());
                info.put("errorWorkRecordCount", rec.getErrorWorkRecordCount());

                info.put("removeErrorCount", rec.getRemoveCount());
                info.put("removeCustomerCount", rec.getRemoveCustomerCount());
                info.put("removeContactCount", rec.getRemoveContactCount());
                info.put("removeWorkRecordCount", rec.getRemoveWorkRecordCount());
                info.put("createdDate", rec.getCreatedDate());
                createdTime = rec.getCreatedDate();
                if (createdTime != null) {
                    info.put("createdDate", createdTime);
                } else {
                    info.put("createdDate", "");
                }

                List<Object> jsonArr = dataManager.fromJSONString(head, List.class);
                if (!initInfo) {
                    result.put("data", jsonArr);
                    result.put("info", new HashMap<String, Object>());
                    result.put("result", false);
                    return result;
                }
                result.put("data", jsonArr);
                result.put("info", info);
                result.put("result", true);
            }
            return result;
        } catch (Exception e) {
            logger.error("error getRecordHead", e);
            throw new AppException("app.common.server.error");
        }
    }

    private int _recordRemove(long recordId, long userId, long sheetType) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
       // criteria.eq("userId", userId);
        criteria.eq("parentId", recordId);
        criteria.eq("sheetType", sheetType);
        jsonQueryBuilder.addCriteria(criteria);
        String queryStr = jsonQueryBuilder.toJsonQuerySpec();
        return ServiceLocator.getInstance().lookup(BO.ImportRecordRelationNew).batchDelete(queryStr);
    }

    private void resetRemoveErrorCount(long id, long removeCount, long removeCustomerCount, long removeContactCount,
            long removeWorkRecordCount) {
        IImportRecordNewRow rec = query(id);
        if (rec != null) {
            // 错误数
            long errorCount = 0;
            long errorCustomerCount = 0;
            long errorContactCount = 0;
            long errorWorkRecordCount = 0;

            // 错误计数赋值
            if (rec.getErrorCount() != null) {
                errorCount = rec.getErrorCount() - removeCount;
            }
            if (errorCount < 0) {
                errorCount = 0;
            }
            if (rec.getErrorCustomerCount() != null) {
                errorCustomerCount = rec.getErrorCustomerCount() - removeCustomerCount;
            }
            if (errorCustomerCount < 0) {
                errorCustomerCount = 0;
            }
            if (rec.getErrorContactCount() != null) {
                errorContactCount = rec.getErrorContactCount() - removeContactCount;
            }
            if (errorContactCount < 0) {
                errorContactCount = 0;
            }
            if (rec.getErrorWorkRecordCount() != null) {
                errorWorkRecordCount = rec.getErrorWorkRecordCount() - removeWorkRecordCount;
            }
            if (errorWorkRecordCount < 0) {
                errorWorkRecordCount = 0;
            }
            // 移除数赋值
            if (rec.getRemoveCount() != null) {
                removeCount = rec.getRemoveCount() + removeCount;
            }
            if (rec.getRemoveCustomerCount() != null) {
                removeCustomerCount = rec.getRemoveCustomerCount() + removeCustomerCount;
            }
            if (rec.getRemoveContactCount() != null) {
                removeContactCount = rec.getRemoveContactCount() + removeContactCount;
            }
            if (rec.getRemoveWorkRecordCount() != null) {
                removeWorkRecordCount = rec.getRemoveWorkRecordCount() + removeWorkRecordCount;
            }
            rec.setErrorCount(errorCount);
            rec.setErrorCustomerCount(errorCustomerCount);
            rec.setErrorContactCount(errorContactCount);
            rec.setErrorWorkRecordCount(errorWorkRecordCount);

            rec.setRemoveCount(removeCount);
            rec.setRemoveCustomerCount(removeCustomerCount);
            rec.setRemoveContactCount(removeContactCount);
            rec.setRemoveWorkRecordCount(removeWorkRecordCount);

            upsert(rec);
        }
    }

    private boolean _recordRemove(Long id) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();

        IImportRecordNewRow rec = query(id);
        if (rec == null) {
            return true;
        }
        int removeCount0 = _recordRemove(id, userId, 0L);
        int removeCount1 = _recordRemove(id, userId, 1L);
        int removeCount2 = _recordRemove(id, userId, 2L);
        int removeCount = removeCount0 + removeCount1 + removeCount2;
        if (removeCount > 0) {
            resetRemoveErrorCount(id, removeCount, removeCount0, removeCount1, removeCount2);
            return true;
        }
        return true;// 没有删除记录也返回true
    }

    @Override
    public Map<String, Object> recordRemove(Long id) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", false);
        try {
            boolean bool = _recordRemove(id);
            result.put("result", bool);
        } catch (Exception e) {
            result = getErrorMsg("app.common.server.error");
            e.printStackTrace();
        }
        return result;
    }

    private boolean _recordDetailRemove(Long id) {
        long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        IImportRecordRelationNewRow rec = (IImportRecordRelationNewRow) ServiceLocator.getInstance()
                .lookup(BO.ImportRecordRelationNew).query(id);
        if (rec == null) {
            return true;// 没有数据就当成 删除
        }
        int removeCount0 = 0;
        int removeCount1 = 0;
        int removeCount2 = 0;
        if (rec.getSheetType() == 0) {
            removeCount0 = 1;
        }
        if (rec.getSheetType() == 1) {
            removeCount1 = 1;
        }
        if (rec.getSheetType() == 2) {
            removeCount2 = 1;
        }

        long parentId = rec.getParentId();

        boolean bool = recordDetailRemove(id, userId);
        if (bool) {
            resetRemoveErrorCount(parentId, 1L, removeCount0, removeCount1, removeCount2);
        }
        return bool;
    }

    @Override
    public Map<String, Object> recordDetailRemove(Long id) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> result = new HashMap<String, Object>();
        try {
            boolean bool = _recordDetailRemove(id);
            result.put("result", bool);
        } catch (Exception e) {
            result = getErrorMsg("app.common.server.error");
            logger.error("error", e);
        }
        return result;

    }

    private void _recordsClean(long userid) {
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
       // criteria.eq("userId", userid);
        jsonQueryBuilder.addCriteria(criteria);
        String queryStr = jsonQueryBuilder.toJsonQuerySpec();
        batchDelete(queryStr);
    }

    @Override
    public Map<String, Object> recordsClean() {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> result = new HashMap<String, Object>();
        long userid = EnterpriseContext.getCurrentUser().getUserLongId();
        try {
            _recordsClean(userid);
            result.put("result", true);
        } catch (Exception e) {
            logger.error("error", e);
            result.put("result", false);
        }
        return result;
    }

    private List<Map<String, Object>> _getStatus(ArrayList<Long> ids) {
        if (ids == null || ids.size() == 0)
            return null;

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.in(SC.id, ids.toArray());
        jsonQueryBuilder.addCriteria(criteria);

        IImportRecordNewRowSet list = queryAll(jsonQueryBuilder.toJsonQuerySpec());

        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        int count = list.size();
        if (count > 0) {
            int i = 0;
            for (i = 0; i < count; i++) {
                IImportRecordNewRow rec = list.getRow(i);
                Map<String, Object> statusMap = new HashMap<String, Object>();
                statusMap.put("id", rec.getId());
                statusMap.put("status", rec.getStatus());
                statusMap.put("fileName", rec.getFileName());

                statusMap.put("total", rec.getTotal());
                statusMap.put("totalCustomer", rec.getTotalCustomer());
                statusMap.put("totalContact", rec.getTotalContact());
                statusMap.put("totalWorkRecord", rec.getTotalWorkRecord());

                statusMap.put("errorCount", rec.getErrorCount());
                statusMap.put("errorCustomerCount", rec.getErrorCustomerCount());
                statusMap.put("errorContactCount", rec.getErrorContactCount());
                statusMap.put("errorWorkRecordCount", rec.getErrorWorkRecordCount());

                statusMap.put("removeErrorCount", rec.getRemoveCount());
                statusMap.put("removeCustomerCount", rec.getRemoveCustomerCount());
                statusMap.put("removeContactCount", rec.getRemoveContactCount());
                statusMap.put("removeWorkRecordCount", rec.getRemoveWorkRecordCount());
                statusMap.put("createdDate", rec.getCreatedDate());
                result.add(statusMap);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> recordsStatus(String ids) {
        //ServiceLocator.getInstance().lookup(UserServiceItf.class).authBoss();

        Map<String, Object> result = new HashMap<String, Object>();
        ArrayList<Long> params = new ArrayList<Long>();
        if (StringUtils.isEmpty(ids)) {
            throw new AppException("app.common.params.invalid");
        }
        String idsArr[] = ids.split(",");
        int i = 0;
        int size = idsArr.length;
        for (i = 0; i < size; i++) {
            params.add(Long.valueOf(idsArr[i]));
        }
        List<Map<String, Object>> data = _getStatus(params);
        result.put("result", true);
        result.put("data", data);
        return result;
    }

    @Override
    public Map<String, Object> getErrorMsg(String name) {
        Map<String, String> msg = ExceptionResources.getResources(name);
        Map<String, Object> result = new HashMap<String, Object>();
        if (msg == null) {
            msg = ExceptionResources.getResources("app.common.server.error");
        }
        result.put("result", false);
        if (msg != null) {
            result.put("message", msg.get("info"));
            result.put("code", msg.get("code"));
        } else {
            result.put("message", "网络繁忙，请稍后再试");
            result.put("code", "502");
        }
        return result;
    }
    
    @Override
    public Long importRecordFromGZQ(List<String> headers1, List<String> headers2, List<String> headers3){
        ArrayList<Map<String, Object>> headCustomer = new ArrayList<Map<String, Object>>();
        ArrayList<Map<String, Object>> headContact = new ArrayList<Map<String, Object>>();
        ArrayList<Map<String, Object>> headWorkRecord = new ArrayList<Map<String, Object>>();
        
        Map<String, Map<String, Object>> fieldsHead1 = ImportUtil.getFieldsNew(headers1, 0);
        Map<String, Map<String, Object>> fieldsHead2 = ImportUtil.getFieldsNew(headers2, 1);
        Map<String, Map<String, Object>> fieldsHead3 = ImportUtil.getFieldsNew(headers3, 2);
        
        for (Map.Entry<String, Map<String, Object>> entry : fieldsHead1.entrySet()) {
            headCustomer.add(entry.getValue());
        }
        for (Map.Entry<String, Map<String, Object>> entry : fieldsHead2.entrySet()) {
            headContact.add(entry.getValue());
        }
        for (Map.Entry<String, Map<String, Object>> entry : fieldsHead3.entrySet()) {
            headWorkRecord.add(entry.getValue());
        }
		  IImportRecordNewRow rec = createRow();
	      rec.setUserId(EnterpriseContext.getCurrentUser().getUserLongId());
	      rec.setStatus(0L);
	      rec.setTotal(0L);
	      rec.setTotalContact(0L);
	      rec.setTotalCustomer(0L);
	      rec.setTotalWorkRecord(0L);
	      rec.setRemoveCount(0L);
	      rec.setRemoveContactCount(0L);
	      rec.setRemoveCustomerCount(0L);
	      rec.setRemoveWorkRecordCount(0L);
	      rec.setErrorCount(0L);
	      rec.setErrorContactCount(0L);
	      rec.setErrorCustomerCount(0L);
	      rec.setErrorWorkRecordCount(0L);
          rec.setHead(dataManager.toJSONString(headCustomer) + "&" + dataManager.toJSONString(headContact) + "&"
                  + dataManager.toJSONString(headWorkRecord));
         upsert(rec);
         return rec.getId();
    }
    
    @Override
    public IImportRecordNewRow getLastestImportReocrd(){
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.lt(ImportRecordNewMetaData.status, 1L);
        criteria.addChild(Criteria.NOT().empty(ImportRecordNewMetaData.fileURL));
        jsonQueryBuilder.addCriteria(criteria).addOrderAsc(SC.createdDate).addOrderAsc(SC.id).setMaxResult(1);
        IImportRecordNewRowSet rowSet = query(jsonQueryBuilder.toJsonQuerySpec());
    	if(rowSet!=null&&rowSet.getRows().size()>0){
    		return rowSet.getRow(0); 
    	}
        return null;
    }
}
