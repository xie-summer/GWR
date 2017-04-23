/**  
 * @Project: shanghai
 * @Title: SearchController.java
 * @Package com.gewara.web.action.api2.search
 * @author shenyanghong paul.wei2011@gmail.com
 * @date Aug 10, 2012 3:57:07 PM
 * @version V1.0  
 */

package com.gewara.web.action.search;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.GewaSearchKey;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.BaseObject;
import com.gewara.model.bbs.Diary;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.News;
import com.gewara.model.content.Video;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.service.PlaceService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.service.sport.OpenTimeTableService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.CardItem;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;

/**
 * @ClassName SearchController
 * @Description 站内搜索控制器
 * @author weihonglin pau.wei2011@gmail.com
 * @date Aug 10, 2012
 */

@Controller
public class SearchController extends AnnotationController {

	@Autowired@Qualifier("qaService")
	private QaService qaService;
	
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;

	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;

	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Autowired@Qualifier("sportService")
	private SportService sportService;

	
	@Autowired@Qualifier("openTimeTableService")
	private OpenTimeTableService openTimeTableService;
	
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	
	
	@Autowired@Qualifier("dramaPlayItemService")
	private DramaPlayItemService dramaPlayItemService;


	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@RequestMapping("/newindex/test.xhtml")
	public String searchKey(){
		return "/newindex/newsearch.vm";
	}
	
	@RequestMapping("/newSearchKeyNum.xhtml")
	public String newSearchKeyNum(String channel,String category, String skey,HttpServletRequest request,
			HttpServletResponse response, ModelMap model){
		String citycode = WebUtils.getAndSetDefault(request, response);
		Set<String> keyList = searchService.searchKey(citycode,channel,null,category,skey,15);
		String result = "[\"" + StringUtils.join(keyList, "\",\"") + "\"]";
		model.put("result", result);
		return "common/searchkey.vm";
	}

