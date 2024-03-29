package com.yuhaojituan.article.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuhaojituan.model.article.dtos.ArticleHomeDTO;
import com.yuhaojituan.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    /**
     * 查询文章列表
     *
     * @param dto
     * @param type 0：加载更多   1：加载最新
     * @return
     */
    public List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDTO dto, @Param("type") Short type);
}