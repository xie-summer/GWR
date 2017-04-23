package com.gewara.web.action.drama;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.Flag;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.json.PageView;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.user.Member;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.VideoService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CacheDataService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.order.impl.SpdiscountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.MarkHelper;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.partner.DramaSpdiscountFilter;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class NewDramaDetailController extends BaseDramaController {
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	@Autowired@Qualifier("markService")
	private MarkService markService;
	@Autowired@Qualifier("cacheDataService")
	private CacheDataService cacheDataService;
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("spdiscountService")
	private SpdiscountService spdiscountService;
	@Autowired@Qualifier("goodsService")
	protected GoodsService goodsService;
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;

	// 话剧剧照
	@RequestMapping("/drama/dramaPicture.xhtml")
	public String dramaPictureList(Long dramaid, HttpServletRequest request, ModelMap model, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null)
			return show404(model, "该话剧不存在或被删除！");
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_DRAMA, drama.getId()));
		int pictureCount = pictureService.getPictureCountByRelatedid(TagConstant.TAG_DRAMA, drama.getId());
		int memberPictureCount = pictureService.getMemberPictureCount(drama.getId(), TagConstant.TAG_DRAMA, null, TagConstant.FLAG_PIC, Status.Y);
		model.put("drama", drama);
		model.put("pictureCount", pictureCount);
		model.put("memberPictureCount", memberPictureCount);
		getDramaCount(drama, citycode, model);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		// 套头信息
		getHeadInfo(drama, model);
		// 活动
		getActivityList(citycode, null, rh, model);
		// 剧社
		getTroupeCompany(drama, model);
		// 人物
		getActorsList(drama, model);
		return "drama/new_dramaPicture.vm";
	}

	// 话剧剧照详细
	@RequestMapping("/drama/dramaPictureDetail.xhtml")
	public String dramaPictureDetail(ModelMap model, Long dramaid, Long pid, String pvtype, HttpServletRequest request, HttpServletResponse response) {
		Drama drama = daoService.getObject(Drama.class, dramaid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		pictureComponent.pictureDetail(model, TagConstant.TAG_DRAMA, dramaid, pid, pvtype);
		if (drama == null) {
			Object obj = model.get(TagConstant.TAG_DRAMA);
			if (obj == null)
				return show404(model, "该话剧不存在或被删除！");
			drama = (Drama) obj;
		}
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_DRAMA, drama.getId()));
		// 哇啦
		List<Comment> commentList = commentService.getCommentListByRelatedId("picture",null, pid, null, 0, 4);
		Map<Long, Comment> tranferCommentMap = new HashMap<Long, Comment>();// 转载评论
		for (Comment comment : commentList) {
			if (comment.getTransferid() != null) {
				Comment c = commentService.getCommentById(comment.getTransferid());
				if (c != null && StringUtils.isNotBlank(c.getBody())) {
					tranferCommentMap.put(c.getId(), c);
				}
			}
		}
		model.put("commentList", commentList);
		model.put("drama", drama);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		// 套头信息
		getHeadInfo(drama, model);
		// 活动
		getActivityList(citycode, null, rh, model);
		// 剧社
		getTroupeCompany(drama, model);
		// 人物
		getActorsList(drama, model);
		getDramaCount(drama, citycode, model);
		return "drama/new_dramaPictureDetail.vm";
	}

	// 上传话剧剧照
	@RequestMapping("/drama/newAttachDramaPicture.xhtml")
	public String attachDramaPicture(ModelMap model, String tag, Long relatedid, HttpServletRequest request, HttpServletResponse response) {
		String citycode = WebUtils.getAndSetDefault(request, response);
		Drama drama = daoService.getObject(Drama.class, relatedid);
		if (drama == null)
			return show404(model, "该话剧不存在或被删除！");
		Map dataMap = pictureComponent.attachRelatePicture(tag, relatedid, citycode);
		model.putAll(dataMap);
		model.put("tag", tag);
		model.put("relatedid", relatedid);
		getDramaCount(drama, citycode, model);
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		// 套头信息
		getHeadInfo(drama, model);
		// 活动
		getActivityList(citycode, null, rh, model);
		// 剧社
		getTroupeCompany(drama, model);
		// 人物
		getActorsList(drama, model);
		return "drama/new_attachPicture.vm";
	}

	@RequestMapping("/drama/ajax/newDramaPictureList.xhtml")
	public String dramaPictureList(ModelMap model, Long relatedid, Integer pageNo, String type) {
		pictureComponent.pictureList(model, pageNo, TagConstant.TAG_DRAMA, relatedid, type, "/drama/ajax/newDramaPictureList.xhtml");
		return "drama/new_ajaxDramaPictureList.vm";
	}

	// 活动
	private void getActivityList(String citycode, Long dramaid, RelatedHelper rh, ModelMap model) {
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = null;
		if(dramaid != null){
			code = synchActivityService.getActivityListByTimetype(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, null, "drama", dramaid, null, null, 0, 3);
		}else{
			code = synchActivityService.getActivityListByTimetype(citycode,null, RemoteActivity.TIME_CURRENT, null, "drama", dramaid, null, null, 0, 5);
		}
		if(code.isSuccess()){
			activityList = code.getRetval();
		}
		model.put("activityList", activityList);
		List<Serializable> theatreIdList = BeanUtil.getBeanPropertyList(activityList, Serializable.class, "relatedid", true);
		relateService.addRelatedObject(1, "activityList", rh, TagConstant.TAG_THEATRE, theatreIdList);
	}

	// 剧社
	private void getTroupeCompany(Drama drama, ModelMap model) {
		if (StringUtils.isNotBlank(drama.getTroupecompany())) {
			List<DramaStar> dramaList = dramaStarService.getDramaStarList(null, DramaStar.TYPE_TROUPE, null, drama.getTroupecompany(), 0, 1);
			if (dramaList.size() > 0) {
				DramaStar dramaStar = dramaList.get(0);
				model.put("dramaStar", dramaStar);
				List<Map<String, String>> historyDramaList = JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
				model.put("historyDramaList", historyDramaList);
				List<DramaStar> troupeStartList = dramaStarService.getDramaStarListByTroupe(TagConstant.TAG_DRAMA, dramaStar.getTroupe(), DramaStar.TYPE_STAR, 0, 4);
				model.put("troupeStartList", troupeStartList);
			}
		}
	}

	// 人物
	private void getActorsList(Drama drama, ModelMap model) {
		List<DramaStar> actorsList = new ArrayList<DramaStar>();
		if(StringUtils.isNotBlank(drama.getActors())){
			List<Long> actorIdList = BeanUtil.getIdList(drama.getActors(), ",");
			actorsList = daoService.getObjectList(DramaStar.class, actorIdList);
			Map<Long, List<Drama>> dramaListMap = new HashMap<Long, List<Drama>>();
			Map<Long, List<Map<String, String>>> historyDramaListMap = new HashMap<Long, List<Map<String, String>>>();
			for (DramaStar dramaStar : actorsList) {
				List<Map<String, String>> tmp =  JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
				historyDramaListMap.put(dramaStar.getId(), tmp);
				List<Drama> dramaList = openDramaService.getDramaByStarid(dramaStar.getId(), 0, 2);
				dramaListMap.put(dramaStar.getId(), dramaList);
			}
			model.put("dramaListMap", dramaListMap);
			model.put("historyDramaListMap", historyDramaListMap);
		}
		model.put("actorsList", actorsList);
	}

	// 主创人员
	private void getDirectorsList(Drama drama, ModelMap model) {
		List<DramaStar> directorsList = new ArrayList<DramaStar>();
		if(StringUtils.isNotBlank(drama.getDirector())){
			List<Long> directorIdList = BeanUtil.getIdList(drama.getDirector(), ",");
			directorsList = daoService.getObjectList(DramaStar.class, directorIdList);
			Map<Long, List<Map<String, String>>> directorDramaListMap = new HashMap<Long, List<Map<String, String>>>();
			for (DramaStar dramaStar : directorsList) {
				List<Map<String, String>> tmp =  JsonUtils.readJsonToObject(new TypeReference<List<Map<String, String>>>(){}, dramaStar.getRepresentativeRelate());
				directorDramaListMap.put(dramaStar.getId(), tmp);
			}
			model.put("directorDramaListMap", directorDramaListMap);
		}
		model.put("directorsList", directorsList);
	}

	// 套头信息
	private void getHeadInfo(Drama drama, ModelMap model) {
		if(StringUtils.isNotBlank(drama.getTroupecompany())){
			List<Long> dramaIdCompanyList = BeanUtil.getIdList(drama.getTroupecompany(), ",");
			// 出品单位
			List<DramaStar> troupeList = daoService.getObjectList(DramaStar.class, dramaIdCompanyList);
			model.put("troupeList", troupeList);
		}
		// 演出场数
		Timestamp curTime = DateUtil.getCurFullTimestamp();
		Integer dramaPlayCount = dramaPlayItemService.getDramaCount(drama.getId(), curTime);
		model.put("dramaPlayCount", dramaPlayCount);
		// 更多属性
		Map<String, String> dramaDataMap = VmUtils.readJsonToMap(drama.getDramadata());
		model.put("dramaDataMap", dramaDataMap);
		// 套头
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_DRAMA, drama.getId()));
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		markCountMap.put(drama.getId(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_DRAMA, drama.getId()));

		// 评分统计
		model.put("curMarkCountMap", markCountMap);
		model.putAll(markService.getGradeCount(TagConstant.TAG_DRAMA, drama.getId()));
		model.put("markData", markService.getMarkdata(TagConstant.TAG_DRAMA));
		model.put("markHelper", new MarkHelper());
		model.putAll(markService.getPercentCount(TagConstant.TAG_DRAMA, drama.getId()));
		
		//优惠信息
		//getSpecialDiscountMap(drama.getCitycode(), drama.getId(), model);
		//价格
		List<Integer> allPriceList = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
		model.put("allPriceList", allPriceList);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
	}

	private void getDramaCount(Drama drama, String citycode, ModelMap model) {
		// 剧评数
		Integer diaryCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_DRAMA, drama.getId());
		model.put("diaryCount", diaryCount);
		// 哇啦数 Integer commentCount = commentService.getCommentCountByRelatedId(TagConstant.TAG_DRAMA, drama.getId());
		Integer commentCount = commonService.getCommentCount().get(drama.getId()+TagConstant.TAG_DRAMA);
		commentCount = commentCount == null? 0 :commentCount;
		model.put("commentCount", commentCount);
		model.putAll(pictureComponent.getCommonData(TagConstant.TAG_DRAMA, citycode, drama.getId()));
	}
	
	// 保存短信排片
	@RequestMapping("/ajax/drama/savePlayItemMessage.xhtml")
	public String savePlayItemMessage(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String mptag, Long mprelatedid, Long mpcategoryid,String mobile, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError(model, "请先登录！");
		if (!StringUtils.equals(mptag, TagConstant.TAG_THEATRE)) return showJsonError(model, "参数错误！");
		if (!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号格式不合法！");
		String opkey = "dramaplayitem" + WebUtils.getRemoteIp(request);
		boolean allow = operationService.isAllowOperation(opkey, 30, OperationService.ONE_DAY, 10);
		if (!allow)	return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Drama drama = daoService.getObject(Drama.class, mpcategoryid);
		if(drama == null) return showJsonError(model, "关联剧目不存在！");
		Date playDate = drama.getReleasedate();
		Timestamp starttime = new Timestamp(playDate.getTime());
		Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(playDate).getTime());
		List<OpenDramaItem> odiList = openDramaService.getOdiList(null, mprelatedid, mpcategoryid, starttime, endtime, true);
		if (!odiList.isEmpty())	return showJsonError(model, "当前时期已有排片！");
		String msg = "演出《" + drama.getBriefname() + "》" + DateUtil.format(playDate, "MM月dd日") + "场次已开放购票，登录格瓦拉生活网http://t.cn/ap63v4可以在线购票了";
		nosqlService.addPlayItemMessage(member.getId(), mptag, mprelatedid, playDate, mpcategoryid, mobile, Status.N, AddressConstant.ADDRESS_WEB, msg);
		operationService.updateOperation(opkey, 30, OperationService.ONE_DAY, 10);
		return showJsonSuccess(model);
	}
	// 保存短信排片
	@RequestMapping("/ajax/drama/savePlayItemMessageWithcaptchaId.xhtml")
	public String savePlayItemMessage(@CookieValue(value = LOGIN_COOKIE_NAME, required = false)
	String sessid, HttpServletRequest request, String mptag, Long mprelatedid, Long mpcategoryid,String mobile,  String captchaId, String captcha, ModelMap model) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if (member == null) return showJsonError(model, "请先登录！");
		if (!StringUtils.equals(mptag, TagConstant.TAG_THEATRE)) return showJsonError(model, "参数错误！");
		if (!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号格式不合法！");
		String opkey = "dramaplayitem" + WebUtils.getRemoteIp(request);
		boolean allow = operationService.isAllowOperation(opkey, 30, OperationService.ONE_DAY, 10);
		if (!allow)	return showJsonError(model, "你操作过于频繁，请稍后再试！");
		Drama drama = daoService.getObject(Drama.class, mpcategoryid);
		if(drama == null) return showJsonError(model, "关联剧目不存在！");
		Date playDate = drama.getReleasedate();
		Timestamp starttime = new Timestamp(playDate.getTime());
		Timestamp endtime = new Timestamp(DateUtil.getLastTimeOfDay(playDate).getTime());
		List<OpenDramaItem> odiList = openDramaService.getOdiList(null, mprelatedid, mpcategoryid, starttime, endtime, true);
		if (!odiList.isEmpty())	return showJsonError(model, "当前时期已有排片！");
		String msg = "演出《" + drama.getBriefname() + "》" + DateUtil.format(playDate, "MM月dd日") + "场次已开放购票，登录格瓦拉生活网http://t.cn/ap63v4可以在线购票了";
		nosqlService.addPlayItemMessage(member.getId(), mptag, mprelatedid, playDate, mpcategoryid, mobile, Status.N, AddressConstant.ADDRESS_WEB, msg);
		operationService.updateOperation(opkey, 30, OperationService.ONE_DAY, 10);
		return showJsonSuccess(model);
	}
	
	private void spdiscountList(ModelMap model, Drama drama){
		DramaSpdiscountFilter osf = new DramaSpdiscountFilter(drama, DateUtil.getCurFullTimestamp());
		List<SpecialDiscount> spdiscountList  =  spdiscountService.getSpecialDiscountData(osf, SpecialDiscount.OPENTYPE_GEWA, PayConstant.APPLY_TAG_DRAMA);		
		model.put("spdiscountList", spdiscountList);	
	}
	
	@RequestMapping("/drama/diaryList.xhtml")
	public String transDiaryList(Integer pageNo,String type, String searchKey, String timeType, ModelMap model, HttpServletRequest request,	HttpServletResponse response) {
		if (StringUtils.isBlank(type)){
			type = "drama";
		}
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 5;
		int firstPerPage = pageNo * rowsPerPage;
		List<Diary> diaryList = new ArrayList<Diary>();
		Integer rowsCount = 0;
		String citycode = WebUtils.getAndSetDefault(request, response);
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Timestamp fromtime = null;
		if("weekcount".equals(timeType)){
			fromtime = DateUtil.getBeginTimestamp(DateUtil.addDay(cur,-7));
		}else if("monthcount".equals(timeType)){
			fromtime = DateUtil.getBeginTimestamp(DateUtil.addDay(cur, -30));
		}else{
			timeType = "allcount";
		}
		if ("new".equals(type)) {
			rowsCount = diaryService.getDiaryCountByKey(Diary.class, citycode, null, TagConstant.TAG_DRAMA, null,searchKey, fromtime, cur);
			diaryList = diaryService.getDiaryBySearchkeyAndOrder(citycode,searchKey, fromtime, cur, "addtime",firstPerPage,rowsPerPage);
			List<Long> dramaIds = BeanUtil.getBeanPropertyList(diaryList,Long.class,"categoryid", true);
			Map<Long,Drama> dramas = daoService.getObjectMap(Drama.class,dramaIds);
			model.put("dramasMap",dramas);
			model.put("diaryList", diaryList);
		} else if ("hot".equals(type)) {
			rowsCount = diaryService.getDiaryCountByKey(Diary.class, citycode, null, TagConstant.TAG_DRAMA, null, searchKey, fromtime,cur);
			diaryList = diaryService.getDiaryBySearchkeyAndOrder(citycode,searchKey, fromtime , cur, "sumnumed", firstPerPage, rowsPerPage);
			List<Long> dramaIds = BeanUtil.getBeanPropertyList(diaryList,Long.class,"categoryid", true);
			Map<Long,Drama> dramas = daoService.getObjectMap(Drama.class,dramaIds);
			model.put("dramasMap",dramas);
			model.put("diaryList", diaryList);
		}else if("drama".equals(type)){
			List<Long> idList = new ArrayList();
			if(fromtime == null){
				idList = blogService.getIdListBlogDataByTag(citycode, TagConstant.TAG_DRAMA, "dramaname", searchKey, false, "", firstPerPage, rowsPerPage);
				rowsCount = blogService.getIdCountBlogDataByTag(citycode, TagConstant.TAG_DRAMA, "dramaname", searchKey);
			}else{
				Date startdate = DateUtil.getDateFromTimestamp(fromtime);
				Date enddate = DateUtil.getBeginningTimeOfDay(DateUtil.getDateFromTimestamp(cur));
				idList = blogService.getIdListEveryDayByTag(citycode, TagConstant.TAG_DRAMA, "dramaname", searchKey, TagConstant.TAG_DIARY, startdate, enddate, firstPerPage, rowsPerPage);
				rowsCount = blogService.getIdCountEveryDayByTag(citycode, TagConstant.TAG_DRAMA, "dramaname", searchKey, TagConstant.TAG_DIARY, startdate, enddate);
			}
			List<Drama> dramaList = daoService.getObjectList(Drama.class, idList);
			Map<Long,List<Diary>> dramaDiarys = new HashMap<Long,List<Diary>>();
			Map<Long,Integer> dramaWalaCount = new HashMap<Long,Integer>();
			for(Drama drama:dramaList){
				List<Diary> diarys = diaryService.getDiaryList(Diary.class,citycode, null, TagConstant.TAG_DRAMA, drama.getId(),0, 3, "sumnumed");
				dramaDiarys.put(drama.getId(),diarys);
				addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diarys));
				dramaWalaCount.put(drama.getId(),commonService.getCommentCount().get(drama.getId()+TagConstant.TAG_DRAMA));
			}
			model.put("dramaDiarys",dramaDiarys);
			model.put("dramaList", dramaList);
			model.put("dramaWalaCountMap",dramaWalaCount);
		}
		//分页信息
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "drama/diaryList.xhtml", true, true);
		Map<String,String> params = new HashMap<String,String>();
		Map<String, Integer> diaryCountMap = commonService.getDiaryCount();
		model.put("diaryCountMap", diaryCountMap);
		params.put("type",type);
		params.put("searchKey", searchKey);
		params.put("timeType",timeType);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("rowsCount", rowsCount);
		model.put("type", type);
		model.put("searchKey", searchKey);
		model.put("timeType",timeType);
		model.put("dramaTypeMap",DramaConstant.dramaTypeMap);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		return "drama/wide_dramaComment.vm";
	}
	@RequestMapping("/drama/ajax/diaryList.xhtml")
	public String ajaxDiaryList(Integer index, Long dramaId, ModelMap model,HttpServletRequest request,HttpServletResponse response){
		if(index == null){
			index = 3;
		}
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Diary> dirayList = diaryService.getDiaryList(Diary.class,citycode, null, TagConstant.TAG_DRAMA, dramaId, index, 5, "sumnumed");
		if(dirayList.isEmpty()) return showJsonError(model, "没有更多数据！");
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(dirayList));
		model.put("dirayList",dirayList);
		model.put("dramaid",dramaId);
		return "include/drama/mod_hotJumuComment.vm";
	}
	// 剧目详细页面(项目)
	@RequestMapping("/drama/dramaDetail.xhtml")
	public String daramDetail(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Long dramaid, String order, HttpServletRequest request, HttpServletResponse response) {
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null)	return show404(model, "剧目不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		if (pageCacheService.isUseCache(request)) {
			PageParams params = new PageParams();
			params.addLong("dramaid", dramaid);
			PageView pageView = pageCacheService.getPageView(request, "drama/dramaDetail.xhtml", params, citycode);
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		if(StringUtils.isBlank(order))
			order = "addtime";
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_DRAMA, drama.getId(), 0, 5, order);
		model.put("diaryList",diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		model.put("order",order);
		List<GewaQuestion> questionList = qaService.getQuestionByCategoryAndCategoryid(citycode, TagConstant.TAG_DRAMA, drama.getId(), true, null, 0, 5);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(questionList));
		Map<Long,List<GewaAnswer>> answersMap = new HashMap<Long,List<GewaAnswer>>();
		for(GewaQuestion gq:questionList){
			List<GewaAnswer> answers = qaService.getAnswerListByQuestionId(0, 3, gq.getId());
			answersMap.put(gq.getId(), answers);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(answers));
		}
		model.put("answersMap", answersMap);
		model.put("questionList",questionList);
		model.put("subs","detail");
		return "drama/wide_dramaDetail.vm";
	}
	
	@RequestMapping("/drama/ajax/ticketDescription.xhtml")
	public String dramaTicketDescription(Long dramaid, HttpServletRequest request, ModelMap model){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if(drama == null) return showJsonError(model, "项目不存在或被删除！");
		if (pageCacheService.isUseCache(request)) {
			PageParams params = new PageParams();
			params.addLong("dramaid", dramaid);
			PageView pageView = pageCacheService.getPageView(request, "drama/ajax/ticketDescription.xhtml", params, drama.getCitycode());
			if (pageView != null) {
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(drama.getOtherinfo());
		String ticketDesc = otherInfoMap.get(Flag.SERVICE_TICKETDESC);
		if(StringUtils.isNotBlank(ticketDesc)){
			try{
				Long diaryid = Long.valueOf(StringUtils.trim(ticketDesc));
				Diary diary = daoService.getObject(Diary.class, diaryid);
				if(diary != null){
					model.put("diaryBody", blogService.getDiaryBody(diaryid));
				}
			}catch (Exception e) {
				dbLogger.warn("", e);
			}
		}
		return "include/drama/mod_dramaDe_ticketDesc.vm";
	}
	private void publicInfo(Drama drama,String citycode,ModelMap model){
		model.putAll(pictureComponent.getHeadData(TagConstant.TAG_DRAMA, drama.getId()));
		// 视频
		List<Video> videoList = videoService.getVideoListByTag(TagConstant.TAG_DRAMA, drama.getId(), 0, 5);
		model.put("videoList", videoList);
		// 话剧照片
		List<Picture> dramaPictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_DRAMA, drama.getId(), 0, 5);
		model.put("dramaPictureList", dramaPictureList);
		List<Integer> allPriceList = dramaPlayItemService.getPriceList(null, drama.getId(), null, null, true);
		model.put("allPriceList", allPriceList);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
		//相关人物
		getActorsList(drama, model);
		getDirectorsList(drama, model);
		Map<Long,Integer> recentDramaCountMap = new HashMap<Long,Integer>();
		List<DramaStar> actorsList = (List<DramaStar>)model.get("actorsList");
		if(!CollectionUtils.isEmpty(actorsList)){
			for(DramaStar ds:actorsList){
				recentDramaCountMap.put(ds.getId(),dramaToStarService.getDramaCountByStarid(ds.getId()));
			}
		}
		List<DramaStar> directorsList = (List<DramaStar>)model.get("directorsList");
		if(!CollectionUtils.isEmpty(directorsList)){
			for(DramaStar ds: directorsList){
				recentDramaCountMap.put(ds.getId(),dramaToStarService.getDramaCountByStarid(ds.getId()));
			}
		}
		model.put("recentDramaCountMap", recentDramaCountMap);
		getDramaCount(drama, citycode, model);
		//优惠活动
		spdiscountList(model, drama);
		// 剧社
		getTroupeCompany(drama, model);
		int count = newsService.getNewsCount(citycode, TagConstant.TAG_DRAMA, "", drama.getId(), null);
		model.put("newsCount",count);
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_DRAMA, drama.getId(), null, 0, 5);
		model.put("newsList", newsList);
		model.put("questionCount",qaService.getQuestionCountByCategoryAndCid(citycode,TagConstant.TAG_DRAMA,drama.getId()));
		//获取正在进行中的官方活动，按照报名结束日期由近到远排序
		List<RemoteActivity> activityList = new ArrayList<RemoteActivity>();
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, null, null, TagConstant.TAG_DRAMA,drama.getId(),"duetime",0,5);
		if(code.isSuccess()){
			activityList = code.getRetval();
			if(activityList==null || activityList.isEmpty()){
				ErrorCode<List<RemoteActivity>> code1 = synchActivityService.getActivityListByOrder(citycode, RemoteActivity.ATYPE_GEWA, RemoteActivity.TIME_CURRENT, TagConstant.TAG_THEATRE, null, null,null,"duetime", 0, 5);
				if(code1.isSuccess()){
					activityList = code1.getRetval();
				}
			}
		}
		model.put("activityList",activityList);
		Theatre curTheatre = null;
		List<Theatre> ts = dramaPlayItemService.getTheatreList(citycode,drama.getId(), false, 1);
		if(ts!=null && ts.size()>0)
			curTheatre = ts.get(0);
		model.put("curTheatre",curTheatre);
		List<Long> dramaList = new ArrayList<Long>();
		putDramaInfo(citycode, dramaList,model);
	}
	//统一获取演出的是否可选座,是否可售票,以及演出的价格
	private void putDramaInfo(String citycode, List<Long> dramaList,ModelMap model){
		Map<Long, List<Theatre>> theatreMap = new HashMap<Long, List<Theatre>>();
		Map<Long, List<Integer>> dramaPriceMap = new HashMap<Long, List<Integer>>();
		for(Long dramaId : dramaList){
			theatreMap.put(dramaId, dramaPlayItemService.getTheatreList(citycode, dramaId, false, 2));//因为涉及到两个缓存,还需要讨论是否要从数据库中直接取,故暂时不改
			dramaPriceMap.put(dramaId, dramaPlayItemService.getPriceList(null, dramaId, DateUtil.getCurFullTimestamp(), null, false));
		}
		List<Long> openseatList = openDramaService.getCurDramaidList(citycode, OdiConstant.OPEN_TYPE_SEAT);
		List<Long> bookingList = openDramaService.getCurDramaidList(citycode);
		model.put("openseatList",openseatList);
		model.put("bookingList", bookingList);
		List<Drama> bookList = daoService.getObjectList(Drama.class, bookingList);
		Collections.sort(bookList, new MultiPropertyComparator(new String[]{"boughtcount"}, new boolean[]{false}));
		List<Drama> interestDramaList = BeanUtil.getSubList(bookList, 0, 5);
		model.put("interestDramaList",interestDramaList);
		model.put("theatreMap", theatreMap);
		model.put("dramaPriceMap",dramaPriceMap);
	}
	//剧目详情页中的视频剧照标签页
	@RequestMapping("/drama/dramaVideo.xhtml")
	public String dramaVideoPic(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Long dramaid, HttpServletRequest request, HttpServletResponse response){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null)
			return show404(model, "剧目不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		List<Video> dramaVideoList = videoService.getVideoListByTag(TagConstant.TAG_DRAMA, drama.getId(), 0, -1);
		model.put("dramaVideoList", dramaVideoList);
		// 话剧照片
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_DRAMA, drama.getId(), 0, -1);
		model.put("pictureList", pictureList);
		model.put("allVideoCount",videoService.getVideoCountByTag(TagConstant.TAG_DRAMA, dramaid));
		model.put("pictureCount",pictureService.getPictureCountByRelatedid(TagConstant.TAG_DRAMA,dramaid));
		model.put("subs","video");
		return "drama/wide_dramaDetail.vm";
	}
	//剧目详情页中的剧照标签页 剧照大图
	@RequestMapping("/drama/dramaPicDetail.xhtml")
	public String dramaVideoPicDetail(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Long dramaid,Long pid, HttpServletRequest request, HttpServletResponse response){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null)
			return show404(model, "剧目不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		List<Picture> dramaPictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_DRAMA, drama.getId(), 0, -1);
		if(pid!=null){
			Picture first = null;
			for(Picture pic:dramaPictureList){
				if(pic.getId().equals(pid)){
					first = pic;
					break;
				}
			}
			if(first!=null){
				dramaPictureList.remove(first);
				dramaPictureList.add(0,first);
			}
		}
		model.put("dramaPictureList", dramaPictureList);
		model.put("subs","pictureDetail");
		return "drama/wide_dramaDetail.vm";
	}
	//剧目详情页中的剧照标签页 剧照大图
	@RequestMapping("/drama/dramaVideoDetail.xhtml")
	public String dramaVideoDetail(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Long dramaid,Long vid, HttpServletRequest request, HttpServletResponse response){
		Drama drama = null;
		if (dramaid !=null){
			drama = daoService.getObject(Drama.class, dramaid);
			if(drama == null){
				return show404(model, "剧目不存在或已经删除！");
			}
		}else{
			Video video = daoService.getObject(Video.class, vid);
			if(video == null){
				return show404(model, "视频不存在或被删除！");
			}
			drama = daoService.getObject(Drama.class, video.getRelatedid());
			if(drama == null){
				return show404(model, "剧目不存在或已经删除！");
			}
		}
			
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		List<Video> videoList = videoService.getVideoListByTag(TagConstant.TAG_DRAMA, drama.getId(), 0, -1);
		if(vid!=null){
			Video first = null;
			for(Video vi:videoList){
				if(vi.getId().equals(vid)){
					first = vi;
					break;
				}
			}
			if(first!=null){
				videoList.remove(first);
				videoList.add(0,first);
			}
		}
		model.put("videoList", videoList);
		model.put("subs","videoDetail");
		return "drama/wide_dramaDetail.vm";
	}
	//剧目详情页中,剧评标签页
	@RequestMapping("/drama/dramaDiaryList.xhtml")
	public String dramaDiary(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Integer pageNo, Long dramaid,String order, HttpServletRequest request, HttpServletResponse response){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null){
			return show404(model, "剧目不存在或已经删除！");
		}
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firestPre = pageNo * rowsPerPage;
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		Integer rowsCount = diaryService.getDiaryCount(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_DRAMA, drama.getId());
		if(StringUtils.isBlank(order)){
			order = "addtime";
		}
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, TagConstant.TAG_DRAMA, drama.getId(), firestPre, rowsPerPage, order);
		model.put("diaryList",diaryList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "drama/" + drama.getId() + "/diarylist", true, true);
		Map params = new HashMap();
		params.put("order", order);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		publicInfo(drama,citycode,model);
		model.put("order",order);
		model.put("subs","diary");
		return "drama/wide_dramaDetail.vm";
	}
	//剧目详情资讯标签页
	@RequestMapping("/drama/dramaNewsList.xhtml")
	public String dramaNews(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Long dramaid,HttpServletRequest request, HttpServletResponse response){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null)
			return show404(model, "剧目不存在或已经删除！");
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		model.put("newsList", newsService.getNewsList(citycode, TagConstant.TAG_DRAMA, drama.getId(),"",-1,-1));
		model.put("subs","news");
		return "drama/wide_dramaDetail.vm";
	}
	//剧目详情哇友提问
	@RequestMapping("/drama/dramaQuestion.xhtml")
	public String dramaQuestion(@CookieValue(value=LOGIN_COOKIE_NAME,required=false) String sessid, 
			ModelMap model, Integer pageNo, Long dramaid,HttpServletRequest request, HttpServletResponse response){
		Drama drama = daoService.getObject(Drama.class, dramaid);
		if (drama == null){
			return show404(model, "剧目不存在或已经删除！");
		}
		cacheDataService.getAndSetIdsFromCachePool(Drama.class, dramaid);
		cacheDataService.getAndSetClazzKeyCount(Drama.class, dramaid);
		String citycode = drama.getCitycode();
		WebUtils.setCitycode(request, citycode, response);
		if(pageNo == null){
			pageNo = 0;
		}
		int rowsPerPage = 20;
		int firestPre = pageNo * rowsPerPage;
		Member logonMember = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		int rowsCount = qaService.getQuestionCountByCategoryAndCid(citycode, TagConstant.TAG_DRAMA, drama.getId());
		List<GewaQuestion> questionList = qaService.getQuestionByCategoryAndCategoryid(citycode, TagConstant.TAG_DRAMA, drama.getId(), firestPre, rowsPerPage);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(questionList));
		Map<Long,List<GewaAnswer>> answersMap = new HashMap<Long,List<GewaAnswer>>();
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, pageNo, "drama/dramaQuestion.xhtml", true, true);
		Map params = new HashMap();
		params.put("dramaid", dramaid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		Long answerMemberid = qaService.getGewaraAnswerByMemberid();
		for(GewaQuestion gq:questionList){
			List<GewaAnswer> answers = new ArrayList<GewaAnswer>();
			List<GewaAnswer> gac = qaService.getAnswerListByQuestionId(0 , 3 , gq.getId());
			//先查找管理员的回复
			List<GewaAnswer> ga = qaService.getAnswerListByQuestionAndMemId(0, 1 , gq.getId(), answerMemberid);
			if(!ga.isEmpty()){
				GewaAnswer answer = ga.get(0);
				gac.remove(answer);
				answers.add(answer);
			}
			answers.addAll(gac);
			answers = BeanUtil.getSubList(answers, 0, 3);
			answersMap.put(gq.getId(), answers);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(answers));
		}
		model.put("answersMap", answersMap);
		model.put("questionList",questionList);
		model.put("subs","question");
		return "drama/wide_dramaDetail.vm";
	}
	// 哇啦
	@RequestMapping("/drama/commentList.xhtml")
	public String dramaWala_new(@RequestParam("dramaid") Long dramaid, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		Drama drama = daoService.getObject(Drama.class, dramaid);
		String citycode = WebUtils.getAndSetDefault(request, response);
		if (drama == null)
			return show404(model, "话剧不存在或已经删除！");
		model.put("drama", drama);
		publicInfo(drama,citycode,model);
		model.put("subs","wala");
		return "drama/wide_dramaDetail.vm";
	}
	
}
