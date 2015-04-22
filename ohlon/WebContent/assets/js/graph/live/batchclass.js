window.onload = function() {

	// dataPoints
	var dataPoints1 = [];

	var chart = new CanvasJS.Chart("chartContainer", {
		zoomEnabled : true,
		toolTip : {
			shared : true
		},
		axisX : {
			interval : 1,
			gridThickness : 0,
			labelFontSize : 9,
		},
		axisY2 : {
			gridColor : "rgba(1,77,101,.1)",
			minimum : 0,
			maximum : 5
		},
		data : [ {
			type : "bar",
			axisYType : "secondary",
			color : "#014D65",
			name : "Batch Instance #",
			dataPoints : dataPoints1
		} ]
	});

	var updateInterval = 1000;

	var updateChart = function() {

		$.ajax({
			url : JOLOKIA_URL + "/read/ephesoft:type=batchinstance-stats/BatchInstancesByBC",
			dataType : "json",
			success : function(data) {
				var d = eval(data.value);

				dataPoints1.length = 0;

				for (var i = 0; i < d.length; i++) {
					dataPoints1.push({
						label : d[i].batchClassId,
						y : d[i].size
					});
				}

				chart.render();
			}
		});
	};

	// update chart after specified interval
	setInterval(function() {
		updateChart()
	}, updateInterval);
}