package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.eventbus.MessageProducer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.jwt.JWTOptions;

/**
 * Verifies login credentials and adds tokens
 * 
 * @author mjepkoech
 *
 */
public class LoginMicroService extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("login");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject reqdata = message.body();

			System.out.println("LoginMicroservice received a message: " + reqdata);
			try {
				JsonObject data = reqdata.getJsonObject("data").getJsonObject("transaction_details");

				JsonArray fields = new JsonArray();

				String funcSP = "sp_userLogin";

				String mobilenumber = data.getString("mobilenumber").trim();
				String password = data.getString("password").trim();

				fields = new JsonArray().add(mobilenumber).add(password);
				System.out.println("my fields=" + fields);

				JsonObject authData = new JsonObject();
				authData.put("storedprocedure", funcSP);
				authData.put("params", fields);

				EventBus esbBus = vertx.eventBus();
				DeliveryOptions options = new DeliveryOptions();
				int time = 5000;
				options.setSendTimeout(time);
				MessageProducer<JsonObject> producer = esbBus.publisher("DATABASEACCESS", options);
				producer.send(authData, (AsyncResult<Message<JsonObject>> artxn) -> {
					if (artxn.succeeded()) {

						JsonObject responseFields = artxn.result().body();

						JsonArray params = responseFields.getJsonArray("params");

						JsonObject dbfields = new JsonObject();
						String username = "";
						int param_size = params.size();
						for (int i = 0; i < param_size; i++) {
							dbfields = params.getJsonObject(i);
							if (dbfields.containsKey("name")) {
								username = dbfields.getString("name");
								break;

							}
						}
						// Set default config
						JsonObject jwt = new JsonObject().put("keyStore", new JsonObject().put("path", "keystore.jceks")
								.put("type", "jceks").put("password", "secret"));
						JWTAuth jwtconfig = JWTAuth.create(vertx, jwt);

						String newtoken = jwtconfig.generateToken(
								new JsonObject().put("sub", data.getString("mobilenumber")).put("name", username),
								new JWTOptions().setExpiresInMinutes(60));

						responseFields.getJsonArray("params").getJsonObject(0).put("token", newtoken);

						System.out.println("Received reply from Session Manager: " + responseFields);

						JsonObject feedback = new JsonObject();
						feedback.put("data", params.getJsonObject(0));

						message.reply(feedback);
					} else {
						message.fail(0, artxn.cause().getMessage());

					}

				});
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Login handler has reached all nodes");
			} else {
				System.out.println("Login failed!");
			}
		});
	}

}