	@RequestMapping("/newSearchKey.xhtml")
	public String searchKey(String skey, String channel, String category, Integer pageNo, HttpServletRequest request,
			HttpServletResponse response, ModelMap model) throws Exception {

		String citycode = WebUtils.getAndSetDefault(request, response);
		// 右边数据
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getActivityListByStatus(citycode, Status.Y_PROCESS, 0, 4);
		if(code.isSuccess()){
			model.put("hotActivityList", code.getRetval());
		}
		List<GewaQuestion> hotQaList = qaService.getQuestionByQsAndTagList(citycode, GewaQuestion.QS_STATUS_N, null,
				"addtime", 10);
		model.put("hotQaList", hotQaList);
		List<GewaCommend> recommendztList = commonService.getGewaCommendList(citycode,SignName.SEARCH_ZT, null,null,true,0,10);
		model.put("recommendztList",recommendztList);
		if (StringUtils.isBlank(skey))	{return "newindex/newKeyList.vm";}
		model.put("queryString", skey);
		// 调用SearchAPI
		Timestamp starttime = new Timestamp(System.currentTimeMillis());
		String ip = WebUtils.getRemoteIp(request);
		Map<String, Object> map = searchService.searchKey(ip,citycode,skey, channel,null,category, pageNo);
		String rowsInfo = (String) map.get(SearchService.ROWS_INFO);
		Integer rowsCount = (Integer) map.get(SearchService.ROWS_COUNT);
		List<GewaSearchKey> skList = (List<GewaSearchKey>) map.get(SearchService.ROWS_SK_LIST);
		if(pageNo==null) pageNo = 0;
		if(rowsCount==null) rowsCount=0;
		PageUtil pageUtil = new PageUtil(rowsCount, SearchService.ROWS_PER_PAGE, pageNo, "newSearchKey.xhtml", true, true);
		Map params = new HashMap();
		params.put("skey", skey);
		params.put("channel", channel);
		params.put("category", category);
		pageUtil.initPageInfo(params);
		int start = SearchService.ROWS_PER_PAGE * pageNo+1;
		int end = rowsCount>SearchService.ROWS_PER_PAGE*(pageNo+1)?SearchService.ROWS_PER_PAGE*(pageNo+1):rowsCount;

		//关联数据展示
		Map<Long, Boolean> bookingMapDrama = new HashMap<Long, Boolean>();
		Map<Long, List<CardItem>> gymCardMap = new HashMap<Long, List<CardItem>>();
		Map<Long,List<RemoteCourse>> coachCourseMap=new HashMap<Long, List<RemoteCourse>>();
		Map<Long,List<RemoteGym>> coachGymMap=new HashMap<Long, List<RemoteGym>>();
		Map<Long, MarkCountData> markCountMap = new HashMap<Long, MarkCountData>();
		if(skList!=null&&!skList.isEmpty()){
			Iterator<GewaSearchKey> it = skList.iterator();
			while(it.hasNext()){
        		GewaSearchKey sk = it.next();
    			if(sk.getRelatedid()!=null && StringUtils.isNotBlank(sk.getCategory())){
    				Object obj = relateService.getRelatedObject(sk.getCategory(), sk.getRelatedid());
    				if(obj != null){
    					sk.setRelatedObj(obj);
       				if("drama".equals(sk.getCategory())){
       					initDramaData(sk,bookingMapDrama);//演出剧院,票价
       				}else if("gym".equals(sk.getCategory())){
       					initGymData(sk,gymCardMap);//可购卡
       				}else if("gymcoach".equals(sk.getCategory())){
       					initGymcoachData(sk,coachCourseMap,coachGymMap);//可预定课程
       				}else if("gewaquestion".equals(sk.getCategory())){
       					GewaQuestion qa =(GewaQuestion)obj;
       					addCacheMember(model,qa.getMemberid());//发帖用户信息
       				}else if("diary".equals(sk.getCategory())){
       					DiaryBase diary =(DiaryBase)obj;
       					addCacheMember(model,diary.getMemberid());//发帖用户信息
       				}else if(StringUtils.equals(TagConstant.TAG_MOVIE, sk.getCategory())){
       					markCountMap.put(sk.getRelatedid(), markService.getMarkCountByTagRelatedid(TagConstant.TAG_MOVIE, sk.getRelatedid()));
       				}
    				}
    			}
			}
		}
		Map<Long, List<DramaStar>> dramaStarListMap= new HashMap<Long, List<DramaStar>>();
		Map<Long, List<DramaStar>> dramaDirectorListMap= new HashMap<Long, List<DramaStar>>();
		if(skList!=null&&!skList.isEmpty()){//高亮
			for(GewaSearchKey sk:skList){
				if(sk.getRelatedObj() instanceof Drama){
					Drama drama = (Drama) sk.getRelatedObj();
					if(StringUtils.isNotBlank(drama.getActors())){
						List<Long> actorIdList = BeanUtil.getIdList(drama.getActors(), ",");
						List<DramaStar> actorsList = daoService.getObjectList(DramaStar.class, actorIdList);
						dramaStarListMap.put(drama.getId(), actorsList);
					}
					if(StringUtils.isNotBlank(drama.getDirector())){
						List<Long> directorIdList = BeanUtil.getIdList(drama.getDirector(), ",");
						List<DramaStar> directorsList = daoService.getObjectList(DramaStar.class, directorIdList);
						dramaDirectorListMap.put(drama.getId(), directorsList);
					}
				}
				Map<String, Object> related = searchService.getBeanSearchLight(sk.getRelatedObj(), skey);//高亮截取长度字符串
				sk.setRelatedObj(related);
			}

		}
		model.put("dramaStarListMap", dramaStarListMap);
		model.put("dramaDirectorListMap", dramaDirectorListMap);
		model.put("curMarkCountMap", markCountMap);
		model.put("markData", markService.getMarkdata(TagConstant.TAG_MOVIE));
		model.put("skList", skList);
		model.put("rowsInfo", rowsInfo);
		model.put("rowsCount", rowsCount);
		model.put("start", start);
		model.put("end", end);
		model.put("pageUtil", pageUtil);
		model.put("gymCardMap", gymCardMap);
		model.put("coachCourseMap", coachCourseMap);
		model.put("coachGymMap", coachGymMap);
		model.put("bookingMapDrama", bookingMapDrama);
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		model.put("subwaylineMap", placeService.getSubwaylineMap(citycode));
		model.put("theatreidList", dramaPlayItemService.getTheatreidList(citycode, null, true));
		model.put("sportIdList", openTimeTableService.getCurOttSportIdList(null, citycode));
		model.put("opiMovieList", openPlayService.getOpiMovieidList(citycode, null));
		model.put("opiCinemaList", openPlayService.getOpiCinemaidList(citycode, null));
		model.put("sportItemList", sportService.getBookingSportItemList());
		model.put("costtime", (System.currentTimeMillis() - starttime.getTime()) * 1.00 / (1000.00));
		return "newindex/newKeyList.vm";
	}

