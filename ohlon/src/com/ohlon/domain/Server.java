package com.ohlon.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Server {

	private String id;
	private String label;
	private String jolokiaUrl;
	private String dataUrl;
	private String dcmaUrl;
	private String username;
	private String password;
	private String[] serverNames;
	private String[] pages;

	private Log log = LogFactory.getLog(Server.class);

	public Server(String serverId, JSONObject data) {
		this.id = serverId;
		try {
			if (data.has("label"))
				this.label = data.getString("label");
			if (data.has("jolokia"))
				this.jolokiaUrl = data.getString("jolokia");
			if (data.has("data"))
				this.dataUrl = data.getString("data");
			if (data.has("dcma"))
				this.dcmaUrl = data.getString("dcma");
			if (data.has("username"))
				this.username = data.getString("username");
			if (data.has("password"))
				this.password = data.getString("password");
			if (data.has("server_name")) {
				JSONArray config = data.getJSONArray("server_name");
				this.serverNames = new String[config.length()];
				for (int i = 0; i < config.length(); i++)
					this.serverNames[i] = config.getString(i);
			} else
				this.serverNames = new String[0];
			if (data.has("pages")) {
				if (data.get("pages") instanceof JSONArray) {
					JSONArray config = data.getJSONArray("pages");
					this.pages = new String[config.length()];
					for (int i = 0; i < config.length(); i++)
						this.pages[i] = config.getString(i);
				} else {
					Object page = data.get("pages");
					this.pages = new String[1];
					this.pages[0] = page.toString();
				}
			} else
				this.pages = new String[0];
		} catch (JSONException e) {
			log.error(e);
		}
	}

	public boolean isRunning() {
		boolean result = true;

		RequestConfig config = RequestConfig.custom().setSocketTimeout(1000).setConnectTimeout(1000).build();

		try {
			String url = this.jolokiaUrl + "/exec/ephesoft:type=application-details/getApplicationDetails()/";
			HttpClient client = HttpClientBuilder.create().disableAutomaticRetries().setDefaultRequestConfig(config).setDefaultCredentialsProvider(getCredentialsProvider()).build();
			HttpGet request = new HttpGet(url);
			client.execute(request);
		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	public Set<BatchInstance> getActiveBatchInstancesDetails() {
		Set<BatchInstance> result = new HashSet<BatchInstance>();

		try {
			String url = this.jolokiaUrl + "/exec/ephesoft:type=batchinstance-stats/getActiveBatchInstancesList()/";
			HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(getCredentialsProvider()).build();
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == 200) {
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String dataStr = IOUtils.toString(rd);
				JSONObject data = new JSONObject(dataStr);
				if (data.has("value")) {
					JSONArray batchInstances = new JSONArray(data.getString("value"));

					for (int i = 0; i < batchInstances.length(); i++) {
						BatchInstance batchInstance = new BatchInstance(batchInstances.getJSONObject(i));
						batchInstance.setServer(this);
						result.add(batchInstance);
					}
				}
			}
		} catch (Exception e) {
			log.error(e);
		}

		return result;
	}

	private CredentialsProvider getCredentialsProvider() {
		if (username != null && username.length() > 0) {
			CredentialsProvider provider = new BasicCredentialsProvider();
			UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
			provider.setCredentials(AuthScope.ANY, credentials);
		}
		return null;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getJolokiaUrl() {
		return jolokiaUrl;
	}

	public String getDataUrl() {
		return dataUrl;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setJolokiaUrl(String jolokiaUrl) {
		this.jolokiaUrl = jolokiaUrl;
	}

	public void setDataUrl(String dataUrl) {
		this.dataUrl = dataUrl;
	}

	public String getDcmaUrl() {
		return dcmaUrl;
	}

	public void setDcmaUrl(String dcmaUrl) {
		this.dcmaUrl = dcmaUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String[] getServerNames() {
		return serverNames;
	}

	public void setServerNames(String[] serverNames) {
		this.serverNames = serverNames;
	}

	public String[] getPages() {
		return pages;
	}

	public void setPages(String[] pages) {
		this.pages = pages;
	}

	@Override
	public String toString() {
		return "Server ID: " + this.id + "; Label: " + this.label + "; Jolokia URL: " + this.jolokiaUrl + "; Data URL: " + this.dataUrl;
	}

}
