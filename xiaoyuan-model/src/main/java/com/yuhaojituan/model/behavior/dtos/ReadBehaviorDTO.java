package com.yuhaojituan.model.behavior.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReadBehaviorDTO {
    // 设备ID
    Integer equipmentId;
    // 文章、动态、评论等ID
    @NotNull
    Long articleId;
    /**
     * 阅读次数
     * 不使用
     */
    Short count;
}