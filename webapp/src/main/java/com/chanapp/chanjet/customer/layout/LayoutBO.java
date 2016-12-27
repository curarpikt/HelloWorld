package com.chanapp.chanjet.customer.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.metadata.ContactMetaData;
import com.chanapp.chanjet.customer.constant.metadata.CustomerMetaData;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanapp.chanjet.customer.service.layout.FieldVO;
import com.chanapp.chanjet.web.constant.SC;
import com.chanapp.chanjet.web.reader.PropertiesReader;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.customization.IBOFieldForCustomization;
import com.chanjet.csp.cmr.api.metadata.customization.IBusinessObjectForCustomization;
import com.chanjet.csp.cmr.api.metadata.customization.ICustomizationSession;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.IBusinessObject;
import com.chanjet.csp.cmr.api.metadata.userschema.type.businessObject.field.IBOField;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.field.IField;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class LayoutBO {

	public final static String _initFlag = "inited";
	public final static String _sortEdit = "sortEdit";
	public final static String _sortView = "sortView";
	public final static String _sortManager = "sortManager";
	private final static String _customerEditFields = "customerEditFields";
	private final static String _customerMangerFields = "customerMangerFields";
	private final static String _contactFields = "contactFields";
	private final static String _allowBlank = "allowBlank";
	private final static String _disabled = "disabled";
	private final static String _fieldPattern = "fieldPattern";
	private final static String _noBlank = "noBlank";
	// 字段类型
	public final static Long FIELD_STATUS_ENABLE = 1L;
	public final static Long FIELD_STATUS_DISABLE = 0L;
	final static PropertiesReader reader = PropertiesReader.getInstance("customer/layout.properties");

	private IBusinessObject bo;
	private IEntity eo;
	private String BOName;
	private String entityName;
	protected static final IAppMetadataManager metaDataManager = AppWorkManager.getAppMetadataManager();

	public LayoutBO(String entityName) {
		this.eo = metaDataManager.getEntityByName(entityName);
		this.BOName = entityName + "BO";
		this.bo = metaDataManager.getBusinessObjectByName(this.BOName);
		//this.boHome = AppWorkManager.getBusinessObjectManager().getPrimaryBusinessObjectHome(BOName);
		this.entityName = entityName;
	}
	public void fixManagerLayout(){
		String initEO = eo.getProperty("initLayout");
		String managerSort = eo.getProperty(_sortManager);
		if (managerSort!=null&&(initEO == null || !initEO.equals(_initFlag))) {
			ICustomizationSession customSession = metaDataManager.startCustomizationSession();
			try {
				IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
				customSession.setProperty(custBO, _sortManager, managerSort);
				customSession.commit();
			} catch (Exception e) {
				if(customSession!=null)
					customSession.rollBack();
				throw e;
			}
		}			
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
			fixRepeatSort(custBO,_sortEdit,customSession);
			fixRepeatSort(custBO,_sortView,customSession);
			customSession.commit();
		} catch (Exception e) {
			if(customSession!=null)
				customSession.rollBack();
			//throw e;
		}
	}
	
	private void fixRepeatSort(IBusinessObjectForCustomization custBO,String view,ICustomizationSession customSession){
		String sortEdit = bo.getProperty(view);
		List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(sortEdit, List.class);
		Set<String> trimFields = new LinkedHashSet<String>();
		boolean hasChange = false;
		for(String sortField:sortFields){
			if(trimFields.contains(sortField.trim())){
				hasChange = true;
			}
			trimFields.add(sortField.trim());
		}
		if(hasChange){
			customSession.setProperty(custBO, view, AppWorkManager.getDataManager().toJSONString(trimFields));
		}
	}
	
	
	public void initBOLayout() {
		String inited = bo.getProperty(_sortEdit);	
		// 3.3已初始化过BO布局直接返回
		if (StringUtils.isNotBlank(inited)){
			//修复3.3生成的错误数据
			fixManagerLayout();
			return;
		}
		String initEO = eo.getProperty("initLayout");
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			initFieldSort(customSession, initEO);
			initFieldProperties(customSession, initEO);
			customSession.commit();
		} catch (Exception e) {
			if(customSession!=null)
				customSession.rollBack();
			throw e;
		}
	}
	
	private void initFieldProperties(ICustomizationSession customSession, String initEO) {
		Map<String, IBOField> fieldMap = bo.getFields();
		IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
		// 设置过字段属性
/*		if (initEO != null && initEO.equals(_initFlag)) {

			// FieldDataServiceItf fieldDataService =
			// ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
			FieldDataServiceImpl fieldDataService = new FieldDataServiceImpl();
			List<String> disableFields = fieldDataService.getDisableFields(entityName);
			for (Entry<String, IBOField> entry : fieldMap.entrySet()) {
				String filedName = entry.getKey();
				IBOFieldForCustomization boField = custBO.getField(filedName);
				if (isIngoreField(boField))
					continue;
				IField custField = eo.getField(filedName);
				// 设置字段非空
				setAllowBlankProperty(boField, custField, customSession);
				// 设置字段停用
				if (disableFields.contains(filedName)) {
					customSession.setProperty(boField, _disabled, "true");
				} else {
					customSession.setProperty(boField, _disabled, "false");
				}
			}
		}*/
		// 未设置过的直接在BOFIELD配置初始值
	}

	private static void setAllowBlankProperty(IBOFieldForCustomization boField, IField custField,
			ICustomizationSession customSession) {
		String oldPattern = custField.getProperty(_fieldPattern);
		Boolean value = true;
		if (StringUtils.isNotBlank(oldPattern)) {
			Map<String, Object> allowBlank = AppWorkManager.getDataManager().jsonStringToMap(oldPattern);
			Boolean oldValue = (Boolean) allowBlank.get(_noBlank);
			if (oldValue) {
				value = false;
			}
		}
		customSession.setProperty(boField, _allowBlank, value.toString());
	}

	private boolean isIngoreField(IBOField custField) {
		if (custField.getSystemField() != null && custField.getSystemField()) {
			/*
			 * String name = custField.getName(); if
			 * (SC.owner.equalsIgnoreCase(name)) { return false; }
			 */
			return true;
		}
		if (custField.getParent() == null) {
			return true;
		}
		return false;
	}

	private void initFieldSort(ICustomizationSession customSession, String initEO) {

		// 定义初始化界面。编辑界面、管理界面、卡片界面
		String[] views = new String[] { _sortEdit, _sortManager, _sortView };
		IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());

		// 新开通企业从配置文件获取布局顺序
		if (initEO == null || !initEO.equals(_initFlag)) {
			// 保存字段顺序
			for (String view : views) {
				String sortStr = AppWorkManager.getDataManager().toJSONString(getDefalutSortWithCustom(view));

				customSession.setProperty(custBO, view, sortStr);
			}
		}
		// 非新开通企业会有历史数据，从业务表中获取
