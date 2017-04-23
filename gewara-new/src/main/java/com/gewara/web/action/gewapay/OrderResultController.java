package com.gewara.web.action.gewapay;

import java.util.ArrayList;
import java.util.Collections;
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

import com.gewara.constant.DramaConstant;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.helper.order.GewaOrderHelper;
import com.gewara.model.agency.TrainingGoods;
import com.gewara.model.bbs.DiaryBase;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.content.Advertising;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.drama.TheatreProfile;
import com.gewara.model.drama.TheatreRoom;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.goods.SportGoods;
import com.gewara.model.goods.TicketGoods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.CinemaProfile;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.PubSaleOrder;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.FriendInfo;
import com.gewara.model.user.HiddenMember;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.pay.BackConstant;
import com.gewara.service.PlaceService;
import com.gewara.service.SynchService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.DiaryService;
import com.gewara.service.content.AdService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.service.drama.DramaService;
import com.gewara.service.member.FriendService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.sport.SportService;
import com.gewara.service.ticket.OpenPlayService;
import com.gewara.support.ErrorCode;
import com.gewara.support.MultiPropertyComparator;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.CooperateService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.gym.SynchGymService;
import com.gewara.util.BeanUtil;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.gewara.xmlbind.gym.RemoteGym;

@Controller
public class OrderResultController extends BasePayController {
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	@Autowired@Qualifier("cooperateService")
	private CooperateService cooperateService;
	public void setCooperateService(CooperateService cooperateService) {
		this.cooperateService = cooperateService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("diaryService")
	private DiaryService diaryService;
	public void setDiaryService(DiaryService diaryService) {
		this.diaryService = diaryService;
	}
	@Autowired@Qualifier("friendService")
	private FriendService friendService;
	public void setFriendService(FriendService friendService) {
		this.friendService = friendService;
	}
	@Autowired@Qualifier("dramaOrderService")
	protected DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("dramaService")
	protected DramaService dramaService;
	public void setDramaService(DramaService dramaService) {
		this.dramaService = dramaService;
	}
	@Autowired@Qualifier("openPlayService")
	protected OpenPlayService openPlayService;
	public void setOpenPlayService(OpenPlayService openPlayService) {
		this.openPlayService = openPlayService;
	}
	@Autowired@Qualifier("sportService")
	protected SportService sportService;
	public void setSportService(SportService sportService){
		this.sportService = sportService;
	}

	@Autowired@Qualifier("placeService")
	protected PlaceService placeService;
	public void setPlaceService(PlaceService placeService) {
		this.placeService = placeService;
	}
	
	@Autowired@Qualifier("adService")
	private AdService adService;
	
	@Autowired@Qualifier("synchGymService")
	protected SynchGymService synchGymService;
	public void setSynchGymService(SynchGymService synchGymService) {
		this.synchGymService = synchGymService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	
	@RequestMapping("/gewapay/selfTicket.xhtml")
	public String selfTicket(String tradeNo, String specialComents, ModelMap model){
		Member member = getLogonMember();
		ErrorCode code = synchService.selfTicket(tradeNo, member, specialComents);
		if(!code.isSuccess()){
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/gewapay/orderResult.xhtml")
	public String orderResult(String tradeNo, Long orderId, ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		GewaOrder gorder = null;
		if(orderId!=null){
			gorder = daoService.getObject(GewaOrder.class, orderId);
		}else{
			gorder = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		}
		if(gorder == null) return show404(model, "订单信息不存在！");
		Member member = getLogonMember();
		if(member==null) return forwardMessage(model, "请登录！");
		if (!gorder.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		model.put("logonMember", member);
		model.put("order", gorder);
		if(gorder.isAllPaid()){
			model.put("success", true);
			model.put("fee", gorder.getDue());
		}else{
			model.put("success", false);
			model.put("fee", gorder.getAlipaid() + gorder.getGewapaid());
		}
		String citycode = gorder.getCitycode();
		if(StringUtils.isBlank(citycode)) citycode = WebUtils.getAndSetDefault(request, response);
		processSpdiscount(gorder,model);
		if (gorder instanceof TicketOrder) {
			String url = "gewapay/ticket/wide_orderResult.vm";
			addTicketOrderData(member, (TicketOrder)gorder, model);
			GewaConfig gc = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SUBJECT_DOUBLE_ELEVEN);
			if(StringUtils.isNotBlank(gc.getContent())){
				List<Long> speDiscountId = BeanUtil.getIdList(gc.getContent(), ","); 
				List<Discount> list = paymentService.getOrderDiscountList(gorder);
				if(!list.isEmpty()){
					for (Discount discount : list) {
						if(speDiscountId.contains(discount.getRelatedid()) && StringUtils.equals(discount.getTag(), PayConstant.DISCOUNT_TAG_PARTNER)){
							url = "gewapay/ticket/wide_db11_orderResult.vm";
							break;
						}
					}
				}
			}
			return url;// 购票订单
		} else if(gorder instanceof GoodsOrder){
			GoodsOrder goodsorder = (GoodsOrder)gorder;
			BaseGoods goods = daoService.getObject(BaseGoods.class, goodsorder.getGoodsid());
			model.put("goods", goods);
			if(goods instanceof SportGoods){
				SportGoods sportGoods = (SportGoods)goods;
				Sport sport = daoService.getObject(Sport.class, sportGoods.getRelatedid());
				SportItem item = daoService.getObject(SportItem.class, sportGoods.getItemid());
				//添加好友备注列表
				List<FriendInfo> friendInfoList = friendService.getFriendInfoListByAddMemberidAndMemberid(member.getId(), null);
				model.put("friendInfoList", friendInfoList);
				//hiddenMember列表添加的好友
				List<HiddenMember> hiddenMemberList = friendService.getHiddenMemberListByMemberid(member.getId());
				//卖品
				List<Goods> goodsList = goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH_SPORT, sport.getId(), true, true, true, "goodssort", true, false);
				List<Map> memberList = untransService.getPayMemberListByTagAndId(TagConstant.TAG_SPORT, sport.getId(), 0, 9);
				List<MemberInfo> memberInfoList = new ArrayList<MemberInfo>();
				for(Map memberMap : memberList){
					memberInfoList.add(daoService.getObject(MemberInfo.class, Long.parseLong(memberMap.get("memberid") + "")));
				}
				model.put("hiddenMemberList", hiddenMemberList);
				model.put("sport", sport);
				model.put("item", item);
				model.put("goodsList", goodsList);
				model.put("memberInfoList", memberInfoList);
				model.put("memberList", memberList);
				model.put("sportorder", goodsorder);
				return "gewapay/sport/goodsOrderResult.vm";
			}else if(goods instanceof ActivityGoods){
				ErrorCode<RemoteActivity> code2 = synchActivityService.getRemoteActivity(goods.getRelatedid());
				if(code2.isSuccess()) model.put("activity", code2.getRetval());
				return "gewapay/goods/activityOrderResult.vm";
			}else if(goods instanceof TicketGoods){
				TicketGoods ticketGoods = (TicketGoods) goods;
				Object relate = relateService.getRelatedObject(ticketGoods.getTag(), ticketGoods.getRelatedid());
				model.put("relate", relate);
				Object category = relateService.getRelatedObject(ticketGoods.getCategory(), ticketGoods.getCategoryid());
				model.put("category", category);
				return "gewapay/goods/ticketGoodsOrderResult.vm";
			}else if(goods instanceof TrainingGoods){
				return "gewapay/sport/agency/wide_orderResult.vm";
			}
			return "gewapay/goods/goodsOrderResult.vm";
		} else if(gorder instanceof SportOrder){
			SportOrder sportorder = (SportOrder)gorder;
			model.put("sportorder", sportorder);
			Sport sport = daoService.getObject(Sport.class, sportorder.getSportid());
			SportItem item = daoService.getObject(SportItem.class, sportorder.getItemid());
			OpenTimeTable ott = daoService.getObject(OpenTimeTable.class, sportorder.getOttid());
			List<GewaCommend> gcPictureList = commonService.getGewaCommendList(citycode , SignName.SPORTORDER_PICTUE, null, null, true, 0, 2);
			model.put("gcPictureList", gcPictureList);
			model.put("sport", sport);
			model.put("item", item);
			model.put("ott", ott);
			return "gewapay/sport/wide_orderResult.vm";
		} else if(gorder instanceof DramaOrder){
			DramaOrder dramaOrder = (DramaOrder)gorder;
			OpenDramaItem item = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", dramaOrder.getDpid(), true);
			addDramaOrderResultInfo(dramaOrder,item, model);
			if (item.getTopicid() != null) {
				DiaryBase topic = diaryService.getDiaryBase(item.getTopicid());
				model.put("topic", topic);
				if(topic != null)
					model.put("diaryBody", blogService.getDiaryBody(topic.getId()));
			}
			List<BuyItem> buyList = daoService.getObjectListByField(BuyItem.class, "orderid", dramaOrder.getId());
			List<OpenDramaItem> itemList = dramaOrderService.getOpenDramaItemList(item, buyList);
			Collections.sort(itemList, new MultiPropertyComparator(new String[]{"playtime"}, new boolean[]{true}));
			if(!itemList.isEmpty()){
				item = itemList.get(0);
			}
			model.put("item", item);
			return "gewapay/drama/wide_orderResult.vm";
		}else if(gorder instanceof GymOrder){
			GymOrder gymOrder = (GymOrder)gorder;
			ErrorCode<RemoteGym> code = synchGymService.getRemoteGym(gymOrder.getGymid(), true);
			if(code.isSuccess()){
				model.put("gym", code.getRetval());
			}
			return "gewapay/gym/orderGymResult.vm";
		}else if(gorder instanceof PubSaleOrder){
			PubSaleOrder porder = (PubSaleOrder)gorder;
			PubSale sale = daoService.getObject(PubSale.class, porder.getPubid());
			model.put("sale", sale);
			return "exchange/pubsale/pubSaleOrderResult.vm";
		}else if(gorder instanceof MemberCardOrder){
			model.put("order", gorder);
			return "gewapay/sport/wide_orderResultByMemberCard.vm";
		}
		WebUtils.setCitycode(request, citycode, response);
		return "gewapay/otherOrderResult.vm";
	}
	@RequestMapping("/gewapay/payResultProxy.xhtml")
	public String payecoDNAPay(String tradeNo, ModelMap model) {
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo);
		Member member = getLogonMember();
		if(member==null) return forwardMessage(model, "请登录！");
		if (!order.getMemberid().equals(member.getId())) return show404(model, "不能修改他人的订单！");
		model.put("tradeNo", tradeNo);
		model.put("order", order);
		if(order.getStatus().startsWith(OrderConstant.STATUS_PAID)){
			return "redirect:/gewapay/orderResult.xhtml";
		}
		if(StringUtils.startsWith(order.getPaymethod(), PaymethodConstant.PAYMETHOD_UMPAY)){
			return "gewapay/umPayMsg.vm";
		}else if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_PAYECO_DNA)){
			return "gewapay/payecoDNAPayMsg.vm";
		}
		return "redirect:/gewapay/orderResult.xhtml";
	}
	private void processSpdiscount(GewaOrder order,ModelMap model){
		List<Discount> discountList = paymentService.getOrderDiscountList(order);
		for(Discount discount : discountList){
			SpecialDiscount sd = daoService.getObject(SpecialDiscount.class,discount.getRelatedid());
			if(sd != null){
				if(order.getAddtime().after(sd.getTimefrom()) && order.getAddtime().before(sd.getTimeto()) && sd.getDrawactivity() != null && SpecialDiscount.REBATES_CARDD.equals(sd.getRebatestype())){
					if(sd.getBindDrawCardNum() != null && sd.getBindDrawCardNum() > 0){
						model.put("specialDiscount", sd);
						return;
					}
				}
			}
		}
	}
	
