package com.yuhaojituan.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.model.user.pojos.ApUser;
import com.yuhaojituan.user.mapper.ApUserMapper;
import com.yuhaojituan.user.service.ApUserService;
import org.springframework.stereotype.Service;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements ApUserService {
    
}