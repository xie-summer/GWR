package com.gewara.web.action.inner.mobile.sports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.gewacup.ClubInfo;
import com.gewara.json.gewacup.MiddleTable;
import com.gewara.json.gewacup.Players;
import com.gewara.model.user.Member;
import com.gewara.support.ServiceHelper;
import com.gewara.util.BindUtils;
import com.gewara.util.ValidateUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.action.subject.gewacup.GewaCupApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;

@Controller
public class OpenApiGewaCupController extends BaseOpenApiController {
	@Autowired
	@Qualifier("gewaCupApiService")
	private GewaCupApiController gewaCupApiService;

	// WAP最多报名10人
	private static final int MAX_WAP_PLAYERCOUNT = 10;

	/**
	 * 用户报名
	 */
	@RequestMapping("/openapi/mobile/gewacup/savePlayersInfo.xhtml")
	public String savePlayersInfo(HttpServletRequest request, Long clubInfoId, String mid, ModelMap model) {
		String yearstype = request.getParameter("yearstype");
		if (clubInfoId != null) {
			DBObject queryCondition = new BasicDBObject();
			DBObject yearstypeDbObject = mongoService.queryBasicDBObject("yearstype", "=", yearstype);
			DBObject clubInfoIdDbObject = mongoService.queryBasicDBObject("clubInfoId", "=", clubInfoId);
			queryCondition.putAll(yearstypeDbObject);
			queryCondition.putAll(clubInfoIdDbObject);
			List<MiddleTable> midList = mongoService.getObjectList(MiddleTable.class, queryCondition);
			if (!midList.isEmpty() && midList.size() >= 8)
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "每个俱乐部最多只能报8个项目！");
		}
		return savePlayersInfoMethod(request, clubInfoId, mid, yearstype, model);
	}

	/**
	 * 当前报名个数
	 */
	@RequestMapping("/openapi/mobile/gewacup/sourceCount.xhtml")
	public String getGewacupCountBySource(HttpServletRequest request, ModelMap model) {
		int count = getSourceCount(request);
		initField(model, request);
		return getSingleResultXmlView(model, count);
	}
	
	
	/**
	 * 报名状态
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/gewacup/getSignupStatus.xhtml")
	public String getSignupStatus(HttpServletRequest request, ModelMap model) {
		String yearstype = request.getParameter("yearstype");
		String clubstatus = gewaCupApiService.getTime("gewaCupPersonal", yearstype);
		return getSingleResultXmlView(model, clubstatus);
	}

	/**
	 * 我的报名信息
	 */
	@RequestMapping("/openapi/mobile/gewacup/myPlayersInfo.xhtml")
	public String myPlayersInfo(HttpServletRequest request, ModelMap model) {
		String yearstype = request.getParameter("yearstype");
		return myPlayersInfoMethod(yearstype, request, model);
	}

	private static List<String> GEWA_CPU_TYPELIST = new ArrayList<String>();
	static {
		GEWA_CPU_TYPELIST.add(MongoData.GEWA_CUP_BOY_SINGLE);
		GEWA_CPU_TYPELIST.add(MongoData.GEWA_CUP_BOY_DOUBLE);
		GEWA_CPU_TYPELIST.add(MongoData.GEWA_CUP_GIRL_SINGLE);
		GEWA_CPU_TYPELIST.add(MongoData.GEWA_CUP_GIRL_DOUBLE);
		GEWA_CPU_TYPELIST.add(MongoData.GEWA_CUP_MIXED_DOUBLE);
	}

	private int getSourceCount(HttpServletRequest request) {
		String yearstype = request.getParameter("yearstype");
		String source = request.getParameter("source");
		DBObject queryCollection = new BasicDBObject();
		queryCollection.put("memberid",new BasicDBObject().append(QueryOperators.NE, null));
		queryCollection.put("yearstype", yearstype);
		queryCollection.put("source", source);
		int count = mongoService.getObjectCount(Players.class, queryCollection);
		return count;
	}

	private String savePlayersInfoMethod(HttpServletRequest request, Long clubInfoId, String mid, String yearsType, ModelMap model) {
		String clubstatus = "";
		if (clubInfoId == null)
			clubstatus = gewaCupApiService.getTime("gewaCupPersonal", yearsType);
		else
			clubstatus = gewaCupApiService.getTime("gewaCupCommu", yearsType);
		if (!StringUtils.equals(clubstatus, "game"))
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "此报名方式已经停止或未开始!");
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null)
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请先登录！");
		Map datamap = request.getParameterMap();
		
		
		String type = ServiceHelper.get(datamap, "type");
		String idcards = ServiceHelper.get(datamap, "idcards");
		String source = ServiceHelper.get(datamap, "source");
		if (StringUtils.isBlank(type) || !GEWA_CPU_TYPELIST.contains(type))
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "报名类型不正确！");

		if (!ValidateUtil.isMobile(ServiceHelper.get(datamap, "phone")))
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你的手机号码格式不正确！");

		if (StringUtils.isBlank(source)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "渠道不能为空");
		}
		// WAP渠道最多只报名10人
		if (StringUtils.equals(source, AddressConstant.ADDRESS_WAP) && getSourceCount(request) >= MAX_WAP_PLAYERCOUNT) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "很抱歉，五星体育报名名额已满。欢迎关注格瓦拉@运动频道，免单活动时常有。");
		}
		
		
		MiddleTable mt = new MiddleTable();
		boolean playerstatus = true;
		boolean partnerstatus = true;
		
		if (StringUtils.isNotBlank(mid)) {
			mt = mongoService.getObject(MiddleTable.class, "id", mid);
			if (mt == null)
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "参数错误！");
			Players play = mongoService.getObject(Players.class, "id", mt.getFromid());
			if (StringUtils.equals(play.getIdcards(), idcards))
				playerstatus = false;
		}
		if (playerstatus) {
			if (gewaCupApiService.getIdcards(idcards, type, yearsType))
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你的身份证已报过这项目，或已经参加了两个项目！");
		}

		List<Players> playerList = new ArrayList<Players>();
		if (StringUtils.equals(type, MongoData.GEWA_CUP_BOY_DOUBLE) || StringUtils.equals(type, MongoData.GEWA_CUP_GIRL_DOUBLE)
				|| StringUtils.equals(type, MongoData.GEWA_CUP_MIXED_DOUBLE)) {
			String partnerphone = ServiceHelper.get(datamap, "partnerphone");
			String partneridcards = ServiceHelper.get(datamap, "partneridcards");
			if (!ValidateUtil.isMobile(partnerphone))
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "搭档手机号码格式不正确！！");
			if (StringUtils.equals(idcards, partneridcards))
				return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "你的身份证不能和搭档身份证相同！");
			if (StringUtils.isNotBlank(mid)) {
				Players partner = mongoService.getObject(Players.class, "id", mt.getToid());
				if (partner != null) {
					if (StringUtils.equals(partner.getIdcards(), partneridcards))
						partnerstatus = false;
				}
			}
			if (partnerstatus) {
				if (gewaCupApiService.getIdcards(partneridcards, type, yearsType))
					return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "搭档身份证已报过这项目，或已经参加了两个项目！");
			}
			Players partnerplayer = new Players();
			partnerplayer.setIdcards(partneridcards);
			partnerplayer.setIdcardslogo(ServiceHelper.get(datamap, "partneridcardslogo"));
			partnerplayer.setPlayer(ServiceHelper.get(datamap, "partnerplayer"));
			partnerplayer.setPhone(partnerphone);
			partnerplayer.setSex(ServiceHelper.get(datamap, "partnersex"));
			if (clubInfoId != null)
				partnerplayer.setClubInfoId(clubInfoId);
			playerList.add(partnerplayer);
		}
		Players player = new Players();
		BindUtils.bindData(player, datamap);
		player.setMemberid(member.getId());
		player.setMembername(member.getNickname());
		if (clubInfoId != null)
			player.setClubInfoId(clubInfoId);
		playerList.add(0, player);
		gewaCupApiService.savePlayers(playerList, type, yearsType, source);
		initField(model, request);
		return getSingleResultXmlView(model, "success");
	}

	private String myPlayersInfoMethod(String yearsType, HttpServletRequest request, ModelMap model) {
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		if (member == null)
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "请先登录！");

		DBObject queryCondition = new BasicDBObject();
		DBObject memberidDbObject = mongoService.queryBasicDBObject("memberid", "=", member.getId());
		DBObject yearstypeDbObject = mongoService.queryBasicDBObject("yearstype", "=", yearsType);
		queryCondition.putAll(memberidDbObject);
		queryCondition.putAll(yearstypeDbObject);
		List<ClubInfo> clubList = mongoService.getObjectList(ClubInfo.class, queryCondition);
		DBObject clubInfoIdDbObject = mongoService.queryBasicDBObject("clubInfoId", "=", null);
		queryCondition.putAll(clubInfoIdDbObject);
		List<MiddleTable> midList = mongoService.getObjectList(MiddleTable.class, queryCondition);
		if (!clubList.isEmpty()) {
			gewaCupApiService.getClubPlayersInfo(clubList, yearsType, model);
		}
		if (!midList.isEmpty()) {
			model.put("personalList", gewaCupApiService.getPersonalPlayersInfo(midList));
		}
		model.put("personalstatus", gewaCupApiService.getTime("gewaCupPersonal", yearsType));
		model.put("clubstatus", gewaCupApiService.getTime("gewaCupCommu", yearsType));
		initField(model, request);
		return getXmlView(model, "sport/hisports/myPlayersInfo.vm");
	}
}
