package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.constant.MemberConstant;
import com.gewara.model.BaseObject;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;

public class MemberInfo extends BaseObject {
	private static final long serialVersionUID = -3838425704891306595L;
	public static String[] disallowBindField = new String[]{//不允许绑定的字段
		"regfrom", "headpic", "addtime", "updatetime", "rights", "expvalue", 
		"otherinfo", "newtask", "pointvalue", "inviteid", "ip"
	};

	private Long id;
	//从Member中移动到此处
	private String nickname;		//与Member中相同
	private String sex; // 性别
	private String blood;
	private String msn;
	private String qq;
	private String company;
	private String tag; // 感兴趣的版块
	private String sign;
	
	private String liveprovince;
	private String livecity;
	private String livecounty;
	private String liveindexarea;
	private String fromcity;
	private Long universityid;
	private String universityname;
	private String birthday;
	private String realname;
	private String phone;
	private String introduce;
	private String postcode;//邮编
	private String favortag;	// 兴趣爱好
	private String invitetype;//注册类型，比如：抽奖邀请
	private String address;
	private String source; //注册来源
	
	//系统信息
	private String regfrom;
	private String headpic; 		//头像
	private Timestamp addtime;
	private Timestamp updatetime;
	private String rights;
	private Integer expvalue;
	private String newtask;//新手任务
	private String otherinfo;	//其他信息，如： usecard=xxxxx;changehis=2222
	private Integer pointvalue;//积分总值
	private Integer version;	//版本
	private Long inviteid; //邀请人ID
	private String ip; //注册IP

	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getHeadpic() {
		return headpic;
	}
	public void setHeadpic(String headpic) {
		this.headpic = headpic;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Timestamp getUpdatetime() {
		return updatetime;
	}
	public void setUpdatetime(Timestamp updatetime) {
		this.updatetime = updatetime;
	}
	public String getHeadpicUrl(){
		if(StringUtils.isNotBlank(headpic)) return headpic;
		return "img/default_head.png";
	}
	public String getLogo(){
		return getHeadpicUrl();
	}
	public Long getInviteid() {
		return inviteid;
	}
	
	public void setInviteid(Long inviteid) {
		this.inviteid = inviteid;
	}
	
	public String getPostcode() {
		return postcode;
	}

	public void setPostcode(String postcode) {
		this.postcode = postcode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getInvitetype() {
		return invitetype;
	}

	public void setInvitetype(String invitetype) {
		this.invitetype = invitetype;
	}


	public String getRegfrom() {
		return regfrom;
	}

	public void setRegfrom(String regfrom) {
		this.regfrom = regfrom;
	}
	public String getNewtask() {
		return newtask;
	}

	public void setNewtask(String newtask) {
		this.newtask = newtask;
	}

	public MemberInfo() {
	}
	
	public MemberInfo(Long id, String nickname) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.nickname = nickname;
		this.rights = StringUtils.join(new String[]{
				MemberConstant.RIGHTS_INDEX_PUBLIC, MemberConstant.RIGHTS_ALBUM_PUBLIC, 
				MemberConstant.RIGHTS_FRIEND_PUBLIC, MemberConstant.RIGHTS_COMMU_PUBLIC, 
				MemberConstant.RIGHTS_TOPIC_PUBLIC,MemberConstant.RIGHTS_ACTIVITY_PUBLIC,
				MemberConstant.RIGHTS_QA_PUBLIC,MemberConstant.RIGHTS_AGENDA_FRIEND}, ",");
		this.pointvalue = 0;
		this.version = 0;
		this.id = id;
		this.expvalue = 0;
		this.regfrom = "web";
		this.sex = "女";
		this.updatetime = addtime;
	}

	public Integer getExpvalue() {
		return expvalue;
	}

	public void setExpvalue(Integer expvalue) {
		this.expvalue = expvalue;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}
	@Override
	public Serializable realId() {
		return id;
	}

	public String getBlood() {
		return blood;
	}

	public void setBlood(String blood) {
		this.blood = blood;
	}

	public String getMsn() {
		return msn;
	}

	public void setMsn(String msn) {
		this.msn = msn;
	}

	public String getQq() {
		return qq;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public Long getUniversityid() {
		return universityid;
	}

	public void setUniversityid(Long universityid) {
		this.universityid = universityid;
	}

	public String getUniversityname() {
		return universityname;
	}

	public void setUniversityname(String universityname) {
		this.universityname = universityname;
	}
	
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public String getLiveprovince() {
		return liveprovince;
	}

	public void setLiveprovince(String liveprovince) {
		this.liveprovince = liveprovince;
	}

	public String getLivecity() {
		return livecity;
	}

	public void setLivecity(String livecity) {
		this.livecity = livecity;
	}

	public String getLivecounty() {
		return livecounty;
	}

	public void setLivecounty(String livecounty) {
		this.livecounty = livecounty;
	}

	public String getLiveindexarea() {
		return liveindexarea;
	}

	public void setLiveindexarea(String liveindexarea) {
		this.liveindexarea = liveindexarea;
	}

	public String getFromcity() {
		return fromcity;
	}

	public void setFromcity(String fromcity) {
		this.fromcity = fromcity;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getIntroduce() {
		return introduce;
	}

	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}
	
	public String getRsign(String basePath){
		if(StringUtils.isNotBlank(this.sign)){
			return StringUtil.enabbr(this.sign, 160).replaceAll("\\[face:([^\\]]*)\\]", "<img src=" + basePath + "img/face/$1\\.gif align='top'/>");
		}
		return "";
	}
	public String getRIntro(String basePath){
		if(StringUtils.isNotBlank(this.introduce)){
			return StringUtil.enabbr(this.introduce, 160).replaceAll("\\[face:([^\\]]*)\\]", "<img src=" + basePath + "img/face/$1\\.gif />");
		}
		return "";
	}

	public List<String> getFinishedTaskList() {
		if(StringUtils.isBlank(newtask)) return new ArrayList<String>();
		return Arrays.asList(newtask.split(","));
	}
	public List<String> getUnFinishedTaskList() {
		List<String> result = new ArrayList<String>(MemberConstant.TASK_LIST);
		result.removeAll(this.getFinishedTaskList());
		return result;
	}

	public String getOtherinfo() {
		return otherinfo;
	}

	public void setOtherinfo(String otherinfo) {
		this.otherinfo = otherinfo;
	}
	public Integer getPointvalue() {
		return pointvalue;
	}
	public void setPointvalue(Integer pointvalue) {
		this.pointvalue = pointvalue;
	}
	public void addPointValue(Integer pointValue) {
		this.pointvalue += pointValue;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public String getIp(){
		return ip;
	}
	public void setIp(String ip){
		this.ip = ip;
	}

	public boolean isAllNewTaskFinished(){
		List<String> taskList = new ArrayList<String>();
		if(StringUtils.isNotBlank(getNewtask())) 
			taskList = new ArrayList(Arrays.asList(StringUtils.split(getNewtask(), ",")));
		return taskList.containsAll(MemberConstant.TASK_LIST);
	}
	public boolean isReceived(){
		return isAllNewTaskFinished();
	}
	public int getBuyticket(){ //返回1标识购票用户
		List<String> taskList = new ArrayList<String>();
		if(StringUtils.isNotBlank(getNewtask()))  taskList = new ArrayList(Arrays.asList(StringUtils.split(getNewtask(), ",")));
		if(taskList.contains(MemberConstant.TASK_BUYED_TICKET)) return 1;
		return 0;
	}
	public void finishTask(String task){
		if(isFinishedTask(task)) return;
		List<String> taskList = new ArrayList<String>();
		if(StringUtils.isNotBlank(getNewtask()))
			taskList = new ArrayList<String>(Arrays.asList(StringUtils.split(getNewtask(), ",")));
		taskList.add(task);
		Collections.sort(taskList);
		setNewtask(StringUtils.join(taskList, ","));
	}
	public void finishTaskOtherInfo(String task){
		if(MemberConstant.TASK_LIST.contains(task) && getNewtask().contains(task)){
			Map<String, String> otherinfoMap = JsonUtils.readJsonToMap(getOtherinfo());
			if(otherinfoMap.get(MemberConstant.NEWTASK) == null){
				otherinfoMap.put(MemberConstant.NEWTASK, task);
			}else {
				String otherNewTask = otherinfoMap.get(MemberConstant.NEWTASK) + "";
				List<String> taskList = new ArrayList<String>(Arrays.asList(StringUtils.split(otherNewTask, ",")));
				if(!taskList.contains(task)){
					taskList.add(task);
					otherinfoMap.put(MemberConstant.NEWTASK, StringUtils.join(taskList, ","));
				}
			}
			setOtherinfo(JsonUtils.writeMapToJson(otherinfoMap));
		}
	}
	
	public List<String> getOtherNewtaskList(){
		List<String> otherNewtaskList = new ArrayList<String>();
		String temp = JsonUtils.getJsonValueByKey(getOtherinfo(), MemberConstant.NEWTASK);
		if(temp != null) otherNewtaskList = new ArrayList<String>(Arrays.asList(StringUtils.split(temp, ",")));
		return otherNewtaskList;
	}
	
	public boolean isFinishedTask(String task) {
		if(StringUtils.isNotBlank(this.getNewtask())){
			return ! MemberConstant.TASK_LIST.contains(task) /**不存在的任务*/
				||getNewtask().contains(task);
		}else {
			return false;
		}
	}
	public Integer getFinishedCount(){
		if(isReceived()) return MemberConstant.TASK_LIST.size();
		if(StringUtils.isNotBlank(this.newtask)){
			List<String> taskList = Arrays.asList(this.newtask.split(","));
			return CollectionUtils.intersection(taskList, MemberConstant.TASK_LIST).size();
		}
		return 0;
	}
	public List<String> getNewTaskList(){
		if(StringUtils.isBlank(this.newtask)) return new ArrayList<String>();
		return Arrays.asList(StringUtils.split(this.newtask, ","));
	}

	public String getFavortag() {
		return favortag;
	}
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public void setFavortag(String favortag) {
		this.favortag = favortag;
	}
	
	public boolean isRegisterSource(String rsource){
		if(StringUtils.isBlank(rsource)) return false;
		return StringUtils.equals(this.source, rsource);
	}
	
	public boolean isBindSuccess(){
		String value = JsonUtils.getJsonValueByKey(this.otherinfo, MemberConstant.TAG_SOURCE);
		if(StringUtils.isBlank(value)) return true;
		return StringUtils.equals(value, "success");
	}
}
