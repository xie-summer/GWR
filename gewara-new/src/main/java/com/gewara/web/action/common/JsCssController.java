package com.gewara.web.action.common;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.content.SignName;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.sport.Sport;
import com.gewara.service.PlaceService;
import com.gewara.service.sport.SportService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.gym.RemoteCourse;
import com.gewara.xmlbind.gym.RemoteGym;
@Controller
public class JsCssController extends AnnotationController {
	
	private static final int MAXNUM = 2000;	// AutoComplete 最大条数
	
	@Autowired@Qualifier("placeService")
	private PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	public void setCommonService(CommonService commonService) {
		this.commonService = commonService;
	}
	
	@Autowired@Qualifier("synchGymService")
	private SynchGymService synchGymService;
	
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	public void setSportService(SportService sportService) {
		this.sportService = sportService;
	}
	private Map<String, Long> modifyMap = new HashMap<String, Long>();
	private List<String> cssList = Arrays.asList("global", "sns", "template", "layout", "framework");
	private List<String> jsList = Arrays.asList("mootools", "gewara-util", "page");

	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	@RequestMapping("/updateJsVersion.xhtml")
	@ResponseBody
	public String updateJsVersion(String version){
		if(StringUtils.isBlank(version)) version = DateUtil.format(new Date(), "yyyyMMddHH");
		String oldVersion = VmUtils.getJsVersion();
		VmUtils.setJsVersion(version);
		return Config.getHostname()+ ",version:" + oldVersion + "--->" + version; 
	}
	@RequestMapping("/getCss.dhtml")
	public void cmpCss1(String p, String n, String v, HttpServletRequest request, HttpServletResponse res) throws Exception{
		cmp(n, p, v, ".css", request, res);
	}
	@RequestMapping("/getJs.dhtml")
	public void cmpJs1(String p, String n, String v, HttpServletRequest request, HttpServletResponse res) throws Exception{
		res.setCharacterEncoding("UTF-8");
		cmp(n, p, v, ".js", request, res);
	}

	@RequestMapping("/cleanCssJs.xhtml")
	public String cleanCssJs(ModelMap model) throws Exception{
		modifyMap.clear();
		return showMessage(model, "success:" + new Date());
	}

	private String sortFileNames(String fileList, String ext){
		List<String> fixed = null;
		if("js".equals(ext)) fixed = jsList;
		else fixed = cssList;
		if(StringUtils.isBlank(fileList)) return "";
		List<String> result = new ArrayList<String>(fixed);//结果
		List<String> tmpRemove = new ArrayList<String>(fixed);//要删除的
		List<String> tmpList = new ArrayList<String>(Arrays.asList(fileList.split(",")));
		tmpRemove.removeAll(tmpList);//除去不要删除
		result.removeAll(tmpRemove);//删除要删除的
		
		tmpList.removeAll(result);//删除已经保留下来的
		result.addAll(tmpList);
		return StringUtils.join(result, ",");
	}
	private void cmp(String fileList, String path, String version, String ext, HttpServletRequest request, HttpServletResponse res) throws Exception{
		if(StringUtils.isBlank(fileList)) return ;
		String key = fileList + "v=" + version;
		Long lastModify = modifyMap.get(key);
		String key2 = null;
		if(lastModify == null){
			key2 = sortFileNames(fileList, ext);
			lastModify = modifyMap.get(key2 + "v=" + version);
		}
		
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		if(ext.equals(".js")){
			res.setContentType("application/x-javascript; charset=UTF-8");
			res.addHeader("Etag", "js" + key.hashCode());
		}else{
			res.setContentType("text/css; charset=UTF-8");			
			res.addHeader("Etag", "css" + key.hashCode());
		}
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addDay(cur, 60);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		if(StringUtils.isBlank(key2)) key2 = sortFileNames(fileList, ext);
		String[] fileNames = StringUtils.split(key2, ",");
		for(String name : fileNames){
			HttpResult str = HttpUtils.getUrlAsString(config.getAbsPath() + config.getBasePath() + path+"/" + name + ext + "?v="+lastModify, null);
			if(!str.isSuccess()) {
				dbLogger.error("JS,CSS文件错误：" + name);
				throw new IllegalArgumentException("文件错误：" + name);
			}
			out.print(str.getResponse());
		}
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
		modifyMap.put(key2 + "v=" + version, lastModify);
	}
	
