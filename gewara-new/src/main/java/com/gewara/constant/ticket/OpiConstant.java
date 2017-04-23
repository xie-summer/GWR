/**
 * 
 */
package com.gewara.constant.ticket;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;

import com.gewara.model.movie.MoviePlayItem;
import com.gewara.model.movie.MoviePrice;
import com.gewara.model.ticket.OpenPlayItem;
import com.gewara.support.ErrorCode;
import com.gewara.util.DateUtil;
import com.gewara.util.StringUtil;
public abstract class OpiConstant {
	public static final String STATUS_BOOK = "Y";			//接受预订
	public static final String STATUS_NOBOOK = "N";			//不接受预订
	public static final String STATUS_RECOVER = "R";		//从删除状态恢复，需要处理
	public static final String STATUS_DISCARD = "D";		//废弃
	public static final String STATUS_CLOSE = "C";			//场次状态	
	public static final String STATUS_PAST = "P";			//场次过期

	
	public static final String PARTNER_OPEN = "Y";			//对外开放
	public static final String PARTNER_CLOSE = "N";			//不对外开放
	public static final String OPEN_GEWARA = "GEWA";		//开放类型：不与火凤凰对接
	public static final String OPEN_HFH = "HFH";			//开放类型：与火凤凰对接
	public static final String OPEN_MTX = "MTX";			//开放类型：与满天星对接
	public static final String OPEN_DX = "DX";				//开放类型：与鼎新对接
	public static final String OPEN_WD = "WD";				//开放类型：与万达对接
	public static final String OPEN_VISTA = "VISTA";		//开放类型：与Vista对接
	public static final String OPEN_PNX = "PNX";			//开放类型：与东票对接
	public static final String OPEN_JY = "JY";				//开放类型：与Vista对接
	public static final String OPEN_DADI = "DADI";			//大地数字院线
	
	public static final List<String> OPEN_LOWEST_IS_COST = Arrays.asList(
			OpiConstant.OPEN_DADI, OpiConstant.OPEN_WD, OpiConstant.OPEN_VISTA,OpiConstant.OPEN_JY);//最低价即结算价的类型

	
	public static final String OPERATION_DISCARD = "discard";//废弃场次

	public static final String PAYOPTION = "payoption";						//支付选项
	public static final String PAYCMETHODLIST = "paymethodlist";			//支付方法
	public static final String CARDOPTION = "cardoption";					//券选项
	public static final String BATCHIDLIST = "batchidlist";					//批次id集合
	public static final String DEFAULTPAYMETHOD = "defaultpaymethod";		//默认支付方式
	public static final String MEALOPTION = "mealoption";					//套餐项
	public static final String ISREFUND = "isRefund";						//是否可以退票
	public static final String AUTO_OPEN_INFO = "autoOpen";					//自动开放
	public static final String AUTO_OPEN_INFO_STATUS = "autoOpenStatus";	//自动设置器的状态，手动还是自动。
	public static final String SMPNO = "smpno";								//特定的场次编号
	public static final String LYMOVIEIDS = "lymovieids";					//连映场次场次ids
	
	public static final String FROM_SPID = "fromSpid";						//从某种渠道下的订单
	public static final String ADDRESS = "address";							//地址必填
	public static final String UNOPENGEWA = "unopengewa";					//场次不对格瓦拉开放
	public static final String UNSHOWGEWA = "unshowgewa";					//场次不对格瓦拉显示
	public static final String STATISTICS = "statistics";					//座位已统计
	public static final String SEATYPE = "seattype";						//座位是否有单独价格
	
	public static final String MPI_OPENSTATUS_INIT = "init";
	public static final String MPI_OPENSTATUS_OPEN = "open";
	public static final String MPI_OPENSTATUS_CLOSE = "close";
	public static final String MPI_OPENSTATUS_DISABLED = "disabled";		//
	public static final String MPI_OPENSTATUS_PAST = "past";				//过期

	//座位图刷新频率
	public static final int SECONDS_SHOW_SEAT = 900;		//显示座位图，20分钟
	public static final int SECONDS_ADDORDER = 300;		//下单，5分钟
	public static final int SECONDS_UPDATE_SEAT = 60;		//更新，1分钟
	public static final int SECONDS_FORCEUPDATE_SEAT = 10;		//更新，10秒
	
