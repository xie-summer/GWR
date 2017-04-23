package com.gewara.model.movie;

import java.io.Serializable;
import java.sql.Timestamp;

import com.gewara.model.BaseObject;

public class MoviePrice extends BaseObject{
	private static final long serialVersionUID = -7517864551639660916L;
	private Long id;
	private Long movieid;		
	private Integer price;		//
	private String citycode;	
	private String type;		//
	private Integer edition3D;	//
	
	private Integer editionJumu;
	private Integer editionIMAX;
	
	private Timestamp startTime; //开始时间
	private Timestamp endTime; //结束时间
	private Integer rangeEdition3D; //时间段内价格
	private Integer rangePrice; //时间段内价格
	private Integer rangeEditionJumu;//时间段内价格
	private Integer rangeEditionIMAX;//时间段内价格
	@Override
	public Serializable realId() {
		return id;
	}
	
	public MoviePrice(){
		
	}
	public MoviePrice(Long movieid, Integer price, String citycode){
		this.movieid = movieid;
		this.price = price;
		this.citycode = citycode;
	}
	public Long getId() {
		return id;
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
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
	public String getCitycode() {
		return citycode;
	}
	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}
	
	public String getType(){
		return this.type;
	}
	
	public void setType(String type){
		this.type = type;
	}
	
	public Integer getEdition3D() {
		return edition3D;
	}
	public void setEdition3D(Integer edition3d) {
		edition3D = edition3d;
	}

	public Integer getEditionJumu() {
		return editionJumu;
	}

	public void setEditionJumu(Integer editionJumu) {
		this.editionJumu = editionJumu;
	}

	public Integer getEditionIMAX() {
		return editionIMAX;
	}

	public void setEditionIMAX(Integer editionIMAX) {
		this.editionIMAX = editionIMAX;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public Integer getRangeEdition3D() {
		return rangeEdition3D;
	}

	public void setRangeEdition3D(Integer rangeEdition3D) {
		this.rangeEdition3D = rangeEdition3D;
	}

	public Integer getRangePrice() {
		return rangePrice;
	}

	public void setRangePrice(Integer rangePrice) {
		this.rangePrice = rangePrice;
	}

	public Integer getRangeEditionJumu() {
		return rangeEditionJumu;
	}

	public void setRangeEditionJumu(Integer rangeEditionJumu) {
		this.rangeEditionJumu = rangeEditionJumu;
	}

	public Integer getRangeEditionIMAX() {
		return rangeEditionIMAX;
	}

	public void setRangeEditionIMAX(Integer rangeEditionIMAX) {
		this.rangeEditionIMAX = rangeEditionIMAX;
	}
}
