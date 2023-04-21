package com.yuhaojituan.behavior.service;

import com.yuhaojituan.model.behavior.dtos.ReadBehaviorDTO;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface ApReadBehaviorService{
    /**
     * 记录阅读行为
     * @param dto
     * @return
     */
    ResponseResult readBehavior(ReadBehaviorDTO dto);
}