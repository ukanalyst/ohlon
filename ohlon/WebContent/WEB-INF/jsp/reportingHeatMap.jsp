<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico" />
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />

<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
</script>

<style type="text/css">
li.page-link[data-pageid='reporting-heatmap'] {
	background-color: #e7e7e7;
}

.form-group {
	padding: 5px 10px;
}

#modules th:first-child, #plugins th:first-child {
	width: 360px;
}

#heatmap {
	width: 100%;
	min-height: 600px;
}

#heatmap .module-name {
	font-size: 14px;
	color: rgb(51,51,51);
}

#heatmap .module-property-label {
	font-size: 12px;
	font-style: italic;
	color: rgb(51,51,51);
}

#heatmap .plugin-name {
	font-size: 10px;
	color: rgb(51,51,51);
}

#heatmap .plugin-property-label, #heatmap .plugin-property-value {
	font-size: 9px;
	font-style: italic;
	color: rgb(51,51,51);
}
</style>

</head>
<body class="application" style="overflow-x: hidden">

	<script src="assets/js/pages/reporting-heatmap.js"></script>

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Reporting Synchronisation</div>
					<div class="chart-stage">
						<div style='width: 100%; text-align: center;'>
							Last Synchronisation: <span class='lastsync'></span>
						</div>
						<br />
						<button type="button" id="syncDatabase" class="btn btn-default" style='width: 100%; text-align: center;'>Sync Database</button>
					</div>
				</div>
			</div>

			<div class="col-sm-9">
				<div class="chart-wrapper">
					<div class="chart-title">Configure your report</div>
					<div class="chart-stage">
						<form class="form-inline" style="padding: 14px 0px;">
							<div class="form-group">
								<label for="batchclass">Batch Class</label> <select class="form-control" id="batchclass">
								</select>
							</div>
							<div class="form-group">
								<label for="from">From</label>
								<div class='input-group date'>
									<input type='text' class="form-control" id='from' /> <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span> </span>
								</div>
							</div>
							<div class="form-group">
								<label for="from">To</label>
								<div class='input-group date'>
									<input type='text' class="form-control" id='to' /> <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span> </span>
								</div>
							</div>
							<button type="button" id="refreshReports" class="btn btn-default">Refresh reports</button>
						</form>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">Heat Map</div>
					<div class="chart-stage">
						<svg id="heatmap">
						</svg>
					</div>
				</div>
			</div>
		</div>

	<script src="assets/lib/jquery/dist/jquery.min.js"></script>
	<script src="assets/lib/bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="assets/lib/moment.js"></script>
	<script src="assets/lib/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
	<script src="assets/lib/snap.svg-0.4.1/dist/snap.svg-min.js"></script>
	<script src="assets/lib/svg-pan-zoom.min.js"></script>

	<%@include file="includes/footer.jsp"%>

</body>
</html>
