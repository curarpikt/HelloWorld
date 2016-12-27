package com.chanapp.chanjet.customer.restlet.mobile;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

public class Contact extends BaseRestlet {

	@Override
	public Object run() {

			if (this.getMethod().equals(MethodEnum.GET)) {
				if(this.getId()!=null){				
					return _get();	   
				}
			}else if(this.getMethod().equals(MethodEnum.POST)){// 新增联系人
				String payload =this.getPayload();
				return addContact(payload);
			}else if(this.getMethod().equals(MethodEnum.PUT)){// 更新联系人
				//String payload =this.getPayload();
				return _put();
			} else if(this.getMethod().equals(MethodEnum.DELETE)){// 删除联系人
				return deleteContact(this.getId());
			}

		return null;

	}
	
	private Map<String, Object> addContact(String payload){
        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);
        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).addContact(contactParam);
	}
	
	private String deleteContact(Long contactId){
        Assert.notNull(contactId);
        ServiceLocator.getInstance().lookup(ContactServiceItf.class).deleteContact(contactId);
        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return dataManager.toJSONString(rs);
	}
	
   private Object _put() {
        String payload = this.getPayload();
        Assert.notNull(payload);
        LinkedHashMap<String, Object> contactParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(ContactServiceItf.class).updateContact(contactParam);
    }
   
   private Object _get() {
       Long contactId = this.getId();
       Assert.notNull(contactId);
       Row contact = ServiceLocator.getInstance().lookup(ContactServiceItf.class).getContact(contactId);
       boolean hasPri =ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkDeleteDataAuth(EO.Contact, contact.getLong(SC.id), EnterpriseContext.getCurrentUser().getUserLongId());
	    //boolean hasPri=PrivilegeService.checkDataAuth(com.chanapp.chanjet.customer.frame.constant.eo.metadata.ContactMetaData.EOName,id, EnterpriseContext.getCurrentUser().getUserLongId(),DataAuthPrivilege.DELETE);	    	  
	    if(hasPri){
	    	contact.put("privilege", Integer.valueOf("111", 2));
	    }else{
	    	contact.put("privilege", Integer.valueOf("011", 2));
	    }
	    return contact;
   }

}
