package org.hot.batis.gray;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.GenericFilter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 追踪日志处理器
 * @author zheng.li
 **/
public class TraceLogFilter extends GenericFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        String traceId = req.getHeader("n-d-trace-id");
        if (traceId == null || "".equals(traceId)) {
            return;
        }
        ThreadContext.put("n-d-trace-id",traceId);
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
