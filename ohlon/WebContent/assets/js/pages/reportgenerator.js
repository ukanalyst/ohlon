window.onload = function() {
	$('.input-group.date').datetimepicker();

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

	$("#refreshReports").click(function(event) {
		var bc = $("#batchclass option:selected").attr("id");
		var from = $("#from").val();
		var to = $("#to").val();
		var reportPath = $("#report").val();

		var param = "";
		if (bc && bc.length > 0)
			param += "&batchClass=" + bc;
		if (from && from.length > 0)
			param += "&start=" + moment(from, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		if (to && to.length > 0)
			param += "&end=" + moment(to, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");

		// Refresh graph
		$("#birt").attr("src", "frameset?__report=" + reportPath + "&serverId=" + currentServerId + param);

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

	// Load the list of reports
	var reports = reports_def["reports"];
	var html = "";
	for (var i = 0; i < reports.length; i++) {
		html += "<option value='" + reports[i].path + "'>" + reports[i].label + "</option>";
	}
	$("#report").html(html);
}