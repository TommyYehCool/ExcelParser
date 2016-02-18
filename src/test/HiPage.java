package test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HiPage {
	private final static String urlInedx = "https://www.iyp.com.tw";
	private final static String urlSearch = "https://www.iyp.com.tw/search.php";
	private final static String searchKey = "七河企業有限公司";
	private final static String searchLocation = "0";
	public static void main(String[] args) {
		try {
			// SSL 連線使用
			enableSSLSocket();
			// 先連首頁 為了取得cookies
			Connection.Response execute;
			try {
				execute = Jsoup
		                .connect(urlInedx)
		                .method(Connection.Method.GET).execute();

				Document document = 
						Jsoup.connect(urlSearch)
						.data("a_id", searchLocation)
						.data("k", searchKey)
						.cookies(execute.cookies())
						.timeout(100000).get();

				System.out.println(document);
			}
			catch (IOException e) {
				e.printStackTrace();
			}

		}
		catch (KeyManagementException e) {
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	public static void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
		HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new X509TrustManager[] { new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		} }, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
	}
}
