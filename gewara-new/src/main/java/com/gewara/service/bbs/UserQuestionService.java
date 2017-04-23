package com.gewara.service.bbs;

import java.util.List;

import com.gewara.model.bbs.qa.GewaAnswer;
import com.gewara.model.bbs.qa.GewaQuestion;

public interface UserQuestionService {
	/**
	 * 查询当前用户发表的知道
	 */
	List<GewaQuestion> getQuestionByMemberid(Long memberid, int from, int maxnum);
	
	/**
	 * 查询当前用户发表的知道信息数量
	 */
	Integer getQuestionCountByMemberid(Long memberid);
	
	/**
	 * 查询当前用户回复的知道
	 * page 当前页码
	 */
	List<GewaQuestion> getAnswerByMemberid(Long memberid, int from, int maxnum);
	
	/**
	 * 查询当前用户回复的知道信息数量
	 */
	Integer getAnswerCountByMemberid(Long memberid);
	
	/**
	 * 根据questionid查询gewaAnswer
	 * @return
	 */
	GewaAnswer getGewaAnswerByAnswerid(Long questionid, Long memberid);
}
