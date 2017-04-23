package com.gewara.web.action.admin.ajax;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.DiaryConstant;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Flag;
import com.gewara.constant.PointConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.json.MemberSign;
import com.gewara.json.MemberStats;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.DiaryComment;
import com.gewara.model.bbs.DiaryHist;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.City;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Line2Station;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.DiscountInfo;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeSale;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.sport.SportPrice;
import com.gewara.model.sport.SportPriceTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberPicture;
import com.gewara.model.user.SysMessageAction;
import com.gewara.mongo.MongoService;
import com.gewara.service.DataCheckService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.member.PointService;
import com.gewara.service.movie.MCPService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.MemberCountService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.MapApiUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.bbs.ReComment;
import com.gewara.xmlbind.gym.RemoteCoach;
import com.gewara.xmlbind.gym.RemoteGym;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since Apr 14, 2008 AT 6:32:00 PM
 */
@Controller
public class AdminCommonAjaxController extends BaseAdminController {
	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;

	@Autowired
	@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;

	public void setGewaPicService(GewaPicService gewaPicService) {
		this.gewaPicService = gewaPicService;
	}

	@Autowired
	@Qualifier("config")
	private Config config;

	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired
	@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired
	@Qualifier("blogService")
	private BlogService blogService;
	@Autowired
	@Qualifier("shareService")
	private ShareService shareService;

	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	@Qualifier("pointService")
	private PointService pointService;

	public void setPointService(PointService pointService) {
		this.pointService = pointService;
	}

