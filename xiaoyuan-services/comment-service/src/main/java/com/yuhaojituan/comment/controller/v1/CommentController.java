package com.yuhaojituan.comment.controller.v1;

import com.yuhaojituan.comment.service.CommentService;
import com.yuhaojituan.model.comment.dots.CommentDTO;
import com.yuhaojituan.model.comment.dots.CommentLikeDTO;
import com.yuhaojituan.model.comment.dots.CommentSaveDTO;
import com.yuhaojituan.model.comment.pojos.ApComment;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/save")
    public ResponseResult save(@RequestBody CommentSaveDTO dto) {
        return commentService.saveComment(dto);
    }

    @PostMapping("/like")
    public ResponseResult like(@RequestBody CommentLikeDTO dto) {
        return commentService.like(dto);
    }

    @PostMapping("/load")
    public ResponseResult load(@RequestBody CommentDTO dto) {
        return commentService.findByArticleId(dto);
    }
}
