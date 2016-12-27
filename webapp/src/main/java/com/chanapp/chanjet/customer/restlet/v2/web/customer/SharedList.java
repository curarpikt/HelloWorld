package com.chanapp.chanjet.customer.restlet.v2.web.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 根据客户ID查询已共享过的用户
 * 
 * @author tds
 *
 */
public class SharedList extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(SharedList.class);

    @SuppressWarnings("unchecked")
    @Override
    public Object run() {
        Long customerId = this.getId();
        Assert.notNull(customerId);

        List<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();
        List<UserValue> users = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .getSharedUsers(customerId);
        if (users != null) {
            for (UserValue user : users) {
                try {
                    Map<String, Object> userMap = BeanUtils.describe(user);
                    String headpic = (String) userMap.get("headPicture");
                    userMap.remove("headPicture");
                    userMap.put("headPic", headpic);
                    retList.add(userMap);
                } catch (Exception e) {
                    logger.error("BeanUtils.describe error:{},{}", user, e.getMessage());
                }
            }
        }
        return retList;
    }

}
