package com.chanapp.chanjet.customer.layout;

import java.util.List;

import com.chanapp.chanjet.customer.cache.CustomerLayout;
import com.chanapp.chanjet.customer.constant.EO;
import com.chanapp.chanjet.customer.constant.FT;
import com.chanapp.chanjet.customer.constant.SRU;
import com.chanapp.chanjet.customer.constant.metadata.WorkRecordMetaData;
import com.chanjet.csp.appmanager.AppWorkManager;
import com.chanjet.csp.cmr.api.IAppMetadataManager;
import com.chanjet.csp.cmr.api.metadata.customization.ICustomizationSession;
import com.chanjet.csp.cmr.api.metadata.userschema.type.entity.IEntity;
import com.chanjet.csp.common.base.exception.AppException;
import com.chanjet.csp.common.base.util.StringUtils;

public class LayoutManager {
	
	public static void initLayout() {
		// 定义布局实体。客户和联系人
		String[] layoutEOViews = new String[] { EO.Customer, EO.Contact };
		for (String EOview : layoutEOViews) {
			LayoutBO layoutBO = new LayoutBO(EOview);
			layoutBO.initBOLayout();
		}
	}
	
	public static CustomerLayout getLayout() {
		CustomerLayout layout = new CustomerLayout();
		LayoutBO customerlayoutBO = new LayoutBO(EO.Customer);
		layout.setCustomerEdit(customerlayoutBO.getFieldDatas(LayoutBO._sortEdit));
		layout.setCustomerManager(customerlayoutBO.getFieldDatas(LayoutBO._sortManager));
		layout.setCustomerView(customerlayoutBO.getFieldDatas(LayoutBO._sortView));
		LayoutBO contactlayoutBO = new LayoutBO(EO.Contact);
		layout.setContactManager(contactlayoutBO.getFieldDatas(LayoutBO._sortManager));
		layout.setContactView(contactlayoutBO.getFieldDatas(LayoutBO._sortView));
		return layout;
	}

	public static List<String> getDisableFields(String eoName) {
		LayoutBO contactlayoutBO = new LayoutBO(eoName);
		return contactlayoutBO.getDisableFields();
	}

	public static void sortFields(List<String> sortList, String eoName, Long type) {
		String view = "";
		if (type == FT.VIEW) {
			view = LayoutBO._sortView;
		}
		if (type == FT.EDIT) {
			view = LayoutBO._sortEdit;
		}
		if (type == FT.MANAGE) {
			view = LayoutBO._sortManager;
		}
		LayoutBO layoutBO = new LayoutBO(eoName);
		layoutBO.sortFields(sortList, view);
	}

	public static String[] getCustomerEditFields() {
		LayoutBO layoutBO = new LayoutBO(EO.Customer);
		return layoutBO.getFieldsSort(LayoutBO._sortEdit);
	}

	public static String[] getContactEditFields() {
		LayoutBO layoutBO = new LayoutBO(EO.Contact);
		return layoutBO.getFieldsSort(LayoutBO._sortEdit);
	}

	public static void changeFieldStatus(String tableName, List<String> fields, Long status) {
		LayoutBO layoutBO = new LayoutBO(tableName);
		//暂时只支持一个字段
/*		if (fields == null || fields.size() == 0 || fields.size() > 1) {
			throw new AppException("app.layout.disable.paraerror");
		}*/
		for(String field:fields){
			if (SRU.FIELD_STATUS_DISABLE.equals(status)) {
				layoutBO.changeFieldStatus(field, true);
			} else if (SRU.FIELD_STATUS_ENABLE.equals(status)){
				layoutBO.changeFieldStatus(field, false);
			}
		}

	}

	public static void updateSortByAddField(String eoName, String field) {
		LayoutBO layoutBO = new LayoutBO(eoName);
		layoutBO.updateSortByAddField(field);
	}
	
	public static void setFieldPattern(String eoName,String field, String allowBlank) {
		LayoutBO layoutBO = new LayoutBO(eoName);
		layoutBO.setFieldPattern(field, allowBlank);		
	}
	
	
}
