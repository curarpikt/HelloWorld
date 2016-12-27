package com.chanapp.chanjet.customer.service.message;

import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.message.IMessageHome;
import com.chanapp.chanjet.customer.businessobject.api.message.IMessageRow;
import com.chanapp.chanjet.customer.businessobject.api.message.IMessageRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface MessageServiceItf extends BoBaseServiceItf<IMessageHome, IMessageRow, IMessageRowSet> {
    void saveMessage(String taskId, String type, List<Long> userIds, String content);
}
