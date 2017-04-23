package com.gewara.web.action.subject;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.TagConstant;
import com.gewara.model.content.News;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.user.Member;
import com.gewara.service.content.NewsService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.untrans.CommentService;
import com.gewara.util.BeanUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;

//冰河世纪4专题
@Controller
public class IceAge4ProxyController extends AnnotationController {
	@Autowired@Qualifier("newsService")
	private NewsService newsService;
	public void setNewsService(NewsService newsService) {
		this.newsService = newsService;
	}
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	public void setOrderQueryService(OrderQueryService orderQueryService) {
		this.orderQueryService = orderQueryService;
	}
	@Autowired@Qualifier("drawActivityService")
	private DrawActivityService drawActivityService;
	public void setDrawActivityService(DrawActivityService drawActivityService) {
		this.drawActivityService = drawActivityService;
	}
	@Autowired@Qualifier("commentService")
	protected CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService){
		this.goodsService = goodsService;
	}
	
	@RequestMapping("/subject/proxy/iceage4/getNewsList.xhtml")
	public String getNewsList(Long mid, ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		List<News> newsList = newsService.getNewsList(citycode, TagConstant.TAG_MOVIE, mid, null, 0, 8);
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(BeanUtil.getBeanMapList(newsList, false)));
	}
	@RequestMapping("/subject/proxy/iceage4/getGoods.xhtml")
	public String getGoods(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "找不到卖品！");
		return showJsonSuccess(model, JsonUtils.writeObjectToJson(BeanUtil.getBeanMap(goods, false)));
	}
	@RequestMapping("/subject/proxy/iceage4/getGoodsSale.xhtml")
	public String getGoodsSale(Long gid, ModelMap model){
		BaseGoods goods = daoService.getObject(BaseGoods.class, gid);
		if(goods == null) return showJsonError(model, "找不到卖品！");
		Integer gCount = goodsService.getBuyGoodsCount(Goods.class, gid, goods.getAddtime(), null, null);
		return showJsonSuccess(model, gCount+"");
	}
	
	@RequestMapping("/subject/ajax/iceage4/checkTicket.xhtml")
	public String checkTicket(Long mid, @CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid, HttpServletRequest request, ModelMap model){
		Member member = loginService.getLogonMemberBySessid(WebUtils.getRemoteIp(request), sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		int count = orderQueryService.getMemberOrderCountByMemberid(member.getId(), mid);
		if(count == 0) return showJsonError(model, "unTicket");
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/subject/flash/ice.xhtml")
	@ResponseBody
	public String ice(){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", "iceage4", true);
		Integer count = 0;
		List<Prize> prizeList = drawActivityService.getAvailablePrizeList(da.getId(), 0);
		for(Prize prize : prizeList){
			if(!StringUtils.equals(prize.getPtype(), "empty")){
				count += (prize.getPnumber() - prize.getPsendout());
			}
		}
		String result = "cj=" + count;
		return result;
	}
}
