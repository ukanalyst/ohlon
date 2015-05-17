package com.ohlon.ephesoft.logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LogList {

	/**
	 * Specifies the number of log messages appended. Is incremented each time,
	 * a log is added to the linked list
	 */
	private int usedSize = 0;

	/**
	 * Size of the hash map
	 */
	private int capacity;
	private Map<Long, LogEntry> map;
	private LinkedList<LogEntry> linkedList = new LinkedList<LogEntry>();
	private static final DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");

	public LogList() {
	}

	public void setSize(int hashMapSize) {
		this.capacity = hashMapSize;
		map = new HashMap<Long, LogEntry>(hashMapSize);
	}

	/**
	 * Inserts log messages to the linked list and the hash map. If the number
	 * of log messages exceed the hashMapSize, new logs will be added after
	 * removing, the oldest log from the linked list and the hash map.
	 * 
	 * @param log
	 *            the log entries
	 */
	public synchronized void insert(LogEntry log) {
		if (usedSize > capacity) {
			map.remove(linkedList.removeFirst().getId());
			map.put(log.getId(), log);
			linkedList.add(log);
		} else {
			linkedList.add(log);
			map.put(log.getId(), log);
			usedSize++;
		}
	}

	/**
	 * Remove a log entry
	 * 
	 * @param logId
	 *            LogEntry id
	 */
	public synchronized void remove(long logEntryId) {
		LogEntry entry = map.get(logEntryId);
		if (entry != null) {
			map.remove(logEntryId);
			linkedList.remove(entry);
			usedSize--;
		}
	}

	public List<LogEntry> formatFields(List<LogEntry> list) {
		int i = 0;
		LogEntry entry;
		String formattedString;

		while (i < list.size()) {
			entry = list.get(i);
			if (entry.getFormattedTime() == null) {
				synchronized (dfm) {
					formattedString = dfm.format(entry.getTimeStamp());
					entry.setFormattedTime(formattedString);
				}
			}

			formattedString = entry.getMessage().replaceAll("( ){2}?", "  ");
			entry.setMessage(formattedString);
			i++;
		}
		return list;
	}

	/**
	 * When called returns the entire list of logs
	 * 
	 * @return linkedList the list containing the logs
	 */
	public List<LogEntry> getAll() {
		if (linkedList != null) {
			return formatFields(linkedList);
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * When called returns a sub list of the logs starting from the specified
	 * log id
	 * 
	 * @param start
	 *            starting id
	 * @return list the list containing the sub list
	 */
	public List<LogEntry> getListFrom(long start) {
		if (map.get(start) != null) {
			List<LogEntry> list = new ArrayList<LogEntry>();
			long i = start;
			while (map.get(i) != null) {
				list.add(map.get(i));
				i++;
			}
			return formatFields(list);
		} else {
			return Collections.emptyList();
		}
	}

	public void empty() {
		linkedList = new LinkedList<LogEntry>();
		map = new HashMap<Long, LogEntry>(capacity);
		usedSize = 0;
	}
}