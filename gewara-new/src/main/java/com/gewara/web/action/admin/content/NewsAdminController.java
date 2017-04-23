package com.gewara.web.action.admin.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class NewsAdminController extends BaseAdminController {
	@Autowired@Qualifier("commentService")
	private CommentService commentService;
	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@RequestMapping("/admin/blog/newscommentList.xhtml")
	public String newscommentList(String tag,
			@RequestParam(required=false, value="keyname")String key, 
			@RequestParam(required=false, value="pageNo")Integer pageNo, ModelMap model){
		model.put("tag", tag);
		List<Comment> commentList = new ArrayList<Comment>();
		if(StringUtils.isNotBlank(key)){
			commentList = commentService.getCommentListByKey(tag, key);
		}else{
			if(pageNo == null) pageNo = 0;
			Integer count = 0;
			int rowsPerpage = 40;
			int firstRow = pageNo * rowsPerpage;
			count = commentService.getCommentCountByTag(tag);
			commentList = commentService.getCommentListByTag(tag, firstRow, rowsPerpage);
			PageUtil pageUtil = new PageUtil(count, rowsPerpage, pageNo, "admin/blog/newscommentList.xhtml");
			Map params = new HashMap(); params.put("tag", new String[]{tag});
			pageUtil.initPageInfo(params);
			model.put("pageUtil", pageUtil);
		}
		model.put("commentList", commentList);
		addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		return "admin/news/newscommentList.vm";
	}
	
}
