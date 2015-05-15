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

		var param = "";
		if (bc && bc.length > 0)
			param += "&bc=" + bc;
		if (from && from.length > 0)
			param += "&from=" + moment(from, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		if (to && to.length > 0)
			param += "&to=" + moment(to, [ "MM/DD/YYYY h:mm A" ]).format("YYYY-MM-DD HH:mm:ss");
		if (param.length > 0)
			param = '?' + param.substring(1);

		// Refresh graph
		$("#repartition-graph").attr("src", "graph/batchclass-repartition" + param);
		$("#accumulation-graph").attr("src", "graph/batchclass-accumulation" + param);
		$("#batchinstance").attr("src", "graph/batchclass-batchinstances" + param);

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
}