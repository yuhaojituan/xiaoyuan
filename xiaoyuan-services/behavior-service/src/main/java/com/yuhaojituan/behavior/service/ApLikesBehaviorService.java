package com.yuhaojituan.behavior.service;

import com.yuhaojituan.model.behavior.dtos.LikesBehaviorDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorService {
    /**
     * 点赞或取消点赞
     * @param dto
     * @return
     */
	public ResponseResult like(LikesBehaviorDTO dto);
}