package com.gewara.xmlbind.pay;

import java.util.ArrayList;
import java.util.List;

public class TenBankList {
	private List<TenBank> banks = new ArrayList<TenBank>();

	public List<TenBank> getBanks() {
		return banks;
	}

	public void setBanks(List<TenBank> banks) {
		this.banks = banks;
	}
	public void addBank(TenBank bank){
		this.banks.add(bank);
	}
}
