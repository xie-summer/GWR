package com.gewara.service.content.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import com.gewara.model.movie.SpecialActivity;
import com.gewara.service.content.SpecialActivityService;
import com.gewara.service.impl.BaseServiceImpl;

@Service("specialActivityService")
public class SpecialActivityServiceImpl extends BaseServiceImpl implements SpecialActivityService{
	private List<String> movieList = new ArrayList<String>(Arrays.asList("movie", "cinema"));
	private List<String> dramaList = new ArrayList<String>(Arrays.asList("drama", "theatre", "dramastar"));
	private List<String> sportList = new ArrayList<String>(Arrays.asList("sport", "sportservice"));
	private List<String> ktvList = new ArrayList<String>(Arrays.asList("ktv"));
	private List<String> barList = new ArrayList<String>(Arrays.asList("bar", "barsinger"));
	private List<String> gymList = new ArrayList<String>(Arrays.asList("gym", "gymcoach", "gymcourse"));
	
	@Override
	public List<SpecialActivity> getSpecialActivityList(String status, String flag, String relatedid, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(SpecialActivity.class);
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(flag)){
			query.add(Restrictions.or(Restrictions.eq("flag", flag), Restrictions.eq("flag", "all")));
		}
		if(StringUtils.isNotBlank(relatedid)){
			query.add(Restrictions.like("relatedid", relatedid, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.isNotNull("addtime"));
		query.addOrder(Order.desc("addtime"));
		List<SpecialActivity> activityList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return activityList;
	}
	
	@Override
	public int getSpecialActivityCount(String status, String flag, String relatedid) {
		DetachedCriteria query = DetachedCriteria.forClass(SpecialActivity.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(flag)){
			query.add(Restrictions.or(Restrictions.eq("flag", flag), Restrictions.eq("flag", "all")));
		}
		if(StringUtils.isNotBlank(relatedid)){
			query.add(Restrictions.like("relatedid", relatedid, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.isNotNull("addtime"));
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public List<SpecialActivity> getSpecialActivityList(String status, String flag, String relatedid, String searchKey, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(SpecialActivity.class);
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(flag)){
			if(movieList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", movieList), Restrictions.eq("flag", "all")));
			}else if(dramaList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", dramaList), Restrictions.eq("flag", "all")));
			}else if(sportList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", sportList), Restrictions.eq("flag", "all")));
			}else if(ktvList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", ktvList), Restrictions.eq("flag", "all")));
			}else if(barList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", barList), Restrictions.eq("flag", "all")));
			}else if(gymList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", gymList), Restrictions.eq("flag", "all")));
			}else{
				query.add(Restrictions.eq("flag", "all"));
			}
		}
		if(StringUtils.isNotBlank(searchKey)){
			query.add(Restrictions.like("activityname", searchKey, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(relatedid)){
			query.add(Restrictions.like("relatedid", relatedid, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.isNotNull("addtime"));
		query.addOrder(Order.desc("addtime"));
		List<SpecialActivity> activityList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return activityList;
	}
	
	@Override
	public int getSpecialActivityCount(String status, String flag, String relatedid, String searchKey) {
		DetachedCriteria query = DetachedCriteria.forClass(SpecialActivity.class);
		query.setProjection(Projections.rowCount());
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(flag)){
			if(movieList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", movieList), Restrictions.eq("flag", "all")));
			}else if(dramaList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", dramaList), Restrictions.eq("flag", "all")));
			}else if(sportList.contains(flag)){
				query.add(Restrictions.or(Restrictions.in("flag", sportList), Restrictions.eq("flag", "all")));
			}else{
				query.add(Restrictions.eq("flag", "all"));
			}
		}
		if(StringUtils.isNotBlank(searchKey)){
			query.add(Restrictions.like("activityname", searchKey, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(relatedid)){
			query.add(Restrictions.like("relatedid", relatedid, MatchMode.ANYWHERE));
		}
		query.add(Restrictions.isNotNull("addtime"));
		List list = hibernateTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
}
