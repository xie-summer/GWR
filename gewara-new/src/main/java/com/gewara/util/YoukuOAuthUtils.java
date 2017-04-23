package com.gewara.util;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.gewara.support.ErrorCode;

/**
 * Youku Open API Utils
 * 
 * @author taiqichao
 * 
 */
public class YoukuOAuthUtils {

	/** 授权url **/
	public static final String URL_OAUTH2_AUTHORIZE = "https://openapi.youku.com/v2/oauth2/authorize";

	/** 用户授权过的code获取AccessToken **/
	public static final String URL_OAUTH2_TOKEN_CODE = "https://openapi.youku.com/v2/oauth2/token";

	/** 授权跳转地址，必须和应用设置的跳转地址相匹配 **/
	public static final String URL_GEWARA_REDIRECT_URI = "http://localhost/oauth/youkuOAuthCallBack.xhtml";
	
	/** youku根据关键字查询**/
	public static final String URL_VIDEO_BYWOELD = "https://openapi.youku.com/v2/searches/video/by_keyword.json";

	/** youkuWEB上传创建**/
	public static final String UPLOADS_WEB_CREATE = "https://openapi.youku.com/v2/uploads/web/create.json";
	
	/** youku基于表单的视频上传**/
	public static final String UPLOADS_WEB_UPLOAD = "http://upload.youku.com/api/form_data_upload/";
	
	/** youkuWEB上传提交**/
	public static final String UPLOADS_WEB_COMMIT = "https://openapi.youku.com/v2/uploads/web/commit.json";
	
	/** 应用Key **/
	public static final String CLIENT_ID = "6f0c6f5ab70fe63b";

	/** 应用Secret **/
	public static final String CLIENT_SECRET = "c825375fd001487fab5f04a259d9344d";

	/** 类型,授权码方式(Authorization Code) **/
	public static final String RESPONSE_TYPE = "code";

	/** 授权类型授权码方式(AuthorizationCode) **/
	public static final String GRANT_TYPE = "authorization_code";
	
	/** youku禁评论、禁下载**/
	public static final List<String> OPERATION_LIMIT = Arrays.asList("COMMENT_DISABLED","DOWNLOAD_DISABLED");

	/** youku视频格式**/
	public static final List<String> STREAMTYPES = Arrays.asList("flvhd","flv","3gphd","3gp","hd","hd2");
	/**
	 * 构建授权请求url
	 * 
	 * @param state
	 *            状态保持参数，授权完成跳转会回传此参数
	 * @return
	 */
	public static String getOAuthRequestUrl(String state) {
		StringBuilder url = new StringBuilder(URL_OAUTH2_AUTHORIZE.trim());
		url.append("?client_id=" + CLIENT_ID.trim());
		url.append("&response_type=" + RESPONSE_TYPE.trim());
		url.append("&redirect_uri=" + URL_GEWARA_REDIRECT_URI.trim());
		if (StringUtils.isNotBlank(state)) {
			url.append("&state=" + state.trim());
		}
		return url.toString();
	}
	
	/**
	 * 搜索视频通过关键词
	 * @param keyworld  视频搜索关键字
	 * @return
	 */
	public static Page getYoukuSearchesByKeyworld(String keyword, Integer pageNo, Integer rowsPerPage){
		Page page = null;
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", CLIENT_ID.trim());
		params.put("keyword", keyword);
		params.put("page",  Integer.toString(pageNo));
		params.put("count",  Integer.toString(rowsPerPage));
		
		ErrorCode<String> resultCode = sendHttpRequest(getHttpPost(URL_VIDEO_BYWOELD, params));
		
		if (resultCode.isSuccess()) {
			return JsonUtils.readJsonToObject(Page.class, resultCode.getRetval());
		}
		return page;
	}
	
	/**
	 * 使用用户授权过的code获取Access Token
	 * 
	 * @return
	 */
	public static YoukuAccessToken getAccessToken(String code) {

		YoukuAccessToken token = null;

		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", CLIENT_ID.trim());
		params.put("client_secret", CLIENT_SECRET.trim());
		params.put("grant_type", GRANT_TYPE.trim());
		params.put("code", code);
		params.put("redirect_uri", URL_GEWARA_REDIRECT_URI.trim());

		ErrorCode<String> resultCode = sendHttpRequest(getHttpPost(
				URL_OAUTH2_TOKEN_CODE, params));
		if (resultCode.isSuccess()) {
			return JsonUtils.readJsonToObject(YoukuAccessToken.class,
					resultCode.getRetval());
		}
		return token;
	}
	
