window.onload = function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};
	
	if (from.length == 0)
		from = "na"
	if (to.length == 0)
		to = "na"
	if (user.length == 0)
		user = "na"

	if (bc && bc.length > 0)
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getManualStepsAccumulationDetails(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)/' + module + '/' + bc + '/' + from + '/' + to + '/' + user,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);

				// dataPoints
				var dataPoints = [];

				for (var i = 0; i < d.length; i++) {
					dataPoints.push({
						label : d[i].label,
						y : d[i].percentage
					});
				}

				var chart = new CanvasJS.Chart("chartContainer", {
					toolTip : {
						shared : true
					},
					exportEnabled : true,
					animationEnabled : true,
					axisY : {
						suffix : "%",
						maximum : 100
					},
					legend : {
						verticalAlign : "bottom",
						horizontalAlign : "center"
					},
					data : [ {
						color : "#B0D0B0",
						type : "column",
						name : "Percentage",
						dataPoints : dataPoints
					} ]
				});

				chart.render();
			}
		});
};