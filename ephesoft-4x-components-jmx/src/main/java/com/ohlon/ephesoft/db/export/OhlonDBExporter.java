package com.ohlon.ephesoft.db.export;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;

import com.ephesoft.dcma.batch.schema.Batch;
import com.ephesoft.dcma.batch.schema.DocField;
import com.ephesoft.dcma.batch.schema.Document;
import com.ephesoft.dcma.batch.service.BatchSchemaService;
import com.ephesoft.dcma.core.DCMAException;
import com.ephesoft.dcma.core.exception.DCMAApplicationException;
import com.ephesoft.dcma.da.util.ConnectionPoolFactory;
import com.ephesoft.dcma.util.EphesoftStringUtil;
import com.ohlon.ephesoft.db.export.domain.BatchInstanceValue;
import com.ohlon.ephesoft.db.utils.DBUtils;

@Service
@Configurable
public class OhlonDBExporter {

	private static final Logger LOGGER = Logger.getLogger(OhlonDBExporter.class.getName());
	private static OhlonDBExporter _exporter;

	@Autowired
	private BatchSchemaService batchSchemaService;

	public void init() {
		OhlonDBExporter.setInstance(this);
	}
	
	private static void setInstance(OhlonDBExporter ohlonDBExporter) {
		_exporter = ohlonDBExporter;
	}
	
	public static OhlonDBExporter getInstance() {
		return _exporter;
	}

	public void export(String batchInstanceId) throws DCMAException {
		LOGGER.info("Entering OHLON_REPORT_EXPORTER... [" + batchInstanceId + "]");
		try {
			Batch batch = this.batchSchemaService.getBatch(batchInstanceId);
			List<BatchInstanceValue> values = prepareValues(batch);
			List<String> queries = new ArrayList<String>();
			for (BatchInstanceValue biv : values)
				queries.add(biv.createSQLInsertQuery());
			insertItems(queries);
		} catch (Exception exception) {
			String errorMessage = EphesoftStringUtil.concatenate(new String[] { "Ohlon Export Plugin: Problem occured in exporting batch document level fields to database ", exception.getMessage() });

			LOGGER.error(errorMessage, exception);
			throw new DCMAException(errorMessage, exception);
		}
	}

	private List<BatchInstanceValue> prepareValues(Batch batch) {
		List<BatchInstanceValue> values = new ArrayList<BatchInstanceValue>();

		Date creationDate = null;
		String creationDateStr = batch.getBatchCreationDate();
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			creationDate = sdf.parse(creationDateStr);
		} catch (ParseException e) {
			LOGGER.error("An error occured during parsing the date: " + creationDateStr, e);
		}

		List<Document> documents = batch.getDocuments().getDocument();
		for (Document document : documents) {
			// Add all fields
			List<DocField> fields = document.getDocumentLevelFields().getDocumentLevelField();
			for (DocField docField : fields) {
				BatchInstanceValue biv = new BatchInstanceValue(batch.getBatchInstanceIdentifier(), batch.getBatchClassIdentifier(), document.getIdentifier(), creationDate);

				if ("STRING".equalsIgnoreCase(docField.getType()))
					biv.setStringValue(docField.getName(), docField.getValue(), docField.getOcrConfidence());
				else if ("LONG".equalsIgnoreCase(docField.getType()) || "INTEGER".equalsIgnoreCase(docField.getType()))
					biv.setLongValue(docField.getName(), Long.valueOf(docField.getValue()), docField.getOcrConfidence());
				else if ("DOUBLE".equalsIgnoreCase(docField.getType()))
					biv.setDoubleValue(docField.getName(), Double.valueOf(docField.getValue()), docField.getOcrConfidence());
				else if ("FLOAT".equalsIgnoreCase(docField.getType()))
					biv.setFloatValue(docField.getName(), Float.valueOf(docField.getValue()), docField.getOcrConfidence());
				else if ("BOOLEAN".equalsIgnoreCase(docField.getType()))
					biv.setBooleanValue(docField.getName(), Boolean.parseBoolean(docField.getValue()), docField.getOcrConfidence());
				else
					LOGGER.debug("The following type is not supported: " + docField.getType());

				values.add(biv);
			}

			// Add the document type
			BatchInstanceValue biv = new BatchInstanceValue(batch.getBatchInstanceIdentifier(), batch.getBatchClassIdentifier(), document.getIdentifier(), creationDate);
			biv.setStringValue("_DOCUMENT_TYPE_", document.getType(), document.getConfidence());
			values.add(biv);
			
			// Add the # of page
			BatchInstanceValue nbOfPages = new BatchInstanceValue(batch.getBatchInstanceIdentifier(), batch.getBatchClassIdentifier(), document.getIdentifier(), creationDate);
			nbOfPages.setFloatValue("_NB_OF_PAGES_", document.getPages().getPage().size() ,100);
			values.add(nbOfPages);
		}

		return values;
	}

	private void insertItems(List<String> queries) throws DCMAApplicationException, SQLException {
		Connection dbConnection = DBUtils.getReportDBConnection();
		try {
			if (null != dbConnection) {
				dbConnection.setAutoCommit(false);
				Statement statement = null;
				try {
					for (String query : queries) {
						LOGGER.debug("Executing query: " + query);
						if (!EphesoftStringUtil.isNullOrEmpty(query)) {
							statement = dbConnection.createStatement();
							statement.executeUpdate(query);
							statement.close();
						}
					}
					if (null != statement)
						statement.close();
				} finally {
					if (null != statement)
						statement.close();
				}
				dbConnection.commit();
				dbConnection.setAutoCommit(true);
			}
			ConnectionPoolFactory.releaseConnection(dbConnection);
		} catch (Exception exception) {
			if ((null != dbConnection) && (!dbConnection.isClosed())) {
				ConnectionPoolFactory.releaseConnectionOnError(dbConnection);
			}
			throw new DCMAApplicationException("Error occured in executing query", exception);
		}
	}
}
