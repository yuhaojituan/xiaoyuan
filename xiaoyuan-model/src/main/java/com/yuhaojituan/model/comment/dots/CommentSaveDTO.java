package com.yuhaojituan.model.comment.dots;

import lombok.Data;

@Data
public class CommentSaveDTO {
    /**
     * 文章id
     */
    private Long articleId;
    /**
     * 评论内容
     */
    private String content;
}