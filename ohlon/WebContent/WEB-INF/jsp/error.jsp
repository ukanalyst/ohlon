<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico" />
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/lib/bootstrap3-dialog/dist/css/bootstrap-dialog.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />

<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
</script>

<style>
.modal-dialog {
	width: 95%;
	font-size: 11px;
}
</style>

</head>
<body class="application" style="overflow-x: hidden">

	<script src="assets/js/pages/error.js"></script>

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">
						List of errors
						<div class='chart-actions'>
							<button type="button" class="btn btn-default" aria-label="Show/Hide" id="removeAllEntries">
								<span class="glyphicon glyphicon-remove" aria-hidden="true"></span>
							</button>
						</div>
					</div>
					<div class="chart-stage" style="">
						<table id="logEntries" class="table table-hover">
							<thead>
								<tr>
									<th>Actions</th>
									<th style="min-width: 150px;">Time</th>
									<th>Thread Name</th>
									<th>Level</th>
									<th>LoggerName</th>
									<th>Message</th>
								</tr>
							</thead>
							<tbody>
							</tbody>
						</table>
					</div>
				</div>
			</div>

		</div>
	</div>

	<hr>

	<script src="assets/lib/jquery/dist/jquery.min.js"></script>
	<script src="assets/lib/bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="assets/lib/bootstrap3-dialog/dist/js/bootstrap-dialog.min.js"></script>

	<%@include file="includes/footer.jsp"%>

</body>
</html>
