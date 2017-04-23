package com.gewara.model.pay;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.constant.order.ElecCardConstant;
import com.gewara.model.BaseObject;

/**
 * 电子券额外信息
 * @since Dec 30, 2011, 5:09:25 PM
 * @author acerge(gebiao)
 * @function 
 */
public class ElecCardExtra extends BaseObject {
	private static final long serialVersionUID = 6287072047189731464L;
	private Long batchid;			//批次号
	private Long pid;				//父批次ID
	private Timestamp addtime;		//生成时间
	private Long issuerid;			//发行者ID	
	private String category1;		//类别1:大类
	private String category2;		//类别2:小类
	private String applycity;		//申请区域
	private String applydept;		//申请部门
	private String applytype;		//申请类型
	private Long adduserid;			//生成者
	private Timestamp soldtime;		//销售时间
	private Long sellerid; 			//销售者
	private String sellremark;		//卖出说明
	private Integer sellprice;		//卖价
	private String channel;			//销售渠道
	private Integer cardcount;		//卡总数
	private Integer usedcount;		//使用数量
	private Integer delcount;		//废弃数量
	private Integer newcount;		//新卡数量
	private Integer soldcount;		//售出数量
	private Integer lockcount;		//冻结数量
	private Integer issuecount;		//用户领用的数量
	private String mincardno;		//最小卡号
	private String maxcardno;		//最大卡号
	private Timestamp statstime;	//统计时间
	private Long merchantid;		//发行商家：只为影院、运动专管专用，Gewara：0
	private String status;			//批次状态：历史、现在
	public ElecCardExtra(){}
	public ElecCardExtra(Long batchid){
		this.batchid = batchid;
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.status = ElecCardConstant.DATA_NOW;
		this.cardcount = 0;
		this.usedcount = 0;
		this.delcount = 0;
		this.newcount = 0;
		this.lockcount = 0;
		this.merchantid = 0l;
	}
	@Override
	public Serializable realId() {
		return batchid;
	}
	public void copyFrom(ElecCardExtra parentExtra) {
		category1 = parentExtra.category1;		//类别1:大类
		category2 = parentExtra.category2;		//类别2:小类
		applycity = parentExtra.applycity;		//申请区域
		applydept = parentExtra.applydept;		//申请部门
		applytype = parentExtra.applytype;		//申请类型
		sellprice = parentExtra.sellprice;		//卖价
		merchantid = parentExtra.merchantid;
	}

	public Long getBatchid() {
		return batchid;
	}

	public void setBatchid(Long batchid) {
		this.batchid = batchid;
	}

	public Long getPid() {
		return pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public boolean hasParent(){
		return pid != null;
	}

	public Long getIssuerid() {
		return issuerid;
	}

	public void setIssuerid(Long issuerid) {
		this.issuerid = issuerid;
	}

	public String getCategory1() {
		return category1;
	}

	public void setCategory1(String category1) {
		this.category1 = category1;
	}

	public String getCategory2() {
		return category2;
	}

	public void setCategory2(String category2) {
		this.category2 = category2;
	}

	public String getApplycity() {
		return applycity;
	}

	public void setApplycity(String applycity) {
		this.applycity = applycity;
	}

	public String getApplydept() {
		return applydept;
	}

	public void setApplydept(String applydept) {
		this.applydept = applydept;
	}

	public String getApplytype() {
		return applytype;
	}

	public void setApplytype(String applytype) {
		this.applytype = applytype;
	}

	public Long getAdduserid() {
		return adduserid;
	}

	public void setAdduserid(Long adduserid) {
		this.adduserid = adduserid;
	}

	public Timestamp getSoldtime() {
		return soldtime;
	}

	public void setSoldtime(Timestamp soldtime) {
		this.soldtime = soldtime;
	}

	public Long getSellerid() {
		return sellerid;
	}

	public void setSellerid(Long sellerid) {
		this.sellerid = sellerid;
	}

	public String getSellremark() {
		return sellremark;
	}

	public void setSellremark(String sellremark) {
		this.sellremark = sellremark;
	}

	public Integer getSellprice() {
		return sellprice;
	}

	public void setSellprice(Integer sellprice) {
		this.sellprice = sellprice;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}
	public Timestamp getAddtime() {
		return addtime;
	}
	public void setAddtime(Timestamp addtime) {
		this.addtime = addtime;
	}
	public Integer getUsedcount() {
		return usedcount;
	}
	public void setUsedcount(Integer usedcount) {
		this.usedcount = usedcount;
	}
	public Timestamp getStatstime() {
		return statstime;
	}
	public void setStatstime(Timestamp statstime) {
		this.statstime = statstime;
	}
	public Integer getDelcount() {
		return delcount;
	}
	public void setDelcount(Integer delcount) {
		this.delcount = delcount;
	}
	public Integer getNewcount() {
		return newcount;
	}
	public void setNewcount(Integer newcount) {
		this.newcount = newcount;
	}
	public Integer getSoldcount() {
		return soldcount;
	}
	public void setSoldcount(Integer soldcount) {
		this.soldcount = soldcount;
	}
	public String getMincardno() {
		return mincardno;
	}
	public void setMincardno(String mincardno) {
		this.mincardno = mincardno;
	}
	public String getMaxcardno() {
		return maxcardno;
	}
	public void setMaxcardno(String maxcardno) {
		this.maxcardno = maxcardno;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public Integer getCardcount() {
		return cardcount;
	}
	public void setCardcount(Integer cardcount) {
		this.cardcount = cardcount;
	}
	public Long getMerchantid() {
		return merchantid;
	}
	public void setMerchantid(Long merchantid) {
		this.merchantid = merchantid;
	}
	public Integer getLockcount() {
		return lockcount;
	}
	public void setLockcount(Integer lockcount) {
		this.lockcount = lockcount;
	}
	public Integer getIssuecount() {
		return issuecount;
	}
	public void setIssuecount(Integer issuecount) {
		this.issuecount = issuecount;
	}
}
