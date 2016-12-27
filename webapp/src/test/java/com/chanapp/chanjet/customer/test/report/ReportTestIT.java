package com.chanapp.chanjet.customer.test.report;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Web;
import com.chanjet.csp.platform.test.HttpResponse;
@TestVersions({"v4"})
@SuppressWarnings("unchecked")
public class ReportTestIT extends RestletBaseTest {
    @Test
    public void testEnums() throws Exception {
        HttpResponse response = get(Web.report$Enums);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertTrue(result.size() > 0);
    }

    @Test
    public void testAddCustomersAnalysis() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("start_date", "2016-01-04 00:00:00");
        data.put("end_date", "2016-01-05 23:59:59");
        HttpResponse response = post(Web.report$AddCustomersAnalysis, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 0);
    }

    @Test
    public void testAnalysisdata() throws Exception {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("enumName", "industry");
        HttpResponse response = post(Web.report$Analysisdata, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 0);
    }

    @Test
    public void testProgressCount() throws Exception {
        HttpResponse response = get(Web.report$ProgressCount.append(
                "?countType=WEEK&userId=" + this.getAppManagerId() + "&startDate=1433088000000&endDate=1434556800000"));       
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        System.out.println(response.getString());
        assertTrue(Integer.valueOf(result.get("total").toString()) >= 0);
    }

    @Test
    public void testCheckinVisitCount() throws Exception {
        HttpResponse response = get(Web.report$CheckinVisitCount
                .append("?userId=" + this.getAppManagerId() + "&countDate=2016-08&countType=week"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("currentTime"));
    }

    @Test
    public void testCustomerProgress() throws Exception {
        HttpResponse response = get(Web.report$CustomerProgress);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        System.out.println(response.getString());
        assertTrue(result.containsKey("userGroup"));
    }

    @Test
    public void testWorkrecordProgress() throws Exception {
        HttpResponse response = get(Web.report$WorkrecordProgress.append("?countType=WEEK"));        
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        System.out.println(response.getString());
        assertTrue(result.containsKey("userGroup"));
    }

    @Test
    public void testAttendanceCountCsv() throws Exception {
        HttpResponse response = get(
                Web.report$AttendanceCountCsv.append("?userId=" + this.getAppManagerId() + "&countDate=2016-01"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(result.containsKey("currentTime"));
    }

    @Test
    public void testCheckinVisitDetail() throws Exception {
        HttpResponse response = get(Web.report$CheckinVisitDetail
                .append("?userId=" + this.getAppManagerId() + "&startDate=1433088000000&endDate=1434556800000"));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        List<Map<String, Object>> result = this.parseObject(response.getString(), List.class);
        assertNotNull(result);
    }
}
