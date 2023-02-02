package com.yuhaojituan.admin.controller.v1;

import com.yuhaojituan.admin.service.AdChannelService;
import com.yuhaojituan.model.admin.dtos.ChannelDTO;
import com.yuhaojituan.model.admin.pojos.AdChannel;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
public class AdChannelController {
    @Autowired
    private AdChannelService channelService;

    @PostMapping("/list")
    public ResponseResult findByNameAndPage(@RequestBody ChannelDTO dto) {
        return channelService.findByNameAndPage(dto);
    }

    @ApiOperation("频道新增")
    @PostMapping("/save")
    public ResponseResult insert(@RequestBody AdChannel channel) {
        return channelService.insert(channel);
    }

    @ApiOperation("频道修改")
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel adChannel) {
        return channelService.update(adChannel);
    }

    @ApiOperation("根据频道ID删除")
    @GetMapping("/del/{id}")
    public ResponseResult deleteById(@PathVariable("id") Integer id) {
        return channelService.deleteById(id);
    }

}