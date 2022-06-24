package org.hot.batis.gray;

import org.apache.logging.log4j.ThreadContext;
import org.hot.batis.util.ThreadLocalUtil;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 如果服务不走网关的话，可以在这里保存传递灰度信息
 */
public class RequestHandlerInterceptor implements AsyncHandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 日志中添加追踪id
        ThreadContext.clearAll();
        if (request.getHeader("n-d-trace-id") != null) {
            ThreadContext.put("n-d-trace-id", request.getHeader("n-d-trace-id"));
        } else {
            ThreadContext.put("n-d-trace-id", UUID.randomUUID().toString());
        }

        // 灰度信息
        String grayVersion;
        ThreadLocalUtil.clear();
        grayVersion = request.getHeader("gray-version");
        if (grayVersion != null) {
            ThreadLocalUtil.set("gray-version", grayVersion);
        }

        String defaultVersion = request.getHeader("default-version");
        if (defaultVersion != null) {
            ThreadLocalUtil.set("default-version", defaultVersion);
        }

        return true;
    }

}
