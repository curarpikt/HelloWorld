package com.chanapp.chanjet.customer.service.notify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.util.Base64;
import com.chanapp.chanjet.customer.util.PushMsg;
import com.chanapp.chanjet.web.service.BaseServiceImpl;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.ccs.api.cia.UserInfo;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;

/**
 * @author tds
 *
 */
public class NotifyServiceImpl extends BaseServiceImpl implements NotifyServiceItf {
    private static Logger logger = LoggerFactory.getLogger(NotifyServiceImpl.class);

    @SuppressWarnings("unchecked")
    @Override
    public List<Map<String, Object>> records(long timeline, String categories, int count) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String token = EnterpriseContext.getToken();
        UserInfo user = EnterpriseContext.getCurrentUser();
        String text = PushMsg.getHistory(user.getUserLongId(), token, count, categories, timeline);
        if (text != null) {
            logger.info("record text={}", text);
            Map<String, Object> jsonObj = dataManager.jsonStringToMap(text);
            if (jsonObj != null) {
                List<Map<String, Object>> json = (List<Map<String, Object>>) jsonObj.get("data");
                int size = json.size();
                for (int i = 0; i < size; i++) {
                    Map<String, Object> obj = json.get(i);
                    String body = obj.get("body").toString();
                    String from = obj.get("from").toString();
                    from = from.substring(0, from.indexOf("@"));
                    long createTime = ConvertUtil.toLong(obj.get("createTime").toString());
                    long updateTime = ConvertUtil.toLong(obj.get("updateTime").toString());
                    body = body.replaceAll(" ", "+");
                    logger.info("start body={}", body);
                    try {
                        body = new String(Base64.decode(body), "utf-8");
                        logger.info("end body={}", body);
                        Map<String, Object> bodyObj = dataManager.jsonStringToMap(body);
                        if (bodyObj != null && bodyObj.containsKey("x")) {
                            Map<String, Object> result = (Map<String, Object>) bodyObj.get("x");
                            if (result != null) {
                                String _type = "";
                                if (result.containsKey("type")) {
                                    _type = result.get("type").toString();
                                } else {
                                    _type = "Comment";
                                }
                                result.put("from", from);
                                result.put("type", _type);
                                result.put("notifyCreateTime", createTime);
                                result.put("notifyUpdateTime", updateTime);
                                list.add(result);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        resetCount(categories);
        return list;
    }

    private boolean resetCount(String categories) {
        String token = EnterpriseContext.getToken();
        UserInfo user = EnterpriseContext.getCurrentUser();
        return PushMsg.resetCount(user.getUserLongId(), token, categories);
    }

    @Override
    public Map<String, Object> unreadCount(String categories) {
        String token = EnterpriseContext.getToken();
        UserInfo user = EnterpriseContext.getCurrentUser();
        int count = PushMsg.getUnReadCount(user.getUserLongId(), token, categories);

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("count", count);
        return data;
    }

    @Override
    public Map<String, Object> restCount(String categories) {
        String token = EnterpriseContext.getToken();
        UserInfo user = EnterpriseContext.getCurrentUser();
        boolean bool = PushMsg.resetCount(user.getUserLongId(), token, categories);
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("success", bool);
        return data;
    }
}
