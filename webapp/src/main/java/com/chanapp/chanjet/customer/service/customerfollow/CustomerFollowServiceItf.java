package com.chanapp.chanjet.customer.service.customerfollow;

import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowHome;
import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowRow;
import com.chanapp.chanjet.customer.businessobject.api.customerfollow.ICustomerFollowRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface CustomerFollowServiceItf
        extends BoBaseServiceItf<ICustomerFollowHome, ICustomerFollowRow, ICustomerFollowRowSet> {
    List<Long> getCurrUserFollowCustomerIds();

    List<Long> isFollow(Long userId, List<Long> ids);

    void follow(Long customerId, Boolean isFollow);

    List<Long> findFollowByCustomerIds(List<Long> ids);

    List<Long> findFollowCustomers(Long customerid, Integer pageNo, Integer pageSize);
}
