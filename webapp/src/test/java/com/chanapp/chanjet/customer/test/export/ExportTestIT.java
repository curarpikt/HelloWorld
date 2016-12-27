package com.chanapp.chanjet.customer.test.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;

public class ExportTestIT extends RestletBaseTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSaveTask() throws Exception {
        HttpResponse response = get(Web.export$SaveTask);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        boolean saved = Boolean.valueOf(result.get("result").toString());
        if (!saved) {
            response = get(Web.export$TaskList.append("?pageno=0&pagesize=1"));
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            result = this.parseObject(response.getString());
            List<Map<String, Object>> tasks = (List<Map<String, Object>>) result.get("items");
            if (tasks.size() == 0) {
                return;
            }
            result = tasks.get(0);
        }

        String TASK_ID = result.get("id").toString();

        response = get(Web.export$GetExportTasksToday);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Web.export$TaskList.append("?pageno=0&pagesize=1"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        // List<Map<String, Object>> tasks = (List<Map<String, Object>>)
        // result.get("items");

        response = get(Web.export$GetExportTaskById.append("?id=" + TASK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(result.containsKey("result"));

        response = get(Web.export$ExecuteTaskById.append("?id=" + TASK_ID));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        int status = Integer.valueOf(result.get("taskStatus").toString());
        assertTrue(status >= 1);

        response = get(Web.export$GetExportCount);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));

        response = get(Web.export$Download.append("?id=" + TASK_ID));
        assertTrue(Status.OK.getStatusCode() == response.getStatusCode()
                || Status.NO_CONTENT.getStatusCode() == response.getStatusCode());
    }

    @Test
    public void testVisitCountExport() throws Exception {
        HttpResponse response = get(
                Web.export$VisitCountExport.append("?userId=" + this.getAppManagerId() + "&countDate=2016-04"));
        assertTrue(Status.OK.getStatusCode() == response.getStatusCode()
                || Status.NO_CONTENT.getStatusCode() == response.getStatusCode());
    }

    @Test
    public void testAttendanceCountExport() throws Exception {
        HttpResponse response = get(
                Web.export$AttendanceCountExport.append("?userId=" + this.getAppManagerId() + "&countDate=2016-04"));
        assertTrue(Status.OK.getStatusCode() == response.getStatusCode()
                || Status.NO_CONTENT.getStatusCode() == response.getStatusCode());
    }
}
