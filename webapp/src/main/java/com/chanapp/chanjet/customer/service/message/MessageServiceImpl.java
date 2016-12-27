package com.chanapp.chanjet.customer.service.message;

import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.message.IMessageHome;
import com.chanapp.chanjet.customer.businessobject.api.message.IMessageRow;
import com.chanapp.chanjet.customer.businessobject.api.message.IMessageRowSet;
import com.chanapp.chanjet.customer.service.messageitem.MessageItemServiceItf;
import com.chanapp.chanjet.customer.util.ShortIdUtil;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;

public class MessageServiceImpl extends BoBaseServiceImpl<IMessageHome, IMessageRow, IMessageRowSet>
        implements MessageServiceItf {

    @Override
    public void saveMessage(String taskId, String type, List<Long> userIds, String content) {

        IMessageRow messageRow = createRow();

        String taskIdTmp = taskId;
        if (taskId == null || "".equals(taskId)) {
            taskIdTmp = ShortIdUtil.generateShortUuid();
        }
        String title = null;
        messageRow.setTaskId(taskIdTmp);
        messageRow.setType(type);
        messageRow.setContent(content);
        messageRow.setTitle(title);
        upsert(messageRow);

        Long msgId = messageRow.getId();

        ServiceLocator.getInstance().lookup(MessageItemServiceItf.class).saveMessageItems(userIds, taskIdTmp, type,
                msgId);

    }

}
