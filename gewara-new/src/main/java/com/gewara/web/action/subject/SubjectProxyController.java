package com.gewara.web.action.subject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.DrawActicityConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.sys.RelateClassHelper;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.VersionCtl;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.Picture;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CityPrice;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.Spcounter;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.MemberPicture;
import com.gewara.model.user.ShareMember;
import com.gewara.mongo.MongoService;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommonVoteService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.content.NewsService;
import com.gewara.service.content.PictureService;
import com.gewara.service.content.RecommendService;
import com.gewara.service.content.VideoService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.UntransService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.RandomUtils;
import com.gewara.util.RelatedHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;
import com.gewara.xmlbind.gym.RemoteGym;
import com.mongodb.BasicDBObject;
import com.mongodb.QueryOperators;
@Controller
public class SubjectProxyController  extends AnnotationController {
	
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	@Autowired@Qualifier("recommendService")
	private RecommendService recommendService;
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("commonVoteService")
	private CommonVoteService commonVoteService;
	@Autowired@Qualifier("markService")
	private MarkService markService;
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;
	@Autowired@Qualifier("videoService")
	private VideoService videoService;
	@Autowired@Qualifier("qaService")
	private QaService qaService;
	@Autowired@Qualifier("controllerService")
	protected ControllerService controllerService;
	
	@RequestMapping("/subject/test1.xhtml")
	public String test1(ModelMap model){
		HttpResult result = HttpUtils.getUrlAsString("http://180.153.146.137:82/subject/proxy1.xhtml");
		return showDirectJson(model, result.getResponse());
	}
	@RequestMapping("/subject/test2.xhtml")
	public String test2(ModelMap model){
		HttpResult result = HttpUtils.getUrlAsString("http://180.153.146.137:82/partner/bestv/index.xhtml");
		return showDirectJson(model, result.getResponse());
	}
	@RequestMapping("/subject/proxy1.xhtml")
	public String proxy1(ModelMap model){
		return forwardMessage(model, "proxy1");
	}
	@RequestMapping("/subject/proxy2.xhtml")
	public String proxy2(ModelMap model){
		return forwardMessage(model, "proxy2");
	}
	
