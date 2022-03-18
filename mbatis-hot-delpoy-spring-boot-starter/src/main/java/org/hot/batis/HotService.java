package org.hot.batis;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zheng.li
 */
@Component
public class HotService implements InitializingBean, ApplicationContextAware {

    private volatile Configuration configuration;

    private ApplicationContext applicationContext;

    private Resource[] mapperLocations;


    @Value("${spring.profiles.active}")
    private String active;

    @Autowired
    private HotPropertiesConfigure properties;

    private static final Logger log = LoggerFactory.getLogger(HotService.class);

    @Override
    public void afterPropertiesSet() {
        if (!"dev".equals(properties.getActive())) {
            // 判断是否开启了热部署
            // 非开发环境，不执行
            return;
        }
        try {
            SqlSessionFactory sqlSessionFactory = getSqlSessionFactory(properties.getSqlSessionFactoryName());
            configuration = sqlSessionFactory.getConfiguration();
        } catch (Exception e) {
            log.error("setApplicationContext 错误：",e);
        }
        if (configuration != null) {
            log.info("Mybatis热部署标识env={}", active);
            new WatchThread().start();
        }
    }

    /**
     * 获取sqlSessionFactory
     * @param name 名称
     * @return  SqlSessionFactory
     */
    private SqlSessionFactory getSqlSessionFactory(String name) {
        SqlSessionFactory sqlSessionFactory;
        if (name != null && !"".equals(name)) {
            try {
                sqlSessionFactory = (SqlSessionFactory) applicationContext.getBean(name);
            } catch (Exception e) {
                sqlSessionFactory = getSqlSessionFactory("");
            }
        } else {
            sqlSessionFactory = applicationContext.getBean(SqlSessionFactory.class);
        }
        return sqlSessionFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    class WatchThread extends Thread {


        @Override
        public void run() {
            startWatch();
        }

        /**
         * 启动监听
         */
        private void startWatch() {
            try {
                WatchService watcher = FileSystems.getDefault().newWatchService();
                getWatchPaths().forEach(p -> {
                    try {
                        Paths.get(p).register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
                    } catch (Exception e) {
                        log.error("ERROR: 注册xml监听事件", e);
                        throw new RuntimeException("ERROR: 注册xml监听事件", e);
                    }
                });
                while (true) {
                    WatchKey watchKey = watcher.take();
                    Set<String> set = new HashSet<>();
                    List<WatchEvent<?>> events = watchKey.pollEvents();
                    for (WatchEvent<?> event : events) {
                        set.add(event.context().toString());
                    }
                    // 重新加载xml
                    reloadXml(set);
                    boolean valid = watchKey.reset();
                    if (!valid) {
                        log.error("watch break!!");
                        break;
                    }
                }
            } catch (Exception e) {
                System.out.println("Mybatis的xml监控失败!");
                log.info("Mybatis的xml监控失败!", e);
            }
        }

        private Set<String> getWatchPaths() {
            Set<String> set = new HashSet<>();
            Arrays.stream(getMapperLocations()).forEach(r -> {
                try {
                    set.add(r.getFile().getParentFile().getAbsolutePath());
                } catch (Exception e) {
//                    log.info("获取资源路径失败", e);
                }
            });
            log.info("需要监听的xml资源：" + set);
            return set;
        }

        /**
         * 获取mapper文件资源位置
         * @return Resource[]
         */
        private Resource[] getMapperLocations() {
            try {
                ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
                mapperLocations = resolver.getResources("classpath*:/mapper/*/*.xml");
            } catch (Exception e) {
                log.error("mapperLocations 获取失败",e);
                mapperLocations = new Resource[0];
            }
            return mapperLocations;
        }

        /**
         * 删除xml元素的节点缓存
         * @param nameSpace xml中命名空间
         */
        private void clearMap(String nameSpace) {
            log.info("清理Mybatis的namespace={}在mappedStatements、caches、resultMaps、parameterMaps、keyGenerators、sqlFragments中的缓存",nameSpace);
            Arrays.asList("mappedStatements", "caches", "resultMaps", "parameterMaps", "keyGenerators", "sqlFragments").forEach(fieldName -> {
                Object value = getFieldValue(configuration, fieldName);
                if (value instanceof Map) {
                    Map<?, ?> map = (Map<?, ?>) value;
                    List<Object> list = map.keySet().stream().filter(o -> o.toString().startsWith(nameSpace + ".")).collect(Collectors.toList());
                    list.forEach(k -> map.remove((Object) k));
                }
            });
        }

        /**
         * 清除文件记录缓存
         * @param resource xml文件路径
         */
        private void clearSet(String resource) {
            log.info("清理mybatis的资源{}在容器中的缓存", resource);
            Object value = getFieldValue(configuration, "loadedResources");
            if (value instanceof Set) {
                Set<?> set = (Set<?>) value;
                set.remove(resource);
                set.remove("namespace:" + resource);
            }
        }

        /**
         * 获取对象指定属性
         * @param obj       对象信息
         * @param fieldName 属性名称
         */
        private Object getFieldValue(Object obj, String fieldName) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object value = field.get(obj);
                field.setAccessible(accessible);
                return value;
            } catch (Exception e) {
                log.info("ERROR: 加载对象中[{}]", fieldName, e);
                throw new RuntimeException("ERROR: 加载对象中[" + fieldName + "]", e);
            }
        }

        /**
         * 重新加载set中xml
         * @param set 修改的xml资源
         */
        private void reloadXml(Set<String> set) {
            log.info("========== 需要重新加载的文件列表: {}", set);
            getMapperLocations();
            List<Resource> list = Arrays.stream(mapperLocations)
                    .filter(p -> set.contains(p.getFilename()))
                    .collect(Collectors.toList());
            log.info("需要处理的资源路径:{}", list);
            list.forEach(r -> {
                try {
                    clearMap(getNamespace(r));
                    clearSet(r.toString());
                    XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(r.getInputStream(), configuration,
                            r.toString(), configuration.getSqlFragments());
                    xmlMapperBuilder.parse();
                } catch (Exception e) {
                    log.info("ERROR: 重新加载[{}]失败", r.toString(), e);
                    throw new RuntimeException("ERROR: 重新加载[" + r.toString() + "]失败", e);
                } finally {
                    ErrorContext.instance().reset();
                }
            });
            log.info("成功热部署文件列表: {}", set);
        }


        /**
         * 获取xml的namespace
         * @param resource xml资源
         */
        private String getNamespace(Resource resource) {
            log.info("从{}获取namespace", resource.toString());
            try {
                XPathParser parser = new XPathParser(resource.getInputStream(), true, null, new XMLMapperEntityResolver());
                return parser.evalNode("/mapper").getStringAttribute("namespace");
            } catch (Exception e) {
                log.info("ERROR: 解析xml中namespace失败", e);
                throw new RuntimeException("ERROR: 解析xml中namespace失败", e);
            }
        }
    }

}
