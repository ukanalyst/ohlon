var headers = {};
if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
	headers = {
		'Authorization' : "Basic " + JOLOKIA_AUTH
	};

window.onload = function() {

	// Check the version of the ephesoft server
	$.ajax({
		url : JOLOKIA_URL + "/exec/ephesoft:type=application-details/getApplicationDetails/",
		dataType : "json",
		headers : headers,
		success : function(data) {
			if (data.value) {
				var d = eval("(" + data.value + ")");
				if (d) {
					var majorVersion = parseInt(d["ephesoft.version"].split("\\.")[0]);
					if (majorVersion < 4) {
						$(".container-fluid").html("<div class='error-notice'><div class='oaerror danger'><strong>Error</strong> - This page is not available with the current server.</div></div>");
					}
				}
			}
		}
	});

	$('#month').datetimepicker({
		format : 'MM-YYYY'
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

	$('#displayStats').click(function() {
		$("#weeksContainer").html("");
		var month = $("#month input").val();

		if (month != null && month.length > 0) {
			month = moment(month, [ "MM-YYYY" ]);
			var currentMonth = month.get('month');
			var start = month.clone().startOf('month').startOf('week');
			var end = start.clone().endOf('week');

			while (start.get('month') == currentMonth || end.get('month') == currentMonth) {
				displayWeek(start, end, currentMonth);
				start.add(7, 'days');
				end.add(7, 'days');
			}
		}

		// Send a query to ephesoft
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=server-status/getMonthlyStatistics(java.lang.String)/' + $("#month input").val(),
			dataType : "json",
			headers : headers,
			success : function(data) {
				if (data.status == 403) {
					$(".container-fluid").html("<div class='error-notice'><div class='oaerror danger'><strong>Error</strong> - This page is not available with the current server.</div></div>");
				} else {
					var d = eval(data.value);

					for (var i = 0; i < d.length; i++) {
						var day = d[i];
						var obj = $("div.day[day='" + day.date + "']");
						if (obj.length > 0) {
							var percentage = day.availability * 100;
							$(obj).find(".percentage").html(percentage.toFixed(1) + "%");
							$(obj).find(".time").html(convertDuration(day.time));
							// Change the color
							var colorValue = Math.round(120 * day.availability);
							$(obj).find(".metric").css("background", "hsl(" + colorValue + ",50%,45%)");
							$(obj).attr("populated", true);
							$(obj).attr("uptime", day.time);
							$(obj).attr("availability", day.availability);
						}
					}

					// Update the percentage for each week
					var weeks = $(".week");
					for (var i = 0; i < d.length; i++) {
						var week = weeks[i];
						var populatedDays = $(week).find(".day[populated=true]");
						if (populatedDays.length == 0)
							$(week).find(".weekResume").addClass("disabled");
						else {
							var sumPercentage = 0;
							for (var j = 0; j < populatedDays.length; j++)
								sumPercentage += parseFloat($(populatedDays[j]).attr("availability"));

							// Update the percentage
							var percentage = (sumPercentage / populatedDays.length) * 100;
							$(week).find(".weekResume").find(".percentage").html(percentage.toFixed(1) + "%");

							// Change the color
							var colorValue = Math.round(120 * sumPercentage / populatedDays.length);
							$(week).find(".weekResume").css("background", "hsl(" + colorValue + ",50%,45%)");
							$(week).find(".weekResume .percentage").css("color", "hsl(" + colorValue + ",50%,45%)");
						}
					}

					// Attach the "View details" action
					$(".showDetails").click(function() {
						var day = $(this).closest(".day");
						var dayId = $(day).attr("day");
						dayId = moment(dayId).format("DD-MM-YYYY");

						showDetails(dayId);
					});

					// Compute the global statistic
					var days = $(".day[populated=true]");
					if (days.length > 0) {
						var sumPercentage = 0;
						var sumUpTime = 0;
						for (var i = 0; i < days.length; i++) {
							sumPercentage += parseFloat($(days[i]).attr("availability"));
							sumUpTime += parseFloat($(days[i]).attr("uptime"));
						}

						// Update the global percentage
						var percentage = (sumPercentage / days.length) * 100;
						$(".global-statistic .percentage").html(percentage.toFixed(1) + "%");

						// Update the global up time
						var nbOfDays = Math.floor(sumUpTime / 86400);
						var label = "";
						if (nbOfDays > 0)
							label = nbOfDays + "d ";
						label += convertDuration(sumUpTime - 86400 * nbOfDays);
						$(".global-statistic .time").html(label);

						// Change the color
						var colorValue = Math.round(120 * sumPercentage / days.length);
						$(".global-statistic .metric").css("background", "hsl(" + colorValue + ",50%,45%)");
					} else {
						$(".global-statistic .metric").css("background", "");
						$(".global-statistic .percentage").html("");
						$(".global-statistic .time").html("");
					}
				}
			}
		});
	});

	function displayWeek(start, end, currentMonth) {
		var currentWeek = start.week();

		var html = "<div class='week week-" + currentWeek + "'></div>";
		$("#weeksContainer").append(html);
		$(".week-" + currentWeek).append("<div class='label'>Week " + currentWeek + "</div><div class='daysContainer'></div>");

		// Display days
		var current = start.clone();
		while (current <= end) {

			var className = 'day';
			if (currentMonth != current.get('month'))
				className += ' disabled';
			else if (current > moment())
				className += ' disabled';

			html = "<div class='" + className + "' day='" + current.format("YYYY-MM-DD") + "'><div class='label'>" + current.format("ddd, M/D") + "<div class='showDetails action' aria-label='Show Details'><span class='glyphicon glyphicon-eye-open' aria-hidden='true'></span></div></div><div class='metric-container'><div class='metric'><div class='percentage'></div><div class='time'></div></div></div></div>";
			$(".week-" + currentWeek + " .daysContainer").append(html);
			current.add(24, 'hours');
		}

		// Add the week consolidation
		var className = 'day weekResume';
		var days = $(".week-" + currentWeek + " .day");
		var shouldBeDisabled = days.length > 0 && $(days[0]).hasClass("disabled") && $(days[days.length - 1]).hasClass("disabled")
		if (shouldBeDisabled)
			className += ' disabled';

		html = "<div class='" + className + "'><div class='label'>Week</div><div class='metric week'><div class='percentage'></div></div></div>";
		$(".week-" + currentWeek + " .daysContainer").append(html);

	}

	function convertDuration(duration) {
		var nbOfHours = Math.floor(duration / (60 * 60));
		var nbOfMinutes = Math.floor((duration - (nbOfHours * 3600)) / 60);
		var nbOfSeconds = duration - (nbOfHours * 3600) - (nbOfMinutes * 60);

		var label = "";
		if (nbOfHours > 0)
			label = nbOfHours + "h ";
		if (nbOfMinutes > 0)
			label += nbOfMinutes + "m ";
		if (nbOfSeconds > 0)
			label += nbOfSeconds + "s";

		return label;
	}

	function showDetails(dayId) {
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=server-status/getDailyStatistics(java.lang.String)/' + dayId,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);

				var html = "<div class='dayStatistic'>";

				// Add the header
				html += "<div class='row header'>";
				html += "<div class='cell hour'></div>";
				html += "<div class='cell'>AM</div>";
				html += "<div class='cell hour'></div>";
				html += "<div class='cell'>PM</div>";
				html += "</div>";

				for (var i = 0; i < 12; i++) {
					var hour = i;
					if (hour == 0)
						hour = 12;
					html += "<div class='row hour'>";
					html += "<div class='cell hour'>" + hour + "</div>";
					html += "<div class='cell progressbar'>" + generateProgressBar(d[i].slots) + "</div>";
					html += "<div class='cell hour'>" + hour + "</div>";
					html += "<div class='cell progressbar'>" + generateProgressBar(d[12 + i].slots) + "</div>";
					html += "</div>";
				}

				html += "</div>";

				// Add legend
				html += "<div class='legend'>";
				html += "<div class='item'><div class='color active'></div><div class='title'>Server Active</div></div>";
				html += "<div class='item'><div class='color inactive'></div><div class='title'>Server Inactive</div></div>";
				html += "</div>";

				BootstrapDialog.show({
					title : 'Day statistic: ' + dayId,
					message : html
				});

			}
		});
	}

	function generateProgressBar(slots) {
		var html = "<div class='progress'>";

		if (slots != null) {
			var total = 0;
			for (var i = 0; i < slots.length; i++)
				if (slots[i] != null)
					total += slots[i].nbOfSeconds;

			var totalPercentage = 0;
			var className;

			for (var i = 0; i < slots.length; i++) {
				var slot = slots[i];
				if (slot != null) {
					className = "progress-bar";
					if (!slot.isActive)
						className += " inactive";
					var percentage = (100.0 * slot.nbOfSeconds / total).toFixed(2);
					if (totalPercentage + parseFloat(percentage) > 100)
						percentage = (100 - totalPercentage).toFixed(2);
					totalPercentage += parseFloat(percentage);
					html += "<div class='" + className + "' role='progressbar' aria-valuemin='0' aria-valuemax='100' style='width: " + percentage + "%;'></div>";
				}
			}

			// Add the last piece of percentage (if required)
			html += "<div class='" + className + "' role='progressbar' aria-valuemin='0' aria-valuemax='100' style='width: " + (100 - totalPercentage) + "%;'></div>";

		}

		html += "</div>";
		return html;
	}

};
