package com.chanapp.chanjet.customer.service.metadata;

import java.util.ArrayList;
import java.util.List;

import com.chanapp.chanjet.customer.cache.CSPEnumValue;

public class FiledSave {

	private String entityName;
	private String fieldName;
	private String fieldType;
	private String fieldLabel;

	private boolean hidden;
	
	public boolean isHidden() {
		return hidden;
	}
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}
	
	private String localId;
	
	public String getLocalId() {
		return localId;
	}
		



	public void setLocalId(String localId) {
		this.localId = localId;
	}
	private List<CSPEnumValue> enumValues = new ArrayList<CSPEnumValue>();
	private String enumName;
	public String getEnumName() {
		return enumName;
	}
	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public List<CSPEnumValue> getEnumValues() {
		return enumValues;
	}
	public void setEnumValues(List<CSPEnumValue> enumValues) {
		this.enumValues = enumValues;
	}
	public String getEntityName() {
		return entityName;
	}
	public void setEntityName(String entityName) {
		this.entityName = entityName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public String getFieldLabel() {
		return fieldLabel;
	}
	public void setFieldLabel(String fieldLabel) {
		this.fieldLabel = fieldLabel;
	}




}
