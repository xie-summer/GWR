package com.gewara.untrans.subject.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.Status;
import com.gewara.constant.sys.MongoData;
import com.gewara.model.user.Member;
import com.gewara.mongo.MongoService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.draw.DrawUntransService;
import com.gewara.untrans.impl.AbstractUntrantsService;
import com.gewara.untrans.subject.DoubleElevenService;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.ObjectId;

@Service("doubleElevenService")
public class DoubleElevenServiceImpl extends AbstractUntrantsService implements DoubleElevenService {
	private static final String TAG_CLICK_TIME = "clickTime";
	private static final String TAG_SHARE_CLICK = "shareClick";
	private static final String TAG_SHARE_QQ = "qq";
	private static final String TAG_SHARE_QZONE = "qzone";
	private static final String TAG_SHARE_SINA = "sina";
	
	private static final String TAG_SHARE_WIN_QQ = "winqq";
	private static final String TAG_SHARE_WIN_QZONE = "winqzone";
	private static final String TAG_SHARE_WIN_SINA = "winsina";
	
	
	public static final String FIELD_DAYCOUNT = "daycount";
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired
	@Qualifier("drawUntransService")
	private DrawUntransService drawUntransService;
	
	@Override
	public Integer getTodayWinnerCount(Long memberid, String tag) {
		String key = tag+memberid+DateUtil.format(new Date(),"yyMMdd");
		Map<String, String> row = mongoService.getMap("_id", MongoData.NS_DRAWDAYCOUNT, key);
		if(row == null){
			return 0;
		}
		return Integer.parseInt(row.get(FIELD_DAYCOUNT));
/*		
		Integer todayCount = drawActivityService.getWinnerCount(drawActivity.getId(), null, DateUtil.getBeginTimestamp(DateUtil.getCurDate()), 
				DateUtil.getCurFullTimestamp(), null, memberid, null, null);
		return todayCount;
*/	}
	
	@Override
	public ErrorCode<String> drawClick(Long memberid, String tag, String ip, Integer dayCount) {
		if (memberid == null || StringUtils.isBlank(tag)) {
			return ErrorCode.getFailure("参数错误！");
		}
		Member member = daoService.getObject(Member.class, memberid);
		ErrorCode<String> result = drawClick(member, tag, "all", ip, dayCount);
		if(result.isSuccess()){
			String key = tag+memberid+DateUtil.format(new Date(),"yyMMdd");
			Map<String, String> row = mongoService.getMap("_id", MongoData.NS_DRAWDAYCOUNT, key);
			if(row==null){
				row = new HashMap<String, String>();
				row.put("_id", key);
				row.put(FIELD_DAYCOUNT, "1");
			}else{
				row.put(FIELD_DAYCOUNT, ""+(Integer.parseInt(row.get(FIELD_DAYCOUNT))+1));
			}
			mongoService.saveOrUpdateMap(row, "_id", MongoData.NS_DRAWDAYCOUNT);
		}
		return result;
	}

	@Override
	public ErrorCode<String> getClickTime(Long memberid, String tag) {
		if (memberid == null || StringUtils.isBlank(tag)) {
			return ErrorCode.getFailure("参数错误！");
		}
		Map memberMap = getMemberMap(memberid, tag);
		String clickTime = "";
		if (memberMap != null) {
			String dateKey = DateUtil.currentTimeStr();
			Map otherMap = JsonUtils.readJsonToMap(memberMap.get(MongoData.ACTION_CONTENT) + "");
			Map infoMap = JsonUtils.readJsonToMap(otherMap.get(dateKey) + "");
			if(infoMap.get(TAG_CLICK_TIME) != null){
				clickTime = infoMap.get(TAG_CLICK_TIME) + "";
			}
		}
		return ErrorCode.getSuccessReturn(clickTime);
	}

