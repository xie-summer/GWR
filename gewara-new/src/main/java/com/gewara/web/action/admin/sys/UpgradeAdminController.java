package com.gewara.web.action.admin.sys;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.GoodsConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.sys.JsonDataKey;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.bbs.MarkCount;
import com.gewara.model.common.BaseInfo;
import com.gewara.model.common.JsonData;
import com.gewara.model.goods.Goods;
import com.gewara.model.pay.AccountRecord;
import com.gewara.model.pay.OrderExtra;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.Sport;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.service.bbs.MarkService;
import com.gewara.service.bbs.UserMessageService;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.order.GoodsOrderService;
import com.gewara.service.order.GoodsService;
import com.gewara.service.partner.PartnerSynchService;
import com.gewara.service.ticket.SuccessOrderService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.MemberCountService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.ElecCardCoder;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class UpgradeAdminController extends BaseAdminController {
	@Autowired @Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("userMessageService")
	private UserMessageService userMessageService;
	@Autowired@Qualifier("markService")
	private MarkService markService;
	public void setMarkService(MarkService markService) {
		this.markService = markService;
	}
	@Autowired@Qualifier("elecCardService")
	private ElecCardService elecCardService;
	public void setElecCardService(ElecCardService elecCardService) {
		this.elecCardService = elecCardService;
	}

	@Autowired@Qualifier("partnerSynchService")
	private PartnerSynchService partnerSynchService;
	public void setPartnerSynchService(PartnerSynchService partnerSynchService) {
		this.partnerSynchService = partnerSynchService;
	}
	@Autowired@Qualifier("goodsOrderService")
	private GoodsOrderService goodsOrderService;
	public void setGoodsOrderService(GoodsOrderService goodsOrderService) {
		this.goodsOrderService = goodsOrderService;
	}
	@Autowired@Qualifier("goodsService")
	private GoodsService goodsService;
	public void setGoodsService(GoodsService goodsService) {
		this.goodsService = goodsService;
	}
	@Autowired@Qualifier("successOrderService")
	private SuccessOrderService successOrderService;

	@Autowired@Qualifier("memberCountService")
	private MemberCountService memberCountService;

	@RequestMapping("/admin/sysmgr/writeChinaPayFile.xhtml")
	@ResponseBody
	public String writeChinaPayFile() {
		String result = partnerSynchService.writeChinapayTransFile();
		return result;
	}
	@RequestMapping("/admin/sysmgr/upgradeAccountRecord.xhtml")
	@ResponseBody
	public String upgradeAccountRecord(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				int page = 0;
				String query = "from AccountRecord order by accountid, checkid";
				List<AccountRecord> recordList = daoService.queryByRowsRange(query, page*10000, 10000);
				while(recordList.size()>0){
					for(AccountRecord ar: recordList){
						Map change = BeanUtil.getBeanMap(ar);
						change.remove("id");
						monitorService.saveSysChangeLog(AccountRecord.class, ar.getAccountid(), change);
					}
					page ++;
					dbLogger.warn("page:" + page + ", last:" + BeanUtil.getBeanMap(recordList.get(recordList.size()-1)));
					recordList = daoService.queryByRowsRange(query, page*10000, 10000);
				}
			}
		}).start();
		return "start at " + new Date();
	}
	@RequestMapping("/admin/sysmgr/updateChangeHis.xhtml")
	public String updateChangeHis(ModelMap model, String order) {
		String query = "select new map(id as id, changehis as changehis) from TicketOrder t where t.changehis is not null and t.changehis not like '%{%' order by addtime "
				+ (StringUtils.isNotBlank(order) ? "desc" : "");
		List<Map> result = daoService.queryByRowsRange(query, 0, 10000);
		String update = "UPDATE WEBDATA.TICKET_ORDER SET CHANGEHIS=? WHERE RECORDID=?";
		for (Map row : result) {
			String change = JsonUtils.writeMapToJson(getChangeMap(""
					+ row.get("changehis")));
			jdbcTemplate.update(update, change, row.get("id"));
		}
		return forwardMessage(model, "maxRow:"
				+ (result.size() > 0 ? result.get(result.size() - 1) : 0));
	}
	@RequestMapping("/admin/updateNickname.xhtml")
	public String updateNickname(ModelMap model) {
		dbLogger.warn("用户昵称重名升级");
		String sql = "select b.recordid from WEBDATA.member b where b.nickname in (select m.nickname from WEBDATA.member m group by m.nickname having count(m.nickname) >1) order by b.recordid";
		List<Long> memberIdList = jdbcTemplate.queryForList(sql, Long.class);
		List<Member> memberList = daoService.getObjectList(Member.class, memberIdList);
		for (Member member : memberList) {
			Long memberid = member.getId();
			final String nickname = member.getNickname();
			String random = StringUtil.getDigitalRandomString(3);
			String newNickname = nickname + random;
			boolean isExistsMember = memberService.isMemberExists(newNickname, memberid);
			while (isExistsMember) {
				random = StringUtil.getDigitalRandomString(3);
				newNickname = nickname + random;
				isExistsMember = memberService.isMemberExists(newNickname, memberid);
			}
			String msg = newNickname + "，你好：<br />&nbsp;&nbsp;&nbsp;&nbsp;近期系统更新后发现您的昵称与其他用户昵称重名，为避免重复，我们根据时间先后顺序，将后使用该昵称（" + nickname
					+ "）的后面增加了随机数字加以区分，请在收到此消息后尽快修改您的昵称，由此造成的不变，敬请谅解。<br /> 格瓦拉生活网<br />日期" + DateUtil.getCurTimeStr();
			MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
			member.setNickname(newNickname);
			memberInfo.setNickname(newNickname);
			daoService.saveObjectList(member, memberInfo);
			userMessageService.sendSiteMSG(memberid, null, memberid, msg);
			dbLogger.warn("用户昵称重名升级：用户ID：" + memberid + ", 用户昵称：" + nickname + ", 新用户昵称：" + newNickname);
		}
		return showJsonSuccess(model, memberIdList.size() + "");
	}

	public Map<String, String> getChangeMap(String changehis) {
		Map result = new LinkedHashMap<String, String>();
		if (StringUtils.isBlank(changehis))
			return result;
		String[] fieldList = changehis.split(";");
		for (String field : fieldList) {
			String[] pair = field.split("=");
			if (pair.length == 2) {
				if (StringUtils.equals(pair[0], "购买次数"))
					result.put("buytimes", pair[1]);
				else
					result.put(pair[0], pair[1]);
			}
		}
		return result;
	}
	@RequestMapping("/admin/sysmgr/processOrder.xhtml")
	@ResponseBody
	public String updateStats(){
		String query = "select id from OrderExtra where ordertype='ticket' and processLevel=? and addtime > ? ";
		List<Long> idList = daoService.queryByRowsRange(query, 0, 1000, OrderExtra.LEVEL_MAIN, DateUtil.addDay(new Timestamp(System.currentTimeMillis()), -2));
		for(Long orderid: idList){
			TicketOrder order = daoService.getObject(TicketOrder.class, orderid);
			successOrderService.updateTicketOrderStats(order);
		}
		return "count:" + idList.size();
	}
	@RequestMapping("/admin/sysmgr/updateOtherInfo.xhtml")
	public String updateOtherInfo(String clazz, ModelMap model) {
		if (StringUtils.isBlank(clazz))
			throw new IllegalArgumentException("clazz=?");
		List<Long> idList = daoService.getObjectIDList(ServiceHelper
				.getPalceClazz(clazz));
		String errorMsg = "正在更新" + "[" + clazz + "]" + ", 共计:" + idList.size();
		model.put("errorMsg", errorMsg);
		dbLogger.warn(errorMsg);

		int succount = 0;
		for (Long id : idList) {
			doUpdateOtherInfo(clazz, id);
			succount++;
		}
		String infoMsg = "更新完成" + "[" + clazz + "]" + ", 共计:" + succount;
		model.put("infoMsg", infoMsg);
		dbLogger.warn(infoMsg);

		return "testMovie1.vm";
	}

	private void doUpdateOtherInfo(String clazz, Long id) {
		BaseInfo baseObject = daoService.getObject(ServiceHelper.getPalceClazz(clazz), id);
		Map<String, Object> otherinfoMap = JsonUtils.readJsonToMap(baseObject.getOtherinfo());
		for (String otheritem : ServiceHelper.getOtherInfoList()) {
			Object object = BeanUtil.get(baseObject, otheritem);
			if (object != null) {
				String info = "" + object;
				if (StringUtils.isNotBlank(info)) {
					otherinfoMap.put(otheritem, info);
				} else {
					otherinfoMap.remove(otheritem);
				}
			}
		}
		BeanUtil.set(baseObject, "otherinfo", JsonUtils.writeObjectToJson(otherinfoMap));
		daoService.saveObject(baseObject);
		dbLogger.warn("success:" + "[tag=]" + clazz + ",[id=]" + id);
	}

	// 升级语句
	@RequestMapping("/admin/sysmgr/updateSportOtherinfo.xhtml")
	public String updateSportOtherinfo(ModelMap model) {
		List<Sport> sportList = daoService.getAllObjects(Sport.class);
		int num = 0;
		int n = 0;
		int y = 0;
		for (Sport sport : sportList) {
			Map map = JsonUtils.readJsonToMap(sport.getOtherinfo());
			Object parkObj = map.get("park");
			if (parkObj != null) {
				String park = "" + parkObj;
				if (StringUtils.isNotBlank(park)) {
					if (StringUtils.equals(park, "n") || StringUtils.equals(park, "no")) {
						map.remove("park");
						n++;
					} else {
						map.put("park", park.replace("free", "提供免费停车位").
								replace("y", "提供停车位").replace("yes", "提供停车位"));
						y++;
					}
					num++;
					sport.setOtherinfo(JsonUtils.writeObjectToJson(map));
					daoService.saveObject(sport);
				}
			}
		}
		model.put("errorMsg", "更新总数为:" + num);
		String infoMsg = "Yes=" + y + ", No=" + n;
		model.put("infoMsg", infoMsg);
		return "testMovie1.vm";
	}

	@RequestMapping("/admin/sysmgr/updateMarkCount.xhtml")
	@ResponseBody
	public String updateMarkCount(String flag) {
		JsonData data = daoService.getObject(JsonData.class, TagConstant.TAG_MOVIE + JsonDataKey.KEY_MARKCOUNT);
		if(data == null) {
			data = new JsonData();
			data.setDkey(TagConstant.TAG_MOVIE + JsonDataKey.KEY_MARKCOUNT);
			data.setTag(JsonDataKey.KEY_MARKCOUNT);
			data.setData("2008-01-01 00:00:00");
			daoService.addObject(data);
		}
		if(StringUtils.equals(flag, "clean")){
			daoService.removeObjectById(JsonData.class, TagConstant.TAG_MOVIE + JsonDataKey.KEY_MARKCOUNT);

			List<MarkCount> relatedList = markService.getMarkCountListByTag(TagConstant.TAG_MOVIE);
			daoService.removeObjectList(relatedList);
			data.setData("2008-01-01 00:00:00");
			daoService.addObject(data);
		}
		
		Timestamp endtime = markService.updateMarkCount(TagConstant.TAG_MOVIE);
		markService.updateAvgMarkTimes(TagConstant.TAG_MOVIE, DateUtil.addDay(endtime, -30), endtime);
		//markService.saveMaxMarktimes(TagConstant.TAG_MOVIE, DateUtil.addDay(endtime, -30));
		return "SUCCESS";
	}

	@RequestMapping("/admin/sysmgr/updateGoodsOtherInfo.xhtml")
	@ResponseBody
	public String updateGoodsOtherInfo(){
		List<Goods> goodsList = goodsService.getGoodsList(Goods.class, null, null, null, true, true, false, null, true);
		for(Goods goods : goodsList){
			int count = goodsOrderService.getGoodsOrderQuantity(goods.getId(), OrderConstant.STATUS_PAID_SUCCESS)*10;
			Map otherInfo = JsonUtils.readJsonToMap(goods.getOtherinfo());
			otherInfo.put(GoodsConstant.GOODS_SHOPPING_COUNT, count);
			goods.setOtherinfo(JsonUtils.writeObjectToJson(otherInfo));
		}
		daoService.saveObjectList(goodsList);
		return "success";
	}

	private ThreadPoolExecutor executor;
	
	@PostConstruct
	public void init() {
		BlockingQueue<Runnable> taskQueue = new LinkedBlockingQueue<Runnable>();
		executor = new ThreadPoolExecutor(10, 15, 300, TimeUnit.SECONDS, taskQueue);
		executor.allowCoreThreadTimeOut(false);
	}
	
	@RequestMapping("/admin/sysmgr/syncFirstOrderData.xhtml")
	public String syncFirstOrderData(String orderType ,ModelMap model){
		int total = getOrderListCount(orderType);
		int tmp = 0;int currentTradeNO= 0;
		while (total - tmp > 0 ) {
			List<Map> gewaOrderList = getOrderList(tmp, 5000,orderType);
			dbLogger.warn("加入数据队列："+tmp+"到"+(tmp+5000)+"，单轮总计:"+VmUtils.size(gewaOrderList));
			for (Map order : gewaOrderList) {
				if (currentTradeNO%1000==0) {
					dbLogger.warn("当前执行到："+currentTradeNO+"行。");
				}
				executor.execute(new UpdateTask(order));
				currentTradeNO++;
			}
			tmp += 5000;
		}
		dbLogger.warn("共加入数据"+total);
		return showJsonSuccess(model,"共加入数据"+total);
	}
	
	private List<Map> getOrderList(int start, int max ,String orderType){
		String sql = "select memberid||','||trade_no||','||order_type from (select * from (select t.*, row_number() OVER(ORDER BY null) AS row_number from WEBDATA.member_first_order t where t.ORDER_TYPE = ?) p where p.row_number > ?) q where rownum <= ?";
		List<String> list = jdbcTemplate.queryForList(sql,String.class,orderType,start,max);
		List<Map> mapList = new ArrayList<Map>();
		for (String detail : list) {
			Map map = new HashMap();
			map.put("memberid",StringUtils.split(detail,",")[0]);
			map.put("tradeNo",StringUtils.split(detail,",")[1]);
			map.put("orderType",StringUtils.split(detail,",")[2]);
			mapList.add(map);
		}
		return mapList;
	}
	
	private Integer getOrderListCount(String orderType){
		String sql = "SELECT COUNT(*) FROM WEBDATA.MEMBER_FIRST_ORDER WHERE ORDER_TYPE = ?";
		List list = jdbcTemplate.queryForList(sql, Integer.class,orderType);
		if(list.isEmpty()) return 0;
		return  Integer.parseInt(""+list.get(0));
	}
	private class UpdateTask implements Runnable {
		private Map order;
		
		public UpdateTask(Map object) {
			this.order = object;
		}

		@Override
		public void run() {
			memberCountService.saveMbrFirstTicket(Long.valueOf(order.get("memberid")+""), (String)order.get("tradeNo"),(String)order.get("orderType"));
		}

	}
	
	@RequestMapping("/admin/sysmgr/upgradeElec.xhtml")
	@ResponseBody
	public String upgradeElec(Integer page){
		int count = 0;
		if(page==null){
			count = elecCardService.upgradeElecCard();
		}else{
			for(int i=0;i<page;i++){
				count += elecCardService.upgradeElecCard();	
			}
		}
		dbLogger.warn("success,page:" + page);
		return "success:"+count + ", at " + new Timestamp(System.currentTimeMillis());
	}
	@RequestMapping("/admin/sysmgr/testPasswd.xhtml")
	@ResponseBody
	public String match(String passwd){
		String pass = passwd.toUpperCase();
		String md5pass = StringUtil.md5(pass+pass);
		return ElecCardCoder.encode(md5pass);
	}
}