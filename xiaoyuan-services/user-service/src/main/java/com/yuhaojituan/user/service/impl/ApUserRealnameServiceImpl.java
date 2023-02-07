package com.yuhaojituan.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuhaojituan.common.constants.admin.AdminConstants;
import com.yuhaojituan.common.exception.CustException;
import com.yuhaojituan.common.exception.CustomException;
import com.yuhaojituan.feigns.ArticleFeign;
import com.yuhaojituan.feigns.WemediaFeign;
import com.yuhaojituan.model.article.pojos.ApAuthor;
import com.yuhaojituan.model.common.dtos.PageResponseResult;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.user.dtos.AuthDTO;
import com.yuhaojituan.model.user.pojos.ApUser;
import com.yuhaojituan.model.user.pojos.ApUserRealname;
import com.yuhaojituan.model.wemedia.pojos.WmUser;
import com.yuhaojituan.user.mapper.ApUserMapper;
import com.yuhaojituan.user.mapper.ApUserRealnameMapper;
import com.yuhaojituan.user.service.ApUserRealnameService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {
    /**
     * 查询列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult loadListByStatus(AuthDTO dto) {
        // 1 参数检查
        if (dto == null) {
            throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
        }
        dto.checkParam();

        // 2 条件查询
        Page<ApUserRealname> page = new Page<>(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper();

        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(ApUserRealname::getStatus, dto.getStatus());
        }

        IPage<ApUserRealname> resultPage = page(page, lambdaQueryWrapper);

        // 3 返回结果
        return new PageResponseResult(dto.getPage(), dto.getSize(),
                resultPage.getTotal(), resultPage.getRecords());
    }

    @Autowired
    ApUserMapper apUserMapper;
    @Autowired
    WemediaFeign wemediaFeign;
    @Autowired
    ArticleFeign articleFeign;

    @Override
    @Transactional(rollbackFor = Exception.class)
    //如果抛异常就回滚
    public ResponseResult updateStatusById(AuthDTO dto, Short status) {
        //1 参数检查
        if (dto.getId() == null) {
            CustException.cust(AppHttpCodeEnum.PARAM_INVALID, "id为空");
        }
        //2.根据id查询user realname数据
        ApUserRealname apUserRealname = getById(dto.getId());
        //3.待审核状态才审核
        if (!AdminConstants.WAIT_AUTH.equals(apUserRealname.getStatus())) {
            CustException.cust(AppHttpCodeEnum.DATA_NOT_ALLOW, "不是待审核状态");
        }
        //4.根据userid 查user表
        ApUser apUser = apUserMapper.selectOne(Wrappers.<ApUser>lambdaQuery()
                .eq(ApUser::getId, apUserRealname.getUserId()));
        if(apUser == null){
            CustException.cust(AppHttpCodeEnum.DATA_NOT_EXIST, "user表中不存在这个realname数据");
        }

        //上面都满足才update

        //下面三个是都会修改的
        // 更新认证用户信息
        apUserRealname.setStatus(status);
        apUserRealname.setUpdatedTime(new Date());
        //驳回原因
        if (StringUtils.isNotBlank(dto.getMsg())) {
            apUserRealname.setReason(dto.getMsg());
        }
        //修改完毕
        updateById(apUserRealname);

        //4 认证状态如果为 通过
        if (AdminConstants.PASS_AUTH.equals(status)) {
            //4.1 创建自媒体账户
            WmUser wmUser = createWmUser(dto, apUser);
            //4.2 创建作者信息
            createApAuthor(wmUser);
        }
        //5 返回结果
        return ResponseResult.okResult();
    }

    /**
     * 4.2 创建作者信息
     */
    private void createApAuthor(WmUser wmUser) {
        //1 检查是否成功调用
        ResponseResult<ApAuthor> apAuthorResult = articleFeign.findByUserId(wmUser.getApUserId());
        if (apAuthorResult.getCode().intValue() != 0) {
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, apAuthorResult.getErrorMessage());
        }
        //2. 检查作者信息是否已经存在
        ApAuthor apAuthor = apAuthorResult.getData();
        if (apAuthor != null) {
            CustException.cust(AppHttpCodeEnum.DATA_EXIST, "作者信息已存在");
        }
        //3. 添加作者信息
        apAuthor = new ApAuthor();
        apAuthor.setCreatedTime(new Date());
        apAuthor.setName(wmUser.getName());
        apAuthor.setType(AdminConstants.AUTHOR_TYPE); // 自媒体人类型
        apAuthor.setUserId(wmUser.getApUserId()); // APP 用户ID
        apAuthor.setWmUserId(wmUser.getId()); // 自媒体用户ID
        ResponseResult result = articleFeign.save(apAuthor);
        //4. 结果失败，抛出异常
        if (result.getCode() != 0) {
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, result.getErrorMessage());
        }
    }

    /**
     * 4.1 创建自媒体账户
     */
    private WmUser createWmUser(AuthDTO dto, ApUser apUser) {
        ResponseResult<WmUser> wmUserResult = wemediaFeign.findByName(apUser.getName());
        if (wmUserResult.getCode().intValue() != 0) {
            //远程调用失败
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, wmUserResult.getErrorMessage());
        }

        WmUser wmUser = wmUserResult.getData();
        if (wmUser != null) {
            CustException.cust(AppHttpCodeEnum.DATA_EXIST, "自媒体用户信息已存在");
        }

        wmUser = new WmUser();
        wmUser.setName(apUser.getName());
        wmUser.setSalt(apUser.getSalt());  // 盐
        wmUser.setPassword(apUser.getPassword()); // 密码
        wmUser.setPhone(apUser.getPhone());
        wmUser.setCreatedTime(new Date());
        wmUser.setType(0); // 个人
        wmUser.setApUserId(apUser.getId());  // app端用户id
        wmUser.setStatus(AdminConstants.PASS_AUTH.intValue());
        ResponseResult<WmUser> saveResult = wemediaFeign.save(wmUser);
        if (saveResult.getCode().intValue() != 0) {
            CustException.cust(AppHttpCodeEnum.SERVER_ERROR, saveResult.getErrorMessage());
        }
        return saveResult.getData();
    }
}