	// 根据tag + ID, 返回对象
	@RequestMapping("/subject/proxy/getObjectByTagAndID.xhtml")
	public String getObjectByTagAndID(String tag, Long id, ModelMap model){
		BaseObject object = daoService.getObject(RelateClassHelper.getRelateClazz(tag), id);
		if(object == null) return showJsonError_NOT_FOUND(model);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(object));
	}
	
	// 根据tag + ID, 返回对象
	@RequestMapping("/subject/proxy/getRelatedObject.xhtml")
	public String getRelatedObject(String tag, Long id, ModelMap model){
		Object object = relateService.getRelatedObject(tag, id);
		if(object == null) return showJsonError_NOT_FOUND(model);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(object));
	}
	
	//用户绑定微薄
	@RequestMapping("/ajax/member/synInfo.xhtml")
	public String ajaxForMemberSynInfo(String sessid, String ip, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ),member.getId());
		List<String> appList = BeanUtil.getBeanPropertyList(shareMemberList, String.class, "source", true);
		Map result = new HashMap();
		result.put("appList", appList);
		return showJsonSuccess(model, result);
	}
	
	//用户登录信息 
	@RequestMapping("/subject/proxy/memberlogin.xhtml")
	//"id", "nickname", "headpicUrl","mobile","addtime"
	public String ajaxMemberLogin(String sessid, String ip, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError_NOT_LOGIN(model);
		Map memberMap = memberService.getCacheMemberInfoMap(member.getId());
		if(member.isBindMobile()){
			memberMap.put("mobile", member.getMobile());
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(memberMap));
	}
	
	@RequestMapping("/subject/proxy/getSpecialDiscountAllowaddnum.xhtml")
	public String getSpecialDiscountAllowaddnum(Long spid ,ModelMap model){
		if(spid == null){
			return showJsonError(model, "参数错误！");
		}
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, spid);
		Spcounter spcounter = paymentService.getSpdiscountCounter(sd);
		if(spcounter != null){
			if(StringUtils.equals(spcounter.getCtltype(), Spcounter.CTLTYPE_QUANTITY) ){
				int allow1 = spcounter.getLimitmaxnum() - spcounter.getAllquantity();
				int	allow2 = spcounter.getBasenum() - spcounter.getSellquantity();
				return showJsonSuccess(model,"" + Math.min(allow1, allow2));
			}else{
				int allow1 = spcounter.getLimitmaxnum() - spcounter.getAllordernum();
				int allow2 = spcounter.getBasenum() - spcounter.getSellordernum();
				return showJsonSuccess(model,"" + Math.min(allow1, allow2));	
			}
		}else{
			return showJsonError(model, "特价活动数量未设置！");
		}
	}
	/**
	 * 获取电影哇啦总数
	 * @param mid
	 * @param model
	 * @return
	 */
	@RequestMapping("/subject/proxy/getwalaCountByRelatedId.xhtml")
	public String getCommentCountByRelatedId(Long mid ,ModelMap model){
		if(mid == null){
			return showJsonError(model, "参数错误！");
		}
		Integer commentCount = commentService.getCommentCountByRelatedId("movie", mid);
		return showJsonSuccess(model,commentCount + "");
	}
	@RequestMapping("/subject/proxy/loadWalaByTag.xhtml")
	public String loadWala(String tag,int rows,long relatedid, ModelMap model){
		List<Comment> commentList = commentService.getCommentListByRelatedId(tag,null, relatedid, null, 0, rows);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(commentList));
	}
	//根据话题得到哇啦
	@RequestMapping("/subject/proxy/getwala.xhtml")
	public String blackmengetwala(String topic, Integer form, Integer max, ModelMap model){
		if(StringUtils.isBlank(topic) || form == null || max == null) return showJsonError(model, "参数错误！");
		List<Comment> commentList = commentService.getModeratorDetailList(topic, false, form, max);
		List<Map> commentMapList = BeanUtil.getBeanMapList(commentList, new String[]{"memberid", "nickname", "topic", "body", "addtime"});
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(commentMapList));
	}
	// 根据话题得到哇啦数量
	@RequestMapping("/subject/proxy/getwalaCount.xhtml")
	public String blackmengetwala(String topic, ModelMap model){
		if(StringUtils.isBlank(topic)) return showJsonError(model, "参数错误！");
		int count = commentService.getModeratorDetailCount(topic);
		return showJsonSuccess(model, count+"");
	}
	//根据电影ID得到影评
	@RequestMapping("/subject/proxy/getDiary.xhtml")
	public String getDiary(Long movieid, String order, String citycode, Integer formnum, Integer maxnum, ModelMap model){
		if(movieid == null) return showJsonError(model, "参数错误！");
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		if(StringUtils.isBlank(order)) order = "poohnum";
		if(formnum == null) formnum = 0;
		if(maxnum == null) maxnum = 10;
		List<Diary> diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", movieid, formnum, maxnum, order); 
		List<Map> commentMapList = BeanUtil.getBeanMapList(diaryList, new String[]{"id","memberid", "membername", "subject", "summary", "addtime"});
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(commentMapList));
	}
	//根据电影ID得到资讯
	@RequestMapping("/subject/proxy/getNews.xhtml")
	public String getNews(Long movieid, String citycode, Integer formnum, Integer maxnum, ModelMap model){
		if(movieid == null) return showJsonError(model, "参数错误！");
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		if(formnum == null) formnum = 0;
		if(maxnum == null) maxnum = 10;
		List<News> newsList = newsService.getNewsList(citycode, "movie", movieid, null, formnum, maxnum);
		List<Map> commentMapList = BeanUtil.getBeanMapList(newsList, new String[]{"id","title"});
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(commentMapList));
	}
	//tag+id得到此电影或运动等图片
	@RequestMapping("/subject/proxy/getPictureList.xhtml")
	public String getPictureList(String tag, Long relatedid, String type, Integer formnum, Integer maxnum, ModelMap model) {
		if(StringUtils.isBlank(tag) || relatedid == null || StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		if(formnum == null) formnum = 0;
		if(maxnum == null) maxnum = 10;
		List<Map> picMapList = new ArrayList<Map>();
		if(StringUtils.contains(type, "apic")){
			List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, formnum, maxnum);
			picMapList.addAll(BeanUtil.getBeanMapList(pictureList, new String[]{"id","picturename"}));
		}
		if(StringUtils.contains(type, "mpic")){
			List<MemberPicture> memberPictureList = pictureService.getMemberPictureList(relatedid, tag, null, TagConstant.FLAG_PIC, Status.Y, formnum, maxnum);
			picMapList.addAll(BeanUtil.getBeanMapList(memberPictureList, new String[]{"id","picturename"}));
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(picMapList));
	}
	//tag+id得到此电影或运动等视频
	@RequestMapping("/subject/proxy/getVideoList.xhtml")
	public String getVideoList(String tag, Long relatedid, Integer formnum, Integer maxnum, ModelMap model) {
		if(StringUtils.isBlank(tag) || relatedid == null) return showJsonError(model, "参数错误！");
		if(formnum == null) formnum = 0;
		if(maxnum == null) maxnum = 5;
		List<Video> videoList = videoService.getVideoListByTag(tag, relatedid, formnum, maxnum);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(videoList));
	}
	//根据物品IDS得到物品
	@RequestMapping("/subject/proxy/getGoodsListByGoodsId.xhtml")
	public String getGoodsListByGoodsId(String goodsids, ModelMap model){
		List<Long> idList = BeanUtil.getIdList(goodsids, ",");
		if(idList.isEmpty()) return showJsonError(model, "参数错误！");
		List<BaseGoods> goodsList = daoService.getObjectList(BaseGoods.class, idList);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(BeanUtil.getBeanMapList(goodsList, new String[]{"id","goodsname","shortname","oriprice","unitprice","sales","allowaddnum","quantity","maxbuy","limg","fromtime","totime"})));
	}
	//根据条件得到活动
	@RequestMapping("/subject/proxy/getActivity.xhtml")
	public String getActivity(Long relatedid, String citycode, String type, Integer formnum, String order, String tag, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(citycode)) citycode = "310000";
		if(formnum == null) formnum = 0;
		if(maxnum == null) maxnum = 5;
		if(StringUtils.isBlank(type)) type = RemoteActivity.ATYPE_GEWA;
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByOrder(citycode, type, RemoteActivity.TIME_CURRENT, null, null, tag, relatedid, order, formnum, maxnum);
		if(code.isSuccess()){
			List<RemoteActivity> activityList = code.getRetval();
			List<Map> activityMapList = BeanUtil.getBeanMapList(activityList, new String[]{"id","title","address","clickedtimes","logo","startdate","enddate"});
			return showJsonSuccess(model, JsonUtils.writeObjectToJson(activityMapList));
		}
		return showJsonError(model, code.getMsg());
	}
	//根据 type、tag 查询资讯、剧照、视频
	@RequestMapping("/subject/proxy/getInfoList.xhtml")
	public String getInfoList(String type,String tag, Integer from, Integer max, ModelMap model){
		if(from == null) from = 0;
		if(max == null) max = 5;
 		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		List<Map> list = mongoService.find(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params, MongoData.ACTION_ORDERNUM, true, from, max);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(list));
	}
	//根据 type、tag 查询 数量
	@RequestMapping("/subject/proxy/getPictureCount.xhtml")
	public String getCheckPictureCount(String type,String tag, ModelMap model){
		if(StringUtils.isBlank(type)) return showJsonError(model, "参数错误！");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, type);
		params.put(MongoData.ACTION_TAG, tag);
		BasicDBObject query = new BasicDBObject();
		query.put(QueryOperators.GT, 0);
		params.put(MongoData.ACTION_ORDERNUM, query);
		int count = mongoService.getCount(MongoData.NS_ACTIVITY_COMMON_PICTRUE, params);
		return showJsonSuccess(model, count+"");
	}
	//根据用户ID得到用户信息（从缓存拿）
	@RequestMapping("/subject/proxy/getMemberInfoList.xhtml")
	public String getMemberInfoList(String ids, ModelMap model){
		if(StringUtils.isBlank(ids)) return showJsonError(model, "参数错误！");
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		addCacheMember(model, idList);
		Map<Long, Map> map = (Map<Long, Map>) model.get("cacheMemberMap");
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(map));

	}
	//根据activityid查询活动
	@RequestMapping("/subject/proxy/getActivityInfo.xhtml")
	public String getActivityInfo(Long id, ModelMap model){
		if(id == null) return showJsonError(model, "参数错误！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(id);
		if(code.isSuccess()){
			RemoteActivity activity = code.getRetval();
			return showJsonSuccess(model, JsonUtils.writeObjectToJson(activity));
		}
		return showJsonError(model, code.getMsg());
	}
	//判断是否是disney用户
	@RequestMapping("/subject/proxy/isDisneyMember.xhtml")
	public String isDisneyMember(Long memberid, ModelMap model) {
		Map params = new HashMap();
		params.put("memberid", memberid);
		Map memberMap = mongoService.findOne(MongoData.NS_DISNEY_MEMBER, params);
		if(memberMap == null) return showJsonSuccess(model, "nodisney");
		return showJsonSuccess(model);
	}
	//专题发送短信	
	@RequestMapping("/subject/proxy/sendMessage.xhtml")
	public String sendMessage(String ztid, Long memberid, String mobile, String sendTime, String msgContent, String captchaId, String captcha, String hasCaptcha, String ip, ModelMap model){
		if(memberid == null) return showJsonSuccess(model, "请先登录！");
		if(!StringUtils.equals(hasCaptcha, Status.N)){
			boolean isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, ip);
			if(!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		}
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码不能为空！");
		if(StringUtils.isBlank(msgContent)) return showJsonError(model, "短信内容不能为空！");
		if(VmUtils.getByteLength(msgContent) > 128) return showJsonError(model, "短信内容过长！");
		boolean isSendMsg = StringUtils.isNotBlank(blogService.filterAllKey(msgContent));
		String[] stimes = StringUtils.split(sendTime, ",");
		if(stimes == null) return showJsonError(model, "参数错误,发送时间不能为空！");
		for(String stime : stimes){
			Timestamp st = DateUtil.parseTimestamp(stime);
			SMSRecord sms = new SMSRecord(mobile);
			if(isSendMsg){
				sms.setStatus(SmsConstant.STATUS_FILTER);
			}
			if(StringUtils.isBlank(ztid)){
				sms.setTradeNo("zt"+DateUtil.format(st, "yyyyMMdd"));
			}else{
				sms.setTradeNo("zt"+ztid);
			}
			sms.setContent(msgContent);
			sms.setSendtime(st);
			sms.setSmstype(SmsConstant.SMSTYPE_MANUAL);
			sms.setValidtime(DateUtil.getLastTimeOfDay(st));
			sms.setTag(TagConstant.TAG_ZHUANTI);
			sms.setMemberid(memberid);
			untransService.addMessage(sms);
		}
		return showJsonSuccess(model);
	}
	
	//得到推荐信息集合
	@RequestMapping("/subject/proxy/getRecommendedListByPage.xhtml")
	public String getRecommendedListByPage(String tag, String signname, String page, String parentid, String para, ModelMap model){
		if(StringUtils.isBlank(tag) && StringUtils.isBlank(signname) || StringUtils.isBlank(para)) return showJsonSuccess(model);
		String[] param = StringUtils.split(para ,",");
		List<Map> dataMap = recommendService.getRecommendMap(tag, signname, parentid);
		if(dataMap != null && StringUtils.isNotBlank(page)){
			for(Map data : dataMap){
				data.remove("id");
				Object object = relateService.getRelatedObject(page, new Long(data.get(MongoData.ACTION_BOARDRELATEDID).toString().trim()));
				if(object != null){
					data.putAll(BeanUtil.getBeanMapWithKey(object, param));
				}
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(dataMap));
	}
	//得到未知推荐信息集合
	@RequestMapping("/subject/proxy/getRecommendedList.xhtml")
	public String getRecommendedList(String tag, String signname, String parentid, ModelMap model){
		if(StringUtils.isBlank(tag) && StringUtils.isBlank(signname)) return showJsonSuccess(model);
		List<Map> dataMap = recommendService.getRecommendMap(tag, signname, parentid);
		if(dataMap != null){
			List<Long> activityIds = new ArrayList<Long>();
			for(Map data : dataMap){
				if(data.get("newsboard") == null) continue;
				String type = data.get("newsboard")+"";
				Long relatedid = new Long(data.get(MongoData.ACTION_BOARDRELATEDID)+"");
				if(StringUtils.equals(type, "movie")){
					Movie movie = daoService.getObject(Movie.class, relatedid);
					data.putAll(BeanUtil.getBeanMapWithKey(movie, "moviename", "highlight", "director", "actors", "language", "videolen", "rclickedtimes", "boughtcount", "limg", "id"));
				}else if(StringUtils.equals(type, "drama")){
					Drama drama = daoService.getObject(Drama.class, relatedid);
					data.putAll(BeanUtil.getBeanMapWithKey(drama, "dramaname", "highlight", "dramatype", "releasedate", "enddate", "clickedtimes", "boughtcount", "limg", "id"));
				}else if(StringUtils.equals(type, "activity")){
					activityIds.add(relatedid);
					/*ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
					if(code.isSuccess()){
						RemoteActivity activity = code.getRetval();
						data.putAll(BeanUtil.getBeanMapWithKey(activity, "title", "membername", "memberid", "address", "limg", "clickedtimes", "membercount", "id"));
					}*/
				}else if(StringUtils.equals(type, "gym")){
					ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(relatedid, true);
					if(code.isSuccess()){
						RemoteGym gym = code.getRetval();
						data.putAll(BeanUtil.getBeanMapWithKey(gym, "name", "address", "clickedtimes", "limg", "content", "generalmarkedtimes", "id"));
					}
				}else if(StringUtils.equals(type, "sport")){
					Sport sport = daoService.getObject(Sport.class, relatedid);
					data.putAll(BeanUtil.getBeanMapWithKey(sport, "name", "address", "clickedtimes", "feature", "limg", "generalmarkedtimes", "id"));
				}
			}
			//------------抽取在循环内调用http请求
			if(!activityIds.isEmpty()){
				ErrorCode<List<RemoteActivity>> activitysCode = synchActivityService.getRemoteActivityListByIds(activityIds);
				if(activitysCode.isSuccess() && activitysCode.getRetval() != null){
					for(Map data : dataMap){
						if(data.get("newsboard") == null) continue;
						String type = data.get("newsboard")+"";
						Long relatedid = new Long(data.get(MongoData.ACTION_BOARDRELATEDID)+"");
						if(StringUtils.equals(type, "activity")){
							for(RemoteActivity activity : activitysCode.getRetval()){
								if(activity.getId().equals(relatedid)){
									data.putAll(BeanUtil.getBeanMapWithKey(activity, "title", "membername", "memberid", "address", "limg", "clickedtimes", "membercount", "id"));
									break;
								}
							}
						}
					}
				}
			}
			//-----------------------------------------
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(dataMap));
	}
	//得到抽奖奖品信息
	@RequestMapping("/subject/proxy/getPrizeInfo.xhtml")
	public String getPrizeInfo(String ids, ModelMap model){
		if(StringUtils.isBlank(ids)) return showJsonError(model, "参数错误！");
		List<Long> idList = BeanUtil.getIdList(ids, ",");
		List<Prize> prizeList = daoService.getObjectList(Prize.class, idList);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(BeanUtil.getBeanMapList(prizeList, new String[]{"id","pnumber","psendout","otype"})));
	}
	//disney活动专题
	private static final List<String> tagList = Arrays.asList("wreckitralph");
	//抽奖公用
	@RequestMapping("/subject/proxy/clickDraw.xhtml")
	public String clickDraw(Long memberid, String check, String gewatoken, String pointxy, String tag, String noLimitDraw, HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(tag)) return showJsonSuccess(model, "syserror");
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "nologin");
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) return showJsonSuccess(model, "nologin");
		if(tagList.contains(tag) && StringUtils.isBlank(gewatoken)){
			Map params = new HashMap();
			params.put("memberid", member.getId());
			Map memberMap = mongoService.findOne(MongoData.NS_DISNEY_MEMBER, params);
			if(memberMap == null) return showJsonSuccess(model, "nodsn");
		}
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String opkey = tag + member.getId();
		boolean allow = operationService.updateOperation(opkey, 10);
		if(!allow) return showJsonSuccess(model, "frequent");
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null||!da.isJoin()) return showJsonSuccess(model, "nostart");
		Timestamp cur = DateUtil.getCurFullTimestamp();
		Integer num = 1;
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(da.getOtherinfo());
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOBILE)) && !member.isBindMobile()) return showJsonSuccess(model, "nobindMobile");
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_EMAIL)) && !memberInfo.isFinishedTask(MemberConstant.TASK_CONFIRMREG)) return showJsonSuccess(model, "noemail");
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_TICKET)) && StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID))){
			Long movieid = Long.parseLong(otherinfoMap.get(DrawActicityConstant.TASK_MOVIEID));
			int pay = orderQueryService.getMemberOrderCountByMemberid(member.getId(), movieid);
			if(pay == 0) return showJsonSuccess(model, "noticket");
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_MOREDRAW))){
			if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_HOUR))){
				Timestamp startTime = DateUtil.addHour(cur,-Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_HOUR)));
				List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), null,  startTime, cur, "system", member.getId(), null, null, 0, 1);
				if(winnerList.size() == 1) return showJsonSuccess(model, "nodrawcount");
				Timestamp from = DateUtil.getCurTruncTimestamp();
				Timestamp to = DateUtil.getMillTimestamp();
				int today = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), from, to);
				num = today + 1;
			}else if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND))){
				Date date = DateUtil.currentTime();
				Timestamp startTime = DateUtil.getBeginTimestamp(date);
				Timestamp endTime = DateUtil.getEndTimestamp(date);
				Integer winnerCount = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), startTime, endTime);
				Integer inviteCount = memberService.getInviteCountByMemberid(member.getId(), startTime, endTime);
				if(inviteCount - winnerCount >= Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND))) num = inviteCount / Integer.parseInt(otherinfoMap.get(DrawActicityConstant.TASK_FRIEND));
			}
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_WEIBO))){
			List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA, MemberConstant.SOURCE_QQ),member.getId());
			if(VmUtils.isEmptyList(shareMemberList)) return showJsonSuccess(model, "noweibo");
			int today = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(today > 0) return showJsonSuccess(model, "nocount");
		}
		if(StringUtils.isNotBlank(otherinfoMap.get(DrawActicityConstant.TASK_ONLYONE))){
			int drawtimes = drawActivityService.getMemberWinnerCount(member.getId(), da.getId(), da.getStarttime(), da.getEndtime());
			if(drawtimes > 0) return showJsonSuccess(model, "nocount");
		}
		if (!StringUtils.equals(noLimitDraw, "Y")) {
			Integer count = drawActivityService.getCurDrawActivityNum(da, member.getId(), num);
			if(count <= 0) return showJsonSuccess(model, "nodrawcount");
		}
		VersionCtl mvc = drawActivityService.gainMemberVc(""+member.getId());
		try {
			//FIXME:黄牛？？
			ErrorCode<WinnerInfo> ec = drawActivityService.baseClickDraw(da, mvc, false, member);
			if(ec == null || !ec.isSuccess()) return showJsonSuccess(model, ec.getErrcode());
			WinnerInfo winnerInfo = ec.getRetval();
			if(winnerInfo == null) return showJsonSuccess(model, "syserror");
			Prize prize = daoService.getObject(Prize.class, winnerInfo.getPrizeid());
			if(prize == null) return showJsonSuccess(model, "syserror");
			SMSRecord sms =drawActivityService.sendPrize(prize, winnerInfo, true);
			if(sms !=null) untransService.sendMsgAtServer(sms, false);
			Map otherinfo = VmUtils.readJsonToMap(prize.getOtherinfo());
			if(otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT) != null){
				String link = null;
				if(otherinfo.get(DrawActicityConstant.TASK_WALA_LINK) != null){
					link = otherinfo.get(DrawActicityConstant.TASK_WALA_LINK)+"";
					link = "<a href=\""+link+"\" target=\"_blank\" rel=\"nofollow\">"+"链接地址"+"</a>";
				}
				String pointx = null, pointy = null;
				if(StringUtils.isNotBlank(pointxy)){
					List<String> pointList = Arrays.asList(StringUtils.split(pointxy, ":"));
					if(pointList.size() == 2){
						pointx = pointList.get(0);
						pointy = pointList.get(1);
					}
				}
				ErrorCode<Comment> result = commentService.addComment(member, TagConstant.TAG_TOPIC, null, otherinfo.get(DrawActicityConstant.TASK_WALA_CONTENT)+"", link, false, pointx, pointy, WebUtils.getRemoteIp(request));
				if(result.isSuccess()) {
					shareService.sendShareInfo("wala",result.getRetval().getId(), result.getRetval().getMemberid(), null);
				}
			}
			return showJsonSuccess(model, "content="+prize.getPlevel()+"&ptype="+prize.getPtype()+"&prize="+prize.getOtype());
		}catch(StaleObjectStateException e){
			return showJsonSuccess(model, "syserror");
		}catch(HibernateOptimisticLockingFailureException e){
			return showJsonSuccess(model, "syserror");
		}
	}
	//获取排片信息
	@RequestMapping("/subject/proxy/getOpenplayInfo.xhtml")
	public String getOpenplayInfo(Timestamp timeTo,String citycode,Long cinemaId, Long movieId,String edition, ModelMap model){
		Timestamp timeFrom = DateUtil.getCurFullTimestamp();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<OpenPlayItem> list = openPlayService.getOpiList(citycode, cinemaId, movieId, timeFrom, timeTo, true);
		for (OpenPlayItem opi: list) {
			if(StringUtils.containsIgnoreCase(opi.getEdition(), edition)){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("mpid", opi.getMpid());
				map.put("gewaprice", opi.getGewaprice());
				map.put("costprice", opi.getCostprice());
				map.put("playtime", opi.getPlaytime());
				result.add(map);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
	/**
	 * 007专题暂用查DMAX厅影片场次信息
	 * 
	 * */
	@RequestMapping("/subject/proxy/getOpenplaySpyInfo.xhtml")
	public String getOpenplaySpyInfo(Timestamp timeTo,String citycode,Long cinemaId, Long movieId,Long roomId, ModelMap model){
		Timestamp timeFrom = DateUtil.getCurFullTimestamp();
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		List<OpenPlayItem> list = openPlayService.getOpiList(citycode, cinemaId, movieId, timeFrom, timeTo, true);
		for (OpenPlayItem opi: list) {
			if(opi.getRoomid().equals(roomId)){
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("mpid", opi.getMpid());
				map.put("gewaprice", opi.getGewaprice());
				map.put("costprice", opi.getCostprice());
				map.put("playtime", opi.getPlaytime());
				result.add(map);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
	//根据城市ID得到城市的电影和影院
	@RequestMapping("/subject/proxy/getCurCinemaAndMovieByCitycode.xhtml")
	public String getCurCinemaAndMovieByCitycode(String citycodes, ModelMap model){
		Map resultMap = new HashMap();
		if(StringUtils.isNotBlank(citycodes)){
			for(String citycode : StringUtils.split(citycodes, ",")){
				Integer cinemaCount = mcpService.getTicketCinemaCount(citycode, null, null, null);
				List<Long> movieIdList = mcpService.getCurMovieIdList(citycode);
				resultMap.put("cinema"+citycode, cinemaCount);
				resultMap.put("movie"+citycode, movieIdList.size());
			}
		}
		return showJsonSuccess(model, JsonUtils.writeMapToJson(resultMap));
	}
	//根据城市ID得到城市的电影院
	@RequestMapping("/subject/proxy/getCinemaByCitycode.xhtml")
	public String getCinemaByCitycode(String citycodes, ModelMap model){
		List resultList = new LinkedList();
		if(StringUtils.isNotBlank(citycodes)){
			for(String citycode : StringUtils.split(citycodes, ",")){
				List<Cinema> cinemas = mcpService.getCinemaListByCitycode(citycode, 0, 100);
				if(!cinemas.isEmpty()){
					Map<String,List<Map>> resultMap = new HashMap<String,List<Map>>();
					for(Cinema cinema : cinemas){
						if(StringUtils.isNotBlank(cinema.getCountycode())){
							List<Map> cinemaList = resultMap.get(cinema.getCountycode());
							if(cinemaList == null){
								cinemaList = new LinkedList<Map>();
								resultMap.put(cinema.getCountycode(), cinemaList);
							}
							cinemaList.add(BeanUtil.getBeanMapWithKey(cinema,"id","name","countycode","countyname","citycode"));
						}
					}
					resultList.add(resultMap);
				}
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(resultList));
	}
	//保存投票信息
	@RequestMapping("/subject/proxy/saveVote.xhtml")
	public String saveVote(Long memberid, String tag, String itemid, Integer count, ModelMap model){
		if(memberid == null) return showJsonSuccess(model, "请先登录！");
		Map memberMap = commonVoteService.getSingleVote(tag, memberid, null);
		if(memberMap == null){
			memberMap = new HashMap();
			memberMap.put("tag", tag);
			memberMap.put("memberid", memberid);
			memberMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		}
		memberMap.put("addtime", DateUtil.getCurFullTimestampStr());
		memberMap.put("itemid", itemid);
		mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_COMMON_VOTE);
		vote(tag, itemid, count, model);
		return showJsonSuccess(model);
	}
	//得到投票信息
	@RequestMapping("/subject/proxy/getMemberVoteInfo.xhtml")
	public String getMemberVoteInfo(Long memberid, String tag, String itemid, ModelMap model){
		Map memberMap = commonVoteService.getSingleVote(tag, memberid, itemid);
		return showJsonSuccess(model, JsonUtils.writeMapToJson(memberMap));
	}
	//得到票数
	@RequestMapping("/subject/proxy/vote/movieList.xhtml")
	public String votemovieList(String flag, ModelMap model){
		List<Map> list = commonVoteService.getItemVoteList(flag);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(list));
	}
	// 得到最新投票人
	@RequestMapping("/subject/proxy/getVoteInfo.xhtml")
	public String getVoteInfo(String tag, Integer from, Integer maxnum, ModelMap model){
		if(StringUtils.isBlank(tag)) return showJsonSuccess(model);
		if(from == null) from = 0;
		if(maxnum == null) maxnum = 20;
		List<Map> result = commonVoteService.getVoteInfo(tag, from, maxnum);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
	
	@RequestMapping("/subject/proxy/indexCommend.xhtml")
	public String indexCommend(String cityCode,String signName,int from, int maxnum, ModelMap model){
		if(maxnum > 50){
			maxnum = 50;
		}
		List<GewaCommend> gewaCommends = commonService.getGewaCommendList(cityCode,signName, null, null, true, from, maxnum);
		if(StringUtils.equals(SignName.INDEX_MOVIELIST, signName) || StringUtils.equals(SignName.INDEX_MOVIELIST_NEW, signName)){
			RelatedHelper rh = new RelatedHelper();
			commonService.initGewaCommendList("gcMovieList", rh, gewaCommends);
			List<Map> maps = new LinkedList<Map>();
			for(GewaCommend gewaCommend :gewaCommends) {
				Map map = new HashMap();
				Movie movie = (Movie)rh.getR1("gcMovieList",gewaCommend.getId());
				map.put("id",movie.getId());
				map.put("moviename", movie.getMoviename());
				map.put("gmark",VmUtils.getLastMarkStar(movie, "general",markService.getMarkCountByTagRelatedid(gewaCommend.getTag(), gewaCommend.getRelatedid()),markService.getMarkdata(TagConstant.TAG_MOVIE)));
				if(StringUtils.isNotBlank(gewaCommend.getLogo())){
					map.put("logo", gewaCommend.getLogo());
				}else{
					map.put("logo", movie.getLimg());
				}
				map.put("edition", movie.getEdition());
				map.put("hotvalue",movie.getHotvalue());
				CityPrice cityPrice = moviePriceService.getCityPrice(gewaCommend.getRelatedid(), cityCode, TagConstant.TAG_MOVIE);
				if(cityPrice != null){
					map.put("cinemas",cityPrice.getCquantity());//播放影片影院数量
					map.put("mpids",cityPrice.getQuantity());//影片排片数量
				}else{
					map.put("cinemas",0);
					map.put("mpids",0);
				}
				maps.add(map);
			}
			return showJsonSuccess(model, JsonUtils.writeObjectToJson(maps));	
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(gewaCommends));	
	}
	//投票
	private String vote(String tag, String itemid, Integer count, ModelMap model){
		if(count == null) count = RandomUtils.randomInt(3)+3;
		int support = commonVoteService.getSupportCount(tag, itemid);
		commonVoteService.addCommonVote(tag, itemid, support+count);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/proxy/movieListByIds.xhtml")
	public String movieListByIds(String movieids, ModelMap model){
		if(movieids==null) return showJsonSuccess(model, "传递参数错误！");
		List<Long> movieidList = BeanUtil.getIdList(movieids, ",");
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		List<Map> mapList = BeanUtil.getBeanMapList(movieList, "id","name","logo","moviename","highlight","director","actors", "language", "state","videolen","type");
		for (int i = 0; i < mapList.size(); i++) {
			MarkCountData markCount = markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, Long.valueOf(mapList.get(i).get("id").toString()));
			Integer general = VmUtils.getLastMarkStar(siftMovie(movieList,Long.valueOf(mapList.get(i).get("id").toString())), "general", markCount, markService.getMarkdata(TagConstant.TAG_MOVIE));
			mapList.get(i).put("generalmark", general);//general / 10 + "." + general % 10;
		}
		model.put("movieList", mapList);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(mapList));	
	}
	
	private Movie siftMovie(List<Movie> movieList,Long id){
		for (int i = 0; i < movieList.size(); i++) {
			if (movieList.get(i).getId().equals(id)) {
				return movieList.get(i);
			}
		}
		return null;
	}
	
	@RequestMapping("/subject/proxy/activityListByIds.xhtml")
	public String activityListByIds(String aids, ModelMap model){
		if(aids == null) return showJsonError(model, "参数错误！");
		List<Long> aidList = BeanUtil.getIdList(aids, ",");
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(aidList);
		if(code.isSuccess()){
			List<RemoteActivity> activityList = code.getRetval();
			return showJsonSuccess(model, JsonUtils.writeObjectToJson(activityList));
		}
		return showJsonError(model, code.getMsg());
	}
	
	@RequestMapping("/subject/proxy/videoListBymid.xhtml")
	public String videoListBymid(String movieId , ModelMap model){
		List<Video> videoList = videoService.getVideoListByTag("movie", Long.valueOf(movieId), 0, 1000);
		List<MemberPicture> memberVideoList = pictureService.getMemberPictureList(Long.valueOf(movieId), TagConstant.TAG_MOVIE, null, TagConstant.FLAG_VIDEO,
				Status.Y, 0, 1000);
		Map<String , List> videoMap =new HashMap<String, List>();
		videoMap.put("videoList", videoList);
		videoMap.put("memberVideoList", memberVideoList);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(videoMap));
	}
	
	@RequestMapping("/subject/proxy/commentListBymid.xhtml")
	public String commentListBymid(Long memberId, Long movieId, String myOrder, String friend, Integer pageNo, String citycode , ModelMap model) {
		Map<String, Object> map = new HashMap<String, Object>();
		Movie movie = daoService.getObject(Movie.class, movieId);
		if (movie == null)
			return showJsonError(model, "电影不存在或已经删除！");
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 10;
		int first = rowsPerPage * pageNo;
		List<Diary> diaryList = new ArrayList<Diary>();
		if (StringUtils.isBlank(myOrder))
			myOrder = "poohnum";//addtime
		Member member = (Member)relateService.getRelatedObject("member", memberId);
		if (StringUtils.isNotBlank(friend) && member != null) {
			diaryList = diaryService.getFriendDiaryList(DiaryConstant.DIARY_TYPE_COMMENT, "movie", movieId, member.getId(), first, rowsPerPage);
			map.put("friend", true);
		} else {
			//rowsCount = diaryService.getDiaryCount(Diary.class, citycode, Diary.DIARY_TYPE_COMMENT, "movie", movieId);
			diaryList = diaryService.getDiaryList(Diary.class, citycode, DiaryConstant.DIARY_TYPE_COMMENT, "movie", movieId, first, rowsPerPage, myOrder);
		}
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(diaryList));
		map.put("diaryList", diaryList);
		map.put("cacheMemberMap", model.get("cacheMemberMap"));
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(map));
	}
	
	@RequestMapping("/subject/proxy/winnerInfoList.xhtml")
	public String winnerInfoList(String tag, int limit, String prizeType, ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null) return showJsonError(model, "参数错误！");
		if(StringUtils.isBlank(prizeType)) prizeType = "A,D,P,drama,remark,waibi";
		List<Prize> pList = drawActivityService.getPrizeListByDid(da.getId(), StringUtils.split(prizeType, ","));
		List<Long> pIdList = BeanUtil.getBeanPropertyList(pList,Long.class,"id",true);
		List<WinnerInfo> infoList = drawActivityService.getWinnerList(da.getId(),pIdList, null , null, "system",null,null,null,0,limit);
		List<Map> winnerMapList = BeanUtil.getBeanMapList(infoList, new String[]{"memberid", "nickname", "prizeid","addtime"});
		for(Map info : winnerMapList){
			Prize prize = daoService.getObject(Prize.class, Long.valueOf(info.get("prizeid")+""));
			info.put("plevel", prize.getPlevel());
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(winnerMapList));
	}
	
	@RequestMapping("/subject/proxy/pictureListBymid.xhtml")
	public String pictureListBymid(String movieId , ModelMap model){
		if(StringUtils.isBlank(movieId)) return showJsonError(model, "noMovieId");
		Movie movie = daoService.getObject(Movie.class, Long.valueOf(movieId));
		if (movie == null)
			return showJsonError(model, "该影片不存在或被删除！");
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(TagConstant.TAG_MOVIE, Long.valueOf(movieId), 0, 1000);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(pictureList));
	}
	
	@RequestMapping("/subject/proxy/recommendationSubject.xhtml")
	public String recommendationSubject(String actName,String keywords,String desc,String pageURL,String flag,String userId,String check,ModelMap model){
		String checkcode = StringUtil.md5(userId + "rcs!@#");
		if(!StringUtils.equals(check, checkcode)) return showJsonSuccess(model, "nologin");
		SpecialActivity specialActivity = new SpecialActivity("");
		specialActivity.setActivityname(actName);
		specialActivity.setSeokeywords(keywords);
		specialActivity.setSeodescription(desc);
		specialActivity.setWebsite(pageURL);
		specialActivity.setFlag(flag);
		try {
			daoService.saveObject(specialActivity);
		} catch (Exception e) {
			return showJsonError(model, e.getMessage());
		}
		return showJsonSuccess(model,"发布成功！"); 
	}
	
	@RequestMapping("/subject/proxy/addDeliveryAddress.xhtml")
	public String addDeliveryAddress(String memberid,String check,String tag, String realname, String email, String sex, String address,String mobile,ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) return showJsonError(model, "nologin");
		String opkey = memberid+"addDeliveryAddress";
		boolean allow = operationService.updateOperation(opkey, 10, 20);
		if(!allow) return showJsonError(model, "操作太频繁！");
		if(StringUtils.isBlank(tag)) return showJsonError(model, "非法操作！");
		if(StringUtils.isBlank(realname)) return showJsonError(model, "姓名不能为空！");
		if(WebUtils.checkString(realname)) return showJsonError(model, "姓名中含有非法字符！");
		if(StringUtils.isNotBlank(email)&&!ValidateUtil.isEmail(email)) return showJsonError(model, "邮箱格式错误！");
		if(StringUtils.isNotBlank(sex)&&StringUtil.getByteLength(sex)>2) return showJsonError(model, "性别错误！");
		if(StringUtils.isBlank(address)) return showJsonError(model, "地址内容不能为空！");
		if(StringUtil.getByteLength(address)>400) return showJsonError(model, "地址内容过长！");
		if(WebUtils.checkString(address)) return showJsonError(model, "地址中含有非法字符！");
		if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式有误！");

		Map winning = new HashMap();
		winning.put(MongoData.ACTION_TAG, tag);
		winning.put(MongoData.GEWA_CUP_MEMBERID, Long.valueOf(memberid));
		mongoService.removeObjectList(MongoData.NS_WINNING_RECEIPT_INFO, winning);
		winning.put(MongoData.SYSTEM_ID, System.currentTimeMillis() + StringUtil.getRandomString(5));
		winning.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestamp());
		winning.put(MongoData.FIELD_REAL_NAME, realname);
		winning.put(MongoData.FIELD_TELEPHONE, mobile);
		winning.put(MongoData.FIELD_RECEIPT_ADDRESS, address);
		if (StringUtils.isNotBlank(email)) {
			winning.put(MongoData.FIELD_EMAIL, email);
		}
		if (StringUtils.isNotBlank(sex)) {
			winning.put(MongoData.FIELD_SEX, sex);
		}
		mongoService.saveOrUpdateMap(winning, MongoData.SYSTEM_ID, MongoData.NS_WINNING_RECEIPT_INFO);
		return showJsonSuccess(model,"success");
	}
	
	
	/**
	 * 上海电影艺术联盟获取排片数据
	 * @param citycode
	 * @param cinemaIds
	 * @param movieIds
	 * @param from
	 * @param maxnum
	 * @return
	 */
	@RequestMapping("/subject/proxy/getArtFilmAllianceOpi.xhtml")
	public String getArtFilmAllianceOpi(String citycode,String cinemaIds, String movieIds, int from, int maxnum, ModelMap model){
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		if (from < 0) {
			from = 0;
		}
		if (maxnum <= 0 || maxnum >= 200) {
			maxnum = 200;
		}
		List<OpenPlayItem> opiList = openPlayService.getArtFilmAllianceOpi(citycode, cinemaIds, movieIds, from, maxnum);
		result = BeanUtil.getBeanMapList(opiList, true);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(result));
	}
	

	//分享微博
	@RequestMapping("/subject/proxy/shareInfo.xhtml")
	public String sharesTicketInfo(Long memberid, String check, String content, String picurl, ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		if(!StringUtils.equals(check, checkcode)) {
			return showJsonError(model, "请先登录！");
		}
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) {
			return showJsonError(model, "请先登录！");
		}
		if(StringUtils.isBlank(content)) {
			return showJsonError(model, "分享内容不能为空！");
		}
		boolean allow = operationService.updateOperation("shareFilmFest" + member.getId(), OperationService.HALF_MINUTE, 5);
		if(!allow) {
			return showJsonError(model, "你操作过于频繁，请稍后再试！");
		}
		List<ShareMember>  shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA), member.getId());
		if(shareMemberList.isEmpty()) {
			return showJsonError(model, "请先绑定微博！");
		}
		shareService.sendShareInfo("subject", null, member.getId(), content, picurl);
		return showJsonSuccess(model);
	}
	//得到知道
	@RequestMapping("/subject/proxy/getQuestionList.xhtml")
	public String getQuestionList(String tag, Long relatedid, ModelMap model){
		List<GewaQuestion> questionList = qaService.getQuestionByCategoryAndCategoryid(null, tag, relatedid, -1, -1);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(questionList));
	}
	//得到知道的答案
	@RequestMapping("/subject/proxy/getAnswerList.xhtml")
	public String getQuestionList(String qids, Integer maxnum, ModelMap model){
		if(maxnum == null) maxnum = 3;
		Map<Long,List<GewaAnswer>> answersMap = new HashMap<Long,List<GewaAnswer>>();
		String[] ids = StringUtils.split(qids, ",");
		Long answerMemberid = qaService.getGewaraAnswerByMemberid();
		if(ids != null){
			for (String id : ids) {
				int max = maxnum;
				Long qid = Long.parseLong(id);
				List<GewaAnswer> answers = new ArrayList<GewaAnswer>();
				//先查找管理员的回复
				List<GewaAnswer> ga = qaService.getAnswerListByQuestionAndMemId(0, 1, qid, answerMemberid);
				if(ga!=null&&!ga.isEmpty()){
					answers.add(ga.get(0));
					max = 2;
				}
				List<GewaAnswer> gac = qaService.getAnswerListByQuestionId(0, max, qid);
				answers.addAll(gac);
				answersMap.put(qid, answers);
			}
		}
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(answersMap));
	}
	
	@RequestMapping("/subject/proxy/getMuOrderCount.xhtml")
	public String getMuOrderCount(String memberids, Long relatedid, Timestamp fromtime, Timestamp totime, ModelMap model){
		if(fromtime == null) fromtime = DateUtil.getMonthFirstDay(DateUtil.getCurFullTimestamp());
		if(totime == null) totime = DateUtil.getCurFullTimestamp();
		List<Long> idList = BeanUtil.getIdList(memberids, ","); 
		int count = orderQueryService.getMUOrderCountByMbrids(idList, relatedid, fromtime, totime);
		return showJsonSuccess(model, count+"");
	}
	
	@RequestMapping("/subject/proxy/getMemberWinnerCount.xhtml")
	public String getMemberWinnerCount(String tag, Long memberid, ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, true);
		if(da == null || !da.isJoin() || memberid == null) return showJsonSuccess(model, "0");
		int count = drawActivityService.getMemberWinnerCount(memberid, da.getId(), da.getStarttime(), da.getEndtime());
		return showJsonSuccess(model, ""+count);
	}
	
	@RequestMapping("/subject/proxy/getMemberFirstTime.xhtml")
	public String getMemberFirstTime(Long memberid, String citycode, ModelMap model){
		Map<String , Object> firstInfo = new HashMap<String, Object>();
		MemberInfo mi = daoService.getObject(MemberInfo.class, memberid);
		firstInfo.put("addTime", mi.getAddtime());
		Map map = mongoService.findOne(MongoData.NS_FIRSTORDER, MongoData.SYSTEM_ID, memberid);
		if (map != null && map.get("ticket") != null) {
			TicketOrder order = daoService.getObjectByUkey(TicketOrder.class, "tradeNo", map.get("ticket")+"");
			if (order != null) {
				firstInfo.put("orderTime", order.getCreatetime());
				Movie movie = daoService.getObject(Movie.class, order.getMovieid());
				firstInfo.put("movieName", movie.getName());
			}
		}
		int movieCount = orderQueryService.getMemberTicketCountByMemberid(memberid,  DateUtil.parseTimestamp("2012-11-22", "yyyy-MM-dd"), DateUtil.parseTimestamp("2013-11-28", "yyyy-MM-dd"), OrderConstant.STATUS_PAID_SUCCESS, citycode);
		firstInfo.put("movieCount", movieCount);
		return showJsonSuccess(model , JsonUtils.writeObjectToJson(firstInfo));
	}
	
	@RequestMapping("/subject/proxy/getColorBall.xhtml")
	public String getColorBall(Long memberid,String mobile,String msgContent, ModelMap model) {
		ErrorCode<SMSRecord> code = drawActivityService.getColorBall(memberid, mobile, msgContent);
		if (!code.isSuccess()) {
			return showJsonError(model, code.getErrcode());
		}
		daoService.saveObject(code.getRetval());
		untransService.sendMsgAtServer(code.getRetval(), false);
		return showJsonSuccess(model,"success");
	}
	
	@RequestMapping("/subject/proxy/getCouponCode.xhtml")
	public String getCouponCode(Long memberid,String mobile,String msgContent, ModelMap model) {
		ErrorCode<SMSRecord> code = drawActivityService.getCouponCode(memberid, mobile, msgContent);
		if (!code.isSuccess()) {
			return showJsonError(model, code.getErrcode());
		}
		daoService.saveObject(code.getRetval());
		untransService.sendMsgAtServer(code.getRetval(), false);
		return showJsonSuccess(model,"success");
	}
}