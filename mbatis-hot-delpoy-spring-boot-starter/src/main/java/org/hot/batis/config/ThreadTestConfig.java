package org.hot.batis.config;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.validation.constraints.NotNull;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 测试自定义线程池(测试线程traceId信息传递)
 */
@Configuration
public class ThreadTestConfig {

    @Bean(name = "test")
    public TaskExecutor teamThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数,默认为1
        executor.setCorePoolSize(6);
        // 设置最大线程数，默认为Integer.MAX_VALUE
        executor.setMaxPoolSize(16);
        // 设置线程池维护线程所允许的空闲时间（秒），默认为60s
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("test-");
        /*
        设置拒绝策略
        AbortPolicy:直接抛出java.util.concurrent.RejectedExecutionException异常
        CallerRunsPolicy:主线程直接执行该任务，执行完之后尝试添加下一个任务到线程池中，可以有效降低向线程池内添加任务的速度
        DiscardOldestPolicy:抛弃旧的任务、暂不支持；会导致被丢弃的任务无法再次被执行
        DiscardPolicy:抛弃当前任务、暂不支持；会导致被丢弃的任务无法再次被执行
         */
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 自定义线程工厂
        executor.setThreadFactory(new TraceThreadFactory());
        return executor;
    }

    /**
     * 自定义线程工厂，新线程加入父线程的 traceid 信息
     */
    private static class TraceThreadFactory extends CustomizableThreadFactory {

        @Override
        public Thread newThread (@NotNull Runnable runnable) {
            String traceId = ThreadContext.get("n-d-trace-id");
            Runnable newRunnable = () -> {
                ThreadContext.put("n-d-trace-id",traceId);
                runnable.run();
            };
            return createThread(newRunnable);
        }
    }

}
