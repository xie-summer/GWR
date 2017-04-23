package com.gewara.json.mobile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;

import com.gewara.constant.AdminCityContant;
import com.gewara.util.DateUtil;

/**
 * 手机客户端抢票活动
 * 
 * @author taiqichao
 * 
 */
public class MobileGrabTicketEvent implements Serializable {

	private static final long serialVersionUID = 1L;

	public static String GRAB_STATUS_O = "open";// 开启
	public static String GRAB_STATUS_C = "close";// 关闭
	public static String GRAB_STATUS_BOOKED="booked";//名额已满
	
	private String id;
	private String title;// 抢票活动标题
	private String status;// 状态 open,close
	private String starttime;// 开抢时间
	private String citycode;// 城市
	private Integer price;// 秒杀价
	private String addtime;// 添加时间
	private String updatetime;// 最后修改时间

	public MobileGrabTicketEvent() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStarttime() {
		return starttime;
	}

	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}

	public String getCitycode() {
		return citycode;
	}

	public void setCitycode(String citycode) {
		this.citycode = citycode;
	}

	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	public String getAddtime() {
		return addtime;
	}

	public void setAddtime(String addtime) {
		this.addtime = addtime;
	}

	public String getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(String updatetime) {
		this.updatetime = updatetime;
	}

	public String getCitycodeList() {
		List<String> codes = new ArrayList<String>();
		if (StringUtils.isNotBlank(this.getCitycode())) {
			String[] citys = getCitycode().split(",");
			for (int i = 0; i < citys.length; i++) {
				codes.add(citys[i]);
			}
		}
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(codes);
		} catch (Exception e) {
			return "[]";
		}
	}
	
	public String getCityStr() {
		StringBuilder strBuilder=new StringBuilder();
		if (StringUtils.isNotBlank(this.getCitycode())) {
			String[] citys = getCitycode().split(",");
			for (int i = 0; i < citys.length; i++) {
				strBuilder.append(AdminCityContant.allcityMap.get(citys[i])+",");
			}
		}
		return StringUtils.removeEnd(strBuilder.toString(), ",");
	}

	public String getGrabStatus() {
		Date cur = DateUtil.getCurFullTimestamp();
		Date startTime = DateUtil.parseDate(this.starttime,
				"yyyy-MM-dd HH:mm:ss");
		if (this.status.equals(GRAB_STATUS_O)) {
			if (cur.before(startTime)) {
				return "未开始";
			} else {
				return "进行中";
			}
		} else if(this.status.equals(GRAB_STATUS_BOOKED)){
			return "名额已满";
		}else{
			return "已关闭";
		}
	}
	
	public Long getLeftms(){
		Date cur = DateUtil.getCurFullTimestamp();
		Date startTime = DateUtil.parseDate(this.starttime,"yyyy-MM-dd HH:mm:ss");
		Long mod=startTime.getTime()-cur.getTime();
		if(mod<0){
			mod=0L;
		}
		return mod;
	}
	

}
