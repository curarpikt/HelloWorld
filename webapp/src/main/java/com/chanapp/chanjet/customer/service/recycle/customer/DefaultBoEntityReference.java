package com.chanapp.chanjet.customer.service.recycle.customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.chanapp.chanjet.customer.service.recycle.BoEntityReference;
import com.chanapp.chanjet.customer.service.recycle.BoEntityRelationship;

public class DefaultBoEntityReference implements BoEntityReference {
	private Map<String, List<BoEntityRelationship>> relationsBySource = new HashMap<>();
	private Map<String, List<BoEntityRelationship>> relationsByTarget = new HashMap<>();

	private static BoEntityReference instance = new DefaultBoEntityReference();
	public static BoEntityReference getInstance() {
		return instance;
	}
	
	public DefaultBoEntityReference() {
		BoEntityRelationship aRelation = new BoEntityRelationship();
		aRelation.setSourceBo("Contact");
		aRelation.setSourceField("customer");
		aRelation.setTargetBo("Customer");
		aRelation.setTargetField("id");
		List<BoEntityRelationship> relations = new ArrayList<>();
		relations.add(aRelation);
		relationsBySource.put("Contact", relations);
		relationsByTarget.put("Customer", relations);
		
	}
	
	@Override
	public List<BoEntityRelationship> getSourcesOf(String targetBoName) {		
		return relationsByTarget.get(targetBoName);
	}

	@Override
	public List<BoEntityRelationship> getTargetsOf(String sourceBoName) {
		return relationsBySource.get(sourceBoName);		
	}

}
