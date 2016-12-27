package com.chanapp.chanjet.customer.test.todowork;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
import com.chanjet.csp.platform.test.utils.ConfigInfo;
@TestVersions({"v4"})
@SuppressWarnings("unchecked")
public class TodoWorkTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";
    private String WORKRECORD_ID = "";
    private String TODOWORK_ID = "";
    
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
	static String WORKRECORD_PAYLOAD = FileReader.read("workrecord/WorkRecord.json");
	static String ORIGIN_WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD;
	static String TODOWORK_PAYLOAD = FileReader.read("todowork/TodoWork.json");
	static String ORIGIN_TODOWORK_PAYLOAD = TODOWORK_PAYLOAD;
//	System.out.print("123");
//	System.out.println("CUSTOMER_PAYLOAD" + CUSTOMER_PAYLOAD);
	

	
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

        TODOWORK_ID = "";

        replaceTodoWorkPaylod();

    }

    private void replaceWorkRecordPaylod() {
        WORKRECORD_PAYLOAD = ORIGIN_WORKRECORD_PAYLOAD.replace("100001", CUSTOMER_ID);
        WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD.replace("-1442818200499", "-" + System.currentTimeMillis());
    }

    private void replaceTodoWorkPaylod() {
        TODOWORK_PAYLOAD = ORIGIN_TODOWORK_PAYLOAD.replace("100001", CUSTOMER_ID);
        TODOWORK_PAYLOAD = TODOWORK_PAYLOAD.replace("100002", WORKRECORD_ID);
    }

    @After
    public void tearDown() throws Exception {
/*        if (TODOWORK_ID != null && !TODOWORK_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("TodoWork/" + TODOWORK_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }
*/
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
    public void testAddTodoWork() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();
    }

    @Test
    public void testEditTodoWork() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        String TODOWORK_PAYLOAD_EDIT = TODOWORK_PAYLOAD;
        TODOWORK_PAYLOAD_EDIT = TODOWORK_PAYLOAD_EDIT.replace("\"TODO\",", "\"TODO\",\"id\":" + TODOWORK_ID + ",");
        TODOWORK_PAYLOAD_EDIT = TODOWORK_PAYLOAD_EDIT.replace("有客户的第一个待办", "有客户的第二个待办");
        response = put(Web.TodoWork, TODOWORK_PAYLOAD_EDIT);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertEquals(response.getString(), result.get("workContent"), "有客户的第二个待办");
    }

    @Test
    public void testDeleteTodoWork() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = delete(Web.TodoWork.append("/" + TODOWORK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));

        TODOWORK_ID = "";
    }

    @Test
    public void testQueryTodoWork() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.TodoWork.append("/" + TODOWORK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertEquals(response.getString(), result.get("workContent"), "有客户的第一个待办");
    }

    @Test
    public void testTodoList() throws Exception {   	
    	//System.out.println("CUSTOMER_PAYLOAD::" + CUSTOMER_PAYLOAD);
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        //System.out.println("TODOWORK_PAYLOAD$$$$$$$$$$$$$$$$$$$$$$$$$$$" + TODOWORK_PAYLOAD);

        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        //System.out.println("response.getString():" + response.getString());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();
        response = get(Web.todowork$TodoList);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        //System.out.println("response.getString():" + response.getString());
        //System.out.println("result.getString():" + result.toString());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testCountTodoWork() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$CountTodoWork.append("?status=TODO&startDate=1442582000000&endDate=1444582000000"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("num").toString()) >= 1);
    }

    @Test
    public void testQuery() throws Exception {
        String DONE_TODOWORK_PAYLOAD = TODOWORK_PAYLOAD;
        DONE_TODOWORK_PAYLOAD = DONE_TODOWORK_PAYLOAD.replace("TODO", "DONE");
        HttpResponse response = post(Web.TodoWork, DONE_TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$Query.append("?status=DONE&startDate=1442582000000&endDate=1444582000000"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testCountDoneTodoWorks() throws Exception {
        String DONE_TODOWORK_PAYLOAD = TODOWORK_PAYLOAD;
        DONE_TODOWORK_PAYLOAD = DONE_TODOWORK_PAYLOAD.replace("TODO", "DONE");
        HttpResponse response = post(Web.TodoWork, DONE_TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$CountDoneTodoWorks.append("?timeType=MONTH"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testDoneList() throws Exception {
        String DONE_TODOWORK_PAYLOAD = TODOWORK_PAYLOAD;
        DONE_TODOWORK_PAYLOAD = DONE_TODOWORK_PAYLOAD.replace("TODO", "DONE");
        HttpResponse response = post(Web.TodoWork, DONE_TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$DoneList.append("?timeType=MONTH&pageno=1&pagesize=20"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testHandle() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("id", TODOWORK_ID);
        data.put("status", "DONE");
        response = put(Web.todowork$Handle, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testFindByWorkRecordId() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$FindByWorkRecordId.append("?workrecordId=" + WORKRECORD_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result1 = this.parseObject(response.getString(), List.class);
        assertTrue(result1.size() >= 1);
    }

    @Test
    public void testGetTodoRemindType() throws Exception {
        HttpResponse response = post(Web.TodoWork, TODOWORK_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        TODOWORK_ID = result.get("id").toString();

        response = get(Web.todowork$GetTodoRemindType);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result1 = this.parseObject(response.getString(), List.class);
        assertTrue(result1.size() >= 1);
    }

}
