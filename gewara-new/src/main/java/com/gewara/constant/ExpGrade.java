package com.gewara.constant;

/**
 * 经验值加分数量
 * @author acerge(acerge@163.com)
 * @since 5:19:39 PM Jul 28, 2009
 */
public interface ExpGrade {
	static final int EXP_DIARY_ADD = 10; //写影评、健身心得、普通帖子、投票、活动
	static final int EXP_DIARY_SUB = 15;
	static final int EXP_DIARY_REPLYEE = 1; //被回复
	static final int EXP_DIARY_REPLYER_ADD = 5; //回复
	static final int EXP_DIARY_REPLYER_SUB = 10; //回复
	static final int EXP_DIARY_HOT = 20; //帖子、投票精品
	static final int EXP_TALK_HOT = 20; //影评、健身心得精品
	static final int EXP_FLOWER = 1; //被献花者
	static final int EXP_COMMENT_ADD = 20; //场所点评
	static final int EXP_COMMENT_SUB = 30; //场所点评删除
	static final int EXP_FIRST_COMMENT_ADD = 40; //前五位点评
	static final int EXP_FIRST_COMMENT_SUB = 60; //前五位删除
	static final int EXP_QUESTION_ADD = 5; //提出问题
	static final int EXP_ANSWER_ADD_COMMON = 2; //普通网友回答问题
	static final int EXP_ANSWER_ADD_EXPERT = 2; //专家回答问题
	static final int EXP_DIARY_RECOMMEND = 1;//发表的主帖被推荐
	static final int EXP_COMMU_ADD = 20;//创建圈子
	static final int EXP_COMMU_DEL = 50;//删除圈子
	static final int EXP_ANSWER_DEL_COMMON = 4;//回答知道被删除
	static final int EXP_COMMENT_ADD_COMMON = 1;//发表哇啦
}
