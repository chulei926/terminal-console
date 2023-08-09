package com.leichu.terminal.console.interactive.soap;

import com.leichu.terminal.console.interactive.config.ConfigFactory;
import com.leichu.terminal.console.interactive.config.SoapConfig;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * SOAP客户端.
 * <p>基于Okhttp实现.</p>
 *
 * @author leichu.
 * @since 2023-07-30.
 */
public class SoapClient {

	private static final Logger logger = LoggerFactory.getLogger(SoapClient.class);

	private static volatile SoapClient instance = null;

	private static volatile OkHttpClient httpClient = null;

	public static final MediaType XML_CONTENT_TYPE = MediaType.parse("text/xml; charset=utf-8");

	public static final Integer OK = 200;

	private SoapClient() {
		SoapConfig soapConfig = ConfigFactory.getInstance().getSoapConfig();
		TrustManager[] trustManagers = buildTrustManagers();
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
				.connectTimeout(soapConfig.getConnectTimeout(), TimeUnit.SECONDS)
				.writeTimeout(soapConfig.getWriteTimeout(), TimeUnit.SECONDS)
				.readTimeout(soapConfig.getReadTimeout(), TimeUnit.SECONDS)
				.sslSocketFactory(createSSLSocketFactory(trustManagers), (X509TrustManager) trustManagers[0])
				.hostnameVerifier((hostName, session) -> true)
				.retryOnConnectionFailure(soapConfig.getRetryOnConnectionFailure())
				.addInterceptor(new LogInterceptor());
		httpClient = builder.build();
	}

	private static SSLSocketFactory createSSLSocketFactory(TrustManager[] trustAllCerts) {
		SSLSocketFactory ssfFactory = null;
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new SecureRandom());
			ssfFactory = sc.getSocketFactory();
		} catch (Exception e) {
			logger.error("createSSLSocketFactory error!", e);
		}
		return ssfFactory;
	}

	private static TrustManager[] buildTrustManagers() {
		return new TrustManager[]{
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] chain, String authType) {
					}

					@Override
					public void checkServerTrusted(X509Certificate[] chain, String authType) {
					}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[]{};
					}
				}
		};
	}

	public static SoapClient getInstance() {
		if (null == instance) {
			synchronized (SoapClient.class) {
				if (null == instance) {
					instance = new SoapClient();
				}
			}
		}
		return instance;
	}


	public Response post(String url, String xmlBody, Map<String, String> headers) throws Exception {
		RequestBody body = RequestBody.create(xmlBody, XML_CONTENT_TYPE);
		HttpUrl.Builder urlBuilder = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
		Request.Builder builder = new Request.Builder()
				.url(urlBuilder.build().toString())
				.post(body);
		if (null != headers) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.header(entry.getKey(), entry.getValue());
			}
		}
		return httpClient.newCall(builder.build()).execute();
	}

	public static String extractResponseBody(Response response) {
		String body = "";
		if (null == response || null == response.body()) {
			return body;
		}
		try {
			BufferedSource source = response.body().source();
			source.request(Long.MAX_VALUE);
			Buffer buffer = source.buffer();
			body = buffer.clone().readString(StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error("Extract response body error！", e);
		}
		return body;
	}


	private static final class LogInterceptor implements Interceptor {

		@Override
		public Response intercept(Chain chain) throws IOException {
			Instant start = Instant.now();
			Request originalRequest = chain.request();
			String url = originalRequest.url().toString();
			Response response = null;
			String requestBodyString = "";
			try {
				if (null != originalRequest.body()) {
					Buffer buffer = new Buffer();
					RequestBody requestBody = originalRequest.body();
					requestBody.writeTo(buffer);
					requestBodyString = buffer.readString(StandardCharsets.UTF_8);
				}
				response = chain.proceed(originalRequest);
				Instant end = Instant.now();
				String responseBodyString = "";
				if (null != response.body()) {
					responseBodyString = extractResponseBody(response); // clone buffer before reading from it
				}
				logger.info(">>>>>>>>>>>>>>>>>>>> Http request success >>>>>>>>>>>>>>>>>>>> URL:{} Method:{} RequestBody:{} Response:{} Cost:{}ms",
						url, originalRequest.method(), requestBodyString, responseBodyString, Duration.between(start, end).toMillis());
			} catch (Exception e) {
				logger.error(">>>>>>>>>>>>>>>>>>>> Http request error >>>>>>>>>>>>>>>>>>>> URL:{} Method:{}  RequestBody:{}",
						url, originalRequest.method(), requestBodyString, e);
			}
			return response;
		}
	}

}
