package com.worldpay.vertx;

import java.nio.file.Files;
import java.nio.file.Paths;

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
public class BasicTest {

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

	@Test
	public void testBadEndpoint(TestContext context) {
		// This test is asynchronous, so get an async handler to inform the test
		// when we are done.
		final Async async = context.async();

		vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
			response.handler(body -> {
				context.assertEquals(response.statusCode(), 404);
				async.complete();
			});
		});
	}
	
	@Test
	public void testPostJson(TestContext context) throws Exception{
		// This test is asynchronous, so get an async handler to inform the test
		// when we are done.
		final Async async = context.async();
		
		ClassLoader classLoader = getClass().getClassLoader();
        String json = new String(Files.readAllBytes(Paths.get(classLoader.getResource("v1_test.json").getFile())));
        context.assertNotNull(json);
		//System.out.println("Payload: " + json);
		
		vertx.createHttpClient().post(8080, "localhost", "/post", response -> {
			response.handler(body -> {
				System.out.println("Body from POST: " + body);
				context.assertEquals("application/json", response.getHeader("content-type"));
				async.complete();
			});
		}).setChunked(true).end(json);
	}
	
	@Test
	public void testGetParameter(TestContext context) throws Exception{
		// This test is asynchronous, so get an async handler to inform the test
		// when we are done.
		final Async async = context.async();
		
		vertx.createHttpClient().get(8080, "localhost", "/get/{testParam}", response -> {
			response.handler(body -> {
				System.out.println("Body from GET: " + body);
				context.assertEquals("text/plain", response.getHeader("content-type"));
				async.complete();
			});
		}).setChunked(true).end();
	}
}
