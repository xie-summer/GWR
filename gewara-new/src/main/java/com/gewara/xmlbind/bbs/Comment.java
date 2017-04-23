package com.gewara.xmlbind.bbs;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.constant.order.AddressConstant;
import com.gewara.model.BaseObject;
import com.gewara.model.user.Member;

public class Comment extends BaseObject implements Serializable{

	private static final long serialVersionUID = 4476980910614491968L;
	public static final String POINT_MODERATOR = "moderator";	//发表哇啦话题加积分

	protected Long id;
	protected String body;			//内容
	protected Timestamp addtime;	//添加时间
	protected String tag;
	protected Long relatedid;
	protected Integer flowernum; 	//鲜花数
	protected String status;
	protected Long memberid;
	protected String nickname;
	protected String flag;
	protected Long transferid;		//转载id
	protected String address;		//发表来源
	protected Integer replycount;	//回复数
	protected Integer transfercount;//转载数
	protected Timestamp replytime;
	protected String topic;			//WALA主题
	protected Integer generalmark;	//评分
	protected String otherinfo;
	protected String apptype;
	protected String link;
	protected String pointx;
	protected String pointy;
	protected String ip; 			//发表哇啦用户的Ip 
	protected String picturename;
	protected Timestamp orderTime;	//用来排序的时间，根据回复，转发或顶来权重加成
	protected Integer bodyLength;	//发布哇啦的内容长度
	protected String suspectedAd;	//是否疑似广告wala，这类哇啦排除在热门哇啦外
	
	public static final String TYPE_ADDREPLY = "add";//回复数加一
	public static final String TYPE_DOWNREPLY = "down";//回复数减一
	public Comment(){
	}
	public Comment(Member member){
		this.memberid = member.getId();
		this.transfercount = 0;
		this.replycount = 0;
		this.flowernum = 0;
		this.status = Status.Y_NEW;
		this.address=AddressConstant.ADDRESS_WEB;
		this.nickname = member.getNickname();
		this.addtime=new Timestamp(System.currentTimeMillis());
	}
	public Comment(Member member, String tag, Long relatedId, String body){
		this(member);
		this.tag = tag;
		this.relatedid = relatedId;
		this.body = body;
	}
	
	public Comment(Member member, String tag, Long relatedId, String body, String picturename, String link){
		this(member);
		this.tag = tag;
		this.relatedid = relatedId;
		this.body = body;
		this.picturename= picturename;
		this.link = link;
	}

	public String getAddressInfo(){
		return AddressConstant.addressMap.get(address);
	}
	public Integer getTransfercount() {
		return transfercount;
	}
	public void setTransfercount(Integer transfercount) {
		this.transfercount = transfercount;
	}
	public Integer getReplycount() {
		return replycount;
	}
	public void setReplycount(Integer replycount) {
		this.replycount = replycount;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public Long getTransferid() {
		return transferid;
	}
	public void setTransferid(Long transferid) {
		this.transferid = transferid;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public void addFlag(String sflag){
		if(StringUtils.isBlank(sflag)) this.flag = sflag;
		if(!StringUtils.contains(this.flag, sflag)) {
			if(StringUtils.isBlank(this.flag)) this.flag = sflag;
			else this.flag += "," + sflag;
		}
		
	}
	
	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
	public void add2Replycount(String type){
		if(this.replycount == null) this.replycount = 0;
		if(TYPE_ADDREPLY.equals(type))
			this.replycount +=1;
		else if(TYPE_DOWNREPLY.equals(type))
			this.replycount -=1;
	}
	

	public void addTransfercount(){
		this.transfercount +=1;
	}
	public Integer getFlowernum() {
		return flowernum;
	}
	public void setFlowernum(Integer flowernum) {
		this.flowernum = flowernum;
	}
	public void addFlowernum(){
		if(this.flowernum == null){
			this.flowernum = 0;
		}
		this.flowernum += 1;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Long getMemberid(){
		if(memberid!=null) return memberid;
		return null;
	}
	public Timestamp getReplytime() {
		return replytime;
	}
	public void setReplytime(Timestamp replytime) {
		this.replytime = replytime;
	}
	public String getFromFlag(){
		return AddressConstant.addressMap.get(this.address);
	}
	public String getFromFlag2(){
		String str = AddressConstant.addressMap.get(this.address);
		if(StringUtils.equals(str, "手机短信")) return "手机";
		return str;
	}
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Integer getGeneralmark() {
		return generalmark;
	}
	public void setGeneralmark(Integer generalmark) {
		this.generalmark = generalmark;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public String getApptype() {
		return apptype;
	}
	public void setApptype(String apptype) {
		this.apptype = apptype;
	}
	public String getPicturename() {
		return picturename;
	}
	public void setPicturename(String picturename) {
		this.picturename = picturename;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getPointx() {
		return pointx;
	}
	public void setPointx(String pointx) {
		this.pointx = pointx;
	}
	public String getPointy() {
		return pointy;
	}
	public void setPointy(String pointy) {
		this.pointy = pointy;
	}
	public String getIp(){
		return ip;
	}
	public void setIp(String ip){
		this.ip = ip;
	}
	@Override
	public Serializable realId() {
		return id;
	}
	public Timestamp getOrderTime() {
		return orderTime;
	}
	public void setOrderTime(Timestamp orderTime) {
		this.orderTime = orderTime;
	}
	public Integer getBodyLength() {
		return bodyLength;
	}
	public void setBodyLength(Integer bodyLength) {
		this.bodyLength = bodyLength;
	}
	public String getSuspectedAd() {
		return suspectedAd;
	}
	public void setSuspectedAd(String suspectedAd) {
		this.suspectedAd = suspectedAd;
	}
}
