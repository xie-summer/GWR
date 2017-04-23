package com.gewara.web.action.admin.sys;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.Config;
import com.gewara.bank.BankConstant;
import com.gewara.constant.AdminCityContant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.sys.CacheConstant;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.helper.sys.AdminCityHelper;
import com.gewara.jms.JmsConstant;
import com.gewara.model.acl.User;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.partner.PartnerCinema;
import com.gewara.model.partner.PartnerMovie;
import com.gewara.model.pay.PayBank;
import com.gewara.model.pay.PayBankColl;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.movie.MCPService;
import com.gewara.untrans.BaoKuService;
import com.gewara.untrans.CacheService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.activity.TenWowActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.ChangeEntry;
import com.gewara.util.MarkHelper;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class SysAdminController extends BaseAdminController {
	@Autowired@Qualifier("jdbcTemplate")
	private JdbcTemplate jdbcTemplate;
	public void setJdbcTemplate(JdbcTemplate template) {
		jdbcTemplate = template;
	}
	@Autowired@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	@Autowired @Qualifier("mcpService")
	private MCPService mcpService;
	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	public void setJmsService(JmsService jmsService) {
		this.jmsService = jmsService;
	}
	@Autowired@Qualifier("config")
	private Config config;
	public void setConfig(Config config) {
		this.config = config;
	}

	@Autowired@Qualifier("cacheService")
	private CacheService cacheService;
	public void setCacheService(CacheService cacheService) {
		this.cacheService = cacheService;
	}

	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@RequestMapping("/admin/sysmgr/getMonitorStatus.xhtml")
	@ResponseBody
	public String getMonitorStatus(){
		return ""+monitorService.getMonitorStatus();
	}
	@RequestMapping("/doAuthIp.xhtml")
	public String doAuthIp(){
		return "admin/doAuthIp.vm";
	}
	@RequestMapping("/admin/sysmgr/refresh.xhtml")
	public String refresh(ModelMap model){
		Map<String, String> cityMap = AdminCityContant.getCitycode2CitynameMap();
		model.put("cityMap", cityMap);
		List<AdminCityHelper> province2CityList = AdminCityHelper.province2CityListMap();
		model.put("province2CityList", province2CityList);
		return "admin/sysmgr/refresh.vm";
	}

	
	/**
	 * @function 测试API
	 * @author bob.hu
	 *	@date	2011-06-03 14:38:45
	 */
	@RequestMapping("/admin/sysmgr/debugAPI.dhtml")
	public String mobile(){
		return "testmobile.vm";
	}
	@RequestMapping("/admin/sysmgr/getCheckValue.dhtml")
	public String getCheckValue(String checkKey, ModelMap model){
		checkKey = StringUtils.replace(checkKey, ",", "");
		String check = StringUtil.md5(checkKey);
		return showJsonSuccess(model, check);
	}
	
	@RequestMapping("/removeMemkey.xhtml")
	@ResponseBody
	public String removeKey(String regionName, String key){
		cacheService.remove(regionName, key);
		return "ok";
	}
	@RequestMapping("/testConnection.xhtml")
	@ResponseBody
	public String testConnection(){
		int count = daoService.getObjectCount(GewaConfig.class);
		return "success=" + count;
	}
	private int curIdx = 0, length=1;
	private List<Long> cinemaidList;
	@RequestMapping("/testJms.xhtml")
	@ResponseBody
	public String testJms(String date, String check){
		String checkStr = date + "kd3ydj3";
		String check2 = StringUtil.md5(checkStr);
		if(StringUtils.equals(check, check2)){
			if(cinemaidList == null) {
				cinemaidList = mcpService.getBookingCinemaIdList("310000", null);
				length = cinemaidList.size();
			}
			curIdx = (curIdx +1) % length;
			String pageUrl = "cinema/cinemaDetail.xhtml?cinemaId=" + cinemaidList.get(curIdx);
			Map<String, String> params = new HashMap<String, String>();
			params.put("pageUrl", pageUrl);
			params.put("citycode", "310000");
			params.put("cacheMin", "10");
			jmsService.sendMsgToDst(JmsConstant.QUEUE_UPDATECACHE, JmsConstant.TAG_UPADATE_PAGE_CACHE, params);
			return "success";
		}else{
			return "checkError";
		}
	}
	@RequestMapping("/admin/sysmgr/place.xhtml")
	public String placeList(ModelMap model){
		model.put("provinceList",placeService.getAllProvinces());
		return "admin/sysmgr/place.vm";
	}
	@RequestMapping("/admin/sysmgr/toggleDebug.xhtml")
	public String toggleDebug(String debug, ModelMap model){
		config.setDebugEnabled(StringUtils.equals(debug, "true"));
		return forwardMessage(model, "debug:" + Config.isDebugEnabled());
	}
	@RequestMapping("/admin/sysmgr/pageConfig.xhtml")
	public String getConfig(ModelMap model){
		model.put("config", config.getPageMap());
		return "admin/sysmgr/pageConfig.vm";
	}
	@RequestMapping("/admin/sysmgr/subwayline.xhtml")
	public String subwayline(ModelMap model){
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		return "admin/sysmgr/subwayline.vm";
	}
	@RequestMapping("/admin/sysmgr/indexarea.xhtml")
	public String indexarea(){
		return "admin/sysmgr/indexarea.vm";
	}
	@RequestMapping("/admin/sysmgr/search.xhtml")
	public String search(){
		return "admin/sysmgr/search.vm";
	}
	

	@RequestMapping(value="/admin/sysmgr/printTables.xhtml")
	@ResponseBody
	public String printTables() throws IOException{
		//结果与select 'WEBDATA.'||table_name||'.'||column_name from user_tab_columns t where table_name in (select table_name from user_tables) order by table_name,column_name做对比
		Map<String, ClassMetadata> allData = sessionFactory.getAllClassMetadata();
		List<String> all = new ArrayList<String>();
		for(String clazz: allData.keySet()){
			AbstractEntityPersister persister = (AbstractEntityPersister)allData.get(clazz);
			List<String> columns = getColumns(persister, false);
			columns.addAll(Arrays.asList(persister.getIdentifierColumnNames()));
			for(String column: columns){
				all.add(StringUtils.upperCase(persister.getTableName() + "." + column));
			}
		}
		Collections.sort(all);
		Writer writer = new FileWriter("/tmp/hibernate.txt");
		IOUtils.writeLines(all, "\n", writer);
		writer.close();
		return "success";
	}
	private List<String> getColumns(AbstractEntityPersister persister, boolean filter){
		List<String> columns = new ArrayList<String>();
		int len =  persister.getPropertyNames().length;
		for(int i=0;i<len;i++){
			String[] s = persister.getPropertyColumnNames(i);
			if(s==null || s[0]==null) continue;
			String label = s[0];
			if(filter && StringUtils.containsIgnoreCase(label, "pass") ||
					StringUtils.containsIgnoreCase(label, "privatekey") ||
					StringUtils.containsIgnoreCase(label, "secretkey")) continue;
			columns.add(label);
		}
		return columns;
	}

	@RequestMapping(value="/admin/sysmgr/execQuery.xhtml", method=RequestMethod.GET)
	public String query(){
		return "admin/sysmgr/execQuery.vm";
	}
	private String[] ips = new String[]{"180.166.48.210"};
	private String[] disableList = new String[]{
			"delete","trancate","pass","privatekey","secretkey","partnerkey",//列名
			"smsrecord"//表名
	};
	
	private boolean isInvalid(String sql){
		if(StringUtils.contains(sql, '*')||!StringUtils.startsWithIgnoreCase(sql, "select")){
			return true;
		}
		
		for(String disable: disableList){
			if(StringUtils.containsIgnoreCase(sql, disable)) return true;
		}
		return false;
	}
	//TODO:敏感字段
	private String[] sensitiveFields = new String[]{
			"mobile","email","pass"	
	};
	private String filterValue(Map row, String column){
		String v = row.get(column)==null?"":""+row.get(column);
		for(String field: sensitiveFields){
			if(StringUtils.containsIgnoreCase(column, field)){
				return StringUtil.md5(v + "xxxxyyzzz");
			}
		}
		return v;
	}
	@RequestMapping(value="/admin/sysmgr/execQuery.xhtml", method=RequestMethod.POST)
	public String query(HttpServletRequest request, HttpServletResponse res, String table, String sql, final String contentType, String queryAll, ModelMap model) throws IOException{
		User user = getLogonUser();
		String ip = WebUtils.getRemoteIp(request);
		if(!ArrayUtils.contains(ips, ip) && !WebUtils.isLocalRequest(request)) {
			res.sendError(400, "bad request!!!!!!!!");
			return null;
		}
		if(StringUtils.isNotBlank(table)){
			ClassMetadata data = sessionFactory.getClassMetadata(table);
			if(data==null) {
				Map<String, ClassMetadata> allData = sessionFactory.getAllClassMetadata();
				String tail = "." + table;
				for(String clazz: allData.keySet()){
					if(StringUtils.endsWithIgnoreCase(clazz, tail)){
						data = allData.get(clazz);
						break;
					}
				}
			}
			if(data==null) {
				return forwardMessage(model, "error: class not found!");
			}
			AbstractEntityPersister persister = (AbstractEntityPersister)data;
			List<String> columns = getColumns(persister, true);

			return forwardMessage(model, "select " + StringUtils.join(columns, ",") + " from " + persister.getTableName());
		}
		dbLogger.error("[" + user.getId() + "]exec query:" + sql);
		if(isInvalid(sql)) return forwardMessage(model, "error");
		String sql2 = StringUtils.replace(sql.toLowerCase(), "updatetime", "apdatetime");
		if(StringUtils.containsIgnoreCase(sql2, "update")) return forwardMessage(model, "error:update");
		if(StringUtils.contains(contentType, "xls")){
			download("xls", res);
		}else{
			res.setContentType("text/html");
		}
		final Writer writer = res.getWriter();
		String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"" + 
 				"\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
 				"<html xmlns=\"http://www.w3.org/1999/xhtml\">" + 
 				"<head>" +
 				"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>" +
 				"<link rel=\"stylesheet\" type=\"text/css\" href=\"http://static.gewara.cn/component/css/default.css\" />" +
 				"</head><body>";
		writer.write(header);
		final int max = StringUtils.isNotBlank(queryAll)?1000:20;
		try{
			jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
				@Override
				public String extractData(ResultSet rs) throws SQLException, DataAccessException {
					RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper(); 
					int rownum = 1;
					try{
						int rowcount=0;
						ResultSetMetaData md = rs.getMetaData();
						List<String> columnList = new ArrayList<String>();
						for(int i=1, count = md.getColumnCount();i<=count;i++){
							String label = md.getColumnLabel(i);
							if(StringUtils.containsIgnoreCase(label, "pass") ||
									StringUtils.containsIgnoreCase(label, "privatekey") ||
									StringUtils.containsIgnoreCase(label, "secretkey") ) continue;
							columnList.add(label);
						}
						writer.write("<table class=\"table\">");
						writer.write("<tr>");
						writer.write("<td>rownum</td>");
						for(String column:columnList){
							writer.write("<td>" + column + "</td>");
						}
						writer.write("</tr>");
						while(rs.next() && rowcount<max){
							Map<String, Object> row = mapper.mapRow(rs, rowcount ++);
							writer.write("<tr>");
							writer.write("<td>" + rownum + "</td>");
							for(String column:columnList){
								String v = filterValue(row, column);
								if(!StringUtils.equals(contentType, "xls")) v = VmUtils.escapeHtml(v);
								writer.write("<td style=\"vnd.ms-excel.numberformat:@\">" + v + "</td>");
							}
							writer.write("</tr>");
							rownum ++;
						}
						writer.write("</table></body></html>");
					}catch(Exception e){
						return StringUtil.getExceptionTrace(e, 50);
					}
					return null;
				}
			});
		}catch(Exception e){
			return forwardMessage(model, StringUtil.getExceptionTrace(e, 50));
		}
		return null;
	}
	@RequestMapping("/admin/sysmgr/configList.xhtml")
	public String configList(Long cid, ModelMap model) {
		String url = "admin/sysmgr/config.vm";
		if(cid==null) return url;
		GewaConfig cfg = daoService.getObject(GewaConfig.class, cid);
		model.put("config", cfg);
		return url;
	}
	@RequestMapping("/admin/sysmgr/setConfigContent.xhtml")
	public String setConfigContent(Long cid, String content, ModelMap model) {
		User user = getLogonUser();
		GewaConfig cfg = daoService.getObject(GewaConfig.class, cid);
		ChangeEntry changeEntry = new ChangeEntry(cfg);
		cfg.setContent(content);
		cfg.setUpdatetime(new Timestamp(System.currentTimeMillis()));
		daoService.saveObject(cfg);
		monitorService.saveChangeLog(user.getId(), GewaConfig.class, cid, changeEntry.getChangeMap(cfg));
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/modpcm.xhtml")
	public String setConfigContent(Long id, ModelMap model) {
		PartnerCinema pc = daoService.getObject(PartnerCinema.class, id);
		if(pc!=null) {
			pc.setCinemaid(null);
			daoService.saveObject(pc);
		}else {
			PartnerMovie pm = daoService.getObject(PartnerMovie.class, id);
			if(pm!=null){ 
				pm.setMovieid(null);
				daoService.saveObject(pm);
			}
		}
		return forwardMessage(model, "成功！");
	}
	
	@RequestMapping("/admin/sysmgr/cityshare.xhtml")
	public String cityshare(ModelMap model){
		Long gewacfgid = ConfigConstant.CFG_SHARECITY;
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, gewacfgid);
		List<String> selCitycodes = Arrays.asList(StringUtils.split(gewaConfig.getContent(), ","));
		model.put("selcitycode", selCitycodes);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("gewacfgid", gewacfgid);
		return "admin/sysmgr/shareCitys.vm";
	}
	@RequestMapping("/admin/sysmgr/saveCityshare.xhtml")
	public String saveCityshare(Long gewacfgid, String relatecity, ModelMap model){
		GewaConfig gewaConfig = daoService.getObject(GewaConfig.class, gewacfgid);
		gewaConfig.setContent(relatecity);
		daoService.saveObject(gewaConfig);
		return showJsonSuccess(model);
	}

	@RequestMapping("/admin/sysmgr/changeMarkMethod.xhtml")
	@ResponseBody
	public String changeMarkMethod(int markMethod){
		MarkHelper.markMethod = markMethod;
		return ""+new Date();
	}

	@RequestMapping("/admin/sysmgr/paylimit.xhtml")
	public String paylimit(ModelMap model){
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAYLIMIT);
		List<String> limitList = new ArrayList<String>();
		String[] strs  = StringUtils.split(cfg.getContent(), ",");
		if(strs!=null) limitList = Arrays.asList(strs);
		List<PayBank> confPayList = paymentService.getPayBankList(PayBank.TYPE_PC);
		model.put("confPayList", confPayList);
		model.put("limitList", limitList);
		model.put("config", cfg);
		model.put("creditMap", BankConstant.getAlipayKjCreditMap());
		model.put("payTextMap", PaymethodConstant.getPayTextMap());
		return "admin/sysmgr/paylimit.vm";
	}


	
	@RequestMapping("/admin/sysmgr/payBankList.xhtml")
	public String payBankList(ModelMap model){
		List<PayBank> payList = daoService.getObjectList(PayBank.class, "sortnum", true, 0, 100);
		model.put("payList", payList);
		return "admin/sysmgr/currpayList.vm";
	}
	@RequestMapping("/admin/sysmgr/payBankSort.xhtml")
	public String currPayList(Long id, Integer sortnum, ModelMap model){
		PayBank pay = daoService.getObject(PayBank.class, id);
		Integer oldnum = pay.getSortnum();
		pay.setSortnum(sortnum);
		daoService.saveObject(pay);
		User user = getLogonUser();
		dbLogger.warn("用户["+user.getId()+  ", "+ user.getRealname() +"]改变支付排序：" +  oldnum+ "--->" + sortnum);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/setPayBankMethod.xhtml")
	public String setCurrPayMethod(Long id, String paymethod, ModelMap model){
		PayBank pay = daoService.getObject(PayBank.class, id);
		String oldmethod = pay.getPaymethod();
		String sql = "from SpecialDiscount where timeto>? and paymethod like ? ";
		List<SpecialDiscount> qryList = daoService.queryByRowsRange(sql, 0, 1, new Timestamp(System.currentTimeMillis()), "%" + oldmethod + "%");
		if(qryList.size()>0){
			return showJsonError(model, "["+qryList.get(0).getDescription() + "], 已经有"+oldmethod+", 不能更改， 请先修改特价活动");
		}
		pay.setPaymethod(paymethod);
		daoService.saveObject(pay);
		User user = getLogonUser();
		dbLogger.warn("用户["+user.getId()+  ", "+ user.getRealname() +"]改变支付渠道：" +  oldmethod+ "--->" + paymethod);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/paybankList.xhtml")
	public String setCurrPayMethod(Long parentid, ModelMap model){
		PayBank currpay = daoService.getObject(PayBank.class, parentid);
		List<PayBankColl> collList = daoService.getObjectListByField(PayBankColl.class, "parentid", parentid);
		model.put("collList", collList);
		model.put("currpay", currpay);
		return "admin/sysmgr/paycollList.vm";
	}
	@RequestMapping("/admin/sysmgr/getPaybankColl.xhtml")
	public String getPaybankColl(Long id, ModelMap model){
		PayBankColl coll = daoService.getObject(PayBankColl.class, id);
		Map jsonMap = new HashMap();
		if(id!=null){
			jsonMap.put("id", coll.getId());
			jsonMap.put("name", coll.getName());
			jsonMap.put("paymethod", coll.getPaymethod());
		}
		return showJsonSuccess(model, jsonMap);
	}
	@RequestMapping("/admin/sysmgr/addPaycoll.xhtml")
	public String addPaycoll(Long id,Long parentid, String paymethod, String name, ModelMap model){
		if(StringUtils.isBlank(paymethod) || StringUtils.isBlank(name) || parentid==null) {
			return showJsonError(model, "数据不能为空！");
		}
		String oldname = "";
		String oldpaymethod = "";
		PayBankColl coll = null;
		if(id!=null){
			coll = daoService.getObject(PayBankColl.class, id);
			oldname = coll.getName();
			oldpaymethod = coll.getPaymethod();
		}else {
			coll = new PayBankColl();
		}
		coll.setParentid(parentid);
		coll.setName(name);
		coll.setPaymethod(StringUtils.trim(paymethod));
		daoService.saveObject(coll);
		User user = getLogonUser();
		dbLogger.warn("用户["+user.getId()+  ", "+ user.getRealname() +"]修改银行配置：" +  oldname+ "--->" + coll.getName() + "," + oldpaymethod + "---->" + coll.getPaymethod());
		return showJsonSuccess(model);
	}
	
	@Resource
	private TenWowActivityService tenWowActivityService;
	
	@Resource
	private BaoKuService baoKuService;

	@RequestMapping("/admin/sysmgr/loadTinWowTxt.xhtml")
	public String loadTinWowTxt(String fileName,boolean isLoad,ModelMap model){
		boolean resurnCode=tenWowActivityService.loadTinWowTxt(fileName,isLoad);
		model.put("resurnCode", resurnCode);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/addApiLoginIp.xhtml")
	public String addApiLoginIp(String ip, ModelMap model){
		cacheService.remove(CacheConstant.REGION_HALFDAY, "TIME" + ip);
		cacheService.remove(CacheConstant.REGION_HALFDAY, "LIMIT" + ip);
		return showJsonSuccess(model);
	}
	@RequestMapping("/admin/sysmgr/loadBaoKuTxt.xhtml")
	public String loadBaoKuTxt(String fileName,boolean isLoad,ModelMap model){
		boolean resurnCode=baoKuService.loadBaoKuTxt(fileName,isLoad);
		model.put("resurnCode", resurnCode);
		return showJsonSuccess(model);
	}
	
	//导入暴库所有的数据
	@RequestMapping("/admin/sysmgr/loadBaoKuFiles.xhtml")
	public String loadBaoKuFiles(boolean isLoad,ModelMap model){
		baoKuService.loadBaoKuFiles(isLoad);
		return showJsonSuccess(model);
	}
	
	//比较破解的数据在暴库中存在的
	@RequestMapping("/admin/sysmgr/scanLoadBaoKu.xhtml")
	public String scanLoadBaoKu(ModelMap model){
		baoKuService.scanLoadBaoKu();
		return showJsonSuccess(model);
	}
	
	//导入gewara破解的数据到暴库
	@RequestMapping("/admin/sysmgr/loadGewaraBaoKu.xhtml")
	public String loadGewaraBaoKu(ModelMap model){
		baoKuService.loadGewaraBaoKu();
		return showJsonSuccess(model);
	}
	
	//比较我们member中存在的暴库数据
	@RequestMapping("/admin/sysmgr/scanMemberBaoKu.xhtml")
	public String scanMemberBaoKu(ModelMap model){
		baoKuService.scanMemberBaoKu();
		return showJsonSuccess(model);
	}
	@RequestMapping(value="/admin/sysmgr/printClazz.xhtml")
	public String printClazz(ModelMap model) throws Exception {
		List<String> clazzList = new ArrayList<String>();
		Map<String, ClassMetadata> allData = sessionFactory.getAllClassMetadata();
		for(String clazz: allData.keySet()){
			clazzList.add(clazz);
		}
		Collections.sort(clazzList);
		return forwardMessage(model, clazzList);
	}
	@RequestMapping(value="/admin/sysmgr/filterScript.xhtml")
	public String filterScript(final String clazz, ModelMap model){
		if(clazz==null){
			return forwardMessage(model, "clazz=?");
		}else{
			new Thread(new Runnable(){
				@Override
				public void run() {
					filterScript(clazz);
				}
			}).start();
		}
		return forwardMessage(model, "workStart:" + new Timestamp(System.currentTimeMillis()));
	}
	private void filterScript(final String clazz){
		try {
			final Class persist = Class.forName(clazz);
			DetachedCriteria query = DetachedCriteria.forClass(persist);
			try {
				if(persist.getField("id")!=null){
					query.addOrder(Order.asc("id"));
				}
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			}
			int count = 0;
			List result =  hibernateTemplate.findByCriteria(query, 0, 10000);
			String reg1 = "<script|onblur|onclick|onfocus|onload";
			String reg2 = ".{1,20}(<script|onblur|onclick|onfocus|onload).{1,20}";
			while(result.size()>0){
				for(Object o: result){
					Map<String, String> map = BeanUtil.toSimpleStringMap(BeanUtil.getBeanMap(o));
					String tmp = "";
					for(String key: map.keySet()){
						String value = map.get(key);
						if(StringUtils.isNotBlank(value) && StringUtil.regMatch(value, reg1, true)){
							tmp += key + ":" + StringUtils.join(StringUtil.findByRegex(value, reg2, false), ",") + "@@";
						}
					}
					if(StringUtils.isNotBlank(tmp)){
						dbLogger.warn("FILTERSCRIPT:" + clazz + ":" + BeanUtil.get(o, "id") + tmp.replaceAll("\n", "\\n"));
					}
				}
				count += result.size();
				dbLogger.warn("FILTERSCRIPT:" + clazz + ",count:" + count);
				result = hibernateTemplate.findByCriteria(query, count, 10000);
			}
			dbLogger.warn("FILTERSCRIPT:" + clazz + "complete:" + count);
		} catch (Exception e) {
			dbLogger.warn("", e);
		}
	}
}
