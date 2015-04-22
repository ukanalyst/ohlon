package com.bataon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.bataon.ephesoft.db.utils.DBUtils;
import com.ephesoft.dcma.da.service.BatchInstanceService;
import com.ephesoft.dcma.da.service.PluginService;
import com.ephesoft.dcma.reporting.service.EphesoftReportingService;

@Component
@ManagedResource(objectName = "ephesoft:type=reporting-stats", description = "Reporting Statistics about Ephesoft")
public class ReportingStats {

	private static final Logger log = Logger.getLogger(ReportingStats.class.getName());

	/**
	 * Initializing ephesoftReportingService {@link EphesoftReportingService}.
	 */
	@Autowired
	private EphesoftReportingService ephesoftReportingService;

	/**
	 * Initializing batchInstanceService {@link BatchInstanceService}.
	 */
	@Autowired
	private BatchInstanceService batchInstanceService;
	PluginService pluginService;

	@ManagedOperation(description = "Refresh report database")
	public String refreshReportDatabase() {
		try {
			ephesoftReportingService.performReportingAction();
		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage());
			System.err.println(e);
		}
		return getLatestReportSyncDate();
	}

	@ManagedOperation(description = "Get latest database sync")
	public String getLatestReportSyncDate() {

		String lastSync = "";

		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the last update time
			String sql = "SELECT LAST_UPDATE_TIME FROM  `last_update_time`";

			PreparedStatement statement = c.prepareStatement(sql);
			ResultSet rs = statement.executeQuery();

			if (rs.next())
				lastSync = rs.getTimestamp("LAST_UPDATE_TIME").toString();

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage());
			System.err.println(e);
		}

		return lastSync;
	}

	@ManagedOperation(description = "Get batch class execution details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassExecutionDetails(String identifier, String from, String to) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT WORKFLOW_NAME, WORKFLOW_TYPE,  AVG(DURATION) AS AVGDURATION,  MIN(DURATION) AS MINDURATION,  MAX(DURATION) AS MAXDURATION,  AVG(1000 * TOTAL_NUMBER_DOCUMENTS / DURATION) AS AVGDOCPS,  MIN(1000 * TOTAL_NUMBER_DOCUMENTS / DURATION) AS MINDOCPS, MAX(1000 * TOTAL_NUMBER_DOCUMENTS / DURATION) AS MAXDOCPS,  AVG(1000 * TOTAL_NUMBER_PAGES / DURATION) AS AVGPAGEPS, MIN(1000 * TOTAL_NUMBER_PAGES / DURATION) AS MINPAGEPS, MAX(1000 * TOTAL_NUMBER_PAGES / DURATION) AS MAXPAGEPS, SUM(TOTAL_NUMBER_PAGES) AS NBOFPAGES, SUM(TOTAL_NUMBER_DOCUMENTS) AS NBOFDOCUMENTS, COUNT(DISTINCT PROCESS_KEY) AS NBOFBATCHINSTANCES FROM report_data WHERE BATCH_CLASS_ID = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND END_TIME <= '" + to + "'";

			sql += " GROUP BY WORKFLOW_NAME ORDER BY WORKFLOW_TYPE, WORKFLOW_NAME";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				JSONObject m = new JSONObject();
				m.put("WORKFLOW_NAME", rs.getString("WORKFLOW_NAME"));
				m.put("WORKFLOW_TYPE", rs.getString("WORKFLOW_TYPE"));
				m.put("AVGDURATION", rs.getDouble("AVGDURATION"));
				m.put("MINDURATION", rs.getDouble("MINDURATION"));
				m.put("MAXDURATION", rs.getDouble("MAXDURATION"));
				m.put("AVGDOCPS", rs.getDouble("AVGDOCPS"));
				m.put("MINDOCPS", rs.getDouble("MINDOCPS"));
				m.put("MAXDOCPS", rs.getDouble("MAXDOCPS"));
				m.put("AVGPAGEPS", rs.getDouble("AVGPAGEPS"));
				m.put("MINPAGEPS", rs.getDouble("MINPAGEPS"));
				m.put("MAXPAGEPS", rs.getDouble("MAXPAGEPS"));
				m.put("NBOFPAGES", rs.getDouble("NBOFPAGES"));
				m.put("NBOFDOCUMENTS", rs.getDouble("NBOFDOCUMENTS"));
				m.put("NBOFBATCHINSTANCES", rs.getDouble("NBOFBATCHINSTANCES"));
				captured.put(m);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return captured.toString();
	}

	@ManagedOperation(description = "Get artifact repartition details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "type", description = "Artifact type."),
			@ManagedOperationParameter(name = "name", description = "Artifact name."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getArtifactRepartitionDetails(String identifier, String type, String name, String from, String to) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT DURATION FROM report_data WHERE BATCH_CLASS_ID = ? AND WORKFLOW_TYPE = ? AND WORKFLOW_NAME = ? ";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND END_TIME <= '" + to + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, type);
			statement.setString(3, name);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getLong("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getLong(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", "< " + (int) (currentStart / 1000) + "s");
						m.put("count", currentNumber);
						obj.put(m);

						currentNumber = 0;
						currentStart += 5000; // + 5s
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == captured.length();
					}
				}

				// We check that all values are stored
				if (currentNumber > 0) {
					// Add the last slot
					JSONObject m = new JSONObject();
					m.put("label", "< " + (int) (currentStart / 1000) + "s");
					m.put("count", currentNumber);
					obj.put(m);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get artifact accumulation details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "type", description = "Artifact type."),
			@ManagedOperationParameter(name = "name", description = "Artifact name."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getArtifactAccumulationDetails(String identifier, String type, String name, String from, String to) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT DURATION FROM report_data WHERE BATCH_CLASS_ID = ? AND WORKFLOW_TYPE = ? AND WORKFLOW_NAME = ? ";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND END_TIME <= '" + to + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, type);
			statement.setString(3, name);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getLong("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getLong(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", "< " + (int) (currentStart / 1000) + "s");
						m.put("percentage", (int) (100.0 * currentNumber / captured.length()));
						obj.put(m);

						currentStart += 5000; // + 5s
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == captured.length();
					}
				}

				// Add the last slot
				JSONObject m = new JSONObject();
				m.put("label", "< " + (int) (currentStart / 1000) + "s");
				m.put("percentage", (int) (100.0 * currentNumber / captured.length()));
				obj.put(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get batch instance execution details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier.") })
	public String getBatchInstanceExecutionDetails(String identifier) {
		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT WORKFLOW_NAME, START_TIME, END_TIME, WORKFLOW_TYPE, DURATION, TOTAL_NUMBER_DOCUMENTS, TOTAL_NUMBER_PAGES, BATCH_CLASS_ID FROM report_data WHERE BATCH_INSTANCE_ID = ? ORDER BY START_TIME";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				JSONObject m = new JSONObject();
				m.put("BATCH_CLASS_ID", rs.getString("BATCH_CLASS_ID"));
				m.put("WORKFLOW_NAME", rs.getString("WORKFLOW_NAME"));
				m.put("WORKFLOW_TYPE", rs.getString("WORKFLOW_TYPE"));
				m.put("START_TIME", rs.getTimestamp("START_TIME"));
				m.put("END_TIME", rs.getTimestamp("END_TIME"));
				m.put("DURATION", rs.getLong("DURATION"));
				m.put("TOTAL_NUMBER_DOCUMENTS", rs.getInt("TOTAL_NUMBER_DOCUMENTS"));
				m.put("TOTAL_NUMBER_PAGES", rs.getInt("TOTAL_NUMBER_PAGES"));
				captured.put(m);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return captured.toString();
	}

	@ManagedOperation(description = "Get manual steps reportins")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date"), @ManagedOperationParameter(name = "user", description = "User") })
	public String getManualStepsExecutionDetails(String identifier, String from, String to, String user) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT BATCH_STATUS,  AVG(m.DURATION) AS AVGDURATION,  MIN(m.DURATION) AS MINDURATION,  MAX(m.DURATION) AS MAXDURATION,  AVG(1000 * TOTAL_NUMBER_DOCUMENTS / m.DURATION) AS AVGDOCPS,  MIN(1000 * TOTAL_NUMBER_DOCUMENTS / m.DURATION) AS MINDOCPS, MAX(1000 * TOTAL_NUMBER_DOCUMENTS / m.DURATION) AS MAXDOCPS,  AVG(1000 * TOTAL_NUMBER_PAGES / m.DURATION) AS AVGPAGEPS, MIN(1000 * TOTAL_NUMBER_PAGES / m.DURATION) AS MINPAGEPS, MAX(1000 * TOTAL_NUMBER_PAGES / m.DURATION) AS MAXPAGEPS, SUM(TOTAL_NUMBER_PAGES) AS NBOFPAGES, SUM(TOTAL_NUMBER_DOCUMENTS) AS NBOFDOCUMENTS, COUNT(DISTINCT PROCESS_KEY) AS NBOFBATCHINSTANCES FROM manual_step_data_for_multiple_users m LEFT JOIN report_data r ON m.REPORT_DATA_ID = r.id WHERE BATCH_CLASS_ID = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND m.START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND m.END_TIME <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND m.USER_NAME = '" + user + "'";

			sql += " GROUP BY BATCH_STATUS";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				JSONObject m = new JSONObject();
				m.put("BATCH_STATUS", rs.getString("BATCH_STATUS"));
				m.put("AVGDURATION", rs.getDouble("AVGDURATION"));
				m.put("MINDURATION", rs.getDouble("MINDURATION"));
				m.put("MAXDURATION", rs.getDouble("MAXDURATION"));
				m.put("AVGDOCPS", rs.getDouble("AVGDOCPS"));
				m.put("MINDOCPS", rs.getDouble("MINDOCPS"));
				m.put("MAXDOCPS", rs.getDouble("MAXDOCPS"));
				m.put("AVGPAGEPS", rs.getDouble("AVGPAGEPS"));
				m.put("MINPAGEPS", rs.getDouble("MINPAGEPS"));
				m.put("MAXPAGEPS", rs.getDouble("MAXPAGEPS"));
				m.put("NBOFPAGES", rs.getDouble("NBOFPAGES"));
				m.put("NBOFDOCUMENTS", rs.getDouble("NBOFDOCUMENTS"));
				m.put("NBOFBATCHINSTANCES", rs.getDouble("NBOFBATCHINSTANCES"));
				captured.put(m);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return captured.toString();
	}

	@ManagedOperation(description = "Get manual step repartition details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "module", description = "Module."), @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."),
			@ManagedOperationParameter(name = "from", description = "From Date"), @ManagedOperationParameter(name = "to", description = "To Date"),
			@ManagedOperationParameter(name = "user", description = "User name") })
	public String getManualStepsRepartitionDetails(String module, String identifier, String from, String to, String user) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			String sql = "SELECT m.DURATION AS DURATION FROM manual_step_data_for_multiple_users m LEFT JOIN report_data r ON m.REPORT_DATA_ID = r.id WHERE BATCH_CLASS_ID = ? AND m.BATCH_STATUS = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND m.START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND m.END_TIME <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND m.USER_NAME = '" + user + "'";

			sql += " ORDER BY m.DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, module);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getLong("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getLong(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", "< " + (int) (currentStart / 1000) + "s");
						m.put("count", currentNumber);
						obj.put(m);

						currentNumber = 0;
						currentStart += 5000; // + 5s
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == captured.length();
					}
				}

				// We check that all values are stored
				if (currentNumber > 0) {
					// Add the last slot
					JSONObject m = new JSONObject();
					m.put("label", "< " + (int) (currentStart / 1000) + "s");
					m.put("count", currentNumber);
					obj.put(m);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get manual steps accumulation details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "module", description = "Module."), @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."),
			@ManagedOperationParameter(name = "from", description = "From Date"), @ManagedOperationParameter(name = "to", description = "To Date"),
			@ManagedOperationParameter(name = "user", description = "User name") })
	public String getManualStepsAccumulationDetails(String module, String identifier, String from, String to, String user) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			String sql = "SELECT m.DURATION AS DURATION FROM manual_step_data_for_multiple_users m LEFT JOIN report_data r ON m.REPORT_DATA_ID = r.id WHERE BATCH_CLASS_ID = ? AND m.BATCH_STATUS = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND m.START_TIME >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND m.END_TIME <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND m.USER_NAME = '" + user + "'";

			sql += " ORDER BY m.DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, module);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getLong("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 second
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getLong(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", "< " + (int) (currentStart / 1000) + "s");
						m.put("percentage", (int) (100.0 * currentNumber / captured.length()));
						obj.put(m);

						currentStart += 5000; // + 5s
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == captured.length();
					}
				}

				// Add the last slot
				JSONObject m = new JSONObject();
				m.put("label", "< " + (int) (currentStart / 1000) + "s");
				m.put("percentage", (int) (100.0 * currentNumber / captured.length()));
				obj.put(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();
	}
}
