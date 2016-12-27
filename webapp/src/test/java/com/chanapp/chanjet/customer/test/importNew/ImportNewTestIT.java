package com.chanapp.chanjet.customer.test.importNew;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class ImportNewTestIT extends RestletBaseTest {

    static String IMPORT_CUSTOMER_PAYLOAD = FileReader
            .read("importNew/ImportCustomer.json");

/*    @Test
    public void testText() throws Exception {
        HttpResponse response = post(Web.importNew$Text.append("?sheetType=0"), IMPORT_CUSTOMER_PAYLOAD);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }*/

    @Test
    public void testExcelTemplate() throws Exception {
        HttpResponse response = get(Web.importNew$excel$Template);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpload() throws Exception {
        HttpResponse response;
        Map<String, Object> result;

        try {
            changeUploadFileFormNameTo("Filedata");
            response = upload(Web.importNew$Upload,
                    "importNew/template" + (isV3() ? "_v3" : "") + ".xls");
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            result = this.parseObject(response.getString());
            assertTrue(Boolean.valueOf(result.get("result").toString()));
        } finally {
            changeUploadFileFormNameTo("file1");
        }

        String TASK_ID = result.get("id").toString();
        response = get(Web.importNew$Task.append("?id=" + TASK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Web.importNew$records$Status.append("?ids=" + TASK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Web.importNew$Records);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 0);

        response = get(Web.importNew$record$Head.append("/" + TASK_ID + "?sheetType=0"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(
                Web.importNew$record$Details.append("?parentId=" + TASK_ID + "&sheetType=0&pageno=0&pagesize=10"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        List<Map<String, Object>> details = (List<Map<String, Object>>) result.get("items");
        if (details.size() > 0) {
            response = delete(Web.importNew$record$detail$Remove.append("/" + details.get(0).get("id")));
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            result = this.parseObject(response.getString());
            assertTrue(Boolean.valueOf(result.get("result").toString()));
        }

/*        response = delete(Web.importNew$record$Remove.append("/" + TASK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = delete(Web.importNew$records$Clean);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));*/
    }

}
