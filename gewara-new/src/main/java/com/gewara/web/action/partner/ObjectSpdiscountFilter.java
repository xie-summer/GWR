package com.gewara.web.action.partner;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.helper.discount.SpecialDiscountHelper;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BeanUtil;

public class ObjectSpdiscountFilter extends SpdiscountFilter {
	protected String citycode;
	protected String tag;
	protected Long relatedid;
	protected Timestamp addtime;
	
	public ObjectSpdiscountFilter(String citycode, String tag, Long relatedid, Timestamp addtime){
		this.citycode = citycode;
		this.tag = tag;
		this.relatedid = relatedid;
		this.addtime = addtime;
	}

	@Override
	public boolean excludeOpi(SpecialDiscount item) {
		return excludeOpi(item, true);
	}
	
	protected boolean excludeOpi(SpecialDiscount item, boolean isJudgeCategory){
		if(!SpecialDiscountHelper.isEnabledByCitycode(item, citycode)) return true;
		List<Long> idList = null;
		if(ServiceHelper.isTag(tag) && StringUtils.isNotBlank(item.getRelatedid())){
			idList = BeanUtil.getIdList(item.getRelatedid(), ",");
		}else if(isJudgeCategory && ServiceHelper.isCategory(tag) && StringUtils.isNotBlank(item.getCategoryid())){
			idList = BeanUtil.getIdList(item.getCategoryid(), ",");
		}
		if(!CollectionUtils.isEmpty(idList) && !idList.contains(relatedid)) return true;
		//限制周几
		if(!SpecialDiscountHelper.isEnabledByAddweek(item, addtime)) return true;
		//限制下单时间
		if(!SpecialDiscountHelper.isEnabledByAddtime(item, addtime)) return true;
		return false;
	}

}
