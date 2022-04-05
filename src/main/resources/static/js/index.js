$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	const title = $("#recipient-name").val();
	const content = $("#message-text").val();

	$.post(
		CONTEXT_PATH + "/discussPost/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			console.log(data.code);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code === 0) {
					window.location.reload();
				}
			}, 2000);
		}
	)
}