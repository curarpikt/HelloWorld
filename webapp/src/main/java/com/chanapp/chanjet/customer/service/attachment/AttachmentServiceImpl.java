package com.chanapp.chanjet.customer.service.attachment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentHome;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRow;
import com.chanapp.chanjet.customer.businessobject.api.attachment.IAttachmentRowSet;
import com.chanapp.chanjet.customer.businessobject.api.customerwithoutbusiness.ICustomerWithOutBusinessRow;
import com.chanapp.chanjet.customer.constant.BO;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.metadata.AttachmentMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CheckinMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.reader.OssReader;
import com.chanapp.chanjet.customer.service.privilege.PrivilegeServiceItf;
import com.chanapp.chanjet.customer.util.Assert;
import com.chanapp.chanjet.customer.util.CspCssUtil;
import com.chanapp.chanjet.customer.util.DateUtil;
import com.chanapp.chanjet.customer.util.FileUtil;
import com.chanapp.chanjet.customer.util.PreviewUtil;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.service.BoBaseServiceImpl;
import com.chanapp.chanjet.web.service.BoBaseServiceItf;
import com.chanapp.chanjet.web.service.ServiceLocator;
import com.chanapp.chanjet.web.util.ConvertUtil;
import com.chanapp.chanjet.web.util.QueryLimitUtil;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.bo.api.IBusinessObjectHome;
import com.chanjet.csp.bo.api.IBusinessObjectRow;
import com.chanjet.csp.bo.api.IBusinessObjectRowSet;
import com.chanjet.csp.ccs.api.common.EnterpriseContext;
import com.chanjet.csp.ccs.impl.oss.OssServiceImpl;
import com.chanjet.csp.common.base.exception.ServerException;
import com.chanjet.csp.common.base.json.JSONObject;
import com.chanjet.csp.common.base.util.StringUtils;
import com.chanjet.csp.ui.util.Criteria;
import com.chanjet.csp.ui.util.JsonQueryBuilder;
import com.chanjet.csp.ui.util.OSSUtil;

