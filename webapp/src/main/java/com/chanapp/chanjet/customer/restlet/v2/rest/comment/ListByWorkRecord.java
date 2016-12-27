package com.chanapp.chanjet.customer.restlet.v2.rest.comment;

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
        //Long workRecordId = this.getId();
        String version = this.getParam("version");
        String id = this.getParam("id");
        Assert.notNull(version);
       Assert.notNull(id);

        return ServiceLocator.getInstance().lookup(CommentServiceItf.class).findByWorkRecordForRest(Long.parseLong(id),Long.parseLong(version));
    }
}
