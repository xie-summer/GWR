/**
 * 
 */
package com.gewara.service.member.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.model.user.FavoriteTag;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.FavoriteTagService;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.util.StringUtil;

/**
 *    @function FavoriteTagService µœ÷¿‡
 * 	@author bob.hu
 *		@date	2011-02-22 18:15:16
 */
@Service("favoriteTagService")
public class FavoriteTagServiceImpl extends BaseServiceImpl implements FavoriteTagService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public List<FavoriteTag> getRandomFavorList(int count) {
		DetachedCriteria query = DetachedCriteria.forClass(FavoriteTag.class);
		query.addOrder(Order.desc("clickcount"));
		List<FavoriteTag> list = readOnlyTemplate.findByCriteria(query, 0, 100);
		Set randoms = null;
		if(list != null && list.size() > 0){
			if(list.size() <= count){
				return list;
			}else if(list.size() <= 99){
				randoms = StringUtil.getRandomNumber(list.size(), count);
			}else{
				randoms = StringUtil.getRandomNumber(99, count);
			}
			List<FavoriteTag> tags = new ArrayList<FavoriteTag>();
			for(Object object : randoms){
				tags.add(list.get((Integer)object));
			}
			return tags;
		}
		return null;
	}

	@Override
	public void updateFavoriteTagCount(String tag) {
		FavoriteTag favoriteTag = baseDao.getObject(FavoriteTag.class, tag);
		if(favoriteTag != null){
			favoriteTag.setClickcount(favoriteTag.getClickcount() + 1);
			this.hibernateTemplate.save(favoriteTag);
		}
	}
}