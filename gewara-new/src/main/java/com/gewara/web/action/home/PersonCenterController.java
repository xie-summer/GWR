package com.gewara.web.action.home;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.MemberStats;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.Moderator;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.commu.CommuMember;
import com.gewara.model.content.Advertising;
import com.gewara.model.content.Picture;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Album;
import com.gewara.model.user.AlbumComment;
import com.gewara.model.user.FavoriteTag;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.service.OperationService;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.AlbumService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.content.AdService;
import com.gewara.service.member.FavoriteTagService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.PersonCenterService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.RoleTag;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.activity.RemoteApplyjoin;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;

@Controller
public class PersonCenterController extends BaseHomeController{
	public static final String  PEX_PATRN="^[0-9a-zA-Z\u4e00-\u9fa5]+$";
	
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("personCenterService")
	private PersonCenterService personCenterService;
	@Autowired@Qualifier("albumService")
	private AlbumService albumService;
	
	@Autowired@Qualifier("favoriteTagService")
	private FavoriteTagService favoriteTagService;
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	public void setVelocityTemplate(VelocityTemplate velocityTemplate) {
		this.velocityTemplate = velocityTemplate;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService) {
		this.commuService = commuService;
	}
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("blogService")
	protected BlogService blogService;
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("adService")
	private AdService adService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@RequestMapping("/home/sns/personIndex.xhtml")
	public String personIndex(ModelMap model, String tag, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		
		//左侧导航
		this.getHomeLeftNavigate(memberInfo ,model);
		
		//居住地
		String liveplace = placeService.getLocationPair(member.getId(), " - ");
		model.put("liveplace", liveplace);
		
		//广告位
		List<Advertising> ads = adService.getAdListByPid(citycode, "wala");
		if (ads != null && ads.size() > 0) {
			List<Map> jsonMapList = new ArrayList<Map>();
			for (Advertising advertising : ads) {
				Map jsonMap = new HashMap();
				jsonMap.putAll(BeanUtil.getBeanMapWithKey(advertising, "title", "link", "logicaldir", "adtype"));
				jsonMap.put("adpath", config.getBasePath() + advertising.getAd());
				jsonMap.put("pid", "wala");
				jsonMapList.add(jsonMap);
			}
			model.put("jsonMapList", jsonMapList);
		}
		
		//右边时间
		Integer curYear = DateUtil.getCurrentYear();
		Date today = DateUtil.getCurDate();
		Integer curMonth = DateUtil.getCurrentMonth();
		model.put("curYear", curYear);
		model.put("today", DateUtil.format(today, "yyyy-MM-dd HH:mm:ss"));
		model.put("yesterday", DateUtil.format(DateUtil.addDay(today, -1), "yyyy-MM-dd HH:mm:ss"));
		model.put("curMonth", curMonth);
		model.put("yYear", curYear-1);
		model.put("yyYear", curYear-2);
		model.put("tag", tag);
		return "sns/index.vm";
	}
	
	@RequestMapping("/admin/getAllRecommendMembers.xhtml")
	public String getAllRecommendMembers(ModelMap model){
		Map params = new HashMap();
		List<Map> list = mongoService.find(MongoData.NS_RECOMMEND_MEMBER, params, "ordernum", true);
		model.put("memberList", list);
		return "admin/sns/recommendMemberList.vm";
	}
	
