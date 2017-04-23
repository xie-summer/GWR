package com.gewara.web.action.admin.sport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MultiMap;
import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.SportUpGrade;
import com.gewara.model.acl.User;
import com.gewara.model.common.County;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.model.sport.SportProfile;
import com.gewara.mongo.MongoService;
import com.gewara.service.sport.SportOrderService;
import com.gewara.service.sport.SportService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Mar 19, 2008 AT 7:56:42 PM
 */
@Controller
public class SportAdminController extends BaseAdminController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	@Autowired@Qualifier("sportOrderService")
	private SportOrderService sportOrderService;
	public void setSportOrderService(SportOrderService sportOrderService) {
		this.sportOrderService = sportOrderService;
	}
	@RequestMapping("/admin/sport/sportList.xhtml")
	public String sportList(String flag, String key, Integer pageNo, HttpServletRequest request, ModelMap model) throws Exception {
		String citycode = getAdminCitycode(request);
		Integer rowsCount = placeService.getPlaceCount(Sport.class, citycode);
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		if(StringUtils.isNotBlank(flag)) rowsPerPage = 100;
		int first = rowsPerPage * pageNo;
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/sport/sportList.xhtml", true, true);
		pageUtil.initPageInfo();
		model.put("pageUtil", pageUtil);

		DetachedCriteria query = DetachedCriteria.forClass(Sport.class);
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(key)){
			query.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
					, Restrictions.ilike("pinyin", key, MatchMode.ANYWHERE)));
		}
		if(StringUtils.isNotBlank(flag)) query.add(Restrictions.isNotNull("flag"));
		query.addOrder(Order.asc("name"));
		List sportList = hibernateTemplate.findByCriteria(query, first, rowsPerPage);
		model.put("sportList", sportList);
		model.put("flag", flag);
		List<SportItem> commendItemList = sportService.getCommendSportItemList(0, 3);
		model.put("commendItemList", commendItemList);
		return "admin/sport/sportList.vm";
	}
	@RequestMapping("/admin/sport/relateItemList.xhtml")
	public String sportItemList(@RequestParam("sportId")Long sportId, ModelMap model) throws Exception {
		Sport sport = daoService.getObject(Sport.class, sportId);
		model.put("sport", sport);
		List<SportItem> itemList = sportService.getSportItemListBySportId(sportId, null);
		for(int i=0;itemList!=null&&i<itemList.size();i++){
			SportItem sportItem=itemList.get(i) ;
			Sport2Item sport2Item=sportService.getSport2Item(sportId, sportItem.getId());
			if(sport2Item!=null)
			 sportItem.setOtherinfo(sport2Item.getOtherinfo());
			 sportItem.setBooking(sport2Item.getBooking());
		}
		List<SportItem> topItemList = sportService.getTopSportItemList();
		List<SportItem> allItem = sportService.getAllSportItem();
		allItem.removeAll(topItemList);
		allItem.removeAll(itemList);
		MultiMap itemMap = new MultiValueMap();
		for(SportItem item:allItem){
			itemMap.put(item.getParentid(), item);
		}
		model.put("topItemList", topItemList);
		model.put("itemMap", itemMap);
		model.put("itemList", itemList);
		return "admin/sport/sportItemRelateList.vm";
	}
	@RequestMapping("/admin/sport/priceTableList.xhtml")
	public String priceTableList(@RequestParam("sportId")Long sportId, ModelMap model) throws Exception {
		Sport sport = daoService.getObject(Sport.class, sportId);
		model.put("sport", sport);
		List<SportPriceTable> priceTableList = sportService.getPriceTableListBySportId(sportId);
		model.put("priceTableList", priceTableList);
		List<SportItem> allItem = sportService.getAllSportItem();
		model.put("allItem", allItem);
		return "admin/sport/priceTableList.vm";
	}
	@RequestMapping("/admin/sport/priceList.xhtml")
	public String priceList(@RequestParam("priceTableId")Long priceTableId, ModelMap model) throws Exception {
		SportPriceTable priceTable = daoService.getObject(SportPriceTable.class, priceTableId);
		model.put("priceTable", priceTable);
		List<SportPrice> priceList = sportService.getSportPriceList(priceTableId);
		model.put("priceList", priceList);
		return "admin/sport/priceList.vm";
	}

	@RequestMapping("/admin/sport/trainingList.xhtml")
	public String trainingList(@RequestParam("sportId")Long sportId, ModelMap model) throws Exception {
		Sport sport = daoService.getObject(Sport.class, sportId);
		model.put("sport", sport);
		return "admin/sport/trainingList.vm";
	}
	@RequestMapping("/admin/sport/sportItemList.xhtml")
	public String coachList(ModelMap model) throws Exception {
		List<SportItem> sportItemList = sportService.getTopSportItemList();
		model.put("sportItemList", sportItemList);
		return "admin/sport/sportItemList.vm";
	}
	@RequestMapping("/admin/sport/modifySportDetail.xhtml")
	public String modifySport(@RequestParam(required=false, value="sportId")Long sportId, HttpServletRequest request, ModelMap model) {
		String defaultCountyCode = "";
		String defaultIndexareaCode = "";
		Long defaultLine = 0L;
		Long defaultStation = 0L;
		String citycode = null;
		Sport sport = null;
		if (sportId != null) {
			sport = daoService.getObject(Sport.class, sportId);
			if(StringUtils.isNotBlank(sport.getCountycode()))
				defaultCountyCode = sport.getCountycode();
			if(StringUtils.isNotBlank(sport.getIndexareacode()))
				defaultIndexareaCode = sport.getIndexareacode();
			defaultStation = sport.getStationid()==null? 0L:sport.getStationid();
			citycode = sport.getCitycode();
		}else{
			citycode = getAdminCitycode(request);
		}
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		List<Subwaystation> stationList =  new ArrayList<Subwaystation>();
		Subwayline subwayline = daoService.getObject(Subwayline.class, defaultLine);
		if(subwayline!=null) stationList = placeService.getSubwaystationsByLineId(subwayline.getId());
		model.put("sport", sport);
		model.put("lineList", lineList);
		model.put("stationList", stationList);
		model.put("defaultLineId", defaultLine);
		model.put("defaultStationId", defaultStation);
		model.put("defaultCountyCode", defaultCountyCode);
		model.put("defaultIndexareaCode", defaultIndexareaCode);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		County defaultCounty = daoService.getObject(County.class, defaultCountyCode);
		if(defaultCounty!=null){
			model.put("indexareaList", placeService.getIndexareaByCountyCode(defaultCounty.getCountycode()));
		}
		return "admin/sport/sportDetailForm.vm";
	}
	
	//推荐圈子
	@RequestMapping("/admin/sport/sportindex-commu.xhtml")
	public String indexCommu(HttpServletRequest request, ModelMap model,String signname){
		String url = "admin/sport/commu.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	//圈子活动
	@RequestMapping("/admin/sport/commuindex-activity.xhtml")
	public String activity(HttpServletRequest request, ModelMap model) {
		String signname = SignName.SPORTINDEX_COMMNUACTIVITY;
		String url = "admin/sport/activity.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	/**
	 * 运动首页推荐
	 * @return
	 */
	@RequestMapping("/admin/recommend/sportrecommend.xhtml")
	public String cinemaRecommend(HttpServletRequest request, ModelMap model){
		String citycode = this.getAdminCitycode(request);
		model.put("admincitycode", citycode);
		return "admin/sport/sportRecommend.vm";
	}
	
	/**
	 *  运动推荐区 排序
	 **/
	@RequestMapping("/admin/sport/sportByOrder.xhtml")
	public String cinemaByOrder(HttpServletRequest request, ModelMap model){
		String signname = SignName.SPORT_ORDER;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null, false, true, 0,50);
		if(gcList.isEmpty()){
			// 第一次加载, 没有任何推荐, 此时查出所有推荐区
			String cityCode = this.getAdminCitycode(request);
			Map<String, String> countyMap = placeService.getCountyPairByCityCode(cityCode);
			model.put("countyMap", countyMap);
		}
		model.put("signname", signname);
		model.put("gcList", gcList);
		return "admin/sport/sportByOrderRecommend.vm";
	}
	
	/**
	 *  运动推荐区 排序保存
	 **/
	@RequestMapping("/admin/sport/saveSportOrder.xhtml")
	public String saveCinemaOrder(String relatedids, String signname, String titles, String ordernums, HttpServletRequest request, ModelMap model){
		String[] relatedidss = StringUtils.split(relatedids, ",");
		String[] titless = StringUtils.split(titles, ",");
		String[] ordernumss = StringUtils.split(ordernums, ",");
		
		GewaCommend gewaCommend = null;
		for(int i=0; i<relatedidss.length; i++){
			gewaCommend  = new GewaCommend(signname, titless[i], new Long(relatedidss[i]), new Integer(ordernumss[i]));
			gewaCommend.setCitycode(this.getAdminCitycode(request));
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	
	/**
	 *  运动推荐区 单个排序保存
	 **/
	@RequestMapping("/admin/sport/saveSportOrderNum.xhtml")
	public String saveCinemaOrder(Long id, Integer ordernum, ModelMap model){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, id);
		if(gewaCommend != null){
			gewaCommend.setOrdernum(ordernum);
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/saveUpGrade.xhtml")
	public String saveUpGrade(String uid,String code,String upgradeUrl,String remark,String apptype,ModelMap model){
		SportUpGrade sug = mongoService.getObject(SportUpGrade.class, MongoData.DEFAULT_ID_NAME, uid);
		if(sug == null){
			sug = new SportUpGrade();
			String id = System.currentTimeMillis()+StringUtil.getRandomString(5);
			sug.setAddTime(new Timestamp(System.currentTimeMillis()));
			sug.setId(id);
		}        
		sug.setUpgradeUrl(upgradeUrl);
		sug.setVersionCode(code);
		sug.setRemark(remark);
		sug.setApptype(apptype);
		sug.setNickName(getLogonUser().getNickname());
		mongoService.saveOrUpdateObject(sug, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/sport/delUpGrade.xhtml")
	public String delUpGrade(ModelMap model,String uid){
		SportUpGrade sug = mongoService.getObject(SportUpGrade.class, MongoData.DEFAULT_ID_NAME, uid);
		mongoService.removeObject(sug, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	//新版运动项目列表
	@RequestMapping("/admin/sport/newSportItemList.xhtml")
	public String newCoachList(ModelMap model) throws Exception {
		List<SportItem> sportItemList = sportService.getTopSportItemList();
		model.put("sportItemList", sportItemList);
		return "admin/sport/new_sportItemList.vm";
	}
	//电商管理
	@RequestMapping("/admin/sport/sportProfileList.xhtml")
	public String sportProfileList(String key, Long siId, String company, Integer pageNo, String isBooking, HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		Boolean booking = true;
		if(StringUtils.equals(isBooking, Sport.BOOKING_CLOSE)){
			booking = false;
		}
		Integer rowsCount = sportService.getSportProfileCount(key, citycode, siId, company, booking);
		List<SportProfile> sportProfileList = new ArrayList<SportProfile>();
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 30;
		int first = rowsPerPage * pageNo;
		if(rowsCount>0){
			sportProfileList = sportService.getSportProfileList(key, citycode, siId, company, booking, first, rowsPerPage);
		}
		Map<Long, Sport> sportMap = new HashMap<Long, Sport>();
		Map<Long, List<SportItem>> itemMap = new HashMap<Long, List<SportItem>>();//运动项目
		Map<Long,Integer> sportOpenTimeTableCountMap=new HashMap<Long,Integer>();
		for(SportProfile spf : sportProfileList){
			List<SportItem> itemList = sportService.getSportItemListBySportId(spf.getId(), SportProfile.STATUS_OPEN);
			itemMap.put(spf.getId(), itemList);
			Sport sport = daoService.getObject(Sport.class, spf.getId());
			sportMap.put(spf.getId(), sport);
			Integer count=sportOrderService.getSportOpenTimeTableCount(spf.getId());
			sportOpenTimeTableCountMap.put(spf.getId(), count);
		}
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/sport/sportProfileList.xhtml");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("key", key);
		params.put("company", company);
		params.put("siId", siId);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<SportItem> sportItemList = daoService.getAllObjects(SportItem.class);
		model.put("sportItemList", sportItemList);
		model.put("sportMap", sportMap);
		model.put("pageUtil", pageUtil);
		model.put("sportProfileList", sportProfileList);
		model.put("itemMap", itemMap);
		model.put("sportOpenTimeTableCountMap", sportOpenTimeTableCountMap);
		return "admin/sport/sportProfileList.vm";
	}
	
	@RequestMapping("/admin/sport/ajax/updateSpPretype.xhtml")
	public String updateSpPretype(Long sportid, ModelMap model){
		User user = getLogonUser();
		SportProfile sp = daoService.getObject(SportProfile.class, sportid);
		if(sp == null) return showJsonError_NOT_FOUND(model);
		ChangeEntry changeEntry = new ChangeEntry(sp);
		sp.setPretype(SportProfile.PRETYPE_ENTRUST);
		daoService.saveObject(sp);
		monitorService.saveChangeLog(user.getId(), SportProfile.class, sp.getId(), changeEntry.getChangeMap(sp));
		return showJsonSuccess(model);
	}
	
	//新版场馆列表
	@RequestMapping("/admin/sport/newSportList.xhtml")
	public String newSportList(String flag, String key, Integer pageNo, HttpServletRequest request, ModelMap model) throws Exception {
		String citycode = getAdminCitycode(request);
		Integer rowsCount =0;
		List<Sport> sportList = new ArrayList<Sport>();
		if (pageNo == null) pageNo = 0;
		int rowsPerPage = 30;
		int first = rowsPerPage * pageNo;
		rowsCount =sportService.getSportCount(flag, key, citycode);
		if(rowsCount>0){
			sportList=sportService.getSportList(flag, key, citycode, first, rowsPerPage);
		}
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "admin/sport/newSportList.xhtml");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("key", key);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("sportList", sportList);
		model.put("flag", flag);
		return "admin/sport/new_sportList.vm";
	}
	//新版场馆详细
	@RequestMapping("/admin/sport/newModifySportDetail.xhtml")
	public String newModifySport(@RequestParam(required=false, value="sportId")Long sportId, HttpServletRequest request, ModelMap model) {
		String defaultCountyCode = "";
		String defaultIndexareaCode = "";
		Long defaultLine = 0L;
		Long defaultStation = 0L;
		String citycode = null;
		Sport sport = null;
		if (sportId != null) {
			sport = daoService.getObject(Sport.class, sportId);
			if(StringUtils.isNotBlank(sport.getCountycode()))
				defaultCountyCode = sport.getCountycode();
			if(StringUtils.isNotBlank(sport.getIndexareacode()))
				defaultIndexareaCode = sport.getIndexareacode();
			defaultStation = sport.getStationid()==null? 0L:sport.getStationid();
			citycode = sport.getCitycode();
		}else{
			citycode = getAdminCitycode(request);
		}
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		List<Subwaystation> stationList =  new ArrayList<Subwaystation>();
		Subwayline subwayline = daoService.getObject(Subwayline.class, defaultLine);
		if(subwayline!=null) stationList = placeService.getSubwaystationsByLineId(subwayline.getId());
		model.put("sport", sport);
		model.put("lineList", lineList);
		model.put("stationList", stationList);
		model.put("defaultLineId", defaultLine);
		model.put("defaultStationId", defaultStation);
		model.put("defaultCountyCode", defaultCountyCode);
		model.put("defaultIndexareaCode", defaultIndexareaCode);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		County defaultCounty = daoService.getObject(County.class, defaultCountyCode);
		if(defaultCounty!=null){
			model.put("indexareaList", placeService.getIndexareaByCountyCode(defaultCounty.getCountycode()));
		}
		return "admin/sport/new_sportDetailForm.vm";
	}
	@RequestMapping("/admin/sport/ajax/addSportProfile.xhtml")
	 public String getOpenSport(Long sportId, ModelMap model) {
		SportProfile sportProfile=null;
		Sport sport = null;
		if(sportId != null){
			sportProfile = daoService.getObject(SportProfile.class, sportId);
			sport = daoService.getObject(Sport.class, sportId);
		}
		if(sportProfile != null && sport != null){
			sportProfile.setBooking(SportProfile.STATUS_OPEN);
			daoService.saveObject(sportProfile);
			sport.setBooking(SportProfile.STATUS_OPEN);
			daoService.saveObject(sport);
		}else{
			return showJsonError(model, "参数错误！");
		}
		return showJsonSuccess(model);
	}
	//修改备注
	@RequestMapping("/admin/sport/ajax/setCompany.xhtml")
	 public String setCompany(Long sportId,String value2, ModelMap model) {
		SportProfile sportProfile = null;
		if(sportId != null) sportProfile = daoService.getObject(SportProfile.class, sportId);
		if(sportProfile != null) sportProfile.setCompany(value2);
		else return showJsonError(model, "参数错误！");
		daoService.saveObject(sportProfile);
		return showJsonSuccess(model);
	}
	//关闭预定
	@RequestMapping("/admin/sport/ajax/setBooking.xhtml")
	 public String setBooking(Long sid, String isOpen, ModelMap model) {
		SportProfile sportProfile = null;
		Sport sport = null;
		String booking = SportProfile.STATUS_CLOSE;
		if(StringUtils.equals(isOpen, SportProfile.STATUS_OPEN)){
			booking = SportProfile.STATUS_OPEN;
		}
		if(sid != null){
			sportProfile = daoService.getObject(SportProfile.class, sid);
			sport = daoService.getObject(Sport.class, sid);
		}
		if(sportProfile != null && sport != null){
			sportProfile.setBooking(booking);
			daoService.saveObject(sportProfile);
			sport.setBooking(booking);
			daoService.saveObject(sport);
		}else{
			return showJsonError(model, "参数错误！");
		}	
		return showJsonSuccess(model);
	}
	//新服务项目页
	@RequestMapping("/admin/sport/new_relateItemList.xhtml")
	public String sport2ItemList(@RequestParam("sportId")Long sportId, ModelMap model) throws Exception {
		Sport sport = daoService.getObject(Sport.class, sportId);
		model.put("sport", sport);
		List<Sport2Item> sport2ItemList = sportService.getSport2ItemListBySportId(sportId);
		Map<Long, Sport2Item> sport2ItemMap = BeanUtil.groupBeanList(sport2ItemList, "itemid");
		List<SportItem> itemList = daoService.getObjectList(SportItem.class, BeanUtil.getBeanPropertyList(sport2ItemList, Long.class, "itemid", true));
		model.put("sport2ItemMap", sport2ItemMap);
		model.put("itemList", itemList);
		return "admin/sport/new_sportItemRelateList.vm";
	}
	
}
