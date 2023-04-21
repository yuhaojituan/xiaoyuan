package com.yuhaojituan.feigns;

import com.yuhaojituan.config.YuhaojituanFeignAutoConfiguration;
import com.yuhaojituan.feigns.fallback.UserFeignFallback;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.user.pojos.ApUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "xiaoyuan-user", // 调用服务
        fallbackFactory = UserFeignFallback.class, // 服务降级
        configuration = YuhaojituanFeignAutoConfiguration.class) // feign日志配置
public interface UserFeign {
    @GetMapping("/api/v1/user/{id}")
    ResponseResult<ApUser> findUserById(@PathVariable("id") Integer id);
}