	private void addDramaOrderResultInfo(DramaOrder order, OpenDramaItem item, ModelMap model){
		Member member = daoService.getObject(Member.class, order.getMemberid());
		Theatre theatre = daoService.getObject(Theatre.class, order.getTheatreid());
		TheatreProfile profile = daoService.getObject(TheatreProfile.class, theatre.getId());
		Drama drama = daoService.getObject(Drama.class, order.getDramaid());
		if(profile!=null && profile.getTopicid()!=null){
			model.put("topic",  diaryService.getDiaryBase(profile.getTopicid()));
		}
		TheatreRoom section = daoService.getObject(TheatreRoom.class, item.getRoomid());
		Goods goods = goodsService.getGoodsByTagAndRelatedid(Goods.class, GoodsConstant.GOODS_TAG_BMH_THEATRE, order.getTheatreid(), true, true, true);
		model.put("goods", goods);
		model.put("item", item);
		model.put("drama", drama);
		model.put("section", section);
		model.put("theatre", theatre);
		model.put("profile", profile);
		model.put("member", member);
		model.put("dramaTypeMap", DramaConstant.dramaTypeMap);
	}

	private boolean isValidBack(GewaOrder order, ModelMap model){
		if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_CHINAPAY2)){
			if(StringUtils.indexOf(order.getOtherinfo(), BackConstant.shbankcardno) != -1){
				ErrorCode<String> code = cooperateService.checkShbankBack(order.getId(), order.getOtherinfo(), "true");
				model.put("bankname", BackConstant.SHBACK);
				return code.isSuccess();
			}else if(StringUtils.indexOf(order.getOtherinfo(), BackConstant.xybankcardno) != -1){
				ErrorCode<String> code = cooperateService.checkXybankBack(order.getId(), order.getOtherinfo(), "true");
				model.put("bankname", BackConstant.YXBACK);
				return code.isSuccess();
			}
		}
		return true;
	}
	
	private void addTicketOrderData(Member member, TicketOrder order, ModelMap model){
		OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", order.getMpid(), true);
		model.put("opi", opi);
		if (opi.getTopicid() != null) {
			DiaryBase topic = diaryService.getDiaryBase(opi.getTopicid());
			model.put("topic", topic);
			if(topic != null)
				model.put("diaryBody", blogService.getDiaryBody(topic.getId()));
		}
		CinemaProfile profile = daoService.getObject(CinemaProfile.class, order.getCinemaid());
		if (profile != null) {
			model.put("takemethod", profile.getTakemethod());
			if(profile.hasDefinePaper()){
				GewaConfig gconfig = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUSTOM_PAPER);
				model.put("gconfig", gconfig);
			}
		} else {
			model.put("takemethod", "U");// 未确定
		}
		Goods goods = goodsService.getGoodsByTagAndRelatedid(Goods.class, GoodsConstant.GOODS_TAG_BMH, order.getCinemaid(), true, true, true);
		model.put("goods", goods);
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		model.put("itemList", itemList);
		model.put("GewaOrderHelper", new GewaOrderHelper());
		model.put("profile", profile);
		Cinema cinema=daoService.getObject(Cinema.class,order.getCinemaid());
		model.put("cinema",cinema);
		Movie movie=daoService.getObject(Movie.class, order.getMovieid());
		model.put("movie",movie);
		model.put("member", member);
		List<Goods> goodsList = goodsService.getGoodsList(Goods.class, GoodsConstant.GOODS_TAG_BMH, opi.getCinemaid(), true, true, true, "goodssort", true, false);
		model.put("goodsList", goodsList);
		model.put("isvalid", isValidBack(order,model));
		
		Advertising ad = adService.getFirstAdByPostionTag("ticketsuccess");
		model.put("ad", ad);
	}
}
