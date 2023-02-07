package com.yuhaojituan.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.yuhaojituan.feigns")
@ComponentScan("com.yuhaojituan.feigns.fallback")
public class YuhaojituanFeignAutoConfiguration {
    @Bean
    Logger.Level level() {
        return Logger.Level.FULL;
    }
}