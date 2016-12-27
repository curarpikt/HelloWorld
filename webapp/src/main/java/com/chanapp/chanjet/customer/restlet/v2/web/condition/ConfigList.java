package com.chanapp.chanjet.customer.restlet.v2.web.condition;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.searchcondition.SearchConditionServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 查询条件配置查询接口
 * 
 * @author tds
 *
 */
public class ConfigList extends BaseRestlet {
    @Override
    public Object run() {
        String entityType = this.getParam("entityType");
        Assert.inConditions(entityType, "app.searchCondition.entityType.invalid");
        List<Map<String, Object>> entites = ServiceLocator.getInstance().lookup(SearchConditionServiceItf.class)
                .findConfigConditions(entityType, EnterpriseContext.getCurrentUser().getUserLongId());
        return entites;
    }

}
