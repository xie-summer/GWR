package com.gewara.web.action.merchant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.gewara.Config;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.partner.Merchant;
import com.gewara.util.BeanUtil;
import com.gewara.web.action.AnnotationController;

public class BaseMerchantController extends AnnotationController{
	@Autowired@Qualifier("config")
	protected Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	protected boolean hasRights(Merchant merchant, Long cinemaid){
		return BeanUtil.getIdList(merchant.getRelatelist(), ",").contains(cinemaid);
	}
	protected Merchant getLogonMerchant(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth!=null && auth.isAuthenticated()){//µÇÂ¼
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			if(user instanceof Merchant) return (Merchant)user;
		}
		return null;
	}
}
