<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
</script>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico"/>
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />
<style>
.form-group {
	padding: 5px 10px;
}
</style>
</head>
<body class="application" style="overflow-x: hidden">

	<script type="text/javascript" src="assets/js/pages/batchclass.js"></script>

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">Configure your report</div>
					<div class="chart-stage">
						<form class="form-inline">
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
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Batch Instances</div>
					<div class="chart-stage">
						<iframe id="batchinstance" src="graph/batchclass-batchinstances" width="100%" height="610px" style="border: 0; overflow-x: hidden;"></iframe>
					</div>
					<div class="chart-notes">List all batch instances</div>
				</div>
			</div>
			<div class="col-sm-9">
				<div class="row">
					<div class="col-sm-12">
						<div class="chart-wrapper">
							<div class="chart-title">Completion Time Repartition</div>
							<div class="chart-stage">
								<iframe id="repartition-graph" src="graph/batchclass-repartition" width="100%" height="260px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Batch Instance Completion Time Repartition</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-12">
						<div class="chart-wrapper">
							<div class="chart-title">Completion Time</div>
							<div class="chart-stage">
								<iframe id="accumulation-graph" src="graph/batchclass-accumulation" width="100%" height="260px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Batch Instance Completion Time Accumulation</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<hr>

	<script src="assets/lib/jquery/dist/jquery.min.js"></script>
	<script src="assets/lib/bootstrap/dist/js/bootstrap.min.js"></script>
	<script src="assets/lib/moment.js"></script>
	<script src="assets/lib/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>

	<%@include file="includes/footer.jsp"%>

</body>
</html>
