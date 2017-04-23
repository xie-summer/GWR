package com.gewara.web.support;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.gewara.util.HttpResult;
import com.gewara.util.HttpUtils;

public class WindowsLiveLogin {
	public static class WLLException extends RuntimeException {
		private static final long serialVersionUID = -788698919921350955L;

		public WLLException(String s) {
			super(s);
		}
	}

	private static void fatal(String s) {
		throw new WLLException(s);
	}

	public WindowsLiveLogin(String appId, String secret) {
		this(appId, secret, null);
	}

	public WindowsLiveLogin(String appId, String secret, String securityAlgorithm) {
		this(appId, secret, securityAlgorithm, false);
	}

	public WindowsLiveLogin(boolean forceDelAuthNonProvisioned, String policyUrl, String returnUrl) {
		setForceDelAuthNonProvisioned(forceDelAuthNonProvisioned);
		setPolicyUrl(policyUrl);
		setReturnUrl(returnUrl);
	}

	public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned) {
		this(appId, secret, securityAlgorithm, forceDelAuthNonProvisioned, null);
	}

	public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned, String policyUrl) {
		this(appId, secret, securityAlgorithm, forceDelAuthNonProvisioned, policyUrl, null);
	}

	public WindowsLiveLogin(String appId, String secret, String securityAlgorithm, boolean forceDelAuthNonProvisioned, String policyUrl,
			String returnUrl) {
		setForceDelAuthNonProvisioned(forceDelAuthNonProvisioned);
		setAppId(appId);
		setSecret(secret);
		setSecurityAlgorithm(securityAlgorithm);
		setPolicyUrl(policyUrl);
		setReturnUrl(returnUrl);
	}

	public WindowsLiveLogin(String settingsFile) {
		Map<String, String> settings = parseSettings(settingsFile);
		if ("true".equals(settings.get("force_delauth_nonprovisioned"))) {
			setForceDelAuthNonProvisioned(true);
		} else {
			setForceDelAuthNonProvisioned(false);
		}

		setAppId(settings.get("appid"));
		setSecret(settings.get("secret"));
		setOldSecret(settings.get("oldsecret"));
		setOldSecretExpiry(settings.get("oldsecretexpiry"));
		setSecurityAlgorithm(settings.get("securityalgorithm"));
		setPolicyUrl(settings.get("policyurl"));
		// 自定义返回地址
		setReturnUrl(settings.get("returnurl"));
		setBaseUrl(settings.get("baseurl"));
		setSecureUrl(settings.get("secureurl"));
		setConsentBaseUrl(settings.get("consenturl"));
	}

	public WindowsLiveLogin() {
	}

	private String appId;
	private String baseUrl;
	private String consentUrl;
	private byte[] cryptKey;
	private byte[] signKey;
	private boolean forceDelAuthNonProvisioned = false;
	private byte[] oldCryptKey;
	private byte[] oldSignKey;
	private Date oldSecretExpiry;
	private String returnUrl;
	private String securityAlgorithm;
	private String policyUrl;
	private String secureUrl;

	public void setAppId(String appId) {
		if (isVoid(appId)) {
			if (forceDelAuthNonProvisioned) {
				return;
			}
			fatal("Error: setAppId: Attempt to set null application ID.");
		}
		Pattern p = Pattern.compile("^\\w+$");
		Matcher m = p.matcher(appId);
		if (!m.matches()) {
			fatal("Error: setAppId: Application ID must be alphanumeric: " + appId);
		}
		this.appId = appId;
	}

	public void validateAppId() {
		if (isVoid(appId)) {
			fatal("Error: getAppId: Application ID was not set. Aborting.");
		}
	}


	public void setSecret(String secret) {
		if (isVoid(secret) || secret.length() < 16) {
			if (forceDelAuthNonProvisioned) {
				return;
			}

			fatal("Error: setSecret: Secret key is expected to be non-null and longer than 16 characters.");
		}

		signKey = derive(secret, "SIGNATURE");
		cryptKey = derive(secret, "ENCRYPTION");
	}


	public void setOldSecret(String secret) {
		if (isVoid(secret)) {
			return;
		}

		if (secret.length() < 16) {
			fatal("Error: setOldSecret: Secret key is expected to be non-null and longer than 16 characters.");
		}

		oldSignKey = derive(secret, "SIGNATURE");
		oldCryptKey = derive(secret, "ENCRYPTION");
	}

	public void setOldSecretExpiry(String timestamp) {
		if (isVoid(timestamp)) {
			return;
		}

		try {
			long timestampLong = Long.parseLong(timestamp);
			this.oldSecretExpiry = new Date(timestampLong * 1000);
		} catch (Exception e) {
			fatal("Error: setOldSecretExpiry: Invalid timestamp: " + timestamp);
		}
	}

	public Date getOldSecretExpiry() {
		return oldSecretExpiry;
	}


	public void setSecurityAlgorithm(String securityAlgorithm) {
		this.securityAlgorithm = securityAlgorithm;
	}

	public String getSecurityAlgorithm() {
		if (isVoid(securityAlgorithm)) {
			return "wsignin1.0";
		}
		return securityAlgorithm;
	}


	public void setForceDelAuthNonProvisioned(boolean forceDelAuthNonProvisioned) {
		this.forceDelAuthNonProvisioned = forceDelAuthNonProvisioned;
	}


	public void setPolicyUrl(String policyUrl) {
		if (isVoid(policyUrl)) {
			if (forceDelAuthNonProvisioned) {
				fatal("Error: setPolicyUrl: Null policy URL given.");
			}
		}

		this.policyUrl = policyUrl;
	}

	public String getPolicyUrl() {
		if (isVoid(policyUrl)) {
			if (forceDelAuthNonProvisioned) {
				fatal("Error: getPolicyUrl: Policy URL must be set in a Delegated Auth non-provisioned scenario. Aborting.");
			}
		}

		return policyUrl;
	}

	public void setReturnUrl(String returnUrl) {
		if (isVoid(returnUrl)) {
			if (forceDelAuthNonProvisioned) {
				fatal("Error: setReturnUrl: Null return URL given.");
			}
		}

		this.returnUrl = returnUrl;
	}

	public String getReturnUrl() {
		if (isVoid(returnUrl)) {
			if (forceDelAuthNonProvisioned) {
				fatal("Error: getReturnUrl: Return URL must be set in a Delegated Auth non-provisioned scenario. Aborting.");
			}
		}

		return returnUrl;
	}


	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public String getBaseUrl() {
		if (isVoid(baseUrl)) {
			return "http://login.live.com/";
		}

		return baseUrl;
	}

	public void setSecureUrl(String secureUrl) {
		this.secureUrl = secureUrl;
	}

	public String getSecureUrl() {
		if (isVoid(secureUrl)) {
			return "https://login.live.com/";
		}

		return secureUrl;
	}


	public void setConsentBaseUrl(String consentUrl) {
		this.consentUrl = consentUrl;
	}

	public String getConsentBaseUrl() {
		if (isVoid(consentUrl)) {
			return "https://consent.live.com/";
		}

		return consentUrl;
	}

	public URL getLoginUrl() {
		return getLoginUrl(null);
	}

	public URL getLoginUrl(String context) {
		return getLoginUrl(context, null);
	}

	public URL getLoginUrl(String context, String market) {
		String url = getBaseUrl();
		validateAppId();
		url += "wlogin.srf?appid=" + appId;
		url += "&alg=" + getSecurityAlgorithm();

		if (!isVoid(context)) {
			url += "&appctx=" + escape(context);
		}

		if (!isVoid(market)) {
			url += "&mkt=" + escape(market);
		}

		try {
			return new URL(url);
		} catch (Exception e) {
		}

		return null;
	}

	public URL getLogoutUrl() {
		return getLogoutUrl(null);
	}

	public URL getLogoutUrl(String market) {
		String url = getBaseUrl();
		validateAppId();
		url += "logout.srf?appid=" + appId;

		if (!isVoid(market)) {
			url += "&mkt=" + escape(market);
		}

		try {
			return new URL(url);
		} catch (Exception e) {
		}

		return null;
	}

	public static class User {

		public User(String timestamp, String id, String flags, String context, String token) {
			setTimestamp(timestamp);
			setId(id);
			setFlags(flags);
			setContext(context);
			setToken(token);
		}

		private Date timestamp;

		public Date getTimestamp() {
			return timestamp;
		}

		private void setTimestamp(String timestamp) {
			if (isVoid(timestamp)) {
				throw new WLLException("Error: User: Null timestamp in token.");
			}

			long timestampLong;

			try {
				timestampLong = Long.parseLong(timestamp);
			} catch (Exception e) {
				throw new WLLException("Error: User: Invalid timestamp: " + timestamp);
			}

			this.timestamp = new Date(timestampLong * 1000);
		}

		private String id;

		public String getId() {
			return id;
		}

		private void setId(String id) {
			if (isVoid(id)) {
				throw new WLLException("Error: User: Null id in token.");
			}

			Pattern p = Pattern.compile("^\\w+$");
			Matcher m = p.matcher(id);
			if (!m.matches()) {
				throw new WLLException("Error: User: Invalid id: " + id);
			}

			this.id = id;
		}

		private boolean usePersistentCookie;

		public boolean usePersistentCookie() {
			return usePersistentCookie;
		}

		private void setFlags(String flags) {
			this.usePersistentCookie = false;
			if (!isVoid(flags)) {
				try {
					int flagsInt = Integer.parseInt(flags);
					this.usePersistentCookie = ((flagsInt % 2) == 1);
				} catch (Exception e) {
					throw new WLLException("Error: User: Invalid flags: " + flags);
				}
			}
		}

		private String context;

		public String getContext() {
			return context;
		}

		private void setContext(String context) {
			this.context = context;
		}

		private String token;

		public String getToken() {
			return token;
		}

		private void setToken(String token) {
			this.token = token;
		}
	}

	public User processLogin(Map<String, String[]> query) {
		if (query == null) {
			return null;
		}

		String[] values = query.get("action");
		if ((values == null) || (values.length != 1)) {
			return null;
		}
		String action = values[0];

		if (!"login".equals(action)) {
			return null;
		}

		values = query.get("stoken");
		if ((values == null) || (values.length != 1)) {
			return null;
		}
		String token = values[0];

		String context = null;
		values = query.get("appctx");
		if ((values != null) && (values.length == 1)) {
			context = values[0];
			context = escape(context);
		}

		return processToken(token, context);
	}

	public User processToken(String token) {
		return processToken(token, null);
	}

	public User processToken(String token, String context) {
		if (isVoid(token)) {
			return null;
		}

		String decodedToken = decodeAndValidateToken(token);

		if (isVoid(decodedToken)) {
			return null;
		}

		Map<String, String> parsedToken = parse(decodedToken);

		if ((parsedToken == null) || (parsedToken.size() < 3)) {
			return null;
		}
		validateAppId();
		String tokenAppId = parsedToken.get("appid");

		if (!appId.equals(tokenAppId)) {
			return null;
		}

		User user = null;

		try {
			user = new User(parsedToken.get("ts"), parsedToken.get("uid"), parsedToken.get("flags"), context, token);
		} catch (WLLException e) {
		}

		return user;
	}

	public String getClearCookieResponseType() {
		return "image/gif";
	}

	public static byte[] getClearCookieResponseBody() {
		String gif = "R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAEALAAAAAABAAEAAAIBTAA7";
		byte[] dd = Base64.decodeBase64(gif);
		return dd;
	}

	public static void main(String[] args) {
		getClearCookieResponseBody();
	}

	public URL getConsentUrl(String offers) {
		return getConsentUrl(offers, null);
	}

	public URL getConsentUrl(String offers, String context) {
		return getConsentUrl(offers, context, null);
	}

	public URL getConsentUrl(String offers, String context, String ru) {
		return getConsentUrl(offers, context, ru, null);
	}

	public URL getConsentUrl(String offers, String context, String ru, String market) {
		if (isVoid(offers)) {
			throw new WLLException("Error: getConsentUrl: Invalid offers list.");
		}

		String url = getConsentBaseUrl() + "Delegation.aspx";

		url += "?ps=" + escape(offers);

		if (!isVoid(context)) {
			url += "&appctx=" + escape(context);
		}

		if (isVoid(ru)) {
			ru = getReturnUrl();
		}

		if (!isVoid(ru)) {
			url += "&ru=" + escape(ru);
		}

		if (!isVoid(market)) {
			url += "&mkt=" + escape(market);
		}

		String plUrl = getPolicyUrl();
		if (!isVoid(plUrl)) {
			url += "&pl=" + escape(plUrl);
		}

		if (!forceDelAuthNonProvisioned) {
			url += "&app=" + getAppVerifier();
		}
		try {
			URL result = new URL(url);
			return result;
		} catch (MalformedURLException e) {
			// ignore
		}
		return null;
	}

	public URL getRefreshConsentTokenUrl(String offers, String refreshToken) {
		return getRefreshConsentTokenUrl(offers, refreshToken, null);
	}

	public URL getRefreshConsentTokenUrl(String offers, String refreshToken, String ru) {
		if (isVoid(offers)) {
			throw new WLLException("Error: getRefreshConsentTokenUrl: Invalid offers list.");
		}

		if (isVoid(refreshToken)) {
			throw new WLLException("Error: getRefreshConsentTokenUrl: Invalid refresh token.");
		}

		String url = getConsentBaseUrl() + "RefreshToken.aspx";
		url += "?ps=" + escape(offers);
		url += "&reft=" + refreshToken;

		if (isVoid(ru)) {
			ru = getReturnUrl();
		}

		if (!isVoid(ru)) {
			url += "&ru=" + escape(ru);
		}

		if (!forceDelAuthNonProvisioned) {
			url += "&app=" + getAppVerifier();
		}

		try {
			return new URL(url);
		} catch (Exception e) {
			throw new WLLException("Error: getRefreshConsentTokenUrl: Unable to create refresh consent token URL: " + url + ": " + e);
		}
	}

	/*
	 * Returns the URL for the consent-management user interface.
	 */
	public URL getManageConsentUrl() {
		return getManageConsentUrl(null);
	}

	public URL getManageConsentUrl(String market) {
		String url = getConsentBaseUrl() + "ManageConsent.aspx";

		if (!isVoid(market)) {
			url += "?mkt=" + escape(market);
		}

		try {
			return new URL(url);
		} catch (Exception e) {
			throw new WLLException("Error: getManageConsentUrl: Unable to create manage consent URL: " + url + ": " + e);
		}
	}

	public static class ConsentToken {
		private WindowsLiveLogin wll;

		public ConsentToken(WindowsLiveLogin wll, String delegationToken, String refreshToken, String sessionKey, String expiry, String offers,
				String locationID, String context, String decodedToken, String token) {
			this.wll = wll;
			setDelegationToken(delegationToken);
			setRefreshToken(refreshToken);
			setSessionKey(sessionKey);
			setExpiry(expiry);
			setOffers(offers);
			setLocationID(locationID);
			setContext(context);
			setDecodedToken(decodedToken);
			setToken(token);
		}

		private String delegationToken;

		public String getDelegationToken() {
			return delegationToken;
		}

		private void setDelegationToken(String delegationToken) {
			if (isVoid(delegationToken)) {
				throw new WLLException("Error: ConsentToken: Null delegation token.");
			}

			this.delegationToken = delegationToken;
		}

		private String refreshToken;

		public String getRefreshToken() {
			return refreshToken;
		}

		private void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}

		private byte[] sessionKey;

		public byte[] getSessionKey() {
			return sessionKey;
		}

		private void setSessionKey(String sessionKey) {
			if (isVoid(sessionKey)) {
				throw new WLLException("Error: ConsentToken: Null session key.");
			}

			this.sessionKey = WindowsLiveLogin.u64(sessionKey);
		}

		private Date expiry;

		public Date getExpiry() {
			return expiry;
		}

		public void setExpiry(String expiry) {
			if (isVoid(expiry)) {
				throw new WLLException("Error: ConsentToken: Null expiry time.");
			}

			long expiryLong;

			try {
				expiryLong = Long.parseLong(expiry);
			} catch (Exception e) {
				throw new WLLException("Error: ConsentToken: Invalid expiry time: " + expiry);
			}

			this.expiry = new Date(expiryLong * 1000);
		}

		private List<String> offers;

		public List<String> getOffers() {
			return offers;
		}

		private String offersString;

		public String getOffersString() {
			return offersString;
		}

		private void setOffers(String offers) {
			if (isVoid(offers)) {
				throw new WLLException("Error: ConsentToken: Null offers.");
			}

			offers = unescape(offers);

			this.offersString = "";
			this.offers = new ArrayList<String>();

			String[] offersList = offers.split(";");

			for (String offer : offersList) {
				if (!isVoid(this.offersString)) {
					this.offersString += ",";
				}

				int separator = offer.indexOf(":");
				if (separator == -1) {
					this.offers.add(offer);
					this.offersString += offer;
				} else {
					offer = offer.substring(0, separator);
					this.offers.add(offer);
					this.offersString += offer;
				}

			}
		}

		private String locationID;

		public String getLocationID() {
			return locationID;
		}

		private void setLocationID(String locationID) {
			if (isVoid(locationID)) {
				throw new WLLException("Error: ConsentToken: Null Location ID.");
			}
			this.locationID = locationID;
		}

		private String context;

		public String getContext() {
			return context;
		}

		private void setContext(String context) {
			this.context = context;
		}

		String decodedToken;

		public String getDecodedToken() {
			return decodedToken;
		}

		private void setDecodedToken(String decodedToken) {
			this.decodedToken = decodedToken;
		}

		String token;

		public String getToken() {
			return token;
		}

		/**
		 * Sets the raw token.
		 */
		private void setToken(String token) {
			this.token = token;
		}

		public boolean isValid() {
			if (isVoid(getDelegationToken())) {
				return false;
			}

			long now = System.currentTimeMillis();

			if ((now - 300) > getExpiry().getTime()) {
				return false;
			}

			return true;
		}

		public boolean refresh() {
			ConsentToken ct = wll.refreshConsentToken(this);

			if (ct == null) {
				return false;
			}

			copy(ct);

			return true;
		}

		private void copy(ConsentToken consentToken) {
			this.delegationToken = consentToken.delegationToken;
			this.refreshToken = consentToken.refreshToken;
			this.sessionKey = consentToken.sessionKey;
			this.expiry = consentToken.expiry;
			this.offers = consentToken.offers;
			this.offersString = consentToken.offersString;
			this.locationID = consentToken.locationID;
			this.decodedToken = consentToken.decodedToken;
			this.token = consentToken.token;
		}
	}

	public ConsentToken processConsent(Map<String, String[]> query) {
		if (query == null) {
			return null;
		}

		String[] values = query.get("action");
		if ((values == null) || (values.length != 1)) {
			return null;
		}
		String action = values[0];

		if (!"delauth".equals(action)) {
			return null;
		}

		values = query.get("ResponseCode");
		if ((values == null) || (values.length != 1)) {
			return null;
		}
		String responseCode = values[0];

		if (!"RequestApproved".equals(responseCode)) {
			return null;
		}

		values = query.get("ConsentToken");
		if ((values == null) || (values.length != 1)) {
			return null;
		}
		String token = values[0];

		String context = null;
		values = query.get("appctx");
		if ((values != null) && (values.length == 1)) {
			context = values[0];
			context = escape(context);
		}

		return processConsentToken(token, context);
	}

	public ConsentToken processConsentToken(String token) {
		return processConsentToken(token, null);
	}

	public ConsentToken processConsentToken(String token, String context) {
		String decodedToken = token;

		if (isVoid(token)) {
			return null;
		}

		Map<String, String> parsedToken = parse(unescape(token));

		if (parsedToken == null) {
			return null;
		}

		if (!isVoid(parsedToken.get("eact"))) {
			decodedToken = decodeAndValidateToken(parsedToken.get("eact"));
			if (isVoid(decodedToken)) {
				return null;
			}

			parsedToken = parse(decodedToken);
			decodedToken = escape(decodedToken);
		}

		ConsentToken consentToken = null;

		try {
			consentToken = new ConsentToken(this, parsedToken.get("delt"), parsedToken.get("reft"), parsedToken.get("skey"), parsedToken.get("exp"),
					parsedToken.get("offer"), parsedToken.get("lid"), context, decodedToken, token);
		} catch (WLLException e) {
		}

		return consentToken;
	}

	public ConsentToken refreshConsentToken(ConsentToken token) {
		return refreshConsentToken(token, null);
	}

	public ConsentToken refreshConsentToken(ConsentToken token, String ru) {
		if (token == null) {
			return null;
		}

		return refreshConsentToken(token.getOffersString(), token.getRefreshToken(), ru);
	}

	public ConsentToken refreshConsentToken(String offers, String refreshToken) {
		return refreshConsentToken(offers, refreshToken, null);
	}

	public ConsentToken refreshConsentToken(String offers, String refreshToken, String ru) {
		URL url = null;

		try {
			url = getRefreshConsentTokenUrl(offers, refreshToken, ru);
		} catch (Exception e) {
			return null;
		}

		if (url == null) {
			return null;
		}

		String body = fetch(url);

		if (isVoid(body)) {
			return null;
		}

		String re = "\\{\"ConsentToken\":\"(.*)\"\\}";
		Pattern p = Pattern.compile(re);
		Matcher m = p.matcher(body);

		if (!m.find()) {
			return null;
		}

		return processConsentToken(m.group(1));
	}

	public String decodeAndValidateToken(String token) {
		boolean haveOldSecret = false;

		long now = (new Date()).getTime();
		long expiry = (oldSecretExpiry == null) ? 0 : oldSecretExpiry.getTime();

		if (now < expiry) {
			if ((oldCryptKey != null) && (oldSignKey != null)) {
				haveOldSecret = true;
			}
		}

		String stoken = decodeAndValidateToken(token, cryptKey, signKey);

		if (isVoid(stoken) && haveOldSecret) {
			return decodeAndValidateToken(token, oldCryptKey, oldSignKey);
		}

		return stoken;
	}

	public String decodeAndValidateToken(String token, byte[] cryptKey1, byte[] signKey1) {
		String stoken = decodeToken(token, cryptKey1);

		if (!isVoid(stoken)) {
			stoken = validateToken(stoken, signKey1);
		}

		return stoken;
	}

	public String decodeToken(String token) {
		return decodeToken(token, cryptKey);
	}

	public String decodeToken(String token, byte[] cryptKey1) {
		if ((cryptKey1 == null) || (cryptKey1.length == 0)) {
			fatal("Error: decodeToken: Secret key was not set. Aborting.");
		}

		if (isVoid(token)) {
			return null;
		}

		try {
			int ivLen = 16;
			byte[] tokenBytes = u64(token);

			if ((tokenBytes == null) || (tokenBytes.length <= ivLen) || (tokenBytes.length % ivLen != 0)) {
				return null;
			}

			byte[] iv = Arrays.copyOf(tokenBytes, ivLen);
			byte[] crypted = Arrays.copyOfRange(tokenBytes, ivLen, tokenBytes.length);

			SecretKeySpec keySpec = new SecretKeySpec(cryptKey1, "AES");
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			String decrypted = new String(cipher.doFinal(crypted));
			return decrypted;
		} catch (Exception e) {
		}

		return null;
	}

	public byte[] signToken(String token) {
		return signToken(token, signKey);
	}

	public byte[] signToken(String token, byte[] signKey1) {
		if ((signKey1 == null) || (signKey1.length == 0)) {
			fatal("Error: signToken: Secret key was not set. Aborting.");
		}

		if (isVoid(token)) {
			return null;
		}

		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec = new SecretKeySpec(signKey1, "AES");
			mac.init(keySpec);
			return mac.doFinal(token.getBytes());
		} catch (Exception e) {
		}

		return null;
	}

	public String validateToken(String token) {
		return validateToken(token, signKey);
	}

	public String validateToken(String token, byte[] signkey) {
		if (isVoid(token)) {
			return null;
		}

		String[] split = token.split("&sig=");

		if (split.length != 2) {
			return null;
		}

		byte[] sig = u64(split[1]);
		if (sig == null) {
			return null;
		}

		byte[] sig2 = signToken(split[0], signkey);
		if (sig2 == null) {
			return null;
		}

		if (Arrays.equals(sig, sig2)) {
			return token;
		}
		return null;
	}

	public String getAppVerifier() {
		return getAppVerifier(null);
	}

	public String getAppVerifier(String ip) {
		validateAppId();
		String token = "appid=" + appId + "&ts=" + getTimestamp();

		if (!isVoid(ip)) {
			token += "&ip=" + ip;
		}

		token += "&sig=" + e64(signToken(token));
		return escape(token);
	}

	public URL getAppLoginUrl() {
		return getAppLoginUrl(null, null, false);
	}

	public URL getAppLoginUrl(String siteId) {
		return getAppLoginUrl(siteId, null, false);
	}

	public URL getAppLoginUrl(String siteId, String ip) {
		return getAppLoginUrl(siteId, ip, false);
	}

	public URL getAppLoginUrl(String siteId, String ip, boolean js) {
		String url = getSecureUrl();
		url += "wapplogin.srf?app=" + getAppVerifier(ip);
		url += "&alg=" + getSecurityAlgorithm();

		if (!isVoid(siteId)) {
			url += "&id=" + siteId;
		}

		if (js) {
			url += "&js=1";
		}

		try {
			return new URL(url);
		} catch (Exception e) {
		}

		return null;
	}

	public String getAppSecurityToken() {
		return getAppSecurityToken(null, null);
	}

	public String getAppSecurityToken(String siteId) {
		return getAppSecurityToken(siteId, null);
	}

	public String getAppSecurityToken(String siteId, String ip) {
		URL url = getAppLoginUrl(siteId, ip);

		if (url == null) {
			return null;
		}

		String body = fetch(url);
		if (isVoid(body)) {
			return null;
		}

		String regex = "\\{\"token\":\"(.*)\"\\}";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(body);

		if (!m.find()) {
			return null;
		}

		return m.group(1);
	}

	/**
	 * Returns a string that can be passed to the getTrustedParams function as
	 * the 'retcode' parameter. If this is specified as the 'retcode', the
	 * application will be used as return URL after it finishes trusted sign-in.
	 */
	public String getAppRetCode() {
		validateAppId();
		return "appid=" + appId;
	}

	public Map<String, String> getTrustedParams(String user) {
		return getTrustedParams(user, null);
	}

	public Map<String, String> getTrustedParams(String user, String retcode) {
		String token = getTrustedToken(user);
		if (isVoid(token)) {
			return null;
		}
		token = "<wst:RequestSecurityTokenResponse xmlns:wst=\"http://schemas.xmlsoap.org/ws/2005/02/trust\"><wst:RequestedSecurityToken><wsse:BinarySecurityToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\">"
				+ token
				+ "</wsse:BinarySecurityToken></wst:RequestedSecurityToken><wsp:AppliesTo xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"><wsa:EndpointReference xmlns:wsa=\"http://schemas.xmlsoap.org/ws/2004/08/addressing\"><wsa:Address>uri:WindowsLiveID</wsa:Address></wsa:EndpointReference></wsp:AppliesTo></wst:RequestSecurityTokenResponse>";

		Map<String, String> params = new HashMap<String, String>();
		params.put("wa", getSecurityAlgorithm());
		params.put("wresult", token);
		if (!isVoid(retcode))
			params.put("wctx", retcode);
		return params;
	}

	public String getTrustedToken(String user) {
		if (isVoid(user)) {
			return null;
		}
		validateAppId();
		String token = "appid=" + appId + "&uid=" + escape(user) + "&ts=" + getTimestamp();
		token += "&sig=" + e64(signToken(token));
		return escape(token);
	}

	public URL getTrustedLoginUrl() {
		String url = getSecureUrl();
		url += "wlogin.srf";

		try {
			return new URL(url);
		} catch (Exception e) {
		}

		return null;
	}

	public URL getTrustedLogoutUrl() {
		String url = getSecureUrl();
		validateAppId();
		url += "logout.srf?appid=" + appId;
		try {
			return new URL(url);
		} catch (Exception e) {

		}

		return null;
	}

	private Map<String, String> parseSettings(String settingsFile) {
		try {
			InputStream settingsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(settingsFile);

			if (settingsStream == null) {
				fatal("Error: parseSettings: Could not load the settings file: " + settingsFile);
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(settingsStream);

			NodeList nl = document.getElementsByTagName("windowslivelogin");

			if (nl.getLength() != 1) {
				fatal("Error: parseSettings: Failed to parse settings file: " + settingsFile);
			}

			Node topNode = nl.item(0);
			nl = topNode.getChildNodes();
			Map<String, String> settings = new HashMap<String, String>();

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Node.ELEMENT_NODE) {
					settings.put(n.getNodeName(), n.getFirstChild().getNodeValue());
				}
			}

			return settings;
		} catch (Exception e) {
			fatal("Error: parseSettings: Unable to load settings from: " + settingsFile + ": " + e);
		}

		return null;
	}

	private byte[] derive(String secret, String prefix) {
		if (isVoid(secret) || isVoid(prefix)) {
			fatal("Error: derive: secret or prefix is null.");
		}

		try {
			int keyLen = 16;
			String key = prefix + secret;
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(key.getBytes());
			byte rv[] = Arrays.copyOf(digest, keyLen);
			return rv;
		} catch (Exception e) {
			fatal("Error: derive: Unable to derive key: " + e);
		}

		return null;
	}

	private static Map<String, String> parse(String input) {
		if (isVoid(input)) {
			return null;
		}

		Map<String, String> map = new HashMap<String, String>();
		String[] pairs = input.split("&");

		for (String pair : pairs) {
			String[] kv = pair.split("=");

			if (kv.length != 2) {
				return null;
			}

			map.put(kv[0], kv[1]);
		}

		return map;
	}

	private static String getTimestamp() {
		Date now = new Date();
		return String.valueOf(now.getTime() / 1000);
	}

	private static String e64(byte[] bytes) {
		if (bytes == null) {
			return null;
		}

		return escape(Base64.encodeBase64String(bytes));
	}

	private static byte[] u64(String s) {
		if (s == null) {
			return null;
		}
		return Base64.decodeBase64(unescape(s));
	}

	private String fetch(URL url) {
		StringBuilder body = new StringBuilder();

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				body.append(inputLine);
			}

			in.close();
			return body.toString();
		} catch (Exception e) {
		}

		return null;
	}

	private static boolean isVoid(String string) {
		if ((string == null) || (string.length() == 0)) {
			return true;
		}

		return false;
	}

	public static String escape(String s) {
		if (s == null) {
			return null;
		}

		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (Exception e) {
		}

		return null;
	}

	public static String unescape(String s) {
		if (s == null) {
			return null;
		}

		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (Exception e) {
		}

		return null;
	}

	public static Map<String, String> getContact(ConsentToken token) {
		String url = "https://livecontacts.services.live.com/users/@L@" + token.getLocationID() + "/rest/LiveContacts";
		Map<String, String> header = new HashMap<String, String>();
		header.put("UserAgent", "Windows Live Data Interactive SDK");
		header.put("ContentType", "application/xml; charset=utf-8");
		header.put("Authorization", "DelegatedToken dt=\"" + token.getDelegationToken() + "\"");
		Map<String, String> m = new HashMap<String, String>();
		try {
			HttpResult result = HttpUtils.postUrlAsString(url, m, header, "utf-8");
			SAXReader saxReader = new SAXReader();
			org.dom4j.Document document = saxReader.read(new StringReader(result.getResponse()));
			Element rootElement = document.getRootElement();
			Element contactsElement = rootElement.element("Contacts");
			List<Element> contactList = contactsElement.elements("Contact");
			String email = null;
			String sortName = null;
			for (int i = 0; i < contactList.size(); i++) {
				Element element = contactList.get(i);
				Element e = element.element("Emails");
				if (e != null) {
					e = e.element("Email").element("Address");
					if (e != null)
						email = e.getText();
				}
				e = element.element("Profiles").element("Personal").element("SortName");
				if (e != null) {
					sortName = e.getText();
					if (StringUtils.isBlank(sortName))
						sortName = email;
				}
				if (StringUtils.isNotBlank(email) && StringUtils.isNotBlank(sortName)) {
					m.put(email, sortName);
				}

			}
		} catch (Exception e) {
			
		}
		return m;
	}

}
