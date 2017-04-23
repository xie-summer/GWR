package com.gewara.service.gewapay.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.ApiConstant;
import com.gewara.constant.BindConstant;
import com.gewara.constant.ChargeConstant;
import com.gewara.constant.MemberConstant;
import com.gewara.constant.PayConstant;
import com.gewara.constant.PaymethodConstant;
import com.gewara.constant.SmsConstant;
import com.gewara.constant.Status;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.helper.discount.ElecCardHelper;
import com.gewara.helper.order.ElecCardContainer;
import com.gewara.helper.order.ElecCardFilter;
import com.gewara.model.acl.User;
import com.gewara.model.common.JsonData;
import com.gewara.model.drama.DramaOrder;
import com.gewara.model.drama.OpenDramaItem;
import com.gewara.model.draw.Prize;
import com.gewara.model.draw.WinnerInfo;
import com.gewara.model.goods.BaseGoods;
import com.gewara.model.pay.Charge;
import com.gewara.model.pay.Discount;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.pay.ElecCardBatch;
import com.gewara.model.pay.ElecCardExtra;
import com.gewara.model.pay.GoodsOrder;
import com.gewara.model.pay.GymOrder;
import com.gewara.model.pay.MemberAccount;
import com.gewara.model.pay.SMSRecord;
import com.gewara.model.pay.SportOrder;
import com.gewara.model.pay.TicketOrder;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.model.user.Member;
import com.gewara.pay.PayUtil;
import com.gewara.service.gewapay.ElecCardService;
import com.gewara.service.gewapay.PaymentService;
import com.gewara.service.impl.BaseServiceImpl;
import com.gewara.service.member.BindMobileService;
import com.gewara.support.ErrorCode;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.BeanUtil;
import com.gewara.util.BindUtils;
import com.gewara.util.ChangeEntry;
import com.gewara.util.DateUtil;
import com.gewara.util.ElecCardCoder;
import com.gewara.util.RandomUtil;
import com.gewara.util.StringUtil;
import com.gewara.util.ValidateUtil;
import com.gewara.xmlbind.gym.CardItem;

