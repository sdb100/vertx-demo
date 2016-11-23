package com.worldpay.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class APIVerticle extends AbstractVerticle {

	/**
	 * This method is called when the verticle is deployed. It creates a HTTP
	 * server and registers a simple request handler.
	 * <p/>
	 * Notice the `listen` method. It passes a lambda checking the port binding
	 * result. When the HTTP server has been bound on the port, it call the
	 * `complete` method to inform that the starting has completed. Else it
	 * reports the error.
	 *
	 * @param fut
	 *            the future
	 */
	@Override
	public void start(Future<Void> fut) {

		Router router = Router.router(vertx);
		router.get("/get/:param").handler(this::getHandler);
		router.post("/post").handler(BodyHandler.create());
		router.post("/post").handler(this::postHandler);

		vertx.createHttpServer().requestHandler(router::accept).listen(
				// Retrieve the port from the configuration,
				// default to 8080.
				config().getInteger("http.port", 8080), result -> {
					if (result.succeeded()) {
						fut.complete();
					} else {
						fut.fail(result.cause());
					}
				});
	}
	
	private void getHandler(RoutingContext routingContext) {
		String param = routingContext.request().getParam("param");
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "text/plain").setStatusCode(200);
		response.end(param);

	}
	
	private void postHandler(RoutingContext routingContext) {
		JsonObject json = routingContext.getBodyAsJson();
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "application/json").setStatusCode(200);
		response.end(json.encodePrettily());
	}
}
