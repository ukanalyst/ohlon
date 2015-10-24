package com.ohlon.service.impl;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.util.UriUtils;

import com.ohlon.domain.Server;
import com.ohlon.helper.BirtHelper;
import com.ohlon.service.BirtService;
import com.ohlon.service.ServerService;

public class BirtServiceImpl implements BirtService {

	private Log log = LogFactory.getLog(BirtServiceImpl.class);

	@Autowired
	protected ServerService serverService;

	@Override
	public Server getServer(String serverId) {
		return serverService.getServer(serverId);
	}

	@Override
	public JSONArray getBatchClassList(String serverId) {
		log.debug("getBatchClassList: serverId=" + serverId);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String url = jolokiaUrl + "/read/ephesoft:type=batchinstance-stats/BatchClass";
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getBatchInstances(String serverId, String batchClass, Date from, Date to) throws ParseException {
		log.debug("getBatchInstances: serverId=" + serverId + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=batchinstance-stats/getBatchInstanceByBatchClass(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer)/"
				+ batchClass + "/" + _from + "/" + _to + "/-1/-1";
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getBatchRepartition(String serverId, String batchClass, Date from, Date to) throws ParseException {
		log.debug("getBatchRepartition: serverId=" + serverId + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=batchinstance-stats/getBatchClassRepartition(java.lang.String,java.lang.String,java.lang.String)/" + batchClass + "/" + _from + "/" + _to;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getBatchAccumulation(String serverId, String batchClass, Date from, Date to) throws ParseException {
		log.debug("getBatchAccumulation: serverId=" + serverId + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=batchinstance-stats/getBatchClassAccumulation(java.lang.String,java.lang.String,java.lang.String)/" + batchClass + "/" + _from + "/" + _to;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getManualStepsExecutionDetails(String serverId, String batchClass, Date from, Date to, String user) throws ParseException {
		log.debug("getManualStepsExecutionDetails: serverId=" + serverId + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to + "; user=" + user);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);
		String _user = user == null || user.length() == 0 ? "na" : user;

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getManualStepsExecutionDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String)/" + batchClass + "/" + _from
				+ "/" + _to + "/" + _user;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getManualStepsRepartitionDetails(String serverId, String module, String batchClass, Date from, Date to, String user) throws ParseException {
		log.debug("getManualStepsRepartitionDetails: serverId=" + serverId + "; module=" + module + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to + "; user=" + user);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);
		String _user = user == null || user.length() == 0 ? "na" : user;

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getManualStepsRepartitionDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/" + module
				+ "/" + batchClass + "/" + _from + "/" + _to + "/" + _user;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getManualStepsAccumulationDetails(String serverId, String module, String batchClass, Date from, Date to, String user) throws ParseException {
		log.debug("getManualStepsAccumulationDetails: serverId=" + serverId + "; module=" + module + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to + "; user=" + user);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);
		String _user = user == null || user.length() == 0 ? "na" : user;

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getManualStepsAccumulationDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/"
				+ module + "/" + batchClass + "/" + _from + "/" + _to + "/" + _user;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getBatchClassExecutionDetails(String serverId, String batchClass, Date from, Date to) throws ParseException {
		log.debug("getBatchClassExecutionDetails: serverId=" + serverId + "; batchClass=" + batchClass + "; from=" + from + "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getBatchClassExecutionDetails(java.lang.String,java.lang.String,java.lang.String)/" + batchClass + "/" + _from + "/" + _to;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getArtifactRepartitionDetails(String serverId, String batchClass, String workflowType, String workflowName, Date from, Date to) throws ParseException {
		log.debug("getArtifactRepartitionDetails: serverId=" + serverId + "; batchClass=" + batchClass + "; workflowType=" + workflowType + "; workflowName=" + workflowName + "; from=" + from
				+ "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getArtifactRepartitionDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/"
				+ batchClass + "/" + workflowType + "/" + workflowName + "/" + _from + "/" + _to;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray getArtifactAccumulationDetails(String serverId, String batchClass, String workflowType, String workflowName, Date from, Date to) throws ParseException {
		log.debug("getArtifactAccumulationDetails: serverId=" + serverId + "; batchClass=" + batchClass + "; workflowType=" + workflowType + "; workflowName=" + workflowName + "; from=" + from
				+ "; to=" + to);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		String _from = from == null ? "na" : BirtHelper.toString(from);
		String _to = to == null ? "na" : BirtHelper.toString(to);

		String url = jolokiaUrl + "/exec/ephesoft:type=reporting-stats/getArtifactAccumulationDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/"
				+ batchClass + "/" + workflowType + "/" + workflowName + "/" + _from + "/" + _to;
		try {
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	@Override
	public JSONArray executeQuery(String serverId, String database, String query) throws ParseException {
		log.debug("executeQuery: serverId=" + serverId + "; database=" + database + "; query=" + query);

		// Get server
		Server server = getServer(serverId);
		String jolokiaUrl = server.getJolokiaUrl();
		log.debug("Jolokia URL: " + jolokiaUrl);

		try {
			String url = jolokiaUrl + "/exec/ephesoft:type=database-query-executer/executeQuery(java.lang.String,java.lang.String)/" + database + "/" + UriUtils.encodeQueryParam(query, "UTF-8");
			String response = query(server, url);
			JSONObject jsonResponse = new JSONObject(response);
			if (jsonResponse.getInt("status") == 200)
				return new JSONArray(jsonResponse.getString("value"));
		} catch (Exception e) {
			log.error("An error occured", e);
		}

		return new JSONArray();
	}

	private String query(Server server, String url) throws ClientProtocolException, IOException {
		String result = "";

		// Prepare the authentication
		URL _url = new URL(url);
		int port = _url.getPort() == -1 ? 80 : _url.getPort();
		HttpHost target = new HttpHost(_url.getHost(), port, _url.getProtocol());
		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(new AuthScope(target.getHostName(), port), new UsernamePasswordCredentials(server.getUsername(), server.getPassword()));
		CloseableHttpClient httpclient = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();

		try {
			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(target, basicAuth);

			// Add AuthCache to the execution context
			HttpClientContext localContext = HttpClientContext.create();
			localContext.setAuthCache(authCache);

			HttpGet httpget = new HttpGet(_url.getPath());

			log.debug("Executing request " + httpget.getRequestLine() + " to target " + target);
			CloseableHttpResponse response = httpclient.execute(target, httpget, localContext);
			try {
				log.debug(response.getStatusLine());
				result = EntityUtils.toString(response.getEntity(), "UTF-8");
				EntityUtils.consume(response.getEntity());
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
		}
		return result;
	}
}
