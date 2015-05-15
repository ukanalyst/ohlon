window.onload = function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

	// dataPoints
	var dataPoints1 = [];

	var chart = new CanvasJS.Chart("chartContainer", {
		zoomEnabled : true,
		exportEnabled : true,
		toolTip : {
			shared : true
		},
		axisY : {
			includeZero : true,
			labelFontSize : 9,
			suffix : "%",
			maximum : 100
		},
		data : [ {
			// dataSeries1
			type : "line",
			xValueType : "dateTime",
			name : "CPU Usage",
			dataPoints : dataPoints1
		} ],
		legend : {
			cursor : "pointer",
			itemclick : function(e) {
				if (typeof (e.dataSeries.visible) === "undefined" || e.dataSeries.visible) {
					e.dataSeries.visible = false;
				} else {
					e.dataSeries.visible = true;
				}
				chart.render();
			}
		}
	});

	var updateInterval = 1000;
	var NB_OF_VALUES = 300;

	var time = new Date;

	var updateChart = function() {

		$.ajax({
			url : JOLOKIA_URL + "/read/java.lang:type=OperatingSystem/ProcessCpuLoad",
			dataType : "json",
			headers : headers,
			success : function(data) {
				if (dataPoints1.length > NB_OF_VALUES)
					dataPoints1.shift();

				dataPoints1.push({
					x : new Date(),
					y : Math.round(data.value * 100)
				});
				chart.render();
			}
		});
	};

	// update chart after specified interval
	setInterval(function() {
		updateChart()
	}, updateInterval);
}