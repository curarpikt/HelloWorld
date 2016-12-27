package com.chanapp.chanjet.customer.test.invite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanjet.csp.platform.test.HttpResponse;

public class InviteMobileTestIT extends RestletBaseTest {
    static String INVITE_SHORT_PAYLOAD = FileReader.read("invite/InviteShort.json");

    @Test
    public void testInviteShort() throws Exception {
        HttpResponse response = post(Rest.invite$InviteShort, INVITE_SHORT_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testRecords() throws Exception {
        HttpResponse response = get(Rest.invite$Records.append("?pageno=1&pagesize=10"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("total"));
    }

}
