package com.chanapp.chanjet.customer.test.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
@TestVersions({"v4"})
@SuppressWarnings("unchecked")
public class ConditionTestIT extends RestletBaseTest {
    static String CUSTOMER_CONDITION_PAYLOAD = FileReader
            .read("condition/CustomerCondition.json");

    @Test
    public void testConfig() throws Exception {
        HttpResponse response = post(Web.condition$Config, CUSTOMER_CONDITION_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testFavorite() throws Exception {
        HttpResponse response = post(Web.condition$Favorite, CUSTOMER_CONDITION_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testFavoriteDelete() throws Exception {
        HttpResponse response = delete(Web.condition$FavoriteDelete.append("?versionId=1443490952363"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testConfigList() throws Exception {
        HttpResponse response = get(Web.condition$ConfigList.append("?entityType=Customer"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }

    @Test
    public void testFavoriteList() throws Exception {
        HttpResponse response = get(Web.condition$FavoriteList.append("?entityType=Customer"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }
}
