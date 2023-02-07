package com.yuhaojituan.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.admin.dtos.AdUserDTO;
import com.yuhaojituan.model.admin.pojos.AdUser;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface AdUserService extends IService<AdUser> {
    /**
     * 登录功能
     * @param DTO
     * @return
     */
    ResponseResult login(AdUserDTO DTO);
}