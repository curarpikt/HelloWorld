package com.chanapp.chanjet.customer.vo;


public class ProcessResult {
	
	private boolean isSuccess;

	private Object data;

	public ProcessResult(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

}
