package com.chanapp.chanjet.customer.restlet.v2.web.todowork;

import java.util.ArrayList;
import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRowSet;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 查询工作记录下的待办
 * 
 * @author tds
 *
 */
public class FindByWorkRecordId extends BaseRestlet {
    @Override
    public Object run() {
        Long workRecordId = this.getParamAsLong("workrecordId");
        Assert.notNull(workRecordId);

        ITodoWorkRowSet todoWorks = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class)
                .findByWorkrecordId(workRecordId);

        List<Row> returns = new ArrayList<Row>();
        for (int i = 0; i < todoWorks.size(); i++) {
            ITodoWorkRow todoWork = todoWorks.getRow(i);
            returns.add(BoRowConvertUtil.toRow(todoWork));
        }
        return returns;

    }
}
