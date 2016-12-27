package com.chanapp.chanjet.customer.vo.sysc;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.chanjet.csp.bo.api.IBusinessObjectRow;

public class SyscData {
	
	private long timestamp;
	
	private List<SyscEntity> entites = new ArrayList<SyscEntity>();
	
	private List<SyscDeleteEntity> deleteEntites = new ArrayList<SyscDeleteEntity>();
	
	public void addEntity(String entityType, IBusinessObjectRow entity) {
		this.entites.add(new SyscEntity(entityType, entity));
	}
	
	public void addEntities(String entityType, List<IBusinessObjectRow> entities) {
		for(IBusinessObjectRow entity : entities) {
			this.entites.add(new SyscEntity(entityType, entity));
		}
	}
	
	public void addDeleteEntity(String entityType, Long entityId) {
		this.deleteEntites.add(new SyscDeleteEntity(entityType, entityId));
	}
	
	private class SyscDeleteEntity {
		
		private String entityType;
		
		private Long entityId;

		public SyscDeleteEntity(String entityType, Long entityId) {
			this.entityType = entityType;
			this.entityId = entityId;
		}

		public String getEntityType() {
			return entityType;
		}

		public void setEntityType(String entityType) {
			this.entityType = entityType;
		}

		public Long getEntityId() {
			return entityId;
		}

		public void setEntityId(Long entityId) {
			this.entityId = entityId;
		}
 	}

	public long getTimestamp() {
		if(timestamp <= 0) {
			return (new Date()).getTime();
		}
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public List<SyscEntity> getEntites() {
		return entites;
	}

	public void setEntites(List<SyscEntity> entites) {
		this.entites = entites;
	}

	public List<SyscDeleteEntity> getDeleteEntites() {
		return deleteEntites;
	}

	public void setDeleteEntites(List<SyscDeleteEntity> deleteEntites) {
		this.deleteEntites = deleteEntites;
	}
}


