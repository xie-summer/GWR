package com.gewara.model.drama;

import com.gewara.model.common.BaseSettle;
import com.gewara.model.pay.SettleConfig;
import com.gewara.util.DateUtil;

public class DramaSettle extends BaseSettle {

	private static final long serialVersionUID = 3859456713215899179L;
	private Long dramaid;					//Ω·À„œÓƒø
	
	public DramaSettle(){}
	
	public DramaSettle(Long dramaid, SettleConfig settle){
		this.dramaid = dramaid;
		this.settle = settle;
		this.addtime = DateUtil.getCurFullTimestamp();
	}

	@Override
	public String getSettletype() {
		return "drama";
	}

	public Long getDramaid() {
		return dramaid;
	}

	public void setDramaid(Long dramaid) {
		this.dramaid = dramaid;
	}

}
