package com.yuhaojituan.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.dtos.WmUserDTO;
import com.yuhaojituan.model.wemedia.pojos.WmUser;

public interface WmUserService extends IService<WmUser> {
    /**
     * 登录
     * @param dto
     * @return
     */
    public ResponseResult login(WmUserDTO dto);
}