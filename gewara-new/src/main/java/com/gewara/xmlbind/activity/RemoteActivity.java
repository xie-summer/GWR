package com.gewara.xmlbind.activity;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.Status;
import com.gewara.util.DateUtil;
import com.gewara.xmlbind.BaseInnerResponse;

public class RemoteActivity extends BaseInnerResponse {
	public static final int TIME_CURRENT = 3; // 未过期的活动
	public static final int TIME_OVER = 2; // 过期（结束）的活动
	public static final int TIME_ALL = 1; // 所有活动
	public static final int TIME_RECORD = 10; // 
	
	public static final String FLAG_TOP_HEAD = "tophead";// 总论坛置顶
	public static final String FLAG_TOP_RELATED = "toprelated";// 分论坛置顶
	public static final String FLAG_TOP_CATEGORY = "topcategory";// 分论坛中项目置顶
	public static final String FLAG_RECOMMEND = "recommend";// 推荐
	public static final String FLAG_RECOMMEND_WAP = "redwap";// 推荐手机
	public static final String FLAG_HOME = "home";// 首页
	public static final String FLAG_HOT = "hot";// 精华
	
	public static final String ATYPE_USER = "user"; // 用户活动
	public static final String ATYPE_BUSS = "buss"; // 商家活动
	public static final String ATYPE_GEWA = "gewa"; // gewa活动
	
	public static final String SIGN_PRICE5 = "price5"; // 5元抢票
	public static final String SIGN_PUBSALE = "pubsale";	// 竞拍
	public static final String SIGN_STARMEET = "starmeet";// 明星见面会
	public static final String SIGN_ONLINE = "online";// 线上活动
	public static final String SIGN_RESERVE = "reserve";//约战
	
	public static final String OTHER_BINDMOBILE = "bindMobile";	//用户参加活动需绑定手机
	public static final String OTHER_BINDEMAIL = "bindEmail";	//用户参加活动需绑定邮箱
	public static final String OTHER_HASHEADURL = "hasHeadUrl";	//用户参加活动需上传头像
	public static final String OTHER_HASADDRESS = "hasAddress";	//用户参加活动需填写地址
	public static final String OTHER_WALA = "wala";				//用户参加活动后才能发WALA
	public static final String OTHER_NEWMEMBER = "newMember";	//新用户才能参加活动
	public static final String OTHER_USEPOINT = "usePoint";		//用户参加活动需消耗积分
	public static final String OTHER_TICKET = "ticket";			//购买特定电影用户才能参加
	public static final String OTHER_LONGWALA = "longWala";		//叠楼活动特殊哇啦
	public static final String OTHER_EASYJOIN = "easyJoin";		//简单参加活动，无需填写更多信息
	
	public static final String DATETYPE_TODAY = "today";
	public static final String DATETYPE_TOMORROW = "tomorrow";
	public static final String DATETYPE_WEEKEDN = "weekend";
	public static final String DATETYPE_AWEEK = "aweek";
	
	private Long id;
	private String title;			// 标题
	private String contentdetail;	// 内容
	private String atype;			// 类型
	private Date startdate;
	private String starttime;
	private Date enddate;
	private String endtime;
	private String address;
	private String contactway;		// 联系方式
	private String summary;			// 推荐说明
	private Integer capacity;
	private Long memberid;			// 发起者
	private Integer clickedtimes;
	private String citycode;
	private String countycode;
	private String indexareacode;
	private Timestamp addtime;
	private Long relatedid;
	private String tag;
	private String category;
	private Long categoryid;
	private Integer membercount;	//已参加活动人数
	private String status;
	private Timestamp replytime;
	private Integer replycount;
	private Long replyid;			// 回复者
	private Long communityid;		// 圈子
	private Timestamp updatetime;
	private String flag;
	private String logo;
	private String priceinfo;
	private String seotitle;
	private String seodescription;
	private String repeat;
	private String membername;
	private String replyname;
	private String sign; 			//活动类型 
	private Timestamp duetime; 		//报名截止时间
	private String activityurl;		//线上活动关联网址
	private String mobilemsg;		//活动手机短信
	private String qq;				//联系qq号
	
	private String needprepay;		// 是否预付费
	
	private Integer totalFee;		// 临时变量, 保存该活动总的收费
	private String joinLimit;		//加入人数限制
	
