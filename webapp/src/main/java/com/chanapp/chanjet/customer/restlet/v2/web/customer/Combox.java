package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.List;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 客户下拉框查询
 * 
 * @author tds
 *
 */
public class Combox extends BaseRestlet {
    @Override
    public Object run() {
        try {
            String param = this.getParam("param");
            int pageNo = this.getParamAsInt("pageno");
            int pageSize = this.getParamAsInt("pagesize");

            CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
            long total = customerService.countCustomers(param);
            long allPage = (total / pageSize) + 1;
            if (allPage < pageNo) {
                pageNo = (int) allPage;
            }
            List<Row> entites = customerService.customerForCombox(param, pageNo, pageSize);
            RowSet rowSet = new RowSet();
            rowSet.setItems(entites);
            rowSet.setTotal(total);
            return rowSet;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(e, "app.common.server.error");
        }
    }

}
