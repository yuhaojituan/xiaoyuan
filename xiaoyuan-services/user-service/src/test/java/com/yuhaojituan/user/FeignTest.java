package com.yuhaojituan.user;


import com.yuhaojituan.feigns.ArticleFeign;
import com.yuhaojituan.feigns.WemediaFeign;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

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

    @Test
    public void maoPao() {
        int[] a = {1, 3, 5, 7, 9, 2, 4, 6, 8, 10};

        for (int i=0;i<a.length-1;i++) {
            for (int j = 0; j < a.length - 1-i; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j + 1];
                    a[j+1]=a[j];
                    a[j]=temp;
                }
            }
        }

    }

}
