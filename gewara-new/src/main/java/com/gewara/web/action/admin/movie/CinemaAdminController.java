package com.gewara.web.action.admin.movie;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.CharacteristicType;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OpiConstant;
import com.gewara.json.CinemaIncrementalReport;
import com.gewara.json.CinemaProNotify;
import com.gewara.json.RoomOuterRingSeat;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.common.County;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaRoom;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MultiPlay;
import com.gewara.model.movie.RoomSeat;
import com.gewara.mongo.MongoService;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.content.PictureService;
import com.gewara.service.movie.MCPService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.untrans.NosqlService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.RelatedHelper;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Controller
public class CinemaAdminController extends BaseAdminController {
	@Autowired@Qualifier("config")
	private Config config;
	
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
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	@Autowired@Qualifier("nosqlService")
	private NosqlService nosqlService;
	
	@Autowired@Qualifier("markService")
	private MarkService markService;
	
	@Autowired@Qualifier("pictureService")
	private PictureService pictureService;

	//2. cinema-----------------------------------------------------------------------------
	@RequestMapping("/admin/cinema/cinemaList.xhtml")
	public String cinemaList(String key, HttpServletRequest request, ModelMap model) throws Exception {
		DetachedCriteria query = DetachedCriteria.forClass(Cinema.class);
		String citycode = getAdminCitycode(request) ;
		query.add(Restrictions.eq("citycode", citycode));
		if(StringUtils.isNotBlank(key)){
			query.add(Restrictions.or(Restrictions.ilike("name", key, MatchMode.ANYWHERE)
					, Restrictions.ilike("pinyin", key, MatchMode.ANYWHERE)));
		}
		query.addOrder(Order.desc("hotvalue"));
		query.addOrder(Order.asc("name"));
		List cinemaList = hibernateTemplate.findByCriteria(query);
		model.put("cinemaList", cinemaList);
		return "admin/cinema/cinemaList.vm";
	}

