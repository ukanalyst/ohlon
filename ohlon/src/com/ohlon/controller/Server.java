package com.ohlon.controller;

public class Server {

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		if (id != null && id.length() > 0)
			this.id = id;
	}
}
