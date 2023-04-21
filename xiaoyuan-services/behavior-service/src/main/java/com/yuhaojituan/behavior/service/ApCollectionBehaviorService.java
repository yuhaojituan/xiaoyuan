package com.yuhaojituan.behavior.service;

import com.yuhaojituan.model.behavior.dtos.CollectionBehaviorDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface ApCollectionBehaviorService {
    /**
     * 收藏 取消收藏
     * @param dto
     * @return
     */
    ResponseResult collectBehavior(CollectionBehaviorDTO dto);
}