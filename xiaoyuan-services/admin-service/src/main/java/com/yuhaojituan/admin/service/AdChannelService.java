package com.yuhaojituan.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.admin.dtos.ChannelDTO;
import com.yuhaojituan.model.admin.pojos.AdChannel;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface AdChannelService extends IService<AdChannel> {
    public ResponseResult findByNameAndPage(ChannelDTO dto);
    public ResponseResult insert(AdChannel channel);

    public ResponseResult update(AdChannel adChannel);

    public ResponseResult deleteById(Integer id);
}
