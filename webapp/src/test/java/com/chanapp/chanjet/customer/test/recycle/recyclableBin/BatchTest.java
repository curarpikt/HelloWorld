package com.chanapp.chanjet.customer.test.recycle.recyclableBin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.cmr.api.metadata.customization.json.CustomBO;
import com.chanjet.csp.platform.test.HttpResponse;

public class BatchTest extends RecyclableBinTest {
	private List<Long> customerIds = new ArrayList<>();

	@Before
	public void setUp() throws Exception {
		createTestData();
	}

	private void createTestData() throws Exception {
		// add customer1
		HttpResponse response = post(Web.customer$WithContact,
				CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "")
								.replaceAll("13166666666", System.currentTimeMillis() + ""));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
		Map<String, Object> result = this.parseObject(response.getString());
		Map<String, Object> customer = (Map<String, Object>) result.get("customer");		
		customerIds.add(Long.valueOf(customer.get("id").toString()));
		
		// add customer2
		response = post(Web.customer$WithContact,
				CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "")
								.replaceAll("13166666666", System.currentTimeMillis() + ""));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
		result = this.parseObject(response.getString());
		customer = (Map<String, Object>) result.get("customer");		
		customerIds.add(Long.valueOf(customer.get("id").toString()));		
	}

	@Test
	public void shouldAllowBatchFrom11To01() throws Exception {
		// When: batch drop customers to recyclable bin
		Map<String, Object> recyclableCustomerIds = batchDropToRecyclableBin(CUSTOMER_BO, customerIds);		
		Long recyclableCustomerId = Long.valueOf(recyclableCustomerIds.get("recyclableId").toString());
		assertNotNull(recyclableCustomerId);
		JSONArray recyclableRelationIds = (JSONArray) recyclableCustomerIds.get("relationIds");	
		assertNotNull(recyclableRelationIds);
		assertEquals(2, recyclableRelationIds.size());
		
		// Then: check whether the customer has been deleted
		boolean customer1Existed = checkExisted(CUSTOMER_BO, customerIds.get(0));
		assertEquals(false, customer1Existed);
		boolean customer2Existed = checkExisted(CUSTOMER_BO, customerIds.get(1));
		assertEquals(false, customer2Existed);
	}
	
	@Test
	public void shouldAllowBatchFrom01To11() throws Exception {
		// Given: batch drop customers to recyclable bin
		Map<String, Object> recyclableCustomerIds = batchDropToRecyclableBin(CUSTOMER_BO, customerIds);		
		Long recyclableCustomerId = Long.valueOf(recyclableCustomerIds.get("recyclableId").toString());
		assertNotNull(recyclableCustomerId);
		JSONArray recyclableRelationIds = (JSONArray) recyclableCustomerIds.get("relationIds");	
		assertNotNull(recyclableRelationIds);
		assertEquals(2, recyclableRelationIds.size());
		
		// When: batch recycle the customers
		JSONArray recycledCustomerIds = batchRecycle(recyclableCustomerId);		
		
		// Then: check the customer have been recycled	
		Long recycledCustomer1Id = Long.valueOf(recycledCustomerIds.getInteger(0).toString());
		boolean customer1Existed = checkExisted(CUSTOMER_BO, recycledCustomer1Id);
		assertEquals(true, customer1Existed);
		Long recycledCustomer2Id = Long.valueOf(recycledCustomerIds.getInteger(0).toString());
		boolean customer2Existed = checkExisted(CUSTOMER_BO, recycledCustomer2Id);
		assertEquals(true, customer2Existed);		
	}

}
