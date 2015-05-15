window.onload = function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

	$('.input-group.date').datetimepicker();

	$("#refreshReports").click(function(event) {
		var bc = $("#batchclass option:selected").attr("id");
		var from = $("#from").val();
		var to = $("#to").val();
		var user = $("#user").val();

		if (from && from.length > 0)
			from = moment(from, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		else
			from = "na";
		if (to && to.length > 0)
			to = moment(to, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		else
			to = "na";
		if (!(user && user.length > 0))
			user = "na";

		$("#review td[data-attr]").html("");
		$("#validation td[data-attr]").html("");
		$(".metric-main").html("");

		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getManualStepsExecutionDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String)/' + bc + '/' + from + '/' + to + '/' + user,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);

				for (var i = 0; i < d.length; i++) {
					var containerId = "";
					if (d[i].BATCH_STATUS == "READY_FOR_REVIEW" || d[i].BATCH_STATUS == "REVIEW")
						containerId = "review";
					else
						containerId = "validation";

					$("#" + containerId + " td[data-attr='MINDURATION']").html(convertDuration(d[i].MINDURATION));
					$("#" + containerId + " td[data-attr='AVGDURATION']").html(convertDuration(d[i].AVGDURATION));
					$("#" + containerId + " td[data-attr='MAXDURATION']").html(convertDuration(d[i].MAXDURATION));

					$("#" + containerId + " td[data-attr='AVGDOCPS']").html(Number(d[i].AVGDOCPS * 60 * 60).toFixed(1));
					$("#" + containerId + " td[data-attr='MINDOCPS']").html(Number(d[i].MINDOCPS * 60 * 60).toFixed(1));
					$("#" + containerId + " td[data-attr='MAXDOCPS']").html(Number(d[i].MAXDOCPS * 60 * 60).toFixed(1));

					$("#" + containerId + " td[data-attr='AVGPAGEPS']").html(Number(d[i].AVGDOCPS * 60).toFixed(1));
					$("#" + containerId + " td[data-attr='MINPAGEPS']").html(Number(d[i].MINDOCPS * 60).toFixed(1));
					$("#" + containerId + " td[data-attr='MAXPAGEPS']").html(Number(d[i].MAXDOCPS * 60).toFixed(1));

					// Populate metrics
					$(".metric-main.number." + containerId).html(d[i].NBOFBATCHINSTANCES)
					$(".metric-main.document." + containerId).html(d[i].NBOFDOCUMENTS)
					$(".metric-main.page." + containerId).html(d[i].NBOFPAGES)
				}

				// Update graphs
				var param = "";
				if (bc && bc.length > 0)
					param += "&bc=" + bc;
				if (name && name.length > 0)
					param += "&name=" + name;
				if (from && from.length > 0)
					param += "&from=" + from;
				if (to && to.length > 0)
					param += "&to=" + to;
				if (user && user.length > 0)
					param += "&user=" + user;
				if (param.length > 0)
					param = '?' + param.substring(1);

				$('#review-repartition').attr("src", "graph/user/review-repartition.do" + param);
				$('#validation-repartition').attr("src", "graph/user/validation-repartition.do" + param);
				$('#review-accumulation').attr("src", "graph/user/review-accumulation.do" + param);
				$('#validation-accumulation').attr("src", "graph/user/validation-accumulation.do" + param);

			}
		});

		event.preventDefault();
	});

	$.ajax({
		url : JOLOKIA_URL + "/read/ephesoft:type=batchinstance-stats/BatchClass",
		dataType : "json",
		headers : headers,
		success : function(data) {
			var d = eval(data.value);
			for (var i = 0; i < d.length; i++) {
				$("#batchclass").append("<option id='" + d[i].batchClassId + "'>" + d[i].batchClassId + " (" + d[i].batchClassName + ")</option>");
			}
		}
	});

	// Display the last sync date
	$.ajax({
		url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getLatestReportSyncDate',
		dataType : "json",
		headers : headers,
		success : function(data) {
			var date = data.value;

			if (date == null || date.length == 0)
				$(".lastsync").html("NEVER");
			else
				$(".lastsync").html(date);
		}
	});

	// Create the event on report sync db
	$("#syncDatabase").click(function(event) {
		$(".lastsync").html("Processing...");

		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/refreshReportDatabase',
			dataType : "json",
			headers : headers,
			success : function(data) {
				var date = data.value;

				if (date == null || date.length == 0)
					$(".lastsync").html("NEVER");
				else
					$(".lastsync").html(date);
			}
		});

		event.preventDefault();
	});
}

function convertDuration(duration) {
	var duration = Math.floor(duration / 1000);
	var nbOfHours = Math.floor(duration / (60 * 60));
	var nbOfMinutes = Math.floor((duration - (nbOfHours * 3600)) / 60);
	var nbOfSeconds = duration - (nbOfHours * 3600) - (nbOfMinutes * 60);

	var label = "";
	if (nbOfHours > 0)
		label = nbOfHours + "h ";
	if (nbOfMinutes > 0)
		label += nbOfMinutes + "m ";
	label += nbOfSeconds + "s ";

	return label;
}