package com.yuhaojituan.article.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.article.dtos.ArticleHomeDTO;
import com.yuhaojituan.model.article.pojos.ApArticle;
import com.yuhaojituan.model.common.dtos.ResponseResult;

public interface ApArticleService extends IService<ApArticle> {

    /**
     * 保存或修改文章
     */
    public void publishArticle(Integer newsId);

    /**
     * 根据参数加载文章列表
     */
    ResponseResult load(Short loadtype, ArticleHomeDTO dto);
}