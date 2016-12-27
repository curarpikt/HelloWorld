package com.chanapp.chanjet.customer.service.searchcondition;

import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionHome;
import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionRow;
import com.chanapp.chanjet.customer.businessobject.api.searchcondition.ISearchConditionRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface SearchConditionServiceItf
        extends BoBaseServiceItf<ISearchConditionHome, ISearchConditionRow, ISearchConditionRowSet> {
    void deleteFavoriteCondition(Long userId, Long versionId);

    List<Map<String, Object>> findConfigConditions(String entityType, Long userId);

    void batchAddConfigCondition(Map<String, Object> map, Long userId);

    void deleteFavoriteCondition(Long userId, String entityType);

    void deleteConfigCondition(Long userId, String entityType);

    void batchAddFavoriteCondition(Map<String, Object> map, Long userId);

    List<ISearchConditionRow> findFavoriteConditions(String entityType, Long userId);
}
