package com.chanapp.chanjet.customer.service.exporttask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
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
import org.apache.poi.hssf.util.Region;
import com.chanapp.chanjet.customer.constant.SRU;

/**
 * POI excel导出 工具类
 * 
 * @author JIAYUEP 2015-05-25
 */
@SuppressWarnings("deprecation")
public class ExportExcel {

    /**
     * 调用demo 1 ：main 函数 generateWorkbook() 创建工作薄 2：generateCellStyle(...) 创建样式
     * 3：generateSheet(...) 创建sheet（多sheet调用多次）
     */
    public static void main(String[] args) {
        // demo1();
        demo2();

    }

    public final static Integer CELL_MAX_LEN_LIMIT = 40;

    public static HSSFWorkbook demo2() {
        HSSFWorkbook workbook = ExportExcel.generateWorkbook();

        HSSFCellStyle commStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.WHITE,
                POIColorConstants.BLACK);

        POIStylePara commStylePara = new POIStylePara();
        commStylePara.setCellStyle(commStyle);
        HSSFCellStyle titleStyle1 = ExportExcel.generateTitleCellStyle(workbook, POIColorConstants.BLUE,
                POIColorConstants.RED);

        POIStylePara POIStylePara1 = new POIStylePara();
        POIStylePara1.setCellStyle(titleStyle1);
        POIStylePara1.setColStart(0);
        POIStylePara1.setColEnd(3);
        List<String> titles = new ArrayList<String>();

        titles.add("客户_客户名称");
        titles.add("客户_电话");
        titles.add("跟单状态");
        titles.add("客户_客户级别");

        List<Map<String, Object>> dataList = new ArrayList<>();

        for (int j = 0; j < 4; j++) {
            Map<String, Object> data1 = new LinkedHashMap<String, Object>();
            data1.put("name", "张三" + j);
            data1.put("mobil", "1310000000" + j);
            data1.put("statuse", "跟单状态" + j);
            data1.put("lev", "级别" + j);
            dataList.add(data1);

        }

        // ExportExcel.generateMySheet(workbook, titles, sheetName, dataList,
        // commStylePara);

        try {
            OutputStream out = new FileOutputStream("E://demoM.xls");
            workbook.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return workbook;
    }

