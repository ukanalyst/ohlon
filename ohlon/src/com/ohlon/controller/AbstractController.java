package com.ohlon.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.apache.commons.codec.binary.Base64;
import com.ohlon.domain.Server;
import com.ohlon.service.ServerService;

public abstract class AbstractController {

	private JSONArray serverData = null;

	@Autowired
	private ServerService serverService;

	@Value("${batchinstance.hideDelay}")
	private String batchinstanceHideDelay;

	@Autowired
	private ResourceLoader resourceLoader;

	protected Map<String, Object> generateParams(String serverId) {
		Map<String, Object> params = new HashMap<String, Object>();
		try {
			JSONArray servers = getServerData();
			String currentId = serverId;
			Server currentServer = serverService.getServer(currentId);
			if (currentServer == null) {
				currentId = servers.getJSONObject(0).getString("id");
				currentServer = serverService.getServer(currentId);
			}

			params.put("jolokia", currentServer.getJolokiaUrl());
			params.put("dataUrl", currentServer.getDataUrl());
			params.put("batchinstanceHideDelay", batchinstanceHideDelay);
			params.put("servers", servers);
			params.put("currentId", currentId);

			if (currentServer.getUsername() != null) {
				String authString = currentServer.getUsername() + ":" + currentServer.getPassword();
				params.put("auth", new String(Base64.encodeBase64(authString.getBytes())));
			} else
				params.put("auth", "");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}

	protected Map<String, Object> generateParams() {
		String currentId = null;
		try {
			JSONArray servers = getServerData();
			currentId = servers.getJSONObject(0).getString("id");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generateParams(currentId);
	}

	protected JSONArray getServerData() {
		if (this.serverData == null || this.serverData.length() == 0) {

			List<Server> servers = serverService.getAvailableServers();
			JSONArray data = new JSONArray();

			try {
				for (Server server : servers) {
					JSONObject serverObj = new JSONObject();
					serverObj.put("label", server.getLabel());
					serverObj.put("id", server.getId());
					data.put(serverObj);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			this.serverData = data;

		}
		return this.serverData;
	}
}
