package com.gewara.web.action.common;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.MemberConstant;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.user.ShareMember;
import com.gewara.mongo.MongoService;
import com.gewara.service.ticket.TicketOrderService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.GewaPicService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.untrans.drama.DramaOrderExporter;
import com.gewara.util.DateUtil;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.AnnotationController;
import com.gewara.xmlbind.activity.RemoteActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

@Controller
public class TestController extends AnnotationController {
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired@Qualifier("gewaPicService")
	private GewaPicService gewaPicService;
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	@Autowired@Qualifier("ticketOrderService")
	private TicketOrderService ticketOrderService;
	
	private static Map orderMap = new HashMap<String, Order>();
	static{
		orderMap.put("name", Order.asc("name"));
		orderMap.put("clickedtimes", Order.desc("clickedtimes"));
		orderMap.put("generalmark", Order.desc("avggeneral"));
		orderMap.put("environmentmark", Order.desc("avgenvironment"));
		orderMap.put("servicemark", Order.desc("avgservice"));
		orderMap.put("pricemark", Order.desc("avgprice"));
		orderMap.put("audiomark", Order.desc("avgaudio"));
	}
	@RequestMapping("/testTradeNo.xhtml")
	@ResponseBody
	public String testTradeNo(){
		String tradeNo = ticketOrderService.getTicketTradeNo();
		return "success:" + tradeNo;
	}
	
	@RequestMapping("/testTimeout.xhtml")
	@ResponseBody
	public String testTimeout(int sleep){
		long cur = System.currentTimeMillis();
		long last = cur + 1000*sleep;
		while (last - System.currentTimeMillis()> 0){
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		long time = System.currentTimeMillis() - cur;
		dbLogger.warn(""+time);
		return new Date(cur) + "---->" + new Date() + "--->"+time;
	}
	@RequestMapping("/admin/sendTestMsg.xhtml")
	@ResponseBody
	public String sendTestMsg() throws Exception{
		String s = DateUtil.getCurFullTimestampStr();
		jmsService.delaySendMsgToDst("testQueue", "testTag", s, 120000);
		return s;
	}
	@RequestMapping("/testParse.xhtml")
	public String testParse(ModelMap model) {
		model.put("parse", "#parse('include/accusation.vm')");
		return "test.vm";
	}
	//批量上传图片
	@RequestMapping("/drama/theatreListMap.xhtml")
	public String theatreListMap() {
		return "drama/theatre/theatreListMap.vm";
	}
	//批量上传图片
	@RequestMapping("/drama/theatreListNearby.xhtml")
	public String theatreListNearby() {
		return "drama/theatre/theatreListNearby.vm";
	}
	//批量上传图片
	@RequestMapping("/upImage.xhtml")
	public String testUp() {
		return "subject/upImage.vm";
	}
	//改造选时间下拉框案例
	@RequestMapping("/select.xhtml")
	public String select() {
		return "subject/select.vm";
	}
	//对话框案例
	@RequestMapping("/load.xhtml")
	public String load() {
		return "subject/load.vm";
	}
	//异步history
	@RequestMapping("/history.xhtml")
	public String history() {
		List<Map> xxxx = new ArrayList<Map>();
		Map map = new HashMap();
		map.put("xxxx", xxxx);
		xxxx.add(map);
		
		dbLogger.warn(JsonUtils.writeObjectToJson(xxxx));
		
		//model.put("orderMap", map);
		//model.put("xxxx", Arrays.asList("xxxx","yyyy"));
		return "subject/history.vm";
	}
	@RequestMapping("/testMongo.xhtml")
	@ResponseBody
	public String testMongo(){
		Map<String, Object> jsonMap = new HashMap<String, Object>();
		jsonMap.put("id", "123");
		jsonMap.put("adate", new Date());
		jsonMap.put("adatetime", DateUtil.getCurFullTimestamp());
		mongoService.addMap(jsonMap, "id", "test.mongo.bo");
		return "ok";
	}
	
	@RequestMapping("/testNew.dhtml")
	public String testNew2(ModelMap model){
		return showMessage(model, "" + new Date());
	}
	@RequestMapping("/home/cn.xhtml")
	@ResponseBody
	public String testCN(String cn){
		dbLogger.warn(cn);
		return "dddd"+cn + "sss";
	}
	@RequestMapping("/pubsale.xhtml")
	public String pubsale(){
		return "exchange/pubsale/index.vm";
	}
	@RequestMapping("/testwalatemplate.xhtml")
	public String testwalatemplate(ModelMap model){
		model.put("moderate", "哇啦拜金");
		return "testwalatemplate.vm";
	}
	
	@RequestMapping("/testMember.xhtml")
	public String testMember(){
		return "testMovie1.vm";
	}
	
	@RequestMapping("/testCreatePic.xhtml")
	public String createPic(ModelMap model){
		try {
		   String str = "TEL:13585915161";// 二维码内容
		   str = "MATMSG:TO:shusong.liu@gewara.com;SUB:二维码测试;BODY:你好！测试成功吗？;;";
		   String path = "/qrcode/";
		   Hashtable hints= new Hashtable(); 
		   hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
		   BitMatrix byteMatrix; 
		   byteMatrix= new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 200, 200,hints);
		   ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		   BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(byteMatrix);
		   ImageIO.write(bufferedImage, "png", pngOutputStream);
		   String filename = gewaPicService.saveToTempPic(new ByteArrayInputStream(pngOutputStream.toByteArray()), "png");
		   gewaPicService.addWaterMark(gewaPicService.getTempFilePath(filename));
		   String filepath = gewaPicService.moveRemoteTempTo(1l, "qrcode",null, path, filename);
		   //gewaPicService.addWarterMark("/userfiles/image/201109/s_540bff37_1324c0207be__7ff6.jpg", "/images/201007/headpic/36fa372a-cbe5-47fe-9b83-7d1699cb07cc.jpg", 10, 10, 0.5f);
		   model.put("url", filepath);
		} catch (Exception e) {
			dbLogger.error("", e);
		}
		return "test.vm";
	}
	
