package com.chanapp.chanjet.customer.test.comment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

public class CommentTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";
    private String WORKRECORD_ID = "";
    private String COMMENT_ID = "";

    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
    static String WORKRECORD_PAYLOAD = FileReader.read("workrecord/WorkRecord.json");
    static String ORIGIN_WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD;
    static String COMMENT_PAYLOAD = FileReader.read("comment/Comment.json");
    static String ORIGIN_COMMENT_PAYLOAD = COMMENT_PAYLOAD;

    @SuppressWarnings("unchecked")
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

        replaceWorkRecordPaylod();

        response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        COMMENT_ID = "";

        replaceCommentPaylod();

    }

    private void replaceWorkRecordPaylod() {
        WORKRECORD_PAYLOAD = ORIGIN_WORKRECORD_PAYLOAD.replace("100001", CUSTOMER_ID);
        WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD.replace("-1442818200499", "-" + System.currentTimeMillis());
    }

    private void replaceCommentPaylod() {
        COMMENT_PAYLOAD = ORIGIN_COMMENT_PAYLOAD.replace("100001", WORKRECORD_ID);
    }

    @After
    public void tearDown() throws Exception {
        if (COMMENT_ID != null && !COMMENT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Comment/" + COMMENT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

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
    public void testAddComment() throws Exception {
        HttpResponse response = post(Web.Comment, COMMENT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        COMMENT_ID = result.get("id").toString();
    }

    @Test
    public void testListByWorkRecord() throws Exception {
        HttpResponse response = post(Web.Comment, COMMENT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        COMMENT_ID = result.get("id").toString();

        response = get(Web.comment$ListByWorkRecord.append("/" + WORKRECORD_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

}
