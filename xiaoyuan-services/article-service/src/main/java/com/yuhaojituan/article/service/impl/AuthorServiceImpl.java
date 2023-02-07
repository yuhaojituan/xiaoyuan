package com.yuhaojituan.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.article.mapper.AuthorMapper;
import com.yuhaojituan.article.service.AuthorService;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import org.springframework.stereotype.Service;

@Service
public class AuthorServiceImpl extends ServiceImpl<AuthorMapper, ApAuthor> implements AuthorService {
}