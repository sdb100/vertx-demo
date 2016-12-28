package com.worldpay.vertx;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * A very simple demo application for vertx. Provides get and post endpoints and
 * listens on 8080.
 * 
 * @author Steve
 *
 */
public class APIVerticle extends AbstractVerticle {

    private static final Logger LOGGER = Logger.getLogger(AbstractVerticle.class.getName());

    private static final String NAME = "name";
    private static final String VALUE = "value";

    private CassandraConnector connector;

    private Vertx vertx;
    private Context context;

    private long counter = 0;

    /**
     * Start the server. This sets up routes and starts listening on a port.
     * Note that the server creation is asynchronous so the Future must be told
     * when the server is ready.
     */
    @Override
    public void start(Future<Void> fut) {
        this.vertx = this.getVertx();
        this.context = vertx.getOrCreateContext();

        try {
            LOGGER.info("Cassandra host: " + System.getenv("OPENSHIFT_CASSANDRA-CDK_HOST"));
            this.connector = new CassandraConnector();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Cassandra failed to start", e);
        }

        int port = config().getInteger("http.port", 8080);
        LOGGER.info(MessageFormat.format("Starting server in directory: {0} on port {1}...",
                System.getProperty("user.dir"), port));

        Router router = Router.router(vertx);
        router.get("/ping").handler(rc -> {
            rc.response().putHeader("content-type", "text/plain").setStatusCode(200).end("pong");
        });
        router.get("/get/:param").handler(this::getHandler);
        router.get("/put/:param").handler(this::putHandler);
        router.post("/post").handler(BodyHandler.create());
        router.post("/post").handler(this::postHandler);

        vertx.createHttpServer().requestHandler(router::accept).listen(port, result -> {
            if (result.succeeded()) {
                LOGGER.info("Server start complete.");
                fut.complete();
            } else {
                LOGGER.info("Server start failed.");
                fut.fail(result.cause());
            }
        });
    }

    /**
     * A route handler for the simple get operation. This executes the given
     * string as a Cassandra CQL statement.
     * 
     * @param routingContext
     *            standard context data for the route.
     */
    private void getHandler(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        LOGGER.info(MessageFormat.format("GET request running on {0}: {1}", Thread.currentThread(), ++this.counter));
        if (this.connector == null) {
            response.end("Cassandra not running");
        } else {
            String statement = routingContext.request().getParam("param");
            Session session = this.connector.getSession();

            ListenableFuture<ResultSet> resultSetFuture = session.executeAsync(statement);
            CompletableFuture<String> f = new CompletableFuture<>();

            Futures.addCallback(resultSetFuture, new FutureCallback<ResultSet>() {
                public void onSuccess(ResultSet r) {
                    LOGGER.info(MessageFormat.format("GET callback running on {0}: {1}", Thread.currentThread(), counter));

                    List<Row> rows = r.all();

                    String result = rows.stream()
                            .collect(StringBuffer::new, 
                                    (sb, row) -> sb.append(row.getString(NAME)).append(":")
                                        .append(row.getString(VALUE)).append("\n"), 
                                    (sb1, sb2) -> sb1.append(sb2))
                            .toString();

                    response.putHeader("content-type", "text/plain").setStatusCode(200);
                    response.end(result);

                    f.complete(result); // surplus to requirements really
                }

                public void onFailure(Throwable thrown) {
                    LOGGER.info(MessageFormat.format("GET callback running on {0}: {1}", Thread.currentThread(), counter));
                    response.setStatusCode(500).end(thrown.getMessage());
                    f.completeExceptionally(thrown); // not really needed
                }
            }, (r) -> context.runOnContext((v) -> r.run())); // run on the verticle event loop thread
        }
    }

    /**
     * A route handler for the simple put operation. This puts the parameter in
     * the Cassandra DB, which is expected to have keyspace dev and table
     * name_value.
     * 
     * @param routingContext
     *            standard context data for the route.
     */
    private void putHandler(RoutingContext routingContext) {
        String param = routingContext.request().getParam("param");
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "text/plain").setStatusCode(200);
        response.end(param);

    }

    /**
     * A route handler for the post operation. This bounces the post block back
     * to the caller as json, with mime type application/json. There's no
     * special error handling, so nonsense in will yield nonsense out.
     * 
     * @param routingContext
     *            standard context data for the route.
     */
    private void postHandler(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        HttpServerResponse response = routingContext.response();
        response.putHeader("content-type", "application/json").setStatusCode(200);
        response.end(json.encodePrettily());
    }

}
