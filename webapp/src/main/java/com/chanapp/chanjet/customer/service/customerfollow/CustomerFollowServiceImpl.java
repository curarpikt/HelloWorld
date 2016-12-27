package com.chanapp.chanjet.customer.service.customerfollow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowHome;
import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowRow;
import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowRowSet;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.metadata.CustomerFollowMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class CustomerFollowServiceImpl
        extends BoBaseServiceImpl<ICustomerFollowHome, ICustomerFollowRow, ICustomerFollowRowSet>
        implements CustomerFollowServiceItf {

    @Override
    public List<Long> getCurrUserFollowCustomerIds() {
        Long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
        List<Long> ids = new ArrayList<Long>();
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        Criteria criteria = Criteria.AND();
        criteria.eq(CustomerFollowMetaData.userId, currUserId);
        jsonQueryBuilder.addCriteria(criteria);
        ICustomerFollowRowSet rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        if (rowSet != null && rowSet.getCustomerFollowRows() != null) {
            for (ICustomerFollowRow row : rowSet.getCustomerFollowRows()) {
                ids.add(row.getCustomerId());
            }
        }
        return ids;
    }

    @Override
    public List<Long> isFollow(Long userId, List<Long> ids) {
        String cqlQueryString = "select customerId from " + getBusinessObjectHome().getDefinition().getId()
                + " where userId = :userId and customerId in :ids";
        List<Long> hasFollow = new ArrayList<Long>();
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("userId", userId);
        paraMap.put("ids", ids.toArray());
        List<Map<String, Object>> list = runCQLQuery(cqlQueryString, paraMap);

        if (list != null) {
            for (Map<String, Object> values : list) {
                Long id = (Long) values.get("customerId");
                hasFollow.add(id);
            }
        }
        return hasFollow;
    }

    @Override
    public void follow(Long customerId, Boolean isFollow) {
        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .getCustomerById(customerId);
        if (customer == null) {
            throw new AppException("app.customer.object.notexist");
        }
        if (isFollow != null && isFollow) {
            followCustomer(customerId, EnterpriseContext.getCurrentUser().getUserLongId());
        } else {
            unFollowCustomer(customerId, EnterpriseContext.getCurrentUser().getUserLongId());
        }
    }

    /**
     * 增加关注
     * 
     * @param customerId
     * @param userid
     */
    private void followCustomer(Long customerId, Long userid) {
        JsonQuery jq = JsonQuery.getInstance().setCriteriaStr(CustomerFollowMetaData.customerId + "=" + customerId
                + " AND " + CustomerFollowMetaData.userId + "=" + userid);
        ICustomerFollowRowSet rowset = query(jq.toString());
        if (rowset.size() > 0) {
            return;
        } else {
            ICustomerFollowRow row = createRow();
            row.setCustomerId(customerId);
            row.setUserId(userid);
            upsert(row);
        }
    }

    /**
     * 取消关注
     * 
     * @param customerId
     * @param userid
     */
    private void unFollowCustomer(Long customerId, Long userid) {
        JsonQuery jq = JsonQuery.getInstance().setCriteriaStr(CustomerFollowMetaData.customerId + "=" + customerId
                + " AND " + CustomerFollowMetaData.userId + "=" + userid);
        batchDelete(jq.toString());
    }

    @Override
    public List<Long> findFollowByCustomerIds(List<Long> ids) {
        List<Long> followIds = new ArrayList<Long>();
        if (ids != null && ids.size() > 0) {
            Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
            followIds = isFollow(userId, ids);
            return followIds;
        }
        return followIds;
    }

    @Override
    public List<Long> findFollowCustomers(Long customerid, Integer pageNo, Integer pageSize) {
        String cqlQueryString = "select c.lastRecord.id as id from " + getBusinessObjectId(BO.Customer) + " c "
                + "where (c.isDeleted is null or c.isDeleted =?) and c.lastRecord != null "
                + "and c.id in(select customerId from " + getBusinessObjectId(BO.CustomerFollow) + " where userId =?) "
                + "order by c.lastRecord.createdDate desc";
        List<Object> paraMap = new ArrayList<Object>();
        paraMap.add(false);
        paraMap.add(customerid);
        List<Long> result = new ArrayList<Long>();
        List<Map<String, Object>> list = runCQLQuery(cqlQueryString, paraMap, (pageNo - 1) * (pageSize - 1), pageSize);
        for (Map<String, Object> obj : list) {
            result.add((Long) obj.get("id"));
        }
        return result;
    }
}
