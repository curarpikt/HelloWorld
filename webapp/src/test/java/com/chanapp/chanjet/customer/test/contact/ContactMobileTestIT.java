package com.chanapp.chanjet.customer.test.contact;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanjet.csp.platform.test.HttpResponse;

@SuppressWarnings("unchecked")
public class ContactMobileTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";

    static String CONTACT_PAYLOAD = FileReader.read("contact/MobileContact.json");
    static String ORIGIN_CONTACT_PAYLOAD = CONTACT_PAYLOAD;

    @Before
    public void setUp() throws Exception {
        CUSTOMER_ID = "";

        CONTACT_ID = "";

        replaceCustomerInfo();
    }

    private void replaceCustomerInfo() {
        CONTACT_PAYLOAD = ORIGIN_CONTACT_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "")
                .replaceAll("13166666666", System.currentTimeMillis() + "");
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

    @Test
    public void testAddContact() throws Exception {
        HttpResponse response = post(Rest.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        Map<String, Object> customer = (Map<String, Object>) contact.get("customer");
        CONTACT_ID = contact.get("id").toString();
        CUSTOMER_ID = customer.get("id").toString();
    }

/*    @Test
    public void testHeadPicture() throws Exception {
        HttpResponse response = post(Rest.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        Map<String, Object> customer = (Map<String, Object>) contact.get("customer");
        CONTACT_ID = contact.get("id").toString();
        CUSTOMER_ID = customer.get("id").toString();

        response = upload(Rest.contact$HeadPicture, "contact/abc.jpg");
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

    }*/

    @Test
    public void testGetContactsByMobileAndPhone() throws Exception {
        HttpResponse response = post(Rest.Contact, CONTACT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> contact = (Map<String, Object>) result.get("entity");
        Map<String, Object> customer = (Map<String, Object>) contact.get("customer");
        CONTACT_ID = contact.get("id").toString();
        CUSTOMER_ID = customer.get("id").toString();

        response = get(Rest.contact$GetContactsByMobileAndPhone.append("?mobile=18309090902&phone=11112222"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);

    }

}
