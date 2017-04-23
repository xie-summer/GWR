package com.gewara.helper.discount;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.gewara.constant.PayConstant;
import com.gewara.constant.order.ElecCardConstant;
import com.gewara.model.pay.ElecCard;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.pay.PayValidHelper;
import com.gewara.support.ErrorCode;
import com.gewara.util.BeanUtil;
import com.gewara.util.DateUtil;
import com.gewara.util.VmUtils;

/**
 * 电子券通用规则
 * @author acerge(acerge@163.com)
 * @since 7:21:43 PM Jul 10, 2011
 */
public class ElecCardHelper {

	/**
	 * 针对场次规则
	 * @param card
	 * @param opi
	 * @return
	 */
	public static ErrorCode getDisableReason(ElecCard card, OpenPlayItem opi) {
		//1、判断卡是否有效
		if(StringUtils.equals(card.getStatus(), ElecCardConstant.STATUS_USED)) return ErrorCode.getFailure("该券已被使用！");
		if(!card.available()) return ErrorCode.getFailure("该券已过期！");
		if(!card.validTag(PayConstant.APPLY_TAG_MOVIE)) return ErrorCode.getFailure("此卡不能在电影版块使用");
		Long batchid = card.getEbatch().getId();
		boolean isSupportCard = new PayValidHelper(VmUtils.readJsonToMap(opi.getOtherinfo())).supportCard(batchid);
		if(!isSupportCard) return ErrorCode.getFailure("该券不支持在该场次使用！");
		
		if(!StringUtils.contains(opi.getElecard(), card.getCardtype())){
			return ErrorCode.getFailure("本场次不支持使用此兑换券");
		}
		if(StringUtils.isNotBlank(card.getWeektype())){
			String week = ""+DateUtil.getWeek(opi.getPlaytime());
			if(card.getWeektype().indexOf(week) < 0){ 
				return ErrorCode.getFailure("该券限制为只能在周" + card.getWeektype() + "使用！");
			}
		}
		if(StringUtils.isNotBlank(card.getValidcinema())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidcinema(), ",");
			if(!cidList.contains(opi.getCinemaid())){
				return ErrorCode.getFailure("该券不支持在此影院使用！");
			}
		}
		if(!card.isUseCurTime()){//时间段限制
			String opentime = card.getEbatch().getAddtime1();
			String closetime = card.getEbatch().getAddtime2();
			return ErrorCode.getFailure("该券限制为只能在" + opentime + "至" +  closetime + "时段内使用！");
		}
		//限制场次时间段
		if(StringUtils.isNotBlank(card.getEbatch().getOpentime()) && StringUtils.isNotBlank(card.getEbatch().getClosetime())){
			String playtime = DateUtil.format(opi.getPlaytime(), "HHmm");
			String opentime = card.getEbatch().getOpentime();
			String closetime = card.getEbatch().getClosetime();
			if(playtime.compareTo(opentime)<0 || playtime.compareTo(closetime)>0)
				return ErrorCode.getFailure("该券限制场次只能在" + opentime + "至" + closetime + "时段内使用！");
		}
		if(!card.isCanUseCity(opi.getCitycode())){
			return ErrorCode.getFailure("该券不支持在该城市使用！");
		}
		if(StringUtils.isNotBlank(card.getValidmovie())){
			List<Long> cidList = BeanUtil.getIdList(card.getValidmovie(), ",");
			if(!cidList.contains(opi.getMovieid())){
				return ErrorCode.getFailure("影片不能使用此兑换券！");
			}
		}
		if(StringUtils.isNotBlank(card.getValiditem())){
			List<Long> cidList = BeanUtil.getIdList(card.getValiditem(), ",");
			if(!cidList.contains(opi.getMpid())){
				return ErrorCode.getFailure("本场次不能使用此兑换券！");
			}
		}
		if(!isEditionMatch(card.getCardtype(), card.getEbatch().getEdition(), opi.getEdition())){
			if(StringUtils.equals(card.getEbatch().getEdition(), ElecCardConstant.EDITION_ALL)){
				return ErrorCode.getFailure("该券限制为只能兑换2D或3D版本的影片！");
			}
			return ErrorCode.getFailure("该券限制为只能兑换" + card.getEbatch().getEdition() + "版本的影片！");
		}
		return ErrorCode.SUCCESS;
	}
	public static boolean isEditionMatch(String cardtype, String cardEdition, String opiEdition) {
		if(StringUtils.contains("BCD", cardtype)) return true;//BCD卡不限版本
		if(StringUtils.contains(opiEdition, "IMAX") || StringUtils.contains(opiEdition, "巨幕")){
			return StringUtils.contains(cardEdition, "IMAX");
		}else if(StringUtils.contains(opiEdition, "4D")){
			return StringUtils.contains(cardEdition, "4D");
		}
		if(StringUtils.isBlank(cardEdition) || StringUtils.equals(cardEdition, ElecCardConstant.EDITION_ALL)) return true;
		if(opiEdition.contains(ElecCardConstant.EDITION_3D)) return StringUtils.equals(ElecCardConstant.EDITION_3D, cardEdition);
		return !StringUtils.equals(ElecCardConstant.EDITION_3D, cardEdition);
	}
	
	public static String getSupportCard(OpenPlayItem opi){
		if(StringUtils.isBlank(opi.getElecard()) || StringUtils.equals(opi.getElecard(), "M")){
			return "";
		}
		List<String> result = new ArrayList<String>(3);
		Map<String, String> map = ElecCardConstant.getNormalMap();
		
		if(StringUtils.contains(opi.getEdition(), "IMAX")){
			map = ElecCardConstant.getImaxMap();
		}
		for(char c: opi.getElecard().toCharArray()){
			if(map.containsKey("" + c)){
				result.add(map.get("" + c));
			}
		}
		return StringUtils.join(result, "、");
	}
	public static boolean supportAllCard(OpenPlayItem opi){
		return StringUtils.isNotBlank(opi.getElecard()) && opi.getElecard().contains("A") 
				&& opi.getElecard().contains("B") && opi.getElecard().contains("D");
	}
}
