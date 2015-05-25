package com.ohlon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ohlon.ephesoft.db.utils.DBUtils;

@Component
@ManagedResource(objectName = "ephesoft:type=server-status", description = "Server status reporting")
public class ServerStatus {

	@ManagedOperation(description = "Get server statistics over a month")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "month", description = "Month in format (MM-YYYY)") })
	public String getMonthlyStatistics(String month, String server_names) {

		JSONArray data = new JSONArray();
		String[] serverNames = new String[0];
		if (!server_names.equalsIgnoreCase("na"))
			serverNames = server_names.split("\\|");

		try {
			SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat dateFormat = new SimpleDateFormat("MM-yyyy");
			Date monthDate = dateFormat.parse(month);

			// Get the first day of the month
			Calendar firstDayOfTheMonth = Calendar.getInstance();
			firstDayOfTheMonth.setTime(monthDate);
			firstDayOfTheMonth.set(Calendar.DAY_OF_MONTH, firstDayOfTheMonth.getActualMinimum(Calendar.DAY_OF_MONTH));
			firstDayOfTheMonth.set(Calendar.HOUR, 0);
			firstDayOfTheMonth.set(Calendar.MINUTE, 0);
			firstDayOfTheMonth.set(Calendar.SECOND, 0);

			// Get the last day of the month
			Calendar lastDayOfTheMonth = Calendar.getInstance();
			lastDayOfTheMonth.setTime(firstDayOfTheMonth.getTime());
			lastDayOfTheMonth.set(Calendar.DAY_OF_MONTH, firstDayOfTheMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
			lastDayOfTheMonth.set(Calendar.HOUR, 23);
			lastDayOfTheMonth.set(Calendar.MINUTE, 59);
			lastDayOfTheMonth.set(Calendar.SECOND, 59);

			Connection c = DBUtils.getReportDBConnection();
			PreparedStatement statement;

			if (serverNames.length == 0) {
				// Create the sql query
				String sql = "SELECT start_hour, end_hour FROM server_status WHERE (start_hour <= ? AND end_hour >= ?) OR (start_hour >= ? AND end_hour <= ?) OR (start_hour <= ? AND end_hour >= ?) ORDER BY start_hour, end_hour";
				statement = c.prepareStatement(sql);
				statement.setTimestamp(1, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(2, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(3, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(4, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(5, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(6, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));
			} else {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < serverNames.length; i++)
					builder.append("?,");
				builder.deleteCharAt(builder.length() - 1);

				// Create the sql query
				String sql = "SELECT start_hour, end_hour FROM server_status WHERE ((start_hour <= ? AND end_hour >= ?) OR (start_hour >= ? AND end_hour <= ?) OR (start_hour <= ? AND end_hour >= ?)) AND (server_name IN ("
						+ builder.toString() + ")) ORDER BY start_hour, end_hour";
				statement = c.prepareStatement(sql);
				statement.setTimestamp(1, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(2, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(3, new java.sql.Timestamp(firstDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(4, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(5, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));
				statement.setTimestamp(6, new java.sql.Timestamp(lastDayOfTheMonth.getTimeInMillis()));

				for (int i = 0; i < serverNames.length; i++)
					statement.setString(7 + i, serverNames[i]);
			}

			// Execute the query
			ResultSet rs = statement.executeQuery();

			Calendar today = Calendar.getInstance();

			// For the month
			Calendar currentDay = (Calendar) firstDayOfTheMonth.clone();
			boolean isRunning = false;
			boolean hasMoreRows = rs.next();
			while (currentDay.get(Calendar.MONTH) == firstDayOfTheMonth.get(Calendar.MONTH) && isBefore(currentDay, today)) {
				JSONObject day = new JSONObject();
				day.put("date", outputFormat.format(currentDay.getTime()));
				day.put("time", 0);

				boolean isEndOfTheDay = false;

				Calendar beginningOfTheDay = (Calendar) currentDay.clone();
				beginningOfTheDay.set(Calendar.HOUR, 0);
				beginningOfTheDay.set(Calendar.MINUTE, 0);
				beginningOfTheDay.set(Calendar.SECOND, 0);

				Calendar endOfTheDay = (Calendar) currentDay.clone();
				endOfTheDay.set(Calendar.HOUR, 23);
				endOfTheDay.set(Calendar.MINUTE, 59);
				endOfTheDay.set(Calendar.SECOND, 59);

				if (rs.isAfterLast()) {
					// Nothing happens, same status as previously
					if (isRunning) {
						day.put("availability", 1);
					} else {
						day.put("availability", 0);
					}
				} else {

					Calendar slotStart = Calendar.getInstance();
					slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());

					Calendar slotEnd = Calendar.getInstance();
					slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());

					if (isAfter(slotStart, endOfTheDay)) {
						// Nothing happens, same status as previously
						if (isRunning) {
							day.put("availability", 1);
							day.put("time", 86400);
						} else {
							day.put("availability", 0);
						}
					} else {
						long secondsActive = 0;

						// Check if the current slot started before
						if (isBefore(slotStart, beginningOfTheDay)) {
							// Check if the current slot finished after
							// today
							if (isAfter(slotEnd, endOfTheDay)) {
								secondsActive = 86400;
							} else {
								// Check the duration of the current slot
								long slotDuration = (slotEnd.getTimeInMillis() - beginningOfTheDay.getTimeInMillis()) / 1000;
								secondsActive += slotDuration;

								// Check next row
								hasMoreRows = rs.next();
								if (hasMoreRows) {
									// Re-initialize slot start and slot end
									slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());
									slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());
								}
								
								isRunning = false;
							}
						}

						while (hasMoreRows && !isEndOfTheDay && DateUtils.isSameDay(currentDay, slotStart)) {
							// Check if the slot ends the same day
							long slotDuration = 0;
							if (DateUtils.isSameDay(currentDay, slotEnd)) {
								slotDuration = (slotEnd.getTimeInMillis() - slotStart.getTimeInMillis()) / 1000;

								// Check next row
								hasMoreRows = rs.next();
								if (hasMoreRows) {
									// Re-initialize slot start and slot end
									slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());
									slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());
								}

								isRunning = false;
							} else {
								slotDuration = (endOfTheDay.getTimeInMillis() - slotStart.getTimeInMillis()) / 1000;

								isRunning = true;
								isEndOfTheDay = true;
							}
							secondsActive += slotDuration;
						}

						// At the end of the process, compute the
						// availability
						day.put("availability", secondsActive / 86400.0);
						day.put("time", secondsActive);
					}

				}
				currentDay.add(Calendar.DATE, 1);
				data.put(day);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return data.toString();
	}

	@ManagedOperation(description = "Get server statistics over a day")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "day", description = "Day in format (DD-MM-YYYY)") })
	public String getDailyStatistics(String day, String server_names) {

		JSONArray data = new JSONArray();
		String[] serverNames = new String[0];
		if (!server_names.equalsIgnoreCase("na"))
			serverNames = server_names.split("\\|");

		try {
			SimpleDateFormat outputFormat = new SimpleDateFormat("H");
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date currentDay = dateFormat.parse(day);

			// Get the first day of the month
			Calendar beginningOfTheDay = Calendar.getInstance();
			beginningOfTheDay.setTime(currentDay);
			beginningOfTheDay.set(Calendar.HOUR, 0);
			beginningOfTheDay.set(Calendar.MINUTE, 0);
			beginningOfTheDay.set(Calendar.SECOND, 0);

			// Get the last day of the month
			Calendar endOfTheDay = Calendar.getInstance();
			endOfTheDay.setTime(currentDay);
			endOfTheDay.set(Calendar.HOUR, 23);
			endOfTheDay.set(Calendar.MINUTE, 59);
			endOfTheDay.set(Calendar.SECOND, 59);

			Connection c = DBUtils.getReportDBConnection();
			PreparedStatement statement;

			if (serverNames.length == 0) {
				// Create the sql query
				String sql = "SELECT start_hour, end_hour FROM server_status WHERE (start_hour <= ? AND end_hour >= ?) OR (start_hour >= ? AND end_hour <= ?) OR (start_hour <= ? AND end_hour >= ?) ORDER BY start_hour, end_hour";
				statement = c.prepareStatement(sql);
				statement.setTimestamp(1, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(2, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(3, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(4, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));
				statement.setTimestamp(5, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));
				statement.setTimestamp(6, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));
			} else {
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < serverNames.length; i++)
					builder.append("?,");
				builder.deleteCharAt(builder.length() - 1);

				// Create the sql query
				String sql = "SELECT start_hour, end_hour FROM server_status WHERE ((start_hour <= ? AND end_hour >= ?) OR (start_hour >= ? AND end_hour <= ?) OR (start_hour <= ? AND end_hour >= ?)) AND (server_name IN ("
						+ builder.toString() + ")) ORDER BY start_hour, end_hour";
				statement = c.prepareStatement(sql);
				statement.setTimestamp(1, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(2, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(3, new java.sql.Timestamp(beginningOfTheDay.getTimeInMillis()));
				statement.setTimestamp(4, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));
				statement.setTimestamp(5, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));
				statement.setTimestamp(6, new java.sql.Timestamp(endOfTheDay.getTimeInMillis()));

				for (int i = 0; i < serverNames.length; i++)
					statement.setString(7 + i, serverNames[i]);
			}

			// Execute the query
			ResultSet rs = statement.executeQuery();

			// For the month
			Calendar currentHour = (Calendar) beginningOfTheDay.clone();
			boolean isRunning = false;
			boolean hasMoreRows = rs.next();

			while (currentHour.get(Calendar.DATE) == beginningOfTheDay.get(Calendar.DATE) && isBefore(currentHour, endOfTheDay)) {

				JSONObject hour = new JSONObject();
				JSONArray slots = new JSONArray();
				hour.put("time", outputFormat.format(currentHour.getTime()));
				hour.put("slots", slots);

				boolean isEndOfTheHour = false;

				Calendar beginningOfTheHour = (Calendar) currentHour.clone();
				beginningOfTheHour.set(Calendar.MINUTE, 0);
				beginningOfTheHour.set(Calendar.SECOND, 0);

				Calendar endOfTheHour = (Calendar) currentHour.clone();
				endOfTheHour.set(Calendar.MINUTE, 59);
				endOfTheHour.set(Calendar.SECOND, 59);

				if (rs.isAfterLast()) {
					// Nothing happens, same status as previously
					if (isRunning) {
						slots.put(createSlot(3600, true));
					} else {
						slots.put(createSlot(3600, false));
					}
				} else {

					Calendar slotStart = Calendar.getInstance();
					slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());

					Calendar slotEnd = Calendar.getInstance();
					slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());

					if (isAfter(slotStart, endOfTheHour)) {
						// Nothing happens, same status as previously
						if (isRunning) {
							slots.put(createSlot(3600, true));
						} else {
							slots.put(createSlot(3600, false));
						}
					} else {

						Calendar currentRunningHour = (Calendar) currentHour.clone();

						// Check if the current slot started before
						if (isBefore(slotStart, beginningOfTheHour)) {
							// Check if the current slot finished after
							// today
							if (isAfter(slotEnd, endOfTheHour)) {
								slots.put(createSlot(3600, true));
								currentRunningHour = endOfTheHour;
							} else {
								// Check the duration of the current slot
								int slotDuration = Math.round((slotEnd.getTimeInMillis() - beginningOfTheHour.getTimeInMillis()) / 1000);
								slots.put(createSlot(slotDuration, true));

								currentRunningHour.setTime(slotEnd.getTime());

								// Check next row
								hasMoreRows = rs.next();
								if (hasMoreRows) {
									// Re-initialize slot start and slot end
									slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());
									slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());
								}
							}
						}

						while (hasMoreRows && !isEndOfTheHour && currentHour.get(Calendar.HOUR_OF_DAY) == slotStart.get(Calendar.HOUR_OF_DAY) && DateUtils.isSameDay(currentHour, slotStart)) {
							// Check if the slot ends the same day
							if (currentHour.get(Calendar.HOUR_OF_DAY) == slotEnd.get(Calendar.HOUR_OF_DAY)) {

								// Add a non-active slot
								slots.put(createSlot(Math.round((slotStart.getTimeInMillis() - currentRunningHour.getTimeInMillis()) / 1000), false));

								int slotDuration = Math.round((slotEnd.getTimeInMillis() - slotStart.getTimeInMillis()) / 1000);
								slots.put(createSlot(slotDuration, true));

								currentRunningHour.setTime(slotEnd.getTime());

								// Check next row
								hasMoreRows = rs.next();
								if (hasMoreRows) {
									// Re-initialize slot start and slot end
									slotStart.setTimeInMillis(rs.getTimestamp("start_hour").getTime());
									slotEnd.setTimeInMillis(rs.getTimestamp("end_hour").getTime());
								}

								isRunning = false;
							} else {

								// Add a non-active slot
								slots.put(createSlot(Math.round((slotStart.getTimeInMillis() - currentRunningHour.getTimeInMillis()) / 1000), false));

								int slotDuration = Math.round((endOfTheHour.getTimeInMillis() - slotStart.getTimeInMillis()) / 1000);
								slots.put(createSlot(slotDuration, true));

								isRunning = true;
								isEndOfTheHour = true;
								currentRunningHour = endOfTheHour;
							}
						}

						// Add the remaining time
						slots.put(createSlot(Math.round((endOfTheHour.getTimeInMillis() - currentRunningHour.getTimeInMillis()) / 1000), isRunning));
					}

				}
				currentHour.add(Calendar.HOUR, 1);
				data.put(hour);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return data.toString();
	}

	private JSONObject createSlot(int nbOfMinutes, boolean isActive) throws JSONException {
		if (nbOfMinutes > 0) {
			JSONObject data = new JSONObject();
			data.put("nbOfSeconds", nbOfMinutes);
			data.put("isActive", isActive);
			return data;
		}
		return null;
	}

	private boolean isBefore(Calendar date1, Calendar date2) {
		return date1.compareTo(date2) < 0;
	}

	private boolean isAfter(Calendar date1, Calendar date2) {
		return date1.compareTo(date2) > 0;
	}

}
