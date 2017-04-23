package com.gewara.helper.ticket;

import java.util.ArrayList;
import java.util.List;

import com.gewara.model.movie.MoviePlayItem;

public class UpdateMpiContainer {
	private List<MoviePlayItem> updateList = new ArrayList<MoviePlayItem>();
	private List<MoviePlayItem> insertList = new ArrayList<MoviePlayItem>();
	private List<Long> delList = new ArrayList<Long>();
	public List<MoviePlayItem> getUpdateList() {
		return updateList;
	}
	public List<MoviePlayItem> getInsertList() {
		return insertList;
	}
	public List<Long> getDelList() {
		return delList;
	}
	public void addUpdate(MoviePlayItem mpi){
		this.updateList.add(mpi);
	}
	public void addInsert(MoviePlayItem mpi){
		this.insertList.add(mpi);
	}
	public void addDelete(Long mpid){
		this.delList.add(mpid);
	}
}
