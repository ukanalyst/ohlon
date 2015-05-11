package com.ohlon.ephesoft.jmx;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ephesoft.dcma.util.ApplicationConfigProperties;

@Component
@ManagedResource(objectName = "ephesoft:type=application-details", description = "Provides information about the application.")
public class ApplicationDetails {

	private static final Logger log = Logger.getLogger(ApplicationDetails.class.getName());

	@ManagedOperation(description = "Get application details")
	public String getApplicationDetails() {
		JSONObject data = new JSONObject();

		try {
			ApplicationConfigProperties properties = ApplicationConfigProperties.getApplicationConfigProperties();
			data.put("ephesoft.version", properties.getProperty("ephesoft.product.version"));
			data.put("os.name", System.getProperty("os.name"));

		} catch (Exception e) {
			log.log(Level.SEVERE, e.getMessage());
			System.err.println(e);
		}
		return data.toString();
	}

}
