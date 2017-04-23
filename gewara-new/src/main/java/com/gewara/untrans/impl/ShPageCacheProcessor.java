package com.gewara.untrans.impl;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.gewara.constant.sys.ConfigConstant;
import com.gewara.jms.JmsConstant;
import com.gewara.model.common.GewaConfig;
import com.gewara.service.DaoService;
import com.gewara.untrans.JmsService;
import com.gewara.untrans.PageCacheProcessor;
import com.gewara.util.IPUtil;
import com.gewara.util.WebUtils;

@Service("pageCacheProcessor")
public class ShPageCacheProcessor implements PageCacheProcessor{
	@Autowired@Qualifier("jmsService")
	private JmsService jmsService;
	public void setJmsService(JmsService jmsService) {
		this.jmsService = jmsService;
	}
	@Autowired@Qualifier("daoService")
	private DaoService daoService;
	public void setDaoService(DaoService daoService) {
		this.daoService = daoService;
	}
	@Override
	public void sendMsgToDst(Map<String, String> params) {
		jmsService.sendMsgToDst(JmsConstant.QUEUE_UPDATECACHE, JmsConstant.TAG_UPADATE_PAGE_CACHE, params);
	}
	@Override
	public String getKeyPre() {
		GewaConfig cfg = daoService.getObject(GewaConfig.class, ConfigConstant.CFG_PAGECACHE_VERSION);
		String keyPre = cfg.getContent();
		return keyPre;
	}
	@Override
	public boolean canClear(HttpServletRequest request) {
		String ip = WebUtils.getRemoteIp(request);
		return IPUtil.isInnerIp(ip);
	}
}
