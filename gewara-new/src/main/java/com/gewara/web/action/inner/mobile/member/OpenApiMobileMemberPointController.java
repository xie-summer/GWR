package com.gewara.web.action.inner.mobile.member;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.PointConstant;
import com.gewara.model.user.Festival;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Point;
import com.gewara.service.bbs.CommonPartService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ShareService;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
import com.gewara.web.action.inner.mobile.BaseOpenApiController;
import com.gewara.web.filter.OpenApiMobileAuthenticationFilter;

@Controller
public class OpenApiMobileMemberPointController extends BaseOpenApiController{
	@Autowired@Qualifier("commonPartService")
	private CommonPartService commonPartService;
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	/**
	 * 每日积分红包领取方式列表
	 * @param memberEncode
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/getPointType.xhtml")
	public String getPointType(String memberEncode,ModelMap model){
		if(StringUtils.isBlank(memberEncode)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "memberEncode is not null!");
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		List<Point> todayPointList = pointService.getPointListByMemberid(member.getId(), PointConstant.TAG_LOGIN_ACTIVIRY, DateUtil.getBeginningTimeOfDay(new Timestamp(System.currentTimeMillis())), null, null, 0, 1);
		if(todayPointList.isEmpty()){
			model.put("isSuccess", "fail");
		}else{
			model.put("isSuccess", "success");
			model.put("todayPoint", todayPointList.get(0));
		}
		//节日判断
		Date curDate = new Date();
		Festival festival = commonPartService.getCurFestival(curDate);
		if(festival != null){
			model.put("festival", festival);
		}
		Festival nextFestival = commonPartService.getNextFestival(curDate);
		if(nextFestival != null){
			model.put("nextFestival", nextFestival);
			model.put("diffFestivalDay", DateUtil.getDiffDay(nextFestival.getFestdate(), DateUtil.getCurDate()));
		}
		model.put("curDate", DateUtil.format(curDate, "yyyy年MM月dd日"));
		MemberInfo info = daoService.getObject(MemberInfo.class, member.getId());
		if(info.getPointvalue()>=5){
			model.put("pointIsEngouth", true);
		}
		model.put("memberinfo", info);
		Integer continunum = pointService.getPointRewardsDay(member.getId(), DateUtil.getBeginTimestamp(curDate));
		model.put("continunum", continunum);
		return getXmlView(model, "inner/mobile/pointType.vm");
	}
	/**
	 * 领取每日红包
	 * @param memberEncode
	 * @param pointType
	 * @param model
	 * @return
	 */
	@RequestMapping("/openapi/mobile/member/getDayPoint.xhtml")
	public String getDayPoint(String memberEncode,String pointType,ModelMap model){
		if(StringUtils.isBlank(pointType) || StringUtils.isBlank(memberEncode)){
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "memberEncode or pointType is not null!");
		}
		Member member = OpenApiMobileAuthenticationFilter.getOpenApiAuth().getMember();
		model.put("type", pointType);
		try{
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			if("festival".equals(pointType)){
				ErrorCode<Map> resultCode = pointService.addLoginPointInFestival(member);
				if(!resultCode.isSuccess()) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, resultCode.getMsg());
				model.putAll(resultCode.getRetval());
			}else{
				if("risk".equals(pointType)){
					pointType = "bit";
				}else if("stable".equals(pointType)){
					pointType = "";
				}else if("weibo".equals(pointType)){
					pointType = "brt";
					ErrorCode code = pointService.validWeiboPoint(member, true);
					if(!code.isSuccess()){
						return getErrorXmlView(model, code.getErrcode(), code.getMsg());
					}
				}
				ErrorCode<Point> point = pointService.addLoginPoint(member, pointType, cur);
				if(!point.isSuccess()) {
					return getErrorXmlView(model, point.getErrcode(), point.getMsg());
				}
				if(StringUtils.equals(pointType, "brt")){
					String content = "今天成功领取"+point.getRetval().getPoint()+"积分微博控红包 @格瓦拉生活网 http://www.gewara.com/everday/acct/mygift.xhtml";
					String picUrl = "css/home/red/mygift_wb.jpg";
					shareService.sendShareInfo("point", point.getRetval().getId(), member.getId(), content, picUrl);
				}
				model.put("pointValue", point.getRetval());
			}
		} catch (Exception e) {
			dbLogger.error(StringUtil.getExceptionTrace(e));
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "操作异常！");
			
		}
		/*获取下一个节日*/
		Date curDate = new Date();
		Festival nextFestival = commonPartService.getNextFestival(curDate);
		if(nextFestival != null){
			model.put("nextFestival", nextFestival);
			model.put("diffFestivalDay", DateUtil.getDiffDay(nextFestival.getFestdate(), DateUtil.getCurDate()));
		}
		model.put("curDate", DateUtil.format(curDate, "yyyy年MM月dd日"));
		model.put("memberinfo",daoService.getObject(MemberInfo.class, member.getId()));
		Integer continunum = pointService.getPointRewardsDay(member.getId(), DateUtil.getBeginTimestamp(curDate));
		model.put("continunum", continunum);
		return getXmlView(model, "api/mobile/pointData.vm");
	}
}
