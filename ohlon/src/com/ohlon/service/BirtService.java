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
}
