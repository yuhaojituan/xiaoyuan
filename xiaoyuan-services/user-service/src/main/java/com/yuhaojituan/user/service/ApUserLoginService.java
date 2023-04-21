package com.yuhaojituan.user.service;

import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.user.dtos.LoginDTO;

public interface ApUserLoginService {
    /**
     * app端登录
     * @param dto
     * @return
     */
    public ResponseResult login(LoginDTO dto);
}