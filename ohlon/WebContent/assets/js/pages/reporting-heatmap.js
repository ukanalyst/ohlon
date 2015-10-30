window.onload = function() {
	$('.input-group.date').datetimepicker();
	var SVG_INITIALIZED = false;

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

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

		// Get the batch class structure
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=batchinstance-stats/getBatchClassStructure(java.lang.String)/' + bc,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var structure = eval(data.value);

				// Get the reporting values
				$.ajax({
					url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getBatchClassExecutionDetails(java.lang.String,java.lang.String,java.lang.String)/' + bc + '/' + from + '/' + to,
					dataType : "json",
					headers : headers,
					success : function(data) {
						var d = eval(data.value);

						var s = Snap("#heatmap");
						if (SVG_INITIALIZED) {
							svgPanZoom("#heatmap").destroy();
							s.clear();
						}

						var MODULE_WIDTH = 250;
						var MODULE_X = 50;
						var MODULE_HEIGHT = 100;
						var MODULE_SEP = 20;
						var PLUGIN_WIDTH = 210;
						var PLUGIN_HEIGHT = 75;
						var PLUGIN_SEP = 10;

						var currentYModule = MODULE_SEP;

						// Define the arrow end
						s.path("M 0.0,0.0 L 5.0,-5.0 L -12.5,0.0 L 5.0,5.0 L 0.0,0.0 z").attr({
							style : 'fill-rule:evenodd;stroke:#000000;stroke-width:1.0pt;',
							transform : 'scale(0.8) rotate(180) translate(12.5,0)'
						}).marker(0, 0, 5, 5, 0, 0).attr({
							"id" : "ArrowEnd",
							overflow : "visible"
						});

						// Create the list of modules
						var listOfModules = [];
						for (var i = 0; i < structure.length; i++)
							listOfModules.push(structure[i].name);

						var scoreModules = computeScore(listOfModules, d);

						// Browse the structure
						for (var i = 0; i < structure.length; i++) {
							var module = structure[i];
							var moduleStats = getReportingData(d, module.name);

							s.rect(MODULE_X, currentYModule, MODULE_WIDTH, MODULE_HEIGHT, 10, 10).attr({
								strokeWidth : 1,
								style : "fill:" + getColor(scoreModules[module.name.toLowerCase()].score) + ";stroke:" + getColor(scoreModules[module.name.toLowerCase()].score),
								fillOpacity : 0.2,
								strokeOpacity : 1

							});
							// Module name
							s.text(MODULE_X + MODULE_WIDTH / 2, currentYModule + 35, module.name.replace(/_/g, " ")).attr({
								textAnchor : "middle"
							}).addClass("module-name");
							// Separator
							s.polyline(MODULE_X, currentYModule + 50, MODULE_X + MODULE_WIDTH, currentYModule + 50).attr({
								stroke : "#adadad",
								strokeWidth : 1
							});
							// Draw line from the previous module
							if (currentYModule > MODULE_HEIGHT) {

								var _height = MODULE_HEIGHT + MODULE_SEP;
								var _start = currentYModule + (MODULE_HEIGHT / 2) - _height;
								var _end = _start + _height;
								var _middle = _start + (_height / 2)

								var path = "";
								path += "M" + MODULE_X + "," + _start;
								path += " C0," + _middle;
								path += " 0," + _middle + " ";
								path += MODULE_X + "," + _end;

								s.path(path).attr({
									fill : "none",
									stroke : "black",
									strokeWidth : 4,
									style : "marker-end:url(#ArrowEnd)"
								});
							}
							var _box_width = MODULE_WIDTH / 3.0;

							if (moduleStats != null) {
								// Display the min
								s.text(MODULE_X + _box_width / 2, currentYModule + 70, "(MIN)").attr({
									textAnchor : "middle"
								}).addClass("module-property-label");
								s.text(MODULE_X + _box_width / 2, currentYModule + 90, convertDuration(moduleStats.MINDURATION)).attr({
									textAnchor : "middle"
								}).addClass("module-property-value");
								s.polyline(MODULE_X + _box_width, currentYModule + 50, MODULE_X + _box_width, currentYModule + 100).attr({
									stroke : "#adadad",
									strokeWidth : 1
								});

								// Display the average
								s.text(_box_width + MODULE_X + _box_width / 2, currentYModule + 70, "(AVG)").attr({
									textAnchor : "middle"
								}).addClass("module-property-label");
								s.text(_box_width + MODULE_X + _box_width / 2, currentYModule + 90, convertDuration(moduleStats.AVGDURATION)).attr({
									textAnchor : "middle"
								}).addClass("module-property-value");
								s.polyline(MODULE_X + 2 * _box_width, currentYModule + 50, MODULE_X + 2 * _box_width, currentYModule + 100).attr({
									stroke : "#adadad",
									strokeWidth : 1
								});

								// Display the max
								s.text(2 * _box_width + MODULE_X + _box_width / 2, currentYModule + 70, "(MAX)").attr({
									textAnchor : "middle"
								}).addClass("module-property-label");
								s.text(2 * _box_width + MODULE_X + _box_width / 2, currentYModule + 90, convertDuration(moduleStats.MAXDURATION)).attr({
									textAnchor : "middle"
								}).addClass("module-property-value");
							}

							// Display the plugins
							var plugins = module.plugins;

							var currentXPlugin = MODULE_X + MODULE_WIDTH + PLUGIN_SEP;
							var currentYPlugin = currentYModule + ((MODULE_HEIGHT - PLUGIN_HEIGHT) / 2);
							var scorePlugins = computeScore(plugins, d);

							for (var j = 0; j < plugins.length; j++) {
								var pluginName = plugins[j];
								var pluginStats = getReportingData(d, pluginName);

								s.rect(currentXPlugin, currentYPlugin, PLUGIN_WIDTH, PLUGIN_HEIGHT, 10, 10).attr({
									strokeWidth : 1,
									style : "fill:" + getColor(scorePlugins[pluginName.toLowerCase()].score) + ";stroke:" + getColor(scorePlugins[pluginName.toLowerCase()].score),
									fillOpacity : 0.2,
									strokeOpacity : 1
								});

								// Plugin name
								s.text(currentXPlugin + PLUGIN_WIDTH / 2, currentYPlugin + 25, pluginName.replace(/_/g, " ")).attr({
									textAnchor : "middle"
								}).addClass("plugin-name");
								// Separator
								s.polyline(currentXPlugin, currentYPlugin + 35, currentXPlugin + PLUGIN_WIDTH, currentYPlugin + 35).attr({
									stroke : "#adadad",
									strokeWidth : 1
								});

								var _box_width = PLUGIN_WIDTH / 3.0;

								if (pluginStats != null) {
									// Display the min
									s.text(currentXPlugin + _box_width / 2, currentYPlugin + 55, "(MIN)").attr({
										textAnchor : "middle"
									}).addClass("plugin-property-label");
									s.text(currentXPlugin + _box_width / 2, currentYPlugin + 65, convertDuration(pluginStats.MINDURATION)).attr({
										textAnchor : "middle"
									}).addClass("plugin-property-value");
									s.polyline(currentXPlugin + _box_width, currentYPlugin + 35, currentXPlugin + _box_width, currentYPlugin + 75).attr({
										stroke : "#adadad",
										strokeWidth : 1
									});

									// Display the average
									s.text(_box_width + currentXPlugin + _box_width / 2, currentYPlugin + 55, "(AVG)").attr({
										textAnchor : "middle"
									}).addClass("plugin-property-label");
									s.text(_box_width + currentXPlugin + _box_width / 2, currentYPlugin + 65, convertDuration(pluginStats.AVGDURATION)).attr({
										textAnchor : "middle"
									}).addClass("plugin-property-value");
									s.polyline(currentXPlugin + 2 * _box_width, currentYPlugin + 35, currentXPlugin + 2 * _box_width, currentYPlugin + 75).attr({
										stroke : "#adadad",
										strokeWidth : 1
									});

									// Display the max
									s.text(2 * _box_width + currentXPlugin + _box_width / 2, currentYPlugin + 55, "(MAX)").attr({
										textAnchor : "middle"
									}).addClass("plugin-property-label");
									s.text(2 * _box_width + currentXPlugin + _box_width / 2, currentYPlugin + 65, convertDuration(pluginStats.MAXDURATION)).attr({
										textAnchor : "middle"
									}).addClass("plugin-property-value");
								}

								currentXPlugin += PLUGIN_SEP + PLUGIN_WIDTH;
							}

							currentYModule += MODULE_SEP + MODULE_HEIGHT;

						}

						// Refresh the map height
						$("#heatmap").height(currentYModule + "px");
						svgPanZoom('#heatmap', {
							zoomEnabled : true,
							controlIconsEnabled : true
						});
						SVG_INITIALIZED = true;
					}
				});

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

function getReportingData(data, name) {
	for (var i = 0; i < data.length; i++) {
		if (data[i].WORKFLOW_NAME.toLowerCase() == name.toLowerCase())
			return data[i];
	}
	return null;
}

function computeScore(list, data) {

	var scores = {};

	// Initialize variables
	var min = [ -1, -1 ];
	var avg = [ -1, -1 ];
	var max = [ -1, -1 ];

	// For each component
	for (var i = 0; i < list.length; i++) {

		// Get the value
		var _d = getReportingData(data, list[i]);
		if (_d != null) {
			if (scores[list[i].toLowerCase()] == null)
				scores[list[i].toLowerCase()] = {};

			if (min[0] == -1 || _d.MINDURATION < min[0])
				min[0] = _d.MINDURATION;
			if (min[1] == -1 || _d.MINDURATION > min[1])
				min[1] = _d.MINDURATION;

			if (avg[0] == -1 || _d.AVGDURATION < avg[0])
				avg[0] = _d.AVGDURATION;
			if (avg[1] == -1 || _d.AVGDURATION > avg[1])
				avg[1] = _d.AVGDURATION;

			if (max[0] == -1 || _d.MAXDURATION < max[0])
				max[0] = _d.MAXDURATION;
			if (max[1] == -1 || _d.MAXDURATION > max[1])
				max[1] = _d.MAXDURATION;

			// Save the value
			scores[list[i].toLowerCase()].name = list[i];
			scores[list[i].toLowerCase()].min = _d.MINDURATION;
			scores[list[i].toLowerCase()].avg = _d.AVGDURATION;
			scores[list[i].toLowerCase()].max = _d.MAXDURATION;

		} else
			console.log("Error retrieving stats for: " + list[i]);
	}

	// Then, we can compute the score
	for (var i = 0; i < list.length; i++) {
		var d = scores[list[i].toLowerCase()];

		if (d != null) {
			d.scoreMin = (d.min - min[0]) / (min[1] - min[0]);
			d.scoreAvg = (d.avg - avg[0]) / (avg[1] - avg[0]);
			d.scoreMax = (d.max - max[0]) / (max[1] - max[0]);

			d.score = (d.scoreMin + d.scoreAvg + d.scoreMax) / 3;
		} else {
			scores[list[i].toLowerCase()] = {
				score : 0
			};
		}
	}

	return scores;
}

function getColor(value) {
	// value from 0 to 1
	var hue = ((1 - value) * 120).toString(10);
	return [ "hsl(", hue, ",100%,50%)" ].join("");
}