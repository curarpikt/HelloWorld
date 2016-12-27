package com.chanapp.chanjet.customer.restlet.v2.web.importNew;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.importrecordnew.ImportRecordNewServiceItf;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.scheduler.api.util.TriggerParameter;

/**
 * 开启导入解析excel任务
 * 
 * @author tds
 *
 */
public class Task extends BaseRestlet {
    @Override
    public Object run() {

/*    	Map<String, Object> jobTriggerMap = new HashMap<String, Object>();
    	jobTriggerMap.put(TriggerParameter.FIELD_NAME_CRON_EXPRESSION, "0 0/1 * * * ?");
    	jobTriggerMap.put(TriggerParameter.FIELD_NAME_TRIGGER_NAME, "imporDataTrigger");
    	jobTriggerMap.put(TriggerParameter.FIELD_NAME_TRIGGER_GROUP, "importGroup");
      	jobTriggerMap.put("appId",AppWorkManager.getCurrentAppId());
    	Map<String, Object> jobDefinition = new HashMap<String, Object>();
    	jobDefinition.put("id", 10001l);
    	jobTriggerMap.put("jobDefinition", jobDefinition);
    	Map<String, Object> triggerReslut = AppWorkManager.getSchedulerServiceManager().addJobTrigger(AppWorkManager.getCurrentAppId(), AppWorkManager.getCurrAppUserId(), jobTriggerMap);
    	//AppWorkManager.getSchedulerServiceManager().getJobTrigger(arg0, arg1, arg2)
    	 */

    	Map<String, Object> result = new HashMap<String, Object>();
        result.put("result", true);
        return result;    
    	//return ServiceLocator.getInstance().lookup(ImportRecordNewServiceItf.class).task(this.getParamAsLong("id"));
    }

}
