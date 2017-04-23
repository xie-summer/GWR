package com.gewara.web.action.api2machine;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.OdiConstant;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.api.ApiUser;
import com.gewara.model.api.Synch;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.drama.Theatre;
import com.gewara.model.machine.GewaFace;
import com.gewara.model.machine.GptbsFace;
import com.gewara.model.machine.MachineSynch;
import com.gewara.model.machine.SeatFace;
import com.gewara.model.machine.StandFace;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.OrderNote;
import com.gewara.service.SynchService;
import com.gewara.service.drama.DramaPlayItemService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.drama.RemoteDramaService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.api.ApiAuth;
import com.gewara.web.filter.NewApiAuthenticationFilter;
@Controller
public class MachineTheatreController extends BaseMachineApiController {
	@Autowired@Qualifier("synchService")
	private SynchService synchService;
	public void setSynchService(SynchService synchService) {
		this.synchService = synchService;
	}
	@Autowired@Qualifier("dramaPlayItemService")
	protected DramaPlayItemService dramaPlayItemService;
	public void setDramaPlayItemService(DramaPlayItemService dramaPlayItemService){
		this.dramaPlayItemService = dramaPlayItemService;
	}
	
	@Autowired@Qualifier("remoteDramaService")
	private RemoteDramaService remoteDramaService;
	/**
	 * 根据场馆、取票密码查询订单信息
	 * 用户实时取票，取完票以后，记录状态
	 * @param cinemaid
	 * @param checkpass
	 * @param shortmobile
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/drama/qryOrderList.xhtml")
	public String qryOrderList(Long theatreid, String checkpass, String shortmobile, ModelMap model){
		if(theatreid==null || StringUtils.isBlank(checkpass) || StringUtils.isBlank(shortmobile)) {
			return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "缺少参数");
		}
		Theatre theatre = daoService.getObject(Theatre.class, theatreid);
		if(theatre == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该剧院！");
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		List<OrderNote> onmList = hibernateTemplate.find("from OrderNote where placeid=? and checkpass=? and substr(mobile,8,4)=? and express=?", theatreid, checkpass, shortmobile, Status.N);
		addCommonMap(apiUser, theatre, onmList, model);
		model.put("nowtime", DateUtil.getMillTimestamp());
		return getXmlView(model, "api2machine/downDramaOrderList2.vm");
	}
	
	/**
	 * 下载订单
	 * @param dramaid
	 * @param ticketnum
	 * @param lastTime
	 * @param pageSize
	 * @param request
	 * @param model
	 * @return
	 */
	@RequestMapping("/apimac/drama/downOrder.xhtml")
	public String downOrder(Long theatreid, String ticketnum, Timestamp lastTime,Integer pageSize, String macid, HttpServletRequest request, ModelMap model){
		if(theatreid==null) return getErrorXmlView(model, ApiConstant.CODE_PARAM_ERROR, "传递参数错误！");
		ApiAuth auth = NewApiAuthenticationFilter.getApiAuth();
		ApiUser apiUser = auth.getApiUser();
		String ip = WebUtils.getRemoteIp(request);
		return downDramaOrder(apiUser, theatreid, ticketnum,lastTime,pageSize, ip, macid, model);
	}
	
