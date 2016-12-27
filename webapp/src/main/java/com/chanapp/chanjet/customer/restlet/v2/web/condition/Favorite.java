package com.chanapp.chanjet.customer.restlet.v2.web.condition;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.searchcondition.SearchConditionServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 查询条件收藏
 * 
 * @author tds
 *
 */
public class Favorite extends BaseRestlet {
    @Override
    public Object run() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        ServiceLocator.getInstance().lookup(SearchConditionServiceItf.class).batchAddFavoriteCondition(
                dataManager.jsonStringToMap(payload), EnterpriseContext.getCurrentUser().getUserLongId());

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

}
