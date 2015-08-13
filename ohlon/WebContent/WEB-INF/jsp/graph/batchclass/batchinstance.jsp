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
	<c:if test="${not empty pages}" > var pages = ${pages};</c:if>
</script>
<link rel="stylesheet" href="../assets/css/batchinstances.css">
<link rel="stylesheet" href="../assets/css/keen-dashboards.css">
<script type="text/javascript" src="../assets/lib/jquery/dist/jquery.min.js"></script>
<script type="text/javascript" src="../assets/lib/moment.js"></script>
<script type="text/javascript" src="../assets/js/graph/batchclass/batchinstance.js"></script>
</head>
<body>
	<div id="container" style="width: 100%;"></div>
</body>
</html>
