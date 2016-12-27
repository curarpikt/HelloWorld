package com.chanapp.chanjet.customer.test.recycle;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;
import com.chanapp.chanjet.customer.service.recycle.BoSubjectRelationship;

public class BoEntityRelationshipTest {

	private Map<BoEntityRelationship, List<BoSubjectRelationship>> relationships = new HashMap<>();
	
	@Before
	public void setUp() {
		String sourceBo = "Contact";
		String sourceField = "customer";
		String targetBo = "Customer";
		String targetField = "id";			
		BoEntityRelationship entityRelationship1 = new BoEntityRelationship(sourceBo, sourceField, targetBo, targetField);
		BoEntityRelationship entityRelationship2 = new BoEntityRelationship(sourceBo, sourceField, targetBo, targetField);

		BoSubjectRelationship subjectRelationship1 = new BoSubjectRelationship("sourcePk1", "targetPk1");		
		BoSubjectRelationship subjectRelationship2 = new BoSubjectRelationship("sourcePk2", "targetPk2");
		
		addRelationship(entityRelationship1, subjectRelationship1);
		addRelationship(entityRelationship2, subjectRelationship2);
	}
	
	private void addRelationship(BoEntityRelationship entityRelationship, BoSubjectRelationship subjectRelationship) {
		List<BoSubjectRelationship> subjectRelationships = relationships.get(entityRelationship);
		subjectRelationships = (subjectRelationships == null) ? new ArrayList<BoSubjectRelationship>() : subjectRelationships;
		subjectRelationships.add(subjectRelationship);
		relationships.put(entityRelationship, subjectRelationships);		
	}

	@Test
	public void test() {		
		assertEquals(1, relationships.keySet().size());
		BoEntityRelationship entityRelationship = 
				new BoEntityRelationship("Contact", "customer", "Customer", "id");
		assertEquals(2, relationships.get(entityRelationship).size());
	}

}
