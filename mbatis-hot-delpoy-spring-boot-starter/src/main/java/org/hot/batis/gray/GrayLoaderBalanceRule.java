package org.hot.batis.gray;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.hot.batis.util.ThreadLocalUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GrayLoaderBalanceRule extends AbstractLoadBalancerRule {

    private static final Logger log = LoggerFactory.getLogger(GrayLoaderBalanceRule.class);

    public Server choose(ILoadBalancer lb, Object key) {
        String version = (String) ThreadLocalUtil.get("gray-version");
        return version != null && version.length() > 0 ? this.grayChoose(lb, key) : this.roundRobinChoose(lb, key);
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    private Server grayChoose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        } else {
            Server server = null;

            do {
                if (server != null) {
                    return server;
                }

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
                Server developServer = null;
                Iterator iterator = upList.iterator();

                while(true) {
                    while(iterator.hasNext()) {
                        Server server1 = (Server)iterator.next();
                        String grayVersion = (String)((DiscoveryEnabledServer)server1).getInstanceInfo().getMetadata().get("gray-version");
                        log.info(((DiscoveryEnabledServer)server1).getInstanceInfo().getAppName() + ":" + server1.getHost() + ":" + server1.getPort() + ":" + grayVersion);
                        if (grayVersion != null && grayVersion.equals(version) && server1.isAlive()) {
                            return server1;
                        }

                        if (defaultVersion != null && defaultVersion.equals("release-2.0.x") && grayVersion.equals("release-2.0.x") && server1.isAlive()) {
                            server = this.transServer(server1);
                        } else if ((grayVersion == null || grayVersion.length() <= 0 || grayVersion.equals("develop")) && server1.isAlive()) {
                            developServer = this.transServer(server1);
                        }
                    }

                    if (server == null && developServer != null) {
                        server = developServer;
                    }
                    break;
                }
            } while(!server.isAlive());

            return server;
        }
    }

    public Server roundRobinChoose(ILoadBalancer lb, Object key) {
        if (lb == null) {
            log.warn("no load balancer");
            return null;
        } else {
            Server server = null;
            int count = 0;

            label80:
            while(true) {
                if (server == null && count++ < 10) {
                    List<Server> reachableServers = new ArrayList();
                    List<Server> allServers = new ArrayList();
                    List<Server> upList = lb.getReachableServers();
                    Iterator var8 = upList.iterator();

                    while(true) {
                        Server server1;
                        String grayVersion;
                        do {
                            if (!var8.hasNext()) {
                                List<Server> allList = lb.getAllServers();
                                Iterator var13 = allList.iterator();

                                while(true) {
                                    String grayVersion;
                                    Server server1;
                                    do {
                                        if (!var13.hasNext()) {
                                            int upCount = reachableServers.size();
                                            int serverCount = allServers.size();
                                            if (upCount != 0 && serverCount != 0) {
                                                int nextServerIndex = this.incrementAndGetModulo(serverCount);
                                                server = (Server)allServers.get(nextServerIndex);
                                                if (server == null) {
                                                    Thread.yield();
                                                } else {
                                                    if (server.isAlive() && server.isReadyToServe()) {
                                                        return server;
                                                    }

                                                    server = null;
                                                }
                                                continue label80;
                                            }

                                            log.warn("No up servers available from load balancer: " + lb);
                                            return null;
                                        }

                                        server1 = (Server)var13.next();
                                        grayVersion = (String)((DiscoveryEnabledServer)server1).getInstanceInfo().getMetadata().get("gray-version");
                                    } while(grayVersion != null && grayVersion.length() > 0 && !grayVersion.equals("develop") && !grayVersion.equals("release-2.0.x"));

                                    allServers.add(server1);
                                }
                            }

                            server1 = (Server)var8.next();
                            grayVersion = (String)((DiscoveryEnabledServer)server1).getInstanceInfo().getMetadata().get("gray-version");
                        } while(grayVersion != null && grayVersion.length() > 0 && !grayVersion.equals("develop") && !grayVersion.equals("release-2.0.x"));

                        reachableServers.add(server1);
                    }
                }

                if (count >= 10) {
                    log.warn("No available alive servers after 10 tries from load balancer: " + lb);
                }

                return server;
            }
        }
    }

}
