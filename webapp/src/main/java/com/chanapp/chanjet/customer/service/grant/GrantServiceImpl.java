package com.chanapp.chanjet.customer.service.grant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.util.QueryLimitUtil;

public class GrantServiceImpl extends BaseServiceImpl implements GrantServiceItf {
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> runCQLQuery(String cqlQueryString, HashMap<String, Object> paraMap) {
        return QueryLimitUtil.runCQLQuery(getBusinessObjectHome("CSPGrant"), session(), cqlQueryString, paraMap);
    }

    @Override
    public List<Long> getCustomerIdsByUserId(Long userId) {
        List<Long> userList = new ArrayList<Long>();
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.objId as objId from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.userId = :userId  and c.entityName=:customerName group by c.objId";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("userId", userId);
        paraMap.put("customerName", customerName);
        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> userobj : result) {
            userList.add((Long) userobj.get("objId"));
        }
        return userList;
    }

    @Override
    public Map<Long, Set<Long>> getGrantsByCustomerIdS(List<Long> customerIds) {
    	
        Map<Long, Set<Long>> grantsMap = new HashMap<Long, Set<Long>>();
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.objId as objId,c.userId as userId from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.objId in:customerId  and c.entityName=:customerName order by createdDate desc ";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("customerId", customerIds);
        paraMap.put("customerName", customerName);

        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> userobj : result) {
            Long objId = (Long) userobj.get("objId");
            Long userId = (Long) userobj.get("userId");
            if (grantsMap.containsKey(objId)) {
                grantsMap.get(objId).add(userId);
            } else {
                Set<Long> grants = new TreeSet<Long>();
                grants.add(userId);
                grantsMap.put(objId, grants);
            }
        }
        return grantsMap;
    }
    
    @Override
    public Map<Long, Set<Long>> getAllGrantsByCustomerIdS(List<Long> customerIds) { 	
        Map<Long, Set<Long>> grantsMap = new HashMap<Long, Set<Long>>();
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.objId as objId,c.userId as userId from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.entityName=:customerName order by createdDate desc ";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("customerName", customerName);
        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> userobj : result) {
            Long objId = (Long) userobj.get("objId");
            Long userId = (Long) userobj.get("userId");
            if(customerIds.contains(objId)){
                if (grantsMap.containsKey(objId)) {
                    grantsMap.get(objId).add(userId);
                } else {
                    Set<Long> grants = new TreeSet<Long>();
                    grants.add(userId);
                    grantsMap.put(objId, grants);
                }
            }
        }
        return grantsMap;
    }

    @Override
    public List<Map<String, Object>> getPrivilegeIds(Long customerId, Long userId) {
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.id as id, c.privilege as privilege from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.objId =:customerId  and c.entityName=:customerName and userId=:userId";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("customerId", customerId);
        paraMap.put("customerName", customerName);
        paraMap.put("userId", userId);
        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        return result;
    }

    @Override
    public List<Long> getUserIdsByCustomerId(Long customerId) {
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.userId as userId from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.objId = :customerId  and c.entityName=:customerName group by c.userId ";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("customerId", customerId);
        paraMap.put("customerName", customerName);
        List<Long> userobjs = new ArrayList<Long>();
        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> userobj : result) {
            userobjs.add((Long) userobj.get("userId"));
        }
        return userobjs;
    }

    @Override
    public List<Long> getGrantsByCustomerIdAndUserId(Long customerId, Long userId) {
        String customerName = CustomerMetaData.EOName;
        String cqlQueryString = "select c.id as id from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.objId =:customerId  and c.entityName=:customerName and userId=:userId";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("customerId", customerId);
        paraMap.put("customerName", customerName);
        paraMap.put("userId", userId);
        List<Long> userobjs = new ArrayList<Long>();
        List<Map<String, Object>> result = runCQLQuery(cqlQueryString, paraMap);
        for (Map<String, Object> userobj : result) {
            userobjs.add((Long) userobj.get("id"));
        }
        return userobjs;
    }

    @Override
    public List<Long> finGrantObjectIdByUserIds(List<Long> userIds, String entityName) {
        List<Long> objIds = new ArrayList<Long>();
        String hql = "select c.objId  as id from com.chanjet.system.systemapp.businessobject.CSPGrant c where c.userId in :userIds  and c.entityName=:entityName group by c.objId";
        HashMap<String, Object> paraMap = new HashMap<String, Object>();
        paraMap.put("userIds", userIds);
        paraMap.put("entityName", entityName);
        List<Map<String, Object>> result = runCQLQuery(hql, paraMap);
        for (Map<String, Object> affiliate : result) {
            Long objId = (Long) affiliate.get("id");
            objIds.add(objId);
        }
        return objIds;
    }

}
