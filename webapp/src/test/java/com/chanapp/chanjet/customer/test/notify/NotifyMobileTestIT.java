package com.chanapp.chanjet.customer.test.notify;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanjet.csp.platform.test.HttpResponse;

public class NotifyMobileTestIT extends RestletBaseTest {

    @Test
    public void testRestCount() throws Exception {
        HttpResponse response = get(Rest.notify$RestCount.append("?categories=1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
       // assertTrue(result.containsKey("count"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRecords() throws Exception {
        HttpResponse response = get(
                Rest.notify$Records.append("?timeline=" + System.currentTimeMillis() + "&categories=1&count=10"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }

    @Test
    public void testUnReadCount() throws Exception {
        HttpResponse response = get(Rest.notify$UnReadCount.append("?categories=1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("count"));
    }

}
