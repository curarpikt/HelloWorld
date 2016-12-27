/**   
 * @Title: HttpClientUtil.java 
 * @Package com.chanjet.app.customer.util 
 * @Description: TODO(用一句话描述该文件做什么) 
 * @author junxing zhangjxd@chanjet.com   
 * @date 2015年1月9日 上午10:54:46 
 * @version V1.0   
 */
package com.chanapp.chanjet.customer.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

/**
 * @ClassName: HttpClientUtil
 * @Description: TODO(这里用一句话描述这个类的作用)
 * @author junxing zhangjxd@chanjet.com
 * @date 2015年1月9日 上午10:54:46
 * 
 */
public class HttpClientUtils {
	private static volatile Map<String, HttpClientUtils> map = new ConcurrentHashMap<String, HttpClientUtils>();

	private static String DEFAULTCONTENTTYPE = "application/x-www-form-urlencoded";
	
	public static HttpClientUtils getInstance() {
		return getInstance(3000, 5000);
	}

	public static HttpClientUtils getInstance(int connectionTimeout,
			int soTimeOut) {
		String key = connectionTimeout + ":" + soTimeOut;
		HttpClientUtils httpClientUtils = map.get(key);
		if (httpClientUtils == null) {
			synchronized (HttpClientUtils.class) {
				httpClientUtils = map.get(key);
				if (httpClientUtils == null) {
					httpClientUtils = new HttpClientUtils(connectionTimeout,
							soTimeOut);
					map.put(key, httpClientUtils);
				}
			}
		}
		return httpClientUtils;
	}

	private HttpClient httpClient;

	private HttpClientUtils(int connectionTimeout, int soTimeOut) {
		MultiThreadedHttpConnectionManager httpClientManager;
		httpClientManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setStaleCheckingEnabled(true);
		params.setMaxTotalConnections(1024);
		params.setDefaultMaxConnectionsPerHost(512);
		params.setConnectionTimeout(connectionTimeout);// 3000
		params.setSoTimeout(soTimeOut);// 5000
		httpClientManager.setParams(params);
		HttpClientParams httpClientParams = new HttpClientParams();
		httpClientParams.setConnectionManagerTimeout(connectionTimeout);// 5秒获取不到一个连接就释放一个错误
		httpClient = new HttpClient(httpClientParams, httpClientManager);
	}

	public PostMethod post(String url, Map<String, String> params)
			throws IOException {
		return post(url, params, true);
	}

	public PostMethod post(String url, Map<String, String> params,
			boolean throwExceptionIfAbNormal) throws IOException {
		return post(url, params, Charsets.UTF8, throwExceptionIfAbNormal);
	}

	public PostMethod post(String url, Map<String, String> params,
			Charsets charset, boolean throwExceptionIfAbNormal)
			throws IOException {
		PostMethod post = new PostMethod(url);
		post.getParams().setContentCharset(charset.encoding);
		setHeadersAndCharset(post,DEFAULTCONTENTTYPE, Charsets.UTF8);
		if (params != null && params.size() > 0) {
			for (Map.Entry<String, String> e : params.entrySet()) {
				post.addParameter(e.getKey(), e.getValue());
			}
		}
		execute(post, throwExceptionIfAbNormal);
		return post;
	}
	public GetMethod get(String url)
			throws IOException {
		return get(url, true);
	}
	
	public GetMethod get(String url, boolean throwExceptionIfAbNormal) throws IOException {
		return get(url, DEFAULTCONTENTTYPE, throwExceptionIfAbNormal);
	}
	
	public GetMethod get(String url, String contentType, boolean throwExceptionIfAbNormal) throws IOException {
		return get(url,contentType, Charsets.UTF8, throwExceptionIfAbNormal);
	}
	
	public GetMethod get(String url,String contentType, Charsets charset, boolean throwExceptionIfAbNormal)
					throws IOException {
		GetMethod method = new GetMethod(url);
		method.getParams().setContentCharset(charset.encoding);
		setHeadersAndCharset(method,contentType, Charsets.UTF8);
		execute(method, throwExceptionIfAbNormal);
		return method;
	}
	public DeleteMethod delete(String url)
			throws IOException {
		return delete(url, true);
	}
	
	public DeleteMethod delete(String url, boolean throwExceptionIfAbNormal) throws IOException {
		return delete(url, DEFAULTCONTENTTYPE, throwExceptionIfAbNormal);
	}
	
	public DeleteMethod delete(String url, String contentType, boolean throwExceptionIfAbNormal) throws IOException {
		return delete(url,contentType, Charsets.UTF8, throwExceptionIfAbNormal);
	}
	
	public DeleteMethod delete(String url,String contentType, Charsets charset, boolean throwExceptionIfAbNormal)
			throws IOException {
		DeleteMethod method = new DeleteMethod(url);
		method.getParams().setContentCharset(charset.encoding);
		setHeadersAndCharset(method,contentType, Charsets.UTF8);
		execute(method, throwExceptionIfAbNormal);
		return method;
	}

	private void setHeadersAndCharset(HttpMethodBase methodBase,String contentType,Charsets charset) {
		methodBase.getParams().setContentCharset(charset.encoding);
		methodBase.setRequestHeader("User-Agent", "chanjet_customer");
		methodBase.setRequestHeader("Referer", "");
		methodBase.setRequestHeader("Content-type", contentType);
	}

	public HttpState execute(HttpMethod method, boolean throwExceptionIfAbNormal)
			throws IOException {
		HttpState state = new HttpState();
		int statusCode = -1;
		try {
			statusCode = httpClient.executeMethod(null, method, state);
		} catch (IOException ioe) {
			method.releaseConnection();
			throw ioe;
		}
		if (throwExceptionIfAbNormal && statusCode != 200) {
			method.releaseConnection();
			throw new IOException("abnormal, status code:" + statusCode
					+ ", uri:" + method.getURI());
		}
		return state;
	}

	public String resultMethod2String(HttpMethodBase method) throws IOException {
		try {
			InputStream in = method.getResponseBodyAsStream();

			if (in == null) {
				return null;// 返回null
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
			int len = -1;
			byte[] b = new byte[4096];
			while ((len = in.read(b)) > 0) {
				baos.write(b, 0, len);
			}
			baos.close();// ignore is ok
			String encoding = method.getResponseCharSet();
			if (encoding == null) {
				encoding = method.getRequestCharSet() != null ? method
						.getRequestCharSet() : Charsets.UTF8.encoding;
			}
			String response = new String(baos.toByteArray(), encoding);

			return response;
		} finally {
			method.releaseConnection();
		}
	}
}
