package com.chanapp.chanjet.customer.service.importrecord;

public class ImportData {
	public String field;
	public String value;
	public String errorType;
	public String msg;
	public Integer length;
	public String type;
	public String enumName;
	
	public Object insertValue;

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public Object getInsertValue() {
		return insertValue;
	}

	public void setInsertValue(Object insertValue) {
		this.insertValue = insertValue;
	}

	public String getEnumName() {
		return enumName;
	}

	public void setEnumName(String enumName) {
		this.enumName = enumName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
