package com.chanapp.chanjet.customer.service.comment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentHome;
import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentRow;
import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.IM;
import com.chanapp.chanjet.customer.constant.metadata.AttachmentMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CommentMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.message.MessageServiceItf;
import com.chanapp.chanjet.customer.service.operationlog.OperationLogServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.util.PortalUtil;
import com.chanapp.chanjet.customer.util.PushMsg;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.sysc.SyscData;
import com.chanapp.chanjet.customer.vo.system.User;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;

public class CommentServiceImpl extends BoBaseServiceImpl<ICommentHome, ICommentRow, ICommentRowSet>
        implements CommentServiceItf {

    @Override
    public Long countCommentByWorkrecordId(Long workrecordId) {
        if (workrecordId != null) {
            Criteria criteria = Criteria.AND();
            //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
            criteria.eq(CommentMetaData.relateToType, WorkRecordMetaData.EOName);
            criteria.eq(CommentMetaData.relateToID, workrecordId);
            JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
            jsonQueryBuilder.addCriteria(criteria);

            int count = getRowCount(jsonQueryBuilder.toJsonQuerySpec());
            return Long.valueOf(count);
        }
        return 0l;
    }

    @Override
    public Row addComment(LinkedHashMap<String, Object> commentParam) {
        Row result = _addComment(commentParam);   
        Long replyId = result.get(CommentMetaData.replyId)==null?null:ConvertUtil.toLong(result.get(CommentMetaData.replyId).toString());
        if (null != replyId) {
            ICommentRow replyComment = findByIdWithAuth(replyId);
            if (replyComment != null) {
                User user = EnterpriseUtil.getUserById(replyComment.getOwner());
                if (user != null) {
                    result.put("replyerName", user.getName());
                }
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Row _addComment(LinkedHashMap<String, Object> commentParam) {
        WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
        IWorkRecordRow record = workRecordService
                .findByIdWithAuth(ConvertUtil.toLong(commentParam.get(CommentMetaData.relateToID).toString()));
        Assert.notNull(record, "app.workrecord.object.deleted");
        String content = commentParam.get(CommentMetaData.content)==null?null:commentParam.get(CommentMetaData.content).toString();       		
        Long replyId = commentParam.get(CommentMetaData.replyId)==null?null:ConvertUtil.toLong(commentParam.get(CommentMetaData.replyId).toString());       		
        
        ICommentRow comment = save(
                commentParam.containsKey(CommentMetaData.localId) ? commentParam.get(CommentMetaData.localId).toString()
                        : "",
                ConvertUtil.toLong(commentParam.get(CommentMetaData.relateToID).toString()),
                commentParam.get(CommentMetaData.relateToType).toString(),             
                content,
                replyId,
                commentParam.containsKey(CommentMetaData.commenter)
                        ? ConvertUtil.toLong(commentParam.get(CommentMetaData.commenter).toString()) : null);

        Row row = BoRowConvertUtil.toRow(comment);
        List<Map<String, Object>> attachments = (List<Map<String, Object>>) commentParam.get("attachments");
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).save(comment.getId(),
                comment.getDefinition().getPrimaryEO().getName(), attachments);

        row.put("attachments", attachments);

        // 共享@用户工作记录
        ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).shareByContent(comment.getContent(),
                record.getId());

        List<Long> sentedIds = new ArrayList<Long>();
        // 回复评论
        sentedIds.addAll(replyaComment(comment));
        // 评论@消息推送
        sentedIds.addAll(atComment(comment, sentedIds));
        // 评论参与人当做@消息推送
        sentedIds.addAll(getCommenter(comment, sentedIds));
        // 工作记录onwer消息推送
        replyWorkRecord(comment, sentedIds);
        return row;
    }

    private List<Long> getCommenter(ICommentRow comment, List<Long> sentedList) {
        List<Long> retIds = new ArrayList<Long>();
        Long workRecordId = comment.getRelateToID();

        ICommentRowSet commenters = getCommentByWorkrecordId(workRecordId,0L);
        if (commenters != null && commenters.size() > 0) {
            for (int i = 0; i < commenters.size(); i++) {
                Long userId = commenters.getRow(i).getOwner();
                if (!sentedList.contains(userId) && !retIds.contains(userId)) {
                    retIds.add(userId);
                }
            }
        }
        if (retIds.size() > 0) {
            String json = getCommentMsg(comment, IM.COMMENT_AT);
            if (json != null) {
                sendMsg(json, retIds, IM.COMMENT_AT);
            }
        }
        return retIds;
    }

    private List<Long> replyWorkRecord(ICommentRow comment, List<Long> sentedIds) {
        List<Long> userlist = new ArrayList<Long>();
        IWorkRecordRow record = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .findByIdWithOutAuth(comment.getRelateToID());
        Assert.notNull(record, "app.workrecord.object.notexist");
        Long workUserId = record.getOwner();
        if (sentedIds.contains(workUserId)) {
            return userlist;
        }
        userlist.add(workUserId);
        String json = getCommentMsg(comment, IM.COMMENT);
        if (json != null) {
            sendMsg(json, userlist, IM.COMMENT);
        }
        return userlist;

    }

    private ICommentRowSet getCommentByWorkrecordId(Long workrecordId,Long version) {
        Criteria criteria = Criteria.AND();
       // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(CommentMetaData.relateToType, WorkRecordMetaData.EOName);
        criteria.eq(CommentMetaData.relateToID, String.valueOf(workrecordId));
		if(version!=null){
			criteria.gt(CommentMetaData.commentTime, version);
		}
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance().addCriteria(criteria);
        jsonQueryBuilder.addCriteria(criteria).addOrderDesc(SC.lastModifiedDate);
        return queryAll(jsonQueryBuilder.toJsonQuerySpec());
    }

    private List<Long> atComment(ICommentRow comment, List<Long> sentedIds) {

        List<Long> sentList = new ArrayList<Long>();
        List<Long> atUsers = PushMsg.getPushUserByContent(comment.getContent());
        for (Long userId : atUsers) {
            if (!sentedIds.contains(userId)) {
                sentList.add(userId);
            }
        }
        if (sentList.size() > 0) {
            String json = getCommentMsg(comment, IM.COMMENT_AT);
            if (json != null) {
                sendMsg(json, sentList, IM.COMMENT_AT);
            }
        }
        return sentList;
    }

    private List<Long> replyaComment(ICommentRow comment) {
        Long replyId = comment.getReplyId();
        List<Long> userlist = new ArrayList<Long>();
        if (replyId != null) {
            ICommentRow replyComment = findByIdWithAuth(replyId);
            String json = getCommentMsg(comment, IM.COMMENT_REPLY);
            if (json != null && replyComment != null) {
                userlist.add(replyComment.getOwner());
                sendMsg(json, userlist, IM.COMMENT_REPLY);
            }
        }
        return userlist;
    }

    private String getCommentMsg(ICommentRow commet, String type) {
        String orgId = PortalUtil.getOrgId();
        String orgName = PortalUtil.getOrgNameById(orgId);
        if (StringUtils.isEmpty(orgId)) {
            return null;
        }
        Row Msgrow = new Row();
        Row apsrow = new Row();
        Row extras = new Row();
        Row row = new Row();
        Long id = commet.getId();

        IWorkRecordRow record = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .findByIdWithAuth(commet.getRelateToID());
        Assert.notNull(record, "app.workrecord.object.notexist");

        String customerName = "";
        if (record.getCustomer() != null) {
            ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                    .getCustomerById(record.getCustomer());
            if (customer != null) {
                customerName = customer.getName();
                if (customer.getStatus() != null) {
                    String customerStatus = customer.getStatus().getLabel();
                    row.put("workrecordStatus", customerStatus);
                } else {
                    row.put("workrecordStatus", "");
                }
            } else {
                row.put("workrecordStatus", "");
            }
        } else {
            row.put("workrecordStatus", "");
        }

        String workrecordContent = record.getContent();
        Row ownerRow = new Row();
        if (commet.getOwner() != null) {
            User user = EnterpriseUtil.getUserById((Long) commet.getOwner());
            ownerRow.put("userid", user.getUserId());
            ownerRow.put("username", user.getName());
            ownerRow.put("headpictrue", user.getHeadPicture());
        }
        Long commentTime = commet.getCommentTime().getTime();
        String commentContent = commet.getContent();
        // 语音评论模板
        String alter = getAlertByType(commentContent, type);
        String from = PushMsg.getFrom(type);
        row.put("customerName", customerName);
        row.put("commentId", id);
        if (IM.COMMENT_REPLY.equalsIgnoreCase(type)) {
            Long replyId = commet.getReplyId();
            ICommentRow replyComment = null;
            if (replyId != null) {
                replyComment = findByIdWithAuth(replyId);
            }
            if (replyComment != null) {
                row.put("replyId", replyId);
                row.put("commentContent", cutString(PushMsg.getAtContent(replyComment.getContent()), 20));
                row.put("replyContent", cutString(PushMsg.getAtContent(commentContent), 20));
            }
        } else {
            row.put("commentContent", cutString(PushMsg.getAtContent(commentContent), 20));
        }
        row.put("workrecordContent", cutString(PushMsg.getAtContent(workrecordContent), 20));
        row.put("commentOwner", ownerRow);
        row.put("commentTime", commentTime);

        row.put("workrecordId", record.getId());
        row.put("type", type);
        row.put("from", from);
        row.put("orgId", orgId);
        row.put("orgName", orgName);

        apsrow.put("alert", alter);
        apsrow.put("sound", "default");
        apsrow.put("badge", 1);

        extras.put("workrecordId", record.getId());
        extras.put("from", from);
        extras.put("type", type);
        extras.put("orgId", orgId);
        extras.put("orgName", orgName);

        Msgrow.put("aps", apsrow);
        Msgrow.put("x", row);
        Msgrow.put("extras", extras);
        return dataManager.toJSONString(Msgrow);
    }

    private String getAlertByType(String content, String Type) {
        String alert = "";
        content = PushMsg.getAtContent(content);
        String name = EnterpriseContext.getCurrentUser().getName();
        switch (Type) {
            case IM.COMMENT:
                alert = name + "评论了你的工作：" + content;
                return alert;
            case IM.COMMENT_AT:
                alert = name + "评论工作时@你：" + content;
                return alert;
            case IM.COMMENT_REPLY:
                alert = name + "回复你的评论:" + content;
                return alert;
        }
        return alert;
    }

    /**
     * 截断字符串
     * 
     * @param value
     * @param length
     * @return
     */
    public String cutString(String value, int length) {
        if (StringUtils.isNotEmpty(value)) {
            if (value.length() > length) {
                return value.substring(0, length - 1) + "...";
            } else {
                return value;
            }
        }
        return "";
    }

    private ICommentRow save(String localId, Long relateToID, String relateToType, String content, Long replyId,
            Long commenter) {
        ICommentRow commentRow = null;
        if (StringUtils.isNotEmpty(localId)) {
            commentRow = findByLocalId(localId);
        }
        if (commentRow == null) {
            commentRow = createRow();
        }
        commentRow.setLocalId(localId);
        commentRow.setRelateToID(relateToID);
        commentRow.setRelateToType(relateToType);
        commentRow.setContent(content);
        commentRow.setReplyId(replyId);
        commentRow.setCommentTime(DateUtil.getNowDateTime());
        if (commenter != null) {
            commentRow.setCommenter(commenter);
        }
        upsert(commentRow);
        return commentRow;
    }

    private ICommentRow findByLocalId(String localId) {
        Criteria criteria = Criteria.AND();
        criteria.eq(CommentMetaData.localId, localId);
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        try {
            ICommentRowSet rowset = query(JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec());
            if (rowset != null && rowset.size() > 0) {
                return rowset.getRow(0);
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private void sendMsg(String json, List<Long> ids, String messageType) {
        if (ids == null)
            return;
        long userid = EnterpriseContext.getCurrentUser().getUserLongId();
        if (ids.contains(userid)) {
            ids.remove(userid);
        }
        String token = EnterpriseContext.getToken();
        StringBuffer text = new StringBuffer();

        for (Long id : ids) {
            text.append(id.toString() + ",");
        }
        ArrayList<Long> userList = (ArrayList<Long>) ids;
        try {
            String from = PushMsg.getFrom(messageType);
            String msgType = PushMsg.getMsgType(from);
            ServiceLocator.getInstance().lookup(MessageServiceItf.class).saveMessage(null, msgType, ids, json);
            PushMsg.asynPush(from, null, userList, json, userid, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public RowSet findByWorkRecordForWeb(Long id) {
        ICommentRowSet comments = queryByWorkrecordId(id,0L);
        if (comments == null || comments.size() < 1) {
            return new RowSet();
        }
        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < comments.size(); i++) {
            if (StringUtils.isBlank(comments.getRow(i).getContent())) {
                ids.add(comments.getRow(i).getId());
            }
        }
        RowSet rowSet = BoRowConvertUtil.toRowSet(comments);
        Map<Long, List<IAttachmentRow>> attachMap = null;
        if (ids.size() > 0) {
            attachMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).findCommentsAttachments(ids);
        }
        for (int i = 0; i < rowSet.getItems().size(); i++) {
            Row row = rowSet.getItems().get(i);
            Long replyId = row.getLong("replyId");
            if (null != replyId) {
                ICommentRow replyComment = findByIdWithAuth(replyId);
                if (replyComment != null) {
                    User user = EnterpriseUtil.getUserById(replyComment.getOwner());
                    if (user != null) {
                        row.put("replyerName", user.getName());
                    }
                }
            }
            if (attachMap != null) {
                List<IAttachmentRow> attachments = attachMap.get(row.getLong("id"));
                if (null != attachments)
                    row.put("attachments", attachments);
            }
        }
        rowSet.setTotal(rowSet.getItems().size());
        return rowSet;
    }

    private ICommentRowSet queryByWorkrecordId(Long id,Long version) {
        IWorkRecordRow record = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findByIdWithAuth(id);
        if (record == null) {
            Assert.notNull(record, "app.workrecord.object.deleted");
        }
        ICommentRowSet commets = getCommentByWorkrecordId(id,version);
        return commets;
    }

    @Override
    public List<Long> getIdListByWorkRecordIdList(List<Long> ids) {
        List<Long> idList = new ArrayList<Long>();
        if (ids == null || ids.size() == 0) {
            return idList;
        }
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.in(CommentMetaData.relateToID, ids.toArray()).eq(CommentMetaData.relateToType,
                WorkRecordMetaData.EOName);
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        ICommentRowSet rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        for (ICommentRow row : rowSet.getCommentRows()) {
            Long id = (Long) row.getFieldValue(SC.id);
            idList.add(id);
        }
        return idList;
    }

    @Override
    public void deleteComment(Long id) {
        // 删除附件
        ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).deleteAttachmentByRelate("Comment", id);
        logicDeleteByIdWithAuth(id, "Comment", BO.Comment);
    }

    private void logicDeleteByIdWithAuth(Long id, String entityName, String boName) {
        this.checkDeleteAuthById(id);
        this.deleteRowWithRecycle(id);
        ServiceLocator.getInstance().lookup(OperationLogServiceItf.class).generate(id, entityName, "DELETE");
    }

	@Override
	public SyscData findByWorkRecordForRest(Long id, Long version) {
		SyscData syscData = new SyscData();
        ICommentRowSet comments = queryByWorkrecordId(id,version);
        if (comments == null || comments.size() < 1) {
            return syscData;
        }
        List<Long> ids = new ArrayList<Long>();
        for (int i = 0; i < comments.size(); i++) {
        	syscData.addEntity(CommentMetaData.EOName, comments.getRow(i));
            if (StringUtils.isBlank(comments.getRow(i).getContent())) {
                ids.add(comments.getRow(i).getId());
            }
        }
        RowSet rowSet = BoRowConvertUtil.toRowSet(comments);
        Map<Long, List<IAttachmentRow>> attachMap = null;
        if (ids.size() > 0) {
            attachMap = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class).findCommentsAttachments(ids);
        }
        for (int i = 0; i < rowSet.getItems().size(); i++) {
            Row row = rowSet.getItems().get(i);
            if (attachMap != null) {
                List attachments = attachMap.get(row.getLong("id"));
                if (null != attachments)
    				syscData.addEntities(AttachmentMetaData.EOName, attachments);
            }
        }
        rowSet.setTotal(rowSet.getItems().size());
        return syscData;
  
	}

}
