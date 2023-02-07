package com.yuhaojituan.feigns;

import com.yuhaojituan.config.YuhaojituanFeignAutoConfiguration;
import com.yuhaojituan.feigns.fallback.WemediaFeignFallback;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "xiaoyuan-wemedia",
        fallbackFactory = WemediaFeignFallback.class,
        configuration = YuhaojituanFeignAutoConfiguration.class
)
public interface WemediaFeign {
    @PostMapping("/api/v1/user/save")
    public ResponseResult<WmUser> save(@RequestBody WmUser wmUser);

    @GetMapping("/api/v1/user/findByName/{name}")
    public ResponseResult<WmUser> findByName(@PathVariable("name") String name);
}