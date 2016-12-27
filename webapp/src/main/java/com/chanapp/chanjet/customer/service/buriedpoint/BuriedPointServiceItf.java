package com.chanapp.chanjet.customer.service.buriedpoint;

import com.chanapp.chanjet.web.service.BaseServiceItf;

public interface BuriedPointServiceItf extends BaseServiceItf {

	void firstLoginPoint()  ;

	void everyLoginPoint() ;

	String getClientIpAddress();

}
