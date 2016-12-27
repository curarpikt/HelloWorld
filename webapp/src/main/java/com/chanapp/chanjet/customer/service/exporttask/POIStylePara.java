package com.chanapp.chanjet.customer.service.exporttask;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

public class POIStylePara {

    /**
     * 单元格样式
     */
    private HSSFCellStyle cellStyle;
    /**
     * 样式起始列 从0开始
     */
    private Integer colStart;
    /**
     * 样式截止列
     */
    private Integer colEnd;

    public HSSFCellStyle getCellStyle() {
        return cellStyle;
    }

    public void setCellStyle(HSSFCellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }

    public Integer getColStart() {
        return colStart;
    }

    public void setColStart(Integer colStart) {
        this.colStart = colStart;
    }

    public Integer getColEnd() {
        return colEnd;
    }

    public void setColEnd(Integer colEnd) {
        this.colEnd = colEnd;
    }

}
