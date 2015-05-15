<!DOCTYPE html>
<html>
<head>
<title>Ohlon</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico"/>
<link rel="stylesheet" href="assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="assets/css/keen-dashboards.css">
<link rel="stylesheet" href="assets/lib/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" />

<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
</script>

<style>
.form-group {
	padding: 5px 10px;
}

#modules th:first-child, #plugins th:first-child {
	width: 360px;
}
</style>

</head>
<body class="application" style="overflow-x: hidden">

	<script src="assets/js/pages/reporting.js"></script>

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
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Number of batch instance(s)</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main red" id="nbOfBatchIntances"></div>
						</div>
					</div>
				</div>
			</div>
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
					<div class="chart-title">Average Processing Time</div>
					<div class="chart-stage">
						<div class="metric-container">
							<div class="metric-main blue" id="averageProcessingTime"></div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-sm-12">
				<div class="chart-wrapper">
					<div class="chart-title">Batch Class</div>
					<div class="chart-stage">
						<table class='table table-hover table-condensed' id='workflow'>
							<thead>
								<tr>
									<th></th>
									<th>Minimum</th>
									<th>Average</th>
									<th>Maximum</th>
								</tr>
							</thead>
							<tbody>
								<tr>
									<td>Duration</td>
									<td data-attr="MINDURATION"></td>
									<td data-attr="AVGDURATION"></td>
									<td data-attr="MAXDURATION"></td>
								</tr>
								<tr>
									<td># Document processed per hour</td>
									<td data-attr="MINDOCPS"></td>
									<td data-attr="AVGDOCPS"></td>
									<td data-attr="MAXDOCPS"></td>
								</tr>
								<tr>
									<td># Page processed per minute</td>
									<td data-attr="MINPAGEPS"></td>
									<td data-attr="AVGPAGEPS"></td>
									<td data-attr="MAXPAGEPS"></td>
								</tr>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>

		<div class="row">
			<div class="col-sm-6">
				<div class="chart-wrapper">
					<div class="chart-title">Modules</div>
					<div class="chart-stage" id="modules">
						<div class='module-template' style="display: none;">
							<table class='table table-hover table-condensed'>
								<thead>
									<tr>
										<th data-attr="WORKFLOW_NAME"></th>
										<th>Minimum</th>
										<th>Average</th>
										<th>Maximum</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td>Duration</td>
										<td data-attr="MINDURATION"></td>
										<td data-attr="AVGDURATION"></td>
										<td data-attr="MAXDURATION"></td>
									</tr>
									<tr>
										<td># Document processed per hour</td>
										<td data-attr="MINDOCPS"></td>
										<td data-attr="AVGDOCPS"></td>
										<td data-attr="MAXDOCPS"></td>
									</tr>
									<tr>
										<td># Page processed per minute</td>
										<td data-attr="MINPAGEPS"></td>
										<td data-attr="AVGPAGEPS"></td>
										<td data-attr="MAXPAGEPS"></td>
									</tr>
								</tbody>
							</table>
							<button type="button" class="display-graph btn btn-default">Show Graph</button>
							<iframe src='graph/reporting/artifact-repartition.do' class='repartition' style="display: none; border: 0; overflow: hidden;" width="100%" height="270px"></iframe>
							<iframe src='graph/reporting/artifact-accumulation.do' class='accumulation' width="100%" height="270px" style="display: none; border: 0; overflow: hidden;"></iframe>
							<br /> <br />
						</div>
					</div>
				</div>
			</div>
			<div class="col-sm-6">
				<div class="chart-wrapper">
					<div class="chart-title">Plugins</div>
					<div class="chart-stage" id="plugins">
						<div class='plugin-template' style="display: none;">
							<table class='table table-hover table-condensed'>
								<thead>
									<tr>
										<th data-attr="WORKFLOW_NAME"></th>
										<th>Minimum</th>
										<th>Average</th>
										<th>Maximum</th>
									</tr>
								</thead>
								<tbody>
									<tr>
										<td>Duration</td>
										<td data-attr="MINDURATION"></td>
										<td data-attr="AVGDURATION"></td>
										<td data-attr="MAXDURATION"></td>
									</tr>
									<tr>
										<td># Document processed per hour</td>
										<td data-attr="MINDOCPS"></td>
										<td data-attr="AVGDOCPS"></td>
										<td data-attr="MAXDOCPS"></td>
									</tr>
									<tr>
										<td># Page processed per minute</td>
										<td data-attr="MINPAGEPS"></td>
										<td data-attr="AVGPAGEPS"></td>
										<td data-attr="MAXPAGEPS"></td>
									</tr>
								</tbody>
							</table>
							<button type="button" class="display-graph btn btn-default">Show Graph</button>
							<iframe src='graph/reporting/artifact-repartition.do' class='repartition' style="display: none; border: 0; overflow: hidden;" width="100%" height="270px"></iframe>
							<iframe src='graph/reporting/artifact-accumulation.do' class='accumulation' width="100%" height="270px" style="display: none; border: 0; overflow: hidden;"></iframe>
							<br /> <br />
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
