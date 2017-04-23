/**
 * 
 */
package com.gewara.xmlbind.partner;

import java.util.ArrayList;
import java.util.List;

public class PartnerMovieList {
	private List<QQMovie> movieList = new ArrayList<QQMovie>();

	public List<QQMovie> getMovieList() {
		return movieList;
	}

	public void setMovieList(List<QQMovie> movieList) {
		this.movieList = movieList;
	}
	public void addMovie(QQMovie movie){
		movieList.add(movie);
	}
}
