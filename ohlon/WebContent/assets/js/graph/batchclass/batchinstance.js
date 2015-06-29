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
	if (bc && bc.length > 0)
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=batchinstance-stats/getBatchInstanceByBatchClass(java.lang.String,java.lang.String,java.lang.String)/' + bc + '/' + from + '/' + to,
			dataType : "json",
			headers : headers,
			success : function(data) {
				var d = eval(data.value);
				var html = "";

				for (var i = 0; i < d.length; i++) {
					html += generateHtml(d[i]);
				}

				$("#container").html(html);
			}
		});
};

function generateHtml(bi) {

	// We create a new batch instance
	var html = "<div id='" + bi.identifier + "' class='batchinstance'>";
	html += generateInnerHtml(bi);
	html += "</div>";

	return html;
}

function generateInnerHtml(bi) {

	var start = new Date(bi.start);
	var end = new Date(bi.end);

	var generateLink = typeof (pages) !== 'undefined' && (pages.indexOf('batchinstance') != -1 || pages.length == 0);
	
	var html = "";

	html += "<div class='details'>";
	html += "	<div class='row'>";
	if (generateLink)
		html += "		<div class='identifier'><a target='_parent' href='../batchinstance?identifier=" + bi.identifier + "'>" + bi.identifier + "</a>: </div>";
	else
		html += "		<div class='identifier'>" + bi.identifier + ": </div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	html += "	<div class='row'>";
	html += "		<div class='label'>Batch Name: </div>";
	html += "		<div class='value'>" + bi.batch_name + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	html += "	<div class='row'>";
	html += "		<div class='label'>Start Date: </div>";
	html += "		<div class='value'>" + bi.start + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	html += "	<div class='row'>";
	html += "		<div class='label'>End Date: </div>";
	html += "		<div class='value'>" + bi.end + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
	html += "	<div class='row'>";
	html += "		<div class='label'>Duration: </div>";
	html += "		<div class='value'>" + convertDuration(end.getTime() - start.getTime()) + "</div>";
	html += "	</div>";
	html += "	<div style='clear:both;'></div>";
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