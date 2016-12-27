package com.chanapp.chanjet.customer.restlet.v2.web.sysreluser;

import com.chanapp.chanjet.customer.service.user.UserServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanapp.chanjet.customer.vo.VORowSet;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class Allusers extends BaseRestlet {
    @Override
    public Object run() {
        // 验证是否APPUSER
        // Assert.authBoss(EnterpriseContext.getCurrentUser().getUserLongId());

        String status = this.getParam("status");
        Integer pageNo = this.getParamAsInt("pageno");
        Integer pageSize = this.getParamAsInt("pagesize");
        Assert.notNull(status);
        Assert.notNull(pageNo);
        Assert.notNull(pageSize);
        UserServiceItf userService = ServiceLocator.getInstance().lookup(UserServiceItf.class);
        VORowSet<Row> retUser= new VORowSet<Row>();
        VORowSet<UserValue> userSet =userService.queryUserWithCustomerCount(pageNo, pageSize, status);
		if(userSet.getItems()!=null&&userSet.getItems().size()>0){
			for(UserValue user:userSet.getItems()){
				Row userRow = BoRowConvertUtil.userValue2Row(user);
				userRow.put("phone", user.getMobile());
				userRow.put("role", user.getUserRole());
				userRow.put("customercount", user.getCustomercount());
				retUser.add(userRow);
			}
			retUser.setTotal(userSet.getTotal());
		}        
        return retUser;
    }
}
