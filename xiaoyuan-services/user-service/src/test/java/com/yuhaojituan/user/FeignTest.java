package com.yuhaojituan.user;


import com.yuhaojituan.feigns.ArticleFeign;
import com.yuhaojituan.feigns.WemediaFeign;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FeignTest {

    @Autowired
    WemediaFeign wemediaFeign;

    //不知道为什么这了client为null  耽误了很长时间  对比了视频一摸一样
    @Test
    public void findWmUser() {
        ResponseResult<WmUser> admin = wemediaFeign.findByName("admin");
        System.out.println(admin);
    }

    @Autowired
    ArticleFeign articleFeign;

    @Test
    public void articleFeign() {
        ResponseResult<ApAuthor> byUserId = articleFeign.findByUserId(1);
        System.out.println(byUserId);
    }

}
