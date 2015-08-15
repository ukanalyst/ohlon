package com.ohlon.ephesoft.jmx;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ohlon.ephesoft.logger.LogEntry;
import com.ohlon.ephesoft.logger.OhlonLogger;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=ohlon-logger", description = "Ohlon In-Memory logger")
public class LoggerExporter {

	private static final Logger log = Logger.getLogger(LoggerExporter.class.getName());

	private LicenseService licenseService;

	@ManagedOperation(description = "Get the number of log entries")
	public int getLogListSize() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		return OhlonLogger.logList.getAll().size();
	}

	@ManagedOperation(description = "Get all log entries")
	public String getLogEntries() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get log entries");

		JSONArray entries = new JSONArray();
		List<LogEntry> logEntries = OhlonLogger.logList.getAll();
		for (LogEntry logEntry : logEntries) {
			entries.put(logEntry.getJsonObject());
		}

		log.debug("# of returned entries: " + entries.length());

		return entries.toString();
	}

	public boolean deleteLogEntry(long id) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return false;
		}

		log.debug("Delete log entry: id=" + id);

		OhlonLogger.logList.remove(id);
		return true;
	}

	public boolean emptyLogEntries() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return false;
		}

		log.debug("Empty all log entries");

		OhlonLogger.logList.empty();
		return true;
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
