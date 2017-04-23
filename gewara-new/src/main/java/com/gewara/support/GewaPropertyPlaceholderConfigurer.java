package com.gewara.support;

import java.util.Properties;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import com.gewara.Config;

public class GewaPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
	private static final String LOCAL_SERVER_IP = "local.server.ip";

	@Override
	protected void convertProperties(Properties props) {
		props.setProperty(LOCAL_SERVER_IP, Config.getServerIp());
		super.convertProperties(props);
	}
	
	
}
