package com.gewara.web.action.admin.order;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.command.OrderParamsCommand;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.service.order.OrderQueryService;
import com.gewara.service.sport.SportService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;


@Controller
public class SportOrderAdminController extends BaseAdminController {
	@Autowired@Qualifier("sportService")
	private SportService sportService;
	@Autowired@Qualifier("orderQueryService")
	private OrderQueryService orderQueryService;
	
	@RequestMapping("/admin/order/sportOrderList.xhtml")
	public String orderList(OrderParamsCommand command, HttpServletResponse response, ModelMap model) throws IOException {
		Timestamp cur = DateUtil.getCurFullTimestamp();
		checkParams(cur, command);
		if (StringUtils.isNotBlank(command.getErrorMsg())) {
			List<Sport> sportList =  sportService.getBookingEqOpenSport(null, Sport.BOOKING_OPEN);
			List<SportItem> sportItemList = sportService.getSportItemBySportId(null);
			model.put("sportList", sportList);
			model.put("sportItemList", sportItemList);
			model.put("command", command);
			return "admin/order/sportOrderList.vm";
		}
		if (command.getXls() != null && command.getXls().equals("true")) {
			this.exportExcel(command, response);
			return null;
		}
		List<Sport> sportList =  sportService.getBookingEqOpenSport(null, Sport.BOOKING_OPEN);
		List<SportItem> sportItemList = sportService.getSportItemBySportId(null);
		
		List<SportOrder> orderList = new ArrayList<SportOrder>();
		int rowsPerPage = 100;
		int firstPre = command.getPageNo() * rowsPerPage;
		command.setOrdertype(OrderConstant.ORDER_TYPE_SPORT);
		int rowsCount = orderQueryService.getOrderCount(command);
		if(rowsCount >0){
			orderList = orderQueryService.getOrderList(SportOrder.class, command, firstPre, rowsPerPage);
		}
		model.put("rowsCount", rowsCount);
		PageUtil pageUtil = new PageUtil(rowsCount, rowsPerPage, command.getPageNo(), "admin/order/mealOrderList.xhtml", true, true);
		Map<String,String> params = BeanUtil.getSimpleStringMap(command);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);
		List<Long> ottidList = BeanUtil.getBeanPropertyList(orderList, Long.class, "ottid", true);
		Map<Long, OpenTimeTable> tableMap = daoService.getObjectMap(OpenTimeTable.class, ottidList);
		model.put("tableMap", tableMap);
		model.put("sportList", sportList);
		model.put("sportItemList", sportItemList);
		model.put("orderList", orderList);
		model.put("command", command);
		return "admin/order/sportOrderList.vm";
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
		String[] str = {"序号","场次","订单号","取票密码","下单时间","联系电话","用户","场馆","场地","场次数量","消费时间","总价","优惠金额","实付金额","支付方式","状态"};
		List<SportOrder> orderList = orderQueryService.getOrderList(SportOrder.class, command, -1, -1);
		//创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		//创建表
		HSSFSheet sheet = wb.createSheet("SportOrder");
		//创建表头
		HSSFHeader header = sheet.getHeader();
		//设置表格的宽度
		sheet.setDefaultColumnWidth(13);
		//设置标题
		header.setCenter("运动订单");
		HSSFRow row = sheet.createRow(0);
		//逐一设置Title的值
		for(int i = 0;i < str.length;i++)
		{
			 HSSFCell headerCell = row.createCell(i);  
			 headerCell.setCellValue(new HSSFRichTextString(str[i]));  
		}
		for(int rownum = 1; rownum <= orderList.size(); rownum++){
			SportOrder sportOrder = orderList.get(rownum - 1);
//			Map sportMap = BeanUtil.getBeanMap(sportOrder);
//			ArrayList<String> mapkeys = new ArrayList<String>(map.keySet());
//			int j = 1;
//			for (String key: mapkeys) {
//				HSSFCell cell = row.createCell(j);
//				j++;
//			}
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
			HSSFCell cell12 = row.createCell(11);
			HSSFCell cell13 = row.createCell(12);
			HSSFCell cell14 = row.createCell(13);
			HSSFCell cell15 = row.createCell(14);
			HSSFCell cell16 = row.createCell(15);
			cell1.setCellValue(new HSSFRichTextString(String.valueOf(rownum)));
			cell2.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getOttid())));
			cell3.setCellValue(new HSSFRichTextString(sportOrder.getTradeNo()));
			cell4.setCellValue(new HSSFRichTextString(sportOrder.getCheckpass()));
			cell5.setCellValue(new HSSFRichTextString(DateUtil.format(sportOrder.getAddtime(), "MM-dd HH:mm:ss")));
			cell6.setCellValue(new HSSFRichTextString(sportOrder.getMobile()));
			cell7.setCellValue(new HSSFRichTextString(sportOrder.getMembername()));
			cell8.setCellValue(new HSSFRichTextString(VmUtils.readJsonToMap(sportOrder.getDescription2()).get("运动馆名")));
			cell9.setCellValue(new HSSFRichTextString(VmUtils.readJsonToMap(sportOrder.getDescription2()).get("详细")));
			cell10.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getQuantity())));
			cell11.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getQuantity())));
			cell12.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getTotalAmount())));
			cell13.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getDiscount())));
			cell14.setCellValue(new HSSFRichTextString(String.valueOf(sportOrder.getDue())));
			cell15.setCellValue(new HSSFRichTextString(sportOrder.getPaymethodText()));
			cell16.setCellValue(new HSSFRichTextString(sportOrder.getStatusText()));
		}
		String exportName = "SportOrder_" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".xls";
		response.setContentType("application/xls");
		response.addHeader("Content-Disposition", "attachment;filename=" + exportName);
		wb.write(response.getOutputStream());
	}
}
