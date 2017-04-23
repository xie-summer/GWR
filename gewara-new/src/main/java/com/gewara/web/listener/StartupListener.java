package com.gewara.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.gewara.Config;
import com.gewara.constant.AdminCityContant;
import com.gewara.model.common.GewaConfig;
import com.gewara.pay.ChinapayUtil;
import com.gewara.pay.NewPayUtil;
import com.gewara.pay.PayOtherUtil;
import com.gewara.pay.PayUtil;
import com.gewara.pay.UnionpayFastUtil;
import com.gewara.pay.UnionpayWalletUtil;
import com.gewara.service.DaoService;
import com.gewara.util.GewaLogger;
import com.gewara.util.IPUtil;
import com.gewara.util.LoggerUtils;
import com.gewara.util.ObjectId;
import com.gewara.util.VmUtils;
import com.gewara.web.action.inner.util.ActivityRemoteUtil;
import com.gewara.web.action.inner.util.GymRemoteUtil;
import com.gewara.web.support.ServletContextWrapper;

/**
 * <p>
 * StartupListener class used to initialize and database settings and populate
 * any application-wide drop-downs.
 * 
 * <p>
 * Keep in mind that this listener is executed outside of
 * OpenSessionInViewFilter, so if you're using Hibernate you'll have to
 * explicitly initialize all loaded data at the Dao or service level to avoid
 * LazyInitializationException. Hibernate.initialize() works well for doing
 * this.
 * 
 * @author <a href="mailto:acerge@163.com">gebiao(acerge)</a>
 * @since 2007-9-28下午02:05:17
 */
public class StartupListener extends ContextLoaderListener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContextWrapper context = new ServletContextWrapper(event.getServletContext());
		//根据环境加载配置文件
		String SPRING_CONFIG_KEY = "contextConfigLocation";
		String[] remoteConfig = new String[]{
				"classpath:spring/config.remote.xml",
				"classpath:spring/appContext-*.xml",
				"classpath:spring/camel.remote.xml",
				"classpath:spring/dubbo-gewara-provider.remote.xml"
		};
		String[] localConfig = new String[]{
				"classpath:spring/config.local.xml",
				"classpath:spring/appContext-*.xml"
		};

		String[] local173Config = new String[]{
				"classpath:spring/config.137.xml",
				"classpath:spring/appContext-*.xml",
				"classpath:spring/camel.137.xml",
				"classpath:spring/dubbo-gewara-provider.remote.xml"
		};
		
		String SPRING_CONFIG_VALUE = "";
		
		String ip = Config.getServerIp();
		String payother = "com/gewara/pay/payother.remote.properties";
		String chinapay = "com/gewara/pay/chinapay.remote.properties";
		String unionPayFast = "com/gewara/pay/unionpayFast.remote.properties";
		//TODO:removeFile
		//String cmwifi = "com/gewara/pay/cmwifi.remote.properties";
		String payurl = "com/gewara/pay/payurl.remote.properties";
		String newpay = "com/gewara/pay/pay.remote.properties";
		
		String unionpayWallet = "com/gewara/pay/unionpayWallet.remote.properties";
		String activityremote = "com/gewara/config/activityApi.properties";
		String gymremote = "com/gewara/config/gymApi.properties";
		GewaLogger logger = LoggerUtils.getLogger(StartupListener.class, Config.getServerIp(), Config.SYSTEMID);
		if(ip.startsWith("172.22.1")){//remote
			if(ip.startsWith("172.22.1.37") ){//远程测试,先写死
				System.setProperty(Config.SYSTEMID + "-GEWACONFIG", "REMOTE-TEST");
				SPRING_CONFIG_VALUE = StringUtils.join(local173Config, ",");
				logger.warn("Config Using REMOTE-TEST:" + SPRING_CONFIG_VALUE);
				
				payother = "com/gewara/pay/payother.137.properties";
				logger.warn("Payother Using LOCAL:" + payother);
			}else{
				System.setProperty(Config.SYSTEMID + "-GEWACONFIG", "REMOTE");
				SPRING_CONFIG_VALUE = StringUtils.join(remoteConfig, ",");
				logger.warn("Config Using REMOTE:" + SPRING_CONFIG_VALUE);
			}
		}else{
			System.setProperty(Config.SYSTEMID + "-GEWACONFIG", "LOCAL");
			SPRING_CONFIG_VALUE = StringUtils.join(localConfig, ",");
			
			logger.warn("Config Using LOCAL:" + SPRING_CONFIG_VALUE);
			payother = "com/gewara/pay/payother.local.properties";
			logger.warn("Payother Using LOCAL:" + payother);
			
			chinapay = "com/gewara/pay/chinapay.local.properties";
			logger.warn("Chinapay Using LOCAL:" + chinapay);
			payurl = "com/gewara/pay/payurl.local.properties";
			logger.warn("Payurl Using LOCAL:" + payurl);
			newpay = "com/gewara/pay/pay.local.properties";
			logger.warn("NewPayUtil Using LOCAL:" + payurl);
			unionpayWallet = "com/gewara/pay/unionpayWallet.local.properties";
			logger.warn("UnionpayWalletUtil Using LOCAL:" + unionpayWallet);

			unionPayFast = "com/gewara/pay/unionpayFast.local.properties";
			logger.warn("unionPay2 Using LOCAL:" + unionPayFast);
		}
		//FIXME:所有线上正式环境密钥统一保存在线上环境一个地方，所有支付相关都移走！！
		PayOtherUtil.init(payother);
		ChinapayUtil.init(chinapay);
		UnionpayFastUtil.init(unionPayFast);
		PayUtil.init(payurl);
		NewPayUtil.init(newpay);
		UnionpayWalletUtil.init(unionpayWallet);
		
		ActivityRemoteUtil.init(activityremote);
		GymRemoteUtil.init(gymremote);
		
		IPUtil.initAll();

		context.setInitParams(SPRING_CONFIG_KEY, SPRING_CONFIG_VALUE);
		ServletContextEvent wrapperEvent = new ServletContextEvent(context);
		super.contextInitialized(wrapperEvent);
		
		//初始化其他信息
		WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(context);
		Config config = ctx.getBean(Config.class);
		DaoService daoService = ctx.getBean(DaoService.class);
		GewaConfig gc = daoService.getObject(GewaConfig.class, config.getLong("imgServer"));
		VmUtils utils = new VmUtils();
		utils.initImg(gc, config.getString("picPath"));
		config.replacePageTool("VmUtils", utils);
		AdminCityContant adminCityContant = new AdminCityContant();
		config.replacePageTool("AdminCityContant", adminCityContant);
		logger.warn("INIT GLOCAL PAGE-TOOLS: " + Config.getPageTools());
		logger.warn("INIT OBJECTID:" + ObjectId.get().toString());
	}
}
