package com.gewara.web.action.admin.member;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.gewara.command.InvoiceCommand;
import com.gewara.constant.DramaConstant;
import com.gewara.constant.InvoiceConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.ticket.PartnerConstant;
import com.gewara.model.acl.User;
import com.gewara.model.api.ApiUser;
import com.gewara.model.common.City;
import com.gewara.model.common.Province;
import com.gewara.model.pay.BaseOrderExtra;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.user.Invoice;
import com.gewara.model.user.Member;
import com.gewara.service.OrderException;
import com.gewara.service.gewapay.InvoiceService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.UntransService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;

@Controller
public class InvoiceAdminController extends BaseAdminController {
	// 发票操作列表(已申请、已邮寄、未邮寄、已快递、未开、已重新补开申请、已开)

	@Autowired@Qualifier("untransService")
	private UntransService untransService;
	public void setUntransService(UntransService untransService) {
		this.untransService = untransService;
	}

	@Autowired
	@Qualifier("invoiceService")
	private InvoiceService invoiceService;

	public void setInvoiceService(InvoiceService invoiceService) {
		this.invoiceService = invoiceService;
	}

	// 发票列表
	@RequestMapping("/admin/invoice/invoiceList.xhtml")
	public String queryInvoiceList(ModelMap model, Date fromDate, Date toDate, String order, String invoicestatus, String report, String citycode,
			Integer startAmount, Integer endAmount, Long memberid, String invoiceid, String applytype, String phone, String contactor,
			String searchMethod, Integer pageNo, String xls, String pretype) {
		if (StringUtils.isBlank(order))
			order = "addtime";
		if (pageNo == null)
			pageNo = 0;
		Integer rowsPage = 25;
		Integer firstPerPage = pageNo * rowsPage;
		Integer invoiceCount = 0;
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		if (StringUtils.isNotBlank(invoiceid) || memberid != null || StringUtils.isNotBlank(contactor) || StringUtils.isNotBlank(phone)) {// 快速查找
			searchMethod = "quickSearch";
			invoiceCount = invoiceService.getInvoiceCount(memberid, invoiceid, contactor, phone, order, null, null, pretype);
			if (StringUtils.equals(xls, "xls")) {
				invoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, 0, invoiceCount);
			} else {
				invoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, firstPerPage, rowsPage);
			}
		} else if (StringUtils.isNotBlank(citycode) || StringUtils.isNotBlank(invoicestatus)) {
			searchMethod = "complexSearch";
			invoiceCount = invoiceService.getInvoiceCount(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, applytype, pretype);
			if (StringUtils.equals(xls, "xls")) {
				invoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true,
						applytype, pretype, 0, invoiceCount);
			} else {
				invoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true,
						applytype, pretype, firstPerPage, rowsPage);
			}
		}
		Map<Long, String> memberMap = new HashMap<Long, String>();
		Map<Long, String> cityMap = new HashMap<Long, String>();
		for (Invoice invoice : invoiceList) {
			if (invoice.getMemberid() < PartnerConstant.GEWA_CLIENT) {
				Member member = daoService.getObject(Member.class, invoice.getMemberid());
				String name = member != null ? member.getNickname() : invoice.getContactor();
				memberMap.put(invoice.getId(), name);
			} else {
				ApiUser aUser = daoService.getObject(ApiUser.class, invoice.getMemberid());
				String name = aUser != null ? aUser.getPartnername() : invoice.getContactor();
				memberMap.put(invoice.getId(), name);
			}
			City city = daoService.getObject(City.class, invoice.getCitycode());
			if (city != null)
				cityMap.put(invoice.getId(), city.getCityname());
		}
		PageUtil pageUtil = new PageUtil(invoiceCount, rowsPage, pageNo, "admin/invoice/invoiceList.xhtml");
		model.put("pageUtil", pageUtil);
		Map params = new HashMap();
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("toDate", DateUtil.formatDate(toDate));
		params.put("invoicestatus", invoicestatus);
		params.put("order", order);
		params.put("startAmount", startAmount);
		params.put("endAmount", endAmount);
		params.put("memberid", memberid);
		params.put("invoiceid", invoiceid);
		params.put("applytype", applytype);
		params.put("phone", phone);
		params.put("contactor", contactor);
		params.put("citycode", citycode);
		params.put("pretype", pretype);
		pageUtil.initPageInfo(params);
		model.put("memberMap", memberMap);
		model.put("invoiceList", invoiceList);
		model.put("citycode", citycode);
		model.put("cityMap", cityMap);
		model.put("searchMethod", searchMethod);
		if (StringUtils.isNotBlank(report)) {
			List<Invoice> reportInvoiceList = new ArrayList<Invoice>();
			if (StringUtils.equals(searchMethod, "quickSearch")) {
				reportInvoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, 0, invoiceCount);
			} else if (StringUtils.equals(searchMethod, "complexSearch")) {
				reportInvoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true,
						applytype, pretype, 0, invoiceCount);
			}
			model.put(REPORT_DATA_KEY, reportInvoiceList);
			return showReportView(model, report, "invoice/invoiceList.jasper");
		}