	@RequestMapping("/admin/saveOrUpdateRecommendMembers.xhtml")
	public String saveOrUpdateMembers(ModelMap model, Long memberid, String membername, String reason){
		Map params = new HashMap();
		params.put(MongoData.DEFAULT_ID_NAME, memberid);
		params.put("membername", membername);
		params.put("addtime", DateUtil.getCurDate());
		params.put("status", "y");
		params.put("ordernum", 0);
		params.put("reason", reason);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
		params.put("headpic", memberInfo.getHeadpic());
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_RECOMMEND_MEMBER);
		return showJsonSuccess(model);
	}
	
	/**
	 * 是否显示楼层
	 */
	@RequestMapping("/admin/showFloor.xhtml")
	public String showFloor(ModelMap model,Long id,String showfloor){
		Map oriMap = mongoService.findOne(MongoData.NS_RECOMMEND_MEMBER, "id", id);
		if(oriMap !=null){
			Map destMap = new HashMap(oriMap);
			destMap.put("status", showfloor);
			mongoService.update(MongoData.NS_RECOMMEND_MEMBER, oriMap, destMap);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "显示失败！");
	}
	
	/**
	 * 用户排序
	 */
	@RequestMapping("/admin/setOrder.xhtml")
	public String setOrder(ModelMap model,Long id,Integer ordernum){
		Map oriMap = mongoService.findOne(MongoData.NS_RECOMMEND_MEMBER, "id", id);
		if(oriMap !=null){
			Map destMap = new HashMap(oriMap);
			destMap.put("ordernum", ordernum);
			mongoService.update(MongoData.NS_RECOMMEND_MEMBER, oriMap, destMap);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "设置失败！");
	}
	
	/**
	 * 删除用户信息
	 */
	@RequestMapping("/admin/deleteMember.xhtml")
	public String deleteMember(ModelMap model,Long id){
		mongoService.removeObjectById(MongoData.NS_RECOMMEND_MEMBER, "id", id);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/updateMember.xhtml")
	public String updateMember(ModelMap model,Long memberid, String membername, String reason){
		Map oriMap = mongoService.findOne(MongoData.NS_RECOMMEND_MEMBER, "id", memberid);
		if(oriMap!=null){
			Map destMap = new HashMap(oriMap);
			destMap.put("membername", membername);
			destMap.put("reason", reason);
			mongoService.update(MongoData.NS_RECOMMEND_MEMBER, oriMap, destMap);
			return showJsonSuccess(model);
		}else{
			return showJsonError(model, "你好修改的对象不存在！");
		}
	}
	
	//得到感兴趣的人
	@RequestMapping("/sns/getInterestedPerson.xhtml")
	public String getInterestedPerson(ModelMap model,Long memberid){
		this.myIntersetedPerson(memberid, model);
		return "sns/myleft/interestedPerson.vm";
	}
	
	//得到热门圈子
	@RequestMapping("/sns/gethotCommu.xhtml")
	public String gethotCommu(ModelMap model,Long memberid){
		this.hotCommuList(memberid, model);
		return "sns/myleft/hotCommu.vm";
	}
	
	//得到热门话题
	@RequestMapping("/sns/getHotModerator.xhtml")
	public String getHotModerator(ModelMap model){
		model.put("moderatorList", this.getRandomHotModerator(5));
		return "sns/myleft/hotModerator.vm"; 
	}
	
	//得到推荐活动
	@RequestMapping("/sns/getRecommendActivity.xhtml")
	public String getRecommendActivity(ModelMap model, Long memberid){
		this.getRecommendActivtyList(memberid, model);
		return "sns/myleft/recommendActivity.vm"; 
	}
	
	/**
	 * 加入圈子
	 */
	@RequestMapping("/home/sns/AddCommu.xhtml")
	public String AddCommu(ModelMap model, Long commuid, HttpServletRequest request) {
		Member member = getLogonMember();
		if(member == null) return showError_NOT_LOGIN(model);
		//查询圈子信息
		Commu commu = daoService.getObject(Commu.class, commuid);
		if(commu == null || !commu.hasStatus(Status.Y)) return show404(model, "该相册的圈子已被删了！");
		//查询是否已经是圈子成员
		boolean isMember = commuService.isCommuMember(commuid, member.getId());
		if(isMember) 	return showJsonError(model, "已经加入了！");
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
			String memberlinkstr= "<a href=\"" + config.getBasePath() + "home/sns/othersPersonIndex.xhtml?memberid=" + member.getId()+"\" target=\"_blank\">"+member.getNickname()+"</a> ";
			String link = config.getBasePath() + "quan/" + commu.getId();
			String linkStr = memberlinkstr + " 加入了 <a href=\""+link+"\" target=\"_blank\">"+commu.getName()+"</a> 圈子";
			Map otherinfoMap = new HashMap();
			String info = "";
			if(StringUtils.length(commu.getInfo())>38){
				info = VmUtils.htmlabbr(commu.getInfo(), 38);
			}
			otherinfoMap.put("commumembercount", commu.getCommumembercount());
			Integer CommuDiaryCount = commuService.getCommuDiaryCount(Diary.class, commuid,null,null);
			otherinfoMap.put("commuDiaryCount", CommuDiaryCount);
			otherinfoMap.put("info", info);
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			ErrorCode<Comment> ec = commentService.addMicroComment(member, TagConstant.TAG_COMMU_MEMBER, commu.getId(), linkStr, commu.getLogo(), null, null, false, null, otherinfo, null, null, WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request), null);
			if(ec.isSuccess()){
				shareService.sendShareInfo("wala",ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
			}
		}else {
			return showJsonError(model, "有权限！");
		}
		
		Map params = memberCountService.getMemberInfoStats(member.getId());
		if(params != null){
			String jsonStr = (String)params.get("recommendCommu");
			List<Map> list = JsonUtils.readJsonToObject(List.class, jsonStr);
			for(Map map : list){
				if(map.get("commuid") == null) continue;
				String mid = map.get("commuid").toString();
				if(Long.valueOf(mid).equals(commuid)){
					list.remove(map);
					break;
				}
			}
			jsonStr = JsonUtils.writeObjectToJson(list);
			params.put("recommendCommu", jsonStr);
			
			mongoService.saveOrUpdateMap(params, "myid", MongoData.NS_MEMBER_INFO);
		}
		return showJsonSuccess(model, "添加成功！");
	}
	
	//中心哇啦信息
	@RequestMapping("/home/sns/getPersonCenterCommentInfo.xhtml")
	public String getPersonCenterCommentInfo(ModelMap model, String type, String date, Long memberid, String tag, Integer pageNo){
		Member logonMember =this.getLogonMember();
		model.put("logonMember", logonMember);
		if(pageNo == null) pageNo = 0;
		Integer from = pageNo *10;
		//哇啦数据
		List<Comment> commentList = new ArrayList<Comment>();
		
		Date startDate = null;
		if(DateUtil.isValidDate(date)) startDate = DateUtil.parseDate(date);
		else startDate = DateUtil.getMonthFirstDay(DateUtil.currentTime());
		Date enddate = DateUtil.getNextMonthFirstDay(startDate);
		if(StringUtils.isNotBlank(type)){
			commentList = commentService.getCommentListByMemberIdAndTags(StringUtils.split(tag,","), memberid, startDate, enddate, from, 10);
			model.put("type", type);
		}else{
			commentList = commentService.getCommentList(StringUtils.split(tag,","), memberid, startDate, enddate, from, 10);
		}
		this.getCenterCommentInfos(model, commentList);
		return "sns/personCenterCommentInfo.vm";
	}
	
	
	//用户头像展示
	@RequestMapping("/home/sns/ajax/getUserInfo.xhtml")
	public String getUserInfo(ModelMap model, Long id){
		if(id == null) return showJsonSuccess(model, "错误参数！");
		Member member = daoService.getObject(Member.class,id);
		if(member == null) return showJsonError(model, "不存在此用户！");
		MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
		model.put("memberinfo", info);
		//关注，哇啦，瓦丝数量
		Map dataMap = memberCountService.getMemberCount(member.getId());
		model.put("commentCount", dataMap.get(MemberStats.FIELD_COMMENTCOUNT));
		model.put("treasureCount", dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT));
		model.put("fansCount", dataMap.get(MemberStats.FIELD_FANSCOUNT));

		Member logonMember = this.getLogonMember();
		Boolean b = blogService.isTreasureMember(logonMember.getId(), member.getId());
		model.put("b", b);
		model.put("logonMember", logonMember);
		//来自
		String liveplace = placeService.getLocationPair(member.getId(), " - ");
		model.put("liveplace", liveplace);
		String viewPage = velocityTemplate.parseTemplate("common/userInfo.vm", model);
		Map jsonMap = new HashMap();
		jsonMap.put("viewPage", viewPage);
		return showJsonSuccess(model, jsonMap);
	}
	
	//删除图片回复
	@RequestMapping("/home/sns/ajax/deleteAlbumComment.xhtml")
	public String deleteAlbumComment(Long commentid, ModelMap model){
		AlbumComment albumComment = daoService.getObject(AlbumComment.class, commentid);
		if(albumComment == null) return showJsonError(model, "数据有误！");
		daoService.removeObject(albumComment);
		return showJsonSuccess(model);
	}
	//图片信息
	@RequestMapping("/home/sns/ajax/getPictureCommentInfo.xhtml")
	public String getPictureCommentInfo(Long id, ModelMap model){
		Picture picture = daoService.getObject(Picture.class, id);
		if(picture != null){
			MemberInfo memberInfo = null;
			if(picture.hasMemberType(GewaraUser.USER_TYPE_MEMBER)){
				memberInfo = daoService.getObject(MemberInfo.class, picture.getMemberid());
			}
			//8个评论用户
			List<Long> memberidList = albumService.getMemberIdListByAlbumComment(id, 0, 8);
			model.put("memberList", daoService.getObjectList(MemberInfo.class ,memberidList));
			List<AlbumComment> imageCommentList = albumService.getPictureComment(id, 0,8);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(imageCommentList));
			model.put("memberInfo", memberInfo);
			model.put("imageCommentList", imageCommentList);
			model.put("picture", picture);
		}
		model.put("logonMember", getLogonMember());
		return "sns/picLinks.vm";
	}
	// 查询标签
	@RequestMapping("/home/sns/ajax/ajaxTags.xhtml")
	public String ajaxTags(ModelMap model) {
		List<FavoriteTag> list = favoriteTagService.getRandomFavorList(12);
		model.put("favoritetagList", list);
		// 我的兴趣爱好, 分割后存入List
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String favorTags = memberInfo.getFavortag() == null ? "" : memberInfo.getFavortag();
		if(StringUtils.isNotBlank(favorTags)){
			List<String> myfavTags = Arrays.asList(StringUtils.split(favorTags, "|"));
			model.put("myfavTags", myfavTags);
		}
		model.put("tagtype", "hottag");
		return "sns/ajaxtags.vm";
	}

	// 发表微博 Ajax
	@RequestMapping("/home/sns/addMicroBlogDny.xhtml")
	public String addMicroBlogDny(ModelMap model, String micrbody, String link, String video, String bodypic, String tag, Long relatedid,
			HttpServletRequest request) throws Exception {
		Member member = getLogonMember();
		int egg = blogService.isBadEgg(member);
		if (egg != 777) {
			return showJsonError(model, egg+"");
		}
		model.put("logonMember", member);
		
		if (StringUtils.isBlank(micrbody))
			return showJsonError(model, "哇啦内容不能为空！");
		String body = StringUtils.substringBetween(micrbody, "#", "#");
		if(StringUtils.length(body)>60){
			return showJsonError(model, "话题内容长度不能超出60个字符！");
		}
		if (StringUtils.length(micrbody) > 140)
			return showJsonError(model, "哇啦内容长度不能超出140个字符！");
		if(StringUtils.isBlank(tag)) tag = TagConstant.TAG_TOPIC;
		if (blogService.isBlackMember(member.getId())){
			return showJsonError(model, "你在黑名单中，暂不能发表哇啦，如有疑问请联系格瓦拉客服！");
		}
		micrbody = StringUtil.getHtmlText(micrbody);
		if (StringUtils.isNotBlank(link)) {
			if (link.startsWith("http://"))
				link = "<a rel=\"nofollow\" href=\"" + link + "\" target=\"_blank\" rel=\"nofollow\">" + "链接地址" + "</a>";
			else
				link = "<a href=\"http://" + link + "\" target=\"_blank\" rel=\"nofollow\">" + "链接地址" + "</a>";
		}
		if(StringUtils.isNotBlank(bodypic)){
			micrbody+="<img src=\""+bodypic+"\"/>";
		}
		if(StringUtils.isNotBlank(video)){
			micrbody+="<object classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' width='100%' height='200' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=5,0,0,0'>";
			micrbody+="<param name='quality' value='high' />";
			micrbody+="<param name='movie' value='"+video+"'>";
			micrbody+="<param name='wmode' value='transparent'/>";
			micrbody+="<embed src='"+video+"' wmode='transparent' quality='high' width='100%' height='200' swLiveConnect='true' TYPE='application/x-shockwave-flash' PLUGINSPAGE='http://www.macromedia.com/go/getflashplayer'></embed></object>";
		}
		ErrorCode<Comment> result = commentService.addComment(member, tag, relatedid, micrbody, link, false,null,null, WebUtils.getIpAndPort(WebUtils.getRemoteIp(request), request));
		if(result.isSuccess()){
			//给哇啦数+1
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
			//给哇啦数+1
			shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
			Comment comment = result.getRetval();
			model.put("commentList", Arrays.asList(comment));
			addCacheMember(model, comment.getMemberid());
			Map<Long, List<String>> videosMap = new HashMap<Long, List<String>>();// 存储点评视频地址
			if (StringUtils.isNotBlank(comment.getBody())) {
				List<String> videos = WebUtils.getVideos(comment.getBody());
				videosMap.put(comment.getId(), videos);
			}
			model.put("videosMap", videosMap);
		}else{
			if(result.getRetval()!=null && result.getRetval().getStatus().equals(Status.N_FILTER)){
				return showJsonError(model,result.getMsg());
			}
			return showJsonError(model, result.getMsg());
		}
		model.put("comment", result.getRetval());
		return "sns/loadComment.vm";
	}
	//回复哇啦
	@RequestMapping("/home/sns/replyComment.xhtml")
	public String replyComment(Long commentid, String type, String body, String isMicro, ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if (blogService.isBlackMember(member.getId())) return showJsonError_BLACK_LIST(model);
		String ip = WebUtils.getRemoteIp(request);
		if(StringUtils.equals(type, "f")){
			//转发哇啦
			if(body.length() > 140) return showJsonError(model, "内容太长！");
			Comment comment = commentService.getCommentById(commentid);
			if (comment != null) {
				String topic = "";
				if(StringUtils.isNotBlank(comment.getTopic())){
					topic = "#" + comment.getTopic() + "#";
				}
				if(StringUtils.isNotBlank(topic)){
					body = topic + body;
				}
				ErrorCode<Comment> result = commentService.addMicroComment(member, comment.getTag(), comment.getRelatedid(), body, null, AddressConstant.ADDRESS_WEB, commentid, false, null,null,null, WebUtils.getIpAndPort(ip, request));
				if(result.isSuccess()){
					shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
					return showJsonSuccess(model);
				}
				return showJsonError(model, result.getMsg());
			}
		}
		if(StringUtils.equals(type, "albumcomment")){
			//图片哇啦
			Picture albumImage = daoService.getObject(Picture.class, commentid);
			if(albumImage == null) return showJsonError(model, "图片信息不正确！");
			Album album = daoService.getObject(Album.class, albumImage.getRelatedid());
			if(album == null) return showJsonError(model, "相册信息不正确！");
			if(body.length() > 200) return showJsonError(model, "内容太长！");
			AlbumComment albumComment = new AlbumComment(member.getId());
			albumComment.setAlbumid(album.getId());
			albumComment.setImageid(commentid);
			albumComment.setBody(body);
			daoService.saveObject(albumComment);
			model.put("imageCommentList", Arrays.asList(albumComment));
		    addCacheMember(model, albumComment.getMemberid());
		    model.put("albumcomment", "albumcomment");
		    return "sns/replyComment.vm";
		}
		if(StringUtils.isBlank(body)){
			return showJsonError(model, "回复内容不能为空！");
		}
		if(body.length() > 140) return alertMessage(model, "内容太长！");
		ErrorCode<ReComment> result = saveComment(commentid, null, null, body, isMicro, member, WebUtils.getIpAndPort(ip, request));
		if(!result.isSuccess()){
			return showJsonError(model, result.getMsg());
		}
		List<ReComment> reCommentList = Arrays.asList(result.getRetval());
		model.put("reCommentList", reCommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(reCommentList));
		model.put("logonMember", member);
		model.put("replyOne", "replyOne");
		return "sns/replyComment.vm";
	}
	/**
	 * 删除哇啦回复
	 * @param model
	 * @param requestid
	 * @return
	 */
	@RequestMapping("/home/sns/deleteMicroReComment.xhtml")
	public String deleteMicroReComment(ModelMap model, Long mid) {
		Member member = getLogonMember();
		ReComment reComment = walaApiService.getReCommentById(mid);
		if(reComment == null) return showJsonError(model, "你要删除的记录不存在！");
		reComment.setStatus(Status.N_DELETE);
		if(!member.getId().equals(reComment.getMemberid())) return showJsonError(model, "不能删除别人的数据！");
		try {
			commentService.updateCommentReplyCount(reComment.getRelatedid(), Comment.TYPE_DOWNREPLY);
			walaApiService.updateReComment(reComment);
			return this.showJsonSuccess(model);
		} catch (Exception e) {
			return showJsonError(model, "删除失败！");
		}
	}
	
	private void getWalaRecomments(Long commentid, ModelMap model, String index){
		Comment comment = commentService.getCommentById(commentid);
		List<ReComment> reCommentList = new ArrayList<ReComment>();
		if(StringUtils.isNotBlank(index)){
			reCommentList = walaApiService.getReCommentByRelatedidAndTomemberid(commentid, null, null, 6, Integer.parseInt(index));
		}else{
			reCommentList = walaApiService.getReCommentByRelatedidAndTomemberid(commentid, null, null, 0, 6);
		}
		Integer recommentCount= walaApiService.getReCommentCountByRelatedidAndTomemberid(commentid, null, null);
		model.put("recommentCount", recommentCount);
		model.put("comment", comment);
		model.put("reCommentList", reCommentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(reCommentList));
		Member logonMember = this.getLogonMember();
		if(logonMember != null){
			model.put("logonMember", logonMember);
			addCacheMember(model, logonMember.getId());
		}
	}
	/**
	 * 获取微博回复
	 */
	@RequestMapping("/home/sns/getReCommentList.xhtml")
	public String getWalaReCommentList(Long commentid, ModelMap model, String index) {
		if(commentid == null) return showJsonError(model, "参数不能为空！");
		this.getWalaRecomments(commentid, model, index);
		model.put("index", index);
		return "sns/replyComment.vm";
	}
	private ErrorCode<ReComment> saveComment(Long commentid,Long transferid, Long tomemberid, String body, String isMicro, Member member, String ip) {
		if(StringUtils.isBlank(body)) return ErrorCode.getFailure("回复内容不能为空！");
		if(StringUtil.getByteLength(body)>20000) return ErrorCode.getFailure("内容字符过长！");
		Comment comment = commentService.getCommentById(commentid);
		boolean isWarning = false;
		if(member==null) return ErrorCode.getFailure("请先登录！");
		String opkey = OperationService.TAG_REPLYCONTENT + member.getId();
		if (!operationService.isAllowOperation(opkey, OperationService.HALF_MINUTE, 1)) {
			return ErrorCode.getFailure("发的这么快，手有点酸了吧，休息一下再继续吧！");
		}
		ReComment reComment = new ReComment(member.getId());
		reComment.setAddress(ReComment.ADDRESS_WEB);
		reComment.setRelatedid(comment.getId());
		if(tomemberid==null||"".equals(tomemberid)){
			reComment.setTomemberid(comment.getMemberid());
			reComment.setTag(ReComment.TAG_COMMENT);
		}else{
			reComment.setTomemberid(tomemberid);
			reComment.setTransferid(transferid);
			reComment.setTag(ReComment.TAG_RECOMMENT);
		}
		body = StringUtil.getHtmlText(body);
		reComment.setBody(body);
		String key = blogService.filterContentKey(body);
		if (StringUtils.isNotBlank(key)) {
			reComment.setStatus(Status.N_FILTER);
			isWarning = true;
			monitorService.saveSysWarn("有人发恶意评论！" + key, "有人恶意评论，包含过滤关键字memberId = " + member.getId() + body, RoleTag.bbs);
		}else{
			comment.add2Replycount(Comment.TYPE_ADDREPLY);
			comment.setReplytime(reComment.getAddtime());
			comment.setOrderTime(DateUtil.addHour(comment.getOrderTime(), 36));
			commentService.updateComment(comment);
		}
		Long recommendid =walaApiService.saveReComment(reComment);
		if (StringUtils.isNotBlank(isMicro)) {
			String topic = "";
			if(StringUtils.isNotBlank(comment.getTopic())){
				topic = "#" + comment.getTopic() + "#";
			}
			if(StringUtils.isNotBlank(topic)){
				body = topic + body;
			}
			ErrorCode<Comment> result = commentService.addMicroComment(member, TagConstant.TAG_TOPIC, null, body, null, AddressConstant.ADDRESS_WEB, comment.getId(), false, null,null,null,ip);
			if(result.isSuccess()) shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
			memberCountService.updateMemberCount(member.getId(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
		}/*else{
			shareService.sendShareInfo("reply", reComment.getId(), reComment.getMemberid(), null);
		}*/
		operationService.updateOperation(opkey, OperationService.HALF_MINUTE, 1);
		if (isWarning) return ErrorCode.getFullErrorCode(ApiConstant.CODE_SIGN_ERROR, "你发表的内容包含过滤关键字，需要管理员审核！", reComment);
		reComment.setId(recommendid);
		return ErrorCode.getSuccessReturn(reComment);
	}
	// 随机标签列表
	@RequestMapping("/home/sns/randomintrest.xhtml")
	public String randomintrest(ModelMap model){
		List<FavoriteTag> list = favoriteTagService.getRandomFavorList(12);
		model.put("favoritetagList", list);
		model.put("tagtype", "hottag");
		return "sns/typetags.vm";
	}
	//删除标签
	@RequestMapping("/home/sns/delFavorInfo.xhtml")
	public String delFavorInfo(ModelMap model, String tag, String taytype){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String favorTags = memberInfo.getFavortag() == null ? "" : memberInfo.getFavortag();
		if(StringUtils.isNotBlank(favorTags)){
			List<String> myfavTags = new ArrayList<String>(Arrays.asList(StringUtils.split(favorTags, "|")));
			myfavTags.remove(tag);
			favorTags = StringUtils.join(myfavTags, "|");
			memberInfo.setFavortag(favorTags);
			daoService.saveObject(memberInfo);
			model.put("myfavTags", myfavTags);
		}
		model.put("tagtype", taytype);
		return "sns/typetags.vm";
	}
	//修改标签
	@RequestMapping("/home/sns/updateFavorInfo.xhtml")
	public String updateFavorInfo(ModelMap model, String tag){
		if(StringUtils.isBlank(tag)) return showJsonError(model, "标签不可以为空！");
		if(WebUtils.checkString(tag)) return showJsonError(model, "标签含有非法字符！");
		if(StringUtils.isBlank(StringUtil.findFirstByRegex(tag, PEX_PATRN))) return showJsonError(model, "只支持汉字,数字,字母！");
		if(tag.length()>10){
			return this.showJsonError(model, "标签长度不能大于10个汉字");
		}
		Member member = getLogonMember();
		// 用户自己录入
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		// 过滤 '|', 'null' 等字符
		String favorTags = memberInfo.getFavortag();
		if(StringUtils.isBlank(favorTags)){
			favorTags = "";
		}else{
			if(StringUtils.equals(favorTags, "|")) favorTags = "";
			if(StringUtils.equals(favorTags, "null")) favorTags = "";
		}
		List<String> myfavTags = Arrays.asList(StringUtils.split(favorTags, "|"));
		if(myfavTags.size()>10){
			return this.showJsonError(model, "你的标签添的已经足够多了");
		}
		if(!myfavTags.contains(tag)){
			if(!StringUtils.endsWith(favorTags, "|")){
				favorTags += "|"+tag + "|";
			}else{
				favorTags += tag + "|";
			}
			memberInfo.setFavortag(favorTags);
			daoService.saveObject(memberInfo);
			myfavTags=Arrays.asList(StringUtils.split(favorTags, "|"));
		}
		// 点击公用录入: 首先判断数据库中是否有有tag, 有则查出来 count++, 没有则new一个放入
		FavoriteTag favoriteTag = daoService.getObject(FavoriteTag.class, tag);
		if(favoriteTag == null){
			favoriteTag = new FavoriteTag(tag);
			daoService.saveObject(favoriteTag);
		}else{
			if(!myfavTags.contains(tag)){
				favoriteTagService.updateFavoriteTagCount(tag);
			}
		}
		model.put("myfavTags", myfavTags);
		model.put("tagtype", "mytag");
		return "sns/typetags.vm";
	}
	//去别人的主页
	@RequestMapping("/home/sns/othersPersonIndex.xhtml")
	public String othersPersonIndex(ModelMap model, String tag, String nickname, Long memberid,HttpServletRequest request, HttpServletResponse response){
		Member member = memberService.getMemberByNickname(nickname);
		if(member != null){
			memberid = member.getId();
		}
		member = daoService.getObject(Member.class, memberid);
		if(member == null) return show404(model, "不存在此用户！");
		model.put("member", member);
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		model.put("memberInfo", memberInfo);
		
		//关注，哇啦，瓦丝数量
		Map dataMap = memberCountService.getMemberCount(member.getId());
		model.put("memberStats", dataMap);
		model.put("commentCount", dataMap.get(MemberStats.FIELD_COMMENTCOUNT));
		model.put("treasureCount", dataMap.get(MemberStats.FIELD_ATTENTIONCOUNT));
		model.put("fansCount", dataMap.get(MemberStats.FIELD_FANSCOUNT));
		String favortags = memberInfo.getFavortag();
		if(StringUtils.isNotBlank(favortags)){
			List<String> myfavTags = Arrays.asList(StringUtils.split(favortags, "|"));
			model.put("myfavTags", myfavTags);
		}
		Member logonMember = this.getLogonMember();
		MemberInfo logonMemberInfo = daoService.getObject(MemberInfo.class, logonMember.getId());
		model.put("logonMemberInfo",logonMemberInfo);
		//更新通知消息
		Integer syscount=memberService.getMemberNotReadSysMessageCount(logonMember.getId());
		model.put("syscount", syscount);
		//提示信息
		Map params = new HashMap();
		params.put(MongoData.DEFAULT_ID_NAME, logonMember.getId());
		Map membertPromptInfo = mongoService.findOne(MongoData.NS_PROMPT_INFO, params);
		Integer tipcount=0;
		if(membertPromptInfo != null){
			Map otherinfoMap = JsonUtils.readJsonToMap(""+membertPromptInfo.get("otherinfo"));
			model.put("otherinfoMap", otherinfoMap);
			model.put("membertPromptInfo", membertPromptInfo);
			tipcount=(Integer)membertPromptInfo.get("count");
		}
		model.put("tipcounts", syscount+tipcount);
		//广告位
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<Advertising> ads = adService.getAdListByPid(citycode, "wala");
		if (ads != null && ads.size() > 0) {
			List<Map> jsonMapList = new ArrayList<Map>();
			for (Advertising advertising : ads) {
				Map jsonMap = new HashMap();
				jsonMap.putAll(BeanUtil.getBeanMapWithKey(advertising, "title", "link", "logicaldir", "adtype"));
				jsonMap.put("adpath", config.getBasePath() + advertising.getAd());
				jsonMap.put("pid", "wala");
				jsonMapList.add(jsonMap);
			}
			model.put("jsonMapList", jsonMapList);
		}
		//右边时间
		Integer curYear = DateUtil.getCurrentYear();
		Date today = DateUtil.getCurDate();
		Integer curMonth = DateUtil.getCurrentMonth();
		model.put("curYear", curYear);
		model.put("today", DateUtil.format(today, "yyyy-MM-dd HH:mm:ss"));
		model.put("yesterday", DateUtil.format(DateUtil.addDay(today, -1), "yyyy-MM-dd 	HH:mm:ss"));
		model.put("curMonth", curMonth);
		model.put("yYear", curYear-1);
		model.put("yyYear", curYear-2);
		//居住地
		String liveplace = placeService.getLocationPair(member.getId(), " - ");
		model.put("liveplace", liveplace);
		//是否关注
		model.put("logonMember", logonMember);
		Boolean b = blogService.isTreasureMember(logonMember.getId(), member.getId());
		model.put("b", b);
		//ta关注的人
		List<Treasure> treasureList = blogService.getTreasureListByMemberId(memberid, new String[]{Treasure.TAG_MEMBER},null, null,0,9, Treasure.ACTION_COLLECT);
		List<Long> relatedidList = BeanUtil.getBeanPropertyList(treasureList, Long.class, "relatedid", false);
		List<MemberInfo> treasureMemberList = daoService.getObjectList(MemberInfo.class, relatedidList);
		model.put("treasureMemberList", treasureMemberList);
		//瓦丝
		List<Long> microFansIdList = blogService.getFanidListByMemberId(member.getId(), 0, 9);
		List<MemberInfo> memberInfoList = daoService.getObjectList(MemberInfo.class, microFansIdList);
		model.put("memberInfoList", memberInfoList);
		//他感兴趣的
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		List<Treasure> treasuresList = blogService.getTreasureListByMemberId(member.getId(), null,new String[]{Treasure.TAG_MEMBER}, null, 0, 6, Treasure.ACTION_COLLECT);
		model.put("microTreasureList", treasuresList);
		controllerService.initRelate("microTreasureList", rh, treasuresList);
		//ta的话题
		List<Moderator> moderatorList = moderatorService.getModeratorList(member.getId(), Moderator.TYPE_MEMBER, 0, 5);
		Integer mymoderatorCount = moderatorService.getModeratorCount(Moderator.TYPE_MEMBER, member.getId());
		model.put("microModeratorList", moderatorList);
		model.put("mymoderatorCount", mymoderatorCount);
		//关注他的人同时关注了
		List<Long> fansList = blogService.getTreasureListByMemberIdList(memberid, Treasure.TAG_MEMBER,0,9, Treasure.ACTION_COLLECT);
		List<MemberInfo> fansTreasureMemberList = daoService.getObjectList(MemberInfo.class,fansList);
		model.put("fansTreasureMemberList", fansTreasureMemberList);
		//他的圈子
		List<Commu> commuList=commuService.getCommuListByMemberId(memberid, 0, 3);
		model.put("commuList", commuList);
		model.put("type","other");
		model.put("tag", tag);
		return "sns/index.vm";
	}
	
	
	//得到哇啦流数据
	private void getCenterCommentInfos(ModelMap model,List<Comment> commentList){
		List<Long> memberIdList = new ArrayList<Long>();
		Map<Long,List<RemoteApplyjoin>> activityJoinMemberMap = new HashMap<Long,List<RemoteApplyjoin>>();
		Map<Long,List<GoodsOrder>> activityJoinOrderMemberMap = new HashMap<Long,List<GoodsOrder>>();
		Map<Long,List<ReComment>> recommentListMap = new HashMap<Long,List<ReComment>>();
		Map<Long,Integer> recommentCountMap = new HashMap<Long,Integer>();
		Map<Long,Comment> transferCommentMap = new HashMap<Long,Comment>();
		Map<Long, List<Comment>> memberMapList = new HashMap<Long, List<Comment>>();
		for(Comment comment : commentList){
			List<ReComment> reCommentList = walaApiService.getReCommentByRelatedidAndTomemberid(comment.getId(), null, null, 0, 6);
			Integer recommentCount = walaApiService.getReCommentCountByRelatedidAndTomemberid(comment.getId(), null, null);
			if(memberIdList.isEmpty()){
				memberIdList = ServiceHelper.getMemberIdListFromBeanList(reCommentList);
			}else{
				memberIdList = ListUtils.union(memberIdList, ServiceHelper.getMemberIdListFromBeanList(reCommentList));
			}
			recommentListMap.put(comment.getId(), reCommentList);
			recommentCountMap.put(comment.getId(), recommentCount);
			if(StringUtils.equals(comment.getTag(), TagConstant.TAG_ACTIVITY_MEMBER) || StringUtils.equals(comment.getTag(), TagConstant.TAG_COMMU_ACTIVITY)){
				Long activityid = comment.getRelatedid();
				if(StringUtils.equals(comment.getTag(), TagConstant.TAG_COMMU_ACTIVITY)){
					Map map = JsonUtils.readJsonToMap(comment.getOtherinfo());
					Integer aid=(Integer)map.get("id");
					if(aid!=null){
						activityid = Long.valueOf(aid);
					}
				}
				
				ErrorCode<List<RemoteApplyjoin>> code = synchActivityService.getApplyJoinListByActivityid(activityid, 0, 8);
				if(code.isSuccess()){
					List<RemoteApplyjoin> applyJoinList = code.getRetval();
					if(memberIdList.isEmpty()){
						memberIdList = ServiceHelper.getMemberIdListFromBeanList(applyJoinList);
					}else{
						memberIdList = ListUtils.union(memberIdList, ServiceHelper.getMemberIdListFromBeanList(applyJoinList));
					}
					activityJoinMemberMap.put(comment.getId(), applyJoinList);
				}
			}
			if(comment.getTransferid() != null && comment.getTransferid() != 0L){
				Comment transferComment = commentService.getCommentById(comment.getTransferid());
				if(memberIdList.isEmpty()){
					memberIdList = Arrays.asList(transferComment.getMemberid());
				}else{
					memberIdList = ListUtils.union(memberIdList, Arrays.asList(transferComment.getMemberid()));
				}
				transferCommentMap.put(comment.getId(), transferComment);
			}
			if(TagConstant.TAG_PICTURE_MEMBER.equals(comment.getTag()) || TagConstant.TAG_MEMBERPICTURE_MEMBER.equals(comment.getTag())){
				List<Comment> commentlist = commentService.getCommentListByRelatedId(comment.getTag(),null, comment.getRelatedid(), null, 0, 8);
				List<Long> memberidlist = ServiceHelper.getMemberIdListFromBeanList(commentlist);
				if(memberIdList.isEmpty()){
					memberIdList = memberidlist;
				}else{
					memberIdList = ListUtils.union(memberIdList,memberidlist);
				}
				memberMapList.put(comment.getId(), commentlist);
			}
		}
		if(memberIdList.isEmpty()){
			memberIdList = ServiceHelper.getMemberIdListFromBeanList(commentList);
		}else{
			memberIdList = ListUtils.union(memberIdList, ServiceHelper.getMemberIdListFromBeanList(commentList));
		}
		addCacheMember(model, memberIdList);
		model.put("commentList", commentList);
		model.put("transferCommentMap", transferCommentMap);
		model.put("activityJoinMemberMap", activityJoinMemberMap);
		model.put("recommentListMap", recommentListMap);
		model.put("recommentCountMap", recommentCountMap);
		model.put("activityJoinOrderMemberMap", activityJoinOrderMemberMap);
		model.put("memberMapList", memberMapList);
	}

	//申请内测
	@RequestMapping("/home/sns/applyBeta.xhtml")
	public String applyBeta(ModelMap model, String qq, String email){
		Member member=this.getLogonMember();
		Map params = new HashMap();
		params.put(MongoData.DEFAULT_ID_NAME, member.getId());
		params.put("membername", member.getNickname());
		params.put("addtime", DateUtil.getCurDate());
		params.put("email", email);
		params.put("qq", qq);
		params.put("status", "N");
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_APPLYBETA_MEMBER);
		return this.showJsonSuccess(model);
	}
	//得到内测用户
	@RequestMapping("/admin/getAllBetaMembers.xhtml")
	public String getAllBetaMembers(ModelMap model){
		Map params = new HashMap();
		List<Map> list = mongoService.find(MongoData.NS_APPLYBETA_MEMBER, params);
		model.put("memberList", list);
		return "admin/sns/betaMemberList.vm";
	}
	/**
	 * 删除内测用户信息
	 */
	@RequestMapping("/admin/deleteBetaMember.xhtml")
	public String deleteBetaMember(ModelMap model,Long id){
		mongoService.removeObjectById(MongoData.NS_APPLYBETA_MEMBER, "id", id);
		return showJsonSuccess(model);
	}
	/**
	 * 审核内测用户
	 */
	@RequestMapping("/admin/examineBetaMember.xhtml")
	public String examineBetaMember(ModelMap model,Long id,String examineStatu){
		Map oriMap = mongoService.findOne(MongoData.NS_APPLYBETA_MEMBER, "id", id);
		if(oriMap !=null){
			Map destMap = new HashMap(oriMap);
			destMap.put("status", examineStatu);
			mongoService.update(MongoData.NS_APPLYBETA_MEMBER, oriMap, destMap);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "显示失败！");
	}
	
	@RequestMapping("/personCenter/leftTest.xhtml")
	@ResponseBody
	public String leftTest(String memberid)
	{
		Long memberId = Long.valueOf(memberid);
		personCenterService.putMemberId(memberId);
		return "true";
	}
	
}
