package com.yuhaojituan.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import com.yuhaojituan.model.user.dtos.LoginDTO;
import com.yuhaojituan.model.user.pojos.ApUser;
import com.yuhaojituan.user.mapper.ApUserMapper;
import com.yuhaojituan.user.service.ApUserLoginService;
import com.yuhaojituan.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class ApUserLoginServiceImpl implements ApUserLoginService {
    @Autowired
    ApUserMapper apUserMapper;
    /**
     * app端登录
     * @param dto
     * @return
     */
    @Override
    public ResponseResult login(LoginDTO dto) {
        //1.校验参数
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            ApUser apUser = apUserMapper.selectOne(Wrappers.<ApUser>lambdaQuery()
                    .eq(ApUser::getPhone, dto.getPhone()));
            if (apUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "请检查手机号");
            }
            // 数据库密码
            String dbpassword = apUser.getPassword();
            String newPassword = DigestUtils.md5DigestAsHex((dto.getPassword() + apUser.getSalt()).getBytes());
            if (!dbpassword.equals(newPassword)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "手机号或密码错误");
            }
            Map<String, Object> map = new HashMap<>();
            apUser.setPassword("");
            apUser.setSalt("");
            map.put("token", AppJwtUtil.getToken(apUser.getId().longValue()));
            map.put("user", apUser);
            return ResponseResult.okResult(map);
        }else {
            if(dto.getEquipmentId() == null){
                return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
            }
            Map<String,Object> map = new HashMap<>();
            // 通过设备ID登录的 userId存0
            map.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }
}