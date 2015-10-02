<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico" />
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />
<link rel="stylesheet" href="./assets/css/login.css">
<link rel="stylesheet" href="./assets/css/alert.css">

<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
	var SERVER_NAMES = ${server_name};
</script>
<style type="text/css">
	li.page-link[data-pageid='serverstatus'] {
		background-color: #e7e7e7;
	}
</style>
<link rel="stylesheet" href="./assets/css/pages/serverstatus.css">

</head>
<body class="application" style="overflow-x: hidden">

	<script src="./assets/js/pages/serverstatus.js"></script>

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Select a month</div>
					<div class="chart-stage">
						<div class="form-group" style="margin-bottom: 0px !important;">
							<div class='input-group date' id='month'>
								<input type='text' class="form-control" /> <span class="input-group-addon"> <span class="glyphicon glyphicon-calendar"></span>
								</span>
							</div>
							<button type="button" id="displayStats" class="btn btn-default" style='width: 100%; text-align: center;'>Refresh</button>
						</div>
					</div>
				</div>
			</div>

			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Reporting Synchronisation</div>
					<div class="chart-stage">
						<div style='width: 100%; text-align: center; padding-top: 14px;'>
							Last Synchronisation: <span class='lastsync'></span>
						</div>
						<br />
						<button type="button" id="syncDatabase" class="btn btn-default" style='width: 100%; text-align: center;'>Sync Database</button>
					</div>
				</div>
			</div>

			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Availability</div>
					<div class="chart-stage global-statistic">
						<div class="metric-container">
							<div class="metric">
								<div class="percentage"></div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Up time</div>
					<div class="chart-stage global-statistic">
						<div class="metric-container">
							<div class="metric">
								<div class="time"></div>
							</div>
						</div>
					</div>
				</div>
			</div>

		</div>

		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">Month Availability</div>
					<div class="chart-stage" id="weeksContainer"></div>
				</div>
			</div>
		</div>
	</div>

	<hr>

	<script src="assets/lib/jquery/dist/jquery.min.js"></script>
	<script src="assets/lib/bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="assets/lib/moment.js"></script>
	<script src="assets/lib/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
	<script src="assets/lib/bootstrap3-dialog/dist/js/bootstrap-dialog.min.js"></script>

	<%@include file="includes/footer.jsp"%>

</body>
</html>
