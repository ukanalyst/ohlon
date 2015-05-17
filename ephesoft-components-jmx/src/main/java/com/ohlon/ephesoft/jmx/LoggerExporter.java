package com.ohlon.ephesoft.jmx;

import java.util.List;

import org.json.JSONArray;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ohlon.ephesoft.logger.LogEntry;
import com.ohlon.ephesoft.logger.OhlonLogger;

@Component
@ManagedResource(objectName = "ephesoft:type=ohlon-logger", description = "Ohlon In-Memory logger")
public class LoggerExporter {

	@ManagedOperation(description = "Get the number of log entries")
	public int getLogListSize() {
		return OhlonLogger.logList.getAll().size();
	}
	
	@ManagedOperation(description = "Get all log entries")
	public String getLogEntries() {
		JSONArray entries = new JSONArray();
		List<LogEntry> logEntries = OhlonLogger.logList.getAll();
		for (LogEntry logEntry : logEntries) {
			entries.put(logEntry.getJsonObject());
		}
		return entries.toString();
	}
	
	public boolean deleteLogEntry(long id) {
		OhlonLogger.logList.remove(id);
		return true;
	}
	
	public boolean emptyLogEntries() {
		OhlonLogger.logList.empty();
		return true;
	}

}
