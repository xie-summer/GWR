package com.gewara.web.action.partner;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gewara.constant.MemberConstant;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.acl.GewaraUser;
import com.gewara.model.partner.BindUnionpayMember;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.mongo.MongoService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.StringUtil;
@Controller
public class PartnerUnionpayBindUsersController extends BasePartnerController{
	public static final String KEY = "21b12361-8541-498c-9a27-63dd36b8721f";//"c712cf0b-ce2e-4bcb-91f6-c5541de719bd";
	public static final String SOURCE_ID = "105290078320121";
	public static final String FORWARD_URL = "https://online.unionpay.com/portal/user/introUser.do";//"http://58.246.226.99/portal/user/introUser.do"/*"https://online.unionpay.com/portal/user/introUser.do"*/;
	@Autowired@Qualifier("mongoService")
	protected MongoService mongoService;
	public void setMongoService(MongoService mongoService) {
		this.mongoService = mongoService;
	}
	
	private  Member getLogonMember(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth == null) return null;
		if(auth.isAuthenticated() && !auth.getName().equals("anonymous")){//µÇÂ¼
			GewaraUser user = (GewaraUser) auth.getPrincipal();
			if(user instanceof Member) {
				return (Member) user;
			}
		}
		return null;
	}
	
	@RequestMapping("/home/acct/forwardUnionpayRegister.xhtml")
	public String forwardUnionpayRegister(ModelMap model){
		Member member = getLogonMember();
		MemberInfo memberInfo = daoService.getObject(MemberInfo.class, member.getId());
		int regType = 2;
		String mobile = "";
		String email = "";
		if(StringUtils.equals(memberInfo.getSource(), MemberConstant.REGISTER_MOBLIE)){
			regType = 1;
			mobile = member.getMobile();
		}else{
			email = member.getEmail();
		}
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("sourceId", SOURCE_ID);
		tmpMap.put("outerUserId", member.getId() + "");
		tmpMap.put("regType", regType + "");
		if(regType == 1){
			tmpMap.put("mobile", mobile);
		}else{
			tmpMap.put("email", email);
		}
		String data = JsonUtils.writeMapToJson(tmpMap);
		tmpMap.put("token",  StringUtil.md5(data + StringUtil.md5(KEY)));
		try {
			Map<String,String> submitParams = new HashMap<String,String>();
			String bizreq = "{\"data\":"+data+",\"token\":\""+tmpMap.get("token")+"\"}";
			submitParams.put("bizreq", Base64.encodeBase64String(bizreq.getBytes("UTF-8")));
			model.put("submitParams",submitParams);
			model.put("method","post");
			model.put("submitUrl",FORWARD_URL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "tempSubmitForm.vm";
	}
	
	@RequestMapping("/pay/bindUnionpayCallback.xhtml")
	@ResponseBody
	public String forwardUnionpayRegisterCallback(String bizres){
		if(StringUtils.isNotBlank(bizres)){
			try {
				bizres = new String(Base64.decodeBase64(bizres),"UTF-8");
				Map map = JsonUtils.readJsonToMap(bizres);
				Object token = map.get("token");
				Object data = map.get("data");
				if(token != null && StringUtils.isNotBlank(token.toString()) && data != null){
					Map<String,String> datas = (Map<String,String>)data;
					if(StringUtils.equals(token.toString(), StringUtil.md5( JsonUtils.writeMapToJson(datas) + StringUtil.md5(KEY)))){
						Map params = new HashMap();
						params.put("memberId", Long.parseLong(datas.get("outerUserId")));
						params.put("usrState", datas.get("usrState"));
						params.put("notifyType", datas.get("notifyType"));
						params.put("cardNo", datas.get("extFiled"));
						List<BindUnionpayMember> bindMembers = mongoService.getObjectList(BindUnionpayMember.class,params,"addTime", false,0,1);
						if(bindMembers == null || bindMembers.size() == 0){
							Member member = this.daoService.getObject(Member.class, Long.parseLong(datas.get("outerUserId")));
							if(member != null){
								BindUnionpayMember bindMember = new BindUnionpayMember();
								bindMember.set_id(ObjectId.uuid());
								bindMember.setAddTime(DateUtil.formatTimestamp(new Date()));
								bindMember.setCardNo(datas.get("extFiled"));
								bindMember.setMemberId(member.getId());
								bindMember.setNotifyType(datas.get("notifyType"));
								bindMember.setUsrState(datas.get("usrState"));
								mongoService.addObject(bindMember, MongoData.SYSTEM_ID);
								return "success";
							}
						}
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		return "fail";
	}
}
