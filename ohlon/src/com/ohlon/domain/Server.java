package com.ohlon.domain;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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
		} catch (JSONException e) {
			log.error(e);
		}
	}

	public Set<BatchInstance> getActiveBatchInstancesDetails() {
		Set<BatchInstance> result = new HashSet<BatchInstance>();

		try {
			String url = this.jolokiaUrl + "/exec/ephesoft:type=batchinstance-stats/getActiveBatchInstancesList()/";
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			HttpResponse response = client.execute(request);
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

		} catch (Exception e) {
			log.error(e);
		}

		return result;
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

	@Override
	public String toString() {
		return "Server ID: " + this.id + "; Label: " + this.label + "; Jolokia URL: " + this.jolokiaUrl + "; Data URL: " + this.dataUrl;
	}

}
