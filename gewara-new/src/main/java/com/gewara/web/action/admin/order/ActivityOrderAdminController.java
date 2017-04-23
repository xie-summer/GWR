package com.gewara.web.action.admin.order;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.support.PropertyComparator;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.OrderParamsCommand;
import com.gewara.constant.GoodsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.api.OrderResult;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.goods.ActivityGoods;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.goods.Goods;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.sport.Sport;
import com.gewara.pay.PayUtil;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.OrderQueryService;
import com.gewara.util.DateUtil;
import com.gewara.web.action.admin.BaseAdminController;


@Controller
public class ActivityOrderAdminController extends BaseAdminController {
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	
	@RequestMapping("/admin/order/activityOrderList.xhtml")
	public String orderList(OrderParamsCommand command, HttpServletResponse response, ModelMap model) throws IOException {
		Timestamp cur = DateUtil.getCurFullTimestamp();
		checkParams(cur, command);
		if (StringUtils.isNotBlank(command.getErrorMsg())) {
			List<BaseInfo> cinemaList = new ArrayList<BaseInfo>();
			BaseInfo info = null;
			List<Long> cinemaidList = hibernateTemplate.find("select distinct g.relatedid from Goods g where g.tag=? and g.status!=?", GoodsConstant.GOODS_TAG_BMH, Status.DEL);
			for(Long cid : cinemaidList){
				info = daoService.getObject(Cinema.class, cid);
				if(info==null){
					info = daoService.getObject(Sport.class, cid);
				}
				if(info!=null)cinemaList.add(info);
			}
			model.put("cinemaList", cinemaList);
			model.put("command", command);
			return "admin/order/activityOrderList.vm";
		}
		if (command.getXls() != null && command.getXls().equals("true")) {
			this.exportExcel(command, response);
			return null;
		}
		List<Long> cinemaidList = hibernateTemplate.find("select distinct g.relatedid from Goods g where g.tag=? and g.status!=?", GoodsConstant.GOODS_TAG_BMH, Status.DEL);
		List<BaseInfo> cinemaList = new ArrayList<BaseInfo>();
		BaseInfo info = null;
		for(Long cid : cinemaidList){
			info = daoService.getObject(Cinema.class, cid);
			if(info==null){
				info = daoService.getObject(Sport.class, cid);
			}
			if(info!=null)cinemaList.add(info);
		}
		Map<Long, List<Goods>> goodsMap = new HashMap<Long, List<Goods>>();
		for(BaseInfo bi : cinemaList){
			goodsMap.put(bi.getId(), hibernateTemplate.find("from Goods g where g.tag=? and g.relatedid=? order by g.goodssort", GoodsConstant.GOODS_TAG_BMH, bi.getId()));
		}
		model.put("goodsMap", goodsMap);
		model.put("cinemaList", cinemaList);
		List<GoodsOrder> orderList = orderQueryService.getGoodsOrderList(ActivityGoods.class, command, -1, -1);
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		Map<Long, Boolean> takeMap = new HashMap<Long, Boolean>();
		Map<String, Integer> buynumMap = new HashMap<String, Integer>();
		String ordertype = OrderResult.ORDERTYPE_MEAL;
		BaseGoods goods = null;
		Map<Long, BaseInfo> cinemaMap = new HashMap<Long, BaseInfo>();
		for(GoodsOrder order : orderList){
			goods = daoService.getObject(BaseGoods.class, order.getGoodsid());
			info = daoService.getObject(Cinema.class, goods.getRelatedid());
			if(info==null){
				info = daoService.getObject(Sport.class, goods.getRelatedid());
			}
			cinemaMap.put(order.getId(), info);
			takeMap.put(order.getId(), isTakeByTradeno(order.getTradeNo(), ordertype));
			if(!buynumMap.containsKey(order.getMobile()))
				buynumMap.put(order.getMobile(), goodsOrderService.getGewaorderCountByMobile(null, order.getMobile(), "goods"));
		}
		PayUtil payUtil = new PayUtil();
		model.put("takeMap", takeMap);
		model.put("cinemaMap", cinemaMap);
		model.put("orderList", orderList);
		model.put("buynumMap", buynumMap);
		model.put("command", command);
		model.put("payUtil", payUtil);
		return "admin/order/activityOrderList.vm";
	}
	
	private void checkParams(Timestamp cur, OrderParamsCommand command){
		if(StringUtils.isBlank(command.getLevel())){
			command.setStatus(OrderConstant.STATUS_PAID_FAILURE);
			command.setEndtime(cur);
			command.setStarttime(DateUtil.addDay(DateUtil.getBeginningTimeOfDay(cur), -10));
			command.setLevel("1");
		}
		if (StringUtils.isBlank(command.getOrder()) && StringUtils.isBlank(command.getMobile())) {
			if(command.getStarttime() == null || command.getEndtime() == null){
				command.setErrorMsg("交易时段范围不能为空！");
			}
			if (DateUtil.getDiffDay(command.getEndtime(), command.getStarttime()) > 5) {
				command.setErrorMsg("查询时间间隔不得大于5天！");
			}
		}
	}
	
