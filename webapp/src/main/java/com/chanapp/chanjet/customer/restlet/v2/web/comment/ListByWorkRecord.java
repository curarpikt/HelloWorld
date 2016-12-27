package com.chanapp.chanjet.customer.restlet.v2.web.comment;

import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;

/**
 * 按工作记录查询评论
 * 
 * @author tds
 *
 */
public class ListByWorkRecord extends BaseRestlet {
    @Override
    public Object run() {
        Long workRecordId = this.getId();
        Assert.notNull(workRecordId);

        return ServiceLocator.getInstance().lookup(CommentServiceItf.class).findByWorkRecordForWeb(workRecordId);
    }
}
