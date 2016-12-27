package com.chanapp.chanjet.customer.test.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.test.Csp;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

import net.bytebuddy.utility.RandomString;

public class MetaDataTestIT extends RestletBaseTest {
    static String TEXT_FIELD_PAYLOAD = FileReader.read("metadata/MetaData.json");
    static String ENUM_FIELD_PAYLOAD = FileReader.read("metadata/MetaData3.json");
    static String UPDATE_ENUM_FIELD_PAYLOAD = FileReader.read("metadata/MetaData2.json");

    @Test
    public void testGetMetaData() throws Exception {
        HttpResponse response = get(Web.dynattr$Metadata);
        Map<String, Object> result = this.parseObject(response.getString());
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

 
    @Test
    public void testAddCustomerTextField() {
        try {
        	String fieldLable = "BBBB"+RandomString.make();
          //  String select ="?$select=fields/"+fieldLable+"/name";
        	Map textField = new HashMap();
        	textField.put("fieldLabel","测试文本1"+RandomString.make());
        	textField.put("entityName", "Customer");
        	//textField.put("hidden", false);
        	textField.put("fieldType", "Text");
            HttpResponse response = post(Web.dynattr$Save, JSON.toJSON(textField));
            //Map<String, Object> result = this.parseObject(response.getString());
     
//            String BOproperty = getBOProperty(select);
//            JSONObject data = JSON.parseObject(BOproperty);
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testAddCustomerEnumField() {
        try {
        	String temp = ENUM_FIELD_PAYLOAD.replace("Field1458782605574", "Field145878260"+RandomString.make());
        	temp =temp.replace("Enum1458782605937", "Enum1458782"+RandomString.make());
        	temp =temp.replace("enumField2", "enumField2"+RandomString.make());     	
        	HttpResponse response = post(Web.dynattr$Save, temp);
            Map<String, Object> result = this.parseObject(response.getString());
            System.out.println(response.getString());
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @TestVersions({"v4"})
    @Test
    public void testUpdateCustomerEnumField() {
        try {
            HttpResponse response = post(Web.dynattr$Update, UPDATE_ENUM_FIELD_PAYLOAD);
            Map<String, Object> result = this.parseObject(response.getString());
            System.out.println(response.getString());
            assertEquals(response.getString(), Status.NO_CONTENT.getStatusCode(), response.getStatusCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @TestVersions({"v4"})
    @Test
    public void testDisableField() {
        try {
            String jsondata = "{\"Customer\":[\"address\"]}";
            HttpResponse response = post(Web.dynattr$Disable, jsondata);
            Map<String, Object> result = this.parseObject(response.getString());
            String select ="?$select=fields/address/(name,properties)";
            String BOproperty = getBOProperty(select);
            JSONObject data = JSON.parseObject(BOproperty);
            System.out.println(BOproperty);
            Object check = data.getJSONObject("fields").getJSONObject("address").getJSONObject("properties").get("disabled");
            assertEquals(check,"true");
            // System.out.println(response.getString());        
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void testEnableField() {
        try {
            String jsondata = "{\"Customer\":[\"address\"]}";
            HttpResponse response = post(Web.dynattr$Enable, jsondata);
            Map<String, Object> result = this.parseObject(response.getString());
            String select ="?$select=fields/address/(name,properties)";
            String BOproperty = getBOProperty(select);
            JSONObject data = JSON.parseObject(BOproperty);
            System.out.println(BOproperty);
            Object check = data.getJSONObject("fields").getJSONObject("address").getJSONObject("properties").get("disabled");
            assertEquals(check,"false");
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SuppressWarnings("rawtypes")
    @TestVersions({"v4"})
    @Test
    public void testSetFieldPattern() throws Exception {

        Map para = new HashMap();
        Map patternMap = new HashMap();
        patternMap.put("noBlank", false);
        para.put("pattern", JSON.toJSONString(patternMap));
        para.put("fieldName", "phone");
        para.put("entityName", "Customer");
        String patternStr = JSON.toJSONString(para);
      //  {"pattern":{"noBlank":false},"fieldName":"phone","entityName":"Customer"}
     //   {entityName: "Customer", fieldName: "industry", pattern: "{"noBlank":true}"}
        System.out.println(patternStr);
        HttpResponse response = post(Web.dynattr$SetFieldPattern, patternStr);
        Map<String, Object> result = this.parseObject(response.getString());
        // http://localhost:8080/cspdemo/testapp/services/1.0/metadata/field/Contact/name
        HttpResponse metaResponse = get(Csp.metadata$field.append("Customer/phone"));
        Map<String, Object> metaResult = this.parseObject(metaResponse.getString());
        Map properties = (Map) metaResult.get("properties");
        Map pattern = this.parseObject((String) properties.get("fieldPattern"), Map.class);
/*        if (!pattern.containsKey("noBlank") || !pattern.get("noBlank").equals(false)) {
            fail("set property failed");
        }*/
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void testEnums() throws Exception {
        HttpResponse response = get(Web.dynattr$Enums);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }
    
    @Test
    public void testGetBOProperty() throws Exception{
	     HttpResponse response = this.doGet("appManager", "/services/1.0/metadata/bo/CustomerBO?$select=fields/Field1466491662435/name", null);
	  	//HttpResponse response = this.doGet("appManager", "/services/1.0/metadata/bo/Customer", null);
        System.out.println(response.getString());
    }
    
    
   public String getBOProperty(String select) throws Exception{
	      HttpResponse response = this.doGet("appManager", "/services/1.0/metadata/bo/CustomerBO"+select, null);
    	//HttpResponse response = this.doGet("appManager", "/services/1.0/metadata/bo/Customer", null);
    	String body = response.getString();
    	return body;	
   }

   
	@Test
	public void testBOQuery() throws Exception{
		//getMetadataDisable(); 
		HttpResponse response =  this.doGet("appManager","/services/1.0/metadata/bo/WorkRecordBO",null);	
		String body = response.getString();
		System.out.println(body);
	}
}