$(document).ready(function() {

	$.fn.editable.defaults.placement = 'bottom';

	$("div.account .name").click(function(evt) {
		var accountId = $(evt.target).closest("div.account").attr("accountId");
		$.get( "/server/list/" + accountId, function( data ) {
		  $( "#servers" ).html(data);
		  initServerList();
		}, "html" );
	});

	$("#listOrphans").click(function() {
		$.get( "/server/orphan", function( data ) {
		  $( "#servers" ).html(data);
		  initServerList();
		}, "html" );
	});
	
	function initServerList() {
	  $('#servers a.servername').editable({
	  	mode: "inline"
	  });
	  $('#servers a.account').editable({
	  	mode: "inline"
	  });
	  $('#servers a.expirationdate').editable();
	}
});