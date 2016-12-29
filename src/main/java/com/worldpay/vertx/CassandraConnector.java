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

    private static final String KEYSPACE = "create keyspace if not exists dev with replication = {'class':'SimpleStrategy', 'replication_factor':1}";
    private static final String DEV = "use dev";
    private static final String TABLE = "create table if not exists name_value( name text PRIMARY KEY, value text)";
 
    private Cluster cluster;
    private Session session;
    
    private String cassandraIP = "127.0.0.1";

    public CassandraConnector() {
        PoolingOptions poolingOptions = new PoolingOptions();
        poolingOptions.setConnectionsPerHost(HostDistance.LOCAL, 4, 10);
        poolingOptions.setMaxRequestsPerConnection(HostDistance.LOCAL, 10000);
        
        String ip = System.getenv("CASSANDRA_CDK_SERVICE_HOST"); // Magical value from openshift
        if(ip != null){
            this.cassandraIP = ip;
        }
        
        cluster = Cluster.builder()
                .addContactPoint(this.cassandraIP)
                .withLoadBalancingPolicy(new RoundRobinPolicy())
                .withPoolingOptions(poolingOptions)
                .build();
        
        session = cluster.connect();
        session.execute(KEYSPACE);
        session.execute(DEV);
        session.execute(TABLE);
        session.close();
        
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
