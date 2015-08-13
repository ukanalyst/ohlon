package com.ohlon.ephesoft.db.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBUtils {

	private static final Logger log = Logger.getLogger(DBUtils.class.getName());
	private static boolean IS_MSSQL;

	public static Connection getDBConnection() {
		Connection c = null;

		try {
			// get the database connection properties from the ephesoft
			// configuration
			Properties p = new Properties();
			String home = System.getenv("DCMA_HOME");
			File propFile = new File(home, "WEB-INF/classes/META-INF/dcma-data-access/dcma-db.properties");
			InputStream is = new FileInputStream(propFile);
			p.load(is);
			
			IS_MSSQL = "org.hibernate.dialect.SQLServerDialect".equalsIgnoreCase((String) p.get("dataSource.dialect"));
			log.info("Is MSSQL: " + IS_MSSQL);

			// get the connection information from the properties file
			String username = (String) p.get("dataSource.username");
			String password = (String) p.get("dataSource.password");
			String driver = (String) p.get("dataSource.driverClassName");
			String db = (String) p.get("dataSource.databaseName");
			String server = (String) p.get("dataSource.serverName");
			String url = (String) p.get("dataSource.url");

			// fix the URL by substituting in the parameters
			url = url.replace("${dataSource.serverName}", server);
			url = url.replace("${dataSource.databaseName}", db);
			url = url.replace("${dataSource.username}", username);
			url = url.replace("${dataSource.password}", password);

			// get a connection to the database
			Class.forName(driver).newInstance();
			c = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			System.err.println(e);
			log.log(Level.SEVERE, e.getMessage());
		}

		return c;
	}
	
	public static Connection getReportDBConnection() {
		Connection c = null;
		
		try {
			// get the database connection properties from the ephesoft
			// configuration
			Properties p = new Properties();
			String home = System.getenv("DCMA_HOME");
			File propFile = new File(home, "WEB-INF/classes/META-INF/dcma-reporting/etl-variables.properties");
			InputStream is = new FileInputStream(propFile);
			p.load(is);
			
			// get the connection information from the properties file
			String hostname = (String) p.get("reporting.hostname");
			String port = (String) p.get("reporting.port");
			String dbname = (String) p.get("reporting.dbname");
			String username = (String) p.get("reporting.username");
			String password = (String) p.get("reporting.password");
			String driver = (String) p.get("reporting.driverClassName");
			String url = (String) p.get("reporting.url");
			
			// fix the URL by substituting in the parameters
			url = url.replace("$dcmareport{reporting.hostname}", hostname);
			url = url.replace("$dcmareport{reporting.port}", port);
			url = url.replace("$dcmareport{reporting.dbname}", dbname);
			
			// get a connection to the database
			Class.forName(driver).newInstance();
			c = DriverManager.getConnection(url, username, password);
		} catch (Exception e) {
			System.err.println(e);
			log.log(Level.SEVERE, e.getMessage());
		}
		
		return c;
	}
	
	public static boolean isMSSQL() {
		return IS_MSSQL;
	}
}
