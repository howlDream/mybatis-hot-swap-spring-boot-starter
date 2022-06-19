package org.hot.batis.gray;

import org.hot.batis.util.ThreadLocalUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import java.util.UUID;

/**
 * 如果服务不走网关的话，可以在这里保存灰度信息
 */
@Component
public class RequestHandlerInterceptor extends HandlerInterceptorAdapter {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        ThreadContext.clearAll();
//        if (request.getHeader("n-d-trace-id") != null) {
//            ThreadContext.put("n-d-trace-id", request.getHeader("n-d-trace-id"));
//        } else {
//            ThreadContext.put("n-d-trace-id", this.application + "-" + UUID.randomUUID().toString());
//        }

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
