package com.yuhaojituan.model.admin.dtos;

import com.yuhaojituan.model.common.dtos.PageRequestDTO;
import lombok.Data;

@Data
public class SensitiveDTO extends PageRequestDTO {
    /**
     * 敏感词名称
     */
    private String name;
}