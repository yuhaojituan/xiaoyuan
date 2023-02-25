package com.yuhaojituan.article;

import com.yuhaojituan.article.service.ApArticleService;
import com.yuhaojituan.feigns.WemediaFeign;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.pojos.WmNews;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ArticleTest {
    @Autowired
    ApArticleService apArticleService;
    @Test
    public void publishArticle(){
        apArticleService.publishArticle(6270);
    }

    @Autowired
    WemediaFeign wemediaFeign;
    @Test
    public void testWmFeign(){
        ResponseResult<WmNews> wmNewsById = wemediaFeign.findWmNewsById(6270);
        System.out.println(wmNewsById);

    }
}