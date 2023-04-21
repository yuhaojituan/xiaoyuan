package com.yuhaojituan.user.service;

import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.user.dtos.UserRelationDTO;

public interface ApUserRelationService {
    /**
     * 用户关注/取消关注
     * @param dto
     * @return
     */
    public ResponseResult follow(UserRelationDTO dto);
}