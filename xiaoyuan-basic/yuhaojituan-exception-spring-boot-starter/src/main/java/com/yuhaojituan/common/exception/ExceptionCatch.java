package com.yuhaojituan.common.exception;

import com.yuhaojituan.model.common.dtos.ResponseResult;
import com.yuhaojituan.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@Configuration   //这个注解是为了让spring扫描到controller advice
@RestControllerAdvice   // Springmvc 异常处理拦截注解
public class ExceptionCatch {
    /**
     * 解决项目中所有的异常拦截
     * @return
     */
    @ExceptionHandler(Exception.class)  // exception 所有子类
    public ResponseResult exception(Exception ex) {
        ex.printStackTrace();
        // 记录日志
        log.error("ExceptionCatch ex:{}", ex);
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "您的网络异常，请稍后重试");
    }

    /**
     * 拦截自定义异常
     * @return
     */
    @ExceptionHandler(CustomException.class)
    public ResponseResult custException(CustomException ex) {
        ex.printStackTrace();
        log.error("CustomException ex:{}", ex);
        AppHttpCodeEnum codeEnum = ex.getAppHttpCodeEnum();
        return ResponseResult.errorResult(codeEnum);
    }

}