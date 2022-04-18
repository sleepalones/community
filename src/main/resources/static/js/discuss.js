$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
})

function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {
            "entityType":entityType,
            "entityId":entityId,
            "entityUserId":entityUserId,
            "postId":postId
        },
        function (map) {
            map = $.parseJSON(map);
            if (map.code === 0){
                $(btn).children("i").text(map.data.likeCount);
                $(btn).children("b").text(map.data.likeStatus===1?'已赞':'赞')
            }else {
                alert(map.msg);
            }
        }
    )
}

//置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discussPost/top",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    );
}

//加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discussPost/wonderful",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    );
}

//删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discussPost/delete",
        {"id":$("#postId").val()},
        function (data) {
            data = $.parseJSON(data);
            if (data.code === 0) {
                location.href = CONTEXT_PATH + "/index"
            }else {
                alert(data.msg);
            }
        }
    );
}
