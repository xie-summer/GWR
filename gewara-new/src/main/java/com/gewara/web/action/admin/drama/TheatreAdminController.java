package com.gewara.web.action.admin.drama;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.Config;
import com.gewara.constant.ExpGrade;
import com.gewara.constant.Flag;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.SysAction;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaPoint;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.common.County;
import com.gewara.model.common.Indexarea;
import com.gewara.model.common.Subwayline;
import com.gewara.model.common.Subwaystation;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaStar;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreField;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.user.Member;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.QaService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.untrans.SearchService;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.PinYinUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;

@Controller
public class TheatreAdminController extends BaseAdminController {
	@Autowired@Qualifier("controllerService")
	private ControllerService controllerService;
	
	@Autowired@Qualifier("searchService")
	private SearchService searchService;
	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}
	@Autowired@Qualifier("qaService")
	protected QaService qaService;
	public void setQaService(QaService qaService) {
		this.qaService = qaService;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	public void setUserMessageService(UserMessageService userMessageService) {
		this.userMessageService = userMessageService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}
	
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	
	@RequestMapping("/admin/theatre/ajax/setTheatreHotValue.xhtml")
	public String setTheatreHotValue(Long theatreId, Integer value, ModelMap model) {
		User user = getLogonUser();
		Theatre theatre = daoService.getObject(Theatre.class, theatreId);
		if(theatre != null){
			ChangeEntry changeEntry = new ChangeEntry(theatre);
			theatre.setHotvalue(value);
			daoService.updateObject(theatre);
			monitorService.saveChangeLog(user.getId(), Theatre.class, theatre.getId(),changeEntry.getChangeMap( theatre));
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/theatre/ajax/setStarHotValue.xhtml")
	public String setStarHotValue(Long starid, Integer value, ModelMap model) {
		DramaStar dramaStar = daoService.getObject(DramaStar.class, starid);
		if(dramaStar!=null){
			dramaStar.setHotvalue(value);
			dramaStar.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObject(dramaStar);
		}
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/theatre/modifyTheatreDetail.xhtml")
	public String modifyTheatreDetail(Long theatreId, HttpServletRequest request, ModelMap model) {
		Theatre theatre = new Theatre();
		String citycode = null;
		if(theatreId !=null){
			theatre = daoService.getObject(Theatre.class, theatreId);
			citycode = theatre.getCitycode();
		}else{
			citycode = getAdminCitycode(request);
		}
		
		List<Subwayline> lineList = placeService.getSubwaylinesByCityCode(citycode);
		List<Subwaystation> stationList =  new ArrayList<Subwaystation>();
		model.put("theatre", theatre);
		model.put("lineList", lineList);
		model.put("stationList", stationList);
		model.put("citycode", citycode);
		List countyList = placeService.getCountyByCityCode(citycode);
		model.put("countyList", countyList);
		if(StringUtils.isNotBlank(theatre.getCountycode())){
			model.put("indexareaList", placeService.getIndexareaByCountyCode(theatre.getCountycode()));
		}
		return "admin/theatre/theatreForm.vm";
	}
	
	@RequestMapping("/admin/theatre/saveTheatre.xhtml")
	public String saveTheatre(Long theatreId, String name, String countyCode, String indexareaCode, String stationid, HttpServletRequest request, ModelMap model) {
		Theatre theatre = null;
		String citycode = "";
		if (theatreId != null){
			theatre = daoService.getObject(Theatre.class, theatreId);
		}else{
			theatre = new Theatre(name);
			citycode = getAdminCitycode(request);
		}
		ChangeEntry changeEntry = new ChangeEntry(theatre);
		BindUtils.bindData(theatre, request.getParameterMap());
		
		String[] otheritemList = new String[]{Flag.SERVICE_PARK, Flag.SERVICE_VISACARD, Flag.SERVICE_PLAYGROUND};
		Map<String, Object> otherinfoMap = controllerService.getAndSetOtherinfo(theatre.getOtherinfo(), Arrays.asList(otheritemList), request);
		theatre.setOtherinfo(JsonUtils.writeObjectToJson(otherinfoMap));
		//验证内容
		String msg=ValidateUtil.validateNewsContent(null,theatre.getContent());
		if(StringUtils.isNotBlank(msg)) return showJsonError(model, msg);
		if(StringUtils.isNotBlank(countyCode)){
			County county = daoService.getObject(County.class, countyCode);
			theatre.setCountycode(StringUtils.trimToNull(countyCode));
			theatre.setCountyname(county.getCountyname());
			theatre.setCitycode(county.getCitycode());
		}else{
			theatre.setCountycode(null);
			theatre.setCountyname(null);
			if(StringUtils.isNotBlank(citycode)) theatre.setCitycode(citycode);
		}
		if(StringUtils.isNotBlank(indexareaCode)){
			theatre.setIndexareacode(StringUtils.trimToNull(indexareaCode));
			theatre.setIndexareaname(daoService.getObject(Indexarea.class, indexareaCode).getIndexareaname());
		}
		if(StringUtils.isNotBlank(stationid)){
			Subwaystation subwaystation = daoService.getObject(Subwaystation.class, Long.valueOf(stationid));
			theatre.setStationname(subwaystation.getStationname());
		}else{
			theatre.setStationname(null);
		}
		if(theatre.getAddtime()==null) theatre.setAddtime(new Timestamp(System.currentTimeMillis()));
		theatre.setPinyin(PinYinUtils.getPinyin(theatre.getName()));
		theatre.setUpdatetime(new Timestamp(System.currentTimeMillis()));
//		theatre.setSearchkey(placeService.getSearchKey(theatre));
		daoService.saveObject(theatre);
		monitorService.saveChangeLog(getLogonUser().getId(), Theatre.class, theatre.getId(),changeEntry.getChangeMap(theatre));
		blogService.addBlogData(getLogonUser().getId(), TagConstant.TAG_THEATRE, theatre.getId());
		searchService.pushSearchKey(theatre);//更新索引至索引服务器
		return showJsonSuccess(model, theatre.getId()+"");
	}
	@RequestMapping("/admin/theatre/theatreList.xhtml")
	public String getTheatreList(String name, HttpServletRequest request, ModelMap model) {
		DetachedCriteria qry = DetachedCriteria.forClass(Theatre.class);
		qry.add(Restrictions.eq("citycode", getAdminCitycode(request)));
		if (StringUtils.isNotBlank(name)) qry.add(Restrictions.like("name", name, MatchMode.ANYWHERE));
		qry.addOrder(Order.asc("id"));
		List<Theatre> theatreList = hibernateTemplate.findByCriteria(qry);
		model.put("theatreList", theatreList);
		return "admin/theatre/theatreList.vm";
	}
	@RequestMapping("/admin/theatre/fieldDetail.xhtml")
	public String getFieldDetail(Long fieldid,  ModelMap model) {
		if(fieldid!=null) {
			TheatreField field = daoService.getObject(TheatreField.class, fieldid);
			model.put("field", field);
		}
		return "admin/theatre/fieldDetail.vm";
	}
	
	@RequestMapping("/admin/theatre/roomList.xhtml")
	public String getTheatreList(Long theatreid,  ModelMap model) {
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		String query = "from TheatreField t where t.theatreid=? ";
		List<TheatreField> fieldList = hibernateTemplate.find(query, theatreid);
		model.put("fieldList", fieldList);
		String hql = "from TheatreRoom r where r.theatreid=? order by r.num";
		List<TheatreRoom> roomList = hibernateTemplate.find(hql, theatreid);
		Map<Long, List<TheatreRoom>> roomMap = BeanUtil.groupBeanList(roomList, "fieldid");
		model.put("theatre", theatre);
		model.put("roomMap", roomMap);
		return "admin/theatre/roomList.vm";
	}
	@RequestMapping("/admin/theatre/roomDetail.xhtml")
	public String getTheatreDetail(Long roomid, Long theatreid, Long fieldid, ModelMap model) {
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		if(theatre == null) return show404(model, "场馆不存在或被删除！");
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return show404(model, "场地不存在或被删除！");
		if(roomid!=null) {
			TheatreRoom room = daoService.getObject(TheatreRoom.class, roomid);
			model.put("room", room);
		}
		return "admin/theatre/roomDetail.vm";
	}
	@RequestMapping("/admin/theatre/saveRoom.xhtml")
	public String saveRoom(Long id, Long theatreid, Long fieldid, HttpServletRequest request, ModelMap model){
		TheatreRoom room = null;
		if(id!=null) {
			room = daoService.getObject(TheatreRoom.class, id);
			if(room == null) return showJsonError(model, "场区不存在！");
		}else{
			room = new TheatreRoom(theatreid, fieldid);
		}
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		if(theatre == null) return show404(model, "场馆不存在或被删除！");
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return showJsonError(model, "场地不存在！");
		ChangeEntry changeEntry = new ChangeEntry(room);
		BindUtils.bindData(room, request.getParameterMap());
		if(room.getFirstline()==null) room.setFirstline(1);
		if(room.getFirstrank()==null) room.setFirstrank(1);
		room.setRoomtype(field.getFieldtype());
		room.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(room);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreRoom.class, room.getId(),changeEntry.getChangeMap( room));
		return showJsonSuccess(model, room.getId()+"");
	}
	@RequestMapping("/admin/theatre/saveField.xhtml")
	public String saveField(Long id, Long theatreid, HttpServletRequest request, ModelMap model){
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		if(theatre == null) return showJsonError(model, "场馆不存在或被删除！");
		TheatreField field = null;
		if(id!=null) {
			field = daoService.getObject(TheatreField.class, id);
			if(field == null) return showJsonError_NOT_FOUND(model);
			field.setUpdatetime(DateUtil.getCurFullTimestamp());
		}else{
			field = new TheatreField(theatreid, OdiConstant.PARTNER_GEWA);
		}
		ChangeEntry changeEntry = new ChangeEntry(field);
		BindUtils.bindData(field, request.getParameterMap());
		daoService.saveObject(field);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreField.class, field.getId(),changeEntry.getChangeMap(field));
		return showJsonSuccess(model, field.getId()+"");
	}
	@RequestMapping("/admin/theatre/delRoom.xhtml")
	public String delRoom(Long id,  ModelMap model){
		TheatreRoom room = daoService.getObject(TheatreRoom.class, id);
		daoService.removeObject(room);
		monitorService.saveDelLog(getLogonUser().getId(), id, room);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/theatre/delField.xhtml")
	public String delField(Long id,  ModelMap model){
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", id);
		if(!roomList.isEmpty()) return showJsonError(model, "场地存在区域不能删除！");
		TheatreField field = daoService.getObject(TheatreField.class, id);
		ChangeEntry changeEntry = new ChangeEntry(field);
		field.setStatus(Status.N);
		field.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(field);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreField.class, id, changeEntry.getChangeMap(field));
		return showJsonSuccess(model);
	}
	
	/**
	 *  演出推荐区 排序
	 **/
	@RequestMapping("/admin/theatre/theatreByOrder.xhtml")
	public String cinemaByOrder(HttpServletRequest request, ModelMap model){
		String signname = SignName.THEATRE_ORDER;
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
		return "admin/theatre/theatreByOrderRecommend.vm";
	}
	
	/**
	 *  演出推荐区 排序保存
	 **/
	@RequestMapping("/admin/theatre/saveTheatreOrder.xhtml")
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
	 *  演出推荐区 单个排序保存
	 **/
	@RequestMapping("/admin/theatre/saveTheatreOrderNum.xhtml")
	public String saveCinemaOrder(Long id, Integer ordernum, ModelMap model){
		GewaCommend gewaCommend = daoService.getObject(GewaCommend.class, id);
		if(gewaCommend != null){
			gewaCommend.setOrdernum(ordernum);
			daoService.saveObject(gewaCommend);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/theatre/saveTheatreCounty.xhtml")
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
	
	@RequestMapping("/admin/theatre/answerQuestion.xhtml")
	public String answerTheatreAuestion(String questionstatus,ModelMap model){
		List<GewaQuestion> questionList = qaService.getQuestionByQsAndTagList("",questionstatus,TagConstant.TAG_THEATRE,"addtime",-1);
		List<Long> dramaIds = BeanUtil.getBeanPropertyList(questionList,Long.class,"categoryid",true);
		List<Drama> dramas = daoService.getObjectList(Drama.class,dramaIds);
		Map<Long,Drama> dramaMap = BeanUtil.beanListToMap(dramas,"id");
		model.put("questionList", questionList);
		model.put("dramaMap", dramaMap);
		return "admin/drama/dramaQuestionList.vm";
	}
	
	@RequestMapping("/admin/theatre/getGewaQuestion.xhtml")
	public String getGewaQuestion(Long id, ModelMap model){
		GewaQuestion question = daoService.getObject(GewaQuestion.class, id);
		if(question == null) return showJsonError_NOT_FOUND(model);
		model.put("question", question);
		return "admin/drama/questionDetail.vm";
	}
	@RequestMapping("/admin/theatre/saveAnswer.xhtml")
	public String answer(HttpServletRequest request, ModelMap model) {
		String ip = WebUtils.getRemoteIp(request);
		GewaAnswer answer = new GewaAnswer("");
		Map<String, String[]> answerMap = request.getParameterMap();
		BindUtils.bindData(answer, answerMap);
		Long answerMemberid = qaService.getGewaraAnswerByMemberid(); //40856321L;
		answer.setMemberid(answerMemberid);
		answer.setIp(WebUtils.getIpAndPort(ip, request));
		if (StringUtils.isBlank(answer.getContent())) return showJsonError(model, "回答内容不能为空！");
		if (StringUtil.getByteLength(answer.getContent())>4000)return showJsonError(model, "回答内容不能超过4000个字符！，当前你已输入"+StringUtil.getByteLength(answer.getContent())+"字符");
		if (WebUtils.checkPropertyAll(answer)) return showJsonError(model, "含有非法字符！");
		GewaQuestion question = daoService.getObject(GewaQuestion.class, answer.getQuestionid());
		boolean addPoint = false;
		Integer point = ExpGrade.EXP_ANSWER_ADD_COMMON;
		Member questionMember = daoService.getObject(Member.class, question.getMemberid());
		question.setReplymemberid(answerMemberid);
		question.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		question.addReplycount();
		if (GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus()))
			question.setQuestionstatus(GewaQuestion.QS_STATUS_N);
		daoService.saveObject(answer);
		daoService.saveObject(question);
		if (addPoint) {
			GewaQaPoint sendquestion = new GewaQaPoint(question.getId(), answer.getId(), answerMemberid, point, GewaQaPoint.TAG_REPLYQUESTION);
			daoService.saveObject(sendquestion);
		}
		if (!question.getMemberid().equals(answerMemberid) && (GewaQuestion.QS_STATUS_N.equals(question.getQuestionstatus()) || 
				GewaQuestion.QS_STATUS_Z.equals(question.getQuestionstatus()))) {// 发送邮件
			model.put("questionnickname", questionMember.getNickname());
			model.put("nickname", "小编");
			model.put("question", BeanUtil.getBeanMapWithKey(question, "id", "title"));
			model.put("content", answer.getContent());
			String body = "你在格瓦拉知道上的提问： <a href='"+config.getBasePath()+"qa/qaDetail.xhtml?qid="+question.getId()+"'>"+question.getTitle()+"</a> 已有新的回答了。";
			userMessageService.sendSiteMSG(question.getMemberid(), SysAction.ACTION_QUESTION_NEW_ANSWER, question.getId(), body);
		}
		if(StringUtils.equals(answer.getStatus(), Status.N_FILTER)) return showJsonError_KEYWORD(model);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/theatre/getFieldAreaSite.xhtml")
	public String fieldAreaSite(Long fieldid, HttpServletRequest request, ModelMap model){
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return showMessageAndReturn(model, request, "场地数据不存在或被删除！");
		List<TheatreRoom> areaList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", field.getId());
		model.put("field", field);
		model.put("areaList", areaList);
		Map<Long, String> areaZoneMap = BeanUtil.beanListToMap(areaList, "id", "hotzone", true);
		model.put("areaZoneMap", JsonUtils.writeObjectToJson(areaZoneMap));
		return "admin/theatre/fieldAreaSite.vm";
	}
	
	@RequestMapping("/admin/theatre/updateFieldLogo.xhtml")
	public String updateFieldLogo(Long fieldid, String logo, HttpServletRequest request, ModelMap model){
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return showMessageAndReturn(model, request, "场地数据不存在或被删除！");
		ChangeEntry changeEntry = new ChangeEntry(field);
		field.setLogo(logo);
		field.setUpdatetime(DateUtil.getCurFullTimestamp());
		daoService.saveObject(field);
		monitorService.saveChangeLog(getLogonUser().getId(), TheatreField.class, fieldid, changeEntry.getChangeMap(field));
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/admin/theatre/setHotzoneByFieldid.xhtml")
	public String theatreRoomSite(Long fieldid, String hotzone, ModelMap model){
		TheatreField field = daoService.getObject(TheatreField.class, fieldid);
		if(field == null) return showJsonError(model, "场地不存在！");
		if(StringUtils.isBlank(hotzone)) return showJsonError(model, "绘图数据不能为空！");
		List<TheatreRoom> roomList = daoService.getObjectListByField(TheatreRoom.class, "fieldid", field.getId());
		Map<Long, TheatreRoom> roomMap = BeanUtil.beanListToMap(roomList, "id");	
		try{
			List<Map> hotzoneList = JsonUtils.readJsonToObjectList(Map.class, hotzone);
			Timestamp cur = DateUtil.getCurFullTimestamp();
			for (Map obj: hotzoneList) {
				Long roomid = Long.valueOf(obj.get("roomid")+"");
				TheatreRoom room = roomMap.get(roomid);
				if(room == null) return showJsonError(model, "场区不存在！");
				String zone = obj.get("hotzone")+"";
				if(StringUtils.isBlank(zone) || StringUtils.equals(zone, room.getHotzone())){
					continue;
				}
				ChangeEntry changeEntry = new ChangeEntry(room);
				room.setHotzone(zone);
				room.setUpdatetime(cur);
				daoService.saveObject(room);
				monitorService.saveChangeLog(getLogonUser().getId(), TheatreRoom.class, room.getId(), changeEntry.getChangeMap(room));
			}
		}catch(Exception e){
			dbLogger.warn("", e);
			return showJsonError(model, "绘图数据错误！");
		}
		return showJsonSuccess(model);
	}
}
