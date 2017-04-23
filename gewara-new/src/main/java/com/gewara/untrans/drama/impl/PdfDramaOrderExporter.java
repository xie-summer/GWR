package com.gewara.untrans.drama.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.command.PdfReportUtil;
import com.gewara.constant.Status;
import com.gewara.constant.order.BuyItemConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.DramaPlayItem;
import com.gewara.model.drama.SellDramaSeat;
import com.gewara.model.drama.Theatre;
import com.gewara.model.pay.BuyItem;
import com.gewara.model.pay.OrderAddress;
import com.gewara.service.DaoService;
import com.gewara.service.drama.DramaOrderService;
import com.gewara.untrans.drama.DramaOrderExporter;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

@Service("pdfDramaOrderExporter")
public class PdfDramaOrderExporter implements DramaOrderExporter {
	
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	
	@Autowired@Qualifier("dramaOrderService")
	private DramaOrderService dramaOrderService;
	
	@Autowired@Qualifier("config")
	private Config config;
	
	public void getPdfDramaOrderDocument(Document document, List<DramaOrder> orderList){
		document.setPageSize(PageSize.A5);
		for (DramaOrder dramaOrder : orderList) {
			OrderAddress orderAddress = daoService.getObject(OrderAddress.class, dramaOrder.getTradeNo());
			try {
				PdfPTable contrainerTable = PdfReportUtil.createTable(PdfReportUtil.COLSPANS);
				float[] totalWidth = new float[PdfReportUtil.COLSPANS];
				for (int i = 0; i < PdfReportUtil.COLSPANS; i++) {
					totalWidth[i] = PdfReportUtil.ROWWIDTH;
				}
				contrainerTable.setTotalWidth(totalWidth);
				PdfPTable tableHeader = getHeaderTable(dramaOrder);
				PdfPCell cellHeader = new PdfPCell(tableHeader);
				cellHeader.setColspan(PdfReportUtil.COLSPANS);
				contrainerTable.addCell(cellHeader);
				if(orderAddress != null){
					PdfPTable addressTable = getOrderAddressTable(orderAddress);
					PdfPCell cellAddress = new PdfPCell(addressTable);
					cellAddress.setColspan(PdfReportUtil.COLSPANS);
					contrainerTable.addCell(cellAddress);
				}
				PdfPTable orderInfoTable = getOrderInfoTable(dramaOrder);
				PdfPCell cellOrderInfo = new PdfPCell(orderInfoTable);
				cellOrderInfo.setColspan(PdfReportUtil.COLSPANS);
				contrainerTable.addCell(cellOrderInfo);
				PdfPTable tableFooter = getFooterTable();
				PdfPCell cellFooter = new PdfPCell(tableFooter);
				cellFooter.setColspan(PdfReportUtil.COLSPANS);
				contrainerTable.addCell(cellFooter);
				contrainerTable.addCell(PdfReportUtil.createCell("", PdfReportUtil.getKeyfont(), Element.ALIGN_CENTER, 6, 1, false));
				contrainerTable.addCell(PdfReportUtil.createCell("收件人签字：", PdfReportUtil.getKeyfont(), Element.ALIGN_CENTER, 2, 1, false));
				contrainerTable.addCell(PdfReportUtil.createCell("", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, false));
				document.add(contrainerTable);
			} catch (DocumentException e) {
				
			}
		}
	}

