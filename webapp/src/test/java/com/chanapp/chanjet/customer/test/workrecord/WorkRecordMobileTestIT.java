package com.chanapp.chanjet.customer.test.workrecord;

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
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

/**
 * @author tds
 *
 */
@SuppressWarnings("unchecked")
public class WorkRecordMobileTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";
    private String WORKRECORD_ID = "";

    static String WORKRECORD_PAYLOAD = FileReader.read("workrecord/WorkRecord.json");
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
    static String ORIGIN_WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD;

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
        CONTACT_ID = contact.get("id").toString();

        WORKRECORD_ID = "";

        replacePaylod();
    }

    private void replacePaylod() {
        WORKRECORD_PAYLOAD = ORIGIN_WORKRECORD_PAYLOAD.replace("100001", CUSTOMER_ID);
        WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD.replace("-1442818200499", "-" + System.currentTimeMillis());
    }

    @After
    public void tearDown() throws Exception {
        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        // if (CUSTOMER_ID != null && !CUSTOMER_ID.isEmpty()) {
        // HttpResponse response = delete("/services/1.0/bo/dml/Customer/" +
        // CUSTOMER_ID);
        // assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(),
        // response.getStatusCode());
        // }
        //
        // if (WORKRECORD_ID != null && !WORKRECORD_ID.isEmpty()) {
        // HttpResponse response = delete("/services/1.0/bo/dml/WorkRecord/" +
        // WORKRECORD_ID);
        // assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(),
        // response.getStatusCode());
        // }

    }

    @Test
    public void testWorkRecord() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = get(Rest.WorkRecord.append("/" + WORKRECORD_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("customerName"));
    }

/*    @Test
    public void testAttamentUpload() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        Url url = Rest.workrecord$AttamentUpload;
        if (isV3()) {
            url = url.append("/aaa");
        } else {
            url = url.append("?category=aaa");
        }
        response = upload(url, "workrecord/WorkRecord.json");
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        // {"result":true,"name":"WorkRecord","suffix":"json","url":"http://sto.chanapp.chanjet.com/90003747057/2016/04/20/70ad2a2ec8ea4928b1deaa8e0d20ebd4.json","size":109}
        // {"name":"WorkRecord","suffix":"json","url":"http://sto.chanapp.chanjet.com/90003734250/2016/04/20/be8a8a38fb13454d86d6de11cf1a6e3d.json","size":109}
        assertEquals("json", result.get("suffix").toString());
    }
*/
}
