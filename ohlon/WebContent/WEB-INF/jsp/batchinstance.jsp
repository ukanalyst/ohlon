<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico"/>
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />
<link rel="stylesheet" href="./assets/css/login.css">
<link rel="stylesheet" href="./assets/css/alert.css">

<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
	var DATA_URL = "${dataUrl}";
	var identifier = "${identifier}";
</script>
<style type="text/css">
	li.page-link[data-pageid='batchinstance'] {
		background-color: #e7e7e7;
	}
</style>
<script src="./assets/lib/google-code-prettify/src/run_prettify.js"></script>
<script src="./assets/lib/google-code-prettify/src/prettify.js"></script>
<script src="./assets/lib/vkbeautify.js"></script>
<script src="./assets/lib/diff.js"></script>

<link rel="stylesheet" href="./assets/css/pages/batchinstance.css">

</head>
<body class="application" style="overflow-x: hidden">

	<script src="./assets/js/pages/batchinstance.js"></script>

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-2">
				<div class="chart-wrapper">
					<div class="chart-title">Search a batch instance</div>
					<div class="chart-stage">
						<div class="form-group" style="margin-bottom: 0px !important;">
							<div class="input-group">
								<input type="text" class="form-control" placeholder="Batch Identifier" id="batchidentifier"> <span class="input-group-btn">
									<button id="displayDetails" class="btn btn-default" type="button">Go!</button>
								</span>
							</div>
							<!-- /input-group -->
						</div>
					</div>
				</div>
			</div>

			<div class="col-sm-10">
				<div class="chart-wrapper">
					<div class="chart-title">Execution information</div>
					<div class="chart-stage form-inline">
						<div class="form-group">
							<label for="batchclass">Batch Class</label> <input type="text" readonly="readonly" class="form-control" id="batchclass" style="width: 60px;" />
						</div>
						<div class="form-group">
							<label for="from">From</label> <input type='text' class="form-control" id='from' style="width: 225px;" readonly="readonly" />
						</div>
						<div class="form-group">
							<label for="from">To</label> <input type='text' class="form-control" id='to' style="width: 225px;" readonly="readonly" />
						</div>
						<div class="form-group">
							<label for="from">Duration</label> <input type='text' class="form-control" id='duration' style="width: 100px;" readonly="readonly" />
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Number of document(s)</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main yellow" id="nbOfDocuments"></div>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Number of page(s)</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main green" id="nbOfPages"></div>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Processing Time</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main blue" id="processingTime"></div>
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Execution Percentage</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main red" id="executionPercentage"></div>
						</div>
					</div>
				</div>
			</div>
		</div>


		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">
						Details
						<div class='chart-actions'>
							<button type="button" class="btn btn-default" aria-label="Show/Hide" id="showhideDetails">
								<span class="glyphicon glyphicon-eye-close" aria-hidden="true"></span>
							</button>
						</div>
					</div>
					<div class="chart-stage" id="progress-details"></div>
				</div>
			</div>
		</div>


		<div class="row">
			<div class="col-sm-4">
				<div class="chart-wrapper">
					<div class="chart-title">
						List of files
						<div class='chart-actions'>
							<button type="button" class="btn btn-default" aria-label="Refresh" id="refreshFiles">
								<span class="glyphicon glyphicon-refresh" aria-hidden="true"></span>
							</button>
						</div>
					</div>
					<div class="chart-stage" id="listoffiles"></div>
				</div>
			</div>
			<div class="col-sm-8">
				<div class="chart-wrapper">
					<div class="chart-title">Preview</div>
					<div class="chart-stage" id="previewfile"></div>
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
