$(document).ready(function() {
	// Load the list of servers
	serverIds = Object.keys(servers);
	var html = "";
	for (var i = 0; i < serverIds.length; i++) {
		html += '<li class="server" id ="' + serverIds[i] + '"><a href="live?id=' + serverIds[i] + '"><span class="glyphicon glyphicon-tasks"></span> ' + servers[serverIds[i]].label + '</a></li>';
	}
	$("#top-menu").prepend(html);

	$("#top-menu .server").removeClass("active");
	$("#top-menu #" + currentServerId + ".server").addClass("active");
});