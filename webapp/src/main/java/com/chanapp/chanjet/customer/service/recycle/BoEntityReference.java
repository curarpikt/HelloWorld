package com.chanapp.chanjet.customer.service.recycle;

import java.util.List;

public interface BoEntityReference {
	public List<BoEntityRelationship> getSourcesOf(String boName);
	public List<BoEntityRelationship> getTargetsOf(String boName);
}
