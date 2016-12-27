package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class WithContact extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(WithContact.class);

    /**
     * WEB端，没有合并概念的新增客户带联系人。 （入参中客户ID一定为NULL）
     * 
     * @param customerAndContactsVaule 客户联系人信息（JSON字符串）
     * @return Object mergeFlag:false、customer:客户所有信息（DB）、contact:联系人信息（DB）
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("add customer:{}", payload);

        // 返回值
        Map<String, Object> result = new HashMap<String, Object>();
     
        Map<String, Object> boRowMap = dataManager.jsonStringToMap(payload);
        LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) boRowMap.get("customerValue");

        // 客户名称重复性检查
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        Map<String, Object> sameCustomer = customerService.getExistsCustomer(null, (String) customerParam.get("name"),
                (String) customerParam.get("phone"));

        Assert.customerRepeat(sameCustomer);

        // 新增客户
        ICustomerRow customerRow = customerService.addCustomer(customerParam);
        // 返回结果设置
        result.put("mergeFlag", false);
        result.put("customer", BoRowConvertUtil.toRow(customerRow));

        // 新增联系人
        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) boRowMap.get("contactValue");

        if (contactParam != null) {
            ContactServiceItf contactService = ServiceLocator.getInstance().lookup(ContactServiceItf.class);
            IContactRow contactRow = contactService.addContactForCustomer(contactParam, customerRow.getId());
            result.put("contact", BoRowConvertUtil.toRow(contactRow));
        }

        // 返回
        return result;
    }

}
