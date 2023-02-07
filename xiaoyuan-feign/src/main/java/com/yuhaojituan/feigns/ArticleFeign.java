package com.yuhaojituan.feigns;

import com.yuhaojituan.config.YuhaojituanFeignAutoConfiguration;
import com.yuhaojituan.feigns.fallback.ArticleFeignFallback;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "xiaoyuan-article",
        fallbackFactory = ArticleFeignFallback.class,
        configuration = YuhaojituanFeignAutoConfiguration.class
)
public interface ArticleFeign {
    @GetMapping("/api/v1/author/findByUserId/{userId}")
    ResponseResult<ApAuthor> findByUserId(@PathVariable("userId") Integer userId);

    @PostMapping("/api/v1/author/save")
    ResponseResult save(@RequestBody ApAuthor apAuthor);
}