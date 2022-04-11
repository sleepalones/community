$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	const btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.post(
			CONTEXT_PATH + "/follow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (map) {
				map = $.parseJSON(map);
				if (map.code === 0) {
					window.location.reload();
				}else {
					alert(map.msg);
				}
			}
		);
		//$(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.post(
			CONTEXT_PATH + "/unfollow",
			{"entityType":3,"entityId":$(btn).prev().val()},
			function (map) {
				map = $.parseJSON(map);
				if (map.code === 0) {
					window.location.reload();
				}else {
					alert(map.msg);
				}
			}
		);
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}