	private PdfPTable getHeaderTable(DramaOrder order){
		PdfPTable tableHeader = PdfReportUtil.createTable(PdfReportUtil.COLSPANS);
		PdfPCell cell = PdfReportUtil.createCell("格瓦拉生活网客户演出票详单", PdfReportUtil.getKeyfont(), Element.ALIGN_CENTER, 7, 1, true);
		cell.setMinimumHeight(PdfReportUtil.ROWHEIGHT*2);
		tableHeader.addCell(cell);
		String url = config.getAbsPath() + config.getBasePath() + "barcode.xhtml?msg=" + order.getTradeNo();
		try {
			final int subColspan = PdfReportUtil.COLSPANS - 7;
			PdfPTable rightTableHeader = PdfReportUtil.createTable(subColspan);
			float[] totalWidth = new float[subColspan];
			for (int i = 0; i < subColspan; i++) {
				totalWidth[i] = PdfReportUtil.ROWWIDTH;
			}
			rightTableHeader.setTotalWidth(totalWidth);
			PdfPCell cellImage = PdfReportUtil.createCell(Image.getInstance(new URL(url)), Element.ALIGN_CENTER, 3);
			cellImage.setFixedHeight(29);
			rightTableHeader.addCell(cellImage);
			String value = order.getTradeNo();
			PdfPCell cell2 = PdfReportUtil.createCell(value, PdfReportUtil.getTextfont(), Element.ALIGN_LEFT, 3, 1, true);
			rightTableHeader.addCell(cell2);
			PdfPCell cellHeader = new PdfPCell(rightTableHeader);
			cellHeader.setColspan(3);
			tableHeader.addCell(cellHeader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tableHeader;
	}
	
	private PdfPTable getOrderAddressTable(OrderAddress address){
		PdfPTable addressHeader = PdfReportUtil.createTable(PdfReportUtil.COLSPANS);
		PdfPCell cell = PdfReportUtil.createCell("收货信息", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 1, 1, true);
		cell.setMinimumHeight(PdfReportUtil.ROWHEIGHT*2);
		addressHeader.addCell(cell);
		final int subColspan = PdfReportUtil.COLSPANS - 1;
		PdfPTable rightTableHeader = PdfReportUtil.createTable(subColspan);
		try {
			float[] totalWidth = new float[subColspan];
			for (int i = 0; i < subColspan; i++) {
				totalWidth[i] = PdfReportUtil.ROWWIDTH;
			}
			rightTableHeader.setTotalWidth(totalWidth);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rightTableHeader.addCell(PdfReportUtil.createCell("姓名", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell(address.getRealname(), PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("电话", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, true));
		rightTableHeader.addCell(PdfReportUtil.createCell(address.getMobile(), PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, true));
		rightTableHeader.addCell(PdfReportUtil.createCell("客服热线", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("4000-406-506", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, true));
		rightTableHeader.addCell(PdfReportUtil.createCell("地址", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell(address.gainAddress(), PdfReportUtil.getTextfont(), Element.ALIGN_LEFT, 8, 1, true));
		PdfPCell cellHeader = new PdfPCell(rightTableHeader);
		cellHeader.setColspan(9);
		addressHeader.addCell(cellHeader);
		return addressHeader;
	}
	
	private PdfPTable getOrderInfoTable(DramaOrder order){
		List<BuyItem> itemList = daoService.getObjectListByField(BuyItem.class, "orderid", order.getId());
		List<Long> theatreIdList = BeanUtil.getBeanPropertyList(itemList, "placeid", true);
		Map<Long,Theatre> theatreMap = daoService.getObjectMap(Theatre.class, theatreIdList);
		List<Long> dramaIdList = BeanUtil.getBeanPropertyList(itemList, "itemid", true);
		Map<Long,Drama> dramaMap = daoService.getObjectMap(Drama.class, dramaIdList);
		List<Long> odiIdList = BeanUtil.getBeanPropertyList(itemList, "relatedid", true);
		Map<Long,DramaPlayItem> itemMap = daoService.getObjectMap(DramaPlayItem.class, odiIdList);
		List<SellDramaSeat> sellSeatList = dramaOrderService.getDramaOrderSeatList(order.getId());
		Map<String,SellDramaSeat> sellSeatMap = BeanUtil.beanListToMap(sellSeatList, "key");
		
		PdfPTable orderInfoHeader = PdfReportUtil.createTable(PdfReportUtil.COLSPANS);
		PdfPCell cell = PdfReportUtil.createCell("收货信息", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 1, 1, true);
		cell.setMinimumHeight(PdfReportUtil.ROWHEIGHT* (itemList.size() + 1));
		orderInfoHeader.addCell(cell);
		final int subColspan = PdfReportUtil.COLSPANS - 1;
		PdfPTable rightTableHeader = PdfReportUtil.createTable(subColspan);
		try {
			float[] totalWidth = new float[subColspan];
			for (int i = 0; i < subColspan; i++) {
				totalWidth[i] = PdfReportUtil.ROWWIDTH;
			}
			rightTableHeader.setTotalWidth(totalWidth);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rightTableHeader.addCell(PdfReportUtil.createCell("场管名称", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("项目名称", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("座位", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("数量", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("单价", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("优惠金额", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("总价", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
		rightTableHeader.addCell(PdfReportUtil.createCell("演出时间", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, true));
		final boolean isSeatOrder = !sellSeatList.isEmpty();
		boolean flag = true;
		int i = 0;
		for (BuyItem item : itemList) {
			Theatre theatre = theatreMap.get(item.getPlaceid());
			String placename = (theatre != null) ? theatre.getRealBriefname():"";
			rightTableHeader.addCell(PdfReportUtil.createCell(placename, PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			Drama drama = dramaMap.get(item.getItemid());
			String itemname = (drama != null) ? drama.getRealBriefname():"";
			rightTableHeader.addCell(PdfReportUtil.createCell(itemname, PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			//座位
			if(!isSeatOrder){
				rightTableHeader.addCell(PdfReportUtil.createCell("", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			}else{
				if(flag){
					Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(item.getOtherinfo());
					String seatLabel = otherInfoMap.get(BuyItemConstant.OTHERINFO_KEY_SEATLABEL);
					if(i>0 || StringUtils.isNotBlank(seatLabel)){
						List<String> seatList = Arrays.asList(StringUtils.split(seatLabel, ","));
						List<String> seatLabelList = new ArrayList<String>();
						for (String key : seatList) {
							SellDramaSeat sellSeat = sellSeatMap.get(key);
							if(sellSeat != null){
								seatLabelList.add(sellSeat.getSeatLabel());
							}
						}
						PdfPCell seatCell = PdfReportUtil.createCell(StringUtils.join(seatLabelList, ","), PdfReportUtil.getTextfont(), Element.ALIGN_CENTER);
						rightTableHeader.addCell(seatCell);
					}else if(i==0){
						flag = false;
						List<String> seatList = BeanUtil.getBeanPropertyList(sellSeatList, "seatLabel", true);
						PdfPCell seatCell = PdfReportUtil.createCell(StringUtils.join(seatList, ","), PdfReportUtil.getTextfont(), Element.ALIGN_CENTER);
						seatCell.setMinimumHeight(PdfReportUtil.ROWHEIGHT* (itemList.size() + 1));
						rightTableHeader.addCell(seatCell);
					}
				}
			}
			rightTableHeader.addCell(PdfReportUtil.createCell(item.getQuantity()+"", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			rightTableHeader.addCell(PdfReportUtil.createCell(item.getUnitprice()+"", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			rightTableHeader.addCell(PdfReportUtil.createCell(item.getDisfee()+"", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			rightTableHeader.addCell(PdfReportUtil.createCell(item.getTotalfee()+"", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER));
			DramaPlayItem dpi = itemMap.get(item.getRelatedid());
			String playtime = "";
			if(dpi.hasPeriod(Status.N)){
				playtime = DateUtil.formatTimestamp(dpi.getPlaytime()) + "-" + DateUtil.formatTimestamp(dpi.getEndtime());
			}else{
				playtime = DateUtil.formatTimestamp(dpi.getPlaytime());
			}
			rightTableHeader.addCell(PdfReportUtil.createCell(playtime, PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 2, 1, true));
			i ++;
		}
		PdfPCell cellHeader = new PdfPCell(rightTableHeader);
		cellHeader.setColspan(9);
		orderInfoHeader.addCell(cellHeader);
		return orderInfoHeader;
	}
	
	private PdfPTable getFooterTable(){
		PdfPTable tableFooter = PdfReportUtil.createTable(PdfReportUtil.COLSPANS);
		PdfPCell cell = PdfReportUtil.createCell("收货信息", PdfReportUtil.getTextfont(), Element.ALIGN_CENTER, 1, 1, true);
		cell.setMinimumHeight(PdfReportUtil.ROWHEIGHT*2);
		tableFooter.addCell(cell);
		String text = "商品送达时请客户认真核对，若发现数量短缺或价格不符等问题，请及时联系我们客服，一旦确认收货，视为信息无误，无法退换，感谢您的配合！";
		PdfPCell cell2 = PdfReportUtil.createCell(text, PdfReportUtil.getTextfont(), Element.ALIGN_LEFT, 9, 1, true);
		cell2.setMinimumHeight(PdfReportUtil.ROWHEIGHT*2);
		tableFooter.addCell(cell2);
		return tableFooter;
	}
}
