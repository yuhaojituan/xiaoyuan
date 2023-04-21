package com.yuhaojituan.model.behavior.dtos;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

@Data
public class ArticleBehaviorDTO {
    // 设备ID
    Integer equipmentId;
    // 文章ID 
    @JsonSerialize(using = ToStringSerializer.class)
    Long articleId;
    // 作者ID
    Integer authorId;
    // 作者对应的apuserid
    Integer authorApUserId;
}