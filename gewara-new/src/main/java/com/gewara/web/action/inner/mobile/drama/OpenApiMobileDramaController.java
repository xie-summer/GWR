package com.gewara.web.action.inner.mobile.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.api.GewaApiDramaHelper;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.user.Member;
import com.gewara.model.user.Treasure;
import com.gewara.service.member.TreasureService;
import com.gewara.service.order.GoodsService;
import com.gewara.support.NullPropertyOrder;
import com.gewara.untrans.CommonService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileDramaController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
@Controller
public class OpenApiMobileDramaController extends BaseOpenApiMobileDramaController{
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("goodsService")
	protected GoodsService goodsService;
	@Autowired@Qualifier("treasureService")
	private TreasureService treasureService;
	@RequestMapping("/openapi/mobile/drama/typeList.xhtml")
	public String itemList(ModelMap model, HttpServletRequest request) {
		Map<String, String> qryMap = DramaConstant.dramaTypeMap;
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(String key : qryMap.keySet()){
			Map<String, Object> tmp = new HashMap<String, Object>();
			tmp.put("dramatype", key);
			tmp.put("name", qryMap.get(key));
			resMapList.add(tmp);
		}
		return getOpenApiXmlList(resMapList, "typeList,type", model, request);
	}
	@RequestMapping("/openapi/mobile/drama/commendDramaList.xhtml")
	public String commendDramaList(String citycode, int from, int maxnum, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		List<Drama> dramaList = new ArrayList<Drama>();
		List<GewaCommend> commendList = commonService.getGewaCommendList(citycode, SignName.SHOWINDEX_DRAMA, null, null, true, from, maxnum);
		for(GewaCommend commend : commendList){
			if(StringUtils.equals(commend.getTag(), TagConstant.TAG_DRAMA) && commend.getRelatedid()!=null){
				Drama drama = daoService.getObject(Drama.class, commend.getRelatedid());
				dramaList.add(drama);
			}
		}
		modelList(citycode, dramaList, model, request);
		return getOpenApiXmlList(model);
	}
	@RequestMapping("/openapi/mobile/drama/getCurHotDramas.xhtml")
	public String getCurHotDramas(String citycode, String dramatype, String booking, int from, int maxnum, ModelMap model, HttpServletRequest request) {
		if(StringUtils.isBlank(citycode)) citycode = AdminCityContant.CITYCODE_SH;
		List<Drama> dramaList = new ArrayList<Drama>();
		if(StringUtils.isBlank(dramatype) && StringUtils.isBlank(booking)){
			List<GewaCommend> commendList = commonService.getGewaCommendList(citycode, SignName.SHOWINDEX_DRAMA, null, null, true, from, maxnum);
			for(GewaCommend commend : commendList){
				if(StringUtils.equals(commend.getTag(), TagConstant.TAG_DRAMA) && commend.getRelatedid()!=null){
					Drama drama = daoService.getObject(Drama.class, commend.getRelatedid());
					dramaList.add(drama);
				}
			}
		}else {
			DetachedCriteria query = DetachedCriteria.forClass(Drama.class, "d");
			query.add(Restrictions.eq("d.citycode", citycode));
			if(StringUtils.isNotBlank(dramatype)) query.add(Restrictions.eq("d.dramatype", dramatype));
			DetachedCriteria sub = getSubQry();
			query.add(Subqueries.exists(sub));
			query.addOrder(NullPropertyOrder.desc("d.clickedtimes"));
			query.addOrder(Order.asc("d.id"));
			dramaList = hibernateTemplate.findByCriteria(query, from, maxnum);
		}
		modelList(citycode, dramaList, model, request);
		return getOpenApiXmlList(model);
	}
	private void modelList(String citycode, List<Drama> dramaList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		for(Drama drama : dramaList){
			Map<String, Object> resMap = getDramaData(drama);
			List<Integer> allPriceList = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
			Collections.sort(allPriceList);
			resMap.put("prices", StringUtils.join(allPriceList, ","));
			resMap.put("boughtcount", drama.getBoughtcount());
			resMap.putAll(initDramOtherMap(citycode, drama));
			resMapList.add(resMap);
		}
		initField(model, request);
		putDramaListNode(model);
		model.put("resMapList", resMapList);
	}
	
