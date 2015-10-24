package com.ohlon.ephesoft.jmx;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ephesoft.dcma.util.ApplicationConfigProperties;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=application-details", description = "Provides information about the application.")
public class ApplicationDetails {

	private static final Logger log = Logger.getLogger(ApplicationDetails.class.getName());

	private LicenseService licenseService;

	@ManagedOperation(description = "Get application details")
	public String getApplicationDetails() {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Application Details");

		JSONObject data = new JSONObject();

		try {
			ApplicationConfigProperties properties = ApplicationConfigProperties.getApplicationConfigProperties();
			data.put("ephesoft.version", properties.getProperty("ephesoft.product.version"));
			data.put("os.name", System.getProperty("os.name"));
			data.put("ohlon.expiration", licenseService.getMessage());

		} catch (Exception e) {
			log.error(e.getMessage());
		}

		log.debug("Result: " + data);

		return data.toString();
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}

}
