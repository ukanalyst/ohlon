package com.ohlon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ephesoft.dcma.da.service.BatchInstanceService;
import com.ephesoft.dcma.da.service.PluginService;
import com.ephesoft.dcma.report.scheduler.ReportScheduler;
import com.ohlon.ephesoft.db.utils.DBUtils;
import com.ohlon.ephesoft.db.utils.ListUtils;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=reporting-stats", description = "Reporting Statistics about Ephesoft")
public class ReportingStats {

	private static final Logger log = Logger.getLogger(ReportingStats.class.getName());

	private LicenseService licenseService;

	/**
	 * Initializing etlScheduler {@link ReportScheduler}.
	 */
	@Autowired
	private ReportScheduler etlScheduler;

	/**
	 * Initializing batchInstanceService {@link BatchInstanceService}.
	 */
	@Autowired
	private BatchInstanceService batchInstanceService;
	PluginService pluginService;

	@ManagedOperation(description = "Refresh report database")
	public String refreshReportDatabase() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		try {
			log.debug("Start job to refresh database");
			etlScheduler.setReportVariables();
			etlScheduler.executeStandardEtl();
			etlScheduler.executeAdvancedEtl();
			etlScheduler.executeDashboardEtl();
			log.debug("End of the job");
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return getLatestReportSyncDate();
	}

	@ManagedOperation(description = "Get latest database sync")
	public String getLatestReportSyncDate() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get lastest database sync");

		String lastSync = "";

		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the last update time
			String sql = "SELECT last_execution_at FROM last_execution WHERE job='STANDARD';";

