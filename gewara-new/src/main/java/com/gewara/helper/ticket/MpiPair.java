package com.gewara.helper.ticket;

import com.gewara.model.movie.MoviePlayItem;

public class MpiPair {
	private MoviePlayItem gmpi;
	private MoviePlayItem pmpi;
	public MoviePlayItem getGmpi() {
		return gmpi;
	}
	public void setGmpi(MoviePlayItem gmpi) {
		this.gmpi = gmpi;
	}
	public MoviePlayItem getPmpi() {
		return pmpi;
	}
	public void setPmpi(MoviePlayItem pmpi) {
		this.pmpi = pmpi;
	}
	public int getCount(){
		if(gmpi!=null && pmpi!=null){
			return 2;
		}
		return 1;
	}
	public MoviePlayItem getFirst(){
		if(gmpi==null) return pmpi;
		return gmpi;
	}
	public MoviePlayItem getSecond(){
		if(gmpi!=null && pmpi!=null) return pmpi;
		return null;
	}
}
