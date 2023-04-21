package com.yuhaojituan.behavior.service.impl;

import com.yuhaojituan.behavior.service.ApArticleBehaviorService;
import com.yuhaojituan.behavior.service.ApBehaviorEntryService;
import com.yuhaojituan.common.constants.user.UserRelationConstants;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.model.behavior.dtos.ArticleBehaviorDTO;
import com.yuhaojituan.model.behavior.pojos.ApBehaviorEntry;
import com.yuhaojituan.model.behavior.pojos.ApCollection;
import com.yuhaojituan.model.behavior.pojos.ApLikesBehavior;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.threadlocal.AppThreadLocalUtils;
import com.yuhaojituan.model.user.pojos.ApUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApArticleBehaviorServiceImpl implements ApArticleBehaviorService {

    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public ResponseResult loadArticleBehavior(ArticleBehaviorDTO dto) {

        HashMap<String, Boolean> map = new HashMap<>();
        map.put("isfollow", false);
        map.put("islike", false);
        map.put("iscollection", false);

        ApUser user = AppThreadLocalUtils.getUser();
        if (user == null) {
            //未登录全false
            return ResponseResult.okResult(map);
        }
        ApBehaviorEntry entry = apBehaviorEntryService.findByUserIdOrEquipmentId(user.getId(), null);
        Query query = Query.query(Criteria.where("articleId").is(dto.getArticleId()).and("entryId").is(entry.getId()));
        ApLikesBehavior likeOne = mongoTemplate.findOne(query, ApLikesBehavior.class);
        if (likeOne != null) {
            map.replace("islike", true);
        }
        ApCollection collectionOne = mongoTemplate.findOne(query, ApCollection.class);
        if (collectionOne != null) {
            map.replace("iscollection", true);
        }
        //查看是否关注
        Double score = redisTemplate.opsForZSet()
                .score(UserRelationConstants.FOLLOW_LIST + user.getId(), String.valueOf(dto.getAuthorApUserId()));
        if (score != null) {
            map.replace("isfollow", true);
        }
        return ResponseResult.okResult(map);
    }
}
