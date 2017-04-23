package com.gewara.service.partner;

import java.util.List;

import com.gewara.model.partner.Merchant;

public interface MerchantService {
	List<Merchant> getMerchantList(String company);
	void saveMerchant(Merchant merchant, String loginpass);

}
