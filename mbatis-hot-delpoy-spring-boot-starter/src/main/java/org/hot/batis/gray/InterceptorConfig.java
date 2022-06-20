package org.hot.batis.gray;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    public InterceptorConfig() {
    }

    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestHandlerInterceptor());
    }
}


