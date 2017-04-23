package com.gewara.constant;

public abstract class SysAction {
	public static final String APPLY_TYPE_SELFSERVICE="selfservice";//用户自己
	public static final String APPLY_TYPE_CUSTOMSERVICE="customservice";//客服后台申请
	
	public static final String STATUS_APPLY = "apply";
	public static final String STATUS_AGREE = "agree";
	public static final String STATUS_REFUSE = "refuse";
	public static final String STATUS_RESULT = "result";	//系统信息专用
	
	
	public static final String ACTION_APPLY_FRIEND_ADD = "apply_friend_add"; //加好友
	public static final String ACTION_APPLY_COMMU_INVITE = "apply_commu_invite";//圈子邀请
	public static final String ACTION_APPLY_C_ADMIN = "apply_c_admin";//得到-->圈子正管理员
	public static final String ACTION_APPLY_COMMU_JOIN = "apply_commu_join";//申请加入圈子
	public static final String ACTION_TICKET_SUCCESS = "ticket_sucess";//成功购票
	public static final String ACTION_GETPOINT = "getpoint";//获取积分
	
	public static final String ACTION_COMMU_APPLYCERTIFICATION = "commu_apply"; 	// 圈子申请认证
	public static final String ACTION_FRIEND_BIRTHDAY = "friend_birthday";			// 好友生日祝福
	
	public static final String ACTION_QUESTION_NEW_ANSWER = "question_new_answer"; 	//问题有新回答
	public static final String ACTION_ANSWER_IS_BEST = "answer_is_best";			//最佳回答

	public static final String ACTION_ACTIVITY_JOIN = "activity_join";				//参加活动
}
