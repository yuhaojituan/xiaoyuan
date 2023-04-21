package com.yuhaojituan.model.comment.dots;

import lombok.Data;

@Data
public class CommentLikeDTO {

    /**
     * 评论id
     */
    private String commentId;

    /**
     * 0：点赞
     * 1：取消点赞
     */
    private Short operation;
}