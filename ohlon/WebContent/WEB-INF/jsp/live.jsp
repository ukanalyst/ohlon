<!DOCTYPE html>
<html>
<head>
<title>Ephesoft Monitoring</title>
<link rel="icon" type="image/ico" href="./assets/img/favicon.ico"/>
<link rel="stylesheet" href="./assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="./assets/css/keen-dashboards.css">
</head>
<body class="application" style="overflow-x: hidden">

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid">

		<div class="row">
			<div class="col-sm-3">
				<div class="chart-wrapper">
					<div class="chart-title">Batch Instances</div>
					<div class="chart-stage">
						<iframe src="graph/batchinstance" width="100%" height="720px" style="border: 0; overflow: hidden;"></iframe>
					</div>
					<div class="chart-notes">Display the list of active batch instances</div>
				</div>
			</div>
			<div class="col-sm-9">
				<div class="row">
					<div class="col-sm-6">
						<div class="chart-wrapper">
							<div class="chart-title">Heap Memory Usage</div>
							<div class="chart-stage">
								<iframe src="graph/heap-memory-usage" width="100%" height="300px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Chart displaying the heap memory usage.</div>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="chart-wrapper">
							<div class="chart-title">CPU Usage</div>
							<div class="chart-stage">
								<iframe src="graph/cpu-usage" width="100%" height="300px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Chart displaying the CPU usage.</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-3">
						<div class="chart-wrapper">
							<div class="chart-title">Running</div>
							<div class="chart-stage">
								<iframe src="graph/running-batch-instances" width="100%" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Running Batch Instances</div>
						</div>
					</div>
					<div class="col-sm-3">
						<div class="chart-wrapper">
							<div class="chart-title">Error</div>
							<div class="chart-stage">
								<iframe src="graph/error-batch-instances" width="100%" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Error Batch Instances</div>
						</div>
					</div>
					<div class="col-sm-3">
						<div class="chart-wrapper">
							<div class="chart-title">Ready For Review</div>
							<div class="chart-stage">
								<iframe src="graph/readyforreview-batch-instances" width="100%" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Ready For Review Batch Instances</div>
						</div>
					</div>
					<div class="col-sm-3">
						<div class="chart-wrapper">
							<div class="chart-title">Ready For Validation</div>
							<div class="chart-stage">
								<iframe src="graph/readyforvalidation-batch-instances" width="100%" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Ready For Validation Batch Instances</div>
						</div>
					</div>
				</div>
				<div class="row">
					<div class="col-sm-6">
						<div class="chart-wrapper">
							<div class="chart-title">Priority</div>
							<div class="chart-stage">
								<iframe src="graph/batch-priorities" width="500px" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Batch Instances per Priority</div>
						</div>
					</div>
					<div class="col-sm-6">
						<div class="chart-wrapper">
							<div class="chart-title">Batch Class</div>
							<div class="chart-stage">
								<iframe src="graph/batchclass" width="500px" height="120px" style="border: 0; overflow: hidden;" scrolling="no"></iframe>
							</div>
							<div class="chart-notes">Batch Instances per Batch Class</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>



	<hr>

	<script src="./assets/lib/jquery/dist/jquery.min.js"></script>
	<script src="./assets/lib/bootstrap/dist/js/bootstrap.min.js"></script>

	<%@include file="includes/footer.jsp"%>

</body>
</html>
