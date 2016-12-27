package com.chanapp.chanjet.customer.test.sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class SyncTestIT extends RestletBaseTest {
    static String LOAD_CSV_PAYLOAD = FileReader.read("sync/LoadCSV.json");
    static String SAVE_PAYLOAD = FileReader.read("sync/SyncSave.json");
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");

    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";
    private String CHECKIN_ID = "";

    @After
    public void tearDown() throws Exception {
        if (CHECKIN_ID != null && !CHECKIN_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Checkin/" + CHECKIN_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CUSTOMER_ID != null && !CUSTOMER_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Customer/" + CUSTOMER_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

    }

    @TestVersions({"v4"})
    @Test
    public void testLoadCSV() throws Exception {
        HttpResponse response = post(Rest.sync$LoadCSV, LOAD_CSV_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        //System.out.println(response.getString());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Long.valueOf(result.get("syncVersion").toString()) >= 0L);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSyncSave() throws Exception {
        HttpResponse response = post(Web.customer$WithContact,
                CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                        System.currentTimeMillis() + ""));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID = contact.get("id").toString();

        response = post(Rest.sync$SyncSave, SAVE_PAYLOAD.replace("100001", CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("entity"));
        result = (Map<String, Object>) result.get("entity");
        assertEquals(result.get("customerId").toString(), CUSTOMER_ID);

        CHECKIN_ID = result.get("id").toString();
    }

}
