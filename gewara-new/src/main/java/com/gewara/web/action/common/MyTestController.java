package com.gewara.web.action.common;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.job.SendMessageJob;
import com.gewara.job.SportOrderJob;
import com.gewara.job.impl.DramaOrderJobImpl;
import com.gewara.model.common.GewaCity;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.drama.TheatreSeatArea;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.TicketOrder;
import com.gewara.mongo.MongoService;
import com.gewara.pay.ChinapayUtil;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.drama.DramaStarService;
import com.gewara.service.drama.OpenDramaService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.untrans.SysManageService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.PictureUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class MyTestController extends BaseAdminController {

	@Autowired
	@Qualifier("sysManageService")
	private SysManageService sysManageService;
	
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	
	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;

	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}

	@Autowired
	@Qualifier("cacheService")
	private CacheService cacheService;

	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Autowired
	@Qualifier("config")
	private Config config;

	public void setConfig(Config config) {
		this.config = config;
	}
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;
	
	@Autowired@Qualifier("sportOrderJob")
	public SportOrderJob sportOrderJob;
	
	@Autowired@Qualifier("dramaOrderJob")
	public DramaOrderJobImpl dramaOrderJob;
	
	@Autowired@Qualifier("sendMessageJob")
	public SendMessageJob sendMessageJob;
	
	@Autowired@Qualifier("openDramaService")
	public OpenDramaService openDramaService;
	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	
	@Autowired@Qualifier("dramaStarService")
	private DramaStarService dramaStarService;
	
	@RequestMapping("/testCaptcha.xhtml")
	@ResponseBody
	public String testCaptcha(HttpServletRequest request, String captchaId, String captcha, String zt) {
		String ip = WebUtils.getRemoteIp(request);
		boolean isValidCaptcha = false;
		if(StringUtils.isBlank(zt)){
			isValidCaptcha = controllerService.validateCaptcha(captchaId, captcha, ip);
		}else{
			isValidCaptcha = controllerService.validateZtCaptcha(captchaId, captcha, ip);
		}
		return ""+isValidCaptcha;
	}
	
	@RequestMapping("/longRequest.xhtml")
	@ResponseBody
	public String longRequest(HttpServletRequest request, long sleep) {
		if(!WebUtils.isLocalRequest(request)){
			return "error not local!";
		}
		String req = HttpUtils.getUrlAsString("http://192.168.2.6/longRequest2.xhtml?sleep=" + sleep).getResponse();
		return "sleep:" + req;
	}
	@RequestMapping("/longRequest2.xhtml")
	@ResponseBody
	public String longRequest2(HttpServletRequest request, long sleep) {
		if(!WebUtils.isLocalRequest(request)){
			return "error not local!";
		}
		long cur = System.currentTimeMillis();
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			dbLogger.warn("", e);
		}
		System.out.println("sleep:" + (System.currentTimeMillis() - cur));
		return "sleep:" + (System.currentTimeMillis() - cur);
	}
	/**
	 * bob. 简单监控 MemCache
	 * */
	@RequestMapping("/testMemcache.xhtml")
	public String testMemcache(ModelMap model) {
		Map test2 = null;
		try {
			cacheService.set(CacheConstant.REGION_TENMIN, "test1", 10);
			Integer testInt = (Integer) cacheService.get(CacheConstant.REGION_TENMIN, "test1") + 10;
			model.put("test1", testInt);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				dbLogger.error("", e);
			}

			Map test = new HashMap();
			test.put("key1", "xx");
			test.put("key2", true);
			test.put("key3", 10);
			test.put("key4", 'a');
			test.put("key5", 'a');
			cacheService.set(CacheConstant.REGION_TENMIN, "map", test);
			test2 = (Map) cacheService.get(CacheConstant.REGION_TENMIN, "map");
			model.put("test2", test2);
			dbLogger.warn("" + test2);
		} catch (Exception e) {
			dbLogger.warn("Error", e);
			return showJsonError(model, "memcached.OperationTimeoutException");
		}
		return showJsonSuccess(model, "" + test2);
	}

	public static void main(String[] args) {
		String[] a = {"aa","bb","cc","aa"};
		Set<String> opkeySet = new HashSet(Arrays.asList(a));
		System.out.println(opkeySet);
	}
	
	@RequestMapping("/testCityCode.xhtml")
	public String testCityCode(ModelMap model){
		String citycode = "310000";
		String address = "江苏省苏州";
		if (StringUtils.isNotBlank(address)) {
			Map<String, List<GewaCity>> proMap = AdminCityContant.getProMap();
			for (String proName : proMap.keySet()) {
				if (StringUtils.contains(address, proName)){
					boolean isBreak = true;
					List<GewaCity> cityList = proMap.get(proName);
					for (GewaCity gewaCity : cityList) {
						if(StringUtils.contains(address, gewaCity.getCityname())){
							citycode = gewaCity.getCitycode();
							isBreak = false;
							break;
						}
					}
					if(isBreak){
						citycode = cityList.get(0).getCitycode();
					}
					break;
				}
			}
		}
		model.put("citycode", citycode);
		return showJsonSuccess(model, citycode);
	}

	/**
	 * bob. 简单监控 Hbase队列
	 * */
	@RequestMapping("/testHbaseQueue.xhtml")
	@ResponseBody
	public String testHbaseQueue() {
		double totalcount = 0;
		Map<String, String> serverMap = sysManageService.getRegisterServers();
		for (String server : serverMap.keySet()) {
			String url = "http://" + server + ":8080" + config.getBasePath() + "getHbaseQueueCount.xhtml";
			HttpResult result = HttpUtils.getUrlAsString(url);
			String response = result.getResponse();
			if (StringUtils.isBlank(response)) {
				totalcount += 0.01; // 百分位表示挂掉的服务器
			} else {
				totalcount += Integer.parseInt(response) + 0.1; // 十分位表示正常的服务器
			}
		}
		return "" + totalcount;
	}

	@RequestMapping("/getHbaseQueueCount.xhtml")
	@ResponseBody
	public String getHbaseQueueCount() {
		return "" + monitorService.getMonitorStatus();
	}

	/**********************************************************************************************************/

	// 清除MemCache
	@RequestMapping("/clearMemcache.xhtml")
	public String clearMemcache() {
		return "clearMemCache.vm";
	}

	@RequestMapping("/clearMemcacheKey.xhtml")
	public String clearMemcacheKey(String regionName, String key, ModelMap model) {
		cacheService.remove(regionName, key);
		return showJsonSuccess(model, "" + new Date());
	}

	// 测试错误，不要删除
	@RequestMapping("/logRendError.xhtml")
	public String logRendError(HttpServletRequest request, ModelMap model) {
		Map<String, String> params = WebUtils.getRequestMap(request);
		dbLogger.warnMap(params);
		return showJsonSuccess(model);
	}

	@RequestMapping("/testVmError.xhtml")
	public String testVmError(HttpServletRequest request) {
		Map<String, String> params = WebUtils.getRequestMap(request);
		dbLogger.warnMap(params);
		return "vmError.vm";
	}

	@RequestMapping("/testSeq.xhtml")
	@ResponseBody
	public String testSeq(){
		List[] result = new ArrayList[2000];
		List<String> seqList = hibernateTemplate.find("select seqNo from OpenPlayItem where playtime>? and seqNo is not null", new Timestamp(System.currentTimeMillis()));
		for(String seq: seqList){
			int s = Math.abs(seq.hashCode()*337)%2000;
			if(result[s]==null) result[s] = new ArrayList();
			result[s].add(seq);
		}
		for(int i=0;i<2000;i++){
			dbLogger.warn("SEQDISTRIBUTED" + i + "---->count:" + (result[i]==null?"0,NULL": result[i].size() + ", " + StringUtils.join(result[i], ",")));
		}
		return "complete!" ;
	}

	@RequestMapping("/db.xhtml")
	public String testHand(HttpServletRequest request, ModelMap model) {
		dbLogger.warn(WebUtils.getHeaderStr(request));
		TicketOrder order = daoService.getObject(TicketOrder.class, 32282799L);
		Map<String, String> params = ChinapayUtil.getDanbaoParams(order, "222.68.188.78");
		String submitUrl = params.remove("payurl");
		String method = params.remove("submitMethod");
		model.put("submitUrl", submitUrl);
		model.put("method", method);
		model.put("pause", true);
		model.put("submitParams", params);
		return "tempSubmitForm.vm";
	}

	@RequestMapping("/testPic.xhtml")
	public String testPic(ModelMap model) {
		String src = "/opt/lamp/weblog/test.jpg"; // 600px × 800px
		String dst1 = "/opt/lamp/weblog/test1.jpg"; // 800*900
		String dst2 = "/opt/lamp/weblog/test2.jpg"; // 800*600
		String dst3 = "/opt/lamp/weblog/test3.jpg"; // 500*900
		String dst4 = "/opt/lamp/weblog/test4.jpg"; // 350*250
		String dst5 = "/opt/lamp/weblog/test5.jpg"; // 250*350

		PictureUtil.resizeCrop(src, dst1, 800, 900);
		PictureUtil.resizeCrop(src, dst2, 800, 600);
		PictureUtil.resizeCrop(src, dst3, 500, 900);
		PictureUtil.resizeCrop(src, dst4, 350, 250);
		PictureUtil.resizeCrop(src, dst5, 250, 350);

		return forwardMessage(model, "msg:" + new Date());
	}
	@RequestMapping("/test/testCacheHit1.xhtml")
	public String testCacheHit1(ModelMap model, Long id) {
		dbLogger.warn("test1" + id);
		Movie movie1 = daoService.getObject(Movie.class, id);
		return forwardMessage(model, movie1.getName());
	}


	@RequestMapping("/test/testWrite2.xhtml")
	public String testReadWrite2(ModelMap model, Long id) {
		dbLogger.warn("testGetreadOnlyTemplate");
		Movie movie = daoService.getObject(Movie.class, id);
		movie.setType("xx" + movie.getType());
		daoService.saveObject(movie);
		dbLogger.warn("afterSave");
		Movie movie2 = daoService.getObject(Movie.class, id);
		String s = movie.getType() + "<->" + movie2.getType();
		movie2.setType(movie.getType().substring(2));
		daoService.saveObject(movie2);
		return forwardMessage(model, s);
	}

	@RequestMapping("/test/testWrite3.xhtml")
	public String testWrite3(ModelMap model, Long id) {
		dbLogger.warn("beforeWrite");
		Movie movie = daoService.getObject(Movie.class, id);
		movie.setType("xx" + movie.getType());
		daoService.saveObject(movie);
		movie.setType(movie.getType().substring(2));
		daoService.saveObject(movie);
		return forwardMessage(model, movie.getType());
	}

	@RequestMapping("/test/testAdd1.xhtml")
	public String testAdd(ModelMap model, Long id) {
		JsonData jd = new JsonData("testObj" + id, "testData" + id);
		daoService.saveObject(jd);
		return forwardMessage(model, "jd:" + jd.getData());
	}

	@RequestMapping("/test/testRemove.xhtml")
	public String testRemove(ModelMap model, Long id) {
		daoService.removeObjectById(JsonData.class, "testObj" + id);
		return forwardMessage(model, "remove:" + id);
	}

	@RequestMapping("/testEmail.xhtml")
	public String testEmail(Integer page) {
		if (page == null)
			page = 1;
		String query = "select email from Member";
		List<String> emailList = daoService.queryByRowsRange(query, 20000 * page, 20000 * page + 20000);
		for (String email : emailList) {
			if (!ValidateUtil.isEmail(email))
				dbLogger.warn("[" + email + "]");
		}
		Map data = new HashMap();
		data.put("id", System.currentTimeMillis());
		return "testMovie.vm";
	}

	@RequestMapping("/testVelocity.xhtml")
	public String testVelocity() {
		return "testVelocity.vm";
	}
	
	@RequestMapping("/clearFilmFestPageView.xhtml")
	public String clearFilmFestPageView(ModelMap model) {
		String cacheKey = "c";
		PageParams params = new PageParams();
		params.addSingleString(cacheKey, filmFestService.getCachePre());
		pageCacheService.clearPageView("filmfest/sixteen.xhtml", params, AdminCityContant.CITYCODE_SH);
		return showJsonSuccess(model);
	}


	@RequestMapping("/testRemoteString.xhtml")
	@ResponseBody
	public String testRemoteString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<br/><br/>");
		Map errone = mongoService.findOne("com.gewara.json.SysWarn", MongoData.SYSTEM_ID, new ObjectId("4fb071948234044369000002"));
		String title = (String) errone.get("title");
		/*
		 * sb.append("old___" + title); System.out.println("old___" + title);
		 * sb.append("<br/><br/>");
		 */
		String escabbr = VmUtils.escabbr(title, 1000);
		sb.append("enabbr___" + escabbr);
		System.out.println("enabbr___" + escabbr);
		sb.append("<br/><br/>");
		/*
		 * String escabbr = VmUtils.escabbr(title, 1000); sb.append("escabbr___"
		 * + escabbr); System.out.println("escabbr___" + escabbr);
		 * sb.append("<br/><br/>");
		 */
		return sb.toString();
	}

	@RequestMapping("/admin/updateRelateCount.xhtml")
	public String updateRelateCount(ModelMap model){
		sendMessageJob.updateRelateCount();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/updateSeatMap.xhtml")
	@ResponseBody
	public String updateSeatMap(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<TheatreRoom> roomList = daoService.getAllObjects(TheatreRoom.class);
				for (TheatreRoom theatreRoom : roomList) {
					if(theatreRoom.getLinenum()>0 && theatreRoom.getRanknum()>0){
						try{
							String str = openDramaService.getTheatreRoomSeatMapStr(theatreRoom);
							theatreRoom.setSeatmap(str);
							daoService.saveObject(theatreRoom);
							dbLogger.warn("Update:" +theatreRoom.getLinenum() + "," + theatreRoom.getRanknum());
						}catch(Exception e){
							dbLogger.warn("", e);
						}
					}
				}
				List<TheatreSeatArea> seatAreaList = daoService.getAllObjects(TheatreSeatArea.class);
				for (TheatreSeatArea theatreSeatArea : seatAreaList) {
					try{
						if(theatreSeatArea.getLinenum()>0 && theatreSeatArea.getRanknum()>0){
							String str = openDramaService.getTheatreSeatAreaMapStr(theatreSeatArea);
							theatreSeatArea.setSeatmap(str);
							daoService.saveObject(theatreSeatArea);
						}
					}catch(Exception e){
						dbLogger.warn("", e);
					}
				}
			}
		}).start();
		return "start at:" + new Timestamp(System.currentTimeMillis());
	}
	
	@RequestMapping("/admin/otsJob.xhtml")
	public String openTimeSaleToSuccess(ModelMap model){
		sportOrderJob.openTimeSaleToSuccess();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/dramaOrder.xhtml")
	public String dramaOrder(ModelMap model){
		dramaOrderJob.correctOrder();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/updateBlogEveryDay.xhtml")
	public String updateBlogEveryDay(Timestamp starttime, Timestamp endtime, ModelMap model){
		if(starttime == null || endtime == null) return showJsonError(model, "starttime endttime 不能为空！");
		List<Map> diaryCountMapList = blogService.getDiaryMapList(starttime, endtime);
		int update = 0, error = 0;
		for (Map map : diaryCountMapList) {
			String mid = String.valueOf(map.get("categoryid"));
			String category = String.valueOf(map.get("category"));
			String adddate = String.valueOf(map.get("adddate"));
			Integer count = Integer.valueOf(String.valueOf(map.get("rowcount")));
			count = (count == null ? 0 : count);
			try{
				blogService.saveOrUpdateBlogDateEveryDay(0L, category, Long.parseLong(mid), TagConstant.TAG_DIARY, DateUtil.parseDate(adddate), count);
				dbLogger.warn("更新帖子数：类型：" + category +",ID: " + Long.parseLong(mid) +",日期：" + adddate + ",数量：" + count);
				update ++;
			}catch (Exception e) {
				dbLogger.warn("", e);
				error ++;
			}
		}
		return showJsonSuccess(model, "update:" + update + ", error:" + error);
	}
	
	@RequestMapping("/admin/dramaToStar.xhtml")
	public String dramaToStar(ModelMap model){
		final String KEY_UPDATE_STAR = "updateStar";
		String hql ="from Drama d where (d.actors is not null or d.director is not null or d.troupecompany is not null) and d.otherinfo not like ? order by addtime";
		List<Drama> dramaList = daoService.queryByRowsRange(hql, 0, 1000, "%" + KEY_UPDATE_STAR +"%");
		Map<String,DramaStar> starMap = new HashMap<String,DramaStar>();
		int update = 0, error = 0;
		for (Drama drama : dramaList) {
			try{
				ChangeEntry changeEntry = new ChangeEntry(drama);
				if(StringUtils.isNotBlank(drama.getActors())){
					List<Long> idList = BeanUtil.getIdList(drama.getActors(), ",");
					if(idList.isEmpty()){
						List<String> startList = Arrays.asList(StringUtils.split(drama.getActors(), ","));
						for (String starName : startList) {
							
							DramaStar dramaStar = starMap.get(starName);
							if(dramaStar == null){
								dramaStar = dramaStarService.getDramaStarByName(starName, null);
							}
							if(dramaStar != null){
								if(!idList.contains(dramaStar.getId())){
									idList.add(dramaStar.getId());
								}
								starMap.put(starName, dramaStar);
							}
						}
					}
					drama.setActors(StringUtils.join(idList, ","));
				}
				
				if(StringUtils.isNotBlank(drama.getDirector())){
					List<Long> idList = BeanUtil.getIdList(drama.getDirector(), ",");
					if(idList.isEmpty()){
						List<String> startList = Arrays.asList(StringUtils.split(drama.getDirector(), ","));
						for (String starName : startList) {
							DramaStar dramaStar = starMap.get(starName);
							if(dramaStar == null){
								dramaStar = dramaStarService.getDramaStarByName(starName, null);
							}
							if(dramaStar != null){
								if(!idList.contains(dramaStar.getId())){
									idList.add(dramaStar.getId());
								}
								starMap.put(starName, dramaStar);
							}
						}
					}
					drama.setDirector(StringUtils.join(idList, ","));
				}
				if(StringUtils.isNotBlank(drama.getTroupecompany())){
					List<Long> idList = BeanUtil.getIdList(drama.getTroupecompany(), ",");
					if(idList.isEmpty()){
						List<String> startList = Arrays.asList(StringUtils.split(drama.getTroupecompany(), ","));
						for (String starName : startList) {
							DramaStar dramaStar = starMap.get(starName);
							if(dramaStar == null){
								dramaStar = dramaStarService.getDramaStarByName(starName, DramaStar.TYPE_TROUPE);
							}
							if(dramaStar != null){
								if(!idList.contains(dramaStar.getId())){
									idList.add(dramaStar.getId());
								}
								starMap.put(starName, dramaStar);
							}
						}
					}
					drama.setTroupecompany(StringUtils.join(idList, ","));
				}
				if(!changeEntry.getChangeMap(drama).isEmpty()){
					Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(drama.getOtherinfo());
					otherInfoMap.put(KEY_UPDATE_STAR, "true");
					drama.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
					daoService.saveObject(drama);
					update ++;
				}
			}catch(Exception e){
				dbLogger.warn("", e);
				error++;
			}
		}
		return showJsonSuccess(model, "update:" + update + ",error:" +error);
	}
	
	@RequestMapping("/admin/updateDramaPrice.xhtml")
	public String updateDramaPrice(ModelMap model){
		List<Long> idList = daoService.getObjectIDList(Drama.class);
		for (Long dramaid : idList) {
			dramaPlayItemService.refreshDramaPrice(dramaid);
		}
		return showJsonSuccess(model);
	}
}
