package com.ohlon.ephesoft.service;

public class LicenseServiceImpl implements LicenseService {

	private String MESSAGE;

	public void init() {
		MESSAGE = "You are using the unlimited open source version.";
	}

	public boolean checkLicense() {
		// Do Nothing
		return true;
	}

	public String getMessage() {
		return MESSAGE;
	}
}
