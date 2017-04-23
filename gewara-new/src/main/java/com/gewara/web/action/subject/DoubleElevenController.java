package com.gewara.web.action.subject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.constant.PayConstant;
import com.gewara.model.draw.DrawActivity;
import com.gewara.model.draw.Prize;
import com.gewara.mongo.MongoService;
import com.gewara.service.drama.DrawActivityService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.impl.ControllerService;
import com.gewara.untrans.subject.DoubleElevenService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.JsonUtils;
import com.gewara.web.action.AnnotationController;

@Controller
public class DoubleElevenController extends AnnotationController {
	@Autowired
	@Qualifier("doubleElevenService")
	private DoubleElevenService doubleElevenService;
	@Autowired@Qualifier("controllerService")
	protected ControllerService controllerService;
	@Autowired@Qualifier("drawActivityService")
	protected DrawActivityService drawActivityService;
	@Autowired@Qualifier("jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;
	@Autowired
	private MongoService mongoService;
	

	// 抽奖
	@RequestMapping("/subject/proxy/doubleEleven/drawClick.xhtml")
	public String drawClick(Long memberid, String tag, String captchaId, String captcha, String ip, ModelMap model) {
		int count = doubleElevenService.getTodayWinnerCount(memberid, tag);
		if(count > 13){
			boolean isValidCaptcha = controllerService.validateZtCaptcha(captchaId, captcha, ip);
			if(!isValidCaptcha) return showJsonError_CAPTCHA_ERROR(model);
		}
		ErrorCode<String> drawClickResult = doubleElevenService.drawClick(memberid, tag, ip, count);
		if (drawClickResult.isSuccess()) {
			return showJsonSuccess(model, drawClickResult.getRetval());
		} else {
			return showJsonError(model, drawClickResult.getMsg());
		}
	}
	//当日抽奖次数
	@RequestMapping("/subject/proxy/doubleEleven/getTodayWinnerCount.xhtml")
	public String getTodayWinnerCount(Long memberid, String tag, ModelMap model){
		int count = doubleElevenService.getTodayWinnerCount(memberid, tag);
		return showJsonSuccess(model, count + "");
	}
	
	// 得到抽奖时间
	@RequestMapping("/subject/proxy/doubleEleven/getClickTime.xhtml")
	public String getClickTime(Long memberid, String tag, ModelMap model) {
		ErrorCode<String> getClickTimeResult = doubleElevenService.getClickTime(memberid, tag);
		if (getClickTimeResult.isSuccess()) {
			return showJsonSuccess(model, getClickTimeResult.getRetval());
		} else {
			return showJsonError(model, getClickTimeResult.getMsg());
		}
	}

	// 得到抽奖次数
	@RequestMapping("/subject/proxy/doubleEleven/getClickCount.xhtml")
	public String getClickCount(Long memberid, String tag, ModelMap model) {
		ErrorCode<String> getClickCountResult = doubleElevenService.getClickCount(memberid, tag);
		if (getClickCountResult.isSuccess()) {
			return showJsonSuccess(model, getClickCountResult.getRetval());
		} else {
			return showJsonError(model, getClickCountResult.getMsg());
		}
	}

	// 保存分享微博
	@RequestMapping("/subject/proxy/doubleEleven/saveShareWeibo.xhtml")
	public String saveShareWeibo(Long memberid, String tag, String source, ModelMap model) {
		ErrorCode<String> saveShareWeiboCode = doubleElevenService.saveShareWeibo(memberid, tag, source);
		if (saveShareWeiboCode.isSuccess()) {
			return showJsonSuccess(model);
		} else {
			return showJsonError(model, saveShareWeiboCode.getMsg());
		}
	}
	
	//得到当天分享微博状态
	@RequestMapping("/subject/proxy/doubleEleven/getShareStatusMap.xhtml")
	public String getShareStatusMap(Long memberid, String tag, ModelMap model){
		if(memberid == null || StringUtils.isBlank(tag)) return showJsonError(model, "参数错误！");
		Map resultMap = doubleElevenService.getShareStatusMap(memberid, tag);
		return showJsonSuccess(model, JsonUtils.writeMapToJson(resultMap));
	}
	//我的战绩
	@RequestMapping("/subject/proxy/doubleEleven/getMemberRecord.xhtml")
	public String getMemberRecord(Long memberid, String tag, ModelMap model){
		Map result = new HashMap();
		DrawActivity drawActivity = daoService.getObjectByUkey(DrawActivity.class, "tag", tag, false);
		int pointCount = 0;
		int fiveCard = 0;
		int tenCard = 0;
		int exchangeCard = 0;
		int sum = 0;
		if(drawActivity != null){
			List winnerList = drawActivityService.getWinnerInfoByMemberid(drawActivity.getId(), memberid, -1, -1);
			if(!winnerList.isEmpty()){
				sum = winnerList.size();
				Map<Object, List> winnerMap = BeanUtil.groupBeanProperty(winnerList, "prizeid", "id");
				for (Object prizeid : winnerMap.keySet()) {
					Prize prize = daoService.getObject(Prize.class, Long.parseLong(prizeid+""));
					List wList = winnerMap.get(prizeid);
					if(PayConstant.CARDTYPE_POINT.equals(prize.getPtype())){
						if(prize.getPvalue().equals(20)){
							pointCount += wList.size() * 20;
						}else{
							pointCount += wList.size() * 5;
						}
					}else if(PayConstant.CARDTYPE_A.equals(prize.getPtype())){//兑换券
						exchangeCard += wList.size();
					}else if(PayConstant.CARDTYPE_D.equals(prize.getPtype())){//抵值券
						if(StringUtils.contains(prize.getPlevel(), "5")){//5元抵值券
							fiveCard += wList.size();
						}else{
							tenCard += wList.size();
						}
					}
				}
			}
		}
		result.put("sum", sum);
		result.put("pointCount", pointCount);
		result.put("fiveCard", fiveCard);
		result.put("tenCard", tenCard);
		result.put("exchangeCard", exchangeCard);
		return showJsonSuccess(model, JsonUtils.writeMapToJson(result));
	}
	@RequestMapping("/admin/doubleEleven/saveData.xhtml")
	public String saveData(ModelMap model){
		DrawActivity da = daoService.getObjectByUkey(DrawActivity.class, "tag", "doubleEleven", true);
		String sql = "select w.memberid as memberid, count(*) as cnt,m.addtime as regtime from webdata.winnerinfo w,webdata.memberinfo m where w.activityid=" + da.getId() + " and m.recordid=w.memberid group by w.memberid,m.addtime ";
		jdbcTemplate.query(sql, new ResultSetExtractor<String>(){
			@Override
			public String extractData(ResultSet rs) throws SQLException, DataAccessException {
				RowMapper<Map<String, Object>> mapper = new ColumnMapRowMapper();
				int rowcount = 0;
				while(rs.next()){
					Map<String, Object> data = mapper.mapRow(rs, rowcount ++);
					Long memberid = Long.parseLong(""+data.get("memberid"));
					Integer cnt = Integer.parseInt(""+data.get("cnt"));
					String regtime = DateUtil.formatTimestamp((Timestamp)data.get("regtime")); 
					Map<String, Object> cMap = new HashMap<String, Object>();
					cMap.put("_id", ""+memberid);
					cMap.put("regtime", regtime);
					cMap.put("cnt", cnt);
					
					mongoService.addMap(cMap, "_id", "draw_doubleEleven");
					if(rowcount %10000 == 0){
						dbLogger.warn("UPDATEDRAW:" + rowcount + "," + cMap);
					}
				}
				return "TOTAL:" + rowcount;
			}
		});
		
		return showJsonSuccess(model);
	}
	
}
