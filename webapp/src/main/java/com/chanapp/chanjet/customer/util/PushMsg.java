package com.chanapp.chanjet.customer.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.http.HttpCommon;
import com.chanapp.chanjet.customer.http.HttpResponse;
import com.chanapp.chanjet.customer.reader.CiaReader;
import com.chanapp.chanjet.customer.reader.IMReader;
import com.chanapp.chanjet.customer.service.msg.MsgManager;
import com.chanapp.chanjet.customer.service.msg.MsgSendVO;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.data.api.DataManager;

/**
 * 消息推送
 */
public class PushMsg {

    private static String APPID = "3";
    private static final Logger logger = LoggerFactory.getLogger(PushMsg.class);
    private static BlockingQueue<Runnable> workQueue = new LinkedBlockingDeque<Runnable>();
    private static ThreadPoolExecutor tpe = new ThreadPoolExecutor(20, 20, 5, TimeUnit.SECONDS, workQueue);

    protected static final DataManager dataManager = AppWorkManager.getDataManager();

    // 获得消息分类
    public static String getFrom(String categories) {
        String from = "0";
        switch (categories) {
            case IM.COMMENT:
                from = IM.COMMENT_ID;
                break;
            case IM.SYSTEM:
                from = IM.SYSTEM_ID;
                break;
            case IM.PERMISSIONS_CHANGE:
                from = IM.PERMISSIONS_CHANGE_ID;
                break;
            case IM.CUSTOMER_TRANSFER:
                from = IM.CUSTOMER_TRANSFER_ID;
                break;
            case IM.ACCOUNT_STOP:
                from = IM.ACCOUNT_STOP_ID;
                break;
            case IM.WORK_RECORD_AT:
                from = IM.WORK_RECORD_AT_ID;
                break;
            case IM.CUSTOMER_SHARE:
                from = IM.CUSTOMER_SHARE_ID;
                break;
            case IM.CUSTOMER_CANCEL_SHARE:
                from = IM.CUSTOMER_CANCEL_SHARE_ID;
                break;
            case IM.COMMENT_AT:
                from = IM.COMMENT_AT_ID;
                break;
            case IM.COMMENT_REPLY:
                from = IM.COMMENT_REPLY_ID;
                break;
        }
        return from;
    }

    private static String getCategories(String categories) {
        if (StringUtils.isEmpty(categories)) {
            return "0";
        }
        return categories;
    }

    /**
     * 异步调用发送IM消息
     */
    public static void asynPush(final String categories, final String oid, final ArrayList<Long> ids, final String body,
            final long bid, final String token) {
        int len = ids.size();
        for (int i = len - 1; i > -1; i--) {
            Long userId = Long.parseLong(ids.get(i).toString());
            boolean isOk = isEnable(userId);
            if (!isOk) {
                ids.remove(i);
            }
        }
    	final Map<Long, MsgSendVO>  msgMap = MsgManager.getMsgMap(body, ids);	
        tpe.execute(new Runnable() {
            public void run() {
                int len = ids.size();
                List<Long> userlist = getAuthUser(token);
                for (int i = 0; i < len; i++) {
                    Long userId = Long.parseLong(ids.get(i).toString());
					//工作圈消息推送			
					if(msgMap.get(userId)!=null){
						MsgManager.sendMsg(msgMap.get(userId));
					}
                    if (userlist != null && userlist.contains(userId))
                        push(categories, oid, Long.valueOf(ids.get(i).toString()), body, bid, token);
                }
            }
        });
    }

