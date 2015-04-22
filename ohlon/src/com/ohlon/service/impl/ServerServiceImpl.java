package com.ohlon.service.impl;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.ohlon.domain.Server;
import com.ohlon.service.ServerService;

public class ServerServiceImpl implements ServerService {

	private Log log = LogFactory.getLog(ServerServiceImpl.class);

	@Autowired
	private ResourceLoader resourceLoader;

	@Override
	public Set<Server> getAvailableServers() {
		Resource resource = resourceLoader.getResource("classpath:config/servers.json");
		Set<Server> result = new HashSet<Server>();
		try {
			String jsonData = IOUtils.toString(resource.getInputStream());
			JSONObject data = new JSONObject(jsonData);
			JSONObject servers = data.getJSONObject("servers");
			JSONArray serverIds = servers.names();

			for (int i = 0; i < serverIds.length(); i++) {
				String serverId = serverIds.getString(i);
				log.debug("Load server: " + serverId);
				result.add(new Server(serverId, servers.getJSONObject(serverId)));
			}

		} catch (Exception e) {
			log.error(e);
		}
		return result;
	}
}
