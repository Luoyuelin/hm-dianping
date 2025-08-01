package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author：lyl
 * @Package：com.hmdp.config
 * @Project：hm-dianping
 * @name：MvcConfig
 * @Date：2025/4/22 21:16
 * @Filename：MvcConfig
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor())
                .excludePathPatterns(
                        "./voucher/**",
                        "./shop-type/**",
                        "./shop/**",
                        "./upload/**",
                        "/blog/hot",
                        "/user/code",
                        "/user/login"
                );

    }
}
