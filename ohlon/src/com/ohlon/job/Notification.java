package com.ohlon.job;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.internet.MimeMessage;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.ohlon.domain.BatchInstance;
import com.ohlon.domain.Server;
import com.ohlon.service.ServerService;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Notification extends QuartzJobBean {

	private Log log = LogFactory.getLog(Notification.class);
	private static boolean initialCheckDone = false;
	private static Map<String, String> currentInstances = new HashMap<String, String>();
	private static Set<String> currentFailedServerIds = new HashSet<String>();
	private static String PUSHBULLET_URL = "https://api.pushbullet.com/v2/pushes";

	private ServerService serverService;
	private JavaMailSender sender;
	private Properties props;

	@Override
	protected void executeInternal(JobExecutionContext context) throws JobExecutionException {

		try {
			sender = (JavaMailSender) context.getMergedJobDataMap().get("mailSender");
			serverService = (ServerService) context.getMergedJobDataMap().get("serverService");
			Resource resource = new ClassPathResource("/config/application.properties");
			props = PropertiesLoaderUtils.loadProperties(resource);

			boolean serviceEnabled = "true".equalsIgnoreCase(props.getProperty("notification.enabled"));
			log.debug("Notification service enabled: " + serviceEnabled);

			if (serviceEnabled) {

				boolean sendAtFirstStartup = "true".equalsIgnoreCase(props.getProperty("notification.startup.enabled"));
				Set<BatchInstance> biToPublish = new HashSet<BatchInstance>();

				List<Server> servers = serverService.getAvailableServers();
				for (Server server : servers) {

					// Check if the server is running
					if (!server.isRunning()) {
						if (!currentFailedServerIds.contains(server.getId())) {
							publishServerFailure(server);
							currentFailedServerIds.add(server.getId());
						}
					} else {
						currentFailedServerIds.remove(server.getId());

						Set<BatchInstance> bis = server.getActiveBatchInstancesDetails();

						for (BatchInstance batchInstance : bis) {

							log.debug("Analyze: " + batchInstance);

							// If the batch instance is already listed
							if (currentInstances.containsKey(batchInstance.getIdentifier())) {

								// If the status is updated
								if (!currentInstances.get(batchInstance.getIdentifier()).equalsIgnoreCase(batchInstance.getStatus())) {
									// Update the status
									currentInstances.put(batchInstance.getIdentifier(), batchInstance.getStatus());
									// Check if the batch instance has to be
									// published
									if (isValidStatus(batchInstance))
										biToPublish.add(batchInstance);
								}

							} else {
								// We reference a new batch instance
								currentInstances.put(batchInstance.getIdentifier(), batchInstance.getStatus());
								// Check if the batch instance has to be
								// published
								if (isValidStatus(batchInstance))
									biToPublish.add(batchInstance);
							}
						}
					}

				}

				if (!initialCheckDone && sendAtFirstStartup) {
					log.debug("Should send notification at the first startup: " + sendAtFirstStartup);
					publish(biToPublish);
				} else if (initialCheckDone) {
					publish(biToPublish);
				}

				// Validate that the initial check done has been completed
				initialCheckDone = true;
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}
	}

	private void publish(Set<BatchInstance> batchInstances) {

		for (BatchInstance batchInstance : batchInstances) {
			log.debug("Publish: " + batchInstance);

			String status = batchInstance.getStatus();

			// Check which system, we need to use
			String systems = props.getProperty("notification.status." + status.replaceAll("_", "").toLowerCase());
			log.debug("  To: " + systems);
			if (systems != null && systems.length() > 0) {
				String[] listOfSystem = systems.split("\\|");
				for (String system : listOfSystem) {
					if ("email".equalsIgnoreCase(system)) {
						publishEmailNotification(batchInstance);
					} else if ("pushbullet".equalsIgnoreCase(system)) {
						publishBulletNotification(batchInstance);
					} else
						log.warn("  The system " + system + " is not a valid notification system.");
				}
			}
		}

	}

	private void publishBulletNotification(BatchInstance batchInstance) {
		try {
			log.debug("Send PushBullet notification: " + batchInstance);
			String status = batchInstance.getStatus();

			boolean pushbuletServiceEnabled = "true".equalsIgnoreCase(props.getProperty("notification.pushbullet.enabled"));
			log.debug("Server enabled: " + pushbuletServiceEnabled);

			if (pushbuletServiceEnabled) {

				List<NameValuePair> data = new ArrayList<>(1);
				data.add(new BasicNameValuePair("type", "link"));

				String title = props.getProperty("notification.pushbullet." + status.replaceAll("_", "").toLowerCase() + ".title");
				title = title.replaceAll("\\$identifier", batchInstance.getIdentifier());
				data.add(new BasicNameValuePair("title", title));

				String body = props.getProperty("notification.pushbullet." + status.replaceAll("_", "").toLowerCase() + ".body");
				body = body.replaceAll("\\$identifier", batchInstance.getIdentifier());
				data.add(new BasicNameValuePair("body", body));

				String url = batchInstance.getServer().getDcmaUrl() + "/ReviewValidate.html?batch_id=" + batchInstance.getIdentifier();
				if ("ERROR".equalsIgnoreCase(batchInstance.getStatus()))
					// Add the batch instance management page as the URL
					url = batchInstance.getServer().getDcmaUrl() + "/BatchInstanceManagement.html";
				data.add(new BasicNameValuePair("url", url));

				// Check if their a list of devices
				String devices = props.getProperty("notification.pushbullet." + status.replaceAll("_", "").toLowerCase() + ".devices");
				String emails = props.getProperty("notification.pushbullet." + status.replaceAll("_", "").toLowerCase() + ".emails");
				if (devices != null && devices.length() > 0) {
					String[] deviceList = devices.split(";");
					for (String device : deviceList) {
						data.add(new BasicNameValuePair("device_iden", device));
						sendPushBulletNotification(data);
					}
				} else if (emails != null && emails.length() > 0) {
					String[] emailList = emails.split(";");
					for (String email : emailList) {
						data.add(new BasicNameValuePair("email", email));
						sendPushBulletNotification(data);
					}
				} else {
					sendPushBulletNotification(data);
				}
			}
		} catch (Exception e) {
			log.error("An error occured", e);
			e.printStackTrace();
		}
	}

	private void sendPushBulletNotification(List<NameValuePair> data) {
		// Send the query
		try {

			HttpClientBuilder b = HttpClientBuilder.create();

			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					return true;
				}
			}).build();
			b.setSslcontext(sslContext);

			@SuppressWarnings("deprecation")
			HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
			Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create().register("http", PlainConnectionSocketFactory.getSocketFactory())
					.register("https", sslSocketFactory).build();

			// allows multi-threaded use
			PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
			b.setConnectionManager(connMgr);

			HttpClient client = b.build();

			HttpPost request = new HttpPost(PUSHBULLET_URL);
			request.setHeader("Authorization", "Bearer " + props.getProperty("notification.pushbullet.token"));
			request.setEntity(new UrlEncodedFormEntity(data, "UTF-8"));

			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String dataStr = IOUtils.toString(rd);
			log.debug(dataStr);

		} catch (Exception e) {
			log.error("An error occured", e);
			e.printStackTrace();
		}
	}

	private void publishEmailNotification(BatchInstance batchInstance) {

		try {
			log.debug("Send email notification: " + batchInstance);
			String status = batchInstance.getStatus();

			boolean emailServiceEnabled = "true".equalsIgnoreCase(props.getProperty("notification.email.enabled"));
			log.debug("Server enabled: " + emailServiceEnabled);

			if (emailServiceEnabled) {

				Map<String, Object> templateVars = new HashMap<String, Object>();
				templateVars.put("identifier", batchInstance.getIdentifier());
				templateVars.put("status", batchInstance.getStatus());
				templateVars.put("date", new Date());
				if ("ERROR".equalsIgnoreCase(batchInstance.getStatus()))
					// Add the batch instance management page as the URL
					templateVars.put("url", batchInstance.getServer().getDcmaUrl() + "/BatchInstanceManagement.html");
				else
					// Add the review/validate page
					templateVars.put("url", batchInstance.getServer().getDcmaUrl() + "/ReviewValidate.html?batch_id=" + batchInstance.getIdentifier());

				MimeMessage message = sender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
				StringBuffer content = new StringBuffer();
				try {
					Configuration freemarkerConfiguration = new Configuration();
					freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/");
					Template freemarkerTemplate = freemarkerConfiguration.getTemplate(props.getProperty("notification.email." + status.replaceAll("_", "").toLowerCase() + ".template"));
					content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, templateVars));
				} catch (Exception e) {
					log.error("An error occured", e);
				}

				helper.setFrom(props.getProperty("notification.email.from"));
				helper.setTo(props.getProperty("notification.email." + status.replaceAll("_", "").toLowerCase() + ".recipient").split(";"));

				String title = props.getProperty("notification.email." + status.replaceAll("_", "").toLowerCase() + ".title");
				title = title.replaceAll("\\$identifier", batchInstance.getIdentifier());
				message.setSubject(title);
				message.setContent(content.toString(), "text/html");

				sender.send(message);
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

	}

	private void publishEmailNotification(Server server) {

		try {
			log.debug("Send email notification: " + server);

			boolean emailServiceEnabled = "true".equalsIgnoreCase(props.getProperty("notification.email.enabled"));
			log.debug("Server enabled: " + emailServiceEnabled);

			if (emailServiceEnabled) {

				Map<String, Object> templateVars = new HashMap<String, Object>();
				templateVars.put("server", server.getLabel());
				templateVars.put("date", new Date());

				MimeMessage message = sender.createMimeMessage();
				MimeMessageHelper helper = new MimeMessageHelper(message, false, "utf-8");
				StringBuffer content = new StringBuffer();
				try {
					Configuration freemarkerConfiguration = new Configuration();
					freemarkerConfiguration.setClassForTemplateLoading(this.getClass(), "/");
					Template freemarkerTemplate = freemarkerConfiguration.getTemplate(props.getProperty("notification.email.server.template"));
					content.append(FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, templateVars));
				} catch (Exception e) {
					log.error("An error occured", e);
				}

				helper.setFrom(props.getProperty("notification.email.from"));
				helper.setTo(props.getProperty("notification.email.server.recipient").split(";"));

				String title = props.getProperty("notification.email.server.title");
				title = title.replaceAll("\\$server", server.getLabel());
				message.setSubject(title);
				message.setContent(content.toString(), "text/html");

				sender.send(message);
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}

	}

	private void publishBulletNotification(Server server) {
		try {
			log.debug("Send PushBullet notification: " + server);

			boolean pushbuletServiceEnabled = "true".equalsIgnoreCase(props.getProperty("notification.pushbullet.enabled"));
			log.debug("Server enabled: " + pushbuletServiceEnabled);

			if (pushbuletServiceEnabled) {

				List<NameValuePair> data = new ArrayList<>(1);
				data.add(new BasicNameValuePair("type", "note"));

				String title = props.getProperty("notification.pushbullet.server.title");
				title = title.replaceAll("\\$server", server.getLabel());
				data.add(new BasicNameValuePair("title", title));

				String body = props.getProperty("notification.pushbullet.server.body");
				body = body.replaceAll("\\$server", server.getLabel());
				data.add(new BasicNameValuePair("body", body));

				// Check if their a list of devices
				String devices = props.getProperty("notification.pushbullet.server.devices");
				String emails = props.getProperty("notification.pushbullet.server.emails");
				if (devices != null && devices.length() > 0) {
					String[] deviceList = devices.split(";");
					for (String device : deviceList) {
						data.add(new BasicNameValuePair("device_iden", device));
						sendPushBulletNotification(data);
					}
				} else if (emails != null && emails.length() > 0) {
					String[] emailList = emails.split(";");
					for (String email : emailList) {
						data.add(new BasicNameValuePair("email", email));
						sendPushBulletNotification(data);
					}
				} else {
					sendPushBulletNotification(data);
				}
			}
		} catch (Exception e) {
			log.error("An error occured", e);
		}
	}

	private void publishServerFailure(Server server) {
		// Check which system, we need to use
		String systems = props.getProperty("notification.server");
		log.debug("  To: " + systems);
		if (systems != null && systems.length() > 0) {
			String[] listOfSystem = systems.split("\\|");
			for (String system : listOfSystem) {
				if ("email".equalsIgnoreCase(system)) {
					publishEmailNotification(server);
				} else if ("pushbullet".equalsIgnoreCase(system)) {
					publishBulletNotification(server);
				} else
					log.warn("  The system " + system + " is not a valid notification system.");
			}
		}
	}

	private boolean isValidStatus(BatchInstance batchInstance) {
		return "READY_FOR_REVIEW".equalsIgnoreCase(batchInstance.getStatus()) || "READY_FOR_VALIDATION".equalsIgnoreCase(batchInstance.getStatus())
				|| "ERROR".equalsIgnoreCase(batchInstance.getStatus());
	}

	public void setServerService(ServerService serverService) {
		this.serverService = serverService;
	}

}