	@RequestMapping("/admin/cinema/ajax/saveCinemaDiscount.xhtml")
	public String adminSaveCinemaDiscount(Long cinemaid, String discount,String type, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaid);
		ChangeEntry changeEntry = new ChangeEntry(cinema);
		//验证内容
		if(StringUtils.isNotBlank(type)){
			String msg=ValidateUtil.validateNewsContent(null, discount);
			if(StringUtils.isNotBlank(msg))return showJsonError(model, msg);
		}
		cinema.setDiscount(discount);
		daoService.saveObject(cinema);
		User user = getLogonUser();
		monitorService.saveChangeLog(user.getId(), Cinema.class, cinema.getId(),changeEntry.getChangeMap( cinema));
		return showJsonSuccess(model);
	}	
	
	@RequestMapping("/admin/cinema/modifyCinemaDetail.xhtml")
	public String modifyCinema(Long cinemaId,String status,HttpServletRequest request, ModelMap model) {
		if(StringUtils.isBlank(status)){
			status = "baseinfo";
		}
		model.put("status", status);
		Cinema cinema = new Cinema();
		String citycode = null;
		if (cinemaId != null){
			cinema = daoService.getObject(Cinema.class, cinemaId);
			citycode = cinema.getCitycode();
		}else{
			citycode = getAdminCitycode(request) ;
		}
		model.put("cinema", cinema);
		model.put("citycode", citycode);
		if(StringUtils.equals(status, "baseinfo")){
			List countyList = placeService.getCountyByCityCode(citycode);
			model.put("countyList", countyList);
			if(StringUtils.isNotBlank(cinema.getCountycode())){
				model.put("indexareaList", placeService.getIndexareaByCountyCode(cinema.getCountycode()));
			}
			List<Map> telephones = JsonUtils.readJsonToObjectList(Map.class, cinema.getContactTelephone());
			model.put("telephones", telephones);
			return "admin/cinema/baseCinemaForm.vm";
		}else if(StringUtils.equals(status, "showAround")){
			List<Subwayline> lineList = hibernateTemplate.find("from Subwayline s where s.citycode=? order by s.id", citycode);
			Map<Long,String> stationMap = new HashMap<Long,String>();
			for (Subwayline line : lineList) {
				 List<Subwaystation> tmpList = placeService.getSubwaystationsByLineId(line.getId());
				 stationMap.put(line.getId(),StringUtils.join(BeanUtil.getBeanPropertyList(tmpList, "id", true),","));
			}
			model.put("stationMap", stationMap);
			model.put("lineList", lineList);
			List<Subwaystation> stationList =  placeService.getSubwaystationsByCityCode(citycode);
			model.put("stationList", stationList);
			Map subwayTransportMap = JsonUtils.readJsonToMap(cinema.getSubwayTransport());
			model.put("subwayTransportMap", subwayTransportMap);
			return "admin/cinema/showAroundCinemaForm.vm";
		}
		return "admin/cinema/baseCinemaForm.vm";
		//return "admin/cinema/cinemaForm.vm";
	}
	/**
	 * 后台导出Excle影院信息
	 * @param cinemaId
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@RequestMapping("/admin/cinema/export.xhtml")
	public ModelAndView export(@RequestParam("cinemaId")Long cinemaId, HttpServletResponse response) throws Exception {
		String[] str = {"日期","厅号","片名","播放时间","播放时间","播放时间","播放时间","播放时间","播放时间","播放时间"};
		List<MoviePlayItem> mpiList = mcpService.getCinemaCurMpiList(cinemaId);
		//**********************
		//创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		//创建表
		HSSFSheet sheet = wb.createSheet("Movie Data");
		//设置表格的宽度
		sheet.setDefaultColumnWidth(15);
		//创建表头
		HSSFHeader header = sheet.getHeader();
		//设置标题
		header.setCenter("电影排版数据表");
		HSSFRow row = sheet.createRow(0);
		//逐一设置Title的值
		for(int i = 0;i < str.length;i++)
		{
			 HSSFCell headerCell = row.createCell(i);  
			 headerCell.setCellValue(new HSSFRichTextString(str[i]));  
		}
		Map</*date + room + movie*/String, List<MoviePlayItem>> mpiMap = new TreeMap<String, List<MoviePlayItem>>();
		for(MoviePlayItem mpi:mpiList){
			Movie movie = daoService.getObject(Movie.class, mpi.getMovieid());
			String key = DateUtil.formatDate(mpi.getPlaydate()) + "@" + mpi.getPlayroom() + "@" + movie.getName();
			List<MoviePlayItem> tmpList = mpiMap.get(key);
			if(tmpList==null){
				tmpList = new ArrayList<MoviePlayItem>();
				mpiMap.put(key, tmpList);
			}
			tmpList.add(mpi);
		}
		int rownum = 1;
		for(String key: mpiMap.keySet()){
			row = sheet.createRow(rownum); rownum++;
			String[] keyList = key.split("@");
			HSSFCell cell = row.createCell(0);
			cell.setCellValue(new HSSFRichTextString(keyList[0]));
			cell = row.createCell(1);
			if(!"null".equals(keyList[1])){
				cell.setCellValue(new HSSFRichTextString(keyList[1]));
			}else{
				cell.setCellValue(new HSSFRichTextString(" "));
			}
			cell = row.createCell(2);
			cell.setCellValue(new HSSFRichTextString(keyList[2]));
			int i=3;
			for(MoviePlayItem mpi:mpiMap.get(key)){
 			  cell = row.createCell(i);
 			  cell.setCellValue(new HSSFRichTextString(mpi.getPlaytime()));
 			  i++;
			}
		}
		wb.write(response.getOutputStream());
		
		return null;
	}
	@RequestMapping("/admin/cinema/roomseat.xhtml")
	public String roomseat(@RequestParam("rid")Long roomId, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		model.put("room", room);
		int seatno = room.getLinenum()*room.getRanknum();
		if(seatno==0 || room.getRanknum()>100 || room.getLinenum() >100){
			return "admin/cinema/seatnum.vm";
		}else{
			int maxlinenum = room.getLinenum();
			int maxranknum = room.getRanknum();
			List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomId);
			Map<Integer, String> rowMap = new HashMap<Integer, String>();
			Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
			for(RoomSeat seat:seatList){
				maxlinenum = Math.max(maxlinenum, seat.getLineno());
				maxranknum = Math.max(maxranknum, seat.getRankno());
				rowMap.put(seat.getLineno(), seat.getSeatline());
				seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			}
			Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
			model.put("cinema", cinema);
			model.put("seatMap", seatMap);
			model.put("rowMap", rowMap);
			model.put("maxlinenum", maxlinenum);
			model.put("maxranknum", maxranknum);
			return "admin/cinema/roomseat.vm";
		}
	}
	//安排走廊，墙体位置
	@RequestMapping("/admin/cinema/outerRingSeat.xhtml")
	public String outerRingSeat(@RequestParam("rid")Long roomId, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		model.put("room", room);
		int seatno = room.getLinenum()*room.getRanknum();
		if(seatno==0 || room.getRanknum()>100 || room.getLinenum() >100){
			return "admin/cinema/seatnum.vm";
		}else{
			int maxlinenum = room.getLinenum();
			int maxranknum = room.getRanknum();
			List<RoomSeat> seatList = openPlayService.getSeatListByRoomId(roomId);
			Map<Integer, String> rowMap = new HashMap<Integer, String>();
			Map<String, RoomSeat> seatMap = new HashMap<String, RoomSeat>();
			for(RoomSeat seat:seatList){
				maxlinenum = Math.max(maxlinenum, seat.getLineno());
				maxranknum = Math.max(maxranknum, seat.getRankno());
				rowMap.put(seat.getLineno(), seat.getSeatline());
				seatMap.put("row" + seat.getLineno() + "rank" + seat.getRankno(), seat);
			}
			Cinema cinema = daoService.getObject(Cinema.class, room.getCinemaid());
			model.put("cinema", cinema);
			model.put("seatMap", seatMap);
			model.put("rowMap", rowMap);
			model.put("maxlinenum", maxlinenum);
			model.put("maxranknum", maxranknum);
			model.put("outerRingseatMap", nosqlService.getOuterRingSeatByRoomId(roomId));
			return "admin/cinema/outerRingSeat.vm";
		}
	}
	
	@RequestMapping("/admin/cinema/addOuterRingSeat.xhtml")
	public String addOuterRingSeat(long roomId,String seatStyle,String seatMark,ModelMap model){
		Map params = new HashMap();
		params.put("roomId", roomId);
		CinemaRoom room = daoService.getObject(CinemaRoom.class,roomId);
		if(room == null){
			this.showJsonError(model,"影厅已经删除");
		}
		RoomOuterRingSeat rrs = null;
		List<RoomOuterRingSeat> rrsList = mongoService.getObjectList(RoomOuterRingSeat.class, params, "addTime", true,0, 1);
		if(rrsList != null && rrsList.size() > 0){
			rrs = rrsList.get(0);
		}
		if(rrs == null){
			rrs = new RoomOuterRingSeat();
			rrs.set_id(ObjectId.uuid());
			rrs.setAddTime(DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
			rrs.setRoomId(roomId);
		}
		if(StringUtils.isBlank(rrs.getOuterRingSeat())){
			if(StringUtils.isNotBlank(seatStyle)){
				List<String> tmpList = new ArrayList<String>();
				String[] seatMarks = StringUtils.split(seatMark, ",");
				for(String s : seatMarks){
					tmpList.add(s + ":" + seatStyle);
				}
				rrs.setOuterRingSeat(StringUtils.join(tmpList, ","));
			}
			mongoService.saveOrUpdateObject(rrs, MongoData.SYSTEM_ID);
		}else{
			String[] seats = StringUtils.split(rrs.getOuterRingSeat(),",");
			List<String> seatList =  new ArrayList<String>();
			Map<String,String> seatMap = new HashMap<String,String>();
			String[] seatMarks = StringUtils.split(seatMark, ",");
			for(String seat : seats){
				String[] t = StringUtils.split(seat,":");
				seatList.add(t[0]);
				seatMap.put(t[0], seat);
			}
			for(String s : seatMarks){
				if(seatList.contains(s)){
					if(StringUtils.isBlank(seatStyle)){
						seatMap.remove(s);
					}else{
						seatMap.put(s, s + ":" + seatStyle);
					}
				}else{
					if(StringUtils.isNotBlank(seatStyle)){
						seatMap.put(s, s + ":" + seatStyle);
					}
				}
			}
			rrs.setOuterRingSeat(StringUtils.join(seatMap.values(), ","));
			mongoService.saveOrUpdateObject(rrs, MongoData.SYSTEM_ID);
		}
		Map map = JsonUtils.readJsonToMap(room.getOtherinfo());
		if(StringUtils.isNotBlank(rrs.getOuterRingSeat())){
			map.put("outerRingseat", "true");
		}else{
			map.put("outerRingseat", "false");
		}
		room.setOtherinfo(JsonUtils.writeMapToJson(map));
		daoService.updateObject(room);
		return this.showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/cinema/setRoomMsg.xhtml")
	public String setRoomMsg(@RequestParam("rid")Long roomId,ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		model.put("room", room);
		model.put("otherinfoMap", JsonUtils.readJsonToMap(room.getOtherinfo()));
		return "admin/cinema/roomMsgAndTopic.vm";
	}
	
	@RequestMapping("/admin/cinema/saveRoomMsg.xhtml")
	public String saveRoomMsg(Long roomId,String message1,String message2,String topicId, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		if(room == null){
			this.showJsonError(model,"影厅已经删除");
		}
		Map map = JsonUtils.readJsonToMap(room.getOtherinfo());
		if(StringUtils.isBlank(message1)){
			map.remove("message1");
		}else{
			map.put("message1", message1);
		}
		if(StringUtils.isBlank(message2)){
			map.remove("message2");
		}else{
			map.put("message2", message2);
		}
		if(StringUtils.isBlank(topicId)){
			map.remove("topicId");
		}else{
			map.put("topicId",Long.parseLong(topicId) + "");
		}
		room.setOtherinfo(JsonUtils.writeMapToJson(map));
		daoService.updateObject(room);
		model.put("room", room);
		model.put("otherinfoMap",map);
		return "admin/cinema/roomMsgAndTopic.vm";
	}
	
	@RequestMapping("/admin/cinema/addRoomDoor.xhtml")
	public String addRoomDoor(long roomId,String[] roomDoor,ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		if(roomDoor == null){
			room.setRoomDoor(null);
		}else{
			room.setRoomDoor(StringUtils.join(roomDoor, ","));
		}
		this.daoService.updateObject(room);
		return this.showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/cinema/arrangeRoom.xhtml")
	public String arrangeRoom(@RequestParam("rid")Long roomId, int linenum, int ranknum, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, roomId);
		int seatno = linenum * ranknum;
		if(seatno==0 || seatno >500){
			model.put("room", room);
			return "admin/cinema/seatnum.vm";
		}else{
			room.setLinenum(linenum);
			room.setRanknum(ranknum);
			daoService.saveObject(room);
			List<RoomSeat> seatList = new ArrayList<RoomSeat>();
			for(int row=1; row<=linenum; row++){
				for(int rank=1; rank<=ranknum; rank++){
					RoomSeat seat = new RoomSeat(roomId, row, rank);
					seatList.add(seat);
				}
			}
			daoService.saveObjectList(seatList);
			model.put("rid", roomId);
			return "redirect:/admin/cinema/roomseat.xhtml";
		}
	}
	
	@RequestMapping("/admin/cinema/roomList.xhtml")
	public String roomList(@RequestParam("cinemaId")Long cinemaId, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinemaId);
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		model.put("roomList", roomList);
		model.put("cinema", cinema);
		return "admin/cinema/roomList.vm";
	}
	@RequestMapping("/admin/cinema/characteristicTypeImgList.xhtml")
	public String characteristicTypeImgList( ModelMap model){
		model.put("typeList",CharacteristicType.characteristicTypeList);
		model.put("typeMap",CharacteristicType.characteristicNameMap);
		return "admin/cinema/characteristicImgList.vm";
	}
	@RequestMapping("/admin/cinema/cityCharacteristicTypeList.xhtml")
	public String cityCharacteristicTypeList( ModelMap model){
		model.put("typeList",CharacteristicType.characteristicTypeList);
		model.put("cityMap",AdminCityContant.citycode2CitynameMap);
		List<Map> map =mongoService.find(MongoData.NS_CITY_ROOM_CHARACTERISTIC, new HashMap<String,Object>());
		Map<String,Map> cityCharacteristic = BeanUtil.beanListToMap(map, MongoData.SYSTEM_ID);
		model.put("cityCharacteristic", cityCharacteristic);
		model.put("typeMap",CharacteristicType.characteristicNameMap);
		return "admin/cinema/characteristicTypeList.vm";
	}
	@RequestMapping("/admin/cinema/saveCityCharacteristicType.xhtml")
	public String saveCityCharacteristicType(String citycode,String[] type, ModelMap model){
		Map map = mongoService.findOne(MongoData.NS_CITY_ROOM_CHARACTERISTIC, MongoData.SYSTEM_ID, citycode);
		if(map == null){
			map = new HashMap();
			map.put(MongoData.SYSTEM_ID, citycode);
		}
		String cType = null;
		if(type != null){
			cType = StringUtils.join(type, ",");
		}
		map.put("characteristic",cType);
		mongoService.saveOrUpdateMap(map, MongoData.SYSTEM_ID, MongoData.NS_CITY_ROOM_CHARACTERISTIC);
		return this.showRedirect("admin/cinema/cityCharacteristicTypeList.xhtml", model);
	}
	
	@RequestMapping("/admin/cinema/discount.xhtml")
	public String discount(@RequestParam("cinemaId")Long cinemaId, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		model.put("cinema", cinema);
		return "admin/cinema/discount.vm";
	}
	@RequestMapping("/admin/cinema/editPlayItem.xhtml")
	public String editPlayItem(@RequestParam("cinemaId")Long cinemaId, Date date, ModelMap model){
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
		model.put("modifiable", true);
		return "admin/cinema/editPlayItem.vm";
	}
	@RequestMapping("/admin/cinema/multiPlayList.xhtml")
	public String multiPlayList(@RequestParam("cinemaId")Long cinemaId, ModelMap model){
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		List<MultiPlay> multiPlayList = mcpService.getCurMultyPlayList(cinema.getId());
		Collections.sort(multiPlayList, new PropertyComparator("playdate", false, false));
		model.put("multiPlayList", multiPlayList);
		model.put("cinema", cinema);
		List<CinemaRoom> roomList = daoService.getObjectListByField(CinemaRoom.class, "cinemaid", cinema.getId());
		Collections.sort(roomList, new PropertyComparator("num", false, true));
		model.put("roomList", roomList);

		List<Movie> movieList = new LinkedList<Movie>(mcpService.getCurMovieList(cinema.getCitycode()));
		model.put("movieList", movieList);
		return "admin/cinema/multiPlayList.vm";
	}
	
	@RequestMapping("/admin/cinema/newssubtitle.xhtml")
	public String newssubtitle(Long parentid, HttpServletRequest request, ModelMap model){
		String signname = SignName.NEWS_SUBTITLE;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, parentid,null, false,0, 100);
		model.put("signname", signname);
		model.put("gcList", gcList);
		return "admin/cinema/newssubtitle.vm";
	}
	
	/**
	 *  电影推荐区 排序
	 **/
	@RequestMapping("/admin/cinema/cinemaByOrder.xhtml")
	public String cinemaByOrder(HttpServletRequest request, ModelMap model){
		String signname = SignName.CINEMA_ORDER;
		List<GewaCommend> gcList = commonService.getGewaCommendList(getAdminCitycode(request) , signname, null, false, true, 0,50);
		String cityCode = this.getAdminCitycode(request);
		if(gcList.isEmpty()){
			// 第一次加载, 没有任何推荐, 此时查出所有推荐区
			Map<String, String> countyMap = placeService.getCountyPairByCityCode(cityCode);
			model.put("countyMap", countyMap);
		}
		model.put("signname", signname);
		model.put("gcList", gcList);
		List<County> countyList = daoService.getObjectListByField(County.class, "citycode", cityCode);
		model.put("countyList", countyList);
		return "admin/cinema/cinemaByOrderRecommend.vm";
	}
	
	/**
	 *  电影推荐区 排序保存
	 **/
	@RequestMapping("/admin/cinema/saveCinemaOrder.xhtml")
	public String saveCinemaOrder(String relatedids, String signname, String titles, String ordernums, HttpServletRequest request, ModelMap model){
		String[] relatedidss = StringUtils.split(relatedids, ",");
		String[] titless = StringUtils.split(titles, ",");
		String[] ordernumss = StringUtils.split(ordernums, ",");
		
		GewaCommend gewaCommend = null;
		for(int i=0; i<relatedidss.length; i++){
			gewaCommend  = new GewaCommend(signname, titless[i], new Long(relatedidss[i]), new Integer(ordernumss[i]));
			gewaCommend.setCitycode(this.getAdminCitycode(request));
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	/**
	 *  电影推荐区 单个排序保存
	 **/
	@RequestMapping("/admin/cinema/saveCinemaOrderNum.xhtml")
	public String saveCinemaOrder(Long id, Integer ordernum, ModelMap model){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, id);
		if(gewaCommend != null){
			gewaCommend.setOrdernum(ordernum);
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/cinema/saveCinemaCounty.xhtml")
	public String saveCinemaCounty(String signname, String countycode,  HttpServletRequest request, ModelMap model){
		if(StringUtils.isBlank(countycode)) return showJsonError(model, "请选择城区！");
		List<GewaCommend> gcList = commonService.getCommendListByRelatedid(Long.valueOf(countycode), signname, null);
		if(!gcList.isEmpty()) return showJsonError(model, "该城区已存在！");
		County county = daoService.getObjectByUkey(County.class, "countycode", countycode, true);
		String cityCode = this.getAdminCitycode(request);
		GewaCommend gc = new GewaCommend(signname);
		gc.setCitycode(cityCode);
		gc.setTitle(county.getCountyname());
		gc.setRelatedid(Long.valueOf(countycode));
		daoService.saveObject(gc);
		return showJsonSuccess(model);
	}
	
/*	*//**
	 * 热门搜索
	 *//*
	@RequestMapping("/admin/cinema/movieindex-search.xhtml")
	public String search(HttpServletRequest request, ModelMap model) {
		String signname = SignName.MOVIEINDEX_SEARCH;
		String url = "admin/cinema/movieindex/search.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, false, url, request, model);
	}
	
	@RequestMapping("/admin/cinema/movieindex-buyticket.xhtml")
	public String buyticket(HttpServletRequest request, ModelMap model) {
		String signname = SignName.MOVIEINDEX_BUYTICKET;
		String url = "admin/cinema/movieindex/buyticket.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, false, url, request, model);
	}
	//经典电影
	@RequestMapping("/admin/cinema/movieindex-classicmovie.xhtml")
	public String classicMovie(HttpServletRequest request, ModelMap model){
		String signname = SignName.MOVIEINDEX_CLASSICMOVIE;
		String url = "admin/cinema/movieindex/classicmovie.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	//影评
	@RequestMapping("/admin/cinema/movieindex-diary.xhtml")
	public String diary(Long parentid, HttpServletRequest request, ModelMap model) {
		String signname = SignName.MOVIEINDEX_DIARY;
		String url = "admin/cinema/movieindex/diary.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, parentid, true, url, request, model);
	}
	
*/	
	//无人运营首页电影电影
	@RequestMapping("/admin/cinema/automovieindex-movie.xhtml")
	public String autoMovie(HttpServletRequest request, ModelMap model) {
		String signname = SignName.AUTO_MOVIEINDEX_MOVIE;
		String url = "admin/cinema/movieindex/movie.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	//电影电影
	@RequestMapping("/admin/cinema/movieindex-movie.xhtml")
	public String movie(HttpServletRequest request, ModelMap model) {
		String signname = SignName.MOVIEINDEX_MOVIE;
		String url = "admin/cinema/movieindex/movie.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	//活动
	@RequestMapping("/admin/cinema/movieindex-activity.xhtml")
	public String activity(HttpServletRequest request,String signname, ModelMap model) {
		if(StringUtils.isBlank(signname)){
			signname = SignName.MOVIEINDEX_ACTIVITY;
		}
		String url = "admin/cinema/movieindex/activity.vm";
		String citycode = getAdminCitycode(request);
		List citylist = getCityList();
		if(citylist.contains(citycode))
			model.put("type", "true");
		else
			model.put("type", "false");
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	private List<String> getCityList(){
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PASSCITY);
		List<String> citys = new ArrayList<String>();
		if(gewaConfig!=null){
			String sharecitys = gewaConfig.getContent();
			citys = Arrays.asList(StringUtils.split(sharecitys, ","));
			return citys;
		}else{
			return citys;
		}
	}
	
	//最新预告片
	@RequestMapping("/admin/cinema/movieindex-vedio.xhtml")
	public String vedio(HttpServletRequest request, ModelMap model) {
		String signname = SignName.MOVIEINDEX_VIDEO;
		String url = "admin/cinema/movieindex/video.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	//关注电影院
	@RequestMapping("/admin/cinema/movieindex-hotcinema.xhtml")
	public String hotCinema(HttpServletRequest request, ModelMap model){
		String signname = SignName.MOVIEINDEX_HOTCINEMA;
		String url = "admin/cinema/movieindex/hotcinema.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	//即将上映电影
	@RequestMapping("/admin/cinema/movieindex-futuremovie.xhtml")
	public String futuremovie(HttpServletRequest request, ModelMap model){
		String signname = SignName.MOVIEINDEX_FUTUREMOVIE;
		String url = "admin/cinema/movieindex/futuremovie.vm";
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		return getCommendList(rh, signname, null, true, url, request, model);
	}
	
	@RequestMapping("/admin/cinema/ajax/getCinemaRoomById.xhtml")
	public String getCinemaRoomById(Long cinemaRoomId, ModelMap model){
		CinemaRoom room = daoService.getObject(CinemaRoom.class, cinemaRoomId);
		if(room == null) return showJsonError_NOT_FOUND(model);
		Map result = BeanUtil.getBeanMap(room);
		return showJsonSuccess(model, result);
	}
	
	@RequestMapping("/admin/cinema/ajax/saveOrUpdateCinemaRoom.xhtml")
	public String saveOrUpdateCinemaRoom(Long id, Long cinemaId, String num, String roomname, 
			Integer seatnum, Integer screenheight, Integer screenwidth, String content, String characteristictype,ModelMap model){
		CinemaRoom cinemaRoom = null;
		final String hql = "from CinemaRoom c where c.cinemaid=? and c.roomname=?";
		List<CinemaRoom> cinemaRoomList = daoService.queryByRowsRange(hql, 0, 1, cinemaId, roomname);
		if(id!=null) {//只能更改
			cinemaRoom = daoService.getObject(CinemaRoom.class, id);
		}else {
			cinemaRoom = new CinemaRoom(cinemaId, OpiConstant.OPEN_GEWARA);
			cinemaRoom.setLinenum(0);
			cinemaRoom.setRanknum(0);
		}
		if(seatnum==null)seatnum = 0;
		if(screenheight==null)screenheight = 0;
		if(screenwidth==null)screenwidth = 0;
		if(StringUtils.isBlank(num)) return showJsonError(model, "影厅排序号不能为空！");
		if(!StringUtils.equals(cinemaRoom.getRoomname(),roomname)){//判断是否更改过影厅名称，如更改过后需要判断影厅名称是否存在
			if(!cinemaRoomList.isEmpty() && 
					(cinemaRoomList.get(0).getRoomname().equals(roomname))
					) return showJsonError(model, "该影院已存在该影厅名称！");
		}
		//判断影厅排序号是否重复
		final String numHql = "from CinemaRoom c where c.cinemaid=? and c.num=?";//查询排序号
		if(!StringUtils.equals(cinemaRoom.getNum(),num)){//判断是否更改过影厅排序号
			List<CinemaRoom> roomNumCount = daoService.queryByRowsRange(numHql, 0, 1, cinemaId, num);
			if(!roomNumCount.isEmpty()&&roomNumCount.size()>0) {
				return showJsonError(model, "该影院已存在该影厅排序号！");
			}
		}
		ChangeEntry changeEntry = new ChangeEntry(cinemaRoom);
		cinemaRoom.setRoomname(roomname);
		cinemaRoom.setSeatnum(seatnum);
		cinemaRoom.setScreenheight(screenheight);
		cinemaRoom.setScreenwidth(screenwidth);
		cinemaRoom.setContent(content);
		cinemaRoom.setNum(num);
		if (StringUtils.isNotBlank(characteristictype)) {
			cinemaRoom.setCharacteristic(characteristictype);
		}else{
			cinemaRoom.setCharacteristic(null);
		}
		daoService.saveObject(cinemaRoom);
		User user = getLogonUser();
		monitorService.saveChangeLog(user.getId(), CinemaRoom.class, cinemaRoom.getId(),changeEntry.getChangeMap( cinemaRoom));
		Map result = BeanUtil.getBeanMap(cinemaRoom);
		this.saveCinemaRoom(false, cinemaId,cinemaRoom.getId(),model);
		return showJsonSuccess(model, result);
	}
	@RequestMapping("/admin/cinema/saveCinemaRoom.xhtml")
	public String saveCinemaRoom(boolean check, Long cinemaId, Long cinemaRoomId, ModelMap model) {
		Cinema cinema = daoService.getObject(Cinema.class, cinemaId);
		if(cinema == null) return showJsonError(model, "cinema为空！传入cinemaID存在问题！");
		String otherinfo = cinema.getOtherinfo();
		Map<String, String> otherinfoMap = VmUtils.readJsonToMap(otherinfo);
		if(check){
			if(StringUtils.isBlank(otherinfoMap.get("roomList"))) otherinfoMap.put("roomList", "");
			if(!StringUtils.contains(otherinfoMap.get("roomList"), cinemaRoomId.toString())){
				String roomList = otherinfoMap.get("roomList") + "," + cinemaRoomId.toString();
				otherinfoMap.put("roomList", roomList);
			}
		}else{
			if(StringUtils.contains(otherinfoMap.get("roomList"), cinemaRoomId.toString())){
				String roomList = StringUtils.replace(otherinfoMap.get("roomList"), "," + cinemaRoomId.toString(), "");
				otherinfoMap.put("roomList", roomList);
			}
			if(StringUtils.isBlank(otherinfoMap.get("roomList"))) otherinfoMap.remove("roomList");
		}
		cinema.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(cinema);
		return showJsonSuccess(model);
	}
	
	
	//2. cinema-----------------------------------------------------------------------------
	@RequestMapping("/admin/cinema/cinemaIncrementalReport.xhtml")
	public String cinemaIncrementalReport(Date date, Integer pageNo,ModelMap model) throws Exception {
		if(pageNo == null) {
			pageNo = 0;
		}
		int maxnum = 200;
		Map<String,Object> params = new HashMap<String,Object>();
		if(date == null){
			date = DateUtil.addDay(new Date(), -1);
		}
		params.put("date", DateUtil.format(date,"yyyy年MM月dd日"));
		List<CinemaIncrementalReport> rList = this.mongoService.getObjectList(CinemaIncrementalReport.class, params, "citycode", true, pageNo * maxnum, maxnum);
		model.put("rList", rList);
		PageUtil pageUtil = new PageUtil(mongoService.getObjectCount(CinemaIncrementalReport.class, params), maxnum, pageNo, "/admin/cinema/cinemaIncrementalReport.xhtml", true, true);
		Map pageParams = new HashMap();
		pageParams.put("date", DateUtil.format(date,"yyyy-MM-dd"));
		pageUtil.initPageInfo(pageParams);
		model.put("pageUtil", pageUtil);
		return "admin/cinema/cinemaIncrementalReport.vm";
	}
	
	@RequestMapping("/admin/cinema/cinemaMemberMarkDetail.xhtml")
	public String cinemaIncrementalReport(String flag,long cinemaId,ModelMap model) throws Exception {
		List<MemberMark>  markList = markService.getMarkList("cinema", cinemaId, "generalmark", flag);
		model.put("markList", markList);
		model.put("cinema",this.daoService.getObject(Cinema.class, cinemaId));
		return "admin/cinema/memberMarkList.vm";
	}
	
	@RequestMapping("/admin/cinema/cinemaProNotifyList.xhtml")
	public String cinemaProNotifyList(String status,Date startTime,Date endTime,String num,String cinemaName,
			Integer pageNo,ModelMap model){
		DBObject queryCondition = new BasicDBObject();
		if(StringUtils.isBlank(status)){
			status = CinemaProNotify.STATUS_NEW;
		}
		if(StringUtils.equals(CinemaProNotify.STATUS_NEW, status)){
			queryCondition.putAll(mongoService.queryBasicDBObject("status", "=",CinemaProNotify.STATUS_NEW));
		}else{
			queryCondition.putAll(mongoService.queryBasicDBObject("status", "!=",CinemaProNotify.STATUS_NEW));
		}
		if(startTime != null && endTime != null){
			queryCondition.putAll(mongoService.queryBasicDBObject("addTime", ">=", DateUtil.formatTimestamp(DateUtil.getBeginningTimeOfDay(startTime))));
			queryCondition.putAll(mongoService.queryBasicDBObject("addTime", "<", DateUtil.formatTimestamp(DateUtil.getLastTimeOfDay(endTime))));
		}
		if(StringUtils.isNotBlank(num)){
			queryCondition.putAll(mongoService.queryBasicDBObject("num", "=", num));
		}
		if(StringUtils.isNotBlank(cinemaName)){
			queryCondition.putAll(mongoService.queryBasicDBObject("cinemaName", "=", cinemaName));
		}
		if(pageNo == null) {
			pageNo = 0;
		}
		int maxnum = 200;
		List<CinemaProNotify> cList = mongoService.getObjectList(CinemaProNotify.class, queryCondition, "addTime", false, pageNo * maxnum, maxnum);
		model.put("cList", cList);
		PageUtil pageUtil = new PageUtil(mongoService.getObjectCount(CinemaProNotify.class, queryCondition), maxnum, pageNo, "/admin/cinema/cinemaProNotifyList.xhtml", true, true);
		Map pageParams = new HashMap();
		pageParams.put("status", status);
		pageParams.put("startTime", startTime);
		pageParams.put("endTime", endTime);
		pageParams.put("num", num);
		pageParams.put("cinemaName",cinemaName);
		pageUtil.initPageInfo(pageParams);
		model.put("pageUtil", pageUtil);
		model.put("status", status);
		return "admin/cinema/cinemaProNotify.vm";
	}
	
	@RequestMapping("/admin/cinema/ajaxProcessCinemaNotifyList.xhtml")
	public  String processCinemaNotify(String id,String status,String msg,ModelMap model){
		if(StringUtils.isBlank(msg)){
			return this.showJsonError(model, "请填写备注信息！");
		}
		if(!(StringUtils.equals(status, CinemaProNotify.STATUS_PROCESS_N) || StringUtils.equals(status, CinemaProNotify.STATUS_PROCESS_Y))){
			return this.showJsonError(model, "处理状态错误，请正确操作！");
		}
		CinemaProNotify notify = mongoService.getObject(CinemaProNotify.class, MongoData.SYSTEM_ID, id);
		if(notify == null){
			return this.showJsonError(model, "要处理的公告不存在！");
		}
		User user = getLogonUser();
		Map<String,String> params = new HashMap<String,String>();
		params.put("id", notify.getNum());
		params.put("userid",user.getId() + "");
		params.put("handleMsg", msg);
		HttpResult result = HttpUtils.postUrlAsString(config.getString("cinemaproApiUrl") + "api/notice/handle.xhtml", params, 6000);
		if(!result.isSuccess()){
			return this.showJsonError(model, "调用影院商家系统故障，请稍后再试");
		}else{
			if(!StringUtils.equals(result.getResponse(), "success")){
				return this.showJsonError(model, result.getResponse());
			}
		}
		notify.setStatus(status);
		notify.setCheckTime(DateUtil.getCurFullTimestampStr());
		notify.setCheckUserId(user.getId());
		notify.setCheckUserName(user.getNickname());
		notify.setRemark(msg);
		mongoService.saveOrUpdateObject(notify, MongoData.SYSTEM_ID);
		return this.showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/cinema/pictureList.xhtml")
	public String pictureList(@RequestParam("tag") String tag, @RequestParam("relatedid") Long relatedid, ModelMap model) {
		List<Picture> pictureList = pictureService.getPictureListByRelatedid(tag, relatedid, 0, 200);
		Object object = relateService.getRelatedObject(tag, relatedid);
		model.put("firstpic", BeanUtil.get(object, "firstpic"));
		model.put("cinema", object);
		model.put("pictureList", pictureList);
		model.put("placeList", Arrays.asList("cinema", "ktv", "sport", "bar", "gym", "theatre", "coach", "gymcourse"));
		return "admin/cinema/pictureList.vm";
	}
}
