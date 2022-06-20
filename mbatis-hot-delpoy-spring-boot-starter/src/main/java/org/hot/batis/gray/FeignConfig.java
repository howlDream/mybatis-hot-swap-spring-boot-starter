package org.hot.batis.gray;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    public RequestInterceptor requestInterceptor() {
        return new FeignInterceptor();
    }

}