	@RequestMapping("/testRequest.xhtml")
	public String showTest(){
		return "test.vm";
	}
	
	@RequestMapping("/testWindowopen.xhtml")
	public String testWindowopen(){
		return "testMovie1.vm";
	}
	
	@RequestMapping("/testSpringMVC.xhtml")
	public String testSpringMVC(ModelMap model){
		List<Map> list = new ArrayList<Map>();
		for(int i=0; i<3; i++){
			Map<String, String> dataMap = new HashMap<String, String>();
			dataMap.put("name", "zhangsan" + i);
			dataMap.put("rid", "1000" + i);
			list.add(dataMap);
		}
		String json = JsonUtils.writeObjectToJson(list);
		model.put("json", json);
		
		
		return "testMovie1.vm";
	}
	
	/***
	 * www.mtag.cn 组群/用户 爬虫
	 * 
	 * 方式: 活动 + 小组 + 关注/被关注
	 *  1. 活动入口
	 *    http://www.mtag.cn/Event/EventPeople.aspx?activityId=26&other=1
	 *    http://www.mtag.cn/Event/EventPeople.aspx?activityId=118&other=1 
	 
	 *  2. 群组: http://www.mtag.cn/Group/GroupMember.aspx?groupId=59 目前最大为59
	 *  	实现逻辑: 遍历id, 设最大值为100, 当 HttpResponse返回值中 title为 "- Mtag聚影网" 时表示不存在. 可记录真实用户组数.
	 *  	
	 *  	实现逻辑: 读取http://www.mtag.cn/Group/GroupMember.aspx?groupId=? 中的HttpResponse
	 *    正则匹配 FriendsMovieTag/Default.aspx?profileId=?( 组长会与组员重复, 数据可忽略不计)

	 *  3. 关注:
	 *  	http://www.mtag.cn/FriendsMovieTag/Attentions.aspx?term=ma&profileId=zhengwei880130@126.com
	 *  
	 *  4. 被关注数:
	 *  	http://www.mtag.cn/FriendsMovieTag/Attentions.aspx?term=am&profileId=zhengwei880130@126.com
	 *  	
	 *		-- 以上.
	 *    
	 * */
	
