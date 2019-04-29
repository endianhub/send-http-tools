package com.xh.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * <b>Title: </b>
 * <p>Description: </p>
 * 
 * @author H.Yang
 * @email xhaimail@163.com
 * @date 2019/04/18
 */
public class HttpTools {

	public static final String METHOD_POST = "POST";
	public static final String METHOD_GET = "GET";
	public static final String CHARSET_UTF_8 = "UTF-8";
	public static final String CHARSET_GBK = "GBK";

	public static final String TEXT_HTML = "text/html";// HTML格式
	public static final String TEXT_PLAIN = "text/plain";// 纯文本格式
	public static final String TEXT_XML = "text/xml";// XML格式
	public static final String APPLICATION_XML = "application/xml";// XML数据格式
	public static final String APPLICATION_JSON = "application/json";// JSON数据格式
	public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";// 二进制流数据（如常见的文件下载）
	public static final String APPLICATION_DEFAULT = "application/x-www-form-urlencoded";// 浏览器的原生form表单，提交的数据按照 key1=val1&key2=val2
																							// 的方式进行编码，key和val都进行了URL转码
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";// 需要在表单中进行文件上传时，就需要使用该格式

	private static Map paramsMap = new HashMap();

	private static SSLContext ctx = null;
	private static HostnameVerifier verifier = null;
	private static SSLSocketFactory socketFactory = null;

	private static final SerializerFeature[] features = { //
			SerializerFeature.DisableCircularReferenceDetect, // 打开循环引用检测，JSONField(serialize = false)不循环
			SerializerFeature.WriteDateUseDateFormat,// 默认使用系统默认 格式日期格式化
			// SerializerFeature.WriteMapNullValue, //输出空置字段
			// SerializerFeature.WriteNullListAsEmpty,//list字段如果为null，输出为[]，而不是null
			// SerializerFeature.WriteNullNumberAsZero,// 数值字段如果为null，输出为0，而不是null
			// SerializerFeature.WriteNullBooleanAsFalse,//Boolean字段如果为null，输出为false，而不是null
			// SerializerFeature.WriteNullStringAsEmpty//字符类型字段如果为null，输出为""，而不是null
	};

	public void addParameter(String name, Object value) {
		this.paramsMap.put(name, value);
	}

	/***********************************************************************************************
	 * 
	 * 		HTTS请求
	 * 
	 ***********************************************************************************************/

