package com.gewara.untrans.impl;

import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;

import com.gewara.bank.OrderQuery;
import com.gewara.bank.SpsdoOrderQry;
import com.gewara.constant.sys.LogTypeConstant;
import com.gewara.model.pay.GewaOrder;
import com.gewara.pay.SandPayUtil;
import com.gewara.pay.SpSdoUtil;
import com.gewara.pay.UnionpayWalletUtil;
import com.gewara.untrans.PartnerWebService;
import com.gewara.util.ApiUtils;
import com.gewara.util.CAUtil;
import com.gewara.util.JsonUtils;
import com.gewara.util.StringUtil;
import com.gewara.xmlbind.partner.UnionpayWalletResponse;
import com.gewara.xmlbind.pay.QrySandOrder;

/**
 * 注意一下几点：
 * 1）checkValue参数的顺序要同文档中的顺序保持一致
 * @author acerge(acerge@163.com)
 * @since 3:07:17 PM Dec 23, 2009
 */
@Service("partnerWebService")
public class PartnerWebServiceImpl extends BaseWebService implements PartnerWebService{
	@Autowired@Qualifier("spsdoTemplate")
	private WebServiceTemplate spsdoTemplate;
	public void setSpsdoTemplate(WebServiceTemplate spsdoTemplate) {
		this.spsdoTemplate = spsdoTemplate;
	}
	@Autowired@Qualifier("sandTemplate")
	private WebServiceTemplate sandTemplate;
	
	@Autowired@Qualifier("chianpayWalletPushOrderTemplate")
	private WebServiceTemplate chianpayWalletPushOrderTemplate;
	
