package com.chanapp.chanjet.customer.restlet.mobile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;

public class Customer extends BaseRestlet {

	@Override
	public Object run() {
		if (this.getMethod().equals(MethodEnum.GET)) {
			if(this.getId()!=null){
				return customerDeatail();
			}else{
				Assert.notNull(this.getParamAsInt("first"), "app.common.para.format.error");
				Assert.notNull(this.getParamAsInt("max"), "app.common.para.format.error");
				int pageNo = this.getParamAsInt("first");
				int pageSize = this.getParamAsInt("max");
				String keyword = this.getParam("keyWord");
				return _queryCustomerList(pageNo, pageSize, keyword);
			}
		}else if(this.getMethod().equals(MethodEnum.POST)){
			String payload =this.getPayload();
			return addCustomerAndContacts(payload);
		}else if(this.getMethod().equals(MethodEnum.PUT)){
			return _put();
		} else if(this.getMethod().equals(MethodEnum.DELETE)){
			return _delete();
		}				
		return null;

	
	}
	private Object customerDeatail(){
		return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).getCustomerDetail(this.getId());
	}
	
	private Object _queryCustomerList(Integer pageNo,Integer pageSize,String keyword){
		return ServiceLocator.getInstance().lookup(CustomerServiceItf.class).queryCustomerListByKeyword(pageNo,pageSize,keyword);
	}
	
    private Object _delete() {
        Long customerId = this.getId();
        Assert.notNull(customerId);
        ServiceLocator.getInstance().lookup(CustomerServiceItf.class).deleteCustomer(customerId);
        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }
    
    private Object _put() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);
        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .updateCustomer(customerParam);

        return BoRowConvertUtil.toRow(customer);
    }
    
    
	public String addCustomerAndContacts(String payload) {
        // 返回值
        Map<String, Object> result = new HashMap<String, Object>();     
        Map<String, Object> boRowMap = dataManager.jsonStringToMap(payload);
        LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) boRowMap.get(BO.Customer);
        // 客户名称重复性检查
        CustomerServiceItf customerService = ServiceLocator.getInstance().lookup(CustomerServiceItf.class);
        Map<String, Object> sameCustomer = customerService.getExistsCustomer(null, (String) customerParam.get("name"),
                (String) customerParam.get("phone"));
        Assert.customerRepeat(sameCustomer);
        // 新增客户
        ICustomerRow customerRow = customerService.addCustomer(customerParam);
        // 返回结果设置
        Row customerSelfRow = BoRowConvertUtil.toRow(customerRow);
        customerSelfRow.put("privilege", Integer.valueOf("111", 2));
        result.put(EO.Customer, customerSelfRow);
        // 新增联系人
        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) boRowMap.get(BO.Contact);
        if (contactParam != null) {
            ContactServiceItf contactService = ServiceLocator.getInstance().lookup(ContactServiceItf.class);
            IContactRow contactRow = contactService.addContactForCustomer(contactParam, customerRow.getId());
            result.put(EO.Contact, BoRowConvertUtil.toRow(contactRow));
        }
        // 返回          
   
        return JSON.toJSONString(result);
	}
}
