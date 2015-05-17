package com.ohlon.ephesoft.logger;

import java.util.Arrays;

import org.apache.commons.lang.time.FastDateFormat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LogEntry {

	private long id;
	private String message;
	private String threadName;
	private long timeStamp;
	private String loggerName;
	private String ip;
	private String host;
	private String formattedTime;
	private String logLevel;
	private String[] throwableStrRep;
	private static final FastDateFormat dfm = FastDateFormat.getInstance("yyyy-MM-dd' 'HH:mm:ss");

	public LogEntry() {
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFormattedTime() {
		return formattedTime;
	}

	public void setFormattedTime(String formattedTime) {
		this.formattedTime = formattedTime;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public String[] getThrowableStrRep() {
		return throwableStrRep;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLogLevel() {
		return logLevel;
	}

	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	public void setThrowable(String[] throwableStrRep) {
		this.throwableStrRep = throwableStrRep;
	}

	public JSONObject getJsonObject() {
		String date;
		synchronized (this) {
			date = dfm.format(timeStamp);
		}

		JSONObject data = new JSONObject();
		try {
			data.put("id", id);
			data.put("date", date);
			data.put("threadName", threadName);
			data.put("logLevel", logLevel);
			data.put("loggerName", loggerName);
			data.put("message", message);
			if (throwableStrRep != null)
				data.put("throwableStrRep", new JSONArray(Arrays.asList(throwableStrRep)));
			else
				data.put("throwableStrRep", new JSONArray());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return data;
	}

	@Override
	public String toString() {

		String date;

		synchronized (this) {
			date = dfm.format(timeStamp);
		}
		if (host != null && ip != null) {
			return date + " [" + threadName + "] " + " [" + host + "-" + ip + "] " + logLevel + " " + loggerName + " " + message;
		} else if (host != null && ip == null) {
			return date + " [" + threadName + "] " + " [" + host + "] " + logLevel + " " + loggerName + " " + message;
		} else if (host == null && ip != null) {
			return date + " [" + threadName + "] " + " [" + ip + "] " + logLevel + " " + loggerName + " " + message;
		} else {
			return date + " [" + threadName + "] " + logLevel + " " + loggerName + " " + message;
		}
	}

}
