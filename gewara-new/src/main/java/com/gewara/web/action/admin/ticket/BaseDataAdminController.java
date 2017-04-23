package com.gewara.web.action.admin.ticket;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.Config;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.helper.sys.CachedScript;
import com.gewara.helper.sys.ScriptEngineUtil;
import com.gewara.helper.ticket.AutoSetterHelper;
import com.gewara.model.acl.User;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.RoomSeat;
import com.gewara.model.ticket.AutoSetter;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.mongo.MongoService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.untrans.NosqlService;
import com.gewara.untrans.hbase.ChangeLogService;
import com.gewara.untrans.ticket.MpiOpenService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ObjectId;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class BaseDataAdminController extends BaseAdminController {
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	
	@Autowired
	@Qualifier("changeLogService")
	private ChangeLogService changeLogService;
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;

	@Autowired@Qualifier("mpiOpenService")
	private MpiOpenService mpiOpenService;
	
	@Autowired@Qualifier("nosqlService")
	protected NosqlService nosqlService;
	
	@RequestMapping("/admin/ticket/baseData.xhtml")
	public String baseData(@RequestParam("cid")Long cid, ModelMap model){
		Cinema cinema =  daoService.getObject(Cinema.class, cid);
		model.put("cinema", cinema);
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cid);
		model.put("profile", profile);

		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		model.put("roomList", roomList);
		model.put("partnerTextMap", OpiConstant.partnerTextMap);
		model.put("partnerFlagMap", OpiConstant.partnerFlagMap);
		model.put("takemethodMap", OpiConstant.takemethodMap);
		return "admin/ticket/baseData.vm";
	}
	@RequestMapping("/admin/ticket/saveBaseData.xhtml")
	public String saveBaseData(Long cid, HttpServletRequest request, ModelMap model){
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cid);
		if(profile==null) profile = new CinemaProfile(cid);
		String oldOpentype = profile.getOpentype();
		String oldDirect = profile.getDirect();
		ChangeEntry changeEntry = new ChangeEntry(profile);
		BindUtils.bindData(profile, request.getParameterMap());
		daoService.saveObject(profile);
		User user = getLogonUser();
		model.put("cid", cid);
		model.put("msg", "保存成功！");
		Cinema cinema = daoService.getObject(Cinema.class, cid);
		boolean isSaveCinema = false;
		if(!StringUtils.equals(cinema.getBooking(), profile.getStatus())) {
			cinema.setBooking(profile.getStatus());
			isSaveCinema = true;
		}
		if(!StringUtils.equals(cinema.getPopcorn(), profile.getPopcorn())) {
			cinema.setPopcorn(profile.getPopcorn());
			isSaveCinema = true;
		}
		if(!StringUtils.equals(oldOpentype, profile.getOpentype())) {
			isSaveCinema = true;
		}
		if(!StringUtils.equals(oldDirect, profile.getDirect())) {
			isSaveCinema = true;
		}
		if(isSaveCinema){
			cinema.setUpdatetime(DateUtil.getCurFullTimestamp());
			daoService.saveObject(cinema);
		}
		monitorService.saveChangeLog(user.getId(), CinemaProfile.class,profile.getId(),changeEntry.getChangeMap(profile));
		return "redirect:/admin/ticket/baseData.xhtml";
	}
	@RequestMapping("/admin/ticket/updateRoom.xhtml")
	public String updateRoom(long roomid, String num, int allowsellnum, String roomname, 
			int linenum, int ranknum, int seatnum, String playtype,String defaultEdition, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
		ChangeEntry changeEntry = new ChangeEntry(room);
		room.setRoomname(roomname);
		room.setAllowsellnum(allowsellnum);
		room.setPlaytype(playtype);
		room.setDefaultEdition(defaultEdition);
		//HfhCinema hcinema = daoService.getObjectByUkey(HfhCinema.class, "gcid", room.getCinemaid(), true);
		String msg = "保存成功";
		if(!StringUtils.equals(room.getRoomtype(), OpiConstant.OPEN_GEWARA)){
			msg = OpiConstant.getParnterText(room.getRoomtype()) + "对接影院，只更改了名称及可售座位数！";
		}else{
			room.setNum(num);
			room.setLinenum(linenum);
			room.setRanknum(ranknum);
			room.setSeatnum(seatnum);
			room.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		}
		daoService.saveObject(room);
		monitorService.saveChangeLog(getLogonUser().getId(), CinemaRoom.class, room.getId(), changeEntry.getChangeMap(room));
		return showJsonSuccess(model, msg);
	}
	@RequestMapping("/admin/ticket/updateRoomSeatMapStr.xhtml")
	public String updateRoomSeatMapStr(Long roomid, String updateEmpty, Date from, HttpServletRequest request, ModelMap model) {
		if(roomid!=null){
			CinemaRoom room = daoService.getObject(CinemaRoom.class, roomid);
			String seatmap = openPlayService.getRoomSeatMapStr(room);
			room.setSeatmap(seatmap);
			daoService.saveObject(room);
			return forwardMessage(model, seatmap);
		}
		List<CinemaRoom> roomList = null;
		if(StringUtils.isNotBlank(updateEmpty)){
			String query = "from CinemaRoom where seatnum>0 and seatmap is null and roomtype!='GEWA'";
			roomList = hibernateTemplate.find(query);
		}else if(from !=null){
			String query = "from CinemaRoom c where c.updatetime >? and c.cinemaid in (" +
					"select p.id from Cinema p where p.citycode=? and p.booking=? )";
			roomList = hibernateTemplate.find(query, new Timestamp(from.getTime()), getAdminCitycode(request), Cinema.BOOKING_OPEN);
		}

		if(roomList!=null){
			List<String> msgList = new ArrayList<String>();
			msgList.add("共更新" + roomList.size() + "个影厅");
			for(CinemaRoom room: roomList){
				try{
					String seatmap = openPlayService.getRoomSeatMapStr(room);
					room.setSeatmap(seatmap);
					daoService.saveObject(room);
					msgList.add(room.getId() + "更新");
				}catch(Exception e){
					msgList.add(room.getId() + "错误：" + e.getMessage());
				}
			}
			return forwardMessage(model, msgList);
		}
		return forwardMessage(model, "使用roomid=或from=2008-02-02");
	}
	@RequestMapping("/admin/ticket/seatInitstatus.xhtml")
	public String seatInitstatus(@RequestParam("rid")Long roomId, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		model.put("room", room);
		List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomId);
		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
		for(RoomSeat seat:seatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
		}
		Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
		model.put("cinema", cinema);
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		return "admin/ticket/seatStatus.vm";
	}
	@RequestMapping("/admin/ticket/auto/setterList.xhtml")
	public String setterList(Long cinemaid, String expired, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		model.put("cinema", cinema);
		String query = "from AutoSetter where cinemaid=? ";
		if(StringUtils.isBlank(expired)) query += " and playtime2 >= ? ";
		else query += " and playtime2 < ? ";
		query += "  order by playtime1";
		List<AutoSetter> setterList = hibernateTemplate.find(query, cinemaid, DateUtil.getCurFullTimestamp());
		model.put("setterList", setterList);
		return "admin/ticket/auto/setterList.vm";
	}
	@RequestMapping("/admin/ticket/auto/modifySetter.xhtml")
	public String modifySetter(Long sid, Long cinemaid, ModelMap model){
		if(sid!=null){
			AutoSetter setter = daoService.getObject(AutoSetter.class, sid);
			model.put("setter", setter);
			cinemaid = setter.getCinemaid();
			String setterMovies = StringUtils.replace(StringUtils.replace(setter.getMovies(),"\n", ""),"\r","");
			String[] movieIdList = StringUtils.split(setterMovies,',');
			List<Movie> movies = new ArrayList<Movie>();
			if(movieIdList != null){
				for(String movieId : movieIdList){
					if(ValidateUtil.isNumber(movieId)){
						Movie movie = daoService.getObject(Movie.class, Long.valueOf(movieId));
						movies.add(movie);
					}
				}
			}
			model.put("setterMovies", setterMovies);
			model.put("movies", movies);
		}
		model.put("cinemaid", cinemaid);
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, cinemaid);
		model.put("cinema", cinema);
		if(profile != null){
			model.put("isLowest", OpiConstant.OPEN_LOWEST_IS_COST.contains(profile.getOpentype()) || 
					StringUtils.equals(profile.getOpentype(), OpiConstant.OPEN_JY));
		}
		return "admin/ticket/auto/modifySetter.vm";
	}
	@RequestMapping("/admin/ticket/auto/checkCollision.xhtml")
	public String checkCollision(Long cinemaid, ModelMap model){
		List<AutoSetter> setterList = openPlayService.getValidSetterList(cinemaid,"ALL");
		for(int i=0, len = setterList.size(); i < len -1; i++){
			for(int j=i+1; j< len; j++){
				boolean result = AutoSetterHelper.isCollision(setterList.get(i), setterList.get(j));
				if(result) return forwardMessage(model, "冲突：" + setterList.get(i).gainFullDesc() + "<--------->" + setterList.get(j).gainFullDesc());
			}
		}
		return forwardMessage(model, "无冲突！");
	}
	private boolean checkScriptEngine(Long cinemaid, String script){
		String sql = "from MoviePlayItem where playdate=? and playtime< ? and cinemaid=? order by playdate, playtime";
		List<MoviePlayItem> mpiList = daoService.queryByRowsRange(sql, 0, 1, DateUtil.getCurDate(), DateUtil.formatTime(new Date()), cinemaid);
		if(mpiList.size()>0){
			Map<String, Object> context = new HashMap<String, Object>();
			context.put("mpi", mpiList.get(0));
			CachedScript cs = ScriptEngineUtil.buildCachedScript(script, false);
			return !cs.run(context).hasError();
		}
		return true;
	}
	
	@RequestMapping("/admin/ticket/auto/saveSetter.xhtml")
	public String saveSetter(HttpServletRequest request, Long sid, Long cinemaid, ModelMap model){
		AutoSetter setter = null;
		if(sid!=null){
			setter = daoService.getObject(AutoSetter.class, sid);
			cinemaid = setter.getCinemaid();
		}else{
			setter = new AutoSetter(cinemaid);
		}
		ChangeEntry changeEntry = new ChangeEntry(setter);
		setter.setCinemaid(cinemaid);
		BindUtils.bindData(setter, request.getParameterMap());
		if(StringUtils.isBlank(setter.getMovies())){
			setter.setOrdernum(0);
		}else{
			setter.setOrdernum(200);
		}
		if(StringUtils.isNotBlank(setter.getLimitScript())){
			setter.setLimitScript(StringUtils.replace(setter.getLimitScript(), "\r\n", ""));
			if(!checkScriptEngine(cinemaid, setter.getLimitScript())){
				 return showJsonError(model, "限制函数设置的有问题！");
			}
		}
		if(StringUtils.isNotBlank(setter.getPriceScript())){
			setter.setPriceScript(StringUtils.replace(setter.getPriceScript(), "\r\n", ""));
			if(!checkScriptEngine(cinemaid, setter.getPriceScript())){
				 return showJsonError(model, "设置函数填写的有问题！");
			}
		}
		//验证星期类型
		if(!ValidateUtil.isNumber(setter.getWeektype()) || setter.getWeektype().length() > 7) return showJsonError(model, "星期格式不正确(1234567)！");
		Set<String> weekSet = new TreeSet<String>();
		for(char i : setter.getWeektype().toCharArray()){
			weekSet.add(i+"");
		}
		setter.setWeektype(weekSet.toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", ""));
		
		//验证场次时段
		String[] timescopeList = StringUtils.split(setter.getTimescope(), ',');
		Set<String> timescopeSet = new TreeSet<String>();
		for(String timescope : timescopeList){
			if(timescope.length() != 9) return showJsonError(model, "场次时段格式不正确！");
			for(String temp : StringUtils.split(timescope, '~')){
				if(!ValidateUtil.isNumber(temp) || temp.length() != 4) return showJsonError(model, "场次时段格式不正确！");
				if(Integer.valueOf(temp) > 2400) return showJsonError(model, "场次时段时间过大(0000~2400)！");
			}
			timescopeSet.add(timescope);
		}
		setter.setTimescope(timescopeSet.toString().replace("[", "").replace("]", "").replace(" ", ""));
		
		//验证影厅序号
		String[] roomnumList = StringUtils.split(setter.getRoomnum(), ',');
		Set<String> roomnumSet = new TreeSet<String>();
		for(String roomnum : roomnumList){
			if(!ValidateUtil.isNumber(roomnum)) return showJsonError(model, "影厅序号不正确！");
			int i = Integer.valueOf(roomnum);
			if(i < 0 || i > 20) return showJsonError(model, "影厅序号超过范围(1-20)！");
			roomnumSet.add(roomnum);
		}
		setter.setRoomnum(roomnumSet.toString().replace("[", "").replace("]", "").replace(" ", ""));
		
		//验证版本
		String[] editionList = StringUtils.split(setter.getEdition(), ',');
		Set<String> editionSet = new TreeSet<String>();
		for(String edition : editionList){
			if(!OpiConstant.isValidEdition(edition)) return showJsonError(model, "版本错误！");
			editionSet.add(edition);
		}
		setter.setEdition(editionSet.toString().replace("[", "").replace("]", "").replace(" ", ""));
		//效验电影ID
		if(setter.getMovies() != null){
			String[] moviesList = StringUtils.split(setter.getMovies(), ',');
			Set<String> moviesSet = new TreeSet<String>();
			for(String movieid : moviesList){
				if(!ValidateUtil.isNumber(movieid)) return showJsonError(model, "电影ID格式不正确！");
				moviesSet.add(movieid);
			}
			setter.setMovies(moviesSet.toString().replace("[", "").replace("]", "").replace(" ", ""));
		}
		daoService.saveObject(setter);
		User user = getLogonUser();
		monitorService.saveChangeLog(user.getId(),AutoSetter.class,setter.getId(),changeEntry.getChangeMap(setter));
		return showJsonSuccess(model, ""+setter.getId());
	}
	
	@RequestMapping("/admin/ticket/auto/copySetter.xhtml")
	public String copySetter(Long sid,ModelMap model){
		if(sid == null){
			return showJsonError(model, "未选中要复制的设置器！");
		}
		AutoSetter setter = daoService.getObject(AutoSetter.class, sid);
		AutoSetter newSetter = new AutoSetter(setter.getCinemaid());
		BeanUtils.copyProperties(setter, newSetter);
		newSetter.setId(null);
		newSetter.setStatus(AutoSetter.STATUS_CLOSE);
		newSetter.setCheckStatus(AutoSetter.CHECK_F);
		daoService.addObject(newSetter);
		return showJsonSuccess(model, ""+setter.getId());
	}
	@RequestMapping("/admin/ticket/auto/mpiList.xhtml")
	public String mpiList(Long cid,ModelMap model){
		List<AutoSetter> setterList = openPlayService.getCheckSetterList();
		Map<Long,Cinema> cinemaMap = new HashMap<Long,Cinema>();
		Map<Long,Map<String, String>> changeLogs = new HashMap<Long,Map<String, String>>();
		for(AutoSetter setter : setterList){
			cinemaMap.put(setter.getId(),daoService.getObject(Cinema.class, setter.getCinemaid()));
			Collection<Map<String, String>> c = changeLogService.getChangeLogList(Config.SYSTEMID,"AutoSetter", setter.getId()).values();
			if(!c.isEmpty()){
				changeLogs.put(setter.getId(),(Map<String, String>)c.toArray()[c.size() - 1]);
			}
		}
		model.put("setterList", setterList);
		model.put("cinemaMap", cinemaMap);
		model.put("changeLogs", changeLogs);
		model.put("cinema", daoService.getObject(Cinema.class,cid));
		return "admin/ticket/auto/checkSetterList.vm";
	}
	
	@RequestMapping("/admin/ticket/auto/saveAutoSetterLimit.xhtml")
	public String saveAutoSetterLimit(String name,String value,ModelMap model){
		Map params = new HashMap();
		params.put("name",name);
		Map sMap = mongoService.findOne(MongoData.NS_AUTO_SETTER_LIMIT, params);
		if(sMap == null){
			sMap = new HashMap();
			sMap.put("addTime", DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			sMap.put("name",name);
			sMap.put("value",value);
			sMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
		}else{
			sMap.put("value",value);
		}
		mongoService.saveOrUpdateMap(sMap, MongoData.SYSTEM_ID, MongoData.NS_AUTO_SETTER_LIMIT);
		return showJsonSuccess(model, "");
	}
	
	@RequestMapping("/admin/ticket/auto/checkSetterStatus.xhtml")
	public String checkAutoSetter(Long sid,String status,ModelMap model){
		User user = getLogonUser();
		AutoSetter setter = daoService.getObject(AutoSetter.class, sid);
		ChangeEntry changeEntry = new ChangeEntry(setter);
		setter.setCheckStatus(status);
		setter.setCheckUser(user.getId());
		daoService.updateObject(setter);
		CachedScript limitCs = null;
		if(StringUtils.isNotBlank(setter.getLimitScript())){
			limitCs = ScriptEngineUtil.buildCachedScript(setter.getLimitScript(), true);
		}
		if(AutoSetter.CHECK_T.equals(setter.getCheckStatus()) && AutoSetter.STATUS_OPEN_A.equals(setter.getStatus())){
			String qry = "select p.id from MoviePlayItem p where playdate >= ? and cinemaid= ?";
			List<Long> idList = hibernateTemplate.find(qry,DateUtil.getCurDate(), setter.getCinemaid());
			List<MoviePlayItem> playitemList = daoService.getObjectList(MoviePlayItem.class, idList); 
			List<OpenPlayItem> opiList = openPlayService.getOpiList(null, setter.getCinemaid(),null, DateUtil.getCurTruncTimestamp(), null, false);
			List<Long> mpidList = BeanUtil.getBeanPropertyList(opiList, Long.class, "mpid", true);
			Map<String,String> limit = nosqlService.getAutoSetterLimit();
			List<MoviePlayItem> validList = new ArrayList<MoviePlayItem>();
			for(MoviePlayItem mpi: playitemList) {
				if(!mpidList.contains(mpi.getId()) && AutoSetterHelper.isMatch(setter, mpi,limit,limitCs)) {
					validList.add(mpi);
				}
			}
			mpiOpenService.asynchAutoOpenMpiList(validList, setter);
		}
		monitorService.saveChangeLog(user.getId(),AutoSetter.class,setter.getId(),changeEntry.getChangeMap(setter));
		return showJsonSuccess(model, ""+setter.getId());
	}
	
	@RequestMapping("/admin/ticket/auto/checkMovieId.xhtml")
	public String checkMovieId(Long sid,String selectType,Long cinemaId, ModelMap model){
		Map movieMap = new HashMap();
		if(sid != null){
			AutoSetter setter = daoService.getObject(AutoSetter.class, sid);
			String[] movieIdList = StringUtils.split(StringUtils.replace(StringUtils.replace(setter.getMovies(),"\n", ""),"\r",""),',');
			if(StringUtils.equals("select", selectType)){
				if(movieIdList != null){
					for(String movieId : movieIdList){
						if(ValidateUtil.isNumber(movieId)){
							Movie movie = daoService.getObject(Movie.class, Long.valueOf(movieId));
							movieMap.put(movieId, movie);
						}
					}
				}
			}else{
				List<Movie> movieList = mcpService.getCurMovieListByCinemaId(cinemaId);
				List<Movie> futureMovieList = mcpService.getFutureMovieList(0, 200, null);
				for(Movie movie : futureMovieList){
					if(!movieList.contains(movie)){
						movieList.add(movie);
					}
				}
				movieMap = BeanUtil.beanListToMap(movieList, "id");
				Set keys = movieMap.keySet();
				if(movieIdList != null){
					for(String movieId : movieIdList){
						if(keys.contains(Long.valueOf(movieId))){
							movieMap.remove(Long.valueOf(movieId));
						}
					}
				}
			}
		}else if(StringUtils.equals(selectType, "unSelect")){
			List<Movie> movieList = mcpService.getCurMovieListByCinemaId(cinemaId);
			List<Movie> futureMovieList = mcpService.getFutureMovieList(0, 200, null);
			for(Movie movie : futureMovieList){
				if(!movieList.contains(movie)){
					movieList.add(movie);
				}
			}
			movieMap = BeanUtil.beanListToMap(movieList, "id");
		}
		model.put("movieMap", movieMap);
		return "admin/ticket/auto/checkMovie.vm";
	}

	@RequestMapping("/admin/ticket/auto/baseSetterList.xhtml")
	public String baseSetterList(Long cinemaId,ModelMap model,Boolean isBefore,HttpServletRequest request){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		model.put("cinema", cinema);
		String query = "from AutoSetter where checkStatus = 'T' and status = 'open_a' and cinemaid=? ";
		if(isBefore != null && isBefore){
			query += " and playtime2 < ? ";
		}else{
			query += " and playtime2 >= ? ";
		}
		query += "  order by playtime1";
		List<AutoSetter> setterList = hibernateTemplate.find(query, cinemaId, DateUtil.getCurFullTimestamp());
		String cQuery = "select cinemaid from AutoSetter where checkStatus = 'T' and status = 'open_a'";
		if(isBefore != null && isBefore){
			cQuery += " and playtime2 < ? ";
		}else{
			cQuery += " and playtime2 >= ? ";
		}
		cQuery += " group by cinemaid";
		List<Long> cidList = hibernateTemplate.find(cQuery, DateUtil.getCurFullTimestamp());
		Map<String,List<Cinema>> cinemas = new HashMap<String,List<Cinema>>();
		for(Long cid : cidList){
			Cinema c = this.daoService.getObject(Cinema.class, cid);
			if(cinemas.get(c.getCountycode()) != null){
				cinemas.get(c.getCountycode()).add(c);
			}else{
				List<Cinema> cList = new ArrayList<Cinema>();
				cList.add(c);
				cinemas.put(c.getCountycode(), cList);
			}
		}
		String citycode = getAdminCitycode(request);
		Map<String, String> countyMap = placeService.getCountyPairByCityCode(citycode);
		countyMap.put("其他", "其他");
		model.put("countyMap", countyMap);
		model.put("cinemas", cinemas);
		model.put("setterList", setterList);
		model.putAll(nosqlService.getAutoSetterLimit());
		return "admin/ticket/auto/baseSetterList.vm";
	}
}
