package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.helper.ticket.AutoSetterHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.json.MovieMpiRemark;
import com.gewara.json.TempRoomSeat;
import com.gewara.model.acl.User;
import com.gewara.model.common.County;
import com.gewara.model.common.GewaCity;
import com.gewara.model.common.JsonData;
import com.gewara.model.express.ExpressConfig;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.GoodsGift;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.PayBank;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenPlayItemExt;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.GewaCityService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.FilmFestService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.service.ticket.OpiManageService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.ticket.MpiSeat;
import com.mongodb.DBObject;


@Controller
public class OpenPlayAdminController extends BaseAdminController {
	@Autowired@Qualifier("nosqlService")
	protected NosqlService nosqlService;
	@Autowired@Qualifier("opiManageService")
	private OpiManageService opiManageService;
	public void setOpiManageService(OpiManageService opiManageService) {
		this.opiManageService = opiManageService;
	}
	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	public void setTicketOperationService(TicketOperationService ticketOperationService) {
		this.ticketOperationService = ticketOperationService;
	}
	@Autowired
	@Qualifier("changeLogService")
	private ChangeLogService changeLogService;
	
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
	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService){
		this.cacheService = cacheService;
	}

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	@Autowired@Qualifier("filmFestService")
	private FilmFestService filmFestService;
	
	@Autowired@Qualifier("gewaCityService")
	private GewaCityService gewaCityService;
	
	@RequestMapping("/admin/ticket/mpiManage.xhtml")
	public String mpiManage(Long cid, Date date, Long rid, Long mid, String provincecode,
			HttpServletRequest request,ModelMap model) {
		User user = getLogonUser();
		Date cur = DateUtil.getCurDate();
		if(date == null) date = cur;
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		if(StringUtils.isBlank(provincecode)) {
			String citycode = getDefaultCitycode(request);
			if(cid!=null){
				Cinema cinema = daoService.getObject(Cinema.class, cid);
				citycode = cinema.getCitycode();
			}
			GewaCity c = daoService.getObjectByUkey(GewaCity.class,"citycode", citycode, true);
			provincecode = c.getProvincecode();
		}
		List<String> cityList = new LinkedList<String>();
		Map<String , GewaCity> cityMap = new HashMap<String, GewaCity>();
		String provinceName = "";
		List<Cinema> cinemaList = new LinkedList<Cinema>();
		Map<String, Map<String,String>> countyMap = new HashMap<String,Map<String,String>>();
		for(GewaCity city : proMap.keySet()){
			if(StringUtils.equals(provincecode, city.getProvincecode())){
				provinceName = city.getProvincename();
				List<GewaCity> gewaCityList = proMap.get(city);
				for(GewaCity c : gewaCityList){
					if (user.getCitycode().contains(c.getCitycode())) {
						List<Cinema> cityCinemaList = mcpService.getBookingCinemaList(c.getCitycode());
						cinemaList.addAll(cityCinemaList);
						cityList.add("'" + c.getCitycode() + "'");
						Map<String, String> cMap = placeService.getCountyPairByCityCode(c.getCitycode());
						cMap.put("其他", "其他");
						countyMap.put(c.getCitycode(), cMap);
					}
				}
				cityMap = BeanUtil.beanListToMap(gewaCityList, "citycode");
			}
		}
		if (cityList.isEmpty()) {
			return forwardMessage(model, "你没有切换:" + provinceName + "的权限！");
		}
		model.put("provincecode", provincecode);
		String citycodes = StringUtils.join(cityList, ",");
		List<Long> bookingidList = BeanUtil.getBeanPropertyList(cinemaList, Long.class, "id", true);
		//if(cinemaList.size() ==0 ) return forwardMessage(model, "未开放订票影院！");
		Map<Long, Cinema> cinemaMap = BeanUtil.beanListToMap(cinemaList, "id");
		model.put("partnerMap", OpiConstant.partnerTextMap);
		model.put("flagMap", OpiConstant.partnerFlagMap);
		model.put("proMap", proMap);
		if(cinemaList.isEmpty()) return "admin/ticket/mpiList.vm";
		Timestamp from = new Timestamp(date.getTime());
		Timestamp to = DateUtil.getLastTimeOfDay(from);
		String countCinemaQuery = "select new map(opi.cinemaid as cid, max(cinemaname) as cname, count(opi.id) as opicount) " +
				"from OpenPlayItem opi where opi.playtime >= ? and opi.playtime <= ? and opi.citycode in ( " + citycodes +
				") group by opi.cinemaid";
		List<Map> cinemaMapList = hibernateTemplate.find(countCinemaQuery, from, to);
		model.put("opiCinemaCount", cinemaMapList.size());
		model.put("openCinemaCount", cinemaList.size());
		if(cid==null){
			 if(cinemaMapList.size() > 0) cid = (Long) cinemaMapList.get(0).get("cid");
			 else cid = cinemaList.get(0).getId();
		}
		Map<String/*countycode*/, List<Map>> cinemaCountyMap = new HashMap<String, List<Map>>();
		Map<String, List<String>> cinemaCityMap = new HashMap<String,List<String>>();
		for(Map m : cinemaMapList){
			Cinema cinema = cinemaMap.get(m.get("cid"));
			cinemaList.remove(cinema);
			if(cinema==null) continue; 
			List<Map> tmp = cinemaCountyMap.get(StringUtils.defaultIfEmpty(cinema.getCountycode(), "其他"));
			if(tmp==null){
				tmp = new ArrayList<Map>();
				cinemaCountyMap.put(StringUtils.defaultIfEmpty(cinema.getCountycode(), "其他"), tmp);
			}
			tmp.add(m);
			List<String> countyTmp = cinemaCityMap.get(cinema.getCitycode());
			if(countyTmp == null){
				countyTmp = new ArrayList<String>();
				cinemaCityMap.put(cinema.getCitycode(), countyTmp);
			}
			if(!countyTmp.contains(StringUtils.defaultIfEmpty(cinema.getCountycode(), "其他"))){
				countyTmp.add(StringUtils.defaultIfEmpty(cinema.getCountycode(), "其他"));
			}
		}
		Map<String,List<String>> noOpiCinemaCityMap = new HashMap<String,List<String>>();
		for(Cinema c : cinemaList){
			List<String> countyTmp = noOpiCinemaCityMap.get(c.getCitycode());
			if(countyTmp == null){
				countyTmp = new ArrayList<String>();
				noOpiCinemaCityMap.put(c.getCitycode(), countyTmp);
			}
			if(!countyTmp.contains(StringUtils.defaultIfEmpty(c.getCountycode(), "其他"))){
				countyTmp.add(StringUtils.defaultIfEmpty(c.getCountycode(), "其他"));
			}
		}
		Map<String, List<Cinema>> noOpiCinemaMap = BeanUtil.groupBeanList(cinemaList, "countycode", "其他");
		model.put("countyMap", countyMap);
		model.put("cinemaMap", cinemaMap);
		model.put("noOpiCinemaMap", noOpiCinemaMap);
		model.put("noOpiCinemaCityMap", noOpiCinemaCityMap);
		model.put("cinemaCountyMap", cinemaCountyMap);
		model.put("cinemaCityMap", cinemaCityMap);
		model.put("cityMap",cityMap);
		
		Map<String, String> dateMap = new HashMap<String, String>();
		List<String> dateList = new ArrayList<String>();
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for(int i=0;i< 7; i++){
			String d = DateUtil.formatDate(DateUtil.addDay(cur, i));
			dateList.add(d);
			String dateStr = DateUtil.format(DateUtil.addDay(cur, i), "M月d日");
			dateMap.put(d, dateStr + " " + DateUtil.getCnWeek(DateUtil.addDay(cur, i)));
			Integer count = mcpService.getCinemaMpiCountByDate(cid, DateUtil.addDay(cur, i));
			countMap.put(d, count);
		}
		model.put("dateList", dateList);
		model.put("dateMap", dateMap);
		model.put("countMap", countMap);
		Cinema cinema = cinemaMap.get(cid);
		if(cinema==null) return forwardMessage(model, "此影院未开放订票！");
		model.put("cinema", cinema);
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cid);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		Map<Long, CinemaRoom> roomMap = BeanUtil.beanListToMap(roomList, "id");
		List<Long> roomIdList = BeanUtil.getBeanPropertyList(roomList, Long.class, "id", true);
		Map<Long, List<TempRoomSeat>> tempSeatMap = getTemplateMap(roomIdList);
		model.put("roomMap", roomMap);
		model.put("tempSeatMap", tempSeatMap);
		model.put("roomList", roomList);
		List<Movie> movieList = mcpService.getCurMovieListByCinemaIdAndDate(cid, date);
		List<MoviePlayItem> playitemList = new ArrayList<MoviePlayItem>();
		Map<Long, Movie> movieMap = BeanUtil.beanListToMap(movieList, "id");
		model.put("movieMap", movieMap);
		if(rid != null){
			playitemList = mcpService.getCinemaCurMpiListByRoomIdAndDate(rid, date);
		}else{
			playitemList = mcpService.getCurMpiList(cid, mid, date);
		}
		model.put("movieList", movieList);
		List<OpenPlayItem> opiList = openPlayService.getOpiList(null, cid, null, from, to, false);
		Map<Long, Goods> goodMap=new HashMap<Long, Goods>();
		for(OpenPlayItem opi : opiList){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, null);
			if(goodsGift!=null) {
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				goodMap.put(opi.getId(), goods);
			}
		}
		model.put("goodMap", goodMap);
		List<Long> movieidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "movieid", true);
		model.put("setExpandMovie", BeanUtil.groupBeanProperty(opiList, "movieid", "mpid"));
		model.put("opiList", opiList);
		model.put("movieidList", movieidList);
		Map movienameMap = BeanUtil.getKeyValuePairMap(opiList, "movieid", "moviename");
		model.put("movienameMap", movienameMap);
		List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
		List<MoviePlayItem> tmpMpiList = new ArrayList<MoviePlayItem>(playitemList);
		//去除已开放的场次
		for(MoviePlayItem mpi: tmpMpiList) if(mpidList.contains(mpi.getId())) playitemList.remove(mpi);
		model.put("playitemList", playitemList);
		model.put("curDate", DateUtil.formatDate(date));
		model.put("curTime", DateUtil.formatTimestamp(new Timestamp(System.currentTimeMillis())));
		model.put("userid", getLogonUser().getId());
		String status = AutoSetter.STATUS_OPEN;
		Map<String,String> limit = nosqlService.getAutoSetterLimit();
		if(limit != null){
			String[] playDateS = StringUtils.split(limit.get("playDate"),",");
			List<String> playDate = new ArrayList<String>();
			if(playDateS != null){
				playDate = Arrays.asList(playDateS);
				if(playDate.contains(DateUtil.format(date, "yyyy-MM-dd"))){
					status = null;
				}
			}
		}
		//自动设置器   状态为手动的
		List<AutoSetter> setterList = openPlayService.getValidSetterList(cinema.getId(),status);
		model.put("setterList", setterList);
		Map<Long, AutoSetter> setterMap = new HashMap<Long, AutoSetter>();
		for(MoviePlayItem mpi:playitemList){
			for(AutoSetter setter: setterList){
				CachedScript limitCs = null;
				if(StringUtils.isNotBlank(setter.getLimitScript())){
					limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
				}
				if(AutoSetterHelper.isMatch(setter, mpi,null,limitCs)){
					setterMap.put(mpi.getId(), setter);
					break;
				}
			}
		}
		model.put("setterMap", setterMap);
		Map<Long,Map<String, String>> changeSetterLog = new HashMap<Long,Map<String, String>>();
		for(AutoSetter setter: setterList){
			Map<Long, Map<String, String>> result = changeLogService.getChangeLogList(Config.SYSTEMID, "AutoSetter", setter.getId());
			if(!result.isEmpty()){
				List<Map<String, String>> tmpList = new LinkedList<Map<String, String>>(result.values());
				changeSetterLog.put(setter.getId(),tmpList.get(tmpList.size() - 1));
			}
		}
		model.put("changeSetterLog",changeSetterLog);
		
		//还有未开放场次的影院
		String qry = "select new map(cinemaid as cinemaid, count(id) as unopen) from MoviePlayItem p " +
				"where playdate = ? and openStatus= ? and citycode in ( " + citycodes +
				") group by cinemaid";
		Map unopenedCinema = new HashMap();
		List<Map> rowList = hibernateTemplate.find(qry, date, OpiConstant.MPI_OPENSTATUS_INIT);
		for(Map row:rowList){
			if(bookingidList.contains(row.get("cinemaid"))) unopenedCinema.put(row.get("cinemaid"), row.get("unopen"));
		}
		model.put("unopenedCinema", unopenedCinema);
		Map unopenItemCinema = new HashMap();
		String qryItem = "select new map(p.cinemaid as cinemaid, count(id) as num) from OpenPlayItem p " +
				"where p.playtime >= ? and p.playtime <= ? and p.status= ? and p.citycode in ( " + citycodes +
				") group by p.cinemaid";
		List<Map> rowItemList = hibernateTemplate.find(qryItem, from, to, OpiConstant.STATUS_NOBOOK);
		for(Map row:rowItemList){
			if(bookingidList.contains(row.get("cinemaid"))) unopenItemCinema.put(row.get("cinemaid"), row.get("num"));
		}
		model.put("unopenItemCinema", unopenItemCinema);
		
		
		//刚从删除状态恢复的场次！
		Map recorverItemCinema = new HashMap();
		rowItemList = hibernateTemplate.find(qryItem, from, to, OpiConstant.STATUS_RECOVER);
		for(Map row:rowItemList){
			if(bookingidList.contains(row.get("cinemaid"))) recorverItemCinema.put(row.get("cinemaid"), row.get("num"));
		}
		model.put("recorverItemCinema", recorverItemCinema);
		
		String key = CacheConstant.KEY_OPIGATHER + user.getId();
		String mpids = (String) cacheService.get(CacheConstant.REGION_ONEHOUR, key);
		if(StringUtils.isNotBlank(mpids)){
			model.put("mpidList", BeanUtil.getIdList(mpids, ","));
		}
		model.put("user", user);
		model.put("profile", profile);
		model.put("editionList", OpiConstant.EDITIONS);//版本列表
		Map<String, String> eMap = new HashMap<String, String>();
		int i = 0;
		for(String s : OpiConstant.EDITIONS){
			eMap.put(s, "ed"+(i++));
		}
		model.put("eMap", eMap);
		List<String> languages = BeanUtil.getBeanPropertyList(opiList, String.class, "language", true);
		Map<String, String> languageMap = new HashMap<String, String>();
		for(String language : languages){
			languageMap.put(language, "language" + (i++));
		}
		model.put("languageMap",languageMap);
		model.put("languages",languages);
		String hashAutoSetterCinemaQuery = "select opi.cinemaid as cid " +
				"from OpenPlayItem opi where opi.playtime > ? and opi.playtime < ? and opi.citycode in ( " + citycodes +
				") and otherinfo like ? group by opi.cinemaid";
		List<Long> hashAutoSetterCinema = hibernateTemplate.find(hashAutoSetterCinemaQuery, from, to,"%\"autoOpenStatus\":\"open_a\"%");
		model.put("hashAutoSetterCinema", hashAutoSetterCinema);
		return "admin/ticket/mpiList.vm";
	}
	
	@RequestMapping("/admin/ticket/replaceOpiSeat.xhtml")
	public String replaceOpiSeat(Long mpid, String update, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return forwardMessage(model, "场次数据不存在！");
		List<String> msgList = new ArrayList();
		ErrorCode<List<MpiSeat>> code = mpiOpenService.refreshOpiSeat(mpid, getLogonUser().getId(), Boolean.parseBoolean(update), msgList);
		if(!code.isSuccess()){
			return forwardMessage(model, code.getErrcode() + ":" + code.getMsg());
		}else if(Boolean.parseBoolean(update)){
			return forwardMessage(model, msgList);
		}
		List<MpiSeat> seatList = code.getRetval();
		Collections.sort(seatList, new MultiPropertyComparator(new String[] { "seatline", "seatrank" }, new boolean[] { true, true }));
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, MpiSeat> seatMap = new HashMap<String, MpiSeat>();
		int maxlinenum = 0, maxranknum = 0;
		for (MpiSeat seat : seatList) {
			maxlinenum = Math.max(maxlinenum, seat.getLineno());
			maxranknum = Math.max(maxranknum, seat.getRankno());
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("maxlinenum", maxlinenum);
		model.put("maxranknum", maxranknum);
		model.put("opi", opi);
		return "admin/ticket/mpiSeat.vm";
	}
	
	@RequestMapping("/admin/ticket/verifyUnopenMpi.xhtml")
	public String unopenMpiList(ModelMap model){
		List<MoviePlayItem> opiList = openPlayService.synchUnOpenMpi();
		return showMessage(model, "共校验：" + opiList.size() + "个:" + BeanUtil.getBeanMapList(opiList, "mpid", "moviename", "playdate", "cinemaname"));
	}
	private Map<Long, List<TempRoomSeat>> getTemplateMap(List<Long> roomIdList){
		DBObject queryCondition = mongoService.queryBasicDBObject("roomid", "in", roomIdList);
		List<TempRoomSeat> resultList = mongoService.getObjectList(TempRoomSeat.class, queryCondition);
		Collections.sort(resultList, new MultiPropertyComparator(new String[]{"tmpname"}, new boolean[]{true}));
		Map<Long, List<TempRoomSeat>> dataMap = BeanUtil.groupBeanList(resultList, "roomid");
		return dataMap;
	}
	@RequestMapping("/admin/ticket/mpiOpenStats.xhtml")
	public String mpiOpenStats(String provincecode, ModelMap model){
		List<Map> rowList = new ArrayList<Map>();
		List<Map> qryList = null;
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		model.put("proMap", proMap);
		Map<String , GewaCity> cityMap = new HashMap<String, GewaCity>();
		Map<String , GewaCity> provinceMap = new HashMap<String, GewaCity>();
		List<String> cityList = new LinkedList<String>();
		for(GewaCity city : proMap.keySet()){
			List<GewaCity> gewaCityList = proMap.get(city);
			cityMap.putAll(BeanUtil.beanListToMap(gewaCityList, "citycode"));
			if(StringUtils.equals(provincecode, city.getProvincecode())){
				for(GewaCity c : gewaCityList){
					cityList.add("'" + c.getCitycode() + "'");
				}
			}
			provinceMap.put(city.getProvincecode(), city);
		}
		model.put("cityMap", provinceMap);
		//开放场次统计
		if(StringUtils.isBlank(provincecode)) {
			Date curr = DateUtil.currentTime();
			String qry_mpi = "select new map(citycode as key, openStatus as openStatus, count(id) as total ,playdate as playdate) from MoviePlayItem p " +
					"where cinemaid in (select id from Cinema c where c.booking=?) and p.gewaprice is null  and playdate in (?,?,?,?,?) group by citycode, openStatus ,playdate";
			rowList = hibernateTemplate.find(qry_mpi, Cinema.BOOKING_OPEN, curr, DateUtil.addDay(curr, 1), DateUtil.addDay(curr, 2), DateUtil.addDay(curr, 3), DateUtil.addDay(curr, 4));
			String qry_opi = "select new map(citycode as key, status as openStatus, count(id) as total , to_date(to_char(playtime,'yyyy-MM-dd'),'yyyy-MM-dd') as playdate) from OpenPlayItem p " +
					"where playtime>=? and playtime<? and closetime>? group by citycode, status, to_date(to_char(playtime,'yyyy-MM-dd'),'yyyy-MM-dd')";
			qryList = hibernateTemplate.find(qry_opi, DateUtil.getBeginningTimeOfDay(curr), DateUtil.getBeginningTimeOfDay(DateUtil.addDay(curr, 5)), curr);
			rowList.addAll(qryList);
			List<Map> proRowList = new ArrayList<Map>();
			Map<String,Long> proMaps = new HashMap<String,Long>();
			for(Map row:rowList){
				String key = cityMap.get(row.get("key")).getProvincecode() + "@" + row.get("openStatus") + "@" + DateUtil.format(DateUtil.parseDate(row.get("playdate")+""), "yyyy-MM-dd");
				Long proCount = proMaps.get(key);
				proMaps.put(key, proCount == null ? (Long)row.get("total") : (Long)row.get("total") + proCount);
			}
			for(String key : proMaps.keySet()){
				Map map = new HashMap();
				String[] ks = StringUtils.split(key,"@");
				map.put("key", ks[0]);
				map.put("openStatus", ks[1]);
				map.put("playdate", ks[2]);
				map.put("total", proMaps.get(key));
				proRowList.add(map);
			}
			rowList = proRowList;
			model.put("curCitycode", "000000");
		}else{
			String citycodes = StringUtils.join(cityList, ",");
			Timestamp cur_time = DateUtil.getCurFullTimestamp();
			String qry = "select new map(playdate as key, openStatus as openStatus, count(id) as total) from MoviePlayItem p " +
					"where citycode in (" + citycodes + ") and cinemaid in (select id from Cinema c where c.citycode in (" + citycodes + ") and c.booking=?) and p.gewaprice is null  group by playdate, openStatus";
			rowList = hibernateTemplate.find(qry,Cinema.BOOKING_OPEN);
			String qry_opi = "select new map(to_date(to_char(playtime,'yyyy-MM-dd'),'yyyy-MM-dd') as key, status as openStatus, count(id) as total) from OpenPlayItem " +
					"where citycode in (" + citycodes + ") and closetime > ? group by status, to_date(to_char(playtime,'yyyy-MM-dd'),'yyyy-MM-dd') ";
			qryList = hibernateTemplate.find(qry_opi, cur_time);
			rowList.addAll(qryList);
			model.put("curCitycode", provincecode);
		}
		Map<Object, Map> dateStatsMap = new TreeMap<Object, Map>();
		for(Map row:rowList){
			String key = "";
			if(row.get("playdate") == null) key = (DateUtil.parseDate(row.get("key")+"") == null)? row.get("key")+"" :DateUtil.format(DateUtil.parseDate(row.get("key")+""), "yyyy-MM-dd");
			else key = row.get("key")+""+DateUtil.format(DateUtil.parseDate(row.get("playdate")+""), "yyyy-MM-dd");
			Map data = dateStatsMap.get(key);
			if(data==null){
				data = new HashMap();
				dateStatsMap.put(key, data);
			}
			data.put(row.get("openStatus"), row.get("total"));
		}
		model.put("dateStatsMap", dateStatsMap);
		//当前所有开放场次延迟
		String qry2 = "select new map(openuser as userid, avg(delayMin) as avgdelay, " +
				"max(delayMin) as maxdelay, min(delayMin) as mindelay, count(id) as total) " +
				"from OpenPlayItemExt t where opentime > ? and openuser >0 group by openuser";
		Timestamp statsTime = DateUtil.addDay(DateUtil.getCurTruncTimestamp(), -3);
		List<Map> userStatsList = hibernateTemplate.find(qry2, statsTime);
		model.put("statsTime", statsTime);
		model.put("userStatsList", userStatsList);
		model.put("curCitycode", provincecode);
		Map<Long, String> usernameMap = daoService.getObjectPropertyMap(User.class, "id", "nickname");
		model.put("usernameMap", usernameMap);
		return "admin/ticket/mpiOpenStats.vm";
	}
	@RequestMapping("/admin/ticket/extensions.xhtml")
	public String extensions(@RequestParam("mpid")Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		if(opi == null) return showMessage(model, "本场不接受预订！");
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		model.put("room", room);
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, OpenSeat> seatMap = new HashMap<String, OpenSeat>();
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(mpid);
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);

		for(OpenSeat seat:openSeatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
		model.put("opi", opi);
		model.put("cinema", cinema);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		OpenPlayItemExt openPlayItemExt = daoService.getObjectByUkey(OpenPlayItemExt.class, "mpid", mpid, true);
		model.put("opiExt", openPlayItemExt);
		return "admin/ticket/extensions.vm";
	}
	@RequestMapping("/admin/ticket/seatpriceAll.xhtml")
	public String seatpriceAll(String[] movies, ModelMap model){
		if(movies == null){
			 return showMessage(model, "没有可拓展信息设置的场次！");
		}
		List<Long> movieIdList = new ArrayList<Long>();
		Map<Long,String> movieMap = new HashMap<Long,String>();
		for(String movie : movies){
			String[] ms = StringUtils.split(movie,":");
			Long movieId = Long.parseLong(ms[0]);
			movieIdList.add(movieId);
			movieMap.put(movieId,ms[1]);
		}
		model.put("movieMap", movieMap);
		model.put("movieList", daoService.getObjectList(Movie.class, movieIdList));
		return "admin/ticket/extensionsAll.vm";
	}
	
	@RequestMapping("/admin/ticket/modifyMsg.xhtml")
	public String modifyMsg(Long opid, ModelMap model) {
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		model.put("opi", opi);
		JsonData template = daoService.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE+opi.getId());
		if(template!=null){
			model.put("msgMap", VmUtils.readJsonToMap(template.getData()));
		}
		CinemaProfile cp = daoService.getObject(CinemaProfile.class, opi.getCinemaid());
		model.put("cp", cp);
		return "admin/ticket/modifyMsg.vm";
	}
	@RequestMapping("/admin/ticket/saveMsg.xhtml")
	public String saveMpi(Long opid, String notifymsg1, String notifymsg2, ModelMap model) {
		JsonData template = daoService.getObject(JsonData.class, JsonDataKey.KEY_SMSTEMPLATE+opid);
		if(StringUtils.isBlank(notifymsg1) && StringUtils.isBlank(notifymsg2)){
			if(template==null){
				return showMessage(model, "请输入短信模版内容！");
			}else{
				daoService.removeObject(template);
				monitorService.saveDelLog(getLogonUser().getId(), template.getDkey(), template);
				return showMessage(model, "清除短信成功！");
			}
		}else{
			if(template==null) {
				template = new JsonData(JsonDataKey.KEY_SMSTEMPLATE+opid);
			}
			ChangeEntry changeEntry = new ChangeEntry(template);
			Map<String, String> dataMap = new HashMap<String, String>();
			if(StringUtils.isNotBlank(notifymsg1)) dataMap.put("notifymsg1", notifymsg1.trim());
			if(StringUtils.isNotBlank(notifymsg2)) dataMap.put("notifymsg2", notifymsg2.trim());
			OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
			template.setValidtime(DateUtil.addDay(opi.getPlaytime(), 1));
			template.setData(JsonUtils.writeMapToJson(dataMap));
			daoService.saveObject(template);
			monitorService.saveChangeLog(getLogonUser().getId(), JsonData.class, JsonDataKey.KEY_SMSTEMPLATE+opid, changeEntry.getChangeMap(template));
			return showMessage(model, "设置短信成功！");
		}
	}
	@RequestMapping("/admin/ticket/modifyMpi.xhtml")
	public String modifyMpi(Long opid, ModelMap model) {
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		model.put("opi", opi);
		model.put("editionList", OpiConstant.EDITIONS);
		return "admin/ticket/modifyMpi.vm";
	}
	@RequestMapping("/admin/ticket/movieMpiRemarkList.xhtml")
	public String movieMpiRemarkList(ModelMap model){
		String now = DateUtil.format(DateUtil.currentTime(), "yyyy-MM-dd HH:mm:ss");
		List<MovieMpiRemark> remarks = mongoService.getObjectList(MovieMpiRemark.class,mongoService.queryBasicDBObject("validTime", ">", now), "validTime", true, 0, 500);
		model.put("remarks",remarks);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/ticket/movieMapiRemarks.vm";
	}
	
	@RequestMapping("/admin/ticket/remarksDetail.xhtml")
	public String remarksDetail(String id, ModelMap model) {
		if(id!=null) {
			model.put("remark",mongoService.getObject(MovieMpiRemark.class, MongoData.SYSTEM_ID, id));
		}
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/ticket/remarksDetail.vm";
	}
	@RequestMapping("/admin/ticket/ajax/delteMoiveMpiRemark.xhtml")
	public String delteMoiveMpiRemark(String id,ModelMap model){
		if(StringUtils.isBlank(id)){
			return showJsonError(model, "设置选择对应id");
		}
		MovieMpiRemark r = mongoService.getObject(MovieMpiRemark.class, MongoData.SYSTEM_ID, id);
		if(r == null){
			return showJsonError(model, "要删除的对象已经不存在");
		}
		mongoService.removeObject(r, MongoData.SYSTEM_ID);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/ajax/saveMovieMpiRemark.xhtml")
	public String saveMovieMpiRemark(String id,Long movieId,String remark,Timestamp validTime,String citycode, ModelMap model) {
		MovieMpiRemark r = new MovieMpiRemark();
		if(StringUtils.isNotBlank(id)) {
			r = mongoService.getObject(MovieMpiRemark.class, MongoData.SYSTEM_ID, id);
		}
		if(movieId == null){
			return showJsonError(model, "电影id不存在！");
		}
		Movie movie = this.daoService.getObject(Movie.class, movieId);
		if(movie == null){
			return showJsonError(model, "选择的电影id错误，没有对应的影片");
		}
		if(validTime == null){
			return showJsonError(model, "请设置有效时间");
		}
		r.setMovieId(movieId);
		r.setCityCode(citycode);
		r.setRemark(remark);
		r.setValidTime(DateUtil.format(validTime, "yyyy-MM-dd HH:mm:ss"));
		if(StringUtils.isBlank(id)){
			r.set_id(MongoData.buildId());
		}
		mongoService.saveOrUpdateObject(r,  MongoData.SYSTEM_ID);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/saveMpi.xhtml")
	public String saveMpi(Long opid, String edition, String language, Integer price, String playtime, 
			Long topicid, String remark, String dayotime, String dayctime, Integer buylimit, Integer asellnum,
			 String sptitle, String smpno, String lymovieids, ModelMap model) {
		if(!OpiConstant.isValidEdition(edition)) return showJsonError(model, "影片版本错误！");
		Long userid = getLogonUser().getId();
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, opi.getMpid());
		if(mpi == null) return showJsonError(model, "场次信息不存在！");
		
		ChangeEntry opiChangeEntry = new ChangeEntry(opi);
		ChangeEntry mpiChangeEntry = new ChangeEntry(mpi);
		mpi.setEdition(edition);
		mpi.setLanguage(language);
		mpi.setPrice(price);
		mpi.setPlaytime(playtime);
		if(StringUtils.isNotBlank(smpno)){
			mpi.setRemark(smpno);
		}
		opi.setDayotime(dayotime);
		opi.setDayctime(dayctime);
		opi.setEdition(edition);
		opi.setPrice(price);
		opi.setPlaytime(Timestamp.valueOf(mpi.getFullPlaytime()));
		opi.setLanguage(language);
		opi.setTopicid(topicid);
		
		MoviePrice moviePrice = openPlayService.getMoviePrice(opi.getMovieid(), opi.getCitycode());
		if(moviePrice != null){
			ErrorCode<Integer> code = OpiConstant.getLowerPrice(edition, moviePrice,opi.getPlaytime());
			if(!code.isSuccess()) return showJsonError(model, code.getMsg());
			int lowerPrice = code.getRetval();
			if(opi.getCostprice() != null && lowerPrice > opi.getCostprice()) {
				return showJsonError(model, "修改了版本，成本价不能低于影片最低卖价！");
			}
		}else{
			return showJsonError(model,"影片" + opi.getMovieid() + "未设置最低价格!");
		}
		
		if(StringUtils.isNotBlank(sptitle)) {
			opi.setOtherinfo(JsonUtils.addJsonKeyValue(opi.getOtherinfo(), "sptitle", sptitle));
		}else {
			opi.setOtherinfo(JsonUtils.removeJsonKeyValue(opi.getOtherinfo(), "sptitle"));
		}
		if(StringUtils.isNotBlank(lymovieids)) {
			List<Long> midList = BeanUtil.getIdList(lymovieids, ",");
			for(Long mid : midList){
				Movie movie = daoService.getObject(Movie.class, mid);
				if(movie==null) return showJsonError(model, mid+"对应的电影不存在！");
			}
			opi.setOtherinfo(JsonUtils.addJsonKeyValue(opi.getOtherinfo(), OpiConstant.LYMOVIEIDS, lymovieids));
		}else {
			opi.setOtherinfo(JsonUtils.removeJsonKeyValue(opi.getOtherinfo(), OpiConstant.LYMOVIEIDS));
		}
		if(StringUtils.isNotBlank(smpno)){
			opi.setOtherinfo(JsonUtils.addJsonKeyValue(opi.getOtherinfo(), OpiConstant.SMPNO, smpno));
		}else {
			boolean isFilmMpi = filmFestService.isFilmMoviePlayItem(mpi.getBatch());
			if(isFilmMpi){
				return showJsonError(model, "电影节场次必须设定指定场次号！"); 
			}
			opi.setOtherinfo(JsonUtils.removeJsonKeyValue(opi.getOtherinfo(), OpiConstant.SMPNO));
		}
		opi.setRemark(remark);
		if(asellnum != null) opi.setAsellnum(asellnum);
		if(buylimit != null) opi.setBuylimit(""+buylimit);
		else if(StringUtils.isNotBlank(opi.getBuylimit())) opi.setBuylimit(null);
		daoService.saveObject(mpi);
		monitorService.saveChangeLog(userid, MoviePlayItem.class, mpi.getId(), mpiChangeEntry.getChangeMap(mpi));
		daoService.saveObject(opi);
		Map diff = opiChangeEntry.getChangeMap(opi);
		monitorService.saveChangeLog(userid, OpenPlayItem.class, mpi.getId(), diff);
		opiManageService.updateOriginInfo(mpi);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/opiOther.xhtml")
	public String opiOther(Long opid, ModelMap model) {
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		CinemaProfile cp = daoService.getObject(CinemaProfile.class, opi.getCinemaid());
		List<PayBank> bankList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("opi", opi);
		model.put("isRefund", cp.getIsRefund());
		model.put("otherinfo", opi.getOtherinfo());
		model.put("confPayList", bankList);
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/ticket/opiOther.vm";
	}
	@RequestMapping("/admin/ticket/saveOpiOther.xhtml")
	public String saveOpiOther(Long opid, String isRefund, String address,
			String payoption, String paymethodlist, String defaultpaymethod, String expressid,
			String cardoption, String mealoption, String batchidlist, String unopengewa, String unshowgewa, ModelMap model) {
		OpenPlayItem opi = daoService.getObject(OpenPlayItem.class, opid);
		ChangeEntry changeEntry = new ChangeEntry(opi);
		Map<String, String> otherinfo = VmUtils.readJsonToMap(opi.getOtherinfo());
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		if(StringUtils.equals(payoption, "del")) {
			otherinfo.remove(OpiConstant.PAYOPTION);
			otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
		}else if(StringUtils.isNotBlank(payoption)){
			otherinfo.put(OpiConstant.PAYOPTION, payoption);
			if(StringUtils.isNotBlank(paymethodlist)) { 
				paymethodlist = checkpaymethodlist(paymethodlist);
				List<String> paymethodList = Arrays.asList(StringUtils.split(paymethodlist, ","));
				if(StringUtils.isBlank(defaultpaymethod) && paymethodList.size()!=1) return showJsonError(model, "请选择默认支付方式");
				
				otherinfo.put(OpiConstant.DEFAULTPAYMETHOD, defaultpaymethod);
				otherinfo.put(OpiConstant.PAYCMETHODLIST, paymethodlist);
			}else {
				otherinfo.remove(OpiConstant.DEFAULTPAYMETHOD);
				otherinfo.remove(OpiConstant.PAYCMETHODLIST);
			}
			if(StringUtils.equals(payoption, "notuse") && StringUtils.isBlank(paymethodlist)){
				return showJsonError(model, "支付方式选择不可用，必须勾选支付方式！");
			}
		}
		if(StringUtils.equals(cardoption, "del")) {
			otherinfo.remove(OpiConstant.CARDOPTION);
			otherinfo.remove(OpiConstant.BATCHIDLIST);
		}else if(StringUtils.isNotBlank(cardoption) && StringUtils.isNotBlank(batchidlist)){
			String[] batchidList = StringUtils.split(batchidlist, ",");
			for(String batchid : batchidList){
				ElecCardBatch batch = daoService.getObject(ElecCardBatch.class, new Long(batchid));
				if(batch==null) return showJsonError(model, batchid+"对应的批次不存在！");
			}
			otherinfo.put(OpiConstant.CARDOPTION, cardoption);
			otherinfo.put(OpiConstant.BATCHIDLIST, batchidlist);
		}
		if(StringUtils.equals(mealoption, "del")){
			otherinfo.remove(OpiConstant.MEALOPTION);
		}else {
			otherinfo.put(OpiConstant.MEALOPTION, mealoption);
		}
		if(StringUtils.isBlank(address)){
			otherinfo.remove(OpiConstant.ADDRESS);
		}else{
			if (StringUtils.isBlank(expressid)) {
				return showJsonError(model, "必须填写快递方式！");
			}
			ExpressConfig config = daoService.getObject(ExpressConfig.class, expressid);
			if(config == null) {
				return showJsonError(model, "编号为：" + expressid + ",的配送方式不存在或被删除！");
			}
			otherinfo.put(OpiConstant.ADDRESS, address);
			opi.setExpressid(expressid);
		}
		if(StringUtils.isBlank(unopengewa)){
			otherinfo.remove(OpiConstant.UNOPENGEWA);
		}else{
			otherinfo.put(OpiConstant.UNOPENGEWA, unopengewa);
		}
		if(StringUtils.isBlank(isRefund)){
			otherinfo.remove(OpiConstant.ISREFUND);
		}else{
			otherinfo.put(OpiConstant.ISREFUND, isRefund);
		}
		if(StringUtils.isBlank(unshowgewa)){
			otherinfo.remove(OpiConstant.UNSHOWGEWA);
		}else{
			otherinfo.put(OpiConstant.UNSHOWGEWA, unshowgewa);
		}
		
		opi.setOtherinfo(JsonUtils.writeMapToJson(otherinfo));
		daoService.saveObject(opi);
		monitorService.saveChangeLog(getLogonUser().getId(), OpenPlayItem.class, opi.getMpid(), changeEntry.getChangeMap(opi));
		return showJsonSuccess(model);
	}
	private String checkpaymethodlist(String paymethodlist){
		if(StringUtils.equals(paymethodlist, ",")) paymethodlist = "";
		return VmUtils.printList(Arrays.asList(StringUtils.split(paymethodlist, ",")));
	}

	@RequestMapping("/admin/ticket/updateOpiStats.xhtml")
	public String updateOpiStat(Long mpid, ModelMap model) {
		//只有电影节时使用
		//int count = opiManageService.verifyOpiSeatLock(mpid);
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_ADDORDER, true);
		if(!remoteLockList.isSuccess()) {
			return showJsonError(model, remoteLockList.getMsg());
		}
		String status = opi.seatAmountStatus();
		opiManageService.updateOpiStats(opi.getId(), remoteLockList.getRetval(), false);
		Map jsonMap = BeanUtil.getBeanMapWithKey(opi, "gsellnum", "csellnum", "locknum", "remainnum", "updatetime");
		String newStatus = opi.seatAmountStatus();
		if(!StringUtils.equals(status, newStatus)){//座位数量有变动
			mpiOpenService.refreshMpiRelatePage(opi);
		}
		return showJsonSuccess(model, jsonMap);
	}

	@RequestMapping("/admin/ticket/saveOpenPlayItemExt.xhtml")
	public String saveOpenPlayItemExt(Long mpid, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		OpenPlayItemExt openPlayItemExt = daoService.getObjectByUkey(OpenPlayItemExt.class, "mpid", mpid, true);
		if(openPlayItemExt == null) return showJsonError(model, "该场次扩展数据不存在！");
		Map dataMap = request.getParameterMap();
		ChangeEntry changeEntry = new ChangeEntry(openPlayItemExt);
		BindUtils.bindData(openPlayItemExt, dataMap);
		if(openPlayItemExt.getActualprice() <0){
			return showJsonError(model, "结算价调整额不能小于0！");
		}
		if(StringUtils.isBlank(openPlayItemExt.getSettle())){
			return showJsonError(model, "是否结算不能为空！");
		}
		if(StringUtils.isBlank(openPlayItemExt.getImprest())){
			return showJsonError(model, "是否预付费不能为空！");
		}
		daoService.saveObject(openPlayItemExt);
		monitorService.saveChangeLog(user.getId(), OpenPlayItemExt.class, openPlayItemExt.getMpid(), changeEntry.getChangeMap(openPlayItemExt));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/saveMovieOpenPlayItemExt.xhtml")
	public String saveMovieOpenPlayItemExt(String mpids, HttpServletRequest request, ModelMap model){
		User user = getLogonUser();
		if(StringUtils.isBlank(mpids)){
			return showJsonError(model, "请选择场次！");
		}
		String[] mpidList = StringUtils.split(mpids,",");
		Map dataMap = request.getParameterMap();
		for(String mpid : mpidList){
			OpenPlayItemExt openPlayItemExt = daoService.getObjectByUkey(OpenPlayItemExt.class, "mpid", Long.parseLong(mpid), true);
			if(openPlayItemExt == null) continue;
			ChangeEntry changeEntry = new ChangeEntry(openPlayItemExt);
			BindUtils.bindData(openPlayItemExt, dataMap);
			daoService.saveObject(openPlayItemExt);
			monitorService.saveChangeLog(user.getId(), OpenPlayItemExt.class, openPlayItemExt.getMpid(), changeEntry.getChangeMap(openPlayItemExt));
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/refreshOpiSeat.xhtml")
	@ResponseBody
	public String refreshOpiSeat(Long mpid){
		openPlayService.refreshOpenSeatList(mpid);
		return "success:" + mpid;
	}
	
	@RequestMapping("/admin/ticket/checkOpiRoomSeat.xhtml")
	public String changeOpiRoom(Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		String checkQuery1 = "select count(*) from OpenSeat s where s.mpid=? and exists(" +
				"select id from RoomSeat r where r.roomid=? and r.seatline=s.seatline and r.seatrank=s.seatrank and r.lineno=s.lineno and r.rankno=s.rankno)";
		int count = Integer.parseInt(""+hibernateTemplate.find(checkQuery1, mpid, opi.getRoomid()).get(0));
		if(count == opi.getSeatnum()){
			return forwardMessage(model, "数据完全正确，不需要校验, 座位数:" + count);
		}
		List<OpenSeat> oseatList = openPlayService.getOpenSeatList(mpid);
		List<RoomSeat> roomSeatList = openPlayService.getSeatListByRoomId(opi.getRoomid());
		Map<String, OpenSeat> removeSeatMap = groupOpenSeatList(oseatList);
		Map<String, RoomSeat> rseatMap = groupRoomSeatList(roomSeatList);
		Map<String, RoomSeat> addSeatMap = new HashMap<String, RoomSeat>(rseatMap);
		Map<String, OpenSeat> updateMap = new HashMap<String, OpenSeat>();
		//1、删除相同，addSeatMap剩下的就是新增加的座位
		for(String position: removeSeatMap.keySet()){
			addSeatMap.remove(position);
		}
		//2、删除相同，oseatMap剩下的就是要删除的座位, 移除的可能要更新：lineno和rankno可能不同
		for(String position: rseatMap.keySet()){
			OpenSeat oseat = removeSeatMap.remove(position);
			if(oseat!=null) {
				RoomSeat rseat = rseatMap.get(position);
				if(!StringUtils.equals(rseat.getPosition(), oseat.getPosition())) {
					updateMap.put(position, oseat);
				}
			}
		}
		//3、判断卖出的座位现有影厅是否存在
		List<SellSeat> sellList = openPlayService.getSellSeatListByMpid(mpid);
		Map<String, SellSeat> sellMap = groupSellSeatList(sellList);
		List<SellSeat> problemSeatList = new ArrayList<SellSeat>();
		for(String position: sellMap.keySet()){
			if(removeSeatMap.containsKey(position)){
				problemSeatList.add(sellMap.get(position));
			}
		}
		model.put("removeSeatMap", removeSeatMap);
		model.put("rseatMap", rseatMap);
		model.put("addSeatMap", addSeatMap);
		model.put("updateMap", updateMap);
		model.put("problemSeatList", problemSeatList);
		return "admin/ticket/checkOpiRoomSeat.vm";
	}
	@RequestMapping("/admin/ticket/updateOpiSeatList.xhtml")
	public String updateOpiSeatList(Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		String checkQuery1 = "select count(*) from OpenSeat s where s.mpid=? and exists(" +
				"select id from RoomSeat r where r.roomid=? and r.seatline=s.seatline and r.seatrank=s.seatrank and r.lineno=s.lineno and r.rankno=s.rankno)";
		int count = Integer.parseInt(""+hibernateTemplate.find(checkQuery1, mpid, opi.getRoomid()).get(0));
		if(count == opi.getSeatnum()){
			return forwardMessage(model, "数据完全正确，不需要校验, 座位数");
		}
		//1、删除原有座位
		String delete = "delete OpenSeat where mpid=? ";
		hibernateTemplate.bulkUpdate(delete, mpid);
		//2、插入新座位
		opiManageService.batchInsertOpenSeat(opi, null);
		ErrorCode<List<String>> remoteLockList = ticketOperationService.updateRemoteLockSeat(opi, OpiConstant.SECONDS_SHOW_SEAT, false);
		List<String> hfhLockList = new ArrayList<String>();
		if(remoteLockList.isSuccess()){
			hfhLockList = remoteLockList.getRetval();
		}
		opiManageService.updateOpiStats(opi.getId(), hfhLockList, false);
		//3、同步老座位
		String update = "UPDATE WEBDATA.OPEN_SEAT T SET RECORDID=(SELECT RECORDID FROM WEBDATA.SELLSEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.MPID=S.MPID) " +
				"WHERE T.MPID= ? AND EXISTS(SELECT RECORDID FROM WEBDATA.SELLSEAT S WHERE T.SEATLINE=S.SEATLINE AND T.SEATRANK=S.SEATRANK AND T.MPID=S.MPID)";
		int sell = jdbcTemplate.update(update, mpid);
		openPlayService.refreshOpenSeatList(mpid);
		return forwardMessage(model, "数据有差异，校验成功，卖出座位数：" + sell);
	}
	private Map<String, OpenSeat> groupOpenSeatList(List<OpenSeat> oseatList){
		Map<String, OpenSeat> result = new HashMap<String, OpenSeat>();
		for(OpenSeat oseat: oseatList){
			result.put(oseat.getSeatline() + ":" + oseat.getSeatrank(), oseat);
		}
		return result;
	}
	private Map<String, RoomSeat> groupRoomSeatList(List<RoomSeat> oseatList){
		Map<String, RoomSeat> result = new HashMap<String, RoomSeat>();
		for(RoomSeat oseat: oseatList){
			result.put(oseat.getSeatline() + ":" + oseat.getSeatrank(), oseat);
		}
		return result;
	}
	private Map<String, SellSeat> groupSellSeatList(List<SellSeat> sellSeatList){
		Map<String, SellSeat> result = new HashMap<String, SellSeat>();
		for(SellSeat oseat: sellSeatList){
			result.put(oseat.getSeatline() + ":" + oseat.getSeatrank(), oseat);
		}
		return result;
	}
	
	@RequestMapping("/admin/ticket/singleDayMpi.xhtml")
	public String singleDayMpi(ModelMap model){
		List<Map> qryMapList = mongoService.getMapList(MongoData.NS_SINGLEDAY);
		Map<Long, OpenPlayItem> manOpiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long, OpenPlayItem> womenOpiMap = new HashMap<Long, OpenPlayItem>();
		for(Map map : qryMapList){
			Long manmpid = Long.valueOf(map.get("manmpid")+"");
			Long womenmpid = Long.valueOf(map.get("womenmpid")+"");
			manOpiMap.put(manmpid, daoService.getObjectByUkey(OpenPlayItem.class, "mpid", manmpid, false));
			womenOpiMap.put(womenmpid, daoService.getObjectByUkey(OpenPlayItem.class, "mpid", womenmpid, false));
		}
		model.put("qryMapList", qryMapList);
		model.put("manOpiMap", manOpiMap);
		model.put("womenOpiMap", womenOpiMap);
		return "admin/ticket/singleDay.vm";
	}
	@RequestMapping("/admin/ticket/addSingDayMpid.xhtml")
	public String addSingDayMpids(Long manmpid, Long womenmpid, ModelMap model){
		if(manmpid==null || womenmpid==null) return showJsonError(model, "请填写场次id");
		OpenPlayItem mpi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", manmpid, false);
		if(mpi==null) return showJsonError(model, "男生场不存在！");
		mpi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", womenmpid, false);
		if(mpi==null) return showJsonError(model, "女生场不存在！");
		Map map = mongoService.getMap("manmpid", MongoData.NS_SINGLEDAY, manmpid);
		if(map==null) map = new HashMap();
		map.put("manmpid", manmpid);
		map.put("womenmpid", womenmpid);
		mongoService.saveOrUpdateMap(map, "manmpid", MongoData.NS_SINGLEDAY);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/ticket/delSingDayMpid.xhtml")
	public String delSingDayMpid(Long manmpid, ModelMap model){
		mongoService.removeObjectById(MongoData.NS_SINGLEDAY, "manmpid", manmpid);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/disabledOpi.xhtml")
	public String disabledOpi(Integer pageNo, Long cid, String provincecode, ModelMap model){
		Map<GewaCity, List<GewaCity>> proMap = gewaCityService.getAdmCityMap();
		Cinema cinema = null;
		if(pageNo == null) pageNo = 0;
		int rowsPer =50;
		int firstPre = pageNo * rowsPer;
		int counts = 0;
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		List<Long> cinemaidList = new ArrayList<Long>();
		if(StringUtils.isNotBlank(provincecode)){
			for(GewaCity city : proMap.keySet()){
				if(StringUtils.equals(provincecode, city.getProvincecode())){
					List<GewaCity> gewaCityList = proMap.get(city);
					for(GewaCity c : gewaCityList){
						if(cinema != null && !cinema.getCitycode().equals(c.getCitycode())){
							continue;
						}
						int count = openPlayService.getDisabledOpiCount(c.getCitycode(), cid);
						if(count > 0) {
							opiList.addAll(openPlayService.getDisabledOpiLlist(c.getCitycode(), cid, firstPre, rowsPer));
						}
						cinemaidList.addAll(openPlayService.getDisabledCinemaIdList(c.getCitycode()));
						counts = counts + count;
					}
					break;
				}
			}
		}else{
			String citycode = null;
			int count = openPlayService.getDisabledOpiCount(citycode, cid);
			if(count > 0) {
				opiList.addAll(openPlayService.getDisabledOpiLlist(citycode, cid, firstPre, rowsPer));
			}
			cinemaidList.addAll(openPlayService.getDisabledCinemaIdList(citycode));
			counts = counts + count;
		}
		PageUtil pageUtil = new PageUtil(counts, rowsPer, pageNo, "/admin/ticket/disabledOpi.xhtml");
		Map params = new HashMap();
		if(cid == null) params.put("cid", cid);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<Long> movieidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "movieid", true);
		model.put("movieidList", movieidList);
		Map movienameMap = BeanUtil.getKeyValuePairMap(opiList, "movieid", "moviename");
		model.put("movienameMap", movienameMap);
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		List<String> countycodeList = BeanUtil.getBeanPropertyList(cinemaList, String.class, "countycode", true);
		Map<Long, CinemaProfile> cpMap = daoService.getObjectMap(CinemaProfile.class, cinemaidList);
		model.put("cpMap", cpMap);
		Map<String, County> countyMap = daoService.getObjectMap(County.class, countycodeList);
		model.put("countyMap", countyMap);
		Map<String, List<Cinema>> opiCinemaMap = BeanUtil.groupBeanList(cinemaList, "countycode", "other");
		model.put("opiCinemaMap", opiCinemaMap);
		List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
		Map<Long, MoviePlayItem> itemMap = daoService.getObjectMap(MoviePlayItem.class, mpidList);
		model.put("itemMap", itemMap);
		model.put("opiList", opiList);
		Map<Long, Goods> goodMap=new HashMap<Long, Goods>();
		for(OpenPlayItem opi : opiList){
			GoodsGift goodsGift = goodsOrderService.getBindGoodsGift(opi, null);
			if(goodsGift!=null) {
				Goods goods = daoService.getObject(Goods.class, goodsGift.getGoodsid());
				goodMap.put(opi.getId(), goods);
			}
		}
		model.put("goodMap", goodMap);
		model.put("user", getLogonUser());
		model.put("provincecode", provincecode);
		model.put("proMap", proMap);
		return "admin/ticket/disabledOpiList.vm";
	}
	/**
	 * 预加载热门场次座位缓存
	 * @param model
	 * @return
	 */
	@RequestMapping("/admin/ticket/preloadHotspotPmiCache.xhtml")
	public String preloadHotspotPmiCache(ModelMap model){
		this.ticketOperationService.preloadHotspotPmiCache();
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/ticket/refreshMtxPrice.xhtml")
	@ResponseBody
	public String refreshMtxPrice(Long cid){
		//满天星切换
		String query = "from OpenPlayItem where playtime > ? and cinemaid=? and opentype='MTX'";
		List<OpenPlayItem> opiList = daoService.queryByRowsRange(query, 0, 2000, DateUtil.getCurFullTimestamp(), cid);
		int count = opiList.size(), error = 0;
		for(OpenPlayItem opi: opiList){
			try{
				ErrorCode code = ticketOperationService.updateCostPrice(opi.getSeqNo(), opi.getCostprice());
				if(!code.isSuccess()) {
					error ++;
				}
			}catch(Exception e){
				error ++;
			}
		}
		return cid + ", total:" +  count + ", error:" + error;
		
	}
	@RequestMapping("/admin/ticket/countMpiList.xhtml")
	public String disabledOpi(Timestamp starttime, Timestamp endtime, ModelMap model) {
		String vm = "admin/ticket/countMpiList.vm";
		if(starttime==null || endtime==null){
			return vm;
		}
		int days = DateUtil.getDiffDay(endtime, starttime);
		if(days>2){
			return forwardMessage(model, "时间间隔不能大于2天");
		}
		Date sd1 = DateUtil.getBeginningTimeOfDay(starttime);
		String st1 = DateUtil.format(starttime, "HH:mm");
		
		Date sd2 = DateUtil.getBeginningTimeOfDay(endtime);
		String st2 = DateUtil.format(endtime, "HH:mm");
		
		DetachedCriteria query = DetachedCriteria.forClass(MoviePlayItem.class, "d");
		LogicalExpression se1 = Restrictions.and(Restrictions.eq("playdate", sd1), Restrictions.ge("playtime", st1));
		LogicalExpression se2 = Restrictions.and(Restrictions.eq("playdate", sd2), Restrictions.le("playtime", st2));
		query.add(Restrictions.or(se1, se2));
		query.addOrder(Order.asc("citycode"));
		query.addOrder(Order.asc("playdate"));
		query.addOrder(Order.asc("playtime"));
		List<MoviePlayItem> mpiList = hibernateTemplate.findByCriteria(query);
		Map<Long, OpenPlayItem> opiMap = new HashMap<Long, OpenPlayItem>();
		Map<Long, Movie> movieMap = new HashMap<Long, Movie>();
		Map<Long, Cinema> cinemaMap = new HashMap<Long, Cinema>();
		for(MoviePlayItem mpi : mpiList){
			opiMap.put(mpi.getId(), daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId()));
			if(!movieMap.containsKey(mpi.getMovieid())){
				movieMap.put(mpi.getMovieid(), daoService.getObject(Movie.class, mpi.getMovieid()));
			}
			if(!cinemaMap.containsKey(mpi.getCinemaid())){
				cinemaMap.put(mpi.getCinemaid(), daoService.getObject(Cinema.class, mpi.getCinemaid()));
			}
		}
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("movieMap", movieMap);
		model.put("cinemaMap", cinemaMap);
		model.put("opiMap", opiMap);
		model.put("mpiList", mpiList);
		return vm;
	}
}
