package com.gewara.constant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;

public abstract class DiaryConstant implements Serializable {

	private static final long serialVersionUID = 7800942669108910128L;

	// 下面是日志的类型：一般帖、投票帖子、影评
	public static final String DIARY_TYPE_ALL = ""; // 所有
	public static final String DIARY_TYPE_COMMENT = "comment"; // 影评、心得，剧评等
	public static final String DIARY_TYPE_TOPIC_DIARY = "topic_diary"; // 一般帖子
	public static final String DIARY_TYPE_TOPIC_VOTE_RADIO = "topic_vote_radio"; // 投票（单选）
	public static final String DIARY_TYPE_TOPIC_VOTE_MULTI = "topic_vote_multi"; // 投票（多选）
	public static final String DIARY_TYPE_TOPIC = "topic"; // 所有帖子
	public static final String DIARY_TYPE_TOPIC_VOTE = "topic_vote"; // 投票
	public static final String DIVISION_Y = "Y";	// 影评
	public static final String DIVISION_N = "N";	// 关联多个
	public static final String DIVISION_A = "A";	// 全部
	public static final Map<String, String> DIARY_TYPE_MAP;
	static {
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put("0", DIARY_TYPE_ALL);
		tmp.put("1", DIARY_TYPE_COMMENT);
		tmp.put("2", DIARY_TYPE_TOPIC_DIARY);
		tmp.put("3", DIARY_TYPE_TOPIC_VOTE_RADIO);
		tmp.put("4", DIARY_TYPE_TOPIC_VOTE_MULTI);
		tmp.put("5", DIARY_TYPE_TOPIC);
		tmp.put("6", DIARY_TYPE_TOPIC_VOTE);
		DIARY_TYPE_MAP = UnmodifiableMap.decorate(tmp);
	}
}
