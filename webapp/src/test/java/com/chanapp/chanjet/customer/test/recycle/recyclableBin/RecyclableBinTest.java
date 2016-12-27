package com.chanapp.chanjet.customer.test.recycle.recyclableBin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
import com.google.common.base.Joiner;

public class RecyclableBinTest extends RestletBaseTest {

	protected static final String CUSTOMER_BO = "Customer";
	protected static final String CONTACT_BO = "Contact";
	protected static final String WORK_RECORD_BO = "WorkRecord";
	protected static final String CUSTOMER_PAYLOAD = FileReader.read("customer/WithContact.json");
	protected static final String WORKRECORD_PAYLOAD = FileReader.read("workrecord/WorkRecord.json");	   
	protected static final String COMMENT_PAYLOAD = FileReader.read("comment/Comment.json");
	
	public Map<String, Long> dropToRecyclableBin(String boName, Long objectId) throws Exception {
		JSONObject params = new JSONObject();
		params.put("boName", boName);
		params.put("objectId", objectId);
		params.put("batch", false);
		HttpResponse response = post(Web.RecyclableBin, params);		
		Map<String, Object> result = this.parseObject(response.getString());
		Long recyclableId = Long.valueOf(result.get("recyclableId").toString());
		assertNotNull(recyclableId);
		
		JSONArray relationIds = (JSONArray) result.get("relationIds");		
		Long relationId = null;
		if (relationIds != null && !relationIds.isEmpty()) {
			relationId = Long.valueOf(relationIds.get(0).toString());
		}		
		
		Map<String, Long> ids = new HashMap<>();
		ids.put("recyclableId", recyclableId);
		ids.put("relationId", relationId);
		return ids;
	}
	
	public Map<String, Object> batchDropToRecyclableBin(String boName, List<Long> objectIds) throws Exception {
		JSONObject params = new JSONObject();
		params.put("boName", boName);
		params.put("objectIds", objectIds);
		params.put("reason", "Not used");
		params.put("batch", true);
		HttpResponse response = post(Web.RecyclableBin, params);		
		Map<String, Object> result = this.parseObject(response.getString());		
		return result;
	}
	
	public Long recycle(Long recyclableId) throws Exception {
		JSONObject params = new JSONObject();
		params.put("recyclableId", recyclableId);
		HttpResponse response = put(Web.RecyclableBin, params);
		//assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());		
			     
	    Map<String, Object> result = this.parseObject(response.getString());
	    JSONArray recyclableIds = (JSONArray) result.get("resultObj");
	    Long recycledId = recyclableIds.getLongValue(0);
		return recycledId;
	}	
	
	public JSONArray batchRecycle(Long recyclableId) throws Exception {
		JSONObject params = new JSONObject();
		params.put("recyclableId", recyclableId);
		HttpResponse response = put(Web.RecyclableBin, params);
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());		
			     
	    Map<String, Object> result = this.parseObject(response.getString());
	    JSONArray recycledIds =  (JSONArray) result.get("resultObj");
		return recycledIds;		
	}
	
	public boolean checkExisted(String boName, Long objectId) throws Exception {
		Url path = null;
		if (boName.equals(CONTACT_BO)) {
			path = Web.Contact;
		} else if (boName.equals(CUSTOMER_BO)) {
			path = Web.Customer;
		} else if (boName.equals(WORK_RECORD_BO)) {
			path = Web.WorkRecord;
		}
		HttpResponse response = get(path.append("/" + objectId));		
		// assertEquals(response.getString(), Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
		Map<String, Object> result = this.parseObject(response.getString());
		if (result.get("errorId") == null) {
			return true;
		} else {
			assertNotNull(result.get("errorMessage"));
			return false;
		}		
	}
	
	public boolean checkExistedInRecyclableBin(Long recyclableId) throws Exception {
		HttpResponse response = get(Web.RecyclableBin.append("?recyclableId=" + recyclableId));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());	
		Map<String, Object> result = this.parseObject(response.getString());
		Map<String, Object> theRecyclabe = (Map<String, Object>) result.get("recyclable");
		return (theRecyclabe == null || theRecyclabe.isEmpty()) ? false : true;
	}

	public boolean checkeRelationExistedInRecyclableBin(Long recyclableRelationId) throws Exception {
		HttpResponse response = get(Web.RecyclableBin.append("?relationId=" + recyclableRelationId));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());		
			     
	    Map<String, Object> result = this.parseObject(response.getString());
	    Map<String, Object> theRelation = (Map<String, Object>) result.get("relation");
		return (theRelation == null || theRelation.isEmpty()) ? false : true;		
	}
	
	public Map<String, Object> deleteFromRecyclableBin(List<Long> recyclableIds) throws Exception {
		String batchRecyclableIds = Joiner.on(',').join(recyclableIds);		
	
		HttpResponse response = delete(Web.RecyclableBin.append("?recyclableIds=" + batchRecyclableIds));		
		Map<String, Object> result = this.parseObject(response.getString());		
		return result;
	}

}
