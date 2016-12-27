package com.chanapp.chanjet.customer.test.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.Test;

import com.chanapp.chanjet.customer.test.Rest;
import com.chanapp.chanjet.customer.test.RestletBaseTest;
import com.chanapp.chanjet.customer.test.Status;
import com.chanjet.csp.platform.test.HttpResponse;

public class UserMobileTestIT extends RestletBaseTest {

    @Test
    public void testAtUser() throws Exception {
        HttpResponse response = get(Rest.user$AtUser.append("?keyWord=" + urlEncode("1")));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }

    @Test
    public void testUserInfo() throws Exception {
        HttpResponse response = get(Rest.user$UserInfo.append("?name=" + urlEncode(isV3() ? "通天塔" : "newdemo")
                + "&headPic=" + urlEncode("http://store.chanjet.com/release/imgs/homePage/k04.jpg")));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("success").toString()));

    }

    @Test
    public void testBindingMobile() throws Exception {
        HttpResponse response = get(Rest.user$BindingMobile.append("?mobile=17001098084&activeCode=123&pwd=121212"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertFalse(Boolean.valueOf(result.get("result").toString()));
        assertEquals(result.get("errorCode").toString(), "20006");// 激活码不存在
    }

    @Test
    public void testBindingMobileExists() throws Exception {
        HttpResponse response = get(Rest.user$BindingMobileExists.append("?mobile=17001098084"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertEquals(result.get("exists").toString(), "1");
    }

    @Test
    public void testBindingOrgPartner() throws Exception {
        HttpResponse response = get(Rest.user$BindingOrgPartner.append("?orgId=17001098084&partnerId=123"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertFalse(Boolean.valueOf(result.get("result").toString()));
        assertEquals(result.get("errorCode").toString(), "20123");// 用户不在组织的企业客户身份中
    }

    @Test
    public void testGetBindingMobile() throws Exception {
        HttpResponse response = get(Rest.user$GetBindingMobile);
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
    }

    @Test
    public void testGetPartnerInfoById() throws Exception {
        HttpResponse response = get(Rest.user$GetPartnerInfoById.append("?partnerId=123"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertFalse(Boolean.valueOf(result.get("result").toString()));
        assertEquals(result.get("errorCode").toString(), "20221");// 组织不是服务商类型
    }

/*    @Test
    public void testHeadPicture() throws Exception {
        HttpResponse response = upload(Rest.user$HeadPicture, "user/aaa.jpg");
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());

    }*/

    @Test
    public void testSendBindingMobileMsg() throws Exception {
        HttpResponse response = get(Rest.user$SendBindingMobileMsg.append("?mobile=17001098084"));
        System.out.println(response.getString());
        assertEquals(response.getString(), Status.OK.getStatusCode(), response.getStatusCode());
        Map<String, Object> result = this.parseObject(response.getString());
        assertTrue(Boolean.valueOf(result.get("result").toString()));
        assertEquals(result.get("mobile").toString(), "17001098084");
    }
}
