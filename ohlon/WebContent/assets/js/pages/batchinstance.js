var headers = {};
if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
	headers = {
		'Authorization' : "Basic " + JOLOKIA_AUTH
	};

window.onload = function() {

	$("#refreshFiles").click(function(event) {
		var bi = $("#batchidentifier").val();
		refreshFiles(bi);
	});

	$('input#batchidentifier').keyup(function(e) {
		if (e.keyCode == 13) {
			$("button#displayDetails").click();
		}
	});

	$("#showhideDetails").click(function() {
		$(".artifact-detail[duration=0]").toggleClass("hidden");
	});

	$("#displayDetails").click(function(event) {
		var bi = $("#batchidentifier").val();

		// Clean
		$(".metric-main").html("");
		$("#plugins .progress-bar").remove();
		$("#modules .progress-bar").remove();
		$("#modules-details div").remove();
		$("input[readonly]").val("");

		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=reporting-stats/getBatchInstanceExecutionDetails(java.lang.String)/' + bi,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);

				if (d.length > 0) {

					$("#progress-details").html('<div class="progress" id="modules"></div><div class="progress" id="plugins"></div><div id="modules-details"></div><div style="clear: both;"></div>');

					var start = null;
					var end = null;
					var total_time = 0;
					var execution_time = 0;
					var current_label_module = null;
					var current_time_module = null;
					var current_time_plugin = null;
					var min_start = null;
					var max_end = null;

					// Search the workflow first
					for (var i = 0; i < d.length; i++) {
						var c_start = moment(d[i].START_TIME).toDate();
						var c_end = moment(d[i].END_TIME).toDate();

						if (d[i].WORKFLOW_TYPE == "WORKFLOW") {

							$("#batchclass").val(d[i].BATCH_CLASS_ID);

							// Refresh metrics
							$("#nbOfDocuments").html(d[i].TOTAL_NUMBER_DOCUMENTS);
							$("#nbOfPages").html(d[i].TOTAL_NUMBER_PAGES);

							start = c_start;
							end = c_end;
						}

						// Search the min and max date
						if (i == 0) {
							min_start = c_start;
							min_end = c_end;
						} else {
							if (c_start < min_start)
								min_start = c_start;
							if (c_end > max_end)
								max_end = c_end;
						}
					}

					// Update main properties
					start = min_start;
					end = max_end;

					// Update duration properties
					total_time = max_end.getTime() - min_start.getTime();
					duration = total_time;
					$("#duration").val(convertDuration(duration));
					$("#processingTime").html(convertDuration(duration));
					$("#from").val(moment(min_start).format());
					$("#to").val(moment(max_end).format());

					// Then populate
					for (var i = 0; i < d.length; i++) {
						if (d[i].WORKFLOW_TYPE == "MODULE") {
							var startModule = moment(d[i].START_TIME).toDate();
							var endModule =moment(d[i].END_TIME).toDate();

							// Check if the time is correct
							if (startModule < start)
								duration = total_time;

							if (current_time_module == null)
								current_time_module = start;

							// Add the first waiting piece
							addTimeSlot($("#modules")[0], startModule.getTime() - current_time_module.getTime(), duration, "progress-bar-waiting");

							// Add the first waiting piece
							addTimeSlot($("#modules")[0], endModule.getTime() - startModule.getTime(), duration, "progress-bar-success", d[i].WORKFLOW_NAME);

							// Add the artifact detail
							addArtifactDetails($("#modules-details")[0], d[i].WORKFLOW_NAME, startModule, endModule, "ephesoft-module");

							current_time_module = endModule;
							current_label_module = d[i].WORKFLOW_NAME;

						} else if (d[i].WORKFLOW_TYPE == "PLUGIN") {

							var startPlugin = moment(d[i].START_TIME).toDate();
							var endPlugin = moment(d[i].END_TIME).toDate();

							if (current_time_plugin == null)
								current_time_plugin = start;

							// Add the first waiting piece
							addTimeSlot($("#plugins")[0], startPlugin.getTime() - current_time_plugin.getTime(), duration, "progress-bar-waiting");

							// Add the first execution details
							addTimeSlot($("#plugins")[0], endPlugin.getTime() - startPlugin.getTime(), duration, "progress-bar-success", d[i].WORKFLOW_NAME);

							// Add the artifact detail
							var potentialModules = $("div[label='" + current_label_module + "'] > a");
							if (potentialModules.length == 1)
								addArtifactDetails(potentialModules[0], d[i].WORKFLOW_NAME, startPlugin, endPlugin, "ephesoft-plugin");
							else {
								var index = -1;
								for (var j = 0; j < potentialModules.length; j++) {
									var _moduleStart = parseInt($(potentialModules[j]).attr("data-start"));
									var _moduleEnd = parseInt($(potentialModules[j]).attr("data-start"));
									if (startPlugin.getTime() >= _moduleStart && endPlugin.getTime() <= _moduleEnd)
										index = j;
								}

								if (index == -1)
									index = potentialModules.length - 1;
								addArtifactDetails(potentialModules[index], d[i].WORKFLOW_NAME, startPlugin, endPlugin, "ephesoft-plugin");
							}

							current_time_plugin = endPlugin;

							// Add execution time
							execution_time += endPlugin.getTime() - startPlugin.getTime();

						}
					}

					// Add the last waiting piece for plugins
					addTimeSlot($("#plugins")[0], end - current_time_plugin, duration, "progress-bar-waiting");

					// Bind mousenter and mouseleave events
					$("div.artifact-detail").mouseenter(function() {
						var artifactId = $(this).attr("label");
						$("div.progress-bar[artifactname='" + artifactId + "']").addClass("highlight");
					}).mouseleave(function() {
						var artifactId = $(this).attr("label");
						$("div.progress-bar[artifactname='" + artifactId + "']").removeClass("highlight");
					});

					// Update the execution time percentage
					var percentage = 100 * execution_time / duration;
					$("#executionPercentage").html(Math.round(percentage) + " %");
				} else {
					$("#progress-details").html('<div class="error-notice"><div class="oaerror info"><strong>INFO</strong> - The batch instance ' + bi + ' doesn\'t exist or is not available yet in the reporting database. Be sure that this identifier is a real one, or re-synchronize the reporting database</div></div>');
				}

			}
		});
		refreshFiles(bi);

	});

	// Check if we have a parameter
	if (identifier != null && identifier.length > 0) {
		$("#batchidentifier").val(identifier);
		$("#displayDetails").click();
	}
};

