package com.gewara.helper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;

import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;

public final class PassbookUtil {
	private static final String passId = "pass.gewara.testpassbook";
	public static String getPassJson(TicketOrder order, OpenPlayItem opi, String address, String ticketMsg,Cinema cinema){
		//1、top
		Map top = new HashMap();
		top.put("formatVersion", 1);
		top.put("passTypeIdentifier", passId);
		String organizationName = opi.getMoviename() + " " + DateUtil.format(opi.getPlaytime(), "HH:mm") + " " + opi.getCinemaname();
		top.put("organizationName",organizationName);
		top.put("serialNumber", order.getTradeNo());
		top.put("teamIdentifier", "UWJVY6ERGE");
		
		//String authenticationToken = order.getTradeNo() + ":" + StringUtil.md5(order.getTradeNo()+ "xydkdjei"); 
		//map.put("webServiceURL", "https://www.gewara.com/mobile/getPassport.xhtml");
		//map.put("authenticationToken", authenticationToken);
		top.put("relevantDate", DateUtil.format(opi.getPlaytime(), "yyyy-MM-dd HH:mm:ss").replace(" ", "T") + "+08:00");
		top.put("logoText", "格瓦拉@电影");
		top.put("description", "取票");
		top.put("foregroundColor", "rgb(255, 255, 255)");
		top.put("backgroundColor", "rgb(60, 65, 76)");
		if(StringUtils.isNotBlank(cinema.getPointx()) && StringUtils.isNotBlank(cinema.getPointy())){
			double pointx = 0;
			double pointy = 0;
			try {
				pointx = Double.parseDouble(cinema.getPointx());
				pointy = Double.parseDouble(cinema.getPointy());
				List locations = new ArrayList(1);
				Map location = new HashMap();
				location.put("longitude", pointx);
				location.put("latitude", pointy);
				location.put("relevantText","在附近「" + opi.getCinemaname() + "」观影");
				locations.add(location);
				top.put("locations", locations);
			} catch (NumberFormatException e) {
			}
		}
		//~~children
		//2、--->barcode
		Map barcode = new HashMap();
		top.put("barcode", barcode);
		barcode.put("message", "barcodeMessage");
		barcode.put("format", "PKBarcodeFormatQR");
		barcode.put("messageEncoding", "utf8");
		//3. eventTicket
		Map eventTicket = new HashMap();
		top.put("eventTicket", eventTicket);
		
		List primaryFields = new ArrayList(1);
		List secondaryFields = new ArrayList(1);
		List auxiliaryFields = new ArrayList(1);
		List backFields = new ArrayList(1);
		
		eventTicket.put("primaryFields", primaryFields);
		eventTicket.put("secondaryFields", secondaryFields);
		eventTicket.put("auxiliaryFields", auxiliaryFields);
		eventTicket.put("backFields", backFields);

		//3.1 primaryFields
		Map tmp = new HashMap();
		tmp.put("key", "movieName");
		tmp.put("label", "影片");
		tmp.put("value", opi.getMoviename());
		primaryFields.add(tmp);
		
		//3.2 secondaryFields
		tmp = new HashMap();
		tmp.put("key", "cinemaName");
		tmp.put("label", "影院");
		tmp.put("value", opi.getCinemaname());
		secondaryFields.add(tmp);
		
		//3.3 auxiliaryFields
		tmp = new HashMap();
		tmp.put("key", "movieSession");
		tmp.put("label", "场次");
		tmp.put("value", DateUtil.format(opi.getPlaytime(), "M月d日 HH:mm"));
		auxiliaryFields.add(tmp);

		Map descMap = JsonUtils.readJsonToMap(order.getDescription2());
		
		tmp = new HashMap();
		tmp.put("key", "seatList");
		tmp.put("label", "座位");
		tmp.put("value", descMap.get("影票").toString().replaceAll("座\\d+元", "座"));
		auxiliaryFields.add(tmp);

		tmp = new HashMap();
		tmp.put("key", "ticketAmount");
		tmp.put("label", "数量");
		tmp.put("value", order.getQuantity());
		auxiliaryFields.add(tmp);
		
		tmp = new HashMap();
		tmp.put("key", "cinemaAddress");
		tmp.put("label", "影院地址");
		tmp.put("value", address);
		backFields.add(tmp);
		
		tmp = new HashMap();
		tmp.put("key", "getTicketSms");
		tmp.put("label", "取票短信");
		tmp.put("value", ticketMsg);
		backFields.add(tmp);

		tmp = new HashMap();
		tmp.put("key", "servicePhoneNumber");
		tmp.put("label", "客服电话");
		tmp.put("value", "4000-406-506");
		backFields.add(tmp);
		
		//result
		String result = JsonUtils.writeObjectToJson(top);
		return result;
	}
	public static byte[] sign(byte[] content) throws Exception {
		return PassSignUtil.sign(content);
	}
	public static void main(String[] args) throws Exception {
		//byte[] signed = IOUtils.toByteArray(new FileInputStream("F:\\passbook\\manifest.json"));
		//CAUtil.doCheck(content, sign, publicKey)
		//byte[] ss = sign("sdflskjdflsdjf".getBytes());
		//System.out.println(Hex.encodeHexString(ss));
	}
	public static String rsa(byte[] content) throws Exception{
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		byte[] encodedPassword = md.digest(content);
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < encodedPassword.length; i++) {
			if ((encodedPassword[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString(encodedPassword[i] & 0xff, 16));
		}
		return buf.toString();
	}
	public static ZipOutputStream getZipStream(OutputStream os) {
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os));
		return zos;
	}
	public static void addEntry(ZipOutputStream zos, String entryName, byte[] content){
		ZipEntry entry = new ZipEntry(entryName);
		try {
			zos.putNextEntry(entry);
			zos.write(content);
			zos.closeEntry();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
