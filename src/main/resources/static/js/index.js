$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//发送 ajax 请求之前，将 CSRF 令牌设置到请求的消息头中
	/*const token = $("meta[name='_csrf']").attr("content");
	const header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e, xhr, options) {
		xhr.setRequestHeader(header,token);
	})*/

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
