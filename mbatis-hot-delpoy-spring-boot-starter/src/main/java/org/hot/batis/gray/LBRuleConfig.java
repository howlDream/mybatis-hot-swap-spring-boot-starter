package org.hot.batis.gray;

import com.netflix.loadbalancer.IRule;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RibbonClients(defaultConfiguration = {LBRuleConfig.class})
public class LBRuleConfig {

    /**
     *  指定ribbon负载均衡规则
     * @return IRule
     */
    @Bean
    public IRule ribbonRule(){
        return new GrayLoadBalanceRule();
    }

}
