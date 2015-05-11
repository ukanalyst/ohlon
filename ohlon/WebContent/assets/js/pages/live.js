window.onload = function() {
	var updateServerStatusInterval = 3000;

	refreshServerStatus();
	setInterval(function() {
		refreshServerStatus()
	}, updateServerStatusInterval);
}

function refreshServerStatus() {
	$.ajax({
		url : JOLOKIA_URL + "/exec/ephesoft:type=application-details/getApplicationDetails/",
		timeout : 2000,
		dataType : "json",
		success : function(data) {
			if (data.value) {
				var d = eval("(" + data.value + ")");
				if (d) {
					$("#ephesoft-version").html(d["ephesoft.version"]);
					$("#os-name").html(d["os.name"]);

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