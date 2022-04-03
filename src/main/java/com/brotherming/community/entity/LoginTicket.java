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
public class LoginTicket implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private String ticket;

    /**
     * 0-有效; 1-无效;
     */
    private Integer status;

    private LocalDateTime expired;


}
