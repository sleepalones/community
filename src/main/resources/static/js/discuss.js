function like(btn,entityType,entityId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId},
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