    /**
     * 
     * 不验证用户是否被停用都发送消息
     */
    public static void asynPushNoAuthUser(final String categories, final String oid, final ArrayList<Long> ids,
            final String body, final long bid, final String token) {
    	final Map<Long, MsgSendVO>  msgMap = MsgManager.getMsgMap(body, ids);	
        tpe.execute(new Runnable() {
            public void run() {
                int len = ids.size();
                for (int i = 0; i < len; i++) {
                    Long userId = Long.parseLong(ids.get(i).toString());
					if(msgMap.get(userId)!=null){
						MsgManager.sendMsg(msgMap.get(userId));
					}
                    push(categories, oid, userId, body, bid, token);
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    public static List<Long> getAuthUser(String token) {
        List<Long> retList = new ArrayList<Long>();
        Map<String, Object> resMap = null;
        String url = CiaReader.getAppAuthroizeForEntUserUrl();
        String appId = EnterpriseContext.getAppId();
        String appKey = EnterpriseContext.getAppKey();
        logger.info("appid={}", appId);
        Map<String, String> app = new HashMap<String, String>();
        app.put("appId", appId);
        Map<String, String> paramter = new HashMap<String, String>();
        paramter.put("appAuthroizeInfo", dataManager.toJSONString(app));
        String text = null;
        try {
            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/plain");
           // String appAuthorizeInfo = HttpCommon.generatorHttpParams(paramter);
            text = com.chanjet.csp.ccs.impl.common.HttpUtil.HttpPostAction(url + "?appKey=" + appKey + "&access_token=" + token,paramter);

/*            HttpResponse response = HttpUtil.doPost(url + "?appKey=" + appKey + "&access_token=" + token + "&" + appAuthorizeInfo, headers, null,
                    3000);
            text = response.getString();*/
            //System.out.println("ciaText:"+text);
            logger.info("Ciauser:" + url + "?appKey=" + appKey + "&access_token=" + token+"，text："+text);

        } catch (Exception e1) {
            e1.printStackTrace();
            logger.error("CiaException:" + url + "?appKey=" + appKey + "&access_token=" + token, paramter, e1);
        }
        if (StringUtils.isNotEmpty(text)) {
            try {
                if (!text.contains("\"errorCode\"")) {
                    resMap = dataManager.jsonStringToMap(text);
                    String userList = (String) resMap.get("AppAuthroizeForEntUser");
                    @SuppressWarnings("rawtypes")
                    List<Map> authUserList = dataManager.fromJSONString(userList, List.class);
                    for (Map<String, Object> userMap : authUserList) {
                        String userId = (String) userMap.get("userId");
                        retList.add(Long.parseLong(userId));
                    }
                }
            } catch (Exception e) {
                logger.error("getAppAuthroizeForEntUser error", e);
            }
        }
        return retList;
    }

    private static boolean isEnable(long userId) {
        boolean bool = false;
        try {
        	AppUser user = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());

            if (user != null) {
            //    String status = sysRelUser.getStatus();
                if (user.getIsActive()) {
                    bool = true;
                }
            }
        } catch (Exception e) {
            logger.error("isActive userId={}", userId);
            logger.error("isActive error", e);
        }
        logger.info("isEnable={}", bool);
        return bool;
    }

    /**
     * 
     * 消息推送
     * 
     * @param categories
     * 
     * @param oid
     * @param touserid
     * @param body { "aps": { "alert": "提示语", "sound": "default", "badge": 1 },
     *            "x": { "workrecid": "工作记录ID", "commetnid": "评论ID" }, "extras":
     *            { "id": 1 } }
     * @param bid
     * @param token
     * @return boolean
     */
    private static boolean push(String categories, String oid, long touserid, String body, long bid, String token) {
        String level = "2";
        try {
            logger.info("push content={}", body);

            String url = IMReader.getPushUrl();
            if (url == null) {
                logger.info("im url is null");
                return false;
            }
            Map<String, String> params = new HashMap<String, String>();
            if (StringUtils.isEmpty(oid)) {
                oid = "";
            }
            body = Base64.encodeBytes(body.toString().getBytes("utf-8"));
            params.put("oid", oid);
            params.put("from", categories);
            params.put("to", String.valueOf(touserid));
            params.put("title", APPID);
            params.put("body", body);
            params.put("level", level);
            String text = httpPost(url, params, String.valueOf(bid), token);
            logger.info("result={}", text);

            if (text == null) {
                return false;
            }
            Map<String, Object> json = dataManager.jsonStringToMap(text);
            if (json.containsKey("retcode") && json.get("retcode").toString().equals("0")) {
                return true;
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("error", e);
        }
        return false;
    }

    /**
     * 
     * 获得历史消息
     */
    public static String getHistory(long bid, String token, int count, String categories, long timeline) {
        String userid = String.valueOf(bid);
        categories = getCategories(categories);
        String time = String.valueOf(timeline);
        if (timeline == 0) {
            time = "";
        }
        String url = IMReader.getHistoryUrl();
        if (url == null) {
            logger.info("im url is null");
            return null;
        }
        url = String.format(url, APPID, bid, categories, count, time);
        String text = httpGet(url, userid, token);
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        Map<String, Object> json = dataManager.jsonStringToMap(text);
        if (json.containsKey("result") && json.get("result").toString().equals("0")) {
            return text;
        }
        return null;
    }

    private static String getResponseText(HttpResponse response) {
        if (response.getStatusCode() == 200) {
            String text = response.getString();
            if (StringUtils.isNotEmpty(text)) {
                return text;
            }
        } else {
            logger.error("push method failed stausCode={}, text={}", response.getStatusCode(), response.getString());
        }
        return null;
    }

    /**
     * POST方式发送消息
     * 
     * @param url 接口地址
     * @param params Map <String,String>参数
     * @param bid 登录用户ID
     * @param token 登录用户TOKEN
     * @return String
     */
    public static String httpPost(String url, Map<String, String> params, String bid, String token) {
        if (params != null && params.size() > 0) {
            logger.info("token={},url={},params={}", token, url, params.toString());
        } else {
            logger.info("token={},url={},params={}", token, url, null);
        }

        try {
            Map<String, String> header = new HashMap<String, String>();
            header.put("bid", bid);
            header.put("token", token);
            /*  
            String text = HttpUtil.HttpPostAction(url, header);
            return text;*/
            String bodyStr = HttpCommon.generatorHttpParams(params);
            byte[] body  = null;
            if(bodyStr!=null){
               body = bodyStr.getBytes();
            }       
            HttpResponse response = com.chanapp.chanjet.customer.util.HttpUtil.doPost(url, header, body, 3000);
            return getResponseText(response);
        } catch (Exception e) {
            logger.error("push http address error", e);
        }
        return null;
    }

    /**
     * GET方法调用IM接口
     * 
     * @param url 接口地址
     * @param bid 登录用户ID
     * @param token 登录用户TOKEN
     * @return String
     */
    public static String httpGet(String url, String bid, String token) {
        logger.info("token={},url={}", token, url);
        try {
            Map<String, String> header = new HashMap<String, String>();
            header.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            header.put("bid", bid);
            header.put("token", token);
            HttpResponse response = com.chanapp.chanjet.customer.util.HttpUtil.doGet(url, header, null, 3000);
            return getResponseText(response);
        } catch (Exception e) {
            logger.error("push http address error", e);
        }
        return null;
    }

    /**
     * 
     * 获得未读消息数
     */
    public static int getUnReadCount(long bid, String token, String categories) {
        categories = getCategories(categories);
        String url = IMReader.getUnreadCountUrl();
        if (url == null) {
            logger.info("im url is null");
            return 0;
        }
        url = String.format(url, APPID, bid, categories);
        String userid = String.valueOf(bid);
        String text = httpGet(url, userid, token);
        if (text != null) {
            Map<String, Object> json = dataManager.jsonStringToMap(text);
            if (json.containsKey("result")) {
                return Integer.valueOf(json.get("result").toString());
            }
        }
        return 0;
    }

    /**
     * 
     * 重置未读消息数
     */
    public static boolean resetCount(long bid, String token, String categories) {
        categories = getCategories(categories);
        String url = IMReader.getResetUnreadCountUrl();
        if (url == null) {
            logger.info("im url is null");
            return false;
        }
        String userid = String.valueOf(bid);
        url = String.format(url, APPID, bid, categories);
        Map<String, String> params = new HashMap<String, String>();
        String text = httpPost(url, params, userid, token);
        if (text != null) {
            Map<String, Object> json = dataManager.jsonStringToMap(text);
            if (json.containsKey("result") && Integer.valueOf(json.get("result").toString()).equals(0)) {
                return true;
            }
        }
        return false;
    }

    public static List<Long> getPushUserByContent(String content) {
        List<Long> users = new ArrayList<Long>();
        if (content == null)
            return users;
        Pattern p_get_at_users = Pattern.compile("\\{@([^,\\{\\}]+){1},([0-9]+){1}\\}");
        Matcher matcher = p_get_at_users.matcher(content);
        Long userId = 0L;
        while (matcher.find()) {
            userId = Long.parseLong(matcher.group(2));
            if (!users.contains(userId)) {
                users.add(userId);
            }
        }
        return users;
    }

    public static String getAtContent(String content) {
        if (content == null)
            return "";
        String atContent = content;
        Pattern p_get_at_users = Pattern.compile("\\{@([^,\\{\\}]+){1},([0-9]+){1}\\}");
        Matcher matcher = p_get_at_users.matcher(content);
        while (matcher.find()) {
            String replace = matcher.group();
            String name = "@" + matcher.group(1);
            atContent = atContent.replace(replace, name);
        }
        return atContent;
    }

    public static String getMsgType(String from) {
        return IM.localMsgType.get(from);
    }

    /**
     * 截断字符串
     * 
     * @param value
     * @param length
     * @return
     */
    public static String cutString(String value, int length) {
        if (StringUtils.isNotEmpty(value)) {
            if (value.length() > length) {
                return value.substring(0, length - 1) + "...";
            } else {
                return value;
            }
        }
        return "";
    }
}
