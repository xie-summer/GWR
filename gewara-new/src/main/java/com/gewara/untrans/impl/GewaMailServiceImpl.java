package com.gewara.untrans.impl;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.command.EmailRecord;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.SysAction;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.movie.Cinema;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.SysMessageAction;
import com.gewara.pay.PayUtil;
import com.gewara.service.DaoService;
import com.gewara.untrans.GewaMailService;
import com.gewara.untrans.MailService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.HttpResult;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.util.PKCoderUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.xmlbind.gym.CardItem;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
@Service("gewaMailService")
public class GewaMailServiceImpl implements GewaMailService{
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);

	public static final String TYPE_MOVIEEND = "movie_end";
	public static final String TYPE_DRAMAEND = "drama_end";
	public static final String EMAIL_ADDRESS_EDIT = "bianji@gewara.com";

	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Autowired@Qualifier("mailService")
	private MailService mailService;
	@Override
	public void sendTicketOrderEmail(TicketOrder order, OpenPlayItem opi) {
		if(order.sureOutPartner()) return;
		Member member = daoService.getObject(Member.class, order.getMemberid());
		if(StringUtils.isNotBlank(member.getEmail())){
			Map<String, String> model = new HashMap<String, String>();
			model.put("orderid", order.getId()+"");
			model.put("memberid", order.getMemberid()+"");
			model.put("tradeno", order.getTradeNo());
			model.put("nickname", order.getMembername());
			String type = TYPE_MOVIEEND;
			Timestamp sendtime = DateUtil.addHour(opi.getPlaytime(), 3);
			model.put("movieid", opi.getMovieid()+"");
			model.put("cinemaid", opi.getCinemaid()+"");
			model.put("moviename", opi.getMoviename());
			model.put("cinemaname", opi.getCinemaname());
			String title = "观影结束，发表评论赢取积分";
			String template = "mail/commentMail.vm";
	
			String content = JsonUtils.writeMapToJson(model);
			EmailRecord er = new EmailRecord(EmailRecord.SENDER_GEWARA, title, content, member.getEmail());
			er.setType(type);
			er.setSendtime(sendtime);
			er.setTemplate(template);
			HttpResult code = mailService.sendHtmlMessageUsingApi(er);
			if(code != null && code.isSuccess()){
				Map<String,String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
				otherInfoMap.put(OrderConstant.STATUS_EMAIL_ID, code.getResponse());
				order.setOtherinfo(JsonUtils.writeObjectToJson(otherInfoMap));
				daoService.saveObject(order);
			}
		}
	}
	@Override
	public void sendDramaOrderEmail(DramaOrder order){
		String type = TYPE_DRAMAEND;
		Member member = daoService.getObject(Member.class, order.getMemberid());
		if(StringUtils.isNotBlank(member.getEmail())){
			OpenDramaItem odi = daoService.getObjectByUkey(OpenDramaItem.class, "dpid", order.getDpid(), true);
			Timestamp sendtime = DateUtil.addHour(odi.getPlaytime(), 3);
			Map model = new HashMap();
			model.put("nickname", order.getMembername());
			model.put("dramaid", odi.getDramaid()+"");
			model.put("theatreid", odi.getTheatreid()+"");
			model.put("dramaname", odi.getDramaname());
			model.put("theatrename", odi.getTheatrename());
			String title = "观剧结束，发表评论赢取积分";
			String template = "mail/dramaMail.vm";
			String content = JsonUtils.writeMapToJson(model);
			EmailRecord er = new EmailRecord(EmailRecord.SENDER_GEWARA, title, content, member.getEmail());
			er.setType(type);
			er.setSendtime(sendtime);
			er.setTemplate(template);
			mailService.sendHtmlMessageUsingApi(er);
		}
	}

	@Override
	public void sendGymOrderEmail(GymOrder order, CardItem gymCardItem){
		Member member = daoService.getObject(Member.class, order.getMemberid());
		if(StringUtils.isNotBlank(member.getEmail())){
			Map model = new HashMap();
			model.put("nickname", order.getMembername());
			model.put("addtime", DateUtil.format(order.getAddtime(), "yyyy-MM-dd HH:mm:ss"));
			model.put("tradeNo", order.getTradeNo());
			model.put("cardname", gymCardItem.getName());
			Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(order.getOtherinfo());
			model.put("realname", otherInfoMap.get("realname"));
			model.put("telphone", otherInfoMap.get("telphone"));
			model.put("due", order.getDue());
			String title = "成功购买健身卡";
			String template = "mail/gymMail.vm";
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, title, template, model, member.getEmail());
		}
	}
	@Override
	public SysMessageAction sendTemplateHtmlSysMessageAction(String body, Long memberid, Long actionid){
		SysMessageAction sma = new SysMessageAction(SysAction.ACTION_TICKET_SUCCESS);
		sma.setFrommemberid(1L);
		sma.setBody(body);
		sma.setStatus(SysAction.STATUS_RESULT);
		sma.setTomemberid(memberid);
		sma.setActionid(actionid);
		return sma;
	}
	
	@Override
	public void sendRegEmail(Member member) {
		if(StringUtils.isNotBlank(member.getEmail())){
			String radom = StringUtil.md5WithKey(member.getId() + "");
			String returnUrl = "userSeniorRecognition.xhtml?encode="+radom+"&memberid=" + member.getId();
			Map model = new HashMap();
			model.put("nickname", member.getNickname());
			model.put("returnUrl", returnUrl);
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "欢迎您注册格瓦拉生活网", "mail/welcome.vm", model, member.getEmail());
		}
	}
	@Override
	public void sendTicketOrderEmail(Member member, TicketOrder order, Cinema cinema) {
		if(StringUtils.isNotBlank(member.getEmail())){
			Map model = new HashMap();
			model.put("nickname",  member.getNickname());
			model.put("order", BeanUtil.getBeanMapWithKey(order,"paymethod", "due", "quantity"));
			Map<String, String> descMap = JsonUtils.readJsonToMap(order.getDescription2());
			model.put("moviename", descMap.get("影片"));
			model.put("mpi", descMap.get("场次"));
			int point  = order.getDue();
			if(StringUtils.equals(order.getPaymethod(), PaymethodConstant.PAYMETHOD_GEWAPAY)) point = order.getDue()-order.getWabi();
			if(point<0) point = 0;
			model.put("point", point);
			model.put("cinema", BeanUtil.getBeanMapWithKey(cinema, "name" ,"countyname", "address"));
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "成功购买电影票", "mail/mail.vm", model, member.getEmail());
		}
	}
	@Override
	public void sendChangeEmail(Member member, String newEmail) {
		String random = StringUtil.getRandomString(20) +"@"+ System.currentTimeMillis();
		String encode = PayUtil.md5WithKey(random, newEmail, "" + member.getId());
		String queryStr = "id="+member.getId() + "&random=" + random + "&email="+newEmail+"&encode="+encode+"&op=mdyeml";
		Map model = new HashMap();
		model.put("nickname", member.getNickname());
		model.put("queryStr", queryStr);
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "你的登陆邮箱更改申请", "mail/exchangeEmail.vm", model, newEmail);
	}
	@Override
	public void sendSecurityEmail(Member member, String email){
		if(ValidateUtil.isEmail(email)){
			String random = StringUtil.getRandomString(20) +"@"+ System.currentTimeMillis();
			String encode = PayUtil.md5WithKey(random, email, "" + member.getId());
			String queryStr = "id="+member.getId() + "&random=" + random + "&email="+email+"&encode="+encode+"&op=bindeml";
			dbLogger.warn(queryStr);
			Map model = new HashMap();
			model.put("nickname", member.getNickname());
			model.put("queryStr", queryStr);
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "你设置安全登陆邮箱", "mail/securityEmail.vm", model, email);
		}
	}
	@Override
	public void sendRemoveMobileEmail(Member member, String mobile) {
		if(StringUtils.isNotBlank(member.getEmail())){
			Long timeMillis = System.currentTimeMillis()+DateUtil.m_hour * 2;
			String validKey = PKCoderUtil.encodeString(String.valueOf(timeMillis));
			String encode = StringUtil.md5WithKey(String.valueOf(member.getId()+timeMillis));
			String returnUrl = "home/acct/removieMobile.xhtml?encode="+encode+"&mobile="+mobile+"&validKey="+validKey;
			Map model = new HashMap();
			model.put("nickname", member.getNickname());
			model.put("returnUrl", returnUrl);
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "解除手机绑定", "mail/removeMobile.vm", model, member.getEmail());
		}
	}
	@Override
	public void sendGetPasswordMail(String nickname,Long memberid, String email, String uuid) {
		String encode = PayUtil.md5WithKey(email, "" + memberid, uuid);
		String returnUrl = "modifyPassword.xhtml?encode="+encode+"&email="+email+"&uuid="+uuid;
		Map model = new HashMap();
		model.put("nickname", nickname);
		model.put("returnUrl", returnUrl);
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "找回登录密码", "mail/getPassword.vm", model, email);
	}
	@Override
	public void sendValidateEmail(Member member, String uuid) {
		String email = member.getEmail();
		String encode = PayUtil.md5WithKey(email, "" + member.getId(), uuid);
		String returnUrl = "mbrIdtAuthEml.xhtml?encode=" + encode + "&memberid=" + member.getId() + "&uuid=" + uuid;
		Map model = new HashMap();
		model.put("nickname", member.getNickname());
		model.put("returnUrl", returnUrl);
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "修改登录密码", "mail/validateEmail.vm", model, email);
	}
	@Override
	public void sendSeniorRecognitionEmail(String nickname, Long memberid,String email) {
		String radom = StringUtil.md5WithKey(memberid+"");
		String returnUrl ="";
		returnUrl = "userSeniorRecognition.xhtml?encode="+radom+"&memberid="+memberid;
		Map model = new HashMap();
		model.put("nickname", nickname);
		model.put("returnUrl", returnUrl);
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "邮箱验证", "mail/seniorRecognition.vm", model,email);
	}
	@Override
	public void sendModifyCinemaMail(GewaraUser user,Long relatedid,String url,String msg) {
		Map map = new HashMap();
		map.put("user", BeanUtil.getBeanMapWithKey(user, "id", "email"));
		map.put("relatedid", relatedid);
		map.put("message",msg);
		map.put("url", url);
		
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "商家修改影院信息", "mail/modifyCinemaMail.vm", map, EMAIL_ADDRESS_EDIT);
	}
	@Override
	public void sendAdviseEmail(String membername, String body, String email) {
		if(ValidateUtil.isEmail(email)){
			Map model = new HashMap();
			model.put("nickname", membername);
			model.put("body", body);
			model.put("email", email);
			mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "反馈建议信息回复", "mail/adviseEmail.vm", model, email);
		}
	}
	@Override
	public void sendCardWarnEmail(String nickname,String email, String count) {
		Map model = new HashMap();
		model.put("nickname", nickname);
		model.put("count", count);
		mailService.sendTemplateEmail(EmailRecord.SENDER_GEWARA, "格瓦拉优惠券过期提醒", "mail/cardWarn.vm", model, email);
	}

}