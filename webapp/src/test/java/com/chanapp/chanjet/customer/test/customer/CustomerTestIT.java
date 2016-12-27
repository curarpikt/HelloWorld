package com.chanapp.chanjet.customer.test.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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

@SuppressWarnings("unchecked")
@TestVersions({"v4"})
public class CustomerTestIT extends RestletBaseTest {
    private String CUSTOMER_ID = "";
    private String CUSTOMER_ID2 = "";

    private String CONTACT_ID = "";
    private String CONTACT_ID2 = "";

    static String PAYLOAD = FileReader.read("customer/WithContact.json");
    static String H5_PAYLOAD = FileReader.read("customer/WithContactH5.json");
    static String ORIGIN_PAYLOAD = PAYLOAD;

    private static String NAME = "";
    private static String PHONE = "";

    @Before
    public void setUp() throws Exception {
        CUSTOMER_ID = "";
        CUSTOMER_ID2 = "";

        CONTACT_ID = "";
        CONTACT_ID2 = "";

        clearNameAndPhone();

        PAYLOAD = ORIGIN_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                System.currentTimeMillis() + "");
        
    }

    private void clearNameAndPhone() {
        NAME = "";
        PHONE = "";
    }

    @After
    public void tearDown() throws Exception {
/*        if (CONTACT_ID != null && !CONTACT_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CONTACT_ID2 != null && !CONTACT_ID2.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Contact/" + CONTACT_ID2));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CUSTOMER_ID != null && !CUSTOMER_ID.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Customer/" + CUSTOMER_ID));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }

        if (CUSTOMER_ID2 != null && !CUSTOMER_ID2.isEmpty()) {
            HttpResponse response = delete(Csp.bo$dml.append("Customer/" + CUSTOMER_ID2));
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        }*/
    }

    private void addOneCustomerWithContact() throws Exception {
       PAYLOAD = ORIGIN_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                System.currentTimeMillis() + "");
        HttpResponse response = post(Web.customer$WithContact, PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID = contact.get("id").toString();

        NAME = customer.get("name").toString();
        PHONE = customer.get("phone").toString();
    }
    
