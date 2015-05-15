<!DOCTYPE HTML>
<html>
<head>
<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
</script>
<script type="text/javascript" src="../assets/js/graph/live/error-batch-instances.js"></script>
<script type="text/javascript" src="../assets/lib/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="../assets/lib/jquery-knob/dist/jquery.knob.min.js"></script>
<style type="text/css">
#container>div {
	display: block !important;
	margin: auto auto;
}
</style>
</head>
<body>
	<div id="container" style="width: 100%;">
		<input type="text" value="75" class="dial" data-fgColor="#aa3939" data-angleOffset=-125 data-angleArc=250 data-width="140" style="display: none;">
	</div>
</body>
</html>