	@Override
	public boolean pushOrderToUnionpayWallet(String transSeq,String cdhdUsrId,String billId,int num){
		if(StringUtils.isBlank(cdhdUsrId) || StringUtils.isBlank(billId)){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, "数据为空：cdhdUsrId" + cdhdUsrId + ":billId" + billId);
			return false;
		}
		Map data = new LinkedHashMap();
		data.put("transSeq", transSeq);
		data.put("cdhdUsrId", cdhdUsrId);
		data.put("usageIn", "1");
		data.put("couponId", billId);
		data.put("couponNum", num + "");
		data.put("bindFlag", "0");
		data.put("couponSceneId", "000");
		String sign = CAUtil.doSign(JsonUtils.writeMapToJson(data),UnionpayWalletUtil.UNIONPAY_WALLET_WEBSERVICE_SIGN_PRIVATEKEY, "UTF-8", CAUtil.SHA1WithRSA);
		String msg = "";
		try {
			msg = "<billdwnsvcWS xmlns=\"urn:pack.outersvc_typedef.salt11\"><inbuf>{\"venderId\":\"" + UnionpayWalletUtil.UNIONPAY_WALLET_VENDERID + "\",\"onlTransPwd\":\"" + UnionpayWalletUtil.UNIONPAY_WALLET_WEBSERVICE_PASSWORD + "\",\"data\":" + 
				JsonUtils.writeMapToJson(data) + ",\"signToken\":\"" + UnionpayWalletUtil.byteArr2HexStr(Base64.decodeBase64(sign)) + "\"}</inbuf></billdwnsvcWS>";
		} catch (Exception e) {
			e.printStackTrace();
		}
		StreamSource source = new StreamSource(new StringReader(msg));
		WebServiceMessageCallback callback = new MyWebServiceMessageCallback("urn:pack.outersvc_typedef.salt11/billdwnsvcWS");
		XmlObject result = null;
		try {
			result = (XmlObject) chianpayWalletPushOrderTemplate.sendSourceAndReceive(source, callback, new MySourceExtractor(chianpayWalletPushOrderTemplate));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if(result == null){
			return false;
		}
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, "推送订单到银联钱包" + result.toString());
		BeanReader beanReader = ApiUtils.getBeanReader("tuxedo:billdwnsvcWSResponse", UnionpayWalletResponse.class);
		UnionpayWalletResponse response = (UnionpayWalletResponse)ApiUtils.xml2Object(beanReader, result.toString());
		if(response != null && StringUtils.isNotBlank(response.getResult())){
			Map map = JsonUtils.readJsonToMap(response.getResult());
			// 20130828 银联钱包要求也支持0000的成功应答码
			if(map.get("respCd") != null && (StringUtils.equals(map.get("respCd").toString(), "0000") || StringUtils.equals(map.get("respCd").toString(), "000000"))){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String qrySpsdoOrder(GewaOrder order){
		OrderQuery query = SpSdoUtil.qryOrder(order);
		String msgbody = "<request>";
		msgbody += surround("MerchantNo", query.getMerchantNo());
		msgbody += surround("SignType", query.getSignType()+"");
		msgbody += surround("Mac", query.getMac());
		msgbody += surround("OrderNo", query.getOrderNo()) + "</request>";
		
		String namespace = "http://tempuri.org/";
		String soapAction = "http://tempuri.org/OrderQuery";
		String msgname = "OrderQuery";
		WebServiceMessageCallback callback = new MyWebServiceMessageCallback(soapAction);
		String msg = "<" + msgname ;
		if(StringUtils.isNotBlank(namespace)) msg += " xmlns=\"" + namespace + "\"";
		msg += ">" + msgbody + "</" + msgname + ">";
		StreamSource source = new StreamSource(new StringReader(msg));
		XmlObject result = (XmlObject) spsdoTemplate.sendSourceAndReceive(source, callback, new MySourceExtractor(spsdoTemplate));
		return result.toString();
	}
	@Override
	public SpsdoOrderQry qrySpsdoOrder(String response){
		BeanReader reader = getBeanReader("OrderQueryResponse", SpsdoOrderQry.class);
		SpsdoOrderQry result = (SpsdoOrderQry)xml2Object(reader, response);
		return result;
	}
	@Override
	public QrySandOrder qrySandOrder(GewaOrder order){
		String msgbody = SandPayUtil.getQryOrderMsg(order);
		String response = sendMessageSand(sandTemplate, "http://impl.gwla.service.sandmall.sand.com", "getOrderByquery", msgbody);
		response = response.replace("<ns:getOrderByqueryResponse xmlns:ns=\"http://impl.gwla.service.sandmall.sand.com\">", "")
		.replace("</ns:getOrderByqueryResponse>", "").replaceAll("ax21:", "").replaceAll("ns:return", "GwalResults");
		if(StringUtils.isBlank(response) || StringUtils.indexOfAny(response, "<>") < 0){
			dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, ""+response);
			return null;
		}
		dbLogger.errorWithType(LogTypeConstant.LOG_TYPE_PARTNER, ""+response);
		BeanReader reader = getBeanReader("GwalResults", QrySandOrder.class);
		QrySandOrder result = (QrySandOrder)xml2Object(reader, response);
		return result;
	}
	/**
	 * 山德合作
	 * @param template
	 * @param namespace
	 * @param msgname
	 * @param msgbody
	 * @return
	 */
	private String sendMessageSand(WebServiceTemplate template, String namespace, String msgname, String msgbody){
		try {
			String soapAction = namespace + msgname;
			WebServiceMessageCallback callback = new MyWebServiceMessageCallback(soapAction);
			String msg = "<" + msgname ;
			if(StringUtils.isNotBlank(namespace)) msg += " xmlns=\"" + namespace + "\"";
			msg += ">" + msgbody + "</" + msgname + ">";
			StreamSource source = new StreamSource(new StringReader(msg));
			XmlObject result = (XmlObject) template.sendSourceAndReceive(source, callback, new MySourceExtractor(template));
			return result.toString();
		} catch (Exception e) {
			dbLogger.error(msgname + msgbody + StringUtil.getExceptionTrace(e, 15));
		}
		return null;
	}
}