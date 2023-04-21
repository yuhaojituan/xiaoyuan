package com.yuhaojituan.comment.service;

import com.yuhaojituan.model.comment.dots.CommentDTO;
import com.yuhaojituan.model.comment.dots.CommentLikeDTO;
import com.yuhaojituan.model.comment.dots.CommentSaveDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface CommentService {
    /**
     * 保存评论
     * @return
     */
    public ResponseResult saveComment(CommentSaveDTO dto);
    /**
     * 点赞评论
     * @param dto
     * @return
     */
    public ResponseResult like(CommentLikeDTO dto);
    /**
     * 根据文章id查询评论列表
     * @return
     */
    public ResponseResult findByArticleId(CommentDTO dto);
}