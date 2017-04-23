package com.gewara.web.action.subject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.sys.MongoData;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.pay.UnionpayWalletUtil;
import com.gewara.service.order.OrderQueryService;
import com.gewara.untrans.PartnerWebService;
import com.gewara.util.HttpResult;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.web.action.AnnotationController;
@Controller
public class SubjectUnionpayWalletProxyController  extends AnnotationController {
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@Autowired@Qualifier("partnerWebService")
	private PartnerWebService partnerWebService;

	@RequestMapping("/subject/proxy/wallet/queryWalletOrder.xhtml")
	public String queryWalletOrder(Long memberid, String check,Long[] spIds,ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		Map jsonMap = new HashMap();
		if(!StringUtils.equals(check, checkcode)) {
			jsonMap.put("code", "0001");
			jsonMap.put("msg","nologin");
			return showJsonSuccess(model, jsonMap);
		}
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) {
			jsonMap.put("code", "0001");
			jsonMap.put("msg","nologin");
			return showJsonSuccess(model, jsonMap);
		}
		if(spIds == null){
			jsonMap.put("code", "0001");
			jsonMap.put("msg","setError");
			return showJsonSuccess(model, jsonMap);
		}
		List<GewaOrder> orderList = orderQueryService.getPreferentialOrder(member.getId(),Arrays.asList(spIds));
		int noPushNum = 0;
		for(GewaOrder order : orderList){
			Map otherInfo = JsonUtils.readJsonToMap(order.getOtherinfo());
			if(!(otherInfo != null && otherInfo.get("isPushWallet") != null)){
				noPushNum++;
			}
		}
		jsonMap.put("code", "0000");
		jsonMap.put("dataCount", orderList.size());
		jsonMap.put("noPushNum",noPushNum);
		return showJsonSuccess(model, jsonMap);
	}
	
	@RequestMapping("/subject/proxy/wallet/pushOrderToWallet.xhtml")
	public String pushOrderToWallet(Long memberid, String check,String userId,Long[] spIds,String mobile,ModelMap model){
		String checkcode = StringUtil.md5(memberid + "njmk5678");
		Map jsonMap = new HashMap();
		if(!StringUtils.equals(check, checkcode)) {
			jsonMap.put("code", "0001");
			jsonMap.put("msg","nologin");
			return showJsonSuccess(model, jsonMap);
		}
		Member member = daoService.getObject(Member.class, memberid);
		if(member == null) {
			jsonMap.put("code", "0001");
			jsonMap.put("msg","nologin");
			return showJsonSuccess(model, jsonMap);
		}
		if(spIds == null){
			jsonMap.put("code", "0001");
			jsonMap.put("msg","setError");
			return showJsonSuccess(model, jsonMap);
		}
		HttpResult checkResult = UnionpayWalletUtil.getUserbyMobile(mobile);
		if(!checkResult.isSuccess()){
			jsonMap.put("code", "0001");
			jsonMap.put("msg",checkResult.getMsg());
			return showJsonSuccess(model, jsonMap);
		}
		userId = checkResult.getResponse().toString();
		List<GewaOrder> orderList = orderQueryService.getPreferentialOrder(member.getId(),Arrays.asList(spIds));
		Map<String,String> mappingMap = new HashMap<String,String>();
		List<Map> mappings = mongoService.getMapList(MongoData.NS_UNIONPAY_WALLET_MAPPING);
		if(mappings != null){
			for(Map map : mappings){
				mappingMap.put(map.get("tag") + map.get("mpid").toString(),  map.get("billId").toString());
			}
		}
		int noPushNum = 0;
		for(GewaOrder order : orderList){
			Map otherInfo = JsonUtils.readJsonToMap(order.getOtherinfo());
			if(!(otherInfo != null && otherInfo.get("isPushWallet") != null && StringUtils.equals(otherInfo.get("isPushWallet").toString(),"success"))){
				boolean result = this.toPush(order, userId, mappingMap);
				if(!result){
					noPushNum ++ ;
					continue;
				}
				otherInfo.put("isPushWallet", "success");
				otherInfo.put("unionpayWalletUser", userId);
				order.setOtherinfo(JsonUtils.writeMapToJson(otherInfo));
				this.daoService.updateObject(order);
			}
			
		}
		jsonMap.put("code", "0000");
		jsonMap.put("dataCount", orderList.size());
		jsonMap.put("noPushNum",noPushNum);
		return showJsonSuccess(model, jsonMap);
	}
	
	private boolean toPush(GewaOrder order,String userId,Map<String,String> mappingMap){
		if(order instanceof TicketOrder){
			return partnerWebService.pushOrderToUnionpayWallet(order.getTradeNo(),userId,mappingMap.get("cinema" + ((TicketOrder)order).getMpid()),order.getQuantity());
		}else if(order instanceof SportOrder){
			return partnerWebService.pushOrderToUnionpayWallet(order.getTradeNo(),userId,mappingMap.get("sport" + ((SportOrder)order).getOttid()),order.getQuantity());
		}else if(order instanceof DramaOrder){
			return partnerWebService.pushOrderToUnionpayWallet(order.getTradeNo(),userId,mappingMap.get("drama" + ((DramaOrder)order).getDpid()),order.getQuantity());
		}
		return false;
	}
	
	
}
