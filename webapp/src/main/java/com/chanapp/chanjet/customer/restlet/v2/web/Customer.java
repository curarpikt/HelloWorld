package com.chanapp.chanjet.customer.restlet.v2.web;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.metadata.CustomerFollowMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.jsonquery.JsonQuery;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.dataauth.api.UserAffiliate;

/**
 * 客户详情查询/编辑/删除
 * 
 * @author tds
 *
 */
public class Customer extends BaseRestlet {
    @Override
    public Object run() {
        // 编辑
        if (this.getMethod() == MethodEnum.PUT) {
            return _put();
        } else if (this.getMethod() == MethodEnum.DELETE) {
            return _delete();
        }

        // 查看详情
        Long customerId = this.getId();
        Assert.notNull(customerId, "客户ID不能为空");

        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .findByIdWithAuth(customerId);
        if (customer == null) {
            throw new AppException("app.customer.object.notexist");
        }
        boolean hasPri = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkUpdateDataAuth(
                CustomerMetaData.EOName, customerId, EnterpriseContext.getCurrentUser().getUserLongId());
        Map<String, Object> retMap = BoRowConvertUtil.toRow(customer);
        if(ServiceLocator.getInstance().lookup(UserServiceItf.class).isBoss(EnterpriseContext.getCurrentUser().getUserLongId())){
        	  retMap.put("editable", true);
        }else{
        	  retMap.put("editable", hasPri);
        }
        String jsonQuerySepc = JsonQuery.getInstance().setCriteriaStr(CustomerFollowMetaData.customerId + "="
                + customerId + " AND " + CustomerFollowMetaData.userId + "=" + customer.getOwner()).toString();
        IBusinessObjectRowSet rowset = ServiceLocator.getInstance().lookup(BO.CustomerFollow).query(jsonQuerySepc);
        if (rowset.size() > 0) {
            retMap.put("follow", true);
        } else {
            retMap.put("follow", false);
        }

        boolean delPri = false;
        boolean transferPri = false;
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        UserValue currUser = ServiceLocator.getInstance().lookup(UserServiceItf.class).getUserValueByUserId(userId);
        if (userId.equals(customer.getOwner()) || SRU.LEVEL_BOSS.equals(currUser.getUserLevel())) {
            delPri = true;
            transferPri = true;
        } else {
            UserAffiliate userImp = AppWorkManager.getDataAuthManager().getUserAffiliate();
            List<Map<String, Object>> bossList = userImp.getAllBoss(session(), customer.getOwner(), null);
            if (bossList != null && bossList.size() > 0) {
                for (Map<String, Object> bossMap : bossList) {
                    if (userId.equals(bossMap.get(SC.id))) {// 代表登录用户要么是该客户的owner要么是owner的上级
                        delPri = true;
                        transferPri = true;
                        break;
                    }
                }
            }
        }
        retMap.put("delAble", delPri);
        retMap.put("transferAble", transferPri);
        return retMap;
    }

    private Object _put() {
        String payload = this.getPayload();
        Assert.notNull(payload);

        LinkedHashMap<String, Object> customerParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);
        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .updateCustomer(customerParam);

        return BoRowConvertUtil.toRow(customer);
    }

    private Object _delete() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        ServiceLocator.getInstance().lookup(CustomerServiceItf.class).deleteCustomer(customerId);

        Map<String, Object> rs = new HashMap<String, Object>();
        rs.put("success", true);
        return rs;
    }

}
