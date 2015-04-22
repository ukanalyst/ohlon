window.onload = function() {
	$('.input-group.date').datetimepicker();

	$("#refreshReports").click(function(event) {
		var bc = $("#batchclass option:selected").attr("id");
		var from = $("#from").val();
		var to = $("#to").val();

		if (from && from.length > 0)
			from = moment(from, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		else
			from = "na";
		if (to && to.length > 0)
			to = moment(to, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		else
			to = "na";

		$("#workflow td[data-attr]").html("");
		$(".metric-main").html("");
		$("div.data").remove();

		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getBatchClassExecutionDetails(java.lang.String,java.lang.String,java.lang.String)/' + bc + '/' + from + '/' + to,
			dataType : "json",
			success : function(data) {
				var d = eval(data.value);

				for (var i = 0; i < d.length; i++) {
					if (d[i].WORKFLOW_TYPE == "WORKFLOW") {

						$("#workflow td[data-attr='MINDURATION']").html(convertDuration(d[i].MINDURATION));
						$("#workflow td[data-attr='AVGDURATION']").html(convertDuration(d[i].AVGDURATION));
						$("#workflow td[data-attr='MAXDURATION']").html(convertDuration(d[i].MAXDURATION));

						$("#workflow td[data-attr='AVGDOCPS']").html(Number(d[i].AVGDOCPS * 60 * 60).toFixed(1));
						$("#workflow td[data-attr='MINDOCPS']").html(Number(d[i].MINDOCPS * 60 * 60).toFixed(1));
						$("#workflow td[data-attr='MAXDOCPS']").html(Number(d[i].MAXDOCPS * 60 * 60).toFixed(1));

						$("#workflow td[data-attr='AVGPAGEPS']").html(Number(d[i].AVGDOCPS * 60).toFixed(1));
						$("#workflow td[data-attr='MINPAGEPS']").html(Number(d[i].MINDOCPS * 60).toFixed(1));
						$("#workflow td[data-attr='MAXPAGEPS']").html(Number(d[i].MAXDOCPS * 60).toFixed(1));
						
						// Populate main metrics
						$("#nbOfBatchIntances").html(d[i].NBOFBATCHINSTANCES);
						$("#nbOfDocuments").html(d[i].NBOFDOCUMENTS);
						$("#nbOfPages").html(d[i].NBOFPAGES);
						$("#averageProcessingTime").html(convertDuration(d[i].AVGDURATION));
					} else if (d[i].WORKFLOW_TYPE == "MODULE") {
						var _module = $(".module-template").clone();
						_module.css("display", "");
						_module.removeClass("module-template");
						_module.addClass("data");

						$(_module).find("button.display-graph").attr("atype", d[i].WORKFLOW_TYPE);
						$(_module).find("button.display-graph").attr("aname", d[i].WORKFLOW_NAME);
						$(_module).find("button.display-graph").click(displayGraph);

						$(_module).find("th[data-attr='WORKFLOW_NAME']").html(d[i].WORKFLOW_NAME.replace(/_/g, " "));

						$(_module).find("td[data-attr='MINDURATION']").html(convertDuration(d[i].MINDURATION));
						$(_module).find("td[data-attr='AVGDURATION']").html(convertDuration(d[i].AVGDURATION));
						$(_module).find("td[data-attr='MAXDURATION']").html(convertDuration(d[i].MAXDURATION));

						$(_module).find("td[data-attr='AVGDOCPS']").html(Number(d[i].AVGDOCPS * 60 * 60).toFixed(1));
						$(_module).find("td[data-attr='MINDOCPS']").html(Number(d[i].MINDOCPS * 60 * 60).toFixed(1));
						$(_module).find("td[data-attr='MAXDOCPS']").html(Number(d[i].MAXDOCPS * 60 * 60).toFixed(1));

						$(_module).find("td[data-attr='AVGPAGEPS']").html(Number(d[i].AVGDOCPS * 60).toFixed(1));
						$(_module).find("td[data-attr='MINPAGEPS']").html(Number(d[i].MINDOCPS * 60).toFixed(1));
						$(_module).find("td[data-attr='MAXPAGEPS']").html(Number(d[i].MAXDOCPS * 60).toFixed(1));

						$("#modules").append(_module);
					} else if (d[i].WORKFLOW_TYPE == "PLUGIN") {
						var _plugin = $(".plugin-template").clone();
						_plugin.css("display", "");
						_plugin.removeClass("plugin-template");
						_plugin.addClass("data");

						$(_plugin).find("button.display-graph").attr("atype", d[i].WORKFLOW_TYPE);
						$(_plugin).find("button.display-graph").attr("aname", d[i].WORKFLOW_NAME);
						$(_plugin).find("button.display-graph").click(displayGraph);

						$(_plugin).find("th[data-attr='WORKFLOW_NAME']").html(d[i].WORKFLOW_NAME.replace(/_/g, " "));

						$(_plugin).find("td[data-attr='MINDURATION']").html(convertDuration(d[i].MINDURATION));
						$(_plugin).find("td[data-attr='AVGDURATION']").html(convertDuration(d[i].AVGDURATION));
						$(_plugin).find("td[data-attr='MAXDURATION']").html(convertDuration(d[i].MAXDURATION));

						$(_plugin).find("td[data-attr='AVGDOCPS']").html(Number(d[i].AVGDOCPS * 60 * 60).toFixed(1));
						$(_plugin).find("td[data-attr='MINDOCPS']").html(Number(d[i].MINDOCPS * 60 * 60).toFixed(1));
						$(_plugin).find("td[data-attr='MAXDOCPS']").html(Number(d[i].MAXDOCPS * 60 * 60).toFixed(1));

						$(_plugin).find("td[data-attr='AVGPAGEPS']").html(Number(d[i].AVGDOCPS * 60).toFixed(1));
						$(_plugin).find("td[data-attr='MINPAGEPS']").html(Number(d[i].MINDOCPS * 60).toFixed(1));
						$(_plugin).find("td[data-attr='MAXPAGEPS']").html(Number(d[i].MAXDOCPS * 60).toFixed(1));

						$("#plugins").append(_plugin);
					}
				}

			}
		});

		event.preventDefault();
	});

	$.ajax({
		url : JOLOKIA_URL + "/read/ephesoft:type=batchinstance-stats/BatchClass",
		dataType : "json",
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

function displayGraph() {

	var isHidden = ($(this).closest('div').find("iframe").css("display") == "none");

	if (isHidden) {

		var bc = $("#batchclass option:selected").attr("id");
		var type = $(this).attr("atype");
		var name = $(this).attr("aname");
		var from = $("#from").val();
		var to = $("#to").val();

		var param = "";
		if (bc && bc.length > 0)
			param += "&bc=" + bc;
		if (type && type.length > 0)
			param += "&type=" + type;
		if (name && name.length > 0)
			param += "&name=" + name;
		if (from && from.length > 0)
			param += "&from=" + moment(from, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		if (to && to.length > 0)
			param += "&to=" + moment(to, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		if (param.length > 0)
			param = '?' + param.substring(1);

		$(this).closest('div').find("iframe.repartition").attr("src", "graph/reporting/artifact-repartition" + param);
		$(this).closest('div').find("iframe.accumulation").attr("src", "graph/reporting/artifact-accumulation" + param);
		$(this).closest('div').find("iframe").css("display", "");
		$(this).html("Hide Graph");
	} else {
		$(this).html("Show Graph");
		$(this).closest('div').find("iframe").css("display", "none");
	}
};