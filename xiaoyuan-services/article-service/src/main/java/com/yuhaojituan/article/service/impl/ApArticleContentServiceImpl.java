package com.yuhaojituan.article.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.article.mapper.ApArticleContentMapper;
import com.yuhaojituan.article.service.ApArticleContentService;
import com.yuhaojituan.model.article.pojos.ApArticleContent;
import org.springframework.stereotype.Service;

@Service
public class ApArticleContentServiceImpl extends ServiceImpl<ApArticleContentMapper, ApArticleContent> implements ApArticleContentService {

}