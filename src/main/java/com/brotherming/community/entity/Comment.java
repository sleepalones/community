package com.brotherming.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author brotherming
 * @since 2022-04-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    /**
     * 评论的目标类型
     */
    private Integer entityType;

    /**
     * 评论目标类型的ID
     */
    private Integer entityId;

    /**
     * 回复某个人，指定的ID
     */
    private Integer targetId;

    private String content;

    private Integer status;

    private LocalDateTime createTime;


}
