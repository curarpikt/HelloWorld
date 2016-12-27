package com.chanapp.chanjet.customer.service.comment;

import java.util.LinkedHashMap;
import java.util.List;

import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentHome;
import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentRow;
import com.chanapp.chanjet.customer.businessobject.api.comment.ICommentRowSet;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.customer.vo.RowSet;
import com.chanapp.chanjet.customer.vo.sysc.SyscData;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;

public interface CommentServiceItf extends BoBaseServiceItf<ICommentHome, ICommentRow, ICommentRowSet> {
    Long countCommentByWorkrecordId(Long workrecordId);

    Row addComment(LinkedHashMap<String, Object> comment);

    RowSet findByWorkRecordForWeb(Long id);
    
    SyscData findByWorkRecordForRest(Long id,Long version);

    List<Long> getIdListByWorkRecordIdList(List<Long> ids);

    void deleteComment(Long id);
}
