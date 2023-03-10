package com.yuhaojituan.model.admin.dtos;

import com.yuhaojituan.model.common.dtos.PageRequestDTO;
import lombok.Data;

@Data
public class ChannelDTO extends PageRequestDTO {
    /**
     * 频道名称
     */
    private String name;
    /**
     * 频道状态
     */
    private Integer status;
}