			PreparedStatement statement = c.prepareStatement(sql);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			if (rs.next())
				lastSync = rs.getTimestamp("last_execution_at").toString();

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error(e.getMessage());
		}

		log.debug(lastSync);

		return lastSync;
	}

	@ManagedOperation(description = "Get batch class execution details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassExecutionDetails(String identifier, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get batch execution details: identifier=" + identifier + "; from=" + from + "; to=" + to);

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			// Get the batch class ID
			int batch_class_id = getBatchClassDbId(identifier);

			// Query the main database to find batch instances
			String sql = "SELECT MIN(BUSINESS_KEY_) AS WORKFLOW_NAME, COUNT(*) AS COUNT, MIN(DURATION_) AS MIN, MAX(DURATION_) AS MAX, SUM(DURATION_) AS SUM";
			sql += " FROM batch_instance bi LEFT JOIN ACT_HI_PROCINST proc ON proc.NAME_ = bi.identifier";
			sql += " WHERE bi.batch_class_id = ?";

			// Specific case for SQLServer
			if (DBUtils.isMSSQL()) {
				sql = "SELECT MIN(BUSINESS_KEY_) AS WORKFLOW_NAME, COUNT(*) AS COUNT, MIN(DURATION_) AS MIN, MAX(DURATION_) AS MAX, SUM(DURATION_) AS SUM";
				sql += " FROM batch_instance bi LEFT JOIN ACT_HI_PROCINST procinst ON (procinst.BUSINESS_KEY_ like bi.identifier + '.%' OR procinst.BUSINESS_KEY_ = bi.identifier)";
				sql += " WHERE bi.batch_class_id = ?";
			}

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			sql += " GROUP BY PROC_DEF_ID_";
			sql += " ORDER BY WORKFLOW_NAME";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setInt(1, batch_class_id);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			Map<String, Map<String, Object>> data = new HashMap<String, Map<String, Object>>();

			while (rs.next()) {
				if (rs.getString("WORKFLOW_NAME") != null) {
					String wfName = rs.getString("WORKFLOW_NAME").replaceAll("^BI[\\d|A-Z]+\\.", "");
					String wfType = "WORKFLOW";

					if (wfName.endsWith("-m"))
						wfType = "MODULE";
					else if (wfName.endsWith("-p"))
						wfType = "PLUGIN";

					// If it's a plugin or a module
					String moduleName = wfName.substring(0, wfName.length() - 2);
					if (wfType.equalsIgnoreCase("WORKFLOW"))
						moduleName = "BATCHINSTANCE";

					if (data.containsKey(moduleName)) {
						Map<String, Object> moduleData = data.get(moduleName);
						moduleData.put("COUNT", ((Integer) moduleData.get("COUNT")) + rs.getInt("COUNT"));
						moduleData.put("TOTALDURATION", ((Long) moduleData.get("TOTALDURATION")) + rs.getLong("SUM"));
						if (rs.getInt("MIN") < (Integer) moduleData.get("MINDURATION"))
							moduleData.put("MINDURATION", rs.getInt("MIN"));
						if (rs.getInt("MAX") > (Integer) moduleData.get("MAXDURATION"))
							moduleData.put("MAXDURATION", rs.getInt("MAX"));
					} else {
						Map<String, Object> moduleData = new HashMap<String, Object>();
						moduleData.put("NAME", moduleName);
						moduleData.put("TYPE", wfType);
						moduleData.put("COUNT", rs.getInt("COUNT"));
						moduleData.put("TOTALDURATION", rs.getLong("SUM"));
						moduleData.put("MINDURATION", rs.getInt("MIN"));
						moduleData.put("MAXDURATION", rs.getInt("MAX"));
						data.put(moduleName, moduleData);
					}
				}
			}

			Connection c2 = DBUtils.getReportDBConnection();

			// Query the report database to get the number of pages and
			// documents
			sql = "SELECT AVG(NBOFDOCS) AS NBOFDOCS, AVG(NBOFPAGES) AS NBOFPAGES, SUM(NBOFDOCS) AS SUMNBOFDOCS, SUM(NBOFPAGES) AS SUMNBOFPAGES FROM";
			sql += "  ( SELECT batch_instance_id, COUNT(DISTINCT doc_identifier) AS NBOFDOCS, COUNT(DISTINCT page_identifier) AS NBOFPAGES";
			sql += "    FROM finished_batch_xml_data fbxd";
			sql += "    WHERE fbxd.batch_class_id = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND fbxd.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND fbxd.creation_date <= '" + to + "'";

			sql += "    GROUP BY batch_instance_id) sub;";

			PreparedStatement statement2 = c2.prepareStatement(sql);
			statement2.setString(1, identifier);
			ResultSet rs2 = statement2.executeQuery();

			float nbOfDocs = 0;
			float nbOfPages = 0;
			int sumNbOfDocs = 0;
			int sumNbOfPages = 0;

			if (rs2.next()) {
				nbOfDocs = rs2.getFloat("NBOFDOCS");
				nbOfPages = rs2.getFloat("NBOFPAGES");
				sumNbOfDocs = rs2.getInt("SUMNBOFDOCS");
				sumNbOfPages = rs2.getInt("SUMNBOFPAGES");
			}

			// Browse the data
			for (String artifactId : data.keySet()) {

				Map<String, Object> artifactData = data.get(artifactId);

				if ((Integer) artifactData.get("MAXDURATION") > 0) {

					JSONObject m = new JSONObject();
					m.put("WORKFLOW_NAME", artifactData.get("NAME"));
					m.put("WORKFLOW_TYPE", artifactData.get("TYPE"));

					if ("WORKFLOW".equalsIgnoreCase((String) artifactData.get("TYPE"))) {
						m.put("NBOFDOCUMENTS", sumNbOfDocs);
						m.put("NBOFPAGES", sumNbOfPages);
					} else {
						m.put("NBOFDOCUMENTS", nbOfDocs);
						m.put("NBOFPAGES", nbOfPages);
					}

					int nbOfBatchInstances = (Integer) artifactData.get("COUNT");
					double avgDuration = (Long) artifactData.get("TOTALDURATION") / nbOfBatchInstances;
					m.put("NBOFBATCHINSTANCES", nbOfBatchInstances);

					m.put("AVGDURATION", avgDuration);
					m.put("MINDURATION", artifactData.get("MINDURATION"));
					m.put("MAXDURATION", artifactData.get("MAXDURATION"));

					m.put("AVGDOCPS", (double) (1000.0 * nbOfDocs / m.getDouble("AVGDURATION")));
					m.put("MINDOCPS", (double) (1000.0 * nbOfDocs / m.getDouble("MAXDURATION")));
					m.put("MAXDOCPS", (double) (1000.0 * nbOfDocs / m.getDouble("MINDURATION")));
					m.put("AVGPAGEPS", (double) (1000.0 * nbOfPages / m.getDouble("AVGDURATION")));
					m.put("MINPAGEPS", (double) (1000.0 * nbOfPages / m.getDouble("MAXDURATION")));
					m.put("MAXPAGEPS", (double) (1000.0 * nbOfPages / m.getDouble("MINDURATION")));

					captured.put(m);
				}
			}

			// Close the query
			rs2.close();
			statement2.close();
			c2.close();

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + captured);

		return captured.toString();
	}

	private int getBatchClassDbId(String identifier) {
		int result = -1;
		try {
			Connection c = DBUtils.getDBConnection();
			String sql = "SELECT id FROM batch_class WHERE identifier=?";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			if (rs.next())
				result = rs.getInt("id");

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}
		return result;
	}

	@ManagedOperation(description = "Get artifact repartition details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "type", description = "Artifact type."),
			@ManagedOperationParameter(name = "name", description = "Artifact name."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getArtifactRepartitionDetails(String identifier, String type, String name, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get artifact repartition details: identifier=" + identifier + "; type=" + type + "; name=" + name + "; from=" + from + "; to=" + to);

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			String businessKey = name;
			if (type.equalsIgnoreCase("PLUGIN"))
				businessKey += "-p";
			if (type.equalsIgnoreCase("MODULE"))
				businessKey += "-m";

			// Query the main database to find batch instances

			String sql = "SELECT DURATION_ AS DURATION FROM batch_instance bi LEFT JOIN batch_class bc ON bi.batch_class_id = bc.id LEFT JOIN ACT_HI_PROCINST procinst ON procinst.NAME_ = bi.identifier WHERE bc.identifier = ?";
			sql += " AND BUSINESS_KEY_ LIKE '%" + businessKey + "%'";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getInt("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Captured: " + captured);

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getInt(currentIndex)) {
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
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get artifact accumulation details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "type", description = "Artifact type."),
			@ManagedOperationParameter(name = "name", description = "Artifact name."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getArtifactAccumulationDetails(String identifier, String type, String name, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get artifact accumulation details: identifier=" + identifier + "; type=" + type + "; name=" + name + "; from=" + from + "; to=" + to);

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			String businessKey = name;
			if (type.equalsIgnoreCase("PLUGIN"))
				businessKey += "-p";
			if (type.equalsIgnoreCase("MODULE"))
				businessKey += "-m";

			// Query the main database to find batch instances

			String sql = "SELECT DURATION_ AS DURATION FROM batch_instance bi LEFT JOIN batch_class bc ON bi.batch_class_id = bc.id LEFT JOIN ACT_HI_PROCINST procinst ON procinst.NAME_ = bi.identifier WHERE bc.identifier = ? ";
			sql += " AND BUSINESS_KEY_ LIKE '%" + businessKey + "'";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getInt("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Captured: " + captured);

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getInt(currentIndex)) {
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
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get batch instance execution details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier.") })
	public String getBatchInstanceExecutionDetails(String identifier) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		JSONArray captured = new JSONArray();
		try {

			log.debug("Get batch instance execution details: identifier=" + identifier);

			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT BUSINESS_KEY_ AS WORKFLOW_NAME, DURATION_ AS DURATION, START_TIME_ AS START_TIME, END_TIME_ AS END_TIME FROM ACT_HI_PROCINST WHERE NAME_ = ? ORDER BY START_TIME";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			// Query the report database to get the number of pages and
			// documents
			sql = "SELECT COUNT(DISTINCT doc_identifier) AS NBOFDOCS, COUNT(DISTINCT page_identifier) AS NBOFPAGES, batch_instance_id, batch_class_id FROM finished_batch_xml_data WHERE batch_instance_id = ? GROUP BY batch_instance_id, batch_class_id";
			Connection c2 = DBUtils.getReportDBConnection();
			PreparedStatement statement2 = c2.prepareStatement(sql);
			statement2.setString(1, identifier);
			ResultSet rs2 = statement2.executeQuery();
			int nbOfDocs = 0;
			int nbOfPages = 0;
			String BCID = "";
			while (rs2.next()) {
				nbOfDocs = rs2.getInt("NBOFDOCS");
				nbOfPages = rs2.getInt("NBOFPAGES");
				BCID = rs2.getString("batch_class_id");
			}

			while (rs.next()) {
				String wfName = rs.getString("WORKFLOW_NAME").replaceAll("^" + identifier + "\\.", "");
				String wfType = "WORKFLOW";
				if (wfName.endsWith("-m"))
					wfType = "MODULE";
				else if (wfName.endsWith("-p"))
					wfType = "PLUGIN";

				JSONObject m = new JSONObject();
				m.put("BATCH_CLASS_ID", BCID);
				m.put("WORKFLOW_NAME", wfName);
				m.put("WORKFLOW_TYPE", wfType);
				m.put("START_TIME", rs.getTimestamp("START_TIME"));
				m.put("END_TIME", rs.getTimestamp("END_TIME"));
				m.put("DURATION", rs.getInt("DURATION"));
				m.put("TOTAL_NUMBER_DOCUMENTS", nbOfDocs);
				m.put("TOTAL_NUMBER_PAGES", nbOfPages);
				captured.put(m);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + captured);

		return captured.toString();
	}

	@ManagedOperation(description = "Get manual steps reporting")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date"), @ManagedOperationParameter(name = "user", description = "User") })
	public String getManualStepsExecutionDetails(String identifier, String from, String to, String user) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Manual Steps Execution Details: identifier=" + identifier + "; from=" + from + "; to=" + to + "; user=" + user);

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			// get the batch instance details
			String sql = "SELECT batch_instance_id as identifier, review_validate AS status, review_validate_duration as duration FROM batch_review_validate WHERE batch_class_id = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND batch_start_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND batch_end_date <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND review_validate_user = '" + user + "'";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			Map<String, Map<String, Object>> data = new HashMap<String, Map<String, Object>>();
			Set<String> listOfIdentifiers = new HashSet<String>();
			Map<String, Map<String, Integer>> stepDuration = new HashMap<String, Map<String, Integer>>();

			while (rs.next()) {
				String status = rs.getString("status");
				listOfIdentifiers.add(rs.getString("identifier"));

				// Record the duration
				if (!stepDuration.containsKey(status))
					stepDuration.put(status, new HashMap<String, Integer>());
				stepDuration.get(status).put(rs.getString("identifier"), rs.getInt("duration"));

				if (data.containsKey(status)) {
					Map<String, Object> moduleData = data.get(status);
					moduleData.put("IDENTIFIER", moduleData.get("IDENTIFIER") + "/" + rs.getString("identifier"));
					moduleData.put("COUNT", ((Integer) moduleData.get("COUNT")) + 1);
					moduleData.put("TOTALDURATION", ((Integer) moduleData.get("TOTALDURATION")) + rs.getInt("duration"));
					if (rs.getInt("duration") < (Integer) moduleData.get("MINDURATION"))
						moduleData.put("MINDURATION", rs.getInt("duration"));
					if (rs.getInt("duration") > (Integer) moduleData.get("MAXDURATION"))
						moduleData.put("MAXDURATION", rs.getInt("duration"));
				} else {
					Map<String, Object> moduleData = new HashMap<String, Object>();
					moduleData.put("STATUS", status);
					moduleData.put("COUNT", 1);
					moduleData.put("IDENTIFIER", rs.getString("identifier"));
					moduleData.put("TOTALDURATION", rs.getInt("duration"));
					moduleData.put("MINDURATION", rs.getInt("duration"));
					moduleData.put("MAXDURATION", rs.getInt("duration"));
					data.put(status, moduleData);
				}
			}

			Map<String, Integer> nbOfDocs = new HashMap<String, Integer>();
			Map<String, Integer> nbOfPages = new HashMap<String, Integer>();

			if (listOfIdentifiers.size() > 0) {
				// Query the report database to get the number of pages and
				// documents
				sql = "SELECT COUNT(DISTINCT doc_identifier) AS NBOFDOCS, COUNT(DISTINCT page_identifier) AS NBOFPAGES, batch_instance_id FROM finished_batch_xml_data WHERE batch_instance_id IN (";
				boolean isFirst = true;
				for (String BIIdentifier : listOfIdentifiers) {
					if (!isFirst)
						sql += ",";
					sql += "'" + BIIdentifier + "'";
					isFirst = false;
				}
				sql += ") GROUP BY batch_instance_id;";

				Connection c2 = DBUtils.getReportDBConnection();
				PreparedStatement statement2 = c2.prepareStatement(sql);
				ResultSet rs2 = statement2.executeQuery();

				while (rs2.next()) {
					nbOfDocs.put(rs2.getString("batch_instance_id"), rs2.getInt("NBOFDOCS"));
					nbOfPages.put(rs2.getString("batch_instance_id"), rs2.getInt("NBOFPAGES"));
				}

				// Close the query
				rs2.close();
				statement2.close();
				c2.close();
			}

			// Browse the data
			for (String artifactId : data.keySet()) {

				Map<String, Object> artifactData = data.get(artifactId);

				JSONObject m = new JSONObject();
				m.put("BATCH_STATUS", artifactId);

				int nbOfBatchInstances = artifactData.get("IDENTIFIER").toString().split("/").length;
				double avgDuration = (Integer) artifactData.get("TOTALDURATION") / nbOfBatchInstances;

				m.put("AVGDURATION", avgDuration);
				m.put("MINDURATION", artifactData.get("MINDURATION"));
				m.put("MAXDURATION", artifactData.get("MAXDURATION"));

				int _nbOfPages = 0;
				int _nbOfDocs = 0;
				List<Double> docps = new ArrayList<Double>();
				List<Double> pageps = new ArrayList<Double>();
				for (String _identifier : artifactData.get("IDENTIFIER").toString().split("/")) {
					_nbOfDocs += nbOfDocs.get(_identifier);
					_nbOfPages += nbOfPages.get(_identifier);
					docps.add((double) (1000.0 * nbOfDocs.get(_identifier) / stepDuration.get(artifactId).get(_identifier)));
					pageps.add((double) (1000.0 * nbOfPages.get(_identifier) / stepDuration.get(artifactId).get(_identifier)));
				}

				m.put("NBOFPAGES", _nbOfPages);
				m.put("NBOFDOCUMENTS", _nbOfDocs);
				m.put("NBOFBATCHINSTANCES", nbOfBatchInstances);

				m.put("AVGDOCPS", ListUtils.average(docps));
				m.put("MINDOCPS", ListUtils.minimum(docps));
				m.put("MAXDOCPS", ListUtils.maximum(docps));
				m.put("AVGPAGEPS", ListUtils.average(pageps));
				m.put("MINPAGEPS", ListUtils.minimum(pageps));
				m.put("MAXPAGEPS", ListUtils.maximum(pageps));
				captured.put(m);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + captured);

		return captured.toString();
	}

	@ManagedOperation(description = "Get manual step repartition details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "module", description = "Module."), @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."),
			@ManagedOperationParameter(name = "from", description = "From Date"), @ManagedOperationParameter(name = "to", description = "To Date"),
			@ManagedOperationParameter(name = "user", description = "User name") })
	public String getManualStepsRepartitionDetails(String module, String identifier, String from, String to, String user) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Manual Steps Repartition Details: module=" + module + "; identifier=" + identifier + "; from=" + from + "; to=" + to + "; user=" + user);

		if (module.startsWith("READY_FOR_"))
			module = module.replaceAll("READY_FOR_", "");

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			String sql = "SELECT review_validate_duration AS DURATION FROM batch_review_validate WHERE batch_class_id = ? AND review_validate = ? ";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND batch_start_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND batch_end_date <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND review_validate_user = '" + user + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, module);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getInt("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Captured: " + captured);

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 seconds
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getInt(currentIndex)) {
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
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return captured.toString();
	}

	@ManagedOperation(description = "Get manual steps accumulation details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "module", description = "Module."), @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."),
			@ManagedOperationParameter(name = "from", description = "From Date"), @ManagedOperationParameter(name = "to", description = "To Date"),
			@ManagedOperationParameter(name = "user", description = "User name") })
	public String getManualStepsAccumulationDetails(String module, String identifier, String from, String to, String user) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Manual Steps Accumulation Details: module=" + module + "; identifier=" + identifier + "; from=" + from + "; to=" + to + "; user=" + user);

		if (module.startsWith("READY_FOR_"))
			module = module.replaceAll("READY_FOR_", "");

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getReportDBConnection();

			String sql = "SELECT review_validate_duration AS DURATION FROM batch_review_validate WHERE batch_class_id = ? AND review_validate = ? ";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND batch_start_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND batch_end_date <= '" + to + "'";

			if (user != null && user.length() > 0 && !user.equalsIgnoreCase("na"))
				sql += " AND review_validate_user = '" + user + "'";

			sql += " ORDER BY DURATION";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			statement.setString(2, module);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				captured.put(rs.getInt("DURATION"));
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Captured: " + captured);

		if (captured.length() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 5000; // 5 second
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getInt(currentIndex)) {
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
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return captured.toString();
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
