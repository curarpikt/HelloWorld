package com.chanapp.chanjet.customer.vo.sysc;

import com.chanapp.chanjet.customer.util.BoRowConvertUtil;
import com.chanapp.chanjet.customer.vo.Row;
import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class SyscEntity {
	
	private String entityType;
	
	private String operationType;
	
	private Row entity;
	
	public SyscEntity(String entityType, IBusinessObjectRow entity) {
		this.entityType = entityType;
		this.entity = BoRowConvertUtil.toRow(entity);
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Row getEntity() {
		return entity;
	}

	public void setEntity(Row entity) {
		this.entity = entity;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
}
