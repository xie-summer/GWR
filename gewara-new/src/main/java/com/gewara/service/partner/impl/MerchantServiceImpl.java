package com.gewara.service.partner.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.gewara.model.movie.Cinema;
import com.gewara.model.partner.Merchant;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.partner.MerchantService;
import com.gewara.util.BeanUtil;
@Service("merchantService")
public class MerchantServiceImpl extends BaseServiceImpl implements MerchantService{
	@Autowired@Qualifier("passwordEncoder")
	private PasswordEncoder passwordEncoder;
	@Override
	public List<Merchant> getMerchantList(String company) {
		DetachedCriteria query = DetachedCriteria.forClass(Merchant.class);
		if(StringUtils.isNotBlank(company)){
			query.add(Restrictions.eq("company", company));
		}
		List<Merchant> result = hibernateTemplate.findByCriteria(query);
		return result;
	}

	@Override
	public void saveMerchant(Merchant merchant, String loginpass) {
		if(StringUtils.isNotBlank(loginpass)){
			merchant.setLoginpass(passwordEncoder.encodePassword(loginpass, null));
		}
		if(StringUtils.isNotBlank(merchant.getRelatelist())){
			List<Long> cinemaidList = BeanUtil.getIdList(merchant.getRelatelist(), ",");
			Map brandMap = baseDao.getObjectPropertyMap(Cinema.class, "id", "brandname", cinemaidList);
			Set brandSet = new HashSet(brandMap.values());
			if(brandSet.size()>1){
				throw new IllegalArgumentException("院线不同：" + brandSet);
			}
		}
		baseDao.saveObject(merchant);
	}
	
}
