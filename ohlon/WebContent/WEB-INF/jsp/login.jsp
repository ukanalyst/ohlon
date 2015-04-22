<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<title>Ephesoft Monitoring</title>
<link rel="stylesheet" href="./assets/lib/bootstrap/dist/css/bootstrap.min.css">
<link rel="stylesheet" href="./assets/css/keen-dashboards.css">
<link rel="stylesheet" href="./assets/css/login.css">
<link rel="stylesheet" href="./assets/css/alert.css">
</head>
<body class="application" style="overflow-x: hidden">

	<%@include file="includes/header.jsp"%>

	<div class="container-fluid" style="margin-top: 40px">

		<div class="row">
			<div class="col-sm-6 col-md-4 col-md-offset-4">
				<div class="panel panel-default">
					<div class="panel-heading">
						<strong>Login</strong>
					</div>
					<div class="panel-body">

						<form role="form" action="<c:url value='j_spring_security_check' />" method="POST">
							<fieldset>
								<div class="row">
									<c:if test="${not empty error}">
										<div class="error-notice">
											<div class="oaerror danger">
												<strong>Error</strong> - ${error}
											</div>
										</div>
									</c:if>
									<c:if test="${not empty msg}">
										<div class="error-notice">
											<div class="oaerror info">
												<strong>Info</strong> - ${msg}
											</div>
										</div>
									</c:if>
								</div>
								<div class="row">
									<div class="center-block">
										<img class="profile-img" src="https://lh5.googleusercontent.com/-b0-k99FZlyE/AAAAAAAAAAI/AAAAAAAAAAA/eu7opA4byxI/photo.jpg?sz=120" alt="">
									</div>
								</div>
								<div class="row">
									<div class="col-sm-12 col-md-10  col-md-offset-1 ">
										<div class="form-group">
											<div class="input-group">
												<span class="input-group-addon"> <i class="glyphicon glyphicon-user"></i>
												</span> <input class="form-control" placeholder="Username" name="username" type="text" autofocus autocomplete="off">
											</div>
										</div>
										<div class="form-group">
											<div class="input-group">
												<span class="input-group-addon"> <i class="glyphicon glyphicon-lock"></i>
												</span> <input class="form-control" placeholder="Password" name="password" type="password" value="" autocomplete="off">
											</div>
										</div>
										<div class="form-group">
											<input type="submit" class="btn btn-lg btn-primary btn-block" value="Sign in">
										</div>

									</div>
								</div>
							</fieldset>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
						</form>
					</div>
					<div class="panel-footer ">
						Need more information! <a href="http://www.bataon.com" onClick=""> Click Here </a>
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