function addTimeSlot(container, length, biDuration, className, artifactName) {
	// Check the artifact name
	if (artifactName == null)
		artifactName = "";

	// Compute the class name
	var nbOfExistingSlots = $(container).find("div." + className).length;
	if (nbOfExistingSlots % 2 == 0)
		className += " odd";

	if (length > 0)
		$(container).append("<div artifactName=\"" + artifactName + "\" class=\"progress-bar " + className + "\" style=\"width: " + (length * 100.0 / biDuration) + "%\"></div>");
}

function addArtifactDetails(container, label, start, end, className) {
	var duration = end.getTime() - start.getTime();
	$(container).append("<div data-start=\"" + start.getTime() + "\" data-end=\"" + end.getTime() + "\" duration=\"" + duration + "\" label=\"" + label + "\" class=\"artifact-detail list-group " + className + "\"><a href=\"#\" class=\"list-group-item\"><h5 class=\"list-group-item-heading\">" + label.replace(/-m|-p|BC\d+/g, "").replace(/_/g, " ") + " (" + convertDuration(end.getTime() - start.getTime()) + ")</h5><p class=\"list-group-item-text\">" + moment(start).format("hh:mm:ss") + "</p><p class=\"list-group-item-text\">" + moment(end).format("hh:mm:ss") + "</p></a></div>");
}

