window.onload = function() {

	var headers = {};
	if (JOLOKIA_AUTH != null && JOLOKIA_AUTH.length > 0)
		headers = {
			'Authorization' : "Basic " + JOLOKIA_AUTH
		};
	
	var updateInterval = 1000;
	var created = false;

	var updateChart = function() {

		$.ajax({
			url : JOLOKIA_URL + "/read/ephesoft:type=batchinstance-stats/ReadyForReviewBatchInstances",
			dataType : "json",
			headers : headers,
			success : function(data) {
				if (created)
					$(".dial").val(data.value).trigger('change');
				else {
					$(".dial").val(data.value);
					$(".dial").css("display", "");
					$(".dial").knob({
						readOnly : true,
						min : 0,
						max : 10
					});
					created = true;
				}
			}
		});
	};

	// update chart after specified interval
	setInterval(function() {
		updateChart()
	}, updateInterval);
}