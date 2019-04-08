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
 * Authenticates the channel
 * 
 * @author mjepkoech
 *
 */
public class ChannelAuthenticator extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("AUTHAPI");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject data = message.body();
			System.out.println("I have received a message: " + data);
			String funcSP = "sp_authenticateapi";
			// JsonArray fields = data.getJsonArray("params");
			JsonArray fields = new JsonArray().add("milly").add("WAP").add("pass123");
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

					message.reply(responseFields);

				} else {
					message.fail(0, "No data");
				}
			});
		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("CLIENTAUTH handler registration has reached all nodes");
			} else {
				System.out.println("CLIENTAUTH Registration failed!");
			}
		});
	}

}