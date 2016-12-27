package com.chanapp.chanjet.customer.service.msg;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.ISVMD5;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.impl.common.HttpUtil;
import com.chanjet.csp.common.base.json.JSONObject;
import com.chanjet.csp.data.api.DataManager;

public class MsgManager {

	private static final String appSecret = "1177857983959047175";
	private static final String appId = "10000007";
	private static final String from = "50000001025";
	private static final String appName = "客户管家";
	private static final Logger logger = LoggerFactory.getLogger(MsgManager.class);
	
	private static final String msg_url ="https://gzq.chanjet.com/notify/web/openim/push";
	public static Map<Long, MsgSendVO> getMsgMap(String body, List<Long> ids) {
		Map<Long, MsgSendVO> msgMap = new HashMap<Long, MsgSendVO>();
		for (Long id : ids) {
			MsgSendVO vo = _initMsgVO(id);
			setMsgVOWithBody(body, vo);
			msgMap.put(id, vo);
		}
		return msgMap;
	}

	public static void sendMsg(MsgSendVO vo) {
		try {
			DataManager dataM =AppWorkManager.getDataManager();
			Map<String, String> paraMap =dataM.fromJSONString(dataM.toJSONString(vo), Map.class);		
            String text = HttpUtil.HttpPostAction(msg_url, paraMap);
            logger.info("sendMsg:"+text);
           //	System.out.println("sendMsg:"+text);
			//HttpUtil.HttpPostAction(pushMsgUrl.getInstance().getPushMsgUrl(), paraMap);
		} catch (Exception e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}

	private static MsgSendVO _initMsgVO(Long to) {
		MsgSendVO vo = new MsgSendVO();
		vo.setAppId(appId);
		/*
		 * String orgId = PortalUtil.getOrgId(); vo.setOrgId(orgId);
		 * vo.setOrgname(PortalUtil.getOrgNameById(orgId));
		 */
		vo.setFrom(from);

		vo.setAppName(appName);
		vo.setTo(Long.toString(to));

		User user = EnterpriseUtil.getUserById(EnterpriseContext.getCurrentUser().getUserLongId());
		// 所有主标题都是被发送者人名
		vo.setMtitle(user.getName());
		String userId = EnterpriseContext.getCurrentUser().getUserId();
		vo.setUserid(userId);
		return vo;
	}

	private static void setMsgVOWithBody(String body, MsgSendVO vo) {
		JSONObject jsonBody = AppWorkManager.getDataManager().createJSONObject(body);

		// 公共部分
		JSONObject aps = jsonBody.get("aps") == null ? null : (JSONObject) jsonBody.get("aps");
		Assert.notNull(aps);
		// 设置摘要
		String alert = aps.get("alert") == null ? "" : (String) aps.get("alert");
		vo.setAlert(alert);

		// 设置签名
		Long createTime =System.currentTimeMillis();

		if(alert!=null&&createTime!=null){
			vo.setCreateTime(createTime.toString());
			vo.setSign(_getSign(alert,createTime.toString()));	
		}
		String targetId = jsonBody.getString("targetId");
		// 按类型组装
		JSONObject x = jsonBody.get("x") == null ? null : (JSONObject) jsonBody.get("x");
		Assert.notNull(x);
		String from = x.get("from") == null ? "" : (String) x.get("from");
		String orgId = x.get("orgId") == null ? null : (String) x.get("orgId");
		String orgName = x.get("orgName") == null ? null : (String) x.get("orgName");
		vo.setOrgid(orgId);
		vo.setOrgname(orgName);
		MsgExtVO extVO = new MsgExtVO();
		String workrecordId = x.get("workrecordId") == null ? null : x.get("workrecordId").toString();
		switch (from) {
		
		case IM.COMMENT_ID:
			String workrecordContent = x.get("workrecordContent") == null ? null : (String) x.get("workrecordContent");
			extVO.setAction("ToDetail");
			extVO.setTarget(WorkRecordMetaData.EOName);
			if (workrecordId != null) {
				extVO.setTargetId(Arrays.asList(Long.parseLong(workrecordId)));
			}
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			vo.setQuote(workrecordContent);
			vo.setStitle("评论你的跟进");
			break;
		case IM.SYSTEM_ID:

			break;
		case IM.PERMISSIONS_CHANGE_ID:
			// 无副标题
			break;
		case IM.CUSTOMER_TRANSFER_ID:
			extVO.setAction("ToList");
			extVO.setTarget(CustomerMetaData.EOName);
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			break;
		case IM.ACCOUNT_STOP_ID:
			// 停用用户原代码未发消息
			break;
		case IM.WORK_RECORD_AT_ID:
			vo.setStitle("在跟进中@了你");
			extVO.setAction("ToDetail");
			extVO.setTarget(WorkRecordMetaData.EOName);
			if (workrecordId != null) {
				extVO.setTargetId(Arrays.asList(Long.parseLong(workrecordId)));
			}
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			break;
		case IM.CUSTOMER_SHARE_ID:
			if(targetId==null){
				extVO.setAction("ToList");
			}else{
				extVO.setAction("ToDetail");
				extVO.setTargetId(Arrays.asList(Long.parseLong(targetId)));
			}
		
			extVO.setTarget(CustomerMetaData.EOName);
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			// 无副标题
			break;
		case IM.CUSTOMER_CANCEL_SHARE_ID:
			// 无副标题
			break;
		case IM.COMMENT_AT_ID:
			vo.setStitle("在评论中@了你");
			extVO.setAction("ToDetail");
			extVO.setTarget(WorkRecordMetaData.EOName);
			if (workrecordId != null) {
				extVO.setTargetId(Arrays.asList(Long.parseLong(workrecordId)));
			}
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			break;
		case IM.COMMENT_REPLY_ID:
			String commentContent = x.get("commentContent") == null ? null : (String) x.get("commentContent");
			vo.setQuote(commentContent);
			extVO.setAction("ToDetail");
			extVO.setTarget(WorkRecordMetaData.EOName);
			if (workrecordId != null) {
				extVO.setTargetId(Arrays.asList(Long.parseLong(workrecordId)));
			}
			vo.setExt(AppWorkManager.getDataManager().toJSONString(extVO));
			vo.setStitle("回复你的评论");
			break;
		}
	}

	private static String _getSign(String alert,String createTime) {
		StringBuilder builder = new StringBuilder();
		// 注意生成方法签名的createTime 为创建消息的时候的：System.currentTimeMillis()
/*		private static final String appSecret = "1177857983959047175";
		private static final String from = "50000001025";*/
		builder.append(from).append(appSecret).append(createTime).append(alert);
		logger.info("befroe:"+builder.toString());
       //	System.out.println("befroe:"+builder.toString());
		String sign = ISVMD5.crypt(builder.toString());
    	//System.out.println("md5:"+sign);
		logger.info("md5:"+sign);
		return sign;
	}
}
