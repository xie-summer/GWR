package com.gewara.model.common;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * 用来记录用户最后一次某种操作的流水表
 * @author gebiao(ge.biao@gewara.com)
 * @since Mar 5, 2013 2:53:41 PM
 */
public class LastOperation extends BaseObject{
	private static final long serialVersionUID = 4241005391113922079L;
	private String lastkey;			//如：ticket + memberid表示最后一次下TicketOrder单
	private String tag;				//分类 TICKET
	private String lastvalue;		//值
	private Timestamp lasttime;		//最后时间
	private Timestamp validtime;	//有效时间
	public LastOperation(){
	}
	public LastOperation(String lastkey, String lastvalue, Timestamp lasttime, Timestamp validtime, String tag) {
		this.lastkey = lastkey;
		this.lastvalue = lastvalue;
		this.lasttime = lasttime;
		this.validtime = validtime;
		this.tag = tag;
	}
	public String getTag() {
		return tag;
	}
	public void setTag(String tag) {
		this.tag = tag;
	}
	public Timestamp getLasttime() {
		return lasttime;
	}
	public void setLasttime(Timestamp lasttime) {
		this.lasttime = lasttime;
	}
	public Timestamp getValidtime() {
		return validtime;
	}
	public void setValidtime(Timestamp validtime) {
		this.validtime = validtime;
	}
	@Override
	public Serializable realId() {
		return lastkey;
	}
	public String getLastvalue() {
		return lastvalue;
	}
	public void setLastvalue(String lastvalue) {
		this.lastvalue = lastvalue;
	}
	public String getLastkey() {
		return lastkey;
	}
	public void setLastkey(String lastkey) {
		this.lastkey = lastkey;
	}
	
}
