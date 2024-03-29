package com.yuhaojituan.article.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.article.mapper.ApArticleConfigMapper;
import com.yuhaojituan.article.mapper.ApArticleContentMapper;
import com.yuhaojituan.article.mapper.ApArticleMapper;
import com.yuhaojituan.article.mapper.AuthorMapper;
import com.yuhaojituan.article.service.ApArticleService;
import com.yuhaojituan.article.service.GeneratePageService;
import com.yuhaojituan.common.constants.article.ArticleConstants;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.feigns.AdminFeign;
import com.yuhaojituan.feigns.WemediaFeign;
import com.yuhaojituan.model.admin.pojos.AdChannel;
import com.yuhaojituan.model.article.dtos.ArticleHomeDTO;
import com.yuhaojituan.model.article.pojos.ApArticle;
import com.yuhaojituan.model.article.pojos.ApArticleConfig;
import com.yuhaojituan.model.article.pojos.ApArticleContent;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.wemedia.pojos.WmNews;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {
    @Autowired
    private WemediaFeign wemediaFeign;
    @Autowired
    private AdminFeign adminFeign;
    @Autowired
    private AuthorMapper authorMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private GeneratePageService generatePageService;

    //因为这里涉及远程调用操作多张表 所以事务
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 100000)
    @Override
    public void publishArticle(Integer newsId) {
        // 1. 查询并检查自媒体文章 并审核  wmNews
        WmNews wmNews = getWmNews(newsId);
        // 2. 封装 ApArticle
        ApArticle apArticle = getApArticle(wmNews);
        // 3. 保存或修改 article信息
        saveOrUpdateArticle(apArticle);
        // 4. 保存关联配置和内容信息
        saveConfigAndContent(wmNews, apArticle);
        // 5. 文章页面静态化
        generatePageService.generateArticlePage(wmNews.getContent(),apArticle);
        // 6. 更新 wmNews状态  改为9  并设置articleId
        updateWmNews(newsId, wmNews, apArticle);
        // 7. TODO 通知es索引库添加文章索引
    }

    /**
     * 查询自媒体文章
     */
    private WmNews getWmNews(Integer newsId) {
        ResponseResult<WmNews> newsResult = wemediaFeign.findWmNewsById(newsId);
        if (!newsResult.checkCode()) {
            log.error("文章发布失败 远程调用自媒体文章接口失败  文章id: {}", newsId);
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用自媒体文章接口失败");
        }
        WmNews wmNews = newsResult.getData();
        if (wmNews == null) {
            log.error("文章发布失败 未获取到自媒体文章信息  文章id: {}", newsId);
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "未查询到自媒体文章");
        }
        // 判断状态是否为 4 或 8， 如果不是  不处理
        short status = wmNews.getStatus().shortValue();
        if (status != WmNews.Status.ADMIN_SUCCESS.getCode() && status != WmNews.Status.SUCCESS.getCode()) {
            log.error("文章发布失败 文章状态不为 4 或 8， 不予发布 , 文章id : {}", newsId);
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "自媒体文章状态错误");
        }
        return wmNews;
    }

    /**
     * 修改自媒体文章
     */
    private void updateWmNews(Integer newsId, WmNews wmNews, ApArticle apArticle) {
        wmNews.setStatus(WmNews.Status.PUBLISHED.getCode());
        wmNews.setArticleId(apArticle.getId());
        ResponseResult updateResult = wemediaFeign.updateWmNews(wmNews);
        if (!updateResult.checkCode()) {
            log.error("文章发布失败 远程调用修改文章接口失败， 不予发布 , 文章id : {} ", newsId);
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用修改文章接口失败");
        }
    }

    /**
     * 保存 配置 和 内容信息
     */
    private void saveConfigAndContent(WmNews wmNews, ApArticle apArticle) {
        // 添加配置信息
        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        apArticleConfig.setIsComment(true);
        apArticleConfig.setIsForward(true);
        apArticleConfig.setIsDown(false);
        apArticleConfig.setIsDelete(false);
        apArticleConfigMapper.insert(apArticleConfig);
        // 添加文章详情
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        apArticleContent.setContent(wmNews.getContent());
        apArticleContentMapper.insert(apArticleContent);
    }

    /**
     * 保存或修改文章信息
     */
    private void saveOrUpdateArticle(ApArticle apArticle) {
        // 判断wmNews之前是否关联 articleId
        if (apArticle.getId() == null) {
            // 无关联  新增 article
            // 保存文章
            apArticle.setCollection(0); // 收藏数
            apArticle.setLikes(0);// 点赞数
            apArticle.setComment(0);// 评论数
            apArticle.setViews(0); // 阅读数
            save(apArticle);
        } else {
            // 有关联  修改 article
            // 修改文章  删除之前关联的配置信息   内容信息
            ApArticle article = getById(apArticle.getId());
            if (article == null) {
                CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "关联的文章不存在");
            }
            updateById(apArticle);
            apArticleConfigMapper.delete(Wrappers.<ApArticleConfig>lambdaQuery().eq(ApArticleConfig::getArticleId, apArticle.getId()));
            apArticleContentMapper.delete(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, apArticle.getId()));
        }
    }

    /**
     * 封装apArticle
     */
    private ApArticle getApArticle(WmNews wmNews) {
        ApArticle apArticle = new ApArticle();
        // 拷贝属性
        BeanUtils.copyProperties(wmNews, apArticle);
        apArticle.setId(wmNews.getArticleId());
        apArticle.setFlag((byte) 0); // 普通文章
        apArticle.setLayout(wmNews.getType());// 布局
        // 远程查询频道信息
        ResponseResult<AdChannel> channelResult = adminFeign.findOne(wmNews.getChannelId());
        if (!channelResult.checkCode()) {
            log.error("文章发布失败 远程调用查询频道出现异常， 不予发布 , 文章id : {}  频道id : {}", wmNews.getId(), wmNews.getChannelId());
            CustException.cust(AppHttpCodeEnum.REMOTE_SERVER_ERROR, "远程调用查询频道出现异常");
        }
        AdChannel channel = channelResult.getData();
        if (channel == null) {
            log.error("文章发布失败 未查询到相关频道信息， 不予发布 , 文章id : {}  频道id : {}", wmNews.getId(), wmNews.getChannelId());
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "未查询到相关频道信息");
        }
        apArticle.setChannelName(channel.getName());
        // 设置作者信息
        ApAuthor author = authorMapper.selectOne(Wrappers.<ApAuthor>lambdaQuery().eq(ApAuthor::getWmUserId, wmNews.getUserId()));
        if (author == null) {
            log.error("文章发布失败 未查询到相关作者信息， 不予发布 , 文章id : {}  自媒体用户id : {}", wmNews.getId(), wmNews.getUserId());
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "根据自媒体用户，查询关联作者信息失败");
        }
        apArticle.setAuthorId(Long.valueOf(author.getId()));
        apArticle.setAuthorName(author.getName());
        return apArticle;
    }


    @Autowired
    private ApArticleMapper apArticleMapper;
    @Value("${file.oss.web-site}")
    private String webSite;
    @Value("${file.minio.readPath}")
    private String readPath;
    /**
     * 根据参数加载文章列表
     */
    @Override
    public ResponseResult load(Short loadtype, ArticleHomeDTO dto) {
        //1 参数检查
        // 页大小
        Integer size = dto.getSize();
        if (size == null || size <= 0) {
            size = 10;
        }
        dto.setSize(size);
        // 频道
        if (StringUtils.isBlank(dto.getTag())) {
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }
        // 时间
        if (dto.getMaxBehotTime() == null) {
            dto.setMaxBehotTime(new Date());
        }
        if (dto.getMinBehotTime() == null) {
            dto.setMinBehotTime(new Date());
        }
        // 类型判断
        if (!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !loadtype.equals(ArticleConstants.LOADTYPE_LOAD_NEW)) {
            loadtype = ArticleConstants.LOADTYPE_LOAD_MORE;
        }


        //2 执行查询
        List<ApArticle> articleList = apArticleMapper.loadArticleList(dto, loadtype);
        // 添加静态页面访问前缀  ===============新增代码=================
        for (ApArticle apArticle : articleList) {
            apArticle.setStaticUrl(readPath + apArticle.getStaticUrl());
        }
        //给图片加上网站前缀
        for (ApArticle article : articleList) {
            // 获取文章封面字段
            String images = article.getImages();
            if (StringUtils.isNotBlank(images)) {
                // 将封面按照  ,号  切割   生成流
                images = Arrays.stream(images.split(","))
                        // 每一个路径添加前缀
                        .map(url -> webSite + url)
                        // 将加了前缀的路径  拼接成字符串
                        .collect(Collectors.joining(","));
                article.setImages(images);
            }
        }

        //3 返回结果
        ResponseResult result = ResponseResult.okResult(articleList);
        return result;
    }






}