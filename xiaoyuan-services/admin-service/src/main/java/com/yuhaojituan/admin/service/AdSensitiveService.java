package com.yuhaojituan.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.admin.dtos.SensitiveDTO;
import com.yuhaojituan.model.admin.pojos.AdSensitive;
import com.yuhaojituan.model.common.dtos.ResponseResult;

import java.util.List;

public interface AdSensitiveService extends IService<AdSensitive> {

    /**
     * 查询敏感词内容列表
     * @return
     */
    public ResponseResult<List<String>> selectAllSensitives();

    /**
     * 查询敏感词列表
     * @param dto
     * @return
     */
    public ResponseResult list(SensitiveDTO dto);

    /**
     * 新增
     * @param adSensitive
     * @return
     */
    public ResponseResult insert(AdSensitive adSensitive);

    /**
     * 修改
     * @param adSensitive
     * @return
     */
    public ResponseResult update(AdSensitive adSensitive);

    /**
     * 删除
     * @param id
     * @return
     */
    public ResponseResult delete(Integer id);
}