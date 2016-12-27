package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.common.base.exception.AppException;

/**
 * 按客户名称或者客户电话模糊查询，全企业下是否重复。
 * 
 * @author tds
 *
 */
public class CheckNameRepeat extends BaseRestlet {
    @Override
    public Object run() {

        String name = this.getParam("name");
        Assert.hasLength(name, "app.customer.name.required");

        name = ConvertUtil.toUTF8(name);

        Long id = this.getParamAsLong("id");
        String phone = this.getParam("phone");

        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> sameCustomer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .getExistsCustomer(id, name, phone);
        boolean repeat = false;
        String msg = "";
        try {
            if (sameCustomer != null) {
                Assert.customerRepeat(sameCustomer);
            }
        } catch (AppException e) {
            repeat = true;
            msg = e.getMessage();
        }
        if(repeat){
            map.put("repeat", "true");
        }else{
        	map.put("repeat", "false");
        }    
        map.put("msg", msg);
        return map;

    }

}