	@Autowired
	@Qualifier("mcpService")
	private MCPService mcpService;

	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}

	@Autowired
	@Qualifier("dataCheckService")
	private DataCheckService dataCheckService;

	public void setDataCheckService(DataCheckService dataCheckService) {
		this.dataCheckService = dataCheckService;
	}

	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;

	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@RequestMapping("/admin/common/ajax/checkname.xhtml")
	public String checkname(String checkItem, String name, ModelMap model) {
		boolean b = dataCheckService.checkname(checkItem, name);
		if (b)
			showJsonError(model, "该名称已经存在！");
		return showJsonSuccess(model);
	}

	// 记录删除记录的后台人员信息
	private void deleteLog(String status, Long id, String type, String obj) {
		if (Status.N_DELETE.equals(status)) {
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "管理员:" + getLogonUser().getNickname() + " 删除编号为：" + id + ",类型为：" + type
					+ ",内容为：" + obj);
		}
	}

	@RequestMapping("/admin/common/ajax/getCityByProvinceCode.xhtml")
	public String getCityByProvinceCode(String provinceCode, ModelMap model) {
		List<City> cityList = placeService.getCityByProvinceCode(provinceCode);
		List<Map> provinceMap = BeanUtil.getBeanMapList(cityList, false);
		Map result = new HashMap();
		result.put("provinceMap", provinceMap);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/getCountyByCityCode.xhtml")
	public String getCountyByCityCode(String cityCode, ModelMap model) {
		List<County> countyList = placeService.getCountyByCityCode(cityCode);
		List<Map> countyMap = BeanUtil.getBeanMapList(countyList, false);
		Map result = new HashMap();
		result.put("countyMap", countyMap);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/getIndexareaByCountyCode.xhtml")
	public String getIndexareaByCountyCode(String countycode, ModelMap model) {
		List<Indexarea> result = placeService.getIndexareaByCountyCode(countycode);
		Map m = new HashMap();
		m.put("indexareaList", BeanUtil.getBeanMapList(result, false));
		return showJsonSuccess(model, m);
	}

	@RequestMapping("/admin/common/getCounty.xhtml")
	public String getCounty(String countycode, ModelMap model) {
		County county = daoService.getObject(County.class, countycode);
		if (county == null)
			return showJsonError(model, "该区县不存在或被删除！");
		Map result = BeanUtil.getBeanMap(county);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/saveOrUpdateCounty.xhtml")
	public String saveOrUpdateCounty(String countycode, HttpServletRequest request, ModelMap model) {
		County county = daoService.getObject(County.class, countycode);
		if (county == null)
			return showJsonError(model, "该区县不存在或被删除！");
		BindUtils.bindData(county, request.getParameterMap());
		if (StringUtils.isBlank(county.getCitycode()))
			return showJsonError(model, "区县的市级代码不能为空！");
		if (StringUtils.isBlank(county.getCountyname()))
			return showJsonError(model, "区县名称不能为空！");
		placeService.updateCounty(county);
		return showJsonSuccess(model);
	}

	// ~~~~~~~~~~~~~~~~~优惠信息~~~~~~~~~~~~~~~~~~
	@RequestMapping("/admin/common/ajax/getDiscountInfoById.xhtml")
	public String getDiscountInfoById(Long discountInfoId, ModelMap model) {
		DiscountInfo discountInfo = daoService.getObject(DiscountInfo.class, discountInfoId);
		if (discountInfo == null)
			return showJsonError(model, "数据不存在！");
		Map result = BeanUtil.getBeanMap(discountInfo);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/saveOrUpdateDiscountInfo.xhtml")
	public String saveOrUpdateDiscountInfo(Long id, HttpServletRequest request, ModelMap model) {
		DiscountInfo discountInfo = new DiscountInfo("");
		if (id != null)
			discountInfo = daoService.getObject(DiscountInfo.class, id);
		ChangeEntry changeEntry = new ChangeEntry(discountInfo);
		BindUtils.bindData(discountInfo, request.getParameterMap());
		if (StringUtil.getByteLength(discountInfo.getContent()) > 20000)
			return showJsonError(model, "内容字符过长！");
		if (StringUtil.getByteLength(discountInfo.getTitle()) > 60)
			return showJsonError(model, "标题字符过长！");
		daoService.saveObject(discountInfo);
		monitorService.saveChangeLog(getLogonUser().getId(), DiscountInfo.class, discountInfo.getId(), changeEntry.getChangeMap(discountInfo));
		Map result = BeanUtil.getBeanMap(discountInfo);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/removeDiscountInfoById.xhtml")
	public String removeDiscountInfoById(Long discountInfoId, ModelMap model) {
		DiscountInfo discountInfo = daoService.getObject(DiscountInfo.class, discountInfoId);
		if (discountInfo == null)
			return showJsonError(model, "数据不存在！");
		daoService.removeObject(discountInfo);
		monitorService.saveDelLog(getLogonUser().getId(), discountInfo.getId(), discountInfo);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/downloadP.xhtml")
	public void downloadP(String picpath, HttpServletResponse response) {
		download("jpg", response);
		try {
			URL url = new URL(picpath);
			ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
			ImageIO.write(ImageIO.read(url), "jpeg", jpegOutputStream);

			ServletOutputStream responseOutputStream = response.getOutputStream();
			responseOutputStream.write(jpegOutputStream.toByteArray());
			responseOutputStream.flush();
			responseOutputStream.close();
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e));
		} finally {
		}
	}

	@RequestMapping("/admin/common/ajax/removeComment.xhtml")
	public String removeComment(Long commentId, String reason, String reasonDetail, ModelMap model) {
		if (!reason.equals("5")) {
			reason = ServiceHelper.getReason(reason);
		} else {
			reason = reasonDetail;
		}
		Comment comment = commentService.getCommentById(commentId);
		if (comment != null) {
			comment.setStatus(Status.N_DELETE);
			commentService.updateComment(comment);
			SysMessageAction sysMessage = new SysMessageAction(SysAction.STATUS_RESULT);
			sysMessage.setFrommemberid(1l);
			String value = "";
			if ("gym".equals(comment.getTag())) {
				ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(comment.getRelatedid(), true);
				if(code.isSuccess()){				
					RemoteGym gym = code.getRetval();
					value = gym.getName().substring(0, gym.getName().length() > 5 ? 5 : gym.getName().length());
				}
			} else if ("movie".equals(comment.getTag())) {
				Movie movie = daoService.getObject(Movie.class, comment.getRelatedid());
				if (movie != null) {
					value = movie.getName().substring(0, movie.getName().length() > 5 ? 5 : movie.getName().length());
				}
			} else if ("sport".equals(comment.getTag())) {
				Sport sport = daoService.getObject(Sport.class, comment.getRelatedid());
				value = sport.getName().substring(0, sport.getName().length() > 5 ? 5 : sport.getName().length());
			} else if ("gymnews".equals(comment.getTag()) || "cinemanews".equals(comment.getTag()) || "ktvnews".equals(comment.getTag())
					|| "movienews".equals(comment.getTag()) || "sportnews".equals(comment.getTag()) || "barnews".equals(comment.getTag())) {
				News news = daoService.getObject(News.class, comment.getRelatedid());
				value = news.getTitle().substring(0, news.getTitle().length() > 5 ? 5 : news.getTitle().length());
			} else if ("gymcoach".equals(comment.getTag())) {
				ErrorCode<RemoteCoach> code = synchGymService.getRemoteCoach(comment.getRelatedid(), true);
				if(code.isSuccess()){		
					RemoteCoach gymcoach = code.getRetval();
					value = gymcoach.getName().substring(0, gymcoach.getName().length() > 5 ? 5 : gymcoach.getName().length());
				}
			} else if ("cinema".equals(comment.getTag())) {
				Cinema cinema = daoService.getObject(Cinema.class, comment.getRelatedid());
				value = cinema.getName().substring(0, cinema.getName().length() > 5 ? 5 : cinema.getName().length());
			}
			sysMessage.setBody("您对【" + value + "...】的回复内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysMessage.setTomemberid(comment.getMemberid());
			daoService.saveObject(sysMessage);
			return showJsonSuccess(model);
		}
		return showJsonError(model, "数据不存在！");
	}

	@RequestMapping("/admin/common/ajax/saveMap.xhtml")
	public String saveMap(String tag, Long relatedid, String pointx, String pointy, ModelMap model) {
		BaseInfo info = (BaseInfo) relateService.getRelatedObject(tag, relatedid);
		if (info == null)
			return showJsonError(model, "该对象不存在！");
		if(StringUtils.isNotBlank(pointx) && StringUtils.isNotBlank(pointy)){
			try {
				Double.parseDouble(pointx);
				Double.parseDouble(pointy);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return showJsonError(model, "坐标必须为数字！");
			}
		}
		info.setPointx(pointx);
		info.setPointy(pointy);
		daoService.saveObject(info);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/ajax/saveMapPicture.xhtml")
	public String saveMapPicture(String tag, Long relatedid, String mapname, ModelMap model) {
		String img = tag + relatedid + ".gif";
		File file = new File(gewaPicService.getTempFilePath(img));
		try {
			boolean result = HttpUtils.getUrlAsInputStream(mapname, null, new HttpUtils.FileRequestCallback(file));
			if (!result)
				return showJsonError(model, "生成图片失败！");
			gewaPicService.addToRemoteFile(file, getLogonUser().getId(), tag, relatedid, "/images/map/" + img);
		} catch (Exception e) {
			dbLogger.error("生成图片失败:" + StringUtil.getExceptionTrace(e) + "\n mapname:" + mapname);
			return showJsonError(model, "生成图片失败！");
		}
		return showJsonSuccess(model);
	}
	/**
	 * 百度地图生成图片的方法
	 * 
	 * @param tag
	 * @param relatedid
	 * @param bpointx
	 * @param bpointy
	 * @param mapname
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/common/ajax/saveBMap.xhtml")
	public String saveBMap(String tag, Long relatedid, String bpointx, String bpointy, String mapname, ModelMap model) {
		if(StringUtils.isNotBlank(bpointx) && StringUtils.isNotBlank(bpointy)){
			try {
				Double.parseDouble(bpointx);
				Double.parseDouble(bpointy);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return showJsonError(model, "坐标必须为数字！");
			}
		}
		String img = tag + relatedid + "bd.gif";
		File file = new File(gewaPicService.getTempFilePath(img));
		try {
			boolean result = HttpUtils.getUrlAsInputStream(mapname, null, new HttpUtils.FileRequestCallback(file));
			if (!result)
				return showJsonError(model, "生成图片失败！");
			gewaPicService.addToRemoteFile(file, getLogonUser().getId(), tag, relatedid, "/images/map/" + img);
		} catch (Exception e) {
			dbLogger.error("生成图片失败:" + StringUtil.getExceptionTrace(e) + "\n mapname:" + mapname);
			return showJsonError(model, "生成图片失败！");
		}
		BaseInfo info = (BaseInfo) relateService.getRelatedObject(tag, relatedid);
		info.setBpointx(bpointx);
		info.setBpointy(bpointy);
		daoService.saveObject(info);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/modBBSStatus.xhtml")
	public String modBBSStatus(String tag, Long id, String status, String reason, String reasonDetail, ModelMap model) {
		User user = getLogonUser();
		if ("N_DELETE".equals(status) || "D".equals(status)) { // "D":手机短信
			if (!reason.equals("5")) {
				reason = ServiceHelper.getReason(reason);
			} else {
				reason = reasonDetail;
			}
		}
		SysMessageAction sysmessage = new SysMessageAction(SysAction.STATUS_RESULT);
		sysmessage.setFrommemberid(1l);
		if ("diary".equals(tag) || "ADdiary".equals(tag)) {
			DiaryBase diary = diaryService.getDiaryBase(id);
			ChangeEntry changeEntry = new ChangeEntry(diary);
			diary.setStatus(status);
			if ("N_DELETE".equals(status)) {
				deleteLog(status, diary.getId(), tag, diary.getSubject());
				sysmessage.setBody("您发表的【" + diary.getSubject().substring(0, diary.getSubject().length() > 5 ? 5 : diary.getSubject().length())
						+ "...】内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysmessage.setTomemberid(diary.getMemberid());
				daoService.saveObject(sysmessage);
			}
			daoService.saveObject(diary);
			searchService.pushSearchKey(diary);//更新索引至索引服务器
			monitorService.saveChangeLog(user.getId(), Diary.class, diary.getId(), changeEntry.getChangeMap(diary));
			if (StringUtils.contains(diary.getStatus(), Status.Y) && status.equals(Status.N_DELETE)) {
				// 给帖子数-1
				memberCountService.updateMemberCount(diary.getId(), MemberStats.FIELD_DIARYCOUNT, 1, false);
				// 给给帖子数-1
			}
			if (status.equals(Status.Y_NEW)) {
				// 给帖子数+1
				memberCountService.updateMemberCount(diary.getId(), MemberStats.FIELD_DIARYCOUNT, 1, true);
				// 给给帖子数+1
			}
		} else if ("diarycomment".equals(tag)) {
			DiaryComment dc = daoService.getObject(DiaryComment.class, id);
			ChangeEntry changeEntry = new ChangeEntry(dc);
			DiaryBase diary = diaryService.getDiaryBase(dc.getDiaryid());
			if (diary == null) {
				diary = daoService.getObject(DiaryHist.class, id);
			}
			dc.setStatus(status);
			if ("N_DELETE".equals(status)) {
				deleteLog(status, dc.getId(), tag, dc.getBody());
				sysmessage.setBody("您对【" + diary.getSubject().substring(0, diary.getSubject().length() > 5 ? 5 : diary.getSubject().length())
						+ "...】的回复内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysmessage.setTomemberid(dc.getMemberid());
				daoService.saveObject(sysmessage);
			}
			daoService.saveObject(dc);
			monitorService.saveChangeLog(user.getId(), DiaryComment.class, dc.getId(), changeEntry.getChangeMap(dc));
		} else if ("gewaquestion".equals(tag) || "nightquestion".equals(tag)) {
			GewaQuestion question = daoService.getObject(GewaQuestion.class, id);
			ChangeEntry changeEntry = new ChangeEntry(question);
			question.setStatus(status);
			if ("N_DELETE".equals(status)) {
				deleteLog(status, question.getId(), tag, question.getContent());
				sysmessage.setBody("您发表的【" + question.getTitle().substring(0, question.getTitle().length() > 5 ? 5 : question.getTitle().length())
						+ "...】内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）向管理员申诉。");
				sysmessage.setTomemberid(question.getMemberid());
				daoService.saveObject(sysmessage);
				memberService.addExpForMember(question.getMemberid(), -ExpGrade.EXP_DIARY_ADD);
			}
			daoService.saveObject(question);
			searchService.pushSearchKey(question);//更新索引至索引服务器
			monitorService.saveChangeLog(user.getId(), GewaQuestion.class, question.getId(), changeEntry.getChangeMap(question));
		} else if ("gewaanswer".equals(tag)) {
			GewaAnswer answer = daoService.getObject(GewaAnswer.class, id);
			ChangeEntry changeEntry = new ChangeEntry(answer);
			GewaQuestion question = daoService.getObject(GewaQuestion.class, answer.getQuestionid());
			answer.setStatus(status);
			if ("N_DELETE".equals(status)) {
				deleteLog(status, answer.getId(), tag, answer.getContent());
				sysmessage.setBody("您对【" + question.getTitle().substring(0, question.getTitle().length() > 5 ? 5 : question.getTitle().length())
						+ "...】的回复内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysmessage.setTomemberid(answer.getMemberid());
				daoService.saveObject(sysmessage);
				memberService.addExpForMember(answer.getMemberid(), -ExpGrade.EXP_ANSWER_DEL_COMMON);
			}
			daoService.saveObject(answer);
			monitorService.saveChangeLog(user.getId(), GewaAnswer.class, answer.getId(), changeEntry.getChangeMap(answer));
		} else if ("comment".equals(tag)) {
			Comment comment = commentService.getCommentById(id);
			comment.setStatus(status);
			if (Status.N_DELETE.equals(status)) {
				String value = "";
				if (StringUtils.contains(comment.getTag(), "news")) {
					News news = daoService.getObject(News.class, comment.getRelatedid());
					value = news.getTitle().substring(0, news.getTitle().length() > 5 ? 5 : news.getTitle().length());
				} else {
					Object obj = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
					if (obj != null)
						value = StringUtil.enabbr((String) BeanUtil.get(obj, "name"), 20);
				}
				sysmessage.setBody("您对【" + value + "...】的点评涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysmessage.setTomemberid(comment.getMemberid());
				daoService.saveObject(sysmessage);
			}
			deleteLog(status, comment.getId(), comment.getTag() + "comment", comment.getBody());
			commentService.updateComment(comment);
		} else if ("mobileMsg".equals(tag)) {
			SMSRecord smsrecord = daoService.getObject(SMSRecord.class, id);
			ChangeEntry changeEntry = new ChangeEntry(smsrecord);
			deleteLog(status, smsrecord.getId(), tag, smsrecord.getContent());
			smsrecord.setStatus(status);
			if ("D".equals(status)) {
				sysmessage.setBody("你的生活日程【" + smsrecord.getContent() + "...】<br/>内容涉及【" + reason
						+ "】,没有通过审核,此条短信没有发出,<br/>如有任何疑问，通过邮件（gewara@gewara.com）向管理员申诉。");
				sysmessage.setTomemberid(smsrecord.getMemberid());
				daoService.saveObject(sysmessage);
			}
			daoService.saveObject(smsrecord);
			monitorService.saveChangeLog(user.getId(), SMSRecord.class, smsrecord.getId(), changeEntry.getChangeMap(smsrecord));
		} else if ("microcomment".equals(tag)) {
			Comment comment = commentService.getCommentById(id);
			deleteLog(status, comment.getId(), tag, comment.getBody());
			comment.setStatus(status);
			String body = comment.getBody();
			sysmessage.setBody("您发表的哇啦 " + (body == null ? "" : "【" + body.substring(0, body.length() > 5 ? 5 : body.length()) + "...】") + "涉及【"
					+ reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysmessage.setTomemberid(comment.getMemberid());
			daoService.saveObject(sysmessage);
			commentService.updateComment(comment);
			if (StringUtils.equals(status, Status.N_DELETE)) {
				// 给wala数-1
				memberCountService.updateMemberCount(comment.getMemberid(), MemberStats.FIELD_COMMENTCOUNT, 1, false);
				// 给哇啦数-1
			}
			if (StringUtils.equals(status, Status.Y_NEW)) {
				// 给wala数+1
				memberCountService.updateMemberCount(comment.getMemberid(), MemberStats.FIELD_COMMENTCOUNT, 1, true);
				// 给哇啦数+1
			}
		} else if ("microrecomment".equals(tag)) {
			ReComment recomment = walaApiService.getReCommentById(id);
			ChangeEntry changeEntry = new ChangeEntry(recomment);
			deleteLog(status, recomment.getId(), tag, recomment.getBody());
			recomment.setStatus(status);
			String body = recomment.getBody();
			sysmessage.setBody("您发表的哇啦回复 " + (body == null ? "" : "【" + body.substring(0, body.length() > 5 ? 5 : body.length()) + "...】") + "涉及【"
					+ reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
			sysmessage.setTomemberid(recomment.getMemberid());
			daoService.saveObject(sysmessage);
			walaApiService.updateReComment(recomment);
			monitorService.saveChangeLog(user.getId(), ReComment.class, recomment.getId(), changeEntry.getChangeMap(recomment));
		}else if ("Commu".equals(tag)) {
			Commu commu = daoService.getObject(Commu.class, id);
			ChangeEntry changeEntry = new ChangeEntry(commu);
			if (StringUtils.contains(commu.getStatus(), Status.Y) && status.equals(Status.N_DELETE)) {
				// 给帖子数-1
				memberCountService.updateMemberCount(commu.getId(), MemberStats.FIELD_COMMUCOUNT, 1, false);
				// 给给帖子数-1
			}
			if (status.equals(Status.Y)) {
				// 给帖子数+1
				memberCountService.updateMemberCount(commu.getId(), MemberStats.FIELD_COMMUCOUNT, 1, true);
				// 给给帖子数+1
			}
			commu.setStatus(status);
			if ("N_DELETE".equals(status)) {
				deleteLog(status, commu.getId(), tag, commu.getName());
				sysmessage.setBody("您发表的【" + commu.getName().substring(0, commu.getName().length() > 5 ? 5 : commu.getName().length())
						+ "...】内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
				sysmessage.setTomemberid(commu.getAdminid());
				daoService.saveObject(sysmessage);
			}
			daoService.saveObject(commu);
			searchService.pushSearchKey(commu);//更新索引至索引服务器
			monitorService.saveChangeLog(user.getId(), Commu.class, commu.getId(), changeEntry.getChangeMap(commu));
		}
		return showJsonSuccess(model);
	}

	/**
	 * 将电影帖子改为影评
	 * 
	 * @param diaryId
	 * @return
	 */
	@RequestMapping("/admin/common/ajax/changeToDiary.xhtml")
	public String changeToDiary(Long diaryId, ModelMap model) {
		DiaryBase diary = diaryService.getDiaryBase(diaryId);
		if (diary == null)
			return showJsonError(model, "该贴子不存在或被删除！");
		diary.setType(DiaryConstant.DIARY_TYPE_COMMENT);
		daoService.saveObject(diary);
		return showJsonSuccess(model);
	}

	/**
	 * 将电影帖子改为影评
	 * 
	 * @param diaryId
	 * @return
	 */
	@RequestMapping("/admin/common/ajax/changeToTopic.xhtml")
	public String changeToTopic(Long topicId, ModelMap model) {
		DiaryBase diary = diaryService.getDiaryBase(topicId);
		if (DiaryConstant.DIARY_TYPE_TOPIC_VOTE.equals(diary.getType()))
			return showJsonSuccess(model); // 投票帖子，不改
		diary.setType(DiaryConstant.DIARY_TYPE_TOPIC_DIARY);
		daoService.saveObject(diary);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/saveGeneralInfo.xhtml")
	public String saveGeneralInfo(Long id, String tag, HttpServletRequest request, ModelMap model) {
		GewaCommend gi = new GewaCommend(SignName.TAG_QACOMMENDPIC);
		if (id != null)
			gi = daoService.getObject(GewaCommend.class, id);
		gi.setTag(tag);
		BindUtils.bindData(gi, request.getParameterMap());
		daoService.saveObject(gi);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/updateGiUpdatetime.xhtml")
	public String updateGiUpdatetime(Long id, ModelMap model) {
		GewaCommend gi = daoService.getObject(GewaCommend.class, id);
		gi.setOrdernum(1);
		daoService.saveObject(gi);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/removeGeneralInfo.xhtml")
	public String removeGeneralInfo(Long id, ModelMap model) {
		GewaCommend gi = daoService.getObject(GewaCommend.class, id);
		daoService.removeObject(gi);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/getGeneralInfo.xhtml")
	public String getGeneralInfo(Long id, ModelMap model) {
		GewaCommend commend = daoService.getObject(GewaCommend.class, id);
		Map result = BeanUtil.getBeanMap(commend);
		return showJsonSuccess(model, result);
	}

	// =======================
	@RequestMapping("/admin/common/ajax/addExPointByDiary.xhtml")
	public String addExPointByDiary(String pointtype, HttpServletRequest request, ModelMap model) {
		Map<String, String[]> dataMap = request.getParameterMap();
		Set<String> keySet = dataMap.keySet();
		String pointType = "";
		String reason = "";
		if (StringUtils.equals(TagConstant.TAG_MOVIE, pointtype)) {
			pointType = PointConstant.FREEBACK_DIARYMOVIE;
			reason = "发表影片评论";
		} else if (StringUtils.equals(DramaConstant.TYPE_DRAMA, pointtype)) {
			pointType = PointConstant.FREEBACK_DIARYDRAMA;
			reason = "发表话剧评论";
		} else {
			return forwardMessage(model, "类型错误！");
		}
		User user = getLogonUser();
		for (String string : keySet) {
			if (StringUtils.contains(string, "pointtype"))
				continue;
			Long did = Long.valueOf(string);
			DiaryBase diary = diaryService.getDiaryBase(did);
			if (diary != null) {
				String content = null;
				String value = ServiceHelper.get(dataMap, string);
				if (StringUtils.isNotBlank(value)) {
					String[] values = StringUtils.split(value, ",");
					if (!ArrayUtils.isEmpty(values)) {
						if (!values[0].matches("[0-9]+"))
							return showJsonError(model, "积分设置异常！");
						Integer pointValue = Integer.valueOf(values[0]);
						SysMessageAction sysmessage = new SysMessageAction(SysAction.STATUS_RESULT);
						sysmessage.setFrommemberid(1l);
						sysmessage.setTomemberid(diary.getMemberid());
						if (pointValue > 0 && pointValue <= 1000) {
							if (values.length > 1) {
								content = values[1];
							}
							reason = StringUtils.isNotBlank(content) ? content : reason;
							pointType = StringUtils.isNotBlank(content) ? PointConstant.TAG_CONTENT : pointType;
							if (TagConstant.TAG_MOVIE.equals(diary.getCategory()) && diary.getCategoryid() != null) {
								Movie movie = daoService.getObject(Movie.class, diary.getCategoryid());
								sysmessage.setBody("你对《" + movie.getName() + "》影片的评论太精彩了，<br/>因此获得格瓦拉编辑的推荐并获赠" + pointValue + "个积分的奖励，请继续努力哦~~");
							} else if (DramaConstant.TYPE_DRAMA.equals(diary.getCategory()) && diary.getCategoryid() != null) {
								Drama drama = daoService.getObject(Drama.class, diary.getCategoryid());
								sysmessage.setBody("你对《" + drama.getName() + "》话剧的评论太精彩了，<br/>因此获得格瓦拉编辑的推荐并获赠" + pointValue + "个积分的奖励，请继续努力哦~~");
							}
							diary.addFlag(VmUtils.appendString(diary.getFlag(), ",", Flag.POINT_ADDED));
							Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(diary.getOtherinfo());
							otherinfoMap.put("point", String.valueOf(pointValue));
							otherinfoMap.put("tag", pointType);
							otherinfoMap.put("reason", reason);
							diary.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
							daoService.saveObject(diary);
							pointService.addPointInfo(diary.getMemberid(), pointValue, reason, pointType, diary.getId(), user.getId());
						} else if (pointValue > 1000) {
							return showJsonSuccess(model, "赠送积分不能大于1000!");
						} else {// TODO:此处的条件怎么产生？？？？
							diary.addFlag(VmUtils.appendString(diary.getFlag(), ",", Flag.POINT_IGNORE));
							if (TagConstant.TAG_MOVIE.equals(diary.getCategory()) && diary.getCategoryid() != null) {
								Movie movie = daoService.getObject(Movie.class, diary.getCategoryid());
								sysmessage.setBody("你对《" + movie.getName() + "》影片的点评没有获得格瓦拉编辑的推荐，不要灰心，再接再厉哦~~");
							} else if (DramaConstant.TYPE_DRAMA.equals(diary.getCategory()) && diary.getCategoryid() != null) {
								Drama drama = daoService.getObject(Drama.class, diary.getCategoryid());
								sysmessage.setBody("你对《" + drama.getName() + "》话剧的点评没有获得格瓦拉编辑的推荐，不要灰心，再接再厉哦~~");
							}
							daoService.saveObject(diary);
						}
						daoService.saveObject(sysmessage);
					}
				}
			}
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/addExPointByComment.xhtml")
	public String addExPointByComment(String pointtype, HttpServletRequest request, ModelMap model) {
		Map<String, String[]> dataMap = request.getParameterMap();
		Set<String> keySet = dataMap.keySet();
		String pointType = "";
		String reason = "";
		if (StringUtils.equals("cinema", pointtype)) {
			pointType = PointConstant.FREEBACK_COMMENTCINEMA;
			reason = "发表影院点评";
		} else if (StringUtils.equals(Theatre.TAG_THEATRE, pointtype)) {
			pointType = PointConstant.FREEBACK_COMMENTTHEATRE;
			reason = "发表剧院点评";
		} else {
			return forwardMessage(model, "类型错误！");
		}
		for (String key : keySet) {
			if ("pointtype".equals(key))
				continue;
			Long did = Long.valueOf(key);
			Integer pointValue = Integer.valueOf(ServiceHelper.get(dataMap, key));
			Comment comment = commentService.getCommentById(did);
			if (comment != null) {
				SysMessageAction sysmessage = new SysMessageAction(SysAction.STATUS_RESULT);
				sysmessage.setFrommemberid(1l);
				sysmessage.setTomemberid(comment.getMemberid());
				if (pointValue > 0 && pointValue <= 1000) {
					User user = getLogonUser();
					comment.addFlag(VmUtils.appendString(comment.getFlag(), ",", Flag.POINT_ADDED));
					if ("cinema".equals(comment.getTag())) {
						Cinema cinema = daoService.getObject(Cinema.class, comment.getRelatedid());
						if (cinema != null) {
							sysmessage.setBody("你对\"" + cinema.getName() + "\"影院的点评太精彩了，<br/>因此获得gewara" + pointValue + "个积分的奖励，请继续努力哦~~");
						}
					} else if (Theatre.TAG_THEATRE.equals(comment.getTag())) {
						Theatre theatre = daoService.getObject(Theatre.class, comment.getRelatedid());
						if (theatre != null) {
							sysmessage.setBody("你对\"" + theatre.getName() + "\"剧院的点评太精彩了，<br/>因此获得gewara" + pointValue + "个积分的奖励，请继续努力哦~~");
						}
					}
					Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(comment.getOtherinfo());
					otherinfoMap.put("point", String.valueOf(pointValue));
					otherinfoMap.put("tag", pointType);
					otherinfoMap.put("reason", reason);

					comment.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
					commentService.updateComment(comment);
					pointService.addPointInfo(comment.getMemberid(), pointValue, reason, pointType, comment.getId(), user.getId());
				} else if (pointValue > 1000) {
					return showJsonSuccess(model, "赠送积分不能大于1000!");
				} else {
					comment.addFlag(VmUtils.appendString(comment.getFlag(), ",", Flag.POINT_IGNORE));
					if ("cinema".equals(comment.getTag())) {
						Cinema cinema = daoService.getObject(Cinema.class, comment.getRelatedid());
						if (cinema != null) {
							sysmessage.setBody("你对\"" + cinema.getName() + "\"影院的点评没有获得gewara积分，不要灰心，再接再厉哦~~");
						}
					} else if (Theatre.TAG_THEATRE.equals(comment.getTag())) {
						Theatre theatre = daoService.getObject(Theatre.class, comment.getRelatedid());
						if (theatre != null) {
							sysmessage.setBody("你对\"" + theatre.getName() + "\"剧院的点评没有获得gewara积分，不要灰心，再接再厉哦~~");
						}
					}
					commentService.updateComment(comment);
				}
				daoService.saveObject(sysmessage);
			}
		}
		return showJsonSuccess(model);
	}
	private List<String> autoMovieIndex = Arrays.asList(new String[]{SignName.AUTO_MOVIEINDEX_ACTIVITY,SignName.AUTO_MOVIEINDEX_MOVIE});
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~推荐~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	@RequestMapping("/admin/common/ajax/saveGewaCommend.xhtml")
	public String saveGewaCommend(Long id, HttpServletRequest request, ModelMap model, Long spparentid, String comment) {
		GewaCommend gc = new GewaCommend("");
		Map dataMap = request.getParameterMap();
		String citycode = ServiceHelper.get(dataMap, "citycode");
		if(autoMovieIndex.contains(ServiceHelper.get(dataMap, "signname"))){
			citycode = "000000";
		}
		if (id != null) {
			gc = daoService.getObject(GewaCommend.class, id);
			if (StringUtils.isBlank(citycode)) {
				citycode = gc.getCitycode();
			}
		} else {
			if (StringUtils.isBlank(citycode)) {
				citycode = getAdminCitycode(request);
			}
		}
		String tag = "";
		BindUtils.bindData(gc, dataMap);
		if (gc.getSignname().equals(SignName.SPORT_DETAIL_CEPING)) {
			News news = daoService.getObject(News.class, gc.getRelatedid());
			gc.setParentid(spparentid);
			gc.setTitle(news.getTitle());
		}
		if (gc.getRelatedid() != null && StringUtils.isNotBlank(gc.getTag())) {
			if (gc.getTag().startsWith("news")) {
				tag = "news";
			} else if (gc.getTag().startsWith("diary")) {
				tag = TagConstant.TAG_DIARY;
			} else if (gc.getTag().startsWith("movie")) {
				tag = TagConstant.TAG_MOVIE;
			} else if (gc.getTag().startsWith("sportitemvenue")) {
				tag = "sport";
				gc.setTag("sport");
			} else {
				tag = gc.getTag();
			}
			Object relate = relateService.getRelatedObject(tag, gc.getRelatedid());
			if (relate == null && StringUtils.equals(tag, TagConstant.TAG_DIARY)) {
				relate = daoService.getObject(DiaryHist.class, gc.getRelatedid());
			}else if(relate == null && StringUtils.equals(tag, "mpi")) {
				relate = daoService.getObject(MoviePlayItem.class, gc.getRelatedid());
			}
			if (relate == null)
				return showJsonError(model, "对象不存在,请核实对象标识、ID");
			if ("opentimeitem".equals(tag)){
				OpenTimeItem oti = (OpenTimeItem)relate;
				if(oti.getOtsid() == null) return showJsonError(model, "改场次不是竞价场次！");
				OpenTimeSale ots = daoService.getObject(OpenTimeSale.class, oti.getOtsid());
				if(ots == null) return showJsonError(model, "改场次不是竞价场次！");
			}
			if ("diary_commu".equals(gc.getTag())) {
				DiaryBase diary = (DiaryBase) relate;
				if (diary.getCommunityid() == null || diary.getCommunityid() == 0) {
					return showJsonError(model, "圈子热门贴必须是关联过圈子的帖子");
				} else {
					gc.setParentid(diary.getCommunityid());
				}
			}
			if ("diary_drama".equals(gc.getTag())) {
				DiaryBase diary = (DiaryBase) relate;
				if (!"drama".equals(diary.getCategory())) {
					return showJsonError(model, "必须是关联话剧的帖子!");
				} else {
					gc.setParentid(diary.getCategoryid());
				}
			}
			if (SignName.DIARY_MOVIEINDEX.equals(gc.getSignname())) {
				DiaryBase diary = (DiaryBase) relate;
				if (!("movie".equals(diary.getCategory()) && StringUtils.isNotBlank(diary.getCategoryid() + "")))
					return showJsonError(model, "必须是关联电影的帖子!");
				gc.setParentid(diary.getCategoryid());
			}
			// 活动模块
			if (/*SignName.INDEX_IFNO.equals(gc.getSignname()) ||*/ (SignName.MOVIEINDEX_ACTIVITY.equals(gc.getSignname()) || SignName.AUTO_MOVIEINDEX_ACTIVITY.equals(gc.getSignname())) && StringUtils.equals(tag, "activity")) {
				RemoteActivity activity = (RemoteActivity)relate;
				if (DateUtil.getMillTimestamp().after(activity.getActivityEndTime())) {
					return showJsonError(model, "活动时间已过期.");
				}
				gc.setStoptime(activity.getActivityEndTime());
			}
		}
		gc.setCitycode(citycode);
		daoService.saveObject(gc);
		// 同时添加一条哇啦
		if (StringUtils.equals(comment, "y")) {
			// 给哇啦数+1
			memberCountService.updateMemberCount(1L, MemberStats.FIELD_COMMENTCOUNT, 1, true);
			// 给哇啦数+1
			Diary diary = daoService.getObject(Diary.class, gc.getRelatedid());
			Movie movie = daoService.getObject(Movie.class, diary.getCategoryid());
			Member member = daoService.getObject(Member.class, diary.getMemberid());
			String linkStr = "你感兴趣 #" + movie.getName() + "#" + " 被 <a href=\"" + config.getBasePath() + "home/sns/othersPersonIndex.xhtml?memberid="
					+ member.getId() + "\" target=\"_blank\">" + member.getNickname() + "</a>评论到:";
			Map otherinfoMap = new HashMap();
			String diaryContent = blogService.getDiaryBody(diary.getId());
			if (StringUtils.length(diaryContent) > 128) {
				diaryContent = VmUtils.htmlabbr(diaryContent, 128);
				diaryContent = diaryContent + "..." + "<a href=\"" + config.getBasePath() + "blog/t" + diary.getId() + "\" target=\"_blank\">"
						+ "详情>>" + "</a>";
			}
			otherinfoMap.put("diaryContent", diaryContent);
			otherinfoMap.put("interstendComment", "interstendComment");
			String otherinfo = JsonUtils.writeObjectToJson(otherinfoMap);
			if (movie != null) {
				ErrorCode<Comment> ec = commentService.addMicroComment(daoService.getObject(Member.class, 1L), TagConstant.TAG_MOVIE_COMMENT,
						movie.getId(), linkStr, movie.getLogo(), null, null, false, null, otherinfo, null, null, WebUtils.getRemoteIp(request), null);
				if (ec.isSuccess()) {
					shareService.sendShareInfo("wala", ec.getRetval().getId(), ec.getRetval().getMemberid(), null);
				}
			}
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/removeGewaCommend.xhtml")
	public String removeGewaCommend(Long id, ModelMap model) {
		User user = getLogonUser();
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, id);
		if (gewaCommend == null)
			return showJsonError_NOT_FOUND(model);
		daoService.removeObject(gewaCommend);
		monitorService.saveDelLog(user.getId(), id, gewaCommend);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/changeGCOrderNum.xhtml")
	public String changeGCOrderNum(Long id, Integer ordernum, ModelMap model) {
		GewaCommend gc = daoService.getObject(GewaCommend.class, id);
		if (gc == null)
			return showJsonError_NOT_LOGIN(model);
		gc.setOrdernum(ordernum);
		daoService.updateObject(gc);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/changeSPTOrderNum.xhtml")
	public String changeSPTOrderNum(Long id, Integer ordernum, ModelMap model) {
		SportPriceTable spt = daoService.getObject(SportPriceTable.class, id);
		if (spt == null)
			return showJsonError_NOT_LOGIN(model);
		spt.setOrdernum(ordernum);
		daoService.updateObject(spt);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/changeSportPriceOrderNum.xhtml")
	public String changeSportPriceOrderNum(Long id, Integer ordernum, ModelMap model) {
		SportPrice spt = daoService.getObject(SportPrice.class, id);
		if (spt == null)
			return showJsonError_NOT_LOGIN(model);
		spt.setOrdernum(ordernum);
		daoService.updateObject(spt);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/changeSIOrderNum.xhtml")
	public String changeSIOrderNum(Long id, Integer ordernum, ModelMap model) {
		SportItem si = daoService.getObject(SportItem.class, id);
		if (si == null)
			return showJsonError_NOT_FOUND(model);
		si.setOrdernum(ordernum);
		daoService.updateObject(si);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/changeSIpopularIndex.xhtml")
	public String changeSIpopularIndex(Long id, Integer popularIndex, ModelMap model) {
		SportItem si = daoService.getObject(SportItem.class, id);
		if (si == null)
			return showJsonError_NOT_FOUND(model);
		si.setPopularIndex(popularIndex);
		daoService.updateObject(si);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/batchModBBSStatus.xhtml")
	public String batchModBBSStatus(String tag, String ids, String status, String reason, String reasonDetail, ModelMap model) {
		if (!"gewaquestion".equals(tag) && !"diary".equals(tag) && !"gewaanswer".equals(tag) && !"diarycomment".equals(tag)
				&& !"activity".equals(tag) && !"activitycomment".equals(tag) && !"comment".equals(tag) && !"microcomment".equals(tag)
				&& !"microrecomment".equals(tag)) {
			return showJsonError(model, "数据不存在！");
		}
		if ("N_DELETE".equals(status) || "D".equals(status)) { // "D":手机短信
			if (!reason.equals("5")) {
				reason = ServiceHelper.getReason(reason);
			} else {
				reason = reasonDetail;
			}
		}
		SysMessageAction sysmessage = null;
		GewaQuestion question = null;
		DiaryBase diary = null;
		GewaAnswer answer = null;
		DiaryComment diarycomment = null;
		Comment comment = null;
		ReComment recomment = null;
		User user = getLogonUser();

		for (String id : ids.split(",")) {
			sysmessage = new SysMessageAction(SysAction.STATUS_RESULT);
			sysmessage.setFrommemberid(1l);
			if ("gewaquestion".equals(tag)) {
				question = daoService.getObject(GewaQuestion.class, new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(question);
				question.setStatus(status);
				if ("N_DELETE".equals(status)) {
					deleteLog(status, question.getId(), tag, question.getContent());
					sysmessage.setBody("您发表的【"
							+ question.getTitle().substring(0, question.getTitle().length() > 5 ? 5 : question.getTitle().length()) + "...】内容涉及【"
							+ reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）向管理员申诉。");
					sysmessage.setTomemberid(question.getMemberid());
					daoService.saveObject(sysmessage);
				}
				daoService.saveObject(question);
				searchService.pushSearchKey(question);//更新索引至索引服务器
				monitorService.saveChangeLog(user.getId(), GewaQuestion.class, question.getId(), changeEntry.getChangeMap(question));
			} else if ("gewaanswer".equals(tag)) {// 知道回答
				answer = daoService.getObject(GewaAnswer.class, new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(answer);
				question = daoService.getObject(GewaQuestion.class, answer.getQuestionid());
				answer.setStatus(status);
				if ("N_DELETE".equals(status)) {
					deleteLog(status, answer.getId(), tag, answer.getContent());
					sysmessage.setBody("您对【" + question.getTitle().substring(0, question.getTitle().length() > 5 ? 5 : question.getTitle().length())
							+ "...】的回复内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(answer.getMemberid());
					daoService.saveObject(sysmessage);
				}
				daoService.saveObject(answer);
				monitorService.saveChangeLog(user.getId(), GewaAnswer.class, answer.getId(), changeEntry.getChangeMap(answer));
			} else if ("diary".equals(tag)) {
				diary = diaryService.getDiaryBase(new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(diary);
				diary.setStatus(status);
				if ("N_DELETE".equals(status)) {
					deleteLog(status, diary.getId(), tag, diary.getSubject());
					sysmessage.setBody("您发表的【" + diary.getSubject().substring(0, diary.getSubject().length() > 5 ? 5 : diary.getSubject().length())
							+ "...】内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(diary.getMemberid());
					daoService.saveObject(sysmessage);
				}
				daoService.saveObject(diary);
				searchService.pushSearchKey(diary);//更新索引至索引服务器
				monitorService.saveChangeLog(user.getId(), Diary.class, diary.getId(), changeEntry.getChangeMap(diary));
			} else if ("diarycomment".equals(tag)) {
				diarycomment = daoService.getObject(DiaryComment.class, new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(diarycomment);
				diary = diaryService.getDiaryBase(diarycomment.getDiaryid());
				diarycomment.setStatus(status);
				if ("N_DELETE".equals(status)) {
					deleteLog(status, diarycomment.getId(), tag, diarycomment.getBody());
					sysmessage.setBody("您对【" + diary.getSubject().substring(0, diary.getSubject().length() > 5 ? 5 : diary.getSubject().length())
							+ "...】的回复内容涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(diarycomment.getMemberid());
					daoService.saveObject(sysmessage);
				}
				daoService.saveObject(diarycomment);
				monitorService.saveChangeLog(user.getId(), DiaryComment.class, diarycomment.getId(), changeEntry.getChangeMap(diarycomment));
			} else if ("comment".equals(tag)) {
				comment = commentService.getCommentById(new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(recomment);
				comment.setStatus(status);
				if ("N_DELETE".equals(status)) {
					String value = "";
					if (StringUtils.contains(comment.getTag(), "news")) {
						News news = daoService.getObject(News.class, comment.getRelatedid());
						value = news.getTitle().substring(0, news.getTitle().length() > 5 ? 5 : news.getTitle().length());
					} else {
						Object obj = relateService.getRelatedObject(comment.getTag(), comment.getRelatedid());
						if (obj != null)
							value = StringUtil.enabbr((String) BeanUtil.get(obj, "name"), 10);
					}
					sysmessage.setBody("您对【" + value + "...】的点评涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(comment.getMemberid());
					daoService.saveObject(sysmessage);
				}
				deleteLog(status, comment.getId(), comment.getTag() + "comment", comment.getBody());
				commentService.updateComment(comment);
				monitorService.saveChangeLog(user.getId(), Comment.class, comment.getId(), changeEntry.getChangeMap(comment));
			} else if ("microcomment".equals(tag)) {
				comment = commentService.getCommentById(new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(comment);
				deleteLog(status, comment.getId(), tag, comment.getBody());
				comment.setStatus(status);
				if ("N_DELETE".equals(status)) {
					String body = comment.getBody();
					sysmessage.setBody("您发表的哇啦 " + (body == null ? "" : "【" + body.substring(0, body.length() > 5 ? 5 : body.length()) + "...】")
							+ "涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(comment.getMemberid());
					daoService.saveObject(sysmessage);
					commentService.updateComment(comment);
					monitorService.saveChangeLog(user.getId(), Comment.class, comment.getId(), changeEntry.getChangeMap(comment));
				}
			} else if ("microrecomment".equals(tag)) {
				recomment = walaApiService.getReCommentById(new Long(id));
				ChangeEntry changeEntry = new ChangeEntry(recomment);
				deleteLog(status, recomment.getId(), tag, recomment.getBody());
				recomment.setStatus(status);
				if ("N_DELETE".equals(status)) {
					String body = recomment.getBody();
					sysmessage.setBody("您发表的哇啦回复 " + (body == null ? "" : "【" + body.substring(0, body.length() > 5 ? 5 : body.length()) + "...】")
							+ "涉及【" + reason + "】,已被管理员删除,<br/>如有任何疑问，可使用站内信或邮件（gewara@gewara.com）<br/>向管理员申诉。");
					sysmessage.setTomemberid(recomment.getMemberid());
					daoService.saveObject(sysmessage);
					walaApiService.updateReComment(recomment);
					monitorService.saveChangeLog(user.getId(), ReComment.class, recomment.getId(), changeEntry.getChangeMap(recomment));
				}
			}

		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/saveGrabTicketSubject.xhtml")
	public String saveGrabTicketSubject(Long id, HttpServletRequest request, ModelMap model) {
		GrabTicketSubject grab = new GrabTicketSubject("");
		if (id != null)
			grab = daoService.getObject(GrabTicketSubject.class, id);
		BindUtils.bindData(grab, request.getParameterMap());
		// 验证内容
		String msg = ValidateUtil.validateNewsContent(null, grab.getContent());
		if (StringUtils.isNotBlank(msg))
			return showJsonError(model, msg);
		if (StringUtils.equals(grab.getTag(), GrabTicketSubject.TAG_XPRICE) && grab.getPrice() == null) {
			return showJsonError(model, "价格必须填写");
		}
		if (StringUtils.equals(grab.getTag(), GrabTicketSubject.TAG_PRICE5) && grab.getMovieid() == null) {
			return showJsonError(model, "5元抢票影片ID是必填项！");
		}
		grab.setCitycode(getAdminCitycode(request));
		daoService.saveObject(grab);
		return showJsonSuccess(model, BeanUtil.getBeanMap(grab));
	}

	@RequestMapping("/admin/common/ajax/removeGrabTicketSubject.xhtml")
	public String removeGrabTicketSubject(Long id, ModelMap model) {
		GrabTicketSubject grab = daoService.getObject(GrabTicketSubject.class, id);
		daoService.removeObject(grab);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/saveGrabTicketMpi.xhtml")
	public String saveGrabTicketMpi(Long id, Long mpid, HttpServletRequest request, ModelMap model) {
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if (opi == null)
			return showJsonError(model, "本场次没有开放！");
		GrabTicketMpi grab = mcpService.getGrabTicketMpiListByMpid(mpid);
		if (id == null && grab != null)
			return showJsonError(model, "本场次已经添加过！");

		grab = new GrabTicketMpi("");
		if (id != null)
			grab = daoService.getObject(GrabTicketMpi.class, id);
		BindUtils.bindData(grab, request.getParameterMap());
		daoService.saveObject(grab);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/removeGrabTicketMpi.xhtml")
	public String removeGrabTicketMpi(Long mpid, ModelMap model) {
		GrabTicketMpi grab = mcpService.getGrabTicketMpiListByMpid(mpid);
		daoService.removeObject(grab);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/getGrabTicketMpi.xhtml")
	public String getGrabTicketMpi(Long mpid, ModelMap model) {
		GrabTicketMpi grab = mcpService.getGrabTicketMpiListByMpid(mpid);
		Map result = BeanUtil.getBeanMap(grab);
		return showJsonSuccess(model, result);
	}

	@RequestMapping("/admin/common/ajax/updateGrabTicketSubjectStatus.xhtml")
	public String updateGrabTicketSubjectStatus(Long id, String status, ModelMap model) {
		GrabTicketSubject grab = daoService.getObject(GrabTicketSubject.class, id);
		grab.setStatus(status);
		daoService.saveObject(grab);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/updateGrabTicketSubjectUpdatetime.xhtml")
	public String updateGrabTicketSubjectUpdatetime(Long id, String value, ModelMap model) {
		GrabTicketSubject grab = daoService.getObject(GrabTicketSubject.class, id);
		if (StringUtils.isNotBlank(value)) {
			grab.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		} else {
			grab.setUpdatetime(null);
		}
		daoService.saveObject(grab);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/common/ajax/changeMemberPictureStatus.xhtml")
	public String changeVideoStatus(ModelMap model, Long mpid, String status) {
		User user = getLogonUser();
		MemberPicture memberPicture = daoService.getObject(MemberPicture.class, mpid);
		if (memberPicture == null)
			return showJsonError(model, " 参数出错！");
		ChangeEntry changeEntry = new ChangeEntry(memberPicture);
		memberPicture.setStatus(status);
		daoService.saveObject(memberPicture);
		monitorService.saveChangeLog(user.getId(), MemberPicture.class, mpid, changeEntry.getChangeMap(memberPicture));
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/removeMemberPicture.xhtml")
	public String reVideo(ModelMap model, Long mpid) {
		User user = getLogonUser();
		MemberPicture memberPicture = daoService.getObject(MemberPicture.class, mpid);
		if (memberPicture == null)
			return showJsonError(model, " 参数出错！");
		ChangeEntry changeEntry = new ChangeEntry(memberPicture);
		memberPicture.setStatus(Status.N_DELETE);
		daoService.saveObject(memberPicture);
		monitorService.saveChangeLog(user.getId(), MemberPicture.class, mpid, changeEntry.getChangeMap(memberPicture));
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/saveMemberPicture.xhtml")
	public String saveMemberPicture(ModelMap model, Long mpictureid, String picturename) {
		User user = getLogonUser();
		MemberPicture memberPicture = daoService.getObject(MemberPicture.class, mpictureid);
		if (memberPicture == null)
			return showJsonError(model, "参数出错！");
		ChangeEntry changeEntry = new ChangeEntry(memberPicture);
		memberPicture.setPicturename(picturename);
		daoService.saveObject(memberPicture);
		monitorService.saveChangeLog(user.getId(), MemberPicture.class, mpictureid, changeEntry.getChangeMap(memberPicture));
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/getSubwaylinesByCityCode.xhtml")
	public String getSubwaylinesByCityCode(String citycode, ModelMap model) {
		String url = "admin/sysmgr/loadsubwayline.vm";
		if (StringUtils.isBlank(citycode))
			return url;
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		model.put("lineList", lineList);
		return url;
	}

	@RequestMapping("/admin/common/ajax/getSubwaystationList.xhtml")
	public String getSubwaystationList(Integer pageNo, String stationname, ModelMap model) {
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int firstPerPage = pageNo * rowsPerPage;
		int count = placeService.getSubwaystationCount(stationname);
		List<Subwaystation> subwaystationList = placeService.getSubwaystationList(stationname, firstPerPage, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "/admin/common/ajax/getSubwaystationList.xhtml");
		Map params = new HashMap();
		params.put("stationname", stationname);
		params.put("tag", "station");
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		model.put("subwaystationList", subwaystationList);
		return "admin/sysmgr/subwaystationList.vm";
	}

	@RequestMapping("/admin/common/ajax/getLine2StationByLineId.xhtml")
	public String getLine2StationByLineId(Long lid, ModelMap model) {
		String url = "admin/sysmgr/loadsubwaystation.vm";
		Subwayline line = daoService.getObject(Subwayline.class, lid);
		if (line == null)
			return showJsonError_NOT_FOUND(model);
		List<Line2Station> line2StationList = placeService.getLine2StationByLineId(lid);
		model.put("line2StationList", line2StationList);
		model.put("lid", lid);
		return url;
	}

	@RequestMapping("/admin/common/ajax/addSubwayline.xhtml")
	public String addSubwayline(Long lid, String citycode, String linename, String remark, ModelMap model) {
		if (StringUtils.isBlank(linename))
			return showJsonError(model, "路线名称不能为空！");
		Subwayline line = null;
		if (lid != null) {
			line = daoService.getObject(Subwayline.class, lid);
			if (line == null)
				return showJsonError_NOT_FOUND(model);
			placeService.updateSubwayline(lid, citycode, linename, remark);
		} else {
			line = placeService.getSubwaylineByCitycodeAndName(citycode, linename);
			if (line != null)
				return showJsonError(model, "该路线名称已存在！");
			line = new Subwayline(linename, remark);
			line.setCitycode(citycode);
			daoService.saveObject(line);
		}
		Map map = new HashMap();
		map.put("citycode", citycode);
		return showJsonSuccess(model, map);
	}

	@RequestMapping("/admin/common/ajax/addSubwaystation.xhtml")
	public String addSubwaystation(Long sid, String stationname, ModelMap model) {
		if (StringUtils.isBlank(stationname))
			return showJsonError(model, "站点名称不能为空！");
		Subwaystation station = null;
		if (sid != null) {
			station = daoService.getObject(Subwaystation.class, sid);
			if (station == null)
				return showJsonError_NOT_FOUND(model);
			placeService.updateSubwaystation(sid, stationname);
		} else {
			station = placeService.getSubwaystation(stationname);
			if (station != null)
				return showJsonError(model, "该站点名称已存在！");
			station = new Subwaystation(stationname);
			daoService.saveObject(station);
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/addLine2Station.xhtml")
	public String addLine2Station(Long l2id, Long lineid, Long stationid, ModelMap model) {
		if (lineid == null || stationid == null)
			return showJsonError_DATAERROR(model);
		Subwayline line = daoService.getObject(Subwayline.class, lineid);
		if (line == null)
			return showJsonError_NOT_FOUND(model);
		Subwaystation station = daoService.getObject(Subwaystation.class, stationid);
		if (station == null)
			return showJsonError_NOT_FOUND(model);
		if (l2id != null) {
			Line2Station line2 = daoService.getObject(Line2Station.class, l2id);
			if (line2 == null)
				return showJsonError_NOT_FOUND(model);
			placeService.updateLine2Station(l2id, lineid, stationid);
		} else {
			Line2Station line2 = new Line2Station();
			line2.setLine(line);
			line2.setStation(station);
			daoService.saveObject(line2);
		}
		Map map = new HashMap();
		map.put("lineid", lineid);
		return showJsonSuccess(model, map);
	}

	@RequestMapping("/admin/common/ajax/getSubwayline.xhtml")
	public String getSubwayline(Long lid, ModelMap model) {
		if (lid == null)
			return showJsonError_DATAERROR(model);
		Subwayline line = daoService.getObject(Subwayline.class, lid);
		if (line == null)
			return showJsonError_NOT_FOUND(model);
		Map map = BeanUtil.getBeanMap(line);
		return showJsonSuccess(model, map);
	}

	@RequestMapping("/admin/common/ajax/getSubwaystation.xhtml")
	public String getSubwaystation(Long sid, ModelMap model) {
		if (sid == null)
			return showJsonError_DATAERROR(model);
		Subwaystation station = daoService.getObject(Subwaystation.class, sid);
		if (station == null)
			return showJsonError_NOT_FOUND(model);
		Map map = BeanUtil.getBeanMap(station);
		map.put("sid", station.getId());
		return showJsonSuccess(model, map);
	}
	@RequestMapping("/admin/common/ajax/loadStation.xhtml")
	public String loadStationByLineId(Long lineId, ModelMap model) {
		if (lineId != null){
			 model.put("subwayStations", placeService.getSubwaystationsByLineId(lineId));
			 List<Line2Station> lsList = placeService.getLine2StationByLineId(lineId);
			 Map<Long,Object> tmp = new HashMap<Long,Object>();
			 Map<Long,Line2Station> tmpL2S = new HashMap<Long,Line2Station>();
			 for(Line2Station ls : lsList){
				 if(StringUtils.isNotBlank(ls.getOtherinfo())){
					 tmp.put(ls.getId(), JsonUtils.readJsonToObject(List.class, ls.getOtherinfo()));
				 }
				 tmpL2S.put(ls.getStation().getId(), ls);
			 }
			model.put("otherinfoList", tmp);
			model.put("l2sList", tmpL2S);
			model.put("lineId", lineId);
		}
		return "admin/common/stationByLineId.vm";
	}
	
	@RequestMapping("/admin/common/ajax/setLine2StationTime.xhtml")
	public String setLine2StationTime(Long lineId,HttpServletRequest request, ModelMap model) {
		if(lineId == null){
			return showJsonError_DATAERROR(model);
		}
		List<Line2Station> lsList = placeService.getLine2StationByLineId(lineId);
		for(Line2Station ls : lsList){
			List<Map<String,String>> tmpList = new ArrayList<Map<String,String>>();
			for(int index = 0;index < 2;index++){
				String direction = request.getParameter("direction" + ls.getId() + "[" + index +"]");
				String time = request.getParameter("time" + ls.getId() + "[" + index +"]");
				if(StringUtils.isNotBlank(direction) && StringUtils.isNotBlank(time)){
					Map<String,String> tmp = new HashMap<String,String>();
					tmp.put("direction", direction);
					tmp.put("time", time);
					tmpList.add(tmp);
				}
			}
			String otherinfo = null;
			if(!tmpList.isEmpty()){
				otherinfo = JsonUtils.writeObjectToJson(tmpList);
			}
			if(!(StringUtils.isBlank(ls.getOtherinfo()) && StringUtils.isBlank(otherinfo))){
				placeService.updateLine2StationOtherinfo(ls.getId(), otherinfo);
			}
		}
		return showJsonSuccess(model);
	}
	
	
	@RequestMapping("/admin/common/ajax/getLine2Station.xhtml")
	public String getLine2Station(Long lid, ModelMap model) {
		if (lid == null)
			return showJsonError_DATAERROR(model);
		Line2Station station = daoService.getObject(Line2Station.class, lid);
		if (station == null)
			return showJsonError_NOT_FOUND(model);
		Map map = new HashMap();
		map.put("l2id", station.getId());
		map.put("stationid", station.getStation().getId());
		map.put("lineid", station.getLine().getId());
		return showJsonSuccess(model, map);
	}

	@RequestMapping("/admin/common/ajax/updateLine2StationOrder.xhtml")
	public String updateLine2StationOrder(Long lid, Integer order, ModelMap model) {
		if (lid == null)
			return showJsonError_DATAERROR(model);
		Line2Station station = daoService.getObject(Line2Station.class, lid);
		if (station == null)
			return showJsonError_NOT_FOUND(model);
		placeService.updateLine2StationOrder(lid, order);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/sns/removeMobileByMember.xhtml")
	public String removeMobileByMemberid(Long memberid, ModelMap model, HttpServletRequest request) {
		if (memberid == null){
			return showJsonError_DATAERROR(model);
		}
		User user = getLogonUser();
		ErrorCode code = memberService.unbindMobileByAdmin(memberid, user, WebUtils.getRemoteIp(request));
		if (!code.isSuccess()) {
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/common/ajax/setMemberSignAddress.xhtml")
	public String setMemberSignAddress(ModelMap model, Integer from, Integer maxnum) {
		
		if(from==null) from = 0;
		if(maxnum==null) maxnum = 5000;
		Query query = new Query(); 
		query.addCriteria(Criteria.where("address").is(null));
		//TODO:move to nosqlService
		List<MemberSign> signList = mongoService.getObjectList(MemberSign.class, query.getQueryObject(), "memberid", true, 0, 5000);
		int c = 0;
		for(MemberSign sign : signList){
			double x = sign.getPointx();
			double y = sign.getPointy();
			String address = MapApiUtil.getBaiduMapAddress(x+"", y+"");
			if(StringUtils.isNotBlank(address)){
				sign.setAddress(address);
				c++;
			}
		}
		mongoService.saveOrUpdateObjectList(signList, "memberid");
		return forwardMessage(model, "一共修改：" + c);
	}
	

	@RequestMapping("/admin/sns/dangerMember.xhtml")
	public String dangerMember(){
		return "admin/sysmgr/dangerMemberInfo.vm";
	}
	
	@RequestMapping("/admin/sns/saveDangerMember.xhtml")
	public String saveMemberInfo(String ids, ModelMap model){
		if(StringUtils.isBlank(ids)) return showJsonError(model, "用户ID信息为空！");
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		//List<MemberInfo> infoList = daoService.getObjectList(MemberInfo.class, idList);
		List<MemberInfo> memberInfoList = new ArrayList<MemberInfo>();
		for (Long id: idList) {
			MemberInfo memberInfo = memberService.updateDanger(id); 
			memberInfoList.add(memberInfo);
		}
		return showJsonSuccess(model, memberInfoList.size() + "");
	}
}
