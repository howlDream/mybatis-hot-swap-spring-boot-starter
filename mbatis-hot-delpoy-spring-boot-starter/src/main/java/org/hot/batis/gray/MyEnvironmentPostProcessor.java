package org.hot.batis.gray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class MyEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger log = LoggerFactory.getLogger(MyEnvironmentPostProcessor.class);

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // 属性中加入灰度信息
            ClassPathResource gitResource = new ClassPathResource("git.properties");
            InputStream gitInputStream = gitResource.getInputStream();
            Properties gitProperties = new Properties();
            gitProperties.load(gitInputStream);
            Properties grayProperties = new Properties();
            grayProperties.put("eureka.instance.metadataMap.gray-version", gitProperties.get("git.branch"));
            grayProperties.put("eureka.instance.metadataMap.build-time", gitProperties.get("git.build.time"));
            PropertiesPropertySource propertiesPropertySource = new PropertiesPropertySource("grayconfig", grayProperties);
            environment.getPropertySources().addLast(propertiesPropertySource);
        } catch (IOException e) {
            log.error("post process environment error", e);
        }
    }

    @Override
    public int getOrder() {
        return -2147483637;
    }
}
