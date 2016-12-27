package com.chanapp.chanjet.customer.restlet.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.chanapp.chanjet.customer.businessobject.api.workrecord.IWorkRecordRow;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.metadata.AttachmentMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.comment.CommentServiceItf;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.service.workrecord.WorkRecordServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.ProcessResult;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.restlet.BaseRestlet;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.exception.BOApplicationException;
import com.chanjet.csp.common.base.dataauth.DataAuthPrivilege;
import com.chanjet.csp.common.base.exception.AppException;

public class WorkRecord extends BaseRestlet{
	@Override
    public Object run() {		
		try{
			 if(this.getMethod().equals(MethodEnum.POST)){
				 return _addWorkRecord(this.getPayload());
			 }
			 else if(this.getMethod().equals(MethodEnum.PUT)){
				 return _updateWorkRecord(this.getPayload(),this.getId());
			 }
			 else if(this.getMethod().equals(MethodEnum.GET)){
				 if(this.getId()!=null){
					 return	_get(this.getId());
				 }else{
						Integer first = this.getParamAsInt("first");
						Integer max = this.getParamAsInt("max");
						Assert.notNull(first, "app.common.para.format.error");
						Assert.notNull(max, "app.common.para.format.error");
						Map<String,Object> para = new HashMap<String,Object>();
						para.put("owner", this.getParam("owner"));				
						para.put("keyWord", this.getParam("keyWord"));
						if(this.getParam("customerId")!=null){
							para.put("customerId",  this.getParamAsLong("customerId"));	
						}			
						return getWorkRecordList(first,max,para);
				 }			 
			 }else if(this.getMethod().equals(MethodEnum.DELETE)){
				return _deleleWorkRecord(this.getId());
			 }
		}catch (BOApplicationException e) {
			e.printStackTrace();
			throw new AppException(e.getMessageId(),e.getArguments());
		}
		
	    return null;
    }
	
	private String _addWorkRecord(String payload){
        LinkedHashMap<String, Object> workRecordParam = (LinkedHashMap<String, Object>) dataManager
                .jsonStringToMap(payload);
        Row row = new Row();
		WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
		workRecordService.addWorkRecord(workRecordParam, row);
		Map<String,Object> workRecordMap = getWorkReocrdMap(row);
		if(row.get("attachments")!=null){
			List<Row> attachments = (List<Row>)row.get("attachments");
			List<Map<String,Object>> attachmentsData =getAttachments(attachments);
			workRecordMap.put("attachments", attachmentsData);
		}
		workRecordMap.put("privilege", Integer.valueOf("111", 2));	
		workRecordMap.put("commentCount", 0);	
		return JSON.toJSONString(workRecordMap);
	}
	
	private List<Map<String,Object>> getAttachments(List<Row> attachments){		
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		for(Row row:attachments){
			Map<String,Object> retMap = new HashMap<String,Object>();
			List<String> fields = Arrays.asList(AttachmentMetaData.category,AttachmentMetaData.extend,AttachmentMetaData.fileDir,AttachmentMetaData.fileName,AttachmentMetaData.fileType,AttachmentMetaData.size,SC.id);
			for(String field:fields){
				if(row.containsKey(field)){
					retMap.put(field, row.get(field));
				}				
			}
			datas.add(retMap);
		}
		return datas;
	}
	
	private Map<String,Object> getWorkReocrdMap(Row row){
		Map<String,Object> retMap = new HashMap<String,Object>();
		List<String> fields = Arrays.asList(WorkRecordMetaData.contactTime,WorkRecordMetaData.content,WorkRecordMetaData.customer,WorkRecordMetaData.status,SC.owner,SC.id);
		for(String field:fields){
			if(row.containsKey(field)){
				retMap.put(field, row.get(field));
			}				
		}
		return retMap;
	}
	
	private String _updateWorkRecord(String payload,Long id){
        Row row = new Row();
		WorkRecordServiceItf workRecordService = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class);
		IWorkRecordRow retRow = workRecordService.updateWorkRecordForH5(payload, row,id);
		Map<String,Object> workRecordMap = getWorkReocrdMap(BoRowConvertUtil.toRow(retRow));
		if(row.get("attachments")!=null){
			List<Row> attachments = (List<Row>)row.get("attachments");
			List<Map<String,Object>> attachmentsData =getAttachments(attachments);
			workRecordMap.put("attachments", attachmentsData);
		}
		workRecordMap.put("privilege", Integer.valueOf("111", 2));		
		return JSON.toJSONString(workRecordMap);
	}
	
    private String _get(Long id) {
        Assert.notNull(id);
        IBusinessObjectRow item = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findByIdAndCusWithAuth(id);
		Map owner =(Map)item.getFieldValue(SC.owner);
		Long ownerId = (Long)owner.get(SC.id);
        Row retRow = ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).findWorkRecord(id);
        
        PrivilegeServiceItf PrivilegeService = ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class);
        boolean hasPri=PrivilegeService.checkDeleteDataAuth(EO.WorkRecord,id, AppWorkManager.getCurrAppUserId());	    
		if(AppWorkManager.getCurrAppUserId().equals(ownerId)){
			retRow.put("privilege", Integer.valueOf("111", 2));
		}else if(hasPri){				
			retRow.put("privilege", Integer.valueOf("101", 2));	
		}else{
			retRow.put("privilege", Integer.valueOf("001", 2));	
		}
        return dataManager.toJSONString(retRow);
    }
    
	public String getWorkRecordList(Integer first,Integer max,Map<String,Object> para){
		return ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).getWorkRecordList(first,max,para);
	}
    
	private String _deleleWorkRecord(Long id){
		ProcessResult processResult = new ProcessResult(true);
		ServiceLocator.getInstance().lookup(WorkRecordServiceItf.class).deleteWorkRecord(id);
		return AppWorkManager.getDataManager().toJSONString(processResult);
	}
}
