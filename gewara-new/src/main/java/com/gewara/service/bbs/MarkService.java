package com.gewara.service.bbs;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import com.gewara.json.bbs.MarkCountData;
import com.gewara.model.bbs.MarkCount;
import com.gewara.model.bbs.MemberMark;
import com.gewara.model.user.Member;

public interface MarkService {
	/**
	 * 某类评分聚合数据
	 * @param tag
	 * @return
	 */
	Map getMarkdata(String tag);
	MarkCountData getMarkCountByTagRelatedid(String tag, Long relatedid);
	List<Map> getMarkRelatedidByAddtime(String tag, Timestamp starttime, Timestamp endtime);
	Timestamp updateMarkCount(String tag);
	Integer getMaxMarktimes(String tag, Timestamp starttime);
	void updateAvgMarkTimes(String tag,  Timestamp starttime, Timestamp endtime);
	/**
	 * 获取某个用户对某个场所某项的最后一次评分值
	 * @param tag
	 * @param relatedid
	 * @param markname
	 * @param memberId
	 * @return
	 */
	MemberMark getLastMemberMark(String tag, Long relatedid, String markname, Long memberId);
	MemberMark getCurMemberMark(String tag, Long relatedid, String markname, Long memberId);
	List<MemberMark> getMarkList(String tag, Long relatedid, String markname, int maxnum);
	List<MemberMark> getMarkList(String tag, Long relatedid, String markname, String flag);
	/**
	 * 新增或修改用户对某个场所某项的评分值
	 * @param newmark
	 * @param newflag
	 * @param oldmark
	 * @param oldflag
	 */
	MemberMark saveOrUpdateMemberMark(String tag, Long relatedid, String markname, Integer markvalue, Member member);
	boolean saveOrUpdateMemberMarkMap(String tag, Long relatedid, Member member, Map<String, Integer> memberMarkMap);
	/**
	 * 获取评分详细情况，如：(markvalue:5, markcount:10) 表示五星评分有10个 
	 * @param tag
	 * @param relatedid
	 * @param markname 评分项目名称
	 * @return 
	 */
	List<Map> getMarkDetail(String tag, Long relatedid, String markname);
	Integer getMarkValueCount(String tag, Long relatedid, String markname,int fromValue, int maxValue);
	Map getPercentCount(String tag, Long relatedid);
	Map getGradeCount(String tag, Long relatedid);
	List<MarkCount> getMarkCountListByTag(String tag);
	/**
	 * 评分分级统计
	 * 例如：
	 * 6--7 60% 其中购票用户凭占20% 非购票占80%
	 * 1--3 60% 其中购票用户凭占20% 非购票占10%
	 * @param tag
	 * @param relatedid
	 * @return
	 */
	List<Map> getGradeDetail(String tag, Long relatedid);
}	

