package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;

public class BoReference {
	private BoEntityRelationship entityRelationship;
	private List<BoSubjectRelationship> subjectRelationships;
	
	public BoReference(BoEntityRelationship entityRelationship,
			 List<BoSubjectRelationship> subjectRelationships) {
		this.entityRelationship = entityRelationship;
		this.subjectRelationships = subjectRelationships;
	}

	public BoEntityRelationship getEntityRelationship() {
		return entityRelationship;
	}

	public List<BoSubjectRelationship> getSubjectRelationships() {
		return subjectRelationships;
	}
	
	public boolean isEmpty() {
		return entityRelationship == null ||
				subjectRelationships == null || subjectRelationships.isEmpty();
	}

}
