package com.yuhaojituan.user.controller.v1;

import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.user.pojos.ApUser;
import com.yuhaojituan.user.service.ApUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "app端用户管理api",tags = "app端用户管理api")
@RestController
@RequestMapping("/api/v1/user")
public class ApUserController {
    @Autowired
    private ApUserService apUserService;
    @ApiOperation("根据id查询apUser信息")
    @GetMapping("/{id}")
    public ResponseResult<ApUser> findUserById(@PathVariable("id") Integer id) {
        return ResponseResult.okResult(apUserService.getById(id));
    }
}