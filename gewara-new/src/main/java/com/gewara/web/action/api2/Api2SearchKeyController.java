package com.gewara.web.action.api2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.untrans.SearchService;
import com.gewara.web.action.api.BaseApiController;
@Controller
public class Api2SearchKeyController extends BaseApiController {
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	/**
	 * 运动场馆索引搜索
	 * @param key
	 * @param encryptCode
	 * @param indexName
	 * @param tag
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/api2/common/searchIndex.xhtml")
	public String searchIndex(String key,String encryptCode, String indexName, String tag,
			ModelMap model,HttpServletRequest request,String version){
		List<Map> searchList = null;
		if("cinema".equalsIgnoreCase(tag)){
			DetachedCriteria query = DetachedCriteria.forClass(Cinema.class, "sp");
			if(StringUtils.isNotBlank(indexName)){
				String[] keyList = indexName.trim().split(" +");
				for(String sKey:keyList){
					if(StringUtils.isNotBlank(sKey)) query.add(Restrictions.like("name", sKey.toLowerCase(), MatchMode.ANYWHERE));
				}
			}
			query.setProjection(Projections.distinct(Projections.projectionList()
					.add(Projections.property("sp.name"), "skey")
					.add(Projections.property("sp.id"), "id")
					.add(Projections.property("sp.generalmark"), "mk1")
					.add(Projections.property("sp.generalmarkedtimes"), "mk2")));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			searchList = hibernateTemplate.findByCriteria(query, 0, 5);
		}else if("drama".equalsIgnoreCase(tag)){
			DetachedCriteria query = DetachedCriteria.forClass(Theatre.class, "sp");
			if(StringUtils.isNotBlank(indexName)){
				String[] keyList = indexName.trim().split(" +");
				for(String sKey:keyList){
					if(StringUtils.isNotBlank(sKey)) query.add(Restrictions.like("name", sKey.toLowerCase(), MatchMode.ANYWHERE));
				}
			}
			query.setProjection(Projections.distinct(Projections.projectionList()
					.add(Projections.property("sp.name"), "skey")
					.add(Projections.property("sp.id"), "id")
					.add(Projections.property("sp.generalmark"), "mk1")
					.add(Projections.property("sp.generalmarkedtimes"), "mk2")));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			searchList = hibernateTemplate.findByCriteria(query, 0, 5);
		}else if("sport".equalsIgnoreCase(tag)){
			DetachedCriteria query = DetachedCriteria.forClass(Sport.class, "sp");
			if(StringUtils.isNotBlank(indexName)){
				String[] keyList = indexName.trim().split(" +");
				for(String sKey:keyList){
					if(StringUtils.isNotBlank(sKey)) query.add(Restrictions.like("name", sKey.toLowerCase(), MatchMode.ANYWHERE));
				}
			}
			//TODO:Service中有？？？
			DetachedCriteria subquery = DetachedCriteria.forClass(Sport2Item.class, "t");
			subquery.add(Restrictions.eqProperty("t.sportid", "sp.id"));
			subquery.setProjection(Projections.property("t.sportid"));
			query.add(Subqueries.exists(subquery));
			query.setProjection(Projections.distinct(Projections.projectionList()
					.add(Projections.property("sp.id"), "id")
					.add(Projections.property("sp.name"), "skey")
					.add(Projections.property("sp.generalmark"), "mk1")
					.add(Projections.property("sp.generalmarkedtimes"), "mk2")));
			query.setResultTransformer(DetachedCriteria.ALIAS_TO_ENTITY_MAP);
			searchList = hibernateTemplate.findByCriteria(query, 0, 5);
		}else{
			try {
				Set<String> keyList = searchService.getSearchKeyList(tag, indexName, 5);
				if(keyList != null && keyList.size() > 0){
					searchList = new ArrayList<Map>();
					for(String str:keyList){
						Map<String,String> map = new HashMap<String,String>();
						map.put("skey", str);
						searchList.add(map);
					}
				}
			} catch (Exception e) {
				dbLogger.error(version + "api运动场馆索引搜索出错",e);
			}
		}
		model.put("searchList", searchList);
		return getXmlView(model, "api2/sport/indexvalue.vm");
	}
	
}