	@RequestMapping("/robotMtag_activity.xhtml")
	public String robotMtag_activity(ModelMap model, HttpServletResponse response){
		Set<String> norepeat = new HashSet<String>();
		int membercount = 0;
		for(int i=28; i<=28; i++){
			String url = "http://www.mtag.cn/Event/EventPeople.aspx?activityId="+i+"&other=1";
			HttpResult resp = HttpUtils.getUrlAsString(url);
			String catchRobot = resp.getResponse();
			
			List<String> hrefs = WebUtils.getNodeAttrList(catchRobot, "a", "href");
			for(String a : hrefs){
				if(StringUtils.contains(a, "/FriendsMovieTag/Default.aspx?profileId=")){
					a = StringUtils.replace(a, "/FriendsMovieTag/Default.aspx?profileId=", "");
					norepeat.add(a);
					membercount++;
				}
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				dbLogger.error("", e);
			}
		}
		model.put("membercount", membercount);
		model.put("members", new ArrayList<String>(norepeat));
		
		download("xls", response);
		return "testMovie1.vm";
	}
	
	@RequestMapping("/testTemplate.xhtml")
	public String testTemplate(){
		return "common/newsTemplate/index.vm";
	}
	/***
	 *  1. sina 指定用户 查询微博接口
	 * */
	@RequestMapping("/testSina.xhtml")
	@ResponseBody
	public String testSina(){
		String url = "https://api.weibo.com/2/statuses/user_timeline.json";
		String appKey = "2536251945"; // 0157289cf7bf64f72c31ba3415991d7e
		String meid = "1612233153";
		url += "?source=" + appKey + "&uid=" + meid;
		
		HttpResult code  = HttpUtils.getUrlAsString(url);
		String retData = code.getResponse();
		System.out.println(retData);
		return retData;
	}
	/***
	 *  2. Ku6 查询电影关联的预告片
	 * */
	@RequestMapping("/testKu6.xhtml")
	public String testKu6(ModelMap model){
		String moviename = "绿灯侠";
		String url = "http://so.ku6.com/api/v?format=json&cid=104000&order=date&q=" + moviename;
		HttpResult code  = HttpUtils.getUrlAsString(url);
		
		Map data = JsonUtils.readJsonToMap(code.getResponse());
		//List<Map> map = (List<Map>)((Map)data.get("data")).get("list");
		model.put("data", data);
		return "testMovie1.vm";
	}
	protected static String encryptString(Long memberid) {
		String time = DateUtil.format(DateUtils.addMonths(new Date(System.currentTimeMillis()), 1), "yyyyMM");
		String result = memberid + time;
		result += "@" + StringUtil.md5(result+"lkjwe09ijlwejfo9ijx").substring(0, 8);
		return result;
	}
	@RequestMapping("/requestInfo.xhtml")/**请不要删除**/
	public String requestInfo(HttpServletRequest request,ModelMap model){
		Map<String,String> requestInfoMap = new HashMap<String, String>();
		Enumeration enu = request.getHeaderNames();
		System.out.println(enu.toString());
		while(enu.hasMoreElements()){
			String name = enu.nextElement().toString();
			try{
				Enumeration enus = request.getHeaders(name);
				while(enus.hasMoreElements()){
					String names = enus.nextElement().toString();
					System.out.println(name+"="+names);
					requestInfoMap.put(name, names);
					break;
				}
			}catch(Exception e){}
		}
		model.put("requestMap", requestInfoMap);
		return "testRequest.vm";
	}
	@RequestMapping("/wap/requestInfo.xhtml")/**请不要删除**/
	public String wapRequestInfo(HttpServletRequest request,ModelMap model){
		Map<String,String> requestInfoMap = new HashMap<String, String>();
		Enumeration enu = request.getHeaderNames();
		System.out.println(enu.toString());
		while(enu.hasMoreElements()){
			String name = enu.nextElement().toString();
			try{
				Enumeration enus = request.getHeaders(name);
				while(enus.hasMoreElements()){
					String names = enus.nextElement().toString();
					System.out.println(name+"="+names);
					requestInfoMap.put(name, names);
					break;
				}
			}catch(Exception e){}
		}
		model.put("requestMap", requestInfoMap);
		TicketOrder order = daoService.getObject(TicketOrder.class, 32407920l);
		model.put("tradeno", order.getTradeNo());
		if(order.isAllPaid()){
			model.put("descMap", JsonUtils.readJsonToMap(order.getDescription2()));
			model.put("due",order.getDue());
			model.put("success", true);
		}else{
			model.put("success", false);
		}
		model.put("order", order);
		return "wap/mobileOrderResult.vm";
	}
	
