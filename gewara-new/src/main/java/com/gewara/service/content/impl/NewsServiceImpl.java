package com.gewara.service.content.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.News;
import com.gewara.model.content.NewsPage;
import com.gewara.service.content.NewsService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.util.JsonUtils;
import com.gewara.util.ValidateUtil;
@Service("newsService")
public class NewsServiceImpl extends BaseServiceImpl implements NewsService {
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}	
	@Override
	public Integer getNewsCountByTag(String citycode, String tag, String newstype, String searchKey) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(tag)){
			query.add( Restrictions.eq("tag",tag));
		}
		if(StringUtils.isNotBlank(searchKey)){
			query.add(Restrictions.like("title", searchKey, MatchMode.ANYWHERE));
		}
		if(StringUtils.equals(newstype, "5")) 
			query.add(Restrictions.eq("newstype", newstype));
		else 
			query.add(Restrictions.or(Restrictions.eq("newstype", "1"), Restrictions.eq("newstype", "2")));
		query.setProjection(Projections.rowCount());
		List result = readOnlyTemplate.findByCriteria(query);
		return Integer.parseInt(result.get(0)+"");
	}
	
	@Override
	public List<News> getNewsListByTag(String citycode, String tag, String newstype, String searchKey, Timestamp addtime, String order, int from, int maxrows) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(tag)){
			query.add(Restrictions.eq("tag", tag));
		}
		if(StringUtils.isNotBlank(searchKey)){
			query.add(Restrictions.like("title", searchKey, MatchMode.ANYWHERE));
		}
		if(StringUtils.equals(newstype, "5")) 
			query.add(Restrictions.eq("newstype", newstype));
		else 
			query.add(Restrictions.or(Restrictions.eq("newstype", "1"), Restrictions.eq("newstype", "2"))); 
		if(addtime != null) query.add(Restrictions.ge("addtime", addtime));
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
			query.addOrder(Order.asc("id"));
		}
		else query.addOrder(Order.desc("addtime"));
		List result = readOnlyTemplate.findByCriteria(query,from,maxrows);
		return result;
	}
	@Override
	public List<News> getNewsList(String citycode, String tag, Long relatedid, String category, Long categoryid, String order, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		}
		if(StringUtils.isNotBlank(category)){
			query.add(Restrictions.eq("category", category));
			if(categoryid!=null) query.add(Restrictions.eq("categoryid", categoryid));
		}
		if(StringUtils.isNotBlank(order)){
			query.addOrder(Order.desc(order));
			query.addOrder(Order.asc("id"));
		}else query.addOrder(Order.desc("addtime"));
		List<News> result = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return result;
	}

	@Override
	public List<News> getNewsByRelatedidAndTag(String tag, Long relatedid, int from, int maxnum) {
		String hql = "from News n where n.tag=? and n.relatedid=? and n.releasetime<? order by n.releasetime desc";
		List<News> list = queryByRowsRange(hql, from, maxnum,tag,relatedid, new Timestamp(System.currentTimeMillis()));
		return list;
	}
	@Override
	public List<News> getNewsListByTagAndCategory(String citycode, String tag,String newslabel, int from, int maxnum){
		List params = new ArrayList();
		if(StringUtils.isNotBlank(newslabel)){
			String[] str = newslabel.split("[, ]+");
			String hql = "from News n where n.tag=? ";
			//and n.title like ? order by n.releasetime desc";
			params.add(tag);
			if(str.length>0){ 
				String where = "";
				for(String s:str){
					where += "or n.newslabel like ? ";
					params.add("%"+s+"%");
				}
				hql += "and (" + where.substring(2) + ") ";
			}
			if(StringUtils.isNotBlank(citycode)) {
				hql += " and (n.citycode='" + AdminCityContant.CITYCODE_ALL + "'";
				hql += " or n.citycode like ? )";
				params.add("%"+citycode+"%");
			}
			hql += " order by n.releasetime desc";
			List<News> list = queryByRowsRange(hql, from, maxnum, params.toArray());
			return list;
		}
		return new ArrayList<News>();
	}
	@Override
	public NewsPage getNewsPageByNewsidAndPageno(Long nid, Integer pageno) {
		DetachedCriteria query = DetachedCriteria.forClass(NewsPage.class);
		query.add(Restrictions.eq("newsid", nid));
		query.add(Restrictions.eq("pageno", pageno));
		List<NewsPage> list = readOnlyTemplate.findByCriteria(query, 0, 1);
		return list.isEmpty() ? null : list.get(0);
	}
	@Override
	public List<NewsPage> getNewsPageListByNewsid(Long newsid) {
		String hql = "from NewsPage n where n.newsid=? order by pageno asc";
		List<NewsPage> list = readOnlyTemplate.find(hql,newsid);
		return list;
	}
	@Override
	public List<News> getNewsListByTagAndRelatedId(String citycode, String tag, Long id, String flag, String... type) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("relatedid", id));
		if(StringUtils.isNotBlank(flag))
			query.add(Restrictions.eq("flag", flag));
		if(!ArrayUtils.isEmpty(type)){
			if(ArrayUtils.getLength(type) == 1)
				query.add(Restrictions.eq("newstype", type[0]));
			else 
				query.add(Restrictions.in("newstype", type));
		}
		query.addOrder(Order.desc("addtime"));
		List<News> newsList = readOnlyTemplate.findByCriteria(query, 0, 5);
		return newsList;
	}
	@Override
	public List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, int from, int maxnum) {
		return getNewsList(citycode, tag, relatedid, newstype, null, from, maxnum);
	}
	@Override
	public List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, String title, int from, int maxnum) {
		return getNewsList(citycode, tag, relatedid, newstype, title, null, false, from, maxnum);
	}
	@Override
	public List<News> getNewsList(String citycode, String tag, Long relatedid, String newstype, String title, String order, boolean asc, int from, int maxnum) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null)query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("category", tag));
			if(relatedid!=null)query.add(Restrictions.eq("categoryid", relatedid));
		}
		if(StringUtils.isNotBlank(newstype)){
			query.add(Restrictions.eq("newstype", newstype));
		}
		if(StringUtils.isNotBlank(title)){
			query.add(Restrictions.ilike("title", title, MatchMode.ANYWHERE));
		}
		if(StringUtils.isNotBlank(order) && ClassUtils.hasMethod(News.class, "get" + StringUtils.capitalize(order))){
			if(asc){
				query.addOrder(Order.asc(order));
			}else{
				query.addOrder(Order.desc(order));
			}
			query.addOrder(Order.desc("id"));
		}else query.addOrder(Order.desc("updatetime"));
		List<News> newsList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return newsList;
	}

	@Override
	public News getNextNews(String tag, Long nid) {
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.gt("id", nid));
		query.addOrder(Order.asc("id"));
		List<News> newsList = readOnlyTemplate.findByCriteria(query, 0, 1);
		if(newsList.isEmpty()) return null;
		return newsList.get(0);
	}
	@Override
	public Integer getNewsCount(String citycode, String tag, String newstype, Long relatedid, String title){
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null)query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("category", tag));
			if(relatedid!=null)query.add(Restrictions.eq("categoryid", relatedid));
		}
		if(StringUtils.isNotBlank(newstype)){
			query.add(Restrictions.eq("newstype", newstype));
		}
		if(StringUtils.isNotBlank(title)){
			query.add(Restrictions.ilike("title", title, MatchMode.ANYWHERE));
		}
		query.setProjection(Projections.rowCount());
		List list = readOnlyTemplate.findByCriteria(query);
		if (list.isEmpty()) return 0;
		return Integer.parseInt(""+list.get(0));
	}
	
	@Override
	public String validateNews(Long newsid){
		News news = baseDao.getObject(News.class, newsid);
		GewaConfig config = baseDao.getObject(GewaConfig.class, ConfigConstant.CFG_SPECIAL_CHAR);
		String spchar = config.getContent();
		String msg = "ตฺ1าณ:" + ValidateUtil.validateNewsContent(spchar, news.getContent());
		List<NewsPage> pageList = getNewsPageListByNewsid(newsid);
		int i=2;
		String tmp = "";
		for(NewsPage page: pageList){
			tmp = ValidateUtil.validateNewsContent(spchar, page.getContent());
			if(StringUtils.isNotBlank(tmp)) msg += "ตฺ"+i+"าณ:" + tmp;
		}
		return msg;
	}
	@Override
	public List<News> getNewsListByNewstype(String citycode, String tag, Long relatedid, String[] newstype, int from ,int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(!ArrayUtils.isEmpty(newstype)){
			if(ArrayUtils.getLength(newstype) == 1)
				query.add(Restrictions.eq("newstype", newstype[0]));
			else
				query.add(Restrictions.in("newstype", newstype));
		}
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid!=null) query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("category", tag));
			if(relatedid!=null) query.add(Restrictions.eq("categoryid", relatedid));
		}
		List<News> newsList = readOnlyTemplate.findByCriteria(query, from, maxnum);
		return newsList;
	}
	@Override
	public Integer getNewsCountByNewstype(String citycode, String tag, Long relatedid, String[] newstype){
		DetachedCriteria query = DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) query.add(Restrictions.eq("citycode", citycode));
		if(!ArrayUtils.isEmpty(newstype)){
			if(ArrayUtils.getLength(newstype) == 1)
				query.add(Restrictions.eq("newstype", newstype[0]));
			else
				query.add(Restrictions.in("newstype", newstype));
		}
		if(ServiceHelper.isTag(tag)){
			query.add(Restrictions.eq("tag", tag));
			if(relatedid != null) query.add(Restrictions.eq("relatedid", relatedid));
		}else if(ServiceHelper.isCategory(tag)){
			query.add(Restrictions.eq("category", tag));
			if(relatedid != null) query.add(Restrictions.eq("categoryid", relatedid));
		}
		query.setProjection(Projections.rowCount());
		List<News> newsList=readOnlyTemplate.findByCriteria(query);
		if(newsList.isEmpty()) return 0;
		return Integer.valueOf(newsList.get(0)+"");
	}
	@Override
	public List<News> getCurrentNewsByTag(String citycode, String tag, final String newstype, final int from,
			final int num) {
		DetachedCriteria query=DetachedCriteria.forClass(News.class);
		if(StringUtils.isNotBlank(citycode)) 
			query.add(Restrictions.or(Restrictions.eq("citycode", AdminCityContant.CITYCODE_ALL), Restrictions.like("citycode", citycode, MatchMode.ANYWHERE)));
		if(StringUtils.isNotBlank(tag)) 
			query.add(Restrictions.eq("tag", tag));
		if(StringUtils.isNotBlank(newstype)) 
			query.add(Restrictions.eq("newstype", newstype));
		query.add(Restrictions.le("releasetime", new Timestamp(System.currentTimeMillis())));
		query.addOrder(Order.desc("releasetime"));
		List<News> newsList = readOnlyTemplate.findByCriteria(query, from, num);
		return newsList;
	}
	@Override
	public void updateTips(Long nid) {
		News news = baseDao.getObject(News.class, nid);
		Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(news.getOtherinfo());
		if(StringUtils.isBlank(otherinfoMap.get("tips"))){
			otherinfoMap.put("tips", "1");
		}else{
			Long tips = Long.parseLong(otherinfoMap.get("tips")) + 1;
			otherinfoMap.put("tips", tips.toString());
		}
		news.setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		baseDao.saveObject(news);
	}
}
