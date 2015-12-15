<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@page session="true"%>

<script type="text/javascript">
	<c:if test="${not empty servers}" > var servers = ${servers};</c:if>
	<c:if test="${not empty pages}" > var pages = ${pages};</c:if>
	var currentServerId = "${currentId}";
</script>

<style>
.navbar-login {
	width: 305px;
	padding: 10px;
	padding-bottom: 0px;
}

.navbar-login-session {
	padding: 10px;
	padding-bottom: 0px;
	padding-top: 0px;
}

.icon-size {
	font-size: 87px;
}

div[role='navigation'] .container {
	width: 100%;
}
</style>

<c:url value="/j_spring_security_logout" var="logoutUrl" />
<!-- csrt for log out-->
<form action="${logoutUrl}" method="post" id="logoutForm">
	<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
</form>

<script>
	function logout() {
		document.getElementById("logoutForm").submit();
	}
</script>

<div class="navbar navbar-default navbar-fixed-top" role="navigation">
	<div class="container">
		<div class="navbar-header">
			<a target="_blank" href="http://www.ohlon.com"><img style="padding: 5px;" src="./assets/img/app-header.png" /></a>
		</div>
		<div class="collapse navbar-collapse">
			<ul class="nav navbar-nav">

				<li class="page-link" style="display: none;" data-pageid="live"><a href="./live">Live</a></li>
				<li class="page-link" style="display: none;" data-pageid="error"><a href="./error">Error</a></li>
				<li class="page-link" style="display: none;" data-pageid="error"><div class="error-indicator">0</div></li>
				<li class="dropdown" style="display: none;"><a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Analysis <span class="caret"></span></a>
					<ul class="dropdown-menu">
						<li class="page-link" style="display: none;" data-pageid="reporting"><a href="./reporting">Detailed View</a></li>
						<li class="page-link" style="display: none;" data-pageid="reporting-heatmap"><a href="./reporting-heatmap">Heat Map</a></li>
					</ul></li>
				<li class="page-link" style="display: none;" data-pageid="batchclass"><a href="./batchclass">Batch Class</a></li>
				<li class="page-link" style="display: none;" data-pageid="batchinstance"><a href="./batchinstance">Batch Instance</a></li>
				<li class="page-link" style="display: none;" data-pageid="user"><a href="./user">Manual Steps</a></li>
				<li class="page-link" style="display: none;" data-pageid="serverstatus"><a href="./serverstatus">Server Status</a></li>
				<li class="page-link" style="display: none;" data-pageid="reportgenerator"><a href="./reportgenerator">Report</a></li>

			</ul>
			<ul class="nav navbar-nav navbar-right">
				<li class="dropdown"><a href="#" class="dropdown-toggle" data-toggle="dropdown"> <span class="glyphicon glyphicon-user"></span>  <strong>Profile</strong> <span class="glyphicon glyphicon-chevron-down"></span>
				</a>
					<ul class="dropdown-menu" id="top-menu">
						<li><a href="javascript:logout()"><span class="glyphicon glyphicon-log-out"></span> Logout</a></li>
					</ul></li>
			</ul>
		</div>
	</div>
</div>