/*    @Test
    public void addMutiCustomers() throws Exception{
    	for(int i=0;i<10000;i++){
    		addOneCustomerWithContact();
    	}    	
    }*/

    @Test
    public void testWithContact() throws Exception {
		addOneCustomerWithContact();
        HttpResponse response = post(Web.customer$WithContact, PAYLOAD);
        Map<String, Object> result;
        if (response.getStatusCode() == Status.OK.getStatusCode()) {
            result = this.parseObject(response.getString());
            System.out.println(response.getString());
            Map<String, Object> customer = (Map<String, Object>) result.get("customer");
            Map<String, Object> contact = (Map<String, Object>) result.get("contact");
            CUSTOMER_ID2 = customer.get("id").toString();
            CONTACT_ID2 = contact.get("id").toString();
          //  fail("save customer with contact duplicated check error");
        }

        assertEquals(response.getString(), Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue((result.containsKey("message")
                && result.get("message").toString().contains("app.customer.namephone.duplicated"))
                || (result.containsKey("errorId")
                        && result.get("errorId").toString().contains("app.customer.namephone.duplicated")));

    }

    @Test
    public void testDetail() throws Exception {
        addOneCustomerWithContact();
        HttpResponse response = getWithRegular2(Web.Customer.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
        response = get(Web.Customer.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("area") && result.get("area") != null);
        Map<String, Object> area = (Map<String, Object>) result.get("area");
        assertEquals(area.get("value"), "dongbei");
    }

    @Test
    public void testCombox() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$Combox.append("?param=1&pagesize=20&pageno=1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testDeleteByCondition() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("condtions", "{\"area\":[\"dongbei\"]}");
        data.put("reason", "testdelreason");

        HttpResponse response = post(Web.customer$DeleteByConditon, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("success").toString()) >= 1);
    }

    @Test
    public void testDeleteByIds() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("ids", Arrays.asList(CUSTOMER_ID));
        data.put("reason", "testdelreason");

        HttpResponse response = post(Web.customer$DeleteByIds, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("success").toString()) >= 1);
    }

    @Test
    public void testAttachmentSave() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("height", 2160);
        data.put("width", 3840);
        data.put("name", "butterfly1");
        data.put("suffix", "jpg");
        data.put("url", "http://sto.chanapp.chanjet.com/90003747057/2015/09/18/e18a1879364f4640a4ea5ebab86ea5fd.jpg");
        data.put("size", "6658067");

        HttpResponse response = post(Web.customer$AttamentSave.append("/" + CUSTOMER_ID), data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertEquals(data.get("height").toString(), result.get("height").toString());
        assertEquals(data.get("width").toString(), result.get("width").toString());
        assertEquals(data.get("name").toString(), result.get("name").toString());
        assertEquals(data.get("suffix").toString(), result.get("suffix").toString());
        assertEquals(data.get("url").toString(), result.get("url").toString());
        assertEquals(data.get("size").toString(), result.get("size").toString());
    }

    @Test
    public void testProcess() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$Process.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        // Map<String, Object> result =
        // this.parseObject(response.getString());
    }

    @Test
    public void testSortField() throws Exception {
        List<String> data = Arrays.asList("phone", "name", "address", "remark", "industry", "area", "derive", "type",
                "level", "fax", "url");

        HttpResponse response = post(Web.customer$sortField$Edit, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testCheckNameRepeat() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$CheckNameRepeat
                .append("?id=" + CUSTOMER_ID + "123&name=" + urlEncode(NAME) + "&phone=" + PHONE));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("repeat").toString()));
    }

    @Test
    public void testAttachments() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("height", 2160);
        data.put("width", 3840);
        data.put("name", "butterfly1");
        data.put("suffix", "jpg");
        data.put("url", "http://sto.chanapp.chanjet.com/90003747057/2015/09/18/e18a1879364f4640a4ea5ebab86ea5fd.jpg");
        data.put("size", "6658067");

        HttpResponse response = post(Web.customer$AttamentSave.append("/" + CUSTOMER_ID), data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertEquals(data.get("height").toString(), result.get("height").toString());
        assertEquals(data.get("width").toString(), result.get("width").toString());
        assertEquals(data.get("name").toString(), result.get("name").toString());
        assertEquals(data.get("suffix").toString(), result.get("suffix").toString());
        assertEquals(data.get("url").toString(), result.get("url").toString());
        assertEquals(data.get("size").toString(), result.get("size").toString());

        response = get(Web.customer$Attachments.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> list = this.parseObject(response.getString(), List.class);
        assertEquals(list.size(), 1);
        assertEquals("6658067", list.get(0).get("size").toString());
    }

    @Test
    public void testAttamentUploadOnly() throws Exception {
        addOneCustomerWithContact();
        // HttpResponse response = post(Web.customer$AttamentUploadOnly");
    }
    @TestVersions({"v4"})
    @Test
    public void testCustomerEdit() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("url", "www.chanjet888.com");
        data.put("id", CUSTOMER_ID);

        HttpResponse response = put(Web.Customer, data);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
   
        Map<String, Object> result = this.parseObject(response.getString());
        assertEquals(result.get("url").toString(), "www.chanjet888.com");

    }

    @Test
    public void testCustomerDelete() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = delete(Web.Customer.append("/"+CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    }

    @Test
    public void testAttamentDelete() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("height", 2160);
        data.put("width", 3840);
        data.put("name", "butterfly1");
        data.put("suffix", "jpg");
        data.put("url", "http://sto.chanapp.chanjet.com/90003747057/2015/09/18/e18a1879364f4640a4ea5ebab86ea5fd.jpg");
        data.put("size", "6658067");

        HttpResponse response = post(Web.customer$AttamentSave.append("/" + CUSTOMER_ID), data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertEquals(data.get("height").toString(), result.get("height").toString());
        assertEquals(data.get("width").toString(), result.get("width").toString());
        assertEquals(data.get("name").toString(), result.get("name").toString());
        assertEquals(data.get("suffix").toString(), result.get("suffix").toString());
        assertEquals(data.get("url").toString(), result.get("url").toString());
        assertEquals(data.get("size").toString(), result.get("size").toString());

        response = get(Web.customer$Attachments.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> list = this.parseObject(response.getString(), List.class);
        assertTrue(list.size() >= 1);
        assertEquals("6658067", list.get(0).get("size").toString());

        response = delete(Web.customer$Attament.append("/" + list.get(0).get("id").toString()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testSharedGuide() throws Exception {
        HttpResponse response = get(Web.customer$SharedGuide);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testCondition() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> queryValue = new HashMap<String, Object>();
        //queryValue.put("owner", Arrays.asList(getAppManagerId()));
        HttpResponse response = get(Web.customer$Condition
                .append("?queryValue=" + urlEncode(this.toJSONString(queryValue)) + "&pageno=1&pagesize=20"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 1);
    }

    @Test
    public void testFollow() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$Follow.append("?customerId=" + CUSTOMER_ID + "&isFollow=true"));
        assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());

        response = get(Web.customer$Follow.append("?customerId=" + CUSTOMER_ID + "&isFollow=false"));
        assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testShare() throws Exception {
        addOneCustomerWithContact();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("sharedUserIds", Arrays.asList(this.getRegularUserId()));
        data.put("customerIds", Arrays.asList(CUSTOMER_ID));
        data.put("privileges", Arrays.asList("SELECT"));

        HttpResponse response = post(Web.customer$Share, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUnShare() throws Exception {
        addOneCustomerWithContact();

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("sharedUserIds", Arrays.asList(this.getRegularUserId()));
        data.put("customerIds", Arrays.asList(CUSTOMER_ID));
        data.put("privileges", Arrays.asList("SELECT"));

        HttpResponse response = post(Web.customer$UnShare, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testUnShareList() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$UnShareList.append("?customerId=" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }

    @Test
    public void testSharedList() throws Exception {
        addOneCustomerWithContact();

        HttpResponse response = get(Web.customer$SharedList.append("/" + CUSTOMER_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }
    
    @TestVersions({"v4"})
    @Test
    public void testH5WithContact() throws Exception{
       String tempPayload = H5_PAYLOAD.replaceAll("6626666", System.currentTimeMillis() + "").replaceAll("13166666666",
                System.currentTimeMillis() + "");
        HttpResponse response = this.doPost("appManager", "/restlet/mobile/Customer", tempPayload);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        System.out.println(response.getString());
/*        Map<String, Object> result = this.parseObject(response.getString());
        Map<String, Object> customer = (Map<String, Object>) result.get("customer");
        Map<String, Object> contact = (Map<String, Object>) result.get("contact");
        CUSTOMER_ID = customer.get("id").toString();
        CONTACT_ID = contact.get("id").toString();
        NAME = customer.get("name").toString();
        PHONE = customer.get("phone").toString();*/
    
    }
    
    @TestVersions({"v4"})
    @Test
    public void testH5Delete() throws Exception{
        addOneCustomerWithContact();
        HttpResponse response = this.doDelete("appManager", "/restlet/mobile/Customer/"+CUSTOMER_ID, null);      
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));
    
    }
    
    @TestVersions({"v4"})
    @Test
    public void testH5Upate() throws Exception{
        addOneCustomerWithContact();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("url", "www.chanjet888.com");
        data.put("id", CUSTOMER_ID);
        HttpResponse response = this.doPut("appManager", "/restlet/mobile/Customer/"+CUSTOMER_ID, data);  
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
       // assertTrue(Boolean.valueOf(result.get("success").toString()));
    
    }
    
    @Test
    public void testH5QueryList() throws Exception{
		HttpResponse response =  this.doGet("appManager", "/restlet/mobile/Customer?first=0&max=20&keyWord=测试",null);
		assertEquals(200, response.getStatusCode());
		String body = response.getString();
		System.out.println(body);
    }
    
    @Test
    public void testH5QueryDetail() throws Exception{
    	addOneCustomerWithContact();
		HttpResponse response =  this.doGet("appManager", "/restlet/mobile/Customer/"+CUSTOMER_ID,null);
		assertEquals(200, response.getStatusCode());
		String body = response.getString();
		//System.out.println(body);
    }
    
	@Test
	public void testNearbyCustomers() throws Exception{
		//Double latitude,Double longitude
		///NearbyCustomer?latitude=40.067496&longitude=116.236093&first=0&max=25
		//testAddCheckIn();
		HttpResponse response =  this.doGet("appManager","/restlet/mobile/checkin/NearbyCustomers?first=0&max=20&latitude=40.067462&longitude=116.236256",null);
		assertEquals(200, response.getStatusCode());
		String body = response.getString();
		System.out.println(body);
	}
}
