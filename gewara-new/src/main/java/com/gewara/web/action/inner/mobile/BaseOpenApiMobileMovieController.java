package com.gewara.web.action.inner.mobile;

import org.springframework.ui.ModelMap;

public class BaseOpenApiMobileMovieController extends BaseOpenApiMobileController{
	protected void putMovieNode(ModelMap model){
		model.put("root", "movie");
	}
	protected void putMovieListNode(ModelMap model){
		model.put("root", "movieList");
		model.put("nextroot", "movie");
	}
	
	protected void putCinemaNode(ModelMap model){
		model.put("root", "cinema");
	}
	protected void putCinemaListNode(ModelMap model){
		model.put("root", "cinemaList");
		model.put("nextroot", "cinema");
	}
	protected void putPlayItemNode(ModelMap model){
		model.put("root", "playItem");
	}
	protected void putPlayItemListNode(ModelMap model){
		model.put("root", "playItemList");
		model.put("nextroot", "playItem");
	}
	
}
