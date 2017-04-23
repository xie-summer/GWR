package com.gewara.web.action.subject.admin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.User;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.xmlbind.activity.RemoteActivity;

@Controller
public class GewaSpecialActivityAdminController extends BaseAdminController {

	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	@RequestMapping("/admin/sport/getGewaCupList.xhtml")
	public String getGewaCupList(ModelMap model, String cupid){
		if(StringUtils.isNotBlank(cupid)){
			Map dataMap=mongoService.getMap(MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, cupid);
			if(dataMap != null){
				if(dataMap.get("memberid")!=null){
					Member member=daoService.getObject(Member.class, Long.parseLong(dataMap.get("memberid")+""));
					model.put("member", member);
				}
			}
			model.put("dataMap", dataMap);
		}else {
			List<Map> gewaCupList= mongoService.getMapList(MongoData.NS_GEWA_CUP_NAMESPACE);
			Map<String, Commu> commuMap=new HashMap<String, Commu>();
			Map<String, Member> MemberMap=new HashMap<String, Member>();
			Map<String, GewaOrder> gewaOrderMap=new HashMap<String, GewaOrder>();
			for(Map gewaCup: gewaCupList){
				if(gewaCup.get("commuid")!= null){
					Commu commu=daoService.getObject(Commu.class, Long.parseLong(gewaCup.get("commuid")+""));
					if(commu != null && commu.hasStatus(Status.Y)){
						commuMap.put(gewaCup.get("cupid")+"", commu);
					}
				}
				Member member=daoService.getObject(Member.class,  Long.parseLong(gewaCup.get("memberid")+""));
				MemberMap.put(gewaCup.get("cupid")+"", member);
				if(StringUtils.isNotBlank(gewaCup.get("orderid")+"")) {
					GewaOrder order = daoService.getObject(GewaOrder.class, Long.parseLong(gewaCup.get("orderid")+""));
					gewaOrderMap.put(gewaCup.get("cupid")+"", order);
				}
			}
			model.put("gewaOrderMap", gewaOrderMap);
			model.put("MemberMap", MemberMap);
			model.put("commuMap", commuMap);
			model.put("gewaCupList", gewaCupList);
		}
		return "admin/gewacup/gewacupList.vm";
	}
	@RequestMapping("/admin/sport/gewaCupInfo.xhtml")
	public String gewaCupInfo(ModelMap model, String cupid){
		if(StringUtils.isBlank(cupid)) return showJsonError(model, "参数出错!");
		Map dataMap = mongoService.getMap(MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, cupid);
		if(dataMap.get("memberid")!=null){
			Member member=daoService.getObject(Member.class, Long.parseLong(dataMap.get("memberid")+""));
			model.put("member", member);
		}
		if(dataMap.get("commuid")!=null){
			Commu commu=daoService.getObject(Commu.class, Long.parseLong(dataMap.get("commuid")+""));
			model.put("commu", commu);
		}
		model.put("dataMap", dataMap);
		return "admin/gewacup/gewacupinfo.vm";
	}
	@RequestMapping("/admin/sport/checkGewaCup.xhtml")
	public String checkGewaCup(ModelMap model, String cupid, String cupstatus){
		if(cupstatus.equals("a")) return showJsonError(model, "不能选择申请中状态");
		Map dataMap=mongoService.getMap(MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, cupid);
		dataMap.put(MongoData.GEWA_CUP_STATUS, cupstatus);
		mongoService.saveOrUpdateMap(dataMap, MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, false, true);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sport/deleteGewaCup.xhtml")
	public String deleteGewaCup(ModelMap model, String cupid){
		if(StringUtils.isBlank(cupid)) return showJsonError(model, "参数出错!");
		mongoService.removeObjectById(MongoData.NS_GEWA_CUP_NAMESPACE,MongoData.GEWA_CUP_IDNAME, cupid);
		return showJsonSuccess(model);
	}
	
	//设置订单号
	@RequestMapping("/admin/sport/updateGewaCup.xhtml")
	public String updateGewaCup(ModelMap model, Long orderid, String cupid){
		User user = getLogonUser();
		Map dataMap=mongoService.getMap(MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, cupid);
		if(dataMap==null) return showJsonError(model, "参数出错！");
		GoodsOrder order = daoService.getObject(GoodsOrder.class, orderid);
		if(order!=null){
			//判断订单关联的活动ID是否正确
			Goods goods = daoService.getObject(Goods.class, order.getGoodsid());
			if(goods!=null){
				ErrorCode<RemoteActivity> code = synchActivityService.getRemoteActivity(goods.getRelatedid());
				if(code.isSuccess()){
					RemoteActivity activity = code.getRetval();
					if((dataMap.get("joinmethod")+"").equals("personal")){
						if(!(new Long("3952660").equals(activity.getId())))return showJsonError(model, "订单ID关联活动ID不正确");
					}else if((dataMap.get("joinmethod")+"").equals("club")){
						if(!(new Long("3952658").equals(activity.getId())))return showJsonError(model, "订单ID关联活动ID不正确");
					}
				}
			}
			//该用户的订单,订单状态=paid_success
			if((!order.getMemberid().equals(new Long(dataMap.get("memberid")+""))) /**&& !order.getStatus().equals(GewaOrder.STATUS_PAID_SUCCESS)*/){
				return showJsonError(model, "订单的用户ID不正确！");
			}
			if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)){
				return showJsonError(model, "订单未支付成功！");
			}
			dataMap.put(MongoData.GEWA_CUP_ORDERID, orderid);
			mongoService.saveOrUpdateMap(dataMap, MongoData.GEWA_CUP_IDNAME, MongoData.NS_GEWA_CUP_NAMESPACE, false, true);
			dbLogger.warn("设置订单ID的管理员：" + user.getRealname());
		}else {
			return showJsonError(model, "订单数据出错，不存在此数据！");
		}
		return showJsonSuccess(model);
	}
}
