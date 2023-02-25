package com.yuhaojituan.article.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.article.mapper.ApArticleConfigMapper;
import com.yuhaojituan.article.service.ApArticleConfigService;
import com.yuhaojituan.model.article.pojos.ApArticleConfig;
import org.springframework.stereotype.Service;

@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
}