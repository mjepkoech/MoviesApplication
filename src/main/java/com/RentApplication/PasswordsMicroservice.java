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
 * User password recovery
 * 
 * @author mjepkoech
 *
 */
public class PasswordsMicroservice extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("recover");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject data = message.body();

			System.out.println("I have received a message: " + data);
			try {

				String funcSP = "sp_resetPassword";

				String mobilenumber = data.getJsonObject("data").getJsonObject("transaction_details")
						.getString("mobilenumber").trim();
				String email = data.getJsonObject("data").getJsonObject("transaction_details").getString("email")
						.trim();
				JsonArray fields = new JsonArray().add(mobilenumber).add(email);

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
						System.out.println("Received reply from Session Mgr: " + responseFields);

						String to = mobilenumber;
						JsonArray params = responseFields.getJsonArray("params");

						JsonObject dbfields = new JsonObject();
						String otp = "";
						for (int i = 0; i < params.size(); i++) {
							dbfields = params.getJsonObject(i);
							if (dbfields.containsKey("otp")) {
								otp = dbfields.getString("otp");
								break;
							}
						}

						String sms = "Your OTP token for password reset is " + otp + ". ";
						JsonObject smsdata = new JsonObject().put("smsmessage", sms).put("mobilenumber", to);

						MessageProducer<JsonObject> otpsend = esbBus.publisher("notification", options);
						otpsend.send(smsdata, (AsyncResult<Message<JsonObject>> otpartxn) -> {
							if (otpartxn.succeeded()) {
								JsonObject res = otpartxn.result().body();
								System.out.println("Received reply from SMS Mgr: " + res);
								// message.reply(responseFields);
							} else {
								// message.fail(0, otpartxn.cause().getMessage());
								System.out.println("Received reply from SMS Mgr: " + otpartxn.cause().getMessage());
							}
						});

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
				System.out.println("Password reset handler registration has reached all nodes");
			} else {
				System.out.println("Password reset Registration failed!");
			}
		});
	}

}