	public static final int MAX_MINUTS_TICKETS = 15;		//电影票交易最大保留时间（分钟）
	public static final int MAX_MINUTS_TICKETS_MTX = 10; 	//满天星座位保留时间
	public static final int MAX_MINUTS_TICKETS_PNX = 5;		//票务系统座位保留时间
	
	public static final int MAXSEAT_PER_ORDER = 5;			//最大锁座数
	public static final int MAXSEAT_PER_ORDER_PNX = 4;		//最大锁座数（东票，满天星）
	
	
	public static final List<String> EDITIONS = Arrays.asList("2D","3D","IMAX2D","IMAX3D","双机3D","巨幕2D","巨幕3D", "4D");
	public static final List<String> EDITIONS_3D = Arrays.asList("3D", "IMAX3D", "双机3D","巨幕3D", "4D");
	public static final List<String> LANGUAGES = Arrays.asList(
			"国语","英语","粤语","法语","韩语","赛德克语","西班牙语","德语","俄语","日语",
			"泰语","意大利语","印度语","土耳其语","希腊语","波斯语","芬兰语","丹麦语",
			"荷兰语","葡萄牙语","波兰语","阿拉伯语","印尼语","乌克兰语","匈牙利语","马来语",
			"越南语","陕西话","闽南语","闽南话","巴西语","原版"/*TODO: remove*/);
	public static boolean isValidEdition(String edition){
		return StringUtils.isNotBlank(edition) && EDITIONS.contains(edition);
	}
	
	public static final Map<String, String> partnerTextMap;
	public static final Map<String, String> partnerFlagMap;
	public static final Map<String, String> takemethodMap;
	static{
		Map<String, String> tmp = new HashMap<String, String>();
		tmp.put(OPEN_HFH, "火凤凰");
		tmp.put(OPEN_MTX, "满天星");
		tmp.put(OPEN_DX, "鼎新");
		tmp.put(OPEN_WD, "万达");
		tmp.put(OPEN_VISTA, "Vista");
		tmp.put(OPEN_GEWARA, "格瓦拉");
		tmp.put(OPEN_PNX, "东票");
		tmp.put(OPEN_JY, "金逸");
		tmp.put(OPEN_DADI, "大地");
		partnerTextMap = MapUtils.unmodifiableMap(tmp);
		Map<String, String> tmpFlag = new HashMap<String, String>();
		tmpFlag.put(OPEN_HFH, "H");
		tmpFlag.put(OPEN_MTX, "M");
		tmpFlag.put(OPEN_DX, "DX");
		tmpFlag.put(OPEN_WD, "W");
		tmpFlag.put(OPEN_VISTA, "V");
		tmpFlag.put(OPEN_GEWARA, "G");
		tmpFlag.put(OPEN_PNX, "P");
		tmpFlag.put(OPEN_JY, "J");
		tmpFlag.put(OPEN_DADI, "DD");
		partnerFlagMap = MapUtils.unmodifiableMap(tmpFlag);
		Map<String, String> tmpTakemethod = new LinkedHashMap<String, String>();
		tmpTakemethod.put("P", "现场派送");
		tmpTakemethod.put("W", "影院售票窗口");
		tmpTakemethod.put("A", "格瓦拉取票机");
		tmpTakemethod.put("U", "联和院线自助取票机");
		tmpTakemethod.put("L", "卢米埃影院自助取票机");
		tmpTakemethod.put("D", "万达院线自助取票机");
		tmpTakemethod.put("J", "金逸院线自助取票机");
		tmpTakemethod.put("M", "影院会员自助取票机");
		takemethodMap = MapUtils.unmodifiableMap(tmpTakemethod);
	}
	
	public static boolean hasPartner(String opentype){
		if(StringUtils.equals(OPEN_GEWARA, opentype)) return false;
		if(StringUtils.isBlank(partnerTextMap.get(opentype))) return false;
		return true;
	}
	
