package com.brotherming.community.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.brotherming.community.entity.Message;
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
public interface MessageMapper extends BaseMapper<Message> {

    @Select({
            "select id,from_id,to_id,conversation_id,content,status,create_time ",
            "from message where id in ( ",
                "select max(id) from message ",
                "where status != 2 ",
                "and from_id != 1 ",
                "and (from_id = #{userId} or to_id = #{userId}) ",
                "group by conversation_id ",
            ") ",
            "order by id desc ",
            "limit #{current}, #{limit}"
    })
    List<Message> selectConversations(int userId, int current, int limit);

    @Select({
            "select count(m.maxid) from (",
                "select max(id) as maxid from message ",
                "where status != 2",
                "and from_id != 1",
                "and (from_id = #{userId} or to_id = #{userId}) ",
                "group by conversation_id ",
            ") as m"
    })
    int selectConversationCount(int userId);
}
