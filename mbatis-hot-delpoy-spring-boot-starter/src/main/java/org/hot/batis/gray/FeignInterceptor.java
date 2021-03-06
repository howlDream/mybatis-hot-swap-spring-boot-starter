package org.hot.batis.gray;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.apache.logging.log4j.ThreadContext;
import org.hot.batis.util.ThreadLocalUtil;

import java.util.UUID;

/**
 * feign请求拦截器
 */
public class FeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // 将本地线程变量中的灰度信息放入header中
        String grayVersion = (String) ThreadLocalUtil.get("gray-version");
        if (grayVersion != null) {
            template.header("gray-version", grayVersion);
        }

        String defaultVersion = (String)ThreadLocalUtil.get("default-version");
        if (defaultVersion != null) {
            template.header("default-version", defaultVersion);
        }

        // 线程追踪信息可以保存
        String traceId = ThreadContext.get("n-d-trace-id");
        if (traceId != null) {
            template.header("n-d-trace-id", traceId);
        } else {
            traceId = UUID.randomUUID().toString();
            template.header("n-d-trace-id",traceId);
        }

    }
}
