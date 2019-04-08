package com.RentApplication;

import java.util.HashSet;
import java.util.Set;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.api.impl.RequestParametersImpl;
import io.vertx.ext.web.api.validation.HTTPRequestValidationHandler;
import io.vertx.ext.web.api.validation.ParameterType;
import io.vertx.ext.web.handler.CorsHandler;

/**
 * 
 * This is the gateway class
 * 
 *
 */
public class APIGateway extends AbstractVerticle {
	

	@Override
	public void start() throws Exception {
		System.out.println("deploymentId =" + vertx.getOrCreateContext().deploymentID());
		int port = 7453;

		Router router = Router.router(vertx);
		HTTPRequestValidationHandler validationHandler = HTTPRequestValidationHandler.create().addPathParam("parameter",
				ParameterType.GENERIC_STRING);

		Set<String> allowedHeaders = new HashSet<>();
		allowedHeaders.add("x-requested-with");
		allowedHeaders.add("Access-Control-Allow-Origin");
		allowedHeaders.add("Access-Control-Allow-Headers");
		allowedHeaders.add("Access-Control-Allow-Methods");
		allowedHeaders.add("origin");
		allowedHeaders.add("Content-Type");
		allowedHeaders.add("accept");
//		allowedHeaders.add("multipart/form-data");
		allowedHeaders.add("X-PINGARUNER");

		router.route().handler(CorsHandler.create("*").allowedHeaders(allowedHeaders).allowedMethod(HttpMethod.POST)
				.allowedMethod(HttpMethod.GET));


//		router.post("/uploads").handler(context -> {
//		    context.response().headers().set("Content-Type", "text/plain");
//		    context.response().end(context.getBody()); // returns request body (uploaded files, fields, metadata)
//		});
		router.post("/:parameter").handler(validationHandler).handler(this::calloperations);

        HttpServer server = vertx.createHttpServer();
		server.requestHandler(router::accept);
		server.listen(port,  res -> {
			if (res.succeeded()) {
				System.out.println("ESB Rest API Server is now listening at :" + port);
			} else {
				System.out.println("Failed to bind ESB Rest API!");
			}
		});
	}
	
	/**
	 * AUTHENTICATES CHANNEL
	 * CALLS OPERATION EG REGISTER, LOGIN, TENANT, LANDLORD, ADMIN
	 * @param routing
	 */

	public void calloperation (RoutingContext routing) {
		routing.request().bodyHandler(request -> {

			RequestParametersImpl urlparams = routing.get("parsedParameters");
			String path = urlparams.pathParameter("parameter").toString();

			JsonObject body = request.toJsonObject();

			HttpServerResponse response = routing.response().putHeader("Content-Type", "application/json");
			try {
				JsonObject data = body;

				// 1. AUTHENTICATE CHANNEL

//				System.out.println("DATA FROM CHANNEl: " + request);

				EventBus esbBus = vertx.eventBus();
				DeliveryOptions options = new DeliveryOptions();
				int time = 5000;
				options.setSendTimeout(time);
				MessageProducer<JsonObject> producer = esbBus.publisher("AUTHAPI", options);

				producer.send(data, (AsyncResult<Message<JsonObject>> artxn) -> {
					if (artxn.succeeded()) {
						String responseFields = artxn.result().body().toString();
						System.out.println("Received reply from auth Mgr: " + responseFields);

						MessageProducer<JsonObject> producerOps = esbBus.publisher(path, options);

						producerOps.send(data, (AsyncResult<Message<JsonObject>> artxnOps) -> {

							if (artxnOps.succeeded()) {
								JsonObject responseDataOps = artxnOps.result().body();
								System.out.println("Received reply from Operation Microservices: " + responseDataOps);

								String f39 = "1";
								responseDataOps.put("status", f39);

								response.end(responseDataOps.toString());
								response.close();

							} else {

								JsonObject feedback = new JsonObject();
								JsonObject params = new JsonObject();

								String f48 = artxnOps.cause().getMessage();
//								Integer f49 = 0;
								String f57 = "0";
								feedback.put("status", f57);
//								params.put("id", f49);
								params.put("message", f48);
								feedback.put("data", params);

								response.end(feedback.toString());
								response.close();

							}
						});

					} else {
						String f48 = artxn.cause().getMessage();
						String f39 = "57";
						data.put("39", f39);
						data.put("48", f48);

						response.end(data.toString());
						response.close();

					}

				});

			} catch (Exception ex) {
				response.end(ex.getMessage());
			}

		});
	}
	
	
	public void calloperations(RoutingContext routing) {
		routing.request().bodyHandler(request -> {

			RequestParametersImpl urlparams = routing.get("parsedParameters");
			String path = urlparams.pathParameter("parameter").toString();

			JsonObject body = request.toJsonObject();

			HttpServerResponse response = routing.response().putHeader("Content-Type", "application/json");
			try {
				JsonObject data = body;

				// 1. AUTHENTICATE CHANNEL

//				System.out.println("DATA FROM CHANNEl: " + request);

				EventBus esbBus = vertx.eventBus();
				DeliveryOptions options = new DeliveryOptions();
				int time = 5000;
				options.setSendTimeout(time);
				MessageProducer<JsonObject> producer = esbBus.publisher("AUTHAPI", options);

				producer.send(data, (AsyncResult<Message<JsonObject>> artxn) -> {
					if (artxn.succeeded()) {
						String responseFields = artxn.result().body().toString();
						System.out.println("Received reply from auth Mgr: " + responseFields);

						MessageProducer<JsonObject> producerOps = esbBus.publisher(path, options);

						producerOps.send(data, (AsyncResult<Message<JsonObject>> artxnOps) -> {

							if (artxnOps.succeeded()) {
								JsonObject responseDataOps = artxnOps.result().body();
								System.out.println("Received reply from Operation Microservices: " + responseDataOps);

								String f39 = "1";
								responseDataOps.put("status", f39);

								response.end(responseDataOps.toString());
								response.close();

							} else {

								JsonObject feedback = new JsonObject();
								JsonObject params = new JsonObject();

								String f48 = artxnOps.cause().getMessage();
//								Integer f49 = 0;
								String f57 = "0";
								feedback.put("status", f57);
//								params.put("id", f49);
								params.put("message", f48);
								feedback.put("data", params);

								response.end(feedback.toString());
								response.close();

							}
						});

					} else {
						String f48 = artxn.cause().getMessage();
						String f39 = "57";
						data.put("39", f39);
						data.put("48", f48);

						response.end(data.toString());
						response.close();

					}

				});

			} catch (Exception ex) {
				response.end(ex.getMessage());
			}

		});
	}
}
