package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;

import com.gewara.model.BaseObject;
/**
 * 珍藏
 * @author acerge(acerge@163.com)
 * @since 11:21:53 AM Sep 4, 2009
 */
public class Treasure extends BaseObject{
	
	private static final long serialVersionUID = -673268778354641818L;
	//场所与人的收藏
	public static final String ACTION_COLLECT = "collect";
	//签到
	public static final String ACTION_SIGN = "sign";
	//场所的想去，电影的想看
	public static final String ACTION_XIANGQU = "xiangqu";
	//场所的去过，电影的看过
	public static final String ACTION_QUGUO = "quguo";
	//想跟某教练学
	public static final String ACTION_XIANGXUE="xiangxue";
	//正在练瑜伽
	public static final String ACTION_PLAYING="playing";
	//练过瑜伽
	public static final String ACTION_PLAYED="played";
	//想一起练瑜伽
	public static final String ACTION_TOGETHER="together";
	// 话剧明星版块
	public static final String ACTION_FANS = "fans";	// 成为粉丝
	//感兴趣
	public static final String ACTION_BARINTEREST = "barinterest";
	//收藏活动
	public static final String ACTION_ACTIVITY = "activity";
	//是某人的学生
	public static final String ACTION_STUDENT = "student";
	
	public static final List<String> ACTION_LIST = Arrays.asList(ACTION_BARINTEREST, ACTION_FANS, ACTION_COLLECT, ACTION_SIGN, 
			ACTION_XIANGQU, ACTION_STUDENT, ACTION_QUGUO, ACTION_XIANGXUE, ACTION_PLAYING, ACTION_PLAYED, ACTION_TOGETHER);
	
	//关注人
	public static final String TAG_MEMBER = "member";
	
	
	private Long id;
	private Long memberid; //动作发出者
	//private Member member; //手工关联
	private String tag; //模块
	private String action;//动作：xiangqu、quguo、collect、xiangxue
	private Long relatedid;//关联的对象
	private Timestamp addtime; 
	
	//20110418
	private String actionlabel; //标签
	
	public String getActionlabel() {
		return actionlabel;
	}
	public void setActionlabel(String actionlabel) {
		this.actionlabel = actionlabel;
	}
	
	public Treasure() {}
	
	public Treasure(Long memberid){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
	}
	public Treasure(Long memberid, String tag, Long relatedid, String action){
		this(memberid);
		this.tag = tag;
		this.relatedid = relatedid;
		this.action = action;
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

	public Long getRelatedid() {
		return relatedid;
	}

	public void setRelatedid(Long relatedid) {
		this.relatedid = relatedid;
	}

	public Long getMemberid() {
		return memberid;
	}
	public void setMemberid(Long memberid) {
		this.memberid = memberid;
	}
	/*public void setMember(Member member) {
		this.member = member;
	}
	public Member getMember() {
		return member;
	}*/
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
}
