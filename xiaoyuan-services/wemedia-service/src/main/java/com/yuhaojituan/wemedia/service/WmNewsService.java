package com.yuhaojituan.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.wemedia.dtos.WmNewsDTO;
import com.yuhaojituan.model.wemedia.dtos.WmNewsPageReqDTO;
import com.yuhaojituan.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {
    /**
     * 查询所有自媒体文章
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDTO dto);

    /**
     * 自媒体文章发布
     */
    ResponseResult submitNews(WmNewsDTO dto);

    /**
     * 上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDTO dto);
}