package com.gewara.web.action.inner.mobile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.ui.ModelMap;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.ApiConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.app.AppConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.api.GewaApiBbsHelper;
import com.gewara.helper.api.GewaApiGoodsHelper;
import com.gewara.helper.api.GewaApiHelper;
import com.gewara.helper.api.GewaApiMemberHelper;
import com.gewara.helper.api.GewaApiMovieHelper;
import com.gewara.helper.api.GewaApiOrderHelper;
import com.gewara.json.PhoneActivity;
import com.gewara.json.Weixin2Wala;
import com.gewara.model.api.ApiUser;
import com.gewara.model.bbs.Diary;
import com.gewara.model.content.Picture;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.GoodsPrice;
import com.gewara.model.mobile.AsConfig;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.movie.MovieVideo;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.OrderAddress;
import com.gewara.model.pay.OtherFeeDetail;
import com.gewara.model.pay.SpCode;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.model.user.SysMessageAction;
import com.gewara.model.user.TempMember;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.VmUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.bbs.Comment;

public class BaseOpenApiMobileController extends BaseOpenApiController{
	protected Map<String, Object> getMovieData(Movie movie){
		return getMovieData(movie, getMobilePath() + movie.getLogo(), true);
	}
	protected void getMovieMap(Movie movie, ModelMap model, HttpServletRequest request){
		if(movie==null) return;
		Map<String, Object> resMap = getMovieData(movie);
		putDetail(resMap, model, request);
	}
	protected void getMovieListMap(List<Movie> movieList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Movie movie : movieList){
			Map<String, Object> params = getMovieData(movie);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	
	protected Map<String, Object> getCinemaData(Cinema cinema){
		String mobilePath = getMobilePath();
		return getCinemaData(cinema, mobilePath + cinema.getLimg(), mobilePath + cinema.getFirstpic(), true);
	}
	protected void getCinemaMap(Cinema cinema, ModelMap model, HttpServletRequest request){
		if(cinema==null) return;
		String mobilePath = getMobilePath();
		Map<String, Object> resMap = getCinemaData(cinema, mobilePath + cinema.getLimg(), mobilePath + cinema.getFirstpic(), true);
		putDetail(resMap, model, request);
	}
	protected void getCienmaListMap(List<Cinema> cinemaList, ModelMap model, HttpServletRequest request){
		String mobilePath = getMobilePath();
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Cinema cinema : cinemaList){
			Map<String, Object> params = getCinemaData(cinema, mobilePath + cinema.getLimg(), mobilePath + cinema.getFirstpic(), true);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	
	//物品数据
	protected void getGoodsOrderData(GoodsOrder order, ModelMap model, HttpServletRequest request){
		if(order==null) return;
		Map<String, Object> resMap = getGoodsOrderMap(order);
		putDetail(resMap, model, request);
	}
	protected Map<String, Object> getGoodsOrderMap(GoodsOrder order){
		Map<String, Object> resMap = GewaApiOrderHelper.getGoodsOrderMap(order);
		return resMap;
	}
	protected Map<String, Object> getDramaOrderMap(DramaOrder order) {
		Map<String, Object> resMap = GewaApiOrderHelper.getDramaOrderMap(order);
		Theatre theatre = daoService.getObject(Theatre.class, order.getTheatreid());
		Drama drama = daoService.getObject(Drama.class, order.getDramaid());
		OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid());
		int expressFee = 0;
		String takemethod = OdiConstant.TAKEMETHOD_QUPIAOJI;
		if(StringUtils.equals(order.getExpress(), Status.Y)){
			List<OtherFeeDetail> feeList = daoService.getObjectListByField(OtherFeeDetail.class, "orderid", order.getId());
			Map<String, OtherFeeDetail> feeMap = BeanUtil.beanListToMap(feeList, "feetype");
			OtherFeeDetail feeDetail = feeMap.get(OtherFeeDetail.FEETYPE_E);
			expressFee = feeDetail.getFee();
			takemethod = OdiConstant.TAKEMETHOD_KUAIDI;
			OrderAddress address = daoService.getObject(OrderAddress.class, order.getTradeNo());
			if(AdminCityContant.zxsList.contains(address.getProvincecode())) {
				resMap.put("expressAddress", address.getCityname()+address.getAddress());
			}else {
				resMap.put("expressAddress", address.getProvincename()+address.getCityname()+address.getAddress());
			}
			resMap.put("expressPeople", address.getRealname());
			resMap.put("expressMobile", address.getMobile());
		}
		String priceInfo = "";
		if(odi.isOpenseat()){
			Map<String, String> descMap = VmUtils.readJsonToMap(order.getDescription2());
			priceInfo = descMap.get("座位");
		}else {
			List<BuyItem> buyItemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
			for(BuyItem bi : buyItemList){
				priceInfo = priceInfo + "," +bi.getUnitprice()+"元x" + bi.getQuantity()+"张"; 
			}
			if(StringUtils.isNotBlank(priceInfo)){
				priceInfo = priceInfo.substring(1);
			}
		}
		resMap.put("priceInfo", priceInfo);
		resMap.put("dramalogo", getMobilePath() + drama.getLimg());
		resMap.put("dramaname", odi.getDramaname());
		resMap.put("roomname", odi.getRoomname());
		resMap.put("theatrename", theatre.getRealBriefname());
		resMap.put("expressFee", expressFee);
		resMap.put("takemethod", takemethod);
		resMap.putAll(getBaseInfoMap(theatre));
		return resMap;
	}
	protected void getGoodsPriceListMap(List<GoodsPrice> goodsPriceList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(GoodsPrice goodsPrice : goodsPriceList){
			Map<String, Object> params = GewaApiGoodsHelper.getGoodsPriceData(goodsPrice);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	protected void getGoodsData(BaseGoods goods, ModelMap model, HttpServletRequest request){
		if(goods==null) return;
		Map<String, Object> resMap = getGoodsMap(goods);
		putDetail(resMap, model,request);
	}
	protected Map<String, Object> getGoodsMap(BaseGoods goods){
		Map<String, Object> resMap = GewaApiGoodsHelper.getGoodsMap(goods, getMobilePath() + goods.getLimg());
		return resMap;
	}
	//------------------------------------------------------------------------------------------------------
	//场次数据
	protected void getOpiMap(OpenPlayItem opi, ModelMap model, HttpServletRequest request){
		if(opi==null) return;
		Map<String, Object> resMap = GewaApiMovieHelper.getOpiData(opi);
		putDetail(resMap, model, request);
	}
	
	protected void getOpiListMap(List<OpenPlayItem> opiList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(OpenPlayItem opi : opiList){
			Map<String, Object> params = GewaApiMovieHelper.getOpiData(opi);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	//------------------------------------------------------------------------------------------------------
	//影片数据
	protected void getMovieDiaryMap(Diary diary, String content, ModelMap model, HttpServletRequest request){
		if(diary==null) return;
		MemberInfo member  = daoService.getObject(MemberInfo.class, diary.getMemberid());
		Map<String, Object> resMap = GewaApiBbsHelper.getMovieDiary(diary, getMobilePath() + member.getHeadpicUrl(), content);
		putDetail(resMap, model, request);
	}
	protected void getMovieDiaryListMap(List<Diary> diaryList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Diary diary : diaryList){
			MemberInfo member  = daoService.getObject(MemberInfo.class, diary.getMemberid());
			Map<String, Object> params = GewaApiBbsHelper.getMovieDiary(diary, getMobilePath() + member.getHeadpicUrl(), null);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	//------------------------------------------------------------------------------------------------------
	//哇啦数据
	protected void getCommentMap(Comment comment, ModelMap model, HttpServletRequest request){
		if(comment==null) return;
		String mobilePath = getMobilePath();
		MemberInfo member  = daoService.getObject(MemberInfo.class, comment.getMemberid());
		Map<String, Object> resMap = GewaApiBbsHelper.getComment(comment, mobilePath + member.getHeadpicUrl());
		if(StringUtils.isNotBlank(comment.getPicturename())) resMap.put("smallpicture", mobilePath + comment.getPicturename());
		if(comment.getTransferid()!=null){
			Comment tcomment = commentService.getCommentById(comment.getTransferid());
			MemberInfo tmember  = daoService.getObject(MemberInfo.class, tcomment.getMemberid());
			String tlogo = null;
			if(StringUtils.isNotBlank(tcomment.getPicturename())) tlogo = mobilePath + tcomment.getPicturename();
			resMap.putAll(GewaApiBbsHelper.getTransferMap(comment, tcomment, mobilePath + tmember.getHeadpicUrl(), tlogo));
		}
		putDetail(resMap, model, request);
	}
	protected void getCommentListMap(List<Comment> commentList, ModelMap model, HttpServletRequest request){
		putList(getCommentResMapList(commentList), model, request);
	}
	protected List<Map<String, Object>> getCommentResMapList(List<Comment> commentList){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		String mobilePath = getMobilePath();
		for(Comment comment : commentList){
			MemberInfo member  = daoService.getObject(MemberInfo.class, comment.getMemberid());
			if(member!=null){
				Map<String, Object> params = GewaApiBbsHelper.getComment(comment, mobilePath + member.getHeadpicUrl());
				if(StringUtils.isNotBlank(comment.getPicturename())) params.put("smallpicture", mobilePath + comment.getPicturename());
				if(comment.getTransferid()!=null){
					Comment tcomment = commentService.getCommentById(comment.getTransferid());
					if(tcomment!=null){
						MemberInfo tmember  = daoService.getObject(MemberInfo.class, tcomment.getMemberid());
						if(tmember!=null){
							String tpic = StringUtils.isNotBlank(tcomment.getPicturename())?(mobilePath + tcomment.getPicturename()):"";
							params.putAll(GewaApiBbsHelper.getTransferMap(comment, tcomment, mobilePath+tmember.getHeadpicUrl(), tpic));
						}
					}
				}
				resMapList.add(params);
			}
		}
		return resMapList;
	}
	//用户登录
	protected void getLoginMap(Member member, ModelMap model, HttpServletRequest request){
		if(member==null) return;
		ErrorCode<String> encodeResult = memberService.getAndSetMemberEncode(member);
		if(!encodeResult.isSuccess()) return;
		MemberInfo memberInfo  = daoService.getObject(MemberInfo.class, member.getId());
		Map<String, Object> resMap = GewaApiMemberHelper.getLoginMember(member, encodeResult.getRetval(), getMobilePath() + memberInfo.getHeadpicUrl());
		ErrorCode<OpenMember> omCode = getOpenMember(memberInfo);
		if(omCode.isSuccess()){
			OpenMember openMember = omCode.getRetval();
			if(StringUtils.isNotBlank(openMember.getNickname())){
				resMap.put("nickname", openMember.getNickname());
			}
		}
		model.put("root", "member");
		putDetail(resMap, model, request);
	}
	//用户登录
	protected Map<String, Object> getLoginMap(Member member, MemberInfo memberInfo){
		ErrorCode<String> encodeResult = memberService.getAndSetMemberEncode(member);
		Map<String, Object> resMap = GewaApiMemberHelper.getLoginMember(member, encodeResult.getRetval(), getMobilePath() + memberInfo.getHeadpicUrl());
		return resMap;
	}
	//用户登录
	protected Map<String, Object> getMemberMap(Member member, MemberInfo info){
		Map<String, Object> resMap = GewaApiMemberHelper.getLoginMember(member, null, getMobilePath() + info.getHeadpicUrl());
		if(info.getBirthday()!=null && DateUtil.isValidDate((info.getBirthday()+""))){
			resMap.put("birthday", info.getBirthday());
		}
		resMap.put("sign", info.getSign());
		resMap.put("pointvalue", info.getPointvalue());
		resMap.put("sex", StringUtils.isBlank(info.getSex())?"女":"男");
		return resMap;
	}
	protected void getPictureListMap(List<Picture> pictureList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(Picture picture : pictureList){
			Map<String, Object> params = GewaApiHelper.getPicture(picture, getMobilePath() + picture.getPicturename());
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "pictureList");
		model.put("nextroot", "picture");
	}
	
	protected void getPhoneActivityMap(PhoneActivity activity, ModelMap model, HttpServletRequest request){
		Map<String, Object> resMap = GewaApiBbsHelper.getPhoneActivity(activity, getMobilePath()+activity.getLogo());
		putDetail(resMap, model, request);
		model.put("root", "phoneActivity");
	}
	protected void getPhoneActivityListMap(List<PhoneActivity> activityList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(PhoneActivity activity : activityList){
			Map<String, Object> params = GewaApiBbsHelper.getPhoneActivity(activity, getMobilePath()+activity.getLogo());
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "phoneActivityList");
		model.put("nextroot", "phoneActivity");
	}
	protected void getCardListMap(List<ElecCard> cardList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(ElecCard card : cardList){
			Map<String, Object> params = GewaApiOrderHelper.getCardMap(card);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "cardList");
		model.put("nextroot", "card");
	}
	protected void getMovieVideoListMap(List<MovieVideo> movieVideoList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(MovieVideo mv : movieVideoList){
			Map<String, Object> params = GewaApiMovieHelper.getMovieVideoData(mv);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
	}
	
	protected void getActivityListMap(List<RemoteActivity> activityList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		String mobilePath = getMobilePath();
		for(RemoteActivity activity : activityList){
			MemberInfo info = daoService.getObject(MemberInfo.class, activity.getMemberid());
			Map<String, Object> params = GewaApiBbsHelper.getActivity(activity, mobilePath + info.getHeadpicUrl(), mobilePath + activity.getLogo());
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "activityList");
		model.put("nextroot", "activity");
	}
	protected Map<String, Object> getActivityMap(RemoteActivity activity){
		MemberInfo info = daoService.getObject(MemberInfo.class, activity.getMemberid());
		String mobilePath = getMobilePath();
		Map<String, Object> resMap = GewaApiBbsHelper.getActivity(activity, mobilePath + info.getHeadpicUrl(), mobilePath + activity.getLogo());
		return resMap;
	}
	protected void getSysMsgListMap(List<SysMessageAction> sysMsgList, ModelMap model, HttpServletRequest request){
		List<Map<String, Object>> resMapList = new ArrayList<Map<String,Object>>();
		for(SysMessageAction sysMsg : sysMsgList){
			Map<String, Object> params = GewaApiMemberHelper.getMovieData(sysMsg);
			resMapList.add(params);
		}
		putList(resMapList, model, request);
		model.put("root", "sysMsgList");
		model.put("nextroot", "sysMsg");
	}
	
	protected ErrorCode<OpenMember> getOpenMember(MemberInfo memberInfo){
		if(memberInfo==null){
			return ErrorCode.getFailure("用户不存在！");
		}
		Map<String, String> otherMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
		if(otherMap.containsKey(MemberConstant.OPENMEMBER)){
			List<OpenMember> openMemberList = daoService.getObjectListByField(OpenMember.class, "memberid", memberInfo.getId());
			if(openMemberList.size()>0){
				return ErrorCode.getSuccessReturn(openMemberList.get(0));
			}
		}
		return ErrorCode.getFailure("非第三方用户登陆！");
	}
	
	protected Map<String, Object> getWeixin2WalaMap(Weixin2Wala wala){
		String mobilePath = getMobilePath();
		Map<String, Object> resMap = GewaApiBbsHelper.getWeixin2Wala(wala, mobilePath + wala.getPicUrl());
		return resMap;
	}
	protected ErrorCode validGewaOrder(GewaOrder order, Member member){
		if(order == null) {
			return ErrorCode.getFailure("订单不存在！");
		}
		if(!order.getMemberid().equals(member.getId())){ 
			return ErrorCode.getFailure("不能操作他人订单！");
		}
		return ErrorCode.SUCCESS;
	}
	protected ErrorCode validPaidGewaOrder(GewaOrder order, Member member){
		ErrorCode code = validGewaOrder(order, member);
		if(!code.isSuccess()){
			return ErrorCode.getFailure(code.getMsg());
		}
		if (!order.isAllPaid()){ 
			return ErrorCode.getFailure("非支付成功的订单！");
		}
		return ErrorCode.SUCCESS;
	}
	protected int isAliMember(MemberInfo memberInfo){
		if(memberInfo==null) return 0;
		return StringUtils.equals(memberInfo.getRegfrom(), MemberConstant.SOURCE_ALIPAY)?1:0;
	}
	/**
	 * 获取订单部分公用信息
	 * @param model
	 * @param order
	 */
	protected void getOrderCommon(ModelMap model,GewaOrder order,boolean isSuccessOrder){
		model.put("tradeno", order.getTradeNo());
		if(isSuccessOrder){
			model.put("status",ApiConstant.ORDER_STATUS_MAP.get(OrderConstant.STATUS_PAID_SUCCESS));
		}else{
			model.put("status",ApiConstant.ORDER_STATUS_MAP.get(order.getStatus()));
		}
		model.put("due", order.getDue());
		model.put("totalDiscount", order.getDiscount());
		model.put("discountAmount", order.getDiscount());
		model.put("totalAmount", order.getTotalAmount());
	}
	protected boolean isBooingActivityGoods(ActivityGoods goods, String appVersion){
		if(appVersion.compareTo(AppConstant.MOVIE_APPVERSION_4_6)<0){
			if(goods.hasBooking()){
				return true;
			}
			return false;
		}else {
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			if(goods.getTotime().after(cur) && StringUtils.equals(goods.getStatus(), Status.Y)){
				return true;
			}
			return false;
		}
	}
	protected AsConfig getAsConfig(ApiUser partner, HttpServletRequest request){
		String appsource = request.getParameter("appSource");
		String appVersion = request.getParameter("appVersion");
		String hql = "from AsConfig where partnerid=? and appsource=? and appVersion=?";
		List<AsConfig> asList = hibernateTemplate.find(hql, partner.getId(), appsource, appVersion);
		if(asList.size()>0){
			return asList.get(0);
		}
		hql = "from AsConfig where partnerid=? and appsource=? and appVersion is null";
		asList = hibernateTemplate.find(hql, partner.getId(), appsource);
		if(asList.size()>0){
			return asList.get(0);
		}
		return null;
	}
	protected List<Long> getMemberSdid(Member member){
		TempMember tm = daoService.getObjectByUkey(TempMember.class, "memberid", member.getId());
		List<Long> result = new ArrayList<Long>();
		if(tm!=null && StringUtils.equals(tm.getStatus(), Status.Y)){
			List<SpCode> scList = daoService.getObjectListByField(SpCode.class, "memberid", member.getId());
			for(SpCode sc : scList){
				if(sc.getUsedcount()==0){
					result.add(sc.getSdid());
				}
			}
		}
		return result;
	}
}
