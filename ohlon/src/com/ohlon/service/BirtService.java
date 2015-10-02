package com.ohlon.service;

import java.text.ParseException;
import java.util.Date;

import org.json.JSONArray;

import com.ohlon.domain.Server;

public interface BirtService {

	Server getServer(String serverId);
	JSONArray getBatchClassList(String serverId);
	JSONArray getBatchInstances(String serverId, String batchClass, Date from, Date to) throws ParseException;
	JSONArray getBatchRepartition(String serverId, String batchClass, Date from, Date to) throws ParseException;
	JSONArray getBatchAccumulation(String serverId, String batchClass, Date from, Date to) throws ParseException;
	JSONArray getManualStepsExecutionDetails(String serverId, String batchClass, Date from, Date to, String user) throws ParseException;
	JSONArray getManualStepsRepartitionDetails(String serverId, String module, String batchClass, Date from, Date to, String user) throws ParseException;
	JSONArray getManualStepsAccumulationDetails(String serverId, String module, String batchClass, Date from, Date to, String user) throws ParseException;
	JSONArray getBatchClassExecutionDetails(String serverId, String batchClass, Date from, Date to) throws ParseException;
	JSONArray getArtifactRepartitionDetails(String serverId, String batchClass, String workflowType, String workflowName, Date from, Date to) throws ParseException;
	JSONArray getArtifactAccumulationDetails(String serverId, String batchClass, String workflowType, String workflowName, Date from, Date to) throws ParseException;
	JSONArray executeQuery(String serverId, String database, String query) throws ParseException;
}
