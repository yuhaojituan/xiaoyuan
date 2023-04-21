package com.yuhaojituan.model.comment.vos;

import com.yuhaojituan.model.comment.pojos.ApComment;
import lombok.Data;

@Data
public class ApCommentVo extends ApComment {
    /**
     * 0：点赞
     */
    private Short operation;
}