package com.yuhaojituan.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.common.constants.message.NewsAutoScanConstants;
import com.yuhaojituan.common.constants.message.NewsUpOrDownConstants;
import com.yuhaojituan.common.constants.wemiedia.WemediaConstants;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.model.common.dtos.PageResponseResult;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.threadlocal.WmThreadLocalUtils;
import com.yuhaojituan.model.wemedia.dtos.WmNewsDTO;
import com.yuhaojituan.model.wemedia.dtos.WmNewsPageReqDTO;
import com.yuhaojituan.model.wemedia.pojos.WmNews;
import com.yuhaojituan.model.wemedia.pojos.WmNewsMaterial;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import com.yuhaojituan.wemedia.mapper.WmMaterialMapper;
import com.yuhaojituan.wemedia.mapper.WmNewsMapper;
import com.yuhaojituan.wemedia.mapper.WmNewsMaterialMapper;
import com.yuhaojituan.wemedia.service.WmNewsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Value("${file.oss.web-site}")
    String webSite;

    /**
     * 查询所有自媒体文章
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDTO dto) {
        //1 参数检查
        if (dto == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();
        //2 条件封装执行查询
        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        // 文章标题模糊查询
        wrapper.like(StringUtils.isNotBlank(dto.getKeyword()), WmNews::getTitle, dto.getKeyword());
        // 频道id
        wrapper.eq(dto.getChannelId() != null, WmNews::getChannelId, dto.getChannelId());
        // 文章状态
        wrapper.eq(dto.getStatus() != null, WmNews::getStatus, dto.getStatus());
        // 发布时间 >= 开始时间
        wrapper.ge(dto.getBeginPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate());
        // 发布时间 <= 开始时间
        wrapper.le(dto.getEndPubDate() != null, WmNews::getPublishTime, dto.getBeginPubDate());
        // 当前自媒体人文章
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN);
        }
        wrapper.eq(WmNews::getUserId, user.getId());
        // 按照创建日期倒序
        wrapper.orderByDesc(WmNews::getCreatedTime);
        // 分页条件构建
        Page<WmNews> page = new Page<>(dto.getPage(), dto.getSize());
        //3 执行查询
        IPage<WmNews> pageResult = page(page, wrapper);
        //4 返回封装查询结果
        PageResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(), pageResult.getTotal());
        result.setData(pageResult.getRecords());
        // 处理文章图片 
        result.setHost(webSite);
        return result;
    }


    //该功能为保存、修改（是否有id）、保存草稿的共用方法
    @Override
    public ResponseResult submitNews(WmNewsDTO dto) {
        // 1 参数校验
        if (StringUtils.isBlank(dto.getContent())) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 校验是否登陆
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NEED_LOGIN);
        }
        // 2 保存或修改文章
        WmNews wmNews = new WmNews();
        // 将dto参数里面的值设置到wmNews
        BeanUtils.copyProperties(dto, wmNews);
        //如果文章布局是自动，需要设置为null
        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            wmNews.setType(null);
        }
        // 处理dto参数 images封面集合 转换成 字符串  TODO 这里看前端传来的 images是否包含website
        String images = imageListToStr(dto.getImages(), webSite);
        wmNews.setImages(images);
        wmNews.setUserId(user.getId());
        saveWmNews(wmNews);
        // 如果是草稿  直接返回
        if (WemediaConstants.WM_NEWS_DRAFT_STATUS.equals(dto.getStatus())) {
            return ResponseResult.okResult();
        }
        //抽取文章中关联的图片路径
        List<String> materials = parseContentImages(dto.getContent());
        //关联文章内容中的图片和素材关系
        if (!CollectionUtils.isEmpty(materials)) {
            saveRelativeInfo(materials, wmNews.getId(), WemediaConstants.WM_CONTENT_REFERENCE);
        }
        //关联文章封面中的图片和素材关系  封面可能是选择自动或者是无图
        saveRelativeInfoForCover(dto, materials, wmNews);


        //发送到待审核队列
        rabbitTemplate.convertAndSend(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_QUEUE, wmNews.getId());


        return ResponseResult.okResult();
    }

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 图片列表转字符串，并去除图片前缀
     */
    private String imageListToStr(List<String> images, String webSite) {
        return images.stream()  // 获取流
                .map((url) -> url.replace(webSite, ""))  // 对流数据的中间操作
                .collect(Collectors.joining(","));
    }

    @Autowired
    WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 保存或修改文章
     */
    private void saveWmNews(WmNews wmNews) {
        wmNews.setCreatedTime(new Date());
        wmNews.setUserId(WmThreadLocalUtils.getUser().getId());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable(WemediaConstants.WM_NEWS_UP); // 上架
        if (wmNews.getId() == null) { // 保存操作
            save(wmNews);
        } else {  // 修改
            // 当前文章 和 素材关系表数据删除
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery()
                    .eq(WmNewsMaterial::getNewsId, wmNews.getId()));
            updateById(wmNews);
        }
    }

    /**
     * 抽取文章内容中 所引用的所有图片
     */
    private List<String> parseContentImages(String content) {
        List<Map> contents = JSON.parseArray(content, Map.class);
        // 遍历文章内容   将所有 type为image的 value获取出来  去除前缀路径
        return contents.stream()
                // 过滤type=image所有的集合
                .filter(map -> map.get("type").equals(WemediaConstants.WM_NEWS_TYPE_IMAGE))
                // 获取到image下的value  图片url
                .map(x -> (String) x.get("value"))
                // 图片url去除前缀
                .map(url -> url.replace(webSite, "").replace(" ", ""))
                // 去除重复的路径
                .distinct()
                // stream 转成list集合
                .collect(Collectors.toList());
    }

    @Autowired
    WmMaterialMapper wmMaterialMapper;

    /**
     * 保存素材和文章关系
     */
    private void saveRelativeInfo(List<String> urls, Integer newsId, Short type) {
        //1 查询文章内容中的图片对应的素材ID
        List<Integer> ids = wmMaterialMapper.selectRelationsIds(urls,
                WmThreadLocalUtils.getUser().getId());
        //2 判断素材是否缺失
        if (CollectionUtils.isEmpty(ids) || ids.size() < urls.size()) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "相关素材缺失,保存文章失败");
        }
        //3 保存素材关系
        wmNewsMaterialMapper.saveRelations(ids, newsId, type);
    }


    /**
     * 【3.3】 关联文章封面中的图片和素材关系
     *
     * @param dto       前端用户选择封面信息数据
     * @param materials 从内容中解析的图片列表
     * @param wmNews    文章ID
     */
    private void saveRelativeInfoForCover(WmNewsDTO dto, List<String> materials, WmNews wmNews) {
        // 前端用户选择的图
        List<String> images = dto.getImages();
        // 自动获取封面 ****
        if (WemediaConstants.WM_NEWS_TYPE_AUTO.equals(dto.getType())) {
            int materialSize = materials.size();
            if (materialSize > 0 && materialSize <= 2) {  // 单图
                images = materials.stream().limit(1).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            } else if (materialSize > 2) { // 多图
                images = materials.stream().limit(3).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
            } else {  // 无图
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
            if (images != null && images.size() > 0) {
                // 将图片集合 转为字符串  url1,url2,url3
                wmNews.setImages(imageListToStr(images, webSite));
            }
            updateById(wmNews);
        }
        // 保存图片列表和素材的关系
        if (images != null && images.size() > 0) {
            images = images.stream().map(x -> x.replace(webSite, "")
                    .replace(" ", "")).collect(Collectors.toList());
            saveRelativeInfo(images, wmNews.getId(), WemediaConstants.WM_IMAGE_REFERENCE);
        }
    }


    @Override
    public ResponseResult downOrUp(WmNewsDTO dto) {
        //1.检查参数
        if(dto == null || dto.getId() == null){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID);
        }
        Short enable = dto.getEnable();
        if(enable == null ||
                (!WemediaConstants.WM_NEWS_UP.equals(enable)&&!WemediaConstants.WM_NEWS_DOWN.equals(enable))){
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID,"上下架状态错误");
        }
        //2.查询文章
        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断文章是否发布
        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST,"当前文章不是发布状态，不能上下架");
        }
        //4.修改文章状态，同步到app端（后期做）
        update(Wrappers.<WmNews>lambdaUpdate().eq(WmNews::getId,dto.getId())
                .set(WmNews::getEnable,dto.getEnable()));
        //5. 上下架发送消息通知  用于同步article 及 elasticsearch

        if (wmNews.getArticleId()!=null) {
            if(enable.equals(WemediaConstants.WM_NEWS_UP)){
                // 上架消息
                rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                        NewsUpOrDownConstants.NEWS_UP_ROUTE_KEY,wmNews.getArticleId());
            }else {
                // 下架消息
                rabbitTemplate.convertAndSend(NewsUpOrDownConstants.NEWS_UP_OR_DOWN_EXCHANGE,
                        NewsUpOrDownConstants.NEWS_DOWN_ROUTE_KEY,wmNews.getArticleId());
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }



}