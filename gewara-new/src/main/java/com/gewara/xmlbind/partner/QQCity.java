/**
 * 
 */
package com.gewara.xmlbind.partner;

import java.util.ArrayList;
import java.util.List;


public class QQCity {
	private Long id;
	private String name;
	private String ch;
	private List<QQCinema> cinemaList = new ArrayList<QQCinema>();
	public List<QQCinema> getCinemaList() {
		return cinemaList;
	}
	public void setCinemaList(List<QQCinema> cinemaList) {
		this.cinemaList = cinemaList;
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
	public String getCh() {
		return ch;
	}
	public void setCh(String ch) {
		this.ch = ch;
	}
	public void addCinema(QQCinema cinema){
		cinemaList.add(cinema);
	}
}
