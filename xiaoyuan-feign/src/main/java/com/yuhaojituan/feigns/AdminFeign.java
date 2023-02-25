package com.yuhaojituan.feigns;

import com.yuhaojituan.config.YuhaojituanFeignAutoConfiguration;
import com.yuhaojituan.feigns.fallback.AdminFeignFallback;
import com.yuhaojituan.feigns.fallback.ArticleFeignFallback;
import com.yuhaojituan.model.admin.pojos.AdChannel;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "xiaoyuan-admin",
        fallbackFactory = AdminFeignFallback.class,
        configuration = YuhaojituanFeignAutoConfiguration.class
)
public interface AdminFeign {
    // 查询敏感词内容列表
    @PostMapping("/api/v1/sensitive/sensitives")
    public ResponseResult<List<String>> sensitives();

    @GetMapping("/api/v1/channel/one/{id}")
    public ResponseResult<AdChannel> findOne(@PathVariable Integer id);


}