package com.gewara.xmlbind.bbs;

import java.util.ArrayList;
import java.util.List;

public class ReCommentList {
	
	private List<ReComment> reCommentList = new ArrayList<ReComment>();

	public List<ReComment> getReCommentList() {
		return reCommentList;
	}

	public void setReCommentList(List<ReComment> reCommentList) {
		this.reCommentList = reCommentList;
	}


	public void addReComment(ReComment reComment)
	{
		this.reCommentList.add(reComment);
	}

}
