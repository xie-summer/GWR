package com.gewara.web.action.admin.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CityData;
import com.gewara.constant.content.FilmFestConstant;
import com.gewara.constant.content.SignName;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.model.common.GewaCity;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.service.GewaCityService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.web.util.PageUtil;
@Controller
public class GewaCommendAdminController extends GewaCommendBaseAdminController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	
	private static final Map<String, String> newsSignnameMap = new HashMap<String, String>();
	static{
		newsSignnameMap.put("movie", SignName.NEWS_MOVIE);
		newsSignnameMap.put("sport", SignName.NEWS_SPORT);
		newsSignnameMap.put("gym", SignName.NEWS_GYM);
		newsSignnameMap.put("drama", SignName.NEWS_DRAMA);
	}
	@RequestMapping("/admin/recommend/indexCommend.xhtml")
	public String commendIndex(@CookieValue(required=false)String admin_citycode,Boolean isNew, ModelMap model) {
		model.put("citycode", admin_citycode);
		if(isNew != null && isNew){
			return "admin/recommend/commendIndex_new.vm";
		}
		return "admin/recommend/commendIndex.vm";
	}
	@RequestMapping("/admin/recommend/gcDetail.xhtml")
	public String gcDetail(Long id, ModelMap model,Long spparentid) {
		if(id!=null) {
			GewaCommend gc = daoService.getObject(GewaCommend.class, id);
			if(gc != null){
				model.put("gc", gc);
				String signname = SignName.NEWS_SUBTITLE;
				List<GewaCommend> subgc = commonService.getGewaCommendList(gc.getId(), signname, null, false, 0, 1);
				if(subgc != null && subgc.size() > 0){
					model.put("subgc", subgc);
				}
			}
		}
		model.put("spparentid", spparentid);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/recommend/gcDetail.vm";
	}
	
	@RequestMapping("/admin/recommend/gcDetailSportService.xhtml")
	public String gcDetailSportService(Long id, ModelMap model) {
		if(id!=null) {
			GewaCommend gc = daoService.getObject(GewaCommend.class, id);
			model.put("gc", gc);
		}
		return "admin/recommend/sportindex/gcDetailsportservice.vm";
	}
	@RequestMapping("/admin/recommend/loadSportServicetable.xhtml")
	public String loadSportServicetable(Long sportid, ModelMap model) {
		if(sportid != null) {
			Sport sport = daoService.getObject(Sport.class, sportid);
			if(sport == null){return showError(model, "数据不存在!");}
			List<SportItem> sportitemlist = sportService.getSportItemListBySportId(sportid, null);
			model.put("sport", sport);
			model.put("sportitemlist", sportitemlist);
		}
		return "admin/recommend/sportindex/loadsportitems.vm";
	}
	@RequestMapping("/admin/recommend/loadSportServicetablePrice.xhtml")
	public String loadSportServicetablePrice(Long sportid, Long itemid, ModelMap model) {
		if(itemid != null && sportid != null) {
			SportPriceTable sportPriceTable = sportService.getSportPriceTable(sportid, itemid);
			model.put("sportPriceTable", sportPriceTable);
			if(sportPriceTable != null){
				List<SportPrice> priceList = sportService.getPriceList(sportPriceTable.getId());
				model.put("priceList", priceList);
			}
		}
		return "admin/recommend/sportindex/loadsportitemsprice.vm";
	}
	@RequestMapping("/admin/recommend/saveSporttable.xhtml")
	public String saveSporttable(HttpServletRequest request, ModelMap model) {
		Map<String, String[]> gcMap = request.getParameterMap();
		GewaCommend gc = new GewaCommend("");
		if(StringUtils.isNotBlank(ServiceHelper.get(gcMap, "id"))){
			gc = daoService.getObject(GewaCommend.class, new Long(ServiceHelper.get(gcMap, "id")));
		}
		BindUtils.bindData(gc,gcMap);
		gc.setCountycode(daoService.getObject(Sport.class, gc.getRelatedid()).getCountycode());
		gc.setCitycode(getAdminCitycode(request));
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	//头部网站通知
	@RequestMapping("/admin/recommend/indexWebsiteNotice.xhtml")
	public String websiteNotice(ModelMap model) {
		String signname = SignName.INDEX_WEBSITE_NOTICE;
		List<GewaCommend> gcList = commonService.getGewaCommendList(null , signname, null,null, false, 0, 20);
		model.put("gcList", gcList);
		model.put("signname", signname);
		return "admin/recommend/index/indexWebsiteNotice.vm";
	}
	
	@RequestMapping("/admin/recommend/gcResult.xhtml")
	public String gcResult(ModelMap model) {
		return showMessage(model, "添加信息成功！");
	}
	//其他
	@RequestMapping("/admin/recommend/indexother.xhtml")
	public String indexother(Long parentid, ModelMap model, HttpServletRequest request) {
		String signname = SignName.OTHER;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, parentid,null, false,0, 200);
		model.put("gcList", gcList);
		model.put("signname", signname);
		model.put("parentid", parentid);
		return "admin/recommend/other.vm";
	}

	@RequestMapping("/admin/recommend/newssubtitle.xhtml")
	public String newssubtitle(Long parentid, ModelMap model, HttpServletRequest request){
		String signname = SignName.NEWS_SUBTITLE;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, parentid,null, false,0, 200);
		model.put("signname", signname);
		model.put("gcList", gcList);
		return "admin/recommend/newssubtitle.vm";
	}
	/**
	 * 推荐新闻
	 * @param signtype
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/recommend/newsRecommend.xhtml")
	public String newsRecommend(@RequestParam String signtype, ModelMap model, HttpServletRequest request){
		String signname = newsSignnameMap.get(signtype);
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null,null, false,0, 200);
		model.put("signname", signname);
		model.put("gcList", gcList);
		return "admin/recommend/baseRecommend.vm";
	}
	// 基础推荐()
	@RequestMapping("/admin/recommend/baserecommend.xhtml")
	public String baserecommend(@RequestParam String signname, Long parentid, ModelMap model, HttpServletRequest request){
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, parentid,null, false,0, 100);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("signname", signname);
		model.put("gcList", gcList);
		model.put("spparentid", parentid);
		model.put("admincitycode", this.getAdminCitycode(request));
		return "admin/recommend/baseRecommend.vm";
	}
	
	//专题数据
	@RequestMapping("/admin/recommend/spRecommend.xhtml")
	public String spNewsRecommend(ModelMap model, HttpServletRequest request, String tag, String signname, Integer pageNo){
		if(pageNo==null) pageNo = 0;
		int rowsPerPage = 20;
		Integer count = commonService.getGewaCommendCount(getAdminCitycode(request) , signname, null, tag, false);
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null, tag, false, rowsPerPage*pageNo, rowsPerPage);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		for(GewaCommend gc : gcList){
				if(gc.getParentid()!=null)
					movieMap.put(gc.getId(), daoService.getObject(Movie.class, gc.getParentid()));
		}
		PageUtil pageUtil=new PageUtil(count, rowsPerPage,pageNo,"admin/recommend/spRecommend.xhtml");
		Map params = new HashMap();
		params.put("signname", signname);
		params.put("tag", tag);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("movieMap", movieMap);
		model.put("signname", signname);
		model.put("tag", tag);
		model.put("gcList", gcList);
		return "admin/recommend/spRecommend.vm";
	}
	@RequestMapping("/admin/recommend/spRecommendList.xhtml")
	public String spRecommend(Long relatedid, ModelMap model){
		SpecialActivity special = daoService.getObject(SpecialActivity.class, relatedid);
		if(special == null) return show404(model, "未找到此活动专题！");
		model.put("special", special);
		if(StringUtils.equals(special.getTag(), FilmFestConstant.TAG_FILMFEST_16))
			return "admin/recommend/filmfest/recommendList.vm";
		return "admin/recommend/spRecommendList.vm";
	}
	
	@RequestMapping("/admin/recommend/goodsRecommend.xhtml")
	public String goodsRecommond(ModelMap model, HttpServletRequest request){
		String signname = SignName.POINT_GOODS;
		model.put("signname", signname);
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null,null, false,0, 200);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		return "admin/recommend/goodsRecommend.vm";
	}
	
	@RequestMapping("/admin/recommend/searchRecommend.xhtml")
	public String searchRecommond(ModelMap model, HttpServletRequest request){
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request),SignName.SEARCH_ZT, null,null, false,0,10);
		model.put("gcList", gcList);
		model.put("signname",  SignName.SEARCH_ZT);
		return "admin/recommend/searchRecommend.vm";
	}
	
	//积分兑换
	@RequestMapping("/admin/recommend/commendpointexchange.xhtml")
	public String commendpointexchange(ModelMap model, HttpServletRequest request) {
		String signname = SignName.POINTEXCHANGE;
		String url = "admin/recommend/pointexchange.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, false, url, request, model);
	}
/*	//运动新手必读
	@RequestMapping("/admin/recommend/sportRookie.xhtml")
	public String sportRookie(ModelMap model, HttpServletRequest request){
		String signname = SignName.SPORT_ROOKIE;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null,null, false,0, 200);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		Map<Long, SportItem> siMap = new HashMap<Long, SportItem>();
		for(GewaCommend gc: gcList){
			News news = daoService.getObject(News.class, gc.getRelatedid());
			if(news!=null && news.getCategoryid()!=null && StringUtils.isNotBlank(news.getCategory())){
				SportItem sportItem = daoService.getObject(SportItem.class, news.getCategoryid());
				siMap.put(gc.getId(), sportItem);
			}
		}
		model.put("gcList", gcList);
		model.put("siMap", siMap);
		model.put("signname", signname);
		return "admin/recommend/sport/sportrookie.vm";
	}
	//运动高手进阶
	@RequestMapping("/admin/recommend/sportMaster.xhtml")
	public String sportMaster(ModelMap model, HttpServletRequest request){
		String signname = SignName.SPORT_MASTER;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname,null, null, false,0,200);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		Map<Long, SportItem> siMap = new HashMap<Long, SportItem>();
		for(GewaCommend gc: gcList){
			News news = daoService.getObject(News.class, gc.getRelatedid());
			if(news!=null && news.getCategoryid()!=null && StringUtils.isNotBlank(news.getCategory())){
				SportItem sportItem = daoService.getObject(SportItem.class, news.getCategoryid());
				siMap.put(gc.getId(), sportItem);
			}
		}
		model.put("gcList", gcList);
		model.put("siMap", siMap);
		model.put("signname", signname);
		return "admin/recommend/sport/sportmaster.vm";
	}*/
	//运动项目
	@RequestMapping("/admin/recommend/sportItem.xhtml")
	public String sportItem(ModelMap model, HttpServletRequest request){
		String signname = SignName.SPORT_ITEM;
		String url = "admin/recommend/sport/sportitem.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(new RelatedHelper(), signname, null, true, url, request, model);
	}
	
	//推荐项目场馆
	@RequestMapping("/admin/recommend/sportItemVenue.xhtml")
	public String recommendSportItem(ModelMap model, HttpServletRequest request){
		String signname = SignName.SPORTITEM_VENUE;
		String url = "admin/recommend/sport/sportitemvenue.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(new RelatedHelper(), signname, null, true, url, request, model);
	}
	//运动项目场馆
	@RequestMapping("/admin/recommend/sportVenue.xhtml")
	public String sportVenue(ModelMap model, HttpServletRequest request, Long relatedid){
		String signname = SignName.SPORT_VENUE;
		List<GewaCommend> gcList = commonService.getGewaCommendListByid(relatedid, "sportservice", SignName.SPORT_ITEM, false);
		List<GewaCommend> sportVenueList = null;
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		if(gcList.size()>0){
			sportVenueList = commonService.getGewaCommendList(getAdminCitycode(request) , SignName.SPORT_VENUE, gcList.get(0).getId(), null,false,0,200);
			commonService.initGewaCommendList("sportVenueList", rh, sportVenueList);
		}
		model.put("sportVenueList", sportVenueList);
		model.put("signname", signname);
		return "admin/recommend/sport/sportvenue.vm";
	}
	//圈子推荐首页
	@RequestMapping("/admin/recommend/commuRecommend.xhtml")
	public String commuRecommend(ModelMap model, HttpServletRequest request){
		String signname = SignName.COMMU_INDEX;
		String url = "admin/recommend/commu/commuRecommend.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	/**
	 *  分站之间推荐数据共享
	 *  关联城市
	 * */
	@RequestMapping("/admin/common/dataShareCitys.xhtml")
	public String commonRelateCitys(Long gcid, ModelMap model){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, gcid);
		if(gewaCommend == null) return showJsonError_NOT_FOUND(model);
		Map<GewaCity, List<GewaCity>> map = gewaCityService.getIdxCityMap();
		List<GewaCity> cityList = new ArrayList<GewaCity>();
		for(GewaCity key : map.keySet()) {
			if(!map.get(key).isEmpty())
				cityList.addAll(map.get(key));
		}
		model.put("cityList", cityList);
		model.put("gcid", gcid);
		List<GewaCommend> gewaCommendList = commonService.getCommendListByRelatedid(gewaCommend.getRelatedid(), gewaCommend.getSignname(), gewaCommend.getTag());
		Set<String> tmpSet = new HashSet<String>();
		for(GewaCommend commend : gewaCommendList){
			tmpSet.add(commend.getCitycode());
		}
		List<String> selcitycode = new ArrayList<String>(tmpSet);
		model.put("selcitycode", selcitycode);
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/common/datashareCitys.vm";
	}
	/***
	 *  复制当前数据
	 *  保存关联城市
	 *  当前选择的城市 + 当前gcid == 复制gc数据
	 */
	@RequestMapping("/admin/common/dataShareSaveCitys.xhtml")
	public String dataShareSaveCitys(Long gcid, String relatecityAll, String relatecity, ModelMap model,HttpServletRequest request){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, gcid);
		List<String> citycodes = null;
		if(StringUtils.equals(relatecityAll, AdminCityContant.CITYCODE_ALL)){
			Map<String, String> otherCityNamesMap = CityData.getOtherCityNames();
			citycodes = new ArrayList<String>(otherCityNamesMap.keySet());
		}else{
			if(StringUtils.isNotBlank(relatecity))
			citycodes = Arrays.asList(StringUtils.split(relatecity, ","));
		}
		List<GewaCommend> gewaCommendList = commonService.getCommendListByRelatedid(gewaCommend.getRelatedid(), gewaCommend.getSignname(), gewaCommend.getTag());
		List<String> selcitycode = new ArrayList<String>(); 
		List idList = new ArrayList();
		for(GewaCommend commend : gewaCommendList){
			if(commend.getCitycode().equals("310000")) continue;
			selcitycode.add(commend.getCitycode());
			idList.add(commend.getId());
		}
		for(int i = 0 ;i < selcitycode.size(); i++){
			if(citycodes != null && citycodes.contains(selcitycode.get(i))) continue;
			daoService.removeObjectById(GewaCommend.class, new Long(idList.get(i)+""));
		}
		if(citycodes != null){
			for(String citycode : citycodes){
				if(StringUtils.equals(citycode, getAdminCitycode(request))) continue;
				if(selcitycode.contains(citycode)) continue;
				GewaCommend newGewaCommend = new GewaCommend();
				try {
					BeanUtils.copyProperties(newGewaCommend, gewaCommend);
				} catch (Exception e) {
					dbLogger.error("", e);
					return showJsonError_DATAERROR(model);
				} 
				newGewaCommend.setId(null);
				newGewaCommend.setAddtime(DateUtil.getCurFullTimestamp());
				newGewaCommend.setCitycode(citycode);
				newGewaCommend.setOrdernum(0);
				newGewaCommend.setRelatedid(gewaCommend.getRelatedid());
				daoService.saveObject(newGewaCommend);
			}
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/recommend/partnerAd.xhtml")
	public String partnerAd(ModelMap model, HttpServletRequest request){
		String signname = SignName.PARTNER_AD;
		model.put("signname", signname);
		String qry = "select new map(a.id as id, a.partnerpath as path, a.partnername as name) from ApiUser a order by a.id";
		List<Map> partnerList = hibernateTemplate.find(qry);
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null,null, false,0, 50);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		commonService.initGewaCommendList("gcList", rh, gcList);
		model.put("gcList", gcList);
		model.put("partnerList", partnerList);
		return "admin/recommend/partner/partnerAd.vm";
	}
	
	//话剧公告
	@RequestMapping("/admin/recommend/dramaWebsiteNotice.xhtml")
	public String dramaNotice(ModelMap model) {
		String signname = SignName.DRAMA_WEBSITE_NOTICE;
		List<GewaCommend> gcList = commonService.getGewaCommendList(null , signname, null,null, false, 0, 20);
		model.put("gcList", gcList);
		model.put("signname", signname);
		return "admin/recommend/index/indexWebsiteNotice.vm";
	}
	/**
	 * 电影首页推荐
	 * @return
	 */
	@RequestMapping("/admin/recommend/cinemaRecommend.xhtml")
	public String cinemaRecommend(HttpServletRequest request, ModelMap model){
		String citycode = this.getAdminCitycode(request);
		model.put("admincitycode", citycode);
		return "admin/recommend/cinemaRecommend.vm";
	}

}
