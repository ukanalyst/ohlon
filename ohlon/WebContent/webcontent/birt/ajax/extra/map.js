function drawMyMap(stateData) {

	jQuery("#USAmap").vectorMap({
		map : "us_aea_en",
		series : {
			regions : [ {
				values : stateData,
				scale : [ '#CCCCCC', '#0071A4' ],
				normalizeFunction : 'linear'
			} ]
		},
		onRegionTipShow : function(event, label, code) {
			label.html(label.html() + ': ' + stateData[code]);
		}
	});

}