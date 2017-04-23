package com.gewara.command;

import java.io.Serializable;

/**
 * 手机邀请好友短信发送统计Model
 * @ClassName: InviteReport
 * @author <a href="mailto:yaoper@163.com">Yaoper</a>
 * @date Sep 3, 2012 3:54:21 PM
 * @version V1.0
 */
public class InviteReport implements Serializable{
	private static final long serialVersionUID = -5167685866117951141L;
	private String _id;
	private String day;//日期
	private Integer sendNum;//发送总数
	private Integer failedNum;//发送失败数量
	private String channel;//发送通道（第三方）
	private Integer delay3MinNum;//发送时间延时超过3分钟数量
	private Integer less1MinNum;//发送时间1分钟内的数量
	public String get_id() {
		return _id;
	}
	public void set_id(String _id) {
		this._id = _id;
	}
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public Integer getSendNum() {
		return sendNum;
	}
	public void setSendNum(Integer sendNum) {
		this.sendNum = sendNum;
	}
	public Integer getFailedNum() {
		return failedNum;
	}
	public void setFailedNum(Integer failedNum) {
		this.failedNum = failedNum;
	}
	public String getChannel() {
		return channel;
	}
	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Integer getDelay3MinNum() {
		return delay3MinNum;
	}
	public void setDelay3MinNum(Integer delay3MinNum) {
		this.delay3MinNum = delay3MinNum;
	}
	public Integer getLess1MinNum() {
		return less1MinNum;
	}
	public void setLess1MinNum(Integer less1MinNum) {
		this.less1MinNum = less1MinNum;
	}
}