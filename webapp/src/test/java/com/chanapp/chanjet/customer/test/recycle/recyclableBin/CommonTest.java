package com.chanapp.chanjet.customer.test.recycle.recyclableBin;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
import com.google.common.base.Joiner;

public class CommonTest extends RecyclableBinTest {

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
	public void shouldAllowDeleteByRecyclableIdsTest() throws Exception {
		// Given: batch drop customers to recyclable bin
		Map<String, Object> recyclableCustomerIds = batchDropToRecyclableBin(CUSTOMER_BO, customerIds);		
		Long recyclableCustomerId = Long.valueOf(recyclableCustomerIds.get("recyclableId").toString());
		assertNotNull(recyclableCustomerId);
		JSONArray recyclableRelationIds = (JSONArray) recyclableCustomerIds.get("relationIds");	
		assertNotNull(recyclableRelationIds);
		assertEquals(2, recyclableRelationIds.size());
		
		// When: delete the customer from recyclable bin
		List<Long> toDeleteRecyclableIds = new ArrayList<Long>();
		toDeleteRecyclableIds.add(recyclableCustomerId);
		deleteFromRecyclableBin(toDeleteRecyclableIds);
		
		// Then: 
		// 1. check customer has been deleted from recyclable bin
        boolean existingRecyclabeCustomer = checkExistedInRecyclableBin(recyclableCustomerId);
        assertEquals(false, existingRecyclabeCustomer);
        
        // 2. check relation has been deleted from recyclable bin
        Long relation1Id = Long.valueOf(recyclableRelationIds.getInteger(0).toString());
        boolean existingRelation1 = checkeRelationExistedInRecyclableBin(relation1Id);
        assertEquals(false, existingRelation1);
        Long relation2Id = Long.valueOf(recyclableRelationIds.getInteger(0).toString());
        boolean existingRelation2 = checkeRelationExistedInRecyclableBin(relation2Id);
        assertEquals(false, existingRelation2);
	}	

}