	private String downDramaOrder(ApiUser apiUser,Long threatreid,String ticketnum,Timestamp lastTime,Integer pageSize, String ip, String macid, ModelMap model){
		Theatre theatre = daoService.getObject(Theatre.class, threatreid);
		if(theatre == null) return getErrorXmlView(model, ApiConstant.CODE_DATA_ERROR, "没有该剧院！");
		Synch synch = daoService.getObject(Synch.class, threatreid);
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		if(synch == null) synch = new Synch(threatreid, Synch.TGA_DRAMA);
		apiService.saveSynchWithCinema(synch, cur, null, ticketnum, ip);
		MachineSynch ms = null;
		if(StringUtils.isNotBlank(macid)){
			ms = getMachineSynch(threatreid, Synch.TGA_DRAMA, macid, null);
			ms.setSynchtime(DateUtil.getMillTimestamp());
			if(lastTime==null) lastTime = ms.getSuccesstime();
			daoService.saveObject(ms);
		}else {
			if(lastTime==null) lastTime = synch.getSuccesstime();
		}
		if(lastTime==null) lastTime = DateUtil.addDay(DateUtil.getMillTimestamp(), -90);
		if(pageSize==null) pageSize = 500;
		
		List<OrderNote> onmList = synchService.getOrderNoteList(threatreid, DateUtil.addMinute(lastTime, -5), pageSize);
		model.put("nowtime", cur);
		addCommonMap(apiUser, theatre, onmList, model);
		model.put("theatre", theatre);
		return getXmlView(model, "api2machine/downDramaOrderList2.vm");
	}
	private void addCommonMap(ApiUser apiUser, Theatre theatre, List<OrderNote> onmList, ModelMap model){
		GewaConfig gcon = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_CUSTOM_PAPER);
		List<Map<String,String>> qryMapList = new ArrayList<Map<String,String>>();
		String address = theatre.getAddress();
		Map<String, GewaFace> faceMap = new HashMap<String, GewaFace>();
		Map<Long,List<OrderNote>> onmMap = BeanUtil.groupBeanList(onmList, "orderid");
		Map<String, String> cpMap = new HashMap<String, String>();
		for (Long orderid : onmMap.keySet()) {
			List<OrderNote> noteList = onmMap.get(orderid);
			GewaOrder dramaOrder = daoService.getObject(GewaOrder.class, orderid);
			if(dramaOrder == null){
				dbLogger.warn("orderId" + orderid  +",订单不存在");
				continue;
			}
			cpMap.put(dramaOrder.getTradeNo(), getDefContent(gcon, dramaOrder));
			String otherinfo = "";
			if((dramaOrder instanceof DramaOrder) && !StringUtils.equals(dramaOrder.getCategory(), OdiConstant.PARTNER_GEWA)){
				ErrorCode<String> code = remoteDramaService.qryOrderPrintInfo(orderid);
				if(!code.isSuccess()){
					dbLogger.warn("orderid --> " + orderid +"," +code.getErrcode()+":" + code.getMsg());
					continue;
				}
				otherinfo = code.getRetval();
			}
			if(!CollectionUtils.isEmpty(noteList)){
				List<GptbsFace> faceList = new ArrayList<GptbsFace>();
				if(StringUtils.isNotBlank(otherinfo)){
					List<GptbsFace> otherInfoList = JsonUtils.readJsonToObjectList(GptbsFace.class, otherinfo);
					if(!CollectionUtils.isEmpty(otherInfoList)){
						faceList.addAll(otherInfoList);
					}
				}
				Map<String,List<GptbsFace>> gptbsFaceMap = BeanUtil.groupBeanList(faceList, "siseqno");
				for(OrderNote onm: noteList){
					OpenDramaItem dpi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", onm.getSmallitemid());
					if(dpi!=null && StringUtils.equals(dpi.getPrint(), Status.N)) continue;
					Map<String, String> tmp = VmUtils.readJsonToMap(onm.getDescription());
					String checkpass = StringUtil.md5(onm.getCheckpass()+apiUser.getPrivatekey());
					tmp.put("orderkey", checkpass);
					tmp.put("address", address);
					if(dpi!=null && !dpi.hasGewara()){
						List<GptbsFace> odiFaceList = gptbsFaceMap.get(dpi.getSellerseq());
						if(!CollectionUtils.isEmpty(odiFaceList)){
							int x = 0;
							GewaFace tf = new GewaFace();
							for(GptbsFace agf : odiFaceList){
								if(x==0){
									BeanUtil.copyProperties(tf, agf);
								}
								if(dpi.isOpenseat()){
									SeatFace newSeatface = new SeatFace();
									BeanUtil.copyProperties(newSeatface, agf);
									tf.getSeatList().add(newSeatface);
									tf.setOpentype("seat");
								}else if(dpi.isOpenprice()){
									StandFace newStandface = new StandFace();
									BeanUtil.copyProperties(newStandface, agf);
									tf.getStandList().add(newStandface);
									tf.setOpentype("stand");
								}
								x++;
							}
							faceMap.put(onm.getSerialno(), tf);
						}
					}
					qryMapList.add(tmp);
				}
			}
		}
		model.put("qryMapList", qryMapList);
		model.put("faceMap", faceMap);
	}
	
	@RequestMapping("/apimac/drama/dpiLayout.xhtml")
	public String dpiLayout(Long dpid, ModelMap model){
		DramaPlayItem dpi = daoService.getObject(DramaPlayItem.class, dpid);
		ErrorCode<String> code = remoteDramaService.qryTicketPrice(dpi.getSellerseq());
		if(!code.isSuccess()){
			dbLogger.warn("获取场次失败：dpid:" + dpid + ", seller=" + dpi.getSeller()+", opentype" + dpi.getOpentype());
			return dpid + "item is null：seller=" + dpi.getSeller()+", opentype" + dpi.getOpentype();
		}
		model.put("directResult", code.getRetval());
		return "common/direct.vm";
	}
}
