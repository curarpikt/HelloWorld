package com.chanapp.chanjet.customer.service.workrecordhis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.contact.IContactRow;
import com.chanapp.chanjet.customer.businessobject.api.customer.ICustomerRow;
import com.chanapp.chanjet.customer.businessobject.api.todowork.ITodoWorkRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisHome;
import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisRow;
import com.chanapp.chanjet.customer.businessobject.api.workrecordhis.IWorkRecordHisRowSet;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.service.attachment.AttachmentServiceItf;
import com.chanapp.chanjet.customer.service.contact.ContactServiceItf;
import com.chanapp.chanjet.customer.service.customer.CustomerServiceItf;
import com.chanapp.chanjet.customer.service.todowork.TodoWorkServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.EnterpriseUtil;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.common.base.usertype.DynamicEnum;

public class WorkRecordHisServiceImpl
        extends BoBaseServiceImpl<IWorkRecordHisHome, IWorkRecordHisRow, IWorkRecordHisRowSet>
        implements WorkRecordHisServiceItf {
    private static Logger logger = LoggerFactory.getLogger(WorkRecordHisServiceImpl.class);

    @Override
    public void addWorkRecordHis(Long workRecordId) {
        IWorkRecordRow oldworkRecord = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class)
                .query(workRecordId);
        if (oldworkRecord == null) {
            logger.info("oldworkRecord is null:{}", workRecordId);
            return;
        }
        Long workrecordId = oldworkRecord.getId();
        IWorkRecordHisRow wrh = createRow();
        wrh.setWorkRecordId(oldworkRecord.getId());
        DynamicEnum statusEnum = oldworkRecord.getStatus();
        DynamicEnum followTypeEnum = oldworkRecord.getFollowType();
        Map<String, Object> contMap = new HashMap<String, Object>();
        if (statusEnum != null) {
            Map<String, Object> status = new HashMap<String, Object>();
            status.put("label", statusEnum.getLabel());
            status.put("value", statusEnum.getValue());
            contMap.put("status", status);// 状态标签
        }
        if (followTypeEnum != null) {
            Map<String, Object> followType = new HashMap<String, Object>();
            followType.put("label", followTypeEnum.getLabel());
            followType.put("value", followTypeEnum.getValue());
            contMap.put("followType", followType);
        }

        contMap.put("id", workrecordId);
        contMap.put("content", oldworkRecord.getContent());// 工作记录内容
        contMap.put("address", oldworkRecord.getAddress());// 地址

        ICustomerRow customer = ServiceLocator.getInstance().lookup(CustomerServiceItf.class)
                .findByIdWithAuth(oldworkRecord.getCustomer());
        if (customer != null) {
            contMap.put("customerName", customer.getName());// 客户名称
        }
        IContactRow contact = ServiceLocator.getInstance().lookup(ContactServiceItf.class)
                .findByIdWithAuth(oldworkRecord.getContact());
        if (contact != null) {
            contMap.put("contactName", contact.getName());// 联系人名称
        }
        contMap.put("createdDate", oldworkRecord.getCreatedDate());
        Map<String, Object> createdBy = new HashMap<String, Object>();
        createdBy.put("id", oldworkRecord.getCreatedBy());
        String createByName = EnterpriseUtil.getUserById(oldworkRecord.getCreatedBy()).getName();
        createdBy.put("name", createByName);
        contMap.put("createdBy", createdBy);

        contMap.put("lastModifiedDate", oldworkRecord.getLastModifiedDate());
        Map<String, Object> lastModifiedBy = new HashMap<String, Object>();
        lastModifiedBy.put("id", oldworkRecord.getLastModifiedBy());
        String lastModifyByName = EnterpriseUtil.getUserById(oldworkRecord.getLastModifiedBy()).getName();
        lastModifiedBy.put("name", lastModifyByName);
        contMap.put("lastModifiedBy", lastModifiedBy);

        List<Long> relateToIds = new ArrayList<Long>();
        relateToIds.add(workrecordId);
        // 组装附件
        IAttachmentRowSet attachList = ServiceLocator.getInstance().lookup(AttachmentServiceItf.class)
                .findRowSetByRelate(EO.WorkRecord, relateToIds);
        /// attachmentDao.findAttachmentByRelate("WorkRecord", relateToIds);
        if (attachList != null && attachList.getAttachmentRows().size() > 0) {
            List<Map<String, Object>> attachs = new ArrayList<Map<String, Object>>();
            for (IAttachmentRow attachment : attachList.getAttachmentRows()) {
                Map<String, Object> tmp = new HashMap<String, Object>();
                tmp.put("fileDir", attachment.getFileDir());// 附件地址
                tmp.put("category", attachment.getCategory());
                tmp.put("fileType", attachment.getFileType());
                tmp.put("fileName", attachment.getFileName());
                attachs.add(tmp);
            }
            contMap.put("attachs", attachs);
        }
        // 组装代办
        List<Long> workrecordIds = new ArrayList<Long>();
        workrecordIds.add(workRecordId);
        List<ITodoWorkRow> todoWorkList = ServiceLocator.getInstance().lookup(TodoWorkServiceItf.class)
                .findSetByWorkrecordIds(workrecordIds);
        if (todoWorkList != null && todoWorkList.size() > 0) {
            List<Map<String, Object>> todoWorks = new ArrayList<Map<String, Object>>();
            Map<String, Object> tmp;
            for (ITodoWorkRow todoWork : todoWorkList) {
                tmp = new HashMap<String, Object>();
                tmp.put("workContent", todoWork.getWorkContent());// 代办内容
                tmp.put("planTime", todoWork.getPlanTime());// 办理时间
                tmp.put("remindTime", todoWork.getRemindTime());// 提醒时间
                tmp.put("remindType", todoWork.getRemindType());
                todoWorks.add(tmp);
            }
            contMap.put("todoWorks", todoWorks);
        }
        String cont = dataManager.toJSONString(contMap);
        wrh.setCont(cont);
        upsert(wrh);
    }

}
