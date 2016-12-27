package com.chanapp.chanjet.customer.restlet.v2.web.condition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionRow;
import com.chanapp.chanjet.customer.service.searchcondition.SearchConditionServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * 查询条件收藏查询接口
 * 
 * @author tds
 *
 */
public class FavoriteList extends BaseRestlet {
    @Override
    public Object run() {
        String entityType = this.getParam("entityType");
        Assert.inConditions(entityType, "app.searchCondition.entityType.invalid");
        List<ISearchConditionRow> entites = ServiceLocator.getInstance().lookup(SearchConditionServiceItf.class)
                .findFavoriteConditions(entityType, EnterpriseContext.getCurrentUser().getUserLongId());
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        Map<Long, Object> isRepeat = new HashMap<Long, Object>();
        for (ISearchConditionRow searchCondition : entites) {
            Long versionId = searchCondition.getVersionId();
            Map<String, Object> tempMap = new HashMap<String, Object>();
            tempMap.put("fieldName", searchCondition.getFieldName());
            tempMap.put("defaultValue", searchCondition.getDefaultValue());
            tempMap.put("entityType", searchCondition.getEntityType());
            if (!isRepeat.containsKey(versionId)) {
                Map<String, Object> map = new HashMap<String, Object>();
                isRepeat.put(versionId, map);
                List<Map<String, Object>> temp = new ArrayList<Map<String, Object>>();
                temp.add(tempMap);
                map.put("versionId", versionId);
                map.put("list", temp);
                result.add(map);
            } else {
                for (Map<String, Object> map : result) {
                    if (versionId.equals(map.get("versionId"))) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> list = (List<Map<String, Object>>) map.get("list");
                        list.add(tempMap);
                        break;
                    }
                }
            }
        }
        return result;
    }

}
