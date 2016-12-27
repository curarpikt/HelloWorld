package com.chanapp.chanjet.customer.service.recycle;

public class BoEntityRelationship {
	private static final String WEAK_REFERENCE_KEY_NAME = "relatetoid";
	private String sourceBo;
	private String sourceField;
	private String targetBo;
	private String targetField;
	// private boolean isWeakReference;

	public BoEntityRelationship(String sourceBo, String sourceField, String targetBo, String targetField) {
		this.sourceBo = sourceBo;
		this.sourceField = sourceField;
		this.targetBo = targetBo;
		this.targetField = targetField;
	}

	public BoEntityRelationship() {		
	}

	public String getSourceBo() {
		return sourceBo;
	}
	public void setSourceBo(String sourceBo) {
		this.sourceBo = sourceBo;
	}
	public String getSourceField() {
		return sourceField;
	}
	public void setSourceField(String sourceField) {
		this.sourceField = sourceField;
	}
	public String getTargetBo() {
		return targetBo;
	}
	public void setTargetBo(String targetBo) {
		this.targetBo = targetBo;
	}	
	
	public String getTargetField() {
		return targetField;
	}
	public void setTargetField(String targetField) {
		this.targetField = targetField;
	}

	public int hashCode() {		
		return (sourceBo + sourceField + targetBo + targetField).hashCode();
	}

	public boolean equals(Object obj) {
		if (null == obj) {
			return false;
		}

		if (! (obj instanceof BoEntityRelationship)) {
			return false;
		}

		BoEntityRelationship other = (BoEntityRelationship)obj;
		return other.getSourceBo().equals(this.sourceBo)
			&& other.getSourceField().equals(this.sourceField)
			&& other.getTargetBo().equals(this.targetBo)
			&& other.getTargetField().equals(this.targetField);
	}
	
	public boolean isWeakReference() {
		return WEAK_REFERENCE_KEY_NAME.equals(sourceField.toLowerCase()) || 
			   WEAK_REFERENCE_KEY_NAME.equals(targetField.toLowerCase());
	}

}