@Service("elecCardService")
public class ElecCardServiceImpl extends BaseServiceImpl implements ElecCardService {
	@Autowired@Qualifier("monitorService")
	private MonitorService monitorService;
	@Autowired@Qualifier("bindMobileService")
	private BindMobileService bindMobileService;
	public void setBindMobileService(BindMobileService bindMobileService){
		this.bindMobileService = bindMobileService;
	}
	@Autowired@Qualifier("paymentService")
	private PaymentService paymentService;
	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}
	@Override
	public List<ElecCardExtra> getAllSubCardExtraList() {
		return getSubCardExtraList(null, null, null, null, null, null, null, null);
	}
	@Override
	public List<ElecCardExtra> getSubCardExtraList(String status, String applydept, String applytype, Long adduserid, Timestamp addfrom, Timestamp addto) {
		return getSubCardExtraList(null, null, status, applydept, applytype, adduserid, addfrom, addto);
	}
	@Override
	public List<ElecCardExtra> getSubCardExtraListByIssuerId(Long issuserid) {
		return getSubCardExtraList(null, issuserid, null, null, null, null, null, null);
	}
	@Override
	public List<ElecCardExtra> getSubCardExtraListByStatus(String status) {
		return getSubCardExtraList(null, null, status, null, null, null, null, null);
	}
	@Override
	public List<User> getAddBatchUserList() {
		String hql = "select distinct adduserid from ElecCardExtra";
		List<Long> useridList = hibernateTemplate.find(hql);
		return baseDao.getObjectList(User.class, useridList);
	}
	@Override
	public List<ElecCardExtra> getTopCardExtraList(String status, String applydept, String applytype) {
		DetachedCriteria query = DetachedCriteria.forClass(ElecCardExtra.class);
		query.add(Restrictions.isNull("pid"));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		if(StringUtils.isNotBlank(applydept)) query.add(Restrictions.eq("applydept", applydept));
		if(StringUtils.isNotBlank(applytype)) query.add(Restrictions.eq("applytype", applytype));
		query.addOrder(Order.desc("addtime"));
		List<ElecCardExtra> batchList = hibernateTemplate.findByCriteria(query);
		return batchList;
	}
	private List<ElecCardExtra> getSubCardExtraList(Long pid, Long issuerid, String status, String applydept, String applytype, 
			Long adduserid, Timestamp addfrom, Timestamp addto) {
		DetachedCriteria query = DetachedCriteria.forClass(ElecCardExtra.class);
		if(pid !=null ) query.add(Restrictions.eq("pid", pid));
		else query.add(Restrictions.isNotNull("pid"));
		if(issuerid!=null) query.add(Restrictions.eq("issuerid", issuerid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.like("status", status, MatchMode.START));
		if(StringUtils.isNotBlank(applydept)) query.add(Restrictions.eq("applydept", applydept));
		if(StringUtils.isNotBlank(applytype)) query.add(Restrictions.eq("applytype", applytype));
		if(adduserid!=null) query.add(Restrictions.eq("adduserid", adduserid));
		if(addfrom!=null) query.add(Restrictions.ge("addtime", addfrom));
		if(addto!=null) query.add(Restrictions.le("addtime", addto));
		query.addOrder(Order.desc("pid"));
		query.addOrder(Order.desc("addtime"));
		List<ElecCardExtra> batchList = hibernateTemplate.findByCriteria(query);
		return batchList;
	}
	@Override
	public ErrorCode<ElecCardBatch> preSellBatch(Long bid, Long userid) {
		ElecCardExtra parentExtra = baseDao.getObject(ElecCardExtra.class, bid);
		if(parentExtra.hasParent()) return ErrorCode.getFailure("此批次不能预卖！");
		if(StringUtils.equals(parentExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能预卖！");
		}
		ElecCardBatch parentBatch = baseDao.getObject(ElecCardBatch.class, bid);
		ElecCardBatch childBatch = new ElecCardBatch();
		childBatch.copyFrom(parentBatch);
		childBatch.setPid(bid);
		
		baseDao.saveObject(childBatch);
		ElecCardExtra childExtra = new ElecCardExtra(childBatch.getId());
		childExtra.copyFrom(parentExtra);
		childExtra.setAdduserid(userid);
		childExtra.setPid(bid);
		childExtra.setChannel(parentExtra.getChannel() + System.currentTimeMillis());
		baseDao.saveObject(childExtra);
		return ErrorCode.getSuccessReturn(childBatch);
	}
	@Override
	public ErrorCode<String> addCardFromParent(Long bid, String cardFrom, String cardTo, Long userid){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo)){
			return ErrorCode.getFailure("卡号范围不对！");
		}
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, bid);
		if(!batchExtra.hasParent()) return ErrorCode.getFailure("没有上级批次！");
		if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能增加卡号！");
		}
		ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, bid);
		String update = "update ElecCard set ebatch = ? where ebatch.id = ? and cardno >= ? and cardno <= ? and status = ? ";
		int updated = hibernateTemplate.bulkUpdate(update, batch, batchExtra.getPid(), cardFrom, cardTo, ElecCardConstant.STATUS_NEW );
		return ErrorCode.getSuccess("总共更新" + updated+"");
	}
	@Override
	public ErrorCode addCardFromParent(ElecCardBatch batch, int addnum, Long userid) {
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, batch.getId());
		if(!batchExtra.hasParent()) return ErrorCode.getFailure("没有上级批次！");
		if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能增加卡号！");
		}
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, batch.getId());
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		String queryCardNo = "from ElecCard c where c.ebatch.id = ? and status = ? and possessor is null order by cardno";
		List<ElecCard> cardList1 = queryByRowsRange(queryCardNo, 0, 1, batchExtra.getPid(), userid, ElecCardConstant.STATUS_NEW);
		if(cardList1.isEmpty()) return ErrorCode.getFailure("卡号已耗尽！");
		List<ElecCard> cardList2 = queryByRowsRange(queryCardNo, addnum - 1, 1, batchExtra.getPid(), ElecCardConstant.STATUS_NEW);
		if(cardList1.isEmpty()) return ErrorCode.getFailure("卡号数量不够！");
		String cardFrom = cardList1.get(0).getCardno();
		String cardTo = cardList2.get(0).getCardno();
		String update = "update ElecCard set ebatch = ?, status= ? where ebatch.id = ? and cardno >= ? and cardno <= ? and (status = ? or status = ?) and possessor is null";
		int updated = hibernateTemplate.bulkUpdate(update, batch, ElecCardConstant.STATUS_NEW, batchExtra.getPid(), cardFrom, cardTo, ElecCardConstant.STATUS_NEW, ElecCardConstant.STATUS_DISCARD );
		return ErrorCode.getSuccessReturn("总共更新" + updated+"");
	}
	@Override
	public ErrorCode<String> returnElecCard(Long batchid, String cardFrom, String cardTo, Long userid){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			return ErrorCode.getFailure("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, batchid);
		if(!batchExtra.hasParent()) return ErrorCode.getFailure("没有上级批次！");
		if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		ElecCardBatch parent = baseDao.getObject(ElecCardBatch.class, batchExtra.getPid());
		String update = "update ElecCard set ebatch = ?, status= ? where ebatch.id = ? and cardno >= ? and cardno <= ? and (status = ? or status = ?) and possessor is null";
		int updated = hibernateTemplate.bulkUpdate(update, parent, ElecCardConstant.STATUS_NEW, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_NEW, ElecCardConstant.STATUS_DISCARD );
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	@Override
	public ErrorCode<String> unsellElecCard(Long batchid, String cardFrom, String cardTo, Long userid){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			return ErrorCode.getFailure("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, batchid);
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		String update = "update ElecCard set status= ? where ebatch.id = ? and cardno >= ? and cardno <= ? and status = ? and possessor is null";
		int updated = hibernateTemplate.bulkUpdate(update, ElecCardConstant.STATUS_NEW, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid + "取消卡售出状态,batchId:" + batchid + "cardNo: " + cardFrom + ", " + cardTo + ", 总共更新" + updated);
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	@Override
	public List<ElecCard> getCardList(Long bid, String cardFrom, String cardTo, String status){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			throw new IllegalArgumentException("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		String qry = "from ElecCard where ebatch.id = ? and cardno >= ? and cardno <= ? and status = ? ";
		List<ElecCard> cardList = hibernateTemplate.find(qry, bid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);
		return cardList;
	}
	
	@Override
	public ErrorCode<ElecCardExtra> genElecCard(Long bid, int num, Long userid) {
		if(num > 10000||num<0) return ErrorCode.getFailure("生成数量有错误！");
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, bid);
		if(batchExtra.hasParent()) return ErrorCode.getFailure("子批次不能！");
		if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		int maxCardNo = getMaxCardno(bid);
		if(maxCardNo + num >= 1000000) return ErrorCode.getFailure("生成的卡总数不能超出1000000");
		String cardnoBase = "G"+DateUtil.format(new Date(), "yyMMddHHmm");
		String insert = "INSERT INTO WEBDATA.ELECCARD(RECORDID, STATUS, BATCHID, CARDNO, CARDPASS, CARDPASS2) " +
				"values (WEBDATA.SEQ_ELECCARD.NEXTVAL, ?, ?, ?, ?, ?)";
		List<Object[]> argList = new ArrayList<Object[]>(num);
		for(int i=1; i<= num; i++){
			String cn = cardnoBase + StringUtils.leftPad("" +(maxCardNo + i), 6, '0');
			String pass = ElecCardCoder.getEncodePk();
			String rawPass = ElecCardCoder.decode(pass);
			String pass2 = ElecCardCoder.encode(StringUtil.md5(rawPass+rawPass));
			argList.add(new Object[]{ElecCardConstant.STATUS_NEW, bid, cn, pass, pass2});
		}
		jdbcTemplate.batchUpdate(insert,argList);
		return ErrorCode.getSuccessReturn(batchExtra);
	}
	private int getMaxCardno(Long bid){
		String query = "select max(cardno) from ElecCard where ebatch.id = ? or ebatch.pid = ? ";
		List<String> result = hibernateTemplate.find(query,  bid, bid );
		if(result.isEmpty() || result.get(0)==null) return 0;
		return Integer.parseInt(result.get(0).substring(11));
	}
	@Override
	public List<ElecCard> getElecCardByBatchId(Long bid, String status, boolean mobile, int from, int maxrows) {
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.createAlias("ebatch", "e").add(Restrictions.eq("e.id", bid));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		if(mobile)query.addOrder(Order.desc("mobile"));
		query.addOrder(Order.asc("status"));
		query.addOrder(Order.asc("cardno"));
		List<ElecCard> cardList = hibernateTemplate.findByCriteria(query, from, maxrows);
		return cardList;
	}
	@Override
	public ErrorCode discardElecCard(Long cardId, Long userid){
		ElecCard card = baseDao.getObject(ElecCard.class, cardId);
		if(!card.getStatus().equals(ElecCardConstant.STATUS_SOLD)){
			return ErrorCode.getFailure("只有售出的卡才能废弃！");
		}
		if(card.isUsed()){
			return ErrorCode.getFailure("使用了的卡不能废弃！");
		}
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, card.getEbatch().getId());
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		card.setStatus(ElecCardConstant.STATUS_DISCARD);
		card.setDeluserid(userid);
		card.setDeltime(new Timestamp(System.currentTimeMillis()));
		baseDao.saveObject(card);
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode lockElecCard(Long cardId, Long userid){
		ElecCard card = baseDao.getObject(ElecCard.class, cardId);
		if(card == null) return ErrorCode.getFailure("卡号不存在！");
		if(!card.getStatus().equals(ElecCardConstant.STATUS_SOLD)){
			return ErrorCode.getFailure("只有售出的卡才能冻结！");
		}
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, card.getEbatch().getId());
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		card.setStatus(ElecCardConstant.STATUS_LOCK);
		baseDao.saveObject(card);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid +"操作(冻结Y->L)卡号ID为:"+cardId+",卡号是:"+card.getCardno());
		return ErrorCode.SUCCESS;
	}
	@Override
	public ErrorCode<String> batchLockElecCard(Long batchid, String cardFrom, String cardTo, Long userid){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			return ErrorCode.getFailure("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, batchid);
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		String update = "update ElecCard set status= ? where ebatch.id = ? and cardno >= ? and cardno <= ? and status = ?";
		int updated = hibernateTemplate.bulkUpdate(update, ElecCardConstant.STATUS_LOCK, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid + "冻结(Y->L)卡售出状态,batchId:" + batchid + "cardNo: " + cardFrom + ", " + cardTo + ", 总共更新" + updated);
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	@Override
	public ErrorCode<String> batchUnLockElecCard(Long batchid, String cardFrom, String cardTo, Long userid){
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			return ErrorCode.getFailure("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, batchid);
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		String update = "update ElecCard set status= ? where ebatch.id = ? and cardno >= ? and cardno <= ? and status = ?";
		int updated = hibernateTemplate.bulkUpdate(update, ElecCardConstant.STATUS_SOLD, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_LOCK);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid + "解冻(L->Y)卡售出状态,batchId:" + batchid + "cardNo: " + cardFrom + ", " + cardTo + ", 总共更新" + updated);
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	
	@Override
	public ErrorCode<String> batchLockElecCard(Long batchid, Long memberid, Long userid){
		if(batchid == null && memberid == null) return ErrorCode.getFailure("批次号与用户ID不能都为空！");
		List params = new ArrayList();
		String update = "update ElecCard set status= ? where status = ? ";
		params.add(ElecCardConstant.STATUS_LOCK);
		params.add(ElecCardConstant.STATUS_SOLD);
		if(batchid != null){
			update += " and ebatch.id = ? ";
			params.add(batchid);
		}
		if(memberid != null){
			update += " and possessor = ?";
			params.add(memberid);
		}
		int updated = hibernateTemplate.bulkUpdate(update, params.toArray());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid + "解冻(Y->L)卡售出状态,batchId:" + batchid + "用户ID: " + memberid+ ", 总共更新" + updated);
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	
	@Override
	public ErrorCode<String> batchUnLockElecCard(Long batchid, Long memberid, Long userid){
		if(batchid == null && memberid == null) return ErrorCode.getFailure("批次号与用户ID不能都为空！");
		List params = new ArrayList();
		String update = "update ElecCard set status= ? where status = ? ";
		params.add(ElecCardConstant.STATUS_SOLD);
		params.add(ElecCardConstant.STATUS_LOCK);
		if(batchid != null){
			update += " and ebatch.id = ? ";
			params.add(batchid);
		}
		if(memberid != null){
			update += " and possessor = ?";
			params.add(memberid);
		}
		int updated = hibernateTemplate.bulkUpdate(update, params.toArray());
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid + "解冻(L->Y)卡售出状态,batchId:" + batchid + "用户ID: " + memberid+ ", 总共更新" + updated);
		return ErrorCode.getSuccessReturn("总共更新" + updated);
	}
	@Override
	public ErrorCode unlockcardElecCard(Long cardId, Long userid){
		ElecCard card = baseDao.getObject(ElecCard.class, cardId);
		if(card == null) return ErrorCode.getFailure("卡号不存在！");
		if(!card.getStatus().equals(ElecCardConstant.STATUS_LOCK)){
			return ErrorCode.getFailure("只有冻结的卡才能解冻！");
		}
		ElecCardExtra cardExtra = baseDao.getObject(ElecCardExtra.class, card.getEbatch().getId());
		if(StringUtils.equals(cardExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		card.setStatus(ElecCardConstant.STATUS_SOLD);
		baseDao.saveObject(card);
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid +"操作(解冻L->Y)卡号ID为:"+cardId+",卡号是:"+card.getCardno());
		return ErrorCode.SUCCESS;
	}
	
	@Override
	public ErrorCode<String> returnElecCard(Long cardId, Long userid){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid+"返还卡" + cardId);
		ElecCard card = baseDao.getObject(ElecCard.class, cardId);
		if(!card.getStatus().equals(ElecCardConstant.STATUS_NEW)) 
			return ErrorCode.getFailure("只有未售出的卡才能退还！");
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, card.getEbatch().getId());
		if(!batchExtra.hasParent()) return ErrorCode.getFailure("此卡可能已经退回！");
		if(StringUtils.equals(batchExtra.getStatus(), ElecCardConstant.DATA_HIS)){
			return ErrorCode.getFailure("已经冻结的批次不能修改！");
		}
		ElecCardBatch parent = baseDao.getObject(ElecCardBatch.class, batchExtra.getPid()); 
		card.setEbatch(parent);
		baseDao.saveObject(card);
		ElecCardExtra parentExtra = baseDao.getObject(ElecCardExtra.class, batchExtra.getPid());
		if(parentExtra.getMincardno()==null || parentExtra.getMincardno().compareTo(card.getCardno()) > 0){
			parentExtra.setMincardno(card.getCardno());
		}
		if(parentExtra.getMaxcardno()==null || parentExtra.getMaxcardno().compareTo(card.getCardno()) < 0){
			parentExtra.setMaxcardno(card.getCardno());
		}

		return ErrorCode.getSuccessReturn("成功退还" + card.getCardno());
	}
	@Override
	public ErrorCode<String> unsellElecCard(Long cardId, Long userid){
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, userid+"取消售出状态" + cardId);
		ElecCard card = baseDao.getObject(ElecCard.class, cardId);
		if(!card.getStatus().equals(ElecCardConstant.STATUS_SOLD)) 
			return ErrorCode.getFailure("只有售出的卡才能取消！");
		if(card.getPossessor() != null) return ErrorCode.getFailure("被他人占用的卡不能取消！");
		card.setStatus(ElecCardConstant.STATUS_NEW);
		baseDao.saveObject(card);
		return ErrorCode.getSuccessReturn("成功取消售出状态" + card.getCardno());
	}
	
	@Override
	public ErrorCode<String> soldElecBatch(Long batchId, Long userid){
		ElecCardExtra batch = baseDao.getObject(ElecCardExtra.class, batchId);
		batch.setSellerid(userid);
		String update = "update ElecCard set status = ? where status = ? and ebatch.id = ? ";
		int updated = hibernateTemplate.bulkUpdate(update,  ElecCardConstant.STATUS_SOLD, ElecCardConstant.STATUS_NEW, batchId);
		batch.setSoldtime(new Timestamp(System.currentTimeMillis()));
		dbLogger.warnWithType(LogTypeConstant.LOG_TYPE_USERACTION, "卖出卡[" + userid + "]:" + batchId);
		batch.setSellerid(userid);
		baseDao.saveObject(batch);
		return ErrorCode.getSuccessReturn("本次总共卖出" + updated + "张票！");
	}
	@Override
	public int getElecCardCountByBatchId(Long batchId, String status) {
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.createAlias("ebatch", "e").add(Restrictions.eq("e.id", batchId));
		if(StringUtils.isNotBlank(status)) query.add(Restrictions.eq("status", status));
		query.setProjection(Projections.rowCount());
		List result = hibernateTemplate.findByCriteria(query);
		return Integer.parseInt(""+result.get(0));
	}

	@Override
	public ElecCard getElecCardByPass(String cardPass){
		String query = "from ElecCard where cardpass = ?";
		String encodePass = ElecCardCoder.encode(StringUtil.md5(cardPass + cardPass));
		List<ElecCard> cardList = hibernateTemplate.find(query, encodePass);
		if(cardList.size()>0) return cardList.get(0);
		return null;
	}
	@Override
	public ElecCard getMemberElecCardByNo(Long memberid, String cardno){
		ElecCard card = getElecCardByNo(cardno);
		if(card!=null && card.getPossessor()!=null && card.getPossessor().equals(memberid)) return card;
		return null;
	}
	private ElecCard getElecCardByNo(String cardno){
		String query = "from ElecCard where cardno = ?";
		List<ElecCard> cardList = hibernateTemplate.find(query, cardno);
		if(cardList.size()>0) return cardList.get(0);
		return null;
	}
	
	@Override
	public ElecCard getHistElecCardByPass(String cardPass){
		String query = "select * from WEBDATA.ELECCARD_HIST where cardpass = ?";
		String encodePass = ElecCardCoder.encode(cardPass);
		List<Map<String, Object>> list = jdbcTemplate.queryForList(query, encodePass);
		if(list.size()>0) {
			Map data = list.get(0);
			return getElecCardByMap(data);
		}
		return null;
	}
	@Override
	public ElecCard getHistElecCardByNo(String cardno){
		String query = "select * from WEBDATA.ELECCARD_HIST where cardno = ?";
		List<Map<String, Object>> list = jdbcTemplate.queryForList(query, cardno);
		if(list.size()>0) {
			Map data = list.get(0);
			return getElecCardByMap(data);
		}
		return null;
	}
	private ElecCard getElecCardByMap(Map data){
		ElecCard elecCard = new ElecCard();
		Map map = new CaseInsensitiveMap(data);
		BindUtils.bindData(elecCard, map);
		elecCard.setEbatch(baseDao.getObject(ElecCardBatch.class, new Long(""+map.get("BATCHID"))));
		return elecCard;
	}
	
	@Override
	public void updateBatchExtra(ElecCardExtra extra){
		Timestamp updatetime = new Timestamp(System.currentTimeMillis());
		if(StringUtils.equals(extra.getStatus(), ElecCardConstant.DATA_HIS)) return;//已经结转
		String query1 = "SELECT BATCHID, COUNT(*) AS CARDCOUNT, MAX(CARDNO) AS MAXCARDNO, MIN(CARDNO) AS MINCARDNO," +
				"SUM(CASE STATUS WHEN 'N' THEN 1 ELSE 0 END) AS NEWCOUNT, " +
				"SUM(CASE STATUS WHEN 'Y' THEN 1 ELSE 0 END) AS SOLDCOUNT, " +
				"SUM(CASE STATUS WHEN 'D' THEN 1 ELSE 0 END) AS DELCOUNT, " +
				"SUM(CASE STATUS WHEN 'U' THEN 1 ELSE 0 END) AS USEDCOUNT, " +
				"SUM(CASE STATUS WHEN 'L' THEN 1 ELSE 0 END) AS LOCKCOUNT, " +
				"SUM(NVL2(GAINER, 1, 0)) AS ISSUECOUNT " +
				"FROM WEBDATA.VELECCARD WHERE BATCHID = ? GROUP BY BATCHID";
		List<Map<String, Object>> nowList = jdbcTemplate.queryForList(query1, extra.getBatchid());
		String query2 = "SELECT BATCHID, SUM(CARDCOUNT) AS CARDCOUNT, MAX(MAXCARDNO) AS MAXCARDNO, MIN(MINCARDNO) AS MINCARDNO," +
				"SUM(NEWCOUNT) AS NEWCOUNT, SUM(SOLDCOUNT) AS SOLDCOUNT, SUM(DELCOUNT) AS DELCOUNT, SUM(USEDCOUNT) AS USEDCOUNT," +
				"SUM(LOCKCOUNT) AS LOCKCOUNT, SUM(ISSUECOUNT) AS ISSUECOUNT " +
				"FROM WEBDATA.ELECCARD_HIS_STATUS WHERE BATCHID =? GROUP BY BATCHID";
		
		List<Map<String, Object>> hisList = jdbcTemplate.queryForList(query2, extra.getBatchid());
		int cardcount = 0,newcount=0,soldcount=0,delcount=0,usedcount=0,lockcount=0,issuecount=0;
		String mincardno="", maxcardno="";
		if(!nowList.isEmpty() && nowList.get(0).get("CARDCOUNT")!=null||!hisList.isEmpty() && hisList.get(0).get("CARDCOUNT")!=null){
			if(!nowList.isEmpty() && nowList.get(0).get("CARDCOUNT")!=null){
				Map now = nowList.get(0);
				cardcount = Integer.valueOf(""+now.get("CARDCOUNT"));
				newcount = Integer.valueOf(""+now.get("NEWCOUNT"));
				soldcount = Integer.valueOf(""+now.get("SOLDCOUNT"));
				delcount = Integer.valueOf(""+now.get("DELCOUNT"));
				usedcount = Integer.valueOf(""+now.get("USEDCOUNT"));
				lockcount = Integer.valueOf(""+now.get("LOCKCOUNT"));
				issuecount = Integer.valueOf(""+now.get("ISSUECOUNT"));
				mincardno = ""+now.get("MINCARDNO");
				maxcardno = ""+now.get("MAXCARDNO");
			}
			if(!hisList.isEmpty() && hisList.get(0).get("CARDCOUNT")!=null){
				Map his = hisList.get(0);
				cardcount += Integer.valueOf(""+his.get("CARDCOUNT"));
				newcount += Integer.valueOf(""+his.get("NEWCOUNT"));
				soldcount += Integer.valueOf(""+his.get("SOLDCOUNT"));
				delcount += Integer.valueOf(""+his.get("DELCOUNT"));
				usedcount += Integer.valueOf(""+his.get("USEDCOUNT"));
				lockcount += Integer.valueOf(""+his.get("LOCKCOUNT"));
				issuecount += Integer.valueOf(""+his.get("ISSUECOUNT"));
				if(StringUtils.isEmpty(mincardno)) mincardno = ""+his.get("MINCARDNO");
				else if(mincardno.compareTo(""+his.get("MINCARDNO")) > 0) mincardno = ""+his.get("MINCARDNO");
				if(StringUtils.isEmpty(maxcardno)) maxcardno = ""+his.get("MAXCARDNO");
				else if(maxcardno.compareTo(""+his.get("MAXCARDNO")) < 0) maxcardno = ""+his.get("MAXCARDNO");
			}
		}
		
		extra.setCardcount(cardcount);
		extra.setNewcount(newcount);
		extra.setSoldcount(soldcount);
		extra.setDelcount(delcount);
		extra.setUsedcount(usedcount);
		extra.setLockcount(lockcount);
		extra.setMincardno(mincardno);
		extra.setMaxcardno(maxcardno);
		extra.setStatstime(updatetime);
		extra.setIssuecount(issuecount);
		baseDao.saveObject(extra);
	}
	@Override
	public ErrorCode<String> assignMobile(Long batchid, List<String> mobileList, Integer num, 
			String cardFrom, String cardTo) {
		ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, batchid);
		if(batch==null) return ErrorCode.getFailure("该批次不存在");
		
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			throw new IllegalArgumentException("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		String qry = "from ElecCard where ebatch.id = ? and cardno >= ? and cardno <= ? and mobile is null and status = ? ";
		List<ElecCard> cardList = hibernateTemplate.find(qry, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);

		if(mobileList.size()*num>cardList.size()) return ErrorCode.getFailure("卡号不够分配！");
		
		List<ElecCard> changeList = new ArrayList<ElecCard>();
		Iterator<ElecCard> cardIt = cardList.iterator();
		ElecCard card = null;
		String msg = "共分配" + cardList.size() + "个，错误的手机号有：";
		for(String mobile: mobileList){
			if(ValidateUtil.isMobile(StringUtils.trim(mobile))){
				for(int i=0;i<num;i++){
					card = cardIt.next();
					card.setMobile(StringUtils.trim(mobile));
					changeList.add(card);
				}
			}else{
				msg +="," + mobile;
			}
		}
		baseDao.saveObjectList(cardList);
		return ErrorCode.getSuccessReturn(msg);
	}
	@Override
	public ErrorCode<String> unassignMobile(Long batchid, String cardFrom, String cardTo) {
		ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, batchid);
		if(batch==null) return ErrorCode.getFailure("该批次不存在");
		String query = "from SMSRecord t where t.tradeNo >= ? and t.tradeNo <= ? and t.relatedid =? and t.smstype=? and (t.status='N' or t.status='Y')";
		List<SMSRecord> smsList = hibernateTemplate.find(query, cardFrom, cardTo, batchid, SmsConstant.SMSTYPE_ECARD);
		List<ElecCard> cardList = getCardList(batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);
		Map<String, SMSRecord> smsMap = BeanUtil.beanListToMap(smsList, "tradeNo");
		List<ElecCard> changeList = new ArrayList<ElecCard>();
		List<String> errList = new ArrayList<String>();
		for(ElecCard card: cardList){
			SMSRecord sms = smsMap.get(card.getCardno());
			if(sms!=null){
				if(SmsConstant.STATUS_Y.equals(sms.getStatus())){
					errList.add(card.getCardno());
					continue;
				}else{
					baseDao.removeObject(sms);
				}
			}
			if(StringUtils.isNotBlank(card.getMobile())){
				card.setMobile(null);
				changeList.add(card);
			}
		}
		baseDao.saveObjectList(changeList);
		String msg = "共取消" +  changeList.size() + "个";
		if(errList.size() > 0) msg += ", 短信已发送，无法取消的有：" + StringUtils.join(errList, ",");
		return ErrorCode.getSuccessReturn(msg);
	}
	@Override
	public ErrorCode<String> bind2Member(Long batchid, String flag, List<Long> memberidList, Integer num, String cardFrom, String cardTo) {
		ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, batchid);
		if(batch==null) return ErrorCode.getFailure("该批次不存在");
		if(memberidList.size()>10000) return ErrorCode.getFailure("第次最多绑定500个！");
		if(StringUtils.isBlank(cardFrom) || StringUtils.isBlank(cardTo))
			throw new IllegalArgumentException("卡号范围不对！");
		cardFrom = StringUtils.trim(cardFrom);
		cardTo = StringUtils.trim(cardTo);
		String qry = "from ElecCard where ebatch.id = ? and cardno >= ? and cardno <= ? and mobile is null and gainer is null and status = ? ";
		List<ElecCard> cardList = hibernateTemplate.find(qry, batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);

		if(memberidList.size() * num > cardList.size()) return ErrorCode.getFailure("卡号不够分配！");
		
		List<ElecCard> changeList = new ArrayList<ElecCard>();
		Iterator<ElecCard> cardIt = cardList.iterator();
		ElecCard card = null;
		String errorMember = "";
		int count = 0;
		for(Long memberid: memberidList){
			Member member = baseDao.getObject(Member.class, memberid);
			if(member==null){
				errorMember += "," + memberid;
				continue;
			}else{
				for(int i=0;i<num;i++){
					card = cardIt.next();
					card.setGainer(memberid);
					card.setPossessor(memberid);
					card.setMobile(flag);
					changeList.add(card);
				}
				count +=num;
			}
		}
		String msg = "共分配" + count + "个卡号" + (StringUtils.isBlank(errorMember)?"!":errorMember + "是错误ID！");
		baseDao.saveObjectList(cardList);
		return ErrorCode.getSuccessReturn(msg);
	}
	@Override
	public ErrorCode<String> unbindMember(Long batchid, String flag, String cardFrom, String cardTo) {
		ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, batchid);
		if(batch==null) return ErrorCode.getFailure("该批次不存在");
		List<ElecCard> cardList = getCardList(batchid, cardFrom, cardTo, ElecCardConstant.STATUS_SOLD);
		List<ElecCard> changeList = new ArrayList<ElecCard>();
		for(ElecCard card: cardList){
			if(StringUtils.equals(card.getMobile(), flag) && card.getPossessor()!=null && card.getGainer()!=null){
				card.setMobile(null);
				card.setPossessor(null);
				card.setGainer(null);
				changeList.add(card);
			}
		}
		baseDao.saveObjectList(changeList);
		String msg = "共取消" +  changeList.size() + "个";
		return ErrorCode.getSuccessReturn(msg);
	}
	@Override
	public ElecCardContainer getAvailableCardList(final TicketOrder order, List<Discount> discountList, final OpenPlayItem opi, Long memberid) {
		// 电子票
		List<ElecCard> tmpcard = getValidCardListByMemberId(memberid, "movie");
		ElecCardContainer container = new ElecCardContainer(tmpcard, discountList, new ElecCardFilter(){
			@Override
			public boolean available(ElecCard card) {
				return card.canUse(opi.getPlaytime(), opi.getCinemaid(), opi.getMovieid(), opi.getMpid(), order.getCitycode()) 
						&& ElecCardHelper.getDisableReason(card, opi).isSuccess();
			}
			
		});
		return container;
	}
	@Override
	public ElecCardContainer getAvailableCardList(final DramaOrder order, List<Discount> discountList, final OpenDramaItem item, Long memberid) {
		// 电子票
		List<ElecCard> tmpcard = getValidCardListByMemberId(memberid, "drama");
		ElecCardContainer container = new ElecCardContainer(tmpcard, discountList, new ElecCardFilter(){
			@Override
			public boolean available(ElecCard card) {
				return card.canUse(item.getPlaytime(), item.getTheatreid(), item.getDramaid(), item.getId(), order.getCitycode());
			}
			
		});
		return container;
	}
	@Override
	public ElecCardContainer getAvailableCardList(final SportOrder order, List<Discount> discountList, final OpenTimeTable table, Long memberid) {
		// 电子票
		List<ElecCard> tmpcard = getValidCardListByMemberId(memberid, "sport");
		ElecCardContainer container = new ElecCardContainer(tmpcard, discountList, new ElecCardFilter(){
			@Override
			public boolean available(ElecCard card) {
				return card.canUse(table.getPlaydate(), table.getSportid(), table.getItemid(), table.getId(), order.getCitycode());
			}
			
		});
		return container;
	}
	@Override
	public ElecCardContainer getAvailableCardList(GoodsOrder order, List<Discount> discountList, BaseGoods goods, Long memberid) {
		// 电子票
		List<ElecCard> tmpcard = getValidCardListByMemberId(memberid, "goods");
		ElecCardContainer container = new ElecCardContainer(tmpcard, discountList, new ElecCardFilter(){
			@Override
			public boolean available(ElecCard card) {
				return card.available();
			}
			
		});
		return container;
	}
	@Override
	public ElecCardContainer getAvailableCardList(final GymOrder order, List<Discount> discountList, final CardItem item, Long memberid) {
		// 电子票
		List<ElecCard> tmpcard = getValidCardListByMemberId(memberid, "gym");
		ElecCardContainer container = new ElecCardContainer(tmpcard, discountList, new ElecCardFilter(){
			@Override
			public boolean available(ElecCard card) {
				return card.canUse(null, item.getGymid(), null, item.getId(), order.getCitycode());
			}
			
		});
		return container;
	}
	private List<ElecCard> getValidCardListByMemberId(Long memberId, String tag){
		String query = "from ElecCard e where e.possessor = ? and e.ebatch.tag=?";
		List<ElecCard> result2 = new ArrayList<ElecCard>();
		List<ElecCard> result = hibernateTemplate.find(query, memberId, tag);
		for(ElecCard card : result){
			if(card.available()) result2.add(card);
		}
		return result2;
	}

	private ElecCard getRandomAvalableCard(Long batchid) {
		String query = "from ElecCard t where t.ebatch.id = ? and t.status = ? and t.gainer is null and t.possessor is null and t.mobile is null";
		List<ElecCard> cardList = queryByRowsRange(query, 0, 50, batchid, ElecCardConstant.STATUS_SOLD);
		if(cardList.size() > 0) return RandomUtil.getRandomObject(cardList);
		return null;
	}
	
	@Override
	public List<ElecCard> getCardListByMemberid(Long memberid, String tag, int from, int maxnum){
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.createAlias("ebatch", "e").add(Restrictions.ne("e.cardtype", PayConstant.CARDTYPE_E));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("e.tag", tag));
		query.add(Restrictions.eq("possessor", memberid));
		query.addOrder(Order.desc("status"));
		query.addOrder(Order.desc("e.timeto"));
		query.addOrder(Order.desc("id"));
		List<ElecCard> cardList = hibernateTemplate.findByCriteria(query, from, maxnum);
		return cardList;
	}
	@Override
	public Integer getCardCountByMemberid(Long memberid, String tag){
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.setProjection(Projections.rowCount());
		query.createAlias("ebatch", "e").add(Restrictions.ne("e.cardtype", PayConstant.CARDTYPE_E));
		if(StringUtils.isNotBlank(tag)) query.add(Restrictions.eq("e.tag", tag));
		query.add(Restrictions.eq("possessor", memberid));
		List<ElecCard> cardList = hibernateTemplate.findByCriteria(query);
		if(cardList.isEmpty()) return 0;
		return Integer.parseInt("" + cardList.get(0));
	}
	@Override
	public ErrorCode<String> registerCard(Member member, String cardpass, String ip) {
		Long memberid = member.getId();
		if(StringUtils.isBlank(cardpass)) {
			return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR, "密码不能为空！");
		}
		ElecCard card = getElecCardByPass(StringUtils.upperCase(cardpass));
		if (card == null) return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "票券密码有误！");
		ElecCardBatch elecCardBatch = card.getEbatch();
		if(elecCardBatch != null){
			if(StringUtils.equals(elecCardBatch.getCardtype(), PayConstant.CARDTYPE_E)){
				return ErrorCode.getFailure(ApiConstant.CODE_DATA_ERROR, "绑定票券不支持绑定充值卡！");
			}
			if(StringUtils.equals(elecCardBatch.getActivation(), ElecCardBatch.ACTIVATION_Y)){
				Integer appointCount = getCardCountByMember(elecCardBatch.getId(), member.getId());
				if(appointCount >= elecCardBatch.getAppoint()){
					return ErrorCode.getFailure("根据活动规则，您绑定该批次票券的次数已满，如有疑问，请致电4000-406-506！");
				}
			}
		}
		if(!card.available()) {
			if(!card.canDelay()){
				return ErrorCode.getFailure(card.gainErrorMsg());
			}
		}
		if(card.getPossessor() != null){
			if(!card.getPossessor().equals(memberid)){
				return ErrorCode.getFailure("此券不存在或被占用！");
			}else{
				return ErrorCode.getSuccess("票券已绑定成功！请不要重复绑定！");
			}
		}
		ChangeEntry change = new ChangeEntry(card);
		card.setPossessor(memberid);
		if(card.getGainer() == null){// 20120328加入初次绑定该卡的用户
			card.setGainer(memberid);
		}
		baseDao.saveObject(card);
		monitorService.saveChangeLog(memberid, ElecCard.class, card.getCardno(), change.getChangeMap(card));
		Map<String, String> info = new HashMap<String, String>();
		info.put("cardno", card.getCardno());
		monitorService.saveMemberLogMap(memberid, MemberConstant.ACTION_REGCARD, info, ip);
		return ErrorCode.getSuccessReturn(card.getCardno());
	}
	@Override
	public ErrorCode<ElecCard> chargeByCard(Member member, MemberAccount account, String cardpass, String ip) {
		return chargeByCard(member, account, cardpass, ip, null);
		
	}
	private ErrorCode<ElecCard> chargeByCard(Member member, MemberAccount account, String cardpass, String ip, Long operatorId) {
		if(StringUtils.isBlank(cardpass)) {
			return ErrorCode.getFailure(ApiConstant.CODE_PARAM_ERROR, "密码不能为空！");
		}
		ElecCard card = getElecCardByPass(StringUtils.upperCase(cardpass));
		if(card==null){
			return ErrorCode.getFailure("没有查询到对应的卡，请核实卡密码");
		}
		if(!PayConstant.CARDTYPE_E.equals(card.getEbatch().getCardtype())) {
			return ErrorCode.getFailure("充值错误:此卡并非充值卡！");
		}
		if(card.available()){
			if(card.getPossessor() != null && !card.getPossessor().equals(member.getId())){
				return ErrorCode.getFailure("此兑换券已被占用！");
			}
			ChangeEntry change = new ChangeEntry(card);
			card.setPossessor(member.getId());
			if(card.getGainer() == null){//加入初次绑定该卡的用户
				card.setGainer(member.getId());
			}
			//1.charge
			int amount = card.getEbatch().getAmount();
			Charge charge = new Charge(PayUtil.getChargeTradeNo(), ChargeConstant.WABIPAY);
			charge.setMemberid(member.getId());
			charge.setMembername(member.getNickname());
			charge.setPaymethod(PaymethodConstant.PAYMETHOD_CHARGECARD);
			charge.setPaybank("" + card.getEbatch().getId());			//卡批次ID
			charge.setPayseqno(card.getCardno());
			charge.setTotalfee(amount);
			charge.setStatus(Charge.STATUS_PAID);
			baseDao.saveObject(charge);
			//2.account
			dbLogger.warn(charge.getTradeNo() + "充值卡充值:不可退款金额：由"+ account.getOthercharge()+"增加" + amount);
			account.addBanlance(amount);
			account.addWabicharge(amount); //充值卡为不可退金额
			baseDao.saveObject(account);
			//3.card
			card.setStatus(ElecCardConstant.STATUS_USED);
			card.setOrderid(charge.getId());
			baseDao.saveObject(card);
			monitorService.saveChangeLog(operatorId != null ? operatorId : member.getId(), 
										 ElecCard.class, card.getCardno(), change.getChangeMap(card));
			Map<String, String> info = new HashMap<String, String>();
			info.put("cardno", card.getCardno());
			
			if (operatorId != null)
				info.put("operator", operatorId.toString());
			
			monitorService.saveMemberLogMap(member.getId(), MemberConstant.ACTION_REGCARD, info, ip);
			return ErrorCode.getSuccessReturn(card);
		}
		return ErrorCode.getFailure(card.gainErrorMsg());
		
	}
	
	@Override
	public List<ElecCardBatch> getSubBatchListByMerchantid(Long merchantid) {
		String query = "from ElecCardBatch where merchantid=? order by pid, id";
		List<ElecCardBatch> batchList = hibernateTemplate.find(query, merchantid);
		return batchList;
	}
	@Override
	public void addCardBatch(ElecCardBatch batch, Long userid) {
		baseDao.saveObject(batch);
		ElecCardExtra batchExtra = new ElecCardExtra(batch.getId());
		batchExtra.setAdduserid(userid);
		baseDao.saveObject(batchExtra);
	}
	@Override
	public ErrorCode frozenBatch(Long bid) {
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		ElecCardExtra batchExtra = baseDao.getObject(ElecCardExtra.class, bid);
		if(!batchExtra.hasParent()){
			List<ElecCardExtra> subList = getSubCardExtraList(batchExtra.getBatchid(), null, null, null, null, null, null, null);
			for(ElecCardExtra extra:subList){
				if(!StringUtils.equals(extra.getStatus(), ElecCardConstant.DATA_HIS)){
					return ErrorCode.getFailure("只有子批次都设置才能设置此批次！！");
				}
			}
		}else{
			ElecCardBatch batch = baseDao.getObject(ElecCardBatch.class, bid);
			if(batch.getTimeto().before(cur)){//已过期
				String query = "select count(id) from ElecCard e where e.ebatch.id=? and status = ? and endtime > ?";
				int count = Integer.valueOf(""+hibernateTemplate.find(query, batch.getId(), ElecCardConstant.STATUS_SOLD, cur).get(0));
				if(count >0 ) return ErrorCode.getFailure("还有其他卡未过期！！");
			}else{
				return ErrorCode.getFailure("此批次卡未过期！！");
			}
		}
		batchExtra.setStatus(ElecCardConstant.DATA_NOW);
		updateBatchExtra(batchExtra);
		batchExtra.setStatus(ElecCardConstant.DATA_HIS);
		baseDao.saveObject(batchExtra);
		return ErrorCode.SUCCESS;
	}
	@Override
	public Map<String,Object> getTopCardExtraListByValidcinema(Long cinemaId) {
		DetachedCriteria query = DetachedCriteria.forClass(ElecCardBatch.class);
		query.add(Restrictions.eq("validcinema", cinemaId + ""));
		List<ElecCardBatch> eCardBatchs = hibernateTemplate.findByCriteria(query);
		List<Long> batchIds = new ArrayList<Long>();
		Map<Long,ElecCardBatch> eCardBatchMap = new HashMap<Long,ElecCardBatch>();
		if(eCardBatchs != null){
			for(ElecCardBatch elecCardBatch:eCardBatchs){
				batchIds.add(elecCardBatch.getId());
				eCardBatchMap.put(elecCardBatch.getId(), elecCardBatch);
			}
		}
		if(batchIds.size() == 0){
			return null;
		}
		DetachedCriteria criteria = DetachedCriteria.forClass(ElecCardExtra.class);
		criteria.add(Restrictions.isNull("pid"))
			.add(Restrictions.eq("status", ElecCardConstant.DATA_NOW))
			.add(Restrictions.in("batchid", batchIds));
		List<ElecCardExtra> batchList = hibernateTemplate.findByCriteria(criteria);
		List<ElecCardExtra> subBatchList = hibernateTemplate.findByCriteria(
		DetachedCriteria.forClass(ElecCardExtra.class)
			.add(Restrictions.isNotNull("pid"))
			.add(Restrictions.like("status", ElecCardConstant.DATA_NOW, MatchMode.START))
			.add(Restrictions.in("batchid", batchIds))
			.addOrder(Order.desc("pid"))
			.addOrder(Order.desc("addtime")));
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("topElecCardExtra", batchList);
		map.put("subElecCardExtra", subBatchList);
		map.put("eCardBatchMap", eCardBatchMap);
		return map;
	}
	@Override
	public ErrorCode checkActivationCard(Member member, String cardPass, String checkpass){
		if(!member.isBindMobile()){
			return ErrorCode.getFailure("系统检测到你的账号还没绑定手机号码为了你的账户安全，请先绑定此手机号码至你的账户！");
		}
		ElecCard card = getElecCardByPass(cardPass);
		if(card == null) return ErrorCode.getFailure("你要激活的票券不存在！");
		ElecCardBatch eBatch = card.getEbatch();
		if(!StringUtils.equals(eBatch.getActivation(), ElecCardBatch.ACTIVATION_Y)){ 
			return ErrorCode.getSuccess("该票券不需要激活！");
		}
		if(card.getPossessor()!=null && !member.getId().equals(card.getPossessor())){
			return ErrorCode.getFailure("券已经被他人绑定！");
		}

		ErrorCode bindMobileCode = bindMobileService.checkBindMobile(BindConstant.TAG_DYNAMICCODE_CARD, member.getMobile(), checkpass);
		if(!bindMobileCode.isSuccess()) return ErrorCode.getFailure(bindMobileCode.getMsg());
		Integer appointCount = getCardCountByMobile(eBatch.getId(), member.getMobile());
		if(appointCount >= eBatch.getAppoint()){
			return ErrorCode.getFailure("根据活动规则，你激活票券的次数已满，如有疑问，请致电4000-406-506！");
		}
		card.setMobile(member.getMobile());
		baseDao.saveObject(card);
		dbLogger.warn("memberid:" + member.getId() +",cardno:" + card.getCardno() +",激活手机号:"+ member.getMobile());
		return ErrorCode.SUCCESS;
	}
	private Integer getCardCountByMember(Long batchid, Long memberId){
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.add(Restrictions.eq("possessor", memberId));
		query.createAlias("ebatch", "e").add(Restrictions.eq("e.id", batchid));
		query.setProjection(Projections.rowCount());
		List<Long> cardList = hibernateTemplate.findByCriteria(query);
		if(cardList.isEmpty()) return 0;
		return Integer.parseInt("" + cardList.get(0));
	}
	private Integer getCardCountByMobile(Long batchid, String mobile){
		DetachedCriteria query = DetachedCriteria.forClass(ElecCard.class);
		query.add(Restrictions.eq("mobile", mobile));
		query.createAlias("ebatch", "e").add(Restrictions.eq("e.id", batchid));
		query.setProjection(Projections.rowCount());
		List<Long> cardList = hibernateTemplate.findByCriteria(query);
		if(cardList.isEmpty()) return 0;
		return Integer.parseInt("" + cardList.get(0));
	}
	@Override
	public String getElecCardpass(ElecCard elecCard) {
		return ElecCardCoder.decode(elecCard.getCardpass());
	}
	@Override
	public boolean hasUsed(ElecCardBatch batch) {
		if(!batch.hasParent()) return false;
		String query = "select id from ElecCard where ebatch.id=? and status=? ";
		List<Long> result = queryByRowsRange(query, 0, 1, batch.getId(), ElecCardConstant.STATUS_USED);
		return !result.isEmpty();
	}
	@Override
	public List<String> batchDiscard(Long userId, String[] cardPassOrNoList, boolean byCardno) {
		List<String> msgList = new ArrayList<String>();
		Timestamp cur = new Timestamp(System.currentTimeMillis());
		int count=0;
		for(String cardno:cardPassOrNoList){
			ElecCard card = null;
			if(byCardno){
				card = getElecCardByNo(cardno);
			}else{
				card = getElecCardByPass(cardno);
			}
			
			if(card==null) msgList.add("不存在卡号:" + cardno);
			else if(card.isUsed()) msgList.add("卡已被使用:" + cardno);
			else if(card.getPossessor()!=null) msgList.add("卡被占用" + card.getPossessor() + ":" + cardno);
			else if(StringUtils.isNotBlank(card.getMobile())) msgList.add("卡被占用" + card.getMobile() + ":" + cardno);
			else if(StringUtils.equals(card.getStatus(), ElecCardConstant.STATUS_DISCARD)) msgList.add("已废弃:" + cardno);
			else {
				card.setStatus(ElecCardConstant.STATUS_DISCARD);
				card.setDeluserid(userId);
				card.setDeltime(cur);
				baseDao.updateObject(card);
				count ++;
			}
		}
		msgList.add("总共" + cardPassOrNoList.length + "个, 成功" + count);
		return msgList;
	}
	@Override
	public ElecCard queryCardByNo(String cardno) {
		return getElecCardByNo(cardno);
	}
	@Override
	public ErrorCode batchAddAmountWithElecCard(Set<Long> mems, Set<String> cards, Long operatorId, String ip) {
		List<Member> memberList = baseDao.getObjectList(Member.class, mems);
		int success = 0;
		StringBuffer successIds = new StringBuffer();
		int faild = 0;
		StringBuffer faildIds = new StringBuffer();
		Iterator<String> iterator = cards.iterator();
		for (int i=0; i < memberList.size(); i++ ){
			Member member = memberList.get(i);
			String cardpass = iterator.next();
			
			MemberAccount account = baseDao.getObjectByUkey(MemberAccount.class, "memberid", member.getId(), false);
			if (account == null){
				account = paymentService.createNewAccount(member);
			}
			int org = account.getBanlance();
			ErrorCode chargeResult = chargeByCard(member, account, cardpass, ip, operatorId);
			if (chargeResult.isSuccess()){
				success+=1;
				successIds.append("," + member.getId());
				dbLogger.warn("成功：" + member.getId() + ":" + member.getEmail() + ":" + member.getMobile() + "::" + org + "----->" + account.getBanlance());
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}else{
				faild+=1;
				faildIds.append("," + member.getId());
				dbLogger.warn("失败：" + member.getId() + ":" + member.getEmail() + ":" + member.getMobile() + "::" + cardpass + "::reason:" + chargeResult.getMsg());
			}
		}
		return ErrorCode.getFailure("", "充值结果:" + "\n" + "成功：" + success + "个" + ":" + successIds.toString() + "\n"
				+ "失败：" + faild + "个:" + faildIds.toString());
	}
	@Override
	public ErrorCode randomSendPrize(Prize prize, WinnerInfo winner) {
		if(winner.getStatus().equals(Status.Y)) throw new IllegalArgumentException("奖品已送出！");
		if(!PayConstant.CARDTYPE_A.equals(prize.getPtype()) && 
				!PayConstant.CARDTYPE_D.equals(prize.getPtype())){//兑换券
			return ErrorCode.getFailure("奖品类型不正确！");
			
		}
		Long batchid = Long.parseLong(prize.getTag());
		ElecCard card = getRandomAvalableCard(batchid);
		if(card == null){
			return ErrorCode.getFailure("奖品不够!");
		}
		card.setMobile(winner.getMobile());
		if(card.getEbatch().getDaynum() > 0){
			Timestamp cur = new Timestamp(System.currentTimeMillis());
			Timestamp batchBegin = card.getEbatch().getTimefrom();
			Timestamp from = cur.before(batchBegin)?batchBegin:cur;
			card.setBegintime(from);
			card.setEndtime(DateUtil.addDay(from, card.getEbatch().getDaynum()));
		}
		if(winner.getMemberid() != null) {
			card.setPossessor(winner.getMemberid());
			card.setGainer(winner.getMemberid());
		}
		baseDao.saveObject(card);
		winner.setRemark(card.getCardno());
		winner.setRelatedid(card.getId());
		baseDao.saveObject(winner);
		return ErrorCode.SUCCESS;
	}
	@Override
	public int upgradeElecCard(){
		String query = "from ElecCard where id > ? order by id";
		JsonData config = baseDao.getObject(JsonData.class, "upgradeElecCard");
		Long from = Long.parseLong(config.getData());
		List<ElecCard> cardList = queryByRowsRange(query, 0, 2000, from);
		String update = "UPDATE WEBDATA.ELECCARD SET CARDPASS2 =? WHERE RECORDID = ? ";
		for(ElecCard card: cardList){
			String pass2 = getElecCardpass(card);
			pass2 = ElecCardCoder.encode(StringUtil.md5(pass2 + pass2));
			jdbcTemplate.update(update, pass2, card.getId());
		}
		Long maxid = cardList.get(cardList.size()-1).getId();
		config.setData(""+maxid);
		
		dbLogger.warn("total:" + cardList.size() + ",from:" + from + ",maxid:" + maxid);
		return cardList.size(); 
	}
}
