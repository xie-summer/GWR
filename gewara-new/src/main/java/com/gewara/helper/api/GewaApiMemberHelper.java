package com.gewara.helper.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.AdminCityContant;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberUsefulAddress;
import com.gewara.model.user.SysMessageAction;
import com.gewara.util.VmUtils;

public class GewaApiMemberHelper {
	//用户登录后返回信息
	public static Map<String, Object> getLoginMember(Member member, String memberEncode, String headpic){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("memberid", member.getId());
		params.put("nickname", member.getNickname());
		params.put("mobile", member.getMobile());
		params.put("email", member.getEmail());
		params.put("headpic", headpic);
		if(StringUtils.isNotBlank(memberEncode)) params.put("memberEncode", memberEncode);
		return params;
	}
	//系统消息
	public static Map<String, Object> getMovieData(SysMessageAction sysMessage){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("messageid", sysMessage.getId());
		params.put("body", VmUtils.getText(sysMessage.getBody()));
		params.put("isread", sysMessage.getIsread());
		params.put("addtime", sysMessage.getAddtime());
		return params;
	}
	
	//用户常用联系地址
	public static Map<String, Object> getMemberAddressData(MemberUsefulAddress memberAddress){
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("addressid", memberAddress.getId());
		if(AdminCityContant.zxsList.contains(memberAddress.getProvincecode())){
			params.put("address", memberAddress.getCityname()+memberAddress.getAddress());
		}else {
			params.put("address", memberAddress.getProvincename()+memberAddress.getCityname()+memberAddress.getAddress());
		}
		
		params.put("citycode", memberAddress.getCitycode());
		params.put("cityname", memberAddress.getCityname());
		params.put("countycode", memberAddress.getCountycode());
		params.put("countyname", memberAddress.getCountyname());
		params.put("mobile", memberAddress.getMobile());
		params.put("postalcode", memberAddress.getPostalcode());
		params.put("provincename", memberAddress.getProvincename());
		params.put("provincecode", memberAddress.getProvincecode());
		params.put("realname", memberAddress.getRealname());
		return params;
	}
}
