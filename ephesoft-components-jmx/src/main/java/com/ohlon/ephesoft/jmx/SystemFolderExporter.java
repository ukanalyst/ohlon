package com.ohlon.ephesoft.jmx;

import java.io.File;
import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.stereotype.Component;

import com.ephesoft.dcma.da.domain.BatchInstance;
import com.ephesoft.dcma.da.service.BatchInstanceService;
import com.ohlon.ephesoft.service.LicenseService;

@Component
@ManagedResource(objectName = "ephesoft:type=system-folder", description = "System folder exporter")
public class SystemFolderExporter {

	private static final Logger log = Logger.getLogger(SystemFolderExporter.class.getName());

	private LicenseService licenseService;

	/**
	 * Initializing batchInstanceService {@link BatchInstanceService}.
	 */
	@Autowired
	private BatchInstanceService batchInstanceService;

	@ManagedOperation(description = "Get batch class instance artifact")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier.") })
	public String getBatchInstanceFiles(String identifier) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instance Files: identifier=" + identifier);

		JSONObject result = new JSONObject();
		BatchInstance batchInstance = batchInstanceService.getBatchInstanceByIdentifier(identifier);

		try {
			if (batchInstance != null) {

				String folderName = batchInstance.getLocalFolder() + File.separator + identifier;
				File folder = new File(folderName);

				if (folder.exists()) {
					File[] files = folder.listFiles();
					JSONArray listoffiles = new JSONArray();
					for (File file : files) {
						JSONObject fileObj = new JSONObject();
						fileObj.put("name", file.getName());
						listoffiles.put(fileObj);
					}

					result.put("success", true);
					result.put("files", listoffiles);
				} else {
					result.put("success", false);
					result.put("message", "The folder " + folderName + " doesn't exist anymore.");
				}

			} else {
				result.put("success", false);
				result.put("message", "The batch instance doesn't exit.");
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		log.debug("Result: " + result);

		return result.toString();
	}

	@ManagedOperation(description = "Get batch class execution details")
	@ManagedOperationParameters({ @ManagedOperationParameter(name = "identifier", description = "Batch Instance Identifier."),
			@ManagedOperationParameter(name = "fileName", description = "File name.") })
	public String getBatchInstanceFile(String identifier, String fileName) {

		if (!licenseService.checkLicense()) {
			log.error("License expired");
			return null;
		}

		log.debug("Get Batch Instance File: identifier=" + identifier + "; fileName=" + fileName);

		JSONObject result = new JSONObject();
		BatchInstance batchInstance = batchInstanceService.getBatchInstanceByIdentifier(identifier);

		try {
			if (batchInstance != null) {

				String name = batchInstance.getLocalFolder() + File.separator + identifier + File.separator + fileName;
				File file = new File(name);

				if (file.exists()) {
					String extension = FilenameUtils.getExtension(fileName);
					if (extension.toLowerCase().equalsIgnoreCase("xml")) {
						String data = FileUtils.readFileToString(file, "UTF-8");
						result.put("success", true);
						result.put("data", data);
					} else if (extension.toLowerCase().equalsIgnoreCase("zip")) {
						ZipInputStream zin = new ZipInputStream(new FileInputStream(file));
						for (ZipEntry e; (e = zin.getNextEntry()) != null;) {
							String entryExtension = FilenameUtils.getExtension(e.getName());
							if (entryExtension.equalsIgnoreCase("xml")) {
								String data = IOUtils.toString(zin);
								result.put("success", true);
								result.put("data", data);
							}
						}
					}
				} else {
					result.put("success", false);
					result.put("message", "The file " + name + " doesn't exist anymore.");
				}

			} else {
				result.put("success", false);
				result.put("message", "The batch instance doesn't exit.");
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return result.toString();
	}

	public void setLicenseService(LicenseService licenseService) {
		this.licenseService = licenseService;
	}
}
