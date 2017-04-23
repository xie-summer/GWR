package com.gewara.web.action.admin.mobile;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.order.AddressConstant;
import com.gewara.pay.PayUtil;
import com.gewara.service.MessageService;
import com.gewara.support.ServiceHelper;
import com.gewara.untrans.CommentService;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
import com.gewara.web.util.PageUtil;
import com.gewara.xmlbind.bbs.Comment;

@Controller
public class MobilesAdminController extends BaseAdminController {
	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	public void setCommentService(CommentService commentService) {
		this.commentService = commentService;
	}

	@Autowired
	@Qualifier("messageService")
	private MessageService messageService;

	public void setMessageService(MessageService messageService) {
		this.messageService = messageService;
	}

	@RequestMapping("/admin/blog/wap/wapReplyStats.xhtml")
	public String wapReplyStats(ModelMap model, String sdatefrom, String sdateto, String tag, String topic, String handle, String downloadType,
			HttpServletResponse response) {
		Timestamp starttime = DateUtil.parseTimestamp(sdatefrom);
		Timestamp endtime = DateUtil.parseTimestamp(sdateto);
		model.put("tag", tag);
		if (StringUtils.isBlank(handle))
			handle = "N";
		if (endtime == null) {
			endtime = DateUtil.getCurFullTimestamp();
			sdateto = DateUtil.format(endtime, "yyyy-MM-dd HH:mm:ss");
		}
		if (starttime == null) {
			starttime = DateUtil.getMonthFirstDay(endtime);
			sdatefrom = DateUtil.format(starttime, "yyyy-MM-dd HH:mm:ss");
		}
		// 发送 MAS 数量
		String mascounthql = "select count(t.id) from SMSRecord t where t.tradeNo like ? and t.status = ? and t.smstype = ? and t.sendtime >= ? and t.sendtime <= ? ";
		List<Object> params = new ArrayList<Object>();
		if (StringUtils.isBlank(tag) || StringUtils.equals(tag, TagConstant.TAG_MOVIE)) {
			tag = TagConstant.TAG_MOVIE;
			params.add(PayUtil.FLAG_TICKET + "%");
		} else if (StringUtils.equals(tag, TagConstant.TAG_DRAMA)) {
			params.add(PayUtil.FLAG_DRAMA + "%");
		} else if (StringUtils.equals(tag, TagConstant.TAG_SPORT)) {
			params.add(PayUtil.FLAG_SPORT + "%");
		}
		params.add(Status.Y);
		params.add(SmsConstant.SMSTYPE_10M);
		params.add(starttime);
		params.add(endtime);
		List mascountList = hibernateTemplate.find(mascounthql, params.toArray());
		Integer mascount = ((Long) mascountList.get(0)).intValue();
		model.put("mascount", mascount);
		model.put("sdateto", sdateto);
		model.put("sdatefrom", sdatefrom);
		model.put("topic", topic);
		model.put("handle", handle);
		Integer replycount = 0;
		replycount = commentService.getCommentCountByTagAndAddress(tag, AddressConstant.ADDRESS_MOBILE, starttime, endtime, topic, handle);
		// 回复数量
		model.put("replycount", replycount);

		// 回复率
		model.put("replypercent", VmUtils.formatPercent(replycount, mascount));

		String type = "";
		if (StringUtils.equals(tag, TagConstant.TAG_SPORT))
			type = TagConstant.TAG_SPORTORDER;
		else
			type = TagConstant.TAG_MOVIEORDER;
		int count = messageService.querySmsRecord(null, type, starttime, endtime, null, null);
		int countAll = messageService.querySmsRecord(null, type, null, null, null, null);
		model.put("count", count);
		model.put("countAll", countAll);
		if (StringUtils.equals(downloadType, "xls"))
			download(downloadType, response);
		return "admin/blog/wap/wapreply.vm";
	}

	@RequestMapping("/admin/blog/wap/dramaWapReplayStats.xhtml")
	public String dramaWapReplyStats(ModelMap model, String sdatefrom, String sdateto, String topic, String handle, String downloadType,
			HttpServletResponse response) {
		return wapReplyStats(model, sdatefrom, sdateto, TagConstant.TAG_DRAMA, topic, handle, downloadType, response);
	}

	@RequestMapping("/admin/blog/wap/sportWapReplayStats.xhtml")
	public String sportWapReplyStats(ModelMap model, String sdatefrom, String sdateto, String topic, String handle, String downloadType,
			HttpServletResponse response) {
		return wapReplyStats(model, sdatefrom, sdateto, TagConstant.TAG_SPORT, topic, handle, downloadType, response);
	}

	@RequestMapping("/admin/blog/wap/wapReplyStatsTable.xhtml")
	public String wapReplyStatsTable(ModelMap model, String sdatefrom, String sdateto, String tag, String topic, String handle, Integer pageNo) {
		Timestamp starttime = DateUtil.parseTimestamp(sdatefrom);
		Timestamp endtime = DateUtil.parseTimestamp(sdateto);
		if (pageNo == null)
			pageNo = 0;
		int rowsPerPage = 30;
		int firstRow = pageNo * rowsPerPage;
		if (StringUtils.isBlank(tag))
			tag = TagConstant.TAG_MOVIE;
		Integer replycount = 0;
		if (endtime.before(DateUtil.addDay(DateUtil.currentTime(), -20))) {
			List<Comment> commentList = commentService.getCommentsByTagAndAddress(tag, AddressConstant.ADDRESS_MOBILE, starttime,
					endtime, topic, handle, firstRow, rowsPerPage);
			replycount = commentService.getCommentCountByTagAndAddress(tag, AddressConstant.ADDRESS_MOBILE, starttime, endtime, topic,
					handle);
			model.put("commentList", commentList);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
		} else {
			List<Comment> commentList = commentService.getCommentsByTagAndAddress(tag, AddressConstant.ADDRESS_MOBILE, starttime,
					endtime, topic, handle, firstRow, rowsPerPage);
			// chkAddedpoint(commentList, model);
			model.put("commentList", commentList);
			addCacheMember(model, ServiceHelper.getMemberIdListFromBeanList(commentList));
			replycount = commentService.getCommentCountByTagAndAddress(tag, AddressConstant.ADDRESS_MOBILE, starttime, endtime, topic,
					handle);
		}
		PageUtil pageUtil = new PageUtil(replycount, rowsPerPage, pageNo, "/admin/blog/wap/wapReplyStatsTable.xhtml");
		Map params = new HashMap();
		params.put("sdatefrom", sdatefrom);
		params.put("sdateto", sdateto);
		params.put("tag", tag);
		params.put("handle", handle);
		params.put("topic", topic);
		pageUtil.initPageInfo(params);
		model.put("pageUtil", pageUtil);

		model.put("handle", handle);
		model.put("tag", tag);
		return "admin/blog/wap/wapreplyTable.vm";
	}

	@RequestMapping("/admin/blog/wap/wapdemand.xhtml")
	public String wapdemand(ModelMap model, String sdatefrom, String sdateto) {
		Date datefrom = DateUtil.parseDate(sdatefrom);
		Date dateto = DateUtil.parseDate(sdateto);
		if (datefrom == null || dateto == null)
			return "admin/blog/wap/wapreply.vm";
		model.put("datefrom", datefrom);
		model.put("dateto", dateto);
		return "admin/blog/wap/wapreply.vm";
	}
}