	@Override
	public ErrorCode<String> getClickCount(Long memberid, String tag) {
		if (memberid == null || StringUtils.isBlank(tag)) {
			return ErrorCode.getFailure("参数错误！");
		}
		int shareCount = 0;
		int shareClick = 0;
		int clickCount = 0;
		Map memberMap = getMemberMap(memberid, tag);
		if (memberMap == null) {
			clickCount = 1;
		} else {
			String dateKey = DateUtil.currentTimeStr();
			Map otherMap = JsonUtils.readJsonToMap(memberMap.get(MongoData.ACTION_CONTENT) + "");
			Map infoMap = JsonUtils.readJsonToMap(otherMap.get(dateKey) + "");
			if (infoMap.get(TAG_SHARE_QQ) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_QZONE) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_SINA) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_QQ) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_QZONE) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_SINA) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_CLICK) != null)
				shareClick = Integer.parseInt(infoMap.get(TAG_SHARE_CLICK) + "");
			if (infoMap.get(TAG_CLICK_TIME) == null) {
				clickCount = 1;
			} else {
				String clickTime = infoMap.get(TAG_CLICK_TIME) + "";
				if (!DateUtil.isAfter(DateUtil.addMinute(DateUtil.parseTimestamp(clickTime), 15))) {
					clickCount = 1;
				}
			}
		}
		int result = shareCount - shareClick + clickCount;
		return ErrorCode.getSuccessReturn(result + "");
	}

	@Override
	public ErrorCode<String> saveShareWeibo(Long memberid, String tag, String source) {
		if (memberid == null || StringUtils.isBlank(tag) || StringUtils.isBlank(source)) {
			return ErrorCode.getFailure("参数错误！");
		}
		saveMemberMap(memberid, tag, source, DateUtil.getCurFullTimestampStr());
		return ErrorCode.SUCCESS;
	}

	private ErrorCode<String> drawClick(Member member, String tag, String source, String ip, Integer dayCount) {
		if (member == null || tag == null || source == null)
			return ErrorCode.getFailure("参数错误！");
		int shareCount = 0;
		int shareClick = 0;
		Map memberMap = getMemberMap(member.getId(), tag);
		if (memberMap != null) {
			String dateKey = DateUtil.currentTimeStr();
			Map otherMap = JsonUtils.readJsonToMap(memberMap.get(MongoData.ACTION_CONTENT) + "");
			Map infoMap = JsonUtils.readJsonToMap(otherMap.get(dateKey) + "");
			if (infoMap.get(TAG_SHARE_QQ) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_QZONE) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_SINA) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_QQ) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_QZONE) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_WIN_SINA) != null)
				shareCount++;
			if (infoMap.get(TAG_SHARE_CLICK) != null)
				shareClick = Integer.parseInt(infoMap.get(TAG_SHARE_CLICK) + "");
			if (shareClick >= shareCount) {// 倒计时抽奖时间
				String clickTime = infoMap.get(TAG_CLICK_TIME) + "";
				if (DateUtil.isAfter(DateUtil.addMinute(DateUtil.parseTimestamp(clickTime), 15))) {
					return ErrorCode.getFailure("亲，抽奖时间还没到哦！");
				}
			}
		}
		ErrorCode<String> result = drawUntransService.clickDraw(member, tag, source, null, null, null, ip, true, dayCount);
		if (result.isSuccess()) {
			if (shareClick >= shareCount) {
				// 保存抽奖时间
				saveMemberMap(member.getId(), tag, TAG_CLICK_TIME, DateUtil.getCurFullTimestampStr());
			} else {
				// 保存微博抽奖记录
				shareClick++;
				saveMemberMap(member.getId(), tag, TAG_SHARE_CLICK, shareClick + "");
			}

			return ErrorCode.getSuccessReturn(result.getRetval());
		} else {
			String errorMsg = "系统繁忙请重试！";
			if (StringUtils.equals(result.getMsg(), "statuss=-1")) {
				errorMsg = "请先登录！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=0")) {
				errorMsg = "活动未开始或已结束！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=2")) {
				errorMsg = "系统繁忙请重试！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=5")) {
				errorMsg = "请先绑定手机！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=7")) {
				errorMsg = "积分不足！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=8")) {
				errorMsg = "请先认证邮箱！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=9")) {
				errorMsg = "操作过于频繁！";
			} else if (StringUtils.equals(result.getMsg(), "statuss=11")) {
				errorMsg = "当前抽奖次数不足！";
			}
			return ErrorCode.getFailure(errorMsg);
		}
	}
	@Override
	public Map getShareStatusMap(Long memberid, String tag){
		Map resultMap = new HashMap();
		Map memberMap = getMemberMap(memberid, tag);
		if(memberMap != null){
			String dateKey = DateUtil.currentTimeStr();
			Map otherMap = JsonUtils.readJsonToMap(memberMap.get(MongoData.ACTION_CONTENT) + "");
			Map infoMap = JsonUtils.readJsonToMap(otherMap.get(dateKey) + "");
			if (infoMap.get(TAG_SHARE_QQ) != null)
				resultMap.put(TAG_SHARE_QQ, Status.Y);
			if (infoMap.get(TAG_SHARE_QZONE) != null)
				resultMap.put(TAG_SHARE_QZONE, Status.Y);
			if (infoMap.get(TAG_SHARE_SINA) != null)
				resultMap.put(TAG_SHARE_SINA, Status.Y);
			if (infoMap.get(TAG_SHARE_WIN_QQ) != null)
				resultMap.put(TAG_SHARE_WIN_QQ, Status.Y);
			if (infoMap.get(TAG_SHARE_WIN_QZONE) != null)
				resultMap.put(TAG_SHARE_WIN_QZONE, Status.Y);
			if (infoMap.get(TAG_SHARE_WIN_SINA) != null)
				resultMap.put(TAG_SHARE_WIN_SINA, Status.Y);
		}
		return resultMap;
	}

	private Map getMemberMap(Long memberid, String tag) {
		Map params = new HashMap();
		params.put(MongoData.GEWA_CUP_MEMBERID, memberid);
		params.put(MongoData.ACTION_TYPE, tag);
		params.put(MongoData.ACTION_TAG, tag);
		Map result = mongoService.findOne(MongoData.NS_ACTIVITY_COMMON_MEMBER, params);
		return result;
	}

	private void saveMemberMap(Long memberid, String tag, String key, String value) {
		Map memberMap = getMemberMap(memberid, tag);
		String dateKey = DateUtil.currentTimeStr();
		if (memberMap == null) {
			memberMap = new HashMap();
			memberMap.put(MongoData.SYSTEM_ID, ObjectId.uuid());
			memberMap.put(MongoData.GEWA_CUP_MEMBERID, memberid);
			memberMap.put(MongoData.ACTION_TYPE, tag);
			memberMap.put(MongoData.ACTION_TAG, tag);
			memberMap.put(MongoData.ACTION_ADDTIME, DateUtil.getCurFullTimestampStr());
			Map tempMap = new HashMap();
			tempMap.put(dateKey, "{}");
			memberMap.put(MongoData.ACTION_CONTENT, JsonUtils.writeMapToJson(tempMap));
		}
		Map otherMap = JsonUtils.readJsonToMap(memberMap.get(MongoData.ACTION_CONTENT) + "");
		Map infoMap = JsonUtils.readJsonToMap(otherMap.get(dateKey) + "");
		infoMap.put(key, value);
		otherMap.put(dateKey, JsonUtils.writeMapToJson(infoMap));
		memberMap.put(MongoData.ACTION_CONTENT, JsonUtils.writeMapToJson(otherMap));
		mongoService.saveOrUpdateMap(memberMap, MongoData.SYSTEM_ID, MongoData.NS_ACTIVITY_COMMON_MEMBER);
	}
}
