package com.chanapp.chanjet.customer.test.recycle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
@TestVersions({"v4"})
@SuppressWarnings("unchecked")
public class RecycleTestIT extends RestletBaseTest {
/*    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";

    private String IDS = "";
    private List<Map<String, Object>> ITEMS = new ArrayList<Map<String, Object>>();

    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");

    @Before
    public void setUp() throws Exception {
        CUSTOMER_ID = "";
        CONTACT_ID = "";
    }

    @After
    public void tearDown() throws Exception {
        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
    }

    private void initRecycleIds() throws Exception {
        IDS = "";
        ITEMS = new ArrayList<Map<String, Object>>();

        // add customer
        HttpResponse response = post(Web.customer$WithContact,
                CUSTOMER_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                        System.currentTimeMillis() + ""));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID = contact.get("id").toString();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ids", Arrays.asList(CUSTOMER_ID));
        data.put("reason", "testdelreason");

        // delete customer
        response = post(Web.customer$DeleteByIds, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("success").toString()) >= 1);

        // query recycle
        data = new HashMap<String, Object>();
        data.put("entityName", "Customer");
        data.put("operUserIds", this.getAppManagerId());
        data.put("startTime", System.currentTimeMillis() - 100000L);
        data.put("endTime", System.currentTimeMillis() + 100000L);
        data.put("pageno", "1");
        data.put("pagesize", "10");

        response = post(Web.recycle$GetRecycles, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        Map<String, Object> recycles = (Map<String, Object>) result.get("resultObj");
        assertTrue(recycles.containsKey("items"));

        ITEMS = (List<Map<String, Object>>) recycles.get("items");

        assertTrue(ITEMS.size() > 0);
        for (Map<String, Object> item : ITEMS) {
            if (!IDS.isEmpty()) {
                IDS += ",";
            }
            IDS += ((Map<String, Object>) item.get("recycle")).get("id");
        }

    }

    @Test
    public void testDelRecycles() throws Exception {
        initRecycleIds();

        // delete recycle
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ids", IDS);
        HttpResponse response = post(Web.recycle$DelRecycles, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        assertEquals(result.get("resultObj").toString(), ITEMS.size() + "");
    }

    @Test
    public void testCleanRecycle() throws Exception {
        initRecycleIds();

        HttpResponse response = get(Web.recycle$CleanRecycle);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        assertTrue(Integer.valueOf(result.get("resultObj").toString()) > 0);
    }

    @Test
    public void testGetOperUsers() throws Exception {
        initRecycleIds();

        HttpResponse response = get(Web.recycle$GetOperUsers.append("?entityName=Customer"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        List<Map<String, Object>> users = (List<Map<String, Object>>) result.get("resultObj");
        for (Map<String, Object> user : users) {
            if (user.get("id").toString().equals(this.getAppManagerId())) {
                return;
            }
        }
        fail(response.getString() + ":testGetOperUsers");
    }

    @Test
    public void testRestore() throws Exception {
        //initRecycleIds();
        String recycleId = "100005";
        String entityIds = "100001";
        for (Map<String, Object> item : ITEMS) {
            recycleId = ((Map<String, Object>) item.get("recycle")).get("id").toString();

            List<Map<String, Object>> relations = (List<Map<String, Object>>) item.get("recycleRelations");
            for (Map<String, Object> relation : relations) {
                if (!entityIds.isEmpty()) {
                    entityIds += ",";
                }
                entityIds += relation.get("entityId").toString();
            }

            break;
        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("recycleId", recycleId);
        data.put("entityIds", entityIds);

        HttpResponse response = post(Web.recycle$Restore, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        assertTrue(Integer.valueOf(result.get("resultObj").toString()) > 0);
    }

    @Test
    public void testGetRecycles() throws Exception {
        initRecycleIds();
    }

    @Test
    public void testHisRecycleInfo() throws Exception {
        HttpResponse response = get(Web.recycle$HisRecycleInfo);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));

        // assertEquals(result.get("resultObj").toString(), "3");
    }

    @Test
    public void testRecHisReocrds() throws Exception {
        // {"resultObj":{"needRecycle":"false"}}

        HttpResponse response = get(Web.recycle$HisRecycleInfo);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));
        Map<String, Object> resultObj = (Map<String, Object>) result.get("resultObj");
        assertTrue(resultObj.containsKey("needRecycle"));

        boolean needRecycle = Boolean.valueOf(resultObj.get("needRecycle").toString());

        String tag = needRecycle ? "start" : "run";

        response = get(Web.recycle$RecHisReocrds.append("?tag=" + tag));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("resultObj"));
        resultObj = (Map<String, Object>) result.get("resultObj");
        assertTrue(resultObj.containsKey("result"));

        assertTrue(Boolean.valueOf(resultObj.get("result").toString()));
    }*/
}
