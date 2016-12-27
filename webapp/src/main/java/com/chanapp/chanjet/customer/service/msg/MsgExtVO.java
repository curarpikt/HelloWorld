package com.chanapp.chanjet.customer.service.msg;

import java.util.List;

public class MsgExtVO {
	private String action;
	private String target;
	private List<Long> targetId;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public List<Long> getTargetId() {
		return targetId;
	}
	public void setTargetId(List<Long> targetId) {
		this.targetId = targetId;
	}
	
}
