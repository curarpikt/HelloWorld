package com.chanapp.chanjet.customer.test.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.test.FileReader;
import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanapp.chanjet.customer.test.TestVersions;
import com.chanapp.chanjet.customer.test.Url;
import com.chanapp.chanjet.customer.test.Web;
import com.chanapp.chanjet.customer.vo.UserValue;
import com.chanjet.csp.platform.test.HttpResponse;
@TestVersions({"v3"})
public class UserTestIT extends RestletBaseTest {
	 static String SAVE_HIERARCHYUSERS_PAYLOAD = FileReader.read("user/SaveHierarchyUsers.json");

	@Before
	public void setUp(){
        HttpResponse response;
        Map<String, Object> result;
        try {
            // 先初始化普通用户
            /*
             * response = getWithUser("testRegular", "/v2/web/Init"); result =
             * this.parseObject(response.getString());
             * assertEquals(response.getString(),
             * Status.BAD_REQUEST.getStatusCode(), response.getStatusCode());
             * result = this.parseObject(response.getString());
             * assertTrue(result.containsKey("errorId") &&
             * result.get("errorId").toString().contains(
             * "app.appuser.superuser.noexits"));
             */

           // 初始化应用管理员
            response = get(Web.Init);
            result = this.parseObject(response.getString());
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            assertTrue(result.containsKey("success") && Boolean.parseBoolean(result.get("success").toString()));
            /* 
            //系统管理员
            response = getWithEntManager(Web.Init);
            result = this.parseObject(response.getString());
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            assertTrue(result.containsKey("success") && Boolean.parseBoolean(result.get("success").toString()));
            // 初始化普通用户
            response = getWithRegular(Web.Init);
        //    result = this.parseObject(response.getString());
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
          //  assertTrue(result.containsKey("success") && Boolean.parseBoolean(result.get("success").toString()));
            
           // 初始化普通用户
            response = getWithRegular2(Web.Init);
            result = this.parseObject(response.getString());
            assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
            assertTrue(result.containsKey("success") && Boolean.parseBoolean(result.get("success").toString()));*/

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
	}
    @Test

    public void testAlltranusers() throws Exception {
        HttpResponse response = get(Web.sysreluser$Alltranusers.append("?userId=" + this.getAppManagerId()));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testAllUsers() throws Exception {
        HttpResponse response = get(Rest.sysreluser$Allusers.append("?pageno=1&pagesize=10&status=enable"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void testAllSubordinate() throws Exception {
        HttpResponse response = get(Web.sysreluser$AllSubordinate);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void testGetHierarchyUsers() throws Exception {
        HttpResponse response = get(Web.sysreluser$GetHierarchyUsers);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testGetOrgBoss() throws Exception {
        HttpResponse response = get(Web.sysreluser$GetOrgBoss);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void getGetUserGroups() throws Exception {
        HttpResponse response = get(Web.sysreluser$GetUserGroups);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void getGetHierarchyUsers2Tree() throws Exception {
        HttpResponse response = get(
                Web.sysreluser$GetHierarchyUsers2Tree.append("?monthStart=2015-07&monthEnd=2015-07&bizType=KQ"));
        // monthStart=2015-07&monthEnd=2015-07&bizType=KQ
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }
    
    @TestVersions({"v3"})
    @Test
    public void testDisableUser() throws Exception {
        HttpResponse response = get(Rest.sysreluser$Disable.append("?userId=" + this.getRegularUserId()));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> data = new HashMap<String, Object>();
        
        data.put("account", "suming_mail12@126.com");
        response = post(Web.invite$Send, data);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testSaveHierarchyUsers() throws Exception {
        HttpResponse response = get(Web.sysreluser$GetHierarchyUsers);
        String paraStr = response.getString();    
    	response = post(Web.sysreluser$SaveHierarchyUsers, paraStr);
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }

    @Test
    public void testChangeBoss() throws Exception {
    	//切换BOSS
        HttpResponse response = get(Web.sysreluser$ChangeBoss.append("?bossId=" + this.getRegularUserId()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        //切换回来
    	response = getWithRegular(Web.sysreluser$ChangeBoss.append("?bossId=" + this.getAppManagerId()));
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }
    
    @Test
    public void testGetHierarchyUsersByUserId() throws Exception {
        HttpResponse response = get(Web.sysreluser$GetHierarchyUsersByUserId.append("?userId="+this.getAppManagerId()));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
    }
    
    @Test
    public void testChangeBossByCia() throws Exception{
    	HttpResponse response = doGet("appManager", "restlet/test/TestChangeBossByCIA?userId="+this.getAppManagerId(), null);
        
    	
    }
}
