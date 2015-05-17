$(document).ready(function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

	// Load the list of servers
	var html = "";
	for (var i = 0; i < servers.length; i++) {
		var id = servers[i].id;
		var label = servers[i].label;
		html += '<li class="server" id ="' + id + '"><a href="live?id=' + id + '"><span class="glyphicon glyphicon-tasks"></span> ' + label + '</a></li>';
	}
	$("#top-menu").prepend(html);

	$("#top-menu .server").removeClass("active");
	$("#top-menu #" + currentServerId + ".server").addClass("active");

	// Refresh the number of errors
	refreshErrorNumbers();
	setInterval(refreshErrorNumbers, 1000);

	function refreshErrorNumbers() {
		$.ajax({
			url : JOLOKIA_URL + '/exec/ephesoft:type=ohlon-logger/getLogListSize',
			dataType : "json",
			headers : headers,
			success : function(data) {
				$(".error-indicator").html(data.value);
				if (parseInt(data.value) > 0)
					$(".error-indicator").css("display", "block");
				else
					$(".error-indicator").css("display", "none");
			}
		});
	}
});