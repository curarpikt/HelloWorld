package com.chanapp.chanjet.customer.restlet.v2.web;

import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 新增评论，如果有@人员，共享评论的工作记录并发送消息推送
 * 
 * @author tds
 *
 */
public class Comment extends BaseRestlet {
    private static final Logger logger = LoggerFactory.getLogger(Comment.class);

    @Override
    public Object run() {

        String payload = this.getPayload();
        Assert.notNull(payload);

        logger.info("add comment:{}", payload);

        LinkedHashMap<String, Object> commentParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);

        return ServiceLocator.getInstance().lookup(CommentServiceItf.class).addComment(commentParam);
    }

}
