package com.gewara.service.bbs;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQaExpert;
import com.gewara.model.bbs.qa.GewaQaPoint;
import com.gewara.model.bbs.qa.GewaQuestion;
import com.gewara.model.user.Member;

public interface QaService {
	List<GewaQuestion> getQuestionListByQuestionstatus(String citycode, String questionstatus, String order, int from, int maxnum);

	Integer getQuestionCountByQuestionstatus(String citycode, String questionstatus);

	List<GewaQuestion> getQuestionListByStatus(String status, Date fromDate, Date endDate, int from, int maxnum);

	Integer getQuestionCountByStatus(String status, Date fromDate, Date endDate);

	List<GewaQuestion> getQuestionListByHotvalue(String citycode, int hotvalue, int from, int maxnum);

	List<GewaAnswer> getAnswerListByQuestionid(Long questionid);

	Integer getAnswerCount(Long questionid);

	List<GewaAnswer> getAnswerListByQuestionId(int start, int maxnum, Long questionid);
	
	List<GewaAnswer> getAnswerListByQuestionAndMemId(int start, int maxnum, Long questionid,Long memberId);

	/**
	 * 根据问题查询回复 status=Y_NEW
	 */
	Integer getAnswerCountByQuestionId(Long questionid);

	/**
	 * Ta回答的问题的数量
	 * 
	 * @param mid
	 * @return
	 */
	Integer getAnswerCountByMemberid(Long mid);

	/**
	 * Ta回答的问题被采纳的数量
	 * 
	 * @param mid
	 * @return
	 */
	Integer getBestAnswerCountByMemberid(Long mid);

	/**
	 * 用户是否可以提出问题
	 * 
	 * @param memberid
	 * @param maxdays
	 * @return
	 */
	boolean isQuestion(Long memberid, Integer maxdays);

	/**
	 * 用户是否已经回答了这个问题
	 * 
	 * @param qid
	 * @param mid
	 * @return
	 */
	boolean isAnswerQuestion(Long qid, Long mid);

	/**
	 * 问题的最佳答案
	 * 
	 * @param qid
	 * @return
	 */
	GewaAnswer getBestAnswerByQuestionid(Long qid);

	/**
	 * 用户是否是专家
	 * 
	 * @param mid
	 * @return
	 */
	GewaQaExpert getQaExpertByMemberid(Long mid);

	/**
	 * 更改问题的热度
	 * 
	 * @param id
	 * @param hotvalue
	 * @return 2009-10-29
	 */
	boolean updateQAHotValue(Long id, Integer hotvalue);

	/**
	 * 修改专家信息热度
	 * 
	 * @param id
	 * @param hotvalue
	 * @return 2009-10-30
	 */
	boolean updateQAExpertHotValue(Long id, Integer hotvalue);

	/**
	 * 查询专家信息数量
	 * 
	 * @return 2009-10-29
	 */

	Integer getQAExpertCount();

	/**
	 * 查询专家信息
	 * 
	 * @return 2009-10-29
	 */
	List<GewaQaExpert> getQaExpertList();

	/**
	 * 查找问题
	 * 
	 * @param tag
	 * @param relatedid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<GewaQuestion> getQuestionByTagAndRelatedid(String citycode, String tag, Long relatedid, int from, int maxnum);

	/**
	 * 查找问题
	 * 
	 * @param category
	 * @param categoryid
	 * @param from
	 * @param maxnum
	 * @return
	 */
	List<GewaQuestion> getQuestionByCategoryAndCategoryid(String citycode, String category, Long categoryid, int from, int maxnum);
	
	List<GewaQuestion> getQuestionByCategoryAndCategoryid(String citycode, String category, Long categoryid, boolean status, String questionstatus, int from, int maxnum);

	Integer getQuestionCountByCategoryAndCid(String citycode, String category, Long categoryid);

	/**
	 * 用户最佳答案多少排行
	 * 
	 * @param from
	 * @param maxnum
	 * @return
	 */
	Map<Member, Integer> getTopMemberListByBestAnswer(int from, int maxnum);

	Integer getTopMemberCountByBestAnswer();

	/**
	 * 查询专家信息表的状态
	 * 
	 * @return 2009-10-30
	 */
	GewaQaExpert getQaExpertStatusById(Long id);

	List<GewaAnswer> getAnswerByMemberId(Long id);

	/**
	 * 用户回答问题多少排行
	 * 
	 * @param from
	 * @param maxnum
	 * @return
	 */
	Map<Member, Integer> getTopMemberListByAnswer(int from, int maxnum);

	Integer getTopMemberCountByAnswer();

	/**
	 * 用户经验值多少排行
	 * 
	 * @return
	 */
	List<Map> getTopMemberListByPoint(int from, int maxnum);

	Integer getTopMemberCountByPoint();

	// List<Long> getQuesionidListByMemberid(Long memberid);

	List<GewaQuestion> getQuestionListByMemberid(Long memberid, int from, int maxnum);

	Map<Map<Object, String>, Integer> getQuestionListByTagGroup(String tag, int from, int maxnum);

	Map<Map<Object, String>, Integer> getQuestionListByCategoryGroup(String category, int from, int maxnum);

	List<GewaQaExpert> getCommendExpertList(Integer hotvalue, int from, int maxnum);

	GewaQaPoint getGewaQaPointByQuestionidAndTag(Long qid, String tag);

	/**
	 * 用户累计的问答经验值
	 * 
	 * @param mid
	 * @return
	 */
	Integer getPointByMemberid(Long mid);

	List<GewaQuestion> getQuestionByQsAndTagList(String citycode, String qs, String tag, String order, int maxnum);

	List<GewaQuestion> getQuestionListByQsAndTagAndRelatedid(String tag, Long relatedid, String qs, String order, int maxnum);

	/**
	 * 查询问题总数量
	 * 
	 * @param citycode
	 *            城市代码
	 * @param tag
	 *            关联类型
	 * @param relatedid
	 *            关联对象id
	 * @param status
	 *            问题状态,可选值:N(待解决),Y(已解决),Z(零解决),noproper(无满意答案)
	 * @return
	 */
	int getQuestionCount(String citycode, String tag, Long relatedid, String status);

	/**
	 * 分页查询问题
	 * 
	 * @param citycode
	 *            城市代码
	 * @param tag
	 *            关联类型
	 * @param relatedid
	 *            关联对象id
	 * @param status
	 *            问题状态,可选值:N(待解决),Y(已解决),Z(零解决),noproper(无满意答案)
	 * @param order
	 *            排序
	 * @param from
	 *            页码
	 * @param maxnum
	 *            结果条数
	 * @return
	 */
	List<GewaQuestion> getQuestionList(String citycode, String tag, Long relatedid, String status, String order, int from, int maxnum);
	/**
	 * 根据hotvalue查询知道数量
	 */
	Integer getQuestionCountByHotvalue(String citycode, Integer hotvalue);
	
	/**
	 * 获取演出管理人员回复的memberid;
	 * @return
	 */
	Long getGewaraAnswerByMemberid();
}
