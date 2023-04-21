package com.yuhaojituan.behavior.service.impl;

import com.yuhaojituan.behavior.service.ApBehaviorEntryService;
import com.yuhaojituan.behavior.service.ApReadBehaviorService;
import com.yuhaojituan.model.behavior.dtos.ReadBehaviorDTO;
import com.yuhaojituan.model.behavior.pojos.ApBehaviorEntry;
import com.yuhaojituan.model.behavior.pojos.ApReadBehavior;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.threadlocal.AppThreadLocalUtils;
import com.yuhaojituan.model.user.pojos.ApUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ApReadBehaviorServiceImpl implements ApReadBehaviorService {

    @Autowired
    ApBehaviorEntryService apBehaviorEntryService;
    @Autowired
    MongoTemplate mongoTemplate;

    //通常一个service/mapper 对应一张表 所以不建议吧这些service合为一个  但是controller可以
    @Override
    public ResponseResult readBehavior(ReadBehaviorDTO dto) {
        //1.注解已经参数校验
        //2.获取entry
        ApUser user = AppThreadLocalUtils.getUser();
        Integer id = null;
        if (user != null) {
            //登录了
            id = user.getId();
        }
        ApBehaviorEntry entry = apBehaviorEntryService.findByUserIdOrEquipmentId(id, dto.getEquipmentId());
        //3.判断是否有阅读记录
        Query query = Query.query(Criteria.where("articleId").is(dto.getArticleId()).and("entryId").is(entry.getId()));
        ApReadBehavior one = mongoTemplate.findOne(query, ApReadBehavior.class);

        if (one!=null){
            //有记录  count++
            Short count = one.getCount();
            Update update = new Update();
            update.set("count",++count).set("updatedTime",new Date());
            mongoTemplate.updateFirst(query, update,ApReadBehavior.class);
            return ResponseResult.okResult();
        }
        //没记录
        ApReadBehavior readBehavior = new ApReadBehavior();
        readBehavior.setCount(Short.valueOf("1"));
        readBehavior.setArticleId(dto.getArticleId());
        readBehavior.setEntryId(entry.getId());
        readBehavior.setCreatedTime(new Date());
        readBehavior.setUpdatedTime(new Date());
        mongoTemplate.save(readBehavior);

        return ResponseResult.okResult();
    }
}
