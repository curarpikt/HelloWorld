package com.chanapp.chanjet.customer.vo;

import java.util.List;

import com.chanapp.chanjet.customer.constant.SRU;
import com.chanjet.csp.appmanager.AppWorkManager;

public class UserQuery {
    private List<Long> userIds;
    private String status;
    private String name;

    private String orderPart;
    private Integer first;
    private Integer max;
    private boolean count;

    private String cql = "select user.name,user.headPicture,appUser.isActive,user.mobile,user.email,user.userId  "
            + "from com.chanjet.system.systemapp.businessobject.CSPUser user"
            // + " ,com.chanjet.system.systemapp.businessobject.CSPUserRole
            // userRole "
            + " ,com.chanjet.system.systemapp.businessobject.CSPAppUser appUser "
            + " where user.userId = appUser.user.userId ";

    private String countCql = "select count(user.userId) as total "
            + "from com.chanjet.system.systemapp.businessobject.CSPUser user"
            // + " ,com.chanjet.system.systemapp.businessobject.CSPUserRole
            // userRole "
            + " ,com.chanjet.system.systemapp.businessobject.CSPAppUser appUser "
            + " where  user.userId = appUser.user.userId ";

    public String getCountCql() {
        return _getCql(this.countCql);
    }

    public String getCql() {
        return _getCql(this.cql);
    }

    private String _getCql(String cql) {
        String appId = AppWorkManager.getCurrentAppId();
        StringBuffer tempCql = new StringBuffer(cql);

        tempCql.append(" and appUser.appId ='" + appId + "' ");
        if (this.userIds != null) {
            String _ids = "0";
            List<Long> ids = this.userIds;
            for (Long id : ids) {
                if (!_ids.isEmpty()) {
                    _ids += ",";
                }
                _ids += id;
            }
            tempCql.append(" and user.userId in(" + _ids + ") ");
        }
        if (this.name != null) {
            tempCql.append(" and user.name ='" + this.name + "'");
        }
        if (this.status != null) {
            if (SRU.STATUS_ENABLE.equals(this.status)) {
                tempCql.append(" and appUser.isActive ='T'  and  user.isActive ='T' ");
            } else if (SRU.STATUS_DISABLE.equals(this.status)) {
                tempCql.append(" and (appUser.isActive ='F' or user.isActive ='F')");
            }
        }
        if (this.orderPart != null) {
            tempCql.append(" order by " + this.orderPart);
        }
        return tempCql.toString();
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public String getOrderPart() {
        return orderPart;
    }

    public void setOrderPart(String orderPart) {
        this.orderPart = orderPart;
    }

    public Integer getFirst() {
        return first;
    }

    public void setFirst(Integer first) {
        this.first = first;
    }

    public Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public List<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