public class AttachmentServiceImpl extends BoBaseServiceImpl<IAttachmentHome, IAttachmentRow, IAttachmentRowSet>
        implements AttachmentServiceItf {
    private static final Logger logger = LoggerFactory.getLogger(AttachmentServiceImpl.class);

    @Override
    public IAttachmentRowSet findRowSetByRelate(String relateToType, List<Long> relateToIds) {
        IAttachmentRowSet rowSet = createRowSet();
        if (relateToIds.size() > 0) {
            Criteria criteria = Criteria.AND();
           // criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
            criteria.eq(AttachmentMetaData.relateToType, relateToType);
            criteria.in(AttachmentMetaData.relateToID, relateToIds.toArray());

            JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
            jsonQueryBuilder.addCriteria(criteria);
            jsonQueryBuilder.addOrderAsc(SC.id);
            rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        }
        return rowSet;
    }

    /**
     * 根据关联类型及关联ID，查询对应的附件列表。
     * 
     * @param relateToType 关联类型
     * @param relateToId 关联对象ID
     * @return AttachmentRowSet
     */
    @Override
    public IAttachmentRowSet findAttachmentByRelate(String relateToType, Long relateToId, boolean withDel) {
        Criteria criteria = Criteria.AND();
/*        if (withDel != true) {
            criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        }*/
        criteria.eq(AttachmentMetaData.relateToType, relateToType);
        criteria.eq(AttachmentMetaData.relateToID, relateToId);

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        jsonQueryBuilder.addOrderAsc(SC.id);

        IAttachmentRowSet rowSet = queryAll(jsonQueryBuilder.toJsonQuerySpec());
        return rowSet;
    }

    @Override
    public Integer countAttachmentByRelate(String relateToType, Long relateToId) {
        Criteria criteria = Criteria.AND();
      //  criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        criteria.eq(AttachmentMetaData.relateToType, relateToType);
        criteria.eq(AttachmentMetaData.relateToID, relateToId);

        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);

        return getRowCount(jsonQueryBuilder.toJsonQuerySpec());
    }

    @Override
    public void uploadAttament(Long customerId, Map<String, Object> attMap) {
        BoBaseServiceItf<IBusinessObjectHome, IBusinessObjectRow, IBusinessObjectRowSet> customerService = ServiceLocator
                .getInstance().lookup(BO.CustomerWithOutBusiness);
        ICustomerWithOutBusinessRow customer = (ICustomerWithOutBusinessRow) customerService
                .findByIdWithAuth(customerId);
        Assert.notNull(customer, "app.customer.object.notexist");
        Long userId = EnterpriseContext.getCurrentUser().getUserLongId();
        if (ServiceLocator.getInstance().lookup(PrivilegeServiceItf.class).checkUpdateDataAuth(EO.Customer,
                customer.getId(), userId)) {
            if (StringUtils.isEmpty(customer.getLocalId())) {
                customer.setLocalId(customer.getId() + "");
            }
            customer.setModifiedTime(DateUtil.getNowDateTime());
            customerService.upsert(customer);
        }

        Integer atts = countAttachmentByRelate(EO.Customer, customerId);
        if (atts != null) {
            Assert.maxAttachment(atts);
        }

        IAttachmentRow attVo = createRow();

        attVo.setRelateToID(customerId);
        attVo.setRelateToType("Customer");
        if (attMap.get("name") != null) {
            attVo.setFileName(attMap.get("name").toString());
        }
        if (attMap.get("url") != null) {
            attVo.setFileDir(attMap.get("url").toString());
        }
        if (attMap.get("suffix") != null) {
            attVo.setFileType(attMap.get("suffix").toString());
        }
        if (attMap.get("size") != null) {
            attVo.setSize(Long.valueOf(attMap.get("size").toString()));
        }
        attVo.setCategory("file");
        upsert(attVo);

    }

    @Override
    public List<IAttachmentRow> findCustomerAttachments(Long customerId) {
        return findAttachmentByRelate(EO.Customer, customerId, false).getAttachmentRows();
    }

    @Override
    public Map<String, Object> uploadFile(String originName, InputStream is) {
        Map<String, Object> map = null;
        try {
            boolean isImage = isImageSuffix(OSSUtil.getSuffix(originName));
            map = isImage ? OSSUtil.uploadImage(originName, is) : OSSUtil.uploadFile(originName, is);
            if (map != null && map.containsKey("url")) {
                PreviewUtil.convertToJpg(OSSUtil.removeDomain((String) map.get("url")), "jpg");
            }
        } catch (ServerException | IOException e) {
            logger.error("uploadFile:{}, error:{}", originName, e.getMessage());
        }

        return map;
    }

    @Override
    public boolean isImageSuffix(String suffix) {
        String[] suffixs = { "BMP", "JPG", "JPEG", "PNG", "GIF", "BMP" };
        for (int i = 0; i < suffixs.length; i++) {
            if (suffixs[i].equalsIgnoreCase(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void recoverAttachmentById(Long id) {
        Criteria criteria = Criteria.AND().eq(SC.id, id);
        String json = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        int count = getRowCount(json);
        if (count > 0) {
            batchSetIsDeleted(json, false);
        }
    }

    @Override
    public IAttachmentRowSet save(Long RelateToID, String RelateToType, List<Map<String, Object>> attachments) {
        IAttachmentRowSet rowSet = this.createRowSet();
        if (null != attachments) {
            for (Map<String, Object> attachment : attachments) {
                Long id = attachment.containsKey(SC.id) ? ConvertUtil.toLong(attachment.get(SC.id).toString()) : null;
                IAttachmentRow row = null;
                if (id != null) {
                    row = findByIdWithOutAuth(id);
                }
                if (row == null) {
                    row = createRow();
                }
                row.setCategory((String) attachment.get(AttachmentMetaData.category));
                row.setFileName((String) attachment.get(AttachmentMetaData.fileName));
                row.setFileDir((String) attachment.get(AttachmentMetaData.fileDir));
                if(attachment.get(AttachmentMetaData.size)!=null)
                	row.setSize(ConvertUtil.toLong(attachment.get(AttachmentMetaData.size).toString()));
                if(attachment.get(AttachmentMetaData.imgSize)!=null)
                	row.setImgSize(attachment.get(AttachmentMetaData.imgSize).toString());
                row.setFileType((String) attachment.get(AttachmentMetaData.fileType));
                row.setExtend((String) attachment.get(AttachmentMetaData.extend));
                row.setThumbUri((String) attachment.get(AttachmentMetaData.thumbUri));
                row.setRealUri((String) attachment.get(AttachmentMetaData.realUri));
                row.setIsImg(attachment.containsKey(AttachmentMetaData.isImg)
                        ? ConvertUtil.toBoolean(attachment.get(AttachmentMetaData.isImg).toString()) : false);
                row.setRelateToID(RelateToID);
                row.setRelateToType(RelateToType);
                upsert(row);
                // 更新返回值
                attachment.put(AttachmentMetaData.relateToID, RelateToID);
                attachment.put(AttachmentMetaData.relateToType, RelateToType);
                attachment.put(SC.id, row.getId());
                rowSet.addRow(row);
            }
        }
        return rowSet;
    }

    private IAttachmentRow findByIdWithOutAuth(Long id) {
        IAttachmentRow row = query(id);
        if (row != null) {
/*            Boolean isDelete = (Boolean) row.getFieldValue(SC.isDeleted);
            if (Boolean.TRUE.equals(isDelete)) {
                return null;
            }*/
            return row;
        }
        return null;
    }

    @Override
    public Map<Long, List<IAttachmentRow>> findWorkRecordAttachments(List<Long> ids) {
        Map<Long, List<IAttachmentRow>> map = new HashMap<Long, List<IAttachmentRow>>();
        if (ids != null && ids.size() > 0) {
            IAttachmentRowSet rowSet = findRowSetByRelate(WorkRecordMetaData.EOName, ids);
            List<IAttachmentRow> attachs = new ArrayList<IAttachmentRow>();
            if (rowSet != null && rowSet.getAttachmentRows() != null) {
                attachs = rowSet.getAttachmentRows();
            }
            for (IAttachmentRow attachment : attachs) {
                Long wrId = attachment.getRelateToID();
                if (!map.containsKey(wrId)) {
                    List<IAttachmentRow> tmp = new ArrayList<IAttachmentRow>();
                    tmp.add(attachment);
                    map.put(wrId, tmp);
                } else {
                    map.get(wrId).add(attachment);
                }
            }
        }
        return map;
    }

    @Override
    public void deleteAttachmentByRelate(String relateToType, Long relateToId) {
        Criteria criteria = Criteria.AND().eq(AttachmentMetaData.relateToType, relateToType)
                .eq(AttachmentMetaData.relateToID, relateToId);
        String json = JsonQueryBuilder.getInstance().addCriteria(criteria).toJsonQuerySpec();
        IBusinessObjectRowSet rowSet = QueryLimitUtil.query(json, this.getBusinessObjectHome());
        if(rowSet!=null&&rowSet.getRows()!=null){
        	for(IBusinessObjectRow row:rowSet.getRows()){
        		Long id =(Long)row.getFieldValue(SC.id);
        		deleteRowWithRecycle(id);
        	}
        }
    }

    @Override
    public List<IAttachmentRow> findAttachmentListByRelate(String relateToType, Long relateToId) {
        List<IAttachmentRow> rowList = new ArrayList<IAttachmentRow>();
        IAttachmentRowSet rowSet = findAttachmentByRelate(relateToType, relateToId, false);
        if (rowSet != null && rowSet.getAttachmentRows() != null) {
            rowList = rowSet.getAttachmentRows();
        }
        return rowList;
    }

    @Override
    public IAttachmentRowSet findWRAttachmentsSet(Long id) {
        return findAttachmentByRelate(EO.WorkRecord, id, false);
    }

    @Override
    public Map<Long, List<IAttachmentRow>> findCommentsAttachments(List<Long> ids) {
        Map<Long, List<IAttachmentRow>> map = new HashMap<Long, List<IAttachmentRow>>();
        if (ids != null && ids.size() > 0) {
            IAttachmentRowSet attachs = getCommentAttachments(ids);
            if (attachs != null) {
                for (int i = 0; i < attachs.size(); i++) {
                    IAttachmentRow attachment = attachs.getRow(i);
                    Long wrId = attachment.getRelateToID();
                    if (!map.containsKey(wrId)) {
                        List<IAttachmentRow> tmp = new ArrayList<IAttachmentRow>();
                        tmp.add(attachment);
                        map.put(wrId, tmp);
                    } else {
                        map.get(wrId).add(attachment);
                    }
                }
            }
        }
        return map;
    }

    private IAttachmentRowSet getCommentAttachments(List<Long> ids) {
        if (ids != null && ids.size() > 0) {
            IAttachmentRowSet attachs = findAttachmentByRelate(EO.Comment, ids);
            return attachs;
        }
        return null;
    }

    private IAttachmentRowSet findAttachmentByRelate(String relateToType, List<Long> relateToIds) {
        if (relateToIds.size() > 0) {
            Criteria criteria = Criteria.AND();
            //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
            criteria.eq(AttachmentMetaData.relateToType, relateToType);
            criteria.in(AttachmentMetaData.relateToID, relateToIds.toArray());
            JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
            jsonQueryBuilder.addCriteria(criteria);
            return queryAll(jsonQueryBuilder.toJsonQuerySpec());
        }
        return null;
    }

    @Override
    public List<Boolean> cutImage(Map<String, Object> map) {
        List<List<String>> version = new ArrayList<List<String>>();
        String url = map.get("url").toString();
        createVersion(version, map, 960, 720, "big");
        createVersion(version, map, 688, 516, "middle");
        createVersion(version, map, 256, 192, "small");
        String path = OSSUtil.removeDomain(url);
        List<Boolean> result = new OssServiceImpl().convertImageFile(path, version);
        return result;
    }

    private void createVersion(List<List<String>> version, Map<String, Object> map, Integer width, Integer height,
            String flag) {
        Integer owidth = (Integer) map.get("width");
        Integer oheight = (Integer) map.get("height");
        String suffix = map.get("suffix").toString();
        Map<String, Integer> size = get916(owidth, oheight, width, height);
        List<String> list = new ArrayList<String>();
        list.add("." + flag + "." + suffix);
        list.add("0");
        list.add("0");
        list.add(size.get("height").toString());
        version.add(list);
    }

    private Map<String, Integer> get916(int width, int height, int w, int h) {
        int outWidth = width;
        int outHeight = height;
        if (height / width > 9 / 16) {
            if (height > h) {
                outHeight = h;
                outWidth = h * width / height;
            }
        } else {
            if (width > w) {
                outWidth = w;
                outHeight = w * height / width;
            }
        }
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("width", outWidth);
        map.put("height", outHeight);
        return map;
    }

    @Override
    public Map<String, Object> uploadTransFileToMp3(String originName, InputStream is) {
        HashMap<String, Object> map = null;

        try {
            File file = null;
            file = File.createTempFile("csp", ".tmp");
            FileUtil.copyInputStreamToFile(is, file);
            String suffix = originName.substring(originName.lastIndexOf(".") + 1);

            String realname = OSSUtil.getFileRealname(originName);
            String filename = OSSUtil.genFilename(suffix);

            long filesize = file.length();
            logger.info("[updateSpx]:filesize:" + filesize);

            OssServiceImpl ossService = new OssServiceImpl();
            boolean result = false;
            try{
            	String retString=CspCssUtil.getOssService().writeFileAndConvertSpxToMp3(filename, file);
            	result = true;
            }
            catch(Exception e){
                logger.info(e.getMessage());
            }
            //TODO writeFileAndConvertSpxToMp3
   /*         boolean result = ossService.writeFileAndConvertSpxToMp3(filename, file);*/       
            if (result) {
                if (ossService.getBucket() == null) {
                    ossService.createPublicBucket();
                }
                String url = OssReader.getDomain("oss.preview.domain") + "/" + ossService.getBucketName() + filename;
                map = new HashMap<String, Object>();
                map.put("url", "http://"+url);
                map.put("name", realname);
                map.put("size", "");
                map.put("suffix", suffix);
            }
        } catch (Exception e) {
            logger.error("writeFileAndConvertSpxToMp3 error", e);
        }

        return map;
    }

    @Override
    public List<Long> findAttachmentIdsByRelate(String relateToType, List<Long> relateToIds) {
        List<Long> idList = new ArrayList<Long>();
        Criteria criteria = Criteria.AND();
        //criteria.addChild(Criteria.OR().empty(SC.isDeleted).eq(SC.isDeleted, false));
        if (relateToIds == null || relateToIds.size() == 0)
            return idList;       
        criteria.eq(AttachmentMetaData.relateToType, relateToType);
        if(relateToIds.size()<2000){
        	criteria.in(AttachmentMetaData.relateToID,relateToIds.toArray());
        }
  
        JsonQueryBuilder jsonQueryBuilder = JsonQueryBuilder.getInstance();
        jsonQueryBuilder.addCriteria(criteria);
        List<IBusinessObjectRow> rows = QueryLimitUtil.queryList(jsonQueryBuilder.toJsonQuerySpec(), this.getBusinessObjectHome());     
        for (IBusinessObjectRow row :rows) {
            Long id = (Long) row.getFieldValue(AttachmentMetaData.relateToID);
            if (relateToIds.contains(id)) {
                Long attId = (Long) row.getFieldValue(SC.id);
                idList.add(attId);
            }
        }
        return idList;
    }
    
    @Override
    public void preUpsert(IAttachmentRow row, IAttachmentRow origRow) {
		try{
			//native兼容H5数据
			if(row.getExtend()!=null){
				String extendJson =row.getExtend();
				JSONObject extend = AppWorkManager.getDataManager().createJSONObject(extendJson);	
				String width = extend.getString("width");
				String height = extend.getString("height");
				if(StringUtils.isNotEmpty(width)&&StringUtils.isNotEmpty(height)){
					row.setImgSize(width+","+height);
				}
			}
		}catch(Exception e){
			e.printStackTrace();			
		}
    }
    
    @Override
	public  List<Map<String,Object>> getAttachmentRows(List<IAttachmentRow> attachments){
		List<Map<String,Object>> retList = new ArrayList<Map<String,Object>>();
		for(IAttachmentRow attachment:attachments){
			Map<String,Object> dataMap = new HashMap<String,Object>();	
			dataMap.put(AttachmentMetaData.category, attachment.getCategory());
			dataMap.put(AttachmentMetaData.extend, getExtendByImgSize(attachment.getImgSize(),attachment.getExtend()));
			dataMap.put(AttachmentMetaData.fileDir, attachment.getFileDir());
			dataMap.put(AttachmentMetaData.fileName, attachment.getFileName());
			dataMap.put(AttachmentMetaData.fileType, attachment.getFileType());
			dataMap.put(AttachmentMetaData.size, attachment.getSize());
			dataMap.put(SC.id, attachment.getId());
			retList.add(dataMap);
		}
		return retList;
	}
    
	private static String getExtendByImgSize(String imgSize,String extend){
		try{
			if(StringUtils.isNotEmpty(imgSize)){
				String[] sizes = imgSize.split(",");
				if(sizes==null||sizes.length<2)
					return extend;
				Map<String,Object> extendMap = new HashMap<String,Object>();
				if(StringUtils.isNotEmpty(extend)){					
					extendMap = AppWorkManager.getDataManager().jsonStringToMap(extend);
				}
				extendMap.put("width", sizes[0]);
				extendMap.put("height", sizes[1]);
				return 	AppWorkManager.getDataManager().toJSONString(extendMap);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return extend;
	}

	@Override
	public Map<Long, List<IAttachmentRow>> findCheckinsAttachments(List<Long> ids) {
        Map<Long, List<IAttachmentRow>> map = new HashMap<Long, List<IAttachmentRow>>();
        if (ids != null && ids.size() > 0) {
            IAttachmentRowSet rowSet = findRowSetByRelate(CheckinMetaData.EOName, ids);
            List<IAttachmentRow> attachs = new ArrayList<IAttachmentRow>();
            if (rowSet != null && rowSet.getAttachmentRows() != null) {
                attachs = rowSet.getAttachmentRows();
            }
            for (IAttachmentRow attachment : attachs) {
                Long wrId = attachment.getRelateToID();
                if (!map.containsKey(wrId)) {
                    List<IAttachmentRow> tmp = new ArrayList<IAttachmentRow>();
                    tmp.add(attachment);
                    map.put(wrId, tmp);
                } else {
                    map.get(wrId).add(attachment);
                }
            }
        }
        return map;
    
	}
	
	@Override
	public  void getAttachmentsByRelateItem(List<Map<String,Object>> items,String relateType){
		List<Long> checkinIds = new ArrayList<Long>();
		for(Map<String,Object> item:items){			
			Long id = Long.parseLong(item.get(SC.id).toString());
			checkinIds.add(id);
		}
		IAttachmentRowSet attachmentRowSet = this.findRowSetByRelate(relateType, checkinIds);
		Map<Long,List<IAttachmentRow>> attMap = new HashMap<Long,List<IAttachmentRow>>();
		for(IAttachmentRow attRow:attachmentRowSet.getAttachmentRows()){
			if(attMap.containsKey(attRow.getRelateToID())){
				attMap.get(attRow.getRelateToID()).add(attRow);
			}else{
				List<IAttachmentRow> attList = new ArrayList<IAttachmentRow>();
				attList.add(attRow);
				if(attRow.getRelateToID()==null)
					continue;
				attMap.put(attRow.getRelateToID(), attList);
			}
		}
		for(Map<String,Object> item:items){
			Long id = Long.parseLong(item.get(SC.id).toString());
			if(attMap.containsKey(id)){
				List<IAttachmentRow> attList = attMap.get(id);	
				item.put("attachments",getAttachmentRows(attList));				
			}
		}
	}

}
