package com.worldpay.vertx;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we
 * declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class CassandraTest {

    @Rule
    public Timeout rule = Timeout.seconds(5);

    private Vertx vertx;

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it
     * waits in the verticle has successfully completed its start sequence
     * (thanks to `context.asyncAssertSuccess`).
     *
     * @param context
     *            the test context.
     */
    @Before
    public void setUp(TestContext context) {

        vertx = Vertx.vertx();
        vertx.deployVerticle(APIVerticle.class.getName(), context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing
     * the vert.x instance
     *
     * @param context
     *            the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    // assumes there's a running cassandra set to use the dev keyspace with a
    // name/value table - I haven't mocked it with cassandraunit yet
    @Test
    public void testGetParameter(TestContext context) throws Exception {
        // This test is asynchronous, so get an async handler to inform the test
        // when we are done.
        final Async async = context.async();

        vertx.createHttpClient().get(8080, "localhost", "/get/select%20*%20from%20name_value", response -> {
            response.handler(body -> {
                System.out.println("Body from GET: " + body);
                
                // TODO
                
                async.complete();
            });
        }).setChunked(true).end();
    }


}
