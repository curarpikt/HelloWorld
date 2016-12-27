package com.chanapp.chanjet.customer.service.recycle;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleHome;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleRow;
import com.chanapp.chanjet.customer.businessobject.api.recycle.IRecycleRowSet;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface RecycleServiceItf extends BoBaseServiceItf<IRecycleHome, IRecycleRow, IRecycleRowSet> {
    void addRecoveryByBatchDel(String entityName, List<Long> customerIds, String reason);

    void addRecovery(String entityName, Long id);

    IRecycleRow saveRecycleRow(Long operUser, Timestamp operTime, String reason, String entityName,
            List<IRecycleRow> recycleList);

    void saveRecycleRelation(Long entityId, String entityName, IRecycleRow recycleRow, String content,
            Map<String, List<Long>> recyledIdList);

    int delRecycles(String ids);

    int cleanRecycle();

    List<UserValue> getOperUsers(String entityName);

    int restore(Long recycleId, String entityIds);

    RowSet getRecycles(String entityName, String operUserIds, Long startTime, Long endTime, Integer pageno,
            Integer pagesize);

    Map<String, Object> hisRecycleInfo();

    Map<String, Object> recHisReocrds(String tag);
}
