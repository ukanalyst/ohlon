package com.ohlon.ephesoft.service;

import java.net.NetworkInterface;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class LicenseServiceImpl implements LicenseService {

	private static final Logger log = Logger.getLogger(LicenseServiceImpl.class.getName());

	private Calendar lastCheck;
	private long MAX_DELAY = 6 * 60 * 60 * 1000; // 6 hours
	private String LICENSE_SERVER_URL = "https://license.ohlon.com/license/check";
	private boolean IS_ACTIVE;
	private String MESSAGE;

	public void init() {
		lastCheck = Calendar.getInstance();
		IS_ACTIVE = false;
		MESSAGE = "";
		checkLicense(true);
	}

	public boolean checkLicense() {
		return checkLicense(false);
	}

	private boolean checkLicense(boolean force) {

		log.debug("Checking license");

		Calendar now = Calendar.getInstance();
		long diff = Math.abs(now.getTimeInMillis() - lastCheck.getTimeInMillis());

		log.debug("Last check happened: " + lastCheck.getTime());

		if (diff > MAX_DELAY || force) {
			// Re-check
			String macAdress = getMACAddress();

			if (macAdress.length() == 0) {
				log.error("Impossible to retrieve the MAC address.");
				IS_ACTIVE = false;
				MESSAGE = "Error to retrieve the MAC address.";
				return false;
			}

			log.debug("MAC: " + macAdress);

			try {
				// Send a query to the licensing server
				JSONObject data = new JSONObject();
				data.put("mac", macAdress);

				log.debug("Query body: " + data);

				// Allow access even though certificate is self signed
				Protocol easyHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 443);
				Protocol.registerProtocol("https", easyHttps);
				
				HttpClient client = new HttpClient();
				PostMethod post = new PostMethod(LICENSE_SERVER_URL);
				post.setRequestEntity(new StringRequestEntity(data.toString()));
				post.setRequestHeader("Content-type", "application/json");
				int responseStatus = client.executeMethod(post);

				log.debug("Response status: " + responseStatus);

				if (responseStatus != 200) {
					IS_ACTIVE = false;
					MESSAGE = "Error connecting to the license server.";
				} else {
					// Get the body
					String body = IOUtils.toString(post.getResponseBodyAsStream());

					log.debug("Response body: " + body);

					JSONObject jsonResponse = new JSONObject(body);
					if (jsonResponse.has("valid")) {
						IS_ACTIVE = jsonResponse.getBoolean("valid");
						MESSAGE = jsonResponse.getString("message");
					} else {
						log.error("There is no 'valid' key in the body.");
						IS_ACTIVE = false;
						MESSAGE = "Error parsing query result from the license server.";
					}

					if (jsonResponse.has("message"))
						log.info(jsonResponse.getString("message"));

				}

				post.releaseConnection();

				lastCheck = now;

			} catch (Exception e) {
				IS_ACTIVE = false;
				log.error("Fail checking license", e);
			}

			log.debug("Is active = " + IS_ACTIVE);
			return IS_ACTIVE;
		} else {
			log.debug("Is active = " + IS_ACTIVE);
			return IS_ACTIVE;
		}
	}

	private String getMACAddress() {
		String key = "";
		try {
			List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
			for (NetworkInterface networkInterface : interfaces) {
				byte[] mac = networkInterface.getHardwareAddress();
				if (mac != null) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < mac.length; i++) {
						sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
					}
					if (key.length() > 0)
						key += "|";
					key += sb.toString();
				}
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}
		return key;
	}
	
	public String getMessage() {
		return MESSAGE;
	}
}
