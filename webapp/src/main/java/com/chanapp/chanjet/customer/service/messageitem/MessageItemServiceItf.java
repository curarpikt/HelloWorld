package com.chanapp.chanjet.customer.service.messageitem;

import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemHome;
import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemRow;
import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface MessageItemServiceItf extends BoBaseServiceItf<IMessageItemHome, IMessageItemRow, IMessageItemRowSet> {
    /**
     * 保存消息记录信息
     * 
     * @param userIds
     * @param taskIdTmp
     * @param type
     * @param msgId
     */
    void saveMessageItems(List<Long> userIds, String taskIdTmp, String type, Long msgId);
}
