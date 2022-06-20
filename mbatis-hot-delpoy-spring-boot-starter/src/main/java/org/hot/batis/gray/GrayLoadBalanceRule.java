package org.hot.batis.gray;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.logging.log4j.util.Strings;
import org.hot.batis.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 灰度负载均衡规则
 */
public class GrayLoadBalanceRule extends AbstractLoadBalancerRule {

    private static final Logger log = LoggerFactory.getLogger(GrayLoadBalanceRule.class);

    private AtomicInteger nextServerCyclicCounter;

    public GrayLoadBalanceRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }

    private Server choose(ILoadBalancer lb, Object key) {
        String version = (String) ThreadLocalUtil.get("gray-version");
        // 存在灰度信息走灰度选择规则，否则循环规则
        return Strings.isNotEmpty(version) ? this.grayChoose(lb, key) : this.roundRobinChoose(lb, key);
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    /**
     * 灰度实例服务选择
     * @param lb
     * @param key
     * @return
     */
    private Server grayChoose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        } else {
            Server server = null;

            if (Thread.interrupted()) {
                return null;
            }

            List<Server> upList = lb.getReachableServers();
            List<Server> allList = lb.getAllServers();
            int serverCount = allList.size();
            if (serverCount == 0) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            String defaultVersion = (String)ThreadLocalUtil.get("default-version");
            String version = (String)ThreadLocalUtil.get("gray-version");
            Server defaultServer = null;

            for (Server server1 : upList) {
                String grayVersion = ((DiscoveryEnabledServer) server1).getInstanceInfo().getMetadata().get("gray-version");
                log.info(((DiscoveryEnabledServer) server1).getInstanceInfo().getAppName() + ":" + server1.getHost() + ":" + server1.getPort() + ":" + grayVersion);
                if (grayVersion != null && grayVersion.equals(version) && server1.isAlive()) {
                    // 匹配到灰度服务实例，返回
                    return server1;
                }

                if (defaultVersion != null  && server1.isAlive()) {
                    // 配置的默认服务实例
                    server = server1;
                } else if ((grayVersion == null || grayVersion.length() <= 0 || grayVersion.equals("master")) && server1.isAlive()) {
                    // 默认基线服务
                    defaultServer = server1;
                }
            }

            if (server == null && defaultServer != null) {
                server = defaultServer;
            }

            return server;
        }
    }

    /**
     *  循环规则
     * @param lb  ILoadBalancer
     * @param key key
     * @return Server
     * @see com.netflix.loadbalancer.RoundRobinRule
     */
    private Server roundRobinChoose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        }

        Server server = null;
        int count = 0;
        while (count++ < 10) {
            List<Server> reachableServers = lb.getReachableServers();
            List<Server> allServers = lb.getAllServers();
            int upCount = reachableServers.size();
            int serverCount = allServers.size();

            if ((upCount == 0) || (serverCount == 0)) {
                log.warn("No up servers available from load balancer: " + lb);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = allServers.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.isAlive() && (server.isReadyToServe())) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + lb);
        }
        return server;
    }

    /**
     * Inspired by the implementation of {@link AtomicInteger#incrementAndGet()}.
     *
     * @param modulo The modulo to bound the value of the counter.
     * @return The next value.
     */
    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next))
                return next;
        }
    }

    @Override
    public void initWithNiwsConfig(IClientConfig iClientConfig) {

    }
}