	//社区我发表的帖子
	@RequestMapping("/sns/index.xhtml")
	public String myposts() {
		return "sns/index_demo.vm";
	}
	//ued
	@RequestMapping("/ued/index.xhtml")
	public String udeIndex() {
		return "ued/index.vm";
	}
	//ued
	@RequestMapping("/ued/cssRule.xhtml")
	public String cssRule() {
		return "ued/cssRule.vm";
	}
	//演出宽屏首页
	@RequestMapping("drama/wide_index.xhtml")
	public String wide_index() {
		return "drama/wide_index.vm";
	}
	//场馆列表页
	@RequestMapping("drama/wide_theatreList.xhtml")
	public String wide_theatreList() {
		return "drama/wide_theatreList.vm";
	}
	//场馆地图页
	@RequestMapping("drama/wide_theatreMap.xhtml")
	public String wide_theatreMap() {
		return "drama/wide_theatreMap.vm";
	}
	//演出列表页
	@RequestMapping("drama/wide_dramaList.xhtml")
	public String wide_dramaList() {
		return "drama/wide_dramaList.vm";
	}
	//演出评论页
	@RequestMapping("drama/wide_dramaComment.xhtml")
	public String wide_dramaComment() {
		return "drama/wide_dramaComment.vm";
	}
	//演出资讯页
	@RequestMapping("drama/wide_dramaInfo.xhtml")
	public String wide_dramaInfo() {
		return "drama/wide_dramaInfo.vm";
	}
	//剧院详情页
	@RequestMapping("drama/wide_theatreDetail.xhtml")
	public String wide_theatreDetail() {
		return "drama/wide_theatreDetail.vm";
	}
	//演出详情页
	@RequestMapping("drama/wide_dramaDetail.xhtml")
	public String wide_dramaDetail() {
		return "drama/wide_dramaDetail.vm";
	}
	//剧社列表页
	@RequestMapping("drama/wide_troupeList.xhtml")
	public String wide_troupeList() {
		return "drama/wide_troupeList.vm";
	}
	//剧社列表详情页
	@RequestMapping("drama/wide_troupeDetail.xhtml")
	public String wide_troupeDetail() {
		return "drama/wide_troupeDetail.vm";
	}
	//明星列表页
	@RequestMapping("drama/wide_dramaStarList.xhtml")
	public String wide_dramaStarList() {
		return "drama/wide_dramaStarList.vm";
	}
	//明星详情页
	@RequestMapping("drama/wide_dramaStarDetail.xhtml")
	public String wide_dramaStarDetail() {
		return "drama/wide_dramaStarDetail.vm";
	}
	@RequestMapping("/testLogon.xhtml")/**不能删除，登录测试用*/
	public String ajaxCookieTest(ModelMap model){
		return this.showJsonSuccess(model,  ""+System.currentTimeMillis());
	}
	
