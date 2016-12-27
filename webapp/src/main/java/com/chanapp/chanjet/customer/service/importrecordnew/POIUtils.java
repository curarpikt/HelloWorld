package com.chanapp.chanjet.customer.service.importrecordnew;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.CellRangeAddressList;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ui.util.OSSUtil;

@SuppressWarnings("deprecation")
public class POIUtils {
    private final static Logger logger = LoggerFactory.getLogger(POIUtils.class);
    // private final static int FORMULA_BIGGER_CHAR = 240;

    private final static int FORMULA_HALF_BIGGER_CHAR = 120;

    public static HSSFSheet setHSSFValidation(HSSFSheet sheet, HSSFSheet hiddenSheet, String[] textlist, int firstRow,
            int endRow, int firstCol, int endCol) {
        String hiddenSheetName = hiddenSheet.getSheetName();
        StringBuffer sb = new StringBuffer();
        for (String text : textlist) {
            sb.append(text);
        }
        // 加载下拉列表内容
        DVConstraint constraint = null;
        // if(sb.length() > FORMULA_BIGGER_CHAR){// mod by JIAYUEP
        if (sb.length() > FORMULA_HALF_BIGGER_CHAR) {
            for (int j = 0, length = textlist.length; j < length; j++) {

                // HSSFRow hiddenRow = hiddenSheet.createRow(j);
                HSSFRow hiddenRow = hiddenSheet.getRow(j);
                if (hiddenRow == null) {
                    hiddenRow = hiddenSheet.createRow(j);
                }
                HSSFCell hiddenCell = hiddenRow.createCell(firstCol);
                hiddenCell.setCellValue(textlist[j]);
            }
            String letter = columnNumToLetter(firstCol + 1);
            constraint = DVConstraint.createFormulaListConstraint(
                    hiddenSheetName + "!$" + letter + "$1:$" + letter + "$" + textlist.length);
        } else {
            constraint = DVConstraint.createExplicitListConstraint(textlist);
        }
        // 设置数据有效性加载在哪个单元格上,四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, endRow, firstCol, endCol);
        // 数据有效性对象
        HSSFDataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
        sheet.addValidationData(data_validation_list);
        return sheet;
    }

    public static String columnNumToLetter(int iCol) {
        String letter = "";
        if (iCol <= 26) {
            letter = String.valueOf((char) (iCol + 64));
        } else {
            letter = columnNumToLetter((iCol - 1) / 26) + String.valueOf((char) ((iCol - 1) % 26 + 65));
        }
        return letter;
    }

