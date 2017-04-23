package com.gewara.xmlbind.partner;

public class WeixinReply{
	private String toUserName;
	private String fromUserName;
	private String createTime;
	private String msgType;
	//文本消息
	private String content;
	private String funcFlag;
	
	public void copyMsg(WeixinMsg msg, String body){
		this.toUserName = msg.getFromUserName();
		this.fromUserName = msg.getToUserName();
		this.msgType = msg.getMsgType();
		if(msg.isText()){
			this.funcFlag = "0";
		}
		this.createTime = System.currentTimeMillis()+"";
		this.content = body;
	}
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
	public String getFuncFlag() {
		return funcFlag;
	}
	public void setFuncFlag(String funcFlag) {
		this.funcFlag = funcFlag;
	}
}
