package com.chanapp.chanjet.customer.test.recycle.recyclableBin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class WeakReferenceTest extends RecyclableBinTest {
	private Long workRecordId;
	private Long commentId;
    
	@Before
	public void setUp() throws Exception {
		createTestData();
	}

	private void createTestData() throws Exception {
		HttpResponse response = post(Web.customer$WithContact,
				CUSTOMER_PAYLOAD.replaceAll("6626666",  System.currentTimeMillis() + "")
								.replaceAll("13166666666", System.currentTimeMillis() + ""));

		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
		Map<String, Object> result = this.parseObject(response.getString());
		Map<String, Object> customer = (Map<String, Object>) result.get("customer");
		Long customerId = Long.valueOf(customer.get("id").toString());	

		response = post(Web.WorkRecord, 
				WORKRECORD_PAYLOAD.replace("100001", customerId.toString())
								  .replace("-1442818200499", "-" + System.currentTimeMillis()));
		assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
		result = this.parseObject(response.getString());
		workRecordId = Long.valueOf(result.get("id").toString());

		response = post(Web.Comment, 
				COMMENT_PAYLOAD.replace("100001", workRecordId.toString()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        commentId = Long.valueOf(result.get("id").toString());		
	}
	
	@Test
	public void shouldTransferFrom11To01() throws Exception {
		// When: drop the workRecord to recyclable bin
		Map<String, Long> ids = dropToRecyclableBin(WORK_RECORD_BO, workRecordId);
		
		// Then: 
		// 1. check the workRecord has been dropped to recyclable bin
		Long recyclableId = ids.get("recyclableId");
		assertNotNull(recyclableId);
		// 2. check the relation has been saved in recyclable bin
		Long recyclableRelationId = ids.get("relationId");
		assertNotNull(recyclableRelationId);

		// 3. check the workRecord has been deleted 
		boolean workRecordExisted = checkExisted(WORK_RECORD_BO, workRecordId);
		assertEquals(false, workRecordExisted);
	}
	
	@Test
	public void shouldTransferFrom01To11() throws Exception {
		// Given: drop the workRecord to recyclable bin 
		Map<String, Long> recyclableWorkRecordIds = dropToRecyclableBin(WORK_RECORD_BO, workRecordId);
		Long recyclableWorkRecordId = recyclableWorkRecordIds.get("recyclableId");
		assertNotNull(recyclableWorkRecordId);
		Long recyclableRelationId = recyclableWorkRecordIds.get("relationId");
		assertNotNull(recyclableRelationId);
		
		// When: recycle workRecord
		Long recycledWorkRecordId = recycle(recyclableWorkRecordId);
        assertNotNull(recycledWorkRecordId);
        
        // Then:
        // 1. check workRecord has been deleted from recyclable bin
        boolean existingRecyclabeWorkRecord = checkExistedInRecyclableBin(recyclableWorkRecordId);
        assertEquals(false, existingRecyclabeWorkRecord);
        
        // 2. check relation has been deleted from recyclable bin
        boolean existingRecyclableRelation = checkeRelationExistedInRecyclableBin(recyclableRelationId);
        assertEquals(false, existingRecyclableRelation);
        
        // 3. check workRecord has been recycled  
        boolean existingRecycledWorkRecord = checkExisted(WORK_RECORD_BO, recycledWorkRecordId);
        assertTrue(existingRecycledWorkRecord);
        
        // 4. check comment has been related to workRecord
        boolean existingWorkRecordOfComment = checkCommentOfWorkRecordExisted(recycledWorkRecordId);
        assertTrue(existingWorkRecordOfComment);      
	}

	private boolean checkCommentOfWorkRecordExisted(Long workRecordId) throws Exception {		
        HttpResponse response = get(Web.comment$ListByWorkRecord.append("/" + workRecordId));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
		return (Integer.valueOf(result.get("total").toString()) >= 1) ? true : false;
	}

    
}
