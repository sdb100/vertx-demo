package com.worldpay.vertx;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.HostDistance;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RoundRobinPolicy;

/**
 * This class creates the connection to the Cassandra database.
 * 
 * @author Tibor Both
 */
public class CassandraConnector {

    private static final Logger LOG = LoggerFactory.getLogger(CassandraConnector.class);

    private Cluster cluster;
    private Session session;
    
    private String cassandraIP = "127.0.0.1";

//    private static final int FIVE = 5;

    public CassandraConnector() {
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, 4, 10);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 10000);
        
        String ip = System.getenv("CASSANDRA_CDK_SERVICE_HOST");
        if(ip != null){
            this.cassandraIP = ip;
        }

        cluster = Cluster.builder()
                .addContactPoint(this.cassandraIP)
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withPoolingOptions(poolingOptions)
                .build();
        session = cluster.connect("dev");

//        if (properties.isMonitorPoolUsage()) {
//            monitor();
//        }
    }

    public Session getSession() {
        return session;
    }

    /**
     * Monitors pool usage and logs data every 5 seconds.
    private void monitor() {
        final LoadBalancingPolicy loadBalancingPolicy = cluster.getConfiguration()
                .getPolicies()
                .getLoadBalancingPolicy();
        final PoolingOptions poolingOptions = cluster.getConfiguration()
                .getPoolingOptions();

        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        scheduled.scheduleAtFixedRate(() -> {
            Session.State state = session.getState();
            for (Host host : state.getConnectedHosts()) {
                HostDistance distance = loadBalancingPolicy.distance(host);
                int connections = state.getOpenConnections(host);
                int inFlightQueries = state.getInFlightQueries(host);
                LOG.info("{} connections={}, current load={}, maxload={}", host, connections, inFlightQueries,
                        connections * poolingOptions.getMaxRequestsPerConnection(distance));
            }
        }, FIVE, FIVE, TimeUnit.SECONDS);
    }
     */

    @PreDestroy
    private void destroy() {
        session.close();
        cluster.close();
        LOG.info("Closed Cassandra connection");
    }

}
