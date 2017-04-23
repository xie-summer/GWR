package com.gewara.web.action.community;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.json.MemberStats;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuCard;
import com.gewara.model.bbs.commu.CommuManage;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.bbs.commu.CommuTopic;
import com.gewara.model.bbs.commu.VisitCommuRecord;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.County;
import com.gewara.model.content.Notice;
import com.gewara.model.content.Picture;
import com.gewara.model.user.Album;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.Treasure;
import com.gewara.model.user.UserMessage;
import com.gewara.model.user.UserMessageAction;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.content.NoticeService;
import com.gewara.service.member.FriendService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ReadOnlyTemplate;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.JsonUtils;
import com.gewara.util.PictureUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.util.XSSFilter;
import com.gewara.web.action.BaseHomeController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class AllCommuController extends BaseHomeController{
	@Autowired@Qualifier("readOnlyTemplate")
	private ReadOnlyTemplate readOnlyTemplate;
	public void setReadOnlyHibernateTemplate(ReadOnlyTemplate readOnlyTemplate) {
		this.readOnlyTemplate = readOnlyTemplate;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("noticeService")
	private NoticeService noticeService;
	public void setNoticeService(NoticeService noticeService) {
		this.noticeService = noticeService;
	}
	
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}
	@Autowired@Qualifier("albumService")
	private AlbumService albumService;
	public void setAlbumService(AlbumService albumService) {
		this.albumService = albumService;
	}
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	/***
	 *  辅助数据
	 */
	public String commonData(ModelMap model, Long commuid, Member member){
		return commonData(model, commuid, true, member);
	}
	public String commonData(ModelMap model, Long commuid, boolean isshowMember, Member member){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return show404(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "你访问的圈子，已从系统中删除！无法访问！");
		boolean b = commuService.isCommuMember(commuid, member.getId());
		if(!b) return showError(model, "你无权做此操作！");
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showError(model, "你不是管理员，无权作此操作!");
		model.put("commu", commu);
		if(isshowMember){
			model.putAll(controllerService.getCommonData(model, member, member.getId()));
		}
		// 检查当前圈子的状态
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commuid);
		model.put("checkstatus", checkstatus);
		model.put("logonMember", member);
		return null;
	}
	
	/**
	 * 当前用户所加入的所有圈子的话题信息
	 * @param model
	 * @param pageNo
	 * @return
	 */
	@RequestMapping("/home/commu/allCommuDiaryList.xhtml")
	public String allCommuInfo(ModelMap model,Integer pageNo){
		Member member = getLogonMember();
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int count=0;
		List<Diary> listCommuDiary =commuService.getAllCommuDiaryById(member.getId(), pageNo*rowsPerPage, rowsPerPage);
		Map<Long/*commuid*/, Commu> mapCommuDiary = daoService.getObjectMap(Commu.class, BeanUtil.getBeanPropertyList(listCommuDiary, Long.class, "communityid", true));
		count=commuService.getAllCommuDiaryCountById(member.getId());
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/allCommuDiaryList.xhtml", true, true);
		pageUtil.initPageInfo();
		model.put("pageUtil",pageUtil);
		model.put("listCommuDiary", listCommuDiary);
		model.put("mapCommuDiary", mapCommuDiary);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		return "home/community/allCommuDiaryList.vm";
	}
	
	
	@RequestMapping("/home/commu/allCommuAlbumList.xhtml")
	public String allCommuAlbumList(ModelMap model,Integer pageNo){
		Member member = getLogonMember();
		if(member == null) return showError(model, "您还没登录，请返回登录！");
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		model.put("memberInfo",memberInfo);
		if(pageNo==null) pageNo=0;
		int rowsPerPage=12;
		int start = pageNo * rowsPerPage;
		int count=0;
		List<Album> albumList=commuService.getJoinedCommuAlbumList(member.getId(), start, rowsPerPage);
		Map<Long,Integer> imageNum = new HashMap<Long, Integer>();
		for(Album album:albumList){
			Integer num = albumService.getPictureountByAlbumId(album.getId());
			imageNum.put(album.getId(), num);
		}
		Map<Long/*commuid*/,Commu> commuList = daoService.getObjectMap(Commu.class, BeanUtil.getBeanPropertyList(albumList, Long.class, "commuid", true));
		count=commuService.getJoinedCommuAlbumCount(member.getId());
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/allCommuAlbumList.xhtml", true, true);
		pageUtil.initPageInfo();
		model.put("albumCommuList",commuList);
		model.put("imageNum",imageNum);
		model.put("pageUtil",pageUtil);
		model.put("albumList", albumList);
		return "home/community/allCommuAlbumList.vm";
	}
	
	/**
	 * 加入的圈子信息
	 * @param model
	 * @param pageNo
	 * @return
	 */
	@RequestMapping("/home/commu/commuList.xhtml")
	public String joinCommu(ModelMap model,Integer pageNo, Long memberid){
		Member mymember = getLogonMember();
		if(memberid==null){//自己
			memberid = mymember.getId();
		}
		//else mymember = daoService.getObject(Member.class, memberid);
		//if(mymember == null) return showError(model, "该用户不存在！");
		
		//判断访问权限(访问其他人)
		if(memberid!=null && !memberid.equals(mymember.getId())){
			model.putAll(friendService.isPrivate(memberid));
		}
		model.putAll(controllerService.getCommonData(model, mymember, memberid));
		//Member member = daoService.getObject(Member.class, memberid);
		if(pageNo==null) pageNo=0;
		int rowsPerPage=10;
		int count=0;
		List<Commu> listCommu=new ArrayList<Commu>();
		listCommu=commuService.getCommuListByMemberId(memberid, pageNo*rowsPerPage, rowsPerPage);
		count=commuService.getCommuCountByMemberId(memberid);
		if(listCommu.size()==0){//没加入、创建圈子，这是为推荐圈子
			listCommu = commuService.getCommunityListByHotvalue(Commu.HOTVALUE_RECOMMEND, pageNo*rowsPerPage, rowsPerPage);
			count = commuService.getCommunityCountByHotvalue(Commu.HOTVALUE_RECOMMEND);
		}
		Map<Long, Integer> diaryCountMap = new HashMap<Long, Integer>();
		Map<Long, Integer> activityCountMap = new HashMap<Long, Integer>();
		Map<Long, Integer> albumCountMap = new HashMap<Long, Integer>();
		Map mapCommuCount=new HashMap();
		for(Commu commu:listCommu){
			//TODO: 使用聚合数据
			diaryCountMap.put(commu.getId(), commuService.getCommuDiaryCount(Diary.class, commu.getId(), null, null));
			albumCountMap.put(commu.getId(), commuService.getCommuAlbumCountById(commu.getId()));
			mapCommuCount.put(commu.getId(), commuService.getCommumemberCount(commu.getId(), null));
		}
		model.put("diaryCountMap", diaryCountMap);
		model.put("activityCountMap", activityCountMap);
		model.put("albumCountMap", albumCountMap);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/commuList.xhtml", true, true);
		Map params = new HashMap(); 
		params.put("memberid", new String[]{memberid+""});
		pageUtil.initPageInfo(params);
		model.put("mapCommuCount", mapCommuCount);
		model.put("pageUtil", pageUtil);
		model.put("relateMap", relateActivityCommu(listCommu));
		model.put("listCommu", listCommu);
		return "home/community/commuList.vm";
	}
	@RequestMapping("/home/commu/friendCommuList.xhtml")
	public String friendCommuList(ModelMap model,Integer pageNo){
		if(pageNo == null) pageNo = 0;
		int maxnum = 10;
		Integer from = pageNo*maxnum;
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		
		Map<Long, Integer> diaryCountMap = new HashMap<Long, Integer>();
		Map<Long, Integer> activityCountMap = new HashMap<Long, Integer>();
		Map<Long, Integer> albumCountMap = new HashMap<Long, Integer>();
		Map<Long, Map> memberMap = new HashMap<Long, Map>();
		Map mapCommuCount=new HashMap();
		Map <Long, Commu> friendCommuMap = commuService.getFriendCommuMap(member.getId(), from, maxnum);
		List<Commu> commuList = new ArrayList<Commu>(friendCommuMap.values());
		
		Integer count = commuService.getFriendCommuCount(member.getId());

		PageUtil pageUtil = new PageUtil(count, maxnum, pageNo,"home/commu/friendCommuList.xhtml", true, true); 
		for(Long friendid: friendCommuMap.keySet()){
			Commu commu = friendCommuMap.get(friendid);
			diaryCountMap.put(commu.getId(), commuService.getCommuDiaryCount(Diary.class, commu.getId(), null, null));
			albumCountMap.put(commu.getId(), commuService.getPictureCountByCommuid(commu.getId()));
			mapCommuCount.put(commu.getId(), commuService.getCommumemberCount(commu.getId(), null));
			memberMap.put(commu.getId(), memberService.getCacheMemberInfoMap(friendid));
		}
		model.put("pageUtil", pageUtil);
		model.put("memberMap", memberMap);
		model.put("mapCommuCount", mapCommuCount);
		model.put("diaryCountMap", diaryCountMap);
		model.put("activityCountMap", activityCountMap);
		model.put("albumCountMap", albumCountMap);
		model.put("commuList", commuList);
		model.put("logonMember", member);
		return "home/community/friendCommuList.vm";
	}
	/**
	 *  检测活动关联圈子
	 * */
	private Map<Long, String> relateActivityCommu(List<Commu> listCommu){
		Map<Long, String> map = new HashMap<Long, String>();
		if(listCommu != null && listCommu.size() > 0){
			for(Commu commu : listCommu){
				Long cid = commu.getId();
				map.put(cid, this.commuService.getCheckStatusByIDAndMemID(cid));
			}
		}
		return map;
	}
	
	@RequestMapping("/home/commu/searchCommu.xhtml")
	public String searchCommu(ModelMap model, String tag, Long relatedid, String sort, 
			Integer pageNo, String keyword, String countycode, 
			HttpServletRequest request, HttpServletResponse response) {
		Member member=getLogonMember();
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPages=pageNo*rowsPerPage;
		int count = 0;// 数据总条数
		// 圈子信息
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		List<Commu> listCommu = new ArrayList<Commu>();
		List<Map> tagList = commuService.getCommuType();
		model.put("tagList", tagList);
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		Map<String, Integer> countyCountMap=new HashMap<String, Integer>();
		for(County county: countyList){
			Integer countyCount = commuService.getCommuCountByCountycode(county.getCountycode());
			countyCountMap.put(county.getCountycode(), countyCount);
		}
		model.put("countyList", countyList);
		model.put("countyCountMap", countyCountMap);
		if("all".equals(countycode)) countycode="";
		listCommu=commuService.getCommuBySearch(tag, citycode, relatedid, keyword, sort, countycode, firstPages,rowsPerPage);
		count=commuService.getCommuCountBySearch(tag, citycode, relatedid, keyword, sort, countycode);
		//查询是否已经是圈子成员
		Map<Long,Boolean> mapIsCommuMember=new HashMap<Long, Boolean>();
		if(listCommu!=null){
			for (Commu c : listCommu) {
				mapIsCommuMember.put(c.getId(), commuService.isCommuMember(c.getId(), member.getId()));
			}
		}
		Map params = new HashMap();// 存储分页参数
		params.put("tag",tag);
		params.put("relatedid",relatedid);
		params.put("keyword",keyword);
		params.put("sort",sort);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo,
				"/home/commu/searchCommu.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("params", params);
		model.put("mapIsCommuMember", mapIsCommuMember);
		model.put("tagList", tagList);
		model.put("listCommu", listCommu);
		model.put("count", count);
		return "home/community/searchCommu.vm";
	}
	
	/**
	 * 添加圈子
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/home/commu/saveCommu.xhtml")
	public String addCommu(HttpServletRequest request, Long commuid, String tag, Long relatedid, String category, Long categoryid,
			String captchaId, String captcha,ModelMap model,  HttpServletResponse response) {
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Member member = getLogonMember();
		if(member == null) return showJsonError_NOT_LOGIN(model);
		if (blogService.isBlackMember(member.getId())) return showJsonError(model, "黑名单中！");
		if(!member.isBindMobile()) return showJsonError(model, "请先绑定手机！");
		String opkey = OperationService.TAG_ADDCONTENT + member.getId();
		boolean allow = operationService.isAllowOperation(opkey, 60);
		if(!allow) return showJsonError(model, "你操作的太频繁了, 歇会再发吧！");
		Integer eggs = blogService.isBadEgg(member);
		if (eggs != 777) {
			return showJsonError(model, eggs+"");
		}
		Map map = request.getParameterMap();
		Commu c =null;
		ChangeEntry changeEntry = null;
		if(commuid!=null){
			c=daoService.getObject(Commu.class, commuid);
			if(c==null)
				return showError(model, "您请求的圈子不存在或已被删除！");
			if(!c.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
			changeEntry = new ChangeEntry(c);
		}else
			c = new Commu("");
		c.setAdminid(member.getId());
		BindUtils.bindData(c, map);
		c.setIp(WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		c.setCitycode(WebUtils.getAndSetDefault(request, response));
		c.setTag(tag);
		c.setRelatedid(relatedid);
		c.setSmallcategory(category);
		c.setSmallcategoryid(categoryid);
		if(c.getName().length()>30) return showJsonError(model,"圈子名称格式长度不能大于30个字!");
		if(WebUtils.checkPropertyAll(c)) return showJsonError(model, "含有非法字符！");
		boolean b = commuService.isExistCommuName(commuid, c.getName());
		if(b) return showJsonError(model, "已存在此圈子名称，不能重复添加！");
		c = XSSFilter.filterObjAttrs(c, "name","info");
		String commubaseinfo = c.getName() + c.getInfo();
		String key = blogService.filterContentKey(commubaseinfo);
		boolean isNight = true;//blogService.isNight();
		if(StringUtils.isNotBlank(key)){
			c.setStatus(Status.N_DELETE);
			String etitle = "有人创建恶意圈子！" + key;
			String content = "有人创建恶意圈子，包含过滤关键字memberId = " + member.getId()+",[用户IP:" + WebUtils.getRemoteIp(request) + "]" + c.getName() + "\n" + c.getInfo();
			monitorService.saveSysWarn(Commu.class, c.getId(), etitle, content, RoleTag.bbs);
		}else if(isNight){
			c.setStatus(Status.N_NIGHT);
		}
		
		// 检查非正常显示字符
		boolean existsInvalidSymbol = VmUtils.isExistsInvalidSymbol(commubaseinfo);
		if(existsInvalidSymbol){
			c.setStatus(Status.N_DELETE);
			String etitle = "有人创建恶意圈子！" + "检测到非正常字符！";
			String content = "有人创建恶意圈子，包含过滤关键字memberId = " + member.getId()+",[用户IP:" + WebUtils.getRemoteIp(request) + "]" + c.getName() + "\n" + c.getInfo();
			monitorService.saveSysWarn(Commu.class, c.getId(), etitle, content, RoleTag.bbs);
		}
		try {
			daoService.saveObject(c);
			if(commuid==null){
				memberService.addExpForMember(member.getId(), ExpGrade.EXP_COMMU_ADD);
				commuService.joinCommuMember(member.getId(),c.getId());
				CommuCard  cc = new CommuCard(member.getId());
				cc.setCommuid(commuid);
				daoService.addObject(cc);
			}
		} catch (Exception e) {
			dbLogger.error("", e);
			return showJsonError(model, "添加圈子失败！");
		}
		// 保存log
		if (changeEntry != null) {
			monitorService.saveChangeLog(member.getId(), Commu.class, c.getId(), changeEntry.getChangeMap(c));
		}
		searchService.pushSearchKey(c);//更新索引至索引服务器
		operationService.updateOperation(opkey, 40);
		/*if (isNight) {
			return showJsonError(model, "夜间创建的圈子需要通过管理员审核后才能显示！");
		}*/
		return showJsonSuccess(model);
	}

	/**
	 * 添加圈子跳转
	 */
	@RequestMapping("/home/commu/addCommu.xhtml")
	public String redirectAddCommu(String tag, Long relatedid, Long commuid, ModelMap model){
		Commu commu=null;
		if(commuid!=null){
			commu=daoService.getObject(Commu.class, commuid);
			if(commu==null)
				return show404(model, "您请求的圈子不存在或已被删除！");
			if(!commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		}else
			commu = new Commu("");
		Member member = getLogonMember();
		if(ServiceHelper.isTag(tag)){
			commu.setTag(tag);
			commu.setRelatedid(relatedid);
		}else if(ServiceHelper.isCategory(tag)){
			commu.setTag(ServiceHelper.getTag(tag));
			commu.setSmallcategory(tag);
			commu.setSmallcategoryid(relatedid);
		}
		if(commu.getRelatedid() != null){
			Object relate = relateService.getRelatedObject(commu.getTag(), commu.getRelatedid());
			model.put("relate", relate);
			String countycode=(String) BeanUtil.get(relate, "countycode");
			if(relate instanceof BaseInfo && StringUtils.isNotBlank(countycode)){
				model.put("indexareaList", placeService.getIndexareaByCountyCode(countycode));
				model.put("countycode", countycode);
				String indexareacode = (String) BeanUtil.get(relate, "indexareacode");
				model.put("indexareacode", indexareacode);
				List placeList = placeService.getPlaceListByTag(tag, countycode, indexareacode);
				model.put("placeList", placeList);
			}
		}
		if (commu.getSmallcategoryid()!= null) {
			Object relate2 = relateService.getRelatedObject(commu.getSmallcategory(),commu.getSmallcategoryid());
			model.put("relate2", relate2);
		}
		model.put("commu", commu);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("config", config);
		return "home/community/addCommunity.vm";
	}
	
	@RequestMapping("/home/commu/applyAddCommu.xhtml")
	public String addCommu(Long commuid, HttpServletRequest request, ModelMap model) {
		Member member=this.getLogonMember();
		if(member == null) return showError_NOT_LOGIN(model);
		//查询圈子信息
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null || !commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		//查询是否已经是圈子成员
		boolean isMember = commuService.isCommuMember(commuid, member.getId());
		if(isMember) return goBack(model, "你已经是圈子成员，请不要重复添加！");
		
		if("public".equals(commu.getPublicflag())){//圈子无权限,直接加入圈子
			CommuMember commuMember = new CommuMember(member.getId());
			commuMember.setCommuid(commuid);
			commuMember.setFlag(CommuMember.FLAG_NORMAL);
			daoService.saveObject(commuMember);
			commu.addCommumembercount();
			daoService.updateObject(commu);
			
			Treasure treasure = new Treasure(member.getId(), TagConstant.TAG_COMMU, commuid, "collect");
			walaApiService.addTreasure(treasure);
			//添加哇啦
			//给哇啦数+1
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
			//给哇啦数+1
			String memberlinkstr= "<a href=\"" + config.getBasePath() + "home/sns/othersPersonIndex.xhtml?memberid=" + member.getId() + "\" target=\"_blank\">"+member.getNickname()+"</a> ";
			String link = config.getBasePath() + "quan/" + commu.getId();
			String linkStr = memberlinkstr+" 加入了 <a href=\""+link+"\" target=\"_blank\">"+commu.getName()+"</a> 圈子";
			Map otherinfoMap = new HashMap();
			String info = "";
			if(StringUtils.length(commu.getInfo())>38){
				info = VmUtils.htmlabbr(commu.getInfo(), 38);
			}else{
				info = commu.getInfo();
			}
			otherinfoMap.put("info", info);
			otherinfoMap.put("commumembercount", commu.getCommumembercount());
			Integer CommuDiaryCount = commuService.getCommuDiaryCount(Diary.class, commuid,null,null);
			otherinfoMap.put("commuDiaryCount", CommuDiaryCount);
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_COMMU_MEMBER, commu.getId(), linkStr, commu.getLogo(), null, null, false, null, otherinfo,null,null,WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request), null);
			if(ec.isSuccess()){
				shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
			}
		}else {
			model.put("commuid", commuid);
			return showRedirect("/home/commu/applyAddCommuInfo.xhtml", model);
		}
		model.put("commuid", commuid);
		return showRedirect("/quan/commuDetail.xhtml", model);
	}
	
	@RequestMapping("/home/commu/manage.xhtml")
	public String manage(Long commuid, ModelMap model){
		Member member=getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return show404(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())) return showError(model, "你没有这个权限!");
		model.put("isAdmin", member.getId().equals(commu.getAdminid()));
		model.put("commu", commu);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		// 检查当前圈子的状态
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commuid);
		model.put("checkstatus", checkstatus);
		return "home/community/manage/index.vm";
	}
	
	/**
	 * 上传圈子Logo图片
	 */
	@RequestMapping("/home/commu/uploadCommuLogo.xhtml")
	public String uploadHeadLogo(String paramchk, String successFile, ModelMap model) throws Exception{
		String mycheck = StringUtil.md5(successFile + config.getString("uploadKey"));
		if(!mycheck.equals(paramchk)) return forwardMessage(model, "校验错误");
		Map jsonMap = new HashMap();
		jsonMap.put("filename", successFile);
		jsonMap.put("success", true);
		model.put("jsonMap", jsonMap);
		return "common/showUploadResult.vm";
	}
	@RequestMapping("/home/commu/updateCommuLogo.xhtml")
	public String updateCommuLogo(double imgW, double imgH, 
			double imgleft, double imgtop, String filename, Long commuid, ModelMap model) throws Exception{
		Commu commu =daoService.getObject(Commu.class, commuid);
		if(!commu.hasStatus(Status.Y)) return show404(model, "该圈子已经被删除！");
		Member member=getLogonMember();
		String realHeadpic = PictureUtil.getAlbumPicpath() + filename;
		String fromPath = gewaPicService.getTempFilePath(filename);
		gewaPicService.saveToLocal(new File(fromPath), "/image/temp/" + filename);
		String tmpPath = gewaPicService.getTempFilePath("wh_"+filename);
		PictureUtil.resize(fromPath, tmpPath, (int)imgW, (int)imgH); //改变大小
		PictureUtil.crop(tmpPath, fromPath, 90, 90, (int)imgleft, (int)imgtop); //剪切
		gewaPicService.moveRemoteTempTo(member.getId(), "commu", commu.getId(), PictureUtil.getAlbumPicpath(), filename);
		commu.setLogo(realHeadpic);
		daoService.saveObject(commu);
		File f = new File(fromPath);
		if(f.exists()){
			f.delete();
		}
		f = new File(tmpPath);
		if(f.exists()){
			f.delete();
		}
		model.put("commuid", commuid);
		return showRedirect("/home/commu/manage.xhtml", model);
	}
	
	
	//圈子公告管理
	@RequestMapping("/home/commu/managernotice.xhtml")
	public String managerNotice(ModelMap model,Long commuid,Integer pageNo){
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		
		if(pageNo == null) pageNo = 0;
		Integer rowsPerPage = 10;
		Integer from = pageNo*rowsPerPage;
		
		List<Notice> noticeList = noticeService.getNoticeListByCommuid(commuid, Notice.TAG_COMMU, from,rowsPerPage);
		Integer noticeCount = noticeService.getNoticeCountByCount(commuid, Notice.TAG_COMMU);
		PageUtil pageUtil = new PageUtil(noticeCount,rowsPerPage,pageNo,"home/commu/managernotice.xhtml", true, true);
		Map params = new HashMap();
		params.put("commuid",commuid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("noticeList",noticeList);
		return "home/community/manage/commuNotice.vm";
	}
	//删除圈子公告信息
	@RequestMapping("/home/commu/delecommunotice.xhtml")
	public String deleCommuNotice(ModelMap model,Long id,Long commuid){
		Member member=getLogonMember();
		String validata = getAjaxData(model, commuid, member);
		if(validata != null) return validata;
		try{
			daoService.removeObjectById(Notice.class, id);
			return showJsonSuccess(model,"删除成功！");
		}catch(Exception e){
			return showJsonError(model, "无数据，删除失败！");
		}
	}
	
	// 公告预加载
	@RequestMapping("/home/commu/preLoadNotice.xhtml")
	public String preLoadNotice(ModelMap model,Long commuid, Long id){
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		
		Notice notice = daoService.getObject(Notice.class, id);
		if(notice == null){
			return showJsonError_NOT_FOUND(model);
		}
		return showJsonSuccess(model, BeanUtil.getBeanMap(notice, false));
	}
	
	//添加圈子公告信息
	@RequestMapping("/home/commu/addcommunotice.xhtml")
	public String addCommuNotice(ModelMap model,String body,Long commuid, Long id){
		Member member=getLogonMember();
		if(member == null) return showJsonError_NOT_LOGIN(model);
		String ajaxdata = getAjaxData(model, commuid, member);
		if(ajaxdata != null) return ajaxdata;
		if(StringUtils.isBlank(body)) return showError(model, "公告内容不能为空！");
		if(StringUtil.getByteLength(body)>20000) return showJsonError(model, "公告内容字符过长！");
		if(WebUtils.checkString(body)) return showJsonError(model, "公告内容含有非法字符！");
		Notice notice = null;
		try{
			if(id == null){
				notice = new Notice(member.getId()); 
			}else{
				notice = daoService.getObject(Notice.class, id);
				if(notice == null){
					return showJsonError_NOT_FOUND(model);
				}
			}
			notice.setBody(XSSFilter.filterAttr(body));
			notice.setRelatedid(commuid);
			notice.setTag(Notice.TAG_COMMU);
			daoService.saveObject(notice);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model,"添加失败!请检查你输入的数据是否正确!");
		}
	}
	
	/**
	 *	设置通信录
	 */
	@RequestMapping("/home/commu/messageLog.xhtml")
	public String messageLog(ModelMap model,Long commuid) {
		Member member=getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return showError(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "你访问的圈子，已从系统中删除！无法访问！");
		boolean b = commuService.isCommuMember(commuid, member.getId());
		if(!b) return goBack(model,"你不是此圈子的成员，你无权做此操作！");
		List<CommuCard> commuCardList = readOnlyTemplate.findByCriteria(getCommuCard(commuid,member.getId()));
		if(!commuCardList.isEmpty()) model.put("commuCard",commuCardList.get(0));
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		model.put("commu", commu);
		// 检查当前圈子的状态
		String checkstatus = commuService.getCheckStatusByIDAndMemID(commuid);
		model.put("checkstatus", checkstatus);
		return "home/community/manage/messageLog.vm";
	}
	
	private DetachedCriteria getCommuCard(Long commuid,Long memberid){
		DetachedCriteria query = DetachedCriteria.forClass(CommuCard.class);
		query.add(Restrictions.eq("commuid",commuid));
		query.add(Restrictions.eq("memberid", memberid));
		return query;
	}
	
	//添加圈子名片
	@RequestMapping("/home/commu/addmessagelog.xhtml")
	public String addMessageLog(ModelMap model,HttpServletRequest request,Long commuid){
		Member member = this.getLogonMember();
		if(member == null) return showJsonError_NOT_LOGIN(model);
		boolean isCommuMember =  commuService.isCommuMember(commuid, member.getId());
		if(!isCommuMember) return showJsonError(model, "你不是此圈子的成员，不能做此操作！");
		DetachedCriteria query = DetachedCriteria.forClass(CommuCard.class);
		query.add(Restrictions.eq("commuid",commuid));
		query.add(Restrictions.eq("memberid",member.getId()));
		List commuCardList = readOnlyTemplate.findByCriteria(query);
		CommuCard cc = null;
		if(commuCardList.isEmpty()){
			cc = new CommuCard(member.getId());
		}else{
			cc = (CommuCard) commuCardList.get(0);
		}
		Map map = request.getParameterMap();
		BindUtils.bindData(cc, map);
		if(StringUtils.isNotBlank(cc.getPhone())){
			boolean b = ValidateUtil.isPhone(cc.getPhone());
			if(!b) return showJsonError(model, "你输入的手机号码有误！");
		}
		if(WebUtils.checkPropertyAll(cc)) return showJsonError(model, "含有非法字符！");
		try{
			cc = XSSFilter.filterObjAttrs(cc, "realname","company","position","remark");
			daoService.saveObject(cc);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "保存失败！");
		}
	}
	
	
	/**
	 *	话题管理 
	 */
	@RequestMapping("/home/commu/commuDiaryManage.xhtml")
	public String commuDiaryManage(ModelMap model, Long commuid, Integer pageNo) {
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		
		// 圈子主题
		List<CommuTopic> commuTopicList = commonService.getCommuTopicList(commuid, -1, -1);
		model.put("commuTopicList", commuTopicList);
		// 全部帖子
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int start = pageNo * rowsPerPage;
		List<Diary> diarylist = commuService.getCommuDiaryListById(Diary.class, commuid, null, null, start, rowsPerPage);
		int count = commuService.getCommuDiaryCount(Diary.class, commuid, null, null);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/commuDiaryManage.xhtml", true, true);
		Map params = new HashMap(); 
		params.put("commuid", commuid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("diarylist", diarylist);
		return "home/community/manage/commuDiaryManage.vm";
	}
	/**
	 *  Ajax 根据圈子主题 分别获得数据(包含查询)
	 *  Long id, String type, Long commuTopicId, String fromDate, Integer flag, String text, int from, int maxnum
	 */
	@RequestMapping("/home/commu/commuDiaryManageTopic.xhtml")
	public String commuDiaryManageTopic(ModelMap model, Long commuid, Long topicid, Integer pageNo, Date fromDate, Integer flag, String text) {
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int start = pageNo * rowsPerPage;
		
		topicid = (topicid == 0 ? null : topicid);
		flag = (flag == 0 ? null : flag);
		List<Diary>	diarylist = commuService.getCommuDiaryListBySearch(Diary.class, commuid, null, topicid, fromDate, flag, text, start, rowsPerPage);
		int count = commuService.getCommuDiaryCountBySearch(Diary.class, commuid, null, topicid, fromDate, flag, text);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/commuDiaryManageTopic.xhtml", true, true);
		Map params = new HashMap(); 
		params.put("commuid", commuid);
		params.put("topicid", topicid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil",pageUtil);
		model.put("diarylist", diarylist);
		return "home/community/manage/diarytable.vm";
	}
	/**
	 *  帖子转移 点击前的加载.
	 */
	@RequestMapping("/home/commu/loadCommuTopic.xhtml")
	public String loadCommuTopic(Long commuid, Long did, ModelMap model){
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		
		DiaryBase diary = diaryService.getDiaryBase(did);
		if(diary == null){
				return showError(model, "数据错误!");
		}
		
		List<CommuTopic> commuTopicList = commonService.getCommuTopicList(commuid, -1, -1);
		Long primaryKey = diary.getModeratorid();
		if(primaryKey == null){
			model.put("commuTopicList", commuTopicList);
		}else{
			CommuTopic commuTopic = daoService.getObject(CommuTopic.class, primaryKey);
			commuTopicList.remove(commuTopic);
			model.put("commuTopicList", commuTopicList);
		}
		return "home/community/manage/selcommutopic.vm";
	}
	/**
	 *  帖子转移
	 */
	@RequestMapping("/home/commu/diary2otherTopic.xhtml")
	public String diary2otherTopic(ModelMap model, Long commuid, Long did, Long topicid){
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		
		if(did == null){return showJsonError(model, "数据错误!");}
		DiaryBase diary = diaryService.getDiaryBase(did);
		diary.setModeratorid(topicid);
		daoService.saveObject(diary);
		return showJsonSuccess(model);
	}
	/**
	 *  帖子置顶
	 */
	
	/**
	 *  帖子删除
	 */
	@RequestMapping("/home/commu/diary2del.xhtml")
	public String diary2del(ModelMap model, Long commuid, Long did){
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		
		if(did == null){return showJsonError(model, "数据错误!");}
		DiaryBase diary =  diaryService.getDiaryBase(did);
		diary.setStatus(Status.N_DELETE);
		daoService.saveObject(diary);
		searchService.pushSearchKey(diary);//更新索引至索引服务器
		return showJsonSuccess(model);
	}
	
	
	/**
	 *	删除圈子
	 */
	@RequestMapping("/home/commu/deleteCommu.xhtml")
	public String deleteCommu(ModelMap model, Long commuid) {
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		return "home/community/manage/deleteCommu.vm";
	}
	/**
	 *	转让圈子
	 */
	@RequestMapping("/home/commu/assignCommuShow.xhtml")
	public String assignCommuShow(ModelMap model, Long commuid) {
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		return "home/community/manage/extendCommuManage.vm";
	}
	
	/**
	 *	相册管理
	 */
	@RequestMapping("/home/commu/photoManage.xhtml")
	public String photoManage(ModelMap model, Long commuid,Integer pageNo) {
		Member member = getLogonMember();
		String validdata = commonData(model, commuid, member); 
		if(validdata != null)return validdata;
		
		if(pageNo==null) pageNo=0;
		int rowsPerPage=12;
		int start = pageNo * rowsPerPage;
		int count=0;
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		List<Album> albumList=commuService.getCommuAlbumById(commuid, start, rowsPerPage);
		Map<Long,Integer> imageNum = new HashMap<Long, Integer>();
		for(Album album:albumList){
			Integer num = commuService.getPictureCountByCommuid(album.getId());
			imageNum.put(album.getId(), num);
		}
		count=commuService.getCommuAlbumCountById(commuid);
		PageUtil pageUtil=new PageUtil(count,rowsPerPage,pageNo,"/home/commu/photoManage.xhtml", true, true);
		Map params = new HashMap(); 
		params.put("commuid", commuid);
		pageUtil.initPageInfo(params);
		model.put("imageNum",imageNum);
		model.put("pageUtil",pageUtil);
		model.put("albumList", albumList);
		model.put("manageCommu",true);
		return "home/community/manage/photoManage.vm";
	}
	/**
	 *	群发管理 
	 */
	@RequestMapping("/home/commu/commuMessage.xhtml")
	public String commuMessage(ModelMap model, Long commuid, Integer pageNo) {
		Member member=getLogonMember();
		String validdata = commonData(model, commuid, member); 
		if(validdata != null)return validdata;
		Commu commu = (Commu)model.get("commu");
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int start = pageNo * rowsPerPage;
		Integer commuMemberCount = commuService.getCommumemberCount(commu.getId() ,null);
		Map<Long, VisitCommuRecord> visitCommuRecordMap = new HashMap<Long, VisitCommuRecord>();
		List<CommuMember> commuMemberList = commuService.getCommuMemberById(commu.getId(), null, null, "", start, rowsPerPage);
		PageUtil pageUtil=new PageUtil(commuMemberCount, rowsPerPage, pageNo, "home/commu/commuMessage.xhtml", true, true);
		for(CommuMember commuMember:commuMemberList){
			VisitCommuRecord visitCommuRecord=commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, commuMember.getMemberid());
			visitCommuRecordMap.put(commuMember.getId(), visitCommuRecord);
		}
		List<Long> memberIdList = ServiceHelper.getMemberIdListFromBeanList(commuMemberList);
		addCacheMember(model, memberIdList);
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		Map params = new HashMap(); 
		params.put("commuid", commu.getId());
		pageUtil.initPageInfo(params);
		model.put("commuMemberList", commuMemberList);
		model.put("pageUtil", pageUtil);
		model.put("visitCommuRecordMap", visitCommuRecordMap);
		model.put("commu",commu);
		return "home/community/manage/commuMessage.vm";
	}
	//发送信息
	@RequestMapping("/home/commu/sendSysMessage.xhtml")
	public String sendSysMessage(ModelMap model, Long commuid, HttpServletRequest request, String captchaId, String captcha){
		boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, WebUtils.getRemoteIp(request));
		if(!isValidCaptcha) return showJsonError(model, "验证码错误！");
		Map<String, String[]> memberMap = request.getParameterMap();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null)return showJsonError(model, "此圈子不存在！");
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
		Member member = getLogonMember();
		if(blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showError(model, "你没有此操作权限!");
		String checkStr = request.getParameter("systitle") + request.getParameter("syscontent");
		if(WebUtils.checkString(checkStr)) return showJsonError(model, "含有非法字符！");
		if(StringUtils.isNotBlank(blogService.filterContentKey(checkStr))) return showJsonError(model, "包含过滤关键字");
		for(String mmap : memberMap.keySet()){
			if(mmap.startsWith("memberid")){
				CommuCard commuCard=commuService.getCommuCardByCommuidAndMemberid(new Long(memberMap.get(mmap)[0]), commuid);
				if(commuCard == null || Status.Y.equals(commuCard.getMessageset())){//是否接受群发信息通知
					UserMessage userMessage = new UserMessage(""); 
					userMessage.setContent(XSSFilter.filterAttr(request.getParameter("syscontent")));
					String message= XSSFilter.filterAttr(request.getParameter("systitle"))+"来自"+commu.getName()+"的圈子通知";
					userMessage.setSubject(message);
					daoService.saveObject(userMessage);
					
					UserMessageAction uma = new UserMessageAction(new Long(memberMap.get(mmap)[0]));
					BindUtils.bindData(uma, request.getParameterMap());
					uma.setFrommemberid(member.getId());
					uma.setTomemberid(new Long(memberMap.get(mmap)[0]));
					uma.setUsermessageid(userMessage.getId());
					if(uma.getGroupid()==null) {//新发表的情况
						uma.setGroupid(userMessage.getId());
					}
					daoService.saveObject(uma);
				}
			}
		}
		return showJsonSuccess(model);
	}
	/**
	 *	修改圈子属性 
	 */
	@RequestMapping("/home/commu/updateCommu.xhtml")
	public String updateCommu(ModelMap model, Long commuid) {
		Member member = getLogonMember();
		String validdata = commonData(model, commuid, member); 
		if(validdata != null)return validdata;
		Commu commu = (Commu)model.get("commu");
		if(member!=null) {
			model.put("isAdmin", member.getId().equals(commu.getAdminid()));
		}
		// 取得兴趣标签
		String[] interesttags = StringUtils.split(commu.getInteresttag(), "\\|");
		if(interesttags != null && StringUtils.isNotBlank(interesttags[0])){
			List<String> taglist = Arrays.asList(interesttags);
			model.put("taglist", taglist);
		}
		
		// 圈子成员数
		model.put("memberCount", commuService.getCommumemberCount(commuid, null));
		// 圈子话题数
		model.put("diaryCount", commuService.getCommuDiaryCount(Diary.class, commuid, null, null));
		// 圈子相册数
		model.put("albumCount", commuService.getCommuAlbumCountById(commuid));
		// 圈子投票数
		
		if(commu.getRelatedid() != null){
			Object relate = relateService.getRelatedObject(commu.getTag(), commu.getRelatedid());
			if(relate!=null) {
				String countycode=(String) BeanUtil.get(relate, "countycode");
				if(relate instanceof BaseInfo && StringUtils.isNotBlank(countycode)){
					model.put("indexareaList", placeService.getIndexareaByCountyCode(countycode));
					model.put("countycode", countycode);
					String indexareacode = (String) BeanUtil.get(relate, "indexareacode");
					model.put("indexareacode", indexareacode);
					List placeList = placeService.getPlaceListByTag(commu.getTag(), countycode, indexareacode);
					model.put("placeList", placeList);
				}
				model.put("relate", relate);
			}
		}
		
		if(commu.getSmallcategoryid()!=null){
			Object relate2 = relateService.getRelatedObject(commu.getSmallcategory(), commu.getSmallcategoryid());
			model.put("relate2", relate2);
		}
		if(StringUtils.isNotBlank(commu.getCountycode())){
			County county = daoService.getObject(County.class, commu.getCountycode());
			model.put("county", county);
		}
		setCommuRelated(commu);
		return "home/community/manage/updateCommu.vm";
	}
	private void setCommuRelated(Commu commu){
		if(commu == null) return;
		Object relate = null;
		if (StringUtils.isNotBlank(commu.getTag()) && commu.getRelatedid() != null) {
			relate = relateService.getRelatedObject(commu.getTag(), commu.getRelatedid());
			commu.setRelate(relate);
		}
		if (StringUtils.isNotBlank(commu.getSmallcategory()) && commu.getSmallcategoryid() != null) {
			relate = relateService.getRelatedObject(commu.getSmallcategory(), commu.getSmallcategoryid());
			commu.setRelate2(relate);
		}
	}
	// 增加圈子兴趣标签
	@RequestMapping("/home/commu/addCommuInsterestTag.xhtml")
	public String addCommuInsterestTag(ModelMap model, Long commuid, String tag){
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		Commu commu = (Commu)model.get("commu");
		String tags = commu.getInteresttag() == null ? "" : commu.getInteresttag();
		String[] interesttags = StringUtils.split(tags, "\\|");
		if(interesttags != null && interesttags.length > 0){
			List<String> taglist = Arrays.asList(interesttags);
			tag = StringUtils.trim(tag);
			if(taglist.contains(tag)){
				return showError(model, "不能重复添加标签！");
			}
		}
		tags += tag + "|";
		commu.setInteresttag(tags);
		daoService.saveObject(commu);
		getInterestTags(model, commu);
		return "home/community/manage/commuinsteresttag.vm";
	}
	private Map getInterestTags(ModelMap model, Commu commu){
		List<String> taglist = Arrays.asList(StringUtils.split(commu.getInteresttag(), "\\|"));
		model.put("taglist", taglist);
		return model;
	}
	// 删除圈子兴趣标签
	@RequestMapping("/home/commu/delCommuInsterestTag.xhtml")
	public String delCommuInsterestTag(ModelMap model, Long commuid, String tag){
		String validdata = commonData(model, commuid, false, getLogonMember()); 
		if(validdata != null)return validdata;
		Commu commu = (Commu)model.get("commu");
		String[] interesttags = StringUtils.split(commu.getInteresttag(), "\\|");
		if(ArrayUtils.contains(interesttags, tag)){
			interesttags = (String[])ArrayUtils.removeElement(interesttags, tag);
		}
		StringBuilder sb = new StringBuilder();
		for(String str : interesttags){
			sb.append(str);
			sb.append("|");
		}
		commu.setInteresttag(sb.toString());
		daoService.saveObject(commu);
		return showJsonSuccess(model);
	}
	
	/**
	 * 话题版列表
	 */
	@RequestMapping("/home/commu/commuTopicsList.xhtml")
	public String getCommuTopicList(ModelMap model,Long commuid,Integer pageNo){
		if(pageNo == null) pageNo = 0;
		String validdata = commonData(model, commuid, getLogonMember()); 
		if(validdata != null)return validdata;
		Integer rowsPerPage = 20;
		Integer from = pageNo * rowsPerPage;
		List<CommuTopic> commuTopicList = commonService.getCommuTopicList(commuid, from, rowsPerPage);
		Integer count = commonService.getCommuTopicCount(commuid);
		Map params = new HashMap();
		params.put("commuid",commuid);
		PageUtil pageUtil = new PageUtil(count ,rowsPerPage,pageNo,"home/commu/commuTopicsList.xhtml", true, true);
		pageUtil.initPageInfo(params);
		model.put("pageUitl", pageUtil);
		model.put("commuTopicList",commuTopicList);
		model.put("commu",daoService.getObject(Commu.class, commuid));
		return "home/community/manage/diaryModelManage.vm";
	}
	
	/**
	 * 添加话题板块
	 */
	@RequestMapping("/home/commu/addCommuTopic.xhtml")
	public String addCommuTopic(ModelMap model,Long commuid,String topicname){
		Member member=getLogonMember();
		if(WebUtils.checkString(topicname)) return showJsonError(model, "话题含有非法字符！");
		String ajaxdata = getAjaxData(model, commuid, member);
		if(ajaxdata != null) return ajaxdata;
		CommuTopic ct = new CommuTopic(commuid);
		ct.setTopicname(topicname);
		try{
			daoService.saveObject(ct);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "新话题板块添加失败！");
		}
	}
	/**
	 * 删除话题板块
	 */
	@RequestMapping("/home/commu/delCommuTopic.xhtml")
	public String delCommuTopic(ModelMap model,Long id,Long commuid){
		Member member=getLogonMember();
		String ajaxdata = getAjaxData(model, commuid, member);
		if(ajaxdata != null) return ajaxdata;
		try{
			daoService.removeObjectById(CommuTopic.class, id);
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model,"删除失败！");
		}
	}
	/**
	 * 修改话题板块信息
	 */
	@RequestMapping("/home/commu/updateCommuTopic.xhtml")
	public String updateCommuTopic(ModelMap model,HttpServletRequest request,Long commuid,Integer commuTopicCount){
		Member member=getLogonMember();
		String ajaxdata = getAjaxData(model, commuid, member);
		if(ajaxdata != null) return ajaxdata;
		Map map = request.getParameterMap();
		if(commuTopicCount>0){
			for (int i = 0; i<commuTopicCount; i++) {
				String[] topicname = (String[]) map.get("topicname"+i);
				String[] ordernum = (String[]) map.get("ordernum"+i);
				String[] displaynum = (String[]) map.get("displaynum"+i);
				if(StringUtils.isBlank(topicname[0])) return showJsonError(model, "圈子话题板块名称不能为空！");
				if(topicname[0].length()>10) return showJsonError(model,"圈子话题板块名称不能超过10个字！");
				if(WebUtils.checkString(topicname[0])) return showJsonError(model, "话题含有非法字符！");
				if(StringUtils.isBlank(ordernum[0])) return showJsonError(model, "显示顺序不能为空！");
				if(!ValidateUtil.isNumber(ordernum[0])) return showJsonError(model, "显示顺序值只能输入数字！");
				if(StringUtils.isBlank(displaynum[0])) return showJsonError(model, "显示话题数不能为空！");
				if(!ValidateUtil.isNumber(displaynum[0])) return showJsonError(model, "显示话题数值只能输入数字！");
				if(Integer.valueOf(displaynum[0]+"")>30||Integer.valueOf(displaynum[0]+"")<0){
					return showJsonError(model, "显示话题数值只能输入0-30之间的数字！");
				}
			}
			for (int i = 0; i<commuTopicCount; i++) {
				String[] id = (String[]) map.get("id"+i);
				String[] topicname = (String[]) map.get("topicname"+i);
				String[] ordernum = (String[]) map.get("ordernum"+i);
				String[] displaynum = (String[]) map.get("displaynum"+i);
				CommuTopic ct = daoService.getObject(CommuTopic.class, Long.valueOf(id[0]));
				ct.setCommuid(commuid);
				ct.setTopicname(topicname[0]);
				ct.setOrdernum(Integer.valueOf(ordernum[0]));
				ct.setDisplaynum(Integer.valueOf(displaynum[0]));
				daoService.updateObject(ct);
			}
		}else{
			return showError(model, "暂无圈子话题分类！");
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/commu/logoutCommu.xhtml")
	public String logoutCommu(ModelMap model,Long commuid){
		Member member = getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return show404(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "你访问的圈子，已从系统中删除！无法访问！");
		if(member.getId().equals(commu.getAdminid())) return showJsonError(model, "圈子创建者不能退出圈子！");
		boolean isCommuMember = commuService.isCommuMember(commuid, member.getId());
		if(!isCommuMember) return showJsonError(model,"只有圈子成员才有此操作权限");
		DetachedCriteria query = DetachedCriteria.forClass(CommuMember.class);
		query.add(Restrictions.eq("commuid", commuid));
		query.add(Restrictions.eq("memberid", member.getId()));
		List commuMemberlist = readOnlyTemplate.findByCriteria(query, 0, 1);
		try{
			if(!commuMemberlist.isEmpty()){
				CommuMember cm = (CommuMember) commuMemberlist.get(0);
				daoService.removeObject(cm);
				//发送系统消息
				String str = "用户"+member.getNickname()+"退出圈子"+"“"+commu.getName()+"“";
				SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
				sysmessage.setFrommemberid(member.getId());
				sysmessage.setBody(str);
				sysmessage.setTomemberid(commu.getAdminid());
				daoService.saveObject(sysmessage);
			}
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model,"退出圈子失败！");
		}
	}
	
	@RequestMapping("/home/commu/delCommu.xhtml")
	public String delCommu(ModelMap model, Long commuid){
		Member member = getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return showJsonError(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())) {
			return showJsonError(model, "只有创建者才有权限操作！");
		}
		try{
			commu.setStatus(Status.N_DELETE);
			daoService.updateObject(commu);
			searchService.pushSearchKey(commu);//更新索引至索引服务器
			return showJsonSuccess(model);
		}catch(Exception e){
			return showJsonError(model, "删除失败！");
		}
	}
	
	private String getAjaxData(ModelMap model,Long commuid,Member member){
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return show404(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "你访问的圈子，已从系统中删除！无法访问！");
		model.put("commu",commu);
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showJsonError(model, "只有管理员才有权限操作！");
		return null;
	}

	//设置圈子黑名单、批准加入圈子
	@RequestMapping("/home/commu/commuUnapproveAndBlackMember.xhtml")
	public String saveCommuBlackMember(ModelMap model, HttpServletRequest request, Long commuid, String ctype){
		Member member = getLogonMember();
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu==null) return showJsonError(model,"圈子不存在！");
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showError(model, "只有管理员才有权限操作!");
		Map<String ,String[]> memberMap=request.getParameterMap();
		String str = "";
		if(ctype.equals("approvecommu")){//批准加入
			if(member.getId().equals(commu.getAdminid())|| member.getId().equals(commu.getSubadminid())){
				for(String mmap : memberMap.keySet()){
					if(mmap.startsWith("blackmemberid")){
						SysMessageAction sys = userMessageService.getSysMessageAction(commuid, new Long(memberMap.get(mmap)[0]), SysAction.ACTION_APPLY_COMMU_JOIN, true);
						CommuMember commuMember = new CommuMember(sys.getFrommemberid());
						commuMember.setCommuid(sys.getActionid());
						daoService.saveObject(commuMember);
						//此条系统消息状态改变
						sys.setStatus(SysAction.STATUS_AGREE);
						daoService.saveObject(sys);
						
						//加入系统消息
						SysMessageAction newsma = new SysMessageAction(SysAction.STATUS_RESULT);
						newsma.setFrommemberid(member.getId());
						newsma.setTomemberid(sys.getFrommemberid());
						newsma.setActionid(sys.getId());
						str = "恭喜你通过审核成功加入圈子 " + commu.getName();
						newsma.setBody(str);
						daoService.saveObject(newsma);
					}
				}
			}
		}else if(ctype.equals("commonmember")){//通过审核
			for(String mmap : memberMap.keySet()){
				if(mmap.startsWith("blackmemberid")){
					CommuMember tmpcommuMember = commuService.getCommuMemberByMemberidAndCommuid(new Long(memberMap.get(mmap)[0]), commuid);
					CommuMember commuMember = daoService.getObject(CommuMember.class, tmpcommuMember.getId());
					commuMember.setFlag(CommuMember.FLAG_NORMAL);
					daoService.saveObject(commuMember);
					//发送系统消息
					String title = "恭喜你通过审核成功加入圈子" + commu.getName();
					this.sendSysMessage(member.getId(), title, Long.valueOf(memberMap.get(mmap)[0]));
				}
			}
		}else if(ctype.equals("delete")){//删除黑名单成员
			for(String mmap : memberMap.keySet()){
				if(mmap.startsWith("blackmemberid")){
					if(commu.getAdminid().equals(new Long(memberMap.get(mmap)[0]))) return showJsonError(model, "不能删除管理员！");
					if((member.getId()).equals(commu.getAdminid()) || member.getId().equals(commu.getSubadminid())){//判断当前用户是否是这个圈子的管理员
						friendService.deleteCommueMember(new Long(memberMap.get(mmap)[0]), commuid);
						//发送系统消息
						String title = "管理员"+member.getNickname()+"把你从圈子"+commu.getName()+"中踢出了";
						this.sendSysMessage(member.getId(), title, Long.valueOf(memberMap.get(mmap)[0]));
						//删除访问记录
						VisitCommuRecord visitCommuRecord = commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, new Long(memberMap.get(mmap)[0]));
						daoService.removeObject(visitCommuRecord);
					}
				}
			}
		}
		return showJsonSuccess(model);
	}
	//设置成员身份
	@RequestMapping("/home/commu/commuMember.xhtml")
	public String commuMember(ModelMap model, HttpServletRequest request, Long commuid, String commutype){
		Member member = getLogonMember();
		Commu commu=daoService.getObject(Commu.class, commuid);
		if(commu==null)return showJsonError(model, "圈子不存在！");
		if(!commu.hasStatus(Status.Y)) return showJsonError(model, "该圈子已经被删除！");
		if(!member.getId().equals(commu.getAdminid())&&!member.getId().equals(commu.getSubadminid())) return showError(model, "只有管理员才有权限操作!");
		Map<String ,String[]> memberMap = request.getParameterMap();
		if(StringUtils.isBlank(commutype))commutype="common";
		if(commutype.equals("common")){//降为普通成员
			for(String mmap : memberMap.keySet()){
				if(mmap.startsWith("memberid")){
					CommuMember commuMember=commuService.getCommuMemberByMemberidAndCommuid(new Long(memberMap.get(mmap)[0]), commuid);
					if(commu.getAdminid().equals(new Long(memberMap.get(mmap)[0]))) return showJsonError(model, "不能将创建者降为普通成员!");
					if(commuMember != null && !commu.getSubadminid().equals(Long.valueOf((memberMap.get(mmap)[0])))) return showJsonError(model, "已是普通成员!");
					commu.setSubadminid(0L);
					daoService.saveObject(commu);
					//发送系统消息
					String str = "管理员"+member.getNickname()+"解除了你在圈子"+commu.getName()+"的管理员资格";
					this.sendSysMessage(member.getId(), str, Long.valueOf(memberMap.get(mmap)[0]));
				}
			}
		} else if(commutype.equals("administer")){//升为管理员
			Set memberSet = memberMap.keySet();
			int i=0;
			for (Object object : memberSet) {
				if((object+"").startsWith("memberid")){
					i++;
				}
			}
			Collection<String[]> params = memberMap.values();
			if(i>1) return showJsonError(model, "暂时只支持添加一个管理员!");
			if(commu.getSubadminid()!=0) return showJsonError(model, "暂时只支持一个管理员！");
			for (String[] str : params) {
				if((commu.getAdminid()+"").equals(str[0])) return showJsonError(model, "暂时只支持一个管理员!");
			}
			commu.setSubadminid(new Long(memberMap.get("memberid")[0]));
			daoService.saveObject(commu);
			
			String str = "管理员"+member.getNickname()+"把你提升为"+commu.getName()+"的管理员";
			this.sendSysMessage(member.getId(), str, Long.valueOf(memberMap.get("memberid")[0]));
		} else if(commutype.equals("blackmember")){ //设置圈子的黑名单
			for(String mmap : memberMap.keySet()){
				if(mmap.startsWith("memberid")){
					if(commu.getAdminid().equals(new Long(memberMap.get(mmap)[0])) ||
							commu.getSubadminid().equals(new Long(memberMap.get(mmap)[0]))){//判断是否管理员
						return showJsonError(model, "管理员不能关入小黑屋!");
					}else {
						CommuMember commuMember = commuService.getCommuMemberByMemberidAndCommuid(new Long(memberMap.get(mmap)[0]), commuid);
						commuMember.setFlag(CommuMember.FLAG_BLACK);
						daoService.saveObject(commuMember);
						//发送系统消息
						String str = "管理员"+member.getNickname()+"把你关入了"+commu.getName()+ "的小黑屋反省，在解除小黑屋之前你不能在圈内进行发帖和发布活动等操作！";
						this.sendSysMessage(member.getId(), str, Long.valueOf(memberMap.get(mmap)[0]));
					}
				}
			}
		}else if(commutype.equals("deletemember")){
			for(String mmap : memberMap.keySet()){
				if(mmap.startsWith("memberid")){
					if(commu.getAdminid().equals(new Long(memberMap.get(mmap)[0]))) return showJsonError(model, "不能删除管理员！");
					if((member.getId()).equals(commu.getAdminid()) || member.getId().equals(commu.getSubadminid())){//判断当前用户是否是这个圈子的管理员
						friendService.deleteCommueMember(new Long(memberMap.get(mmap)[0]), commuid);
						//发送系统消息
						String title = "管理员"+member.getNickname()+"把你从圈子"+commu.getName()+"中踢出了";
						this.sendSysMessage(member.getId(), title, Long.valueOf(memberMap.get(mmap)[0]));
						//删除访问记录
						VisitCommuRecord visitCommuRecord = commuService.getVisitCommuRecordByCommuidAndMemberid(commuid, new Long(memberMap.get(mmap)[0]));
						daoService.removeObject(visitCommuRecord);
					}
				}
			}
		}
		return showJsonSuccess(model);
	}
	/**
	 * 圈子相册详细信息
	 */
	@RequestMapping("/home/commu/manageAlbumImageList.xhtml")
	public String albumImageList(ModelMap model, Long albumid,Integer pageNo) {
		Member mymember = getLogonMember();
		Album album = daoService.getObject(Album.class, albumid);
		if(album == null) return showError(model, "错误的相册信息！");
		String validdata = commonData(model, album.getCommuid(), getLogonMember()); 
		if(validdata != null)return validdata;
		if(pageNo==null) pageNo=0;
		int rowsPerPage=20;
		int start = pageNo * rowsPerPage;
		int count = 0;
		List<Picture> albumImageList = albumService.getPictureByAlbumId(albumid, start, rowsPerPage);
		count = albumService.getPictureountByAlbumId(albumid);
		PageUtil pageUtil = new PageUtil(count,rowsPerPage,pageNo,"home/commu/manageAlbumImageList.xhtml", true, true);
		Member albumMember = daoService.getObject(Member.class, album.getMemberid());
		Map params = new HashMap(); 
		params.put("albumid", albumid);
		params.put("commuid", album.getCommuid());
		pageUtil.initPageInfo(params);
		if (album.getMemberid().equals(mymember.getId())) model.put("ismycommu",true);
		model.put("mymember",mymember);
		model.put("pageUtil",pageUtil);
		model.put("albumid", albumid);
		model.put("albumImageList",albumImageList);
		model.put("albumMember",albumMember);
		model.put("album",album);
		model.put("commuid", album.getCommuid());
		model.put("commu",model.get("commu"));
		model.put("isShowCommuAlbum", true);
		model.putAll(controllerService.getCommonData(model, mymember, mymember.getId()));
		return "home/community/manage/manageAlbumImageList.vm";
	}
	private void sendSysMessage(Long adminid, String syscontent, Long memberid){
		//发送系统消息
		SysMessageAction sysmessage=new SysMessageAction(SysAction.STATUS_RESULT);
		sysmessage.setFrommemberid(adminid);
		sysmessage.setBody(syscontent);
		sysmessage.setTomemberid(memberid);
		daoService.saveObject(sysmessage);
	}
	
	// 绑定支付宝信息
	@RequestMapping("/home/commu/commuBindAlipay.xhtml")
	public String commuBindAlipay(ModelMap model, Long commuid){
		Member member = getLogonMember();
		String validdata = commonData(model, commuid, member); 
		if(validdata != null)return validdata;
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null)return show404(model, "数据错误!");
		if(!commu.hasStatus(Status.Y)) return show404(model, "你访问的圈子，已从系统中删除！无法访问！");
		if(member != null ){
			// 登录后
			if(!(member.getId().equals(commu.getAdminid()))){
				// 非管理员
				return showError(model, "您没有操作的权限!");
			}
			model.putAll(controllerService.getCommonData(model, member, member.getId()));
			model.put("commu", commu);
			model.put("commuManage", commuService.getCommuManageByCommuid(commuid));
		}
		return "home/community/manage/commubindalipay.vm";
	}
	// 保存支付宝信息到 commu_manage
	@RequestMapping("/home/commu/saveCommumanage4Alipay.xhtml")
	public String saveCommumanage4Alipay(ModelMap model, String alipay, String alipayname, String contactphone, Long commumanageid){
		if(StringUtils.isBlank(alipay))	return showJsonError(model, "支付宝账户必填");
		if(StringUtils.isBlank(alipayname))	return showJsonError(model, "支付宝账户姓名必填");
		
		CommuManage commuManage = daoService.getObject(CommuManage.class, commumanageid);
		if(commuManage != null){
			commuManage.setAlipay(alipay);
			commuManage.setAlipayname(alipayname);
			commuManage.setContactphone(contactphone);
			daoService.saveObject(commuManage);
			return showJsonSuccess(model);
		}
		return showJsonError_DATAERROR(model);
	}
}