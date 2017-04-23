package com.gewara.model.bbs;

import java.sql.Timestamp;

import com.gewara.constant.DiaryConstant;
import com.gewara.constant.Status;
/**
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28ÏÂÎç02:05:17
 */
public class Diary extends DiaryBase {

	private static final long serialVersionUID = 4476980910614491968L;
	
	public Diary(){}
	
	public Diary(String subject) {
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.updatetime = addtime;
		this.replytime = addtime;
		this.utime = addtime;
		this.flowernum = 0;
		this.poohnum = 0;
		this.clickedtimes = 0;
		this.replycount = 0;
		this.viewed = true;
		this.status = Status.Y_NEW;
		this.communityid = 0L;// Ä¬ÈÏÖµ
		this.division = DiaryConstant.DIVISION_N;
		this.subject = subject;
	}
	

	public Diary(String type, String tag, Long relatedid, String category, Long categoryid, String subject) {
		this(subject);
		this.type = type;
		this.tag = tag;
		this.relatedid = relatedid;
		this.category = category;
		this.categoryid = categoryid;
		this.status = Status.Y_NEW;
	}
	@Override
	public boolean canModify() {
		return true;
	}
}
