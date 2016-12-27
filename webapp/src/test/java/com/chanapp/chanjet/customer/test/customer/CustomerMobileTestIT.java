package com.chanapp.chanjet.customer.test.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

@SuppressWarnings("unchecked")
public class CustomerMobileTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";

    private String CONTACT_ID = "";

    static String PAYLOAD = FileReader.read("customer/WithContact.json");
    
    static String GQLSTRING = FileReader.read("customer/GQL.json");
    static String ORIGIN_PAYLOAD = PAYLOAD;

    @Before
    public void setUp() throws Exception {
        CUSTOMER_ID = "";

        CONTACT_ID = "";

        PAYLOAD = ORIGIN_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                System.currentTimeMillis() + "");
    }

    @After
    public void tearDown() throws Exception {
        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CUSTOMER_ID != null && !CUSTOMER_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Customer/" + CUSTOMER_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

    }

    private void addOneCustomerWithContact() throws Exception {
        HttpResponse response = post(Web.customer$WithContact, PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID = contact.get("id").toString();
    }

    @Test
    public void testGetCustomersByName() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Rest.customer$GetCustomersByName.append("?name=" + urlEncode("A")));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        System.out.println(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
        assertTrue(((List<Map<String, Object>>) result.get("items")).size() > 0);
    }

    @Test
    public void testShare() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("sharedUserIds", Arrays.asList(this.getRegularUserId()));
        data.put("customerIds", Arrays.asList(CUSTOMER_ID));
        data.put("privileges", Arrays.asList("SELECT"));

        HttpResponse response = post(Rest.customer$Share, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUnShare() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("sharedUserIds", Arrays.asList(this.getRegularUserId()));
        data.put("customerIds", Arrays.asList(CUSTOMER_ID));
        data.put("privileges", Arrays.asList("SELECT"));

        HttpResponse response = post(Rest.customer$UnShare, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUnShareList() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Rest.customer$UnShareList.append("?customerId=" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
        assertTrue(result.containsKey("users"));
    }
    
    @Test
	public void testGQLQuery() throws Exception{    	
    	Map<String,String> para= new HashMap<String,String>();
    	para.put("query", GQLSTRING);
        HttpResponse response = this.doPost("regularUser", "/services/1.0/gql", para);
        System.out.println(response.getString());
	}
    
    
}
