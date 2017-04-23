package com.gewara.web.action.api2machine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.json.CustomPaper;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.machine.MachineSynch;
import com.gewara.model.machine.TakeTicket;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.model.pay.TicketOrder;
import com.gewara.web.action.inner.mobile.BaseOpenApiMobileController;

public class BaseMachineApiController extends BaseOpenApiMobileController{
	@Autowired@Qualifier("jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;
	protected List<GewaOrder> getGewaOrderByCheckpass(Long placeid, Timestamp addtime, String checkpass, String shortmobile){
		String qry = "select t.trade_no as tradeno from webdata.ticket_order t where t.cinemaid=? and t.addtime>? and t.status=? and checkpass=? and substr(t.mobile,8,4)=? order by addtime desc";
		List<Map<String, Object>> qryMapList = jdbcTemplate.queryForList(qry, placeid, addtime, OrderConstant.STATUS_PAID_SUCCESS, checkpass, shortmobile);
		List<GewaOrder> orderList = new ArrayList<GewaOrder>();
		if(qryMapList!=null && qryMapList.size()>0) {
			for(Map<String, Object> qryMap : qryMapList){
				String tradeno = String.valueOf(qryMap.get("tradeno"));
				GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeno);
				orderList.add(order);
			}
			
		}
		return orderList;
	}
	protected OrderNote getOrderNoteByCheckpass(Long placeid, Timestamp addtime, String checkpass){
		String qry = "from OrderNote where placeid=? and checkpass=? and addtime>?";
		List<OrderNote> ttList = hibernateTemplate.find(qry, placeid, checkpass, addtime);
		if(ttList.size()>0) {
			return ttList.get(0);
		}
		return null;
	}
	
	protected MachineSynch getMachineSynch(Long placeid, String tag, String macid, Timestamp successtime){
		String qry = "from MachineSynch where placeid=? and macid=?";
		List<MachineSynch> msList = hibernateTemplate.find(qry, placeid, macid);
		MachineSynch ms = null;
		if(msList.size()>0){
			ms = msList.get(0);
		}else {
			ms = new MachineSynch();
			ms.setMacid(macid);
			ms.setTag(tag);
			ms.setPlaceid(placeid);
		}
		if(successtime!=null){
			ms.setSuccesstime(successtime);
		}
		daoService.saveObject(ms);
		return ms;
	}
	
	protected TakeTicket validTakeTicket(Long placeid, String macid, String tradeno){
		String qry = "from TakeTicket where placeid=? and macid=? and tradeno=?";
		List<TakeTicket> ttList = hibernateTemplate.find(qry, placeid, macid, tradeno);
		if(ttList.size()>0){
			return ttList.get(0);
		}
		return null;
	}
	protected String getDefContent(GewaConfig gcon, GewaOrder order){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("tradeno", order.getTradeNo());
		params.put("memberid", order.getMemberid());
		if(order instanceof TicketOrder){
			params.put("tag", TagConstant.TAG_CINEMA);
		}else if(order instanceof DramaOrder){
			params.put("tag", TagConstant.TAG_DRAMA);
		}else {
			return "";
		}
		List<CustomPaper> cpList = mongoService.getObjectList(CustomPaper.class, params, "addtime", true, 0, 1);
		if(cpList.size()==1){
			return cpList.get(0).getSelfdomcontent();
		}else {
			return gcon.getContent();
		}
	}
}
