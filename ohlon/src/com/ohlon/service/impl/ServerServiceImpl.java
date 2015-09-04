package com.ohlon.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import com.ohlon.domain.Server;
import com.ohlon.service.ServerService;

@Service("serverService")
public class ServerServiceImpl implements ServerService {

	private Log log = LogFactory.getLog(ServerServiceImpl.class);

	@Autowired
	private ResourceLoader resourceLoader;

	private static Map<String, Server> servers = new HashMap<String, Server>();
	private Server firstLoadedServer;
	private JSONObject reportsDefinition = null;

	@Override
	public List<Server> getAvailableServers() {

		// Populate server list if required
		if (ServerServiceImpl.servers.keySet().size() == 0)
			populateServerList();

		ArrayList<Server> list = new ArrayList<Server>(servers.values());
		Collections.sort(list, new Comparator<Server>() {
			public int compare(Server s1, Server s2) {
				return s1.getId().compareTo(s2.getId());
			}
		});

		return list;
	}

	@Override
	public Server getServer(String serverId) {
		// Populate server list if required
		if (ServerServiceImpl.servers.keySet().size() == 0)
			populateServerList();

		if (serverId == null)
			return firstLoadedServer;

		return servers.get(serverId);
	}

	@Override
	public JSONObject getAvailableReports() {
		if (reportsDefinition == null)
			populateReportsDefinition();
		return reportsDefinition;
	}

	private void populateServerList() {
		Resource resource = resourceLoader.getResource("classpath:config/servers.json");
		try {
			String jsonData = IOUtils.toString(resource.getInputStream());
			JSONObject data = new JSONObject(jsonData);
			JSONObject servers = data.getJSONObject("servers");
			JSONArray serverIdNames = servers.names();
			List<String> serverIds = new ArrayList<String>();

			for (int i = 0; i < serverIdNames.length(); i++)
				serverIds.add(serverIdNames.getString(i));

			Collections.sort(serverIds);

			for (String serverId : serverIds) {
				log.debug("Load server: " + serverId);
				Server server = new Server(serverId, servers.getJSONObject(serverId));
				ServerServiceImpl.servers.put(serverId, server);

				if (firstLoadedServer == null)
					firstLoadedServer = server;
			}

		} catch (Exception e) {
			log.error("An error occured", e);
		}
	}

	private void populateReportsDefinition() {
		Resource resource = resourceLoader.getResource("classpath:config/reports.json");
		try {
			String jsonData = IOUtils.toString(resource.getInputStream());
			reportsDefinition = new JSONObject(jsonData);
		} catch (Exception e) {
			log.error("An error occured", e);
		}
	}
}
