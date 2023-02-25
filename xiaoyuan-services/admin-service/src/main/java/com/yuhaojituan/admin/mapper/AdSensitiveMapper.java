package com.yuhaojituan.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuhaojituan.model.admin.pojos.AdSensitive;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AdSensitiveMapper extends BaseMapper<AdSensitive> {
    @Select("select sensitives from ad_sensitive")
    List<String> findAllSensitives();

}