	@RequestMapping("/ajax/searchTopKey.xhtml")
	public String searchTopKey(ModelMap model) {
		List<String> topSkList = searchService.getTopSearchKeyList(SearchService.TOP_SK_COUNT);
		model.put("topSkList", topSkList);
		return "newindex/newTopSearch.vm";
	}
	
	@RequestMapping("/saveBatchSearchKey.xhtml")
	public @ResponseBody String saveBatchSearchKey() {
		return searchService.saveBatchSearchKey(null);
	}
	
	//@RequestMapping("/reBuildSearchKey.xhtml")
	public @ResponseBody <T extends BaseObject> String reBuildSearchKey() {
		new Thread(new Runnable(){
			@Override
			public void run() {
				int total = 0 ;
				List<Class> list = new ArrayList<Class>();
				list.add(GewaQuestion.class);
				list.add(Movie.class);
				list.add(Cinema.class);
				list.add(Diary.class);
				list.add(News.class);
				list.add(SportItem.class);
				list.add(Sport.class);
				list.add(Video.class);
				list.add(Commu.class);
				list.add(Drama.class);
				list.add(DramaStar.class);
				list.add(Theatre.class);
				for(Class<T> clazz:list){
					int count = searchService.reBuildIndex(clazz);
					total+=count;
					dbLogger.warn(clazz.getName()+" index: " + count + ", total:" + total);
				}
			}
		}).start();
		return "rebuild start!!";
	}
	
	public void initDramaData(GewaSearchKey sk, Map<Long, Boolean> bookingMapDrama){
		bookingMapDrama.put(sk.getRelatedid(), dramaPlayItemService.isBookingByDramaId(sk.getRelatedid()));
	}

	public void initGymData(GewaSearchKey sk, Map<Long, List<CardItem>> cardMap){
		ErrorCode<List<CardItem>> code = synchGymService.getValidGymCardListByGymId(sk.getRelatedid(), null, null, null, null, false, 0, 100);
		if(code.isSuccess()){
			List<CardItem> gymCardList = code.getRetval();
			cardMap.put(sk.getRelatedid(), gymCardList);
		}
	}
	
	public void initGymcoachData(GewaSearchKey sk,Map<Long,List<RemoteCourse>> coachCourseMap,Map<Long,List<RemoteGym>> coachGymMap){
		ErrorCode<List<RemoteCourse>> courseCode = synchGymService.getCourseListByCoachId(sk.getRelatedid());
		if(courseCode.isSuccess()) coachCourseMap.put(sk.getRelatedid(),  courseCode.getRetval());
		ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymListByCoachId(sk.getRelatedid());
		if(gymCode.isSuccess()) coachGymMap.put(sk.getRelatedid(), gymCode.getRetval());
	}
}
