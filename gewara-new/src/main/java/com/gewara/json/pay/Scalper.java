package com.gewara.json.pay;


/**
 * 黄牛实体类
 * <p>一个黄牛有一个电话清单（多账号绑定的手机号）
 * @author user
 *
 */
public class Scalper {
	private Long id;
	private String name;				//黄牛名称
	private String description;	//描述
	private String mobiles;
	
	public String getMobiles() {
		return mobiles;
	}

	public void setMobiles(String mobiles) {
		this.mobiles = mobiles;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
