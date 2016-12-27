package com.chanapp.chanjet.customer.test.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

@SuppressWarnings("unchecked")
public class ContactTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";

    private String CONTACT_ID2 = "";

    static String CONTACT_PAYLOAD = FileReader.read("contact/Contact.json");
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
    static String ORIGIN_CONTACT_PAYLOAD = CONTACT_PAYLOAD;

    @Before
    public void setUp() throws Exception {
        CUSTOMER_ID = "";

        HttpResponse response = post(Web.customer$WithContact,
                CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                        System.currentTimeMillis() + ""));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID2 = contact.get("id").toString();

        CONTACT_ID = "";

        replaceCustomerId();
    }

    private void replaceCustomerId() {
        CONTACT_PAYLOAD = ORIGIN_CONTACT_PAYLOAD.replace("100001", CUSTOMER_ID);
    }

    @After
    public void tearDown() throws Exception {
/*        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CONTACT_ID2 != null && !CONTACT_ID2.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID2));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CUSTOMER_ID != null && !CUSTOMER_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Customer/" + CUSTOMER_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }*/

    }

    @Test
    public void testAddContact() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();
    }
    
/*    @Test
    public void testAddMutiContact() throws Exception{
    	for(int i=0;i<650;i++){
    	    CONTACT_PAYLOAD = ORIGIN_CONTACT_PAYLOAD.replace("100001", CUSTOMER_ID).replace("ABCD", CUSTOMER_ID+i);
            HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            Map<String, Object> result = this.parseObject(response.getString());
            Map<String, Object> contact = (Map<String, Object>) result.get("entity");
           // CONTACT_ID = contact.get("id").toString();
    	}
    }*/

    @Test
    public void testEditContact() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();

        Map<String, Object> map = this.parseObject(CONTACT_PAYLOAD);
        map.put("id", CONTACT_ID);
        map.put("name", "hehehe");
        String _CONTACT_PAYLOAD = this.toJSONString(map);

        response = put(Web.Contact, _CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        contact = this.parseObject(response.getString());
        assertEquals(contact.get("name").toString(), "hehehe");

    }

    @Test
    public void testDeleteContact() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();

        response = delete(Web.Contact.append("/" + CONTACT_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void testListOfCustomer() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();

        response = get(Web.contact$ListOfCustomer.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testQueryContact() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();

        response = get(Web.Contact.append("/" + CONTACT_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        contact = (Map<String, Object>) result.get("entity");
        assertEquals(contact.get("fax").toString(), "222");
    }

    @Test
    public void testSortField() throws Exception {
        List<String> data = Arrays.asList("name", "remark", "email", "qq", "mobile", "phone", "position", "gender",
                "appellation", "weibo", "fax", "customer", "department", "address");

        HttpResponse response = post(Web.contact$sortField$Edit, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }
    
    @Test
    public void testH5addContact() throws Exception {
     //   HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        HttpResponse response = this.doPost("appManager", "/restlet/mobile/Contact", CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        System.out.println(response.getString());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();
    }
    
   
    @Test
    public void testH5deleteContact() throws Exception {
        HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        CONTACT_ID = contact.get("id").toString();
     //   HttpResponse response = post(Web.Contact, CONTACT_PAYLOAD);
        response = this.doDelete("appManager", "/restlet/mobile/Contact/"+CONTACT_ID,null);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());      
        result = this.parseObject(response.getString());
    }
}
