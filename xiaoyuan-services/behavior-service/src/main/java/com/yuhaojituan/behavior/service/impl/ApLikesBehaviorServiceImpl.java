package com.yuhaojituan.behavior.service.impl;


import com.yuhaojituan.behavior.service.ApBehaviorEntryService;
import com.yuhaojituan.behavior.service.ApLikesBehaviorService;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.model.behavior.dtos.LikesBehaviorDTO;
import com.yuhaojituan.model.behavior.pojos.ApBehaviorEntry;
import com.yuhaojituan.model.behavior.pojos.ApLikesBehavior;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.threadlocal.AppThreadLocalUtils;
import com.yuhaojituan.model.user.pojos.ApUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {

    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ResponseResult like(LikesBehaviorDTO dto) {

        ApUser user = AppThreadLocalUtils.getUser();
        //防止下面空指针 get id
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN, "like需要登录");
        }
        if (dto.getArticleId() == null) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "文章id为空");
        }
        if (dto.getOperation() != 0 && dto.getOperation() != 1) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "operation只能是0和1");
        }
        //1.获取entry
        ApBehaviorEntry entry = apBehaviorEntryService.findByUserIdOrEquipmentId(user.getId(), dto.getEquipmentId());

        //判断操作
        Query query = Query.query(Criteria.where("articleId").is(dto.getArticleId()).and("entryId").is(entry.getId()));
        if (dto.getOperation() == 0) {
            //like
            ApLikesBehavior one = mongoTemplate.findOne(query, ApLikesBehavior.class);
            if (one != null) {
                CustException.cust(AppHttpCodeEnum.DATA_EXIST, "已经喜欢过了");
            }
            ApLikesBehavior like = new ApLikesBehavior();
            like.setArticleId(dto.getArticleId());
            like.setEntryId(entry.getId());
            like.setCreatedTime(new Date());
            mongoTemplate.save(like);
            return ResponseResult.okResult();
        } else {
            mongoTemplate.remove(query, ApLikesBehavior.class);
            return ResponseResult.okResult();
        }


    }
}