	public void exportExcel(OrderParamsCommand command, HttpServletResponse response) throws IOException {
		String[] str = {"序号","影院名/套餐名","订单号","购买方式","取票密码","下单时间","联系电话","用户/次数","总价","状态","取票状态"};
		List<Long> cinemaidList = hibernateTemplate.find("select distinct g.relatedid from Goods g where g.tag=? and g.status!=?", GoodsConstant.GOODS_TAG_BMH, Status.DEL);
		BaseInfo info = null;
		for(Long cid : cinemaidList){
			info = daoService.getObject(Cinema.class, cid);
			if(info==null){
				info = daoService.getObject(Sport.class, cid);
			}
		}
		List<GoodsOrder> orderList = orderQueryService.getGoodsOrderList(BaseGoods.class, command, -1, -1);
		Collections.sort(orderList, new PropertyComparator("addtime", false, false));
		Map<Long, String> takeMap = new HashMap<Long, String>();
		Map<String, Integer> buynumMap = new HashMap<String, Integer>();
		BaseGoods goods = null;
		Map<Long, BaseInfo> cinemaMap = new HashMap<Long, BaseInfo>();
		for(GoodsOrder order : orderList){
			goods = daoService.getObject(BaseGoods.class, order.getGoodsid());
			info = daoService.getObject(Cinema.class, goods.getRelatedid());
			if(info==null){
				info = daoService.getObject(Sport.class, goods.getRelatedid());
			}
			cinemaMap.put(order.getId(), info);
			if(!buynumMap.containsKey(order.getMobile()))
				buynumMap.put(order.getMobile(), goodsOrderService.getGewaorderCountByMobile(null, order.getMobile(), "goods"));
			takeMap.put(order.getId(), this.takeTicket(order.getId()));
		}
		//创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		//创建表
		HSSFSheet sheet = wb.createSheet("MealOrder");
		//创建表头
		HSSFHeader header = sheet.getHeader();
		//设置表格的宽度
		sheet.setDefaultColumnWidth(13);
		//设置标题
		header.setCenter("卖品订单");
		HSSFRow row = sheet.createRow(0);
		//逐一设置Title的值
		for(int i = 0;i < str.length;i++)
		{
			 HSSFCell headerCell = row.createCell(i);  
			 headerCell.setCellValue(new HSSFRichTextString(str[i]));  
		}
		for(int rownum = 1; rownum <= orderList.size(); rownum++){
			GoodsOrder goodsOrder = orderList.get(rownum - 1);
			row = sheet.createRow(rownum);
			HSSFCell cell1 = row.createCell(0);
			HSSFCell cell2 = row.createCell(1);
			HSSFCell cell3 = row.createCell(2);
			HSSFCell cell4 = row.createCell(3);
			HSSFCell cell5 = row.createCell(4);
			HSSFCell cell6 = row.createCell(5);
			HSSFCell cell7 = row.createCell(6);
			HSSFCell cell8 = row.createCell(7);
			HSSFCell cell9 = row.createCell(8);
			HSSFCell cell10 = row.createCell(9);
			HSSFCell cell11 = row.createCell(10);
			cell1.setCellValue(new HSSFRichTextString(String.valueOf(rownum)));
			cell2.setCellValue(new HSSFRichTextString((cinemaMap.get(goodsOrder.getId()) != null ? (cinemaMap.get(goodsOrder.getId())).getName() : "" + "/" + goodsOrder.getOrdertitle())));
			cell3.setCellValue(new HSSFRichTextString(goodsOrder.getTradeNo()));
			cell4.setCellValue(new HSSFRichTextString(goodsOrder.getPaymethod().equals("sysPay") ? "单买" : ""));
			cell5.setCellValue(new HSSFRichTextString(goodsOrder.getCheckpass()));
			cell6.setCellValue(new HSSFRichTextString(DateUtil.format(goodsOrder.getAddtime(), "MM-dd HH:mm:ss")));
			cell7.setCellValue(new HSSFRichTextString(goodsOrder.getMobile()));
			cell8.setCellValue(new HSSFRichTextString(goodsOrder.getMembername()));
			cell9.setCellValue(new HSSFRichTextString(String.valueOf(goodsOrder.getTotalAmount())));
			cell10.setCellValue(new HSSFRichTextString(goodsOrder.getStatusText()));
			cell11.setCellValue(new HSSFRichTextString(takeMap.get(goodsOrder.getId())));
		}
		String exportName = "MealOrder_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xls";
		response.setContentType("application/xls");
		response.addHeader("Content-Disposition", "attachment;filename=" + exportName);
		wb.write(response.getOutputStream());
	}

	private String takeTicket(Long orderid){
		String result = "";
		GewaOrder order = daoService.getObject(GewaOrder.class, orderid);
		if(!order.getStatus().equals(OrderConstant.STATUS_PAID_SUCCESS)) {
			result = "该订单不是成功订单！";
			return result;
		}
		if(isTakeByTradeno(order.getTradeNo(), OrderResult.ORDERTYPE_MEAL)){
			result = "已经取票！";
		}else {
			result = "没有取票或者该订单还没有被同步！";
		}
		return result;
	}
	private boolean isTakeByTradeno(String tradeno, String ordertype){
		String qry = "from OrderResult o where o.tradeno=? and o.istake=? and o.ordertype=?";
		List list = hibernateTemplate.find(qry, tradeno, "Y", ordertype);
		return list.size()>0;
	}
}
