package com.ohlon.domain;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

public class BatchInstance {

	private Log log = LogFactory.getLog(BatchInstance.class);

	private String identifier;
	private String status;
	private Server server;

	public BatchInstance(JSONObject data) {
		try {
			if (data.has("identifier"))
				this.identifier = data.getString("identifier");
			if (data.has("status"))
				this.status = data.getString("status");
		} catch (JSONException e) {
			log.error(e);
		}
	}

	public String getIdentifier() {
		return identifier;
	}

	public String getStatus() {
		return status;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof BatchInstance)
			return ((BatchInstance) obj).getIdentifier().equalsIgnoreCase(identifier);
		else
			return false;
	}
}
