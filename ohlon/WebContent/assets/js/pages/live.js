var headers = {};
if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
	headers = {
		'Authorization' : "Basic " + JOLOKIA_AUTH
	};

window.onload = function() {
	var updateServerStatusInterval = 3000;

	refreshServerStatus();
	setInterval(function() {
		refreshServerStatus()
	}, updateServerStatusInterval);

	$("#more-information").click(function() {
		var html = $("#more-information span").html();
		if (html == "More...")
			$("#more-information span").html("Less...");
		else
			$("#more-information span").html("More...");
		$(".more-information-content").toggle();
	});
}

function refreshServerStatus() {
	$.ajax({
		url : JOLOKIA_URL + "/exec/ephesoft:type=application-details/getApplicationDetails/",
		timeout : 2000,
		dataType : "json",
		headers : headers,
		success : function(data) {
			if (data.value) {
				var d = eval("(" + data.value + ")");
				if (d) {
					$("#ephesoft-version").html(d["ephesoft.version"]);
					$("#os-name").html(d["os.name"]);
					$("#expiration-message").html(d["ohlon.expiration"]);

					$("#server-status").removeClass("serverKO");
					$("#server-status").addClass("serverOK");
				}
			}
		},
		error : function() {
			$("#server-status").removeClass("serverOK");
			$("#server-status").addClass("serverKO");
		}
	});

}