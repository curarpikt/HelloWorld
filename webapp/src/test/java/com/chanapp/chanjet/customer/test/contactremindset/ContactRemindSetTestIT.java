package com.chanapp.chanjet.customer.test.contactremindset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

@SuppressWarnings("unchecked")
public class ContactRemindSetTestIT extends RestletBaseTest {
    static String REMINDSET_PAYLOAD = FileReader
            .read("contactremindset/ContactRemindSet.json");

    @Test
    public void testSave() throws Exception {
        HttpResponse response = post(Web.contactremindset$Save, REMINDSET_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        Map<String, Object> result1 = (Map<String, Object>) result.get("resultObj");
        assertTrue(result1.containsKey("result"));

        assertTrue(Boolean.valueOf(result1.get("result").toString()));
    }

    @Test
    public void testQuery() throws Exception {
   /*     HttpResponse response = post(Web.contactremindset$Save, REMINDSET_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));*/

/*        Map<String, Object> result1 = (Map<String, Object>) result.get("resultObj");
        assertTrue(result1.containsKey("result"));

        assertTrue(Boolean.valueOf(result1.get("result").toString()));*/

    	HttpResponse response = get(Web.contactremindset$Query.append("?modifyTime=1444450598436"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        Map<String, Object> result1 = (Map<String, Object>) result.get("resultObj");
        assertTrue(result1.containsKey("enumName"));

        //assertEquals(result1.get("enumName").toString(), "type");
    }

    @TestVersions({"v4"})
    @Test
    public void testGetDefaultSetValue() throws Exception {
        HttpResponse response = get(Web.contactremindset$GetDefaultSetValue);
        System.out.println(response.getString());
/*        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        Map<String, Object> result1 = (Map<String, Object>) result.get("resultObj");
        assertTrue(result1.containsKey("defaultSetValue"));

        assertEquals(result1.get("defaultSetValue").toString(), "7");*/
    }
}
