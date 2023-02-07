package com.yuhaojituan.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import com.yuhaojituan.wemedia.mapper.WmUserMapper;
import com.yuhaojituan.wemedia.service.WmUserService;
import org.springframework.stereotype.Service;

@Service
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
}