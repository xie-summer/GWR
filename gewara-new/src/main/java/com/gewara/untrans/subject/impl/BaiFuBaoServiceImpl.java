package com.gewara.untrans.subject.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.sys.ConfigConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.model.user.Member;
import com.gewara.model.user.TempMember;
import com.gewara.service.ticket.TicketDiscountService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.LockService;
import com.gewara.untrans.draw.DrawUntransService;
import com.gewara.untrans.impl.AbstractUntrantsService;
import com.gewara.untrans.impl.LockServiceImpl.AtomicCounter;
import com.gewara.untrans.subject.BaiFuBaoService;
import com.gewara.util.GewaIpConfig;
import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;

@Service("baiFuBaoService")
public class BaiFuBaoServiceImpl extends AbstractUntrantsService implements BaiFuBaoService, InitializingBean {
	public static final String FLAG_PC = "pc";
	public static final String FLAG_WAP = "wap";
	private static final String TAG = "bai_fu_bao";
	
	@Autowired@Qualifier("drawUntransService")
	private DrawUntransService drawUntransService;
	
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	
	@Autowired@Qualifier("lockService")
	private LockService lockService;
	
	@Autowired@Qualifier("ticketDiscountService")
	private TicketDiscountService ticketDiscountService;

	private String payGateWay = "";
	private String retrieveUrl = "";
	
	private Map<String, AtomicCounter> counterMap = new HashMap<String, AtomicCounter>();
	private Map<String, Long> spconfig = new HashMap<String, Long>();

