package com.yuhaojituan.user.service.impl;

import com.yuhaojituan.common.constants.user.UserRelationConstants;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.threadlocal.AppThreadLocalUtils;
import com.yuhaojituan.model.user.dtos.UserRelationDTO;
import com.yuhaojituan.model.user.pojos.ApUser;
import com.yuhaojituan.user.service.ApUserRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApUserRelationServiceImpl implements ApUserRelationService {
    @Autowired
    StringRedisTemplate redisTemplate;
    @Override
    public ResponseResult follow(UserRelationDTO dto) {
        // 1. 校验参数    authorApUserId   必须登录   operation 0  1
        if(dto.getAuthorApUserId() == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"作者对应的userId不存在");
        }
        Short operation = dto.getOperation();
        if(operation == null || (operation.intValue()!=0 && operation.intValue()!=1)){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"关注类型错误");
        }
        ApUser user = AppThreadLocalUtils.getUser();
        if(user == null){
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN);
        }
        Integer loginId = user.getId();
        Integer followId = dto.getAuthorApUserId();
        // 判断 自己不可以关注自己
        if(loginId.equals(followId)){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW,"不可以自己关注自己哦~");
        }
        //    校验之前有没有关注过   zscore
        //    参数1： key   参数2: 要查询集合元素
        Double score = redisTemplate.opsForZSet()
                .score(UserRelationConstants.FOLLOW_LIST + loginId, String.valueOf(followId));
        if(operation.intValue() == 0&&score!=null){
            CustException.cust(AppHttpCodeEnum.DATA_EXIST,"您已关注，请勿重复关注");
        }
        try {
            // 2. 判断operation 是0  是1
            if(operation.intValue() == 0){
                //    没有关注过    zadd  follow:我的id   作者id
                //                             参数1: key  参数2 集合元素  参数3: score
                redisTemplate.opsForZSet().add(UserRelationConstants.FOLLOW_LIST + loginId,String.valueOf(followId),System.currentTimeMillis());
                //                zadd  fans:作者id    我的id
                redisTemplate.opsForZSet().add(UserRelationConstants.FANS_LIST + followId,String.valueOf(loginId),System.currentTimeMillis());
            }else {
                // 2.2  是1  取关
                //     zrem  follow:我的id   作者id
                redisTemplate.opsForZSet().remove(UserRelationConstants.FOLLOW_LIST + loginId,String.valueOf(followId));
                //     zrem  fans:作者id    我的id
                redisTemplate.opsForZSet().remove(UserRelationConstants.FANS_LIST + followId,String.valueOf(loginId));
            }
        }catch (Exception e){
            e.printStackTrace();
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR);
        }
        return ResponseResult.okResult();
    }
}