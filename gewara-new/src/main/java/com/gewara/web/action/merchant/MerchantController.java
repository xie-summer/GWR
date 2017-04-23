package com.gewara.web.action.merchant;

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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.helper.ticket.SeatPriceHelper;
import com.gewara.helper.ticket.SeatStatusUtil;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.partner.Merchant;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.ticket.OpenSeat;
import com.gewara.model.ticket.SellSeat;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.VelocityTemplate;
import com.gewara.untrans.ticket.TicketOperationService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ReportUtil;

@Controller
public class MerchantController extends BaseMerchantController{
	@Autowired@Qualifier("openPlayService")
	private OpenPlayService openPlayService;
	@Autowired@Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("ticketOperationService")
	private TicketOperationService ticketOperationService;
	@Autowired@Qualifier("velocityTemplate")
	private VelocityTemplate velocityTemplate;
	
	@RequestMapping("/merchant/mpi/mpiStats.xhtml")
	public String mpiStats(ModelMap model){
		Merchant merchant = getLogonMerchant();
		List<Long> cinemaidList = BeanUtil.getIdList(merchant.getRelatelist(), ",");
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		Map<String, List<Cinema>> cinemaMap = BeanUtil.groupBeanList(cinemaList, "citycode");
		Map<Long, Map<String, Integer>> opiCountMap = new HashMap<Long, Map<String, Integer>>();
		for(Long cinemaid: cinemaidList){
			Map<String, Integer> tmp = openPlayService.getOpiCountMap(cinemaid);
			if(!tmp.isEmpty()) opiCountMap.put(cinemaid, tmp);
		}
		model.put("cinemaMap", cinemaMap);
		model.put("opiCountMap", opiCountMap);
		model.put("ReportUtil", new ReportUtil());
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		List<String> dateList = new ArrayList<String>();
		Date cur = new Date();
		for(int i=0;i<7;i++){
			dateList.add(DateUtil.formatDate(DateUtil.addDay(cur, i)));
		}
		model.put("dateList", dateList);
		return "merchant/mpi/mpiStats.vm";
	}
	@RequestMapping("/merchant/mpi/editPlayItem.xhtml")
	public String editPlayItem(@RequestParam("cinemaId")Long cinemaId, Date date, ModelMap model){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return showJsonError(model, "无此影院权限！");

		Map<String, String> dateMap = new HashMap<String, String>();
		List<String> dateList = new ArrayList<String>();
		Date today = DateUtil.getBeginningTimeOfDay(new Date());
		Map<String, Integer> countMap = new HashMap<String, Integer>();
		for(int i=0;i< 12; i++){
			String d = DateUtil.formatDate(DateUtil.addDay(today, i));
			dateList.add(d);
			String dateStr = DateUtil.format(DateUtil.addDay(today, i), "M月d日");
			dateMap.put(d, dateStr + " " + DateUtil.getCnWeek(DateUtil.addDay(today, i)));
			Integer count = mcpService.getCinemaMpiCountByDate(cinemaId, DateUtil.addDay(today, i));
			countMap.put(d, count);
		}
		model.put("dateList", dateList);
		model.put("dateMap", dateMap);
		model.put("countMap", countMap);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinemaId);
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		model.put("roomList", roomList);
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		model.put("cinema", cinema);
		
