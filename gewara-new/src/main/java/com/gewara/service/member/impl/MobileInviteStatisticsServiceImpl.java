package com.gewara.service.member.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.command.InviteReport;
import com.gewara.service.member.MobileInviteStatisticsService;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ObjectId;

@Service("mobileInviteStatisticsService")
public class MobileInviteStatisticsServiceImpl implements MobileInviteStatisticsService {
	@Autowired@Qualifier("hibernateTemplate")
	private HibernateTemplate hibernateTemplate;
	public void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
		this.hibernateTemplate = hibernateTemplate;
	}
	protected final GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	
	/**
	 * 发送手机邀请好友短信统计
	 * @author yaopeng
	 */
	public List<InviteReport> getInviteReportList(Timestamp time) {
		dbLogger.warn("开始执行手机邀请好友短信统计查询...");
		//查询短信发送通道
		String channelSql = "select distinct(s.channel) from SMSRecord s where s.tag='mobile_sms_invite'";
		List<String> channelList = hibernateTemplate.find(channelSql);
		List<InviteReport> inviteList = new ArrayList<InviteReport>();
		if(!channelList.isEmpty()){
			Timestamp startTime = DateUtil.getBeginningTimeOfDay(time);
			Timestamp endTime = DateUtil.getLastTimeOfDay(time);
			for(String channel : channelList){
				InviteReport invite = new InviteReport();
				invite.setDay(DateUtil.format(startTime, "yyyy-MM-dd"));
				if(StringUtils.isNotBlank(channel)){
					invite.setChannel(channel);
					//查询发送总数
					String sendSql = "select count(s.id) from SMSRecord s where s.tag='mobile_sms_invite' and s.channel=? and s.status in ('Y','Y_TRANS','Y_LARGE','N','N_ERR','N_SEND_ERR') and s.sendtime between ? and ?";
					List sendlist = hibernateTemplate.find(sendSql,channel,startTime,endTime);
					int sendNum = 0;
					if(!sendlist.isEmpty()){
						sendNum= Integer.valueOf(sendlist.get(0).toString());
					}
					invite.setSendNum(sendNum);
					//查询发送失败
					String failedSql = "select count(s.id) from SMSRecord s where s.tag='mobile_sms_invite' and s.channel=? and s.status in ('N','N_ERR','N_SEND_ERR') and s.sendtime between ? and ?";
					List failedlist = hibernateTemplate.find(failedSql,channel,startTime,endTime);
					int failedNum = 0;
					if(!failedlist.isEmpty()){
						failedNum= Integer.valueOf(failedlist.get(0).toString());
					}
					invite.setFailedNum(failedNum);
					//查询发送时间延时超过三分钟数量
					String delay3MinSql = "select count(s.id)from SMSRecord s where s.tag='mobile_sms_invite' and s.status in ('Y','Y_LARGE') and s.channel=?" +
							"and ROUND(TO_NUMBER(TO_DATE(TO_CHAR(s.validtime,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss') - TO_DATE(TO_CHAR(s.sendtime,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss')) * 24 * 60)>3" +
							"and s.sendtime between ? and ?";
					List delay3Minlist = hibernateTemplate.find(delay3MinSql,channel,startTime,endTime);
					int delay3MinNum = 0;
					if(!delay3Minlist.isEmpty()){
						delay3MinNum = Integer.valueOf(delay3Minlist.get(0).toString());
					}
					invite.setDelay3MinNum(delay3MinNum);
					//查询发送时间1分钟内的数量
					String less1MinSql = "select count(s.id)from SMSRecord s where s.tag='mobile_sms_invite' and s.status in ('Y','Y_LARGE') and s.channel=?" +
							"and ROUND(TO_NUMBER(TO_DATE(TO_CHAR(s.validtime,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss') - TO_DATE(TO_CHAR(s.sendtime,'yyyy-mm-dd hh24:mi:ss'),'yyyy-mm-dd hh24:mi:ss')) * 24 * 60)<1" +
							"and s.sendtime between ? and ?";
					List less1Minlist = hibernateTemplate.find(less1MinSql,channel,startTime,endTime);
					int less1MinNum = 0;
					if(!less1Minlist.isEmpty()){
						less1MinNum = Integer.valueOf(less1Minlist.get(0).toString());
					}
					invite.setLess1MinNum(less1MinNum);
					invite.set_id(ObjectId.uuid());
					inviteList.add(invite);
				}
			}
		}
		dbLogger.warn("结束执行手机邀请好友短信统计查询...");
		return inviteList;
	}
}
