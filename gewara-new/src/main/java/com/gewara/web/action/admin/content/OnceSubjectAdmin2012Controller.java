package com.gewara.web.action.admin.content;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.DoubleFestivalCheckPicture;
import com.gewara.json.LotteryCode;
import com.gewara.json.OnceSubjectGrabVotes;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;

/**
 * 一次性专题模板
 * @author weikai
 *
 */
@Controller
public class OnceSubjectAdmin2012Controller extends AnnotationController {
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}
	/***
	 * 5元抢票专题(100期)
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_5yuan.xhtml")
	public String newSubjectList_5yuan(){
		return "admin/newsubject/price_5yuan.vm";
	}
	
	@RequestMapping("/admin/newsubject/commonSubjectList_5yuan.xhtml")
	public String commonSubjectList_5yuan(String type,ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		List<OnceSubjectGrabVotes> dataMap = mongoService.getObjectList(OnceSubjectGrabVotes.class, params,MongoData.ACTION_ORDERNUM, true, 0, 200);
		Map<String,Object> graVotesMap = new HashMap<String,Object>();
		if(!dataMap.isEmpty()){
			for(OnceSubjectGrabVotes data : dataMap){
				if(null != data.getRelatedid()){
					if(StringUtils.equals(data.getType(), "cinema")){
						Cinema cinema = daoService.getObject(Cinema.class, data.getRelatedid());
						graVotesMap.put(data.getId(), cinema);
					}else if(StringUtils.equals(data.getType(), "movie")){
						Movie movie = daoService.getObject(Movie.class, data.getRelatedid());
						graVotesMap.put(data.getId(), movie);
					}
					
				}
			}
		}
		model.put("type", type);
		model.put("dataMap", dataMap);
		model.put("graVotesMap", graVotesMap);
		return "admin/newsubject/commonSubjectList_5yuan.vm";
	}
	//5yuan链接给参数(第100期)
	@RequestMapping("/admin/newsubject/detail5yuan.xhtml")
	public String detail5yuan(String id, String type, ModelMap model) {
		if(StringUtils.isNotBlank(id)) {
			OnceSubjectGrabVotes data = mongoService.getObject(OnceSubjectGrabVotes.class, MongoData.DEFAULT_ID_NAME, id);
			if(data != null){
				model.put("data", data);
			}
		}
		model.put("type", type);
		return "admin/newsubject/detail5yuan.vm";
	}
	//5yuan保存和修改影院
	@RequestMapping("/admin/newsubject/addCinema5yuan.xhtml")
	public String addCinema5yuan(Long relatedid, String id, String type, ModelMap model){
		if(relatedid == null)return showJsonError(model,"请填写影院或电影ID");
		String citycode = null;
		String countycode = null;
		if(StringUtils.equals(type, "cinema")){
			Cinema cinema = daoService.getObject(Cinema.class, relatedid);
			if(null == cinema) return showJsonError(model,"请填写正确的影院ID");
			citycode = cinema.getCitycode();
			countycode = cinema.getCountycode();
		}else{
			Movie movie = daoService.getObject(Movie.class, relatedid);
			if(null == movie) return showJsonError(model,"请填写正确的电影ID");
		}
		OnceSubjectGrabVotes grabVotes = new OnceSubjectGrabVotes();
		if(StringUtils.isNotBlank(id)){
			grabVotes = mongoService.getObject(OnceSubjectGrabVotes.class, MongoData.DEFAULT_ID_NAME, id);
			grabVotes.setRelatedid(relatedid);
			grabVotes.setCitycode(citycode);
			grabVotes.setCountycode(countycode);
		}else{
			grabVotes.setId("5yuan"+System.currentTimeMillis());
			grabVotes.setAddtime(DateUtil.currentTime());
			grabVotes.setOrdernum(0);
			grabVotes.setSupport(0);
			grabVotes.setRelatedid(relatedid);
			grabVotes.setCitycode(citycode);
			grabVotes.setCountycode(countycode);
		}
		grabVotes.setType(type);
		mongoService.saveOrUpdateObject(grabVotes,MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	//5yuan抢票影院排序
	@RequestMapping("/admin/newsubject/changeOrderNum5yuan.xhtml")
	public String changeOrderNum5yuan(String id, Integer ordernum, Integer support, ModelMap model){
		OnceSubjectGrabVotes grabVotes = mongoService.getObject(OnceSubjectGrabVotes.class, MongoData.DEFAULT_ID_NAME, id);
		if(grabVotes != null){
			if(null != ordernum){
				grabVotes.setOrdernum(ordernum);
				mongoService.saveOrUpdateObject(grabVotes, MongoData.DEFAULT_ID_NAME);
				return showJsonSuccess(model);
			}else if(null != support){
				grabVotes.setSupport(support);
				mongoService.saveOrUpdateObject(grabVotes, MongoData.DEFAULT_ID_NAME);
				return showJsonSuccess(model);
			}else{
				return showJsonError_NOT_FOUND(model);
			}
		}
		return showJsonError_NOT_FOUND(model);
	}
	@RequestMapping("/admin/newsubject/addtime.xhtml")
	public String addSingleTime(String id,String tag, String url,String type, Timestamp starttime, Timestamp endtime, ModelMap model){
		if(StringUtils.equals(type, "valentine10")){
			Map map = new HashMap();
			if(StringUtils.isBlank(id)){
				map.put(MongoData.DEFAULT_ID_NAME, System.currentTimeMillis() + StringUtil.getRandomString(5));
			}else{
				map = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
			}
			map.put(MongoData.SINGLES_CINEMAURL, url);
			map.put("type", "valentine10");
			mongoService.saveOrUpdateMap(map, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTIVITY_SINGLES);
		}else{
			if(starttime == null || endtime == null) return showJsonError(model, "时间参数错误！");
			if(StringUtils.isBlank(tag)) tag = MongoData.SINGLE_TIMES;
			Map params = new HashMap();
			params.put("type", type);
			params.put("tag", tag);
			mongoService.removeObjectList(MongoData.NS_ACTIVITY_SINGLES, params);
			params.put("starttime", starttime);
			params.put("endtime", endtime);
			params.put("addtime", DateUtil.format(DateUtil.currentTime(), "yyyy-MM-dd HH:mm:ss"));
			params.put(MongoData.DEFAULT_ID_NAME, String.valueOf(System.currentTimeMillis()));
			mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTIVITY_SINGLES);
		}
		return showJsonSuccess(model);
	}
	//5yuan删除功能
	@RequestMapping("/admin/newsubject/delCinema5yuan.xhtml")
	public String delCinema5yuan(String id, ModelMap model){
		if(StringUtils.isBlank(id))return showJsonError_NOT_FOUND(model);
		mongoService.removeObjectById(OnceSubjectGrabVotes.class, MongoData.DEFAULT_ID_NAME, id);
		return showJsonSuccess(model);
	}

	//双旦节首页
	@RequestMapping("/admin/newsubject/doubleFestival.xhtml")
	public String doubleFestival(ModelMap model){
		Map params = new HashMap();
		params.put("tag", MongoData.DOUBLE_FESTIVAL_THIRTEEN);
		Map thirteenMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.DOUBLE_FESTIVAL_POM);
		Map pomMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.DOUBLE_FESTIVAL_TREE);
		Map treeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.DOUBLE_FESTIVAL_PARTY);
		Map partyMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);	
		params = new HashMap();
		params.put("status", "22day");
		List<LotteryCode> lotterylist22 = mongoService.getObjectList(LotteryCode.class, params,"status", false, 0, 3);
		params.put("status", "23day");
		List<LotteryCode> lotterylist23 = mongoService.getObjectList(LotteryCode.class, params,"status", false, 0, 3);
		params.put("status", "24day");
		List<LotteryCode> lotterylist24 = mongoService.getObjectList(LotteryCode.class, params,"status", false, 0, 3);
		model.put("day22", lotterylist22);
		model.put("day23", lotterylist23);
		model.put("day24", lotterylist24);
		model.put("thirteenMap", thirteenMap);
		model.put("pomMap", pomMap);
		model.put("treeMap", treeMap);
		model.put("partyMap", partyMap);
		return "admin/newsubject/doubleFestivalIndex.vm"; 
	}
	//图片
	@RequestMapping("/admin/newsubject/updatePictureSupport.xhtml")
	public String updatePictureSupport(int support,String id, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model,"数据有误!");
		Map checkPic = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
		if(checkPic != null){
			checkPic.put(MongoData.ACTION_SUPPORT,support);
			mongoService.saveOrUpdateMap(checkPic, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
		}
		return showJsonSuccess(model);
	}
	//图片审核(最美十三钗)
	@RequestMapping("/admin/newsubject/doubleFestivalCheckPicture.xhtml")
	public String doubleFestivalCheckPicture(String status,Integer pageNo, Long memberid, ModelMap model){
		if(StringUtils.isBlank(status)) status = Status.Y_NEW;
		if(null == pageNo) pageNo=0;
		int rowsPerPage = 30;
		int forms = pageNo * rowsPerPage;
		Map pms = new HashMap();
		Map params = new HashMap();
		if(null == memberid){
			pms.put(MongoData.ACTION_STATUS, status);
			params.put(MongoData.ACTION_STATUS, status);
		}else{
			pms.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			model.put("memberid", memberid);
		}
		List<DoubleFestivalCheckPicture> checkPicture = mongoService.getObjectList(DoubleFestivalCheckPicture.class, pms,MongoData.GEWA_CUP_MEMBERID, false, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getObjectCount(DoubleFestivalCheckPicture.class, pms), rowsPerPage, pageNo, "admin/newsubject/doubleFestivalCheckPicture.xhtml");
		pageUtil.initPageInfo(params);
		model.put("dfCheckPicList",checkPicture);
		model.put("status", status);
		model.put("pageUtil", pageUtil);
		return "admin/newsubject/doubleFestivalCheckPic.vm";
	}
	@RequestMapping("/admin/newsubject/updateDFCheckPicture.xhtml")
	public String updateDFCheckPicture(String id, String status,Long uid, ModelMap model){
		if(StringUtils.isNotBlank(id)&&StringUtils.isNotBlank(status)&&(null != uid)){
			DoubleFestivalCheckPicture dfCheckPic = mongoService.getObject(DoubleFestivalCheckPicture.class, MongoData.DEFAULT_ID_NAME, id);
			if(null != dfCheckPic){
				dfCheckPic.setStatus(status);
				mongoService.saveOrUpdateObject(dfCheckPic, MongoData.DEFAULT_ID_NAME);
				if(StringUtils.equals(status, Status.Y)){
					LotteryCode lotterycode = new LotteryCode();
					lotterycode.setId(System.currentTimeMillis()+RandomUtils.nextInt(1000));
					lotterycode.setStatus(Status.Y);
					lotterycode.setMemberid(uid);
					String randomcode = System.currentTimeMillis()+StringUtil.getRandomString(5);
					lotterycode.setLotteryno(randomcode);
					mongoService.saveOrUpdateObject(lotterycode, MongoData.DEFAULT_ID_NAME);
					String body = "你参加“争做最美十三钗”活动，上传的图片已通过审核，获得一次圣诞抽奖机会 <a href='"+config.getBasePath()+"subject/loadPromise.xhtml'>去抽奖</a>";
					userMessageService.sendSiteMSG(uid, SysAction.STATUS_RESULT, new Long(id), body);
				}
			}
		}
		return showRedirect("/admin/newsubject/doubleFestivalCheckPicture.xhtml", model);
	}
	@RequestMapping("/admin/newsubject/addWinMember.xhtml")
	public String addWinMember(String dtype, String memberstr, ModelMap model){
		List<Long> listId = BeanUtil.getIdList(memberstr, ",");
		List<Member> memberlist = daoService.getObjectList(Member.class, listId);
		if(memberlist.isEmpty())return showJsonError(model,"请输入获奖用户ID");
		if(memberlist.size() != 3)return showJsonError(model,"请输入3个获奖用户ID");
		for(Member member: memberlist){
			LotteryCode lottery = new LotteryCode();
			lottery.setId(System.currentTimeMillis()+RandomUtils.nextInt(1000));
			lottery.setStatus(dtype);
			lottery.setMemberid(member.getId());
			lottery.setLotteryno(member.getNickname());
			mongoService.saveOrUpdateObject(lottery, MongoData.DEFAULT_ID_NAME);
		}
		return showJsonSuccess(model);
	}
	
	//情人节专题start
	@RequestMapping("/admin/newsubject/valentine.xhtml")
	public String valentine(ModelMap model){
		Map params = new HashMap();
		params.put("tag", MongoData.VALENTINE_MOVIE_DAREN);
		Map thirteenMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.VALENTINE_ACTIVITY_DAREN);
		Map pomMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.VALENTINE_SCENE_LOVE);
		Map treeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.VALENTINE_HAPPY);
		Map partyMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.VALENTINE_PHONE_DAREN);
		Map phoneMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("thirteenMap", thirteenMap);
		model.put("pomMap", pomMap);
		model.put("treeMap", treeMap);
		model.put("partyMap", partyMap);
		model.put("phoneMap", phoneMap);
		Map param = new HashMap();
		param.put("type", "valentine10");
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, param);
		model.put("urlMap", map);
		return "admin/newsubject/valentineSubject.vm";
	}
	
	//情人节专场
	@RequestMapping("/admin/newsubject/commonCinema.xhtml")
	public String sweetImage(String type, String tag, ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		List<Map> cinemaList = mongoService.find(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, params, MongoData.ACTION_ORDERNUM, true);
		if(!cinemaList.isEmpty()){
			model.put("cinemaList", cinemaList);
		}
		model.put("type", type);
		return "admin/newsubject/commonCinemaSubject.vm";
	}
	
	//链接给参数
	@RequestMapping("/admin/newsubject/addCinema.xhtml")
	public String sweetImageParameters(String id, String type, ModelMap model) {
		if(StringUtils.isNotBlank(id)) {
			Map data = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
			if(data != null){
				model.put("data", data);
			}
		}
		model.put("type", type);
		return "admin/newsubject/commonAddCinema.vm";
	}
	
	@RequestMapping("/admin/newsubject/saveCinema.xhtml")
	public String addCinema(String id, String relatedid, String style, String content, String content2, String type, String tag, ModelMap model){
		Map dataMap = new HashMap();
		if(StringUtils.isBlank(style)){
			Cinema cinema = daoService.getObject(Cinema.class, Long.valueOf(relatedid));
			if(cinema == null)return showJsonError(model,"此场馆不存在!");
			if(StringUtils.isNotBlank(id)){
				dataMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
			}else{
				dataMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
				dataMap.put(MongoData.ACTION_ADDTIME, DateUtil.currentTime());
			}
			dataMap.put(MongoData.ACTION_COUNTYCODE, cinema.getCountycode());
			dataMap.put(MongoData.ACTION_CITYCODE,cinema.getCitycode());
			dataMap.put(MongoData.ACTION_TITLE, cinema.getName());
			dataMap.put(MongoData.ACTION_NEWSLOGO, cinema.getLimg());
			
		}else{
			Movie movie = daoService.getObject(Movie.class, Long.valueOf(relatedid));
			if(movie == null)return showJsonError(model,"此电影不存在!");
			if(StringUtils.isNotBlank(id)){
				dataMap = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
			}else{
				dataMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
				dataMap.put(MongoData.ACTION_ADDTIME, DateUtil.currentTime());
			}
			dataMap.put(MongoData.ACTION_TITLE, movie.getName());
			dataMap.put(MongoData.ACTION_NEWSLOGO, movie.getLimg());
		}
		dataMap.put(MongoData.ACTION_TYPE, type);
		dataMap.put(MongoData.ACTION_TAG, tag);
		dataMap.put(MongoData.ACTION_RELATEDID, relatedid);
		dataMap.put(MongoData.ACTION_CONTENT, content);
		dataMap.put(MongoData.ACTION_CONTENT2, content2);
		dataMap.put(MongoData.ACTION_SUPPORT, 0);
		dataMap.put(MongoData.ACTION_ORDERNUM, 0);
		mongoService.saveOrUpdateMap(dataMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/delCinema.xhtml")
	public String delCinema(String id, ModelMap model){
		if(StringUtils.isBlank(id))return showJsonError(model,"参数错误!");
		mongoService.removeObjectById(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/changeOrdernumCinema.xhtml")
	public String changOrdernum(String id, Integer num, String type, ModelMap model){
		if(StringUtils.isBlank(id)||num == null)return showJsonError(model,"参数错误!");
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_PUBLIC_CINEMA, MongoData.SYSTEM_ID, id);
		if(StringUtils.isBlank(type)) map.put(MongoData.ACTION_ORDERNUM, num);
		else map.put(MongoData.ACTION_SUPPORT, num);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_PUBLIC_CINEMA);
		return showJsonSuccess(model);
	}
	
	//情人节图片审核
	@RequestMapping("/admin/newsubject/checkPictureValentine.xhtml")
	public String checkPicturevalentine(String type,String status,Integer pageNo, Long memberid, ModelMap model){
		if(StringUtils.isBlank(status)) status = Status.Y_NEW;
		if(null == pageNo) pageNo=0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		Map queryParams = new HashMap();
		Map params = new HashMap();
		if(null == memberid){
			queryParams.put(MongoData.ACTION_STATUS, status);
			params.put(MongoData.ACTION_STATUS, status);
		}else{
			queryParams.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			model.put("memberid", memberid);
		}
		queryParams.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TYPE, type);
		List<Map> checkPicture = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams, MongoData.ACTION_ADDTIME, true, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams), rowsPerPage, pageNo, "admin/newsubject/checkPictureValentine.xhtml");
		pageUtil.initPageInfo(params);
		model.put("dfCheckPicList",checkPicture);
		model.put("status", status);
		model.put("type", type);
		model.put("pageUtil", pageUtil);
		return "admin/newsubject/commonCheckPictrue.vm";
	}
	//修改图片状态
	@RequestMapping("/admin/newsubject/updatePicStatus.xhtml")
	public String updatePicStatus(String id, String status, String type, ModelMap model){
		if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(status)){
			Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
			if(map != null){
				map.put(MongoData.ACTION_STATUS, status);
				mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
				if(type.equals("valentine")){
					if(StringUtils.equals(status, Status.Y)){
						Map params = new HashMap();
						params.put(MongoData.ACTION_TYPE, type);
						params.put(MongoData.GEWA_CUP_MEMBERID, map.get("memberid"));
						Map memberMap = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
						if(memberMap == null){
							memberMap = new HashMap();
							memberMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis()+StringUtil.getRandomString(5));
							memberMap.put(MongoData.GEWA_CUP_MEMBERID, new Long(map.get("memberid")+""));
							memberMap.put(MongoData.ACTION_MEMBERNAME, map.get("membername"));
							memberMap.put(MongoData.ACTION_TYPE, "valentine");
							memberMap.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
							memberMap.put(MongoData.VALENTINE_HAPPY, Status.Y);
							memberMap.put(MongoData.ACTION_STATUS, MongoData.VALENTINE_HAPPY);
							mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
						}else{
							if(memberMap.get(MongoData.VALENTINE_SWEET_IMAGE) == null){
								memberMap.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
								memberMap.put(MongoData.VALENTINE_HAPPY, Status.Y);
								memberMap.put(MongoData.ACTION_STATUS, MongoData.VALENTINE_HAPPY);
								mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
							}
						}
					}
				}
			}
		}
		return showRedirect("/admin/newsubject/checkPictureValentine.xhtml?type="+type, model);
	}
	//用户
	@RequestMapping("/admin/newsubject/showMemberList.xhtml")
	public String showMemberList(String type, String tag, ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(tag, Status.Y);
		List<Map> memberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		if(memberList != null){
			model.put("memberList", memberList);
		}
		model.put("type", type);
		model.put("tag",tag);
		return "admin/newsubject/commonMemberSubject.vm";
	}
	//添加用户
	@RequestMapping("/admin/newsubject/addMemberUpdateStatus.xhtml")
	public String addMemberUpdateStatus(Long memberid, String tag, String type, String status, ModelMap model){
		if(StringUtils.isBlank(status))status = Status.Y;
		Map params = new HashMap();
		params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
		params.put(MongoData.ACTION_TYPE, type);
		Map member = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		if(member == null){
			member = new HashMap();
			member.put(MongoData.SYSTEM_ID, System.currentTimeMillis()+StringUtil.getRandomString(5));
			member.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			Member m = daoService.getObject(Member.class, memberid);
			if(m == null)return showJsonError(model,"没有此用户");
			member.put(MongoData.ACTION_MEMBERNAME, m.getNickname());
			member.put(MongoData.ACTION_TYPE, type);
		}
		member.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		member.put(tag, status);
		member.put(MongoData.ACTION_STATUS, tag);
		mongoService.saveOrUpdateMap(member, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		return showJsonSuccess(model);
	}
	//链接给参数
	@RequestMapping("/admin/newsubject/memberParameters.xhtml")
	public String memberParameters(String id, String type, String tag, ModelMap model) {
		if(StringUtils.isNotBlank(id)) {
			Map data = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, MongoData.SYSTEM_ID, id);
			if(data != null){
				model.put("data", data);
			}
		}
		model.put("type", type);
		model.put("tag", tag);
		return "admin/newsubject/commonAddMember.vm";
	}
	//查看点灯情况
	@RequestMapping("/admin/newsubject/memberActivityInfo.xhtml")
	public String memberActivityInfo(Integer pageNo, String top, ModelMap model){
		if(null == pageNo) pageNo=0;
		int rowsPerPage = 50;
		if(StringUtils.isNotBlank(top))rowsPerPage=100;
		int forms = pageNo * rowsPerPage;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, "valentine");
		if(StringUtils.isNotBlank(top)){
			params.put(MongoData.VALENTINE_MOVIE_DAREN, Status.Y);
			params.put(MongoData.VALENTINE_ACTIVITY_DAREN, Status.Y);
			params.put(MongoData.VALENTINE_PHONE_DAREN, Status.Y);
			params.put(MongoData.VALENTINE_SCENE_LOVE, Status.Y);
			params.put(MongoData.VALENTINE_HAPPY, Status.Y);
		}
		List<Map> memberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params, MongoData.ACTION_ADDTIME, true, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, params), rowsPerPage, pageNo, "admin/newsubject/memberActivityInfo.xhtml");
		pageUtil.initPageInfo(params);
		model.put("memberList", memberList);
		model.put("pageUtil", pageUtil);
		model.put("temp", "y");
		return "admin/newsubject/valentineSubject.vm";
	}
	//情人节专题end
	
	
	//白色情人节专题start
	@RequestMapping("/admin/newsubject/whiteDay.xhtml")
	public String whiteday(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, MongoData.WHITE_DAY);
		Map whiteActivity = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("whiteActivity", whiteActivity);
		return "admin/newsubject/whiteDaySubject.vm";
	}
	
	@RequestMapping("/admin/newsubject/whiteJoinNum.xhtml")
	public String whiteJoinNum(ModelMap model){
		Map dataMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_SUBJECT_COUNT, MongoData.WHITE_DAY);
		return showJsonSuccess(model, dataMap);
	}
	
	@RequestMapping("/admin/newsubject/saveWhiteJoinNum.xhtml")
	public String saveWhiteJoinNum(Integer joinNum, ModelMap model){
		if(joinNum == null || joinNum >=31400) return showJsonError(model, "数据设置错误！");
		Map dataMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_SUBJECT_COUNT, MongoData.WHITE_DAY);
		if(dataMap == null){
			dataMap = new HashMap();
			dataMap.put(MongoData.DEFAULT_ID_NAME, MongoData.WHITE_DAY);
			dataMap.put("joinNum", 1);
		}else{
			Object result = dataMap.get("joinNum");
			if(result != null){
				Integer count = Integer.valueOf(result+"");
				if(joinNum < count)  return showJsonError(model, "数据设置错误！");
			}
			dataMap.put("joinNum",joinNum);
		}
		mongoService.saveOrUpdateMap(dataMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_SUBJECT_COUNT);
		return showJsonSuccess(model);
	}
	
	//白色情人节图片审核页面
	@RequestMapping("/admin/newsubject/checkPictureWhiteDay.xhtml")
	public String checkPicturWhiteDay(String status,Integer pageNo, Long memberid, ModelMap model){
		if(StringUtils.isBlank(status))status = Status.Y_NEW;
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		Map queryParams = new HashMap();
		Map params = new HashMap();
		if(null == memberid){
			queryParams.put(MongoData.ACTION_STATUS, status);
			params.put(MongoData.ACTION_STATUS, status);
		}else{
			queryParams.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			model.put("memberid", memberid);
		}
		queryParams.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
		List<Map> checkPicture = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams, MongoData.GEWA_CUP_MEMBERID, true, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams), rowsPerPage, pageNo, "/admin/newsubject/checkPictureWhiteDay.xhtml");
		pageUtil.initPageInfo(params);
		model.put("whiteCheckPicList",checkPicture);
		model.put("status", status);
		model.put("pageUtil", pageUtil);
		return "admin/newsubject/checkWhiteDayPictrue.vm";
	}
	@RequestMapping("/admin/newsubject/checkPicture.xhtml")
	public String checkPicture(String status,Integer pageNo, Long memberid, String type, ModelMap model){
		if(StringUtils.isBlank(type)) type = MongoData.SPORT_POOL_PARTY;
		if(StringUtils.isBlank(status))status = Status.Y_NEW;
		if(pageNo == null)pageNo = 0;
		int rowsPerPage = 20;
		int forms = pageNo * rowsPerPage;
		Map queryParams = new HashMap();
		Map params = new HashMap();
		if(null == memberid){
			queryParams.put(MongoData.ACTION_STATUS, status);
			params.put(MongoData.ACTION_STATUS, status);
		}else{
			queryParams.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			model.put("memberid", memberid);
		}
		queryParams.put(MongoData.ACTION_TYPE, type);
		List<Map> checkPicture = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams, MongoData.GEWA_CUP_MEMBERID, true, forms, rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, queryParams), rowsPerPage, pageNo, "/admin/newsubject/checkPicture.xhtml");
		pageUtil.initPageInfo(params);
		model.put("dfCheckPicList",checkPicture);
		model.put("status", status);
		model.put("type", type);
		model.put("pageUtil", pageUtil);
		return "admin/newsubject/commonCheckPictrue.vm";
	}
	//更改图片状态(审核)
	@RequestMapping("/admin/newsubject/whitePicStatus.xhtml")
	public String changePicStatus(String id,String status,ModelMap model){
		if(StringUtils.isNotBlank(id) && StringUtils.isNotBlank(status)){
		 Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID,id);
		 if(map !=null){
			    Map param = new HashMap();
			    param.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
			    param.put(MongoData.ACTION_STATUS, Status.Y);
			    param.put(MongoData.ACTION_MEMBERID, map.get("memberid"));
			    int countY = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, param);
			    param.put(MongoData.ACTION_STATUS, Status.N);
			    int countN = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, param);
				 map.put(MongoData.ACTION_STATUS, status);
				 mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
				 if(StringUtils.equals(status, Status.N) && countN == 0){	
					 Map params = new HashMap();
					 params.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
					 params.put(MongoData.ACTION_MEMBERID, map.get("memberid"));
					 Map memberMap = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
					 if(memberMap !=null){
					 memberMap.put(MongoData.ACTION_STATUS, Status.N);
					 mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
					 }else{
					 memberMap = new HashMap();
					 memberMap.put(MongoData.SYSTEM_ID,System.currentTimeMillis()+StringUtil.getRandomString(5));
					 memberMap.put(MongoData.GEWA_CUP_MEMBERID,  new Long(map.get("memberid")+""));
					 memberMap.put(MongoData.ACTION_MEMBERNAME, map.get("membername"));
					 memberMap.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
					 memberMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
					 memberMap.put(MongoData.ACTION_STATUS, Status.N);
					 mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
					 }
		 }else if(StringUtils.equals(status, Status.Y) && countY == 0){
				 Map params = new HashMap();
				 params.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
				 params.put(MongoData.ACTION_MEMBERID, map.get("memberid"));
				 Map memberMap = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
				 if(memberMap == null){
					 memberMap = new HashMap();
					 memberMap.put(MongoData.SYSTEM_ID,System.currentTimeMillis()+StringUtil.getRandomString(5));
					 memberMap.put(MongoData.GEWA_CUP_MEMBERID,  new Long(map.get("memberid")+""));
					 memberMap.put(MongoData.ACTION_MEMBERNAME, map.get("membername"));
					 memberMap.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
					 memberMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
					 memberMap.put(MongoData.ACTION_STATUS, Status.Y);
					 mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				 }else{
					 memberMap.put(MongoData.ACTION_STATUS, Status.Y);
					 mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
				 }
				 //发送站内信，
				 String body = "恭喜您上传的KISSes已通过审核，获得订购3月14日2张电影票活动价资格（不含IMAX等特殊场次）。请使用优惠活动白色情人节专属通道付款，感谢您对格瓦拉的支持。客服电话：4000-406-506 ";
				 userMessageService.sendSiteMSG(Long.valueOf(map.get("memberid")+""),  SysAction.STATUS_RESULT, null, body);
				 //发送手机短信
				 Long memberid = (Long) map.get("memberid");
				 if(memberid>0){
				 Member m = daoService.getObject(Member.class,memberid );
					 if(m.isBindMobile()){
						 String msg = "恭喜您参加的”白色情人KISSes”活动已经通过审核";
						 SMSRecord sms = new SMSRecord(m.getMobile());  
						 sms.setContent(msg);
						 sms.setTradeNo("white"+DateUtil.format(DateUtil.getCurFullTimestamp(),"yyyyMMddHHmmss")+StringUtil.getRandomString(5));
						 sms.setSendtime(DateUtil.getCurFullTimestamp());
						 sms.setValidtime(DateUtil.addHour(DateUtil.getCurFullTimestamp(), 2) );
						 sms.setSmstype(SmsConstant.SMSTYPE_ACTIVITY);
						 untransService.sendMsgAtServer(sms, true);
					 }
				 }

				Map dataMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_SUBJECT_COUNT, MongoData.WHITE_DAY);
				if(dataMap == null){
					dataMap = new HashMap();
					dataMap.put(MongoData.DEFAULT_ID_NAME, MongoData.WHITE_DAY);
					dataMap.put("joinNum", 1);
				}else{
					dataMap.put("joinNum",Integer.valueOf(dataMap.get("joinNum")+"")+1);
				}
				mongoService.saveOrUpdateMap(dataMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_SUBJECT_COUNT);
			 }
		 }
		}
		return showRedirect("/admin/newsubject/checkPictureWhiteDay.xhtml",model);
	}
	//白色情人节活动设置
	@RequestMapping("/admin/newsubject/whitetime.xhtml")
	public String whiteTime(String starttime,String endtime,String tag,ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, tag);
		Map singlesMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		if(singlesMap == null){
			singlesMap = new HashMap();
			singlesMap.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
			singlesMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
			singlesMap.put(MongoData.ACTION_STARTTIME, starttime);
			singlesMap.put(MongoData.ACTION_ENDTIME, endtime);
			singlesMap.put(MongoData.ACTION_COUNT, 0);
			singlesMap.put(MongoData.ACTION_TAG, tag);
			mongoService.saveOrUpdateMap(singlesMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_SINGLES);
		}else{
			singlesMap.put(MongoData.ACTION_STARTTIME, starttime);
			singlesMap.put(MongoData.ACTION_ENDTIME, endtime);
			mongoService.saveOrUpdateMap(singlesMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_SINGLES);
		}
		return showJsonSuccess(model);
	}
	//查看参加活动人员信息
	@RequestMapping("/admin/newsubject/memberJoinInfo.xhtml")
	public String memberJoinInfo(Integer pageNo,ModelMap model){
		if(pageNo == null) pageNo =0;
		int rowsPerPage = 50;
		int forms = pageNo * rowsPerPage;
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.WHITE_DAY);
		params.put(MongoData.ACTION_STATUS, Status.Y);
		List<Map> memberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER,params,MongoData.ACTION_ADDTIME,true,forms,rowsPerPage);
		PageUtil pageUtil = new PageUtil(mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_MEMBER, params),rowsPerPage,pageNo,"/admin/newsubject/memberJoinInfo.xhtml");
		pageUtil.initPageInfo(params);
		model.put("memberList", memberList);
		model.put("pageUtil", pageUtil);
		model.put("temp", "y");
		return "admin/newsubject/whiteDaySubject.vm";
	}
	//白色情人节END
	
	//年度精选start
	@RequestMapping("/admin/newsubject/annualSelection.xhtml")
	public String annualSelection(ModelMap model){
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, MongoData.SYSTEM_ID, "8888888888ICBC");
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, "8888888888ICBC");
			map.put(MongoData.ANNUALSELECTION_CITY_OF_LOVE, 0);
			map.put(MongoData.ANNUALSELECTION_SAME_ART, 0);
			map.put(MongoData.ANNUALSELECTION_IDOL_INVINCIBLE, 0);
			map.put(MongoData.ANNUALSELECTION_REJUVENATE, 0);
			map.put(MongoData.ANNUALSELECTION_ANNUAL_JUXIAN, 0);
			mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		}
		model.put("map", map);
		Map critics = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, MongoData.SYSTEM_ID, "6666666666ICBC");
		if(critics == null){
			critics = new HashMap();
			critics.put(MongoData.SYSTEM_ID, "6666666666ICBC");
			critics.put(MongoData.ANNUALSELECTION_CITY_OF_LOVE, 0);
			critics.put(MongoData.ANNUALSELECTION_SAME_ART, 0);
			critics.put(MongoData.ANNUALSELECTION_IDOL_INVINCIBLE, 0);
			critics.put(MongoData.ANNUALSELECTION_REJUVENATE, 0);
			critics.put(MongoData.ANNUALSELECTION_ANNUAL_JUXIAN, 0);
			mongoService.saveOrUpdateMap(critics, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		}
		model.put("critics", critics);
		
		Map params = new HashMap();
		params.put("type", MongoData.ANNUALSELECTION);
		params.put("tag", MongoData.ANNUALSELECTION_CITY_OF_LOVE);
		Map thirteenMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.ANNUALSELECTION_SAME_ART);
		Map pomMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.ANNUALSELECTION_IDOL_INVINCIBLE);
		Map treeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.ANNUALSELECTION_REJUVENATE);
		Map partyMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		params.put("tag", MongoData.ANNUALSELECTION_ANNUAL_JUXIAN);
		Map phoneMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("thirteenMap", thirteenMap);
		model.put("pomMap", pomMap);
		model.put("treeMap", treeMap);
		model.put("partyMap", partyMap);
		model.put("phoneMap", phoneMap);
		
		return "admin/newsubject/annualSelectionIndex.vm";
	}
	@RequestMapping("/admin/newsubject/updateNum.xhtml")
	public String updateNum(String id, String type, int num, ModelMap model){
		if(StringUtils.isBlank(id) || StringUtils.isBlank(type)) return showJsonError(model,"参数错误！");
		Map map = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, MongoData.SYSTEM_ID, id);
		if(map.isEmpty())return showJsonError(model,"未找到此数据！");
		map.put(type, num);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
		return showJsonSuccess(model);
	}
	//图片
	@RequestMapping("/admin/newsubject/commonPictrue.xhtml")
	public String commonPictrue(String type, String tag, ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		List picList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true);
		model.put("picList", picList);
		return "admin/newsubject/commonPictrue.vm";
	}
	@RequestMapping("/admin/newsubject/commonAddPictrue.xhtml")
	public String commonAddPictrue(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map pic = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
			model.put("pic", pic);
		}
		return "admin/newsubject/commonAddPictrue.vm";
	}
	@RequestMapping("/admin/newsubject/savePictrue.xhtml")
	public String savePictrue(String id, String type, String tag, String title, String content, String content2, String newslink, String newslogo, ModelMap model){
		Map pic = new HashMap();
		if(StringUtils.isNotBlank(id)){
			pic = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
		}else{
			pic.put(MongoData.SYSTEM_ID, System.currentTimeMillis()+StringUtil.getRandomString(5));
			pic.put(MongoData.ACTION_ORDERNUM, 0);
		}
		pic.put(MongoData.ACTION_TITLE, title);
		pic.put(MongoData.ACTION_CONTENT, content);
		if(StringUtils.isNotBlank(content2))pic.put(MongoData.ACTION_CONTENT2, content2);
		pic.put(MongoData.ACTION_NEWSLOGO, newslogo);
		pic.put(MongoData.ACTION_NEWSLINK, newslink);
		pic.put(MongoData.ACTION_ADDTIME, System.currentTimeMillis());
		pic.put(MongoData.ACTION_TYPE, type);
		pic.put(MongoData.ACTION_TAG, tag);
		mongoService.saveOrUpdateMap(pic, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/newsubject/deletePictrue.xhtml")
	public String deletePictrue(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			mongoService.removeObjectById(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/newsubject/changeOrdernumPictrue.xhtml")
	public String changeOrdernumPictrue(String id, Integer ordernum, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map pic = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_PICTRUE, MongoData.SYSTEM_ID, id);
			pic.put(MongoData.ACTION_ORDERNUM, ordernum);
			mongoService.saveOrUpdateMap(pic, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_PICTRUE);
		}
		return showJsonSuccess(model);
	}
	//年度精选end
	
	//异星战场
	@RequestMapping("/admin/newsubject/alienBattlefield.xhtml")
	public String alienBattlefield(String type, String tag, ModelMap model){
		String url = "admin/newsubject/alienBattlefield.vm";
		if(StringUtils.isBlank(type)) return url;
		if(StringUtils.isBlank(tag)) tag = "yxzc";
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		List<Map> memberList = mongoService.find(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		model.put("memberList", memberList);
		return url;
	}
	
	//百事可乐合作专题
	@RequestMapping("/admin/newsubject/pepsiCola.xhtml")
	public String pepsiCola(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TAG, MongoData.PEPSICOLA);
		Map whiteActivity = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("whiteActivity", whiteActivity);
		return "admin/newsubject/pepsiColaIndex.vm";
	}
}