	public static String getParnterText(String opentype){
		if(StringUtils.isBlank(opentype)) return "";
		String tmpText = partnerTextMap.get(opentype);
		if(StringUtils.isNotBlank(tmpText)) return tmpText;
		return "未知";
	}
	public static String getStatusStr(OpenPlayItem opi){
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		if(opi.getPlaytime().before(curtime)) return "本场次已经过时";

		String time = DateUtil.format(curtime, "HHmm");
		boolean open = opi.getOpentime().before(curtime) && opi.getClosetime().after(curtime) 
				&& opi.getStatus().equals(OpiConstant.STATUS_BOOK) 
				&& StringUtil.between(time, opi.getDayotime(), opi.getDayctime())
				&& opi.getGsellnum() < opi.getAsellnum();
		if(open) return "售票中";
		if(!opi.getStatus().equals(OpiConstant.STATUS_BOOK)) return "本场次暂不开放订票";
		if(opi.getOpentime().after(curtime)) return "本场次" + DateUtil.formatTimestamp(opi.getOpentime()) + "开放订票";
		if(opi.getClosetime().before(curtime)) return "本场次已关闭订票";
		if(opi.getGsellnum() >= opi.getAsellnum()) return "本场次座位已售完";
		if(!StringUtil.between(time, opi.getDayotime(), opi.getDayctime())) 
			return "本场次只在每天" + opi.getDayotime().substring(0,2) + ":" + opi.getDayotime().substring(2,4) + 
				"~" + opi.getDayctime().substring(0,2) + ":" + opi.getDayctime().substring(2,4) + "开放";
		return "未知";
	}
	public static String getUnbookingReason(OpenPlayItem opi){
		if(opi == null) return "开放场次不存在！";
		if(opi.isOrder()) return "";
		Timestamp curtime = new Timestamp(System.currentTimeMillis());
		if(opi.getPlaytime().before(curtime)) return "本场次已经过时";
		String time = DateUtil.format(curtime, "HHmm");
		if(!opi.getStatus().equals(OpiConstant.STATUS_BOOK)) return "本场次暂不开放订票";
		if(opi.getOpentime().after(curtime)) return "本场次" + DateUtil.formatTimestamp(opi.getOpentime()) + "开放订票";
		if(opi.getClosetime().before(curtime)) return "本场次已关闭订票";
		if(opi.getGsellnum() >= opi.getAsellnum()) return "本场次座位已售完";
		if(!StringUtil.between(time, opi.getDayotime(), opi.getDayctime())) 
			return "本场次只在每天" + opi.getDayotime().substring(0,2) + ":" + opi.getDayotime().substring(2,4) + 
				"~" + opi.getDayctime().substring(0,2) + ":" + opi.getDayctime().substring(2,4) + "开放";
		return "未知";
	}
	/**
	 * 与排片比较， 看看是否有差异
	 * @param mpi
	 * @return
	 */
	public static String getDifferent(OpenPlayItem opi, MoviePlayItem mpi){
		String msg = "";
		if(!opi.getMovieid().equals(mpi.getMovieid())){
			msg += ",movie:" + opi.getMovieid() + "--->" + mpi.getMovieid(); 
		}
		if(!opi.getRoomid().equals(mpi.getRoomid())){
			msg += ",room:" + opi.getRoomid() + "--->" + mpi.getRoomid(); 
		}
		if(!StringUtils.equals(opi.getRoomname(), mpi.getPlayroom())){
			msg += ",roomname:" + opi.getRoomname() + "--->" + mpi.getPlayroom(); 
		}
		String oplaydate = DateUtil.formatDate(opi.getPlaytime());
		String mplaydate = DateUtil.formatDate(mpi.getPlaydate());
		if(!oplaydate.equals(mplaydate)){
			msg += ",date:" + oplaydate + "--->" + mplaydate; 
		}
		if(!opi.getTimeStr().equals(mpi.getPlaytime())){
			msg += ",time:" + opi.getTimeStr() + "--->" + mpi.getPlaytime(); 
		}
		if(!StringUtils.equals(opi.getLanguage(), mpi.getLanguage())){
			msg += ",language:" + opi.getLanguage() + "--->" + mpi.getLanguage(); 
		}
		if(!StringUtils.equals(opi.getEdition(), mpi.getEdition())){
			msg += ",edition:" + opi.getEdition() + "--->" + mpi.getEdition(); 
		}
		if(opi.getPrice() == null || mpi.getPrice()==null){
			msg += ",price：" + opi.getPrice() + "--->" + mpi.getPrice();
		}else if(!opi.getPrice().equals(mpi.getPrice())){
			msg += ",price：" + opi.getPrice() + "--->" + mpi.getPrice();
		}
		if(opi.getLowest() != null && mpi.getLowest()!=null && !opi.getLowest().equals(mpi.getLowest())){
			msg += ",lowest：" + opi.getLowest() + "--->" + mpi.getLowest();
		}

		if(!opi.hasGewara()){
			if(!StringUtils.equals(opi.getSeqNo(), mpi.getSeqNo())){
				msg += "seqNo：" + opi.getSeqNo() + "--->" + mpi.getSeqNo();
			}
		}
		if(msg.length()>0) msg = msg.substring(1);
		return msg;
	}
	public static String getFullDesc(OpenPlayItem opi){
		return opi.getCinemaname() + " " + opi.getRoomname() + " " + opi.getMoviename() + DateUtil.format(opi.getPlaytime(), "MM月dd日 HH:mm") + " " + opi.getGewaprice();
	}
	/**
	 * 验证放映版本与影厅的是否匹配
	 * @param roomPlaytype
	 * @param opiEdition
	 * @return
	 */
	public static String validateRoomPlaytype(String roomPlaytype, String opiEdition){
		if(StringUtils.isBlank(roomPlaytype)) return "";
		if(opiEdition.equals("3D")){
			if(roomPlaytype.equals("3D")) return "";
		}else if(opiEdition.equals("IMAX")){
			if(roomPlaytype.equals("IMAX")) return "";
		}else{//2D
			if(roomPlaytype.equals("2D")) return "";
		}
		return "场次和影厅放映版本不匹配：" + roomPlaytype + "<---->" + opiEdition;
	}
	/**
	 * 下载到排片后该影厅的场次自动转换为对应的名称
	 * 如填写为双机3D，只要下载到的场次为3D，自动转换为双机3D，下载到位2D场次不转换。
	 * 如：巨幕2D，巨幕3D，下载到的2D场次自动转换为巨幕2D，3D场次自动转换为巨幕3D。
	 * @param synchEdition
	 * @param roomDefaultEdition
	 * @return
	 */
	public static String getDefaultEdition(String synchEdition,String roomDefaultEdition){
		if(StringUtils.isBlank(synchEdition) || StringUtils.isBlank(roomDefaultEdition)){
			return synchEdition;
		}
		String defaults[] = StringUtils.split(roomDefaultEdition, ",");
		for(String defaultEdition : defaults){
			if(defaultEdition.contains(synchEdition)){
				return defaultEdition;
			}
		}
		return synchEdition;
	}
	
