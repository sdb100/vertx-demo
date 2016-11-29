package com.worldpay.vertx;

import java.util.logging.Logger;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

/**
 * A very simple demo application for vertx. Provides get and post endpoints 
 * and listens on 8080.
 * 
 * @author Steve
 *
 */
public class APIVerticle extends AbstractVerticle {

	private static final Logger LOGGER = Logger.getLogger(AbstractVerticle.class.getName());

	/**
	 * Start the server. This sets up routes and starts listening on a port.
	 * Note that the server creation is asynchronous so the Future must be told
	 * when the server is ready.
	 */
	@Override
	public void start(Future<Void> fut) {

		LOGGER.info("Starting server in directory: " + System.getProperty("user.dir") + "...");

		Router router = Router.router(vertx);
		router.get("/ping").handler(rc -> {
			rc.response().putHeader("content-type", "text/plain").setStatusCode(200).end("pong");
		});
		router.get("/get/:param").handler(this::getHandler);
		router.post("/post").handler(BodyHandler.create());
		router.post("/post").handler(this::postHandler);

		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8080), result -> {
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
	 * A route handler for the simple get operation. This just bounces the 
	 * parameter back to the caller as text.
	 * 
	 * @param routingContext standard context data for the route.
	 */
	private void getHandler(RoutingContext routingContext) {
		String param = routingContext.request().getParam("param");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "text/plain").setStatusCode(200);
		response.end(param);

	}
	
	/**
	 * A route handler for the post operation. This bounces the post block back to the caller as
	 * json, with mime type application/json. There's no special error handling, so nonsense in will
	 * yield nonsense out. 
	 * 
	 * @param routingContext standard context data for the route.
	 */
	private void postHandler(RoutingContext routingContext) {
		JsonObject json = routingContext.getBodyAsJson();
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json").setStatusCode(200);
		response.end(json.encodePrettily());
	}
}