    public static Map<String, List<String>> excel(File file) {
        // Customer_ Contact_ WorkRecord_
        Map<String, List<String>> rs = new HashMap<String, List<String>>();

        if (file == null) {
            return rs;
        }
        String destPath = file.getAbsolutePath();
        String type = OSSUtil.getSuffix(destPath);

        List<String> customerRows = new ArrayList<String>();
        List<String> contactRows = new ArrayList<String>();
        List<String> workRecordRows = new ArrayList<String>();
        try {
            // excel解析
            Workbook workbook = getWrokbook(type, new FileInputStream(destPath));
            for (int i = 0; i < 3; i++) {
                // 获得excel表头信息
                List<String> excelHeaders = readExcelTitle(type, new FileInputStream(destPath), i);
                Map<String, Map<String, Object>> fields = ImportUtil.getFieldsNew(excelHeaders, i);
                if (i > 0) {
                    // fields.put(key, value)
                }
                Sheet sheet = workbook.getSheetAt(i);
                if (sheet != null) {
                    int column_num = sheet.getRow(0).getPhysicalNumberOfCells();
                    int row_num = sheet.getLastRowNum();
                    int row_i = 1;
                    // 从第一行开始读取
                    for (row_i = 1; row_i <= row_num; row_i++) {
                        // 每行的数据
                        Row row = sheet.getRow(row_i);
                        if (row != null) {
                            Map<String, Map<String, Object>> newData = new LinkedHashMap<String, Map<String, Object>>();
                            int column_i = 0;
                            // 获得每行的列数据
                            for (column_i = 0; column_i < column_num; column_i++) {
                                Cell cell = row.getCell(column_i);
                                Map<String, Object> newRow = null;
                                String v = "";
                                String columnName = "";
                                if (cell != null) {
                                    v = parseExcel(cell);
                                }
                                try {
                                    columnName = excelHeaders.get(column_i);
                                } catch (IndexOutOfBoundsException e) {
                                }
                                if (fields.containsKey(columnName)) {
                                    newRow = fields.get(columnName);
                                    newRow.put("value", v);
                                    newData.put(newRow.get("field").toString(), newRow);
                                }
                            }
                            if ((newData.containsKey("Customer_name")
                                    && StringUtils.isNotEmpty(newData.get("Customer_name").get("value").toString()))
                                    || (newData.containsKey("name")
                                            && StringUtils.isNotEmpty(newData.get("name").get("value").toString()))
                                    || (newData.containsKey("owner")
                                            && StringUtils.isNotEmpty(newData.get("owner").get("value").toString()))) {
                                if (i == 0) {
                                    customerRows.add(AppWorkManager.getDataManager().toJSONString(newData));
                                }
                                if (i == 1) {
                                    contactRows.add(AppWorkManager.getDataManager().toJSONString(newData));
                                }
                                if (i == 2) {
                                    workRecordRows.add(AppWorkManager.getDataManager().toJSONString(newData));
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            logger.error("error excel", e);
        }

        rs.put("Customer_", customerRows);
        rs.put("Contact_", contactRows);
        rs.put("WorkRecord_", workRecordRows);
        return rs;
    }

    public static String parseExcel(Cell cell) {
        String result = "";
        switch (cell.getCellType()) {
            case HSSFCell.CELL_TYPE_NUMERIC:// 数字类型
                if (HSSFDateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = null;
                    short format = cell.getCellStyle().getDataFormat();
                    if (format == 14 || format == 180) {
                        sdf = new SimpleDateFormat("yyyy-MM-dd");
                    }
                    if (sdf != null) {
                        double value = cell.getNumericCellValue();
                        Date date = DateUtil.getJavaDate(value);
                        result = sdf.format(date);
                    }
                } else {
                    double value = cell.getNumericCellValue();
                    CellStyle style = cell.getCellStyle();
                    DecimalFormat format = new DecimalFormat("#");
                    String temp = style.getDataFormatString();
                    // 单元格设置成常规
                    if (temp.equals("General")) {
                        format.applyPattern("#");
                    }
                    result = format.format(value);
                }
                break;
            case HSSFCell.CELL_TYPE_STRING:// String类型
                result = cell.getRichStringCellValue().toString();
                break;
            case HSSFCell.CELL_TYPE_BLANK:
                result = "";
                break;
            default:
                result = "";
                break;
        }
        if (result != null) {
            return result.trim();
        }
        return "";
    }

    public static HSSFWorkbook getWrokbook(String type, InputStream is) {
        HSSFWorkbook workbook = null;
        if ("xls".equals(type)) {
            try {
                if (is != null) {
                    workbook = new HSSFWorkbook(is);
                } else {
                    workbook = new HSSFWorkbook();
                }
            } catch (IOException e) {
                logger.error("getWrokbook error", e);
            }
        } else if ("xlsx".equals(type)) {
            try {
                if (is != null) {
                    workbook = new HSSFWorkbook(is);
                } else {
                    workbook = new HSSFWorkbook();
                }
            } catch (IOException e) {
                logger.error("getWrokbook error", e);
            }
        }
        return workbook;
    }

    // add sheetNo 原来是0 0开始
    public static List<String> readExcelTitle(String type, InputStream is, int sheetNo) {

        List<String> header = new ArrayList<String>();
        Workbook workbook = getWrokbook(type, is);
        if (workbook == null) {
            return header;
        }
        Sheet sheet = workbook.getSheetAt(sheetNo);
        if (sheet == null) {// 如果删sheet返回空数组
            return header;
        }
        // String sheetname = sheet.getSheetName();
        Row row = sheet.getRow(0);
        if (row == null) {// 如果删sheet返回空数组
            return header;
        }
        int colNum = row.getPhysicalNumberOfCells();

        for (int i = 0; i < colNum; i++) {
            header.add(parseExcel(row.getCell((short) i)));
        }
        return header;
    }
}
