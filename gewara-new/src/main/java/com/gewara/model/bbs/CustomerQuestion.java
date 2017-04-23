package com.gewara.model.bbs;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.model.BaseObject;

/**
 *  @function 用户提问,建议,bug
 * 	@author bob.hu
 *	@date	2011-03-11 11:00:46
 */
public class CustomerQuestion extends BaseObject {
	/***
	 * 权限
	 */
	public static final String CustomerPermission = "customer";

	/**
	 *	用户提问的类型 服务评价/改进建议/其它
	 * 
	 */
	public static final String TAG_EVALUATE = "evaluate"; //服务评价
	public static final String TAG_ADVISE = "advise"; //改进建议
	public static final String TAG_ADVISE_DRAMA = "adviseDrama"; //演出建议
	public static final String TAG_ADVISE_SPORT = "adviseSport"; //运动建议
	public static final String TAG_MOBILE = "mobile";	//手机客户端
	public static final String TAG_OTHER = "other"; //其它
	public static final List<String> TAG_LIST = Arrays.asList(new String[]{ "evaluate", "advise", "mobile", "other","adviseDrama","adviseSport"});
	
	/**
	 *  帖子状态 待解决/ 已回复/ 已关闭
	 * */
	public static final String Y_NEW = "Y_NEW";
	public static final String Y_TREAT = "Y_TREAT";	
	public static final String Y_STOP = "Y_STOP";
	
	public static final String N_DELETE = "N_DELETE";
	public static final String N_FILTER = "N_FILTER";
	
	private Long id;
	private Long memberid;
	private String membername;
	private String email;
	private String body;
	private Timestamp addtime;
	private String tag;
	private String status;
	private String type;
	private Timestamp updatetime;
	private String citycode;
	private BaseObject member;
	private String feedbackType;//反馈归属类型
	
	private static final long serialVersionUID = 1365933982357637583L;

	public CustomerQuestion(){}
	
	public CustomerQuestion(String email) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.status = Status.Y_NEW;
		this.email = email;
		this.type = AddressConstant.ADDRESS_WEB;
		this.feedbackType="other";
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberid() {
		return memberid;
	}

	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}

	public String getMembername() {
		return membername;
	}

	public void setMembername(String membername) {
		this.membername = membername;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Timestamp getAddtime() {
		return addtime;
	}

	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Timestamp getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public BaseObject getMember() {
		return member;
	}

	public void setMember(BaseObject member) {
		this.member = member;
	}
	
	public String getStatusText(){
		if(StringUtils.equals(status, Y_NEW))return "待回复";
		if(StringUtils.equals(status, Y_TREAT))return "已回复";
		if(StringUtils.equals(status, Y_STOP))return "已关闭";
		return "待回复";
	}
	
	public String getFromFlag(){
		String stype = StringUtils.lowerCase(this.type);
		return AddressConstant.addressMap.get(stype);
	}
	
	public boolean getPic() {// 有无图片
		return StringUtils.contains(this.body, "<img");
	}
	public String getFeedbackType() {
		return feedbackType;
	}

	public void setFeedbackType(String feedbackType) {
		this.feedbackType = feedbackType;
	}
}
