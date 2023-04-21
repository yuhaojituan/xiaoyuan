package com.yuhaojituan.behavior.service.impl;

import com.yuhaojituan.behavior.service.ApBehaviorEntryService;
import com.yuhaojituan.model.behavior.pojos.ApBehaviorEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Date;


//被所有行为服务调用的服务
//操作前  获取实体
@Service
public class ApBehaviorEntryServiceImpl implements ApBehaviorEntryService {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer equipmentId) {

        if (userId != null) {
            //登录了
            Query query = Query.query(Criteria.where("refId").is(userId).and("type").is(1));
            ApBehaviorEntry entry = mongoTemplate.findOne(query, ApBehaviorEntry.class);
            if (entry == null) {
                ApBehaviorEntry newEntry = new ApBehaviorEntry();
                newEntry.setType(ApBehaviorEntry.Type.USER.getCode());
                newEntry.setRefId(userId);
                newEntry.setCreatedTime(new Date());
                ApBehaviorEntry save = mongoTemplate.save(newEntry);
                return save;
            }
            return entry;
        }

        if (equipmentId != null) {
            Query query = Query.query(Criteria.where("refId").is(equipmentId).and("type").is(0));
            ApBehaviorEntry entry = mongoTemplate.findOne(query, ApBehaviorEntry.class);
            if (entry == null) {
                ApBehaviorEntry newEntry = new ApBehaviorEntry();
                newEntry.setType(ApBehaviorEntry.Type.EQUIPMENT.getCode());
                newEntry.setRefId(equipmentId);
                ApBehaviorEntry save = mongoTemplate.save(newEntry);
                return save;
            }
            return entry;
        }
        return null;
    }
}
