package com.ohlon.service;

import java.util.List;

import com.ohlon.domain.Server;

public interface ServerService {

	List<Server> getAvailableServers();

	Server getServer(String serverId);
}
