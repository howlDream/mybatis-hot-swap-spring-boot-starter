package org.hot.batis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author zheng.li
 */
@Configuration
@ConfigurationProperties(prefix = "spring.profiles")
public class HotPropertiesConfigure {

    /**
     * 是否启用标志(dev：开发环境)
     */
    private String active;

    /**
     * sqlSessionFactory的特定bean name
     */
    private String sqlSessionFactoryName = "sqlSessionFactory";

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void setSqlSessionFactoryName(String sqlSessionFactoryName) {
        this.sqlSessionFactoryName = sqlSessionFactoryName;
    }

    public String getSqlSessionFactoryName() {
        return sqlSessionFactoryName;
    }
}
