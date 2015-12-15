$(document).ready(function() {

	var headers = {};
	if (typeof (JOLOKIA_AUTH) !== 'undefined' && JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0) {
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};

		// Refresh the number of errors
		refreshErrorNumbers();
		setInterval(refreshErrorNumbers, 1000);
	}

	if (typeof (servers) !== 'undefined') {
		// Load the list of servers
		var html = "";
		for (var i = 0; i < servers.length; i++) {
			var id = servers[i].id;
			var label = servers[i].label;
			var defaultPageId = servers[i].defaultPageId;
			html += '<li class="server" id ="' + id + '"><a href="' + defaultPageId + '?id=' + id + '"><span class="glyphicon glyphicon-tasks"></span> ' + label + '</a></li>';
		}
		$("#top-menu").prepend(html);

		$("#top-menu .server").removeClass("active");
		$("#top-menu #" + currentServerId + ".server").addClass("active");
	}

	if (typeof (pages) !== 'undefined') {
		// Hide/Show/Delete pages
		if (pages == null || pages.length == 0) {
			// Enable all pages
			$("li.page-link").css("display", "");
			$("li.dropdown").css("display", "");
		} else {
			$("li.page-link").attr("toDelete", true);

			// Display all valid pages
			for (var i = 0; i < pages.length; i++) {
				$("li.page-link[data-pageid='" + pages[i] + "']").css("display", "");
				$("li.page-link[data-pageid='" + pages[i] + "']").removeAttr("toDelete");
			}

			// Delete all other links
			$("li.page-link[toDelete=true]:hidden").remove();
		}
	}

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