package com.gewara.untrans.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.CommentConstant;
import com.gewara.constant.content.OpenShareConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.HttpTimeout;
import com.gewara.jms.JmsConstant;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.LinkShare;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.News;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Agenda;
import com.gewara.model.user.Member;
import com.gewara.model.user.Point;
import com.gewara.model.user.ShareMember;
import com.gewara.service.DaoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.RequestCallback;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.mime.qweibo.examples.QWeiboSyncApi;
import com.mime.qweibo.examples.QWeiboType.ResultType;

@Service("shareService")
public class ShareServiceImpl implements ShareService {

	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	public void setJmsService(JmsService jmsService) {
		this.jmsService = jmsService;
	}
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hbt) {
		hibernateTemplate = hbt;
	}
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}
	/**
	 * 发表新浪微博信息
	 */
	private void sendSinaInfo(LinkShare linkShare,ShareMember shareMember, String content, String picPath) {
		if(StringUtils.isBlank(content)) return;
		Map<String,String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
		if(otherMap.get("token") == null || otherMap.get("expires") == null){
			updateShareMemberRights(shareMember);
			return;
		}
		String token = otherMap.get("token");
		Timestamp addtime = shareMember.getAddtime();
		int expires = Integer.parseInt(otherMap.get("expires")) - 60;
		Timestamp duetime = DateUtil.addSecond(addtime, expires);
		if(!DateUtil.isAfter(duetime)){ //时间过期
			updateShareMemberRights(shareMember);
			return;
		}
		Map params = new HashMap();
		params.put("access_token", token);
		params.put("status", content);
		byte[] fileContent = null;
		if(StringUtils.isNotBlank(picPath)){
			final ByteArrayOutputStream os = new ByteArrayOutputStream();
			HttpUtils.getUrlAsInputStream(picPath, null, new RequestCallback(){
				@Override
				public boolean processResult(InputStream stream) {
					try {
						IOUtils.copy(stream, os);
					} catch (IOException e) {
						return false;
					}
					return true;
				}
			});
			fileContent = os.toByteArray();
		}
		try {
			if(fileContent == null || fileContent.length == 0){
				HttpUtils.postUrlAsString(OpenShareConstant.WEIBO_OAUTH_UPLOAD_TEXT, params, HttpTimeout.NORMAL_REQUEST);
			}else{
				String picturename = DateUtil.format(new Date(), "yyMMddHHmmss") + StringUtil.getRandomString(3) + picPath.substring(picPath.lastIndexOf("."));
				HttpUtils.uploadFile(OpenShareConstant.WEIBO_OAUTH_UPLOAD_PIC_TEXT, params, fileContent, "pic", picturename);
			}
		}catch (Exception e) {
			dbLogger.error(e.getMessage());
		}
		linkShare.setStatus(Status.Y);
		linkShare.setPicUrl(picPath);
		linkShare.setContent(content);
		daoService.updateObject(linkShare);
	}
	
	/**
	 * 发表腾讯微博
	 * @param shareMember
	 */
	private void sendTencentInfo(LinkShare linkShare,ShareMember shareMember, String content){
		if(StringUtils.isBlank(content)) return;
		Map<String,String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
		String token = otherMap.get("token");
		String tokensecret = otherMap.get("tokensecret");
		QWeiboSyncApi syncApi = new QWeiboSyncApi();
		String status =  null;
		String resText = null;
		resText = syncApi.publishMsg(token,tokensecret,content, null,ResultType.ResultType_Json);
		Map jsonObj = JsonUtils.readJsonToMap(resText);
		status = "" + jsonObj.get("ret");
		if("0".equals(status)){
			linkShare.setStatus(Status.Y);
			linkShare.setContent(content);
			daoService.updateObject(linkShare);
		}else if("3".equals(status)){//未授权
			updateShareMemberRights(shareMember);//解除同步
		}
	}
	
	@Override
	public void updateShareMemberRights(ShareMember shareMember){
		if(shareMember == null) return;
		shareMember.setOtherinfo(JsonUtils.addJsonKeyValue(shareMember.getOtherinfo(), "accessrights", "0"));//更新权限信息
		daoService.updateObject(shareMember);
	}
	
	@Override
	public void sendMicroInfo(LinkShare linkShare) {
		if(linkShare == null) return;
		if(linkShare.getStatus().equals(Status.Y)) return;
		Long memberid = linkShare.getMemberid();
		Long tagid = linkShare.getTagid();
		String tag = linkShare.getTag();
		String category = linkShare.getCategory();
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(linkShare.getType()), memberid);
		if(smList.isEmpty()) return;
		ShareMember om = smList.get(0);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(om.getOtherinfo());
		String accessrights = otherinfo.get("accessrights");
		if("1".equals(accessrights)){
			String right = otherinfo.get("rights");
			if(StringUtils.isNotBlank(right)){
				Map map = getSendContent(right, tag, tagid, category, memberid);
				//针对手机客户端iphone,android版本发送的哇啦信息是否同步到新浪，腾讯,如果客户端已同步，则网站不同步
				String content = map.get("content")+"";
				if(MemberConstant.SOURCE_QQ.equals(om.getSource())){
					sendTencentInfo(linkShare, om, content);
				}else if(MemberConstant.SOURCE_SINA.equals(om.getSource())){
					List<String> picList = (List<String>) map.get("picList");
					String fileUrl = "";
					if(picList != null && !picList.isEmpty()){
						fileUrl = config.getString("picPath")+picList.get(0);
					}
					sendSinaInfo(linkShare, om, content, fileUrl);
				}
			}
		}
	}
	
	
	@Override
	public void sendShareInfo(String tag, Long tagid, Long memberid, String category) {
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA,MemberConstant.SOURCE_QQ), memberid);
		if(smList.isEmpty()) return;
		for (ShareMember sm : smList) {
			Map<String, String> otherinfo = VmUtils.readJsonToMap(sm.getOtherinfo());
			String accessrights = otherinfo.get("accessrights");
			if(!"1".equals(accessrights)) continue;
			String right = otherinfo.get("rights");
			if(StringUtils.isBlank(right) && !tag.equals("news")) continue;
			String[] rights = right.split(",");
			List rightsList = Arrays.asList(rights);
			if(!rightsList.contains(tag) && !tag.equals("news")) continue;
			DetachedCriteria query = DetachedCriteria.forClass(LinkShare.class);
			query.add(Restrictions.eq("tag", tag));
			query.add(Restrictions.eq("tagid", tagid));
			query.add(Restrictions.eq("memberid",memberid));
			query.add(Restrictions.eq("type",sm.getSource()));
			if(StringUtils.isNotBlank(category)) query.add(Restrictions.eq("category",category));
			List list = hibernateTemplate.findByCriteria(query);
			if(!list.isEmpty()) continue;
			jmsService.sendMsgToDst(JmsConstant.QUEUE_SHARE, JmsConstant.TAG_SHARE2Out, "tag,tagid,memberid,category,type", tag, tagid, memberid, category, sm.getSource());
		}
	}
	
	@Override
	public void sendShareInfo(String tag, Long tagid, Long memberid, String content, String picUrl) {
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA,MemberConstant.SOURCE_QQ), memberid);
		if(smList.isEmpty()) return;
		for (ShareMember sm : smList) {
			DetachedCriteria query = DetachedCriteria.forClass(LinkShare.class);
			query.add(Restrictions.eq("tag", tag));
			query.add(Restrictions.eq("tagid", tagid));
			query.add(Restrictions.eq("memberid",memberid));
			query.add(Restrictions.eq("type",sm.getSource()));
			List list = hibernateTemplate.findByCriteria(query);
			if(!list.isEmpty()) continue;
			jmsService.sendMsgToDst(JmsConstant.QUEUE_SHARE, JmsConstant.TAG_SHARE2Out, "tag,tagid,memberid,type,content,picUrl", tag, tagid, memberid, sm.getSource(), content, picUrl);
		}
	}
	
	@Override
	public void sendCustomInfo(LinkShare linkShare) {
		if(linkShare == null) return;
		if(linkShare.getStatus().equals(Status.Y)) return;
		Long memberid = linkShare.getMemberid();
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(linkShare.getType()), memberid);
		if(smList.isEmpty()) return;
		for (ShareMember sm : smList) {
			if(MemberConstant.SOURCE_QQ.equals(sm.getSource())){
				sendTencentInfo(linkShare, sm, linkShare.getContent());
			}else if(MemberConstant.SOURCE_SINA.equals(sm.getSource())){
				String fileUrl = linkShare.getPicUrl() == null ? "":(config.getString("picPath")+linkShare.getPicUrl());
				sendSinaInfo(linkShare, sm, linkShare.getContent(), fileUrl);
			}
		}
	}
	
	@Override
	public LinkShare addShareInfo(String tag, Long tagid, Long memberid, String type, String content, String picUrl){
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(type), memberid);
		if(smList.isEmpty()) return null;
		ShareMember shareMember = smList.get(0);
		LinkShare ls = new LinkShare();
		ls.setMemberid(memberid);
		ls.setTag(tag);
		ls.setType(shareMember.getSource());
		ls.setTagid(tagid);
		ls.setContent(content);
		ls.setPicUrl(picUrl);
		ls.setAddtime(new Timestamp(System.currentTimeMillis()));
		ls.setStatus(Status.N);
		daoService.saveObject(ls);
		return ls;
	}
	
	@Override
	public LinkShare addShareInfo(String tag, Long tagid, Long memberid, String category,String type){
		List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList(type), memberid);
		if(smList.isEmpty()) return null;
		ShareMember shareMember = smList.get(0);
		LinkShare ls = new LinkShare(memberid, tag, tagid, shareMember.getSource(), category);
		daoService.saveObject(ls);
		return ls;
	}
	
	public int getWeiboBindStatus(ShareMember shareMember){
		if(shareMember == null) return OpenShareConstant.WEIBO_BIND_STATUS_UNDEFINED;
		if(!StringUtils.equals(shareMember.getSource(), MemberConstant.SOURCE_SINA)) return OpenShareConstant.WEIBO_BIND_STATUS_SUCCESS;
		if(shareMember.getAddtime() == null) return OpenShareConstant.WEIBO_BIND_STATUS_EXPIRED;
		Map<String,String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
		if(otherMap.get("token") == null || otherMap.get("expires") == null) return OpenShareConstant.WEIBO_BIND_STATUS_EXPIRED;
		Timestamp addtime = shareMember.getAddtime();
		int expires = Integer.parseInt(otherMap.get("expires")) - 60;
		Timestamp duetime = DateUtil.addSecond(addtime, expires);
		if(!DateUtil.isAfter(duetime)) return OpenShareConstant.WEIBO_BIND_STATUS_EXPIRED;
		return OpenShareConstant.WEIBO_BIND_STATUS_SUCCESS;
	}
	
	@Override
	public List<String> getSinaFriendList(Long memberid, int count){
		String cacheKey = "get134Sina123123FriendsXAL9" + memberid;
		List<String> friendsnameList = (List<String>) cacheService.get(CacheConstant.REGION_HALFHOUR, cacheKey);
		if(friendsnameList == null){
			friendsnameList = new ArrayList<String>();
			List<ShareMember> smList = getShareMemberByMemberid(Arrays.asList("sina"), memberid);
			if(smList == null || smList.isEmpty()) return friendsnameList;
			ShareMember shareMember = smList.get(0);
			int valid = getWeiboBindStatus(shareMember);
			if(valid != OpenShareConstant.WEIBO_BIND_STATUS_SUCCESS) return friendsnameList;
			Map<String,String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
			String token = otherMap.get("token");
			Map params = new HashMap();
			params.put("access_token", token);
			params.put("uid", shareMember.getLoginname());
			params.put("count", ""+count);
			HttpResult result = HttpUtils.getUrlAsString(OpenShareConstant.WEIBO_OAUTH_GET_FRIENDS, params, "utf-8");
			if(result.isSuccess()){
				String response = result.getResponse();
				Map usersMap = JsonUtils.readJsonToMap(response);
				List<Map> userList = (List<Map>) usersMap.get("users");
				if(userList != null && !userList.isEmpty()){
					for (Map obj : userList) {
						friendsnameList.add(""+obj.get("screen_name"));
					}
				}
				cacheService.set(CacheConstant.REGION_HALFHOUR, cacheKey, friendsnameList);
			}
		}
		return friendsnameList;
	}
	
	@Override
	public ShareMember getShareMemberByLoginname(String source, String loginname) {
		DetachedCriteria query = DetachedCriteria.forClass(ShareMember.class);
		query.add(Restrictions.eq("source",source));
		query.add(Restrictions.eq("loginname", loginname));
		List<ShareMember> result = hibernateTemplate.findByCriteria(query);
		if(result.isEmpty()) return null;
		return result.get(0);
	}
	
	@Override
	public List<ShareMember> getShareMemberByMemberid(List<String> source, Long memberid) {
		DetachedCriteria query = DetachedCriteria.forClass(ShareMember.class);
		if(!source.isEmpty()) query.add(Restrictions.in("source", source));
		if(memberid != null) query.add(Restrictions.eq("memberid", memberid));
		List<ShareMember> list = hibernateTemplate.findByCriteria(query);
		return list;
	}
	
	private Map getSendContent(String right, String tag, Long tagid, String category, Long memberid){
		Map result = new HashMap();
		String content = "";
		List<String> picList = null;
		String[] rights = right.split(",");
		List rightsList = Arrays.asList(rights);
		if(rightsList.contains(tag)){
			String url = "";				//路径
			String type = "";				//类型
			String date = "";				//时间
			String title = "";			//标题
			String body = "";				//内容
			String name = "";				//电影
			String venues = "";			//影院
			String address = "";			//地址
			String pointValue = "";		//积分
			String tnick = "";			//被转载人昵称
			String tbody = "";			//被转载的内容
			if("topic".equals(tag) || "moviecomment".equals(tag) || "dramacomment".equals(tag)){//帖子
				DiaryBase diary =  diaryService.getDiaryBase(tagid);
				if(diary != null){
					picList = WebUtils.getPictures(blogService.getDiaryBody(diary.getId()));
					if(StringUtils.equals(diary.getCategory(), TagConstant.TAG_MOVIE) && diary.getCategoryid() != null){
						type = OpenShareConstant.TAG_SHARE_DIARY_MOVIE;
						Movie movie = daoService.getObject(Movie.class, diary.getCategoryid());
						if(movie != null){
							name = movie.getMoviename();
							if(StringUtils.isNotBlank(movie.getLogo())) picList = Arrays.asList(movie.getLogo());
						}
					}else if(StringUtils.equals(diary.getCategory(), TagConstant.TAG_DRAMA) && diary.getCategoryid() != null){
						type = OpenShareConstant.TAG_SHARE_DIARY_DRAMA;
						Drama drama = daoService.getObject(Drama.class, diary.getCategoryid());
						if(drama != null){
							name = drama.getDramaname();
							if(StringUtils.isNotBlank(drama.getLogo())) picList = Arrays.asList(drama.getLogo());
						}
					}else{
						type = OpenShareConstant.TAG_SHARE_DIARY_TOPIC;
					}
					date = DateUtil.format(diary.getAddtime(), "MM月dd日");
					title = diary.getSubject();
					url = "http://www.gewara.com/blog/t"+diary.getId(); 
				}
			}else if("activity".equals(tag)){//活动
				ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(tagid);
				if(code.isSuccess()){
					RemoteActivity activity = code.getRetval();
					title = activity.getTitle();
					String startDate = DateUtil.format(activity.getStartdate(),"MM月dd日");
					date = startDate+(activity.getEnddate() == null? "" : ("-"+DateUtil.format(activity.getEnddate(),"MM月dd日")));
					url = "http://www.gewara.com/activity/"+activity.getId(); 
					if(StringUtils.isNotBlank(activity.getLogo())) picList = Arrays.asList(activity.getLogo());
					if("joinactivity".equals(category)){
						type = OpenShareConstant.TAG_SHARE_ACTIVITY_JOIN;
					}else{
						type = OpenShareConstant.TAG_SHARE_ACTIVITY_LAUNCH;
					}
				}
			}else if("ticketorder".equals(tag)){//购票
				if("dramaorder".equals(category)){
					type = OpenShareConstant.TAG_SHARE_TICKET_DRAMA;
					DramaOrder dramaOrder = daoService.getObject(DramaOrder.class, tagid);
					if(dramaOrder != null){
						Drama drama = daoService.getObject(Drama.class, dramaOrder.getDramaid());
						Theatre theatre = daoService.getObject(Theatre.class, dramaOrder.getTheatreid());
						Map<String, String> descMap = JsonUtils.readJsonToMap(dramaOrder.getDescription2());
						String playDate = descMap.get("时间");
						date = DateUtil.format(DateUtil.parseDate(playDate),"MM月dd日");
						name = drama.getDramaname();
						venues = theatre.getName();
						url = "http://www.gewara.com/drama/"+drama.getId();
						if(StringUtils.isNotBlank(drama.getLogo())) picList = Arrays.asList(drama.getLogo());
					}
				}else{
					type = OpenShareConstant.TAG_SHARE_TICKET_MOVIE;
					TicketOrder ticketOrder = daoService.getObject(TicketOrder.class, tagid);
					if(ticketOrder != null){
						Map<String, String> descMap = JsonUtils.readJsonToMap(ticketOrder.getDescription2());
						String playDate = descMap.get("场次");
						Movie movie = daoService.getObject(Movie.class, ticketOrder.getMovieid());
						Cinema cinema = daoService.getObject(Cinema.class, ticketOrder.getCinemaid());
						name = movie.getName();
						venues = cinema.getName();
						date = DateUtil.format(DateUtil.parseDate(playDate),"MM月dd日");
						url = "http://www.gewara.com/movie/"+movie.getId();
						if(StringUtils.isNotBlank(movie.getLogo())) picList = Arrays.asList(movie.getLogo());
					}
				}
			}else if("wala".equals(tag)){//哇啦
				Comment comment = commentService.getCommentById(tagid);
				if(comment != null){
					Comment transferComment = null;
					date = DateUtil.format(comment.getAddtime(), "MM月dd日");
					body = comment.getBody().replaceFirst(("#"+comment.getTopic()+"#"),"");
					url = "http://www.gewara.com/wala/"+comment.getMemberid();
					if(comment.getTransferid() != null){
						transferComment = commentService.getCommentById(comment.getTransferid());
					}
					if(transferComment != null){
						type = OpenShareConstant.TAG_SHARE_WALA_TRANSFER;
						tnick = transferComment.getNickname();
						tbody = transferComment.getBody();
						if(StringUtils.isNotBlank(transferComment.getPicturename())) picList = Arrays.asList(transferComment.getPicturename());
					}else{
						if(StringUtils.isNotBlank(comment.getPicturename())) picList = Arrays.asList(comment.getPicturename());
						type = OpenShareConstant.TAG_SHARE_WALA_OTHER;
						if(TagConstant.TAG_TOPIC.equals(comment.getTag())){
							type = OpenShareConstant.TAG_SHARE_WALA_TOPIC;
						}else if(StringUtils.equals(TagConstant.TAG_DRAMA,comment.getTag())){
							type = OpenShareConstant.TAG_SHARE_WALA_DRAMA;
						}else if(StringUtils.equals(TagConstant.TAG_MOVIE,comment.getTag())){
							type = OpenShareConstant.TAG_SHARE_WALA_MOVIE;
						}else{
							type = OpenShareConstant.TAG_SHARE_WALA_OTHER;
						}
						if(StringUtils.isNotBlank(comment.getTopic())) name = comment.getTopic();
					}
				}
			}else if("agenda".equals(tag)){//生活
				type = OpenShareConstant.TAG_SHARE_AGENDA_OTHER;
				Agenda agenda = daoService.getObject(Agenda.class, tagid);
				if(agenda != null){
					date = DateUtil.format(agenda.getStartdate(), "MM月dd日");
					title = agenda.getTitle();
					address = agenda.getAddress();
				}
			}else if("point".equals(tag)){//红包
				Point point = daoService.getObject(Point.class, tagid);
				if(point != null){
					if("festival".equals(category)){
						type = OpenShareConstant.TAG_SHARE_POINT_FESTIVAL;
					}else if("rewards".equals(category)){
						type = OpenShareConstant.TAG_SHARE_POINT_REWARDS;
					}else if("bit".equals(category)){
						if(point.getPoint()<0){
							type = OpenShareConstant.TAG_SHARE_POINT_BIT_NEGATIVE;
						}else{
							type = OpenShareConstant.TAG_SHARE_POINT_BIT_POSITIVE;
						}
					}else if(StringUtils.equals(category, "brt")){
						type = OpenShareConstant.TAG_SHARE_POINT_BRT;
					}else if(StringUtils.equals(category, PointConstant.TAG_SHARE_ORDER)){
						type = OpenShareConstant.TAG_SHARE_POINT_ORDER;
					}else{
						type = OpenShareConstant.TAG_SHARE_POINT_STABLE;
					}
					pointValue = point.getPoint()+"";
					date = DateUtil.format(point.getAddtime(), "MM月dd日");
					url = "http://www.gewara.com/everday/acct/mygift.xhtml";
				}
			}else if("news".equals(tag)){
				News news = daoService.getObject(News.class, tagid);
				if(news != null){
					content = "【"+news.getTitle()+"】"+news.getSummary();
					content += "http://www.gewara.com/news/"+news.getId();
					if(StringUtils.isNotBlank(news.getLogo())) picList = Arrays.asList(news.getLogo());
					else if(StringUtils.isNotBlank(news.getSmallLogo())) picList = Arrays.asList(news.getSmallLogo());
					else if(StringUtils.isNotBlank(news.getTplLogo())) picList = Arrays.asList(news.getTplLogo());
				}
			}
			if(StringUtils.isNotBlank(type)){
				List<JsonData> jsonDataList = daoService.getObjectListByField(JsonData.class, "tag", type);
				if(jsonDataList != null && jsonDataList.size() == 1){
					Map jsonMap = JsonUtils.readJsonToMap(jsonDataList.get(0).getData());
					if(jsonMap.get("content") != null){
						Member member = daoService.getObject(Member.class, memberid);
						content = String.valueOf(jsonMap.get("content"));
						content = StringUtils.replace(content, "time", date);
						content = StringUtils.replace(content, "name", name);
						content = StringUtils.replace(content, "title", title);
						content = StringUtils.replace(content, "venues", venues);
						content = StringUtils.replace(content, "address", address);
						content = StringUtils.replace(content, "point", pointValue);
						content = StringUtils.replace(content,"nick", member.getNickname());
						content = StringUtils.replace(content, "tnick", tnick);
						content = StringUtils.replace(content, "tcontent", tbody);
						content = StringUtils.replace(content, "content", body);
						content = StringUtil.getHtmlText(content, 200) + url + " ";
					}
				}
			}
		}
		if(StringUtils.isNotBlank(content)){
			List<String> regexList = StringUtil.findByRegex(content, CommentConstant.WALA_EXP, true);
			for (String str : regexList) {
				content = StringUtils.replace(content, str, CommentConstant.getExpMap(str));
			}
		}
		result.put("content",content);
		result.put("picList",picList);
		return result;
	}
	@Override
	public List searchShareSinaHisList(Timestamp starttime, Timestamp endtime, String status,String shareType,int from,int maxNum) {
		DetachedCriteria query = DetachedCriteria.forClass(LinkShare.class);
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(shareType)){
			query.add(Restrictions.eq("type", shareType));
		}
		if(starttime != null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime != null) query.add(Restrictions.le("addtime", endtime));
		query.addOrder(Order.asc("addtime"));
		List<LinkShare> shareList = hibernateTemplate.findByCriteria(query, from, maxNum);
		return shareList;
	}
	@Override
	public int searchShareCount(Timestamp starttime, Timestamp endtime, String status, String shareType) {
		DetachedCriteria query = DetachedCriteria.forClass(LinkShare.class);
		if(StringUtils.isNotBlank(status)){
			query.add(Restrictions.eq("status", status));
		}
		if(StringUtils.isNotBlank(shareType)){
			query.add(Restrictions.eq("type", shareType));
		}
		if(starttime != null) query.add(Restrictions.ge("addtime", starttime));
		if(endtime != null) query.add(Restrictions.le("addtime", endtime));
		query.setProjection(Projections.rowCount());
		List<LinkShare> shareList = hibernateTemplate.findByCriteria(query, 0, 0);
		if(shareList.isEmpty()) return 0;
		return new Integer("" + shareList.get(0));
	}
	@Override
	public void createShareMember(Member member, String source, String loginname, String token, String tokensecret, String expires) {
		String rights = null;
		//判断是否当前授权的新浪用户是否被其他用户已经绑定过
		ShareMember isExistsShareMember = getShareMemberByLoginname(MemberConstant.SOURCE_SINA, loginname);
		if(isExistsShareMember == null){//当前新浪用户尚未被我们用户绑定
			List<ShareMember> shareMemberList = getShareMemberByMemberid(Arrays.asList(source), member.getId());//判断账号是否已经绑定微博
			ShareMember shareMember = null;
			if(!shareMemberList.isEmpty()){
				shareMember = shareMemberList.get(0);
				shareMember.setLoginname(loginname);
			}else{
				shareMember = new ShareMember(member.getId(),source, loginname);
				rights = "ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala,point";
			}
			saveShareMember(shareMember, token, expires, tokensecret, "1", rights);
		}else{//已绑定
			List<ShareMember> shareMemberList = getShareMemberByMemberid(Arrays.asList(source), member.getId());
			for (ShareMember sMember : shareMemberList) {
				if(!sMember.getMemberid().equals(isExistsShareMember.getMemberid())){
					rights = "ticketorder,moviecomment,dramacomment,topic,agenda,activity,wala,point";
					daoService.removeObject(sMember);
					dbLogger.warn("用户更改微博绑定！loginname( "+loginname+") memberid:" + sMember.getMemberid() + " --> " + isExistsShareMember.getMemberid());
				}
			}
			isExistsShareMember.setMemberid(member.getId());                                
			saveShareMember(isExistsShareMember, token, expires, null, "1", rights);
		}
	}
	private void saveShareMember(ShareMember shareMember,String token,String expires, String tokensecret, String accessrights,String rights){
		if(shareMember != null){
			Map<String, String> otherInfo = VmUtils.readJsonToMap(shareMember.getOtherinfo());
			if(StringUtils.isNotBlank(expires))otherInfo.put("expires",expires);
			otherInfo.put("token",token);
			otherInfo.put("tokensecret",tokensecret);
			otherInfo.put("accessrights", accessrights);
			if(StringUtils.isNotBlank(rights))otherInfo.put("rights",rights);
			shareMember.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
			shareMember.setAddtime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(shareMember);
		}
		dbLogger.warn("用户绑定微博！loginname( "+shareMember.getLoginname()+") memberid:" + shareMember.getMemberid());
	}
}
