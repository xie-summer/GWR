package com.gewara.untrans.impl;

import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.WebServiceIOException;
import org.springframework.ws.client.core.SourceExtractor;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.SoapMessage;

import com.gewara.Config;
import com.gewara.constant.ApiConstant;
import com.gewara.support.ErrorCode;
import com.gewara.support.GewaObjectStringConverter;
import com.gewara.untrans.monitor.MonitorService;
import com.gewara.util.GewaLogger;
import com.gewara.util.LoggerUtils;
import com.gewara.util.StringUtil;

public class BaseWebService {
	@Autowired@Qualifier("monitorService")
	protected MonitorService monitorService;
	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}
	protected final transient GewaLogger dbLogger = LoggerUtils.getLogger(getClass(), Config.getServerIp(), Config.SYSTEMID);
	protected ErrorCode<String> sendMessage(WebServiceTemplate template, String msgname, String msgbody){
		try{
			String result = sendMessageWithExcption(template, msgname, msgbody);
			return ErrorCode.getSuccessReturn(result);
		}catch(WebServiceIOException e){
			dbLogger.error(e.getClass().getCanonicalName() + ":" + e.getMessage() +  msgname + msgbody);
			return ErrorCode.getFailure(ApiConstant.CODE_CONNECTION_ERROR, "系统有错误" + e.getMessage());
		}catch(Exception e){
			dbLogger.error(msgname + msgbody, e);
			return ErrorCode.getFailure(ApiConstant.CODE_UNKNOWN_ERROR, "系统有错误" + e.getMessage());
		}finally{
			monitorService.incrementCallCount(MonitorService.PRE_WEBSERVICE + msgname);
		}
	}
	protected ErrorCode<String> sendMessage(WebServiceTemplate template, String namespace, String msgname, String msgbody){
		String soapAction = namespace + msgname;
		return sendMessage(template, namespace, soapAction, msgname, msgbody);
	}
	protected ErrorCode<String> sendMessage(WebServiceTemplate template, String namespace, String soapAction, String msgname, String msgbody){
		try{
			String result = sendMessageWithExcption(template, namespace, soapAction, msgname, msgbody);
			return ErrorCode.getSuccessReturn(result);
		}catch(WebServiceIOException e){
			String msg = msgname + "" + e.getClass().getCanonicalName() + ":" + e.getMessage();
			dbLogger.error(msg + msgbody);
			return ErrorCode.getFailure(ApiConstant.CODE_CONNECTION_ERROR, msg);
		}catch(Exception e){
			String msg = msgname + ":" + e.getClass().getCanonicalName() + ":" + e.getMessage();
			dbLogger.error(msg + msgbody, e);
			return ErrorCode.getFailure(ApiConstant.CODE_UNKNOWN_ERROR, msg);
		}finally{
			monitorService.incrementCallCount(MonitorService.PRE_WEBSERVICE + msgname);
		}
	}
	protected ErrorCode<String> sendMessage(WebServiceTemplate template, String namespace, WebServiceMessageCallback callback, String msgname, String msgbody){
		try{
			String result = sendMessageWithExcption(template, namespace, callback, msgname, msgbody);
			return ErrorCode.getSuccessReturn(result);
		}catch(WebServiceIOException e){
			String msg = msgname + "" + e.getClass().getCanonicalName() + ":" + e.getMessage();
			dbLogger.error(msg + msgbody);
			return ErrorCode.getFailure(ApiConstant.CODE_CONNECTION_ERROR, msg);
		}catch(Exception e){
			String msg = msgname + "" + e.getClass().getCanonicalName() + ":" + e.getMessage();
			dbLogger.error(msg + msgbody, e);
			return ErrorCode.getFailure(ApiConstant.CODE_UNKNOWN_ERROR, msg);
		}finally{
			monitorService.incrementCallCount(MonitorService.PRE_WEBSERVICE + msgname);
		}
	}
	private String sendMessageWithExcption(WebServiceTemplate template, String msgname, String msgbody){
		String msg = "<" + msgname + ">" + msgbody + "</" + msgname + ">";
		StreamSource source = new StreamSource(new StringReader(msg));
		XmlObject result = (XmlObject) template.sendSourceAndReceive(source, new MySourceExtractor(template));
		XmlCursor cursor = result.newCursor();
		String text = cursor.getTextValue();
		if(StringUtils.contains(text, "amp")){
			text = StringUtil.substitute(text, "&(?!amp;)","&amp;", true);
		}
		return text.trim();
	}
	private String sendMessageWithExcption(WebServiceTemplate template, String namespace, String soapAction, String msgname, String msgbody){
		if(StringUtils.isNotBlank(namespace)){
			WebServiceMessageCallback callback = new MyWebServiceMessageCallback(soapAction);
			return sendMessageWithExcption(template, namespace, callback, msgname, msgbody);
		}else{
			return sendMessageWithExcption(template, msgname, msgbody);
		}
	}
	private String sendMessageWithExcption(WebServiceTemplate template, String namespace, WebServiceMessageCallback callback, String msgname, String msgbody){
		String msg = "<" + msgname ;
		if(StringUtils.isNotBlank(namespace)) msg += " xmlns=\"" + namespace + "\"";
		msg += ">" + msgbody + "</" + msgname + ">";
		StreamSource source = new StreamSource(new StringReader(msg));
		XmlObject result = (XmlObject) template.sendSourceAndReceive(source, callback, new MySourceExtractor(template));
		XmlCursor cursor = result.newCursor();
		String text = cursor.getTextValue();
		return text.trim();
	}
	public class  MySourceExtractor implements SourceExtractor{
		private WebServiceTemplate template;
		public MySourceExtractor(WebServiceTemplate template){
			this.template = template;
		}
		public Object extractData(Source source) throws IOException, TransformerException {
			return template.getUnmarshaller().unmarshal(source);
		}
	}
	protected String surround(String elname, String value){
		return "<" + elname + ">" + StringEscapeUtils.escapeXml(value) + "</" + elname + ">";
	}
	protected BeanReader getBeanReader(String nodeName, Class clazz){
		BeanReader beanReader = new BeanReader();
		beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(true);
		beanReader.getBindingConfiguration().setMapIDs(false);
		beanReader.getBindingConfiguration().setObjectStringConverter(new GewaObjectStringConverter());
		try {
			beanReader.registerBeanClass(nodeName, clazz);
		} catch (IntrospectionException e) {
		}
		return beanReader;
	}
	protected BeanReader addNodeMapping(BeanReader reader, String nodeName, Class clazz){
		try {
			reader.registerBeanClass(nodeName, clazz);
		} catch (IntrospectionException e) {
		}
		return reader;
	}
	protected Object xml2Object(BeanReader reader, String xml){
		StringReader xmlReader = new StringReader(xml);
		try {
			Object result = reader.parse(xmlReader);
			return result;
		} catch (Exception e) {
			dbLogger.error("错误：" + xml + StringUtil.getExceptionTrace(e, 10));
		}
		return null;
	}
	public static class MyWebServiceMessageCallback implements WebServiceMessageCallback{
		private String soapAction = "";
		public MyWebServiceMessageCallback(String soapAction){
			this.soapAction = soapAction;
		}
		@Override
		public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
			((SoapMessage)message).setSoapAction(soapAction);
		}
	}
}
