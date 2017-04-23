package com.gewara.model.express;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;
import com.gewara.util.DateUtil;
//配送区域
public class ExpressProvince extends BaseObject {

	private static final long serialVersionUID = -1344306185767073656L;
	private Long id;
	private String name;			//配送区域名称
	private String provincename;	//配送省份名称
	private String provincecode;	//配送省份代码
	private Timestamp addtime;		//添加时间
	private Timestamp updatetime;	//更新时间
	private String expressid;		//配送方式id
	private Integer expressfee;		//寄件费用
	private Integer freelimit;			//免费额度

	public ExpressProvince(){}
	
	public ExpressProvince(String provincecode, String expressid, Integer expressfee,Integer freelimit){
		this.provincecode = provincecode;
		this.expressid = expressid;
		this.expressfee = expressfee;
		this.freelimit = freelimit;
		this.addtime = DateUtil.getCurFullTimestamp();
		this.updatetime = this.addtime;
	}
	
	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProvincename() {
		return provincename;
	}

	public void setProvincename(String provincename) {
		this.provincename = provincename;
	}

	public String getProvincecode() {
		return provincecode;
	}

	public void setProvincecode(String provincecode) {
		this.provincecode = provincecode;
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

	public String getExpressid() {
		return expressid;
	}

	public void setExpressid(String expressid) {
		this.expressid = expressid;
	}

	public Integer getExpressfee() {
		return expressfee;
	}

	public void setExpressfee(Integer expressfee) {
		this.expressfee = expressfee;
	}

	public Integer getFreelimit() {
		return freelimit;
	}

	public void setFreelimit(Integer freelimit) {
		this.freelimit = freelimit;
	}

	@Override
	public Serializable realId() {
		return id;
	}

}
