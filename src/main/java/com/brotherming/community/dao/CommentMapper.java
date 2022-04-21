package com.brotherming.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brotherming.community.entity.Comment;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
public interface CommentMapper extends BaseMapper<Comment> {

    @Select({
            "select count(m.maxid) from (",
                "select max(id) as maxid from comment ",
                "where status != 2",
                "and entity_type = 1",
                "and user_id = #{userId}",
                "group by entity_id ",
            ") as m"
    })
    int selectCommentCountByUserId(int userId);


    @Select({
            "select id,user_id,entity_type,entity_id,target_id,content,status,create_time",
            "from comment where id in ( ",
                "select max(id) from comment ",
                "where status != 2",
                "and entity_type = 1",
                "and user_id = #{userId}",
                "group by entity_id ",
            ") ",
            "order by id desc ",
            "limit #{offset}, #{limit}"
    })
    List<Comment> selectNewCommentByUserId(int offset, int limit, int userId);

}
