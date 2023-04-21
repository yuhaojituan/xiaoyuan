package com.yuhaojituan.comment.service;

import com.yuhaojituan.model.comment.pojos.ApComment;

public interface CommentHotService {
    /**
     * 查找热点评论
     */
    public void hotCommentExecutor(ApComment apComment);
}