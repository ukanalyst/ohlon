<!DOCTYPE HTML>
<html>
<head>
<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
	var bc = "${bc}";
	var from = "${from}";
	var to = "${to}";
	var maxDuration = "${maxDuration}";
	var graphInterval = "${graphInterval}";
</script>
<script type="text/javascript" src="../assets/js/graph/batchclass/accumulation.js"></script>
<script type="text/javascript" src="../assets/lib/canvasjs/canvasjs.min.js"></script>
<script type="text/javascript" src="../assets/lib/jquery/dist/jquery.min.js"></script>
</head>
<body>
	<div id="chartContainer" style="height: 250px; width: 100%;"></div>
</body>
</html>
