package com.ohlon.ephesoft.jmx;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ohlon.ephesoft.db.utils.DBUtils;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=database-query-executer", description = "Execute query over the database")
public class DatabaseQueryExecuter {

	private static final Logger log = Logger.getLogger(DatabaseQueryExecuter.class.getName());

	private LicenseService licenseService;

	@ManagedOperation(description = "Execute query")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "database", description = "database"), @ManagedOperationParameter(name = "query", description = "Query to execute") })
	public String executeQuery(String database, String query) {

		JSONArray data = new JSONArray();

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Execute query: database=" + database + "; query=" + query);

		try {
			Connection c = null;
			if ("engine".equalsIgnoreCase(database))
				c = DBUtils.getDBConnection();
			else if ("reporting".equalsIgnoreCase(database))
				c = DBUtils.getReportDBConnection();

			if (c == null)
				throw new Exception("The database " + database + " is not reachable.");

			PreparedStatement statement = c.prepareStatement(query);

			log.debug(statement.toString());

			ResultSet rs = statement.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnCount = rsmd.getColumnCount();

			Set<String> columns = new HashSet<String>();
			for (int i = 1; i < columnCount + 1; i++) {
				String name = rsmd.getColumnName(i);
				columns.add(name);
			}

			while (rs.next()) {
				JSONObject row = new JSONObject();

				for (String columnName : columns) {
					row.put(columnName, rs.getString(columnName));
				}

				data.put(row);
			}

			// Close the query
			rs.close();
			statement.close();
			c.close();

		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + data);

		return data.toString();
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
