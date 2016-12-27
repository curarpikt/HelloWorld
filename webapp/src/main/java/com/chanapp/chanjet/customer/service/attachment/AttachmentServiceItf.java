package com.chanapp.chanjet.customer.service.attachment;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentHome;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface AttachmentServiceItf extends BoBaseServiceItf<IAttachmentHome, IAttachmentRow, IAttachmentRowSet> {
    IAttachmentRowSet findRowSetByRelate(String relateToType, List<Long> relateToIds);

    Integer countAttachmentByRelate(String relateToType, Long relateToId);

    void uploadAttament(Long customerId, Map<String, Object> attMap);

    List<IAttachmentRow> findCustomerAttachments(Long customerId);

    Map<String, Object> uploadFile(String originName, InputStream is);

    IAttachmentRowSet findAttachmentByRelate(String relateToType, Long relateToId, boolean withDel);

    void recoverAttachmentById(Long id);

    IAttachmentRowSet save(Long RelateToID, String RelateToType, List<Map<String, Object>> attachments);

    Map<Long, List<IAttachmentRow>> findWorkRecordAttachments(List<Long> ids);

    void deleteAttachmentByRelate(String relateToType, Long relateToId);

    List<IAttachmentRow> findAttachmentListByRelate(String relateToType, Long relateToId);

    IAttachmentRowSet findWRAttachmentsSet(Long id);

    Map<Long, List<IAttachmentRow>> findCommentsAttachments(List<Long> ids);

    boolean isImageSuffix(String suffix);

    List<Boolean> cutImage(Map<String, Object> map);

    Map<String, Object> uploadTransFileToMp3(String originName, InputStream is);

    List<Long> findAttachmentIdsByRelate(String relateToType, List<Long> relateToIds);

	List<Map<String, Object>> getAttachmentRows(List<IAttachmentRow> attachments);
	
    Map<Long, List<IAttachmentRow>> findCheckinsAttachments(List<Long> ids);

	void getAttachmentsByRelateItem(List<Map<String, Object>> items, String relateType);
}
