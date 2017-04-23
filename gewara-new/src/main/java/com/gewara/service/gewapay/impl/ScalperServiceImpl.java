package com.gewara.service.gewapay.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import com.gewara.json.pay.Scalper;
import com.gewara.mongo.MongoService;
import com.gewara.service.gewapay.ScalperService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;

@Service("scalperService")
public class ScalperServiceImpl extends BaseServiceImpl implements ScalperService, InitializingBean{
	
	@Autowired@Qualifier("mongoService")
	private MongoService mongoService;
	
	private Map<String, Set<String>> scalperMap = new HashMap<String, Set<String>>(); 
	
	@Override
	public ErrorCode<String> checkScalperLimited(Long memberId, String phone, Long specialDiscountId) {
		if(memberId == null || specialDiscountId == null){
			return ErrorCode.getSuccess("没有限制条件");
		}
		String scname = getScaperKey(memberId, phone);
		if(StringUtils.isNotBlank(scname)){
			if(RandomUtils.nextInt(100) < 95){
				return ErrorCode.getFailure(scname + "黄牛限制使用特殊优惠");
			}else{
				return ErrorCode.getSuccess(scname + "黄牛很幸运!");
			}
		}
		return ErrorCode.getSuccess("不在黄牛电话限制列表中");

	}
	@Override
	public boolean isScalper(Long memberId, String phone) {
		return getScaperKey(memberId, phone)!=null;
	}
	private String getScaperKey(Long memberId, String phone) {
		for(String key: scalperMap.keySet()){
			Set<String> list = scalperMap.get(key);
			if(list.contains(String.valueOf(memberId)) || StringUtils.isNotBlank(phone) && list.contains(phone)){
				return key;
			}
		}
		return null;
	}
	
	public void initScalper(){
		List<Scalper> scalperList = mongoService.getObjectList(Scalper.class);
		for(Scalper sc: scalperList){
			scalperMap.put(sc.getId() + sc.getName(), new HashSet<String>(Arrays.asList(StringUtils.split(sc.getMobiles(), ","))));
		}
	}
	
	public Map<String,List<Map>> getSuspectScalperByIp(int hours, int count){
		if(count < 5){
			count = 5;
		}
		if(hours > 0){
			hours = -12;
		}
		Timestamp startTime = DateUtil.addHour(DateUtil.getCurTruncTimestamp(), hours);
		List<String> ipList = this.getIPAndRegisterCount(startTime, count);
		Map<String, List<Map>> memberMap = new HashMap<String, List<Map>>();
		String hql = "select new map(m.id as id, m.nickname as nickname,m.mobile as mobile, m.password as password, mi.addtime as addtime, mi.source as source)" +
				" from Member m ,MemberInfo mi where m.id = mi.id and mi.ip=? and mi.addtime > ? order by m.id desc";
		for(String ip : ipList){				
				memberMap.put(ip, this.queryByRowsRange(hql, 0, 1000, ip, startTime));
		}
		return memberMap;
		
		
	}
	/**
	 * 
	 * @param startTime
	 * @param endTime
	 * @param count 一个ip注册多少个用户
	 * @return
	 */
	private List<String> getIPAndRegisterCount(final Timestamp startTime, final Integer count){
		String sql = "select ip as rgip ,count(recordid) from webdata.memberinfo " +
				"where addtime > ? and ip is not null " +
				"group by ip having count(recordid) > ?";
		return this.jdbcTemplate.query(sql, new RowMapper<String>(){
			@Override
			public String mapRow(ResultSet rs, int rowNum) throws SQLException {
				return rs.getString("rgip");
			}
			
		}, startTime, count);
	}

	@Override
	public void refreshCurrent(String newConfig) {
		dbLogger.warn("刷新黄牛缓存");
		initScalper();
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		initScalper();
	}

}
