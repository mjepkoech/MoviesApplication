package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

/**
 * Sends SMS to users
 * 
 * @author mjepkoech
 *
 */
public class NotificationsMicroservice extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("notification");
		consumer.handler((Message<JsonObject> message) -> {
			JsonObject data = message.body();

			System.out.println("Notifications handler received a message: " + data);
			try {

				WebClient client = WebClient.create(vertx, new WebClientOptions().setSsl(true));

				String to = data.containsKey("mobilenumber") ? data.getString("mobilenumber").trim() : "";
				String sms = data.containsKey("smsmessage") ? data.getString("smsmessage").trim() : "";
				String gwdata = "to=" + to + "&message=" + sms + "&from=ECLECTICS&transactionID=5678889999";

				client.get(8443, "testgateway.ekenya.co.ke", "/ServiceLayer/pgsms/send?" + gwdata).send(responseAr -> {
					if (responseAr.succeeded()) {

						HttpResponse<Buffer> response = responseAr.result();
						System.out.println("Got HTTP response with status " + response.statusCode());
						System.out.println("Got Response: " + response.bodyAsString());

						JsonObject responseFields = new JsonObject(response.bodyAsString());
						String f39 = responseFields.getString("ResultCode");
						String f48 = responseFields.getString("ResultDesc");
						data.put("39", f39);
						data.put("48", f48);
						message.reply(data);

					} else {
						responseAr.cause().printStackTrace();
						message.fail(0, responseAr.cause().getMessage());
					}
				});

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("Notifications handler registration has reached all nodes");
			} else {
				System.out.println("Notifications Registration failed!");
			}
		});
	}

}