/*		if (StringUtils.equals(xls, "xls")) {
			// download("xls", res);
			return exportInvoiceReport(invoiceList, res);
		}*/
		model.put("invoiceCount", invoiceCount);
		model.put("pretypeMap", DramaConstant.pretypeMap);
		return "admin/invoice/newInvoiceList.vm";
	}

	@RequestMapping("/admin/invoice/exportInvoiceReport.xhtml")
	public ModelAndView exportInvoiceReport(Date fromDate, Date toDate, String order, String invoicestatus, String citycode,
			Integer startAmount, Integer endAmount, Long memberid, String invoiceid, String applytype, String phone, String contactor, String pretype,
			HttpServletResponse response) {
		Integer invoiceCount = 0;
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		if (StringUtils.isNotBlank(invoiceid) || memberid != null || StringUtils.isNotBlank(contactor) || StringUtils.isNotBlank(phone)) {// 快速查找
			//searchMethod = "quickSearch";
			invoiceCount = invoiceService.getInvoiceCount(memberid, invoiceid, contactor, phone, order, null, null, pretype);
			invoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, 0, invoiceCount);
		} else if (StringUtils.isNotBlank(citycode) || StringUtils.isNotBlank(invoicestatus)) {
			//searchMethod = "complexSearch";
			invoiceCount = invoiceService.getInvoiceCount(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, applytype, pretype);
			invoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true,
					applytype, pretype, 0, invoiceCount);
		}
		
		String[] sheet1Titles = { "流水号", "付款单位(个人)名称", "纳税人识别号(证件号)", "合计人民币", "复核人", "备注1", "备注2", "备注3", "备注4" };
		String[] sheet2Titles = { "流水号", "项目名称", "计量单位", "数量", "单价", "金额" };
		// 创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建表
		HSSFSheet sheet1 = wb.createSheet("sheet1");
		HSSFSheet sheet2 = wb.createSheet("sheet2");
		// 冻结表头
		sheet1.createFreezePane(9, 1);
		sheet2.createFreezePane(6, 1);
		// 设置表格的宽度
		sheet1.setColumnWidth(0, 3000);
		sheet1.setColumnWidth(1, 10000);
		sheet1.setColumnWidth(2, 8000);
		sheet1.setColumnWidth(3, 3000);
		sheet1.setColumnWidth(4, 3000);
		sheet1.setColumnWidth(5, 3000);
		sheet1.setColumnWidth(6, 3000);
		sheet1.setColumnWidth(7, 3000);
		sheet1.setColumnWidth(8, 3000);
		sheet2.setColumnWidth(0, 3000);
		sheet2.setColumnWidth(1, 3000);
		sheet2.setColumnWidth(2, 3000);
		sheet2.setColumnWidth(3, 3000);
		sheet2.setColumnWidth(4, 3000);
		sheet2.setColumnWidth(5, 3000);
		// 创建表头
		HSSFHeader header1 = sheet1.getHeader();
		HSSFHeader header2 = sheet2.getHeader();
		// 设置标题
		header1.setCenter("sheet1");
		header2.setCenter("sheet2");
		HSSFRow sheet1HeadRow = sheet1.createRow(0);
		HSSFRow sheet2HeadRow = sheet2.createRow(0);
		
		// 列头的样式    
		HSSFFont columnHeadFont = wb.createFont();    
	    columnHeadFont.setFontName("宋体");    
	    columnHeadFont.setFontHeightInPoints((short) 10);    
	    columnHeadFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);    
	    HSSFCellStyle columnHeadStyle = wb.createCellStyle();
	    HSSFDataFormat format = wb.createDataFormat();  
	    columnHeadStyle.setDataFormat(format.getFormat("@")); 
	    columnHeadStyle.setFont(columnHeadFont);    
	    columnHeadStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中    
	    columnHeadStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中    
	    columnHeadStyle.setLocked(true);    
	    columnHeadStyle.setWrapText(true);    
	    columnHeadStyle.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色    
	    columnHeadStyle.setBorderLeft((short) 1);// 边框的大小    
	    columnHeadStyle.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色    
	    columnHeadStyle.setBorderRight((short) 1);// 边框的大小    
	    columnHeadStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单元格的边框为粗体    
	    columnHeadStyle.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色    
	    // 设置单元格的背景颜色（单元格的样式会覆盖列或行的样式）    
	    columnHeadStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
	     
	    
	    //普通常规单元格格式
	    HSSFCellStyle style = wb.createCellStyle();    
	    style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	    style.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
	    //普通文本单元格格式
	    HSSFCellStyle strCellStyle = wb.createCellStyle();    
	    strCellStyle.setDataFormat(format.getFormat("@")); 
	    strCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	    strCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		
		// 逐一设置Title的值
		for (int i = 0; i < sheet1Titles.length; i++) {
			HSSFCell headerCell = sheet1HeadRow.createCell(i);
			headerCell.setCellValue(new HSSFRichTextString(sheet1Titles[i]));
			if (i == 3) {
				sheet1.setDefaultColumnStyle(i,style);
			}else {
				sheet1.setDefaultColumnStyle(i,strCellStyle);
			}
			headerCell.setCellStyle(columnHeadStyle);
		}
		for (int i = 0; i < sheet2Titles.length; i++) {
			HSSFCell headerCell = sheet2HeadRow.createCell(i);
			headerCell.setCellValue(new HSSFRichTextString(sheet2Titles[i]));
			if (i == 4 || i ==5) {
				sheet2.setDefaultColumnStyle(i, style);
			}else {
				sheet2.setDefaultColumnStyle(i, strCellStyle);
			}
			headerCell.setCellStyle(columnHeadStyle);
		}
		HSSFRow sheet1Row = null;
		HSSFRow sheet2Row = null;
		int rownum = 1;
		for (Invoice invoice : invoiceList) {
			sheet1Row = sheet1.createRow(rownum);
			sheet2Row = sheet2.createRow(rownum);
			rownum++;
			HSSFCell cell = sheet1Row.createCell(0);
			cell.setCellValue(new HSSFRichTextString(invoice.getId() + ""));
			cell = sheet1Row.createCell(1);
			cell.setCellValue(new HSSFRichTextString(VmUtils.escapeHtml(invoice.getTitle())));
			cell = sheet1Row.createCell(3);
			cell.setCellValue(invoice.getAmount());
			cell = sheet1Row.createCell(5);
			cell.setCellValue(new HSSFRichTextString(invoice.getId() + ""));

			cell = sheet2Row.createCell(0);
			cell.setCellValue(new HSSFRichTextString(invoice.getId() + ""));
			cell = sheet2Row.createCell(1);
			cell.setCellValue(new HSSFRichTextString(StringUtils.equals(invoice.getInvoicetype(), "movienote") ? "电影票款" : StringUtils.equals(
					invoice.getInvoicetype(), "dramanote") ? "演出票款" : "运动票款"));
			cell = sheet2Row.createCell(2);
			cell.setCellValue(new HSSFRichTextString("批"));
			cell = sheet2Row.createCell(3);
			cell.setCellValue(1);
			cell = sheet2Row.createCell(4);
			cell.setCellValue(invoice.getAmount());
			cell = sheet2Row.createCell(5);
			cell.setCellValue(invoice.getAmount());
		}
		try {
			response.setContentType("application/xls");
			response.setContentType("application/x-download");
			response.addHeader("Content-Disposition", "attachment;filename=gewara"+DateUtil.format(new Date(), "yyMMdd_HHmmss")+ ".xls");
			wb.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	@RequestMapping("/admin/invoice/exportInvoiceAddressReport.xhtml")
	public ModelAndView exportInvoiceAddressReport(Date fromDate, Date toDate, String order, String invoicestatus, String citycode,
			Integer startAmount, Integer endAmount, Long memberid, String invoiceid, String applytype, String phone, String contactor, String pretype,
			HttpServletResponse response) {
		Integer invoiceCount = 0;
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		if (StringUtils.isNotBlank(invoiceid) || memberid != null || StringUtils.isNotBlank(contactor) || StringUtils.isNotBlank(phone)) {// 快速查找
			//searchMethod = "quickSearch";
			invoiceCount = invoiceService.getInvoiceCount(memberid, invoiceid, contactor, phone, order, null, null, pretype);
			invoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, 0, invoiceCount);
		} else if (StringUtils.isNotBlank(citycode) || StringUtils.isNotBlank(invoicestatus)) {
			//searchMethod = "complexSearch";
			invoiceCount = invoiceService.getInvoiceCount(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, applytype, pretype);
			invoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true,
					applytype, pretype, 0, invoiceCount);
		}
		
		String[] sheet1Titles = { "发票专用", "邮编", "地址", "收件人", "流水号", "寄件人"};
		// 创建工作本
		HSSFWorkbook wb = new HSSFWorkbook();
		// 创建表
		HSSFSheet sheet1 = wb.createSheet("Sheet1");
		// 冻结表头
		sheet1.createFreezePane(6, 1);
		// 设置表格的宽度
		sheet1.setColumnWidth(0, 4000);
		sheet1.setColumnWidth(1, 4000);
		sheet1.setColumnWidth(2,16000);
		sheet1.setColumnWidth(3, 7000);
		sheet1.setColumnWidth(4, 4000);
		sheet1.setColumnWidth(5, 9000);
		// 创建表头
		HSSFHeader header1 = sheet1.getHeader();
		// 设置标题
		header1.setCenter("Sheet1");
		HSSFRow sheet1HeadRow = sheet1.createRow(0);
		
		// 列头的样式    
		HSSFFont columnHeadFont = wb.createFont();    
	    columnHeadFont.setFontName("宋体");    
	    columnHeadFont.setFontHeightInPoints((short) 10);    
	    columnHeadFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);    
	    HSSFCellStyle columnHeadStyle = wb.createCellStyle();
	    HSSFDataFormat format = wb.createDataFormat();  
	    columnHeadStyle.setDataFormat(format.getFormat("@")); 
	    columnHeadStyle.setFont(columnHeadFont);    
	    columnHeadStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);// 左右居中    
	    columnHeadStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);// 上下居中    
	    columnHeadStyle.setLocked(true);    
	    columnHeadStyle.setWrapText(true);    
	    columnHeadStyle.setLeftBorderColor(HSSFColor.BLACK.index);// 左边框的颜色    
	    columnHeadStyle.setBorderLeft((short) 1);// 边框的大小    
	    columnHeadStyle.setRightBorderColor(HSSFColor.BLACK.index);// 右边框的颜色    
	    columnHeadStyle.setBorderRight((short) 1);// 边框的大小    
	    columnHeadStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 设置单元格的边框为粗体    
	    columnHeadStyle.setBottomBorderColor(HSSFColor.BLACK.index); // 设置单元格的边框颜色    
	    // 设置单元格的背景颜色（单元格的样式会覆盖列或行的样式）    
	    columnHeadStyle.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
	    //普通文本单元格格式
	    HSSFCellStyle strCellStyle = wb.createCellStyle();    
	    strCellStyle.setDataFormat(format.getFormat("@")); 
	    strCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
	    strCellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		
		// 逐一设置Title的值
		for (int i = 0; i < sheet1Titles.length; i++) {
			HSSFCell headerCell = sheet1HeadRow.createCell(i);
			headerCell.setCellValue(new HSSFRichTextString(sheet1Titles[i]));
			sheet1.setDefaultColumnStyle(i,strCellStyle);
			headerCell.setCellStyle(columnHeadStyle);
		}
		HSSFRow sheet1Row = null;
		int rownum = 1;
		for (Invoice invoice : invoiceList) {
			sheet1Row = sheet1.createRow(rownum);
			rownum++;
			HSSFCell cell = sheet1Row.createCell(0);
			cell.setCellValue(new HSSFRichTextString("发票专用"));
			cell = sheet1Row.createCell(1);
			cell.setCellValue(new HSSFRichTextString(invoice.getPostcode()));
			cell = sheet1Row.createCell(2);
			cell.setCellValue(new HSSFRichTextString(invoice.getAddress()));
			cell = sheet1Row.createCell(3);
			cell.setCellValue(new HSSFRichTextString(invoice.getContactor()+" "+invoice.getPhone()));
			cell = sheet1Row.createCell(4);
			cell.setCellValue(new HSSFRichTextString(invoice.getId() + ""));
			cell = sheet1Row.createCell(5);
			cell.setCellValue(new HSSFRichTextString("格瓦拉生活网（www.gewara.com）"));
		}
		try {
			response.setContentType("application/xls");
			response.setContentType("application/x-download");
			response.addHeader("Content-Disposition", "attachment;filename=invoice@address"+DateUtil.format(new Date(), "yyMMdd_HHmmss")+ ".xls");
			wb.write(response.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	// 发票详细
	@RequestMapping("/admin/invoice/invoiceInfo.xhtml")
	public String invoiceInfo(Long invoiceid, ModelMap model) {
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showMessage(model, "数据不存在！");
		Member member = daoService.getObject(Member.class, invoice.getMemberid());
		int amount = 0;
		if (member != null) {
			amount = invoiceService.getAllTotalOpenedInvoiceByMemberid(member.getId());
			List<Long> idList = BeanUtil.getIdList(invoice.getRelatedid(), ",");
			List<GewaOrder> orderList = daoService.getObjectList(GewaOrder.class, idList);
			model.put("orderList", orderList);
			if (orderList.size() < idList.size()) {
				List<Charge> chargeList = daoService.getObjectList(Charge.class, idList);
				model.put("chargeList", chargeList);
			}
		}
		model.put("amount", amount);
		model.put("invoice", invoice);
		model.put("member", member);
		dbLogger.warn(invoice.getPhone() + invoice.getApplytype());
		model.put("pretypeMap", DramaConstant.pretypeMap);
		return "admin/invoice/newInvoiceInfo.vm";
	}

	// 管理员开发票
	@RequestMapping("/admin/invoice/saveInvoice.xhtml")
	public String saveInvoice(Long invoiceid, String invoicestatus, String invoicecontent, ModelMap model) {
		User user = getLogonUser();
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (StringUtils.isBlank(invoicestatus)) {
			return showJsonError(model, "请选择发票的状态!");
		}
		if (invoice == null)
			return showJsonError(model, "数据不存在！");
		if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_TRASH))
			return showJsonError(model, "该发票已经废弃，不能在开发票！");
		if (invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_OPENED) || invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_POST_COMMON)
				|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_POST_EXPRESS)
				|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_UNPOST)
				|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_OPEN_AGAIN))
			return showJsonError(model, "此发票已开,请不要重复开具发票!");
		ChangeEntry changeEntry = new ChangeEntry(invoice);
		invoice.setOpentime(new Timestamp(System.currentTimeMillis()));
		invoice.setInvoicecontent(invoicecontent);
		if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_APPLY_AGAIN)
				&& StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_OPENED)) {// 重新补开的
			invoice.setInvoicestatus(InvoiceConstant.STATUS_OPEN_AGAIN);// 加入已补开状态
		} else {
			invoice.setInvoicestatus(invoicestatus);
		}
		invoice.setOpentime(new Timestamp(System.currentTimeMillis()));
		invoice.setAdminid(user.getId());
		daoService.saveObject(invoice);
		monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(), changeEntry.getChangeMap(invoice));
		return showJsonSuccess(model);
	}

	// 管理员批量开发票
	@RequestMapping("/admin/invoice/batchInvoiceList.xhtml")
	public String saveInvoiceList(ModelMap model, String invoiceidList) {
		if (StringUtils.isBlank(invoiceidList))
			return showJsonError(model, "参数出错！");
		User user = getLogonUser();
		List<Long> idsList = BeanUtil.getIdList(invoiceidList, ",");
		List<Invoice> invoiceList = daoService.getObjectList(Invoice.class, idsList);
		if (invoiceidList.isEmpty())
			return showJsonError(model, "批量数据不能为空！");
		for (Invoice invoice : invoiceList) {
			if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_TRASH))
				return showJsonError(model, "部分发票已经废弃，不能进行相关操作！");

			if (invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_OPENED)
					|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_POST_COMMON)
					|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_POST_EXPRESS)
					|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_UNPOST)
					|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_OPEN_AGAIN)) {
				return showJsonError(model, "部分发票已开，发票不能重复开！");
			}
			ChangeEntry changeEntry = new ChangeEntry(invoice);
			invoice.setOpentime(new Timestamp(System.currentTimeMillis()));
			invoice.setInvoicecontent("");
			if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_APPLY_AGAIN)) { // 重新申请补开的
				invoice.setInvoicestatus(InvoiceConstant.STATUS_OPEN_AGAIN);// 加入补开状态
			} else {
				invoice.setInvoicestatus(InvoiceConstant.STATUS_OPENED);
			}
			invoice.setAdminid(user.getId());
			daoService.saveObject(invoice);
			monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(), changeEntry.getChangeMap(invoice));
		}
		return showJsonSuccess(model);
	}

	// 邮寄发票状态
	@RequestMapping("/admin/invoice/postInvoice.xhtml")
	public String postInvoice(ModelMap model, Long invoiceid, String invoicestatus, String postnumber) {
		User user = getLogonUser();
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showJsonError(model, "数据不存在！");
		if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_TRASH))
			return showJsonError(model, "此发票已废弃，不能邮寄!");
		if (StringUtils.isBlank(invoicestatus)) {
			return showJsonError(model, "请选择邮寄状态!");
		}
		if (invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_APPLY_AGAIN) || invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_APPLY)
				|| invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_UNOPENED)) {
			return showJsonError(model, "此发票还未开，暂时不能邮寄!");
		}
		if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_POST_COMMON)
				&& StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_EXPRESS)
				&& StringUtils.isNotBlank(invoice.getPostnumber())) {
			return showJsonError(model, "该发票，上次是快递邮寄，且快递号不为空，请先把改发票快递号设为空！");
		}
		ChangeEntry changeEntry = new ChangeEntry(invoice);
		invoice.setInvoicestatus(invoicestatus);
		invoice.setPosttime(new Timestamp(System.currentTimeMillis()));
		invoice.setPostnumber(postnumber);
		// 邮寄发票发送短信
		sendInvoiceMessage(invoice);
		daoService.saveObject(invoice);
		monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(), changeEntry.getChangeMap(invoice));
		return showJsonSuccess(model);
	}

	// 批量合并、申请补开、快递、平邮、暂时不开
	@RequestMapping("/admin/invoice/batchOperationInvoice.xhtml")
	public String batchPostInvoice(ModelMap model, String invoicestatus, String invoiceidList) {
		if (StringUtils.isBlank(invoiceidList))
			return showJsonError(model, "发票ID参数出错！");
		User user = getLogonUser();
		List<Long> idsList = BeanUtil.getIdList(invoiceidList, ",");
		Invoice invoice = null;
		if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_OPENED))
			return showJsonError(model, "状态参数出错！");
		Long checkMemberid = null;
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		for (Long ids : idsList) {
			invoice = daoService.getObject(Invoice.class, ids);
			if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_TRASH))
				return showJsonError(model, "部分发票已经废弃，不能进行相关操作！");
			if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_POST_COMMON)
					|| StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_POST_EXPRESS)) {// 邮寄、快递
				if (invoice.getInvoicestatus().equals(InvoiceConstant.STATUS_APPLY_AGAIN)
						|| StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_APPLY)
						|| StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_UNOPENED)) {
					return showJsonError(model, "部分发票未开，暂时不能邮寄！");
				}
				if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_POST_COMMON)
						&& StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_EXPRESS)
						&& StringUtils.isNotBlank(invoice.getPostnumber())) {
					return showJsonError(model, "流水号为:" + invoice.getId() + "的发票，上次是快递邮寄，且快递号不为空，请先把改发票快递号设为空！");
				}
				invoice.setInvoicestatus(invoicestatus);
				invoice.setPosttime(new Timestamp(System.currentTimeMillis()));
				// 邮寄发票发送短信
				sendInvoiceMessage(invoice);
			} else if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_APPLY_AGAIN)) {// 申请重开
				if (invoice.getInvoicestatus().contains(InvoiceConstant.STATUS_OPEN_AGAIN))
					return showJsonError(model, "部分发票已经重开，不能在重新申请重开！");
				if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_COMMON)
						|| StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_EXPRESS)) {// 只有快递、平邮了才能重新申请
					invoice.setInvoicestatus(InvoiceConstant.STATUS_APPLY_AGAIN);
				} else {
					return showJsonError(model, "部分发票不能重新申请补开，因为发票还没邮寄出去！");
				}
			} else if (StringUtils.equals(invoicestatus, "N_MERGE")) {// 合并发票
				if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_APPLY)
						|| StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_APPLY_AGAIN)) {
					if (checkMemberid != null && !StringUtils.equals(checkMemberid + "", invoice.getMemberid() + ""))
						return showJsonError(model, "不是同一用户的发票");
					checkMemberid = invoice.getMemberid();
					if(!invoiceList.contains(invoice)){
						invoiceList.add(invoice);
					}
				} else {// 不是申请中发票不能合并
					return showJsonError(model, "部分发票不能合并，只有是申请中或申请补开才能合并！");
				}
			} else if (StringUtils.equals(invoicestatus, InvoiceConstant.STATUS_UNPOST)) {// 暂时不开
				if (StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_COMMON)
						|| StringUtils.equals(invoice.getInvoicestatus(), InvoiceConstant.STATUS_POST_EXPRESS)) {// 已经邮寄，不能更改状态成"暂时不开"
					return showJsonError(model, "已经邮寄，不能更改状态成暂时不开");
				} else {
					invoice.setInvoicestatus(InvoiceConstant.STATUS_UNOPENED);
				}
			}
			ChangeEntry changeEntry = new ChangeEntry(invoice);
			daoService.saveObject(invoice);
			monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(), changeEntry.getChangeMap(invoice));
		}
		if (!invoiceList.isEmpty()) {// 合并发票
			try{
				invoiceService.mergeInvoice(invoiceList, user);
			}catch (OrderException e) {
				return showJsonError(model, e.getMsg());
			}
		}
		return showJsonSuccess(model);
	}

	// 开放给客服查询数据
	@RequestMapping("/admin/invoice/getInvoiceList.xhtml")
	public String getInvoiceList(ModelMap model, Date fromDate, Date toDate, String order, String invoicestatus, String citycode,
			Integer startAmount, Integer endAmount, Long memberid, String invoiceid, String applytype, String phone, String contactor, String pretype, Integer pageNo) {
		if (StringUtils.isBlank(order))
			order = "addtime";
		if (pageNo == null)
			pageNo = 0;
		int rowsPage = 25;
		int firstPerPage = rowsPage * pageNo;
		Integer invoiceCount = 0;
		List<Invoice> invoiceList = new ArrayList<Invoice>();
		if (StringUtils.isNotBlank(invoiceid) || memberid != null || StringUtils.isNotBlank(contactor) || StringUtils.isNotBlank(phone)) {// 快速查找
			invoiceCount = invoiceService.getInvoiceCount(memberid, invoiceid, contactor, phone, order, null, null, pretype);
			invoiceList = invoiceService.getInvoiceList(memberid, invoiceid, contactor, phone, order, false, null, null, pretype, firstPerPage, rowsPage);
		} else if (StringUtils.isNotBlank(citycode) || StringUtils.isNotBlank(invoicestatus)) {
			invoiceCount = invoiceService.getInvoiceCount(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, applytype, pretype);
			invoiceList = invoiceService.getInvoiceList(startAmount, endAmount, fromDate, toDate, citycode, invoicestatus, order, true, applytype, pretype,
					firstPerPage, rowsPage);
		}
		Map<Long, String> memberMap = new HashMap<Long, String>();
		Map<Long, String> cityMap = new HashMap<Long, String>();
		for (Invoice invoice : invoiceList) {
			if (invoice.getMemberid() < PartnerConstant.GEWA_CLIENT) {
				Member member = daoService.getObject(Member.class, invoice.getMemberid());
				String name = member != null ? member.getNickname() : invoice.getContactor();
				memberMap.put(invoice.getId(), name);
			} else {
				ApiUser aUser = daoService.getObject(ApiUser.class, invoice.getMemberid());
				String name = aUser != null ? aUser.getPartnername() : invoice.getContactor();
				memberMap.put(invoice.getId(), name);
			}
			City city = daoService.getObject(City.class, invoice.getCitycode());
			if (city != null)
				cityMap.put(invoice.getId(), city.getCityname());
		}
		Map params = new HashMap();
		PageUtil pageUtil = new PageUtil(invoiceCount, rowsPage, pageNo, "admin/invoice/getInvoiceList.xhtml");
		params.put("fromDate", DateUtil.formatDate(fromDate));
		params.put("toDate", DateUtil.formatDate(toDate));
		params.put("invoicestatus", invoicestatus);
		params.put("order", order);
		params.put("startAmount", startAmount);
		params.put("endAmount", endAmount);
		params.put("memberid", memberid);
		params.put("invoiceid", invoiceid);
		params.put("applytype", applytype);
		params.put("phone", phone);
		params.put("contactor", contactor);
		params.put("pretype", pretype);
		pageUtil.initPageInfo(params);
		model.put("memberMap", memberMap);
		model.put("invoiceList", invoiceList);
		model.put("pageUtil", pageUtil);
		model.put("cityMap", cityMap);
		model.put("invoiceCount", invoiceCount);
		model.put("pretypeMap", DramaConstant.pretypeMap);
		return "admin/invoice/customServiceInvoiceList.vm";
	}

	// 后台管理员申请发票
	@RequestMapping("/admin/invoice/getInvoiceInfo.xhtml")
	public String getInvoiceInfo(ModelMap model, String orderno) {
		if (StringUtils.isBlank(orderno)){
			return showJsonError(model, "订单号不能为空！");
		}
		orderno = orderno.replaceAll("，", ",").replaceAll(",,", ",");
		List<String> tradeNoList = Arrays.asList(StringUtils.split(orderno, ","));
		Set<String> tradeNoSet = new HashSet<String>(tradeNoList);
		Long memberid = null;
		String membername = "";
		if(tradeNoSet.isEmpty()) return showJsonError(model, "订单号不能为空！");
		String tradeNo = tradeNoList.get(0);
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null){
			Charge charge = daoService.getObjectByUkey(Charge.class, "tradeNo", tradeNo, false);
			if(charge == null) return showJsonError(model, tradeNo + "订单号不能存在！");
			memberid = charge.getMemberid();
			membername = charge.getMembername();
		}else{
			memberid = order.getMemberid();
			membername = order.getMembername();
		}
		Timestamp cur = DateUtil.getCurFullTimestamp();
		try{
			Map<BaseOrderExtra, GewaOrder> extraMap = invoiceService.validDataTradeNo(memberid, tradeNoSet, cur, false);
			Set<BaseOrderExtra> extraList = extraMap.keySet();
			List<String> tradeList = BeanUtil.getBeanPropertyList(extraList, "tradeno", true);
			Set<String> chargeNoSet = new HashSet<String>(tradeNoSet);
			chargeNoSet.removeAll(tradeList);
			List<Charge> chargeList = invoiceService.validDataCharge(memberid, chargeNoSet, cur, false);
			Map jsonMap = new HashMap();
			jsonMap.put("memberid", memberid);
			jsonMap.put("membername", membername);
			int total = 0;
			for(GewaOrder gewaOrder : extraMap.values()){
				total += gewaOrder.gainInvoiceDue();
			}
			for(Charge charge : chargeList){
				total += charge.getFee();
			}
			jsonMap.put("totalfee", total);
			return showJsonSuccess(model, jsonMap);
		}catch (OrderException e) {
			return showJsonError(model, e.getMsg());
		}
	}

	// 保存客服申请发票数据
	@RequestMapping("/admin/invoice/saveApplyInvoice.xhtml")
	public String saveApplyInvoice(ModelMap model, InvoiceCommand invoiceCommand) {
		if (StringUtils.isBlank(invoiceCommand.getOrderidList()))
			return showJsonError(model, "申请的发票关联的订单号不能空！");
		if (StringUtils.isBlank(invoiceCommand.getTitle()))
			return showJsonError(model, "发票抬头不能为空！");
		if (StringUtils.isBlank(invoiceCommand.getContactor()))
			return showJsonError(model, "收件人不能为空！");
		if (StringUtils.isBlank(invoiceCommand.getPhone()))
			return showJsonError(model, "电话不能为空！");
		if (StringUtils.isBlank(invoiceCommand.getAddress()))
			return showJsonError(model, "邮寄地址不能为空！");
		if (StringUtils.isBlank(invoiceCommand.getCitycode()))
			return showJsonError(model, "邮寄城市不能为空！");
		if (StringUtils.isBlank(invoiceCommand.getPostcode()))
			return showJsonError(model, "邮政编码不能为空！");
		if (!ValidateUtil.isPhone(invoiceCommand.getPhone()))
			return showJsonError(model, "您输入的电话格式不正确!");
		if (!ValidateUtil.isPostCode(invoiceCommand.getPostcode()))
			return showJsonError(model, "您输入的邮政编码格式不正确！");
		User user = getLogonUser();
		try {
			ErrorCode code = invoiceService.receiveInvoice(user, invoiceCommand);
			if (!code.isSuccess()) {
				return showJsonError(model, code.getMsg());
			}
			return showJsonSuccess(model);
		} catch (OrderException e) {
			return showJsonError(model, e.getMsg());
		}
	}

	// 废弃发票
	@RequestMapping("/admin/invoice/trashInvoice.xhtml")
	public String trashInvoice(ModelMap model, Long invoiceid) {
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showJsonError(model, "数据部存在!");
		ErrorCode code = invoiceService.updateInvoiceTrash(invoice, getLogonUser());
		if (!code.isSuccess()) {
			return showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/invoice/modifyInvoice.xhtml")
	public String modifyInvoice(ModelMap model, Long invoiceid) {
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showJsonError(model, "参数错误！");
		model.put("invoice", invoice);
		model.put("citycode", invoice.getCitycode());
		return "admin/invoice/customServiceModifyInvoice.vm";
	}

	@RequestMapping("/admin/invoice/saveModifyInvoice.xhtml")
	public String saveModifyInovice(ModelMap model, Long invoiceid, String title, String phone, String postcode, String contactor, String address,
			String citycode, String invoicetype) {
		if (StringUtils.isBlank(invoicetype))
			return showJsonError(model, "票据类型不能为空！");
		if (StringUtils.isBlank(title))
			return showJsonError(model, "发票抬头不能为空！");
		if (StringUtils.isBlank(contactor))
			return showJsonError(model, "收件人不能为空！");
		if (StringUtils.isBlank(phone))
			return showJsonError(model, "电话不能为空！");
		if (StringUtils.isBlank(citycode))
			return showJsonError(model, "邮寄城市不能为空！");
		if (StringUtils.isBlank(address))
			return showJsonError(model, "邮寄地址不能为空！");
		if (StringUtils.isBlank(postcode))
			return showJsonError(model, "邮政编码不能为空！");
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showJsonError(model, "参数错误！");
		User user = getLogonUser();
		ChangeEntry changeEntry = new ChangeEntry(invoice);
		invoice.setAddress(address);
		invoice.setCitycode(citycode);
		invoice.setContactor(contactor);
		invoice.setInvoicetype(invoicetype);
		invoice.setPhone(phone);
		invoice.setPostcode(postcode);
		invoice.setTitle(title);
		daoService.saveObject(invoice);
		monitorService.saveChangeLog(user.getId(), Invoice.class, invoice.getId(), changeEntry.getChangeMap(invoice));
		return showJsonSuccess(model);
	}

	// 删除发票记录
	@RequestMapping("/admin/invoice/deleteInvoice.xhtml")
	public String deleteInvoice(ModelMap model, Long invoiceid) {
		Invoice invoice = daoService.getObject(Invoice.class, invoiceid);
		if (invoice == null)
			return showJsonError(model, "数据不存在！");
		daoService.removeObject(invoice);
		monitorService.saveDelLog(getLogonUser().getId(), invoiceid, invoice);
		return showJsonSuccess(model);
	}

	// 请求城市地址
	@RequestMapping("/admin/invoice/ajaxLoadAddress.xhtml")
	public String ajaxLoadAddress(String tag, String provincecode, String citycode, String agtag, ModelMap model) {
		List<City> cityList = new ArrayList<City>();
		List<Province> provinceList = new ArrayList<Province>();
		if (StringUtils.isBlank(citycode)) {
			if (StringUtils.isBlank(tag)) {
				provinceList = placeService.getAllProvinces();
				List<Map> provinceMap = BeanUtil.getBeanMapList(provinceList, "provincecode", "provincename");
				model.put("provinceMap", provinceMap);
			} else if (StringUtils.equals(tag, "province")) {
				cityList = placeService.getCityByProvinceCode(provincecode);
				List<Map> cityMap = BeanUtil.getBeanMapList(cityList, "citycode", "cityname");
				model.put("cityMap", cityMap);
			}
			model.put("agtag", agtag);
		} else {
			if (StringUtils.equals(tag, "province")) {
				cityList = placeService.getCityByProvinceCode(provincecode);
				List<Map> cityMap = BeanUtil.getBeanMapList(cityList, "citycode", "cityname");
				model.put("cityMap", cityMap);
			} else {
				provinceList = placeService.getAllProvinces();
				List<Map> provinceMap = BeanUtil.getBeanMapList(provinceList, "provincecode", "provincename");
				model.put("provinceMap", provinceMap);
				City city = daoService.getObject(City.class, citycode);
				model.put("provincecode", city.getProvince().getProvincecode());
				cityList = placeService.getCityByProvinceCode(city.getProvince().getProvincecode());
				List<Map> cityMap = BeanUtil.getBeanMapList(cityList, "citycode", "cityname");
				model.put("cityMap", cityMap);
			}
			model.put("citycode", citycode);
		}
		return "admin/invoice/locationAddress.vm";
	}

	private void sendInvoiceMessage(Invoice invoice) {
		if (ValidateUtil.isMobile(invoice.getPhone())) {
			// 发送短信提示
			Timestamp curtime = new Timestamp(System.currentTimeMillis());
			String msgContent = " 您的发票已由平邮的方式寄送，预计将会在近期送达，请注意查收，如在15个工作日内未收到请及时和我们联系！";
			SMSRecord sms = new SMSRecord(invoice.getPhone());
			sms.setTradeNo("invoice" + invoice.getId());
			sms.setContent(msgContent);
			sms.setSendtime(curtime); //
			sms.setSmstype(SmsConstant.SMSTYPE_INVOICE);
			sms.setValidtime(DateUtil.addHour(curtime, 12));
			sms.setRelatedid(invoice.getId());
			untransService.addMessage(sms);
		}
	}
}