	static {
		try {
			ctx = SSLContext.getInstance("TLS");
			ctx.init(new KeyManager[0], new TrustManager[] { new DefaultTrustManager() }, new SecureRandom());
			ctx.getClientSessionContext().setSessionTimeout(15);
			ctx.getClientSessionContext().setSessionCacheSize(1000);
			socketFactory = ctx.getSocketFactory();
		} catch (Exception e) {
		}
		verifier = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return false;// 默认认证不通过，进行证书校验。
			}
		};
	}

	private static class DefaultTrustManager implements X509TrustManager {
		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
		}
	}

	/***********************************************************************************************
	 * 
	 * 		POST 请求 addParameter参数
	 * 
	 ***********************************************************************************************/
	public static String doPost(String url, String contentType) throws IOException {
		return doPost(url, getParams(), contentType, CHARSET_UTF_8);
	}

	public static String doPost(String url, String contentType, String charset) throws IOException {
		return doPost(url, getParams(), contentType, charset, 3000, 3000);
	}

	public static String doPost(String url, String contentType, int connectTimeout, int readTimeout) throws IOException {
		return doPost(url, getParams(), contentType, CHARSET_UTF_8, 3000, 3000);
	}

	public static String doPost(String url, String contentType, String charset, int connectTimeout, int readTimeout) throws IOException {

		return doPostSend(url, getParams(), contentType, charset, connectTimeout, readTimeout, null);
	}

	/******************************************************************************************/

	public static String doPost(String url, Object params, String contentType) throws IOException {
		return doPost(url, params, contentType, CHARSET_UTF_8);
	}

	public static String doPost(String url, Object params, String contentType, String charset) throws IOException {
		return doPost(url, params, contentType, charset, 3000, 3000);
	}

	public static String doPost(String url, Object params, String contentType, int connectTimeout, int readTimeout) throws IOException {
		return doPost(url, params, contentType, CHARSET_UTF_8, 3000, 3000);
	}

	public static String doPost(String url, Object params, String contentType, String charset, int connectTimeout,
			int readTimeout) throws IOException {

		return doPostSend(url, params, contentType, charset, connectTimeout, readTimeout, null);
	}

	/***********************************************************************************************
	 * 
	 * 		POST 请求 + 代理
	 * 
	 ***********************************************************************************************/

	public static String doPost(String url, Object params, String contentType, IProxy proxy) throws IOException {
		return doPost(url, params, contentType, CHARSET_UTF_8, proxy);
	}

	public static String doPost(String url, Object params, String contentType, String charset, IProxy proxy) throws IOException {
		return doPost(url, params, contentType, charset, 3000, 3000, proxy);
	}

	public static String doPost(String url, Object params, String contentType, int connectTimeout, int readTimeout,
			IProxy proxy) throws IOException {
		return doPost(url, params, contentType, CHARSET_UTF_8, 3000, 3000, proxy);
	}

	public static String doPost(String url, Object params, String contentType, String charset, int connectTimeout, int readTimeout,
			IProxy proxy) throws IOException {

		return doPostSend(url, params, contentType, charset, connectTimeout, readTimeout, proxy.settingProxy());
	}

	/***********************************************************************************************
	 * 
	 * 		GET 请求 addParameter参数
	 * 
	 ***********************************************************************************************/

	public static String doGet(String url, String contentType) throws IOException {
		return doGet(url, getParams(), contentType, CHARSET_UTF_8);
	}

	public static String doGet(String url, String contentType, String charset) throws IOException {
		return doGet(url, getParams(), contentType, charset, 3000, 3000);
	}

	public static String doGet(String url, String contentType, int connectTimeout, int readTimeout) throws IOException {
		return doGet(url, getParams(), contentType, CHARSET_UTF_8, 3000, 3000);
	}

	public static String doGet(String url, String contentType, String charset, int connectTimeout, int readTimeout) throws IOException {
		return doGetSend(url, getParams(), contentType, charset, connectTimeout, readTimeout, null);
	}

	/***************************************************************************************************/

	public static String doGet(String url, Object params, String contentType) throws IOException {
		return doGet(url, params, contentType, CHARSET_UTF_8);
	}

	public static String doGet(String url, Object params, String contentType, String charset) throws IOException {
		return doGet(url, params, contentType, charset, 3000, 3000);
	}

	public static String doGet(String url, Object params, String contentType, int connectTimeout, int readTimeout) throws IOException {
		return doGet(url, params, contentType, CHARSET_UTF_8, 3000, 3000);
	}

	public static String doGet(String url, Object params, String contentType, String charset, int connectTimeout,
			int readTimeout) throws IOException {
		return doGetSend(url, params, contentType, charset, connectTimeout, readTimeout, null);
	}

	/***********************************************************************************************
	 * 
	 * 		GET 请求 + 代理
	 * 
	 ***********************************************************************************************/

	public static String doGet(String url, Object params, String contentType, IProxy proxy) throws IOException {
		return doGet(url, params, contentType, CHARSET_UTF_8, proxy);
	}

	public static String doGet(String url, Object params, String contentType, String charset, IProxy proxy) throws IOException {
		return doGet(url, params, contentType, charset, 3000, 3000, proxy);
	}

	public static String doGet(String url, Object params, String contentType, int connectTimeout, int readTimeout,
			IProxy proxy) throws IOException {
		return doGet(url, params, contentType, CHARSET_UTF_8, 3000, 3000, proxy);
	}

	public static String doGet(String url, Object params, String contentType, String charset, int connectTimeout, int readTimeout,
			IProxy proxy) throws IOException {
		return doGetSend(url, params, contentType, charset, connectTimeout, readTimeout, proxy.settingProxy());
	}

	/***********************************************************************************************
	 * 
	 * 		Send发送
	 * 
	 ***********************************************************************************************/

	public static String doPostSend(String url, Object paramObject, String contentType, String charset, int connectTimeout, int readTimeout,
			Proxy proxy) throws MalformedURLException, IOException {
		// Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));

		HttpURLConnection conn = getConnection(new URL(url), METHOD_POST, contentType, proxy, charset, connectTimeout, readTimeout);
		OutputStream outputStream = conn.getOutputStream();
		if (paramObject != null && paramObject != "") {
			if (paramObject instanceof Map) {
				byte[] bytes = JSON.toJSONBytes(paramObject, features);
				outputStream.write(bytes);
			} else if (paramObject instanceof Byte) {
				byte[] bytes = (byte[]) paramObject;
				outputStream.write(bytes);
			}
		}
		outputStream.flush();
		outputStream.close();

		// 获得响应状态
		int resultCode = conn.getResponseCode();
		StringBuffer sb = new StringBuffer();
		if (HttpURLConnection.HTTP_OK == resultCode) {
			String readLine = new String();
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
			while ((readLine = responseReader.readLine()) != null) {
				sb.append(readLine).append("\n");
			}
			responseReader.close();
		}
		return sb.toString();
	}

	public static String doGetSend(String url, Object paramObject, String contentType, String charset, int connectTimeout, int readTimeout,
			Proxy proxy) throws MalformedURLException, IOException {
		// Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));

		HttpURLConnection conn = getConnection(new URL(url), METHOD_GET, contentType, proxy, charset, connectTimeout, readTimeout);
		OutputStream outputStream = conn.getOutputStream();
		if (paramObject != null && paramObject != "") {
			if (paramObject instanceof Map) {
				byte[] bytes = JSON.toJSONBytes(paramObject, features);
				outputStream.write(bytes);
			} else if (paramObject instanceof Byte) {
				byte[] bytes = (byte[]) paramObject;
				outputStream.write(bytes);
			}
		}
		outputStream.flush();
		outputStream.close();

		// 获得响应状态
		int resultCode = conn.getResponseCode();
		StringBuffer sb = new StringBuffer();
		if (HttpURLConnection.HTTP_OK == resultCode) {
			String readLine = new String();
			BufferedReader responseReader = new BufferedReader(new InputStreamReader(conn.getInputStream(), charset));
			while ((readLine = responseReader.readLine()) != null) {
				sb.append(readLine).append("\n");
			}
			responseReader.close();
		}
		return sb.toString();
	}

	public static HttpURLConnection getConnection(URL url, String method, String contentType, Proxy proxy, String charset,
			int connectTimeout, int readTimeout) throws IOException {
		HttpURLConnection conn = null;
		if ("https".equals(url.getProtocol())) {
			HttpsURLConnection connHttps = null;
			if (proxy != null) {
				connHttps = (HttpsURLConnection) url.openConnection(proxy);
			} else {
				connHttps = (HttpsURLConnection) url.openConnection();
			}
			connHttps.setSSLSocketFactory(socketFactory);
			connHttps.setHostnameVerifier(verifier);
			conn = connHttps;
		} else {
			conn = null;
			if (proxy != null) {
				conn = (HttpURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
		}

		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);

		conn.setRequestMethod(method);
		conn.setDoInput(true);
		conn.setDoOutput(true);
		conn.setRequestProperty("Accept", "text/xml,text/javascript,text/html");
		conn.setRequestProperty("User-Agent", "aop-sdk-java");
		conn.setRequestProperty("Content-Type", contentType);
		conn.setRequestProperty("Charset", charset);
		return conn;
	}

	public static String getParams() {
		String paramStr = "";
		Iterator entries = paramsMap.entrySet().iterator();
		Map.Entry entry = null;
		int i = 0;
		while (entries.hasNext()) {
			entry = (Entry) entries.next();
			if (i == 0) {
				paramStr += entry.getKey() + "=" + entry.getValue();
			} else {
				paramStr += "&" + entry.getKey() + "=" + entry.getValue();
			}
			i++;
		}
		return paramStr;
	}
}
