package com.gewara.web.action.inner.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.content.Picture;
import com.gewara.model.movie.GrabTicketMpi;
import com.gewara.model.movie.GrabTicketSubject;
import com.gewara.model.pay.PubSale;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.Point;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.member.PointService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.PictureUtil;
import com.gewara.web.action.api.BaseApiController;
import com.gewara.xmlbind.activity.RemoteActivity;
@Controller
public class ApiActivityController extends BaseApiController{
	@Autowired@Qualifier("commonService")
	private CommonService commonService;
	
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	
	@Autowired@Qualifier("pointService")
	private PointService pointService;
	
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	
	/**
	 * 获取推荐的活动id列表
	 * @param citycode 城市代码
	 * @param signname 推荐名称
	 * @param isClose 活动是否结束
	 * @param from 页码
	 * @param maxnum 每页显示数
	 * @param model
	 * @return
	 */
	@RequestMapping("/inner/activity/getCommendActivityIds.xhtml")
	public String getCommendActivityIds(
			@RequestParam(defaultValue = "310000", value = "citycode") String citycode,
			@RequestParam(value = "signname")String signname,
			@RequestParam(value = "isClose")boolean isClose,
			@RequestParam(defaultValue = "0", value = "from") Integer from,
			@RequestParam(defaultValue = "10", value = "maxnum") Integer maxnum,
			ModelMap model){
		if(maxnum > 20) maxnum = 20;
		List<GewaCommend> commendIdList = commonService.getGewaCommendList(citycode, signname, null, null, true, from, maxnum);
		List<Long> idList = BeanUtil.getBeanPropertyList(commendIdList, Long.class, "relatedid", true);
		if(isClose){
			ErrorCode<List<RemoteActivity>> code = synchActivityService.getRemoteActivityListByIds(idList);
			if(code.isSuccess() && code.getRetval() != null){
				for(RemoteActivity activity : code.getRetval()){
					if(activity.isOver2()){
						idList.remove(activity.getId());
					}
				}
			}
			/*for (GewaCommend gewaCommend : commendIdList) {
				RemoteActivity activity = null;
				ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(gewaCommend.getRelatedid());
				if(code.isSuccess()){
					activity = code.getRetval();
				}
				if(activity != null){
					if(activity.isOver2()){
						idList.remove(gewaCommend.getRelatedid());
					}
				}else {
					idList.remove(gewaCommend.getRelatedid());
				}
			}*/
		}
		model.put("idList", idList);
		return getXmlView(model, "inner/activity/recommendActivityIdList.vm");
	}
	
