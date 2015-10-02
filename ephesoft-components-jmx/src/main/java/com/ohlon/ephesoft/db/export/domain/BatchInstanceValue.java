package com.ohlon.ephesoft.db.export.domain;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.ephesoft.dcma.util.EphesoftStringUtil;

public class BatchInstanceValue {

	private String batchInstanceIdentifier;
	private String batchClassIdentifier;
	private String documentIdentifier;
	private Date creationDate;

	private String fieldName;
	private double fieldConfidence;
	private String TYPE;
	private boolean booleanValue;
	private long longValue;
	private float floatValue;
	private double doubleValue;
	private String stringValue;

	public BatchInstanceValue(String batchInstanceIdentifier, String batchClassIdentifier, String documentIdentifier, Date creationDate) {
		this.batchInstanceIdentifier = batchInstanceIdentifier;
		this.batchClassIdentifier = batchClassIdentifier;
		this.documentIdentifier = documentIdentifier;
		this.creationDate = creationDate;
		this.TYPE = "";
	}

	public void setBooleanValue(String fieldName, boolean fieldValue, double fieldConfidence) {
		this.fieldName = fieldName;
		this.fieldConfidence = fieldConfidence;
		this.booleanValue = fieldValue;
		this.TYPE = "BOOLEAN";
	}

	public void setLongValue(String fieldName, long fieldValue, double fieldConfidence) {
		this.fieldName = fieldName;
		this.fieldConfidence = fieldConfidence;
		this.longValue = fieldValue;
		this.TYPE = "LONG";
	}

	public void setFloatValue(String fieldName, float fieldValue, double fieldConfidence) {
		this.fieldName = fieldName;
		this.fieldConfidence = fieldConfidence;
		this.floatValue = fieldValue;
		this.TYPE = "FLOAT";
	}

	public void setDoubleValue(String fieldName, double fieldValue, double fieldConfidence) {
		this.fieldName = fieldName;
		this.fieldConfidence = fieldConfidence;
		this.doubleValue = fieldValue;
		this.TYPE = "DOUBLE";
	}

	public void setStringValue(String fieldName, String fieldValue, double fieldConfidence) {
		this.fieldName = fieldName;
		this.fieldConfidence = fieldConfidence;
		this.stringValue = fieldValue;
		this.TYPE = "STRING";
	}

	public String createSQLInsertQuery() {
		String query = null;
		if (TYPE != null && TYPE.length() > 0) {
			String databaseTableName = "ohlon_report_properties";

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

			String columnNames = "batch_instance_identifier,batch_class_identifier,document_identifier,batch_creation_date,_field_name_,_field_confidence_";
			if ("BOOLEAN".equalsIgnoreCase(TYPE))
				columnNames += ",boolean_value";
			else if ("LONG".equalsIgnoreCase(TYPE))
				columnNames += ",long_value";
			else if ("FLOAT".equalsIgnoreCase(TYPE))
				columnNames += ",float_value";
			else if ("DOUBLE".equalsIgnoreCase(TYPE))
				columnNames += ",double_value";
			else if ("STRING".equalsIgnoreCase(TYPE))
				columnNames += ",string_value";

			String columnValues = "'" + batchInstanceIdentifier + "',";
			columnValues += "'" + batchClassIdentifier + "',";
			columnValues += "'" + documentIdentifier + "',";
			columnValues += "'" + sdf.format(creationDate) + "',";
			columnValues += "'" + fieldName + "',";
			columnValues += fieldConfidence + ",";

			if ("BOOLEAN".equalsIgnoreCase(TYPE))
				columnValues += booleanValue ? "TRUE" : "FALSE";
			else if ("LONG".equalsIgnoreCase(TYPE))
				columnValues += longValue;
			else if ("FLOAT".equalsIgnoreCase(TYPE))
				columnValues += floatValue;
			else if ("DOUBLE".equalsIgnoreCase(TYPE))
				columnValues += doubleValue;
			else if ("STRING".equalsIgnoreCase(TYPE))
				columnValues += "'" + stringValue + "'";

			query = EphesoftStringUtil.concatenate(new String[] { "INSERT INTO ", databaseTableName, "(", columnNames, ")", " VALUES ", "(", columnValues, ")" });
		}
		return query;
	}
}
