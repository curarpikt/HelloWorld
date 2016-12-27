package com.chanapp.chanjet.customer.restlet.v2.web.contact;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRowSet;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据客户ID，查询客户下的联系人
 * 
 * @author tds
 *
 */
public class ListOfCustomer extends BaseRestlet {
    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        IContactRowSet contactSet = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .findContactsByCustomer(customerId);
        RowSet rowSet = BoRowConvertUtil.toRowSet(contactSet);
        rowSet.setTotal(rowSet.getItems().size());
        return rowSet;
    }

}
