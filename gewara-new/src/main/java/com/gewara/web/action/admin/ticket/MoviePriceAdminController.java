package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaCity;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.MovieTierPrice;
import com.gewara.mongo.MongoService;
import com.gewara.service.GewaCityService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.MoviePriceService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.FirstLetterComparator;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class MoviePriceAdminController extends BaseAdminController {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoServie(MongoService mongoService){
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	public void setMcpService(MCPService mcpService) {
		this.mcpService = mcpService;
	}
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	
	@Autowired@Qualifier("moviePriceService")
	private MoviePriceService moviePriceService;
	public void setMoviePriceService(MoviePriceService moviePriceService){
		this.moviePriceService = moviePriceService;
	}
	
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@RequestMapping("/admin/ticket/movieLowestPrice.xhtml")
	public String movieLowestPrice(Long movieid, ModelMap model) {
		Set<Movie> movieSet = new HashSet<Movie>();
		if(movieid!=null){
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieSet.add(movie);
		}else {
			List<Movie> movieList = mcpService.getCurMovieList();
			movieSet.addAll(movieList);
			List<Movie> movieList2 = mcpService.getFutureMovieList(0, 100,null);
			movieSet.addAll(movieList2);
		}
		List<Long> movieidList = BeanUtil.getBeanPropertyList(movieSet, Long.class, "id", true);
		Map<String/*movieid+citycode*/, MoviePrice> mpMap = new HashMap<String, MoviePrice>();
		DetachedCriteria qry = DetachedCriteria.forClass(MoviePrice.class);
		if(movieid!=null) qry.add(Restrictions.eq("movieid", movieid));
		qry.add(Restrictions.in("movieid", movieidList));
		List<MoviePrice> mpList = hibernateTemplate.findByCriteria(qry);
		
		for(MoviePrice mp: mpList){
			mpMap.put(mp.getMovieid()+mp.getCitycode(), mp);
		}
		List<Movie> movieList = new ArrayList<Movie>();
		movieList.addAll(movieSet);
		Collections.sort(movieList, new FirstLetterComparator());
		model.put("movieList", movieList);
		model.put("mpMap", mpMap);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/ticket/movieLowestPrice.vm";
	}
	

	@RequestMapping("/admin/ticket/mpi/setMovieLowestPrice.xhtml")
	public String setMovieLowestPrice(Long movieid, String citycode, Integer price, Integer edition,Integer editionIMAX,Integer editionJumu,
			Integer rangePrice,Integer rangeEdition3D,Integer rangeEditionJumu,Integer rangeEditionIMAX,ModelMap model) {
		if(price == null && edition == null && editionIMAX == null && editionJumu == null && 
				rangePrice == null && rangeEdition3D == null && rangeEditionJumu == null && rangeEditionIMAX == null){
			return showJsonError(model, "价格设置错误！");
		}
		User user = getLogonUser();
		MoviePrice mp = openPlayService.getMoviePrice(movieid, citycode);
		Map cityTierMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_CITYPRICETIER, citycode);
		String type = "";
		if(cityTierMap != null){
			type = (String) cityTierMap.get("tag");
		}
		if (mp == null) {
			if(price == null || price <= 0) return showJsonError(model, "价格设置错误,请先设置2D价格！");
			mp = new MoviePrice(movieid, price, citycode);
			dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片最低价格[" + movieid + "][" + user.getRealname() + "][" + price + "][" + DateUtil.formatTimestamp(new Date()) + "]");
		} else {
			if(price != null && price > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getPrice() + "-->" + price + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setPrice(price);
			}
			if(edition != null && edition > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片3D最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getEdition3D() + "-->" + edition + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setEdition3D(edition);
			}
			if(editionIMAX != null && editionIMAX > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片IMAX最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getEditionIMAX() + "-->" + editionIMAX + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setEditionIMAX(editionIMAX);
			}
			if(editionJumu != null && editionJumu > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片巨幕最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getEditionJumu() + "-->" + editionJumu + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setEditionJumu(editionJumu);
			}
			if(rangePrice != null && rangePrice > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片时间段内最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getRangePrice() + "-->" + rangePrice + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setRangePrice(rangePrice);
			}
			if(rangeEdition3D != null && rangeEdition3D > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片时间段内巨幕最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getRangeEdition3D() + "-->" + rangeEdition3D + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setRangeEdition3D(rangeEdition3D);
			}
			if(rangeEditionJumu != null && rangeEditionJumu > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片时间段内巨幕最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getRangeEditionJumu() + "-->" + rangeEditionJumu + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setRangeEditionJumu(rangeEditionJumu);
			}
			if(rangeEditionIMAX != null && rangeEditionIMAX > 0){
				dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片时间段内IMAX最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getRangeEditionIMAX() + "-->" + rangeEditionIMAX + "][" + DateUtil.formatTimestamp(new Date())
						+ "]");
				mp.setRangeEditionIMAX(rangeEditionIMAX);
			}
		}
		mp.setType(type);
		daoService.saveObject(mp);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/movieBasePrice.xhtml")
	public String movieBasePrice(ModelMap model){
		List<Map> priceTierMapList = mongoService.getMapList(MongoData.NS_PRICETIER);
		model.put("priceTierMapList", priceTierMapList);
		List<Map> cityTierMapList = mongoService.getMapList(MongoData.NS_CITYPRICETIER);
		model.put("cityTierMapList", cityTierMapList);
		model.put("tierCityList",BeanUtil.getBeanPropertyList(cityTierMapList, String.class, "id", true));
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		List<GewaCity> citys = new LinkedList<GewaCity>();
		for(GewaCity city : proMap.keySet()){
			citys.addAll(proMap.get(city));
		}
		Collections.sort(citys, new PropertyComparator("pinyin", false, true));
		model.put("cityList",citys);
		model.put("cityMap", BeanUtil.beanListToMap(citys, "citycode"));
		return "admin/ticket/movieBasePrice.vm";
	}
	@RequestMapping("/admin/ticket/batchMoviePrice.xhtml")
	public String batchMoviePrice(String tag, String edition3D,String editionJUMU,String editionIMAX,ModelMap model){
		User user = getLogonUser();
		List<Map> cityTierMapList = mongoService.find(MongoData.NS_CITYPRICETIER, new HashMap());
		if(cityTierMapList.isEmpty()) return showJsonError(model, "请先设置当前城市所在价格的类型！");
		Set<Movie> movieSet = new HashSet<Movie>();
		List<Movie> movieList1 = mcpService.getCurMovieList();
		movieSet.addAll(movieList1);
		List<Movie> movieList2 = mcpService.getFutureMovieList(0, 100,null);
		movieSet.addAll(movieList2);
		
		List<MoviePrice> moviePriceList = new ArrayList<MoviePrice>();
		for (Movie movie : movieSet) {
			Long movieid = movie.getId();
			for (Map cityTierMap : cityTierMapList) {
				String type = (String) cityTierMap.get("tag");
				String citycode = (String)cityTierMap.get("id");
				MovieTierPrice movieTierPrice = moviePriceService.getMovieTierPrice(movieid, type);
				if(movieTierPrice != null){
					Integer price = movieTierPrice.getPrice();
					Integer edi3D = movieTierPrice.getEdition3D();
					MoviePrice mp = openPlayService.getMoviePrice(movie.getId(), citycode);
					String currDate = DateUtil.formatTimestamp(new Date());
					if (mp == null) {
						mp = new MoviePrice(movieid, price, citycode);
						mp.setType(type);
						if(StringUtils.isNotBlank(edition3D)){
							mp.setEdition3D(edi3D);
							mp.setRangeEdition3D(movieTierPrice.getRangeEdition3D());
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片3D最低价格[" + movieid + "][" + user.getRealname() + "][" + edi3D + "][" + currDate + "]");
						}
						if(StringUtils.isNotBlank(editionJUMU)){
							mp.setEditionJumu(movieTierPrice.getEditionJumu());
							mp.setRangeEditionJumu(movieTierPrice.getRangeEditionJumu());
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片巨幕版本最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionJumu() + "][" + currDate + "]");
						}
						if(StringUtils.isNotBlank(editionIMAX)){
							mp.setEditionIMAX(movieTierPrice.getEditionIMAX());
							mp.setRangeEditionIMAX(movieTierPrice.getRangeEditionIMAX());
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片IMAX最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionIMAX() + "][" + currDate + "]");
						}
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片最低价格[" + movieid + "][" + user.getRealname() + "][" + price + "][" + currDate + "]");
					}else if(StringUtils.isNotBlank(tag)){
						mp.setType(type);
						if(StringUtils.isNotBlank(edition3D)){
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片3D最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getEdition3D() + "-->" + edi3D + "][" + currDate + "]");
							mp.setEdition3D(edi3D);
							mp.setRangeEdition3D(movieTierPrice.getRangeEdition3D());
						}
						if(StringUtils.isNotBlank(editionJUMU)){
							mp.setEditionJumu(movieTierPrice.getEditionJumu());
							mp.setRangeEditionJumu(movieTierPrice.getRangeEditionJumu());
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片巨幕版本最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionJumu() + "][" + currDate + "]");
						}
						if(StringUtils.isNotBlank(editionIMAX)){
							mp.setEditionIMAX(movieTierPrice.getEditionIMAX());
							mp.setRangeEditionIMAX(movieTierPrice.getRangeEditionIMAX());
							dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片IMAX最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionIMAX() + "][" + currDate + "]");
						}
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "修改影片最低价格[" + movieid + "][" + user.getRealname() + "][" + mp.getPrice() + "-->" + price + "][" + currDate + "]");
						mp.setPrice(price);
					}else if(mp.getEdition3D() == null){
						mp.setEdition3D(edi3D);
						mp.setRangeEdition3D(movieTierPrice.getRangeEdition3D());
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片3D最低价格[" + movieid + "][" + user.getRealname() + "][" + edi3D + "][" + currDate + "]");
					}else if(mp.getEditionIMAX() == null){
						mp.setEditionIMAX(movieTierPrice.getEditionIMAX());
						mp.setRangeEditionIMAX(movieTierPrice.getRangeEditionIMAX());
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片IMAX最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionIMAX() + "][" + currDate + "]");
					}else if(mp.getEditionJumu() == null){
						mp.setEditionJumu(movieTierPrice.getEditionJumu());
						mp.setRangeEditionJumu(movieTierPrice.getRangeEditionJumu());
						dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "添加影片巨幕版本最低价格[" + movieid + "][" + user.getRealname() + "][" + movieTierPrice.getEditionJumu() + "][" + currDate + "]");
					}
					mp.setRangePrice(movieTierPrice.getRangePrice());
					mp.setStartTime(movieTierPrice.getStartTime());
					mp.setEndTime(movieTierPrice.getEndTime());
					moviePriceList.add(mp);
				}
			}
		}
		if(!moviePriceList.isEmpty())daoService.saveObjectList(moviePriceList);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/saveCityTier.xhtml")
	public String saveCityTier(String tierId,String id, String tag, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "城市不能为空！");
		if(StringUtils.isBlank(tag)) return showJsonError(model, "类别不能为空！");
		if(!MoviePriceService.PRICE_TIER.contains(tag)) return showJsonError(model, "类别错误！");
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		List<GewaCity> citys = new LinkedList<GewaCity>();
		for(GewaCity city : proMap.keySet()){
			citys.addAll(proMap.get(city));
		}
		Map cityMap = BeanUtil.beanListToMap(citys, "citycode");
		if(StringUtils.isBlank(tierId) && cityMap.get(id)== null) return showJsonError(model, "城市错误！");
		if(StringUtils.isNotBlank(tierId)){
			mongoService.removeObjectById(MongoData.NS_CITYPRICETIER,MongoData.DEFAULT_ID_NAME, tierId);
			return showJsonSuccess(model);
		}
		Map cityTierMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_CITYPRICETIER, id);
		if(cityTierMap == null){
			cityTierMap = new HashMap();
			cityTierMap.put(MongoData.DEFAULT_ID_NAME, id);
		}
		cityTierMap.put("tag", tag);
		mongoService.saveOrUpdateMap(cityTierMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_CITYPRICETIER);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/savePriceTier.xhtml")
	public String saveMoviePrice(String id, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "类别不能为空！");
		if(!MoviePriceService.PRICE_TIER.contains(id)) return showJsonError(model, "类别错误！");
		Map priceTierMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_PRICETIER, id);
		if(priceTierMap != null) return showJsonError(model, "该类别已存在！");
		priceTierMap = new HashMap();
		priceTierMap.put(MongoData.DEFAULT_ID_NAME, id);
		mongoService.saveOrUpdateMap(priceTierMap, MongoData.DEFAULT_ID_NAME, MongoData.NS_PRICETIER);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/saveMoviePriceTier.xhtml")
	public String saveMoviePriceTier(Long movieid, String id, Integer price, Integer edition,Integer editionJumu,
			Integer editionIMAX,Integer rangeEdition3D,Integer rangePrice,Integer rangeEditionJumu,Integer rangeEditionIMAX,
			Timestamp startTime,Timestamp endTime, ModelMap model){
		if(StringUtils.isBlank(id)) return showJsonError(model, "类别不能为空！");
		//if(price == null && edition == null) return showJsonError(model, "价格设置有误！"); 
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return showJsonError(model, "该电影不存在或被删除！");
		Map priceTierMap = mongoService.getMap(MongoData.DEFAULT_ID_NAME, MongoData.NS_PRICETIER, id);
		if(priceTierMap == null) return showJsonError(model, "该类别不存在！");
		ErrorCode<MovieTierPrice> code = moviePriceService.saveOrUpdateMovieTierPrice(movieid, id, price, edition,editionJumu,editionIMAX,
				rangeEdition3D,rangePrice,rangeEditionJumu,rangeEditionIMAX,startTime,endTime);
		if(!code.isSuccess()) return showJsonError(model, code.getMsg());
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/saveMoviePriceTierTime.xhtml")
	public String saveMoviePriceTierTime(Long movieid,Timestamp startTime,Timestamp endTime, ModelMap model){
		if(startTime == null && endTime == null) {
			return showJsonError(model, "请填写时间！"); 
		}
		Movie movie = daoService.getObject(Movie.class, movieid);
		if(movie == null) return showJsonError(model, "该电影不存在或被删除！");
		List<MovieTierPrice> priceTierList = moviePriceService.getMovieTierPriceList(movieid);
		for(MovieTierPrice movieTierPrice : priceTierList){
			if(startTime != null)	{
				movieTierPrice.setStartTime(startTime);
			}
			if(endTime != null)	{
				movieTierPrice.setEndTime(endTime);
			}
		}
		this.daoService.saveObjectList(priceTierList);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/priceTier.xhtml")
	public String priceTier(Long movieid, ModelMap model){
		List<Map> priceTierMapList = mongoService.getMapList(MongoData.NS_PRICETIER);
		model.put("priceTierMapList", priceTierMapList);
		Set<Movie> movieSet = new HashSet<Movie>();
		
		if(movieid!=null){
			Movie movie = daoService.getObject(Movie.class, movieid);
			movieSet.add(movie);
		}else {
			List<Movie> movieList = mcpService.getCurMovieList();
			movieSet.addAll(movieList);
			List<Movie> movieList2 = mcpService.getFutureMovieList(0, 100,null);
			movieSet.addAll(movieList2);
		}
		List<Movie> movieList = new ArrayList<Movie>();
		movieList.addAll(movieSet);
		Collections.sort(movieList, new FirstLetterComparator());
		Map movieTierMap = new HashMap();
		for (Movie movie : movieList) {
			List<MovieTierPrice> movieTierList = moviePriceService.getMovieTierPriceList(movie.getId());
			Map dataMap = BeanUtil.groupBeanList(movieTierList, "type");
			movieTierMap.put(movie.getId(), dataMap);
		}
		model.put("movieList", movieList);
		model.put("movieTierMap", movieTierMap);
		return "admin/ticket/priceTierMapList.vm";
	}
	@RequestMapping("/admin/ticket/diffMoviePrice.xhtml")
	public String diffMoviePrice(HttpServletRequest request, ModelMap model){
		String citycode = getAdminCitycode(request);
		List<MoviePrice> moviePriceList = moviePriceService.getDiffMoviePriceList(citycode);
		Map<Long, MoviePrice> moviePriceMap = new HashMap<Long, MoviePrice>();
		Map<Long, MovieTierPrice> movieTierPriceMap = new HashMap<Long, MovieTierPrice>();
		for (MoviePrice moviePrice : moviePriceList) {
			Long movieid = moviePrice.getMovieid();
			MovieTierPrice movieTierPrice = moviePriceService.getMovieTierPrice(movieid, moviePrice.getType());
			if(movieTierPrice != null){
				movieTierPriceMap.put(movieid, movieTierPrice);
			}
			moviePriceMap.put(movieid, moviePrice);
		}
		List<Long> movieidList = BeanUtil.getBeanPropertyList(moviePriceList, Long.class, "movieid", true);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("moviePriceMap", moviePriceMap);
		model.put("movieTierPriceMap", movieTierPriceMap);
		model.put("movieList", movieList);
		return "admin/ticket/diffMoviePriceMapList.vm";
	}
}
