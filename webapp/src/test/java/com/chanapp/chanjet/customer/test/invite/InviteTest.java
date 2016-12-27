package com.chanapp.chanjet.customer.test.invite;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
@TestVersions({"v4"})
public class InviteTest extends RestletBaseTest {
    @Test
    public void testOrganization() throws Exception {
        HttpResponse response = get(Web.invite$Organization);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testUpdateOrgName() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("orgName", "xxx_xxx_" + System.currentTimeMillis());
        HttpResponse response = post(Web.invite$UpdateOrgName, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
       // assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testRecords() throws Exception {
        HttpResponse response = get(Web.invite$Records.append("?pageno=1&pagesize=10"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("total"));
    }

    @Test
    public void testSend() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("account", "suming_mail12@126.com");
        HttpResponse response = post(Web.invite$Send, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        // Map<String, Object> result = this.parseObject(response.getString());
        // assertTrue(Boolean.valueOf(result.get("result").toString()));
    }
}
