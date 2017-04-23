package com.gewara.model.user;

import java.sql.Timestamp;

/**
 *    @function Point表 最新数据
 * 	@author bob.hu
 *		@date	2011-11-03 17:50:11
 */
public class Point extends PointBase {
	private static final long serialVersionUID = -8040847441927190340L;
	public Point(){}
	public Point(Long memberid){
		this.memberid = memberid;
		this.addtime = new Timestamp(System.currentTimeMillis());
	}
	public Point(Long memberid, String tag){
		this.addtime = new Timestamp(System.currentTimeMillis());
		this.memberid = memberid;
		this.tag = tag;
	}
}