	@RequestMapping("/getConstSportKey.xhtml")	// 运动版块需要, 用作运动搜索弹出框
		public void getSportKey(String v, String citycode, HttpServletRequest request, HttpServletResponse res,Long itemid) throws Exception{
		if(StringUtils.isBlank(citycode)) {
			res.sendError(404);
			return;
		} 
		String key = "sportKey" + citycode + v;
		Long lastModify = modifyMap.get(key);
		
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.addHeader("Etag", "js" + key.hashCode());
		
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addDay(cur, 60);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		
		// 20110414 修改, 取得推荐的列表
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode , SignName.SPORT_ORDER, null, false, true,0, -1);
		// 20110505 加入判断(如果该城市没有区域, 生成固定JS)
		if(gcList.size() == 0){
			GewaCommend gewaCommend = new GewaCommend("");
			gewaCommend.setRelatedid(000000L);// 表示无区域
			gewaCommend.setTitle("近郊");
			gcList.add(gewaCommend);
		}
		List<Map> countyListMap = BeanUtil.getBeanMapList(gcList, "relatedid", "title", "ordernum");
		String countyMapJS = JsonUtils.writeObjectToJson(countyListMap);
		String result1 = "function _getcountyMap(){return " + countyMapJS + "; }";
		
		// Map<countycode, List<Cinema>>
		List<Long> idlist=sportService.getBookingSportIdList(itemid, citycode);
		List<Sport> sportList = daoService.getObjectList(Sport.class, idlist);
		Collections.sort(sportList, new MultiPropertyComparator<Sport>(new String[]{"hotvalue","booking","clickedtimes"}, new boolean[]{false, false, false}));
		List<Map> sportMapList = BeanUtil.getBeanMapList(sportList, "id", "name", "booking", "realBriefname", "countycode");
		Map<String, List<Sport>> sportMap = BeanUtil.groupBeanList(sportMapList, "countycode");
		String sportMapJS = JsonUtils.writeObjectToJson(sportMap);
		String result2 = "function _getcinemaMap(){return " + sportMapJS + "; }";
		out.write(result1 + result2);
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
	}
	
	
	@RequestMapping("/getConstTheatreKey.xhtml")	// 演出版块需要, 用作演出后前台搜索弹出框
	public void getTheatreKey(String v, String citycode, HttpServletRequest request, HttpServletResponse res) throws Exception{
		if(StringUtils.isBlank(citycode)) {
			res.sendError(404);
			return;
		} 
		String key = "theatreKey" + citycode + v;
		Long lastModify = modifyMap.get(key);
		
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.addHeader("Etag", "js" + key.hashCode());
		
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addDay(cur, 60);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		
		// 20110414 修改, 取得推荐的列表
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode , SignName.THEATRE_ORDER, null, false, true,0, -1);
		// 20110505 加入判断(如果该城市没有区域, 生成固定JS)
		if(gcList.size() == 0){
			GewaCommend gewaCommend = new GewaCommend("");
			gewaCommend.setRelatedid(000000L);// 表示无区域
			gewaCommend.setTitle("近郊");
			gcList.add(gewaCommend);
		}
		List<Map> countyListMap = BeanUtil.getBeanMapList(gcList, "relatedid", "title", "ordernum");
		String countyMapJS = JsonUtils.writeObjectToJson(countyListMap);
		String result1 = "function _getcountyMap(){return " + countyMapJS + "; }";
		
		// Map<countycode, List<Cinema>>
		Map<String, List<Map>> cinemaMap = new HashMap<String, List<Map>>();
		for(GewaCommend gewaCommend : gcList){
			Long relateid = gewaCommend.getRelatedid();
			String countycode = relateid + "";
			List<Theatre> theatreList = null;
			if(relateid == 0L){
				theatreList = placeService.getPlaceListByHotvalue(citycode, Theatre.class, 0, 300); 
			}else{
				theatreList = placeService.getPlaceListByCountyCode(Theatre.class, countycode, null, false);
			}
			Collections.sort(theatreList, new MultiPropertyComparator<Theatre>(new String[]{"hotvalue","booking","clickedtimes"}, new boolean[]{false, false, false}));
			List<Map> cinemaMapList = BeanUtil.getBeanMapList(theatreList, "id", "name", "booking", "realBriefname");
			cinemaMap.put(countycode, cinemaMapList);
		}
		String cinemaMapJS = JsonUtils.writeObjectToJson(cinemaMap);
		String result2 = "function _gettheatreMap(){return " + cinemaMapJS + "; }";
		out.write(result1 + result2);
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
	}
	
	
	@RequestMapping("/getConstCinemaKey.xhtml")	// 电影版块需要, 用作影院搜索弹出框
	public void getCinemaKey(String v, String citycode, HttpServletRequest request, HttpServletResponse res) throws Exception{
		if(StringUtils.isBlank(citycode)) {
			res.sendError(404);
			return;
		} 
		String key = "cinemaKey" + citycode + v;
		Long lastModify = modifyMap.get(key);
		
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.addHeader("Etag", "js" + key.hashCode());
		
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addDay(cur, 60);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		
		// 20110414 修改, 取得推荐的列表
		List<GewaCommend> gcList = commonService.getGewaCommendList(citycode , SignName.CINEMA_ORDER, null, false, true,0, -1);
		// 20110505 加入判断(如果该城市没有区域, 生成固定JS)
		if(gcList.size() == 0){
			GewaCommend gewaCommend = new GewaCommend("");
			gewaCommend.setRelatedid(000000L);// 表示无区域
			gewaCommend.setTitle("近郊");
			gcList.add(gewaCommend);
		}
		List<Map> countyListMap = BeanUtil.getBeanMapList(gcList, "relatedid", "title", "ordernum");
		String countyMapJS = JsonUtils.writeObjectToJson(countyListMap);
		String result1 = "function _getcountyMap(){return " + countyMapJS + "; }";
		
		// Map<countycode, List<Cinema>>
		Map<String, List<Map>> cinemaMap = new HashMap<String, List<Map>>();
		for(GewaCommend gewaCommend : gcList){
			Long relateid = gewaCommend.getRelatedid();
			String countycode = relateid + "";
			List<Cinema> cinemaList = null;
			if(relateid == 0L){
				cinemaList = placeService.getPlaceListByHotvalue(citycode, Cinema.class, 0, 300); 
			}else{
				cinemaList = placeService.getPlaceListByCountyCode(Cinema.class, countycode, null, false);
			}
			Collections.sort(cinemaList, new MultiPropertyComparator<Cinema>(new String[]{"hotvalue","booking","clickedtimes"}, new boolean[]{false, false, false}));
			List<Map> cinemaMapList = BeanUtil.getBeanMapList(cinemaList, "id", "name", "booking", "realBriefname");
			cinemaMap.put(countycode, cinemaMapList);
		}
		String cinemaMapJS = JsonUtils.writeObjectToJson(cinemaMap);
		String result2 = "function _getcinemaMap(){return " + cinemaMapJS + "; }";
		out.write(result1 + result2);
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
	}
	
	@RequestMapping("/getConstPlaceKey.xhtml")	// 地区管理版块 
	public void getConstPlaceKey(String v, String citycode, HttpServletRequest request, HttpServletResponse res) throws Exception{
		if(StringUtils.isBlank(citycode)) {
			res.sendError(404);
			return;
		} 
		String key = "placeKey" + citycode + v;
		Long lastModify = modifyMap.get(key);
		
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.addHeader("Etag", "js" + key.hashCode());
		
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addDay(cur, 60);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		
		
		
		Map<String, List<Map>/*county*/> countyMap = new HashMap<String, List<Map>>();
		Map<String, List<Map>/*indexarea*/> indexareaMap = new HashMap<String, List<Map>>();
		List<County> countyList = placeService.getCountyByCityCode(citycode);
		countyMap.put(citycode, BeanUtil.getBeanMapList(countyList, "countyname", "countycode", "briefname"));
		for(County county:countyList){
			List<Indexarea> indexareaList = placeService.getIndexareaByCountyCode(county.getCountycode());
			if(!indexareaList.isEmpty()){
				indexareaMap.put(county.getCountycode(), BeanUtil.getBeanMapList(indexareaList, "indexareacode", "indexareaname"));
			}
		}
		
		String countyMapJS = JsonUtils.writeObjectToJson(countyMap);
		String result1 = "var countyMap = " + countyMapJS + "; \n";
		String indexareaMapJS = JsonUtils.writeObjectToJson(indexareaMap);
		String result2 = "var indexareaMap = " + indexareaMapJS + "; \n";
		
		out.write(result1 + result2);
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
	}
	@RequestMapping("/getConstAllRelated.xhtml")	// 所有关联版块
	public void getConstAllRelated(String v, String citycode, String tag, HttpServletRequest request, HttpServletResponse res) throws Exception{
		if(StringUtils.isBlank(citycode) || StringUtils.isBlank(tag)) {
			res.sendError(404);
			return;
		} 
		String key = tag + citycode + v;
		Long lastModify = modifyMap.get(key);
		if(lastModify != null){
			long modifySince = request.getDateHeader("If-Modified-Since");
			if(modifySince >= lastModify){//直接返回Http 304
				res.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}
		}
		res.setCharacterEncoding("UTF-8");
		res.setContentType("application/x-javascript; charset=UTF-8");
		res.addHeader("Etag", "js" + key.hashCode());
		
		lastModify = System.currentTimeMillis();
		lastModify = lastModify /1000 * 1000;//除去毫秒
		res.addHeader("Cache-Control", "max-age=86400");
		Date cur = new Date();
		Date expire = DateUtil.addMinute(cur, -1);
		res.setDateHeader("Last-Modified", lastModify);
		res.setDateHeader("Expires", expire.getTime());
		PrintWriter out = res.getWriter();
		
		String query = null;
		List<Map> result = new ArrayList<Map>();
		List<Map> result2 = new ArrayList<Map>();
		String js1 = "";
		String js2 = "";
		if(StringUtils.equals(tag, "cinema")){
			query = "select new map(id as id, moviename as name, moviename as skey) from Movie order by abs(releasedate-sysdate+15) asc,clickedtimes desc" ;
			result = daoService.queryByRowsRange(query, 0, MAXNUM);
			String commonjs = JsonUtils.writeObjectToJson(result);
			js1 = "var cinemacategoryid = " + commonjs + "; \n";
			
			query = "select new map(id as id, name as name, name as skey, address as address) from Cinema where citycode = ? order by clickedtimes desc" ;
			result2 = daoService.queryByRowsRange(query, 0, MAXNUM, citycode);
			String commonjs2 = JsonUtils.writeObjectToJson(result2);
			js2 = "var cinemarelatedid = " + commonjs2 + "; \n";
		}else if (StringUtils.equals(tag, "gym")) {
			ErrorCode<List<RemoteCourse>> courseCode = synchGymService.getHotCourseList(0, MAXNUM);
			if(courseCode.isSuccess()){
				List<RemoteCourse> courseList = courseCode.getRetval();
				result = BeanUtil.getBeanMapList(courseList, "id", "name", "skey");
				String commonjs = JsonUtils.writeObjectToJson(result);
				js1 = "var gymcategoryid = " + commonjs + "; \n";
			}
			ErrorCode<List<RemoteGym>> gymCode = synchGymService.getGymList(citycode, null, null, "clickedtimes", false, 0, MAXNUM);
			if(gymCode.isSuccess()){
				List<RemoteGym> gymList = gymCode.getRetval();
				result2 = BeanUtil.getBeanMapList(gymList, "id", "name", "skey", "address");
				String commonjs2 = JsonUtils.writeObjectToJson(result2);
				js2 = "var gymrelatedid = " + commonjs2 + "; \n";
			}
		}else if (StringUtils.equals(tag, "sport")) {
			query = "select new map(id as id, name as name, name as skey, address as address) from Sport where citycode = ? order by clickedtimes desc" ;
			result2 = daoService.queryByRowsRange(query, 0, MAXNUM, citycode);
			String commonjs2 = JsonUtils.writeObjectToJson(result2);
			js2 = "var sportrelatedid = " + commonjs2 + "; \n";
		}else if (StringUtils.equals(tag, "theatre")) {
			query = "select new map(id as id, dramaname as name, dramaname as skey) from Drama where citycode= ? order by releasedate asc,clickedtimes desc" ;
			result = daoService.queryByRowsRange(query, 0, MAXNUM, citycode);
			String commonjs = JsonUtils.writeObjectToJson(result);
			js1 = "var theatrecategoryid = " + commonjs + "; \n";
			
			query = "select new map(id as id, name as name, name as skey, address as address) from Theatre where citycode = ? order by clickedtimes desc" ;
			result2 = daoService.queryByRowsRange(query, 0, MAXNUM, citycode);
			String commonjs2 = JsonUtils.writeObjectToJson(result2);
			js2 = "var theatrerelatedid = " + commonjs2 + "; \n";
		}else if (StringUtils.equals(tag, "activity")) {
			
		}
		out.write(js1 + js2);
		out.flush();
		out.close();
		modifyMap.put(key, lastModify);
	}
}
