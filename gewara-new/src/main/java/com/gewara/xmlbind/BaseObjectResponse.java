package com.gewara.xmlbind;



public class BaseObjectResponse extends BaseInnerResponse {
	private String result;

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}
	
	public Integer toIntValue(){
		return Integer.parseInt(result);
	}
}
