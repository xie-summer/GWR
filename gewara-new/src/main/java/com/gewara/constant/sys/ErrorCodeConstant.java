package com.gewara.constant.sys;

import com.gewara.constant.ApiConstant;
import com.gewara.support.ErrorCode;
public class ErrorCodeConstant {
	public static ErrorCode NOT_LOGIN = ErrorCode.getFailure(ApiConstant.CODE_NOTLOGIN, "您还没有登录，请先登录！");
	public static ErrorCode NORIGHTS = ErrorCode.getFailure(ApiConstant.CODE_USER_NORIGHTS, "您没有权限！");
	public static ErrorCode REPEATED = ErrorCode.getFailure(ApiConstant.CODE_REPEAT_OPERATION, "不能重复操作！");
	public static ErrorCode NOT_FOUND = ErrorCode.getFailure(ApiConstant.CODE_NOT_EXISTS, "未找到相关数据！");
	public static ErrorCode DATEERROR = ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "数据有错误！");
	public static ErrorCode BLACK_LIST = ErrorCode.getFailure(ApiConstant.CODE_SIGN_ERROR, "黑名单中！");
}
