package com.gewara.web.action.sport;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.PageView;
import com.gewara.model.agency.Agency;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.common.UserOperation;
import com.gewara.model.content.DiscountInfo;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.DramaToStar;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.Sport2Item;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportItemPrice;
import com.gewara.model.sport.SportProfile;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberPicture;
import com.gewara.service.OperationService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.drama.DramaToStarService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.sport.MemberCardService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.MailService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.PictureComponent;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.MarkHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.partner.ObjectSpdiscountFilter;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.gym.RemoteGym;

/**
 * 
 * @author lss
 * 2010-10-26
 * 运动场馆信息
 */
@Controller
public class NewSportInfoController extends BaseSportController {
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("pictureComponent")
	private PictureComponent pictureComponent = null;
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;
	@Autowired@Qualifier("dramaToStarService")
	private DramaToStarService dramaToStarService;
	@Autowired@Qualifier("memberCardService")
	private MemberCardService memberCardService;
	//获取最近购票的用户
	private Map getSportOrderMemberMapBSportid(Long sportid, Integer maxnum, ModelMap model){
		Map map = new HashMap();
		List<Map> payMemberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_SPORT, sportid, 0, maxnum);
		if (!payMemberList.isEmpty()) {
			List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(payMemberList);
			map.put("payMemberList", payMemberList);
			addCacheMember(model, memberidList);
		}
		return map;
	}
	
	private static final String TAG_SPORT = "sport";

	private void addSportDetailData(ModelMap model, Sport sport){
		model.put("sport", sport);
		//附近其它运动场馆
		List<Sport> sportList = placeService.getPlaceListByCountyCode(Sport.class, sport.getCountycode(), "", false, 0, 5);
		if(!sportList.isEmpty()){
			sportList.remove(sport);
			if(sportList.size()>4) sportList = sportList.subList(0,4);
		}
		Map<Long,Integer> walaCount = new HashMap<Long, Integer>();
		for(Sport sportwala : sportList){
			Integer walaNum = commentService.getCommentCountByRelatedId(TagConstant.TAG_SPORT,sportwala.getId());
			walaCount.put(sportwala.getId(), walaNum);
		}
		model.put("walaCount", walaCount);
		model.put("sportList", sportList);
		
		Integer gwalaNum = 0;
		ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymList(sport.getCitycode(), sport.getCountycode(), sport.getIndexareacode(), "clickedtimes", false, 0, 1);
		if(gymCode.isSuccess() && !gymCode.getRetval().isEmpty()){
			RemoteGym gym = gymCode.getRetval().get(0);
			gwalaNum = commentService.getCommentCountByRelatedId(TagConstant.TAG_GYM, gym.getId());
			model.put("zbgym", gym);
		}
		model.put("gwalaNum", gwalaNum);
		
		Integer cwalaNum = 0;
		Cinema cinema = placeService.getZbPlace(Cinema.class, sport.getCountycode(), sport.getIndexareacode());
		if(cinema != null){
			cwalaNum = commentService.getCommentCountByRelatedId(TagConstant.TAG_CINEMA, cinema.getId());
		}
		model.put("cwalaNum", cwalaNum);
		model.put("zbcinema", cinema);
		model.put("subwaylineMap", placeService.getSubwaylineMap(sport.getCitycode()));
	}
	
	@RequestMapping("/sport/ajax/sendMessage.xhtml")
	public String sendSportMessage(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, Long relatedid, String mobile, String captchaId, String captcha,  ModelMap model){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showError(model, "请先登录！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
		Sport sport = daoService.getObject(Sport.class, relatedid);
		if(sport == null) return showJsonError(model, "运动场馆不存在或被删除！");
		String opkey = "sport_" + member.getId() + "_" + sport.getId();
		boolean allow = operationService.updateOperation(opkey, 10);
		if(!allow) return showJsonError(model, "你操作过于频繁，请稍后再试！");
		String tradeNo = "sport_" + sport.getId();
		Timestamp curtime = DateUtil.getCurFullTimestamp();
		
		Timestamp endtime = DateUtil.getLastTimeOfDay(curtime);
		String opsmskey = "sport_" + member.getId() + "_" + sport.getId()+"_sms"+DateUtil.formatDate(curtime);
		UserOperation op = daoService.getObject(UserOperation.class, opsmskey);
		if(op!=null && op.getOpnum() >= 3) return showJsonError(model, "短信发送次数已超过限制！");
		String sportPhone = "";
		if(StringUtils.isNotBlank(sport.getContactphone())){
			String[] strs = StringUtils.split(sport.getContactphone(), " ");
			sportPhone = strs[0];
		}
		String msgContent = sport.getRealBriefname()+"  地址:"+sport.getAddress()+"  电话:"+sportPhone;
		if(msgContent.length()>60){
			msgContent = msgContent.substring(0, 60);
			mailService.sendEmail("www.gewara.com", "场馆短信过长", "场馆短信超出60个字符，请及时进行修改！", "sandy.chen@gewara.com");
		}
		SMSRecord sms = new SMSRecord(mobile);
		sms.setTradeNo(tradeNo);
		sms.setContent(msgContent);
		sms.setSendtime(curtime);
		sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
		sms.setValidtime(endtime);
		sms.setTag(TagConstant.TAG_SPORT);
		sms.setMemberid(member.getId());
		sms.setRelatedid(sport.getId());
		sms = untransService.addMessage(sms);
		if(sms != null) untransService.sendMsgAtServer(sms, true);
		operationService.updateOperation(opsmskey, OperationService.ONE_DAY, 3);
		return showJsonSuccess(model);
	}
	
	//动态加载运动图片
	@RequestMapping("/sport/ajax/sportPictureList.xhtml")
	public String sportPictureList(ModelMap model, Long relatedid, Integer pageNo, String type){
		pictureComponent.pictureList(model, pageNo, TagConstant.TAG_SPORT, relatedid, type, "/sport/ajax/sportPictureList.xhtml");
		return "sport/new_ajaxSportPictureList.vm";
	}
	
	//运动图片详细
	@RequestMapping("/sport/sportPictureDetail.xhtml")
	public String newSportPicture(ModelMap model, Long sid, Long pid, String pvtype){
		Sport sport = daoService.getObject(Sport.class, sid);
		pictureComponent.pictureDetail(model, TagConstant.TAG_SPORT, sid, pid, pvtype);
		if(sport == null){
			Object obj = model.get(TagConstant.TAG_SPORT);
			if(obj == null) return show404(model, "该运动场馆不存在或被删除！");
			sport = (Sport) obj;
		}
		List<Map> mapList =  (List<Map>)model.get("mapList");
		List<Map<String,Object>> pics = new LinkedList<Map<String,Object>>();
		if(mapList != null){
			for(Map m : mapList){
				Map<String,Object> vm = new HashMap<String,Object>();
				vm.put("picturename",m.get("picturename"));
				vm.put("minpic","cw96h72/"+m.get("picturename"));
				vm.put("description", m.get("description"));
				vm.put("membername", m.get("membername"));
				vm.put("posttime", m.get("posttime"));
				vm.put("id",m.get("id"));
				pics.add(vm);
			}
		}
		Map jsonMap = new HashMap();
		jsonMap.put("sid", sid);
		jsonMap.put("vid", pid);
		jsonMap.put("pvtype", pvtype);
		jsonMap.put("pictureid", model.get("pictureid"));
		jsonMap.put("type", model.get("type"));
		jsonMap.put("pictureList", pics);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/sport/ajax/pictureAddClicktimes.xhtml")
	public void pictureAddClicktimes(Long pictureid){
		cacheDataService.getAndSetIdsFromCachePool(Picture.class, pictureid);
		cacheDataService.getAndSetClazzKeyCount(Picture.class, pictureid);
	}
	
	//新版运动场馆详细页
	@RequestMapping("/sport/sportDetail.xhtml")
	public String newSportDetail(Long sid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String itemId = request.getParameter("itemId");  //项目id
		if(StringUtils.isNotBlank(itemId))model.put("itemId", Long.parseLong(itemId));
		cacheDataService.getAndSetIdsFromCachePool(Sport.class, sid);
		cacheDataService.getAndSetClazzKeyCount(Sport.class, sid);
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		if (pageCacheService.isUseCache(request)) {// 先使用缓存
			PageParams params = new PageParams();
			params.addLong("sid", sid);
			if (StringUtils.isNotBlank(spkey)) {
				params.addSingleString("spkey", spkey);
			}
			PageView pageView = pageCacheService.getPageView(request, "sport/sportDetail.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("sport", sport);
		//经营项目
		List<SportItem> itemList = sportService.getSportItemListBySportId(sport.getId(), SportProfile.STATUS_OPEN);
		model.put("itemList", itemList);
		//最近购票用户
		model.putAll(getSportOrderMemberMapBSportid(sport.getId(), 10, model));
		//右侧周边场馆
		List<Sport> sportList = placeService.getPlaceListByCountyCode(Sport.class, sport.getCountycode(), "clickedtimes", false, 0, 6);
		if(!sportList.isEmpty()){
			sportList.remove(sport);
			if(sportList.size()>4) sportList = sportList.subList(0, 5);
			model.put("sportList", sportList);
		}
		//哇啦点评数
		Integer commnetCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_SPORT, sid);
		model.put("commnetCount", commnetCount);
		//右侧测评
		List<GewaCommend> cepingList = commonService.getGewaCommendList(citycode, SignName.SPORT_DETAIL_CEPING, sport.getId(), null, false, 0, 1);
		if(!cepingList.isEmpty()) {
			GewaCommend gc = cepingList.get(0);
			News news = daoService.getObject(News.class, gc.getRelatedid());
			model.put("cepingNews",news);
			model.put("ceping", gc);
		}
		//图片数量
		Integer picCount = pictureService.getPictureCountByRelatedid(TagConstant.TAG_SPORT, sid);
		Integer mPicCount = pictureService.getMemberPictureCount(sid, TagConstant.TAG_SPORT, null, TagConstant.FLAG_PIC, Status.Y);
		if(picCount > 0){
			List<Picture> pictureList = pictureService.getPictueList(TagConstant.TAG_SPORT, sid, "posttime", false, 0, 3);
			List<Map> pictureMapList = new ArrayList<Map>();
			pictureMapList.addAll(BeanUtil.getBeanMapList(pictureList, "limg"));
			if(pictureList.size() < 3){
				List<MemberPicture> memberPList = pictureService.getMemberPictureList(sid, TagConstant.TAG_SPORT, null, TagConstant.FLAG_PIC, Status.Y, 0, 3-pictureList.size());
				pictureMapList.addAll(BeanUtil.getBeanMapList(memberPList, "limg"));
			}
			model.put("pictureMapList", pictureMapList);
		}
		model.put("picCount", (picCount+mPicCount));
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		//公告
		List<DiscountInfo> discountInfoList = commonService.getCurrentDiscountInfoByRelatedid(TagConstant.TAG_SPORT, sid);
		model.put("discountInfoList", discountInfoList);
		//优惠信息
		ObjectSpdiscountFilter osf = new ObjectSpdiscountFilter(citycode, PayConstant.APPLY_TAG_SPORT, sport.getId(), DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> spdiscountList  =  spdiscountService.getSpecialDiscountData(osf, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_SPORT);		
		model.put("spdiscountList", spdiscountList);
		//培训课程
		List<TrainingGoods> trainingGoodsList = agencyService.getTrainingGoodsList(citycode, TagConstant.TAG_AGENCY, null, null, null, sport.getId(),"goodssort", true, false, 0, 3);
		model.put("trainingGoodsList", trainingGoodsList);
		Map<Long, Agency> agencyMap = new HashMap<Long, Agency>();
		Map<Long,List<DramaToStar>> tcDtsListMap = new HashMap<Long,List<DramaToStar>>();
		List<DramaToStar> tempList = new ArrayList<DramaToStar>();
		for (TrainingGoods trainingGoods : trainingGoodsList) {
			Agency agency = daoService.getObject(Agency.class, trainingGoods.getRelatedid());
			agencyMap.put(trainingGoods.getId(), agency);
			List<DramaToStar> tcDtsList = dramaToStarService.getDramaToStarListByDramaid(GoodsConstant.GOODS_TYPE_TRAINING, trainingGoods.getId(), false);
			tcDtsListMap.put(trainingGoods.getId(), tcDtsList);
			tempList.addAll(tcDtsList);
		}
		List<Long> starIdList = BeanUtil.getBeanPropertyList(tempList, Long.class, "starid", true);
		Map<Long,DramaStar> starMap = daoService.getObjectMap(DramaStar.class, starIdList);
		List<MemberCardType> mctList = memberCardService.getBookingMemberCardTypeListBySportids(sport.getId());
		Map<Long, String> fitItemMap = new HashMap<Long, String>();
		for(MemberCardType mct : mctList){
			fitItemMap.put(mct.getId(), memberCardService.getFitItem(mct.getFitItem()));
		}
		model.put("sp", daoService.getObject(SportProfile.class, sport.getId()));
		model.put("starMap", starMap);
		model.put("mctList", mctList);
		model.put("fitItemMap", fitItemMap);
		model.put("agencyMap", agencyMap);
		model.put("tcDtsListMap", tcDtsListMap);
		return "sport/wide_sportDetail.vm";
	}
	
	//公用头部
	private void sportHeader(Long sid, String citycode, ModelMap model){
		List<SportItem> sportItemList = sportService.getSportItemListBySportId(sid, SportProfile.STATUS_OPEN);
		model.put("sportItemList", sportItemList);
		//点评数
		Integer commnetCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_SPORT, sid);
		model.put("commnetCount", commnetCount);
		//活动数
		ErrorCode<Integer> code = synchActivityService.getActivityCount(citycode, null, RemoteActivity.TIME_ALL, TagConstant.TAG_SPORT, sid);
		if(code.isSuccess()) model.put("activityCount", code.getRetval());
		//哇啦数
		Integer commentCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_SPORT, sid);
		model.put("commentCount", commentCount);
		//图片数
		Integer picCount = pictureService.getPictureCountByRelatedid(TagConstant.TAG_SPORT, sid);
		Integer mPicCount = pictureService.getMemberPictureCount(sid, TagConstant.TAG_SPORT, null, TagConstant.FLAG_PIC, Status.Y);
		
		model.put("picCount", (picCount+mPicCount));
		//资讯数
		Integer newsCount = newsService.getNewsCount(citycode, TagConstant.TAG_SPORT, "", sid, null);
		model.put("newsCount", newsCount);
		//培训数
		Integer sportgoodsCount = goodsService.getGoodsCount(SportGoods.class, TagConstant.TAG_SPORT, sid, true, false, true);
		if(sportgoodsCount > 0) model.put("hasSportGoods", true);
		else model.put("hasSportGoods", false);
		Integer trainCount = commonService.getDiscountInfoCount(sid, TagConstant.TAG_SPORTTRAIN);
		model.put("trainCount", (sportgoodsCount + trainCount));
		//是否可预订
		int count = openTimeTableService.getOpenTimeTableCount(sid, null, DateUtil.getCurDate(), null, null);
		model.put("booking", count ==0 ? false : true);
		model.put("markHelper", new MarkHelper());
	}
	//运动新版4.0 得到开放类型
	@RequestMapping("/sport/ajax/getSportOttOpenTypeList.xhtml")
	public String getSportOttOpenTypeList(ModelMap model,Long sportId,Long itemId, HttpServletRequest request){
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		Sport sport = daoService.getObject(Sport.class, sportId);
		if(sport == null) return showJsonError(model, "你访问的运动场馆不存在！");
		SportItem sportItem = daoService.getObject(SportItem.class, itemId);
		if(sportItem == null) return showJsonError(model, "你访问的运动项目不存在！");
		List<String> opentypeList = openTimeTableService.getOpenTimeTableOpenTypeList(sportId, itemId, DateUtil.currentTime(), null, false);
		model.put("opentypeList", opentypeList);
		model.put("itemid", itemId);
		return "sport/wide_openTypeList.vm";
	}
	//运动新版4.0 根据opentype,sportid,itemid查询opentimetable
	@RequestMapping("/sport/ajax/getSportOttList.xhtml")
	public String getSportOttList(Long sportId, Long itemId, String openType, ModelMap model, HttpServletRequest request){
		String spkey = request.getParameter("spkey");
		model.put("spkey", spkey);
		
		Sport sport = daoService.getObject(Sport.class, sportId);
		if(sport == null) return showJsonError(model, "你访问的运动场馆不存在！");
		SportItem sportItem = daoService.getObject(SportItem.class, itemId);
		if(sportItem == null) return showJsonError(model, "你访问的运动项目不存在！");
		Sport2Item sport2Item = sportService.getSport2Item(sportId, itemId);
		SportProfile sp = daoService.getObject(SportProfile.class, sport.getId());
		model.put("sp", sp);
		model.put("sport", sport);
		model.put("sport2Item", sport2Item);
		model.put("sportItem", sportItem);
		Map<Long, Map<String, Integer>> ottPriceMap = new HashMap<Long, Map<String, Integer>>();
		List openTypeList = new ArrayList();  
		List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sportId, itemId, DateUtil.currentTime(), null, openType, false, 0, 14);
		for(OpenTimeTable ott : ottList){
			if(!openTypeList.contains(ott.getOpenType())) openTypeList.add(ott.getOpenType());
			Map<String, Integer> ottPrice = sportService.getSportPriceByOtt(sportId, itemId, ott.getId());
			ottPriceMap.put(ott.getId(), ottPrice);
		}
		model.put("openTypeList", openTypeList);
		model.put("ottList", ottList);
		if(ottList.isEmpty()){
			List<SportItemPrice> sipList = sportService.getSportItemPriceListBySportIdAndItemId(sportId, itemId);
			model.put("sipList", sipList);
		}
		model.put("ottPriceMap", ottPriceMap);
		return "sport/wide_reserve.vm";
	}
	
	@RequestMapping("/sport/ajax/getSportOtt.xhtml")
	public String getSportOtt(ModelMap model,Long sportId,Long itemId){
		Sport sport = daoService.getObject(Sport.class, sportId);
		if(sport == null) return showJsonError(model, "你访问的运动场馆不存在！");
		Sport2Item sport2Item =sportService.getSport2Item(sportId, itemId);
		SportItem sportItem= daoService.getObject(SportItem.class, itemId);
		SportProfile sp = daoService.getObject(SportProfile.class, sport.getId());
		model.put("sp", sp);
		model.put("sport", sport);
		model.put("sport2Item", sport2Item);
		model.put("sportItem", sportItem);
		Map<Long, Map<String, Integer>> ottPriceMap = new HashMap<Long, Map<String, Integer>>();
		List openTypeList = new ArrayList();  
		List<OpenTimeTable> ottList = openTimeTableService.getOpenTimeTableList(sportId, itemId, DateUtil.currentTime(), null, null, false, 0, 7);
		for(OpenTimeTable ott : ottList){
			if(!openTypeList.contains(ott.getOpenType())) openTypeList.add(ott.getOpenType());
			Map<String, Integer> ottPrice = sportService.getSportPriceByOtt(sportId, itemId, ott.getId());
			ottPriceMap.put(ott.getId(), ottPrice);
		}
		model.put("openTypeList", openTypeList);
		model.put("ottList", ottList);
		if(ottList.isEmpty()){
			List<SportItemPrice> sipList = sportService.getSportItemPriceListBySportIdAndItemId(sportId, itemId);
			model.put("sipList", sipList);
		}
		model.put("ottPriceMap", ottPriceMap);
		return "sport/module/reserve.vm";
	}
	
	//培训
	@RequestMapping("/sport/sportTrain.xhtml")
	public String sportTrain(Long sid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		sportHeader(sid, citycode, model);
		List<SportGoods> sportgoodsList = goodsService.getSportGoodsList(citycode, TagConstant.TAG_SPORT, sid, true, false, true, "goodssort", true);
		model.put("sportgoodsList", sportgoodsList);
		List<DiscountInfo> discountInfoList = commonService.getCurrentDiscountInfoByRelatedid(TagConstant.TAG_SPORTTRAIN, sid);
		model.put("discountInfoList", discountInfoList);
		//周边信息
		addSportDetailData(model, sport);
		return "sport/sportTrain.vm";
	}
	
	//价格信息
	@RequestMapping("/sport/pricetable.xhtml")
	public String pricetable(Long sid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		sportHeader(sid, citycode, model);
		String itemId = request.getParameter("itemId");  //项目id
		if(StringUtils.isNotBlank(itemId))model.put("itemId", Long.parseLong(itemId));
		//经营项目LIST
		List<SportItem> itemList = sportService.getSportItemListBySportId(sport.getId(), SportProfile.STATUS_OPEN);
		for(int i=0;itemList!=null&&i<itemList.size();i++){
			SportItem sportItem=itemList.get(i);
			Sport2Item sport2Item=sportService.getSport2Item(sport.getId(),sportItem.getId());
			if(sport2Item!=null)
				sportItem.setOtherinfo(sport2Item.getOtherinfo());
		}
		for(int i=0;itemList!=null&&i<itemList.size();i++){
			SportItem sportItem=itemList.get(i);
			if(itemId!=null){
				 if(sportItem.getId().longValue()==Long.parseLong(itemId)){
					 itemList.remove(sportItem);
					 itemList.add(0, sportItem);
					 break;
				 }
			}
		}
		model.put("itemList", itemList);
		//最近购票用户
		model.putAll(getSportOrderMemberMapBSportid(sport.getId(), 8, model));
		//周边信息
		addSportDetailData(model, sport);
		return "sport/sportPrice.vm";
	}
	
	//新版运动场馆活动
	@RequestMapping("/sport/activityList.xhtml")
	public String sportActivity(ModelMap model,Long sid,Integer pageNo, HttpServletResponse response,HttpServletRequest request){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		//周边信息
		addSportDetailData(model, sport);
		sportHeader(sid,citycode,model);
		if(pageNo == null) pageNo = 0;
		int maxnum = 8;
		int from = pageNo * maxnum;
		int count = 0;
		if(model.get("activityCount") != null) count = Integer.parseInt(model.get("activityCount")+"");
		PageUtil pageUtil = new PageUtil(count, maxnum, pageNo, "sport/activityList.xhtml", true, true);
		Map params = new HashMap();
		params.put("sid", sid);
		pageUtil.initPageInfo(params);
		Map<Long, String> sportItemMap = new HashMap<Long, String>();
		Map<Long, List> reserCountMap = new HashMap<Long, List>();
		if(count>0){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, null, RemoteActivity.TIME_ALL, TagConstant.TAG_SPORT, sport.getId(), null, null, null, from, maxnum);
			if(code.isSuccess()){
				List<RemoteActivity> activityList = code.getRetval();
				for(RemoteActivity activity : activityList){
					if(activity.getCategoryid() != null){
					SportItem sportItem=daoService.getObject(SportItem.class, activity.getCategoryid());
					if(sportItem != null) sportItemMap.put(activity.getId(), sportItem.getItemname());
					}
					if(VmUtils.eq(activity.getSign(), RemoteActivity.SIGN_RESERVE)){
						ErrorCode<List<RemoteApplyjoin>> code2 = synchActivityService.getApplyJoinListByActivityid(activity.getId());
						if(code2.isSuccess()){
							List<RemoteApplyjoin> applyJoinList = code2.getRetval();
							if(!applyJoinList.isEmpty()){
								List<Long> memberidList = ServiceHelper.getMemberIdListFromBeanList(applyJoinList);
								reserCountMap.put(activity.getId(),applyJoinList);
								addCacheMember(model,memberidList);
							}
						}
					 }
				}
				model.put("activityList", activityList);
			}
			model.put("pageUtil", pageUtil);
			model.put("sportItemMap", sportItemMap);
			model.put("reserCountMap", reserCountMap);
		}
		return "sport/sportActivity.vm";
	}

	//新版运动场馆哇啦
	@RequestMapping("/sport/commentList.xhtml")
	public String sportCommentList(ModelMap model,Long sid, HttpServletResponse response,HttpServletRequest request){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		//周边信息
		addSportDetailData(model, sport);
		sportHeader(sid, citycode, model);
		model.put("sport", sport);
		return "sport/sportComment.vm";
	}


	//新版运动场馆资讯
	@RequestMapping("/sport/newsList.xhtml")
	public String sportNesList(ModelMap model,Long sid,Integer pageNo, HttpServletResponse response,HttpServletRequest request){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request,citycode, response);
		//周边信息
		addSportDetailData(model, sport);
		sportHeader(sid,citycode,model);
		if(pageNo == null) pageNo = 0;
		int maxNum = 6;
		int from = pageNo * maxNum;
		List<News> newsList = newsService.getNewsList(citycode, TAG_SPORT, sid, "", from, maxNum);
		Integer count = newsService.getNewsCount(citycode, TAG_SPORT, null, sid, null);
		Map params = new HashMap();
		params.put("sid", sid);
		PageUtil pageUtil = new PageUtil(count, maxNum, pageNo, "sport/newsList.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("newsList", newsList);
		model.put("pageUtil", pageUtil);
		return "sport/sportNews.vm";
	}
	
	//新版运动场馆图片
	@RequestMapping("/sport/sportPictureList.xhtml")
	public String sportPicture(ModelMap model,Long sid,Integer pageNo, HttpServletResponse response,HttpServletRequest request){
		Sport sport = daoService.getObject(Sport.class, sid);
		if(sport == null) return show404(model, "你访问的运动场馆不存在！");
		String citycode = sport.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		//周边信息
		addSportDetailData(model, sport);
		sportHeader(sid,citycode,model);
		if(pageNo == null) pageNo = 0;
		int maxNum = 18;
		int from = pageNo * maxNum;
		//场馆图片
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TAG_SPORT, sid, from, maxNum);
		Integer count = pictureService.getPictureCountByRelatedid(TAG_SPORT, sid);
		int memberPictureCount = pictureService.getMemberPictureCount(sid, "sport", null, TagConstant.FLAG_PIC, Status.Y);
		model.put("memberPictureCount", memberPictureCount);
		Map params = new HashMap();
		params.put("sid", sid);
		PageUtil pageUtil = new PageUtil(count, maxNum, pageNo,"sport/sportPictureList.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pictureList", pictureList);
		model.put("count", count);
		model.put("pageUtil", pageUtil);
		return "sport/sportPicture.vm";
	}
}
