package com.chanapp.chanjet.customer.service.messageitem;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemHome;
import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemRow;
import com.chanapp.chanjet.customer.businessobject.api.messageitem.IMessageItemRowSet;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.customer.vo.system.AppUser;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanjet.csp.appmanager.AppWorkManager;

public class MessageItemServiceImpl extends BoBaseServiceImpl<IMessageItemHome, IMessageItemRow, IMessageItemRowSet>
        implements MessageItemServiceItf {
    private static Logger logger = LoggerFactory.getLogger(MessageItemServiceImpl.class);
    /**
     * 消息状态-未读
     */
    public final static Long MSG_READSTATUS_UNREAD = 1l;
    /**
     * 消息状态-已读
     */
    public final static Long MSG_READSTATUS_READ = 0l;

    @Override
    public void saveMessageItems(List<Long> userIds, String taskIdTmp, String type, Long msgId) {
        if (userIds != null && userIds.size() > 0) {

            for (Long userId : userIds) {
                boolean isOk = isEnable(userId);
                if (isOk) {
                    IMessageItemRow msgItemRow = createRow();
                    msgItemRow.setMsgId(msgId);
                    msgItemRow.setTaskId(taskIdTmp);
                    msgItemRow.setType(type);
                    msgItemRow.setReadStatus(MSG_READSTATUS_UNREAD);

                    upsert(msgItemRow);
                }
            }
        }

    }

    /**
     * 
     * <p>
     * 判断用户是否停用
     * </p>
     * 
     * @param userId
     */
    private boolean isEnable(long userId) {
        boolean bool = false;
        try {
        	AppUser user = EnterpriseUtil.findAppUserByUserId(userId,AppWorkManager.getCurrentAppId());
            if (user != null) {
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

}
