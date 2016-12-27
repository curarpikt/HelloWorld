package com.chanapp.chanjet.customer.test.recycle.recyclableBin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class StrongReferenceTest extends RecyclableBinTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();	
	
	private Long customerId;
	private Long contactId;

	@Before
	public void setUp() throws Exception {
		createTestData();
	}

	@Test
	public void shouldTransferFrom11To01() throws Exception {
		// When: drop the customer to recyclable bin
		Map<String, Long> ids = dropToRecyclableBin(CUSTOMER_BO, customerId);
		
		// Then: 
		// 1. check the customer has been dropped to recyclable bin
		Long recyclableId = ids.get("recyclableId");
		assertNotNull(recyclableId);
		// 2. check the relation has been saved in recyclable bin
		Long recyclableRelationId = ids.get("relationId");
		assertNotNull(recyclableRelationId);

		// 3. check the customer has been deleted 
		boolean customerExisted = checkExisted(CUSTOMER_BO, customerId);
		assertEquals(false, customerExisted);
        
        // 4. check the contact has no relation with the customer
        boolean customerOfContactExisted = checkCustomerOfContactExisted(contactId);
        assertEquals(false, customerOfContactExisted);
	}

	@Test
	public void shouldTransferFrom01To00() throws Exception {
		// Given: drop the customer to recyclable bin
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		Long recyclableRelationId = recyclableCustomerIds.get("relationId");
		assertNotNull(recyclableRelationId);
			
		// When: drop the contact to recyclable bin
		Map<String, Long> recyclableContactIds = dropToRecyclableBin(CONTACT_BO, contactId);
		
		// Then:		
		// 1. check the contact has been dropped to recyclable bin
		Long recyclableId = recyclableContactIds.get("recyclableId");
		assertNotNull(recyclableId);
		// 2. check the relation doesn't need to been saved
		assertNull(recyclableContactIds.get("relationId"));		
		
		// 3. check the contact has been deleted
		boolean contactExisted = checkExisted(CONTACT_BO, contactId);
		assertEquals(false, contactExisted);     
	}
	
	@Test
	public void shouldTransferFrom00To10() throws Exception {
		// Given: 
		// 1. drop the customer to recyclable bin
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		// 2. drop the contact to recyclable bin
		Map<String, Long> recyclableContactIds = dropToRecyclableBin(CONTACT_BO, contactId);
		
		// When: recycle the customer
		Long recyclableCustomerId = recyclableCustomerIds.get("recyclableId");
		Long recycledCustomerId = recycle(recyclableCustomerId);

		// Then:
		// 1. check the customer has been deleted from recyclable bin
        boolean existingRecyclabeCustomer = checkExistedInRecyclableBin(recyclableCustomerId);
        assertEquals(false, existingRecyclabeCustomer);
        
        // 2. check relation has been in recyclable bin
        Long recyclableRelationId = recyclableCustomerIds.get("relationId");
        boolean existingRecyclableRelation = checkeRelationExistedInRecyclableBin(recyclableRelationId);
        assertTrue(existingRecyclableRelation);
        
        // 3. check customer has been recycled  
        boolean existingRecycledCustomer = checkExisted(CUSTOMER_BO, recycledCustomerId);
        assertTrue(existingRecycledCustomer);        
	}
	
	@Test
	public void shouldTransferFrom10To11() throws Exception {
		// Given: 
		// 1. drop the customer to recyclable bin
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		// 2. drop the contact to recyclable bin
		Map<String, Long> recyclableContactIds = dropToRecyclableBin(CONTACT_BO, contactId);
		// 3. recycle the customer
		Long recyclableCustomerId = recyclableCustomerIds.get("recyclableId");
		Long recycledCustomerId = recycle(recyclableCustomerId);
		
		// When: recycle the contact
		Long recyclableContactId = recyclableContactIds.get("recyclableId");
		Long recycledContactId = recycle(recyclableContactId);
		
		// Then:
		// 1. check the contact has been deleted from the recyclable bin
		boolean existingRecyclabeContact = checkExistedInRecyclableBin(recyclableContactId);
		assertEquals(false, existingRecyclabeContact);
		
		// 2. check the relation has been deleted from the recyclable bin
		Long recyclableRelationId = recyclableCustomerIds.get("relationId");
		boolean existingRecyclableRelation = checkeRelationExistedInRecyclableBin(recyclableRelationId);
		assertEquals(false, existingRecyclableRelation);
		
		// 3. check the contact has been recycled
		boolean existingRecycledContact = checkExisted(CONTACT_BO, recycledContactId);
		assertTrue(existingRecycledContact); 
		
		// 4. check the contact has been related to the customer
        boolean existingCustomerOfContact = checkCustomerOfContactExisted(recycledContactId);
        assertTrue(existingCustomerOfContact); 
	}
	
	@Test
	public void shouldTransferFrom11To10() throws Exception {
		// When: drop the contact to recyclable bin 
		Map<String, Long> ids = dropToRecyclableBin(CONTACT_BO, contactId);
		
		// Then:
		// 1. check the data and references have been saved in recyclable bin
		Long recyclableId = ids.get("recyclableId");
		assertNotNull(recyclableId);
		Long recyclableRelationId = ids.get("relationId");
		assertNotNull(recyclableRelationId);
		
		
		// 2. check the contact has been deleted
		boolean contactExisted = checkExisted(CONTACT_BO, contactId);
		assertEquals(false, contactExisted);
	}

	@Test
	public void shouldTransferFrom10To00() throws Exception {
		// Given: drop the contact to recyclable bin	 
		Map<String, Long> recyclableContactIds = dropToRecyclableBin(CONTACT_BO, contactId);	
		Long recyclableRelationId = recyclableContactIds.get("relationId");
		assertNotNull(recyclableRelationId);
		
		// When: drop the customer to recyclable bin 
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		
		// Then:
		// 1. check the customer has been dropped to recyclable bin
		Long recyclableCustomerId = recyclableCustomerIds.get("recyclableId");
		boolean existingRecyclabeCustomer = checkExistedInRecyclableBin(recyclableCustomerId);
		assertTrue(existingRecyclabeCustomer);
		// 2. check the relation doesn't need to been saved	
		assertNull(recyclableCustomerIds.get("relationId"));		
		
		// 3. check the customer has been deleted
		boolean customerExisted = checkExisted(CUSTOMER_BO, customerId);
		assertEquals(false, customerExisted);
	}
	
	@Test
	public void shouldTransferFrom00To01() throws Exception {
		// Given: 
		// 1. drop the contact to recyclable bin	 
		Map<String, Long> recyclableContactIds = dropToRecyclableBin(CONTACT_BO, contactId);	
		Long recyclableRelationId = recyclableContactIds.get("relationId");
		assertNotNull(recyclableRelationId);

		// 2. drop the customer to recyclable bin 
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		
		// When: recycle the contact
		// will throw exception because the customer of the contact has been deleted
		thrown.expect(RuntimeException.class);		
		Long recyclableContactId = recyclableContactIds.get("recyclableId");
		Long recycledContactId = recycle(recyclableContactId);
				
	}

	@Test
	public void shouldTransferFrom01To11() throws Exception {
		// Given: drop the customer to recyclable bin 
		Map<String, Long> recyclableCustomerIds = dropToRecyclableBin(CUSTOMER_BO, customerId);
		Long recyclableCustomerId = recyclableCustomerIds.get("recyclableId");
		assertNotNull(recyclableCustomerId);
		Long recyclableRelationId = recyclableCustomerIds.get("relationId");
		assertNotNull(recyclableRelationId);
		
		// When: recycle customer
		Long recycledCustomerId = recycle(recyclableCustomerId);
        assertNotNull(recycledCustomerId);
        
        // Then:
        // 1. check customer has been deleted from recyclable bin
        boolean existingRecyclabeCustomer = checkExistedInRecyclableBin(recyclableCustomerId);
        assertEquals(false, existingRecyclabeCustomer);
        
        // 2. check relation has been deleted from recyclable bin
        boolean existingRecyclableRelation = checkeRelationExistedInRecyclableBin(recyclableRelationId);
        assertEquals(false, existingRecyclableRelation);
        
        // 3. check customer has been recycled  
        boolean existingRecycledCustomer = checkExisted(CUSTOMER_BO, recycledCustomerId);
        assertTrue(existingRecycledCustomer);
        
        // 4. check contact has been related to customer
        boolean existingCustomerOfContact = checkCustomerOfContactExisted(contactId);
        assertTrue(existingCustomerOfContact);      
	}

	private void createTestData() throws Exception {
		// add customer
		HttpResponse response = post(Web.customer$WithContact,
				CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "")
								.replaceAll("13166666666", System.currentTimeMillis() + ""));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
		Map<String, Object> result = this.parseObject(response.getString());
		Map<String, Object> customer = (Map<String, Object>) result.get("customer");
		Map<String, Object> contact = (Map<String, Object>) result.get("contact");
		customerId = Long.valueOf(customer.get("id").toString());
		contactId = Long.valueOf(contact.get("id").toString());
	}

	private boolean checkCustomerOfContactExisted(Long contactId) throws Exception {
		HttpResponse response = get(Web.Contact.append("/" + contactId));		
	    assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
	    Map<String, Object> result = this.parseObject(response.getString());
	    Map<String, Object> customer = (Map<String, Object>)result.get("customer");	    
	    return (customer != null && customer.get("id") != null)  ? true : false;
	}

	
}