	@RequestMapping("/openapi/mobile/drama/searchDramas.xhtml")
	public String searchDramas(String citycode, String name, int from, int maxnum, ModelMap model, HttpServletRequest request) {
		List<Drama> dramaList = dramaService.getDramaListByName(citycode, name, null, null, false, 0, 50);
		List<Drama> dramaList2 = new ArrayList<Drama>();
		List<Drama> curList = new ArrayList();
		List<Drama> furList = new ArrayList();
		List<Drama> oldList = new ArrayList();
		Date curdate = new Date();
		for(Drama drama : dramaList){
			Date enddate = drama.getEnddate();
			Date releasedate = drama.getReleasedate();
			if(enddate!=null){
				if(releasedate!=null){
					if(curdate.compareTo(releasedate)>=0){
						if(curdate.compareTo(enddate)<=0){
							curList.add(drama);
						}else{
							oldList.add(drama);
						}
					}else {
						furList.add(drama);
					}
				}else {
					oldList.add(drama);
				}
			}else {
				oldList.add(drama);
			}
		}
		Collections.sort(curList, new PropertyComparator("releasedate", false, true));
		Collections.sort(furList, new PropertyComparator("releasedate", false, true));
		dramaList2.addAll(curList);
		dramaList2.addAll(furList);
		dramaList2.addAll(oldList);
		dramaList2 = BeanUtil.getSubList(dramaList2, from, maxnum);
		modelList(citycode, dramaList2, model, request);
		return getOpenApiXmlList(model);
	}
	private DetachedCriteria getSubQry(){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		DetachedCriteria sub = DetachedCriteria.forClass(OpenDramaItem.class, "odi");
		sub.add(Restrictions.eq("odi.status", OdiConstant.STATUS_BOOK));
		sub.add(Restrictions.le("odi.opentime", curtime));
		sub.add(Restrictions.gt("odi.closetime", curtime));
		sub.add(Restrictions.eqProperty("odi.dramaid", "d.id"));
		sub.setProjection(Projections.property("odi.id"));
		return sub;
	}
	private Map<String, Object> initDramOtherMap(String citycode, Drama drama){
		List<OpenDramaItem> openDramaItemList = dramaService.getOpenDramaItemListBydramaid(citycode, drama.getId());
		Map<String, Object> resMap = new HashMap<String, Object>();
		int openSeat = 0, openExress=0, openMachine=0;
		Set<String> theatrenameSet = new HashSet<String>();
		for(OpenDramaItem odi : openDramaItemList){
			if(openSeat==0 && odi.isOpenseat()){
				openSeat = 1;
			}
			if(openExress==0 && StringUtils.isNotBlank(odi.getExpressid())){
				openExress = 1;
			}
			if(openExress==0 && StringUtils.isBlank(odi.getExpressid())){
				openMachine = 1;
			}
			theatrenameSet.add(odi.getTheatrename());
		}
		resMap.put("openSeat", openSeat);
		resMap.put("openExpress", openExress);
		resMap.put("openMachine", openMachine);
		resMap.put("theatrenames", StringUtils.join(theatrenameSet, ","));
		return resMap;
	}
	@RequestMapping("/openapi/mobile/drama/getDramaDetail.xhtml")
	public String getDramaDetail(Long dramaid, String memberEncode, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该演出项目不存在!");
		Map<String, Object> resMap = getDramaData(drama);
		List<Integer> allPriceList = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
		int dpicount = dramaPlayItemService.getDramaCount(dramaid, DateUtil.getCurFullTimestamp());
		Treasure treasure = null;
		if (StringUtils.isNotBlank(memberEncode)) {
			Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
			treasure = treasureService.getTreasureByTagMemberidRelatedid(TagConstant.TAG_DRAMA, member.getId(), dramaid, Treasure.ACTION_COLLECT);
		}
		resMap.put("prices", StringUtils.join(allPriceList, ","));
		resMap.put("dpicount", dpicount);
		resMap.put("iscollect", treasure == null ? 0 : 1);
		resMap.putAll(initDramOtherMap(citycode, drama));
		return getOpenApiXmlDetail(resMap, "drama", model, request);
	}
	@RequestMapping("/openapi/mobile/drama/getDramaContent.xhtml")
	public String getDramaContent(Long dramaid, Integer width, Integer height, ModelMap model) {
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "该演出项目不存在!");
		String content = getSimpleHtmlContent(drama.getContent(), Status.Y, width, height);
		return getSingleResultXmlView(model, content);
	}
	@RequestMapping("/openapi/mobile/theatre/getTheatreDetail.xhtml")
	public String getTheatreDetail(Long theatreid, ModelMap model, HttpServletRequest request) {
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		Map<String, Object> resMap = getTheatreData(theatre);
		return getOpenApiXmlDetail(resMap, "theatre", model, request);
	}
	
	@RequestMapping("/openapi/mobile/theatre/getTheatreByDramaid.xhtml")
	public String getTheatreDetailByDramaid(Long dramaid, String booking, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		boolean bk = StringUtils.equals(booking, "1");
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Theatre> curTheatre = dramaPlayItemService.getTheatreList(citycode, dramaid, bk, 1);
		if(curTheatre == null || curTheatre.isEmpty()) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "关联数据不存在!");
		Theatre theatre = curTheatre.get(0);
		Map<String, Object> resMap = getTheatreData(theatre);
		initField(model, request);
		putTheatreNode(model);
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	
	@RequestMapping("/openapi/mobile/theatre/getTheatreFieldDetail.xhtml")
	public String theatreFieldDetail(Long fieldid, ModelMap model, HttpServletRequest request) {
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field==null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "场地不存在!");
		Map<String, Object> resMap = GewaApiDramaHelper.getTheatreField(field);
		initField(model, request);
		model.put("root", "theatreField");
		model.put("resMap", resMap);
		return getOpenApiXmlDetail(model);
	}
	@RequestMapping("/openapi/mobile/theatre/getBookingTheatreList.xhtml")
	public String theatreFieldDetail(String citycode, Long dramaid, ModelMap model, HttpServletRequest request) {
		List<OpenDramaItem> odiList = dramaService.getBookingOdiList(citycode, null, dramaid, null);
		List<Long> theatreidList = BeanUtil.getBeanPropertyList(odiList, Long.class, "theatreid", true);
		List<Theatre> theatreList = daoService.getObjectList(Theatre.class, theatreidList);
		List<Map<String, Object>> qryMapList = new ArrayList<Map<String, Object>>();
		for(Theatre theatre : theatreList){
			Map<String, Object> qryMap = getTheatreData(theatre);
			qryMapList.add(qryMap);
		}
		return getOpenApiXmlList(qryMapList, "theatreList,theatre", model, request);
	}
	/**
	 * 我收藏的演出列表
	 */
	@RequestMapping("/openapi/mobile/drama/dramaListByMy.xhtml")
	public String movieListByMy(Integer from, Integer maxnum, ModelMap model, HttpServletRequest request){
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 10;
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Long> dramaidList = treasureService.getTreasureIdList(member.getId(), TagConstant.TAG_DRAMA, Treasure.ACTION_COLLECT);
		List<Map<String, Object>> resMapList = new ArrayList<Map<String, Object>>();
		List<Drama> dramaList = new ArrayList<Drama>(); 
		for(Long dramaid : dramaidList){
			Drama drama = daoService.getObject(Drama.class, dramaid);
			dramaList.add(drama);
		}
		dramaList = BeanUtil.getSubList(dramaList, from, maxnum);
		for(Drama drama : dramaList){
			Map<String, Object> resMap = getDramaData(drama);
			List<Integer> allPriceList = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
			Collections.sort(allPriceList);
			resMap.put("prices", StringUtils.join(allPriceList, ","));
			resMap.put("iscollect", 1);
			resMapList.add(resMap);
		}
		return getOpenApiXmlList(resMapList, "dramaList,drama", model, request);
	}
}
