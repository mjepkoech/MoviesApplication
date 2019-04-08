package com.RentApplication;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Class handling mpesa callbacks from the payment gateway
 * @author mjepkoech
 *
 */
public class MpesaCheckoutCallbackMS extends AbstractVerticle {

	@Override
	public void start() throws Exception {
		EventBus eb = vertx.eventBus();

		MessageConsumer<JsonObject> consumer = eb.consumer("mpesaCheckoutCallback");
		consumer.handler((Message<JsonObject> message) -> {

			JsonObject reqdata = message.body();
			String operation = "add";

			System.out.println("Received reply from Payment Gateway: " + reqdata);

			try {
				JsonArray fields = new JsonArray();
				String funcSP = "sp_updateTransaction";

				String id = reqdata.getString("transactionID").trim();
				String status = reqdata.getString("status").trim();
				String status_desc = reqdata.getString("statusDescription").trim();
				String mobile_number = reqdata.getString("sourceAccountNo").trim();

				fields = new JsonArray().add(id).add(status).add(status_desc).add(mobile_number);

				EventBus esbBus = vertx.eventBus();

				new DatabaseService().callDatabase(funcSP, fields, operation, message, esbBus);

			} catch (Exception ex) {
				message.fail(0, ex.getMessage());
			}

		});

		consumer.completionHandler(res -> {
			if (res.succeeded()) {
				System.out.println("MpesaCheckout Callback handler has reached all nodes");
			} else {
				System.out.println("MpesaCheckout Callback handler failed!");
			}
		});
	}

}