	@Override
	public ErrorCode<String> drawClick(Long memberid, String ip) {
		if (memberid == null) {
			return ErrorCode.getFailure("参数错误！");
		}
		if(!hasQualifications(memberid)){
			return ErrorCode.getFailure("亲爱的用户，您还没有资格码，不能参与抽奖！");
		}
		Member member = daoService.getObject(Member.class, memberid);
		ErrorCode<String> result = drawUntransService.clickDraw(member, TAG, "all", null, null, null, ip, false, null);
		if (result.isSuccess()) {
			return ErrorCode.getSuccessReturn(result.getRetval());
		} else {
			String errorMsg = "系统繁忙请重试！";
			if (StringUtils.equals(result.getMsg(), "statuss=-1")) {
				errorMsg = "请先登录！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=0")) {
				errorMsg = "活动未开始或已结束！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=2")) {
				errorMsg = "系统繁忙请重试！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=5")) {
				errorMsg = "请先绑定手机！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=7")) {
				errorMsg = "积分不足！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=8")) {
				errorMsg = "请先认证邮箱！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=9")) {
				errorMsg = "操作过于频繁！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=11")) {
				errorMsg = "亲爱的用户，您的资格码已经使用，抽奖仅限参加一次！";
			}
			return ErrorCode.getFailure(errorMsg);
		}
	}
	
	@Override
	public long joinCount(){
		//FIXME:名额分类
		Collection<AtomicCounter> counterList = counterMap.values();
		long total = 0;
		for(AtomicCounter counter: counterList){
			total += counter.get();
		}
		return total;
	}
	@Override
	public boolean hasQualifications(Long memberid){
		TempMember tm = daoService.getObject(TempMember.class, memberid);
		if(tm == null || !StringUtils.equals(tm.getStatus(), Status.Y)){
			return false;
		}
		return true;
	}
	@Override
	public ErrorCode<String> getPayUrl(TempMember tm) {
		String tradeNo = tm.getId().toString();
		String deviceType = FLAG_PC.equals(tm.getFlag())?"0":"1";
		
		Map<String, String> params = new HashMap<String, String>();

		params.put("tradeNo", tradeNo);
		params.put("gatewayCode", "bfbActivityPay");
		params.put("deviceType", deviceType);//1移动端支付，0 pc支付，这两个网关不一样
		params.put("otherInfo", "");
		
		HttpResult result = HttpUtils.postUrlAsString(payGateWay, params);
		if(!result.isSuccess()){
			return ErrorCode.getFailure(result.getMsg());
		}
		String res = result.getResponse();
		Map<String, String> returnMap = VmUtils.readJsonToMap(new String(Base64.decodeBase64(res)));
		String paidResult = returnMap.get("payResult");
		if(StringUtils.equals(paidResult, "paid")){
			return ErrorCode.getFailure("已购买活动码，不能再次购买！");
		}
		String payurl = returnMap.get("payurl");
		Map<String, String> submitParams = VmUtils.readJsonToMap(returnMap.get("submitParams"));
		String url = HttpUtils.getFullUrl(payurl, submitParams, "utf-8");
		return ErrorCode.getSuccessReturn(url);
	}
	
	@Override
	public String queryOrder(String tradeNo){
		String check = StringUtil.md5(tradeNo+"sdlfkjsd23489");
		Map<String, String> params = new HashMap<String, String>();
		params.put("tradeNo", tradeNo);
		params.put("check", check);
		String result = HttpUtils.getUrlAsString(retrieveUrl, params).getResponse();
		return result;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if(GewaIpConfig.isGewaLocalIp(Config.getServerIp())){
			if(Config.getServerIp().startsWith("172.22.1.37")){
				payGateWay = "http://172.22.1.37:82/pay2/api/getActivityPayParams.xhtml";
				retrieveUrl = "http://172.22.1.37:82/pay2/api/getActivityOrder.xhtml";
			}else{
				payGateWay = "http://pay.gewara.com/pay2/api/getActivityPayParams.xhtml";
				retrieveUrl = "http://pay.gewara.com/pay2/api/getActivityOrder.xhtml";
			}
		}else{
			payGateWay = "http://paytest.gewala.net/pay2/api/getActivityPayParams.xhtml";
			retrieveUrl = "http://paytest.gewala.net/pay2/api/getActivityOrder.xhtml";
		}
		initCounter();
		new Timer().schedule(new TimerTask(){
			@Override
			public void run() {
				refreshCounter();
				dbLogger.warn("refreshBaiFubaoCounterSuccess");
			}
		}, 1000 * 120);
	}

	@Override
	public void refreshCounter() {
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SUBJECT_BAIFUBAO);
		GewaConfig cfg2 = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SUBJECT_BAIFUBAO_NUM);
		Map<String, String> confMap = JsonUtils.readJsonToMap(cfg.getContent());
		Map<String, String> numMap = new HashMap<String, String>();
		if(cfg2!=null){
			numMap = JsonUtils.readJsonToMap(cfg2.getContent());
		}
		for(String flag: confMap.keySet()){
			AtomicCounter counter = lockService.getAtomicCounter(flag);
			int count = getCount(flag);
			String tmp = numMap.get(flag);
			if(StringUtils.isNotBlank(tmp)){
				count = count + Integer.valueOf(tmp);
			}
			counter.set(count);
			counterMap.put(flag, counter);
		}
	}
	private void initCounter() throws Exception {
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_SUBJECT_BAIFUBAO);
		Map<String, String> confMap = JsonUtils.readJsonToMap(cfg.getContent());
		for(String flag: confMap.keySet()){
			spconfig.put(flag, Long.valueOf(confMap.get(flag)));
			AtomicCounter counter = lockService.getAtomicCounter(flag);
			counterMap.put(flag, counter);
		}
	}

	private int getCount(String flag){
		DetachedCriteria query = DetachedCriteria.forClass(TempMember.class);
		query.add(Restrictions.eq("flag", flag));	
		query.add(Restrictions.eq("status", Status.Y));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		Integer c = Integer.parseInt(""+result.get(0));
		return c;
	}
	
	@Override
	public ErrorCode<String> checkStatus(String mobile, String password){
		TempMember tm = null;
		String tmppwd = StringUtil.md5(password);
		DetachedCriteria query = DetachedCriteria.forClass(TempMember.class);
		query.add(Restrictions.eq("mobile", mobile));
		query.add(Restrictions.eq("tmppwd", tmppwd));
		List<TempMember> resultList = hibernateTemplate.findByCriteria(query);
		if(resultList.isEmpty()){
			query = DetachedCriteria.forClass(Member.class);
			query.add(Restrictions.eq("mobile", mobile));
			query.add(Restrictions.eq("password", tmppwd));
			List<Member> result = hibernateTemplate.findByCriteria(query);
			if(result.isEmpty()){
				return ErrorCode.getFailure("未找到此账号！");
			}else{
				Member m = result.get(0);
				tm = daoService.getObjectByUkey(TempMember.class, "memberid", m.getId());
			}
		}else{
			tm = resultList.get(0);
		}
		if(tm == null) return ErrorCode.getFailure("未找到此账号！");
		if(StringUtils.equals(tm.getStatus(), Status.Y)){
			return ErrorCode.getSuccess("success");
		}else{
			String paid = queryOrder(tm.getId()+"");
			if(StringUtils.contains(paid, "success")){
				Long spid = getSpid(tm);
				ErrorCode<Member> result = ticketDiscountService.processBaiduPaySuccess(tm, spid);
				if(result.isSuccess()){
					return ErrorCode.getSuccess("success");	
				}else{
					return ErrorCode.getFailure("服务器忙，请重试！");
				}
				
			}else{
				return ErrorCode.getFailure("支付失败！");
			}
		}
	}
	
	@Override
	public ErrorCode<TempMember> processPaySuccess(Long tmid) {
		TempMember tm = daoService.getObject(TempMember.class, tmid);
		Long spid = getSpid(tm);
		ErrorCode<Member> result = ticketDiscountService.processBaiduPaySuccess(tm, spid);
		if(result.isSuccess()){
			return ErrorCode.getSuccessReturn(tm);
		}
		return ErrorCode.getFailure(result.getMsg());
	}
	private Long getSpid(TempMember tm){
		return spconfig.get(tm.getFlag());
	}

}
