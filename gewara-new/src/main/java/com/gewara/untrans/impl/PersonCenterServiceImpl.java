package com.gewara.untrans.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.Config;
import com.gewara.constant.Status;
import com.gewara.constant.TagConstant;
import com.gewara.constant.content.SignName;
import com.gewara.constant.sys.MongoData;
import com.gewara.json.bbs.RecommendActivity;
import com.gewara.json.bbs.RecommendCommu;
import com.gewara.json.bbs.RecommendPerson;
import com.gewara.model.bbs.commu.Commu;
import com.gewara.model.content.GewaCommend;
import com.gewara.model.user.Member;
import com.gewara.model.user.MemberInfo;
import com.gewara.model.user.Treasure;
import com.gewara.mongo.MongoService;
import com.gewara.service.DaoService;
import com.gewara.service.bbs.BlogService;
import com.gewara.service.bbs.CommuService;
import com.gewara.service.content.RecommendService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.CommonService;
import com.gewara.untrans.PersonCenterService;
import com.gewara.untrans.activity.SynchActivityService;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.GewaLogger;
import com.gewara.util.JsonUtils;
import com.gewara.util.LoggerUtils;
import com.gewara.xmlbind.activity.RemoteActivity;

@Service("personCenterService")
public class PersonCenterServiceImpl implements PersonCenterService {
	private final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	@Autowired@Qualifier("synchActivityService")
	private SynchActivityService synchActivityService;
	public void setActivityRemoteService(SynchActivityService synchActivityService) {
		this.synchActivityService = synchActivityService;
	}
	private BlockingQueue<Long> meberIdQueue = new LinkedBlockingQueue<Long>();

	@Override
	public void putMemberId(Long memberId) {
		try {
			this.meberIdQueue.put(memberId);
		} catch (InterruptedException e) {
			dbLogger.error("插入用户中心队列被中断 memberID:" + memberId, e);
			Thread.currentThread().interrupt();
		}
	}

