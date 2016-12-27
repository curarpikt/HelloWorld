package com.chanapp.chanjet.customer.service.recycle;

public class BoSubjectRelationship {
	private String sourcePk;
	private String targetPk;
	
	public BoSubjectRelationship(String sourcePk, String targetPk) {
		this.sourcePk = sourcePk;
		this.targetPk = targetPk;
	}
	public String getSourcePk() {
		return sourcePk;
	}
	public void setSourcePk(String sourcePk) {
		this.sourcePk = sourcePk;
	}
	public String getTargetPk() {
		return targetPk;
	}
	public void setTargetPk(String targetPk) {
		this.targetPk = targetPk;
	}
}
