package org.hot.batis.config;

import org.hot.batis.gray.RequestHandlerInterceptor;
import org.hot.batis.gray.TraceLogFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {
    public InterceptorConfig() {
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestHandlerInterceptor());
    }

    @Bean
    TraceLogFilter initTraceLogFilter() {
        return new TraceLogFilter();
    }

}


