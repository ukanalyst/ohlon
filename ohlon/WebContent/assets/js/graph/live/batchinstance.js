var updateInterval = 1000;
var DIFF = 1 * (60 * 60 * 1000);

var refreshFunctions = new Object();

var searchNewBatchInstances = function() {
	$.ajax({
		url : JOLOKIA_URL + '/read/ephesoft:type=batchinstance-stats/ActiveBatchInstancesDetails',
		dataType : 'json',
		success : function(response) {
			var data = eval(response.value);
			if (data)
				for (var i = 0; i < data.length; i++) {
					var identifier = data[i];

					var biElement = $("#" + identifier);
					if (biElement.length == 0) {
						createBatchInstance(identifier);
					}
				}
		}
	});
};

function createBatchInstance(identifier) {
	$.ajax({
		url : JOLOKIA_URL + '/exec/ephesoft:type=batchinstance-stats/getActiveBatchInstancesDetails(java.lang.String)/' + identifier,
		dataType : 'json',
		success : function(response) {
			var bi = JSON.parse(response.value);
			var html = generateHtml(bi);
			$("#container").prepend(html);
			var refresh = setInterval(function() {
				updateBatchInstance(identifier)
			}, updateInterval);
			refreshFunctions[identifier] = refresh;
		}
	});
}

function updateBatchInstance(identifier) {
	$.ajax({
		url : JOLOKIA_URL + '/exec/ephesoft:type=batchinstance-stats/getActiveBatchInstancesDetails(java.lang.String)/' + identifier,
		dataType : 'json',
		success : function(response) {
			var bi = JSON.parse(response.value);
			var biElement = $("#" + identifier);

			if (bi.status == "FINISHED" || bi.status == "DELETED") {
				clearInterval(refreshFunctions[bi.ID]);
				setTimeout(function() {
					$(biElement[0]).fadeOut()
				}, FINISHED_BI_HIDE_DELAY);
			}

			var html = generateInnerHtml(bi);
			$(biElement[0]).html(html);
		}
	});
}

function generateHtml(bi) {

	// We create a new batch instance
	var html = "<div id='" + bi.ID + "' class='batchinstance'>";
	html += generateInnerHtml(bi);
	html += "</div>";

	return html;
}

function generateInnerHtml(bi) {

	var html = "";

	// Create the timeline
	if (bi.wf_modules && bi.wf_modules.length > 0) {
		var now = new Date();
		now.setTime(now.getTime() + DIFF);
		var creationDate = new Date(bi.creationDate);
		var current_duration = (now.getTime()) - creationDate.getTime();
		current_duration = Math.floor(current_duration / 1000);

		html += "<div class='timeline'>";

		// Define the waiting module at the beginning
		var nextModuleStart = new Date(bi.wf_modules[0].wf_module_start);
		var duration = Math.floor((nextModuleStart.getTime() - creationDate.getTime()) / 1000);
		var width = 100 * duration / current_duration;
		if (width > 0)
			html += "<div class='timeslot waiting' style='width:" + width + "%;'>&nbsp;</div>";

		for (var i = 0; i < bi.wf_modules.length; i++) {
			var module = bi.wf_modules[i];
			var completed = true;

			var duration = 0;
			if (module.wf_module_duration && module.wf_module_duration > 0) {
				duration = module.wf_module_duration / 1000;
			} else {
				nextSlotStart = now;
				if (module.wf_module_end) {
					nextSlotStart = new Date(module.wf_module_end);
				} else {
					completed = false;
				}
				duration = Math.floor((nextSlotStart.getTime() - (new Date(module.wf_module_start)).getTime()) / 1000);
			}
			var width = 100 * duration / current_duration;
			if (width > 0)
				html += "<div class='timeslot' style='width:" + width + "%;'>&nbsp;</div>";

			// Add the waiting piece after
			if (completed) {

				var duration = 0;
				if (i + 1 < bi.wf_modules.length)
					duration = Math.floor(((new Date(bi.wf_modules[i + 1].wf_module_start)).getTime() - (new Date(module.wf_module_end)).getTime()) / 1000);
				else if (!(bi.status == "FINISHED" || bi.status == "DELETED"))
					duration = Math.floor((now.getTime() - (new Date(module.wf_module_end)).getTime()) / 1000);
				var width = 100 * duration / current_duration;
				if (width > 0)
					html += "<div class='timeslot waiting' style='width:" + width + "%;'>&nbsp;</div>";
			}
		}

		html += "</div>";
		html += "<div style='clear:both;'></div>";
	}

	html += "<div class='details'>";
	html += "	<div class='row'>";
	html += "		<div class='identifier'><a target='_parent' href='../batchinstance?identifier=" + bi.ID + "'>" + bi.ID + "</a>: </div>";
	html += "		<div class='status'>" + bi.status + "</div>";
	html += "		<div class='priority'>" + bi.priority + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	html += "	<div class='row'>";
	html += "		<div class='label'>BI Creation Date: </div>";
	html += "		<div class='value'>" + bi.creationDate + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	if (bi.wf_bi_start) {
		html += "	<div class='row'>";
		html += "		<div class='label'>Workflow Start Date: </div>";
		html += "		<div class='value'>" + bi.wf_bi_start + "</div>";
		html += "	</div>";
		html += "	<div style='clear:both;'></div>";
	}
	html += "	<div class='row'>";
	html += "		<div class='label'>Last Modified: </div>";
	html += "		<div class='value'>" + bi.modificationDate + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	if (bi.wf_bi_end) {
		html += "	<div class='row'>";
		html += "		<div class='label'>Workflow End Date: </div>";
		html += "		<div class='value'>" + bi.wf_bi_end + "</div>";
		html += "	</div>";
		html += "	<div style='clear:both;'></div>";
	}
	if (bi.wf_bi_duration) {
		html += "	<div class='row'>";
		html += "		<div class='label'>Workflow Duration: </div>";
		html += "		<div class='value'>" + convertDuration(bi.wf_bi_duration) + "</div>";
		html += "	</div>";
		html += "	<div style='clear:both;'></div>";
	}
	if (bi.user) {
		html += "	<div class='row'>";
		html += "		<div class='label'>Current User: </div>";
		html += "		<div class='value'>" + bi.user + "</div>";
		html += "	</div>";
		html += "	<div style='clear:both;'></div>";
	}

	if (bi.wf_modules && bi.wf_modules.length > 0) {
		html += "<div class='modules'>";
		html += "	<div class='row'>";
		html += "		<div class='label'>Modules: </div>";
		html += "	</div>";
		html += "	<div style='clear:both;'></div>";
		for (var i = 0; i < bi.wf_modules.length; i++) {
			var module = bi.wf_modules[i];
			var status = "RUNNING";
			var label = module.wf_module_label;
			label = label.replace(/-m|-p|BC\d+/g, "");
			if (module.wf_module_end) {
				status = convertDuration(module.wf_module_duration);
			}
			if (status == "RUNNING" || module.wf_module_duration > 1000) {
				html += "<div class='row module'>";
				html += "		<div class='label'>" + label + "</div>";
				html += "		<div class='value'>" + status + "</div>";
				html += "</div>";
			}
		}
		html += "</div>";
	}
	html += "</div>";

	return html;
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

// update chart after specified interval
setInterval(function() {
	searchNewBatchInstances()
}, updateInterval);