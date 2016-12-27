package com.chanapp.chanjet.customer.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.message.MessageServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.data.api.DataManager;

public class MsgUtil {
    public static Map<String, String> msgMap = new HashMap<String, String>();

    static {
        msgMap.put("10001", "格式错误");
        msgMap.put("10002", "最小长度错误");
        msgMap.put("10003", "超过最大长度限制，请修改");
        msgMap.put("10004", "非空参数为空的错误");
        msgMap.put("10005", "JSON参数格式错误");
        msgMap.put("10006", "APPKEY 错误");
        msgMap.put("10007", "APPSECRET错误");

        msgMap.put("20001", "注册来源不存在");
        msgMap.put("20002", "邮件发送异常");
        msgMap.put("20003", "短信发送异常");
        msgMap.put("20004", "激活记录不存在");
        msgMap.put("20005", "激活记录已经被激活了");
        msgMap.put("20006", "激活码不存在");
        msgMap.put("20007", "系统错误");
        msgMap.put("20008", "系统错误");
        msgMap.put("20009", "注册与激活的方式不匹配，如邮箱注册使用手机激活");
        msgMap.put("20010", "注册码无效");
        msgMap.put("20011", "系统错误");
        msgMap.put("20012", "查询的开始参数无效");
        msgMap.put("20013", "查询的长度限制参数无效");
        msgMap.put("20024", "验证错误次数超过上线");

        msgMap.put("20103", "邮箱被占用");
        msgMap.put("20104", "手机号被占用");
        msgMap.put("20105", "组织指定身份不存在");
        msgMap.put("20106", "用户不存在");

        msgMap.put("20110", "用户无所属组织");
        msgMap.put("20111", "用户已经绑定邮箱，无法修改");
        msgMap.put("20112", "用户邮箱已绑定");
        msgMap.put("20113", "用户手机已绑定");

        msgMap.put("20114", "用户邮箱未绑定");
        msgMap.put("20115", "用户手机号未绑定");
        msgMap.put("20122", "用户昵称被占用");
        msgMap.put("20123", "用户不在组织的企业客户身份中");
        msgMap.put("20204", "组织不存在");
        msgMap.put("20221", "组织不是服务商类型");
        msgMap.put("20241", "企业在指定应用中已经绑定了服务商");
        msgMap.put("20242", "服务商不存在");

        msgMap.put("20305", "appKey没有访问权限");
        msgMap.put("20311", "用户订阅授权已经存在");
        msgMap.put("20906", "用户不是第三方用户");

        msgMap.put("25030", "组织认证信息无效");
        msgMap.put("25031", "用户认证信息无效");
        msgMap.put("25032", "授权码没有对应的CIC值");
        msgMap.put("25033", "授权信息已经过期");
        msgMap.put("25011", "不支持的授权类型");
        msgMap.put("25012", "访问拒绝");
        msgMap.put("25013", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25014", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25015", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25016", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25017", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25018", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25019", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25020", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25021", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25022", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25023", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25024", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25025", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25026", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25027", "您当前操作的页面已过期，请重新登录。");
        msgMap.put("25028", "认证码与客户端不匹配");
        msgMap.put("25003", "认证码不存在");
        msgMap.put("25005", "无效的客户端凭据");

        msgMap.put("50000", "系统错误");
    }

    public static String getMsg(String code) {
        if (msgMap.containsKey(code)) {
            return msgMap.get(code);
        }
        return "";
    }
    public static void sengMsg(String operTag, String alert, Long userId, String type){
    	sengMsg( operTag,  alert,  userId,  type ,null);
    }

    public static void sengMsg(String operTag, String alert, Long userId, String type,String targetId) {
        Logger logger = LoggerFactory.getLogger(MsgUtil.class);
        DataManager dataManager = AppWorkManager.getDataManager();
        try {
            String orgId = PortalUtil.getOrgId();
            String orgName = PortalUtil.getOrgNameById(orgId);
            if (StringUtils.isNotEmpty(orgId)) {
                ArrayList<Long> ids = new ArrayList<Long>();
                ids.add(userId);
                String token = EnterpriseContext.getToken();
                long currUserId = EnterpriseContext.getCurrentUser().getUserLongId();
                String from = PushMsg.getFrom(type);
                Map<String, Object> aps = new HashMap<String, Object>();
                aps.put("alert", alert);
                aps.put("sound", "default");
                aps.put("badge", 0);
                Map<String, Object> x = new HashMap<String, Object>();
                x.put("content", alert);
                x.put("from", from);
                x.put("type", type);
                x.put("orgId", orgId);
                x.put("orgName", orgName);
                x.put("operTag", operTag);
                Map<String, Object> operator = new HashMap<String, Object>();
                operator.put("username", EnterpriseContext.getCurrentUser().getName());
                operator.put("userid", currUserId);
                operator.put("headpictrue", EnterpriseContext.getCurrentUser().getHeadPicture());
                x.put("operator", operator);
                Map<String, Object> extras = new HashMap<String, Object>();
                extras.put("from", PushMsg.getFrom(type));
                extras.put("type", type);
                extras.put("orgId", orgId);
                extras.put("orgName", orgName);
                extras.put("operTag", operTag);
                Map<String, Object> r = new HashMap<String, Object>();
                r.put("aps", aps);
                r.put("x", x);
                r.put("extras", extras);
                r.put("targetId", targetId);
                String msgType = PushMsg.getMsgType(from);
                ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(operTag, msgType, ids,
                        dataManager.toJSONString(r));
                PushMsg.asynPush(from, null, ids, dataManager.toJSONString(r), currUserId, token);
            }
        } catch (Exception e) {
            logger.error("sengMsg error:({}, {}, {}, {}):{}", operTag, alert, userId, type, e.getMessage());
        }
    }

}
