window.onload = function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

	refreshEntryList();
	setInterval(refreshEntryList, 5000);

	$("#removeAllEntries").click(function() {
		BootstrapDialog.confirm({
			title : 'Confirmation',
			message : 'Are you sure that you want to delete all log entries?',
			type : BootstrapDialog.TYPE_WARNING,
			closable : true,
			callback : function(result) {
				if (result) {
					$.ajax({
						url : JOLOKIA_URL + '/exec/ephesoft:type=ohlon-logger/emptyLogEntries',
						dataType : "json",
						headers : headers,
						success : function() {
							$("#logEntries tbody tr").fadeOut();
						}
					});
				}
			}
		});
	});

	function refreshEntryList() {
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=ohlon-logger/getLogEntries',
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);

				for (var i = 0; i < d.length; i++) {
					var entry = d[i];
					var logId = entry.id;
					if ($("tr.logEntry#" + logId).length == 0) {

						var stacktrace = entry.throwableStrRep.join("<br />").replace(/\t/g, "&nbsp;&nbsp;&nbsp;&nbsp;");
<<<<<<< HEAD
						
						// Add the logentry
						var html = "<tr id='" + entry.id + "' class='logEntry'>";
						html += "<td><span class='viewStackTrace glyphicon glyphicon-list'></span>&nbsp;&nbsp;<span class='deleteEntry glyphicon glyphicon-remove'></span>&nbsp;<div style='display:none;' class='stacktrace'>" + stacktrace + "</div></td>";
						html += "<td>" + entry.date + "</td>";
						html += "<td>" + entry.threadName + "</td>";
						html += "<td>" + entry.logLevel + "</td>";
						html += "<td>" + entry.loggerName + "</td>";
						html += "<td>" + entry.message + "</td>";
=======

						// Add the logentry
						var html = "<tr id='" + entry.id + "' class='logEntry'>";
						html += "<td>" + entry.date + "</td>";
						html += "<td>" + entry.threadName + "</td>";
						html += "<td>" + entry.logLevel + "</td>";
						html += "<td>" + entry.loggerName + "</td>";
						html += "<td>" + entry.message + "</td>";
						html += "<td><span class='viewStackTrace glyphicon glyphicon-list'></span>&nbsp;&nbsp;<span class='deleteEntry glyphicon glyphicon-remove'></span>&nbsp;<div style='display:none;' class='stacktrace'>" + stacktrace + "</div></td>";
>>>>>>> branch 'master' of https://github.com/bchevallereau/ohlon.git
						html += "</tr>";

						var newLine = $(html);

						$(newLine).find('.viewStackTrace').click(function() {
							var msg = $(this).closest("tr").find('.stacktrace').html();

							BootstrapDialog.show({
								title : 'Stack Trace',
								message : msg
							});
						});

						$(newLine).find('.deleteEntry').click(function() {
							var entryId = $(this).closest("tr").attr("id");
							BootstrapDialog.confirm({
								title : 'Confirmation',
								message : 'Are you sure that you want to delete this log entry?',
								type : BootstrapDialog.TYPE_WARNING,
								closable : true,
								callback : function(result) {
									if (result) {
										$.ajax({
											url : JOLOKIA_URL + '/exec/ephesoft:type=ohlon-logger/deleteLogEntry(long)/' + entryId,
											dataType : "json",
											headers : headers,
											success : function() {
												$("tr#" + entryId).fadeOut();
											}
										});
									}
								}
							});
						});

						$("table#logEntries tbody").append(newLine);
					}
				}
			}
		});
	}
}
