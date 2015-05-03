package com.ohlon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

@Component
@ManagedResource(objectName = "ephesoft:type=batchinstance-stats", description = "Batch Instance Statistics about Ephesoft")
public class BatchInstanceStats {

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
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.ERROR);
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getRunningBatchInstances() {
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.RUNNING);
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getReadyForReviewBatchInstances() {
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.READY_FOR_REVIEW);
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getReadyForValidationBatchInstances() {
		List<BatchInstance> batchInstances = batchInstanceService.getBatchInstByStatus(BatchInstanceStatus.READY_FOR_VALIDATION);
		return batchInstances.size();
	}

	@ManagedAttribute
	public int getActiveBatchInstances() {
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
		return batchInstances.size();
	}

	@ManagedAttribute
	public String getActiveBatchInstancesDetails() {
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
			e.printStackTrace();
		}

		return result.toString();
	}
	
	@ManagedAttribute
	public String getActiveBatchInstancesList() {
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
			e.printStackTrace();
		}
		
		return result.toString();
	}

	@ManagedOperation(description = "Get batch instance details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier.") })
	public String getActiveBatchInstancesDetails(String identifier) {
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
			String sql = "SELECT START_TIME_ AS START_, END_TIME_ AS END_, DURATION_ FROM ACT_HI_PROCINST WHERE NAME_ = ? AND BUSINESS_KEY_ = ? ORDER BY START_TIME_;";
			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, batchInstance.getIdentifier());
			statement.setString(2, batchInstance.getIdentifier());
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
			sql = "SELECT SUBSTRING_INDEX(BUSINESS_KEY_, '.', -1) AS WORKFLOW_NAME, DURATION_, START_TIME_ AS START_, END_TIME_ AS END_ FROM ACT_HI_PROCINST proc WHERE proc.NAME_ = ?  AND SUBSTRING_INDEX(BUSINESS_KEY_, '-', -1)='m' ORDER BY ID_";
			statement = c.prepareStatement(sql);
			statement.setString(1, batchInstance.getIdentifier());
			rs = statement.executeQuery();
			JSONArray modules = new JSONArray();
			while (rs.next()) {
				JSONObject module = new JSONObject();
				module.put("wf_module_label", rs.getString("WORKFLOW_NAME").replaceAll("_", " "));
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
			e.printStackTrace();
		}

		return obj.toString();
	}

	@ManagedOperation(description = "Get batch class repartition")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassRepartition(String identifier, String from, String to) {

		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT @low := TRUNCATE(TIME_TO_SEC(TIMEDIFF(bi.`last_modified`,bi.`creation_date`))/60, 0) * 60 as Low,   TRUNCATE(@low + 60, 0) as High,   COUNT(*) AS Count,   CONCAT( FLOOR((TRUNCATE(@low + 60, 0) / 60)), ' minute(s)') AS Label FROM batch_instance AS bi LEFT JOIN `batch_class` bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			sql += " GROUP BY Low;";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				JSONObject m = new JSONObject();
				m.put("label", rs.getString("Label"));
				m.put("count", rs.getInt("Count"));
				m.put("start", rs.getInt("Low"));
				captured.put(m);
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
			int currentStart = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getJSONObject(currentIndex).getInt("start")) {
						// Create empty line
						JSONObject m = new JSONObject();
						m.put("label", (((int) (currentStart / 60)) + 1) + " minute(s)");
						m.put("count", 0);
						obj.put(m);

						currentStart += 60;
					} else {

						JSONObject m = captured.getJSONObject(currentIndex);
						m.remove("start");
						obj.put(m);

						currentStart += 60;
						currentIndex++;

						completed = currentIndex == captured.length();

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();

	}

	@ManagedOperation(description = "Get batch class accumulation")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchClassAccumulation(String identifier, String from, String to) {
		JSONArray captured = new JSONArray();
		int total = 0;
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT @low := TRUNCATE(TIME_TO_SEC(TIMEDIFF(bi.`last_modified`,bi.`creation_date`))/60, 0) * 60 as Low,   TRUNCATE(@low + 60, 0) as High,   COUNT(*) AS Count,   CONCAT( FLOOR((TRUNCATE(@low + 60, 0) / 60)), ' minute(s)') AS Label FROM batch_instance AS bi LEFT JOIN `batch_class` bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			sql += " GROUP BY Low;";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				int count = rs.getInt("Count");
				JSONObject m = new JSONObject();
				m.put("label", rs.getString("Label"));
				m.put("count", rs.getInt("Count"));
				m.put("start", rs.getInt("Low"));
				total += count;
				captured.put(m);
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
			int currentStart = 0;
			int currentNb = 0;
			boolean completed = false;

			JSONArray obj = new JSONArray();
			try {
				while (!completed) {
					if (currentStart < captured.getJSONObject(currentIndex).getInt("start")) {
						// Create empty line
						JSONObject m = new JSONObject();
						m.put("label", (((int) (currentStart / 60)) + 1) + " minute(s)");
						m.put("percentage", (int) (100.0 * currentNb / total));
						obj.put(m);

						currentStart += 60;
					} else {

						JSONObject m = captured.getJSONObject(currentIndex);
						int count = m.getInt("count");
						m.remove("start");
						m.remove("count");
						currentNb += count;
						m.put("percentage", (int) (100.0 * currentNb / total));
						obj.put(m);

						currentStart += 60;
						currentIndex++;

						completed = currentIndex == captured.length();

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return obj.toString();
		} else
			return captured.toString();

	}

	@ManagedOperation(description = "Get batch instance by batch class")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Class Identifier."), @ManagedOperationParameter(name = "from", description = "From Date"),
			@ManagedOperationParameter(name = "to", description = "To Date") })
	public String getBatchInstanceByBatchClass(String identifier, String from, String to) {
		JSONArray captured = new JSONArray();
		try {
			Connection c = DBUtils.getDBConnection();

			// get the batch instance details
			String sql = "SELECT bi.creation_date as creation_date, bi.last_modified as last_modified, batch_name, bi.identifier as identifier FROM batch_instance AS bi LEFT JOIN `batch_class` bc ON bi.batch_class_id = bc.id WHERE bi.batch_status = 'FINISHED' AND bc.identifier = ?";

			if (from != null && from.length() > 0 && !from.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date >= '" + from + "'";

			if (to != null && to.length() > 0 && !to.equalsIgnoreCase("na"))
				sql += " AND bi.creation_date <= '" + to + "'";

			PreparedStatement statement = c.prepareStatement(sql);
			statement.setString(1, identifier);
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
			e.printStackTrace();
		}

		return captured.toString();

	}

	@ManagedAttribute
	public String getBatchInstancesByBC() {
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
			e.printStackTrace();
		}

		return result.toString();
	}

	@ManagedAttribute
	public String getBatchClass() {
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
			e.printStackTrace();
		}

		return result.toString();
	}

	@ManagedAttribute
	public String getBatchInstancesByPriority() {
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
			e.printStackTrace();
		}

		return result.toString();
	}

}
