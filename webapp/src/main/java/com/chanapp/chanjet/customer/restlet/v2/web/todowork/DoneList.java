package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import com.chanapp.chanjet.customer.constant.TD;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 待办完成列表
 * 
 * @author tds
 *
 */
public class DoneList extends BaseRestlet {
    @Override
    public Object run() {
        String timeType = this.getParam("timeType");
        Integer pageno = this.getParamAsInt("pageno");
        Integer pagesize = this.getParamAsInt("pagesize");

        if (pageno == 0) {
            pageno = 1;
        }
        if (pagesize < 0 || pagesize < 1) {
            throw new AppException("app.common.params.invalid");
        }

        return ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class).findTodoWorks(timeType, TD.STATUS_DONE,
                pageno, pagesize);
    }
}
