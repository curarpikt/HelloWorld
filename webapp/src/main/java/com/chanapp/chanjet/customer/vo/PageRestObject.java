package com.chanapp.chanjet.customer.vo;

public class PageRestObject {

    private String param;

    private Integer pageno;

    private Integer pagesize;

    public Integer getPageno() {
        if (pageno == null || pageno < 0) {
            return 1;
        }
        return pageno;
    }

    public void setPageno(Integer pageno) {
        this.pageno = pageno;
    }

    public Integer getPagesize() {
        if (pagesize == null || pagesize < 0) {
            return 10;
        }
        return pagesize;
    }

    public void setPagesize(Integer pagesize) {
        this.pagesize = pagesize;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Integer getPageno(long total) {
        long allPage = (total / this.pagesize) + 1;
        if (allPage < this.pageno) {
            this.pageno = (int) allPage;
        }
        return this.pageno;
    }
}
