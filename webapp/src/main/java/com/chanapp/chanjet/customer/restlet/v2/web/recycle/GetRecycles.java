package com.chanapp.chanjet.customer.restlet.v2.web.recycle;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import com.chanapp.chanjet.customer.service.recycle.RecyclableBinManager;
import com.chanapp.chanjet.customer.service.recycle.BatchRecyclableObject;
import com.chanapp.chanjet.customer.service.recycle.RecyclableObjectFormatter;
import com.chanapp.chanjet.customer.service.recycle.customer.CustomizeRecyclableObjectFormatter;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.AppextResult;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.util.ConvertUtil;

/**
 * 获得列表
 * 
 * @author tds
 *
 */
public class GetRecycles extends BaseRestlet {

    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);
        LinkedHashMap<String, Object> param = (LinkedHashMap<String, Object>) dataManager.jsonStringToMap(payload);
        String entityName = param.get("entityName")==null?null:param.get("entityName").toString();
        
        String batchOperationUserIds = param.get("operUserIds")==null?null:param.get("operUserIds").toString();
        List<Long> operationUserIds = new ArrayList<Long>();
        if (batchOperationUserIds != null && !batchOperationUserIds.equals("")) {                      
            for (String userId : batchOperationUserIds.split(",")) {
                operationUserIds.add(Long.valueOf(userId));
            }       
        }
        
        Long startTime =param.get("startTime")==null?null: ConvertUtil.toLong(param.get("startTime").toString());
        Long endTime =param.get("endTime")==null?null: ConvertUtil.toLong(param.get("endTime").toString());    
        Integer pageno = ConvertUtil.toInt(param.get("pageno").toString());
        Integer pagesize = ConvertUtil.toInt(param.get("pagesize").toString());
        
        boolean isCustomerBo = ("Customer".equals(entityName)) ? true : false;
        List<BatchRecyclableObject> recyclables;
        if (isCustomerBo) {
        	//List<String> sourceBos = Lists.newArrayList("WorkRecord", "Contact");
        	List<String> sourceBos = new ArrayList<>();
        	sourceBos.add("WorkRecord");
        	sourceBos.add("Contact");
        	recyclables = RecyclableBinManager.getWithReferences(entityName,
        			sourceBos, null, operationUserIds, startTime, endTime, pageno, pagesize);
        } else {
        	recyclables = RecyclableBinManager.get(entityName, operationUserIds, startTime, endTime, pageno, pagesize);
        }      
        Integer count = RecyclableBinManager.getCount(entityName, operationUserIds, startTime, endTime);
        RecyclableObjectFormatter formatter = CustomizeRecyclableObjectFormatter.getInstance();
        return new AppextResult(formatter.format(recyclables,count));
    }

}