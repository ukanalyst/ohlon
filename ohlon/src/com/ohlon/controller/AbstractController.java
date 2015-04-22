package com.ohlon.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public abstract class AbstractController {

	private JSONObject serverData = null;

	@Value("${batchinstance.hideDelay}")
	private String batchinstanceHideDelay;

	@Autowired
	private ResourceLoader resourceLoader;

	protected Map<String, Object> generateParams(String serverId) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			JSONObject data = getServerData();
			JSONObject servers = data.getJSONObject("servers");
			JSONArray serverIds = servers.names();
			JSONObject currentServer = null;
			String currentId = serverId;
			if (serverId != null && serverId.length() > 0 && servers.has(serverId))
				currentServer = servers.getJSONObject(serverId);
			else {
				currentId = serverIds.getString(0);
				currentServer = servers.getJSONObject(currentId);
			}

			params.put("jolokia", currentServer.getString("jolokia"));
			params.put("dataUrl", currentServer.getString("data"));
			params.put("batchinstanceHideDelay", batchinstanceHideDelay);
			params.put("servers", servers);
			params.put("currentId", currentId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}

	protected Map<String, Object> generateParams() {
		String currentId = null;
		try {
			JSONObject data = getServerData();
			JSONObject servers = data.getJSONObject("servers");
			JSONArray serverIds = servers.names();
			currentId = serverIds.getString(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generateParams(currentId);
	}

	private JSONObject getServerData() {
		if (this.serverData == null) {
			Resource servers = resourceLoader.getResource("classpath:config/servers.json");
			try {
				String jsonData = IOUtils.toString(servers.getInputStream());
				JSONObject data = new JSONObject(jsonData);
				this.serverData = data;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.serverData;
	}
}
