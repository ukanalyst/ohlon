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
	if (maxDuration.length == 0)
		maxDuration = "-1"
	if (graphInterval.length == 0)
		graphInterval = "60"

	if (bc && bc.length > 0)
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=batchinstance-stats/getBatchClassRepartition(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.Integer)/' + bc + '/' + from + '/' + to + '/' + maxDuration + '/' + graphInterval,
			dataType : "json",
			headers : headers,
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