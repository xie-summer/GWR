package com.gewara.xmlbind.bbs;

import java.util.ArrayList;
import java.util.List;

public class CommentList {
	
	private  List<Comment> commentList = new ArrayList<Comment>();

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}
	
	public void addComment(Comment comment)
	{
		this.commentList.add(comment);
	}

}
