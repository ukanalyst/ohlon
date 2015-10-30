package com.ohlon.ephesoft.logger;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class OhlonLogger extends AppenderSkeleton {

	private volatile long id = 0;
	public final static LogList logList = new LogList();

	public void close() {
		if (this.closed) {
			return;
		}
		this.closed = true;
	}

	public void setSize(int size) {
		logList.setSize(size);
	}

	public boolean requiresLayout() {
		return false;
	}

	@Override
	protected void append(LoggingEvent event) {
		if (event.getLevel() == Level.ERROR) {
			int index = event.getLoggerName().lastIndexOf('.');
			String loggerName;

			if (index > -1) {
				loggerName = event.getLoggerName().substring(index + 1);
			} else {
				loggerName = event.getLoggerName();
			}

			LogEntry log = new LogEntry();
			log.setId(id++);
			log.setHost(event.getProperty("host"));
			log.setIp(event.getProperty("ip"));
			log.setLoggerName(loggerName);
			log.setMessage((String) event.getMessage());
			log.setThreadName(event.getThreadName());
			log.setTimeStamp(event.getTimeStamp());
			log.setLogLevel(event.getLevel().toString());
			log.setThrowable(event.getThrowableStrRep());
			logList.insert(log);
		}
	}

}