function refreshFiles(bi) {
	$.ajax({
		url : JOLOKIA_URL + '/exec/ephesoft:type=system-folder/getBatchInstanceFiles(java.lang.String)/' + bi,
		dataType : "json",
		headers : headers,
		success : function(d) {
			var data = eval('(' + d.value + ')');
			$("#listoffiles").html("");
			$("#previewfile").html("");
			if (!data.success) {
				$("#listoffiles").append('<div class="error-notice"><div class="oaerror info"><strong>INFO</strong> - ' + data.message + '</div></div>');
			} else {
				var files = data.files;
				var html = '<div class="list-group">';
				for (var i = 0; i < files.length; i++) {
					var file = files[i];
					if (file.name.endsWith("batch.xml.zip") || file.name.endsWith("batch_bak.xml.zip"))
						html += '<a class="list-group-item file" fileName="' + file.name + '">' + file.name + '<div class="chart-actions"><button type="button" class="btn btn-default compare-xml-file hidden" aria-label="Compare"><span class="glyphicon glyphicon-transfer" aria-hidden="true"></span></button></div></a>';
					else
						html += '<a class="list-group-item file" fileName="' + file.name + '">' + file.name + '</a>';
				}
				html += '</div>';
				$("#listoffiles").append(html);
			}

			// Then attach click event on file
			$("#listoffiles .file").click(previewFile);
			// Then attach click event on the compare button
			$("#listoffiles .file .compare-xml-file").click(compareXMLFile)
		}
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
	label += nbOfSeconds + "s";

	return label;
}

function previewFile(e) {
	var fileName = $(this).attr("fileName");

	$(this).parent(".list-group").find(".file.active").removeClass("active");
	$(this).parent(".list-group").find(".file .compare-xml-file").removeClass("hidden");
	$(this).addClass("active");

	var extension = fileName.substr(fileName.lastIndexOf('.') + 1).toLowerCase();
	var identifier = $("#batchidentifier").val();
	var url = DATA_URL + "/" + identifier + "/" + fileName;

	if (extension == 'png') {
		$("#previewfile").html("<img src='" + url + "' />");
	} else if (extension == 'tif' || extension == 'tiff' || extension == 'pdf') {
		$("#previewfile").html('<form target="_blank" method="get" action="' + url + '"><button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-download"></span> Download</button></form>');
	} else if (extension == 'xml' || extension == 'zip') {
		// Hide the compare button
		$(this).find(".compare-xml-file").addClass("hidden");

		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=system-folder/getBatchInstanceFile(java.lang.String,java.lang.String)/' + identifier + '/' + fileName,
			dataType : "json",
			headers : headers,
			success : function(d) {
				var result = eval('(' + d.value + ')')
				if (result.success) {
					var data = result.data;
					data = vkbeautify.xml(data);
					data = data.replace(/</g, "&lt;");
					data = data.replace(/>/g, "&gt;");
					data = data.replace(/\t/g, "  ");
					$("#previewfile").html('<pre class="prettyprint">' + data + '</pre>');
					prettyPrint();
				} else {
					$("#previewfile").append('<div class="error-notice"><div class="oaerror info"><strong>INFO</strong> - ' + result.message + '</div></div>');
				}
			}
		});
	}
}

function compareXMLFile(e) {
	e.stopPropagation();
	var identifier = $("#batchidentifier").val();
	var source = $(this).closest(".list-group").find(".file.active");
	var destination = $(this).closest(".file");
	var src_filename = $(source).attr("fileName");
	var dest_filename = $(destination).attr("fileName");

	$.ajax({
		url : JOLOKIA_URL + '/exec/ephesoft:type=system-folder/getBatchInstanceFile(java.lang.String,java.lang.String)/' + identifier + '/' + src_filename,
		dataType : "json",
		headers : headers,
		success : function(d) {
			var result = eval('(' + d.value + ')')
			if (result.success) {
				var src_data = result.data;
				src_data = vkbeautify.xml(src_data);

				$.ajax({
					url : JOLOKIA_URL + '/exec/ephesoft:type=system-folder/getBatchInstanceFile(java.lang.String,java.lang.String)/' + identifier + '/' + dest_filename,
					dataType : "json",
					headers : headers,
					success : function(d) {
						var result = eval('(' + d.value + ')')
						if (result.success) {
							var dest_data = result.data;
							dest_data = vkbeautify.xml(dest_data);

							var diff = JsDiff["diffLines"](src_data, dest_data);
							var fragment = document.createDocumentFragment();

							for (var i = 0; i < diff.length; i++) {

								if (diff[i].added && diff[i + 1] && diff[i + 1].removed) {
									var swap = diff[i];
									diff[i] = diff[i + 1];
									diff[i + 1] = swap;
								}

								var node;
								if (diff[i].removed) {
									node = document.createElement('del');
									node.appendChild(document.createTextNode(diff[i].value));
								} else if (diff[i].added) {
									node = document.createElement('ins');
									node.appendChild(document.createTextNode(diff[i].value));
								} else {
									node = document.createTextNode(diff[i].value);
								}
								fragment.appendChild(node);
							}

							$("#previewfile").html("<pre class='compare-file'></pre>");
							$("#previewfile pre").html(fragment);
						} else {
							$("#previewfile").append('<div class="error-notice"><div class="oaerror info"><strong>INFO</strong> - ' + result.message + '</div></div>');
						}
					}
				});
			} else {
				$("#previewfile").append('<div class="error-notice"><div class="oaerror info"><strong>INFO</strong> - ' + result.message + '</div></div>');
			}
		}
	});

}