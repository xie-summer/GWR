package com.gewara.xmlbind.partner;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.terracotta.agent.repkg.de.schlichtherle.io.FileInputStream;

public class WeixinMsg{
	//公用信息
	private String toUserName;
	private String fromUserName;
	private String createTime;
	private String msgType;
	//文本消息
	private String content;
	//地理位置消息
	private String location_X;
	private String location_Y;
	private String scale;
	private String label;
	//图片消息
	private String picUrl;
	
	private String event;
	private String eventKey;
	
	public String getToUserName() {
		return toUserName;
	}
	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	public String getFromUserName() {
		return fromUserName;
	}
	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	public String getCreateTime() {
		return createTime;
	}
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getLocation_X() {
		return location_X;
	}
	public void setLocation_X(String location_X) {
		this.location_X = location_X;
	}
	public String getLocation_Y() {
		return location_Y;
	}
	public void setLocation_Y(String location_Y) {
		this.location_Y = location_Y;
	}
	public String getScale() {
		return scale;
	}
	public void setScale(String scale) {
		this.scale = scale;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public boolean isText(){
		return StringUtils.equalsIgnoreCase(msgType, "text");
	}
	public boolean isLocation(){
		return StringUtils.equalsIgnoreCase(msgType, "location");
	}
	public boolean isImage(){
		return StringUtils.equalsIgnoreCase(msgType, "image");
	}
	public static String testWX() {
		List<String> lineList = null;
		String str = "";
		try {
			lineList = IOUtils.readLines(new FileInputStream(new File("D:\\xmltest\\weixinmsg.txt")));
			for(String line : lineList){
				str = line;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getEventKey() {
		return eventKey;
	}
	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}
}
