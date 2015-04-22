window.onload = function() {

	if (from.length == 0)
		from = "na"
	if (to.length == 0)
		to = "na"
	if (user.length == 0)
		user = "na"

	if (bc && bc.length > 0)
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getManualStepsRepartitionDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/' + module + '/' + bc + '/' + from + '/' + to + '/' + user,
			dataType : "json",
			success : function(data) {
				var d = eval(data.value);

				// dataPoints
				var dataPoints = [];

				for (var i = 0; i < d.length; i++) {
					dataPoints.push({
						label : d[i].label,
						y : d[i].count
					});
				}

				var chart = new CanvasJS.Chart("chartContainer", {
					toolTip : {
						shared : true
					},
					animationEnabled : true,
					exportEnabled : true,
					axisY : {
						title : "# of BI"
					},
					legend : {
						verticalAlign : "bottom",
						horizontalAlign : "center"
					},
					data : [ {
						type : "column",
						name : "Count",
						dataPoints : dataPoints
					} ]
				});

				chart.render();
			}
		});
};