    public static void generateAttendanceSheet(HSSFWorkbook workbook, List<AttendanceExportHeader> titles,
            List<AttendanceExportMergeDownRow> mergeDownRows, String sheetName, HSSFCellStyle titleStyle,
            HSSFCellStyle commStyle) {
        // 生成一个表格
        HSSFSheet sheet = workbook.createSheet(sheetName);

        // 定义一个红色字 一个黑色字
        HSSFFont fontRed = workbook.createFont();
        fontRed.setFontHeightInPoints((short) 10); // 字体高度
        fontRed.setFontName("宋体"); // 字体
        fontRed.setColor(HSSFColor.RED.index);
        // fontRed.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度

        HSSFFont fontBlack = workbook.createFont();
        fontBlack.setFontHeightInPoints((short) 10); // 字体高度
        fontBlack.setFontName("宋体"); // 字体
        fontBlack.setColor(HSSFColor.BLACK.index);
        // fontBlack.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); // 宽度

        HSSFCellStyle redStyle = ExportExcel.generateCellStyle(workbook, POIColorConstants.WHITE,
                POIColorConstants.RED);

        // 生成title
        HSSFRow row = sheet.createRow(0);

        row.setHeight((short) 400);

        for (int i = 0; i < titles.size(); i++) {
            HSSFCell cell = row.createCell(i);
            cell.setCellStyle(titleStyle);
            HSSFRichTextString text = new HSSFRichTextString(titles.get(i).getColumnLabel());
            cell.setCellValue(text);
        }

        // 初始化数据
        int rowNo = 1;
        int colNo = 0;
        for (int i = 0; i < mergeDownRows.size(); i++) {
            AttendanceExportMergeDownRow mergeDownRow = mergeDownRows.get(i);
            List<AttendanceExportData> datas = mergeDownRow.getExportDataList();

            for (int j = 0; j < datas.size(); j++) {// 创建每一组的每一行

                colNo = 0;
                AttendanceExportData data = datas.get(j);
                row = sheet.createRow(rowNo);// 第0行被title占去了。

                int colI = data.getDaysData().size() + 5;// 统计天数 加5个固定列
                for (int k = 0; k < colI; k++) {// 循环产生cell
                    if (j == 0 && colNo == 0) {// 每一部门的第一行 第一列 是部门 向下合并 开始行 开始列
                                               // 结束行 结束列
                        sheet.addMergedRegion(new Region((short) rowNo, (short) colNo,
                                (short) (rowNo + datas.size() - 1), (short) colNo));
                        HSSFCell cell = row.createCell(colNo);
                        cell.setCellStyle(commStyle);
                        HSSFRichTextString richString = new HSSFRichTextString(mergeDownRow.getParentName() + "(组)");
                        cell.setCellValue(richString);
                        colNo++;
                    } else if (j != 0 && colNo == 0) {// 每一组的非首行 不创建单元格 为了边框样式生效
                                                      // 要创建空单元格
                        HSSFCell cell = row.createCell(colNo);
                        cell.setCellStyle(commStyle);
                        HSSFRichTextString richString = new HSSFRichTextString("");
                        cell.setCellValue(richString);

                        colNo++;
                    } else {
                        // 姓名
                        if (k == 1) {
                            HSSFCell cell1 = row.createCell(colNo);
                            cell1.setCellStyle(commStyle);
                            HSSFRichTextString richString1 = new HSSFRichTextString(data.getName());
                            cell1.setCellValue(richString1);
                            colNo++;
                        }
                        // 实际出勤
                        if (k == 2) {
                            HSSFCell cell2 = row.createCell(colNo);
                            cell2.setCellStyle(commStyle);
                            HSSFRichTextString richString2 = new HSSFRichTextString(
                                    data.getActualTimes() == null ? "--" : data.getActualTimes());
                            cell2.setCellValue(richString2);
                            colNo++;
                        }
                        // 迟到
                        if (k == 3) {
                            HSSFCell cell3 = row.createCell(colNo);
                            if (data.getLateTimes() != null && !"0".equals(data.getLateTimes())
                                    && !"--".equals(data.getLateTimes()) && !"".equals(data.getLateTimes())) {
                                cell3.setCellStyle(redStyle);
                            } else {
                                cell3.setCellStyle(commStyle);
                            }

                            HSSFRichTextString richString3 = new HSSFRichTextString(
                                    data.getLateTimes() == null ? "--" : data.getLateTimes());
                            cell3.setCellValue(richString3);
                            colNo++;
                        }
                        // 早退
                        if (k == 4) {
                            HSSFCell cell4 = row.createCell(colNo);
                            if (data.getLeaveEarlyTimes() != null && !"0".equals(data.getLeaveEarlyTimes())
                                    && !"--".equals(data.getLeaveEarlyTimes())
                                    && !"".equals(data.getLeaveEarlyTimes())) {
                                cell4.setCellStyle(redStyle);
                            } else {
                                cell4.setCellStyle(commStyle);
                            }
                            HSSFRichTextString richString4 = new HSSFRichTextString(
                                    data.getLeaveEarlyTimes() == null ? "--" : data.getLeaveEarlyTimes());
                            cell4.setCellValue(richString4);
                            colNo++;
                        }
                        if (k > 4) {
                            AttendanceExportDayData dayData = data.getDaysData().get(k - 5);
                            if ("0".equals(dayData.getAbsence())) {
                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStyle);
                                HSSFRichTextString richString = new HSSFRichTextString("--");// 缺勤
                                cell.setCellValue(richString);
                            } else {

                                // 同一单元格不同样式
                                String start = "" + dayData.getOnTime();
                                String mid = "--";
                                String end = "" + dayData.getOffTime();
                                String cellStr = start + mid + end;
                                HSSFRichTextString ts = new HSSFRichTextString(cellStr);
                                if ("0".equals(dayData.getOnStatus())) {
                                    ts.applyFont(0, start.length(), fontBlack);
                                } else {
                                    ts.applyFont(0, start.length(), fontRed);
                                }
                                ts.applyFont(start.length(), start.length() + mid.length(), fontBlack);
                                if ("0".equals(dayData.getOffStatus())) {
                                    ts.applyFont(start.length() + mid.length(), cellStr.length(), fontBlack);
                                } else {
                                    ts.applyFont(start.length() + mid.length(), cellStr.length(), fontRed);
                                }

                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStyle);
                                cell.setCellValue(ts);
                            }
                            colNo++;
                        }
                    }

                } // DaysData end

                rowNo++;
            } // datas end

        } // mergeDownRows end

        // 设置表格默认列宽度为15个字节
        sheet.setDefaultColumnWidth((short) 18);
        sheet.setColumnWidth(2, 12 * 256);
        sheet.setColumnWidth(3, 8 * 256);
        sheet.setColumnWidth(4, 8 * 256);
        // 前两个参数是你要用来拆分的列数和行数。后两个参数是下面窗口的可见象限，其中第三个参数是右边区域可见的左边列数，第四个参数是下面区域可见的首行。
        // 冻结首行
        // sheet.createFreezePane( 0, 1, 0, 1 );
        // 冻结首列
        // sheet.createFreezePane( 1, 0, 1, 0 );

        // 冻结5行 首列
        sheet.createFreezePane(5, 1, 5, 1);

    }

    /**
     * 创建一个工作薄
     * 
     * @return Workbook
     */
    public static HSSFWorkbook generateWorkbook() {
        HSSFWorkbook workbook = new HSSFWorkbook();
        return workbook;
    }

    /**
     * 
     * @param workbook
     * @param foregroundColor 单元格前景色
     * @param fontColor 字体颜色
     * @return HSSFCellStyle
     */

    public static HSSFCellStyle generateCellStyle(HSSFWorkbook workbook, short foregroundColor, short fontColor) {
        // 生成一个title样式 蓝底 红字
        HSSFCellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(foregroundColor);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

        style.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框

        style.setAlignment(HSSFCellStyle.ALIGN_LEFT);// 水平
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(fontColor);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style.setFont(font);
        return style;
    }

    /**
     * 
     * @param workbook
     * @param foregroundColor 单元格前景色
     * @param fontColor 字体颜色
     * @return HSSFCellStyle
     */

    public static HSSFCellStyle generateTitleCellStyle(HSSFWorkbook workbook, short foregroundColor, short fontColor) {
        // 生成一个title样式 蓝底 红字
        HSSFCellStyle style = workbook.createCellStyle();
        // 设置这些样式
        style.setFillForegroundColor(foregroundColor);
        style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        style.setBorderRight(HSSFCellStyle.BORDER_THIN);
        style.setBorderTop(HSSFCellStyle.BORDER_THIN);
        style.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 水平
        style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 垂直
        // 生成一个字体
        HSSFFont font = workbook.createFont();
        font.setColor(fontColor);
        font.setFontHeightInPoints((short) 10);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
        // 把字体应用到当前的样式
        style.setFont(font);
        return style;
    }

    /**
     * 计算字符串单字节长度 利用正则表达式将每个中文字符转换为"**" 匹配中文字符的正则表达式： [\u4e00-\u9fa5]
     * 匹配双字节字符(包括汉字在内)：[^\x00-\xff]
     * 
     * @param validateStr
     * @return
     */
    private static int getRegExpLength(String validateStr) {
        if (validateStr == null) {
            return 0;
        }
        // String temp = validateStr.replaceAll("[\u4e00-\u9fa5]", "**");
        String temp = validateStr.replaceAll("[^\\x00-\\xff]", "**");
        return temp.length();
    }

    /**
     * 生成sheet
     * 
     * @param workbook 工作薄
     * @param titles title文字列表
     * @param colWidths 列宽结合 默认15 传0为自适应
     * @param sheetName sheet名
     * @param dataList 数据填充
     * @param enumDatas 枚举类型下拉初始化 <key:列号,value:List<String>>
     * @param titleStyleParas title样式 不用指定开始结束列
     * @param firstColStylePara 第一列样式 不用指定开始结束列
     * @param commStylePara 通用单元格样式
     * @param paras 如控制邮箱 URL超链接样显示等
     */
    @SuppressWarnings({ "rawtypes" })
    public static void generateSheet(HSSFWorkbook workbook, List<String> titles, List<Integer> colWidths,
            String sheetName, List<Map<String, Object>> dataList, Map<Integer, List<String>> enumDatas,
            List<POIStylePara> titleStyleParas, POIStylePara firstColStylePara, POIStylePara commStylePara,
            Map<String, Object> paras) {

        HSSFSheet sheet = workbook.createSheet(sheetName);
        HSSFRow row = sheet.createRow(0);

        List<Integer> colMaxWidths = init_sheet(sheet, row, titles, titleStyleParas);

        // 初始化数据
        for (int i = 0; i < dataList.size(); i++) {
            row = sheet.createRow(i + 1);// 第0行被title占去了。
            row.setHeight((short) 400);
            Map<String, Object> dataMap = dataList.get(i);
            Iterator iter = dataMap.entrySet().iterator();
            int colI = 0;
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object val = entry.getValue();

                // add 计算本例字符串最大长度
                if (getRegExpLength(val.toString()) > colMaxWidths.get(colI)) {
                    colMaxWidths.set(colI, getRegExpLength(val.toString()));
                }

                HSSFCell cell = row.createCell(colI);

                if (colI == 0) {// 如果是第一列
                    cell.setCellStyle(firstColStylePara.getCellStyle());
                } else {
                    cell.setCellStyle(commStylePara.getCellStyle());
                }
                HSSFRichTextString richString = new HSSFRichTextString(val.toString());
                cell.setCellValue(richString);
                colI++;
            }
        }

        set_sheet_with(sheet, colMaxWidths, colMaxWidths);

    }

    private static List<Integer> init_sheet(HSSFSheet sheet, HSSFRow row, List<String> titles,
            List<POIStylePara> titleStyleParas) {

        // 产生表格标题行
        // title有几段样式
        // 如 0-0 1-2 3-6 7-12
        int dividCount = titleStyleParas.size();
        int[] colEnds = new int[dividCount];
        int[] colStarts = new int[dividCount];

        // 取得每个样式应该作用的開始結束列
        for (int i = 0; i < dividCount; i++) {
            colEnds[i] = titleStyleParas.get(i).getColEnd();
            colStarts[i] = titleStyleParas.get(i).getColStart();
        }

        // 生成title

        row.setHeight((short) 400);

        // add 计算本列 字符串的最大长度
        List<Integer> colMaxWidths = new ArrayList<Integer>();

        for (int i = 0; i < titles.size(); i++) {
            colMaxWidths.add(getRegExpLength(titles.get(i)));
            HSSFCell cell = row.createCell(i);
            for (int j = colEnds.length - 1; j >= 0; j--) {
                // 如果初始化的cell 位于样式数组中 最大最小区间 即取该样式
                if (i <= colEnds[j] && i >= colStarts[j]) {

                    cell.setCellStyle(titleStyleParas.get(j).getCellStyle());
                    break;
                }
            }

            HSSFRichTextString text = new HSSFRichTextString(titles.get(i));
            cell.setCellValue(text);
        }

        return colMaxWidths;
    }

    private static void set_sheet_with(HSSFSheet sheet, List<Integer> colWidths, List<Integer> colMaxWidths) {
        // 设置列宽
        if (null != colWidths && colWidths.size() > 0) {
            for (int i = 0; i < colWidths.size(); i++) {
                if (colWidths.get(i) == 0) {
                    int charaecterLength = (colMaxWidths.get(i) + 1) > CELL_MAX_LEN_LIMIT ? CELL_MAX_LEN_LIMIT
                            : (colMaxWidths.get(i) + 1);
                    sheet.setColumnWidth(i, (charaecterLength * 256)); // 计算列最大宽度（长度*256）
                } else {
                	if(colWidths.get(i)>255){
                	    sheet.setColumnWidth(i, 100 * 256);	
                	}else{
                		sheet.setColumnWidth(i, colWidths.get(i) * 256);
                	}                  
                }
            }
        } else {
            // 设置表格默认列宽度为15个字节
            sheet.setDefaultColumnWidth((short) 15 * 256);
        }

        // 冻结首行 首列
        sheet.createFreezePane(1, 1, 1, 1);
    }

    @SuppressWarnings({ "unchecked" })
    public static void generateVisitSheet(HSSFWorkbook workbook, List<String> titles, List<Integer> colWidths,
            String sheetName, Map<String, Object> rsMap, Map<Integer, List<String>> enumDatas,
            List<POIStylePara> titleStyleParas, POIStylePara commStylePara, POIStylePara commStylePara2,
            Map<String, Object> paras) {

        HSSFSheet sheet = workbook.createSheet(sheetName);
        HSSFRow row = sheet.createRow(0);

        List<Integer> colMaxWidths = init_sheet(sheet, row, titles, titleStyleParas);

        // 初始化数据
        List<Map<String, Object>> groups = (List<Map<String, Object>>) rsMap.get("data");
        if (groups != null && groups.size() > 0) {

            List<VisitExportData> emptydatas = new ArrayList<VisitExportData>();
            VisitExportData emptydata = new VisitExportData();
            emptydata.setCustomerName("");
            emptydata.setCheckinTimeStr("");
            emptydatas.add(emptydata);

            // 初始化数据
            int rowNo = 1;
            int colNo = 0;
            for (int i = 0; i < groups.size(); i++) {// 创建组
                Map<String, Object> group = groups.get(i);
                List<Object> users = (List<Object>) group.get("children");

                int groupMergeCount = 0;// 计算组向下合并的单元格
                for (int j = 0; j < users.size(); j++) {
                    Map<String, Object> userMap = (Map<String, Object>) users.get(j);
                    List<VisitExportData> datas = (List<VisitExportData>) userMap.get("visit") == null ? emptydatas
                            : (List<VisitExportData>) userMap.get("visit");
                    groupMergeCount = groupMergeCount + datas.size();
                }

                for (int j = 0; j < users.size(); j++) {// 创建人
                    Map<String, Object> userMap = (Map<String, Object>) users.get(j);
                    List<VisitExportData> datas = (List<VisitExportData>) userMap.get("visit") == null ? emptydatas
                            : (List<VisitExportData>) userMap.get("visit");

                    int userMergeCount = datas.size();// 计算组员向下合并的单元格

                    for (int k = 0; k < datas.size(); k++) {// 创建行
                        colNo = 0;// 每一行其实列号为0
                        VisitExportData data = datas.get(k);
                        row = sheet.createRow(rowNo);// 第0行被title占去了。

                        for (int t = 0; t < 4; t++) {// 循环产生4个cell
                            if (j == 0 && k == 0 && colNo == 0) {// 每一部门 第一行 第一列
                                                                 // 是部门 向下合并 开始行
                                                                 // 开始列 结束行 结束列
                                sheet.addMergedRegion(new Region((short) rowNo, (short) colNo,
                                        (short) (rowNo + groupMergeCount - 1), (short) colNo));
                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStylePara.getCellStyle());
                                HSSFRichTextString richString = new HSSFRichTextString(
                                        (group.get("name") == null ? "" : group.get("name")) + "(组)");

                                cell.setCellValue(richString);

                            } else if ((k != 0 && colNo == 0) || (j != 0 && k == 0 && colNo == 0)) {// 首列非首行
                                                                                                    // 给空单元格
                                                                                                    // 原来是
                                                                                                    // j

                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStylePara.getCellStyle());
                                HSSFRichTextString richString = new HSSFRichTextString("组合并");
                                cell.setCellValue(richString);

                            } else if (k == 0 && colNo == 1) {// 每一人 第一行 二列 是部门
                                                              // 向下合并 开始行 开始列
                                                              // 结束行 结束列
                                sheet.addMergedRegion(new Region((short) rowNo, (short) colNo,
                                        (short) (rowNo + userMergeCount - 1), (short) colNo));
                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStylePara.getCellStyle());

                                HSSFRichTextString richString = new HSSFRichTextString(
                                        (userMap.get("name") == null ? "" : userMap.get("name")) + "");
                                if (SRU.STATUS_DISABLE.equals(userMap.get("status").toString())) {
                                    richString = new HSSFRichTextString(
                                            (userMap.get("name") == null ? "" : userMap.get("name")) + "(停用)");
                                }
                                cell.setCellValue(richString);
                            } else if (k != 0 && colNo == 1) {// 第二列非首行 给空单元格
                                HSSFCell cell = row.createCell(colNo);
                                cell.setCellStyle(commStylePara.getCellStyle());
                                HSSFRichTextString richString = new HSSFRichTextString("人合并");
                                cell.setCellValue(richString);
                            } else {
                                // 客户
                                if (t == 2) {
                                    HSSFCell cell1 = row.createCell(colNo);
                                    cell1.setCellStyle(commStylePara.getCellStyle());
                                    HSSFRichTextString richString1 = new HSSFRichTextString(
                                            data.getCustomerName() == null ? "" : data.getCustomerName());
                                    cell1.setCellValue(richString1);
                                }
                                // 时间
                                if (t == 3) {
                                    HSSFCell cell2 = row.createCell(colNo);
                                    cell2.setCellStyle(commStylePara.getCellStyle());
                                    HSSFRichTextString richString2 = new HSSFRichTextString(
                                            data.getCheckinTimeStr() == null ? "" : data.getCheckinTimeStr());
                                    cell2.setCellValue(richString2);
                                }
                            }
                            colNo++;
                        }
                        rowNo++;
                    } // datas end
                } // users end
            } // groups end
        }

        set_sheet_with(sheet, colMaxWidths, colMaxWidths);
    }

}
