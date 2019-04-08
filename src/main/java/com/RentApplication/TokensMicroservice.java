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

/**
 * Tokens verification
 * 
 * @author mjepkoech
 *
 */
public class TokensMicroservice extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("tokens");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject data = message.body();

			System.out.println("Tokens microservice received a message: " + data);
			try {

				String funcSP = "sp_verifyOtp";
				String userId = data.getString("userId").trim();
				String otp = data.getString("otp").trim();
				JsonArray fields = new JsonArray().add(userId).add(otp);

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
						System.out.println("Received reply from Session Mgr: " + responseFields);

						JsonArray params = responseFields.getJsonArray("params");
						JsonObject feedback = new JsonObject();
						feedback.put("data", params.getJsonObject(0));

						message.reply(feedback);
					} else {
						message.fail(0, "No data");

					}

				});
			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Tokens handler registration has reached all nodes");
			} else {
				System.out.println("Tokens handler failed!");
			}
		});
	}

}