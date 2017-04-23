/**
 * 
 */
package com.gewara.json;

import java.util.Date;

import com.gewara.util.DateUtil;

/**
 * @author Administrator
 *
 */
public class SeeMovie extends SeeOrder{
	private static final long serialVersionUID = -9066897432158801162L;
	public SeeMovie(){}
	public SeeMovie(Long relatedid, String tag, Long memberid, String tradeNo, Date paidtime, Date playDate){
		this.relatedid = relatedid;
		this.tag = tag;
		this.memberid = memberid;
		this.tradeNo = tradeNo;
		this.paidtime = paidtime;
		this.adddate = DateUtil.formatDate(paidtime);
		this.playDate = playDate;
	}
}
