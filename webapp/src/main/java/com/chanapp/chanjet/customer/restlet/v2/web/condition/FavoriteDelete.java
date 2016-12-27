package com.chanapp.chanjet.customer.restlet.v2.web.condition;

import java.util.HashMap;
import java.util.Map;

import com.chanapp.chanjet.customer.service.searchcondition.SearchConditionServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 根据版本删除收藏条件
 * 
 * @author tds
 *
 */
public class FavoriteDelete extends BaseRestlet {
    @Override
    public Object run() {
        Long versionId = this.getParamAsLong("versionId");

        Assert.notNull(versionId, "app.searchCondition.versionId.required");
        ServiceLocator.getInstance().lookup(SearchConditionServiceItf.class)
                .deleteFavoriteCondition(EnterpriseContext.getCurrentUser().getUserLongId(), versionId);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

}
