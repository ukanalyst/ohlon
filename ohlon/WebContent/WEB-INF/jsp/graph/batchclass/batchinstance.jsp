<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page session="true"%>

<!DOCTYPE HTML>
<html>
<head>
<script type="text/javascript">
	var JOLOKIA_URL = "${jolokia}";
	var JOLOKIA_AUTH = "${auth}";
	var bc = "${bc}";
	var from = "${from}";
	var to = "${to}";
	<c:if test="${not empty pages}" >
	var pages = ${pages};
	</c:if>
</script>
<style>
.btn-default:hover, .btn-default:focus, .btn-default:active,
	.btn-default.active, .open>.dropdown-toggle.btn-default {
	color: #333;
	background-color: #e6e6e6;
	border-color: #adadad;
}

.btn-default {
	color: #333;
	background-color: #fff;
	border-color: #ccc;
}

.btn.load-more {
	display: inline-block;
	padding: 6px 12px;
	margin-bottom: 0;
	font-size: 14px;
	font-weight: 400;
	line-height: 1.42857143;
	text-align: center;
	white-space: nowrap;
	vertical-align: middle;
	cursor: pointer;
	-webkit-user-select: none;
	-moz-user-select: none;
	-ms-user-select: none;
	user-select: none;
	background-image: none;
	border: 1px solid #ccc;
	border-radius: 4px;
	width: 100%;
}
</style>
<link rel="stylesheet" href="../assets/css/batchinstances.css">
<link rel="stylesheet" href="../assets/css/keen-dashboards.css">
<script type="text/javascript" src="../assets/lib/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="../assets/lib/moment.js"></script>
<script type="text/javascript" src="../assets/js/graph/batchclass/batchinstance.js"></script>
</head>
<body>
	<div id="container" style="width: 100%;"></div>
	<div>
		<button type="button" class="btn btn-default load-more" style="display: none;">Load More</button>
	</div>
</body>
</html>