	@RequestMapping("/recordError.xhtml")/**不能删除，登录测试用*/
	public String recordError(HttpServletRequest request){
		
		System.out.println(gainIpAddr(request));
		System.out.println("RequestHeader:" + WebUtils.getHeaderStr(request));
		return "redirect:/index.xhtml";
		
	}
	
	private String gainIpAddr(HttpServletRequest request) {
	     String ipAddress = "";
	     ipAddress = request.getHeader("x-forwarded-for");
	     if(StringUtils.isNotBlank(ipAddress) && !ipAddress.equalsIgnoreCase("unknown"))
	     {
	    	 return "x-forwarded-for:" + ipAddress;
	     }
	     
	     ipAddress = request.getHeader("Proxy-Client-IP");
	     if(StringUtils.isNotBlank(ipAddress) && !ipAddress.equalsIgnoreCase("unknown"))
	     {
	    	 return "Proxy-Client-IP:" + ipAddress;
	     }
	     
	     ipAddress = request.getHeader("WL-Proxy-Client-IP");
	     if(StringUtils.isNotBlank(ipAddress) && !ipAddress.equalsIgnoreCase("unknown"))
	     {
	    	 return "WL-Proxy-Client-IP:" + ipAddress;
	     }
	     
	     return ipAddress;
	}
	
	@RequestMapping("/taskComments.xhtml")
	@ResponseBody
	public String testTaskComments(){
		//ErrorCode code = synchActivityService.addClickedtimes(3942519L);
		//ErrorCode code = synchActivityService.collectActivity(2631L, 3942519L);
		//ErrorCode<RemoteActivity> code = synchActivityService.joinActivity(2631, activityid, sex, realname, mobile, 2, joinDate, walaAddress);
		ErrorCode<RemoteActivity> code = synchActivityService.cancelActivity(5038733L, 2631L);
		return code.getRetval()+"";
	}
	
	@RequestMapping("/getShareMember.xhtml")
	public String getShareMember(ModelMap model){
		ShareMember shareMember = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA), 38700620L).get(0);
		Map<String,String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
		String token = otherMap.get("token");
		Map params = new HashMap();
		params.put("access_token", token);
		params.put("uid", shareMember.getLoginname());
		HttpResult result = HttpUtils.getUrlAsString("https://api.weibo.com/2/friendships/friends.json", params, "utf-8");
		if(result.isSuccess()){
			String response = result.getResponse();
			try{
				Map usersMap = JsonUtils.readJsonToMap(response);
				List<Map> userList = (List<Map>) usersMap.get("users");
				for (Map obj : userList) {
					System.out.println(obj.get("screen_name"));
				}
			}catch (Exception e) {
			}
		}else{
			System.out.println(result.getMsg());
		}
		return showJsonSuccess(model);
	}
	
	@Autowired@Qualifier("pdfDramaOrderExporter")
	private DramaOrderExporter pdfDramaOrderExporter;
	
	@RequestMapping("/pdf.xhtml")
	public String pdf(HttpServletResponse response, ModelMap model){
		List<DramaOrder> orderList = new ArrayList<DramaOrder>();
		DramaOrder order = daoService.getObject(DramaOrder.class, 130006975L);
		orderList.add(order);
		Document document = new Document();
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			PdfWriter.getInstance(document, bos);
			document.open();
			pdfDramaOrderExporter.getPdfDramaOrderDocument(document,orderList);
			document.close();
			response.setContentType("application/pdf");
			response.addHeader("Content-Disposition", "attachment;filename=gewara"+DateUtil.format(new Date(), "yyMMdd_HHmmss") + ".pdf");
			OutputStream outputStream = response.getOutputStream();
			bos.writeTo(outputStream);
			outputStream.flush();
			//outputStream.close();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return showJsonSuccess(model);
	}
}
