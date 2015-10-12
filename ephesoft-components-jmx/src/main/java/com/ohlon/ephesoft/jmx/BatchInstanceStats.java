package com.ohlon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ephesoft.dcma.core.common.BatchInstanceStatus;
import com.ephesoft.dcma.da.domain.BatchClass;
import com.ephesoft.dcma.da.domain.BatchInstance;
import com.ephesoft.dcma.da.property.BatchPriority;
import com.ephesoft.dcma.da.service.BatchClassService;
import com.ephesoft.dcma.da.service.BatchInstanceService;
import com.ephesoft.dcma.da.service.PluginService;
import com.ohlon.ephesoft.db.utils.DBUtils;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=batchinstance-stats", description = "Batch Instance Statistics about Ephesoft")
public class BatchInstanceStats {

	private static final Logger log = Logger.getLogger(BatchInstanceStats.class.getName());

	private LicenseService licenseService;

	/**
	 * Initializing batchClassService {@link BatchClassService}.
	 */
	@Autowired
	private BatchClassService batchClassService;

	/**
	 * Initializing batchInstanceService {@link BatchInstanceService}.
	 */
	@Autowired
	private BatchInstanceService batchInstanceService;
	PluginService pluginService;

	@ManagedAttribute
	public int getErrorBatchInstances() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		log.debug("Get Error Batch Instances");
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.ERROR);
		log.debug("Result: " + batchInstances.size());
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getRunningBatchInstances() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		log.debug("Get Running Batch Instances");
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.RUNNING);
		log.debug("Result: " + batchInstances.size());
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getReadyForReviewBatchInstances() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		log.debug("Get Ready For Review Batch Instances");
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.READY_FOR_REVIEW);
		log.debug("Result: " + batchInstances.size());
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getReadyForValidationBatchInstances() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		log.debug("Get Ready For Validation Batch Instances");
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.READY_FOR_VALIDATION);
		log.debug("Result: " + batchInstances.size());
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getActiveBatchInstances() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return 0;
		}

		log.debug("Get Active Batch Instances");
		List<BatchInstanceStatus> statusList = new ArrayList<BatchInstanceStatus>();
		statusList.add(BatchInstanceStatus.ERROR);
		statusList.add(BatchInstanceStatus.NEW);
		statusList.add(BatchInstanceStatus.ASSIGNED);
		statusList.add(BatchInstanceStatus.LOCKED);
		statusList.add(BatchInstanceStatus.OPEN);
		statusList.add(BatchInstanceStatus.READY);
		statusList.add(BatchInstanceStatus.READY_FOR_REVIEW);
		statusList.add(BatchInstanceStatus.READY_FOR_VALIDATION);
		statusList.add(BatchInstanceStatus.RUNNING);

		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstanceByStatusList(statusList);
		log.debug("Result: " + batchInstances.size());
		return batchInstances.size();
	}

	@ManagedAttribute
	public String getActiveBatchInstancesDetails() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Active Batch Instances Details");

		JSONArray result = new JSONArray();

		List<BatchInstanceStatus> statusList = new ArrayList<BatchInstanceStatus>();
		statusList.add(BatchInstanceStatus.ERROR);
		statusList.add(BatchInstanceStatus.NEW);
		statusList.add(BatchInstanceStatus.ASSIGNED);
		statusList.add(BatchInstanceStatus.LOCKED);
		statusList.add(BatchInstanceStatus.OPEN);
		statusList.add(BatchInstanceStatus.READY);
		statusList.add(BatchInstanceStatus.READY_FOR_REVIEW);
		statusList.add(BatchInstanceStatus.READY_FOR_VALIDATION);
		statusList.add(BatchInstanceStatus.RUNNING);

		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstanceByStatusList(statusList);

		try {
			for (BatchInstance batchInstance : batchInstances) {
				result.put(batchInstance.getIdentifier());
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	@ManagedAttribute
	public String getActiveBatchInstancesList() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Active Batch Instances List");

		JSONArray result = new JSONArray();

		List<BatchInstanceStatus> statusList = new ArrayList<BatchInstanceStatus>();
		statusList.add(BatchInstanceStatus.ERROR);
		statusList.add(BatchInstanceStatus.NEW);
		statusList.add(BatchInstanceStatus.ASSIGNED);
		statusList.add(BatchInstanceStatus.LOCKED);
		statusList.add(BatchInstanceStatus.OPEN);
		statusList.add(BatchInstanceStatus.READY);
		statusList.add(BatchInstanceStatus.READY_FOR_REVIEW);
		statusList.add(BatchInstanceStatus.READY_FOR_VALIDATION);
		statusList.add(BatchInstanceStatus.RUNNING);

		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstanceByStatusList(statusList);

		try {
			for (BatchInstance batchInstance : batchInstances) {
				JSONObject bi = new JSONObject();
				bi.put("identifier", batchInstance.getIdentifier());
				bi.put("status", batchInstance.getStatus());
				result.put(bi);
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	@ManagedOperation(description = "Get batch instance details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier.") })
	public String getActiveBatchInstancesDetails(String identifier) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Active Batch Instance Details: identifier=" + identifier);

		BatchInstance batchInstance = batchInstanceService.getBatchInstanceByIdentifier(identifier);
		JSONObject obj = new JSONObject();

		try {
			obj.put("ID", batchInstance.getIdentifier());
			obj.put("priority", BatchPriority.getBatchPriority(batchInstance.getPriority()));
			obj.put("creationDate", batchInstance.getCreationDate());
			obj.put("user", batchInstance.getCurrentUser());
			obj.put("modificationDate", batchInstance.getLastModified());
			obj.put("status", batchInstance.getStatus().toString());

			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT START_, END_, DURATION_ FROM JBPM4_HIST_PROCINST WHERE KEY_=? ORDER BY DBID_;";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, batchInstance.getIdentifier());

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			if (rs.next()) {
				// save batch details information
				obj.put("wf_bi_start", rs.getTimestamp("START_"));
				obj.put("wf_bi_end", rs.getTimestamp("END_"));
				obj.put("wf_bi_duration", rs.getInt("DURATION_"));
			}

			// Close the query
			rs.close();
			statement.close();

			// get the module details
			sql = "SELECT ACTIVITY_NAME_, START_, END_, DURATION_ FROM JBPM4_HIST_ACTINST WHERE HPROCI_ IN (SELECT DBID_ FROM JBPM4_HIST_PROCINST WHERE KEY_= ? ) AND CLASS_ = 'act' AND TYPE_ = 'sub-process' ORDER BY DBID_;";
			statement = c.prepareStatement(sql);
			statement.setString(1, batchInstance.getIdentifier());

			log.debug(statement.toString());

			rs = statement.executeQuery();

			JSONArray modules = new JSONArray();
			while (rs.next()) {
				JSONObject module = new JSONObject();
				module.put("wf_module_label", rs.getString("ACTIVITY_NAME_").replaceAll("_", " "));
				module.put("wf_module_start", rs.getTimestamp("START_"));
				module.put("wf_module_end", rs.getTimestamp("END_"));
				module.put("wf_module_duration", rs.getInt("DURATION_"));
				modules.put(module);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

			obj.put("wf_modules", modules);
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + obj);

		return obj.toString();
	}

	@ManagedOperation(description = "Get batch class repartition")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassRepartition(String identifier, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Class Repartition: identifier=" + identifier + "; from=" + from + "; to=" + to);

		List<Long> durations = new ArrayList<Long>();
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT bi.last_modified as start,bi.creation_date as finish FROM batch_instance AS bi LEFT JOIN batch_class bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				// Capture the duration in second
				durations.add((rs.getTimestamp("start").getTime() - rs.getTimestamp("finish").getTime()) / 1000);
			}

			Collections.sort(durations);

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Durations: " + durations);

		if (durations.size() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 60; // 1 minute
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < durations.get(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", (int) (currentStart / 60) + " minute(s)");
						m.put("count", currentNumber);
						obj.put(m);

						currentNumber = 0;
						currentStart += 60; // + 1m
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == durations.size();
					}
				}

				// We check that all values are stored
				if (currentNumber > 0) {
					// Add the last slot
					JSONObject m = new JSONObject();
					m.put("label", (int) (currentStart / 60) + " minute(s)");
					m.put("count", currentNumber);
					obj.put(m);
				}
			} catch (Exception e) {
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return (new JSONArray()).toString();

	}

	@ManagedOperation(description = "Get batch class accumulation")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassAccumulation(String identifier, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Class Accumulation: identifier=" + identifier + "; from=" + from + "; to=" + to);

		int total = 0;
		List<Long> durations = new ArrayList<Long>();
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT bi.last_modified as start,bi.creation_date as finish FROM batch_instance AS bi LEFT JOIN batch_class bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				// Capture the duration in second
				durations.add((rs.getTimestamp("start").getTime() - rs.getTimestamp("finish").getTime()) / 1000);
				total++;
			}

			Collections.sort(durations);

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Durations: " + durations);

		if (durations.size() > 0) {
			// Fill blank
			int currentIndex = 0;
			int currentStart = 60; // 1 minute
			int currentNumber = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < durations.get(currentIndex)) {
						// We close the slot, and we create a new value
						JSONObject m = new JSONObject();
						m.put("label", (int) (currentStart / 60) + " minute(s)");
						m.put("percentage", (int) (100 * currentNumber / total));
						obj.put(m);

						currentStart += 60; // + 1m
					} else {
						currentNumber++;
						currentIndex++;

						completed = currentIndex == durations.size();
					}
				}

				// We check that all values are stored
				if (currentNumber <= total) {
					// Add the last slot
					JSONObject m = new JSONObject();
					m.put("label", (int) (currentStart / 60) + " minute(s)");
					m.put("percentage", 100);
					obj.put(m);
				}
			} catch (Exception e) {
				log.error("An error occured", e);
			}

			log.debug("Result: " + obj);

			return obj.toString();
		} else
			return (new JSONArray()).toString();
	}

	@ManagedOperation(description = "Get batch instance by batch class")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date"), @ManagedOperationParameter(name = "start", description = "Start"),
			@ManagedOperationParameter(name = "limit", description = "Limit") })
	public String getBatchInstanceByBatchClass(String identifier, String from, String to, Integer start, Integer limit) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instance By Batch Class: identifier=" + identifier + "; from=" + from + "; to=" + to + "; start=" + start + "; limit=" + limit);

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT bi.creation_date as creation_date, bi.last_modified as last_modified, batch_name, bi.identifier as identifier FROM batch_instance AS bi LEFT JOIN batch_class bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";
			
			sql += "LIMIT " + start + "," + limit;

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				JSONObject m = new JSONObject();
				m.put("start", rs.getTimestamp("creation_date"));
				m.put("end", rs.getTimestamp("last_modified"));
				m.put("batch_name", rs.getString("batch_name"));
				m.put("identifier", rs.getString("identifier"));
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

	@ManagedOperation(description = "Get batch instance by batch class")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchInstanceByBatchClass(String identifier, String from, String to) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instance By Batch Class: identifier=" + identifier + "; from=" + from + "; to=" + to);

		return getBatchInstanceByBatchClass(identifier, from, to, 0, 20);
	}

	@ManagedAttribute
	public String getBatchInstancesByBC() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instances By BC");

		JSONArray result = new JSONArray();

		try {
			List<BatchClass> batchClasses = batchClassService.getAllBatchClasses();
			for (BatchClass batchClass : batchClasses) {
				List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByBatchClass(batchClass);
				int active = 0;
				for (BatchInstance batchInstance : batchInstances) {
					if (!batchInstance.getStatus().equals(BatchInstanceStatus.FINISHED) && !batchInstance.getStatus().equals(BatchInstanceStatus.DELETED))
						active++;
				}

				JSONObject obj = new JSONObject();
				obj.put("batchClassId", batchClass.getIdentifier());
				obj.put("batchClassName", batchClass.getName());
				obj.put("size", active);
				result.put(obj);
			}
		} catch (JSONException e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	@ManagedAttribute
	public String getBatchClass() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Class");

		JSONArray result = new JSONArray();

		try {
			List<BatchClass> batchClasses = batchClassService.getAllBatchClasses();
			for (BatchClass batchClass : batchClasses) {
				JSONObject obj = new JSONObject();
				obj.put("batchClassId", batchClass.getIdentifier());
				obj.put("batchClassName", batchClass.getName());
				result.put(obj);
			}
		} catch (JSONException e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	@ManagedAttribute
	public String getBatchInstancesByPriority() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instances By Priority");

		JSONArray result = new JSONArray();
		try {
			for (BatchPriority priority : BatchPriority.values()) {
				List<BatchInstance> batchInstances = batchInstanceService.getBatchInstance(priority);
				int active = 0;
				for (BatchInstance batchInstance : batchInstances) {
					if (!batchInstance.getStatus().equals(BatchInstanceStatus.FINISHED) && !batchInstance.getStatus().equals(BatchInstanceStatus.DELETED))
						active++;
				}

				JSONObject obj = new JSONObject();
				obj.put("priority", priority.toString());
				obj.put("size", active);
				result.put(obj);
			}
		} catch (JSONException e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