		if(date == null) date = today;
		List<MoviePlayItem> playitemList = mcpService.getCinemaCurMpiListByDate(cinemaId, date);
		Collections.sort(playitemList, new PropertyComparator("playtime", false, true));
		Set<Movie> movieSet = new HashSet<Movie>();
		Map<String/*movieid+lang+roomid*/, String/*playtime price pricemark*/> mpiMap = new HashMap<String, String>();
		Map<Long/*movieId*/, Set<String/*lang+@+edition*/>> langMap = new HashMap<Long, Set<String>>();
		for(MoviePlayItem mpi: playitemList){
			Movie movie = daoService.getObject(Movie.class, mpi.getMovieid());
			movieSet.add(movie);
			String key = mpi.getMovieid()/*movieId*/ + 
					StringUtils.defaultIfEmpty(mpi.getLanguage(), " ") + "@" + /*language*/
					StringUtils.defaultIfEmpty(mpi.getEdition(), " ") + /*版本*/
					(mpi.getRoomid()==null?"": mpi.getRoomid());/*playroom*/
			String value = mpiMap.get(key);
			if(StringUtils.isBlank(value)) value="";
			value = value + mpi.getPlaytime() + (mpi.getPrice()==null? "": " " + mpi.getPrice()) + 
				(mpi.getPrice()!=null && StringUtils.isNotBlank(mpi.getPricemark()) ? " " + mpi.getPricemark(): "") + "\n";
			mpiMap.put(key, value);
			
			//id=playtime,id=playtime,....
			String mpikey="mpi" + key;
			String mpivalue = mpiMap.get(mpikey);
			if(StringUtils.isBlank(mpivalue)) mpivalue=""+mpi.getId() + "=" + mpi.getPlaytime();
			else mpivalue=mpivalue+","+mpi.getId() + "=" + mpi.getPlaytime();
			mpiMap.put(mpikey, mpivalue);
			Set<String> langList = langMap.get(mpi.getMovieid());
			if(langList == null){
				langList = new HashSet<String>();
				langMap.put(mpi.getMovieid(), langList);
			}
			langList.add(StringUtils.defaultIfEmpty(mpi.getLanguage(), " ") + "@" + StringUtils.defaultIfEmpty(mpi.getEdition(), " "));
		}
		List<Movie> curMovieList = new ArrayList<Movie>(movieSet);
		Collections.sort(curMovieList, new PropertyComparator("moviename", true, true ));
		model.put("curMovieList", curMovieList);
		model.put("langMap", langMap);
		model.put("mpiMap", mpiMap);
		model.put("curDate", DateUtil.formatDate(date));
		model.put("beforeDate", DateUtil.formatDate(DateUtil.addDay(date, -1)));
		List<Movie> movieList = mcpService.getCurMovieListByMpiCount(cinema.getCitycode(), 0, 100);
		movieList.removeAll(curMovieList);
		movieList.addAll(curMovieList);
		model.put("movieList", movieList);
		CinemaProfile cp = daoService.getObject(CinemaProfile.class, cinemaId);
		model.put("modifiable", cp.hasDirect());
		return "merchant/mpi/editPlayItem.vm";
	}
	/**
	 * 解析输入的排片数据
	 * @param playdate
	 * @param cinemaId
	 * @param movieId
	 * @param roomid
	 * @param data
	 * @return
	 */
	@RequestMapping("/merchant/mpi/savePlayItems.xhtml")
	public String savePlayItems(Date playdate, Long cinemaId, Long movieId, Long roomid, 
			String language, String edition, String data, String idList, ModelMap model){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return showJsonError(model, "无此影院权限！");
		return adminSavePlayItems(playdate, cinemaId, movieId, roomid, language, edition, data, idList, model);
	}
	@RequestMapping("/admin/cinema/ajax/savePlayItems.xhtml")
	public String adminSavePlayItems(Date playdate, Long cinemaId, Long movieId, Long roomid, 
			String language, String edition, String data, String idList, ModelMap model){
		if(StringUtils.isBlank(data) && StringUtils.isBlank(idList)) return showJsonSuccess(model);
		language = StringUtils.trimToEmpty(language);
		edition = StringUtils.trimToEmpty(edition);

		CinemaRoom room = null;
		if(roomid != null) room = daoService.getObject(CinemaRoom.class, roomid);
		//1. parse data
		String[] rows = data.split("\n");
		List<MoviePlayItem> umpiList = new ArrayList<MoviePlayItem>(); //update or add
		List<String> rowList = new ArrayList<String>(); //数据
		for(String row:rows){
			if(StringUtils.isNotBlank(row)) rowList.add(row);
		}
		//找到原来的Mpi
		String[] mpiPairList = idList.split(",");
		Map<String, MoviePlayItem> mpiMap = new HashMap<String, MoviePlayItem>();
		for(String mpiPair:mpiPairList){
			String[] p = mpiPair.split("=");
			try{
				MoviePlayItem mpi = daoService.getObject(MoviePlayItem.class, new Long(p[0]));
				if(mpi != null) mpiMap.put(p[1], mpi);
			}catch(Exception e){
			}
		}
		String msg = "";
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		for(int i=0, max=rowList.size();i< max;i++){
			String[] rowdata = rowList.get(i).trim().split(" +");
			MoviePlayItem mpi = mpiMap.remove(rowdata[0]);
			if(mpi == null){//增加
				mpi = new MoviePlayItem(cinemaId, cinema.getCitycode(), movieId, playdate, room, language, edition);
				mpi.setOpentype(OpiConstant.OPEN_GEWARA);
			}else{//修改
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				if(opi != null){
					msg += mpi.getPlaytime() + "的场次开放购票，不能更改！";
					continue;
				}else{
					mpi.setLanguage(language);
					mpi.setEdition(edition);
				}
			}
			mpi.setPlaytime(rowdata[0]);
			if(rowdata.length > 1 && StringUtils.isNumeric(rowdata[1])) mpi.setPrice(new Integer(rowdata[1]));
			else mpi.setPrice(null);
			if(rowdata.length > 2) mpi.setPricemark(rowdata[2]); else mpi.setPricemark(null);
			umpiList.add(mpi);
		}
		//2. update or add
		daoService.saveObjectList(umpiList);
		
		//3. 删除
		if(mpiMap.size() > 0){
			for(String time:new ArrayList<String>(mpiMap.keySet())){
				MoviePlayItem mpi = mpiMap.get(time);
				OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi.getId(), true);
				if(opi!=null){
					msg += mpi.getPlaytime() + "的场次开放购票，不能删除！";
					umpiList.add(mpiMap.get(time));
				} else {
					daoService.removeObject(mpi);
				}
			}
		}
		String newidList = "";
		for(MoviePlayItem mpi:umpiList){
			newidList += "," + mpi.getId()+ "=" + mpi.getPlaytime();
		}
		if(StringUtils.isNotBlank(msg)) msg += "以上场次如需修改，请联系系统管理员！";
		if(newidList.length()>0) newidList = newidList.substring(1);
		Map m = new HashMap();
		m.put("newidList", newidList);
		m.put("msg", msg);
		return showJsonSuccess(model, m);
	}
	
	
	
	@RequestMapping("/merchant/mpi/getRowPage.xhtml")
	public String getRowPage(Long cinemaId, int rownum, ModelMap model2){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return showJsonError(model2, "无此影院权限！");
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		List<Movie> movieList = new LinkedList<Movie>(mcpService.getCurMovieList(cinema.getCitycode()));
		Map model = new HashMap();
		model.put("rownum", rownum);
		model.put("movieList", movieList);
		model.put("cinema", cinema);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		model.put("roomList", roomList);
		String pdetail = velocityTemplate.parseTemplate("cinema/playitem_row.vm", model);
		return showJsonSuccess(model2, pdetail);
	}
	@RequestMapping("/merchant/mpi/copyPlayItem.xhtml")
	public String copyPlayItem(Date from, Date to, Long cinemaId, ModelMap model){
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, cinemaId)) return showJsonError(model, "无此影院权限！");
		Date today = DateUtil.getBeginningTimeOfDay(new Date());
		List<MoviePlayItem> original = mcpService.getCinemaCurMpiListByDate(cinemaId, to);
		daoService.removeObjectList(original);
		List<MoviePlayItem> copy;
		if(from.before(today)){
			return showJsonError(model, "不能复制过期的排片！");
		}else{
			List<MoviePlayItem> playitemList = mcpService.getCinemaCurMpiListByDate(cinemaId, from);
			copy = MoviePlayItem.copy(playitemList, to);
		}
		daoService.saveObjectList(copy);
		return showJsonSuccess(model);
	}
	@RequestMapping("/merchant/mpi/searchOpi.xhtml")
	public String searchOpi(Long cinemaId, Date playdate, ModelMap model){
		Merchant merchant = getLogonMerchant();

		if(!hasRights(merchant, cinemaId)) {
			return showJsonError(model, "无此影院权限！");
		}
		
		List<Date> playdateList = mcpService.getCurCinemaPlayDate(cinemaId);
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		List<String> roomnameList = new ArrayList<String>();
		if(playdateList.size()>0) {
			if(playdate==null) playdate = playdateList.get(0);
			Timestamp timeFrom = new Timestamp(playdate.getTime());
			Timestamp timeTo = DateUtil.getLastTimeOfDay(timeFrom);
			opiList = openPlayService.getOpiList(null, cinemaId, null, timeFrom, timeTo, false);
			roomnameList = BeanUtil.getBeanPropertyList(opiList, String.class, "roomname", true);
			Collections.sort(roomnameList);
			model.put("playdate", playdate);
		}
		model.put("opiList", opiList);
		model.put("playdateList", playdateList);
		model.put("roomnameList", roomnameList);
		return "merchant/mpi/searchOpi.vm";
	}
	@RequestMapping("/merchant/mpi/seatPage.xhtml")
	public String seat(@RequestParam("mpid")Long mpid, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpid, true);
		
		Merchant merchant = getLogonMerchant();
		if(!hasRights(merchant, opi.getCinemaid())) return showJsonError(model, "无此影院权限！");
		
		CinemaRoom room = daoService.getObject(CinemaRoom.class, opi.getRoomid());
		List<OpenSeat> openSeatList = openPlayService.getOpenSeatList(mpid);
		List<SellSeat> sellSeatList = openPlayService.getSellSeatListByMpid(mpid);
		SeatStatusUtil seatStatusUtil = new SeatStatusUtil(sellSeatList);
		model.put("seatStatusUtil", seatStatusUtil);

		ErrorCode<List<String>> lockSeatList = ticketOperationService.updateLockSeatListAsynch(opi);
		if(!lockSeatList.isSuccess()){
			return forwardMessage(model, lockSeatList.getMsg());
		}
		model.put("hfhLockList", lockSeatList.getRetval());

		Map<Integer, String> rowMap = new HashMap<Integer, String>();
		Map<String, OpenSeat> seatMap = new HashMap<String, OpenSeat>();
		Map<Long, Integer> priceMap = new HashMap<Long, Integer>();
		SeatPriceHelper sph = new SeatPriceHelper(opi, PartnerConstant.GEWA_SELF);
		for(OpenSeat seat:openSeatList){
			rowMap.put(seat.getLineno(), seat.getSeatline());
			seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			priceMap.put(seat.getId(), sph.getPrice(seat));
		}
		model.put("seatMap", seatMap);
		model.put("rowMap", rowMap);
		model.put("opi", opi);
		model.put("room", room);
		model.put("priceMap", priceMap);
		return "merchant/mpi/seatPage.vm";
	}
	@RequestMapping("/merchant/cinemaList.xhtml")
	public String cinemaList(ModelMap model){
		Merchant merchant = getLogonMerchant();
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, BeanUtil.getIdList(merchant.getRelatelist(), ","));
		model.put("cinemaList", cinemaList);
		return "merchant/cinemaList.vm";
	}
}
