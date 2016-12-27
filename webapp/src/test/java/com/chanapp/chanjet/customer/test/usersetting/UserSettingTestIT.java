package com.chanapp.chanjet.customer.test.usersetting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class UserSettingTestIT extends RestletBaseTest {

    @Test
    public void testGetUserSetting() throws Exception {
        HttpResponse response = get(Web.usersetting$GetUserSetting);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUserSetting() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("key", "16.1");
        data.put("value", "true");
        HttpResponse response = post(Web.UserSetting, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("value").toString()));

        response = get(Web.usersetting$GetUserSetting);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("16.1").toString()));

        data.put("value", "false");
        response = post(Web.UserSetting, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertFalse(Boolean.valueOf(result.get("value").toString()));

        response = get(Web.usersetting$GetUserSetting);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertFalse(Boolean.valueOf(result.get("16.1").toString()));
    }

    @Test
    public void testWorkingHours() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("startTime", "08:00");
        data.put("endTime", "18:00");
        data.put("status", true);

        HttpResponse response = post(Web.usersetting$WorkingHours, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("status").toString()));

        response = get(Web.usersetting$WorkingHours);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("status").toString()));
    }

    @Test
    public void testWorkingHoursMobile() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("startTime", "08:00");
        data.put("endTime", "18:00");
        data.put("status", true);

        HttpResponse response = post(Rest.usersetting$WorkingHours, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("status").toString()));

        response = get(Rest.usersetting$WorkingHours);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("status").toString()));
    }
}
