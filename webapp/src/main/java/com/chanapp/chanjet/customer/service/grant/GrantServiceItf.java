package com.chanapp.chanjet.customer.service.grant;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface GrantServiceItf extends BaseServiceItf {
    /**
     * 按用户查询共享过的客户
     * 
     * @param userId
     * @return
     */
    List<Long> getCustomerIdsByUserId(Long userId);

    Map<Long, Set<Long>> getGrantsByCustomerIdS(List<Long> customerIds);

    List<Map<String, Object>> getPrivilegeIds(Long customerId, Long userId);

    List<Long> getUserIdsByCustomerId(Long customerId);

    List<Long> getGrantsByCustomerIdAndUserId(Long customerId, Long userId);

    List<Long> finGrantObjectIdByUserIds(List<Long> userIds, String entityName);

	Map<Long, Set<Long>> getAllGrantsByCustomerIdS(List<Long> customerIds);
}
