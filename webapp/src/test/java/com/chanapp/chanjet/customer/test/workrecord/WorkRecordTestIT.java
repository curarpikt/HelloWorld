package com.chanapp.chanjet.customer.test.workrecord;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

/**
 * @author tds
 *
 */
@SuppressWarnings("unchecked")
public class WorkRecordTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CONTACT_ID = "";
    private String WORKRECORD_ID = "";

    static String WORKRECORD_PAYLOAD = FileReader.read("workrecord/WorkRecord.json");
    static String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
	static String PAYLOAD = FileReader.read("workrecord/workrecordH5.json");
	static String UPDATEPAYLOAD = FileReader.read("workrecord/updateworkrecord.json");
    static String ORIGIN_WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD;

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

        replacePaylod();
    }

    private void replacePaylod() {
        WORKRECORD_PAYLOAD = ORIGIN_WORKRECORD_PAYLOAD.replace("100001", CUSTOMER_ID);
        WORKRECORD_PAYLOAD = WORKRECORD_PAYLOAD.replace("-1442818200499", "-" + System.currentTimeMillis());
    }

    @After
    public void tearDown() throws Exception {
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

    @TestVersions({"v4"})
    @Test
    public void testAddWorkRecord() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();
    }

    @Test
    public void testDeleteWorkRecord() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = delete(Web.WorkRecord.append("/" + WORKRECORD_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }
    @TestVersions({"v4"})
    @Test
    public void testCondition() throws Exception {
       // HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
/*        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();*/

        Map<String, Object> queryValue = new HashMap<String, Object>();
        //queryValue.put("ownerId", getAppManagerId());

        HttpResponse response = get(Web.workrecord$Condition.append(
                "?pageno=1&pagesize=10&queryValue=" + urlEncode(this.toJSONString(queryValue))));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(((List<Object>) result.get("items")).size() >= 1);
    }

    @Test
    public void testDownload() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();
        response = get(Web.workrecord$Download.append("?workrecrodId=" + WORKRECORD_ID + "&url="
                + urlEncode(
                        "http://sto.chanapp.chanjet.com/90003747057/2016/03/30/93508ed906b44d17ba8f7478f958bc8b.json")
                + "&fileName=abcd"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }
    
/*    @Test
    public void testAddWorkRecords() throws Exception{
    	Integer index =120303;
    	for(int i=0;i<1000;i++){
    		index++;
    		CUSTOMER_ID = index.toString();
    		replacePaylod();
    	    HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
    	}
    	
    }
*/
    @Test
    public void testListByCustomer() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = get(Web.workrecord$ListByCustomer.append("/" + CUSTOMER_ID + "?pageno=1&pagesize=2"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(((List<Object>) result.get("items")).size() >= 1);
    }
    @TestVersions({"v4"})
    @Test
    public void testQueryWorkRecord() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

    	response = get(Web.WorkRecord.append("/"+WORKRECORD_ID ));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
       // assertEquals(result.get("content").toString(), "w2w23232");
    }

/*    @Test
    public void testAttamentUpload() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = upload(Web.workrecord$AttamentUpload,
                "workrecord/WorkRecord.json");
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        // {"result":true,"name":"WorkRecord","suffix":"json","url":"http://sto.chanapp.chanjet.com/90003747057/2016/04/20/70ad2a2ec8ea4928b1deaa8e0d20ebd4.json","size":109}
        // {"name":"WorkRecord","suffix":"json","url":"http://sto.chanapp.chanjet.com/90003734250/2016/04/20/be8a8a38fb13454d86d6de11cf1a6e3d.json","size":109}
        assertEquals("json", result.get("suffix").toString());
    }*/

/*    @Test
    public void testAttaments() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = upload(Web.workrecord$AttamentUpload,
                "workrecord/WorkRecord.json");
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertEquals("json", result.get("suffix").toString());

        response = get(Web.workrecord$Attaments.append("/" + WORKRECORD_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }
*/
    @Test
    public void testLatestRecord() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = get(Web.workrecord$LatestRecord);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("number"));
    }


    @Test
    public void testRead() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = get(Web.workrecord$Read);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testFollows() throws Exception {
        HttpResponse response = post(Web.WorkRecord, WORKRECORD_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        WORKRECORD_ID = result.get("id").toString();

        response = get(Web.workrecord$Follows.append("?pageno=1&pagesize=10"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("items"));
    }
    
	@Test
	public void testAddWorkRecordH5() throws Exception{
		String addPayLoad = PAYLOAD.replace("100002", CUSTOMER_ID);
		HttpResponse response =  this.doPost("appManager","/restlet/mobile/WorkRecord", addPayLoad);	
		assertEquals(200, response.getStatusCode());
		//System.out.println(response.getString());
		Map<String, Object> data=this.parseObject(response.getString(), Map.class);
		WORKRECORD_ID=data.get("id").toString();
	}
	
	@Test
	public void testGetWorkRecordListH5() throws Exception{
		//http://uliiad89h3rk.chanapp.com/chanjet/customer/restlet/mobile/WorkRecord?first=0&max=25&keyWord=s&owner=
 		HttpResponse response =  this.doGet("appManager","/restlet/mobile/WorkRecord?first=0&max=30&owner=&customerId=",null);
		assertEquals(200, response.getStatusCode());
 		System.out.println(response.getString());
	}
	
	@Test
	public void testUpdateWorkRecordH5() throws Exception{
		String addPayload = PAYLOAD.replace("100002", CUSTOMER_ID);
		HttpResponse response =  this.doPost("appManager","/restlet/mobile/WorkRecord", addPayload);
		Map<String, Object> data=this.parseObject(response.getString(), Map.class);
		WORKRECORD_ID=data.get("id").toString();
		String updatePayload = UPDATEPAYLOAD.replace("100002", CUSTOMER_ID);
		response =  this.doPut("appManager","/restlet/mobile/WorkRecord/"+WORKRECORD_ID, updatePayload);
		System.out.println("updatework:"+response.getString());
		assertEquals(200, response.getStatusCode());
	}
}
