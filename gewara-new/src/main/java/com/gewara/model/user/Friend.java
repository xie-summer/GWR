/**
 * 
 */
package com.gewara.model.user;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

/**
 * @author hxs(ncng_2006@hotmail.com)
 * @since Jan 27, 2010 9:44:48 AM
 */
public class Friend extends BaseObject {
	private static final long serialVersionUID = 5510942297732228200L;
	private Long id;
	private Long memberfrom;
	private Long memberto;
	private Timestamp addtime;
	private BaseObject relate; //≈Û”—µƒ–≈œ¢
	
	public Friend(){
	}
	
	public Friend(Long memberfrom, Long memberto){
		this.memberfrom = memberfrom;
		this.memberto = memberto;
		this.addtime = new Timestamp(System.currentTimeMillis());
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

	public Long getMemberfrom() {
		return memberfrom;
	}

	public void setMemberfrom(Long memberfrom) {
		this.memberfrom = memberfrom;
	}

	public Long getMemberto() {
		return memberto;
	}

	public void setMemberto(Long memberto) {
		this.memberto = memberto;
	}

	public BaseObject getRelate() {
		return relate;
	}

	public void setRelate(BaseObject relate) {
		this.relate = relate;
	}
}
