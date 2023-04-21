package com.yuhaojituan.model.comment.dots;

import lombok.Data;

import java.util.Date;

@Data
public class CommentDTO {
    private Long articleId;
    // 最小时间
    private Date minDate;
    //是否是首页
    private Short index;
    // 每页条数
    private Integer size;
}