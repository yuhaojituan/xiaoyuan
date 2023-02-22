package com.yuhaojituan.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.common.exception.CustomException;
import com.yuhaojituan.file.service.FileStorageService;
import com.yuhaojituan.model.common.dtos.PageResponseResult;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.threadlocal.WmThreadLocalUtils;
import com.yuhaojituan.model.wemedia.dtos.WmMaterialDTO;
import com.yuhaojituan.model.wemedia.pojos.WmMaterial;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import com.yuhaojituan.wemedia.mapper.WmMaterialMapper;
import com.yuhaojituan.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {
    @Autowired
    FileStorageService fileStorageService;
    @Value("${file.oss.prefix}")
    String prefix;
    @Value("${file.oss.web-site}")
    String webSite;

    @Override
    public ResponseResult<WmMaterial> uploadPicture(MultipartFile multipartFile) {
        // 1 参数校验
        if (multipartFile == null || multipartFile.getSize() == 0) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "请上传正确的文件");
        }
        // 当前线程中获取用户ID
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            CustException.cust(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        String originalFilename = multipartFile.getOriginalFilename();
        if (!checkFileSuffix(originalFilename)) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "请上传正确的素材格式,[jpg,jpeg,png,gif]");
        }
        // 2 上传到oss
        String fileId = null;
        try {
            String filename = UUID.randomUUID().toString().replace("-", "");
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            fileId = fileStorageService.store(prefix, filename + suffix, multipartFile.getInputStream());
            log.info("阿里云OSS 文件 fileId: {}", fileId);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("阿里云文件上传失败 uploadPicture error: {}", e);
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, "服务器繁忙请稍后重试");
        }
        // 3 封装数据并保持到素材库中
        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setIsCollection((short) 0);
        wmMaterial.setType((short) 0);
        wmMaterial.setCreatedTime(new Date());
        // 设置文件id
        wmMaterial.setUrl(fileId);
        wmMaterial.setUserId(user.getId());
        save(wmMaterial);
        // 前端显示
        wmMaterial.setUrl(webSite + fileId);
        // 4 返回结果
        return ResponseResult.okResult(wmMaterial);
    }

    /**
     * 检查文件格式 目前仅仅支持jpg  jpeg  png  gif 图片的上传
     *
     * @param path
     * @return
     */
    private boolean checkFileSuffix(String path) {
        if (StringUtils.isBlank(path)) return false;
        List<String> allowSuffix = Arrays.asList("jpg", "jpeg", "png", "gif");
        boolean isAllow = false;
        for (String suffix : allowSuffix) {
            if (path.endsWith(suffix)) {
                isAllow = true;
                break;
            }
        }
        return isAllow;
    }


    @Override
    public ResponseResult findList(WmMaterialDTO dto) {
        // 1 参数校验
        dto.checkParam();
        // 2 执行业务查询
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        // 收藏
        if (dto.getIsCollection() != null && dto.getIsCollection() == 1) {
            wrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        // 当前登录用户的素材
        WmUser user = WmThreadLocalUtils.getUser();
        if (user == null) {
            throw new CustomException(AppHttpCodeEnum.NO_OPERATOR_AUTH);
        }
        wrapper.eq(WmMaterial::getUserId, user.getId());
        // 时间倒序
        wrapper.orderByDesc(WmMaterial::getCreatedTime);
        IPage<WmMaterial> pageParam = new Page<>(dto.getPage(), dto.getSize());
        IPage<WmMaterial> resultPage = page(pageParam, wrapper);
        List<WmMaterial> records = resultPage.getRecords();
        for (WmMaterial record : records) {
            record.setUrl(webSite + record.getUrl());
        }
        // 3 封装结果
        PageResponseResult pageResponseResult = new PageResponseResult(dto.getPage(), dto.getSize(), resultPage.getTotal());
        pageResponseResult.setData(records);
        return pageResponseResult;
    }
}