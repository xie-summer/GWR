package com.gewara.model.movie;
import java.io.Serializable;

import com.gewara.model.BaseObject;

/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class MultiPlayItem extends BaseObject implements Comparable<MultiPlayItem>{
	private static final long serialVersionUID = -4016785855588367848L;
	private Long id;
	private Long movieid;
	private String language;
	private Long multiPlayid;
	private String edition;
	private Integer playorder;
	@Override
	public Serializable realId() {
		return id;
	}
	public MultiPlayItem() {
	}

	public MultiPlayItem(Long id) {
		this.id = id;
	}
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public Long getMovieid() {
		return movieid;
	}

	public void setMovieid(Long movieid) {
		this.movieid = movieid;
	}

	public Long getMultiPlayid() {
		return multiPlayid;
	}

	public void setMultiPlayid(Long multiPlayid) {
		this.multiPlayid = multiPlayid;
	}

	public Integer getPlayorder() {
		return playorder;
	}

	public void setPlayorder(Integer playorder) {
		this.playorder = playorder;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getEdition() {
		return edition;
	}

	public void setEdition(String edition) {
		this.edition = edition;
	}

	@Override
	public int compareTo(MultiPlayItem another) {
		if(this.playorder == null && another.playorder == null) return this.id.compareTo(another.id);
		if(this.playorder == null) return -1;
		if(another.playorder == null) return 1;
		if(this.playorder.equals(another.playorder)) return this.id.compareTo(another.id);
		return this.playorder.compareTo(another.playorder);
	}
}
