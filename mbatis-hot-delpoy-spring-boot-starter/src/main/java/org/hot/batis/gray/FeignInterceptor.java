package org.hot.batis.gray;

import feign.RequestInterceptor;
import feign.RequestTemplate;
//import org.apache.logging.log4j.ThreadContext;
import org.hot.batis.util.ThreadLocalUtil;
import org.springframework.stereotype.Component;

/**
 * feign请求拦截器
 */
@Component
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 将本地线程变量中的灰度信息放入header中
        String grayVersion = (String) ThreadLocalUtil.get("gray-version");
        if (grayVersion != null) {
            template.header("gray-version", new String[]{grayVersion});
        }

        String defaultVersion = (String)ThreadLocalUtil.get("default-version");
        if (defaultVersion != null) {
            template.header("default-version", new String[]{defaultVersion});
        }

        // 线程追踪信息可以保存
//        String traceId = ThreadContext.get("n-d-trace-id");
//        if (traceId != null) {
//            template.header("n-d-trace-id", new String[]{traceId});
//        } else {
//            traceId = this.application + "-" + UUID.randomUUID().toString();
//            template.header("n-d-trace-id", new String[]{traceId});
//        }

    }
}
