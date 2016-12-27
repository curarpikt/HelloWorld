package com.chanapp.chanjet.customer.test.checkin;

import static org.junit.Assert.*;

import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class CheckinTestIT extends RestletBaseTest{
	static String PAYLOAD = FileReader.read("checkin/checkin.json");
    private String CUSTOMER_ID = "";
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
	
	@Test
	public void testAddCheckIn() throws Exception{

        HttpResponse response = post(Web.customer$WithContact,
                CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                        System.currentTimeMillis() + ""));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        CUSTOMER_ID = customer.get("id").toString();
		String addPayLoad = PAYLOAD.replace("100016", CUSTOMER_ID);
		response =  this.doPost("appManager","/restlet/mobile/CheckIn", addPayLoad);
		assertEquals(200, response.getStatusCode());
		//System.out.println(response.getString());
/*		data=com.chanjet.app.frame.util.JsonUtil.toObject(response.getString(), Map.class);
		CHECKIN_ID=data.get("id").toString();*/
	}
	
	@Test
	public void testGetCheckInList() throws Exception{
		//testAddCheckIn();
 		HttpResponse response =  this.doGet("appManager","/restlet/mobile/CheckIn?first=0&max=25&keyWord=&owner=",null);
 		assertEquals(200, response.getStatusCode());
/* 		response =  this.get(boPath + "/CheckIn?first=0&max=6&keyWord=testcheckin");
 		assertEquals(200, response.getStatusCode());
 		response =  this.get(boPath + "/CheckIn?first=0&max=6&customerId=100001");
 		//assertEquals(200, response.getStatusCode());
*/ 		System.out.println(response.getString());
	}
	
	@Test
	public void testNearbyCustomers() throws Exception{
		//Double latitude,Double longitude
		///NearbyCustomer?latitude=40.067496&longitude=116.236093&first=0&max=25
		//testAddCheckIn();
		HttpResponse response =  this.doGet("appManager","/restlet/mobile/checkin/NearbyCustomers?first=0&max=20&latitude=40.067462&longitude=116.236256",null);
		assertEquals(200, response.getStatusCode());
		String body = response.getString();
		System.out.println(body);
	}
}
