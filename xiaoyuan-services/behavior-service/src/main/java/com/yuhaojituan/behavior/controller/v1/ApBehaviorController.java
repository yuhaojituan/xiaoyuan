package com.yuhaojituan.behavior.controller.v1;

import com.yuhaojituan.behavior.service.ApArticleBehaviorService;
import com.yuhaojituan.behavior.service.ApCollectionBehaviorService;
import com.yuhaojituan.behavior.service.ApLikesBehaviorService;
import com.yuhaojituan.behavior.service.ApReadBehaviorService;
import com.yuhaojituan.model.behavior.dtos.ArticleBehaviorDTO;
import com.yuhaojituan.model.behavior.dtos.CollectionBehaviorDTO;
import com.yuhaojituan.model.behavior.dtos.LikesBehaviorDTO;
import com.yuhaojituan.model.behavior.dtos.ReadBehaviorDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ApBehaviorController {

    @Autowired
    ApLikesBehaviorService apLikesBehaviorService;
    @Autowired
    ApReadBehaviorService apReadBehaviorService;
    @Autowired
    ApCollectionBehaviorService apCollectionBehaviorService;
    @Autowired
    ApArticleBehaviorService apArticleBehaviorService;

    @PostMapping("/likes_behavior")
    public ResponseResult like(@RequestBody LikesBehaviorDTO dto) {
        return apLikesBehaviorService.like(dto);
    }

    @PostMapping("/read_behavior")
    public ResponseResult read(@RequestBody @Validated ReadBehaviorDTO dto) {
        return apReadBehaviorService.readBehavior(dto);
    }

    @PostMapping("/collection_behavior")
    public ResponseResult collect(@RequestBody CollectionBehaviorDTO dto) {
        return apCollectionBehaviorService.collectBehavior(dto);
    }
    @PostMapping("/article/load_article_behavior")
    public ResponseResult behavior(@RequestBody ArticleBehaviorDTO dto) {
        return apArticleBehaviorService.loadArticleBehavior(dto);
    }


}