	public static ErrorCode<Integer> getLowerPrice(String edition/*opi edition*/, MoviePrice mp,Timestamp playTime){
		if(StringUtils.isBlank(edition)){
			return ErrorCode.getFailure(mp.getMovieid() + "影片未设置最低票价");
		}
		if(mp.getStartTime() != null && mp.getEndTime() != null && mp.getStartTime().before(playTime) && mp.getEndTime().after(playTime)){
			if(EDITIONS_3D.contains(edition)){
				if(mp.getRangeEdition3D() == null) return ErrorCode.getFailure("基础数据3D价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getRangeEdition3D());
			}else if(StringUtils.indexOf(edition, "巨幕") != -1){
				if(mp.getRangeEditionJumu() == null) return ErrorCode.getFailure("基础数据巨幕价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getRangeEditionJumu());
			}else if(StringUtils.equals(edition, "IMAX")){
				if(mp.getRangeEditionIMAX() == null) return ErrorCode.getFailure("基础数据IMAX价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getRangeEditionIMAX());
			}
			return ErrorCode.getSuccessReturn(mp.getRangePrice());
		}else{
			if(EDITIONS_3D.contains(edition)){
				if(mp.getEdition3D() == null) return ErrorCode.getFailure("基础数据3D价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getEdition3D());
			}else if(StringUtils.indexOf(edition, "巨幕") != -1){
				if(mp.getEditionJumu() == null) return ErrorCode.getFailure("基础数据巨幕价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getEditionJumu());
			}else if(StringUtils.equals(edition, "IMAX")){
				if(mp.getEditionIMAX() == null) return ErrorCode.getFailure("基础数据IMAX价格不能为空！");
				return ErrorCode.getSuccessReturn(mp.getEditionIMAX());
			}
			return ErrorCode.getSuccessReturn(mp.getPrice());
		}
	}
	//用来记录最后一次更新
	public static String getLastChangeKey(Long mpid) {
		return "LastChange" + mpid;
	}
}