	@PostConstruct
	public void init() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					Long memberId = 0L;
					try {
						memberId = meberIdQueue.take();
						Map map = mongoService.getMap("myid", MongoData.NS_MEMBER_INFO, memberId);
						if(map == null || !StringUtils.equals(DateUtil.getCurDateStr(), (String)map.get("date"))){
							personCenterLeftData(memberId);
						}
					} catch (InterruptedException e) {
						dbLogger.error("抽取个人中心队列被中断 memberID:" + memberId, e);
						Thread.currentThread().interrupt();
					} catch (Exception e) {
						dbLogger.error("生成个人中心聚合数据错误 memberID:" + memberId, e);
					}

				}

			}
		}).start();
	}

	@Autowired
	@Qualifier("daoService")
	private DaoService daoService;
	@Autowired
	@Qualifier("blogService")
	protected BlogService blogService;
	@Autowired
	@Qualifier("recommendService")
	private RecommendService recommendService;
	@Autowired
	@Qualifier("mongoService")
	private MongoService mongoService;
	@Autowired
	@Qualifier("commuService")
	private CommuService commuService;
	@Autowired
	@Qualifier("commonService")
	private CommonService commonService;

	/**
	 * 个人中心聚合数据
	 * 
	 * @param memberId
	 */
	private void personCenterLeftData(Long memberId) {

		Member member = daoService.getObject(Member.class, memberId);

		List<RecommendPerson> listp = new ArrayList<RecommendPerson>();
		// 管理员后台推荐
		Map params = new HashMap();
		List<Map> recommendMemberList = mongoService.find(MongoData.NS_RECOMMEND_MEMBER, params, "ordernum", true);
		for (Map map : recommendMemberList) {
			Long memberid = (Long) map.get("id");
			Boolean b = blogService.isTreasureMember(member.getId(), memberid);
			if (!b) {

				MemberInfo memberInfo = daoService.getObject(MemberInfo.class, memberid);
				RecommendPerson rp = new RecommendPerson(memberid, (String) map.get("membername"), memberInfo.getHeadpicUrl(),
						(String) map.get("reason"));
				listp.add(rp);
			}
		}
		List<RecommendActivity> listActivity = new ArrayList<RecommendActivity>();
		MemberInfo memberinfo = daoService.getObject(MemberInfo.class, member.getId());
		Map pm = new HashMap();
		pm.put("myid", member.getId());
		pm.put("date", DateUtil.getCurDateStr());
		pm.put("recommedPerson", JsonUtils.writeObjectToJson(listp));

		// 热门活动
		List<RemoteActivity> hotActivityList = new ArrayList<RemoteActivity>();
		List<GewaCommend> gewaCommendList = commonService.getGewaCommendList(memberinfo.getFromcity(),SignName.ACTIVITY_HOT_ACTIVITY, null, TagConstant.TAG_ACTIVITY, true,  0, 100);
		List<Long> idList = BeanUtil.getBeanPropertyList(gewaCommendList, Long.class, "relatedid", true);
		ErrorCode<List<RemoteActivity>> code = synchActivityService.getGewaCommendActivityList(idList, false);
		if(code.isSuccess()) hotActivityList = code.getRetval();
		for (RemoteActivity activity : hotActivityList) {
			RecommendActivity ra = new RecommendActivity(activity.getId(), activity.getTitle(), activity.getLogo());
			if (!listActivity.contains(ra))
				listActivity.add(ra);
		}
		pm.put("recomendActivity", JsonUtils.writeObjectToJson(listActivity));

		// 你关注的xx 等用户在这个群中，关注的用户所在的群。
		List<RecommendCommu> commuList = new ArrayList<RecommendCommu>();
		Map<Long, String> commumap = recommendService.getCommuListByToSameMember(member.getId(), "member", Treasure.ACTION_COLLECT);
		for (Long commuId : commumap.keySet()) {
			Commu commu = daoService.getObject(Commu.class, commuId);
			if (commu == null || !commu.hasStatus(Status.Y)) continue;
			boolean isMember = commuService.isCommuMember(commuId, member.getId());
			if (!isMember) {
				RecommendCommu rc = new RecommendCommu(commu.getId(), commu.getName(), commu.getCommumembercount(), commu.getHeadpicUrl(),
						commumap.get(commuId));
				commuList.add(rc);
			}
		}

		// 热门quanzi
		List<GewaCommend> personcommulist = commonService.getGewaCommendList(null, SignName.PERSONINDEX_SPORTCOMMU, null, null, true, 0, 1000);
		for (GewaCommend commend : personcommulist) {
			Commu commu = daoService.getObject(Commu.class, commend.getRelatedid());
			boolean isMember = commuService.isCommuMember(commend.getRelatedid(), member.getId());
			if (!isMember) {

				List<Map> listmap = recommendService.getCommuMyTreasureMember(commend.getRelatedid(), member.getId(), "member",
						Treasure.ACTION_COLLECT);
				String memberIdStr = "";
				for (Map mapMember : listmap) {
					Long memberid = (Long) mapMember.get("memberid");
					if (StringUtils.isNotBlank(memberIdStr)) {
						memberIdStr = memberIdStr + "," + memberid;
					} else {
						memberIdStr = memberid.toString();
					}
				}

				RecommendCommu rc = new RecommendCommu(commu.getId(), commu.getName(), commu.getCommumembercount(), commu.getHeadpicUrl(),
						memberIdStr);
				if (!commuList.contains(rc))
					commuList.add(rc);
			}
		}

		pm.put("recommendCommu", JsonUtils.writeObjectToJson(commuList));

		mongoService.saveOrUpdateMap(pm, "myid", MongoData.NS_MEMBER_INFO);

		dbLogger.warn("生成个人中心推荐数据成功 memberID:" + memberId);

	}
}
