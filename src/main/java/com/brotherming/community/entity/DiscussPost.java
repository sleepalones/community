package com.brotherming.community.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

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
@Document(indexName = "discusspost")
public class DiscussPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Id
    private Integer id;

    @Field(type = FieldType.Text)
    private String userId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String content;

    /**
     * 0-普通; 1-置顶;
     */
    @Field(type = FieldType.Integer)
    private Integer type;

    /**
     * 0-正常; 1-精华; 2-拉黑;
     */
    @Field(type = FieldType.Integer)
    private Integer status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Field(type = FieldType.Date, name = "create_time", format = DateFormat.date_optional_time)
    private LocalDateTime createTime;

    @Field(type = FieldType.Integer)
    private Integer commentCount;

    @Field(type = FieldType.Double)
    private Double score;


}
