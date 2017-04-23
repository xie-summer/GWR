package com.gewara.web.action.subject.admin;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.SubjectActivity;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.common.JsonData;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.drama.Drama;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.SpecialActivity;
import com.gewara.model.pay.PubSale;
import com.gewara.model.sport.Sport;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.service.JsonDataService;
import com.gewara.service.content.RecommendService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.support.ErrorCode;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class SubjectAdminController extends BaseAdminController {
	@Autowired@Qualifier("recommendService")
	private RecommendService recommendService;

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("jsonDataService")
	private JsonDataService jsonDataService = null;
	public void setJsonDataService(JsonDataService jsonDataService) {
		this.jsonDataService = jsonDataService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	
	// 专题首页
	@RequestMapping("/admin/subject/index.xhtml")
	public String index() {
		return "admin/subject/index.vm";
	}
	
	// 5元抢票内容
	@RequestMapping("/admin/subject/price5Detail.xhtml")
	public String price5Detail(ModelMap model, Long gid, String tag) {
		if(gid!=null){
			GrabTicketSubject subject = daoService.getObject(GrabTicketSubject.class, gid);
			model.put("subject", subject);
			tag = subject.getTag();
		}
		model.put("tag", tag);
		return "admin/subject/price5Detail.vm";
	}
	// 5元抢票
	@RequestMapping("/admin/subject/price5.xhtml")
	public String price5(ModelMap model, String tag, HttpServletRequest request) {
		DetachedCriteria query = DetachedCriteria.forClass(GrabTicketSubject.class);
		query.add(Restrictions.eq("tag", tag));
		query.add(Restrictions.eq("citycode", getAdminCitycode(request)));
		query.addOrder(Order.desc("addtime"));
		List<GrabTicketSubject> subjectList = commonService.getGrabTicketSubjectList(getAdminCitycode(request), tag, 0, 200);
		model.put("subjectList", subjectList);
		model.put("tag", tag);
		return "admin/subject/price5.vm";
	}
	// 专题开放的场次
	@RequestMapping("/admin/subject/mpi.xhtml")
	public String mpi(ModelMap model, Long sid) {
		GrabTicketSubject subject = daoService.getObject(GrabTicketSubject.class, sid);
		model.put("subject", subject);
		List<GrabTicketMpi> gtmList = daoService.getObjectListByField(GrabTicketMpi.class, "sid", sid);
		List<Long> mpidList = BeanUtil.getBeanPropertyList(gtmList, Long.class, "mpid", true);
		
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for(Long mpid: mpidList){
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
			if(opi!=null) opiList.add(opi);
		}
		
		List<Movie> movieList = new ArrayList<Movie>();
		Map<Long/*movieid*/, List<OpenPlayItem>> movieOpiMap = BeanUtil.groupBeanList(opiList, "movieid");
		for(Long movieid : movieOpiMap.keySet()){
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieList.add(movie);
		}
		Map<Long, Cinema> cinemaMap = daoService.getObjectMap(Cinema.class, 
				BeanUtil.getBeanPropertyList(opiList, Long.class, "cinemaid", true));
		model.put("cinemaMap", cinemaMap);
		model.put("movieOpiMap", movieOpiMap);
		model.put("movieList", movieList);
		model.put("nowtime", new Timestamp(System.currentTimeMillis()));
		return "admin/subject/mpi.vm";
	}
	
	
	
	
	/**
	 *  最新专题
	 * */
	@RequestMapping("/admin/newsubject/index.xhtml")
	public String newsubjectindex(){
		return "admin/newsubject/index.vm";
	}
	
	// 专题列表
	@RequestMapping("/admin/newsubject/newSubjectList.xhtml")
	public String newSubjectList(ModelMap model, String tag) {
		if(StringUtils.isBlank(tag)) return showError(model, "缺少组标签!");
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SUBJECT);
		params.put(MongoData.ACTION_TAG, tag);
		List<Map> list = mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true);
		model.put("list", list);
		return "admin/newsubject/newSubjectList.vm";
	}
	// 添加专题
	@RequestMapping("/admin/newsubject/addSmallSubject.xhtml")
	public String addSmallSubject(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			if(data != null) model.put("data", data);
		}
		return "admin/newsubject/addSmallSubjectForm.vm";
	}
	private int validRelated(Long relatedid){
		Movie movie = daoService.getObject(Movie.class, relatedid);
		if(movie == null) return -1;
		return 1;
	}
	// 验证
	@RequestMapping("/admin/newsubject/checkSubjectRelatedid.xhtml")
	public String checkSubjectRelatedid(Long relatedid, ModelMap model){
		int count = validRelated(relatedid);
		if(count < 0){
			return showJsonError(model, "关联影片信息不存在!");
		}
		return showJsonSuccess(model);
	}
	
	// 专题保存
	@RequestMapping("/admin/newsubject/saveSmallSubject.xhtml")
	public String saveSmallSubject(String id, String tag, String title, Long relatedid, String newstitle, String newssubject, String newslogo, String newslink, ModelMap model){
		// 验证
		int countRelatedid = validRelated(relatedid);
		if(countRelatedid < 0){
			return showJsonError(model, "关联影片信息不存在!");
		}
		
		Map params = new HashMap();
		int ordernum = 0;
		Date addtime = DateUtil.currentTime();
		if(StringUtils.isNotBlank(id)){
			params = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id); 
			ordernum = (Integer) params.get(MongoData.ACTION_ORDERNUM);
			addtime = (Date) params.get(MongoData.ACTION_ADDTIME);
		}else{
			params.put(MongoData.DEFAULT_ID_NAME, ServiceHelper.assignID(tag));
			params.put(MongoData.SYSTEM_ID, MongoData.buildId());
		}
		params.put(MongoData.ACTION_TAG, tag);
		params.put(MongoData.ACTION_TITLE, title);
		params.put(MongoData.ACTION_RELATEDID, relatedid);
		params.put(MongoData.ACTION_NEWSTITLE, newstitle);
		params.put(MongoData.ACTION_NEWSSUBJECT, newssubject);
		params.put(MongoData.ACTION_NEWSLOGO, newslogo);
		params.put(MongoData.ACTION_NEWSLINK, newslink);
		params.put(MongoData.ACTION_ORDERNUM, ordernum);
		params.put(MongoData.ACTION_ADDTIME, addtime);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SUBJECT);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/changeOrderNum.xhtml")
	public String changeOrderNum(String id, Integer ordernum, ModelMap model){
		Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
		if(data != null){
			data.put(MongoData.ACTION_ORDERNUM, ordernum);
			mongoService.saveOrUpdateMap(data, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
			return showJsonSuccess(model);
		}
		return showJsonError_NOT_FOUND(model);
	}
	
	@RequestMapping("/admin/newsubject/delSubject.xhtml")
	public String delSubject(String bsid, ModelMap model){
		// 自动寻找所有关联关系, 删除
		// 最大父类
		Map params = new HashMap();
		params.put(MongoData.ACTION_PARENTID, bsid);
		mongoService.removeObjectList(MongoData.NS_MAINSUBJECT, params);
		
		boolean issuc = mongoService.removeObjectById(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, bsid);
		if(issuc) return showJsonSuccess(model);
		return showJsonError_DATAERROR(model);
	}
	//把专题推荐到前台资讯
	@RequestMapping("/admin/newsubject/recommendationSubject.xhtml")
	public String recommendationSubject(String id, ModelMap model){
		Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
		SpecialActivity specialActivity = new SpecialActivity("");
		if(StringUtils.isNotBlank(data.get(MongoData.ACTION_TITLE)+""))specialActivity.setActivityname(data.get(MongoData.ACTION_TITLE)+"");
		if(StringUtils.isNotBlank(data.get(MongoData.ACTION_SEOKEYWORDS)+""))specialActivity.setSeokeywords(data.get(MongoData.ACTION_SEOKEYWORDS)+"");
		if(StringUtils.isNotBlank(data.get(MongoData.ACTION_SEODESCRIPTION)+""))specialActivity.setSeodescription(data.get(MongoData.ACTION_SEODESCRIPTION)+"");
		String url = config.getBasePath()+"subject/u/"+id;
		specialActivity.setWebsite(url);
		daoService.saveObject(specialActivity);
		return showJsonSuccess(model);
	}
	// 关联新闻
	@RequestMapping("/admin/newsubject/subjectNewsList.xhtml")
	public String subjectNewsList(String id, ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_RECOMMEND);
		params.put(MongoData.ACTION_RELATEDID, id);
		List<Map> list = mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true);
		model.put("list", list);
		return "admin/newsubject/newSmallSubjectList.vm";
	}
	
	@RequestMapping("/admin/newsubject/gcDetail.xhtml")
	public String gcDetail(String id, ModelMap model) {
		if(StringUtils.isNotBlank(id)) {
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			if(data != null){
				model.put("data", data);
			}
		}
		return "admin/newsubject/gcDetail.vm";
	}
	@RequestMapping("/admin/newsubject/savegcDetail.xhtml")
	public String savegcDetail(String id, String tag, String signname, String newstitle, String newssubject, String newslogo, String newssmalllogo, String newslink, String relatedid, String parentid, String newsboard, String boardrelatedid, ModelMap model){
		Map params = new HashMap();
		int ordernum = 0;
		Date addtime = DateUtil.currentTime();
		if(StringUtils.isNotBlank(id)){
			params = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			if(params.get(MongoData.ACTION_ORDERNUM) != null){
				ordernum = (Integer)params.get(MongoData.ACTION_ORDERNUM);
			}
			if(params.get(MongoData.ACTION_ADDTIME) != null){
				addtime = (Date)params.get(MongoData.ACTION_ADDTIME);
			}
		}else{
			params.put(MongoData.DEFAULT_ID_NAME, ServiceHelper.assignID(tag+signname));
			params.put(MongoData.SYSTEM_ID, MongoData.buildId());
		}
		params.put(MongoData.ACTION_TAG, tag);
		params.put(MongoData.ACTION_SIGNNAME, signname);
		params.put(MongoData.ACTION_NEWSTITLE, StringUtils.trim(newstitle));
		params.put(MongoData.ACTION_NEWSSUBJECT, newssubject);
		params.put(MongoData.ACTION_NEWSLOGO, newslogo);
		params.put(MongoData.ACTION_NEWSSMALLLOGO, newssmalllogo);
		params.put(MongoData.ACTION_NEWSLINK, newslink);
		params.put(MongoData.ACTION_RELATEDID, relatedid);
		params.put(MongoData.ACTION_PARENTID, parentid);
		params.put(MongoData.ACTION_NEWSBOARD, newsboard);
		params.put(MongoData.ACTION_BOARDRELATEDID, boardrelatedid);
		params.put(MongoData.ACTION_ORDERNUM, ordernum);
		params.put(MongoData.ACTION_ADDTIME, addtime);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_RECOMMEND);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
		
		return showJsonSuccess(model);
	}
	
	
	/***
	 *  赛车2专题
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_cars.xhtml")
	public String newSubjectList_cars(){
		return "admin/newsubject/cars_index.vm";
	}
	/***
	 *  中秋专题
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_zhongqiu.xhtml")
	public String newSubjectList_zhongqiu(){
		return "admin/newsubject/zhongqiu_index.vm";
	}
	/***
	 * 万圣节专题
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_hallowmas.xhtml")
	public String newSubjectList_hallowmas(){
		return "admin/newsubject/hallowmas_index.vm";
	}
	/***
	 * 光棍节专题
	 * */
	@RequestMapping("/admin/newsubject/newSubjectList_singles.xhtml")
	public String newSubjectList_singles(ModelMap model){
		Map params = new HashMap();
		params.put("tag", MongoData.SINGLE_TIMES);
		Map timeMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, params);
		model.put("timeMap", timeMap);
		return "admin/newsubject/singles_index.vm";
	}
	//链接给参数
	@RequestMapping("/admin/newsubject/ssDetail.xhtml")
	public String ssDetail(String id, String type, String parentid, ModelMap model) {
		if(StringUtils.isNotBlank(id)) {
			Map data = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
			if(data != null){
				model.put("data", data);
			}
		}
		model.put("type", type);
		model.put("parentid",parentid);
		return "admin/newsubject/ssDetail.vm";
	}
	//推荐光棍节
	@RequestMapping("/admin/newsubject/commonSubjectList_singles.xhtml")
	public String commonSubjectList_singles(String type,String parentid, ModelMap model){
		Map<String, Object> param = new HashMap<String, Object>();
		param.put(MongoData.ACTION_TYPE, type);
		if(StringUtils.isNotBlank(parentid))param.put(MongoData.SINGLES_FOREIGNID,parentid);
		List<Map> dataMap = mongoService.find(MongoData.NS_ACTIVITY_SINGLES, param);
		if(!StringUtils.equals(type, MongoData.VALENTINE_SESSION_CINEMA)){
			for(Map data : dataMap){
				if(type.equals("cinema")){
					BaseObject object = daoService.getObject(Cinema.class, new Long(data.get(MongoData.SINGLES_FOREIGNID)+""));
					if(object != null){
						data.put("object", object);
					}
				}else if(type.equals("goods")){
					BaseObject object = daoService.getObject(Goods.class, new Long(data.get(MongoData.SINGLES_FOREIGNID)+""));
					if(object != null){
						data.put("object", object);
					}
				}else if(type.equals("pubsale")){
					BaseObject object = daoService.getObject(PubSale.class, new Long(data.get(MongoData.SINGLES_FOREIGNID)+""));
					if(object != null){
						data.put("object", object);
					}
				}else if(type.equals("doubleFestival")){
					GoodsGift goodsGift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", new Long(data.get(MongoData.SINGLES_FOREIGNID)+""), true);
					if(goodsGift != null){
						if(goodsGift.getCinemaid() != null){
							Cinema cinema = daoService.getObject(Cinema.class, goodsGift.getCinemaid());
							data.put("cinema", cinema);
						}
						if(goodsGift.getGoodsid() != null){
							Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
							data.put("goods", goods);
						}
						if(goodsGift.getMovieid() != null){
							Movie movie = daoService.getObject(Movie.class, goodsGift.getMovieid());
							data.put("movie", movie);
						}
					}
				}
			}
		}
		model.put("parentid", parentid);
		model.put("type", type);
		model.put("dataMap", dataMap);
		return "admin/newsubject/commonSubjectList_singles.vm";
	}
	// 光棍节添加抢票影院
	@RequestMapping("/admin/newsubject/add_singles.xhtml")
	public String checkBoardDataSingles(String foreignid, String type, String tag, String id, String cinemaurl, String goodsname,String goodscontent,
			String oldprice,String gewaprice, String newslogo, String count, ModelMap model){
		if(StringUtils.isBlank(tag))tag = MongoData.SINGLE_TIMES;
		if(foreignid == null) return showJsonError(model,"ID不能为空!");
		Map saveMap = new HashMap();
		if(StringUtils.isNotBlank(id)){
			saveMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
		}else{
			saveMap.put(MongoData.SYSTEM_ID, MongoData.buildId());
			saveMap.put(MongoData.DEFAULT_ID_NAME,"singles"+System.currentTimeMillis());
			saveMap.put(MongoData.ACTION_ADDTIME, DateUtil.currentTime());
		}
		if(StringUtils.equals(type, "session_cinema")){
			saveMap.put(MongoData.ACTION_COUNT, count);
			saveMap.put(MongoData.ACTION_NEWSLOGO, newslogo);
			saveMap.put("goodscontent", goodscontent);
			saveMap.put("gewaprice", gewaprice);
		}else{
			saveMap.put("random", RandomUtils.nextInt(50)+50);
			saveMap.put(MongoData.ACTION_STATUS, "Y");
			saveMap.put(MongoData.ACTION_TAG, tag);
			saveMap.put(MongoData.ACTION_ORDERNUM, "0");
		}
		saveMap.put(MongoData.ACTION_TYPE, type);
		saveMap.put(MongoData.SINGLES_FOREIGNID, foreignid+"");
		saveMap.put(MongoData.SINGLES_CINEMAURL, cinemaurl);
		if(StringUtils.equals(type, "doubleFestival")){
			GoodsGift goodsGift = daoService.getObjectByUkey(GoodsGift.class, "goodsid", new Long(foreignid), true);
			if(goodsGift != null ){
				if(goodsGift.getCinemaid()!=null){
					Cinema cinema = daoService.getObject(Cinema.class, goodsGift.getCinemaid());
					saveMap.put("countycode", cinema.getCountycode());
				}
			}
			saveMap.put("goodsname", goodsname);
			saveMap.put("goodscontent", goodscontent);
			saveMap.put("oldprice", oldprice);
			saveMap.put("gewaprice", gewaprice);
		}
		mongoService.saveOrUpdateMap(saveMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTIVITY_SINGLES);
		return showJsonSuccess(model);
	}
	//光棍节抢票影院排序
	@RequestMapping("/admin/newsubject/changeOrderNum_singles.xhtml")
	public String changeOrderNumSingles(String id, Integer ordernum, ModelMap model){
		Map data = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
		if(data != null){
			data.put(MongoData.ACTION_ORDERNUM, ordernum);
			mongoService.saveOrUpdateMap(data, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTIVITY_SINGLES);
			return showJsonSuccess(model);
		}
		return showJsonError_NOT_FOUND(model);
	}
	//光棍节删除
	@RequestMapping("/admin/newsubject/delSubject_singles.xhtml")
	public String delSubjectSingles(String id,String type, ModelMap model){
		if(StringUtils.equals(type, "session_cinema")){
			mongoService.removeObjectById(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
			return showJsonSuccess(model);
		}
		Map saveMap = mongoService.findOne(MongoData.NS_ACTIVITY_SINGLES, MongoData.DEFAULT_ID_NAME, id);
		if(saveMap == null)	 return showJsonError(model, "");
		if(StringUtils.equals(String.valueOf(saveMap.get("status")), Status.Y)){
			saveMap.put("status", Status.N);
		}else saveMap.put("status", Status.Y);
		mongoService.saveOrUpdateMap(saveMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_ACTIVITY_SINGLES);
		return showJsonSuccess(model);
	}
	
	
	// 推荐列表
	@RequestMapping("/admin/newsubject/commonSubjectList.xhtml")
	public String commonSubjectList(String tag, String signname, String page, String parentid, ModelMap model){
		List<Map> dataMap = recommendService.getRecommendMap(tag, signname, parentid);
		if(StringUtils.isNotBlank(page)){
			initDataMap(dataMap, page);
		}
		model.put("list", dataMap);
		return "admin/newsubject/commonSmallSubjectList.vm";
	}
	private void initDataMap(List<Map> dataMap, String page){
		for(Map data : dataMap){
			Object object = relateService.getRelatedObject(page, new Long(data.get(MongoData.ACTION_BOARDRELATEDID).toString()));
			if(object != null){
				data.put("object", object);
			}
		}
	}
	
	/*****
	 *  单一影片固定模板
	 * */
	@RequestMapping("/admin/newsubject/simpleSubjectTemplate.xhtml")
	public String simpleSubjectTemplate(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SIMPLETEMPLATE);
		List<Map> list = mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ADDTIME, false);
		model.put("list", list);
		return "admin/newsubject/simpleSubjectList.vm";
	}
	// 添加单一影片固定模板
	@RequestMapping("/admin/newsubject/addSimpleSubject.xhtml")
	public String addSimpleSubject(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			if(data != null) model.put("data", data);
		}
		return "admin/newsubject/addSimpleSubjectForm.vm";
	}
	private String checkBoardData(String board, Long relatedid){
		Object object = relateService.getRelatedObject(board, relatedid);
		if(object instanceof Movie){
			return ((Movie)object).getMoviename();
		}else if(object instanceof HeadInfo){
			return ((HeadInfo)object).getTitle();
		}else if(object instanceof Drama){
			return ((Drama)object).getDramaname();
		}else if(object instanceof Diary){
			return ((Diary)object).getSubject();
		}else if(object instanceof Sport){
			return ((Sport)object).getName();
		}else if(object instanceof RemoteGym){
			return ((RemoteGym)object).getName();
		}else if(object instanceof RemoteActivity){
			return ((RemoteActivity)object).getTitle();
		}
		return null;
	}
	// 检测是否是对应版块里的数据
	@RequestMapping("/admin/newsubject/checkBoardData.xhtml")
	public String checkBoardData(String board, Long relatedid, ModelMap model){
		String returnData = checkBoardData(board, relatedid);
		if(returnData == null){
			return showJsonError_NOT_FOUND(model);
		}
		return showJsonSuccess(model, returnData);
	}
	// 主模板保存
	@RequestMapping("/admin/newsubject/saveSimpleSubject.xhtml")
	public String saveSimpleSubject(String id, String board,String seokeywords, String seodescription, String tag, String title, Long relatedid, String newstitle, String newslogo, String walatitle, ModelMap model){
		// 验证
		String returnData = checkBoardData(board, relatedid);
		if(returnData == null){
			return showJsonError_NOT_FOUND(model);
		}
		
		Map params = new HashMap();
		int ordernum = 0;
		Date addtime = DateUtil.currentTime();
		if(StringUtils.isNotBlank(id)){
			params = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id); 
			ordernum = (Integer) params.get(MongoData.ACTION_ORDERNUM);
			addtime = (Date) params.get(MongoData.ACTION_ADDTIME);
		}else{
			params.put(MongoData.DEFAULT_ID_NAME, ServiceHelper.assignID(board+relatedid));
		}
		params.put(MongoData.ACTION_BOARD, board);
		params.put(MongoData.ACTION_TAG, tag);
		params.put(MongoData.ACTION_TITLE, title);
		params.put(MongoData.ACTION_RELATEDID, relatedid);
		params.put(MongoData.ACTION_NEWSTITLE, newstitle);
		params.put(MongoData.ACTION_NEWSLOGO, newslogo);
		params.put(MongoData.ACTION_WALATITLE, walatitle);
		params.put(MongoData.ACTION_ORDERNUM, ordernum);
		params.put(MongoData.ACTION_ADDTIME, addtime);
		params.put(MongoData.ACTION_SEOKEYWORDS, seokeywords);
		params.put(MongoData.ACTION_SEODESCRIPTION, seodescription);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SIMPLETEMPLATE);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
		
		return showJsonSuccess(model);
	}
	// 子模块配置(简单版)
	@RequestMapping("/admin/newsubject/subSubjectList.xhtml")
	public String subSubjectList(ModelMap model, String parentid){
		model.put("parentid", parentid);
		return "admin/newsubject/subSubjectList.vm";
	}
	
	
	/****************
	 *  统一专题模板
	 * */
	@RequestMapping("/admin/newsubject/unionSubjectTemplate.xhtml")
	public String unionSubjectTemplate(ModelMap model){
		Map params = new HashMap();
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_UNIONTEMPLATE);
		List<Map> list = mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ADDTIME, false);
		model.put("list", list);
		return "admin/newsubject/unionSubjectList.vm";
	}
	// 添加统一专题模板
	@RequestMapping("/admin/newsubject/addUnionSubject.xhtml")
	public String addUnionSubject(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			if(data != null) model.put("data", data);
		}
		return "admin/newsubject/addUnionSubjectForm.vm";
	}
	// 统一专题模板_主模板保存
	@RequestMapping("/admin/newsubject/saveUnionSubject.xhtml")
	public String saveUnionSubject(String id, String subjecttype,String seokeywords, String seodescription, String linkcolor, String board, String tag, String title, Long relatedid, String newstitle, String newslogo, String walatitle, ModelMap model){
		// 验证
		String returnData = checkBoardData(board, relatedid);
		if(returnData == null){
			return showJsonError_NOT_FOUND(model);
		}
		Map params = new HashMap();
		int ordernum = 0;
		Date addtime = DateUtil.currentTime();
		if(StringUtils.isNotBlank(id)){
			params = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id); 
			ordernum = (Integer) params.get(MongoData.ACTION_ORDERNUM);
			addtime = (Date) params.get(MongoData.ACTION_ADDTIME);
		}else{
			params.put(MongoData.DEFAULT_ID_NAME, MongoData.buildId());
		}
		params.put(MongoData.ACTION_SUBJECTTYPE, subjecttype);
		params.put(MongoData.ACTION_BOARD, board);
		params.put(MongoData.ACTION_TAG, tag);
		params.put(MongoData.ACTION_TITLE, title);
		params.put(MongoData.ACTION_LINKCOLOR, linkcolor);
		params.put(MongoData.ACTION_RELATEDID, relatedid);
		params.put(MongoData.ACTION_NEWSTITLE, newstitle);
		params.put(MongoData.ACTION_NEWSLOGO, newslogo);
		params.put(MongoData.ACTION_WALATITLE, walatitle);
		params.put(MongoData.ACTION_ORDERNUM, ordernum);
		params.put(MongoData.ACTION_ADDTIME, addtime);
		params.put(MongoData.ACTION_SEOKEYWORDS, seokeywords);
		params.put(MongoData.ACTION_SEODESCRIPTION, seodescription);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_UNIONTEMPLATE);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
		
		return showJsonSuccess(model);
	}
	// 统一专题模板_子模块配置
	@RequestMapping("/admin/newsubject/subUnionSubjectList.xhtml")
	public String subUnionSubjectList(ModelMap model, String parentid){
		// 0, 上通栏
		model.put("topSublist", getSubUnionSubList("T", parentid));
		// 1, 左模块
		model.put("leftSublist", getSubUnionSubList("L", parentid));
		// 2, 右模块
		model.put("rightSublist", getSubUnionSubList("R", parentid));
		// 3, 下通栏
		model.put("bottomSublist", getSubUnionSubList("B", parentid));
		return "admin/newsubject/subUnionSubjectList.vm";
	}
	private List<Map> getSubUnionSubList(String board, String parentid){
		Map params = new HashMap();
		params.put(MongoData.ACTION_BOARD, board);
		params.put(MongoData.ACTION_PARENTID, parentid);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SUBUNIONTEMPLATE);
		return mongoService.find(MongoData.NS_MAINSUBJECT, params, MongoData.ACTION_ORDERNUM, true);
	}
	
	// 子模块间的加载
	@RequestMapping("/admin/newsubject/addSubTemplate.xhtml")
	public String addSubTemplate(String id, String flag, String parentid, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			model.put("data", mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id)); 
			model.put("id", id);
		}
		if(StringUtils.isNotBlank(parentid)){
			model.put("parent", mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, parentid));
			model.put("parentid", parentid);
		}
		model.put("flag", flag);
		return "admin/newsubject/subTemplateDetail.vm";
	}
	// 更多
	@RequestMapping("/admin/newsubject/addSubTemplateMoreLink.xhtml")
	public String addSubTemplateMoreLink(String id, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			model.put("data", mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id)); 
		}else{
			return showJsonError_DATAERROR(model);
		}
		return "admin/newsubject/subTemplateMore.vm";
	}
	@RequestMapping("/admin/newsubject/saveSubTemplateMoreLink.xhtml")
	public String saveSubTemplateMoreLink(String id, String newslink, ModelMap model){
		if(StringUtils.isNotBlank(id)){
			Map data = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id);
			data.put(MongoData.ACTION_NEWSLINK, newslink);
			mongoService.saveOrUpdateMap(data, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
			return showJsonSuccess(model);
		}
		return showJsonError_DATAERROR(model);
	}
	
	@RequestMapping("/admin/newsubject/saveSubTemplate.xhtml")
	public String saveSubTemplate(String id, String parentid, String board, String subjecttype, String title, ModelMap model){
		Map params = new HashMap();
		int ordernum = 0;
		Date addtime = DateUtil.currentTime();
		if(StringUtils.isNotBlank(id)){
			params = mongoService.findOne(MongoData.NS_MAINSUBJECT, MongoData.DEFAULT_ID_NAME, id); 
			ordernum = (Integer) params.get(MongoData.ACTION_ORDERNUM);
			addtime = (Date) params.get(MongoData.ACTION_ADDTIME);
		}else{
			params.put(MongoData.DEFAULT_ID_NAME, ServiceHelper.assignID(parentid));
		}
		params.put(MongoData.ACTION_SUBJECTTYPE, subjecttype);
		params.put(MongoData.ACTION_PARENTID, parentid);
		params.put(MongoData.ACTION_BOARD, board);
		params.put(MongoData.ACTION_TITLE, title);
		params.put(MongoData.ACTION_ORDERNUM, ordernum);
		params.put(MongoData.ACTION_ADDTIME, addtime);
		params.put(MongoData.ACTION_TYPE, MongoData.ACTION_TYPE_SUBUNIONTEMPLATE);
		mongoService.saveOrUpdateMap(params, MongoData.DEFAULT_ID_NAME, MongoData.NS_MAINSUBJECT);
		
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/subjectActivityList.xhtml")
	public String subjectActivityList(Integer pageNo, String link, String status, ModelMap model){
		Map params = new HashMap();
		if(StringUtils.isNotBlank(link)) params.put("link", link);
		if(StringUtils.isNotBlank(status)) params.put("status", status);
		if(pageNo == null) pageNo = 0;
		int rowsPerPage = 20;
		int firstRow = pageNo * rowsPerPage;
		int count = mongoService.getObjectCount(SubjectActivity.class, params);
		List<SubjectActivity> subjectList = mongoService.getObjectList(SubjectActivity.class, params, "addtime", false, firstRow, rowsPerPage);
		PageUtil pageUtil = new PageUtil(count, rowsPerPage, pageNo, "admin/newsubject/getSubjectActivity.xhtml");
		Map param = new HashMap();
		param.put("pageNo", pageNo);
		param.put("link", link);
		param.put("status", status);
		pageUtil.initPageInfo(param);
		model.put("pageUtil", pageUtil);
		model.put("subjectList", subjectList);
		return "admin/newsubject/subjectActivity.vm";
	}
	@RequestMapping("/admin/newsubject/getSubjectActivity.xhtml")
	public String getSubjectActivity(String id, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "参数错误！");
		SubjectActivity subjectActivity = mongoService.getObject(SubjectActivity.class, MongoData.DEFAULT_ID_NAME, id);
		if(subjectActivity == null) return showJsonError(model, "该关联不存在或被删除！");
		Map result = BeanUtil.getBeanMap(subjectActivity);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/newsubject/setSubjectActivityStatus.xhtml")
	public String setSubjectActivityStatus(String id, String status, ModelMap model){
		if(StringUtils.isBlank(id) || StringUtils.isBlank(status)) return showJsonError(model, "参数错误！");
		SubjectActivity subjectActivity = mongoService.getObject(SubjectActivity.class, MongoData.DEFAULT_ID_NAME, id);
		if(subjectActivity == null) return showJsonError(model, "该关联不存在或被删除！");
		subjectActivity.setStatus(status);
		mongoService.saveOrUpdateObject(subjectActivity, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/setSubjectActivityOrderNum.xhtml")
	public String setSubjectActivityOrderNum(String id, Integer ordernum, ModelMap model){
		if(StringUtils.isBlank(id) || ordernum == null) return showJsonError(model, "参数错误！");
		SubjectActivity subjectActivity = mongoService.getObject(SubjectActivity.class, MongoData.DEFAULT_ID_NAME, id);
		if(subjectActivity == null) return showJsonError(model, "该关联不存在或被删除！");
		subjectActivity.setOrdernum(ordernum);
		mongoService.saveOrUpdateObject(subjectActivity, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/newsubject/saveSubjectActivity.xhtml")
	public String saveSubjectActivity(String id, String link, Long relatedid, ModelMap model){
		SubjectActivity subjectActivity = null;
		if(StringUtils.isBlank(link)) return showJsonSuccess(model, "链接地址不能为空！");
		ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(relatedid);
		if(!code.isSuccess()) {
			return showJsonError(model, "不是有效的活动ID！");
		}
		RemoteActivity activity = code.getRetval();
		if(activity == null) 
		if(StringUtils.isNotBlank(id)){
			subjectActivity = mongoService.getObject(SubjectActivity.class, MongoData.DEFAULT_ID_NAME, id);
			if(subjectActivity == null) return showJsonError(model, "数据不存在或被删除！");
			subjectActivity.setLink(link);
			subjectActivity.setRelatedid(relatedid);
		}else{
			subjectActivity = new SubjectActivity(link, relatedid);
			subjectActivity.setId(ServiceHelper.assignID(TagConstant.TAG_SUBJECTACTIVITY));
		}
		mongoService.saveOrUpdateObject(subjectActivity, MongoData.DEFAULT_ID_NAME);
		return showJsonSuccess(model);
	}
	
	//赛车总动员2用户列表
	@RequestMapping("/admin/blog/subjectReport.xhtml")
	public String carsMemberReport(HttpServletResponse res, ModelMap model, String dKey, String isXls){
		if(StringUtils.isNotBlank(dKey)){
			DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", dKey, true);
				if(da != null){
				List<Prize> prizeList = drawActivityService.getPrizeListByDid(da.getId(),new String[]{"A","D","P", Prize.PRIZE_TYPE_DRAMA,Prize.PRIZE_REMARK});
				Map<String, Member> memberMap = new HashMap<String, Member>();
				Map<String, String> mobileMap=new HashMap<String, String>();
				Map<String, List<Prize>> prizeListMap = new HashMap<String, List<Prize>>();
				Map<String, Map> dataInfoMap = new HashMap<String, Map>();
				Member member=null;
				try {
					List<JsonData> dataList=jsonDataService.getListByTag(dKey, null, 0, 10000);
					for(JsonData jsonData: dataList){
						Long memberid = Long.valueOf(jsonData.getDkey());
						member =daoService.getObject(Member.class, memberid);
						memberMap.put(jsonData.getDkey(), member);
						mobileMap.put(jsonData.getDkey(), "");
						List<WinnerInfo> winnerList = drawActivityService.getWinnerList(da.getId(), BeanUtil.getBeanPropertyList(prizeList, Long.class,"id",true), null, null, "" , memberid, "", "", 0, 30);
						List<Long> idList=BeanUtil.getBeanPropertyList(winnerList, Long.class, "prizeid", true);
						prizeListMap.put(jsonData.getDkey(), daoService.getObjectList(Prize.class, idList));
						dataInfoMap.put(jsonData.getDkey(), JsonUtils.readJsonToMap(jsonData.getData()));
					}
					model.put("dataList", dataList);
				} catch (Exception e) {
					dbLogger.error(e.getMessage());
				}
				model.put("dataInfoMap", dataInfoMap);
				
				model.put("memberMap", memberMap);
				model.put("mobileMap", mobileMap);
				model.put("prizeListMap", prizeListMap);
				if(StringUtils.isNotBlank(isXls)) {
					download("xls", res);
				}
				model.put("da", da);
			}
		}
		return "admin/blog/subjectReport.vm";
	}
	
	@RequestMapping("/admin/subject/getTanksgivingDay.xhtml")
	public String getTanksgivingDay(ModelMap model){
		JsonData jsonData = daoService.getObject(JsonData.class, JsonDataKey.KEY_MPI_TANKSGIVINGDAY);
		model.put("jsonData", jsonData);
		return "admin/subject/tanksgivingDay.vm";
	}
	
	@RequestMapping("/admin/subject/saveTanksgivingDay.xhtml")
	public String saveTanksgivingDay(String mpids, ModelMap model){
		JsonData jsonData = daoService.getObject(JsonData.class, JsonDataKey.KEY_MPI_TANKSGIVINGDAY);
		if(jsonData == null){
			jsonData = new JsonData(JsonDataKey.KEY_MPI_TANKSGIVINGDAY);
			Timestamp cur = DateUtil.getCurFullTimestamp();
			jsonData.setValidtime(DateUtil.addDay(cur, 60));
		}
		Map<String, String> jsonMap = JsonUtils.readJsonToMap(jsonData.getData());
		List<Long> mpidList = BeanUtil.getIdList(mpids, ",");
		jsonMap.put("mpid", StringUtils.join(mpidList, ","));
		jsonData.setData(JsonUtils.writeMapToJson(jsonMap));
		daoService.saveObject(jsonData);
		return showJsonSuccess(model);
	}
	
	
		
		
		
		
}