	/**
	 * youkuWeb上传创建
	 * @param access_token OAuth2授权
	 * @param title 视频标题
	 * @param tags 视频标签
	 * @param category 分类名称
	 * @param copyright_type 版权所有
	 * @param public_type 视频权限
	 * @param watch_password 观看密码明文
	 * @param description 视频描述
	 * @param file_name 视频源文件名称(包括扩展名)
	 * @return
	 */
	public static Create getWebCreate(String access_token, String title, String tags, String category, String copyright_type,
			String public_type, String watch_password, String description, String file_name){
		Create create = new Create();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", CLIENT_ID.trim());
		params.put("access_token", access_token);
		params.put("title", title);
		params.put("tags", tags);
		params.put("category", category);
		params.put("copyright_type", copyright_type);
		params.put("public_type", public_type);
		if(watch_password != null && watch_password != "") params.put("watch_password", watch_password);
		params.put("description", description);
		params.put("file_name", file_name);
		
		ErrorCode<String> resultCode = sendHttpRequest(getHttpPost(UPLOADS_WEB_CREATE, params));
		if (resultCode.isSuccess()) {
			return JsonUtils.readJsonToObject(Create.class, resultCode.getRetval());
		}
		return create;
	}
	
	/**
	 * youku基于表单的视频上传
	 * @param upload_token 上传视频的upload_token
	 * @param fileData 视频文件
	 * @return 
	public static Upload getWebUpload(String upload_token, File fileData){
		Upload upload = new Upload();
		Map<String, String> params = new HashMap<String, String>();
		params.put("upload_token", upload_token);
		params.put("fileData", fileData);
		
		ErrorCode<String> resultCode = sendHttpRequest(getHttpPost(UPLOADS_WEB_UPLOAD, params));
		if (resultCode.isSuccess()) {
			return JsonUtils.readJsonToObject(Upload.class, resultCode.getRetval());
		}
		return upload;
	}
	*/
	
	/**
	 * youkuWEB上传提交
	 * @param access_token OAuth2授权
	 * @param upload_token 上传token
	 * @param upload_server_name 上传服务器标识
	 * @return
	 */
	public static Commit getWebCommit(String access_token, String upload_token, String upload_server_name){
		Commit commit = new Commit();
		Map<String, String> params = new HashMap<String, String>();
		params.put("client_id", CLIENT_ID.trim());
		params.put("access_token", access_token);
		params.put("upload_token", upload_token);
		params.put("upload_server_name", upload_server_name);
		
		ErrorCode<String> resultCode = sendHttpRequest(getHttpPost(UPLOADS_WEB_COMMIT, params));
		if (resultCode.isSuccess()) {
			return JsonUtils.readJsonToObject(Commit.class, resultCode.getRetval());
		}
		return commit;
	}
	
	private static HttpPost getHttpPost(String url, Map<String, String> params) {
		HttpPost httpPost = new HttpPost(url);
		if (params != null) {
			List<NameValuePair> form = new ArrayList<NameValuePair>();
			for (String name : params.keySet()) {
				form.add(new BasicNameValuePair(name, params.get(name)));
			}
			try {
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(form,
						"utf-8");
				httpPost.setEntity(entity);
			} catch (UnsupportedEncodingException e) {
			}
		}
		return httpPost;
	}