/*		else {
			// FieldDataServiceItf fieldDataService =
			// ServiceLocator.getInstance().lookup(FieldDataServiceItf.class);
			FieldDataServiceImpl fieldDataService = new FieldDataServiceImpl();
			for (String view : views) {
				String tempView = view;
				if (view.equals(_sortView)) {
					tempView = _sortEdit;
				}

				String[] sortFields = fieldDataService.getFieldsSort(entityName, tempView);
				List<String> fieldList = new ArrayList<String>();
				Collections.addAll(fieldList, sortFields);
				// 客户的卡片界面
				if (view.equals(_sortView) && entityName.equals(EO.Customer)) {
					// 特殊字段，不参加排序
					String[] customerAddFields = reader.getString("customterAddFields").split(",");
					Collections.addAll(fieldList, customerAddFields);
				}
				customSession.setProperty(custBO, view, AppWorkManager.getDataManager().toJSONString(fieldList));
			}

		}*/
	}

	/**
	 * <p>
	 * 获取默认字段顺序。
	 * </p>
	 * 
	 * @param table
	 * @param view
	 * @return
	 *
	 * @author : lf
	 * @date : 2016年6月12日
	 */
	private List<String> getDefalutSortWithCustom(String view) {
		List<String> retList = new ArrayList<String>();
		String[] baseFields = _getDefalutFields(this.entityName, view);
		for (String basefield : baseFields) {
			if(basefield!=null)
				retList.add(basefield.trim());
		}
		Map<String, IField> customFields = eo.getCustomFields();
		Set<String> customSets = customFields.keySet();
		for (String customField : customSets) {			
			retList.add(customField.trim());
		}
		return retList;
	}

	private String[] _getDefalutFields(String table, String view) {
		if (CustomerMetaData.EOName.equals(table)) {
			if (_sortEdit.equals(view) || _sortView.equals(view)) {
				String fields = reader.getString(_customerEditFields);
				return fields.split(",");
			} else if (_sortManager.equals(view)) {
				String fields = reader.getString(_customerMangerFields);
				return fields.split(",");
			}
		} else if (ContactMetaData.EOName.equals(table)) {
			String fields = reader.getString(_contactFields);
			return fields.split(",");
		}
		return null;
	}

	public ArrayList<Map<String, Object>> getFieldDatas(String view) {
		// List<String> sortFields = new ArrayList<String>();
		String sortStr = bo.getProperty(view);
		List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(sortStr, List.class);
		List<String>  trimedSet = new ArrayList<String>();
		for(String sortfield: sortFields){
			trimedSet.add(sortfield.trim());
		}
		if(_sortView.equals(view)&&entityName.equals("Customer")){	
				if(!trimedSet.contains(CustomerMetaData.status))
					trimedSet.add(CustomerMetaData.status);	
				if(!trimedSet.contains(SC.owner))
					trimedSet.add(SC.owner);
				if(!trimedSet.contains(SC.createdBy))
					trimedSet.add(SC.createdBy);
				if(!trimedSet.contains(SC.createdDate))
					trimedSet.add(SC.createdDate);
				if(!trimedSet.contains(SC.lastModifiedDate))
					trimedSet.add(SC.lastModifiedDate);
		}
		
		if (_sortManager.equals(view)) {
			return _getEditFieldAttr(trimedSet, true);
		} else {
			return _getEditFieldAttr(trimedSet, false);
		}
	}

	private ArrayList<Map<String, Object>> _getEditFieldAttr(List<String> sortFields, boolean isMangager) {
		Map<String, IField> customFields = eo.getCustomFields();
		Set<String> self = customFields.keySet();
		ArrayList<Map<String, Object>> retList = new ArrayList<Map<String, Object>>();		
		if(isMangager){
			List<String> disableFields = getDisableFields();	
			for(String disableField: disableFields){
				if(!sortFields.contains(disableField)){
					sortFields.add(disableField);
				}
			}
		}
		
		for (String field : sortFields) {
			field = field.trim();
			IBOField boField = bo.getField(field);
			String disabled = null;
			String canEditValue = null;
			String canDisable = null;
			if (boField != null) {
				disabled = boField.getProperty(_disabled);
				canEditValue = boField.getProperty("canEditValue");
				canDisable = boField.getProperty("canDisable");
			}
			Map<String, Object> layout = new HashMap<String, Object>();
			if (isMangager) {
				// 停用的字段
				if (disabled != null && disabled.equals("true")) {
					layout.put("status", FIELD_STATUS_DISABLE.toString());
				} else {
					layout.put("status", FIELD_STATUS_ENABLE.toString());
				}
				// 是否可以编辑lable
				if (canDisable != null && canDisable.equals("false")) {
					layout.put("canDisable", "false");
				} else {
					layout.put("canDisable", "true");
				}
				// 是否可以编辑lable
				if (canEditValue != null && canEditValue.equals("false")) {
					layout.put("canEdit", "false");
				} else {
					layout.put("canEdit", "true");
				}
			} else {
				// 停用的字段
				if (disabled != null && disabled.equals("true")) {
					continue;
				}
			}

			String fieldName = field;
			if ("remark".equals(fieldName) || "address".equals(fieldName)) {
				layout.put("fieldType", "LongText");
			}
			if (fieldName.equals("createdBy") || fieldName.equals("lastModifiedBy") || fieldName.equals("owner")
					|| fieldName.equals("customer")) {
				layout.put("displayField", "name");
			}
			/*
			 * if (canEditValue!=null&&canEditValue.equals("false")) {
			 * layout.put("canEdit", "false"); } else { layout.put("canEdit",
			 * "true"); }
			 */
			if (self.contains(fieldName)) {
				layout.put("self", "true");
			} else {
				layout.put("self", "false");
			}
			String fieldPatttern = _getFieldPatttern(boField);
			if (fieldPatttern != null) {
				layout.put("extend", fieldPatttern);
			}
			layout.put("name", field);
			retList.add(layout);
		}


		
		return retList;
	}

	private String _getFieldPatttern(IBOField custField) {
		if (custField == null)
			return null;
		String allowBlank = custField.getProperty(_allowBlank);
		FieldVO pattternVO = new FieldVO();
		if (allowBlank != null && allowBlank.equals("false")) {
			pattternVO.setNoBlank(true);
		} else {
			pattternVO.setNoBlank(false);
		}
		return AppWorkManager.getDataManager().toJSONString(pattternVO);
	}

	public List<String> getDisableFields() {
		List<String> retList = new ArrayList<String>();
		Map<String, IBOField> fieldMap = bo.getFields();
		for (Entry<String, IBOField> entry : fieldMap.entrySet()) {
			IBOField field = entry.getValue();
			if ("true".equals(field.getProperty(_disabled))) {
				retList.add(field.getName());
			}
		}
		Map<String, IBOField> customfieldMap = this.bo.getCustomFields();
		for (Entry<String, IBOField> entry : customfieldMap.entrySet()) {
			IBOField field = entry.getValue();
			if ("true".equals(field.getProperty(_disabled))) {
				retList.add(field.getName());
			}
		}
		return retList;
	}

	public void sortFields(List<String> sortList, String view) {
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
			customSession.setProperty(custBO, view, AppWorkManager.getDataManager().toJSONString(sortList));
			if (view.equals(_sortEdit) && entityName.equals(CustomerMetaData.EOName)) {
				String[] customerAddFields = reader.getString("customterAddFields").split(",");
				if (customerAddFields != null) {
					for (String addField : customerAddFields) {
						if (!sortList.contains(addField)) {
							sortList.add(addField.toString());
						}
					}
				}
				customSession.setProperty(custBO, _sortView, AppWorkManager.getDataManager().toJSONString(sortList));
			}
			customSession.commit();
		} catch (Exception e) {
			customSession.rollBack();
			throw new AppException("app.layout.sort.sorterror");
		}

	}

	public String[] getFieldsSort(String view) {
		String sortStr = this.bo.getProperty(view);
		List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(sortStr, List.class);
		return sortFields.toArray(new String[sortFields.size()]);
	}

	public void changeFieldStatus(String field, boolean status) {
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
			
			IBOFieldForCustomization custField = custBO.getField(field);
			if(custField!=null)
				customSession.setProperty(custField, _disabled, Boolean.toString(status));			
			if(!entityName.equals(WorkRecordMetaData.EOName)){
				// 更新管理界面字段排序
				String managerSortStr = custBO.getProperty(_sortManager);
				List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(managerSortStr, List.class);
				// 停用字段
				if (status) {
					sortFields.remove(field);
					sortFields.add(field);
					customSession.setProperty(custBO, _sortManager,
							AppWorkManager.getDataManager().toJSONString(sortFields));
				}
				// 启用字段
				else {
					List<String> sortList = new ArrayList<String>();
					boolean inserted = false;
					for (String sortField : sortFields) {
						sortField=sortField.trim();
						if (sortField.equals(field))
							continue;
						IBOFieldForCustomization custsortField = custBO.getField(sortField);
						String isDisable = custsortField.getProperty(_disabled);
						if ("true".equals(isDisable) && inserted == false) {
							sortList.add(field);
							inserted = true;
							sortList.add(sortField);
						} else {
							sortList.add(sortField);
						}
					}
					if (inserted == false)
						sortList.add(field);
					customSession.setProperty(custBO, _sortManager, AppWorkManager.getDataManager().toJSONString(sortList));
					checkAddField(custBO,_sortEdit,customSession);
					checkAddField(custBO,_sortView,customSession);
				}
			}

			customSession.commit();
		} catch (Exception e) {
			e.printStackTrace();
			customSession.rollBack();
			throw new AppException("app.layout.disable.paraerror");
		}
	}
	
	private void checkAddField(IBusinessObjectForCustomization custBO,String view,ICustomizationSession customSession){	
		String[] fields = _getDefalutFields(this.entityName, view);
		String sortEdit = custBO.getProperty(view);
		List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(sortEdit, List.class);
		List<String> trimFields = new ArrayList<String>();
		for(String sortField:sortFields){
			trimFields.add(sortField.trim());
		}
		boolean hasChange = false;
		for(String field:fields){
			if(!trimFields.contains(field.trim())){
				trimFields.add(field);
				hasChange = true;
			}
		}
		if(hasChange){
			customSession.setProperty(custBO, view, AppWorkManager.getDataManager().toJSONString(trimFields));
		}
	}


	public void updateSortByAddField(String field) {
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			String[] views = new String[] { _sortEdit, _sortManager, _sortView };
			for (String view : views) {
				String sortStr = this.bo.getProperty(view);
				List<String> sortFields = AppWorkManager.getDataManager().fromJSONString(sortStr, List.class);
				if (!sortFields.contains(field)) {
					sortFields.add(field);
					IBusinessObjectForCustomization custBO = customSession
							.getBusinessObjectForCustomizationById(bo.getId());
					customSession.setProperty(custBO, view, AppWorkManager.getDataManager().toJSONString(sortFields));
				}
			}
			customSession.commit();
		} catch (Exception e) {
			customSession.rollBack();
			throw new AppException("app.attribute.addField.addError");
		}
		// 编辑界面、管理界面、卡片界面

	}

	public void setFieldPattern(String field, String allowBlank) {
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try {
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());
			IBOFieldForCustomization custField = custBO.getField(field);
			customSession.setProperty(custField, _allowBlank, allowBlank);
			customSession.commit();
		} catch (Exception e) {
			customSession.rollBack();
			throw new AppException("app.attribute.AllowBlank.addError");
		}
	}
	
	public void updateFiledLabel(String field,String lable){
		ICustomizationSession customSession = metaDataManager.startCustomizationSession();
		try{
			IBusinessObjectForCustomization custBO = customSession.getBusinessObjectForCustomizationById(bo.getId());	
			IBOFieldForCustomization custField = custBO.getField(field);
			customSession.setLabel(custField, lable);	
			customSession.commit();
		}catch (Exception e) {
			customSession.rollBack();
			throw new AppException("app.attribute.updateFiledLabel.error");
		}


	
	}

}
