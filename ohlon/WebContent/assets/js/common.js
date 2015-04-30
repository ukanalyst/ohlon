$(document).ready(function() {
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
});