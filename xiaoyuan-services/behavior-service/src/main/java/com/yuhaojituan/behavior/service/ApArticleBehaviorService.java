package com.yuhaojituan.behavior.service;

import com.yuhaojituan.model.behavior.dtos.ArticleBehaviorDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface ApArticleBehaviorService {

    /**
     * 加载文章详情 数据回显
     * @param dto
     * @return
     */
    public ResponseResult loadArticleBehavior(ArticleBehaviorDTO dto);
}