	/**
	 * 发送http请求
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private static ErrorCode<String> sendHttpRequest(HttpUriRequest request) {
		try {
			HttpClient httpclient = getHttpClient();
			HttpResponse response = httpclient.execute(request);
			String result = EntityUtils.toString(response.getEntity(), "utf-8");
			int statusCode=response.getStatusLine().getStatusCode();
			if (statusCode>=200&&statusCode<=207) {
				return ErrorCode.getSuccessReturn(result);
			} else {
				return ErrorCode.getFailure(result);
			}
		} catch (Exception e) {
			return ErrorCode.getFailure(e.getMessage());
		}
	}

	/**
	 * 包裹httpclient
	 * 
	 * @return
	 */
	private static HttpClient getHttpClient() {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs,
						String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx);
			ClientConnectionManager ccm = httpclient.getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			Scheme sch = new Scheme("https", 443, ssf);
			sr.register(sch);
			return new DefaultHttpClient(ccm, httpclient.getParams());
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public static class YoukuAccessToken {
		private String access_token;
		private Integer expires_in;
		private String refresh_token;
		private String token_type;

		public YoukuAccessToken() {
		}

		public YoukuAccessToken(String access_token, Integer expires_in,
				String refresh_token, String token_type) {
			this.access_token = access_token;
			this.expires_in = expires_in;
			this.refresh_token = refresh_token;
			this.token_type = token_type;
		}

		public String getAccess_token() {
			return access_token;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public Integer getExpires_in() {
			return expires_in;
		}

		public void setExpires_in(Integer expires_in) {
			this.expires_in = expires_in;
		}

		public String getRefresh_token() {
			return refresh_token;
		}

		public void setRefresh_token(String refresh_token) {
			this.refresh_token = refresh_token;
		}

		public String getToken_type() {
			return token_type;
		}

		public void setToken_type(String token_type) {
			this.token_type = token_type;
		}

	}
	
	public static class Video{
		private String id;
		private String title;
		private String link;
		private String thumbnail;
		private String duration;
		private String category;
		private String tags;
		private String state;
		private String view_count;
		private String favorite_count;
		private String comment_count;
		private String up_count;
		private String down_count;
		private String published;
		private List<String> operation_limit;
		private List<String> streamtypes;
		private User user;
		
		public Video(){}
		
		public Video(String id, String title, String link, String thumbnail, String category, String tags, String state, String published,
				String duration, String view_count, String favorite_count, String comment_count, String up_count, String down_count ){
			this.id=id;
			this.title=title;
			this.link=link;
			this.thumbnail=thumbnail;
			this.category=category;
			this.tags=tags;
			this.state=state;
			this.published=published;
			this.operation_limit=OPERATION_LIMIT;
			this.streamtypes=STREAMTYPES;
			this.duration=duration;
			this.view_count=view_count;
			this.favorite_count=favorite_count;
			this.comment_count=comment_count;
			this.up_count=up_count;
			this.down_count=down_count;
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
		public String getLink() {
			return link;
		}
		public void setLink(String link) {
			this.link = link;
		}
		public String getThumbnail() {
			return thumbnail;
		}
		public void setThumbnail(String thumbnail) {
			this.thumbnail = thumbnail;
		}
		public String getDuration() {
			return duration;
		}
		public void setDuration(String duration) {
			this.duration = duration;
		}
		public String getCategory() {
			return category;
		}
		public void setCategory(String category) {
			this.category = category;
		}
		public String getTags() {
			return tags;
		}
		public void setTags(String tags) {
			this.tags = tags;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public String getView_count() {
			return view_count;
		}
		public void setView_count(String view_count) {
			this.view_count = view_count;
		}
		public String getFavorite_count() {
			return favorite_count;
		}
		public void setFavorite_count(String favorite_count) {
			this.favorite_count = favorite_count;
		}
		public String getComment_count() {
			return comment_count;
		}
		public void setComment_count(String comment_count) {
			this.comment_count = comment_count;
		}
		public String getUp_count() {
			return up_count;
		}
		public void setUp_count(String up_count) {
			this.up_count = up_count;
		}
		public String getDown_count() {
			return down_count;
		}
		public void setDown_count(String down_count) {
			this.down_count = down_count;
		}
		public String getPublished() {
			return published;
		}
		public void setPublished(String published) {
			this.published = published;
		}
		public List<String> getOperation_limit() {
			return operation_limit;
		}
		public void setOperation_limit(List<String> operation_limit) {
			this.operation_limit = operation_limit;
		}
		public List<String> getStreamtypes() {
			return streamtypes;
		}
		public void setStreamtypes(List<String> streamtypes) {
			this.streamtypes = streamtypes;
		}

		public User getUser() {
			return user;
		}

		public void setUser(User user) {
			this.user = user;
		}
		
		
	}

	public static class Page{
		private Integer total;
		private List<Video> videos;
		public Page(){}
		
		public Page(Integer total, List<Video> videos){
			this.total=total;
			this.videos=videos;
		}
		
		public Integer getTotal() {
			return total;
		}
		public void setTotal(Integer total) {
			this.total = total;
		}
		public List<Video> getVideos() {
			return videos;
		}
		public void setVideos(List<Video> videos) {
			this.videos = videos;
		}
	}
	
	public static class User{
		private String id;
		private String name;
		private String link;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getLink() {
			return link;
		}
		public void setLink(String link) {
			this.link = link;
		}
	}
	
	//youkuWeb上传创建 实体类
	public static class Create{
		private String upload_token;
		private String upload_server_uri;
		public String getUpload_token() {
			return upload_token;
		}
		public void setUpload_token(String upload_token) {
			this.upload_token = upload_token;
		}
		public String getUpload_server_uri() {
			return upload_server_uri;
		}
		public void setUpload_server_uri(String upload_server_uri) {
			this.upload_server_uri = upload_server_uri;
		}
	}
	
	//youkuWEB上传提交 实体类
	public static class Commit{
		private String video_id;

		public String getVideo_id() {
			return video_id;
		}

		public void setVideo_id(String video_id) {
			this.video_id = video_id;
		}
	}
	
}
