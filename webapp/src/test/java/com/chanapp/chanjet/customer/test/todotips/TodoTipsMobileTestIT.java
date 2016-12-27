package com.chanapp.chanjet.customer.test.todotips;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanjet.csp.platform.test.HttpResponse;

/**
 * @author tds
 *
 */
@TestVersions({"v4"})
@SuppressWarnings("unchecked")
public class TodoTipsMobileTestIT extends RestletBaseTest {
    @Before
    public void setUp() throws Exception {
        HttpResponse response = get(Rest.todotips$Query);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @After
    public void tearDown() throws Exception {
        HttpResponse response = get(Csp.bo$query.append("TodoTips"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> todoTips = this.parseObject(response.getString(), List.class);
        for (Map<String, Object> todoTip : todoTips) {
            response = delete(Csp.bo$dml.append("TodoTips/" + todoTip.get("id")));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

    @Test
    public void testSave() throws Exception {
        HttpResponse response = get(Rest.todotips$Save.append("?todoTips=hehe1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testUpdate() throws Exception {
        HttpResponse response = get(Rest.todotips$Save.append("?todoTips=hehe1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Csp.bo$query.append("TodoTips"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> todoTips = this.parseObject(response.getString(), List.class);
        assertTrue(todoTips.size() > 0);

        response = get(Rest.todotips$Update.append("?todoTips=xxx&id=" + todoTips.get(0).get("id")));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testDelete() throws Exception {
        HttpResponse response = get(Rest.todotips$Save.append("?todoTips=hehe1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Csp.bo$query.append("TodoTips"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> todoTips = this.parseObject(response.getString(), List.class);
        assertTrue(todoTips.size() > 0);

        response = get(Rest.todotips$Delete.append("?id=" + todoTips.get(0).get("id").toString()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testSort() throws Exception {
        HttpResponse response = get(Rest.todotips$Save.append("?todoTips=hehe1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Rest.todotips$Save.append("?todoTips=hehe2"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Rest.todotips$Save.append("?todoTips=hehe3"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Csp.bo$query.append("TodoTips"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> todoTips = this.parseObject(response.getString(), List.class);
        assertTrue(todoTips.size() > 0);

        String ids = "";
        for (Map<String, Object> todoTip : todoTips) {
            if (!ids.isEmpty()) {
                ids += ",";
            }
            ids += todoTip.get("id").toString();
        }

        response = get(Rest.todotips$Sort.append("?ids=" + ids));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Rest.todotips$Query);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
        todoTips = (List<Map<String, Object>>) result.get("data");
        assertNotNull(todoTips);

        String ids2 = "";
        for (Map<String, Object> todoTip : todoTips) {
            if (!ids2.isEmpty()) {
                ids2 += ",";
            }
            ids2 += todoTip.get("id").toString();
        }

        assertEquals(ids, ids2);
    }

    @Test
    public void testQuery() throws Exception {
        HttpResponse response = get(Rest.todotips$Save.append("?todoTips=hehe1" + System.currentTimeMillis()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Rest.todotips$Query);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
        List<Map<String, Object>> todoTips = (List<Map<String, Object>>) result.get("data");
        assertNotNull(todoTips);

    }

}
