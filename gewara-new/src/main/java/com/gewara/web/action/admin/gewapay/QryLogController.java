package com.gewara.web.action.admin.gewapay;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.UnmodifiableMap;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;
import com.gewara.util.StringUtil;
import com.gewara.util.VmUtils;
import com.gewara.web.action.admin.BaseAdminController;
@Controller
public class QryLogController extends BaseAdminController{
	public static final transient Map<String, String> codeMap;
	static{
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("pnr600", "交易成功");
		tmpMap.put("pnr601", "版本号错误");
		tmpMap.put("pnr602", "商户号格式错误");
		tmpMap.put("pnr603", "商户日期格式错误");
		tmpMap.put("pnr604", "订单号格式错误");
		tmpMap.put("pnr605", "交易金额格式错误");
		tmpMap.put("pnr606", "网关号格式错误");
		tmpMap.put("pnr607", "签名信息格式错误");
		tmpMap.put("pnr608", "网关号在黑名单");
		tmpMap.put("pnr609", "网关不在网关列表中");
		tmpMap.put("pnr610", "金额超过或小于限额");
		tmpMap.put("pnr613", "系统错误");
		tmpMap.put("pnr614", "非法商户号");
		tmpMap.put("pnr615", "商户号已关闭");
		tmpMap.put("pnr616", "非法网关号");
		tmpMap.put("pnr617", "网关号已关闭");
		tmpMap.put("pnr619", "无对应原始交易记录");
		tmpMap.put("pnr620", "原交易失败");
		tmpMap.put("pnr621", "交易金额错误");
		tmpMap.put("pnr628", "数据操作错误");
		tmpMap.put("pnr629", "交易状态错误");
		tmpMap.put("pnr631", "卡信息错误");
		tmpMap.put("pnr632", "回调地址不能为空");
		tmpMap.put("pnr633", "卡BIN错误");
		tmpMap.put("pnr634", "有效期格式错误");
		tmpMap.put("pnr635", "CVV2格式错误");
		tmpMap.put("pnr636", "证件类型格式错误");
		tmpMap.put("pnr637", "证件类型不能为空");
		tmpMap.put("pnr638", "证件号码格式错误");
		tmpMap.put("pnr639", "姓名长度超过限制");
		tmpMap.put("pnr640", "姓名不能为空");
		tmpMap.put("pnr641", "验签名失败");
		tmpMap.put("pnr642", "发送或接收交易数据失败");
		tmpMap.put("pnr643", "原始交易商户日期格式错误");
		tmpMap.put("pnr644", "原始交易订单号格式错误");
		tmpMap.put("pnr645", "交易类型错误");
		tmpMap.put("pnr646", "请求参数异常");
		tmpMap.put("pnr647", "重复退款");
		tmpMap.put("pnr648", "签名失败");
		tmpMap.put("pnr649", "卡信息解密失败");
		tmpMap.put("pnr001", "支付信息错误");
		tmpMap.put("pnr002", "无效卡号或账户");
		tmpMap.put("pnr003", "余额或信用不足");
		tmpMap.put("pnr004", "卡有效期错误");
		tmpMap.put("pnr005", "交易取消");
		tmpMap.put("pnr006", "数据接收错误");
		tmpMap.put("pnr007", "交易超时");
		tmpMap.put("pnr008", "超限额");
		tmpMap.put("pnr009", "非本行卡");
		tmpMap.put("pnr010", "电话授权忙音");
		tmpMap.put("pnr011", "授权次数或使用次数超限");
		tmpMap.put("pnr012", "交易失败");
		tmpMap.put("pnr013", "证件号码不符");
		tmpMap.put("pnr014", "户名不符");
		tmpMap.put("pnr015", "查发卡行");
		tmpMap.put("pnr016", "无效CVV2");
		tmpMap.put("pnr017", "无效商户");
		tmpMap.put("pnr018", "过期的卡");
		tmpMap.put("pnr019", "重复交易");
		tmpMap.put("pnr020", "挂失卡");
		tmpMap.put("pnr021", "被窃卡");
		tmpMap.put("pnr022", "该卡未启用");
		tmpMap.put("pnr023", "假卡");
		tmpMap.put("pnr098", "原始交易数据中缺少有效期或缺少卡号");
		tmpMap.put("pnr099", "其他");
		tmpMap.put("pnr688", "银行交易失败");
		tmpMap.put("pnr689", "银行批结不受理交易");
		tmpMap.put("pnr6AO", "没收卡");
		tmpMap.put("pnr6A1", "不予承兑");
		tmpMap.put("pnr6A2", "无效交易");
		tmpMap.put("pnr6A3", "无效金额");
		tmpMap.put("pnr6A4", "无此发卡方");
		tmpMap.put("pnr6A5", "交易数据或格式错误");
		tmpMap.put("pnr6A6", "受限制的卡");
		tmpMap.put("pnr6A7", "无此卡或无此账户");
		tmpMap.put("pnr6A8", "发卡方或交换中心不能操作");
		tmpMap.put("pnr6A9", "可疑交易");
		tmpMap.put("pnr6AA", "银行日切中");
		tmpMap.put("pnr6AB", "请求正在处理中");
		tmpMap.put("pnr6AC", "银联不支持的银行");
		tmpMap.put("pnr6AD", "超过允许的PIN试输入");
		tmpMap.put("pnr6AE", "不正确的PIN");
		tmpMap.put("pnr6AF", "超出取款次数限制");
		tmpMap.put("pnr6AG", "金融机构或中间网络设施找不到或无法达到");
		tmpMap.put("pnr6AH", "交换中心收不到发卡行应答");
		tmpMap.put("pnr6AI", "MAC校验错");
		tmpMap.put("pnr6AJ", "币种信息不合法");
		tmpMap.put("pnr6AK", "证件号码不能为空");
		tmpMap.put("pnr6AL", "当日不能进行部分消费撤销");
		tmpMap.put("pnr6AM", "隔日不能进行消费撤销");
		tmpMap.put("pnr6AN", "超出退货有效期,不能退货");
		tmpMap.put("pnr6AP", "同一日不能进行隔日退货处理");
		tmpMap.put("pnr6AQ", "密码不正确");
		tmpMap.put("pnr6AR", "地址格式错误");
		tmpMap.put("pnr6AS", "出生日期不正确");
		tmpMap.put("pnr6AT", "累计退款金额超出原交易金额");
		tmpMap.put("pnr6AU", "账户或卡状态不正常");
		tmpMap.put("pnr6AV", "止付卡");
		tmpMap.put("pnr6AW", "已经对账,不允许冲正");
		tmpMap.put("pnr6AX", "请重新送入交易");
		tmpMap.put("pnr6AY", "请联系收单行手工退货");
		tmpMap.put("pnr6AZ", "不支持该卡种");
		codeMap = UnmodifiableMap.decorate(tmpMap);
	}
	//TODO:相关日志写入HBase
	@RequestMapping("/admin/common/qryLog.xhtml")
	public String qryLog(String tradeno, ModelMap model){
		String url1 = "http://180.153.146.139:8080/log/domain2.log";
		String url2 = "http://180.153.146.140:8080/log/domain2.log";
		List<String> resList = new ArrayList<String>();
		resList.addAll(qryLogList(tradeno, url1));
		resList.addAll(qryLogList(tradeno, url2));
		return forwardMessage(model, resList);
	}
	private List<String> qryLogList(String tradeno, String url){
		HttpResult code =  HttpUtils.postUrlAsString(url, null);
		List<String> resList = new ArrayList<String>();
		if(code.isSuccess()){
			String result = code.getResponse();
			String reg = "\"OrdId\":\""+tradeno+"\",\"Pid\":\"\",\"RespCode\":\"pnr(.*?)\"";
			List<String> strList = StringUtil.findByRegex(result, reg, false);
			for(String str : strList){
				String json = "{" + str + "}";
				Map<String, String> tmp = VmUtils.readJsonToMap(json);
				String response = "订单号："+tmp.get("OrdId");
				response = response + ", 失败原因：" + codeMap.get(tmp.get("RespCode"));
				resList.add(response);
			}
		}
		return resList;
	}
}