	/**
	 * 获取图片列表
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping("/inner/activity/picture/pictureList.xhtml")
	public String pictureList(
			String tag,
			Long relatedid,
			@RequestParam(defaultValue="0",required=false,value="from")int from,
			@RequestParam(defaultValue="20",required=false,value="maxnum") int maxnum, 
			ModelMap model){
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		if(relatedid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "relateid不能为空！");
		if(maxnum > 20) maxnum = 20;
		List<Picture> picList = pictureService.getPictureListByRelatedid(tag, relatedid, from, maxnum);
		int count=pictureService.getPictureCountByRelatedid(tag, relatedid);
		model.put("picList", picList);
		model.put("count", count);
		return getXmlView(model, "inner/activity/pictureList.vm");
	}
	
	/**
	 * 发生站内信
	 * @param tomemberid
	 * @param action
	 * @param actionid
	 * @param body
	 */
	@RequestMapping("/inner/activity/message/sendSiteMSG.xhtml")
	public String sendSiteMSG(
			@RequestParam(value = "tomemberid") Long tomemberid,
			@RequestParam(required = false, value = "action") String action,
			@RequestParam(value = "actionid") Long actionid,
			@RequestParam(value = "body") String body,
			ModelMap model){
		if(tomemberid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tomemberid不能为空！");
		if(actionid == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "actionid不能为空！");
		if(body == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "body不能为空！");
		userMessageService.sendSiteMSG(tomemberid, action, actionid, body);
		return getXmlView(model, "api/mobile/result.vm");
	}
	
	/**
	 * 修改积分
	 * @param memberEncode 用户
	 * @param pointvalue 积分
	 * @param reason 原因
	 * @param tag
	 */
	@RequestMapping("/inner/activity/member/editPointInfo.xhtml")
	public String editPointInfo(
			@RequestParam(value = "memberEncode") String memberEncode,
			@RequestParam(value = "pointvalue") Integer pointvalue,
			@RequestParam(required = false, value = "reason") String reason,
			@RequestParam(value = "tag") String tag,
			ModelMap model){
		if(StringUtils.isBlank(memberEncode))
			return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "memberEncode不能为空！");
		Member member = memberService.getMemberByEncode(memberEncode);
		if(member == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "用户不存在！");
		if(pointvalue == null) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "pointvalue不能为空！");
		if(StringUtils.isBlank(tag)) return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "tag不能为空！");
		Point point = pointService.addPointInfo(member.getId(), pointvalue, reason, tag);
		if(point != null){
			return getXmlView(model, "api/mobile/result.vm");
		}
		return getErrorXmlView(model, ApiConstant.CODE_SIGN_ERROR, "修改积分失败！");
	}
	
	/**
	 * 获取开放场次列表
	 * @param mpid 场次id列表
	 * @return
	 */
	@RequestMapping("/inner/activity/getOpenPlayItemByMPID.xhtml")
	public String getOpenPlayItemByMPID(String mpids, ModelMap model){
		if(mpids == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> mpiIdList = Arrays.asList(StringUtils.split(mpids, ","));
		List<Long> mpiIds = new ArrayList<Long>();
		for (String string : mpiIdList) {
			mpiIds.add(Long.parseLong(string));
		}
		List<OpenPlayItem> opiList = new ArrayList<OpenPlayItem>();
		for (Long mpi : mpiIds) {
			OpenPlayItem opi = daoService.getObjectByUkey(OpenPlayItem.class, "mpid", mpi, false);
			if(opi != null) opiList.add(opi);
		}
		model.put("opiList", opiList);
		return getXmlView(model, "inner/activity/openPlayItemDetail.vm");
	}
	
	/**
	 * 获取竞拍信息
	 * @param id
	 * @return
	 */
	@RequestMapping("/inner/activity/getPubSale.xhtml")
	public String getPubSale(Long id, ModelMap model){
		PubSale pubsale = daoService.getObject(PubSale.class, id);
		if(pubsale == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "竞拍信息为空！");
		model.put("pubsale", pubsale);
		return getXmlView(model, "inner/activity/pubSaleDetail.vm");
	}
	
	/**
	 * 获取5元抢票信息
	 * @param id
	 * @return
	 */
	@RequestMapping("/inner/activity/grabTicketSubject.xhtml")
	public String getGrabTicketSubject(Long id, ModelMap model){
		GrabTicketSubject gts = daoService.getObject(GrabTicketSubject.class, id);
		if(gts == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "5元抢票信息为空！");
		model.put("gts", gts);
		return getXmlView(model, "inner/activity/grabTicketSubject.vm"); 
	}
	
	/**
	 * 购票评分
	 * @param tag movie
	 * @param relatedid
	 * @return
	 */
	@RequestMapping("/inner/activity/getMarkCountByTagRelatedid.xhtml")
	public String getMarkCountByTagRelatedid(String tag, Long relatedid, ModelMap model){
		MarkCountData markcount = markService.getMarkCountByTagRelatedid(tag, relatedid);
		if(markcount == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "信息为空！");
		model.put("markcount", markcount);
		return getXmlView(model, "inner/activity/markCount.vm"); 
	}
	
	/**
	 * 
	 * @param sid 抢票专题id
	 * @return
	 */
	@RequestMapping("/inner/activity/getGrabTicketMpi.xhtml")
	public String getGrabTicketMpi(Long sid, ModelMap model){
		List<GrabTicketMpi> gtmList = daoService.getObjectListByField(GrabTicketMpi.class, "sid", sid);
		if(gtmList == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "信息为空！");
		model.put("gtmList", gtmList);
		return getXmlView(model, "inner/activity/grabTicketMpi.vm");
	}
	
	/**
	 * 
	 * @param mpiIdList 关联场次id
	 * @return
	 */
	@RequestMapping("/inner/activity/getTicketOrder.xhtml")
	public String getTicketOrder(String mpiIds, ModelMap model){
		if(mpiIds == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		List<String> mpiIdList = Arrays.asList(StringUtils.split(mpiIds, ","));
		List<Long> Ids = new ArrayList<Long>();
		for (String string : mpiIdList) {
			Ids.add(Long.parseLong(string));
		}
		DetachedCriteria queryOrder =  DetachedCriteria.forClass(TicketOrder.class, "t");
		queryOrder.add(Restrictions.eq("t.status", OrderConstant.STATUS_PAID_SUCCESS));
		queryOrder.add(Restrictions.in("t.mpid", Ids));
		queryOrder.add(Restrictions.or(Restrictions.isNull("t.unitprice"), Restrictions.eq("t.totalfee", 10)));
		queryOrder.addOrder(Order.desc("t.addtime"));
		List<TicketOrder> orderList = hibernateTemplate.findByCriteria(queryOrder);
		model.put("orderList", orderList);
		return getXmlView(model, "inner/activity/ticketOrderList.vm");
	}
	//活动保存图片
	@RequestMapping("/inner/activity/picture/saveActivityPicture.xhtml")
	public String pictureList(Long memberid, Long activityid, String fileName, String realName, ModelMap model) throws Exception {
		if(memberid == null || activityid == null || fileName == null) return getErrorXmlView(model,  ApiConstant.CODE_PARAM_ERROR, "参数错误！");
		String path = PictureUtil.getAlbumPicpath();
		gewaPicService.moveRemoteTempTo(memberid, TagConstant.TAG_ACTIVITY, activityid, path, fileName);
		Picture picture = new Picture(TagConstant.TAG_ACTIVITY);
		picture.setRelatedid(activityid);
		picture.setMemberid(memberid);
		picture.setMemberType(GewaraUser.USER_TYPE_MEMBER);
		picture.setName(realName);
		picture.setDescription(realName);
		picture.setPicturename(path + fileName);
		daoService.saveObject(picture);
		return getSingleResultXmlView(model, "success");
	}
}
