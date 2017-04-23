package com.gewara.model.bbs;



/**
 * @function Point 表历史记录
 * @author john.zhou
 * @date	2011-11-10 10:17:36
 */
public class DiaryHist extends DiaryBase {
	private static final long serialVersionUID = 7942937072649743674L;
	public DiaryHist(){}
	@Override
	public boolean canModify() {
		return false;
	}

}
