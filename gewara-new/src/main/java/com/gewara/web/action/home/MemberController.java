package com.gewara.web.action.home;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.AdminCityContant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.TagConstant;
import com.gewara.constant.ticket.OrderConstant;
import com.gewara.model.drama.Drama;
import com.gewara.model.drama.Theatre;
import com.gewara.model.movie.Cinema;
import com.gewara.model.movie.Movie;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.GewaOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SpCode;
import com.gewara.model.pay.SpecialDiscount;
import com.gewara.model.sport.Sport;
import com.gewara.model.sport.SportItem;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.OpenMember;
import com.gewara.model.user.ShareMember;
import com.gewara.service.OperationService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.ShareService;
import com.gewara.untrans.WalaApiService;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.untrans.monitor.SysLogType;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.util.VmUtils;
import com.gewara.util.WebUtils;
import com.gewara.web.action.BaseHomeController;
import com.gewara.xmlbind.bbs.ReComment;

@Controller
public class MemberController extends BaseHomeController{
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	@Autowired@Qualifier("walaApiService")
	private WalaApiService walaApiService;
	@Autowired@Qualifier("blogService")
	private BlogService blogService;
	public void setBlogService(BlogService blogService) {
		this.blogService = blogService;
	}
	@Autowired@Qualifier("shareService")
	private ShareService shareService;
	public void setShareService(ShareService shareService) {
		this.shareService = shareService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Autowired@Qualifier("operationService")
	private OperationService operationService;
	public void setOperationService(OperationService operationService) {
		this.operationService = operationService;
	}
	@RequestMapping("/home/member/register2.xhtml")
	public String regsuccess(ModelMap model, HttpServletRequest request) {
		Member member = getLogonMember();
		if(member == null) return showError(model, "注册错误，请正确注册！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!memberInfo.isRegisterSource(MemberConstant.REGISTER_MOBLIE) && !memberInfo.isRegisterSource(MemberConstant.REGISTER_CODE)){
			String[] memberValue = member.getEmail().split("@");
			String mailUrl = "";
			boolean isCommon=true;
			if(StringUtils.isNotBlank(memberValue[1])){
				mailUrl = memberValue[1];
				if(memberValue[1].equals("gmail.com")){
					mailUrl="http://www.gmail.com";
					isCommon=false;
				}else if(memberValue[1].equals("hotmail.com")){
					mailUrl="http://www.hotmail.com";
					isCommon=false;
				}else if(memberValue[1].equals("126.com")){
					mailUrl="http://www.126.com";
					isCommon=false;
				}
			}
			model.put("isCommon", isCommon);
			model.put("mailUrl", mailUrl);
		}
		model.put("member", member);
		model.put("memberInfo", memberInfo);
		
		// 2011 由商家引导的用户放置领取5元优惠券按钮(由cookie获取)
		String[] cookies = WebUtils.getCookie4ProtectedPage(request, "partnerReg");
		if(cookies != null){
			model.put("isFromGewaPartner", true);
		}
		return "home/register/completeInfo.vm";
	}
	@RequestMapping("/home/member/register/completeRegister.xhtml")
	public String completeRegister(ModelMap model, HttpServletRequest request){
		Member member = getLogonMember();
		if(member == null) return showError(model, "注册错误，请正确注册！");
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		Map dataMap = request.getParameterMap();
		BindUtils.bindData(memberInfo, dataMap, null, MemberInfo.disallowBindField);
		daoService.updateObject(memberInfo);
		return showJsonSuccess(model);
	}
	@RequestMapping("/home/member/resendRecognition.xhtml")
	public String repeatSend(ModelMap model){
		Member member = getLogonMember();
		if(StringUtils.isBlank(member.getEmail())) return showJsonError(model, "请设置安全邮箱！");
		gewaMailService.sendSeniorRecognitionEmail(member.getNickname(),member.getId(),member.getEmail());
		return showJsonSuccess(model);
	}
	/**
	 * 对外合作用户，用户信息绑定显示
	 */
	@RequestMapping("/home/showBindMemberInfo.xhtml")
	public String showBindMemberInfo(ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String omTag = VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), "openMember");
		model.put("logonMember", member);
		if(omTag != null){
			OpenMember om = memberService.getOpenMemberByLoginname(omTag, member.getId()+"");
			model.put("openMember", om);
			return "home/register/relateRegister.vm";
		}else return "redirect:/home/acct/memberinfo.xhtml";
	}
	
	/**
	 * 对外合作用户与手机动态码注册用户
	 */
	@RequestMapping("/home/saveBindMemberInfo.xhtml")
	public String saveBindMemberInfo(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			ModelMap model,String nickname,String email,String pwd,String pwd2, HttpServletRequest request){
		Member member = getLogonMember();
		if(StringUtils.isBlank(nickname)) return showJsonError(model, "昵称不能为空！");
		if(StringUtils.isBlank(pwd)) return showJsonError(model, "密码不能为空！"); 
		if(StringUtils.isBlank(email)) return showJsonError(model, "邮箱不能为空！");
		boolean matchNickname = ValidateUtil.isCNVariable(nickname, 1, 20);
		if(!matchNickname) return showJsonError(model,"昵称不能包含特殊符号！");
		boolean isEmail = ValidateUtil.isEmail(email);
		if(!isEmail)return showJsonError(model, "邮箱格式不正确！");
		String key = blogService.filterAllKey(nickname);
		if(StringUtils.isNotBlank(key)) return showJsonError(model, "昵称含有非法关键字！"); 
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!StringUtils.equals(VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), MemberConstant.TAG_SOURCE), "fail")) 
			return showJsonError(model, "该用户已设置过登录密码！");
		boolean emailExist = memberService.isMemberExists(email, member.getId());
		if(emailExist) return showJsonError(model, "邮箱已注册，请更换邮箱地址！");
		if(!ValidateUtil.isPassword(pwd)){
			return showJsonError(model, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
		}
		if(!StringUtils.equals(pwd,pwd2))return showJsonError(model, "输入密码和确认密码不一致！");
		if(!StringUtils.equals(member.getNickname(), nickname)){
			boolean nickExist = memberService.isMemberExists(nickname, member.getId());
			if(nickExist) return showJsonError(model, "昵称已被占用！");
		}
		boolean danger = baoKuService.isDanger(member.getEmail(), pwd);
		if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
		member.setEmail(email);
		member.setNickname(nickname);
		member.setPassword(StringUtil.md5(pwd));
		Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
		otherInfoMap.remove(MemberConstant.TAG_DANGER);
		otherInfoMap.put(MemberConstant.TAG_SOURCE,"success");
		memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
		daoService.saveObjectList(member, memberInfo);
		monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
		loginService.updateMemberAuth(sessid, member);
		if(member.isBindMobile()){
			sendWarning("邮箱", member, member.getMobile());
		}
		return showJsonSuccess(model);
	}
	/**
	 * 对外用户同步信息设置
	 */
	@RequestMapping("/home/memberSynchroizaInfo.xhtml")
	public String setMemberSynchroizaInfo(ModelMap model){ 
		Member member = getLogonMember();
		model.putAll(controllerService.getCommonData(model, member, member.getId()));
		List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(MemberConstant.SOURCE_SINA,MemberConstant.SOURCE_QQ),member.getId());
		if(!shareMemberList.isEmpty()){
			for (ShareMember shareMember : shareMemberList) {
				if(MemberConstant.SOURCE_SINA.equals(shareMember.getSource())){
					Map<String, String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
					String rights = otherMap.get("rights");
					model.put("accessRights", otherMap.get("accessrights"));
					model.put("rightsList", rights == null?"":Arrays.asList(rights.split(",")));
					model.put("openMember", shareMember);
				}else if(MemberConstant.SOURCE_QQ.equals(shareMember.getSource())){
					Map<String, String> otherMap = VmUtils.readJsonToMap(shareMember.getOtherinfo());
					String rights = otherMap.get("rights");
					model.put("qqAccessRights", otherMap.get("accessrights"));
					model.put("qqRightsList", rights == null?"":Arrays.asList(rights.split(",")));
					model.put("qqOpenMember", shareMember);
					model.put("qqOpenMemberName",shareMember.getLoginname());
				}
			}
		}
		return "home/acct/new_synchronization.vm";
	}
	
	/**
	 * 保存用户同步信息
	 */
	@RequestMapping("/home/saveSynchroizaInfo.xhtml")
	public String saveMemberSynchroizeInfo(String rights,ModelMap model, String source){
		if(StringUtils.isBlank(rights)) return showJsonError(model, "请选择你要同步的内容！");
		Member member = getLogonMember();
		List<ShareMember> shareMemberList = shareService.getShareMemberByMemberid(Arrays.asList(source), member.getId());
		if(shareMemberList.isEmpty()){
			String msg = "";
			if(MemberConstant.SOURCE_SINA.equals(source)){
				msg = "新浪微薄";
			}else if(MemberConstant.SOURCE_QQ.equals(source)){
				msg = "腾讯微博";
			}
			return showJsonError(model, "请先同步"+msg+"账号！");
		}
		ShareMember shareMember = shareMemberList.get(0);
		shareMember.setOtherinfo(JsonUtils.addJsonKeyValue(shareMember.getOtherinfo(), "rights",rights.substring(0,rights.length()-1)));
		daoService.updateObject(shareMember);
		return showJsonSuccess(model);
	}
	/**
	 * 解除用户同步信息
	 */
	@RequestMapping("/home/removeMemberSynchroizeInfo.xhtml")
	public String removeMemberSynchroizeInfo(Long omid,ModelMap model){
		ShareMember shareMember = daoService.getObject(ShareMember.class, omid);
		if(shareMember != null){
			dbLogger.warn("用户解除同步信息：memberid:" + shareMember.getMemberid() + ", source:" + shareMember.getSource());
			daoService.removeObject(shareMember);
		}
		return showJsonSuccess(model);
	}
	//我的优惠券
	@RequestMapping("/home/card.xhtml")
	public String card(HttpServletRequest request, Long cardid, ModelMap model) throws Exception {
		Member member = getLogonMember();
		ElecCard card = daoService.getObject(ElecCard.class, cardid);
		if(card == null || card.getPossessor()==null || !card.getPossessor().equals(member.getId())){
			Map<String, String> entry = new LinkedHashMap<String, String>();
			entry.put("tag", "elecCard");
			entry.put("memberid", "" + member.getId());
			entry.put("cardid", "" + cardid);
			entry.put("ip", WebUtils.getRemoteIp(request));
			monitorService.addSysLog(SysLogType.monitor, entry);
			return showJsonError(model, "该卡号不存在！");
		}
		boolean isMovieCard = false, isDramaCard = false, isSportCard = false;
		ElecCardBatch elecCardBatch = card.getEbatch();
		List<Long> cinemaidList = BeanUtil.getIdList(elecCardBatch.getValidcinema(), ",");
		List<Long> movieidList = BeanUtil.getIdList(elecCardBatch.getValidmovie(), ",");
		if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_MOVIE)) {
			List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
			List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
			isMovieCard = true;
			model.put("cinemaList", cinemaList);
			model.put("movieList", movieList);
		}else if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_DRAMA)){
			List<Theatre> theatreList = daoService.getObjectList(Theatre.class, cinemaidList);
			List<Drama> dramaList = daoService.getObjectList(Drama.class, movieidList);
			isDramaCard = true;
			model.put("cinemaList", theatreList);
			model.put("movieList", dramaList);
		}else if(StringUtils.equals(card.getEbatch().getTag(), TagConstant.TAG_SPORT)){
			List<Sport> sportList = daoService.getObjectList(Sport.class, cinemaidList);
			List<SportItem> itemList = daoService.getObjectList(SportItem.class, movieidList);
			isSportCard = true;
			model.put("cinemaList", sportList);
			model.put("movieList", itemList);
		}
		if(StringUtils.isNotBlank(elecCardBatch.getWeektype())){
			model.put("strweek", "周" + elecCardBatch.getWeektype());
		}else{
			model.put("strweek", "不限");
		}
		model.put("isMovieCard", isMovieCard);
		model.put("isDramaCard", isDramaCard);
		model.put("isSportCard", isSportCard);
		model.put("cityMap", AdminCityContant.getCitycode2CitynameMap());
		model.put("card", card);
		return "home/acct/card.vm";
	}
	
	//我的优惠活动
	@RequestMapping("/home/code.xhtml")
	public String code(Long codeid, ModelMap model){
		SpCode spCode = daoService.getObject(SpCode.class, codeid);
		if(spCode == null) return showJsonError(model, "卡号错误！");
		Member mebmer = this.getLogonMember();
		if(spCode.getMemberid() == null || !mebmer.getId().equals(spCode.getMemberid())) return showJsonError(model, "参数错误！");
		SpecialDiscount spdiscount = daoService.getObject(SpecialDiscount.class, spCode.getSdid());
		if(StringUtils.isNotBlank(spdiscount.getAddweek())){
			model.put("strweek", "周" + spdiscount.getAddweek());
		}else{
			model.put("strweek", "不限");
		}
		List<Long> cinemaidList = BeanUtil.getIdList(spdiscount.getRelatedid(), ",");
		List<Long> movieidList = BeanUtil.getIdList(spdiscount.getCategoryid(), ",");
		List<Cinema> cinemaList = daoService.getObjectList(Cinema.class, cinemaidList);
		List<Movie> movieList = daoService.getObjectList(Movie.class, movieidList);
		model.put("cinemaList", cinemaList);
		model.put("movieList", movieList);
		model.put("spdiscount", spdiscount);
		model.put("spCode", spCode);
		model.put("paytextMap", PaymethodConstant.getPayTextMap());
		return "home/acct/spCode.vm";
	}
	
	//购票成功，领取特价活动返的优惠券
	@RequestMapping("/home/drawCard.xhtml")
	public String drawCard(ModelMap model,String tradeNo,long sid){
		Member member = getLogonMember();
		if(member==null) return showJsonError(model, "请先登录！");
		GewaOrder order = daoService.getObjectByUkey(GewaOrder.class, "tradeNo", tradeNo, false);
		if(order == null){
			return this.showJsonError(model, "订单已经不存在");
		}
		SpecialDiscount sd = daoService.getObject(SpecialDiscount.class, sid);
		if(sd == null){
			return this.showJsonError(model, "您参加的特价活动已取消！");
		}
		if(!(StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_SUCCESS) || StringUtils.equals(order.getStatus(), OrderConstant.STATUS_PAID_FAILURE))){
			return showJsonError(model, "订单未付款成功，暂不能领取！");
		}
		ErrorCode  code = paymentService.addSpdiscountCard(order, sd, member.getId(), false);
		if(!code.isSuccess()){
			return this.showJsonError(model, code.getMsg());
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/addReComment.xhtml")
	public String addSnsReComment(HttpServletRequest request,ModelMap model){
		Member member = getLogonMember();
		ReComment reComment = new ReComment(member.getId());
		BindUtils.bindData(reComment, request.getParameterMap());
		if(StringUtils.isBlank(reComment.getBody()))return showJsonError(model, "内容不能为空！");
		if(WebUtils.checkString(reComment.getBody()))return showJsonError(model, "内容含有非法字符！");
		if(StringUtil.getByteLength(reComment.getBody())>20000)return showJsonError(model, "内容字符过长！");
		walaApiService.saveReComment(reComment);
		Map map = new HashMap();
		map.put("content", reComment.getBody());
		map.put("headurl", memberService.getCacheHeadpicMap(member.getId()));
		return showJsonSuccess(model, map);
	}
	//用户的基本信息
	@RequestMapping("/home/saveMemberInfo.xhtml")
	public String saveMemberInfo(HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		if(member == null) return showJsonError(model, "请先登录！");
		MemberInfo mi = daoService.getObject(MemberInfo.class, member.getId());
		if(mi==null) {
			mi = new MemberInfo(member.getId(), member.getNickname());
		}
		BindUtils.bindData(mi, request.getParameterMap());
		daoService.saveObject(mi);
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/ajax/member/rescindcard.xhtml")
	public String rescindcard(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		if(memberAccount == null || memberAccount.isNopassword()) {
			Map jsonMap = new HashMap();
			Map errorMap = new HashMap();
			jsonMap.put("errorMap", errorMap);
			errorMap.put("msg", "未设置支付密码");
			return showJsonError(model, jsonMap);
		}
		return "home/acct/rescindCard.vm";
	}
	
	@RequestMapping("/ajax/member/checkpaypassword.xhtml")
	public String checkPayPassword(@CookieValue(value=LOGIN_COOKIE_NAME, required=false)String sessid,
			HttpServletRequest request, ModelMap model){
		String ip = WebUtils.getRemoteIp(request);
		Member member = loginService.getLogonMemberBySessid(ip, sessid);
		if(member == null) return showJsonError(model, "请先登录！");
		MemberAccount memberAccount = daoService.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), true);
		if(memberAccount == null || memberAccount.isNopassword()) {
			Map jsonMap = new HashMap();
			jsonMap.put("msg", "未设置支付密码");
			jsonMap.put("requirePayPass", true);
			return showJsonError(model, jsonMap);
		}
		return showJsonSuccess(model);
	}
	
	@RequestMapping("/home/acct/bindBaseInfo.xhtml")
	public String bindBaseInfo(HttpServletRequest request, ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		String referer = request.getHeader("Referer");
		if(StringUtils.isNotBlank(referer)){
			model.put("comfrom", referer);
		}
		if(!StringUtils.equals(VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), MemberConstant.TAG_SOURCE), "fail"))
			return alertMessage(model, "您已设置过登录密码！", "index.xhtml");
		if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_APP)){
			model.put("source", MemberConstant.REGISTER_APP);
			model.put("from",VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), "openMember"));
		}else if (StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_CODE)) {
			model.put("source", MemberConstant.REGISTER_CODE);
			model.put("mobile", member.getMobile());
		}
		model.put("headpic", memberInfo.getLogo());
		model.put("nickname", member.getNickname());
		return "home/acct/bindBaseInfo.vm";
	}
	
	@RequestMapping("/home/acct/saveBaseInfo.xhtml")
	public String saveBaseInfo(@CookieValue(LOGIN_COOKIE_NAME)String sessid, 
			ModelMap model,String nickname,String email,String pwd,String pwd2, String mobile,String checkpass,String source,String op,HttpServletRequest request){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		if(!StringUtils.equals(VmUtils.getJsonValueByKey(memberInfo.getOtherinfo(), MemberConstant.TAG_SOURCE), "fail")) 
			return alertMessage(model, "您已设置过登录密码！", "index.xhtml");
		if(StringUtils.equals(source,MemberConstant.REGISTER_APP)){
			if (StringUtils.equals(op, "bindEmail")) {
				if(StringUtils.isBlank(nickname)) return showJsonError(model, "昵称不能为空！");
				if(StringUtils.isBlank(pwd)) return showJsonError(model, "密码不能为空！"); 
				if(StringUtils.isBlank(email)) return showJsonError(model, "邮箱不能为空！");
				boolean matchNickname = ValidateUtil.isCNVariable(nickname, 1, 20);
				if(!matchNickname) return showJsonError(model,"昵称不能包含特殊符号！");
				boolean isEmail = ValidateUtil.isEmail(email);
				if(!isEmail)return showJsonError(model, "邮箱格式不正确！");
				String key = blogService.filterAllKey(nickname);
				if(StringUtils.isNotBlank(key)) return showJsonError(model, "昵称含有非法关键字！"); 
				boolean emailExist = memberService.isMemberExists(email, member.getId());
				if(emailExist) return showJsonError(model, "邮箱已注册，请更换邮箱地址！");
				if(!ValidateUtil.isPassword(pwd)){
					return showJsonError(model, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
				}
				if(!StringUtils.equals(pwd,pwd2))return showJsonError(model, "输入密码和确认密码不一致！");
				if(!StringUtils.equals(member.getNickname(), nickname)){
					boolean nickExist = memberService.isMemberExists(nickname, member.getId());
					if(nickExist) return showJsonError(model, "昵称已被占用！");
				}
				boolean danger = baoKuService.isDanger(member.getEmail(), pwd);
				if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
				member.setEmail(email);
				member.setNickname(nickname);
				member.setPassword(StringUtil.md5(pwd));
				Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
				otherInfoMap.remove(MemberConstant.TAG_DANGER);
				otherInfoMap.put(MemberConstant.TAG_SOURCE,"success");
				otherInfoMap.put(MemberConstant.TAG_EMAIL_BINDTIME,DateUtil.getCurTimeStr());
				memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
				memberInfo.setUpdatetime(new Timestamp(System.currentTimeMillis()));
				daoService.saveObjectList(member, memberInfo);
				monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
				monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODEMAIL, null, WebUtils.getRemoteIp(request));
				loginService.updateMemberAuth(sessid, member);
				if(member.isBindMobile()){
					sendWarning("邮箱", member, member.getMobile());
				}
			}else if (StringUtils.equals(op, "bindMobile")) {
				boolean allow = operationService.updateOperation(BindConstant.TAG_BINDMOBILE + "_" + member.getId(), 30);
				if(!allow){
					dbLogger.warn(BindConstant.TAG_BINDMOBILE + "_" + member.getId() + ":" + WebUtils.getRemoteIp(request));
					return showJsonError(model, "操作过于频繁！");
				}
				
				if(StringUtils.isBlank(nickname)) return showJsonError(model, "昵称不能为空！");
				boolean matchNickname = ValidateUtil.isCNVariable(nickname, 1, 20);
				if(!matchNickname) return showJsonError(model,"昵称不能包含特殊符号！");
				String key = blogService.filterAllKey(nickname);
				if(StringUtils.isNotBlank(key)) return showJsonError(model, "昵称含有非法关键字！"); 
				if(!StringUtils.equals(member.getNickname(), nickname)){
					boolean nickExist = memberService.isMemberExists(nickname, member.getId());
					if(nickExist) return showJsonError(model, "昵称已被占用！");
				}
				if(StringUtils.isBlank(checkpass))return showJsonError(model, "手机动态码不能为空！");
				if(!ValidateUtil.isMobile(mobile)) return showJsonError(model, "手机号码格式不正确！");
				if(member.isBindMobile()) return showJsonError(model,"您已绑定手机！");
				boolean exists = memberService.isMemberMobileExists(mobile);
				if(exists) return showJsonError(model, "该号码已绑定其他帐号，请更换其他手机号码！");
				
				if(StringUtils.isBlank(pwd)) return showJsonError(model, "密码不能为空！"); 
				if(!ValidateUtil.isPassword(pwd)){
					return showJsonError(model, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
				}
				if(!StringUtils.equals(pwd,pwd2))return showJsonError(model, "输入密码和确认密码不一致！");
				boolean danger = baoKuService.isDanger(member.getEmail(), pwd);
				if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
				
				
				ErrorCode code = memberService.bindMobile(member, mobile, checkpass, WebUtils.getRemoteIp(request));
				if(!code.isSuccess()) {
					return showJsonError(model, code.getMsg());
				}
				memberInfo = (MemberInfo) code.getRetval();
				member.setNickname(nickname);
				member.setPassword(StringUtil.md5(pwd));
				if(!memberInfo.isFinishedTask(MemberConstant.TASK_BINDMOBILE)){
					if(StringUtils.isNotBlank(member.getMobile())){
						memberInfo.finishTask(MemberConstant.TASK_BINDMOBILE);
					}
				}
				Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
				otherInfoMap.remove(MemberConstant.TAG_DANGER);
				otherInfoMap.put(MemberConstant.TAG_SOURCE,"success");
				otherInfoMap.put(MemberConstant.TAG_MOBILE_BINDTIME,DateUtil.getCurTimeStr());
				memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
				memberInfo.setUpdatetime(new Timestamp(System.currentTimeMillis()));
				daoService.saveObjectList(member, memberInfo);
				loginService.updateMemberAuth(sessid, member);
				Map<String, String> logInfo = new HashMap<String, String>();
				logInfo.put("mobile", VmUtils.getSmobile(mobile));
				monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_BINDMOBILE, logInfo, WebUtils.getRemoteIp(request));
				monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
				monitorService.saveMemberLog(memberInfo.getId(), MemberConstant.ACTION_NEWTASK, MemberConstant.TASK_BINDMOBILE, null);
				sendWarning("手机号", member, member.getMobile(),member.getEmail());
			}
		}else if (StringUtils.equals(source, MemberConstant.REGISTER_CODE)) {
			if(StringUtils.isBlank(nickname)) return showJsonError(model, "昵称不能为空！");
			if(StringUtils.isBlank(pwd)) return showJsonError(model, "密码不能为空！"); 
			boolean matchNickname = ValidateUtil.isCNVariable(nickname, 1, 20);
			if(!matchNickname) return showJsonError(model,"昵称不能包含特殊符号！");
			String key = blogService.filterAllKey(nickname);
			if(StringUtils.isNotBlank(key)) return showJsonError(model, "昵称含有非法关键字！"); 
			if(!ValidateUtil.isPassword(pwd)){
				return showJsonError(model, "密码格式不正确,只能是字母，数字，下划线，长度6—14位！");
			}
			if(!StringUtils.equals(pwd,pwd2))return showJsonError(model, "输入密码和确认密码不一致！");
			if(!StringUtils.equals(member.getNickname(), nickname)){
				boolean nickExist = memberService.isMemberExists(nickname, member.getId());
				if(nickExist) return showJsonError(model, "昵称已被占用！");
			}
			boolean danger = baoKuService.isDanger(member.getEmail(), pwd);
			if(danger) return showJsonError(model, "该帐户设置的密码存在安全风险，不能设置为该密码！");
			member.setNickname(nickname);
			member.setPassword(StringUtil.md5(pwd));
			Map<String, String> otherInfoMap = JsonUtils.readJsonToMap(memberInfo.getOtherinfo());
			otherInfoMap.remove(MemberConstant.TAG_DANGER);
			otherInfoMap.put(MemberConstant.TAG_SOURCE,"success");
			memberInfo.setOtherinfo(JsonUtils.writeMapToJson(otherInfoMap));
			memberInfo.setUpdatetime(new Timestamp(System.currentTimeMillis()));
			daoService.saveObjectList(member, memberInfo);
			monitorService.saveMemberLog(member.getId(), MemberConstant.ACTION_MODPWD, null, WebUtils.getRemoteIp(request));
			loginService.updateMemberAuth(sessid, member);
		}
		return showJsonSuccess(model);
	}
}