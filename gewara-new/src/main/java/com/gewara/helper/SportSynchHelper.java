package com.gewara.helper;

import java.sql.Timestamp;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.MemberCardConstant;
import com.gewara.constant.OpenTimeTableConstant;
import com.gewara.model.pay.MemberCardOrder;
import com.gewara.model.sport.MemberCardInfo;
import com.gewara.model.sport.MemberCardType;
import com.gewara.model.sport.OpenTimeItem;
import com.gewara.model.sport.OpenTimeTable;
import com.gewara.model.sport.SportField;
import com.gewara.util.DateUtil;
import com.gewara.xmlbind.sport.GstOti;
import com.gewara.xmlbind.sport.GstOtt;
import com.gewara.xmlbind.sport.GstSportField;
import com.gewara.xmlbind.sport.RemoteMemberCardInfo;
import com.gewara.xmlbind.sport.RemoteMemberCardOrder;
import com.gewara.xmlbind.sport.RemoteMemberCardType;

public class SportSynchHelper {
	public static void copyMemberCardType(MemberCardType cardType,RemoteMemberCardType mct){
		cardType.setCardTypeCode(mct.getCardTypeCode());
		cardType.setCardTypeUkey(mct.getCardTypeUkey());
		cardType.setCardType(mct.getCardType());
		cardType.setMoney(mct.getMoney());
		cardType.setOverNum(mct.getOverNum());
		cardType.setReserve(mct.getReserve());
		cardType.setDescription(mct.getDescription());
		cardType.setPrice(mct.getPrice());
		cardType.setValidTime(mct.getValidTime());
		cardType.setBelongVenue(mct.getBelongVenue());
		cardType.setBusinessId(mct.getBusinessId());
		cardType.setDiscount(mct.getDiscount());
		cardType.setFitItem(mct.getFitItem());
	}
	public static void copyMemberCardInfo(MemberCardInfo mi, RemoteMemberCardInfo mci){
		mi.setMemberCardCode(mci.getMemberCardCode());
		mi.setName(mci.getName());
		mi.setSex(mci.getSex());
		mi.setMobile(mci.getMobile());
		mi.setOverMoney(mci.getOverMoney());
		mi.setValidtime(mci.getValid());
		mi.setCardStatus(mci.getCardStatus());
		mi.setFitItem(mci.getFitItem());
		mi.setBelongVenue(mci.getBelongVenue());
		mi.setCardTypeUkey(mci.getCardTypeUkey());
	}
	public static void copyMemberCardInfo(MemberCardInfo mi, MemberCardOrder order, RemoteMemberCardOrder rorder){
		mi.setMemberCardCode(rorder.getMemberCardCode());
		mi.setMemberid(order.getMemberid());
		mi.setTypeid(order.getMctid());
		mi.setTradeno(order.getTradeNo());
		mi.setMobile(rorder.getMobile());
		mi.setOverMoney(rorder.getOverMoney());
		if(rorder.getValid()!=null){
			mi.setValidtime(DateUtil.addDay(order.getAddtime(), rorder.getValid()*30));
		}
		mi.setCardStatus(MemberCardConstant.CARD_STATUS_Y);
		mi.setFitItem(rorder.getFitItem());
		mi.setBelongVenue(rorder.getBelongVenue());
		mi.setCardTypeUkey(rorder.getCardTypeUkey());
	}
	public static OpenTimeItem createOpenTimeItem(Long sportid, Long itemid, GstOti oti) {
		OpenTimeItem item = new OpenTimeItem();
		item.setSportid(sportid);
		item.setItemid(itemid);
		
		item.setCostprice(0);
		item.setPrice(0);
		item.setNorprice(oti.getNorprice());
		item.setHour(oti.getHour());
		item.setStatus(oti.getStatus());
		item.setValidtime(new Timestamp(System.currentTimeMillis()));
		
		item.setIkey(oti.getIkey());
		item.setRfieldid(oti.getFieldid());
		item.setRottid(oti.getOttid());
		item.setRotiid(oti.getId());
		
		item.setMinpoint(0);
		item.setMaxpoint(0);
		item.setOtherinfo("{}");
		item.setQuantity(oti.getQuantity());
		item.setSales(oti.getSales() == null?0:oti.getSales());
		
		item.setOpenType(oti.getOpenType());
		item.setEndhour(oti.getEndhour());
		item.setUnitMinute(oti.getUnitMinute());
		item.setUnitType(oti.getUnitType());
		
		item.setUpsetprice(0);
		return item;
	}
	public static OpenTimeTable createOpenTimeTable(GstOtt ott){
		OpenTimeTable table = new OpenTimeTable();
		table.setItemid(ott.getItemid());
		table.setSportid(ott.getSportid());
		table.setPlaydate(ott.getPlaydate());
		table.setStatus(OpenTimeTableConstant.STATUS_NOBOOK);
		table.setOpentime(DateUtil.getCurFullTimestamp());
		table.setRemoteid(ott.getId());
		table.setClosetime(DateUtil.getEndTimestamp(ott.getPlaydate()));
		table.setRstatus(ott.getStatus());
		table.setTkey(ott.getTkey());
		table.setVer(OpenTimeTableConstant.VERSION_V2);
		
		table.setMinpoint(500);
		table.setMaxpoint(10000);
		
		table.setSales(0);
		table.setElecard("ABDM");
		table.setWeek(DateUtil.getWeek(table.getPlaydate()));
		table.setOpenType(ott.getOpenType() == null? OpenTimeTableConstant.OPEN_TYPE_FIELD : ott.getOpenType());
		table.setUnitMinute(ott.getUnitMinute() == null ? 60: ott.getUnitMinute());
		table.setQuantity(ott.getQuantity());
		table.setRemain(ott.getQuantity()!=null?ott.getQuantity():0);
		return table;
	}
	public static SportField createSportField(GstSportField remote){
		SportField sf = new SportField();
		sf.setSportid(remote.getSportid());
		sf.setItemid(remote.getItemid());
		sf.setName(remote.getName());
		sf.setOrdernum(remote.getOrdernum());
		sf.setRemoteid(remote.getId());
		if(StringUtils.equalsIgnoreCase(remote.getStatus(), "delete")) {
			sf.setStatus("N");
		}else {
			sf.setStatus("Y");
		}
		return sf;
	}

}
