package org.hot.batis.config;

import com.netflix.loadbalancer.IRule;
import feign.RequestInterceptor;
import org.hot.batis.gray.FeignInterceptor;
import org.hot.batis.gray.GrayLoadBalanceRule;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignInterceptor();
    }

    @Configuration
    @RibbonClients(defaultConfiguration = {LBRuleConfig.class})
    public static class LBRuleConfig {

        /**
         *  指定ribbon负载均衡规则
         * @return IRule
         */
        @Bean
        public IRule ribbonRule(){
            return new GrayLoadBalanceRule();
        }

    }
}