	private Timestamp fromtime; 	//活动报名开始时间
	private Long signid;			//sign相关联的id，5元抢票，1元竞拍时使用
	private String otherinfo;
	private Integer collectedtimes;	//感兴趣
	private String linkman;			//联系人
	private Integer memberLimit;	//活动人数
	private String ip; 				//发起活动人的IP
	private String usePoint;
	private String joinForm;
	private String onlinePay;		//线上支付
	private Object relate1;
	private Object relate2;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return title;
	}
	public String getCname() {
		return title;
	}
	public String getContentdetail() {
		return contentdetail;
	}
	public void setContentdetail(String contentdetail) {
		this.contentdetail = contentdetail;
	}
	public String getAtype() {
		return atype;
	}
	public void setAtype(String atype) {
		this.atype = atype;
	}
	public Date getStartdate() {
		return startdate;
	}
	public void setStartdate(Date startdate) {
		this.startdate = startdate;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public Date getEnddate() {
		return enddate;
	}
	public void setEnddate(Date enddate) {
		this.enddate = enddate;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getContactway() {
		return contactway;
	}
	public void setContactway(String contactway) {
		this.contactway = contactway;
	}
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public Integer getCapacity() {
		return capacity;
	}
	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}
	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	public Integer getClickedtimes() {
		return clickedtimes;
	}
	public void setClickedtimes(Integer clickedtimes) {
		this.clickedtimes = clickedtimes;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	public String getCountycode() {
		return countycode;
	}
	public void setCountycode(String countycode) {
		this.countycode = countycode;
	}
	public String getIndexareacode() {
		return indexareacode;
	}
	public void setIndexareacode(String indexareacode) {
		this.indexareacode = indexareacode;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Long getRelatedid() {
		return relatedid;
	}
	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public Long getCategoryid() {
		return categoryid;
	}
	public void setCategoryid(Long categoryid) {
		this.categoryid = categoryid;
	}
	public Integer getMembercount() {
		return membercount;
	}
	public void setMembercount(Integer membercount) {
		this.membercount = membercount;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Timestamp getReplytime() {
		return replytime;
	}
	public void setReplytime(Timestamp replytime) {
		this.replytime = replytime;
	}
	public Integer getReplycount() {
		return replycount;
	}
	public void setReplycount(Integer replycount) {
		this.replycount = replycount;
	}
	public Long getReplyid() {
		return replyid;
	}
	public void setReplyid(Long replyid) {
		this.replyid = replyid;
	}
	public Long getCommunityid() {
		return communityid;
	}
	public void setCommunityid(Long communityid) {
		this.communityid = communityid;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getFlag() {
		return flag;
	}
	public void setFlag(String flag) {
		this.flag = flag;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getLimg() {
		return logo;
	}
	public String getPriceinfo() {
		return priceinfo;
	}
	public void setPriceinfo(String priceinfo) {
		this.priceinfo = priceinfo;
	}
	public String getSeotitle() {
		return seotitle;
	}
	public void setSeotitle(String seotitle) {
		this.seotitle = seotitle;
	}
	public String getSeodescription() {
		return seodescription;
	}
	public void setSeodescription(String seodescription) {
		this.seodescription = seodescription;
	}
	public String getRepeat() {
		return repeat;
	}
	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}
	public String getMembername() {
		return membername;
	}
	public void setMembername(String membername) {
		this.membername = membername;
	}
	public String getReplyname() {
		return replyname;
	}
	public void setReplyname(String replyname) {
		this.replyname = replyname;
	}
	public String getSign() {
		return sign;
	}
	public void setSign(String sign) {
		this.sign = sign;
	}
	public Timestamp getDuetime() {
		return duetime;
	}
	public void setDuetime(Timestamp duetime) {
		this.duetime = duetime;
	}
	public String getActivityurl() {
		return activityurl;
	}
	public void setActivityurl(String activityurl) {
		this.activityurl = activityurl;
	}
	public String getMobilemsg() {
		return mobilemsg;
	}
	public void setMobilemsg(String mobilemsg) {
		this.mobilemsg = mobilemsg;
	}
	public String getQq() {
		return qq;
	}
	public void setQq(String qq) {
		this.qq = qq;
	}
	public String getNeedprepay() {
		return needprepay;
	}
	public void setNeedprepay(String needprepay) {
		this.needprepay = needprepay;
	}
	public Integer getTotalFee() {
		return totalFee;
	}
	public void setTotalFee(Integer totalFee) {
		this.totalFee = totalFee;
	}
	public String getJoinLimit() {
		return joinLimit;
	}
	public void setJoinLimit(String joinLimit) {
		this.joinLimit = joinLimit;
	}
	public Timestamp getFromtime() {
		return fromtime;
	}
	public void setFromtime(Timestamp fromtime) {
		this.fromtime = fromtime;
	}
	public Long getSignid() {
		return signid;
	}
	public void setSignid(Long signid) {
		this.signid = signid;
	}
	public String getOtherinfo() {
		return otherinfo;
	}
	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public Integer getCollectedtimes() {
		return collectedtimes;
	}
	public void setCollectedtimes(Integer collectedtimes) {
		this.collectedtimes = collectedtimes;
	}
	public String getLinkman() {
		return linkman;
	}
	public void setLinkman(String linkman) {
		this.linkman = linkman;
	}
	public Integer getMemberLimit() {
		return memberLimit;
	}
	public void setMemberLimit(Integer memberLimit) {
		this.memberLimit = memberLimit;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getDateRange(String splitor){
		String str1 = DateUtil.format(startdate, "M月d日");
		String str2 = (enddate==null ? "": DateUtil.format(enddate, "M月d日"));
		if(StringUtils.isBlank(str2) || StringUtils.equals(str1, str2)) return str1;
		return str1+splitor+str2;
	}
	public String getTimeRange(String splitor){
		if(StringUtils.isBlank(starttime)) return "";
		if(StringUtils.isBlank(endtime)) return starttime;
		return starttime + splitor + endtime;
	}
	public Timestamp getActivityStartTime(){
		String activitytime1 = DateUtil.formatDate(this.startdate);
		String activitytime2 ="";
		if(StringUtils.isNotBlank(this.starttime)){
			activitytime2 = this.starttime +":00"; 
		}else {
			activitytime2 = "00:00:00";
		}
		Timestamp agendatime = DateUtil.parseTimestamp(activitytime1 +" "+activitytime2);
		return agendatime;
	}
	
	public Timestamp getActivityEndTime(){
		String activitytime1 = DateUtil.formatDate(this.enddate);
		String activitytime2 ="";
		if(StringUtils.isNotBlank(this.endtime)){
			activitytime2 = this.endtime +":00"; 
		}else {
			activitytime2 = "00:00:00";
		}
		Timestamp agendatime = DateUtil.parseTimestamp(activitytime1 +" "+activitytime2);
		return agendatime;
	}
	public boolean isOver(){
		if(enddate != null){
			return DateUtil.addDay(new Date(), -1).after(enddate);
		}else{
			if(startdate==null) return true;
			return DateUtil.addDay(new Date(), -1).after(startdate);
		}
	}
	public boolean isPlaying(){
		Timestamp cur=new Timestamp(System.currentTimeMillis());
		if(duetime==null || fromtime==null) return false;
		return duetime.after(cur) && this.fromtime.before(cur);
	}
	public boolean isStart() {
		Timestamp cur=new Timestamp(System.currentTimeMillis());
		if(fromtime==null) return false;
		return !this.fromtime.after(cur);
	}
	public boolean isEnd() {
		Timestamp cur=new Timestamp(System.currentTimeMillis());
		if(duetime==null) return true;
		return this.duetime.before(cur);
	}
	public boolean isJoin(){
		return isPlaying() && (Status.Y_NEW.equals(this.status) || Status.Y_PROCESS.equals(this.status)); 
	}
	public boolean isOver2(){
		if(this.enddate != null && StringUtils.isNotBlank(this.endtime)){
			Timestamp cur=new Timestamp(System.currentTimeMillis());
			String strDate = DateUtil.formatDate(this.enddate) + " " + this.endtime + ":00";
			return DateUtil.parseTimestamp(strDate).before(cur);
		}
		return false;
	}
	
	public Object getRelate1() {
		return relate1;
	}
	public void setRelate1(Object relate1) {
		this.relate1 = relate1;
	}
	public Object getRelate2() {
		return relate2;
	}
	public void setRelate2(Object relate2) {
		this.relate2 = relate2;
	}
	public String getUsePoint() {
		return usePoint;
	}
	public void setUsePoint(String usePoint) {
		this.usePoint = usePoint;
	}
	public String getJoinForm() {
		return joinForm;
	}
	public void setJoinForm(String joinForm) {
		this.joinForm = joinForm;
	}
	public String getOnlinePay() {
		return onlinePay;
	}
	public void setOnlinePay(String onlinePay) {
		this.onlinePay = onlinePay;
	}
	@Override
	public final int hashCode() {
		return (id == null) ? 0 : id.hashCode();
	}
	@Override
	public final boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		RemoteActivity other = (RemoteActivity) obj;
		return !(this.id != null ? !(this.id.equals(other.getId())) : (other.getId() != null));
	}
}
