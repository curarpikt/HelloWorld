package com.chanapp.chanjet.customer.vo;

/**
 * 3.3版本的appext/REST接口返回的数据格式
 * 
 * @author tds
 *
 */
public class AppextResult {
    private Object resultObj;

    public AppextResult(Object resultObj) {
        this.resultObj = resultObj;
    }

    /**
     * @return the resultObj
     */
    public Object getResultObj() {
        return resultObj;
    }

    /**
     * @param resultObj the resultObj to set
     */
    public void setResultObj(Object resultObj) {
        this.resultObj = resultObj;
    }
}
