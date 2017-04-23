package com.gewara.web.action.drama;

import java.io.Serializable;
import java.util.HashMap;
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

import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.json.PageView;
import com.gewara.model.BaseObject;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.HeadInfo;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.service.bbs.CommuService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.PageCacheService;
import com.gewara.untrans.PageParams;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.RelatedHelper;
import com.gewara.util.WebUtils;
import com.gewara.xmlbind.bbs.Comment;
@Controller
public class DramaIndexPageCityController extends BaseDramaController{

	@Autowired@Qualifier("pageCacheService")
	private PageCacheService pageCacheService;
	public void setPageCacheService(PageCacheService pageCacheService){
		this.pageCacheService = pageCacheService;
	}
	@Autowired@Qualifier("commuService")
	private CommuService commuService;
	public void setCommuService(CommuService commuService){
		this.commuService = commuService;
	}
	//其他城市话剧首页
	@RequestMapping("/drama/city/index.xhtml")
	public String cityDramaIndex(ModelMap model, HttpServletRequest request, HttpServletResponse response){
		String citycode = WebUtils.getAndSetDefault(request, response);
		if(pageCacheService.isUseCache(request)){//先使用缓存
			PageParams params = new PageParams();
			PageView pageView = pageCacheService.getPageView(request, "/drama/city/index.xhtml", params, citycode);
			if(pageView != null){
				model.put("pageView", pageView);
				return "pageView.vm";
			}
		}
		RelatedHelper rh = new RelatedHelper();
		model.put("relatedHelper", rh);
		getHeadData(citycode);
		commend1(citycode, model);
		commend2(rh, citycode, model);
		commend3(rh, citycode, model);
		commend5(rh, citycode, model);
		commend6(rh, citycode, model);
		getCommentList(model);
		commend7(rh, citycode, model);
		commend9(rh, citycode, model);
		commend10(rh, citycode, model);
		commend11(citycode, model);
		commend12(rh, citycode, model);
		commend13(rh, citycode, model);
		commend14(citycode, model);
		return "/drama/nanjing/index.vm";
	}
	//首页信息Banner
	private void commend1(String citycode, ModelMap model){
		List<GewaCommend> infoList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_NEWS, null, null, true, 0, 8);
		model.put("infoList", infoList);
	}
	//话剧资讯5条
	private void commend2(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramaNewsList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_TOPRIGHTNEWS, null, null, true, 0, 5);
		commonService.initGewaCommendList("dramaNewsList", rh, dramaNewsList);
		model.put("dramaNewsList", dramaNewsList);
	}
	//热演话剧4条
	private void commend3(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramaList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_HOTSELL, null, null, true, 0, 4);
		commonService.initGewaCommendList("dramaList", rh, dramaList);
		model.put("dramaList", dramaList);
		Map<Long, Boolean> isBookingMap = new HashMap<Long, Boolean>();
		Map<Long, List<Integer>> priceMap = new HashMap<Long, List<Integer>>();
		Map<Long, String> summaryMap = new HashMap<Long, String>();
		Map<Long, Boolean> isOpenseatMap = new HashMap<Long, Boolean>();
		if(dramaList != null && dramaList.size() > 0){
			for(GewaCommend gc : dramaList){
				List<OpenDramaItem> openDramaItemList = dramaService.getOpenDramaItemListBydramaid(citycode, gc.getRelatedid());
				isOpenseatMap.put(gc.getRelatedid(), false);
				for(OpenDramaItem odi : openDramaItemList){
					if(odi.isOpenseat()){
						isOpenseatMap.put(gc.getRelatedid(), true);
						break;
					}
				}
				boolean isBooking = dramaPlayItemService.isBookingByDramaId(gc.getRelatedid());
				isBookingMap.put(gc.getRelatedid(), isBooking);
				priceMap.put(gc.getRelatedid(), dramaPlayItemService.getPriceList(null, gc.getRelatedid(), DateUtil.getCurFullTimestamp(), null, isBooking));
				summaryMap.put(gc.getRelatedid(), gc.getSummary());
			}
		}
		model.put("summaryMap", summaryMap);
		model.put("isBookingMap", isBookingMap);
		model.put("priceMap", priceMap);
		model.put("isOpenseatMap", isOpenseatMap);
	}
	
	//推荐活动4条
	private void commend5(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> activityList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_ACTIVITY, null, null, true, 0, 4);
		commonService.initGewaCommendList("activityList", rh, activityList);
		model.put("activityList", activityList);
	}
	//剧评2条
	private void commend6(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramaHotDiaryList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_HOTDIARY, null, null, true, 0, 2);
		commonService.initGewaCommendList("dramaHotDiaryList", rh, dramaHotDiaryList);
		model.put("dramaHotDiaryList", dramaHotDiaryList );
		List<? extends BaseObject> baseObjectList = rh. getGroupIndexList("dramaHotDiaryList", 1);
		List<Serializable> categoryIdList = BeanUtil.getBeanPropertyList(baseObjectList, Serializable.class, "categoryid", true);
		relateService.addRelatedObject(1, "categoryIdList", rh, TagConstant.TAG_DRAMA, categoryIdList);
	}
	//哇啦10
	private void getCommentList(ModelMap model){
		List<Comment> commentList = commentService.getCommentListByTags(new String[]{"drama"}, null, true, 0, 10);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		model.put("commentList", commentList);
		Map<Long, Comment> tranferCommentMap = new HashMap<Long, Comment>();// 转载评论
		for (Comment comment : commentList) {
			if (comment.getTransferid() != null) {
				Comment c = commentService.getCommentById(comment.getTransferid());
				if (c != null && StringUtils.isNotBlank(c.getBody())) {
					tranferCommentMap.put(c.getId(), c);
				}
			}
		}
		model.put("tranferCommentMap", tranferCommentMap);
	}
	//论坛6条
	private void commend7(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramaDiaryList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_DIARY, null, null, true, 0, 6);
		commonService.initGewaCommendList("dramaDiaryList", rh, dramaDiaryList);
		model.put("dramaDiaryList", dramaDiaryList);
	}
	//场馆3条
	private void commend9(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> theatreList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_THEATRE, null, null, true, 0, 3);
		commonService.initGewaCommendList("theatreList", rh, theatreList);
		model.put("theatreList", theatreList);
		Map<Long, Integer> curDramaCountMap = new HashMap<Long, Integer>();
		for(GewaCommend commend : theatreList){
			Long theatreid = commend.getRelatedid();
			curDramaCountMap.put(theatreid, dramaService.getCurPlayDramaCount(theatreid));
		}
		model.put("curDramaCountMap", curDramaCountMap);
	}
	
	//圈子3条
	private void commend10(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> commuList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_DRAMACOMMU, null, null, true, 0, 3);
		commonService.initGewaCommendList("commuList", rh, commuList);
		model.put("commuList", commuList);
		Map<Long, Integer> commuMemberMap = new HashMap<Long, Integer>();
		for(GewaCommend commend : commuList){
			Long commuid = commend.getRelatedid();
			commuMemberMap.put(commuid, commuService.getCommumemberCount(commuid, null));
		}
		model.put("commuMemberMap", commuMemberMap);
	}
	//广告4条
	private void commend11(String citycode, ModelMap model){
		List<GewaCommend> partnerList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_PARTNER, null, null, true, 0, 4);
		model.put("partnerList", partnerList);
	}

	//人物剧社7条
	private void commend12(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramastarList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_STAR, null, null, true, 0, 7);
		commonService.initGewaCommendList("dramastarList", rh, dramastarList);
		model.put("dramastarList", dramastarList);
		Map<Long, Integer> starDramaCountMap = new HashMap<Long, Integer>();
		for(GewaCommend commend : dramastarList){
			Long starId = commend.getRelatedid();
			starDramaCountMap.put(starId, dramaToStarService.getDramaCountByStarid(starId, true));
		}
		model.put("starDramaCountMap", starDramaCountMap);
	}
	
	//经典剧目4条
	private void commend13(RelatedHelper rh, String citycode, ModelMap model){
		List<GewaCommend> dramaHotList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_HOTDRAMA, null, null, true, 0, 4);
		commonService.initGewaCommendList("dramaHotList", rh, dramaHotList);
		model.put("dramaHotList", dramaHotList);
	}
	
	//经典专题4条
	private void commend14(String citycode, ModelMap model){
		List<GewaCommend> hotsubjectList = commonService.getGewaCommendList(citycode, SignName.DRAMAINDEX_HOTSUBJECT, null, null, true, 0, 4);
		model.put("hotsubjectList", hotsubjectList);
	}
	//头部套头
	private Map getHeadData(String citycode){
		Map model = new HashMap();
		List<GewaCommend> gcHeadList = commonService.getGewaCommendList(citycode, null, SignName.INDEX_HEADINFO, null, HeadInfo.TAG, true, true, 0, 1);
		HeadInfo headInfo = null;
		if(!gcHeadList.isEmpty()){
			headInfo = daoService.getObject(HeadInfo.class, gcHeadList.get(0).getRelatedid());
			model.put("headInfo",BeanUtil.getBeanMapWithKey(headInfo, "css", "logosmall", "logobig", "link"));
		}
		return model;
	}
}
